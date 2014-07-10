package com.cm.beer.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.GpsLocation;
import com.cm.beer.util.Logger;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AroundMe extends ListActivity {
	String TAG;
	GoogleAnalyticsTracker mTracker;
	int ACTIVE_DIALOG;
	ProgressDialog mDialog;
	ProgressDialog mSplashDialog;
	AroundMe mMainActivity;
	Business[] BUSINESSES = null;

	static final int MENU_GROUP = 0;
	static final int CALL_ID = Menu.FIRST;
	static final int VIEW_MAP_ID = Menu.FIRST + 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate");
		}
		mMainActivity = this;

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Instantiated");
		}

		// Start a new thread that will download all the data
		new AsyncDownloadTask().execute("");

		mSplashDialog = ProgressDialog.show(this,
				getString(R.string.around_me_progress_dialog_title),
				getString(R.string.around_me_progress_searching_message), true,
				true);

		Util.setGoogleAdSense(this);
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
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreateContextMenu");
		}
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(MENU_GROUP, CALL_ID, 0, R.string.call);
		menu.add(MENU_GROUP, VIEW_MAP_ID, 1, R.string.view_map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onContextItemSelected");
		}
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case CALL_ID:
			mTracker.trackEvent("AroundMe", "Call", "Clicked", 0);
			mTracker.dispatch();
			call(info.id);
			return true;
		case VIEW_MAP_ID:
			mTracker.trackEvent("AroundMe", "ViewMap", "Clicked", 0);
			mTracker.dispatch();
			viewMap(info.id);
			return true;
		}
		return super.onContextItemSelected(item);
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
			if (Logger.isLogEnabled())  Logger.log("onListItemClick");
		}
		super.onListItemClick(l, v, position, id);
		Business business = BUSINESSES[(int) id];
		String _selection = (business.name + " " + business.address1 + ", "
				+ business.city + " " + business.state).replace(" ", "_");
		mTracker.trackEvent("AroundMe", "Selection", _selection, 0);
		mTracker.dispatch();
		Intent intent = new Intent(mMainActivity.getApplication(),
				BeerWebView.class);
		intent.putExtra("URL", business.mobileUrl);
		intent.putExtra("TITLE", business.name);
		startActivity(intent);
	}

	/**
	 * 
	 * @param id
	 */
	private void call(long id) {
		Business business = BUSINESSES[(int) id];
		String _selection = (business.name + " " + business.address1 + ", "
				+ business.city + " " + business.state).replace(" ", "_");
		mTracker.trackEvent("AroundMe", "Call", _selection, 0);
		mTracker.dispatch();
		Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel: "
				+ business.phone));
		startActivity(i);
	}

	/**
	 * 
	 * @param id
	 */
	private void viewMap(long id) {
		Business business = BUSINESSES[(int) id];
		String _selection = (business.name + " " + business.address1 + ", "
				+ business.city + " " + business.state).replace(" ", "_");
		mTracker.trackEvent("AroundMe", "ViewMap", _selection, 0);
		mTracker.dispatch();
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"
				+ business.latitude + "," + business.longitude + "?q="
				+ business.name + " " + business.address1 + ", "
				+ business.city + " " + business.state));
		startActivity(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onDestroy");
		}
		// Stop the tracker when it is no longer needed.
		mTracker.stop();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Stopped!");
		}
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
			if (Logger.isLogEnabled())  Logger.log("onCreateDialog");
		}
		String dialogMessage = null;
		String dialogTitle = null;
		if (id == AppConfig.DIALOG_LOADING_ID) {
			dialogMessage = this.getString(R.string.progress_loading_message);
			ACTIVE_DIALOG = AppConfig.DIALOG_LOADING_ID;
			dialogTitle = getString(R.string.around_me_progress_dialog_title);
		}
		mDialog = ProgressDialog.show(this, dialogTitle, dialogMessage, true,
				true);
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
	}

	/**
	 * 
	 */
	void displayList() {
		setContentView(R.layout.business_list);
		registerForContextMenu(getListView());
		getListView().setTextFilterEnabled(true);
		mTracker.trackPageView("AroundMe");
		mTracker.dispatch();

		if (BUSINESSES != null) {
			BusinessListAdapter adapter = new BusinessListAdapter(this,
					R.layout.business_list, BUSINESSES);
			setListAdapter(adapter);

			ListView businessesView = getListView();
			businessesView.setTextFilterEnabled(true);

		}
		ImageView poweredByLogo = ((ImageView) findViewById(R.id.powered_by_logo));
		poweredByLogo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(mMainActivity.getApplication(),
						BeerWebView.class);
				intent.putExtra("URL", AppConfig.YELP_LOGO_URL);
				startActivity(intent);
			}
		});

		Toast.makeText(mMainActivity, R.string.hint_around_me_page,
				Toast.LENGTH_LONG).show();

	}

	/************************************************************************************/
	private class AsyncDownloadTask extends AsyncTask<String, Void, Object> {

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(String... args) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("doInBackground starting");
			}

			JSONObject locations = null;

			if (mMainActivity.BUSINESSES == null) {
				try {
					String response[] = Util.getResult(getUrl());

					if ((response[0] != null) && (response[0].startsWith("{"))) {
						locations = new JSONObject(response[0]);
					}

					if (locations != null) {
						JSONArray businesses = locations
								.getJSONArray("businesses");
						JSONObject _businessJson = null;
						Business business = null;
						int businessesLength = businesses.length();
						if (mMainActivity.BUSINESSES == null) {
							mMainActivity.BUSINESSES = new Business[businessesLength];
						}

						for (int i = 0, j = 0; i < businessesLength; i++) {
							_businessJson = businesses.getJSONObject(i);
							if (!_businessJson.getBoolean("is_closed")) {
								business = new Business();
								business.id = j;
								business.address1 = _businessJson
										.getString("address1");
								business.address2 = _businessJson
										.getString("address2");
								business.address3 = _businessJson
										.getString("address3");
								business.city = _businessJson.getString("city");
								business.distance = _businessJson
										.getString("distance");
								business.name = _businessJson.getString("name");
								business.phone = _businessJson
										.getString("phone");
								business.photoUrlSmall = _businessJson
										.getString("photo_url_small");
								business.ratingImgUrlSmall = _businessJson
										.getString("rating_img_url_small");
								business.reviewCount = _businessJson
										.getString("review_count");
								business.state = _businessJson
										.getString("state");
								business.mobileUrl = _businessJson
										.getString("mobile_url");
								business.avgRating = _businessJson
										.getString("avg_rating");
								business.latitude = _businessJson
										.getString("latitude");
								business.longitude = _businessJson
										.getString("longitude");

								mMainActivity.BUSINESSES[j++] = business;
								if (Logger.isLogEnabled())  Logger.log(business.name + "::"
										+ business.address1 + "::" + "::"
										+ business.city + "::"
										+ business.avgRating);
							}

						}
					}
				} catch (Throwable e) {
					Log.e(TAG, "error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);
					mTracker.trackEvent("AroundMe", "DownloadError", ((e
							.getMessage() != null) ? e.getMessage().replace(
							" ", "_") : ""), 0);
					mTracker.dispatch();
				}
			}

			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("doInBackground finished");
			}
			return null;
		}

		protected void onPostExecute(Object result) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("onPostExecute starting");
			}

			if (mMainActivity.mSplashDialog != null) {
				mMainActivity.mSplashDialog.cancel();
				mMainActivity.displayList();
			}
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("onPostExecute finished");
			}
		}

		private JSONObject getLocations2() {
			try {
				return new JSONObject(
						"{\"message\": {\"text\": \"OK\", \"code\": 0, \"version\": \"1.1.1\"}, \"businesses\": [{\"rating_img_url\": \"http://media1.px.yelpcdn.com/static/200911304084228337/i/ico/stars/stars_4.png\", \"country_code\": \"US\", \"id\": \"1V3uTBVaULGRZfUglgjOUA\", \"is_closed\": false, \"city\": \"San Francisco\", \"mobile_url\": \"http://mobile.yelp.com/biz/1V3uTBVaULGRZfUglgjOUA\", \"review_count\": 65, \"zip\": \"94123\", \"state\": \"CA\", \"latitude\": 37.800925999999997, \"rating_img_url_small\": \"http://media3.px.yelpcdn.com/static/20091130418129184/i/ico/stars/stars_small_4.png\", \"address1\": \"1602 Lombard St\", \"address2\": \"\", \"address3\": \"\", \"phone\": \"4156734656\", \"state_code\": \"CA\", \"categories\": [{\"category_filter\": \"bakeries\", \"search_url\": \"http://www.yelp.com/search?find_loc=1602+Lombard+St%2C+San+Francisco+94123&cflt=bakeries\", \"name\": \"Bakeries\"}, {\"category_filter\": \"breakfast_brunch\", \"search_url\": \"http://www.yelp.com/search?find_loc=1602+Lombard+St%2C+San+Francisco+94123&cflt=breakfast_brunch\", \"name\": \"Breakfast & Brunch\"}], \"photo_url\": \"http://media1.px.yelpcdn.com/bpthumb/h9GOBCEz1n6dvUTW0LIS9Q/ms\", \"distance\": 1.7822902202606201, \"name\": \"Cafe GoLo\", \"neighborhoods\": [{\"url\": \"http://www.yelp.com/search?find_loc=Marina%2FCow+Hollow%2C+San+Francisco%2C+CA\", \"name\": \"Marina/Cow Hollow\"}], \"url\": \"http://www.yelp.com/biz/cafe-golo-san-francisco\", \"country\": \"USA\", \"avg_rating\": 4.0, \"longitude\": -122.428049, \"photo_url_small\": \"http://media1.px.yelpcdn.com/bpthumb/h9GOBCEz1n6dvUTW0LIS9Q/ss\", \"reviews\": [{\"rating_img_url_small\": \"http://media2.px.yelpcdn.com/static/200911302337205794/i/ico/stars/stars_small_3.png\", \"user_photo_url_small\": \"http://media1.px.yelpcdn.com/upthumb/ez3bhOTxcRr_k68RGjtFuQ/ss\", \"rating_img_url\": \"http://media4.px.yelpcdn.com/static/200911301694360749/i/ico/stars/stars_3.png\", \"rating\": 3, \"mobile_uri\": \"http://mobile.yelp.com/biz/1V3uTBVaULGRZfUglgjOUA?srid=FdYsSIZfjj1XXMQ7CgMnPQ\", \"url\": \"http://www.yelp.com/biz/cafe-golo-san-francisco#hrid:FdYsSIZfjj1XXMQ7CgMnPQ\", \"user_url\": \"http://www.yelp.com/user_details?userid=DCK1ZYOio-Qed16nKUNMZw\", \"text_excerpt\": \"Good and tasty place. Ordered the egg sandwich, and they gave me a tomato when i asked for it, which was nice. it was kinda tiny though, and looked a little...\", \"user_photo_url\": \"http://media1.px.yelpcdn.com/upthumb/ez3bhOTxcRr_k68RGjtFuQ/ms\", \"date\": \"2010-08-01\", \"user_name\": \"Phil J.\", \"id\": \"FdYsSIZfjj1XXMQ7CgMnPQ\"}, {\"rating_img_url_small\": \"http://media4.px.yelpcdn.com/static/200911301949604803/i/ico/stars/stars_small_5.png\", \"user_photo_url_small\": \"http://media2.px.yelpcdn.com/static/200911302819681786/i/gfx/blank_user_extra_small.gif\", \"rating_img_url\": \"http://media2.px.yelpcdn.com/static/200911302578611207/i/ico/stars/stars_5.png\", \"rating\": 5, \"mobile_uri\": \"http://mobile.yelp.com/biz/1V3uTBVaULGRZfUglgjOUA?srid=FMngX0ghn41A54o2DSrTkA\", \"url\": \"http://www.yelp.com/biz/cafe-golo-san-francisco#hrid:FMngX0ghn41A54o2DSrTkA\", \"user_url\": \"http://www.yelp.com/user_details?userid=Vv7VX0ewSZ1iCsgMqXhIsQ\", \"text_excerpt\": \"Ok, I'll skip the mini-novel re. the stuff going on here other than the food. Bc, call me crazy, but I think the point of yelp is to rate the food and not...\", \"user_photo_url\": \"http://media1.px.yelpcdn.com/static/200911301186834854/i/gfx/blank_user_small.gif\", \"date\": \"2010-07-31\", \"user_name\": \"RJ B.\", \"id\": \"FMngX0ghn41A54o2DSrTkA\"}, {\"rating_img_url_small\": \"http://media4.px.yelpcdn.com/static/200911301949604803/i/ico/stars/stars_small_5.png\", \"user_photo_url_small\": \"http://media1.px.yelpcdn.com/upthumb/HU3OuX8UwtLhYqYqLmUzZw/ss\", \"rating_img_url\": \"http://media2.px.yelpcdn.com/static/200911302578611207/i/ico/stars/stars_5.png\", \"rating\": 5, \"mobile_uri\": \"http://mobile.yelp.com/biz/1V3uTBVaULGRZfUglgjOUA?srid=waUyTGh9QAcfi1snuGdfZw\", \"url\": \"http://www.yelp.com/biz/cafe-golo-san-francisco#hrid:waUyTGh9QAcfi1snuGdfZw\", \"user_url\": \"http://www.yelp.com/user_details?userid=CEvAX3f8TWrWKfqgwsLD2A\", \"text_excerpt\": \"This place is easy to pass if you're not looking for it. It's a very, very small restaurant with about 10 tables or so. But make sure you find it because...\", \"user_photo_url\": \"http://media1.px.yelpcdn.com/upthumb/HU3OuX8UwtLhYqYqLmUzZw/ms\", \"date\": \"2010-07-13\", \"user_name\": \"Holli H.\", \"id\": \"waUyTGh9QAcfi1snuGdfZw\"}], \"nearby_url\": \"http://www.yelp.com/search?find_loc=1602+Lombard+St%2C+San+Francisco+94123\"}]}");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * Builds the Yelp Url
		 * 
		 * @return
		 */
		private String getUrl() {
			// GET LAT & LONG
			GpsLocation gpsLocation = Util.getLocation(mMainActivity, mTracker);

			StringBuilder sb = new StringBuilder();
			sb.append(AppConfig.YELP_BASE_URL);
			sb.append(AppConfig.YELP_LAT);
			sb.append(gpsLocation.latitude);
			sb.append(AppConfig.YELP_LONG);
			sb.append(gpsLocation.longitude);
			sb.append(AppConfig.YELP_LIMIT);
			sb.append(AppConfig.YELP_RADIUS);
			sb.append(AppConfig.YELP_CATEGORY);
			sb.append(AppConfig.YELP_YWSID);

			return sb.toString();
		}
	}

	/************************************************************************************/
	private class BusinessListAdapter extends ArrayAdapter<Business> {

		private Business[] businesses;

		public BusinessListAdapter(Context context, int textViewResourceId,
				Business[] businesses) {
			super(context, textViewResourceId, businesses);
			this.businesses = businesses;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.business_list_row, parent, false);
			}
			Business business = businesses[position];
			if (business != null) {
				TextView textView1 = ((TextView) v
						.findViewById(R.id.business_address));
				if (textView1 != null) {
					textView1.setText(business.address1 + ", " + business.city
							+ ", " + business.state + " "
							+ PhoneNumberUtils.formatNumber(business.phone));
				}
				TextView textView2 = ((TextView) v
						.findViewById(R.id.business_name));
				if (textView2 != null) {
					textView2.setText(business.name);
				}
				RatingBar ratingBar = ((RatingBar) v
						.findViewById(R.id.business_ratingbar));
				if (ratingBar != null) {
					ratingBar.setRating(Float.valueOf(business.avgRating)
							.floatValue());
				}
			}
			return v;
		}

	}

	/************************************************************************************/
	private class Business {
		public long id;
		public String name, address1, address2, address3, city, state, phone,
				reviewCount, ratingImgUrlSmall, photoUrlSmall, distance,
				mobileUrl, avgRating, latitude, longitude;
	}

}
