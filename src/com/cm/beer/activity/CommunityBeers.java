package com.cm.beer.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.transfer.CommunityBeer;
import com.cm.beer.util.ContentManager;
import com.cm.beer.util.DrawableManager;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class CommunityBeers extends ListActivity {
	String TAG;
	GoogleAnalyticsTracker mTracker;
	int mActiveDialog;
	ProgressDialog mDialog;
	ProgressDialog mSplashDialog;
	CommunityBeers mMainActivity;
	List<CommunityBeer> mBeers = new ArrayList<CommunityBeer>();
	// NOTE: mCs cannot be null
	String mCs = "";

	static final int MENU_GROUP = 0;
	static final int ABOUT_THIS_BEER_ID = Menu.FIRST;
	static final int SHOW_LOCATION_ID = Menu.FIRST + 1;

	SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM d, yyyy");

	Bundle mExtras;

	BeerListAdapter mAdapter;

	ListView mBeersView;

	View mFooterView;
	DrawableManager mDrawableManager;
	ContentManager mContentManager;

	ImageView mCommunityIcon;

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
		mExtras = getIntent().getExtras();

		// Start a new thread that will download all the data
		Boolean _refreshList = new Boolean(false);
		new AsyncGetCommunityBeers().execute(mExtras, _refreshList);

		mSplashDialog = ProgressDialog.show(this,
				getString(R.string.community_progress_dialog_title),
				getString(R.string.community_progress_searching_message), true,
				true);

		// initialize Footer View for the list
		initFooterView();

		mDrawableManager = DrawableManager.getInstance();
		mContentManager = ContentManager.getInstance();

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
		Log.i(TAG, "onCreateContextMenu");
		int menuPosition = 0;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		CommunityBeer beer = mBeers.get(info.position);
		Log.i(TAG, "onCreateContextMenu:" + beer.beer + ":" + beer.latitude
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
			Log.i(TAG, "onContextItemSelected");
		}
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		CommunityBeer beer = mBeers.get(info.position);
		Log.i(TAG, "onContextItemSelected:" + beer.beer + "::" + beer.style);
		switch (item.getItemId()) {
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

	/**
	 * 
	 * @param rowId
	 */
	private void aboutThisBeer(String beer, String style) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "aboutThisBeer:" + beer);
		}
		String _beerStyleUri = style.replace(" ", "_");
		String _url = AppConfig.WIKIPEDIA_REF_URL + _beerStyleUri;

		mTracker.trackEvent("CommunityBeers", "MoreAboutThisBeer",
				_beerStyleUri, 0);
		mTracker.dispatch();

		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "aboutThisBeer:URL:" + _url);
		}
		openBrowser(_url, beer);
	}

	/**
	 * 
	 * @param url
	 * @param beer
	 */
	private void openBrowser(String url, String beer) {
		Intent intent = new Intent(mMainActivity.getApplication(),
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
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"
				+ beer.latitude + "," + beer.longitude + "?z="
				+ AppConfig.GOOGLE_MAPS_ZOOM_LEVEL));
		startActivity(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView,
	 * android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onListItemClick");
		}

		super.onListItemClick(l, v, position, id);
		CommunityBeer _beer = mBeers.get((int) id);

		String _selection = _beer.beer.replace(" ", "_") + "," + _beer.beerId;
		mTracker.trackEvent("CommunityBeers", "Selection", _selection, 0);
		mTracker.dispatch();
		Intent intent = new Intent(mMainActivity.getApplication(),
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
	protected void onDestroy() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onDestroy");
		}
		// Stop the mTracker when it is no longer needed.
		mTracker.stop();
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Stopped!");
		}
		mContentManager.clear();
		mDrawableManager.clear();
		super.onDestroy();
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
		String mDialogMessage = null;
		String mDialogTitle = null;
		if (id == AppConfig.DIALOG_LOADING_ID) {
			mDialogMessage = this.getString(R.string.progress_loading_message);
			mActiveDialog = AppConfig.DIALOG_LOADING_ID;
			mDialogTitle = getString(R.string.around_me_progress_dialog_title);
		}
		mDialog = ProgressDialog.show(this, mDialogTitle, mDialogMessage, true,
				true);
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
	}

	private void initFooterView() {
		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

	/**
	 * 
	 */
	private void displayList() {
		setContentView(R.layout.community_beer_list);
		registerForContextMenu(getListView());
		getListView().setTextFilterEnabled(true);
		mTracker.trackPageView("CommunityBeers");
		mTracker.dispatch();

		Util.setGoogleAdSense(this);

		mCommunityIcon = (ImageView) findViewById(R.id.community_icon);
		mCommunityIcon.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				Log.i(TAG, "back to community options menu");

				AlertDialog.Builder dialog = new AlertDialog.Builder(
						mMainActivity);
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.setTitle(R.string.community_progress_dialog_title);
				dialog.setMessage(R.string.back_to_community_menu);
				dialog.setPositiveButton(R.string.yes_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Intent intent = new Intent(mMainActivity
										.getApplication(),
										CommunityOptions.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
								mMainActivity.finish();
							}
						});
				dialog.setNegativeButton(R.string.no_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				dialog.create();
				dialog.show();
			}
		});

		if (mBeers != null) {
			mBeersView = getListView();
			// Call this before calling setAdapter
			if ((mBeers.size() >= Integer.valueOf(AppConfig.COMMUNITY_R_VALUE))
					&& (!mCs.equals(""))) {
				mBeersView.addFooterView(mFooterView);
			}

			mBeersView.setTextFilterEnabled(true);

			mAdapter = new BeerListAdapter(this, R.layout.community_beer_list,
					mBeers);
			setListAdapter(mAdapter);

			Toast.makeText(mMainActivity, R.string.hint_community_page,
					Toast.LENGTH_LONG).show();

		}
		if ((mBeers != null) && (mBeers.size() == 0)) {
			TextView noBeers = (TextView) findViewById(android.R.id.empty);
			noBeers.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// Start a new thread that will download all the
					// data
					Boolean _refreshList = new Boolean(false);
					new AsyncGetCommunityBeers().execute(mExtras, _refreshList);

					mSplashDialog = ProgressDialog
							.show(
									mMainActivity,
									getString(R.string.community_progress_dialog_title),
									getString(R.string.community_progress_searching_message),
									true, true);
				}
			});
		}

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
				Log.i(TAG, "doInBackground starting");
			}
			Bundle extras = (Bundle) args[0];
			_mRefreshList = (Boolean) args[1];
			JSONArray beersJSONArray = null;
			try {

				String _url = getUrl(extras);
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

						mMainActivity.mBeers.add(_beer);
						Log.i(TAG, _beer.beerId + "::" + _beer.beer + "::BL::"
								+ _beer.breweryLink);
					}
				} else {
					_retrievedMoreData = false;
				}
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeers", "DownloadError", ((e
						.getMessage() != null) ? e.getMessage().replace(" ",
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

			Log.i(TAG, "onPostExecute Refresh List = "
					+ ((_mRefreshList) ? "true" : "false")
					+ " Retrieved More Data = "
					+ ((_retrievedMoreData) ? "true" : "false"));
			if (!_mRefreshList) {
				Log.i(TAG, "onPostExecute Display List");
				mMainActivity.displayList();
			} else if (_mRefreshList && _retrievedMoreData) {
				Log.i(TAG, "onPostExecute Refresh List");
				mAdapter.notifyDataSetChanged();
				// make spinner invisible
				((ProgressBar) mFooterView.findViewById(R.id.spinner))
						.setVisibility(View.INVISIBLE);
			} else {
				Log.i(TAG, "onPostExecute Remove Footer View!");
				mBeersView.removeFooterView(mFooterView);
			}
			if (mMainActivity.mSplashDialog != null) {
				mMainActivity.mSplashDialog.cancel();
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
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity
								.getWindow()
								.setTitle(
										mMainActivity
												.getString(R.string.title_beers_from_around_the_world));
					}
				});

				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_BEERS_Q
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_TOP_RATED_BEERS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity
								.getWindow()
								.setTitle(
										mMainActivity
												.getString(R.string.title_top_rated_beers));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_TOP_RATED_BEERS_Q
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_WORST_BEERS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity.getWindow().setTitle(
								mMainActivity
										.getString(R.string.title_worst_beers));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_WORST_BEERS_Q
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_BEERS_BY_COUNTRY)) {
				mTracker.trackEvent("CommunityBeers", option, country, 0);
				mTracker.dispatch();
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity
								.getWindow()
								.setTitle(
										mMainActivity
												.getString(R.string.title_beers_by_country));
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
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity
								.getWindow()
								.setTitle(
										mMainActivity
												.getString(R.string.title_beers_by_state));
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
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity
								.getWindow()
								.setTitle(
										mMainActivity
												.getString(R.string.title_search_results));
					}
				});
				return getSearchUrl(extras);
			} else if (option.equals(AppConfig.COMMUNITY_MY_BEER_REVIEWS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity
								.getWindow()
								.setTitle(
										mMainActivity
												.getString(R.string.title_my_beer_reviews));
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
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity
								.getWindow()
								.setTitle(
										mMainActivity
												.getString(R.string.title_most_helpful_beer_reviews));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_MOST_HELPFUL_BEER_REVIEWS_Q
						+ AppConfig.COMMUNITY_R
						+ AppConfig.COMMUNITY_GET_BEERS_CS_PARAM + mCs;
			} else if (option.equals(AppConfig.COMMUNITY_NEW_BEER_REVIEWS)) {
				mTracker.trackEvent("CommunityBeers", option, "Clicked", 0);
				mTracker.dispatch();
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity
								.getWindow()
								.setTitle(
										mMainActivity
												.getString(R.string.title_new_beer_reviews));
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
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity
								.getWindow()
								.setTitle(
										mMainActivity
												.getString(R.string.title_favorite_beer_reviews));
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
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity
								.getWindow()
								.setTitle(
										mMainActivity
												.getString(R.string.title_beer_of_the_day));
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

	/************************************************************************************/
	private class BeerListAdapter extends ArrayAdapter<CommunityBeer> {

		private List<CommunityBeer> _mBeers;

		public BeerListAdapter(Context context, int textViewResourceId,
				List<CommunityBeer> beers) {
			super(context, textViewResourceId, beers);
			this._mBeers = beers;
			Log.i(TAG, "BeerListAdapter:array size=" + beers.size());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.i(TAG, "Entering getView:");
			View v = convertView;
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.community_beer_list_row, parent, false);

			CommunityBeer beer = _mBeers.get(position);

			if (beer != null) {
				Log.i(TAG, "getView:" + beer.beer);
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
										Log.d(TAG, _reviewCount);
										textView.setText(_reviewCount);
									} catch (JSONException e) {
										Log
												.e(
														TAG,
														"error: "
																+ ((e
																		.getMessage() != null) ? e
																		.getMessage()
																		.replace(
																				" ",
																				"_")
																		: ""),
														e);
									}
								}
							}
						};
						mContentManager.fetchContentOnThread(Util
								.getFollowCountUrl(beer.userId),
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
											Log.d(TAG, "Review Count Text:"
													+ _text);
											textView.setText(_text);
										} catch (JSONException e) {
											Log
													.e(
															TAG,
															"error: "
																	+ ((e
																			.getMessage() != null) ? e
																			.getMessage()
																			.replace(
																					" ",
																					"_")
																			: ""),
															e);
										}
									}
								}
							};
							mContentManager.fetchContentOnThread(Util
									.getReviewCountUrl(beer.userId), handler);
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
				// Log.d(TAG, _reviewHelpfulMessage);
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
							Log.i(TAG, "Rating Bar is Null!");
						}
					}
				}

			}
			return v;
		}
	}

}
