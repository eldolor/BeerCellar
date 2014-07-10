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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

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
	CheckBox mCommentPostedEmailSubscription;
	Spinner mBeerListRowsPerPage;

	User mUser;
	SharedPreferences mPreferences;
	Activity mMainActivity;

	static final int EMAIL_SUBSCRIPTION_REQUEST_CODE = 0;
	static final int COMMENT_POSTED_EMAIL_SUBSCRIPTION_REQUEST_CODE = 1;

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
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID,
				this);
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}

		setContentView(R.layout.set_preferences);
		mUser = new User(this);
		mPreferences = this.getSharedPreferences(
				this.getString(R.string.app_name), Activity.MODE_PRIVATE);
		display();
		if (mUser.isLoggedIn()) {
			new AsyncGetEmailSubscriptionStatusTask()
					.execute(mUser.getUserId());
			new AsyncGetCommentPostedEmailSubscriptionStatusTask()
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
					startActivityForResult(intent,
							EMAIL_SUBSCRIPTION_REQUEST_CODE);
				}
			}
		});
		/****************************************/

		mCommentPostedEmailSubscription = (CheckBox) findViewById(R.id.comment_posted_email_subscription);
		mCommentPostedEmailSubscription.setChecked(false);
		// if user not logged in
		mCommentPostedEmailSubscription
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (!mUser.isLoggedIn()) {
							Intent intent = new Intent(mMainActivity
									.getApplication(), LoginIntercept.class);
							intent.putExtra("FACEBOOK_PERMISSIONS",
									AppConfig.FACEBOOK_PERMISSIONS);
							startActivityForResult(intent,
									COMMENT_POSTED_EMAIL_SUBSCRIPTION_REQUEST_CODE);
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
				mPreferences
						.edit()
						.putBoolean(
								AppConfig.RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS,
								mReceiveNewNotification.isChecked()).commit();
				mPreferences
						.edit()
						.putBoolean(
								AppConfig.RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS,
								mReceiveNewFromFollowingNotification
										.isChecked()).commit();
				mPreferences
						.edit()
						.putBoolean(
								AppConfig.RECEIVE_BEER_OF_THE_DAY_NOTIFICATION,
								mReceiveBeerOfTheDayNotification.isChecked())
						.commit();
				Util.evaluateNotificationService(mMainActivity);

				if (mUser.isLoggedIn()) {
					String _emailSubscriptionStatus = (mEmailSubscription
							.isChecked()) ? "Y" : "N";
					new AsyncUpdateEmailSubscriptionStatusTask().execute(
							mUser.getUserId(), _emailSubscriptionStatus);
					String _commentPostedEmailSubscriptionStatus = (mCommentPostedEmailSubscription
							.isChecked()) ? "Y" : "N";
					new AsyncUpdateCommentPostedEmailSubscriptionStatusTask()
							.execute(mUser.getUserId(),
									_commentPostedEmailSubscriptionStatus);
				}

				int rowsPerPage = AppConfig.BEER_LIST_ROWS_PER_PAGE;
				if (!mBeerListRowsPerPage.getSelectedItem().toString()
						.equals("")) {
					rowsPerPage = Integer.valueOf(mBeerListRowsPerPage
							.getSelectedItem().toString());
				}

				mPreferences
						.edit()
						.putInt(AppConfig.PREFERENCE_BEER_LIST_ROWS_PER_PAGE,
								rowsPerPage).commit();
				// send stats
				if (mReceiveNewNotification.isChecked()) {
					mTracker.trackEvent("SetPreferences",
							"ReceiveNewNotification", "Y", 0);
				} else {
					mTracker.trackEvent("SetPreferences",
							"ReceiveNewNotification", "N", 0);
				}
				if (mReceiveNewFromFollowingNotification.isChecked()) {
					mTracker.trackEvent("SetPreferences",
							"ReceiveNewFromFollowingNotification", "Y", 0);
				} else {
					mTracker.trackEvent("SetPreferences",
							"ReceiveNewFromFollowingNotification", "N", 0);
				}
				if (mReceiveBeerOfTheDayNotification.isChecked()) {
					mTracker.trackEvent("SetPreferences",
							"ReceiveBeerOfTheDayNotification", "Y", 0);
				} else {
					mTracker.trackEvent("SetPreferences",
							"ReceiveBeerOfTheDayNotification", "N", 0);
				}

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
		mBeerListRowsPerPage = (Spinner) findViewById(R.id.beer_list_rows_per_page_options);
		ArrayAdapter<CharSequence> adapter6 = ArrayAdapter.createFromResource(
				this, R.array.beer_list_rows_per_page_options,
				android.R.layout.simple_spinner_item);
		adapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mBeerListRowsPerPage.setAdapter(adapter6);
		{
			final CharSequence[] options = getResources().getStringArray(
					R.array.beer_list_rows_per_page_options);
			int position = 0;
			// cast to string for comparision below
			String rowsPerPage = String.valueOf(mPreferences.getInt(
					AppConfig.PREFERENCE_BEER_LIST_ROWS_PER_PAGE,
					AppConfig.BEER_LIST_ROWS_PER_PAGE));

			// traverse for a match
			for (int i = 0; i < options.length; i++) {
				if (options[i].equals(rowsPerPage)) {
					position = i;
					break;
				}
			}

			mBeerListRowsPerPage.setSelection(position, true);
		}

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
		if (requestCode == EMAIL_SUBSCRIPTION_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				display();
				if (mUser.isLoggedIn()) {
					String _emailSubscriptionStatus = (mEmailSubscription
							.isChecked()) ? "Y" : "N";
					new AsyncUpdateEmailSubscriptionStatusTask().execute(
							mUser.getUserId(), _emailSubscriptionStatus);
				}
			}
		} else if (requestCode == COMMENT_POSTED_EMAIL_SUBSCRIPTION_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				display();
				if (mUser.isLoggedIn()) {
					String _emailSubscriptionStatus = (mCommentPostedEmailSubscription
							.isChecked()) ? "Y" : "N";
					new AsyncUpdateCommentPostedEmailSubscriptionStatusTask()
							.execute(mUser.getUserId(),
									_emailSubscriptionStatus);
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
		mDialog = ProgressDialog.show(SetPreferences.this, null,
				this.getString(R.string.progress_loading_message), true, true);
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
				Log.e(TAG,
						(e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "", e);
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
					mEmailSubscription.setChecked((_mEmailSubscriptionStatus != null
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
				Log.e(TAG,
						(e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "", e);
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
	/*******************************************************************************/
	private class AsyncGetCommentPostedEmailSubscriptionStatusTask extends
			AsyncTask<String, Void, Object> {
		String _mStatus;

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
				String url = Util.getCommentPostedEmailSubscriptionStatusUrl(userId);

				Log.i(TAG, "doInBackground:" + url);
				String response[] = Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("{"))) {
					JSONObject json = new JSONObject(response[0]);
					_mStatus = json
							.getString("commentpostedemailsubscriptionstatus");

				}
			} catch (Throwable e) {
				Log.e(TAG,
						(e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "", e);
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
					mCommentPostedEmailSubscription.setChecked((_mStatus != null
							&& (_mStatus.equals("Y")) ? true
							: false));
				}
			});
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPostExecute finished");
			}
		}

	}

	/*******************************************************************************/
	private class AsyncUpdateCommentPostedEmailSubscriptionStatusTask extends
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
				String url = Util
						.updateCommentPostedEmailSubscriptionStatusUrl(userId,
								emailSubscription);

				Log.i(TAG, "doInBackground:" + url);
				String response[] = Util.getResult(url);
				Log.i(TAG, response[0]);
			} catch (Throwable e) {
				Log.e(TAG,
						(e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "", e);
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
