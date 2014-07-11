package com.cm.beer.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
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
import android.net.Uri;
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
import com.cm.beer.transfer.CommunityBeer;
import com.cm.beer.util.DrawableManager;
import com.cm.beer.util.Logger;
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
	CommunityBeer mCommunityBeer;
	String mShareType;

	GoogleAnalyticsTracker mTracker;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();

		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate: ");
		}

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID,
				this);
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Instantiated");
		}

		setContentView(R.layout.share_on_facebook);
		mText = (TextView) findViewById(R.id.txt);
		mMessage = (EditText) ShareOnFacebook.this.findViewById(R.id.message);
		mFacebookPostBeer = (TextView) ShareOnFacebook.this
				.findViewById(R.id.facebook_post_beer);
		mThumbnail = (ImageView) ShareOnFacebook.this
				.findViewById(R.id.thumbnail);
		mFacebookPostDescription = (TextView) ShareOnFacebook.this
				.findViewById(R.id.facebook_post_description);

		mPostButton = (Button) findViewById(R.id.postButton);
		mPostButton.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);

		mFacebook = new Facebook(AppConfig.FACEBOOK_APP_ID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
		SessionStore.restore(mFacebook, this);

		Bundle extras = getIntent().getExtras();
		mShareType = extras != null ? extras.getString("SHARE") : "";
		// default to empty string
		mShareType = (mShareType!=null)?mShareType:"";

		mPostButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					if (AppConfig.LOGGING_ENABLED) {
						if (Logger.isLogEnabled())  Logger.log("mPostButton.setOnClickListener");
					}
					showDialog(AppConfig.DIALOG_POSTING_ID);
					Bundle parameters = new Bundle();
					String _description = mDescription.replace("#", "\n");
					String _caption = mMessage.getText().toString() + "\n"
							+ mName + "\n" + _description;
					parameters.putString("caption", _caption);

					if (mRowId != null) {
						Uri _photoUri = Uri.parse(AppConfig.PICTURES_DIR
								+ AppConfig.PATH_SEPARATOR + mRowId
								+ AppConfig.PICTURES_EXTENSION);
						parameters.putByteArray("photo", com.cm.beer.util.Util
								.convertPhotoToByteArray(_photoUri));
					}
					// if community wine
					if (mShareType.equals("COMMUNITY_BEER")) {
						DrawableManager _drawableManager = DrawableManager
								.getInstance();
						byte[] bitmapdata = _drawableManager
								.fetchDrawableAsByteArray(com.cm.beer.util.Util
										.getImageUrl(mCommunityBeer.beerId));
						parameters.putByteArray("photo", bitmapdata);
					}

					mAsyncRunner.request("me/photos", parameters, "POST",
							new WallPostRequestListener(), /* state */null);
				} catch (Exception e) {
					Log.e(TAG, ((e.getMessage() != null) ? e.getMessage()
							.replace(" ", "_") : ""), e);
				}
			}
		});


		if (mShareType.equals("COMMUNITY_BEER")) {
			mCommunityBeer = extras != null ? (CommunityBeer) extras
					.getSerializable("COMMUNITY_BEER") : null;
			mDescription = setCommunityBeerForGraphApi();
			mThumbnail.setImageDrawable(DrawableManager.getInstance()
					.fetchDrawable(
							com.cm.beer.util.Util
									.getImageUrl(mCommunityBeer.beerId)));
		} else {
			// default
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
					: null;
			mDescription = setMyBeerForGraphApi();
			setThumbnailView(mThumbnail, String.valueOf(mRowId));
		}
		if (Logger.isLogEnabled())  Logger.log("Description: " + mDescription);
		String _mDescriptionForDisplay = mDescription.replace("#", ", ");
		_mDescriptionForDisplay = (_mDescriptionForDisplay.length() > 150) ? (_mDescriptionForDisplay
				.substring(0, 150)) + "..."
				: _mDescriptionForDisplay;
		mFacebookPostBeer.setText(mName);
		mFacebookPostDescription.setText(_mDescriptionForDisplay);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onDestroy");
		}
		// Stop the tracker when it is no longer needed.
		mTracker.stop();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Stopped!");
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
			if (Logger.isLogEnabled())  Logger.log("onCreateOptionsMenu");
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
			if (Logger.isLogEnabled())  Logger.log("onMenuItemSelected");
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
			if (Logger.isLogEnabled())  Logger.log("Send Error Report");
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
			if (Logger.isLogEnabled())  Logger.log("onResume");
		}
		if ((mDialog != null) && (mDialog.isShowing())) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("onResume:active dialog removed");
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
			if (Logger.isLogEnabled())  Logger.log("onCreateDialog");
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
	 */
	private String setCommunityBeerForGraphApi() {
		String beer = null, alcohol = null, price = null, style = null, brewery = null, state = null, country = null, notes = null;
		float rating = 0f;

		StringBuilder _sbDescription = new StringBuilder();

		try {

			beer = mCommunityBeer.beer;
			if (beer != null && (!beer.equals(""))) {
				mName = beer;
			}

			rating = Float.valueOf(mCommunityBeer.rating);
			if (rating > 0) {
				String _rating = "Rated " + ((int) rating) + " out of 5";
				// json.accumulate("caption", _rating);
				_sbDescription.append(_rating);

			}
			alcohol = mCommunityBeer.alcohol;
			if (alcohol != null && (!alcohol.equals(""))) {
				_sbDescription.append("#Alcohol: ");
				_sbDescription.append(alcohol + "%");
			}
			price = mCommunityBeer.price;
			if (price != null && (!price.equals(""))) {
				_sbDescription.append("#Price: ");
				String _currencySymbol = ((mCommunityBeer.currencySymbol != null) && (!mCommunityBeer.currencySymbol
						.equals(""))) ? mCommunityBeer.currencySymbol : "";
				_sbDescription.append(_currencySymbol + " " + price);
			}
			style = mCommunityBeer.style;
			if (style != null && (!style.equals(""))) {
				_sbDescription.append("#Style: ");
				_sbDescription.append(style);
			}

			brewery = mCommunityBeer.brewery;
			if (brewery != null && (!brewery.equals(""))) {
				_sbDescription.append("#Brewery: ");
				_sbDescription.append(brewery);
			}

			state = mCommunityBeer.state;
			if (state != null && (!state.equals(""))) {
				_sbDescription.append("#State: ");
				_sbDescription.append(state);
			}
			country = mCommunityBeer.country;
			if (country != null && (!country.equals(""))) {
				_sbDescription.append("#Country: ");
				_sbDescription.append(country);
			}

			notes = mCommunityBeer.notes;
			if (notes != null && (!notes.equals(""))) {
				_sbDescription.append("#Notes: ");
				_sbDescription.append(notes);
			}
			String _characteristicsJson = mCommunityBeer.characteristics;
			_sbDescription
					.append(this.getCharacteristics(_characteristicsJson));

		} catch (Exception e) {
			Log.e(TAG,
					((e.getMessage() != null) ? e.getMessage()
							.replace(" ", "_") : ""), e);
		}

		return _sbDescription.toString();
	}

	/**
	 * 
	 * @return
	 * @throws JSONException
	 */
	private String setMyBeerForGraphApi() {
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
			String _characteristicsJson = note.getString(note
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_CHARACTERISTICS));
			_sbDescription
					.append(this.getCharacteristics(_characteristicsJson));

		} catch (Exception e) {
			Log.e(TAG,
					((e.getMessage() != null) ? e.getMessage()
							.replace(" ", "_") : ""), e);
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
	 * @param characteristicsJson
	 * @return
	 * @throws JSONException
	 */
	private String getCharacteristics(String characteristicsJson)
			throws JSONException {
		StringBuffer _characteristics = new StringBuffer();
		if (Logger.isLogEnabled())  Logger.log("Characteristics Json: "
				+ characteristicsJson);

		if (characteristicsJson != null && (!characteristicsJson.equals(""))
				&& (characteristicsJson.startsWith("{"))) {
			JSONObject _characteristicsJson = new JSONObject(
					characteristicsJson);

			if (_characteristicsJson != null) {
				if (_characteristicsJson.has("color")) {
					if (!_characteristicsJson.getString("color").equals("")) {
						_characteristics.append("#Color: ");
						_characteristics.append(_characteristicsJson
								.getString("color"));
					}
				}
				if (_characteristicsJson.has("clarity")) {
					if (!_characteristicsJson.getString("clarity").equals("")) {
						_characteristics.append("#Clarity: ");
						_characteristics.append(_characteristicsJson
								.getString("clarity"));
					}
				}
				if (_characteristicsJson.has("aroma")) {
					if (_characteristicsJson.getJSONArray("aroma").length() > 0) {
						JSONArray _aroma = _characteristicsJson
								.getJSONArray("aroma");
						StringBuilder _aromaText = new StringBuilder();
						for (int i = 0; i < _aroma.length(); i++) {
							_aromaText.append(", ");
							_aromaText.append(_aroma.getString(i));
						}
						String _aromaStr = _aromaText.toString();
						_aromaStr = _aromaStr.replaceFirst(", ", "");
						_characteristics.append("#Aroma: ");
						_characteristics.append(_aromaStr);
					}
				}
				if (_characteristicsJson.has("foam")) {
					if (!_characteristicsJson.getString("foam").equals("")) {
						_characteristics.append("#Foam: ");
						_characteristics.append(_characteristicsJson
								.getString("foam"));
					}
				}

				if (_characteristicsJson.has("mouthfeel")) {
					if (!_characteristicsJson.getString("mouthfeel").equals("")) {
						_characteristics.append("#Mouthfeel: ");
						_characteristics.append(_characteristicsJson
								.getString("mouthfeel"));
					}
				}
				if (_characteristicsJson.has("body")) {
					if (!_characteristicsJson.getString("body").equals("")) {
						_characteristics.append("#Body: ");
						_characteristics.append(_characteristicsJson
								.getString("body"));
					}
				}
				if (_characteristicsJson.has("aftertaste")) {
					if (!_characteristicsJson.getString("aftertaste")
							.equals("")) {
						_characteristics.append("#Aftertaste: ");
						_characteristics.append(_characteristicsJson
								.getString("aftertaste"));
					}
				}
			}

		}
		if (Logger.isLogEnabled())  Logger.log("Characteristics: " + _characteristics.toString());
		return _characteristics.toString();

	}

	/**
	 * 
	 */
	private void setThumbnailView(ImageView thumbnailView, String mRowId) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("setThumbnailView()");
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
							if (Logger.isLogEnabled())  Logger.log("setThumbnailView():Setting "
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
					if (Logger.isLogEnabled())  Logger.log("setThumbnailView(): Thumbnail not found for "
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
				if (Logger.isLogEnabled())  Logger.log("setThumbnailView():"
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

		private void handleError(Throwable e) {
			Log.e(TAG,
					"Facebook Error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);

			mTracker.trackEvent("ShareOnFacebook", "FacebookWallPostError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: ""), 0);
			mTracker.dispatch();

			if ((mDialog != null) && (mDialog.isShowing())) {
				if (AppConfig.LOGGING_ENABLED) {
					if (Logger.isLogEnabled())  Logger.log("WallPostRequestListener:onComplete");
				}
				removeDialog(ACTIVE_DIALOG);
			}
			ShareOnFacebook.this.runOnUiThread(new Runnable() {
				public void run() {
					if (mText != null) {
						mText.setText(R.string.on_facebook_wall_post_unsuccessful);
					}
				}
			});
		}

		@Override
		public void onComplete(String response, Object state) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.d(TAG,
						"WallPostRequestListener:onComplete "
								+ response.toString());
			}
			if (Logger.isLogEnabled())  Logger.log("Got response: " + response);
			final String postId;

			try {
				JSONObject json = Util.parseJson(response);
				postId = json.getString("id");
				if (Logger.isLogEnabled())  Logger.log("Post Id:" + postId);
			} catch (JSONException e) {
				Log.e(TAG, "JSON Error in response");
			} catch (FacebookError e) {
				Log.e(TAG,
						"Facebook Error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""));
			}
			if ((mDialog != null) && (mDialog.isShowing())) {
				if (AppConfig.LOGGING_ENABLED) {
					if (Logger.isLogEnabled())  Logger.log("WallPostRequestListener:onComplete");
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
		public void onIOException(IOException e, Object state) {
			handleError(e);
			super.onIOException(e);
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			handleError(e);
			super.onFileNotFoundException(e);

		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			handleError(e);
			super.onMalformedURLException(e);
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			handleError(e);
			super.onFacebookError(e);
		}

	}

	/************************************************************************************/

	/**
	 * 
	 * @author gaindans
	 * 
	 */
	public class GetWallPostRequestListener extends BaseRequestListener {

		private void handleError(Exception e) {
			Log.e(TAG,
					"Facebook Error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);
			if ((mDialog != null) && (mDialog.isShowing())) {
				if (AppConfig.LOGGING_ENABLED) {
					if (Logger.isLogEnabled())  Logger.log("onFacebookError: handleError");
				}
				removeDialog(ACTIVE_DIALOG);
			}
			ShareOnFacebook.this.runOnUiThread(new Runnable() {
				public void run() {
					if (mText != null) {
						mText.setText(getString(R.string.facebook_wall_post_unsuccessful_message));
					}
				}
			});
		}

		@Override
		public void onComplete(String response, Object state) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.d(TAG,
						"GetWallPostRequestListener: onComplete "
								+ response.toString());
			}
			if (Logger.isLogEnabled())  Logger.log("Got response: " + response);
			String message = "<empty>";

			try {
				JSONObject json = Util.parseJson(response);
				message = json.getString("message");
			} catch (JSONException e) {
				Log.e(TAG, "JSON Error in response");
			} catch (FacebookError e) {
				Log.e(TAG,
						"Facebook Error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""));
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
					if (Logger.isLogEnabled())  Logger.log("GetWallPostRequestListener: onComplete");
				}
				removeDialog(ACTIVE_DIALOG);
			}
		}

		@Override
		public void onIOException(IOException e, Object state) {
			handleError(e);
			super.onIOException(e);
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			handleError(e);
			super.onFileNotFoundException(e);
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			handleError(e);
			super.onMalformedURLException(e);
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			if ((mDialog != null) && (mDialog.isShowing())) {
				if (AppConfig.LOGGING_ENABLED) {
					if (Logger.isLogEnabled())  Logger.log("GetWallPostRequestListener: onFacebookError");
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

	}

}