package com.cm.beer.activity.slidingmenu;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.beer.activity.BeerWebView;
import com.cm.beer.activity.CommunityBeerView;
import com.cm.beer.activity.LoginIntercept;
import com.cm.beer.activity.R;
import com.cm.beer.activity.ShareOnFacebook;
import com.cm.beer.activity.R.id;
import com.cm.beer.activity.R.layout;
import com.cm.beer.activity.R.string;
import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.transfer.CommunityBeer;
import com.cm.beer.util.ContentManager;
import com.cm.beer.util.DrawableManager;
import com.cm.beer.util.Logger;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class CommunityBeersFragment extends android.support.v4.app.ListFragment {
	String TAG;
	GoogleAnalyticsTracker mTracker;
	int mActiveDialog;

	List<CommunityBeer> mBeers = new ArrayList<CommunityBeer>();
	// NOTE: mCs cannot be null
	String mCs = "";

	static final int MENU_GROUP = 0;
	static final int ABOUT_THIS_BEER_ID = Menu.FIRST;
	static final int SHOW_LOCATION_ID = Menu.FIRST + 1;
	static final int SHARE_ON_FACEBOOK_ID = Menu.FIRST + 2;

	static final int ACTIVITY_SHARE_ON_FACEBOOK = 2;

	SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM d, yyyy");

	Bundle mExtras;

	BeerListAdapter mAdapter;

	ListView mBeersView;

	View mFooterView;
	DrawableManager mDrawableManager;
	ContentManager mContentManager;
	private View mRootView;

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
		// Start the mTracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID,
				getActivity().getApplication());
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())
				Logger.log("onCreate:Google Tracker Instantiated");
		}
		mExtras = this.getArguments();

		// Start a new thread that will download all the data
		new AsyncGetCommunityBeers().execute(mExtras, Boolean.valueOf(false));

		mDrawableManager = DrawableManager.getInstance();
		mContentManager = ContentManager.getInstance();
		Util.loadInterstitialAd(getActivity().getApplication());

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		mRootView = inflater.inflate(R.layout.fragment_community_beer_list,
				container, false);

		return mRootView;

	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		displayList();
	}

	private void displayList() {
		// initialize Footer View for the list
		initFooterView();
		registerForContextMenu(getListView());
		getListView().setTextFilterEnabled(true);

		mTracker.trackPageView("CommunityBeers");
		mTracker.dispatch();

		Util.setGoogleAdSense(getActivity().getApplication());

		if (mBeers != null) {
			mBeersView = getListView();
			// Call getActivity().getApplication() before calling setAdapter
			if ((mBeers.size() >= Integer.valueOf(AppConfig.COMMUNITY_R_VALUE))
					&& (!mCs.equals(""))) {
				mBeersView.addFooterView(mFooterView);
			}

			mBeersView.setTextFilterEnabled(true);

			mAdapter = new BeerListAdapter(getActivity().getApplication(),
					R.layout.fragment_community_beer_list, mBeers);
			setListAdapter(mAdapter);

			Toast.makeText(getActivity(), R.string.hint_community_page,
					Toast.LENGTH_LONG).show();

		}
		if ((mBeers != null) && (mBeers.size() == 0)) {
			TextView noBeers = (TextView) mRootView
					.findViewById(android.R.id.empty);
			noBeers.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// Start a new thread that will download all the
					// data
					Boolean _refreshList = new Boolean(false);
					new AsyncGetCommunityBeers().execute(mExtras, _refreshList);

				}
			});
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
	public void onConfigurationChanged(Configuration newConfig) {
		// DO NOTHING
		super.onConfigurationChanged(newConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (Logger.isLogEnabled())
			Logger.log("onCreateContextMenu");
		int menuPosition = 0;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		CommunityBeer beer = mBeers.get(info.position);
		menu.add(MENU_GROUP, SHARE_ON_FACEBOOK_ID, menuPosition++,
				R.string.menu_share_on_facebook);
		if (Logger.isLogEnabled())
			Logger.log("onCreateContextMenu:" + beer.beer + ":" + beer.latitude
					+ "," + beer.longitude);
		if ((beer.latitude != null) && (!beer.latitude.equals("0.0"))) {
			if ((beer.longitude != null) && (!beer.longitude.equals("0.0"))) {
				menu.add(MENU_GROUP, SHOW_LOCATION_ID, menuPosition++,
						R.string.menu_show_location);
			}
		}

		if ((beer.style != null) && (!beer.style.equals(""))) {
			menu.add(MENU_GROUP, ABOUT_THIS_BEER_ID, menuPosition++,
					R.string.menu_about_this_beer);
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())
				Logger.log("onContextItemSelected");
		}
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		CommunityBeer beer = mBeers.get(info.position);
		if (Logger.isLogEnabled())
			Logger.log("onContextItemSelected:" + beer.beer + "::" + beer.style);
		switch (item.getItemId()) {
		case SHARE_ON_FACEBOOK_ID:
			mTracker.trackEvent("CommunityBeers", "ShareOnFacebook", "Clicked",
					0);
			mTracker.dispatch();
			shareOnFacebook(info.id);
			return true;
		case ABOUT_THIS_BEER_ID:
			aboutThisBeer(beer.beer, beer.style);
			return true;
		case SHOW_LOCATION_ID:
			mTracker.trackEvent("CommunityBeers", "ShowLocation", "Clicked", 0);
			mTracker.dispatch();
			viewMap(info.position);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (Logger.isLogEnabled())
			Logger.log("onActivityResult; request code = " + requestCode
					+ " result code = " + resultCode);
		Bundle extras = (intent != null) ? intent.getExtras() : null;
		if (requestCode == AppConfig.FACEBOOK_LOGIN_INTERCEPT_REQUEST_CODE_FOR_WALL_POST) {
			if (resultCode == Activity.RESULT_OK) {
				// pass the rowId along to ShareOnFacebook
				long rowId = (extras != null) ? extras
						.getLong(NotesDbAdapter.KEY_ROWID) : 0L;
				if (Logger.isLogEnabled())
					Logger.log("onActivityResult:Row Id=" + rowId);
				CommunityBeer _beer = mBeers.get((int) rowId);
				Intent newIntent = new Intent(getActivity().getApplication(),
						ShareOnFacebook.class);
				newIntent.putExtra("COMMUNITY_BEER", _beer);
				startActivityForResult(newIntent, ACTIVITY_SHARE_ON_FACEBOOK);
			}
		} else {
			// fillData();
			if (resultCode == AppConfig.FACEBOOK_WALL_POST_SUCCESSFUL_RESULT_CODE) {
				Toast.makeText(getActivity().getApplication(),
						R.string.on_facebook_wall_post, Toast.LENGTH_SHORT)
						.show();
			}
		}

	}

	private void shareOnFacebook(long rowId) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())
				Logger.log("shareOnFacebook");
		}
		getActivity().showDialog(AppConfig.DIALOG_LOADING_ID);
		// Intent intent = new Intent(getActivity().getApplication(),
		// ShareOnFacebook.class);
		// intent.putExtra(NotesDbAdapter.KEY_ROWID, rowId);
		// startActivityForResult(intent, ACTIVITY_SHARE);
		Intent intent = new Intent(getActivity().getApplication(),
				LoginIntercept.class);
		intent.putExtra("FACEBOOK_PERMISSIONS", AppConfig.FACEBOOK_PERMISSIONS);
		intent.putExtra(NotesDbAdapter.KEY_ROWID, rowId);
		if (Logger.isLogEnabled())
			Logger.log("shareOnFacebook:Row Id=" + rowId);
		startActivityForResult(intent,
				AppConfig.FACEBOOK_LOGIN_INTERCEPT_REQUEST_CODE_FOR_WALL_POST);
	}

	/**
	 * 
	 * @param rowId
	 */
	private void aboutThisBeer(String beer, String style) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())
				Logger.log("aboutThisBeer:" + beer);
		}
		String _beerStyleUri = style.replace(" ", "_");
		String _url = AppConfig.WIKIPEDIA_REF_URL + _beerStyleUri;

		mTracker.trackEvent("CommunityBeers", "MoreAboutThisBeer",
				_beerStyleUri, 0);
		mTracker.dispatch();

		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())
				Logger.log("aboutThisBeer:URL:" + _url);
		}
		openBrowser(_url, beer);
	}

	/**
	 * 
	 * @param url
	 * @param beer
	 */
	private void openBrowser(String url, String beer) {
		Intent intent = new Intent(getActivity().getApplication(),
				BeerWebView.class);
		intent.putExtra("URL", url);
		intent.putExtra("TITLE", beer);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(intent);

	}

	/**
	 * 
	 * @param id
	 */
	private void viewMap(int position) {
		CommunityBeer beer = mBeers.get(position);

		String _selection = beer.latitude + "," + beer.longitude;

		mTracker.trackEvent("CommunityBeers", "ShowLocation", _selection, 0);
		mTracker.dispatch();
		// Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"
		// + beer.latitude + "," + beer.longitude + "?z="
		// + AppConfig.GOOGLE_MAPS_ZOOM_LEVEL));
		Intent i = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps?" + "z="
						+ AppConfig.GOOGLE_MAPS_ZOOM_LEVEL + "&t=m" + "&q=loc:"
						+ beer.latitude + "," + beer.longitude));
		startActivity(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView,
	 * android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())
				Logger.log("onListItemClick");
		}

		super.onListItemClick(l, v, position, id);
		CommunityBeer _beer = mBeers.get((int) id);

		String _selection = _beer.beer.replace(" ", "_") + "," + _beer.beerId;
		mTracker.trackEvent("CommunityBeers", "Selection", _selection, 0);
		mTracker.dispatch();
		Intent intent = new Intent(getActivity().getApplication(),
				CommunityBeerView.class);
		intent.putExtra("COMMUNITY_BEER", _beer);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(intent);
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
		// Stop the mTracker when it is no longer needed.
		mTracker.stop();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())
				Logger.log("onCreate:Google Tracker Stopped!");
		}
		mContentManager.clear();
		mDrawableManager.clear();
		super.onDestroy();
	}

	private void initFooterView() {
		LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mFooterView = vi.inflate(R.layout.beer_list_footer, getListView(),
				false);
		mFooterView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// make spinner visible
				((ProgressBar) mFooterView.findViewById(R.id.spinner))
						.setVisibility(View.VISIBLE);
				Boolean _refreshList = new Boolean(true);
				new AsyncGetCommunityBeers().execute(mExtras, _refreshList);
			}
		});
	}

	/************************************************************************************/
	private class AsyncGetCommunityBeers extends
			AsyncTask<Object, Void, Object> {

		private boolean _mRefreshList;
		private boolean _retrievedMoreData;

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
			Bundle extras = (Bundle) args[0];
			_mRefreshList = (Boolean) args[1];
			JSONArray beersJSONArray = null;
			try {

				String _url = getUrl(extras);
				if (Logger.isLogEnabled())
					Logger.log("doInBackground:URL=" + _url);
				String response[] = Util.getResult(_url);
				if ((response[0] != null) && (response[0].startsWith("["))) {
					beersJSONArray = new JSONArray(response[0]);
					mCs = response[1];
				}

				if (beersJSONArray != null) {

					CommunityBeer _beer = null;
					JSONObject beerJSONObject = null;
					int beersLength = beersJSONArray.length();
					if (beersLength > 0) {
						_retrievedMoreData = true;
					} else {
						_retrievedMoreData = false;
					}
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

						CommunityBeersFragment.this.mBeers.add(_beer);
						if (Logger.isLogEnabled())
							Logger.log(_beer.beerId + "::" + _beer.beer
									+ "::BL::" + _beer.breweryLink);
					}
				} else {
					_retrievedMoreData = false;
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
					Logger.log("onPostExecute Refresh List = "
							+ ((_mRefreshList) ? "true" : "false")
							+ " Retrieved More Data = "
							+ ((_retrievedMoreData) ? "true" : "false"));
				if (!_mRefreshList) {
					if (Logger.isLogEnabled())
						Logger.log("onPostExecute Display List");
					CommunityBeersFragment.this.displayList();
				} else if (_mRefreshList && _retrievedMoreData) {
					if (Logger.isLogEnabled())
						Logger.log("onPostExecute Refresh List");
					mAdapter.notifyDataSetChanged();
					// make spinner invisible
					((ProgressBar) mFooterView.findViewById(R.id.spinner))
							.setVisibility(View.INVISIBLE);
				} else {
					if (Logger.isLogEnabled())
						Logger.log("onPostExecute Remove Footer View!");
					mBeersView.removeFooterView(mFooterView);
				}
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
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity()
								.getWindow()
								.setTitle(
										getActivity()
												.getString(
														R.string.title_beers_from_around_the_world));
					}
				});

				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_BEERS_Q
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_TOP_RATED_BEERS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity().getWindow().setTitle(
								getActivity().getString(
										R.string.title_top_rated_beers));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_TOP_RATED_BEERS_Q
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_WORST_BEERS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity().getWindow().setTitle(
								getActivity().getString(
										R.string.title_worst_beers));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_WORST_BEERS_Q
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_BEERS_BY_COUNTRY)) {
				mTracker.trackEvent("CommunityBeers", option, country, 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity().getWindow().setTitle(
								getActivity().getString(
										R.string.title_beers_by_country));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_BEERS_BY_COUNTRY_Q
						+ AppConfig.COMMUNITY_GET_BEERS_COUNTRY_PARAM
						+ URLEncoder.encode(country, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_BEERS_BY_STATE)) {
				mTracker.trackEvent("CommunityBeers", option, region, 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity().getWindow().setTitle(
								getActivity().getString(
										R.string.title_beers_by_state));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_BEERS_BY_STATES_Q
						+ AppConfig.COMMUNITY_GET_BEERS_COUNTRY_PARAM
						+ URLEncoder.encode(country, "UTF-8")
						+ AppConfig.COMMUNITY_GET_BEERS_STATE_PARAM
						+ URLEncoder.encode(region, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_SEARCH_BEERS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity().getWindow().setTitle(
								getActivity().getString(
										R.string.title_search_results));
					}
				});
				return getSearchUrl(extras);
			} else if (option.equals(AppConfig.COMMUNITY_MY_BEER_REVIEWS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity().getWindow().setTitle(
								getActivity().getString(
										R.string.title_my_beer_reviews));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_MY_BEERS_Q
						+ AppConfig.COMMUNITY_USERID_PARAM
						+ URLEncoder.encode(userId, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option
					.equals(AppConfig.COMMUNITY_MOST_HELPFUL_BEER_REVIEWS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity()
								.getWindow()
								.setTitle(
										getActivity()
												.getString(
														R.string.title_most_helpful_beer_reviews));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_MOST_HELPFUL_BEER_REVIEWS_Q
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_NEW_BEER_REVIEWS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity().getWindow().setTitle(
								getActivity().getString(
										R.string.title_new_beer_reviews));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_NEW_BEER_REVIEWS_Q
						+ AppConfig.COMMUNITY_BEERIDS_PARAM
						+ URLEncoder.encode(beerIds, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity().getWindow().setTitle(
								getActivity().getString(
										R.string.title_favorite_beer_reviews));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_FAVORITE_BEERS_Q
						+ AppConfig.COMMUNITY_USERID_PARAM
						+ URLEncoder.encode(userId, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_BEER_OF_THE_DAY)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity().getWindow().setTitle(
								getActivity().getString(
										R.string.title_beer_of_the_day));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_NEW_BEER_REVIEWS_Q
						+ AppConfig.COMMUNITY_BEERIDS_PARAM
						+ URLEncoder.encode(beerIds, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option
					.equals(AppConfig.COMMUNITY_COMPARABLE_BEER_REVIEWS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity()
								.getWindow()
								.setTitle(
										getActivity()
												.getString(
														R.string.title_comparable_beer_reviews));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_NEW_BEER_REVIEWS_Q
						+ AppConfig.COMMUNITY_BEERIDS_PARAM
						+ URLEncoder.encode(beerIds, "UTF-8")
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option
					.equals(AppConfig.COMMUNITY_RECOMMENDED_BEER_REVIEWS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getActivity()
								.getWindow()
								.setTitle(
										getActivity()
												.getString(
														R.string.title_recommended_beer_reviews));
					}
				});
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

	/************************************************************************************/
	private class BeerListAdapter extends ArrayAdapter<CommunityBeer> {

		private List<CommunityBeer> _mBeers;

		public BeerListAdapter(Context context, int textViewResourceId,
				List<CommunityBeer> beers) {
			super(context, textViewResourceId, beers);
			_mBeers = beers;
			if (Logger.isLogEnabled())
				Logger.log("BeerListAdapter:array size=" + beers.size());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (Logger.isLogEnabled())
				Logger.log("Entering getView:");
			View v = convertView;
			LayoutInflater vi = (LayoutInflater) getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.community_beer_list_row, parent, false);

			CommunityBeer beer = _mBeers.get(position);

			if (beer != null) {
				if (Logger.isLogEnabled())
					Logger.log("getView:" + beer.beer);
				{
					String _urlImage = AppConfig.COMMUNITY_GET_BEERS_URL
							+ AppConfig.COMMUNITY_GET_IMAGE_Q + beer.beerId;
					ImageView thumbnail = ((ImageView) v
							.findViewById(R.id.list_thumbnail));
					if (thumbnail != null) {
						mDrawableManager.fetchDrawableOnThread(_urlImage,
								thumbnail);
					}
				}
				/***********************************************/
				{
					final TextView textView = ((TextView) v
							.findViewById(R.id.text5));
					if (textView != null) {
						// setup review count
						final Handler followCountHandler = new Handler() {
							@Override
							public void handleMessage(Message message) {
								String jsonStr = (String) message.obj;
								if ((jsonStr != null)
										&& (jsonStr.startsWith("{"))) {
									JSONObject json;
									try {
										json = new JSONObject(jsonStr);
										String followers = json
												.getString("followers");
										String following = json
												.getString("following");

										String _reviewCount = "Followers "
												+ followers + " Following "
												+ following;
										if (Logger.isLogEnabled())
											Logger.log(_reviewCount);
										textView.setText(_reviewCount);
									} catch (JSONException e) {
										Log.e(TAG,
												"error: "
														+ ((e.getMessage() != null) ? e
																.getMessage()
																.replace(" ",
																		"_")
																: ""), e);
									}
								}
							}
						};
						mContentManager.fetchContentOnThread(
								Util.getFollowCountUrl(beer.userId),
								followCountHandler);
					}
				}
				/***********************************************/
				{
					String text = (!beer.userId.equals("")) ? (beer.userName)
							: null;
					final TextView textView = ((TextView) v
							.findViewById(R.id.text4));
					if (text != null) {
						if (textView != null) {
							Handler handler = new Handler() {
								@Override
								public void handleMessage(Message message) {
									String jsonStr = (String) message.obj;
									if ((jsonStr != null)
											&& (jsonStr.startsWith("{"))) {
										JSONObject json;
										try {
											json = new JSONObject(jsonStr);
											String userName = json
													.getString("userName");
											String count = json
													.getString("count");
											String _text = userName + " "
													+ count + " reviews";
											if (Logger.isLogEnabled())
												Logger.log("Review Count Text:"
														+ _text);
											textView.setText(_text);
										} catch (JSONException e) {
											Log.e(TAG,
													"error: "
															+ ((e.getMessage() != null) ? e
																	.getMessage()
																	.replace(
																			" ",
																			"_")
																	: ""), e);
										}
									}
								}
							};
							mContentManager.fetchContentOnThread(
									Util.getReviewCountUrl(beer.userId),
									handler);
						}
					}
				}
				/***********************************************/
				// {
				// final TextView textView = ((TextView) v
				// .findViewById(R.id.text3));
				// if (textView != null) {
				// // setup review helpful message
				// final Handler reviewHelpfulCountHandler = new Handler() {
				// @Override
				// public void handleMessage(Message message) {
				// String jsonStr = (String) message.obj;
				// if ((jsonStr != null)
				// && (jsonStr.startsWith("{"))) {
				// JSONObject json;
				// try {
				// json = new JSONObject(jsonStr);
				// int yes = json.getInt("yes");
				// int no = json.getInt("no");
				// String _reviewHelpfulMessage = yes
				// + " out of " + (yes + no)
				// + " found this review helpful";
				// if (Logger.isLogEnabled()) Logger.log(_reviewHelpfulMessage);
				// textView.setText(_reviewHelpfulMessage);
				// } catch (JSONException e) {
				// Log
				// .e(
				// TAG,
				// "error: "
				// + ((e
				// .getMessage() != null) ? e
				// .getMessage()
				// .replace(
				// " ",
				// "_")
				// : ""),
				// e);
				// }
				// } else {
				// textView.setVisibility(View.GONE);
				// }
				// }
				// };
				// mContentManager.fetchContentOnThread(Util
				// .getReviewHelpfulCountUrl(beer.beerId),
				// reviewHelpfulCountHandler);
				// }
				// }
				/***********************************************/
				{
					String text = (!beer.state.equals("")) ? (beer.state + " " + beer.country)
							: beer.country;
					TextView textView = ((TextView) v.findViewById(R.id.text2));
					if (textView != null) {
						textView.setText(text);
					}
				}
				/***********************************************/
				{
					String text = (!beer.alcohol.equals("")) ? (beer.beer + " "
							+ beer.alcohol + "%") : beer.beer;
					TextView textView = ((TextView) v.findViewById(R.id.text1));
					if (textView != null) {
						textView.setText(text);
					}
				}
				/***********************************************/
				{
					RatingBar ratingBar = (RatingBar) v
							.findViewById(R.id.list_ratingbar);
					if (ratingBar != null) {
						ratingBar.setRating(Float.valueOf(beer.rating)
								.floatValue());
					} else {
						if (AppConfig.LOGGING_ENABLED) {
							if (Logger.isLogEnabled())
								Logger.log("Rating Bar is Null!");
						}
					}
				}

			}
			return v;
		}
	}

}
