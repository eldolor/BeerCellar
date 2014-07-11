package com.cm.beer.activity.slidingmenu;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cm.beer.activity.CommunityBeerView;
import com.cm.beer.activity.LoginIntercept;
import com.cm.beer.activity.Main;
import com.cm.beer.activity.R;
import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.transfer.CommunityBeer;
import com.cm.beer.util.ContentManager;
import com.cm.beer.util.DrawableManager;
import com.cm.beer.util.Logger;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class HomeFragment extends android.support.v4.app.Fragment {

	String TAG;
	String mSelectedOption = null;
	ProgressDialog mSplashDialog;

	static final int ACTIVITY_ABOUT = 0;
	static final int MENU_GROUP = 0;
	static final int ABOUT_ID = Menu.FIRST;
	static final int SEND_ERROR_REPORT_ID = Menu.FIRST + 1;
	static final int PREFERENCES_ID = Menu.FIRST + 2;

	GoogleAnalyticsTracker mTracker;
	DrawableManager mDrawableManager;
	ContentManager mContentManager;
	LinearLayout mTopRatedBeersGallery;
	LinearLayout mMyBeersGallery;
	LinearLayout mWorstRatedBeersGallery;
	LinearLayout mMostHelpfulBeerReviewsGallery;
	LinearLayout mFavoriteBeerReviewsGallery;
	LinearLayout mAroundTheWorldBeersGallery;

	private View mRootView;

	User mUser;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())
				Logger.log("onCreate");
		}

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID,
				getActivity());
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())
				Logger.log("onCreate:Google Tracker Instantiated");
		}
		mUser = new User(getActivity().getApplicationContext());

		mDrawableManager = DrawableManager.getInstance();
		mContentManager = ContentManager.getInstance();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		mRootView = inflater.inflate(R.layout.fragment_home, container, false);

		{
			mTopRatedBeersGallery = (LinearLayout) mRootView
					.findViewById(R.id.top_rated_beers_gallery);
			// Start a new thread that will download all the data
			Bundle lExtras = new Bundle();
			lExtras.putString("OPTION", AppConfig.COMMUNITY_TOP_RATED_BEERS);
			new AsyncGetCommunityBeers().execute(lExtras,
					Boolean.valueOf(false));
		}
		{
			mMyBeersGallery = (LinearLayout) mRootView
					.findViewById(R.id.my_beers_gallery);
			// Start a new thread that will download all the data
			Bundle lExtras = new Bundle();
			lExtras.putString("OPTION", AppConfig.COMMUNITY_MY_BEER_REVIEWS);
			new AsyncGetCommunityBeers().execute(lExtras,
					Boolean.valueOf(false));

		}
		{
			mWorstRatedBeersGallery = (LinearLayout) mRootView
					.findViewById(R.id.worst_rated_beers_gallery);
			// Start a new thread that will download all the data
			Bundle lExtras = new Bundle();
			lExtras.putString("OPTION", AppConfig.COMMUNITY_WORST_BEERS);
			new AsyncGetCommunityBeers().execute(lExtras,
					Boolean.valueOf(false));

		}
		{
			mMostHelpfulBeerReviewsGallery = (LinearLayout) mRootView
					.findViewById(R.id.most_helpful_beer_reviews_gallery);
			// Start a new thread that will download all the data
			Bundle lExtras = new Bundle();
			lExtras.putString("OPTION",
					AppConfig.COMMUNITY_MOST_HELPFUL_BEER_REVIEWS);
			new AsyncGetCommunityBeers().execute(lExtras,
					Boolean.valueOf(false));

		}
		{
			mFavoriteBeerReviewsGallery = (LinearLayout) mRootView
					.findViewById(R.id.favorite_beer_reviews_gallery);
			// Start a new thread that will download all the data
			Bundle lExtras = new Bundle();
			lExtras.putString("OPTION",
					AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS);
			new AsyncGetCommunityBeers().execute(lExtras,
					Boolean.valueOf(false));

		}
		{
			mAroundTheWorldBeersGallery = (LinearLayout) mRootView
					.findViewById(R.id.around_the_world_beers_gallery);
			// Start a new thread that will download all the data
			Bundle lExtras = new Bundle();
			lExtras.putString("OPTION",
					AppConfig.COMMUNITY_BEERS_FROM_AROUND_THE_WORLD);
			new AsyncGetCommunityBeers().execute(lExtras,
					Boolean.valueOf(false));

		}

		return mRootView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())
				Logger.log("onDestroy");
		}
		// Stop the tracker when it is no longer needed.
		mTracker.stop();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())
				Logger.log("onCreate:Google Tracker Stopped!");
		}
		super.onDestroy();
	}

	private void displayList(final String pOption, List<CommunityBeer> pBeers) {
		if (Logger.isLogEnabled())
			Logger.log("displayList():: option = " + pOption + " "
					+ pBeers.size() + " beers displayed");
		LinearLayout lView = evaluateGallery(pOption);

		for (CommunityBeer pBeer : pBeers) {
			final CommunityBeer lBeer = pBeer;

			{
				String _urlImage = AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_IMAGE_Q + lBeer.beerId;
				LinearLayout layout = new LinearLayout(getActivity()
						.getApplicationContext());
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);

				layoutParams.setMargins(30, 20, 30, 0);
				layout.setLayoutParams(layoutParams);
				layout.setGravity(Gravity.CENTER);
				layout.setOrientation(LinearLayout.VERTICAL);

				ImageView thumbnail = new ImageView(getActivity()
						.getApplicationContext());
				thumbnail.setImageResource(R.drawable.bottle);
				thumbnail.setLayoutParams(new LayoutParams(220, 220));
				thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
				layout.addView(thumbnail);

				TextView textView = new TextView(getActivity()
						.getApplicationContext());
				textView.setLayoutParams(new LayoutParams(220, 220));
				textView.setText(lBeer.beer);
				layout.addView(textView);

				mDrawableManager.fetchDrawableOnThread(_urlImage, thumbnail);

				layout.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						String _selection = lBeer.beer.replace(" ", "_") + ","
								+ lBeer.beerId;
						mTracker.trackEvent("CommunityBeers", "Selection",
								_selection, 0);
						mTracker.dispatch();
						Intent intent = new Intent(getActivity()
								.getApplication(), CommunityBeerView.class);
						intent.putExtra("COMMUNITY_BEER", lBeer);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
						startActivity(intent);
					}
				});

				lView.addView(layout);
			}
		}
		if (((pOption.equals(AppConfig.COMMUNITY_MY_BEER_REVIEWS)) || (pOption
				.equals(AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS)))
				&& (!mUser.isLoggedIn())) {
			setLoginView(pOption, lView);
		} else {
			setLoadMoreView(pOption, lView);
		}
	}

	private void setLoadMoreView(final String pOption, LinearLayout lView) {
		LinearLayout layout = new LinearLayout(getActivity()
				.getApplicationContext());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		layoutParams.setMargins(30, 20, 30, 0);
		layout.setLayoutParams(layoutParams);
		layout.setGravity(Gravity.CENTER);
		layout.setOrientation(LinearLayout.VERTICAL);

		TextView textView = new TextView(getActivity().getApplicationContext());
		textView.setLayoutParams(new LayoutParams(220, 220));
		textView.setGravity(Gravity.CENTER);
		textView.setText(R.string.community_beer_list_footer_view_label);
		layout.addView(textView);

		layout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				displayMore(pOption);
			}
		});
		lView.addView(layout);
	}

	private void setLoginView(final String pOption, LinearLayout lView) {
		LinearLayout layout = new LinearLayout(getActivity()
				.getApplicationContext());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		layoutParams.setMargins(30, 20, 30, 0);
		layout.setLayoutParams(layoutParams);
		layout.setGravity(Gravity.CENTER);
		layout.setOrientation(LinearLayout.VERTICAL);

		TextView textView = new TextView(getActivity().getApplicationContext());
		textView.setLayoutParams(new LayoutParams(220, 220));
		textView.setGravity(Gravity.CENTER);
		textView.setText(R.string.title_login);
		layout.addView(textView);

		layout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Main mainActivity = (Main) getActivity();
				if (pOption.equals(AppConfig.COMMUNITY_MY_BEER_REVIEWS)) {
					mainActivity.displayView(4, true);
				} else if (pOption
						.equals(AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS)) {
					mainActivity.displayView(5, true);
				}
			}
		});
		lView.addView(layout);
	}

	private LinearLayout evaluateGallery(String pOption) {
		if (pOption.equals(AppConfig.COMMUNITY_BEERS_FROM_AROUND_THE_WORLD)) {
			return mAroundTheWorldBeersGallery;
		} else if (pOption.equals(AppConfig.COMMUNITY_TOP_RATED_BEERS)) {
			return mTopRatedBeersGallery;
		} else if (pOption.equals(AppConfig.COMMUNITY_WORST_BEERS)) {
			return mWorstRatedBeersGallery;
		} else if (pOption.equals(AppConfig.COMMUNITY_MY_BEER_REVIEWS)) {
			return mMyBeersGallery;
		} else if (pOption
				.equals(AppConfig.COMMUNITY_MOST_HELPFUL_BEER_REVIEWS)) {
			return mMostHelpfulBeerReviewsGallery;
		} else if (pOption.equals(AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS)) {
			return mFavoriteBeerReviewsGallery;
		} else if (pOption.equals(AppConfig.COMMUNITY_BEER_OF_THE_DAY)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_COMPARABLE_BEER_REVIEWS)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_RECOMMENDED_BEER_REVIEWS)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_BEERS_BY_COUNTRY)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_BEERS_BY_STATE)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_SEARCH_BEERS)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_NEW_BEER_REVIEWS)) {
		}
		return null;
	}

	private void displayMore(String pOption) {
		Main mainActivity = (Main) getActivity();
		if (pOption.equals(AppConfig.COMMUNITY_BEERS_FROM_AROUND_THE_WORLD)) {
			mTracker.trackEvent("CommunityOptions",
					"BeersFromAroundTheWorldLoadMore", "Clicked", 0);
			mTracker.dispatch();
			mainActivity.displayView(1, true);
		} else if (pOption.equals(AppConfig.COMMUNITY_TOP_RATED_BEERS)) {
			mTracker.trackEvent("CommunityOptions", "TopRatedBeersLoadMore",
					"Clicked", 0);
			mTracker.dispatch();
			mainActivity.displayView(2, true);
		} else if (pOption.equals(AppConfig.COMMUNITY_WORST_BEERS)) {
			mTracker.trackEvent("CommunityOptions", "WorstBeersLoadMore",
					"Clicked", 0);
			mTracker.dispatch();
			mainActivity.displayView(3, true);
		} else if (pOption.equals(AppConfig.COMMUNITY_MY_BEER_REVIEWS)) {
			mTracker.trackEvent("CommunityOptions", "MyBeerReviews", "Clicked",
					0);
			mTracker.dispatch();
			mainActivity.displayView(4, true);
		} else if (pOption.equals(AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS)) {
			mainActivity.displayView(5, true);
		} else if (pOption.equals(AppConfig.COMMUNITY_BEER_OF_THE_DAY)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_COMPARABLE_BEER_REVIEWS)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_RECOMMENDED_BEER_REVIEWS)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_BEERS_BY_COUNTRY)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_BEERS_BY_STATE)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_SEARCH_BEERS)) {
		} else if (pOption.equals(AppConfig.COMMUNITY_NEW_BEER_REVIEWS)) {
		}

	}

	private void getMyBeers(String userId) {
		if (Logger.isLogEnabled())
			Logger.log("getMyBeers");
		Intent intent = new Intent(getActivity().getApplication(),
				CommunityBeersFragment.class);
		intent.putExtra("OPTION", AppConfig.COMMUNITY_MY_BEER_REVIEWS);
		intent.putExtra("USERID", userId);
		startActivity(intent);

	}

	private void getFavoriteBeers(String userId) {
		if (Logger.isLogEnabled())
			Logger.log("getFavoriteBeers");
		Intent intent = new Intent(getActivity().getApplication(),
				CommunityBeersFragment.class);
		intent.putExtra("OPTION", AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS);
		intent.putExtra("USERID", userId);
		startActivity(intent);

	}

	/******************************************************/
	private class AsyncGetCommunityBeers extends
			AsyncTask<Object, Void, Object> {
		List<CommunityBeer> mBeers = new ArrayList<CommunityBeer>();
		// NOTE: mCs cannot be null
		private String mCs = "";
		Bundle mExtras;

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())
					Logger.log("doInBackground starting");
			}
			mExtras = (Bundle) args[0];

			JSONArray beersJSONArray = null;
			try {

				String _url = getUrl(mExtras);

				String response[] = Util.getResult(_url);
				if ((response[0] != null) && (response[0].startsWith("["))) {
					beersJSONArray = new JSONArray(response[0]);
					mCs = response[1];
				}

				if (beersJSONArray != null) {

					CommunityBeer _beer = null;
					JSONObject beerJSONObject = null;
					int beersLength = beersJSONArray.length();
					if (Logger.isLogEnabled())
						Logger.log("AsyncGetCommunityBeers URL=" + _url
								+ " Downloaded " + beersLength + " beers");

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
						if (Logger.isLogEnabled())
							Logger.log(_beer.beerId + "::" + _beer.beer
									+ "::BL::" + _beer.breweryLink);
					}
				} else {
					Log.e(TAG, "AsyncGetCommunityBeers URL=" + _url
							+ " Download Error " + response[0] + "::"
							+ response[1]);

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
				if (Logger.isLogEnabled())
					Logger.log("doInBackground finished");
			}
			return null;
		}

		protected void onPostExecute(Object result) {
			if (Logger.isLogEnabled())
				Logger.log("onPostExecute starting");
			try {
				if (Logger.isLogEnabled())
					Logger.log("onPostExecute Display List");
				String option = ((mExtras.getString("OPTION") != null) && (!mExtras
						.getString("OPTION").equals(""))) ? mExtras
						.getString("OPTION") : "";
				displayList(option, mBeers);
			} catch (Throwable e) {
				Log.e(TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
			}
			if (Logger.isLogEnabled())
				Logger.log("onPostExecute finished");
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
			if (Logger.isLogEnabled())
				Logger.log("OPTION=" + option);
			String country = ((extras.getString("COUNTRY") != null) && (!extras
					.getString("COUNTRY").equals(""))) ? extras
					.getString("COUNTRY") : "";
			if (Logger.isLogEnabled())
				Logger.log("COUNTRY=" + country);
			String region = ((extras.getString("REGION") != null) && (!extras
					.getString("REGION").equals(""))) ? extras
					.getString("REGION") : "";
			if (Logger.isLogEnabled())
				Logger.log("REGION=" + region);
			String userId = ((extras.getString("USERID") != null) && (!extras
					.getString("USERID").equals(""))) ? extras
					.getString("USERID") : "";
			if (Logger.isLogEnabled())
				Logger.log("USERID=" + userId);
			String beerIds = ((extras.getString("BEERIDS") != null) && (!extras
					.getString("BEERIDS").equals(""))) ? extras
					.getString("BEERIDS") : "";
			if (Logger.isLogEnabled())
				Logger.log("BEERIDS=" + beerIds);

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
			if (Logger.isLogEnabled())
				Logger.log("rating=" + rating);

			String beer = extras.getString(NotesDbAdapter.KEY_BEER);
			if (Logger.isLogEnabled())
				Logger.log("beer=" + beer);

			String alcohol = extras.getString(NotesDbAdapter.KEY_ALCOHOL);
			if (Logger.isLogEnabled())
				Logger.log("alcohol=" + alcohol);

			String style = extras.getString(NotesDbAdapter.KEY_STYLE);
			if (Logger.isLogEnabled())
				Logger.log("style=" + style);

			String brewery = extras.getString(NotesDbAdapter.KEY_BREWERY);
			if (Logger.isLogEnabled())
				Logger.log("brewery=" + brewery);

			String state = extras.getString(NotesDbAdapter.KEY_STATE);
			if (Logger.isLogEnabled())
				Logger.log("state=" + state);

			String country = extras.getString(NotesDbAdapter.KEY_COUNTRY);
			if (Logger.isLogEnabled())
				Logger.log("country=" + country);

			String userName = extras.getString(NotesDbAdapter.KEY_USER_NAME);
			if (Logger.isLogEnabled())
				Logger.log("username	=" + userName);

			String userId = extras.getString(NotesDbAdapter.KEY_USER_ID);
			if (Logger.isLogEnabled())
				Logger.log("userid	=" + userId);

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
			User user = new User(getActivity());
			if (user.getUserId() != null) {
				searchUri.append("&searchuserid="
						+ (URLEncoder.encode(user.getUserId(), "UTF-8")));
			}
			if (Logger.isLogEnabled())
				Logger.log("Search URI => " + searchUri);

			String searchURL = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_SEARCH_BEERS_Q + searchUri.toString()
					+ AppConfig.COMMUNITY_R
					+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			if (Logger.isLogEnabled())
				Logger.log("Search URL => " + searchURL);
			// return encoded url
			return searchURL;
		}

	}

}