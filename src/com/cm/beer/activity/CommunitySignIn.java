package com.cm.beer.activity;

import java.net.URLEncoder;
import java.util.HashMap;

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
import android.widget.Button;
import android.widget.TextView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.Logger;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class CommunitySignIn extends Activity {
	String TAG;
	ProgressDialog mDialog;
	int ACTIVE_DIALOG;
	static final int SIGN_IN_REQUEST = 1;
	static final int SIGN_UP_REQUEST = 2;
	static final int DIALOG_LOGIN_ID = 1;
	static final int DIALOG_RECOVER_PASSWORD_ID = 2;

	// Stateful Field
	Long mRowId;
	Button mSignUp;

	TextView mMessage;
	Button mLogin;
	Button mRecoverPassword;
	TextView mUserId;
	TextView mPassword;

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

		setContentView(R.layout.community_signin);

		display();
	}

	private void display() {
		mMessage = (TextView) findViewById(R.id.message);
		mUserId = (TextView) findViewById(R.id.user_id);
		mPassword = (TextView) findViewById(R.id.password);

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

				Intent intent = new Intent(mMainActivity, CommunitySignUp.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, SIGN_UP_REQUEST);
			}
		});
		mLogin = (Button) findViewById(R.id.login);
		mLogin.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mLogin.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// clear message
				mMessage.setText("");
				mTracker.trackEvent("ShareWithCommunity", "CommunityLogin",
						"Clicked", 0);
				mTracker.dispatch();
				if (Logger.isLogEnabled())  Logger.log("mLogin:onClick:userId:");

				//
				String _userId = mUserId.getText().toString();
				String _password = mPassword.getText().toString();
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
				} else {
					new AsyncLoginTask().execute(_userId, _password);
				}

			}
		});

		mRecoverPassword = (Button) findViewById(R.id.recover_password);
		mRecoverPassword.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mRecoverPassword.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// clear message
				mMessage.setText("");
				mTracker.trackEvent("ShareWithCommunity",
						"CommunityRecoverPassword", "Clicked", 0);
				mTracker.dispatch();
				if (Logger.isLogEnabled())  Logger.log("mRecoverPassword:onClick:");
				//
				String _userId = mUserId.getText().toString();
				if ((_userId == null) || (_userId.equals(""))) {
					mMessage.setText(mMainActivity
							.getString(R.string.userid_missing));
				} else {
					new AsyncRecoverPasswordTask().execute(_userId);
				}
			}
		});

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
		if (requestCode == SIGN_UP_REQUEST) {
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
		if (id == DIALOG_LOGIN_ID) {
			dialogMessage = this.getString(R.string.progress_login_message);
			ACTIVE_DIALOG = DIALOG_LOGIN_ID;
		} else if (id == DIALOG_RECOVER_PASSWORD_ID) {
			dialogMessage = this
					.getString(R.string.progress_recover_password_message);
			ACTIVE_DIALOG = DIALOG_LOGIN_ID;
		}

		mDialog = ProgressDialog.show(CommunitySignIn.this, null,
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
	private class AsyncLoginTask extends AsyncTask<Object, Void, Object> {
		private String _mResponse;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (Logger.isLogEnabled())  Logger.log("doInBackground starting");
			String _userId = (String) args[0];
			String _password = (String) args[1];
			String _response = "";
			try {

				JSONObject _userProfile = new JSONObject();
				_userProfile.put("userId", _userId);
				_userProfile.put("password", _password);

				String _userProfileStr = _userProfile.toString();
				_userProfileStr = URLEncoder.encode(_userProfileStr, "UTF-8");
				if (Logger.isLogEnabled())  Logger.log(_userProfileStr);

				HashMap<String, String> parameters = new HashMap<String, String>();
				parameters.put("q", AppConfig.COMMUNITY_LOGIN_Q_VALUE);
				parameters.put("userprofile", _userProfileStr);

				// Prepare a request object
				String _url = AppConfig.COMMUNITY_GET_USER_SERVICE_URL;
				if (Logger.isLogEnabled())  Logger.log(_url);
				{
					boolean retry = true;
					int retryCount = 0;
					while ((retry)
							&& (retryCount < AppConfig.COMMUNITY_LOGIN_RETRY_COUNT)) {
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
				mMainActivity.showDialog(DIALOG_LOGIN_ID);
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
								.getString(R.string.login_successful));
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
						Intent intent = new Intent();
						mMainActivity.setResult(RESULT_OK, intent);
						mMainActivity.finish();
					} else {
						JSONArray errorCodes = _responseJson
								.getJSONArray("errorCodes");
						for (int i = 0; i < errorCodes.length(); i++) {
							String errorCode = errorCodes.getString(i);
							if (errorCode.equalsIgnoreCase("INVALID_PASSWORD")) {
								mMessage.setText(mMainActivity
										.getString(R.string.login_failed));
								break;
							} else if (errorCode
									.equalsIgnoreCase("USER_DOES_NOT_EXIST")) {
								mMessage
										.setText(mMainActivity
												.getString(R.string.login_failed_user_does_not_exist));
								break;
							} else {
								mMessage.setText(mMainActivity
										.getString(R.string.login_failed));
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

	/************************************************************************************/
	private class AsyncRecoverPasswordTask extends
			AsyncTask<Object, Void, Object> {
		private String _mResponse;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (Logger.isLogEnabled())  Logger.log("doInBackground starting");
			String _userId = (String) args[0];
			String _response = "";
			try {

				JSONObject _userProfile = new JSONObject();
				_userProfile.put("userId", _userId);

				String _userProfileStr = _userProfile.toString();
				_userProfileStr = URLEncoder.encode(_userProfileStr, "UTF-8");
				if (Logger.isLogEnabled())  Logger.log(_userProfileStr);

				HashMap<String, String> parameters = new HashMap<String, String>();
				parameters.put("q",
						AppConfig.COMMUNITY_RECOVER_PASSWORD_Q_VALUE);
				parameters.put("userprofile", _userProfileStr);

				// Prepare a request object
				String _url = AppConfig.COMMUNITY_GET_USER_SERVICE_URL;
				if (Logger.isLogEnabled())  Logger.log(_url);
				{
					boolean retry = true;
					int retryCount = 0;
					while ((retry)
							&& (retryCount < AppConfig.COMMUNITY_LOGIN_RETRY_COUNT)) {
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
				mTracker.trackEvent("CommunitySignIn",
						"AsyncRecoverPasswordTaskError",
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
				mMainActivity.showDialog(DIALOG_RECOVER_PASSWORD_ID);
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
						mMessage
								.setText(mMainActivity
										.getString(R.string.recover_password_successful));
					} else {
						JSONArray errorCodes = _responseJson
								.getJSONArray("errorCodes");
						for (int i = 0; i < errorCodes.length(); i++) {
							String errorCode = errorCodes.getString(i);
							if (errorCode
									.equalsIgnoreCase("USER_DOES_NOT_EXIST")) {
								mMessage
										.setText(mMainActivity
												.getString(R.string.recover_password_failed_user_does_not_exist));
								break;
							} else {
								mMessage
										.setText(mMainActivity
												.getString(R.string.recover_password_failed));
							}
						}

					}
					mDialog.cancel();
				} catch (Throwable e) {
					Log.e(TAG, "error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);
					mTracker.trackEvent("CommunitySignIn",
							"AsyncRecoverPasswordTaskError",
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
