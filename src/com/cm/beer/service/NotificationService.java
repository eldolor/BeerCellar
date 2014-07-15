package com.cm.beer.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;

import com.cm.beer.activity.Main;
import com.cm.beer.activity.R;
import com.cm.beer.config.AppConfig;
import com.cm.beer.util.Logger;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class NotificationService extends IntentService {

	private NotificationManager mNM;
	User mUser;
	Service mService;
	GoogleAnalyticsTracker mTracker;
	private SharedPreferences mPreferences;
	long mAWeekAgoMillis;
	String mDeviceId;

	public NotificationService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public NotificationService() {
		super("NotificationService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			if (intent.getExtras() != null) {
				setup();
				String lAction = intent.getStringExtra("ALARM_TYPE");
				if (lAction.equals(AlarmType.NEW_BEER_REVIEW_ACTION.getType())) {
					processNewBeerReviewNotification();
				} else if (lAction
						.equals(AlarmType.NEW_BEER_REVIEW_FROM_FOLLOWING_ACTION
								.getType())) {
					processNewBeerReviewFromFollowingNotification();
				} else if (lAction.equals(AlarmType.BEER_OF_THE_DAY_ACTION
						.getType())) {
					processBeerOfTheDayNotification();
				}
			}

		} catch (Throwable e) {
			Logger.error(e.getMessage(), e);
		} finally {
			if (Logger.isLogEnabled())
				Logger.log("onHandleIntent():: Exiting");
		}
	}

	public void setup() {
		mService = this;
		if (Logger.isLogEnabled())
			Logger.log("onCreate");
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
		if (Logger.isLogEnabled())
			Logger.log("onCreate::Right Now is "
					+ (sdf.format(rightNow.getTime())));
		rightNow.add(Calendar.DAY_OF_MONTH, -7);
		if (Logger.isLogEnabled())
			Logger.log("onCreate::A Week Ago was "
					+ (sdf.format(rightNow.getTime())));
		mAWeekAgoMillis = rightNow.getTimeInMillis();

		if (Logger.isLogEnabled())
			Logger.log("onCreate:Google Tracker Instantiated");

		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mDeviceId = telephonyManager.getDeviceId();
	}

	public void removeNotification(int notificationId) {
		// Cancel the persistent notification.
		mNM.cancel(notificationId);
	}

	/**
	 * 
	 * @param period
	 */
	private void processNewBeerReviewNotification() {
		if (Logger.isLogEnabled())
			Logger.log("startService");
		try {

			String notificationLastChecked = String
					.valueOf(mPreferences
							.getLong(
									AppConfig.NEW_BEER_REVIEW_NOTIFICATION_LAST_CHECKED,
									mAWeekAgoMillis));
			if (Logger.isLogEnabled())
				Logger.log("TimerTask:run():"
						+ AppConfig.RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS);
			String _userId = mUser.getUserId();
			_userId = ((_userId == null) || (_userId.equals("")) ? ("IMEI|" + mDeviceId)
					: _userId);
			String _url1 = Util.getNewBeerReviewsNotificationUrl(_userId,
					notificationLastChecked);
			if (Logger.isLogEnabled())
				Logger.log(_url1);
			int notificationId1 = R.string.notification_new_beer_reviews;
			String message1 = getString(R.string.notification_new_beer_reviews);
			new AsyncGetNewBeerReviewNotification().execute(_url1,
					notificationId1, message1);

		} catch (Throwable e) {
			if (Logger.isLogEnabled())
				Logger.error(
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
			mTracker.trackEvent("NotificationService", "StartServiceError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: "").replace(" ", "_"), 0);
			mTracker.dispatch();
		}

	}

	private void processNewBeerReviewFromFollowingNotification() {
		try {
			String notificationLastChecked = String
					.valueOf(mPreferences
							.getLong(
									AppConfig.NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATION_LAST_CHECKED,
									mAWeekAgoMillis));
			if (mUser.isLoggedIn()) {
				String _url2 = Util
						.getNewBeerReviewsFromFollowingNotificationUrl(
								mUser.getUserId(), notificationLastChecked);
				if (Logger.isLogEnabled())
					Logger.log(_url2);
				int notificationId2 = R.string.notification_new_beer_reviews_from_following;
				String message2 = getString(R.string.notification_new_beer_reviews_from_following);
				new AsyncGetNewBeerReviewFromFollowingNotification().execute(
						_url2, notificationId2, message2);
			}

		} catch (Throwable e) {
			if (Logger.isLogEnabled())
				Logger.error(
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
			mTracker.trackEvent("NotificationService", "StartServiceError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: "").replace(" ", "_"), 0);
			mTracker.dispatch();
		}

	}

	private void processBeerOfTheDayNotification() {
		try {
			String notificationLastChecked = String
					.valueOf(mPreferences
							.getLong(
									AppConfig.BEER_OF_THE_DAY_NOTIFICATION_LAST_CHECKED,
									mAWeekAgoMillis));
			String _userId = mUser.getUserId();
			_userId = ((_userId == null) || (_userId.equals("")) ? ("IMEI|" + mDeviceId)
					: _userId);
			String _url = Util.getBeerOfTheDayNotificationUrl(_userId,
					notificationLastChecked);
			if (Logger.isLogEnabled())
				Logger.log(_url);
			int notificationId = R.string.notification_beer_of_the_day;
			String message = getString(R.string.notification_beer_of_the_day);
			new AsyncGetBeerOfTheDayNotification().execute(_url,
					notificationId, message);

		} catch (Throwable e) {
			if (Logger.isLogEnabled())
				Logger.error(
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
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

				Intent intent = new Intent(this, Main.class);
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
			if (Logger.isLogEnabled())
				Logger.error(
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
			mTracker.trackEvent("NotificationService", "ShowNotificationError",
					((e.getMessage() != null) ? e.getMessage()
							.replace(" ", "_") : "").replace(" ", "_"), 0);
			mTracker.dispatch();
		}

	}

	/************************************************************************************/
	private class AsyncGetNewBeerReviewNotification extends
			AsyncTask<Object, Void, Object> {
		private JSONArray _mJsonArray = null;
		private int _mNotificationId;
		private String _mMessage;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (Logger.isLogEnabled())
				Logger.log("doInBackground starting");
			String url = (String) args[0];
			_mNotificationId = (Integer) args[1];
			_mMessage = (String) args[2];

			try {
				String response[] = Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("{"))) {
					JSONObject json = new JSONObject(response[0]);
					long _lastChecked = json.getLong("lastChecked");

					if (Logger.isLogEnabled())
						Logger.log("doInBackground lastChecked=" + _lastChecked);
					mPreferences
							.edit()
							.putLong(
									AppConfig.NEW_BEER_REVIEW_NOTIFICATION_LAST_CHECKED,
									_lastChecked).commit();

					_mJsonArray = json.getJSONArray("beerIdList");
					if (Logger.isLogEnabled())
						Logger.log(_mJsonArray.length() + " new beer reviews");
				}

			} catch (Throwable e) {
				if (Logger.isLogEnabled())
					Logger.error("error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);
				mTracker.trackEvent(
						"NotificationService",
						"AsyncGetNewBeerReviewsNotificationError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			if (Logger.isLogEnabled())
				Logger.log("doInBackground finished");
			return null;
		}

		protected void onPostExecute(Object result) {
			if (Logger.isLogEnabled())
				Logger.log("onPostExecute starting");
			showNotification(_mNotificationId, _mJsonArray, _mMessage);
			if (Logger.isLogEnabled())
				Logger.log("onPostExecute finished");
		}

	}

	/************************************************************************************/
	private class AsyncGetNewBeerReviewFromFollowingNotification extends
			AsyncTask<Object, Void, Object> {
		private JSONArray _mJsonArray = null;
		private int _mNotificationId;
		private String _mMessage;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (Logger.isLogEnabled())
				Logger.log("doInBackground starting");
			String url = (String) args[0];
			_mNotificationId = (Integer) args[1];
			_mMessage = (String) args[2];

			try {
				String response[] = Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("{"))) {
					JSONObject json = new JSONObject(response[0]);
					long _lastChecked = json.getLong("lastChecked");

					if (Logger.isLogEnabled())
						Logger.log("doInBackground lastChecked=" + _lastChecked);
					mPreferences
							.edit()
							.putLong(
									AppConfig.NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATION_LAST_CHECKED,
									_lastChecked).commit();

					long _checkBackLaterIn = json.getLong("checkBackLaterIn");

					_mJsonArray = json.getJSONArray("beerIdList");
					if (Logger.isLogEnabled())
						Logger.log(_mJsonArray.length() + " new beer reviews");
				}

			} catch (Throwable e) {
				if (Logger.isLogEnabled())
					Logger.error("error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);
				mTracker.trackEvent(
						"NotificationService",
						"AsyncGetNewBeerReviewsFromFollowingNotificationError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			if (Logger.isLogEnabled())
				Logger.log("doInBackground finished");
			return null;
		}

		protected void onPostExecute(Object result) {
			if (Logger.isLogEnabled())
				Logger.log("onPostExecute starting");
			showNotification(_mNotificationId, _mJsonArray, _mMessage);
			if (Logger.isLogEnabled())
				Logger.log("onPostExecute finished");
		}

	}

	/************************************************************************************/
	private class AsyncGetBeerOfTheDayNotification extends
			AsyncTask<Object, Void, Object> {
		private JSONArray _mJsonArray = null;
		private int _mNotificationId;
		private String _mMessage;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (Logger.isLogEnabled())
				Logger.log("doInBackground starting");
			String url = (String) args[0];
			_mNotificationId = (Integer) args[1];
			_mMessage = (String) args[2];

			try {
				String response[] = Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("{"))) {
					JSONObject json = new JSONObject(response[0]);
					long _lastChecked = json.getLong("lastChecked");

					if (Logger.isLogEnabled())
						Logger.log("doInBackground lastChecked=" + _lastChecked);
					mPreferences
							.edit()
							.putLong(
									AppConfig.BEER_OF_THE_DAY_NOTIFICATION_LAST_CHECKED,
									_lastChecked).commit();

					_mJsonArray = json.getJSONArray("beerIdList");
					if (Logger.isLogEnabled())
						Logger.log(_mJsonArray.length() + " new beer reviews");
				}

			} catch (Throwable e) {
				if (Logger.isLogEnabled())
					Logger.error("error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);
				mTracker.trackEvent(
						"NotificationService",
						"AsyncGetBeerOfTheDayNotificationError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			if (Logger.isLogEnabled())
				Logger.log("doInBackground finished");
			return null;
		}

		protected void onPostExecute(Object result) {
			if (Logger.isLogEnabled())
				Logger.log("onPostExecute starting");
			showNotification(_mNotificationId, _mJsonArray, _mMessage);
			if (Logger.isLogEnabled())
				Logger.log("onPostExecute finished");
		}

	}

}
