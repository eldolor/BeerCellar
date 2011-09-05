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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.transfer.ReviewCount;
import com.cm.beer.util.ContentManager;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class CommunityFollow extends ListActivity {
	String TAG;
	GoogleAnalyticsTracker mTracker;
	int mActiveDialog;
	ProgressDialog mDialog;
	ProgressDialog mSplashDialog;
	CommunityFollow mMainActivity;
	List<ReviewCount> mReviewCounts = new ArrayList<ReviewCount>();
	// NOTE: mCs cannot be null
	String mCs = "";

	SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM d, yyyy");

	Bundle mExtras;

	ReviewCountAdapter mAdapter;

	ListView mReviewCountsView;

	View mFooterView;
	ContentManager mContentManager;

	ImageView mCommunityIcon;

	String mUserId;

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
		Bundle extras = getIntent().getExtras();
		mUserId = extras != null ? extras.getString("USERID") : null;
		if (mUserId == null) {
			User user = new User(this);
			mUserId = user.getUserId();
		}
		mExtras = getIntent().getExtras();

		// Start a new thread that will download all the data
		Boolean _refreshList = new Boolean(false);
		new AsyncGetFollow().execute(mExtras, _refreshList);

		mSplashDialog = ProgressDialog.show(this,
				getString(R.string.community_progress_dialog_title),
				getString(R.string.community_progress_searching_message), true,
				true);

		// initialize Footer View for the list
		initFooterView();

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
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView,
	 * android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onListItemClick");
		}

		super.onListItemClick(l, v, position, id);
		ReviewCount _reviewCount = mReviewCounts.get((int) id);

		// Intent intent = new Intent(mMainActivity.getApplication(),
		// CommunityBeers.class);
		// intent.putExtra("OPTION", AppConfig.COMMUNITY_SEARCH_BEERS);
		// intent.putExtra(NotesDbAdapter.KEY_USER_ID,
		// _reviewCount.getUserId());
		// startActivity(intent);

		Intent intent = new Intent(mMainActivity.getApplication(),
				UserProfile.class);
		intent.putExtra("USERID", _reviewCount.getUserId());
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
				new AsyncGetFollow().execute(mExtras, _refreshList);
			}
		});
	}

	/**
	 * 
	 */
	private void displayList() {
		setContentView(R.layout.follow_list);
		registerForContextMenu(getListView());
		getListView().setTextFilterEnabled(true);
		mTracker.trackPageView("CommunityFollow");
		mTracker.dispatch();

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

		if (mReviewCounts != null) {
			mReviewCountsView = getListView();
			// Call this before calling setAdapter
			if (mReviewCounts.size() >= Integer
					.valueOf(AppConfig.COMMUNITY_R_VALUE)) {
				mReviewCountsView.addFooterView(mFooterView);
			}

			mReviewCountsView.setTextFilterEnabled(true);

			mAdapter = new ReviewCountAdapter(this, R.layout.follow_list,
					mReviewCounts);
			setListAdapter(mAdapter);

		}
		if ((mReviewCounts != null) && (mReviewCounts.size() == 0)) {
			TextView noFollow = (TextView) findViewById(android.R.id.empty);
			String option = mExtras.getString("OPTION");
			if (option.equals(AppConfig.COMMUNITY_FOLLOWING)) {
				noFollow
						.setText(getString(R.string.community_not_following_anyone_message));
			} else if (option.equals(AppConfig.COMMUNITY_FOLLOWERS)) {
				noFollow
						.setText(getString(R.string.community_no_followers_message));
			}
		}

	}

	/************************************************************************************/
	private class AsyncGetFollow extends AsyncTask<Object, Void, Object> {

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
			JSONArray _JSONArray = null;
			try {

				String _url = getUrl(extras);
				Log.i(TAG, "doInBackground:URL=" + _url);
				String response[] = Util.getResult(_url);
				if ((response[0] != null) && (response[0].startsWith("["))) {
					_JSONArray = new JSONArray(response[0]);
					mCs = response[1];
				}

				if (_JSONArray != null) {
					int _arrayLength = _JSONArray.length();
					if (_arrayLength > 0) {
						_retrievedMoreData = true;
					} else {
						_retrievedMoreData = false;
					}
					for (int i = 0; i < _arrayLength; i++) {
						JSONObject _jsonObj = _JSONArray.getJSONObject(i);
						ReviewCount _reviewCount = new ReviewCount();
						_reviewCount.setCount(_jsonObj.getInt("count"));
						_reviewCount.setUserId(_jsonObj.getString("userId"));
						_reviewCount.setUserIdIdx(_jsonObj
								.getString("userIdIdx"));
						_reviewCount
								.setUserLink(_jsonObj.getString("userLink"));
						_reviewCount
								.setUserName(_jsonObj.getString("userName"));
						_reviewCount.setUserNameIdx(_jsonObj
								.getString("userNameIdx"));

						mMainActivity.mReviewCounts.add(_reviewCount);
						Log.i(TAG, _reviewCount.getUserName());
					}
				} else {
					_retrievedMoreData = false;
				}
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityFollow", "DownloadError", ((e
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
				mReviewCountsView.removeFooterView(mFooterView);
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
			String option = extras.getString("OPTION");
			if (option.equals(AppConfig.COMMUNITY_FOLLOWING)) {
				mTracker.trackEvent("CommunityFollow", option, "Clicked", 0);
				mTracker.dispatch();
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity.getWindow().setTitle(
								mMainActivity
										.getString(R.string.title_following));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_FOLLOWING_Q
						+ ((mUserId != null) ? URLEncoder.encode(mUserId,
								"UTF-8") : "") + AppConfig.COMMUNITY_R;
			} else if (option.equals(AppConfig.COMMUNITY_FOLLOWERS)) {
				mTracker.trackEvent("CommunityFollow", option, "Clicked", 0);
				mTracker.dispatch();
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity.getWindow().setTitle(
								mMainActivity
										.getString(R.string.title_followers));
					}
				});
				return AppConfig.COMMUNITY_GET_BEERS_URL
						+ AppConfig.COMMUNITY_GET_FOLLOWERS_Q
						+ ((mUserId != null) ? URLEncoder.encode(mUserId,
								"UTF-8") : "") + AppConfig.COMMUNITY_R;
			}

			return "";
		}

	}

	/************************************************************************************/
	private class ReviewCountAdapter extends ArrayAdapter<ReviewCount> {

		private List<ReviewCount> _mReviewCounts;

		public ReviewCountAdapter(Context context, int textViewResourceId,
				List<ReviewCount> reviewCounts) {
			super(context, textViewResourceId, reviewCounts);
			this._mReviewCounts = reviewCounts;
			Log.i(TAG, "ReviewCountAdapter size=" + reviewCounts.size());
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
			v = vi.inflate(R.layout.follow_list_row, parent, false);

			ReviewCount reviewCount = _mReviewCounts.get(position);

			if (reviewCount != null) {
				Log.i(TAG, "getView:" + reviewCount.getUserId());
				/***********************************************/
				{
					final TextView textView = ((TextView) v
							.findViewById(R.id.item1));
					if (textView != null) {
						textView.setText(reviewCount.getUserName());
					}
				}
				/***********************************************/
				{
					final TextView textView = ((TextView) v
							.findViewById(R.id.item2));
					if (textView != null) {
						textView.setText(reviewCount.getCount() + " reviews");
					}
				}
				/***********************************************/
				{
					final TextView textView = ((TextView) v
							.findViewById(R.id.item3));
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
								.getFollowCountUrl(reviewCount.getUserId()),
								followCountHandler);
					}
				}

			}
			return v;
		}
	}

}
