package com.cm.beer.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class CommunitySearch extends Activity implements
		RatingBar.OnRatingBarChangeListener {
	String TAG;

	AutoCompleteTextView mBeer;
	AutoCompleteTextView mStyle;
	EditText mBrewery;
	AutoCompleteTextView mState;
	AutoCompleteTextView mCountry;
	Button mSearch;
	Button mCancel;
	RatingBar mRating;
	EditText mReviewedBy;

	ProgressDialog mDialog;

	boolean mRatingChanged;

	GoogleAnalyticsTracker mTracker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		if (AppConfig.LOGGING_ENABLED) {
			Log.d(TAG, "onCreate: ");
		}

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}

		setContentView(R.layout.community_beer_search);
		display();
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
		if (id == AppConfig.DIALOG_SEARCHING_ID) {
			dialogMessage = this.getString(R.string.progress_searching_message);
		}
		mDialog = ProgressDialog.show(this, null, dialogMessage, true, true);
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
	}

	/*
	 * 
	 */
	protected void display() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "display");
		}
		/****************************************/
		mBeer = (AutoCompleteTextView) findViewById(R.id.beer);
		ArrayAdapter<String> beerAdapter = new ArrayAdapter<String>(this,
				R.layout.list_item, AppConfig.BEERS);
		mBeer.setAdapter(beerAdapter);
		/****************************************/
		mStyle = (AutoCompleteTextView) findViewById(R.id.style);
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
				R.layout.list_item, AppConfig.STYLES);
		mStyle.setAdapter(typeAdapter);
		/****************************************/
		mBrewery = (EditText) findViewById(R.id.brewery);
		/****************************************/
		mState = (AutoCompleteTextView) findViewById(R.id.state);
		ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(this,
				R.layout.list_item, AppConfig.STATES);
		mState.setAdapter(stateAdapter);
		/****************************************/
		mCountry = (AutoCompleteTextView) findViewById(R.id.country);
		ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(this,
				R.layout.list_item, AppConfig.COUNTRIES);
		mCountry.setAdapter(countryAdapter);
		/****************************************/
		mRating = (RatingBar) findViewById(R.id.rating);
		mRating.setOnRatingBarChangeListener(CommunitySearch.this);
		/****************************************/
		mReviewedBy = (EditText) findViewById(R.id.reviewed_by);
		/****************************************/
		mSearch = (Button) findViewById(R.id.search);
		mSearch.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mSearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "save");
				}
				search();
				setResult(RESULT_OK);
				finish();
			}

		});
		/****************************************/
		mCancel = (Button) findViewById(R.id.cancel);
		mCancel.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				// showDialog(DIALOG_SAVING_ID);
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "cancel");
				}
				setResult(RESULT_OK);
				finish();
			}
		});

	}

	private void search() {
		// TODO Auto-generated method stub
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "search");
		}
		Intent intent = new Intent(CommunitySearch.this.getApplication(),
				CommunityBeers.class);
		// search action
		intent.putExtra("OPTION", AppConfig.COMMUNITY_SEARCH_BEERS);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		StringBuilder searchCriteria = new StringBuilder();

		if (mBeer.getText().toString() != null
				&& (!mBeer.getText().toString().equals(""))) {
			intent
					.putExtra(NotesDbAdapter.KEY_BEER, mBeer.getText()
							.toString());
			searchCriteria.append(NotesDbAdapter.KEY_BEER);
			Log.i(TAG, "beer:" + mBeer.getText().toString());
		}
		if (mStyle.getText().toString() != null
				&& (!mStyle.getText().toString().equals(""))) {
			intent.putExtra(NotesDbAdapter.KEY_STYLE, mStyle.getText()
					.toString());
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_STYLE);
			Log.i(TAG, "style:" + mStyle.getText().toString());
		}
		if (mBrewery.getText().toString() != null
				&& (!mBrewery.getText().toString().equals(""))) {
			intent.putExtra(NotesDbAdapter.KEY_BREWERY, mBrewery.getText()
					.toString());
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_BREWERY);
			Log.i(TAG, "brewery:" + mBrewery.getText().toString());
		}
		if (mState.getText().toString() != null
				&& (!mState.getText().toString().equals(""))) {
			intent.putExtra(NotesDbAdapter.KEY_STATE, mState.getText()
					.toString());
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_STATE);
			Log.i(TAG, "state:" + mState.getText().toString());
		}
		if (mCountry.getText().toString() != null
				&& (!mCountry.getText().toString().equals(""))) {
			intent.putExtra(NotesDbAdapter.KEY_COUNTRY, mCountry.getText()
					.toString());
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_COUNTRY);
			Log.i(TAG, "country:" + mCountry.getText().toString());
		}

		if (mRatingChanged) {
			intent.putExtra(NotesDbAdapter.KEY_RATING, String.valueOf(mRating
					.getRating()));
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_RATING);
			Log.i(TAG, "rating:" + String.valueOf(mRating.getRating()));
		}
		if (mReviewedBy.getText().toString() != null
				&& (!mReviewedBy.getText().toString().equals(""))) {
			intent.putExtra(NotesDbAdapter.KEY_USER_NAME, mReviewedBy.getText()
					.toString());
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_USER_NAME);
			Log.i(TAG, "username:" + mReviewedBy.getText().toString());
		}

		String searchCriteriaStr = searchCriteria.toString().replaceAll(",",
				"_");
		Log.i(TAG, "Search Criteria:" + searchCriteriaStr);
		mTracker
				.trackEvent("CommunitySearch", "Criteria", searchCriteriaStr, 0);
		mTracker.dispatch();

		startActivity(intent);

	}

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating,
			boolean fromUser) {
		mRatingChanged = true;

	}

}
