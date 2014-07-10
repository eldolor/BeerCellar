package com.cm.beer.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class CommunityOptions extends ListActivity {
	String TAG;
	GoogleAnalyticsTracker mTracker;
	int mActiveDialog;
	ProgressDialog mDialog;
	CommunityOptions mMainActivity;

	static final int MENU_GROUP = 0;
	static final int CALL_ID = Menu.FIRST;
	static final int VIEW_MAP_ID = Menu.FIRST + 1;

	protected static final int LOGIN_INTERCEPT_FOR_MY_BEERS_REQUEST_CODE = 0;
	protected static final int LOGIN_INTERCEPT_FOR_FOLLOWING_REQUEST_CODE = 1;
	protected static final int LOGIN_INTERCEPT_FOR_FOLLOWERS_REQUEST_CODE = 2;
	protected static final int LOGIN_INTERCEPT_FOR_FAVORITE_BEERS_REQUEST_CODE = 3;
	protected static final int LOGIN_INTERCEPT_FOR_MY_PROFILE_REQUEST_CODE = 4;
	ImageView mCommunityIcon;
	User mUser;

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
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}
		mUser = new User(this);

		setContentView(R.layout.community_options);
		MenuListAdapter adapter = new MenuListAdapter(mMainActivity,
				R.layout.community_options, AppConfig.COMMUNITY_OPTIONS);
		setListAdapter(adapter);

		setupCommunityIcon();
		Toast.makeText(this, getString(R.string.community_description),
				Toast.LENGTH_LONG).show();

		Util.setGoogleAdSense(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView,
	 * android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int menuPosition, long id) {
		Log.i(TAG, "onListItemClick");

		super.onListItemClick(l, v, menuPosition, id);
		handleMenuItemClicked(menuPosition);
	}

	/**
	 * 
	 * @param menuPosition
	 */
	private void handleMenuItemClicked(int menuPosition) {
		String selectedOption = AppConfig.COMMUNITY_OPTIONS[menuPosition];
		Log.i(TAG, selectedOption);
		// GET USER ID

		Intent intent = null;

		// MATCH BY POSITION IN ARRAY, Starting at 0
		switch (menuPosition) {
		case 0:
			mTracker.trackEvent("CommunityOptions", "MyProfile", "Clicked", 0);
			mTracker.dispatch();
			// If user id does not exist
			if (mUser.isLoggedIn()) {
				intent = new Intent(mMainActivity.getApplication(),
						UserProfile.class);
				intent.putExtra("USERID", mUser.getUserId());
				startActivity(intent);
			} else {
				intent = new Intent(mMainActivity.getApplication(),
						LoginIntercept.class);
				intent.putExtra("FACEBOOK_PERMISSIONS",
						AppConfig.FACEBOOK_PERMISSIONS);
				startActivityForResult(intent,
						LOGIN_INTERCEPT_FOR_MY_PROFILE_REQUEST_CODE);
			}
			return;
		case 1:
			mTracker.trackEvent("CommunityOptions", "BeersFromAroundTheWorld",
					"Clicked", 0);
			mTracker.dispatch();
			intent = new Intent(mMainActivity.getApplication(),
					CommunityBeers.class);
			intent.putExtra("OPTION",
					AppConfig.COMMUNITY_BEERS_FROM_AROUND_THE_WORLD);
			startActivity(intent);
			return;
		case 2:
			mTracker.trackEvent("CommunityOptions", "TopRatedBeers", "Clicked",
					0);
			mTracker.dispatch();
			intent = new Intent(mMainActivity.getApplication(),
					CommunityBeers.class);
			intent.putExtra("OPTION", AppConfig.COMMUNITY_TOP_RATED_BEERS);
			startActivity(intent);
			return;
		case 3:
			mTracker.trackEvent("CommunityOptions", "WorstBeers", "Clicked", 0);
			mTracker.dispatch();
			intent = new Intent(mMainActivity.getApplication(),
					CommunityBeers.class);
			intent.putExtra("OPTION", AppConfig.COMMUNITY_WORST_BEERS);
			startActivity(intent);
			return;
		case 4:
			mTracker
					.trackEvent("CommunityOptions", "SearchBeers", "Clicked", 0);
			mTracker.dispatch();
			intent = new Intent(mMainActivity.getApplication(),
					CommunitySearch.class);
			intent.putExtra("OPTION", AppConfig.COMMUNITY_SEARCH_BEERS);
			startActivity(intent);
			return;
		case 5:
			mTracker.trackEvent("CommunityOptions", "BeersByCountry",
					"Clicked", 0);
			mTracker.dispatch();
			intent = new Intent(mMainActivity.getApplication(),
					CommunityCountries.class);
			intent.putExtra("OPTION", AppConfig.COMMUNITY_BEERS_BY_COUNTRY);
			startActivity(intent);
			return;
		case 6:
			mTracker.trackEvent("CommunityOptions", "BeersByState", "Clicked",
					0);
			mTracker.dispatch();
			intent = new Intent(mMainActivity.getApplication(),
					CommunityStates.class);
			intent.putExtra("OPTION", AppConfig.COMMUNITY_BEERS_BY_STATE);
			startActivity(intent);
			return;
		case 7:
			mTracker.trackEvent("CommunityOptions", "FavoriteBeerReviews",
					"Clicked", 0);
			mTracker.dispatch();
			// If user id does not exist
			if (mUser.isLoggedIn()) {
				getFavoriteBeers(mUser.getUserId());
			} else {
				intent = new Intent(mMainActivity.getApplication(),
						LoginIntercept.class);
				intent.putExtra("FACEBOOK_PERMISSIONS",
						AppConfig.FACEBOOK_PERMISSIONS);
				startActivityForResult(intent,
						LOGIN_INTERCEPT_FOR_FAVORITE_BEERS_REQUEST_CODE);
			}
			return;
		case 8:
			mTracker.trackEvent("CommunityOptions", "MyBeerReviews", "Clicked",
					0);
			mTracker.dispatch();
			// If user id does not exist
			if (mUser.isLoggedIn()) {
				getMyBeers(mUser.getUserId());
			} else {
				intent = new Intent(mMainActivity.getApplication(),
						LoginIntercept.class);
				intent.putExtra("FACEBOOK_PERMISSIONS",
						AppConfig.FACEBOOK_PERMISSIONS);
				startActivityForResult(intent,
						LOGIN_INTERCEPT_FOR_MY_BEERS_REQUEST_CODE);
			}
			return;
		case 9:
			mTracker.trackEvent("CommunityOptions", "Following", "Clicked", 0);
			mTracker.dispatch();
			// If user id does not exist
			if (mUser.isLoggedIn()) {
				intent = new Intent(mMainActivity.getApplication(),
						CommunityFollow.class);
				intent.putExtra("OPTION", AppConfig.COMMUNITY_FOLLOWING);
				startActivity(intent);
			} else {
				intent = new Intent(mMainActivity.getApplication(),
						LoginIntercept.class);
				intent.putExtra("FACEBOOK_PERMISSIONS",
						AppConfig.FACEBOOK_PERMISSIONS);
				startActivityForResult(intent,
						LOGIN_INTERCEPT_FOR_FOLLOWING_REQUEST_CODE);
			}
			return;
		case 10:
			mTracker.trackEvent("CommunityOptions", "Followers", "Clicked", 0);
			mTracker.dispatch();
			// If user id does not exist
			if (mUser.isLoggedIn()) {
				intent = new Intent(mMainActivity.getApplication(),
						CommunityFollow.class);
				intent.putExtra("OPTION", AppConfig.COMMUNITY_FOLLOWERS);
				startActivity(intent);
			} else {
				intent = new Intent(mMainActivity.getApplication(),
						LoginIntercept.class);
				intent.putExtra("FACEBOOK_PERMISSIONS",
						AppConfig.FACEBOOK_PERMISSIONS);
				startActivityForResult(intent,
						LOGIN_INTERCEPT_FOR_FOLLOWERS_REQUEST_CODE);
			}
			return;
		}
	}

	private void setupCommunityIcon() {
		mCommunityIcon = (ImageView) findViewById(R.id.community_icon);
		mCommunityIcon.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				Log.i(TAG, "back to community options menu");

				AlertDialog.Builder dialog = new AlertDialog.Builder(
						mMainActivity);
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.setTitle(R.string.community_progress_dialog_title);
				dialog.setMessage(R.string.back_to_main_menu);
				dialog.setPositiveButton(R.string.yes_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Intent intent = new Intent(mMainActivity
										.getApplication(), Main.class);
								intent
										.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
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

	}

	private void getMyBeers(String userId) {
		Log.i(TAG, "getMyBeers");
		Intent intent = new Intent(mMainActivity.getApplication(),
				CommunityBeers.class);
		intent.putExtra("OPTION", AppConfig.COMMUNITY_MY_BEER_REVIEWS);
		intent.putExtra("USERID", userId);
		startActivity(intent);

	}

	private void getFavoriteBeers(String userId) {
		Log.i(TAG, "getFavoriteBeers");
		Intent intent = new Intent(mMainActivity.getApplication(),
				CommunityBeers.class);
		intent.putExtra("OPTION", AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS);
		intent.putExtra("USERID", userId);
		startActivity(intent);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_INTERCEPT_FOR_MY_BEERS_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Log.i(TAG, "onActivityResult:" + mUser.getUserId());
				getMyBeers(mUser.getUserId());
			}
		} else if (requestCode == LOGIN_INTERCEPT_FOR_FOLLOWING_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Intent intent = new Intent(mMainActivity.getApplication(),
						CommunityFollow.class);
				intent.putExtra("OPTION", AppConfig.COMMUNITY_FOLLOWING);
				startActivity(intent);
			}
		} else if (requestCode == LOGIN_INTERCEPT_FOR_FOLLOWERS_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Intent intent = new Intent(mMainActivity.getApplication(),
						CommunityFollow.class);
				intent.putExtra("OPTION", AppConfig.COMMUNITY_FOLLOWERS);
				startActivity(intent);
			}
		} else if (requestCode == LOGIN_INTERCEPT_FOR_FAVORITE_BEERS_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Log.i(TAG, "onActivityResult:" + mUser.getUserId());
				getFavoriteBeers(mUser.getUserId());
			}
		} else if (requestCode == LOGIN_INTERCEPT_FOR_MY_PROFILE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Intent intent = new Intent(mMainActivity.getApplication(),
						UserProfile.class);
				intent.putExtra("USERID", mUser.getUserId());
				startActivity(intent);

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
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreateDialog");
		}
		String dialogMessage = null;
		String dialogTitle = null;
		if (id == AppConfig.DIALOG_LOADING_ID) {
			dialogMessage = this.getString(R.string.progress_loading_message);
			mActiveDialog = AppConfig.DIALOG_LOADING_ID;
			dialogTitle = getString(R.string.around_me_progress_dialog_title);
		}
		mDialog = ProgressDialog.show(this, dialogTitle, dialogMessage, true,
				true);
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
	}

	/**************************************************************************/
	private class MenuListAdapter extends ArrayAdapter<String> {

		private String[] items;

		public MenuListAdapter(Context context, int textViewResourceId,
				String[] items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.community_option, null);
			}
			String o = items[position];
			if (o != null) {
				TextView tt = (TextView) v.findViewById(R.id.community_option);
				if (tt != null) {
					tt.setText(o);
				}
			}
			return v;
		}
	}

}
