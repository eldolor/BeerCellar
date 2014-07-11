package com.cm.beer.activity;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
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

public class CommunitySignUp extends Activity {
	String TAG;
	ProgressDialog mDialog;
	int ACTIVE_DIALOG;
	static final int SIGN_IN_REQUEST = 1;
	static final int DIALOG_SIGNUP_ID = 1;

	TextView mMessage;
	Button mSignUp;
	Button mDone;
	TextView mUserName;
	TextView mUserId;
	TextView mPassword;
	TextView mAboutYourself;
	AutoCompleteTextView mCountry;
	TextView mZipcode;

	GoogleAnalyticsTracker mTracker;
	Activity mMainActivity;

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

		setContentView(R.layout.community_signup);

		display();
	}

	private void display() {
		mMessage = (TextView) findViewById(R.id.message);

		mUserName = (TextView) findViewById(R.id.user_name);

		mUserId = (TextView) findViewById(R.id.user_id);

		mPassword = (TextView) findViewById(R.id.password);

		mAboutYourself = (TextView) findViewById(R.id.about_yourself);

		mCountry = (AutoCompleteTextView) findViewById(R.id.country);
		ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(this,
				R.layout.list_item, AppConfig.COUNTRIES);
		mCountry.setAdapter(countryAdapter);
		mCountry.setText(Locale.getDefault().getDisplayCountry());

		mZipcode = (TextView) findViewById(R.id.zipcode);

		mSignUp = (Button) findViewById(R.id.signup);
		mSignUp.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mSignUp.setOnClickListener(new OnClickListener() {
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
				String _userId = mUserId.getText().toString();
				String _password = mPassword.getText().toString();
				String _bio = mAboutYourself.getText().toString();
				String _country = mCountry.getText().toString();
				String _zipCode = mZipcode.getText().toString();

				if ((_userId == null) || (_userId.equals(""))) {
					mMessage.setText(mMainActivity
							.getString(R.string.userid_missing));
					return;
				} else if (!Util.isValidEmailAddress(_userId)) {
					mMessage.setText(mMainActivity
							.getString(R.string.invalid_email_address));
					return;
				} else if ((_password == null) || (_password.equals(""))) {
					mMessage.setText(mMainActivity
							.getString(R.string.password_missing));
					return;
				} else if ((_userName == null) || (_userName.equals(""))) {
					mMessage.setText(mMainActivity
							.getString(R.string.username_missing));
					return;
				} else {
					new AsyncSignUpTask().execute(_userName, _userId,
							_password, _bio, _country, _zipCode);
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
		if (id == DIALOG_SIGNUP_ID) {
			dialogMessage = this.getString(R.string.progress_signup_message);
			ACTIVE_DIALOG = DIALOG_SIGNUP_ID;
		}

		mDialog = ProgressDialog.show(CommunitySignUp.this, null,
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
	private class AsyncSignUpTask extends AsyncTask<Object, Void, Object> {
		private String _mResponse;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (Logger.isLogEnabled())  Logger.log("doInBackground starting");
			String _userName = (String) args[0];
			String _userId = (String) args[1];
			String _password = (String) args[2];
			String _bio = (String) args[3];
			String _country = (String) args[4];
			String _zipCode = (String) args[5];

			String _response = "";
			try {

				JSONObject _userProfile = new JSONObject();
				_userProfile.put("userId", _userId);
				_userProfile.put("password", _password);
				_userProfile.put("userName", _userName);

				JSONObject _addlAttributes = new JSONObject();
				_addlAttributes.put("email", _userId);// same as user id
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
				parameters.put("q", AppConfig.COMMUNITY_SIGNUP_Q_VALUE);
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
				mMainActivity.showDialog(DIALOG_SIGNUP_ID);
			}
			if (Logger.isLogEnabled())  Logger.log("onPreExecute finished");
		}

		@Override
		protected void onPostExecute(Object result) {
			if (Logger.isLogEnabled())  Logger.log("onPostExecute starting");
			if ((mMainActivity != null) && (mMessage != null)
					&& (_mResponse != null) && (_mResponse.startsWith("{"))) {
				JSONObject _responseJson;
				try {
					_responseJson = new JSONObject(_mResponse);
					String _status = _responseJson.getString("status");
					boolean success = ((_status != null)
							&& (_status.equalsIgnoreCase("Y")) ? true : false);

					if (success) {
						mMessage.setText(mMainActivity
								.getString(R.string.signup_successful));
						String _userId = null, _userName = null, _userLink = null;
						if (_responseJson.has("userId")) {
							_userId = _responseJson.getString("userId");
						}
						if (_responseJson.has("userName")) {
							_userName = _responseJson.getString("userName");
						}
						if (_responseJson.has("userLink")) {
							_userLink = _responseJson.getString("userLink");
						}

						User _user = new User(mMainActivity);
						_user.onAuthSucceed(_userId, _userName, _userLink,
								AppConfig.USER_TYPE_COMMUNITY);
						// make it visible
						mDone.setVisibility(View.VISIBLE);

					} else {
						JSONArray errorCodes = _responseJson
								.getJSONArray("errorCodes");
						for (int i = 0; i < errorCodes.length(); i++) {
							String errorCode = errorCodes.getString(i);
							if (errorCode.equalsIgnoreCase("EXISTING_USER")) {
								mMessage
										.setText(mMainActivity
												.getString(R.string.signup_failed_existing_user));
								break;
							} else {
								mMessage.setText(mMainActivity
										.getString(R.string.signup_failed));
							}
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

}
