package com.cm.beer.activity;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Main extends Activity implements Eula.OnEulaAgreedTo
{

	String TAG;
	boolean mAlreadyAgreedToEula = false;
	String mSelectedOption = null;
	ProgressDialog mSplashDialog;

	ImageView mBeerBBList;
	ImageView mHelp;
	ImageView mAroundMe;
	ImageView mCommunity;

	Activity mMainActivity;
	private NotificationManager mNM;
	User mUser;

	static final int ACTIVITY_ABOUT = 0;
	static final int MENU_GROUP = 0;
	static final int ABOUT_ID = Menu.FIRST;
	static final int SEND_ERROR_REPORT_ID = Menu.FIRST + 1;
	static final int PREFERENCES_ID = Menu.FIRST + 2;

	GoogleAnalyticsTracker mTracker;
	int mRandom;;

	// private static final String PREFERENCE_DO_NOT_SHOW_DID_YOU_KNOW =
	// "DO_NOT_SHOW_DID_YOU_KNOW";
	static final Handler mHandler = new Handler();

	private SharedPreferences mPreferences;

	public void onEulaAgreedTo()
	{
		mTracker.trackEvent("Main", "AgreedToEULA", "Y", 0);
		mTracker.dispatch();
	}

	public void onEulaNotAgreedTo()
	{
		mTracker.trackEvent("Main", "AgreedToEULA", "N", 0);
		mTracker.dispatch();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		if (AppConfig.LOGGING_ENABLED)
		{
			Log.i(TAG, "onCreate");
		}
		mMainActivity = this;
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mUser = new User(this);

		mPreferences = getSharedPreferences(getString(R.string.app_name),
				Activity.MODE_PRIVATE);

		/** Start the Notification Service **/
		Util.evaluateNotificationService(mMainActivity);

		mRandom = Util.getRandomInt(0,
				(AppConfig.DID_YOU_KNOW_MESSAGES.length - 1));

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID,
				this);
		if (AppConfig.LOGGING_ENABLED)
		{
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}

		// Start a new thread that will download all the data
		setContentView(R.layout.main);
		mBeerBBList = (ImageView) findViewById(R.id.beer_bb_list);
		mBeerBBList.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Intent intent = new Intent(mMainActivity.getApplication(),
						BeerList.class);
				startActivity(intent);
			}
		});
		mHelp = (ImageView) findViewById(R.id.help);
		mHelp.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				if (AppConfig.LOGGING_ENABLED)
				{
					Log.i(TAG, "Help");
				}
				showInstructions(mMainActivity);
				mTracker.trackEvent("Main", "BeerHelp", "Clicked", 0);
				mTracker.dispatch();
			}
		});
		mAroundMe = (ImageView) findViewById(R.id.around_me);
		mAroundMe.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				if (AppConfig.LOGGING_ENABLED)
				{
					Log.i(TAG, "Around Me");
				}
				Intent intent = new Intent(mMainActivity.getApplication(),
						AroundMe.class);
				startActivity(intent);
			}
		});
		mCommunity = (ImageView) findViewById(R.id.community);
		mCommunity.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				if (AppConfig.LOGGING_ENABLED)
				{
					Log.i(TAG, "Community");
				}
				Intent intent = new Intent(mMainActivity.getApplication(),
						CommunityOptions.class);
				startActivity(intent);
			}
		});

		mAlreadyAgreedToEula = Eula.show(this);
		new RateAndReviewTask().execute("");
		new UpdateCheckTask().execute("");
		// so that it only runs once in an hour
		// if ((runDate +
		// AppConfig.GET_RECOMMENDATIONS_NOTIFICATION_CHECK_INTERVAL) <=
		// lastRunDate)
		// {
		String _userId = (mUser.isLoggedIn()) ? mUser.getUserId() : "";
		new AsyncGetRecommendationsTask().execute(_userId);
		// }
		// default to 1
		long usageCount = mPreferences.getLong(
				AppConfig.PREFERENCE_APPLICATION_USAGE_COUNT, 1L);
		mPreferences
				.edit()
				.putLong(AppConfig.PREFERENCE_APPLICATION_USAGE_COUNT,
						(usageCount + 1)).commit();
		Log.i(TAG, "Application Usage Count: " + usageCount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (AppConfig.LOGGING_ENABLED)
		{
			Log.i(TAG, "onKeyDown");
		}
		if (keyCode == KeyEvent.KEYCODE_E)
		{
			if (AppConfig.EMULATE_LOGIN)
			{
				com.cm.beer.util.Util.emulateLogin(mMainActivity);
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_R)
		{
			if (AppConfig.EMULATE_LOGIN)
			{
				com.cm.beer.util.Util.emulateLogout(mMainActivity);
			}
			return true;
		} else
		{
			return (super.onKeyDown(keyCode, event));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.app.Activity#onConfigurationChanged(android.content.res.Configuration
	 * )
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		// DO NOTHING
		super.onConfigurationChanged(newConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		if (AppConfig.LOGGING_ENABLED)
		{
			Log.i(TAG, "onDestroy");
		}
		// Stop the tracker when it is no longer needed.
		mTracker.stop();
		if (AppConfig.LOGGING_ENABLED)
		{
			Log.i(TAG, "onCreate:Google Tracker Stopped!");
		}
		/** Stop the Notification Service **/
		Util.evaluateNotificationService(mMainActivity);

		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if (AppConfig.LOGGING_ENABLED)
		{
			Log.i(TAG, "onCreateOptionsMenu");
		}
		super.onCreateOptionsMenu(menu);
		int position = 0;
		menu.add(MENU_GROUP, PREFERENCES_ID, position++, R.string.preferences);
		menu.add(MENU_GROUP, ABOUT_ID, position++, R.string.menu_about);
		if (AppConfig.DEFAULT_APPSTORE.equals(AppConfig.GOOGLE_APPSTORE))
		{
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
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		if (AppConfig.LOGGING_ENABLED)
		{
			Log.i(TAG, "onMenuItemSelected");
		}
		switch (item.getItemId())
		{
		case ABOUT_ID:
			mTracker.trackEvent("Main", "AboutBeer", "Clicked", 0);
			mTracker.dispatch();
			aboutBeer();
			return true;
		case SEND_ERROR_REPORT_ID:
			mTracker.trackEvent("Main", "SendErrorReport", "Clicked", 0);
			mTracker.dispatch();
			sendErrorReport();
			return true;
		case PREFERENCES_ID:
			mTracker.trackEvent("Main", "Preferences", "Clicked", 0);
			mTracker.dispatch();
			preferences();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * 
	 */
	private void aboutBeer()
	{
		if (AppConfig.LOGGING_ENABLED)
		{
			Log.i(TAG, "aboutBeer");
		}
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(AppConfig.APPLICATION_DETAILS_PAGE_WEBSITE_URI));

		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(intent);

	}

	/**
	 * 
	 */
	private void sendErrorReport()
	{
		if (AppConfig.LOGGING_ENABLED)
		{
			Log.i(TAG, "Send Error Report");
		}

		Intent intent = new Intent(mMainActivity.getApplication(),
				CollectAndSendLog.class);
		startActivity(intent);

	}

	/**
	 * 
	 */
	private void preferences()
	{
		if (AppConfig.LOGGING_ENABLED)
		{
			Log.i(TAG, "aboutBeer");
		}
		Intent intent = new Intent(mMainActivity.getApplication(),
				SetPreferences.class);

		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(intent);

	}

	/**
	 * 
	 * @param activity
	 */
	private void showInstructions(Activity activity)
	{
		new AlertDialog.Builder(activity).setTitle(R.string.instructions_title)
				.setIcon(android.R.drawable.ic_dialog_info).setCancelable(true)
				.setPositiveButton(R.string.done_label, null)
				.setMessage(readAsset(activity, AppConfig.ASSET_INSTRUCTIONS))
				.show();

	}

	private CharSequence readAsset(Activity activity, String asset)
	{
		BufferedReader in = null;
		try
		{
			in = new BufferedReader(new InputStreamReader(activity.getAssets()
					.open(asset)));
			String line;
			StringBuilder buffer = new StringBuilder();
			while ((line = in.readLine()) != null)
				buffer.append(line).append('\n');
			return buffer;
		} catch (IOException e)
		{
			return "";
		} finally
		{
			closeStream(in);
		}
	}

	/**
	 * Closes the specified stream.
	 * 
	 * @param stream
	 *            The stream to close.
	 */
	private void closeStream(Closeable stream)
	{
		if (stream != null)
		{
			try
			{
				stream.close();
			} catch (IOException e)
			{
				Log.e(TAG,
						(e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "", e);
			}
		}
	}

	/************************************************************************/
	private class DidYouKnowTask extends AsyncTask<String, Void, Object>
	{
		private String _TAG = DidYouKnowTask.class.getName();

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(String... args)
		{
			if (AppConfig.LOGGING_ENABLED)
			{
				Log.i(_TAG, "doInBackground starting");
			}
			Runnable notification = new Runnable()
			{
				public void run()
				{
					if (mMainActivity != null)
					{
						final SharedPreferences preferences = mMainActivity
								.getSharedPreferences(
										getString(R.string.app_name),
										Activity.MODE_PRIVATE);
						if (!preferences.getBoolean(
								AppConfig.PREFERENCE_DO_NOT_SHOW_DID_YOU_KNOW,
								false))
						{
							try
							{
								new AlertDialog.Builder(mMainActivity)
										.setTitle(R.string.did_you_know_title)
										.setIcon(
												android.R.drawable.ic_dialog_info)
										.setCancelable(true)
										.setPositiveButton(R.string.ok_label,
												null)
										.setNegativeButton(
												R.string.do_not_show_did_you_know,
												new OnClickListener()
												{

													@Override
													public void onClick(
															DialogInterface arg0,
															int arg1)
													{
														preferences
																.edit()
																.putBoolean(
																		AppConfig.PREFERENCE_DO_NOT_SHOW_DID_YOU_KNOW,
																		true)
																.commit();
													}
												})
										.setMessage(
												AppConfig.DID_YOU_KNOW_MESSAGES[mRandom])
										.show();
							} catch (Throwable e)
							{
								Log.e(_TAG, (e.getMessage() != null) ? e
										.getMessage().replace(" ", "_") : "", e);
							}
						}
					}
				}
			};
			mHandler.postDelayed(notification, AppConfig.DID_YOU_KNOW_DELAY_MS);

			return null;
		}

		@Override
		protected void onPostExecute(Object result)
		{
			if (AppConfig.LOGGING_ENABLED)
			{
				Log.i(_TAG, "onPostExecute starting");
			}
			if (AppConfig.LOGGING_ENABLED)
			{
				Log.i(_TAG, "onPostExecute finished");
			}
		}

	}

	/*******************************************************************************/
	private class UpdateCheckTask extends AsyncTask<String, Void, Object>
	{
		private String _TAG = UpdateCheckTask.class.getName();

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(String... args)
		{
			if (AppConfig.LOGGING_ENABLED)
			{
				Log.i(_TAG, "doInBackground starting");
			}
			try
			{
				String currentVersion = getVersionCode(mMainActivity);
				String url = Util.getUpdateCheckUrl(currentVersion);
				Log.i(_TAG, "doInBackground:" + url);
				String response[] = Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("{")))
				{
					JSONObject json = new JSONObject(response[0]);
					boolean _updateAvailable = json
							.getBoolean("updateAvailable");

					if (_updateAvailable)
					{
						Runnable notification = new Runnable()
						{
							public void run()
							{
								if (mMainActivity != null)
								{
									new AlertDialog.Builder(mMainActivity)
											.setTitle(
													R.string.new_version_available_title)
											.setIcon(
													android.R.drawable.ic_dialog_info)
											.setCancelable(true)
											.setPositiveButton(
													R.string.yes_label,
													new OnClickListener()
													{

														@Override
														public void onClick(
																DialogInterface arg0,
																int arg1)
														{
															try
															{
																Intent intent = null;
																if (AppConfig.DEFAULT_APPSTORE
																		.equals(AppConfig.GOOGLE_APPSTORE))
																{
																	intent = new Intent(
																			Intent.ACTION_VIEW,
																			Uri.parse(AppConfig.GOOGLE_APPSTORE_APPLICATION_DETAILS_PAGE_URI));
																} else if (AppConfig.DEFAULT_APPSTORE
																		.equals(AppConfig.AMAZON_APPSTORE))
																{
																	intent = new Intent(
																			Intent.ACTION_VIEW,
																			Uri.parse(AppConfig.AMAZON_APPSTORE_APPLICATION_DETAILS_PAGE_URI));
																}
																intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
																startActivity(intent);

															} catch (Throwable e)
															{
																Log.e(_TAG,
																		(e.getMessage() != null) ? e
																				.getMessage()
																				.replace(
																						" ",
																						"_")
																				: "",
																		e);
															}

														}
													})
											.setNegativeButton(
													R.string.no_label, null)

											.setMessage(
													R.string.new_version_available_message)
											.show();
								}
							}
						};
						mHandler.post(notification);
					}
				}
			} catch (Throwable e)
			{
				Log.e(_TAG,
						(e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "", e);
			}
			return null;
		}

		private String getVersionCode(Context context)
		{
			String version = "?";
			try
			{
				PackageInfo packagInfo = context.getPackageManager()
						.getPackageInfo(context.getPackageName(), 0);
				version = String.valueOf(packagInfo.versionCode);
			} catch (Throwable e)
			{
				Log.e(_TAG,
						(e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "", e);
			}

			return version;
		}

		@Override
		protected void onPostExecute(Object result)
		{
			if (AppConfig.LOGGING_ENABLED)
			{
				Log.i(_TAG, "onPostExecute starting");
			}
			if (AppConfig.LOGGING_ENABLED)
			{
				Log.i(_TAG, "onPostExecute finished");
			}
		}

	}

	/************************************************************************/
	private class RateAndReviewTask extends AsyncTask<String, Void, Object>
	{

		private String _TAG = RateAndReviewTask.class.getName();

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(String... args)
		{
			if (AppConfig.LOGGING_ENABLED)
			{
				Log.i(_TAG, "doInBackground starting");
			}

			Runnable notification = new Runnable()
			{
				public void run()
				{
					if (mMainActivity != null)
					{
						final SharedPreferences preferences = mMainActivity
								.getSharedPreferences(
										getString(R.string.app_name),
										Activity.MODE_PRIVATE);
						try
						{
							if (preferences
									.getLong(
											AppConfig.PREFERENCE_APPLICATION_USAGE_COUNT,
											0L) > AppConfig.APPLICATION_USAGE_COUNT_THRESHOLD_TO_DISPLAY_RATE_AND_REVIEW)
							{
								if ((preferences
										.getBoolean(
												AppConfig.PREFERENCE_DONE_RATE_AND_REVIEW,
												false)))
								{
									Log.i(_TAG,
											"RateAndReviewTask: Application has already been rated by the user!");
									return;
								}
								if ((preferences
										.getBoolean(
												AppConfig.PREFERENCE_DO_NOT_SHOW_RATE_AND_REVIEW,
												false)))
								{
									Log.i(_TAG,
											"RateAndReviewTask: The user has declined to rate the application!");
									return;
								}
								if (preferences
										.getBoolean(
												AppConfig.PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW,
												false))
								{
									long time = preferences
											.getLong(
													AppConfig.PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW_TIME,
													0L);
									// if it is not time yet then return
									if (System.currentTimeMillis() < time)
									{
										Log.i(_TAG,
												"RateAndReviewTask: It is not time yet to ask the user to rate the application!");
										return;
									}
								}

								final Dialog dialog = new Dialog(mMainActivity,
										android.R.style.Theme_Dialog);
								dialog.setContentView(R.layout.rate_and_review);
								dialog.setTitle(R.string.rate_and_review_reminder_title);
								Button rate = (Button) dialog
										.findViewById(R.id.rate_and_review_reminder_rate_button);
								rate.setOnClickListener(new View.OnClickListener()
								{

									@Override
									public void onClick(View v)
									{
										preferences
												.edit()
												.putBoolean(
														AppConfig.PREFERENCE_DONE_RATE_AND_REVIEW,
														true).commit();
										Log.i(_TAG,
												"RateAndReviewTask: The user has accepted to rate the application!");
										mTracker.trackEvent(
												"Main",
												AppConfig.PREFERENCE_DONE_RATE_AND_REVIEW,
												"Clicked", 0);
										mTracker.dispatch();
										// to the market
										Intent intent = null;
										if (AppConfig.DEFAULT_APPSTORE
												.equals(AppConfig.GOOGLE_APPSTORE))
										{
											intent = new Intent(
													Intent.ACTION_VIEW,
													Uri.parse(AppConfig.GOOGLE_APPSTORE_APPLICATION_DETAILS_PAGE_URI));
										} else if (AppConfig.DEFAULT_APPSTORE
												.equals(AppConfig.AMAZON_APPSTORE))
										{
											intent = new Intent(
													Intent.ACTION_VIEW,
													Uri.parse(AppConfig.AMAZON_APPSTORE_APPLICATION_DETAILS_PAGE_URI));
										}

										intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
										startActivity(intent);
										dialog.cancel();
									}
								});
								Button remind = (Button) dialog
										.findViewById(R.id.rate_and_review_reminder_remind_me_later_button);
								remind.setOnClickListener(new View.OnClickListener()
								{

									@Override
									public void onClick(View v)
									{
										preferences
												.edit()
												.putBoolean(
														AppConfig.PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW,
														true).commit();
										Log.i(_TAG,
												"RateAndReviewTask: The user has asked to be reminded later, to rate the application!");
										long time = System.currentTimeMillis()
												+ AppConfig.PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW_DELAY_INTERVAL;
										preferences
												.edit()
												.putLong(
														AppConfig.PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW_TIME,
														time).commit();
										mTracker.trackEvent(
												"Main",
												AppConfig.PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW,
												"Clicked", 0);
										mTracker.dispatch();
										dialog.cancel();
									}
								});
								Button no = (Button) dialog
										.findViewById(R.id.rate_and_review_reminder_no_button);
								no.setOnClickListener(new View.OnClickListener()
								{

									@Override
									public void onClick(View v)
									{
										preferences
												.edit()
												.putBoolean(
														AppConfig.PREFERENCE_DO_NOT_SHOW_RATE_AND_REVIEW,
														true).commit();
										Log.i(_TAG,
												"RateAndReviewTask: The user has declined to rate the application!");
										mTracker.trackEvent(
												"Main",
												AppConfig.PREFERENCE_DO_NOT_SHOW_RATE_AND_REVIEW,
												"Clicked", 0);
										mTracker.dispatch();
										dialog.cancel();
									}
								});

								dialog.show();
							} else
							{
								Log.i(_TAG,
										"RateAndReviewTask: Application has not reach the display threshold!");

							}

						} catch (Throwable e)
						{
							Log.e(_TAG, (e.getMessage() != null) ? e
									.getMessage().replace(" ", "_") : "", e);
						}
					}
				}
			};
			mHandler.post(notification);

			return null;
		}

		@Override
		protected void onPostExecute(Object result)
		{
			if (AppConfig.LOGGING_ENABLED)
			{
				Log.i(_TAG, "onPostExecute starting");
			}
			if (AppConfig.LOGGING_ENABLED)
			{
				Log.i(_TAG, "onPostExecute finished");
			}
		}

	}

	/**************************************************************************************/
	private class AsyncGetRecommendationsTask extends
			AsyncTask<String, Void, Void>
	{
		private String _TAG = AsyncGetRecommendationsTask.class.getName();
		private JSONArray _mRecommendationsJsonArray;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(String... args)
		{
			Log.i(_TAG, "doInBackground starting");
			String userId = (String) args[0];
			try
			{
				String url = com.cm.beer.util.Util.getRecommendationsUrl(
						userId);

				Log.i(_TAG, "doInBackground:" + url);
				String response[] = com.cm.beer.util.Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("[")))
				{
					_mRecommendationsJsonArray = new JSONArray(response[0]);
				}

			} catch (Throwable e)
			{
				Log.e(_TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
				mTracker.trackEvent(
						"ShareWithCommunity",
						"AsyncGetRecommendationsTaskError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}
			if (AppConfig.LOGGING_ENABLED)
			{
				Log.i(_TAG, "doInBackground finished");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			Log.i(_TAG, "onPostExecute starting");
			if (_mRecommendationsJsonArray != null)
			{
				int notificationId = R.string.notification_recommended_beer_reviews;
				CharSequence message = getString(R.string.notification_recommended_beer_reviews);
				Notification notification = new Notification(R.drawable.icon,
						message, System.currentTimeMillis());
				notification.number = _mRecommendationsJsonArray.length();
				notification.defaults |= Notification.DEFAULT_SOUND;
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				// The PendingIntent to launch our activity if the user selects
				// this
				// notification
				Intent intent = new Intent(mMainActivity, CommunityBeers.class);
				intent.putExtra("OPTION",
						AppConfig.COMMUNITY_RECOMMENDED_BEER_REVIEWS);
				intent.putExtra("BEERIDS",
						_mRecommendationsJsonArray.toString());
				intent.putExtra("NOTIFICATIONID", notificationId);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				PendingIntent contentIntent = PendingIntent.getActivity(
						mMainActivity, notificationId, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				// Set the info for the views that show in the notification
				// panel.
				notification.setLatestEventInfo(mMainActivity,
						getText(R.string.app_name), message, contentIntent);

				// Send the notification.
				// We use a layout id because it is a unique number. We use it
				// later
				// to
				// cancel.
				mNM.notify(notificationId, notification);
			} else
			{
				Log.i(_TAG, "No recommendations found!");
			}
			Log.i(_TAG, "onPostExecute finished");
		}

	}

}