package com.cm.beer.activity;

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
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;
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

public class LoginIntercept extends Activity {
	String TAG;
	static final int MENU_GROUP = 0;
	static final int SEND_ERROR_REPORT_ID = Menu.FIRST;
	static final int SIGN_IN_REQUEST = 1;

	LoginButton mLoginButton;
	Button mCommunityLoginButton;
	TextView mText;

	Facebook mFacebook;
	AsyncFacebookRunner mAsyncRunner;

	ProgressDialog dialog;
	int ACTIVE_DIALOG;

	// Stateful Field
	Long mRowId;
	String mAction;
	Activity mMainActivity;

	GoogleAnalyticsTracker mTracker;

	String[] mFacebookPermissions;
	Intent mOriginalIntent;

	User mUser;

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
		mUser = new User(mMainActivity);
		mOriginalIntent = getIntent();
		/**
		 * Setup an empty array if missing.Facebook code throws an
		 * arrayoutofbound exception otherwise
		 **/
		Bundle extras = (mOriginalIntent != null) ? mOriginalIntent.getExtras()
				: null;
		mFacebookPermissions = ((extras != null)
				&& (extras.getStringArray("FACEBOOK_PERMISSIONS") != null) ? extras
				.getStringArray("FACEBOOK_PERMISSIONS")
				: new String[] {});

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.start(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}

		Log.d(TAG, "onCreate::Intercepting!");
		setContentView(R.layout.login_intercept);
		mLoginButton = (LoginButton) findViewById(R.id.login);
		mCommunityLoginButton = (Button) findViewById(R.id.communityLoginButton);
		mCommunityLoginButton.getBackground().setColorFilter(
				AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
		mCommunityLoginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "mCommunityLoginButton:onClick:userId:");
				Intent intent = new Intent(mMainActivity, CommunitySignIn.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, SIGN_IN_REQUEST);
			}
		});

		mText = (TextView) findViewById(R.id.txt);

		mFacebook = new Facebook();
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
		SessionStore.restore(mFacebook, this);
		SessionEvents.addAuthListener(new BeerAuthListener());
		SessionEvents.addLogoutListener(new BeerLogoutListener());
		mLoginButton.init(mFacebook, mFacebookPermissions);

	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
		if (mFacebook.isSessionValid()) {
			Log.d(TAG, "onStart::Intercepting!:Facebook Session Valid!");
			// End the activity; pass back the original extras
			mMainActivity.setResult(RESULT_OK, mOriginalIntent);
			mMainActivity.finish();
		} else {
			Log.d(TAG, "onStart::Intercepting!:Facebook Session is NOT Valid!");
		}
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
		if (requestCode == SIGN_IN_REQUEST) {
			if (resultCode == RESULT_OK) {
				mMainActivity.setResult(RESULT_OK, intent);
				mMainActivity.finish();
			}
		}
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
			mTracker.trackEvent("FacebookLoginIntercept", "FacebookLogin", "Y",
					0);
			mTracker.dispatch();
			// Get user profile data
			mAsyncRunner.request("me", new Bundle(), "GET",
					new GetUserProfileRequestListener());

		}

		public void onAuthFail(String error) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.d(TAG, "VVWAuthListener: onAuthFail");
			}
			if (mText != null) {
				mText.setText("Login Failed: " + error);
			}
		}
	}

	/************************************************************************************/
	public class BeerLogoutListener implements LogoutListener {
		public void onLogoutBegin() {
			if (AppConfig.LOGGING_ENABLED) {
				Log.d(TAG, "VVWLogoutListener: onLogoutBegin");
			}
			Toast.makeText(LoginIntercept.this,
					R.string.on_facebook_logout_begin, Toast.LENGTH_SHORT)
					.show();
			if (mText != null) {
				mText.setText(R.string.on_facebook_logout_begin);
			}

		}

		public void onLogoutFinish() {
			if (AppConfig.LOGGING_ENABLED) {
				Log.d(TAG, "VVWLogoutListener: onLogoutFinish");
			}
			mTracker.trackEvent("FacebookLoginIntercept", "FacebookLogout",
					"Y", 0);
			mTracker.dispatch();
			Toast.makeText(LoginIntercept.this, R.string.on_facebook_logout,
					Toast.LENGTH_LONG).show();
			if (mText != null) {
				mText.setText(R.string.on_facebook_logout);
			}

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
						AppConfig.USER_TYPE_FACEBOOK);
				final String text = mUser.getUserName() + " "
						+ mMainActivity.getString(R.string.on_facebook_login);

				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						if (mText != null) {
							mText.setText(text);
						}
					}
				});

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

			// End the activity; pass back the original extras
			mMainActivity.setResult(RESULT_OK, mOriginalIntent);
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
			LoginIntercept.this.runOnUiThread(new Runnable() {
				public void run() {
					if (mText != null) {
						mText.setText(text);
					}
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

	/***************************************************************************************/
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
				parameters.put("userprofile", userJsonStr);

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
						mTracker.trackEvent("LoginIntercept",
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
				mTracker.trackEvent("LoginIntercept", "UploadUserProfile", ((e
						.getMessage() != null) ? e.getMessage().replace(" ",
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