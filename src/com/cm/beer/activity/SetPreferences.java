package com.cm.beer.activity;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class SetPreferences extends Activity {
	String TAG;
	ProgressDialog mDialog;

	GoogleAnalyticsTracker mTracker;
	Button mSave;
	Button mCancel;
	Button mLogout;
	CheckBox mReceiveNewNotification;
	CheckBox mReceiveNewFromFollowingNotification;
	CheckBox mReceiveBeerOfTheDayNotification;
	CheckBox mEmailSubscription;

	User mUser;
	SharedPreferences mPreferences;
	Activity mMainActivity;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate");
		}
		mMainActivity = this;

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the mTracker with dispatch interval
		mTracker.start(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}

		setContentView(R.layout.set_preferences);
		mUser = new User(this);
		mPreferences = this.getSharedPreferences(this
				.getString(R.string.app_name), Activity.MODE_PRIVATE);
		display();
		if (mUser.isLoggedIn()) {
			new AsyncGetEmailSubscriptionStatusTask()
					.execute(mUser.getUserId());
		}
	}

	/*
	 * 
	 */
	protected void display() {

		Log.i(TAG, "display");

		/****************************************/
		mReceiveNewNotification = (CheckBox) findViewById(R.id.receive_new_beer_reviews_notification);
		mReceiveNewNotification.setChecked(mPreferences.getBoolean(
				AppConfig.RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS, true));
		/****************************************/
		mReceiveNewFromFollowingNotification = (CheckBox) findViewById(R.id.receive_new_beer_reviews_from_following_notification);
		mReceiveNewFromFollowingNotification
				.setChecked(mPreferences
						.getBoolean(
								AppConfig.RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS,
								true));
		/****************************************/
		mReceiveBeerOfTheDayNotification = (CheckBox) findViewById(R.id.receive_beer_of_the_day_notification);
		mReceiveBeerOfTheDayNotification.setChecked(mPreferences.getBoolean(
				AppConfig.RECEIVE_BEER_OF_THE_DAY_NOTIFICATION, true));
		/****************************************/

		mEmailSubscription = (CheckBox) findViewById(R.id.email_subscription);
		mEmailSubscription.setChecked(false);
		// if user not logged in
		mEmailSubscription.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!mUser.isLoggedIn()) {
					Intent intent = new Intent(mMainActivity.getApplication(),
							LoginIntercept.class);
					intent.putExtra("FACEBOOK_PERMISSIONS",
							AppConfig.FACEBOOK_PERMISSIONS);
					startActivityForResult(intent, 0);
				}
			}
		});

		/****************************************/
		mSave = (Button) findViewById(R.id.save);
		mSave.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				mPreferences.edit().putBoolean(
						AppConfig.RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS,
						mReceiveNewNotification.isChecked()).commit();
				mPreferences
						.edit()
						.putBoolean(
								AppConfig.RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS,
								mReceiveNewFromFollowingNotification
										.isChecked()).commit();
				mPreferences.edit().putBoolean(
						AppConfig.RECEIVE_BEER_OF_THE_DAY_NOTIFICATION,
						mReceiveBeerOfTheDayNotification.isChecked()).commit();
				Util.evaluateNotificationService(mMainActivity);

				String _emailSubscriptionStatus = (mEmailSubscription
						.isChecked()) ? "Y" : "N";
				new AsyncUpdateEmailSubscriptionStatusTask().execute(mUser
						.getUserId(), _emailSubscriptionStatus);

				setResult(RESULT_OK);
				finish();
			}
		});
		/****************************************/
		mCancel = (Button) findViewById(R.id.cancel);
		mCancel.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks

				Log.i(TAG, "cancel");

				showDialog(AppConfig.DIALOG_LOADING_ID);
				setResult(RESULT_OK);
				finish();
			}
		});
		/****************************************/
		mLogout = (Button) findViewById(R.id.logout);
		if (mUser.isLoggedIn()) {
			mLogout.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
					PorterDuff.Mode.MULTIPLY);
		} else {
			mLogout.getBackground().setColorFilter(AppConfig.BUTTON_COLOR_RED,
					PorterDuff.Mode.MULTIPLY);
			mLogout.setText(R.string.logged_out_label);
		}
		mLogout.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				mUser.onLogoutFinish();
				if (mUser.isLoggedIn()) {
					mLogout.getBackground().setColorFilter(
							AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
				} else {
					mLogout.getBackground().setColorFilter(
							AppConfig.BUTTON_COLOR_RED,
							PorterDuff.Mode.MULTIPLY);
					mLogout.setText(R.string.logged_out_label);
				}
			}
		});
		/****************************************/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				display();
				if (mUser.isLoggedIn()) {
					String _emailSubscriptionStatus = (mEmailSubscription
							.isChecked()) ? "Y" : "N";
					new AsyncUpdateEmailSubscriptionStatusTask().execute(mUser
							.getUserId(), _emailSubscriptionStatus);
				}
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
		// Stop the mTracker when it is no longer needed.
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
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreateDialog");
		}
		mDialog = ProgressDialog.show(SetPreferences.this, null, this
				.getString(R.string.progress_loading_message), true, true);
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
			Log.i(TAG, "onResume");
		}
		if ((mDialog != null) && (mDialog.isShowing())) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onResume:active dialog removed");
			}
			removeDialog(AppConfig.DIALOG_LOADING_ID);
		}
		super.onResume();
	}

	/*******************************************************************************/
	private class AsyncGetEmailSubscriptionStatusTask extends
			AsyncTask<String, Void, Object> {
		String _mEmailSubscriptionStatus;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(String... args) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "doInBackground starting");
			}
			String userId = (String) args[0];

			try {
				String url = Util.getEmailSubscriptionStatusUrl(userId);

				Log.i(TAG, "doInBackground:" + url);
				String response[] = Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("{"))) {
					JSONObject json = new JSONObject(response[0]);
					_mEmailSubscriptionStatus = json
							.getString("emailsubscriptionstatus");

				}
			} catch (Throwable e) {
				Log.e(TAG, (e.getMessage() != null) ? e.getMessage().replace(
						" ", "_") : "", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPostExecute starting");
			}

			mMainActivity.runOnUiThread(new Runnable() {
				public void run() {
					mEmailSubscription
							.setChecked((_mEmailSubscriptionStatus != null
									&& (_mEmailSubscriptionStatus.equals("Y")) ? true
									: false));
				}
			});
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPostExecute finished");
			}
		}

	}

	/*******************************************************************************/
	private class AsyncUpdateEmailSubscriptionStatusTask extends
			AsyncTask<String, Void, Object> {

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(String... args) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "doInBackground starting");
			}
			String userId = (String) args[0];
			String emailSubscription = (String) args[1];

			try {
				String url = Util.updateEmailSubscriptionStatusUrl(userId,
						emailSubscription);

				Log.i(TAG, "doInBackground:" + url);
				String response[] = Util.getResult(url);
				Log.i(TAG, response[0]);
			} catch (Throwable e) {
				Log.e(TAG, (e.getMessage() != null) ? e.getMessage().replace(
						" ", "_") : "", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPostExecute starting");
			}

			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPostExecute finished");
			}
		}

	}

}
