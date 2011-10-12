package com.cm.beer.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.Reflect;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class NotificationService extends Service {

	String TAG;
	private NotificationManager mNM;
	User mUser;
	Service mService;
	GoogleAnalyticsTracker mTracker;
	private SharedPreferences mPreferences;
	private Timer mNewBeerReviewTimer = new Timer();
	private Timer mNewBeerReviewFromFollowingTimer = new Timer();
	private Timer mBeerOfTheDayTimer = new Timer();
	long mAWeekAgoMillis;
	String mDeviceId;

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		NotificationService getService() {
			Log.i(TAG, "LocalBinder:getService()");
			return NotificationService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		mService = this;
		Log.i(TAG, "onCreate");
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mUser = new User(mService);
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID,
				this);
		mPreferences = getSharedPreferences(getString(R.string.app_name),
				Activity.MODE_PRIVATE);
		SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy hh:mm aaa");
		Calendar rightNow = Calendar.getInstance();
		Log
				.i(TAG, "onCreate::Right Now is "
						+ (sdf.format(rightNow.getTime())));
		rightNow.add(Calendar.DAY_OF_MONTH, -7);
		Log.i(TAG, "onCreate::A Week Ago was "
				+ (sdf.format(rightNow.getTime())));
		mAWeekAgoMillis = rightNow.getTimeInMillis();

		Log.i(TAG, "onCreate:Google Tracker Instantiated");

		if (!Reflect.service_startForeground(1, new Notification(), this)) {
			// Fall back on the old API.
			setForeground(true);
		}
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mDeviceId = telephonyManager.getDeviceId();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind");
		return mBinder;
	}

	// This is the object that receives interactions from clients.
	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		// Cancel all persistent notifications
		mNM.cancelAll();
		mNewBeerReviewTimer.cancel();
		mNewBeerReviewFromFollowingTimer.cancel();
		mBeerOfTheDayTimer.cancel();
		if (!Reflect.service_stopForeground(true, this)) {
			// Fall back on the old API.
			setForeground(false);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "onStart");
		handleCommand(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		handleCommand(intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	private void handleCommand(Intent intent) {
		Log.i(TAG, "handleCommand");
		// default to true
		if (!mPreferences
				.contains(AppConfig.RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS)) {
			mPreferences.edit().putBoolean(
					AppConfig.RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS, true)
					.commit();
		}
		if (!mPreferences
				.contains(AppConfig.RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS)) {
			mPreferences
					.edit()
					.putBoolean(
							AppConfig.RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS,
							true).commit();
		}
		if (!mPreferences
				.contains(AppConfig.RECEIVE_BEER_OF_THE_DAY_NOTIFICATION)) {
			mPreferences.edit().putBoolean(
					AppConfig.RECEIVE_BEER_OF_THE_DAY_NOTIFICATION, true)
					.commit();
		}

		scheduleNewBeerReviewTimerAtFixedRate(AppConfig.NOTIFICATION_CHECK_INTERVAL);
		scheduleNewBeerReviewFromFollowingTimerAtFixedRate(AppConfig.NOTIFICATION_CHECK_INTERVAL);
		scheduleBeerOfTheDayTimerAtFixedRate(AppConfig.NOTIFICATION_CHECK_INTERVAL);

	}

	public void removeNotification(int notificationId) {
		// Cancel the persistent notification.
		mNM.cancel(notificationId);
	}

	/**
	 * 
	 * @param period
	 */
	private void scheduleNewBeerReviewTimerAtFixedRate(long period) {
		Log.i(TAG, "startService");
		try {
			mNewBeerReviewTimer.scheduleAtFixedRate(new TimerTask() {

				public void run() {
					Log.i(TAG, "TimerTask:run()");
					String notificationLastChecked = String
							.valueOf(mPreferences
									.getLong(
											AppConfig.NEW_BEER_REVIEW_NOTIFICATION_LAST_CHECKED,
											mAWeekAgoMillis));
					if (mPreferences.getBoolean(
							AppConfig.RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS,
							true)) {
						Log
								.i(
										TAG,
										"TimerTask:run():"
												+ AppConfig.RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS);
						String _userId = mUser.getUserId();
						_userId = ((_userId == null) || (_userId.equals("")) ? ("IMEI|" + mDeviceId)
								: _userId);
						String _url1 = Util.getNewBeerReviewsNotificationUrl(
								_userId, notificationLastChecked);
						Log.i(TAG, _url1);
						int notificationId1 = R.string.notification_new_beer_reviews;
						String message1 = getString(R.string.notification_new_beer_reviews);
						new AsyncGetNewBeerReviewNotification().execute(_url1,
								notificationId1, message1);
					} else {
						Log
								.i(TAG,
										"TimerTask:run():User does not want to receive new beer reviews notifications!");
					}
				}

			}, 0, period);

			Log.i(TAG, "New Beer Review Timer scheduled at fixed rate: " + period);

		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			mTracker.trackEvent("NotificationService", "StartServiceError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: "").replace(" ", "_"), 0);
			mTracker.dispatch();
		}

	}

	private void scheduleNewBeerReviewFromFollowingTimerAtFixedRate(long period) {
		Log.i(TAG, "startService");
		try {
			mNewBeerReviewFromFollowingTimer.scheduleAtFixedRate(new TimerTask() {

				public void run() {
					Log.i(TAG, "TimerTask:run()");
					String notificationLastChecked = String
							.valueOf(mPreferences
									.getLong(
											AppConfig.NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATION_LAST_CHECKED,
											mAWeekAgoMillis));
					if (mUser.isLoggedIn()) {
						Log
								.i(
										TAG,
										"TimerTask:run():User Logged In:"
												+ AppConfig.RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS);
						if (mPreferences
								.getBoolean(
										AppConfig.RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS,
										true)) {
							String _url2 = Util
									.getNewBeerReviewsFromFollowingNotificationUrl(
											mUser.getUserId(),
											notificationLastChecked);
							Log.i(TAG, _url2);
							int notificationId2 = R.string.notification_new_beer_reviews_from_following;
							String message2 = getString(R.string.notification_new_beer_reviews_from_following);
							new AsyncGetNewBeerReviewFromFollowingNotification().execute(_url2,
									notificationId2, message2);
						} else {
							Log
									.i(
											TAG,
											"TimerTask:run():User does not want to receive new beer reviews from following notifications!");
						}

					} else {
						Log
								.i(
										TAG,
										"TimerTask:run():User NOT Logged In!:"
												+ AppConfig.RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS);
					}
				}

			}, 0, period);

			Log.i(TAG, "New Beer Review From Following Timer scheduled at fixed rate: " + period);

		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			mTracker.trackEvent("NotificationService", "StartServiceError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: "").replace(" ", "_"), 0);
			mTracker.dispatch();
		}

	}

	private void scheduleBeerOfTheDayTimerAtFixedRate(long period) {
		Log.i(TAG, "startService");
		try {
			mBeerOfTheDayTimer.scheduleAtFixedRate(new TimerTask() {

				public void run() {
					Log.i(TAG, "TimerTask:run()");
					String notificationLastChecked = String
							.valueOf(mPreferences
									.getLong(
											AppConfig.BEER_OF_THE_DAY_NOTIFICATION_LAST_CHECKED,
											mAWeekAgoMillis));
					if (mPreferences.getBoolean(
							AppConfig.RECEIVE_BEER_OF_THE_DAY_NOTIFICATION,
							true)) {
						Log
								.i(
										TAG,
										"TimerTask:run():"
												+ AppConfig.RECEIVE_BEER_OF_THE_DAY_NOTIFICATION);
						String _userId = mUser.getUserId();
						_userId = ((_userId == null) || (_userId.equals("")) ? ("IMEI|" + mDeviceId)
								: _userId);
						String _url = Util.getBeerOfTheDayNotificationUrl(
								_userId, notificationLastChecked);
						Log.i(TAG, _url);
						int notificationId = R.string.notification_beer_of_the_day;
						String message = getString(R.string.notification_beer_of_the_day);
						new AsyncGetBeerOfTheDayNotification().execute(_url,
								notificationId, message);
					} else {
						Log
								.i(TAG,
										"TimerTask:run():User does not want to receive new beer reviews notifications!");
					}
					/************************************/
				}

			}, 0, period);

			Log.i(TAG, "Beer of the Day Timer scheduled at fixed rate: " + period);

		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			mTracker.trackEvent("NotificationService", "StartServiceError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: "").replace(" ", "_"), 0);
			mTracker.dispatch();
		}

	}


	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification(int notificationId, JSONArray jsonArray,
			CharSequence message) {
		try {
			if ((jsonArray != null) && (jsonArray.length() > 0)) {
				// In this sample, we'll use the same text for the ticker and
				// the
				// expanded notification
				CharSequence text = jsonArray.length() + " " + message;

				// Set the icon, scrolling text and timestamp
				Notification notification = new Notification(R.drawable.icon,
						text, System.currentTimeMillis());
				notification.number = jsonArray.length();
				notification.defaults |= Notification.DEFAULT_SOUND;
				notification.flags = Notification.FLAG_AUTO_CANCEL;

				// The PendingIntent to launch our activity if the user selects
				// this
				// notification
				Intent intent = new Intent(this, CommunityBeers.class);
				if (notificationId == R.string.notification_beer_of_the_day) {
					intent.putExtra("OPTION",
							AppConfig.COMMUNITY_BEER_OF_THE_DAY);
				} else {
					intent.putExtra("OPTION",
							AppConfig.COMMUNITY_NEW_BEER_REVIEWS);
				}
				intent.putExtra("BEERIDS", jsonArray.toString());
				intent.putExtra("NOTIFICATIONID", notificationId);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				PendingIntent contentIntent = PendingIntent.getActivity(this,
						notificationId, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				// Set the info for the views that show in the notification
				// panel.
				notification.setLatestEventInfo(this,
						getText(R.string.app_name), text, contentIntent);

				// Send the notification.
				// We use a layout id because it is a unique number. We use it
				// later
				// to
				// cancel.
				mNM.notify(notificationId, notification);
			}
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			mTracker.trackEvent("NotificationService", "ShowNotificationError",
					((e.getMessage() != null) ? e.getMessage()
							.replace(" ", "_") : "").replace(" ", "_"), 0);
			mTracker.dispatch();
		}

	}

	/************************************************************************************/
	private class AsyncGetNewBeerReviewNotification extends AsyncTask<Object, Void, Object> {
		private JSONArray _mJsonArray = null;
		private int _mNotificationId;
		private String _mMessage;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			Log.i(TAG, "doInBackground starting");
			String url = (String) args[0];
			_mNotificationId = (Integer) args[1];
			_mMessage = (String) args[2];

			try {
				String response[] = Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("{"))) {
					JSONObject json = new JSONObject(response[0]);
					long _lastChecked = json.getLong("lastChecked");

					Log.i(TAG, "doInBackground lastChecked=" + _lastChecked);
					mPreferences.edit().putLong(
							AppConfig.NEW_BEER_REVIEW_NOTIFICATION_LAST_CHECKED, _lastChecked)
							.commit();

					long _checkBackLaterIn = json.getLong("checkBackLaterIn");
					Log.i(TAG, "doInBackground checkBackLaterIn="
							+ _checkBackLaterIn);

					long _existingCBLIn = mPreferences.getLong(
							AppConfig.NEW_BEER_REVIEW_NOTIFICATION_CHECK_BACK_LATER_IN, 0L);

					if ((_checkBackLaterIn != 0L)
							&& (_checkBackLaterIn != _existingCBLIn)) {
						mPreferences.edit().putLong(
								AppConfig.NEW_BEER_REVIEW_NOTIFICATION_CHECK_BACK_LATER_IN,
								_checkBackLaterIn).commit();
						mNewBeerReviewTimer.cancel();
						mNewBeerReviewTimer  = new Timer();
						scheduleNewBeerReviewTimerAtFixedRate(_checkBackLaterIn);
					}

					_mJsonArray = json.getJSONArray("beerIdList");
					Log.i(TAG, _mJsonArray.length() + " new beer reviews");
				}

			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("NotificationService",
						"AsyncGetNewBeerReviewsNotificationError", ((e
								.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			Log.i(TAG, "doInBackground finished");
			return null;
		}

		protected void onPostExecute(Object result) {
			Log.i(TAG, "onPostExecute starting");
			showNotification(_mNotificationId, _mJsonArray, _mMessage);
			Log.i(TAG, "onPostExecute finished");
		}

	}
	/************************************************************************************/
	private class AsyncGetNewBeerReviewFromFollowingNotification extends AsyncTask<Object, Void, Object> {
		private JSONArray _mJsonArray = null;
		private int _mNotificationId;
		private String _mMessage;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			Log.i(TAG, "doInBackground starting");
			String url = (String) args[0];
			_mNotificationId = (Integer) args[1];
			_mMessage = (String) args[2];

			try {
				String response[] = Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("{"))) {
					JSONObject json = new JSONObject(response[0]);
					long _lastChecked = json.getLong("lastChecked");

					Log.i(TAG, "doInBackground lastChecked=" + _lastChecked);
					mPreferences.edit().putLong(
							AppConfig.NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATION_LAST_CHECKED, _lastChecked)
							.commit();

					long _checkBackLaterIn = json.getLong("checkBackLaterIn");
					Log.i(TAG, "doInBackground checkBackLaterIn="
							+ _checkBackLaterIn);

					long _existingCBLIn = mPreferences.getLong(
							AppConfig.NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATION_CHECK_BACK_LATER_IN, 0L);

					if ((_checkBackLaterIn != 0L)
							&& (_checkBackLaterIn != _existingCBLIn)) {
						mPreferences.edit().putLong(
								AppConfig.NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATION_CHECK_BACK_LATER_IN,
								_checkBackLaterIn).commit();
						mNewBeerReviewFromFollowingTimer.cancel();
						mNewBeerReviewFromFollowingTimer = new Timer();
						scheduleNewBeerReviewFromFollowingTimerAtFixedRate(_checkBackLaterIn);
					}

					_mJsonArray = json.getJSONArray("beerIdList");
					Log.i(TAG, _mJsonArray.length() + " new beer reviews");
				}

			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("NotificationService",
						"AsyncGetNewBeerReviewsFromFollowingNotificationError", ((e
								.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			Log.i(TAG, "doInBackground finished");
			return null;
		}

		protected void onPostExecute(Object result) {
			Log.i(TAG, "onPostExecute starting");
			showNotification(_mNotificationId, _mJsonArray, _mMessage);
			Log.i(TAG, "onPostExecute finished");
		}

	}
	/************************************************************************************/
	private class AsyncGetBeerOfTheDayNotification extends AsyncTask<Object, Void, Object> {
		private JSONArray _mJsonArray = null;
		private int _mNotificationId;
		private String _mMessage;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			Log.i(TAG, "doInBackground starting");
			String url = (String) args[0];
			_mNotificationId = (Integer) args[1];
			_mMessage = (String) args[2];

			try {
				String response[] = Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("{"))) {
					JSONObject json = new JSONObject(response[0]);
					long _lastChecked = json.getLong("lastChecked");

					Log.i(TAG, "doInBackground lastChecked=" + _lastChecked);
					mPreferences.edit().putLong(
							AppConfig.BEER_OF_THE_DAY_NOTIFICATION_LAST_CHECKED, _lastChecked)
							.commit();

					long _checkBackLaterIn = json.getLong("checkBackLaterIn");
					Log.i(TAG, "doInBackground checkBackLaterIn="
							+ _checkBackLaterIn);

					long _existingCBLIn = mPreferences.getLong(
							AppConfig.BEER_OF_THE_DAY_NOTIFICATION_CHECK_BACK_LATER_IN, 0L);

					if ((_checkBackLaterIn != 0L)
							&& (_checkBackLaterIn != _existingCBLIn)) {
						mPreferences.edit().putLong(
								AppConfig.BEER_OF_THE_DAY_NOTIFICATION_CHECK_BACK_LATER_IN,
								_checkBackLaterIn).commit();
						mBeerOfTheDayTimer.cancel();
						mBeerOfTheDayTimer = new Timer();
						scheduleBeerOfTheDayTimerAtFixedRate(_checkBackLaterIn);
					}

					_mJsonArray = json.getJSONArray("beerIdList");
					Log.i(TAG, _mJsonArray.length() + " new beer reviews");
				}

			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("NotificationService",
						"AsyncGetBeerOfTheDayNotificationError", ((e
								.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			Log.i(TAG, "doInBackground finished");
			return null;
		}

		protected void onPostExecute(Object result) {
			Log.i(TAG, "onPostExecute starting");
			showNotification(_mNotificationId, _mJsonArray, _mMessage);
			Log.i(TAG, "onPostExecute finished");
		}

	}

}
