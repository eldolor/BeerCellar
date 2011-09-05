package com.cm.beer.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.Note;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.facebook.BaseRequestListener;
import com.cm.beer.facebook.LoginButton;
import com.cm.beer.facebook.SessionEvents;
import com.cm.beer.facebook.SessionStore;
import com.cm.beer.facebook.SessionEvents.AuthListener;
import com.cm.beer.facebook.SessionEvents.LogoutListener;
import com.cm.beer.util.User;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ShareWithCommunity extends Activity {
	String TAG;
	static final int MENU_GROUP = 0;
	static final int SEND_ERROR_REPORT_ID = Menu.FIRST;
	static final int SIGN_IN_REQUEST = 1;

	LoginButton mLoginButton;
	TextView mText;
	Button mCommunityLoginButton;
	Button mPostAnonymously;
	Button mDoNotShare;

	Facebook mFacebook;
	AsyncFacebookRunner mAsyncRunner;

	ProgressDialog dialog;
	int ACTIVE_DIALOG;

	// Stateful Field
	Long mRowId;
	String mAction;
	/*
	 * Defaults to false
	 */
	boolean mUploadPhoto = false;
	/*
	 * Defaults to true
	 */
	String mIntercept = AppConfig.SHARE_WITH_COMMUNITY_INTERCEPT;

	GoogleAnalyticsTracker mTracker;
	NotesDbAdapter mDbHelper;
	Activity mMainActivity;

	Note mNote;
	Intent mOriginalIntent;
	User mUser;

	/**
	 * Intercepts by default unless instructed not to. Interception is triggered
	 * when a valid user id is not found.
	 * 
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();

		if (AppConfig.LOGGING_ENABLED) {
			Log.d(TAG, "onCreate: ");
		}
		mMainActivity = this;

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}
		mUser = new User(this);
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();

		mOriginalIntent = getIntent();

		Bundle extras = getIntent().getExtras();

		mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
				: null;
		mAction = extras != null ? extras.getString("ACTION") : null;

		// if not found then default to false
		mUploadPhoto = extras != null ? extras.getBoolean("UPLOAD_PHOTO")
				: false;

		// if not found then default to true
		mIntercept = extras != null ? extras.getString("INTERCEPT")
				: AppConfig.SHARE_WITH_COMMUNITY_INTERCEPT;

		Log.i(TAG, "onCreate::mRowId=" + mRowId + " mAction=" + mAction
				+ " mUploadPhoto=" + ((mUploadPhoto) ? "true" : "false")
				+ " mIntercept=" + mIntercept);

		// check for action missing.
		if (mAction == null) {
			Log.d(TAG, "onCreate::Received No Action. Ending Activity!");
			finish();
		}
		// Fetch the note
		Cursor cursor = null;
		try {
			cursor = mDbHelper.fetchNote(mRowId);
			mNote = com.cm.beer.util.Util.toNote(cursor);
			// For Delete to Work;
			if (mNote == null) {
				mNote = new Note();
				mNote.id = mRowId;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		if (mIntercept != null) {
			// if instructed not to intercept then...
			if (mIntercept
					.equalsIgnoreCase(AppConfig.SHARE_WITH_COMMUNITY_DO_NOT_INTERCEPT)) {
				Log.d(TAG, "onCreate::Instructed not to Intercept!");
				new AsyncShareWithCommunity().execute(mAction, mUploadPhoto,
						mNote);
				mMainActivity.finish();
			} else {

				// If user id exists then execute...
				if (mUser.isLoggedIn()) {
					mNote = updateUser(mDbHelper, mUser.getUserId(), mUser
							.getUserName(), mUser.getUserLink());
					new AsyncShareWithCommunity().execute(mAction,
							mUploadPhoto, mNote);
					mMainActivity.finish();
				} else {
					Log.d(TAG, "onCreate::Intercepting!");
					setContentView(R.layout.share_with_community);
					mLoginButton = (LoginButton) findViewById(R.id.login);
					mText = (TextView) ShareWithCommunity.this
							.findViewById(R.id.txt);

					mCommunityLoginButton = (Button) findViewById(R.id.communityLoginButton);
					mCommunityLoginButton.getBackground().setColorFilter(
							AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);

					mPostAnonymously = (Button) findViewById(R.id.postAnonymouslyButton);
					mPostAnonymously.getBackground().setColorFilter(
							AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
					mDoNotShare = (Button) findViewById(R.id.skipSharingButton);
					mDoNotShare.getBackground().setColorFilter(
							AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);

					mFacebook = new Facebook();
					mAsyncRunner = new AsyncFacebookRunner(mFacebook);
					SessionStore.restore(mFacebook, this);
					SessionEvents.addAuthListener(new BeerAuthListener());
					SessionEvents.addLogoutListener(new BeerLogoutListener());
					mLoginButton.init(mFacebook, new String[] {});

					mCommunityLoginButton
							.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									if (AppConfig.LOGGING_ENABLED) {
										Log
												.d(TAG,
														"mCommunityLoginButton.setOnClickListener");
									}
									mTracker.trackEvent("ShareWithCommunity",
											"CommunitySignIn", "Clicked", 0);
									mTracker.dispatch();
									Log.i(TAG,
											"mCommunityLoginButton:onClick:userId:"
													+ mNote.userId);

									Intent intent = new Intent(mMainActivity,
											CommunitySignIn.class);
									intent
											.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
									intent.putExtra(NotesDbAdapter.KEY_ROWID,
											mRowId);
									intent.putExtra("ACTION",
											AppConfig.ACTION_INSERT);
									startActivityForResult(intent,
											SIGN_IN_REQUEST);
								}
							});

					mPostAnonymously.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							if (AppConfig.LOGGING_ENABLED) {
								Log.d(TAG,
										"mPostAnonymously.setOnClickListener");
							}
							mTracker.trackEvent("ShareWithCommunity",
									"ShareAnonymously", "Clicked", 0);
							mTracker.dispatch();
							Log.i(TAG, "mPostAnonymously:onClick:userId:"
									+ mNote.userId);
							// update user info to anonymous
							mNote = updateUser(mDbHelper, " ", " ", " ");
							new AsyncShareWithCommunity().execute(mAction,
									mUploadPhoto, mNote);
							Intent data = new Intent();
							mMainActivity.setResult(RESULT_OK, data);
							mMainActivity.finish();
						}
					});
					mDoNotShare.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							if (AppConfig.LOGGING_ENABLED) {
								Log.d(TAG, "mDoNotShare.setOnClickListener");
							}
							mTracker.trackEvent("ShareWithCommunity",
									"DoNotShare", "Clicked", 0);
							mTracker.dispatch();
							// update note
							Note note = new Note();
							note.id = mRowId;
							note.share = "N";
							mDbHelper.updateNote(note);
							mMainActivity.finish();
						}
					});
				}
			}
		}// end if (mIntercept != null)

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		Bundle extras = (intent != null) ? intent.getExtras() : null;
		if (resultCode == RESULT_OK) {
			// update user info
			mNote = updateUser(mDbHelper, mUser.getUserId(), mUser
					.getUserName(), mUser.getUserLink());
			new AsyncShareWithCommunity().execute(mAction, mUploadPhoto, mNote);
			Intent data = new Intent();
			mMainActivity.setResult(RESULT_OK, data);
			mMainActivity.finish();
		}
	}

	/**
	 * 
	 * @param userId
	 * @param userName
	 * @param userLink
	 * @return
	 */
	private Note updateUser(NotesDbAdapter dbHelper, String userId,
			String userName, String userLink) {
		Note _note = new Note();
		_note.id = mNote.id;
		_note.userId = userId;
		_note.userName = userName;
		_note.userLink = userLink;
		dbHelper.updateNote(_note);

		// refresh from database
		Cursor _cursor = null;
		try {
			_cursor = dbHelper.fetchNote(mRowId);
			_note = com.cm.beer.util.Util.toNote(_cursor);
			// For Delete to Work;
			if (mNote == null) {
				mNote = new Note();
				mNote.id = mRowId;
			}
		} finally {
			if (_cursor != null) {
				_cursor.close();
			}
		}
		return _note;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onDestroy");
		}
		// Stop the tracker when it is no longer needed.
		mTracker.stop();
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Stopped!");
		}
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "finish");
		}
		// database closed in share with community async task
		if (mDbHelper != null) {
			// close the Db connection
			mDbHelper.close();
		}
		super.finish();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreateOptionsMenu");
		}
		super.onCreateOptionsMenu(menu);
		int position = 0;
		if (AppConfig.DEFAULT_APPSTORE.equals(AppConfig.GOOGLE_APPSTORE)) {
			menu.add(MENU_GROUP, SEND_ERROR_REPORT_ID, position++,
					R.string.menu_send_error_report);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onMenuItemSelected");
		}
		switch (item.getItemId()) {
		case SEND_ERROR_REPORT_ID:
			mTracker.trackEvent("ShareWithCommunity", "SendErrorReport",
					"Clicked", 0);
			mTracker.dispatch();
			sendErrorReport();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * 
	 */
	private void sendErrorReport() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "Send Error Report");
		}
		Intent intent = new Intent(ShareWithCommunity.this.getApplication(),
				CollectAndSendLog.class);
		// intent.putExtra("LOCATION", selectedLocation);
		startActivity(intent);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.app.Activity#onConfigurationChanged(android.content.res.Configuration
	 * )
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// DO NOTHING
		super.onConfigurationChanged(newConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onResume");
		}
		if ((dialog != null) && (dialog.isShowing())) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onResume:active dialog removed");
			}
			removeDialog(ACTIVE_DIALOG);
		}
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreateDialog");
		}
		String dialogMessage = null;
		if (id == AppConfig.DIALOG_POSTING_ID) {
			dialogMessage = this.getString(R.string.posting_dialog_message);
			ACTIVE_DIALOG = AppConfig.DIALOG_POSTING_ID;
		}
		dialog = ProgressDialog.show(this, null, dialogMessage, true, true);
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	/************************************************************************************/

	public class BeerAuthListener implements AuthListener {

		public void onAuthSucceed() {
			if (AppConfig.LOGGING_ENABLED) {
				Log.d(TAG, "VVWAuthListener: onAuthSucceed");
			}
			mTracker.trackEvent("ShareWithCommunity", "FacebookLogin", "Y", 0);
			mTracker.dispatch();
			// mText.setText(R.string.on_facebook_login);
			// Get user profile data
			mAsyncRunner.request("me", new Bundle(), "GET",
					new GetUserProfileRequestListener());

		}

		public void onAuthFail(String error) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.d(TAG, "VVWAuthListener: onAuthFail");
			}
			mText.setText("Login Failed: " + error);
		}
	}

	/************************************************************************************/
	public class BeerLogoutListener implements LogoutListener {
		public void onLogoutBegin() {
			if (AppConfig.LOGGING_ENABLED) {
				Log.d(TAG, "VVWLogoutListener: onLogoutBegin");
			}
			Toast.makeText(ShareWithCommunity.this,
					R.string.on_facebook_logout_begin, Toast.LENGTH_SHORT)
					.show();
			mText.setText(R.string.on_facebook_logout_begin);

		}

		public void onLogoutFinish() {
			if (AppConfig.LOGGING_ENABLED) {
				Log.d(TAG, "VVWLogoutListener: onLogoutFinish");
			}
			mTracker.trackEvent("ShareWithCommunity", "FacebookLogout", "Y", 0);
			mTracker.dispatch();
			Toast.makeText(ShareWithCommunity.this,
					R.string.on_facebook_logout, Toast.LENGTH_LONG).show();
			mText.setText(R.string.on_facebook_logout);

			mUser.onLogoutFinish();
		}
	}

	/************************************************************************************/
	public class GetUserProfileRequestListener extends BaseRequestListener {

		public void onComplete(final String response) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.d(TAG, "GetUserProfileRequestListener: onComplete "
						+ response.toString());
			}
			Log.d(TAG, "Got response: " + response);

			try {
				JSONObject json = Util.parseJson(response);

				mUser.onAuthSucceed(json.getString("id"), json
						.getString("name"), json.getString("link"),
						AppConfig.USER_TYPE_COMMUNITY);

				final String text = mUser.getUserName() + " "
						+ mMainActivity.getString(R.string.on_facebook_login);
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mText.setText(text);
					}
				});

				mNote = updateUser(mDbHelper, mUser.getUserId(), mUser
						.getUserName(), mUser.getUserLink());

				// Execute the original request
				new AsyncShareWithCommunity().execute(mAction, mUploadPhoto,
						mNote);
				try {
					String email = json.getString("email");
					Log.i(TAG, "User email: " + email);
					JSONObject additionalAttributes = new JSONObject();
					additionalAttributes.put("email", email);
					mUser.setAdditionalUserAttributes(additionalAttributes
							.toString());

					new AsyncUploadUserProfile().execute(mUser.getUserId(),
							mUser.getUserName(), mUser.getUserLink(), mUser
									.getAdditionalUserAttributes());

				} catch (JSONException e) {
					Log.e(TAG, "Email address is not available");
				}

			} catch (JSONException e) {
				Log.e(TAG, "JSON Error in response");
			} catch (FacebookError e) {
				Log.e(TAG, "Facebook Error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""));
			}

			if ((dialog != null) && (dialog.isShowing())) {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "GetUserProfileRequestListener: onComplete");
				}
				removeDialog(ACTIVE_DIALOG);
			}

			// End the activity
			Intent data = new Intent();
			mMainActivity.setResult(RESULT_OK, data);
			mMainActivity.finish();

		}

		@Override
		public void onFacebookError(FacebookError e) {
			if ((dialog != null) && (dialog.isShowing())) {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "GetWallPostRequestListener: onFacebookError");
				}
				removeDialog(ACTIVE_DIALOG);
			}
			final String text = "Unable to retrieve User Profile from Facebook. "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : "");
			ShareWithCommunity.this.runOnUiThread(new Runnable() {
				public void run() {
					mText.setText(text);
				}
			});
			super.onFacebookError(e);
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e) {
			handleError(e);
			super.onFileNotFoundException(e);
		}

		@Override
		public void onIOException(IOException e) {
			handleError(e);
			super.onIOException(e);
		}

		@Override
		public void onMalformedURLException(MalformedURLException e) {
			handleError(e);
			super.onMalformedURLException(e);
		}

		private void handleError(Exception e) {
			Log.e(TAG, "Facebook Error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			if ((dialog != null) && (dialog.isShowing())) {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "onFacebookError: handleError");
				}
				removeDialog(ACTIVE_DIALOG);
			}
		}

	}

	private class AsyncShareWithCommunity extends AsyncTask<Object, Void, Void> {

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... params) {
			String response = "";

			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "doInBackground starting");
			}
			try {
				String _q = (String) params[0];
				// to do multipart posts
				boolean _uploadPhoto = (Boolean) params[1];
				Note note = (Note) params[2];
				Log.i(TAG, "AsyncShareWithCommunity:doInBackground:userId:"
						+ mNote.userId);

				Log.i(TAG, "q:" + _q + "::" + "row id:" + note.id);
				JSONObject _beerJson = com.cm.beer.util.Util.toBeerJson(note);
				String beerJsonStr = _beerJson.toString();
				beerJsonStr = URLEncoder.encode(beerJsonStr, "UTF-8");
				Log.i(TAG, beerJsonStr);

				HashMap<String, String> parameters = new HashMap<String, String>();
				parameters.put("q", _q);
				parameters.put("beer", beerJsonStr);

				// picture taken and q was not delete
				if (_uploadPhoto) {
					setPhoto(parameters, note.id);
				}
				// Prepare a request object
				String _url = AppConfig.COMMUNITY_BEER_UPLOAD_URL;
				Log.i(TAG, _url);
				{
					boolean retry = true;
					int retryCount = 0;
					while ((retry)
							&& (retryCount < AppConfig.SHARE_WITH_COMMUNITY_BEER_UPLOAD_RETRY_COUNT)) {
						try {

							response = com.cm.beer.util.Util.openUrl(_url,
									"POST", parameters);
							// Upload successful
							retry = false;
						} catch (Throwable e) {
							Log.e(TAG, "error: "
									+ ((e.getMessage() != null) ? e
											.getMessage().replace(" ", "_")
											: ""), e);
							// increment retry count
							retryCount++;
							Log.e(TAG, "Retrying... Retry Count = "
									+ retryCount);
						}
					}
					Log.d(TAG, "Final Retry Count = " + retryCount);
					if (retryCount > 0) {
						mTracker.trackEvent("ShareWithCommunity", "ShareBeer",
								"RetryCount", retryCount);
						mTracker.dispatch();
					}
				}

				// Examine the response status
				Log.i(TAG, "Response = " + response);
				// if response == ACTION_INSERT => Send complete update
				if (response.equalsIgnoreCase(AppConfig.ACTION_INSERT)) {
					Log.i(TAG, "Server has requested "
							+ AppConfig.ACTION_INSERT);
					parameters.put("q", AppConfig.ACTION_INSERT);
					setPhoto(parameters, note.id);
					{
						boolean retry = true;
						int retryCount = 0;
						while ((retry)
								&& (retryCount < AppConfig.SHARE_WITH_COMMUNITY_BEER_UPLOAD_RETRY_COUNT)) {
							try {
								String _response = com.cm.beer.util.Util
										.openUrl(_url, "POST", parameters);
								// Examine the response status
								Log.i(TAG, "Response = " + _response);
								// Upload successful
								retry = false;
							} catch (Throwable e) {
								Log.e(TAG, "error: "
										+ ((e.getMessage() != null) ? e
												.getMessage().replace(" ", "_")
												: ""), e);
								// increment retry count
								retryCount++;
								Log.e(TAG, "Retrying... Retry Count = "
										+ retryCount);
							}
						}
						Log.d(TAG, "Final Retry Count = " + retryCount);
						if (retryCount > 0) {
							mTracker.trackEvent("ShareWithCommunity",
									"ShareBeer", "RetryCount", retryCount);
							mTracker.dispatch();
						}
					}
				}

			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("ShareWithCommunity", "ShareBeerError", ((e
						.getMessage() != null) ? e.getMessage().replace(" ",
						"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "doInBackground finished");
			}
			return null;
		}

		private void setPhoto(HashMap parameters, long _mRowId) {
			File thumbnailsDir = new File(AppConfig.PICTURES_THUMBNAILS_DIR);
			File thumbnail = new File(thumbnailsDir, mRowId
					+ AppConfig.PICTURES_THUMBNAILS_EXTENSION);

			if (thumbnail != null && thumbnail.exists()) {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "doInBackground::Found Thumbnail "
							+ thumbnail.getPath());
				}
				parameters.put("file", AppConfig.PICTURES_THUMBNAILS_DIR
						+ AppConfig.PATH_SEPARATOR + _mRowId
						+ AppConfig.PICTURES_THUMBNAILS_EXTENSION);
			}

		}

		@Override
		protected void onPostExecute(Void result) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPostExecute starting");
			}
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPostExecute finished");
			}
			super.onPostExecute(result);
		}

	}

	/**************************************************************************************/
	private class AsyncUploadUserProfile extends AsyncTask<Object, Void, Void> {

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... params) {
			String response = "";

			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "doInBackground starting");
			}
			try {
				String userId = (String) params[0];
				String userName = (String) params[1];
				String userLink = (String) params[2];
				String additionalAttributes = (String) params[3];

				JSONObject json = new JSONObject();
				json.put("userId", userId);
				json.put("userName", userName);
				json.put("userLink", userLink);
				json.put("additionalAttributes", additionalAttributes);

				String userJsonStr = URLEncoder
						.encode(json.toString(), "UTF-8");
				Log.i(TAG, userJsonStr);

				HashMap<String, String> parameters = new HashMap<String, String>();
				parameters.put("userProfile", userJsonStr);

				// Prepare a request object
				String _url = com.cm.beer.util.Util.getUploadUserProfileUrl();
				Log.i(TAG, _url);
				{
					boolean retry = true;
					int retryCount = 0;
					while ((retry)
							&& (retryCount < AppConfig.SHARE_WITH_COMMUNITY_BEER_UPLOAD_RETRY_COUNT)) {
						try {

							response = com.cm.beer.util.Util.openUrl(_url,
									"POST", parameters);
							// Upload successful
							retry = false;
						} catch (Throwable e) {
							Log.e(TAG, "error: "
									+ ((e.getMessage() != null) ? e
											.getMessage().replace(" ", "_")
											: ""), e);
							// increment retry count
							retryCount++;
							Log.e(TAG, "Retrying... Retry Count = "
									+ retryCount);
						}
					}
					Log.d(TAG, "Final Retry Count = " + retryCount);
					if (retryCount > 0) {
						mTracker.trackEvent("ShareWithCommunity",
								"UploadUserProfile", "RetryCount", retryCount);
						mTracker.dispatch();
					}
				}

				// Examine the response status
				Log.i(TAG, "Response = " + response);

			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("ShareWithCommunity", "UploadUserProfile",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "doInBackground finished");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPostExecute starting");
			}
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPostExecute finished");
			}
		}

	}

}