package com.cm.beer.activity;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.transfer.CommunityBeer;
import com.cm.beer.util.ContentManager;
import com.cm.beer.util.DrawableManager;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Main extends Activity implements Eula.OnEulaAgreedTo {

	String TAG;
	boolean mAlreadyAgreedToEula = false;
	String mSelectedOption = null;
	ProgressDialog mSplashDialog;

	// ImageView mBeerBBList;
	// ImageView mHelp;
	// ImageView mAroundMe;
	// ImageView mCommunity;

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

	// NOTE: mCs cannot be null
	private String mCs = "";
	// private List<CommunityBeer> mTopRatedBeers = new
	// ArrayList<CommunityBeer>();
	// private List<CommunityBeer> mMyBeers = new ArrayList<CommunityBeer>();
	// private List<CommunityBeer> mBeers = new ArrayList<CommunityBeer>();
	// private List<CommunityBeer> mTopRatedBeers = new
	// ArrayList<CommunityBeer>();
	// private List<CommunityBeer> mTopRatedBeers = new
	// ArrayList<CommunityBeer>();
	// private List<CommunityBeer> mTopRatedBeers = new
	// ArrayList<CommunityBeer>();
	DrawableManager mDrawableManager;
	ContentManager mContentManager;
	LinearLayout mTopRatedBeersGallery;
	LinearLayout mMyBeersGallery;
	LinearLayout mWorstRatedBeersGallery;
	LinearLayout mMostHelpfulBeerReviewsGallery;
	LinearLayout mFavoriteBeerReviewsGallery;
	LinearLayout mAroundTheWorldBeersGallery;

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
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		if (AppConfig.LOGGING_ENABLED) {
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
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}

		mDrawableManager = DrawableManager.getInstance();
		mContentManager = ContentManager.getInstance();

		// Start a new thread that will download all the data
		setContentView(R.layout.main);

		{
			mTopRatedBeersGallery = (LinearLayout) findViewById(R.id.top_rated_beers_gallery);
			// Start a new thread that will download all the data
			Bundle lExtras = new Bundle();
			lExtras.putString("OPTION", AppConfig.COMMUNITY_TOP_RATED_BEERS);
			Boolean _refreshList = new Boolean(false);
			new AsyncGetCommunityBeers().execute(lExtras, _refreshList);
		}
		{
			mMyBeersGallery = (LinearLayout) findViewById(R.id.my_beers_gallery);
			// Start a new thread that will download all the data
			Bundle lExtras = new Bundle();
			lExtras.putString("OPTION", AppConfig.COMMUNITY_MY_BEER_REVIEWS);
			Boolean _refreshList = new Boolean(false);
			new AsyncGetCommunityBeers().execute(lExtras, _refreshList);

		}
		{
			mWorstRatedBeersGallery = (LinearLayout) findViewById(R.id.worst_rated_beers_gallery);
			// Start a new thread that will download all the data
			Bundle lExtras = new Bundle();
			lExtras.putString("OPTION", AppConfig.COMMUNITY_WORST_BEERS);
			Boolean _refreshList = new Boolean(false);
			new AsyncGetCommunityBeers().execute(lExtras, _refreshList);

		}
		{
			mMostHelpfulBeerReviewsGallery = (LinearLayout) findViewById(R.id.most_helpful_beer_reviews_gallery);
			// Start a new thread that will download all the data
			Bundle lExtras = new Bundle();
			lExtras.putString("OPTION",
					AppConfig.COMMUNITY_MOST_HELPFUL_BEER_REVIEWS);
			Boolean _refreshList = new Boolean(false);
			new AsyncGetCommunityBeers().execute(lExtras, _refreshList);

		}
		{
			mFavoriteBeerReviewsGallery = (LinearLayout) findViewById(R.id.favorite_beer_reviews_gallery);
			// Start a new thread that will download all the data
			Bundle lExtras = new Bundle();
			lExtras.putString("OPTION",
					AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS);
			Boolean _refreshList = new Boolean(false);
			new AsyncGetCommunityBeers().execute(lExtras, _refreshList);

		}
		{
			mAroundTheWorldBeersGallery = (LinearLayout) findViewById(R.id.around_the_world_beers_gallery);
			// Start a new thread that will download all the data
			Bundle lExtras = new Bundle();
			lExtras.putString("OPTION",
					AppConfig.COMMUNITY_BEERS_FROM_AROUND_THE_WORLD);
			Boolean _refreshList = new Boolean(false);
			new AsyncGetCommunityBeers().execute(lExtras, _refreshList);

		}

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
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onDestroy");
		}
		// Stop the tracker when it is no longer needed.
		mTracker.stop();
		if (AppConfig.LOGGING_ENABLED) {
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
	public boolean onCreateOptionsMenu(Menu menu) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreateOptionsMenu");
		}
		super.onCreateOptionsMenu(menu);
		int position = 0;
		menu.add(MENU_GROUP, PREFERENCES_ID, position++, R.string.preferences);
		menu.add(MENU_GROUP, ABOUT_ID, position++, R.string.menu_about);
		if (AppConfig.DEFAULT_APPSTORE.equals(AppConfig.GOOGLE_APPSTORE)) {
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
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onMenuItemSelected");
		}
		switch (item.getItemId()) {
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
	private void aboutBeer() {
		if (AppConfig.LOGGING_ENABLED) {
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
	private void sendErrorReport() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "Send Error Report");
		}

		Intent intent = new Intent(mMainActivity.getApplication(),
				CollectAndSendLog.class);
		startActivity(intent);

	}

	/**
	 * 
	 */
	private void preferences() {
		if (AppConfig.LOGGING_ENABLED) {
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
	private void showInstructions(Activity activity) {
		new AlertDialog.Builder(activity).setTitle(R.string.instructions_title)
				.setIcon(android.R.drawable.ic_dialog_info).setCancelable(true)
				.setPositiveButton(R.string.done_label, null)
				.setMessage(readAsset(activity, AppConfig.ASSET_INSTRUCTIONS))
				.show();

	}

	private CharSequence readAsset(Activity activity, String asset) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(activity.getAssets()
					.open(asset)));
			String line;
			StringBuilder buffer = new StringBuilder();
			while ((line = in.readLine()) != null)
				buffer.append(line).append('\n');
			return buffer;
		} catch (IOException e) {
			return "";
		} finally {
			closeStream(in);
		}
	}

	/**
	 * Closes the specified stream.
	 * 
	 * @param stream
	 *            The stream to close.
	 */
	private void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				Log.e(TAG,
						(e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "", e);
			}
		}
	}

	private void displayList(String pOption, List<CommunityBeer> pBeers) {
		Log.i(TAG, "displayList():: option = " + pOption + " " + pBeers.size()
				+ " beers displayed");
		LinearLayout lView = evaluateOption(pOption);
		
		for (CommunityBeer pBeer : pBeers) {
			final CommunityBeer lBeer = pBeer;
			
			String _urlImage = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_GET_IMAGE_Q + lBeer.beerId;
			LinearLayout layout = new LinearLayout(getApplicationContext());
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);

			layoutParams.setMargins(30, 20, 30, 0);
			layout.setLayoutParams(layoutParams);
			layout.setGravity(Gravity.CENTER);
			layout.setOrientation(LinearLayout.VERTICAL);

			ImageView thumbnail = new ImageView(getApplicationContext());
			thumbnail.setImageResource(R.drawable.bottle);
			thumbnail.setLayoutParams(new LayoutParams(220, 220));
			thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
			layout.addView(thumbnail);

			TextView textView = new TextView(getApplicationContext());
			textView.setLayoutParams(new LayoutParams(220, 220));
			textView.setText(lBeer.beer);
			layout.addView(textView);

			mDrawableManager.fetchDrawableOnThread(_urlImage, thumbnail);

			layout.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String _selection = lBeer.beer.replace(" ", "_") + "," + lBeer.beerId;
					mTracker.trackEvent("CommunityBeers", "Selection", _selection, 0);
					mTracker.dispatch();
					Intent intent = new Intent(mMainActivity.getApplication(),
							CommunityBeerView.class);
					intent.putExtra("COMMUNITY_BEER", lBeer);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
					startActivity(intent);
				}
			});
			lView.addView(layout);
		}
		//handle progress bar

	}

	private LinearLayout evaluateOption(String pOption) {
		if (pOption.equals(AppConfig.COMMUNITY_BEERS_FROM_AROUND_THE_WORLD)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_TOP_RATED_BEERS)) {
			return mTopRatedBeersGallery;
		} else if (pOption.equals(AppConfig.COMMUNITY_WORST_BEERS)) {
			return mWorstRatedBeersGallery;

		} else if (pOption.equals(AppConfig.COMMUNITY_BEERS_BY_COUNTRY)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_BEERS_BY_STATE)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_SEARCH_BEERS)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_MY_BEER_REVIEWS)) {
			return mMyBeersGallery;
		} else if (pOption
				.equals(AppConfig.COMMUNITY_MOST_HELPFUL_BEER_REVIEWS)) {
			return mMostHelpfulBeerReviewsGallery;

		} else if (pOption.equals(AppConfig.COMMUNITY_NEW_BEER_REVIEWS)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS)) {
			return mFavoriteBeerReviewsGallery;
		} else if (pOption.equals(AppConfig.COMMUNITY_BEER_OF_THE_DAY)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_COMPARABLE_BEER_REVIEWS)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_RECOMMENDED_BEER_REVIEWS)) {
		}
		return null;
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
				Log.i(_TAG, "doInBackground starting");
			}
			try {
				String currentVersion = getVersionCode(mMainActivity);
				String url = Util.getUpdateCheckUrl(currentVersion);
				Log.i(_TAG, "doInBackground:" + url);
				String response[] = Util.getResult(url);
				if ((response[0] != null) && (response[0].startsWith("{"))) {
					JSONObject json = new JSONObject(response[0]);
					boolean _updateAvailable = json
							.getBoolean("updateAvailable");

					if (_updateAvailable) {
						Runnable notification = new Runnable() {
							public void run() {
								if (mMainActivity != null) {
									new AlertDialog.Builder(mMainActivity)
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
				Log.i(_TAG, "onPostExecute starting");
			}
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(_TAG, "onPostExecute finished");
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
				Log.i(_TAG, "doInBackground starting");
			}

			Runnable notification = new Runnable() {
				public void run() {
					if (mMainActivity != null) {
						final SharedPreferences preferences = mMainActivity
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

								final Dialog dialog = new Dialog(mMainActivity,
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
				Log.i(_TAG, "onPostExecute starting");
			}
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(_TAG, "onPostExecute finished");
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
			Log.i(_TAG, "doInBackground starting");
			String userId = (String) args[0];
			try {
				String url = com.cm.beer.util.Util
						.getRecommendationsUrl(userId);

				Log.i(_TAG, "doInBackground:" + url);
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
				Log.i(_TAG, "doInBackground finished");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.i(_TAG, "onPostExecute starting");
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
			} else {
				Log.i(_TAG, "No recommendations found!");
			}
			Log.i(_TAG, "onPostExecute finished");
		}

	}

	/******************************************************/
	private class AsyncGetCommunityBeers extends
			AsyncTask<Object, Void, Object> {
		List<CommunityBeer> mBeers = new ArrayList<CommunityBeer>();
		Bundle mExtras;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "doInBackground starting");
			}
			mExtras = (Bundle) args[0];

			JSONArray beersJSONArray = null;
			try {

				String _url = getUrl(mExtras);
				Log.i(TAG, "doInBackground:URL=" + _url);
				String response[] = Util.getResult(_url);
				if ((response[0] != null) && (response[0].startsWith("["))) {
					beersJSONArray = new JSONArray(response[0]);
					mCs = response[1];
				}

				if (beersJSONArray != null) {

					CommunityBeer _beer = null;
					JSONObject beerJSONObject = null;
					int beersLength = beersJSONArray.length();
					for (int i = 0; i < beersLength; i++) {
						beerJSONObject = beersJSONArray.getJSONObject(i);
						_beer = new CommunityBeer();
						_beer.country = beerJSONObject.getString("country");
						_beer.created = beerJSONObject.getString("created");
						_beer.notes = beerJSONObject.getString("notes");
						_beer.picture = beerJSONObject.getString("picture");
						_beer.price = beerJSONObject.getString("price");
						_beer.rating = beerJSONObject.getString("rating");
						_beer.state = beerJSONObject.getString("state");
						_beer.share = beerJSONObject.getString("share");
						_beer.style = beerJSONObject.getString("style");
						_beer.updated = beerJSONObject.getString("updated");
						_beer.userId = beerJSONObject.getString("userId");
						_beer.userLink = beerJSONObject.getString("userLink");
						_beer.userName = beerJSONObject.getString("userName");
						_beer.beer = beerJSONObject.getString("beer");
						_beer.beerCreated = beerJSONObject
								.getString("beerCreated");
						_beer.beerId = beerJSONObject.getString("beerId");
						_beer.brewery = beerJSONObject.getString("brewery");
						_beer.beerUpdated = beerJSONObject
								.getString("beerUpdated");
						_beer.alcohol = beerJSONObject.getString("alcohol");

						_beer.timeZone = beerJSONObject.getString("timeZone");
						_beer.latitude = beerJSONObject.getString("latitude");
						_beer.longitude = beerJSONObject.getString("longitude");

						/** Added in V2 of the database **/
						if (beerJSONObject.has("characteristics")) {
							_beer.characteristics = beerJSONObject
									.getString("characteristics");
						}
						/** Added in V3 of the database **/
						if (beerJSONObject.has("breweryLink")) {
							if ((beerJSONObject.getString("breweryLink") != null)
									&& (!beerJSONObject
											.getString("breweryLink")
											.equals(""))
									&& (!beerJSONObject
											.getString("breweryLink").equals(
													"null"))) {
								_beer.breweryLink = beerJSONObject
										.getString("breweryLink");
							}
						}
						/** DEPRECATED in V4 of the database **/
						if (beerJSONObject.has("currency")) {
							_beer.currency = beerJSONObject
									.getString("currency");
						}
						/** Added in V4 of the database **/
						if (beerJSONObject.has("currencyCode")) {
							_beer.currencyCode = beerJSONObject
									.getString("currencyCode");
						}
						/** Added in V4 of the database **/
						if (beerJSONObject.has("currencySymbol")) {
							_beer.currencySymbol = beerJSONObject
									.getString("currencySymbol");
						}

						mBeers.add(_beer);
						Log.i(TAG, _beer.beerId + "::" + _beer.beer + "::BL::"
								+ _beer.breweryLink);
					}
				}
			} catch (Throwable e) {
				Log.e(TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
				mTracker.trackEvent(
						"CommunityBeers",
						"DownloadError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "doInBackground finished");
			}
			return null;
		}

		protected void onPostExecute(Object result) {
			Log.i(TAG, "onPostExecute starting");
			try {
				Log.i(TAG, "onPostExecute Display List");
				String option = ((mExtras.getString("OPTION") != null) && (!mExtras
						.getString("OPTION").equals(""))) ? mExtras
						.getString("OPTION") : "";
				Main.this.displayList(option, mBeers);
			} catch (Throwable e) {
				Log.e(TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
			}
			Log.i(TAG, "onPostExecute finished");
		}

		/**
		 * Builds the Yelp Url
		 * 
		 * @return
		 * @throws UnsupportedEncodingException
		 */
		private String getUrl(Bundle extras)
				throws UnsupportedEncodingException {
			String option = ((extras.getString("OPTION") != null) && (!extras
					.getString("OPTION").equals(""))) ? extras
					.getString("OPTION") : "";
			Log.i(TAG, "OPTION=" + option);
			String country = ((extras.getString("COUNTRY") != null) && (!extras
					.getString("COUNTRY").equals(""))) ? extras
					.getString("COUNTRY") : "";
			Log.i(TAG, "COUNTRY=" + country);
			String region = ((extras.getString("REGION") != null) && (!extras
					.getString("REGION").equals(""))) ? extras
					.getString("REGION") : "";
			Log.i(TAG, "REGION=" + region);
			String userId = ((extras.getString("USERID") != null) && (!extras
					.getString("USERID").equals(""))) ? extras
					.getString("USERID") : "";
			Log.i(TAG, "USERID=" + userId);
			String beerIds = ((extras.getString("BEERIDS") != null) && (!extras
					.getString("BEERIDS").equals(""))) ? extras
					.getString("BEERIDS") : "";
			Log.i(TAG, "BEERIDS=" + beerIds);

			if (option.equals(AppConfig.COMMUNITY_BEERS_FROM_AROUND_THE_WORLD)) {
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_BEERS_Q
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_TOP_RATED_BEERS)) {
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_TOP_RATED_BEERS_Q
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_WORST_BEERS)) {
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_WORST_BEERS_Q
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_BEERS_BY_COUNTRY)) {
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_BEERS_BY_COUNTRY_Q
						+ AppConfig.COMMUNITY_GET_BEERS_COUNTRY_PARAM
						+ URLEncoder.encode(country, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_BEERS_BY_STATE)) {
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_BEERS_BY_STATES_Q
						+ AppConfig.COMMUNITY_GET_BEERS_COUNTRY_PARAM
						+ URLEncoder.encode(country, "UTF-8")
						+ AppConfig.COMMUNITY_GET_BEERS_STATE_PARAM
						+ URLEncoder.encode(region, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_SEARCH_BEERS)) {
				return getSearchUrl(extras);
			} else if (option.equals(AppConfig.COMMUNITY_MY_BEER_REVIEWS)) {
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_MY_BEERS_Q
						+ AppConfig.COMMUNITY_USERID_PARAM
						+ URLEncoder.encode(userId, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option
					.equals(AppConfig.COMMUNITY_MOST_HELPFUL_BEER_REVIEWS)) {
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_MOST_HELPFUL_BEER_REVIEWS_Q
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_NEW_BEER_REVIEWS)) {
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_NEW_BEER_REVIEWS_Q
						+ AppConfig.COMMUNITY_BEERIDS_PARAM
						+ URLEncoder.encode(beerIds, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS)) {
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_FAVORITE_BEERS_Q
						+ AppConfig.COMMUNITY_USERID_PARAM
						+ URLEncoder.encode(userId, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_BEER_OF_THE_DAY)) {
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_NEW_BEER_REVIEWS_Q
						+ AppConfig.COMMUNITY_BEERIDS_PARAM
						+ URLEncoder.encode(beerIds, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option
					.equals(AppConfig.COMMUNITY_COMPARABLE_BEER_REVIEWS)) {
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_NEW_BEER_REVIEWS_Q
						+ AppConfig.COMMUNITY_BEERIDS_PARAM
						+ URLEncoder.encode(beerIds, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option
					.equals(AppConfig.COMMUNITY_RECOMMENDED_BEER_REVIEWS)) {
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_NEW_BEER_REVIEWS_Q
						+ AppConfig.COMMUNITY_BEERIDS_PARAM
						+ URLEncoder.encode(beerIds, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			}

			return "";
		}

		/**
		 * 
		 * @param extras
		 * @return
		 * @throws UnsupportedEncodingException
		 */
		private String getSearchUrl(Bundle extras)
				throws UnsupportedEncodingException {
			StringBuilder searchUri = new StringBuilder();

			String rating = extras.getString(NotesDbAdapter.KEY_RATING);
			Log.i(TAG, "rating=" + rating);

			String beer = extras.getString(NotesDbAdapter.KEY_BEER);
			Log.i(TAG, "beer=" + beer);

			String alcohol = extras.getString(NotesDbAdapter.KEY_ALCOHOL);
			Log.i(TAG, "alcohol=" + alcohol);

			String style = extras.getString(NotesDbAdapter.KEY_STYLE);
			Log.i(TAG, "style=" + style);

			String brewery = extras.getString(NotesDbAdapter.KEY_BREWERY);
			Log.i(TAG, "brewery=" + brewery);

			String state = extras.getString(NotesDbAdapter.KEY_STATE);
			Log.i(TAG, "state=" + state);

			String country = extras.getString(NotesDbAdapter.KEY_COUNTRY);
			Log.i(TAG, "country=" + country);

			String userName = extras.getString(NotesDbAdapter.KEY_USER_NAME);
			Log.i(TAG, "username	=" + userName);

			String userId = extras.getString(NotesDbAdapter.KEY_USER_ID);
			Log.i(TAG, "userid	=" + userId);

			// If all fields are null then return
			if ((rating == null) && (beer == null) && (alcohol == null)
					&& (style == null) && (brewery == null) && (state == null)
					&& (country == null) && (userName == null)
					&& (userId == null)) {
				return "";
			}
			if (rating != null && (!rating.equals(""))) {
				searchUri.append("&rating="
						+ (URLEncoder.encode(rating, "UTF-8")));
			}
			if (beer != null && (!beer.equals(""))) {
				searchUri.append("&beer=" + (URLEncoder.encode(beer, "UTF-8")));
			}
			if (alcohol != null && (!alcohol.equals(""))) {
				searchUri.append("&alcohol="
						+ (URLEncoder.encode(alcohol, "UTF-8")));
			}
			if (style != null && (!style.equals(""))) {
				searchUri.append("&style="
						+ (URLEncoder.encode(style, "UTF-8")));
			}
			if (brewery != null && (!brewery.equals(""))) {
				searchUri.append("&brewery="
						+ (URLEncoder.encode(brewery, "UTF-8")));
			}
			if (state != null && (!state.equals(""))) {
				searchUri.append("&state="
						+ (URLEncoder.encode(state, "UTF-8")));
			}
			if (country != null && (!country.equals(""))) {
				searchUri.append("&country="
						+ (URLEncoder.encode(country, "UTF-8")));
			}

			if (userName != null && (!userName.equals(""))) {
				searchUri.append("&username="
						+ (URLEncoder.encode(userName, "UTF-8")));
			}
			if (userId != null && (!userId.equals(""))) {
				searchUri.append("&userid="
						+ (URLEncoder.encode(userId, "UTF-8")));
			}
			User user = new User(mMainActivity);
			if (user.getUserId() != null) {
				searchUri.append("&searchuserid="
						+ (URLEncoder.encode(user.getUserId(), "UTF-8")));
			}
			Log.i(TAG, "Search URI => " + searchUri);

			String searchURL = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_SEARCH_BEERS_Q + searchUri.toString()
					+ AppConfig.COMMUNITY_R
					+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			Log.i(TAG, "Search URL => " + searchURL);
			// return encoded url
			return searchURL;
		}

	}

}