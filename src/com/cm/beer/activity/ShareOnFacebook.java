package com.cm.beer.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.facebook.BaseRequestListener;
import com.cm.beer.facebook.SessionStore;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ShareOnFacebook extends Activity {
	String TAG;
	static final int MENU_GROUP = 0;
	static final int SEND_ERROR_REPORT_ID = Menu.FIRST;

	TextView mText;
	EditText mMessage;
	TextView mFacebookPostBeer;
	ImageView mThumbnail;
	TextView mFacebookPostDescription;
	Button mPostButton;

	Facebook mFacebook;
	AsyncFacebookRunner mAsyncRunner;

	String mName;
	String mDescription;

	ProgressDialog mDialog;
	int ACTIVE_DIALOG;

	// Stateful Field
	Long mRowId;

	GoogleAnalyticsTracker mTracker;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();

		if (AppConfig.LOGGING_ENABLED) {
			Log.d(TAG, "onCreate: ");
		}

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.start(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}

		Bundle extras = getIntent().getExtras();

		mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
				: null;
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate::_id="
					+ ((mRowId != null) ? mRowId.longValue() : null));
		}

		mDescription = setNoteForGraphApi();

		setContentView(R.layout.share_on_facebook);
		mText = (TextView) findViewById(R.id.txt);
		mMessage = (EditText) ShareOnFacebook.this.findViewById(R.id.message);
		mFacebookPostBeer = (TextView) ShareOnFacebook.this
				.findViewById(R.id.facebook_post_beer);
		mThumbnail = (ImageView) ShareOnFacebook.this
				.findViewById(R.id.thumbnail);
		setThumbnailView(mThumbnail, String.valueOf(mRowId));
		mFacebookPostDescription = (TextView) ShareOnFacebook.this
				.findViewById(R.id.facebook_post_description);

		String _mDescriptionForDisplay = mDescription.replace("#", ", ");
		_mDescriptionForDisplay = (_mDescriptionForDisplay.length() > 150) ? (_mDescriptionForDisplay
				.substring(0, 150))
				+ "..."
				: _mDescriptionForDisplay;

		mFacebookPostBeer.setText(mName);
		mFacebookPostDescription.setText(_mDescriptionForDisplay);

		mPostButton = (Button) findViewById(R.id.postButton);
		mPostButton.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);

		mFacebook = new Facebook();
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
		SessionStore.restore(mFacebook, this);

		mPostButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (AppConfig.LOGGING_ENABLED) {
					Log.d(TAG, "mPostButton.setOnClickListener");
				}
				showDialog(AppConfig.DIALOG_POSTING_ID);
				Bundle parameters = new Bundle();
				String _mDescription = mDescription.replace("#", "\n");
				parameters.putString("message", mMessage.getText().toString()
						+ "\n" + mName + "\n" + _mDescription);
				parameters.putString("file", AppConfig.PICTURES_DIR
						+ AppConfig.PATH_SEPARATOR + mRowId
						+ AppConfig.PICTURES_EXTENSION);
				mAsyncRunner.request("me/photos", parameters, "POST",
						new WallPostRequestListener());
			}
		});
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
			mTracker.trackEvent("ShareOnFacebook", "SendErrorReport",
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
		Intent intent = new Intent(ShareOnFacebook.this.getApplication(),
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
		if ((mDialog != null) && (mDialog.isShowing())) {
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
		mDialog = ProgressDialog.show(this, null, dialogMessage, true, true);
		mDialog.setCanceledOnTouchOutside(false);
		return mDialog;
	}

	/**
	 * 
	 * @return
	 * @throws JSONException
	 */
	private String setNoteForGraphApi() {
		String beer = null, alcohol = null, price = null, style = null, brewery = null, state = null, country = null, notes = null;
		float rating = 0f;
		NotesDbAdapter mDbHelper = null;

		StringBuilder _sbDescription = new StringBuilder();

		try {
			mDbHelper = new NotesDbAdapter(this);
			mDbHelper.open();
			Cursor note = mDbHelper.fetchNote(mRowId);
			startManagingCursor(note);

			beer = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BEER));
			if (beer != null && (!beer.equals(""))) {
				mName = beer;
			}

			rating = Float.valueOf(
					note.getString(note
							.getColumnIndexOrThrow(NotesDbAdapter.KEY_RATING)))
					.floatValue();
			if (rating > 0) {
				String _rating = "Rated " + ((int) rating) + " out of 5";
				// json.accumulate("caption", _rating);
				_sbDescription.append(_rating);

			}
			alcohol = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_ALCOHOL));
			if (alcohol != null && (!alcohol.equals(""))) {
				_sbDescription.append("#Alcohol: ");
				_sbDescription.append(alcohol + "%");
			}
			price = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_PRICE));
			if (price != null && (!price.equals(""))) {
				_sbDescription.append("#Price: ");
				_sbDescription.append(price);
			}
			style = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_STYLE));
			if (style != null && (!style.equals(""))) {
				_sbDescription.append("#Style: ");
				_sbDescription.append(style);
			}

			brewery = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BREWERY));
			if (brewery != null && (!brewery.equals(""))) {
				_sbDescription.append("#Brewery: ");
				_sbDescription.append(brewery);
			}

			state = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_STATE));
			if (state != null && (!state.equals(""))) {
				_sbDescription.append("#State: ");
				_sbDescription.append(state);
			}
			country = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_COUNTRY));
			if (country != null && (!country.equals(""))) {
				_sbDescription.append("#Country: ");
				_sbDescription.append(country);
			}

			notes = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_NOTES));
			if (notes != null && (!notes.equals(""))) {
				_sbDescription.append("#Notes: ");
				_sbDescription.append(notes);
			}

		} catch (Exception e) {
			Log.e(TAG, ((e.getMessage() != null) ? e.getMessage().replace(" ",
					"_") : ""), e);
		} finally {
			if (mDbHelper != null) {
				// close the Db connection
				mDbHelper.close();
			}
		}

		return _sbDescription.toString();
	}

	/**
	 * 
	 */
	private void setThumbnailView(ImageView thumbnailView, String mRowId) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "setThumbnailView()");
		}
		File thumbnail = null;
		File thumbnailsDir = new File(AppConfig.PICTURES_THUMBNAILS_DIR);
		if (thumbnailsDir != null && thumbnailsDir.exists()) {
			thumbnail = new File(thumbnailsDir, mRowId
					+ AppConfig.PICTURES_THUMBNAILS_EXTENSION);
			if (thumbnail != null && thumbnail.exists()) {
				if (thumbnail != null) {
					Bitmap image;
					try {
						image = BitmapFactory.decodeStream(thumbnail.toURL()
								.openStream());
						thumbnailView.setImageBitmap(image);
						if (AppConfig.LOGGING_ENABLED) {
							Log.i(TAG, "setThumbnailView():Setting "
									+ thumbnail.getPath() + " for " + mRowId);
						}
					} catch (MalformedURLException e) {
						Log.e(TAG, ((e.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : ""), e);
					} catch (IOException e) {
						Log.e(TAG, ((e.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : ""), e);
					}
				}
			} else {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "setThumbnailView(): Thumbnail not found for "
							+ mRowId);
				}
				// if not then bind the default icon
				thumbnailView.setImageResource(R.drawable.bottle);
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG,
							"setThumbnailView(): Setting default thumbnail for "
									+ mRowId);
				}
			}
		} else {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "setThumbnailView():"
						+ AppConfig.PICTURES_THUMBNAILS_DIR + " not found");
			}
		}
	}

	/************************************************************************************/
	/**
	 * 
	 * @author gaindans
	 * 
	 */
	public class WallPostRequestListener extends BaseRequestListener {

		public void onComplete(final String response) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.d(TAG, "WallPostRequestListener:onComplete "
						+ response.toString());
			}
			Log.d(TAG, "Got response: " + response);
			final String postId;

			try {
				JSONObject json = Util.parseJson(response);
				postId = json.getString("id");
				Log.d(TAG, "Post Id:" + postId);
			} catch (JSONException e) {
				Log.e(TAG, "JSON Error in response");
			} catch (FacebookError e) {
				Log.e(TAG, "Facebook Error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""));
			}
			if ((mDialog != null) && (mDialog.isShowing())) {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "WallPostRequestListener:onComplete");
				}
				removeDialog(ACTIVE_DIALOG);
				setResult(AppConfig.FACEBOOK_WALL_POST_SUCCESSFUL_RESULT_CODE);
				mTracker.trackEvent("ShareOnFacebook", "FacebookWallPost", "Y",
						0);
				mTracker.dispatch();
				finish();
			}

		}

		@Override
		public void onFacebookError(FacebookError e) {
			handleError(e);
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

		private void handleError(Throwable e) {
			Log.e(TAG, "Facebook Error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);

			mTracker.trackEvent("ShareOnFacebook", "FacebookWallPostError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: ""), 0);
			mTracker.dispatch();

			if ((mDialog != null) && (mDialog.isShowing())) {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "WallPostRequestListener:onComplete");
				}
				removeDialog(ACTIVE_DIALOG);
			}
			ShareOnFacebook.this.runOnUiThread(new Runnable() {
				public void run() {
					if (mText != null) {
						mText
								.setText(R.string.on_facebook_wall_post_unsuccessful);
					}
				}
			});
		}

	}

	/************************************************************************************/

	/**
	 * 
	 * @author gaindans
	 * 
	 */
	public class GetWallPostRequestListener extends BaseRequestListener {

		public void onComplete(final String response) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.d(TAG, "GetWallPostRequestListener: onComplete "
						+ response.toString());
			}
			Log.d(TAG, "Got response: " + response);
			String message = "<empty>";

			try {
				JSONObject json = Util.parseJson(response);
				message = json.getString("message");
			} catch (JSONException e) {
				Log.e(TAG, "JSON Error in response");
			} catch (FacebookError e) {
				Log.e(TAG, "Facebook Error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""));
			}
			final String text = "Your Wall Post: " + message;
			ShareOnFacebook.this.runOnUiThread(new Runnable() {
				public void run() {
					if (mText != null) {
						mText.setText(text);
					}
				}
			});

			if ((mDialog != null) && (mDialog.isShowing())) {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "GetWallPostRequestListener: onComplete");
				}
				removeDialog(ACTIVE_DIALOG);
			}

		}

		@Override
		public void onFacebookError(FacebookError e) {
			if ((mDialog != null) && (mDialog.isShowing())) {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "GetWallPostRequestListener: onFacebookError");
				}
				removeDialog(ACTIVE_DIALOG);
			}
			final String text = "Unable to retrieve your Post from Facebook. "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : "");
			ShareOnFacebook.this.runOnUiThread(new Runnable() {
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
			if ((mDialog != null) && (mDialog.isShowing())) {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "onFacebookError: handleError");
				}
				removeDialog(ACTIVE_DIALOG);
			}
			ShareOnFacebook.this.runOnUiThread(new Runnable() {
				public void run() {
					if (mText != null) {
						mText
								.setText(getString(R.string.facebook_wall_post_unsuccessful_message));
					}
				}
			});
		}

	}

}