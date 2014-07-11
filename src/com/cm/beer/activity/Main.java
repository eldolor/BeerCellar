package com.cm.beer.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.cm.beer.activity.slidingmenu.CommunityBeersFragment;
import com.cm.beer.activity.slidingmenu.HomeFragment;
import com.cm.beer.activity.slidingmenu.LoginInterceptFragment;
import com.cm.beer.activity.slidingmenu.NavDrawerItem;
import com.cm.beer.activity.slidingmenu.NavDrawerListAdapter;
import com.cm.beer.config.AppConfig;
import com.cm.beer.util.Logger;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Main extends android.support.v7.app.ActionBarActivity implements
		Eula.OnEulaAgreedTo {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] mNavMenuTitles;

	private ArrayList<NavDrawerItem> mNavDrawerItems;
	private NavDrawerListAdapter mAdapter;
	private GoogleAnalyticsTracker mTracker;
	private boolean mAlreadyAgreedToEula = false;
	private static final Handler mHandler = new Handler();
	private NotificationManager mNM;
	private User mUser;
	private SharedPreferences mPreferences;
	protected static final int LOGIN_INTERCEPT_FOR_MY_BEERS_REQUEST_CODE = 0;
	protected static final int LOGIN_INTERCEPT_FOR_FOLLOWING_REQUEST_CODE = 1;
	protected static final int LOGIN_INTERCEPT_FOR_FOLLOWERS_REQUEST_CODE = 2;
	protected static final int LOGIN_INTERCEPT_FOR_FAVORITE_BEERS_REQUEST_CODE = 3;
	protected static final int LOGIN_INTERCEPT_FOR_MY_PROFILE_REQUEST_CODE = 4;

	public void onEulaAgreedTo() {
		mTracker.trackEvent("Main", "AgreedToEULA", "Y", 0);
		mTracker.dispatch();
	}

	public void onEulaNotAgreedTo() {
		mTracker.trackEvent("Main", "AgreedToEULA", "N", 0);
		mTracker.dispatch();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID,
				this);
		mNM = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mUser = new User(this);
		mPreferences = getSharedPreferences(getString(R.string.app_name),
				Activity.MODE_PRIVATE);

		/** Start the Notification Service **/
		Util.evaluateNotificationService(Main.this);

		new RateAndReviewTask().execute("");
		new UpdateCheckTask().execute("");
		// String _userId = (mUser.isLoggedIn()) ? mUser.getUserId() : "";
		// new AsyncGetRecommendationsTask().execute(_userId);
		// }
		// default to 1
		long usageCount = mPreferences.getLong(
				AppConfig.PREFERENCE_APPLICATION_USAGE_COUNT, 1L);
		mPreferences
				.edit()
				.putLong(AppConfig.PREFERENCE_APPLICATION_USAGE_COUNT,
						(usageCount + 1)).commit();
		// Normal onCreate
		setupActivity();
		mAlreadyAgreedToEula = Eula.show(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_INTERCEPT_FOR_MY_BEERS_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				if (Logger.isLogEnabled())
					Logger.log("onActivityResult:" + mUser.getUserId());
				getMyBeers(mUser.getUserId());
			}
		} else if (requestCode == LOGIN_INTERCEPT_FOR_FAVORITE_BEERS_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				if (Logger.isLogEnabled())
					Logger.log("onActivityResult:" + mUser.getUserId());
				getFavoriteBeers(mUser.getUserId());
			}
		}
	}

	private void getMyBeers(String userId) {
		if (Logger.isLogEnabled())
			Logger.log("getMyBeers");
		displayView(4, true);
	}

	private void getFavoriteBeers(String userId) {
		if (Logger.isLogEnabled())
			Logger.log("getFavoriteBeers");
		displayView(5, true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		setDisplayView();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		// case com.mavin.lockscreen.R.id.action_send_logs:
		// EasyTracker.getInstance(this).send(
		// MapBuilder.createEvent(
		// "home_activity",
		// "send_logs_menu_item_selected",
		// RegisteredDevice.getInstance(
		// getApplicationContext()).getUserId(), null)
		// .build());
		// MavinUtils.collectAndSendLogs(HomeActivity.this);
		// return true;
		// case com.mavin.lockscreen.R.id.action_send_ad_cache:
		// EasyTracker.getInstance(this).send(
		// MapBuilder.createEvent(
		// "home_activity",
		// "send_ad_cache_menu_item_selected",
		// RegisteredDevice.getInstance(
		// getApplicationContext()).getUserId(), null)
		// .build());
		// MavinUtils.sendAdCache(HomeActivity.this);
		// return true;
		// case com.mavin.lockscreen.R.id.action_refresh:
		// EasyTracker.getInstance(this).send(
		// MapBuilder.createEvent(
		// "home_activity",
		// "refresh_ads_menu_item_selected",
		// RegisteredDevice.getInstance(
		// getApplicationContext()).getUserId(), null)
		// .build());
		// // PSUtils.triggerCampaignDownload(HomeActivity.this);
		// Toast.makeText(HomeActivity.this, R.string.contacting_server,
		// Toast.LENGTH_LONG).show();
		// return true;
		case android.R.id.home:
			// do your stuff here, eg: finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	/**
	 * 
	 * @param position
	 */
	private void displayView(int position) {
		this.displayView(position, true);
	}

	/**
	 * Displaying fragment view for selected nav drawer list item
	 * */
	public void displayView(int position, boolean updateTitle) {
		// When changing pages, reset the action bar actions since they
		// are dependent
		// on which page is currently active.
		supportInvalidateOptionsMenu();

		// update the main content by replacing fragments
		android.support.v4.app.Fragment lFragment = null;
		boolean lAddToBackStack = true;
		boolean lAddFragment = true;

		switch (position) {
		case 0:
			lFragment = new HomeFragment();
			break;
		case 1:
			lFragment = new CommunityBeersFragment();
			{
				Bundle bundle = new Bundle();
				bundle.putString("OPTION",
						AppConfig.COMMUNITY_BEERS_FROM_AROUND_THE_WORLD);
				lFragment.setArguments(bundle);
			}
			mTracker.trackEvent("CommunityOptions",
					"BeersFromAroundTheWorldLoadMore", "Clicked", 0);
			mTracker.dispatch();
			break;
		case 2:
			lFragment = new CommunityBeersFragment();
			{
				Bundle bundle = new Bundle();
				bundle.putString("OPTION", AppConfig.COMMUNITY_TOP_RATED_BEERS);
				lFragment.setArguments(bundle);
			}
			mTracker.trackEvent("CommunityOptions", "TopRatedBeersLoadMore",
					"Clicked", 0);
			mTracker.dispatch();
			break;
		case 3:
			lFragment = new CommunityBeersFragment();
			{
				Bundle bundle = new Bundle();
				bundle.putString("OPTION", AppConfig.COMMUNITY_WORST_BEERS);
				lFragment.setArguments(bundle);
			}
			mTracker.trackEvent("CommunityOptions", "WorstBeersLoadMore",
					"Clicked", 0);
			mTracker.dispatch();
			break;
		case 4:
			mTracker.trackEvent("CommunityOptions", "MyBeerReviews", "Clicked",
					0);
			mTracker.dispatch();
			// If user id does not exist
			Bundle bundle1 = new Bundle();
			if (mUser.isLoggedIn()) {
				lFragment = new CommunityBeersFragment();
				{
					bundle1.putString("OPTION",
							AppConfig.COMMUNITY_MY_BEER_REVIEWS);
					bundle1.putString("USERID", mUser.getUserId());
					lFragment.setArguments(bundle1);
				}
			} else {
				lAddToBackStack = false;
				lFragment = new LoginInterceptFragment();
				bundle1.putStringArray("FACEBOOK_PERMISSIONS",
						AppConfig.FACEBOOK_PERMISSIONS);
				lFragment.setArguments(bundle1);
				lAddFragment = true;
				lAddToBackStack = false;
			}
			break;
		case 5:
			mTracker.trackEvent("CommunityOptions", "FavoriteBeerReviews",
					"Clicked", 0);
			mTracker.dispatch();
			// If user id does not exist
			Bundle bundle2 = new Bundle();
			if (mUser.isLoggedIn()) {
				lFragment = new CommunityBeersFragment();
				{
					bundle2.putString("OPTION",
							AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS);
					bundle2.putString("USERID", mUser.getUserId());
					lFragment.setArguments(bundle2);
				}
			} else {
				lAddToBackStack = false;
				lFragment = new LoginInterceptFragment();
				bundle2.putStringArray("FACEBOOK_PERMISSIONS",
						AppConfig.FACEBOOK_PERMISSIONS);
				lFragment.setArguments(bundle2);
				lAddFragment = true;
				lAddToBackStack = false;
			}
			break;
		default:
			break;
		}

		// pass the fragment title
		if (lFragment != null) {
			if (lFragment.getArguments() != null) {
				lFragment.getArguments().putString("FRAGMENT_TITLE",
						mNavMenuTitles[position]);
			} else {
				Bundle bundle = new Bundle();
				bundle.putString("FRAGMENT_TITLE", mNavMenuTitles[position]);
				lFragment.setArguments(bundle);
			}

			android.support.v4.app.FragmentManager lFragmentManager = getSupportFragmentManager();
			android.support.v4.app.FragmentTransaction lFragmentTransaction = lFragmentManager
					.beginTransaction();

			lFragmentTransaction.replace(R.id.frame_container, lFragment);
			lFragmentTransaction
					.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// Add to backstack
			if (lAddToBackStack)
				lFragmentTransaction.addToBackStack(lFragment.getClass()
						.getName());
			lFragmentTransaction.commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			if (updateTitle) {
				setTitle(mNavMenuTitles[position]);
			}
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (mDrawerToggle != null)
			mDrawerToggle.syncState();
	}

	/**
	 * 
	 * @throws NotFoundException
	 * @throws ExternalStorageNotReadyException
	 * @throws DeviceNotRegisteredException
	 */
	private void setupActivity() throws NotFoundException {
		setContentView(R.layout.main);
		setupNavDrawer();
		getSupportActionBar().setBackgroundDrawable(
				new ColorDrawable(0xFF998675));
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private void setDisplayView() {
		displayView(0, false);
	}

	private void setupNavDrawer() {
		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		mNavMenuTitles = getResources()
				.getStringArray(R.array.nav_drawer_items);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		mNavDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Special message
		mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[0]));
		// Browse
		mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[1]));
		// Favorites
		mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[2]));
		// User
		mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[3]));
		// Settings
		mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[4]));
		// About
		mNavDrawerItems.add(new NavDrawerItem(mNavMenuTitles[5]));

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		mAdapter = new NavDrawerListAdapter(getApplicationContext(),
				mNavDrawerItems);
		mDrawerList.setAdapter(mAdapter);

		// enabling action bar app icon and behaving it as toggle button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.icon, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// Stop the tracker when it is no longer needed.
		mTracker.stop();
		/** Stop the Notification Service **/
		Util.evaluateNotificationService(Main.this);

		super.onDestroy();
	}

	/******************************************/
	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	/*******************************************************************************/
	private class UpdateCheckTask extends AsyncTask<String, Void, Object> {
		private String _TAG = UpdateCheckTask.class.getName();

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(String... args) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())
					Logger.log("doInBackground starting");
			}
			try {
				String currentVersion = getVersionCode(Main.this);
				String url = Util.getUpdateCheckUrl(currentVersion);
				if (Logger.isLogEnabled())
					Logger.log("doInBackground:" + url);
				String response[] = Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("{"))) {
					JSONObject json = new JSONObject(response[0]);
					boolean _updateAvailable = json
							.getBoolean("updateAvailable");

					if (_updateAvailable) {
						Runnable notification = new Runnable() {
							public void run() {
								if (Main.this != null) {
									new AlertDialog.Builder(Main.this)
											.setTitle(
													R.string.new_version_available_title)
											.setIcon(
													android.R.drawable.ic_dialog_info)
											.setCancelable(true)
											.setPositiveButton(
													R.string.yes_label,
													new OnClickListener() {

														@Override
														public void onClick(
																DialogInterface arg0,
																int arg1) {
															try {
																Intent intent = null;
																if (AppConfig.DEFAULT_APPSTORE
																		.equals(AppConfig.GOOGLE_APPSTORE)) {
																	intent = new Intent(
																			Intent.ACTION_VIEW,
																			Uri.parse(AppConfig.GOOGLE_APPSTORE_APPLICATION_DETAILS_PAGE_URI));
																} else if (AppConfig.DEFAULT_APPSTORE
																		.equals(AppConfig.AMAZON_APPSTORE)) {
																	intent = new Intent(
																			Intent.ACTION_VIEW,
																			Uri.parse(AppConfig.AMAZON_APPSTORE_APPLICATION_DETAILS_PAGE_URI));
																}
																intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
																startActivity(intent);

															} catch (Throwable e) {
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
			} catch (Throwable e) {
				Log.e(_TAG,
						(e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "", e);
			}
			return null;
		}

		private String getVersionCode(Context context) {
			String version = "?";
			try {
				PackageInfo packagInfo = context.getPackageManager()
						.getPackageInfo(context.getPackageName(), 0);
				version = String.valueOf(packagInfo.versionCode);
			} catch (Throwable e) {
				Log.e(_TAG,
						(e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "", e);
			}

			return version;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())
					Logger.log("onPostExecute starting");
			}
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())
					Logger.log("onPostExecute finished");
			}
		}

	}

	/************************************************************************/
	private class RateAndReviewTask extends AsyncTask<String, Void, Object> {

		private String _TAG = RateAndReviewTask.class.getName();

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(String... args) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())
					Logger.log("doInBackground starting");
			}

			Runnable notification = new Runnable() {
				public void run() {
					if (Main.this != null) {
						final SharedPreferences preferences = Main.this
								.getSharedPreferences(
										getString(R.string.app_name),
										Activity.MODE_PRIVATE);
						try {
							if (preferences
									.getLong(
											AppConfig.PREFERENCE_APPLICATION_USAGE_COUNT,
											0L) > AppConfig.APPLICATION_USAGE_COUNT_THRESHOLD_TO_DISPLAY_RATE_AND_REVIEW) {
								if ((preferences
										.getBoolean(
												AppConfig.PREFERENCE_DONE_RATE_AND_REVIEW,
												false))) {
									Log.i(_TAG,
											"RateAndReviewTask: Application has already been rated by the user!");
									return;
								}
								if ((preferences
										.getBoolean(
												AppConfig.PREFERENCE_DO_NOT_SHOW_RATE_AND_REVIEW,
												false))) {
									Log.i(_TAG,
											"RateAndReviewTask: The user has declined to rate the application!");
									return;
								}
								if (preferences
										.getBoolean(
												AppConfig.PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW,
												false)) {
									long time = preferences
											.getLong(
													AppConfig.PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW_TIME,
													0L);
									// if it is not time yet then return
									if (System.currentTimeMillis() < time) {
										Log.i(_TAG,
												"RateAndReviewTask: It is not time yet to ask the user to rate the application!");
										return;
									}
								}

								final Dialog dialog = new Dialog(Main.this,
										android.R.style.Theme_Dialog);
								dialog.setContentView(R.layout.rate_and_review);
								dialog.setTitle(R.string.rate_and_review_reminder_title);
								Button rate = (Button) dialog
										.findViewById(R.id.rate_and_review_reminder_rate_button);
								rate.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {
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
												.equals(AppConfig.GOOGLE_APPSTORE)) {
											intent = new Intent(
													Intent.ACTION_VIEW,
													Uri.parse(AppConfig.GOOGLE_APPSTORE_APPLICATION_DETAILS_PAGE_URI));
										} else if (AppConfig.DEFAULT_APPSTORE
												.equals(AppConfig.AMAZON_APPSTORE)) {
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
								remind.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {
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
								no.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {
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
							} else {
								Log.i(_TAG,
										"RateAndReviewTask: Application has not reach the display threshold!");

							}

						} catch (Throwable e) {
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
		protected void onPostExecute(Object result) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())
					Logger.log("onPostExecute starting");
			}
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())
					Logger.log("onPostExecute finished");
			}
		}

	}

	/**************************************************************************************/
	private class AsyncGetRecommendationsTask extends
			AsyncTask<String, Void, Void> {
		private String _TAG = AsyncGetRecommendationsTask.class.getName();
		private JSONArray _mRecommendationsJsonArray;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(String... args) {
			if (Logger.isLogEnabled())
				Logger.log("doInBackground starting");
			String userId = (String) args[0];
			try {
				String url = com.cm.beer.util.Util
						.getRecommendationsUrl(userId);

				if (Logger.isLogEnabled())
					Logger.log("doInBackground:" + url);
				String response[] = com.cm.beer.util.Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("["))) {
					_mRecommendationsJsonArray = new JSONArray(response[0]);
				}

			} catch (Throwable e) {
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
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())
					Logger.log("doInBackground finished");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (Logger.isLogEnabled())
				Logger.log("onPostExecute starting");
			if (_mRecommendationsJsonArray != null) {
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
				Intent intent = new Intent(Main.this,
						CommunityBeersFragment.class);
				intent.putExtra("OPTION",
						AppConfig.COMMUNITY_RECOMMENDED_BEER_REVIEWS);
				intent.putExtra("BEERIDS",
						_mRecommendationsJsonArray.toString());
				intent.putExtra("NOTIFICATIONID", notificationId);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				PendingIntent contentIntent = PendingIntent.getActivity(
						Main.this, notificationId, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);

				// Set the info for the views that show in the notification
				// panel.
				notification.setLatestEventInfo(Main.this,
						getText(R.string.app_name), message, contentIntent);

				// Send the notification.
				// We use a layout id because it is a unique number. We use it
				// later
				// to
				// cancel.
				mNM.notify(notificationId, notification);
			} else {
				if (Logger.isLogEnabled())
					Logger.log("No recommendations found!");
			}
			if (Logger.isLogEnabled())
				Logger.log("onPostExecute finished");
		}

	}

	/**************************************************************************************/

}
