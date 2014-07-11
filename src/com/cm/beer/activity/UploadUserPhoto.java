package com.cm.beer.activity;

import java.io.File;
import java.net.URLDecoder;
import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.BitmapScaler;
import com.cm.beer.util.Logger;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class UploadUserPhoto extends Activity {
	String mFileName;
	String mUserId;
	String TAG;
	ProgressDialog mDialog;
	Activity mMainActivity;

	GoogleAnalyticsTracker mTracker;

	ImageView mUserPhotoView;
	Button mUploadPhoto;

	static final int DIALOG_UPLOADING_USER_PHOTO_ID = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate");
		}
		mMainActivity = this;
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the mTracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Instantiated");
		}

		Bundle extras = getIntent().getExtras();
		mUserId = extras != null ? extras.getString("USERID") : null;
		mFileName = extras != null ? extras.getString("FILENAME") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: FILENAME: " + mFileName);
		setContentView(R.layout.view_user_photo);
		setup();
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
		if (id == DIALOG_UPLOADING_USER_PHOTO_ID) {
			dialogMessage = this.getString(R.string.progress_uploading_message);
		}

		mDialog = ProgressDialog.show(mMainActivity, null, dialogMessage, true,
				true);
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
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
		// Stop the mTracker when it is no longer needed.
		mTracker.stop();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Stopped!");
		}
		super.onDestroy();
	}

	private void setup() {
		/****************************************/
		mUserPhotoView = (ImageView) findViewById(R.id.user_photo);
		if (mFileName != null) {
			try {
				BitmapScaler bitmapScaler = new BitmapScaler(
						new File(mFileName), AppConfig.THUMBNAIL_WIDTH);
				Bitmap thumbnailBitmap = bitmapScaler.getScaled();
				mUserPhotoView.setImageBitmap(thumbnailBitmap);
			} catch (Throwable e) {
				Log.e(TAG, e.getMessage(), e);
			}

		}
		/****************************************/
		mUploadPhoto = (Button) findViewById(R.id.upload_photo);
		mUploadPhoto.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mUploadPhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new AsyncUploadUserPhoto().execute("");
			}
		});

	}

	/************************************************************************************/
	private class AsyncUploadUserPhoto extends AsyncTask<Object, Void, Object> {

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (Logger.isLogEnabled())  Logger.log("doInBackground starting");
			try {
				String response[] = Util.getResult(Util
						.getUploadUserPhotoURLUrl());
				if ((response.length > 0) && (response[0] != null)) {
					String jsonStr = URLDecoder.decode(response[0], "UTF-8");
					if ((jsonStr != null) && (jsonStr.startsWith("{"))) {
						JSONObject uploadUrlJson = new JSONObject(jsonStr);
						String _url = uploadUrlJson.getString("uploadUrl");
						if (Logger.isLogEnabled())  Logger.log("Upload Url: " + _url);

						HashMap<String, String> parameters = new HashMap<String, String>();
						parameters.put("userid", mUserId);
						parameters.put("file", mFileName);
						Log.i(TAG,
								"AsyncUploadUserPhoto.doInBackground(): file: "
										+ mFileName);
						String _response = "";
						{
							boolean retry = true;
							int retryCount = 0;
							while ((retry)
									&& (retryCount < AppConfig.COMMUNITY_UPLOAD_USER_PHOTO_RETRY_COUNT)) {
								try {

									_response = com.cm.beer.util.Util.openUrl(
											_url, "POST", parameters);
									// Upload successful
									retry = false;
								} catch (Throwable e) {
									Log.e(TAG, "error: "
											+ ((e.getMessage() != null) ? e
													.getMessage().replace(" ",
															"_") : ""), e);
									// increment retry count
									retryCount++;
									Log.e(TAG, "Retrying... Retry Count = "
											+ retryCount);
								}
							}
							if (Logger.isLogEnabled())  Logger.log("Final Retry Count = " + retryCount);
							if (retryCount > 0) {
								mTracker.trackEvent("CommunitySignIn", "Login",
										"RetryCount", retryCount);
								mTracker.dispatch();
							}
						}

						// Examine the response status
						if (Logger.isLogEnabled())  Logger.log("Response = " + _response);
					}

				}

			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("UpdateUserPhoto", "UpdatePhotoError", ((e
						.getMessage() != null) ? e.getMessage().replace(" ",
						"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			if (Logger.isLogEnabled())  Logger.log("doInBackground finished");
			return null;
		}

		@Override
		protected void onPreExecute() {
			if (Logger.isLogEnabled())  Logger.log("onPreExecute starting");
			if (mMainActivity != null) {
				mMainActivity.showDialog(DIALOG_UPLOADING_USER_PHOTO_ID);
			}
			if (Logger.isLogEnabled())  Logger.log("onPreExecute finished");
		}

		@Override
		protected void onPostExecute(Object result) {
			if (Logger.isLogEnabled())  Logger.log("onPostExecute starting");
			mDialog.cancel();
			Intent intent = new Intent();
			if (Logger.isLogEnabled())  Logger.log("onPostExecute: FILENAME: " + mFileName);
			intent.putExtra("FILENAME", mFileName);
			mMainActivity.setResult(RESULT_OK, intent);
			mMainActivity.finish();
			if (Logger.isLogEnabled())  Logger.log("onPostExecute finished");
		}

	}

}
