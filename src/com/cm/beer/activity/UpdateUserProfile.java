package com.cm.beer.activity;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.Logger;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class UpdateUserProfile extends Activity {
	String TAG;
	ProgressDialog mDialog;
	int ACTIVE_DIALOG;
	static final int SIGN_IN_REQUEST = 1;
	static final int DIALOG_UPDATE_ID = 1;

	TextView mMessage;
	Button mUpdateProfile;
	Button mDone;
	TextView mUserName;
	TextView mAboutYourself;
	AutoCompleteTextView mCountry;
	TextView mZipcode;

	GoogleAnalyticsTracker mTracker;
	User mUser;
	JSONObject mUserProfileJson;
	UpdateUserProfile mMainActivity;
	String mUserId;

	/** Called when the activity is first created. */
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

		mUser = new User(this);
		new AsyncGetUserProfile().execute(mUserId);

	}

	private void display() throws JSONException {
		setContentView(R.layout.community_update_profile);

		mMessage = (TextView) findViewById(R.id.message);

		mUserName = (TextView) findViewById(R.id.user_name);
		if (mUserProfileJson.has("userName")) {
			mUserName.setText(mUserProfileJson.getString("userName"));
		}

		mAboutYourself = (TextView) findViewById(R.id.about_yourself);
		if (mUserProfileJson.has("bio")) {
			if (Logger.isLogEnabled())  Logger.log("display: bio: " + mUserProfileJson.getString("bio"));
			mAboutYourself.setText(mUserProfileJson.getString("bio"));
		}

		mCountry = (AutoCompleteTextView) findViewById(R.id.country);
		ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(this,
				R.layout.list_item, AppConfig.COUNTRIES);
		mCountry.setAdapter(countryAdapter);
		if (mUserProfileJson.has("country")) {
			mCountry.setText(mUserProfileJson.getString("country"));
		}

		mZipcode = (TextView) findViewById(R.id.zipcode);
		if (mUserProfileJson.has("zipcode")) {
			if (Logger.isLogEnabled())  Logger.log("display: zipcode: "
					+ mUserProfileJson.getString("zipcode"));
			mZipcode.setText(mUserProfileJson.getString("zipcode"));
		}

		mUpdateProfile = (Button) findViewById(R.id.update_profile);
		mUpdateProfile.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mUpdateProfile.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (AppConfig.LOGGING_ENABLED) {
					if (Logger.isLogEnabled())  Logger.log("mSignUp.setOnClickListener");
				}
				mTracker.trackEvent("ShareWithCommunity", "CommunitySignUp",
						"Clicked", 0);
				mTracker.dispatch();
				if (Logger.isLogEnabled())  Logger.log("mSignUp:onClick:userId:");

				//
				String _userName = mUserName.getText().toString();
				String _bio = mAboutYourself.getText().toString();
				String _country = mCountry.getText().toString();
				String _zipCode = mZipcode.getText().toString();

				if ((_userName == null) || (_userName.equals(""))) {
					mMessage.setText(mMainActivity
							.getString(R.string.username_missing));
					return;
				} else {
					new AsyncUpdateProfileTask().execute(_userName, _bio,
							_country, _zipCode);
				}
			}
		});
		mDone = (Button) findViewById(R.id.done);
		mDone.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mDone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				mMainActivity.setResult(RESULT_OK, intent);
				mMainActivity.finish();
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
			if (Logger.isLogEnabled())  Logger.log("onDestroy");
		}
		// Stop the mTracker when it is no longer needed.
		mTracker.stop();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Stopped!");
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
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreateDialog");
		}
		String dialogMessage = null;
		if (id == DIALOG_UPDATE_ID) {
			dialogMessage = this.getString(R.string.progress_saving_message);
			ACTIVE_DIALOG = DIALOG_UPDATE_ID;
		}

		mDialog = ProgressDialog.show(UpdateUserProfile.this, null,
				dialogMessage, true, true);
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
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
			removeDialog(AppConfig.DIALOG_LOADING_ID);
		}
		super.onResume();
	}

	/************************************************************************************/
	private class AsyncUpdateProfileTask extends
			AsyncTask<Object, Void, Object> {
		private String _mResponse;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (Logger.isLogEnabled())  Logger.log("doInBackground starting");
			String _userName = (String) args[0];
			String _bio = (String) args[1];
			String _country = (String) args[2];
			String _zipCode = (String) args[3];

			String _response = "";
			try {

				JSONObject _userProfile = new JSONObject();
				_userProfile.put("userId", mUser.getUserId());
				_userProfile.put("userName", _userName);

				JSONObject _addlAttributes = new JSONObject();
				_addlAttributes.put("country", _country);
				_addlAttributes.put("language", Locale.getDefault()
						.getLanguage());
				_addlAttributes.put("zipcode", _zipCode);
				_addlAttributes.put("bio", _bio);

				_userProfile.put("additionalAttributes", _addlAttributes);

				String _userProfileStr = _userProfile.toString();
				_userProfileStr = URLEncoder.encode(_userProfileStr, "UTF-8");
				if (Logger.isLogEnabled())  Logger.log(_userProfileStr);

				HashMap<String, String> parameters = new HashMap<String, String>();
				parameters.put("q",
						AppConfig.COMMUNITY_UPDATE_USER_PROFILE_Q_VALUE);
				parameters.put("userprofile", _userProfileStr);

				// Prepare a request object
				String _url = AppConfig.COMMUNITY_GET_USER_SERVICE_URL;
				if (Logger.isLogEnabled())  Logger.log(_url);
				{
					boolean retry = true;
					int retryCount = 0;
					while ((retry)
							&& (retryCount < AppConfig.COMMUNITY_SIGNUP_RETRY_COUNT)) {
						try {

							_response = com.cm.beer.util.Util.openUrl(_url,
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
					if (Logger.isLogEnabled())  Logger.log("Final Retry Count = " + retryCount);
					if (retryCount > 0) {
						mTracker.trackEvent("CommunitySignIn", "Login",
								"RetryCount", retryCount);
						mTracker.dispatch();
					}
				}

				// Examine the response status
				if (Logger.isLogEnabled())  Logger.log("Response = " + _response);
				_mResponse = _response;

			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunitySignIn", "AsyncLoginTaskError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
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
				mMainActivity.showDialog(DIALOG_UPDATE_ID);
			}
			if (Logger.isLogEnabled())  Logger.log("onPreExecute finished");
		}

		@Override
		protected void onPostExecute(Object result) {
			if (Logger.isLogEnabled())  Logger.log("onPostExecute starting");
			if ((mMainActivity != null) && (mMessage != null)
					&& (_mResponse != null) && (!_mResponse.equals(""))) {
				JSONObject _responseJson;
				try {
					_responseJson = new JSONObject(_mResponse);
					String _status = _responseJson.getString("status");
					boolean success = ((_status != null)
							&& (_status.equalsIgnoreCase("Y")) ? true : false);

					if (success) {
						mMessage.setText(mMainActivity
								.getString(R.string.profile_update_successful));
						// make it visible
						mDone.setVisibility(View.VISIBLE);

					} else {
						JSONArray errorCodes = _responseJson
								.getJSONArray("errorCodes");
						for (int i = 0; i < errorCodes.length(); i++) {
							String errorCode = errorCodes.getString(i);
							mMessage.setText(mMainActivity
									.getString(R.string.profile_update_failed));
						}
					}
					mDialog.cancel();
				} catch (Throwable e) {
					Log.e(TAG, "error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);
					mTracker.trackEvent("CommunitySignIn",
							"AsyncLoginTaskError",
							((e.getMessage() != null) ? e.getMessage().replace(
									" ", "_") : "").replace(" ", "_"), 0);
					mTracker.dispatch();
				}
			} else {
				// TODO:
			}
			if (Logger.isLogEnabled())  Logger.log("onPostExecute finished");
		}

	}

	/************************************************************************************/
	private class AsyncGetUserProfile extends AsyncTask<Object, Void, Object> {

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (Logger.isLogEnabled())  Logger.log("doInBackground starting");
			String _userId = (String) args[0];
			try {

				String _url = Util.getUserProfileUrl(_userId);
				String response[] = Util.getResult(_url);
				if ((response[0] != null) && (response[0].startsWith("{"))) {
					mUserProfileJson = new JSONObject(response[0]);
				}

			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("UserProfile", "AsyncGetUserProfile", ((e
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
				mMainActivity.showDialog(AppConfig.DIALOG_LOADING_ID);
			}
			if (Logger.isLogEnabled())  Logger.log("onPreExecute finished");
		}

		protected void onPostExecute(Object result) {
			if (Logger.isLogEnabled())  Logger.log("onPostExecute starting");
			try {
				if (mUserProfileJson != null) {
					mMainActivity.display();
				} else {
					// TODO:
					AlertDialog.Builder dialog = new AlertDialog.Builder(
							new ContextThemeWrapper(mMainActivity,
									android.R.style.Theme_Dialog));
					dialog.setIcon(android.R.drawable.ic_dialog_alert);
					dialog
							.setTitle(R.string.unable_to_download_user_profile_message);
					dialog
							.setPositiveButton(
									R.string.ok_label,
									new android.content.DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											Intent intent = new Intent();
											mMainActivity.setResult(
													RESULT_CANCELED, intent);
											mMainActivity.finish();
										}
									});
					dialog.show();

				}
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("UserProfile", "AsyncGetUserProfile", ((e
						.getMessage() != null) ? e.getMessage().replace(" ",
						"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			mDialog.cancel();

			if (Logger.isLogEnabled())  Logger.log("onPostExecute finished");
		}

	}

}
