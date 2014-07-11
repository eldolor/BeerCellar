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
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class CommunityChangePassword extends Activity {
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
	Button mDone;

	TextView mMessage;
	Button mChangePassword;
	TextView mUserId;
	TextView mCurrentPassword;
	TextView mNewPassword;

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

		setContentView(R.layout.community_change_password);

		display();
	}

	private void display() {
		mMessage = (TextView) findViewById(R.id.message);
		mUserId = (TextView) findViewById(R.id.user_id);
		mCurrentPassword = (TextView) findViewById(R.id.current_password);
		mNewPassword = (TextView) findViewById(R.id.new_password);

		mChangePassword = (Button) findViewById(R.id.change_password);
		mChangePassword.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mChangePassword.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// clear message
				mMessage.setText("");
				mTracker.trackEvent("ShareWithCommunity",
						"CommunityChangePassword", "Clicked", 0);
				mTracker.dispatch();
				if (Logger.isLogEnabled())  Logger.log("mLogin:onClick:userId:");

				//
				String _userId = mUserId.getText().toString();
				String _currentPassword = mCurrentPassword.getText().toString();
				String _newPassword = mNewPassword.getText().toString();

				if ((_userId == null) || (_userId.equals(""))) {
					mMessage.setText(mMainActivity
							.getString(R.string.userid_missing));
					return;
				} else if (!Util.isValidEmailAddress(_userId)) {
					mMessage.setText(mMainActivity
							.getString(R.string.invalid_email_address));
					return;
				} else if ((_currentPassword == null)
						|| (_currentPassword.equals(""))) {
					mMessage.setText(mMainActivity
							.getString(R.string.password_missing));
					return;
				} else if ((_newPassword == null) || (_newPassword.equals(""))) {
					mMessage.setText(mMainActivity
							.getString(R.string.password_missing));
					return;
				} else {
					new AsyncChangePasswordTask().execute(_userId,
							_currentPassword, _newPassword);
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

		mDialog = ProgressDialog.show(CommunityChangePassword.this, null,
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
	private class AsyncChangePasswordTask extends
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
			String _currentPassword = (String) args[1];
			String _newPassword = (String) args[2];

			String _response = "";
			try {
				HashMap<String, String> parameters = new HashMap<String, String>();
				parameters
						.put("q", AppConfig.COMMUNITY_CHANGE_PASSWORD_Q_VALUE);

				parameters.put("userid", URLEncoder.encode(_userId, "UTF-8"));
				parameters.put("currentpassword", URLEncoder.encode(
						_currentPassword, "UTF-8"));
				parameters.put("newpassword", URLEncoder.encode(_newPassword,
						"UTF-8"));

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
						mMessage
								.setText(mMainActivity
										.getString(R.string.password_change_successful));
						// make it visible
						mDone.setVisibility(View.VISIBLE);
					} else {
						JSONArray errorCodes = _responseJson
								.getJSONArray("errorCodes");
						for (int i = 0; i < errorCodes.length(); i++) {
							String errorCode = errorCodes.getString(i);
							if (errorCode
									.equalsIgnoreCase("CURRENT_PASSWORD_INVALID")) {
								mMessage.setText(mMainActivity
										.getString(R.string.login_failed));
								break;
							} else {
								mMessage
										.setText(mMainActivity
												.getString(R.string.password_change_failed));
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
