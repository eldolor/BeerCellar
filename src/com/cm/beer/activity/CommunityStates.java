package com.cm.beer.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class CommunityStates extends ListActivity {
	String TAG;
	GoogleAnalyticsTracker mTracker;
	int mActiveDialog;
	ProgressDialog mDialog;
	ProgressDialog mSplashDialog;
	CommunityStates mMainActivity;
	List<State> mStates = new ArrayList<State>();

	SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM d, yyyy");

	String mOption;
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
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}
		mOption = getIntent().getExtras().getString("OPTION");

		// Start a new thread that will download all the data
		new AsyncDownloadTask().execute();

		mSplashDialog = ProgressDialog.show(this,
				getString(R.string.community_progress_dialog_title),
				getString(R.string.community_progress_searching_message), true,
				true);

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
		State _state = mStates.get((int) id);
		String _selection = _state.state + ", " + _state.country;
		mTracker.trackEvent("CommunityRegions", "Selection", _selection, 0);
		mTracker.dispatch();
		Intent intent = new Intent(mMainActivity.getApplication(),
				CommunityBeers.class);
		intent.putExtra("OPTION", mOption);
		intent.putExtra("COUNTRY", _state.country);
		intent.putExtra("REGION", _state.state);
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

	private void setupCommunityIcon() {
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

	}

	/**
	 * 
	 */
	void displayList() {
		setContentView(R.layout.generic_list);
		registerForContextMenu(getListView());
		getListView().setTextFilterEnabled(true);
		mTracker.trackPageView("CommunityStates");
		mTracker.dispatch();

		mCommunityIcon = (ImageView) findViewById(R.id.community_icon);
		setupCommunityIcon();

		if (mStates != null) {
			StateListAdapter adapter = new StateListAdapter(this,
					R.layout.generic_list, mStates);
			setListAdapter(adapter);

			ListView listView = getListView();
			listView.setTextFilterEnabled(true);

		} else {
			((TextView) findViewById(android.R.id.empty))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// Start a new thread that will download all the
							// data
							new AsyncDownloadTask().execute(mOption);

							mSplashDialog = ProgressDialog
									.show(
											mMainActivity,
											getString(R.string.community_progress_dialog_title),
											getString(R.string.community_progress_searching_message),
											true, true);
						}
					});
		}

		// Toast.makeText(mMainActivity, R.string.hint_community_page,
		// Toast.LENGTH_LONG).show();

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
				Log.i(TAG, "doInBackground starting");
			}
			String _url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_GET_STATES_Q;

			JSONArray statesJSONArray = null;

			try {
				String response[] = Util.getResult(_url);
				if ((response[0] != null) && (response[0].startsWith("["))) {
					statesJSONArray = new JSONArray(response[0]);
				}

				if (statesJSONArray != null) {
					State _state = null;
					JSONObject jsonObject = null;
					int countriesLength = statesJSONArray.length();

					for (int i = 0; i < countriesLength; i++) {
						jsonObject = statesJSONArray.getJSONObject(i);
						_state = new State();
						_state.id = String.valueOf(i);
						_state.country = jsonObject.getString("country");
						_state.state = jsonObject.getString("state");
						mMainActivity.mStates.add(_state);
						Log.i(TAG, _state.country + "::");

					}
				}
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityStates", "DownloadError", ((e
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
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPostExecute starting");
			}

			if (mMainActivity.mSplashDialog != null) {
				mMainActivity.displayList();
				mMainActivity.mSplashDialog.cancel();
			}
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPostExecute finished");
			}
		}
	}

	/************************************************************************************/
	private class StateListAdapter extends ArrayAdapter<State> {

		private List<State> _mStates;

		public StateListAdapter(Context context, int textViewResourceId,
				List<State> countries) {
			super(context, textViewResourceId, countries);
			this._mStates = countries;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.generic_list_row, parent, false);
			}
			State state = _mStates.get(position);
			if (state != null) {

				TextView textView1 = ((TextView) v.findViewById(R.id.item1));
				if (textView1 != null) {
					textView1.setText(state.state);
				}
				TextView textView2 = ((TextView) v.findViewById(R.id.item2));
				if (textView2 != null) {
					textView2.setText(state.country);
				}
			}
			return v;
		}
	}

	/************************************************************************************/
	private class State {
		private String id, country, state;

	}

}
