package com.cm.beer.activity;

import java.util.Currency;
import java.util.Locale;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.util.Logger;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Search extends Activity implements
		RatingBar.OnRatingBarChangeListener {
	String TAG;

	AutoCompleteTextView mBeer;
	EditText mAlcohol;
	AutoCompleteTextView mStyle;
	EditText mBrewery;
	AutoCompleteTextView mState;
	AutoCompleteTextView mCountry;
	Button mSearch;
	Button mCancel;
	RatingBar mRating;
	EditText mPrice;
	TextView mPriceLabel;
	CheckBox mShare;

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
			if (Logger.isLogEnabled())  Logger.log("onCreate: ");
		}

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Instantiated");
		}

		setContentView(R.layout.beer_search);
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
			if (Logger.isLogEnabled())  Logger.log("display");
		}
		/****************************************/
		mBeer = (AutoCompleteTextView) findViewById(R.id.beer);
		ArrayAdapter<String> beerAdapter = new ArrayAdapter<String>(this,
				R.layout.list_item, AppConfig.BEERS);
		mBeer.setAdapter(beerAdapter);
		/****************************************/
		mShare = (CheckBox) findViewById(R.id.share);
		/****************************************/
		mAlcohol = (EditText) findViewById(R.id.alcohol);
		/****************************************/
		mPrice = (EditText) findViewById(R.id.price);
		/****************************************/
		mPriceLabel = (TextView) findViewById(R.id.price_label);
		String _currencySymbol = Currency.getInstance(Locale.getDefault())
				.getSymbol();
		mPriceLabel.setText(mPriceLabel.getText().toString() + " "
				+ _currencySymbol);
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
		mRating.setOnRatingBarChangeListener(Search.this);
		/****************************************/
		mSearch = (Button) findViewById(R.id.search);
		mSearch.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mSearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				if (AppConfig.LOGGING_ENABLED) {
					if (Logger.isLogEnabled())  Logger.log("save");
				}
				showDialog(AppConfig.DIALOG_SEARCHING_ID);
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
					if (Logger.isLogEnabled())  Logger.log("cancel");
				}
				showDialog(AppConfig.DIALOG_LOADING_ID);
				setResult(RESULT_OK);
				finish();
			}
		});

	}

	private void search() {
		// TODO Auto-generated method stub
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("search");
		}
		Intent intent = new Intent(Search.this.getApplication(),
				SearchResults.class);
		intent.setAction(Intent.ACTION_SEARCH);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		StringBuilder searchCriteria = new StringBuilder();

		if (mBeer.getText().toString() != null
				&& (!mBeer.getText().toString().equals(""))) {
			intent
					.putExtra(NotesDbAdapter.KEY_BEER, mBeer.getText()
							.toString());
			searchCriteria.append(NotesDbAdapter.KEY_BEER);
			if (Logger.isLogEnabled())  Logger.log("beer:" + mBeer.getText().toString());
		}
		if (mAlcohol.getText().toString() != null
				&& (!mAlcohol.getText().toString().equals(""))) {
			intent.putExtra(NotesDbAdapter.KEY_ALCOHOL, mAlcohol.getText()
					.toString());
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_ALCOHOL);
			if (Logger.isLogEnabled())  Logger.log("year:" + mAlcohol.getText().toString());
		}
		if (mStyle.getText().toString() != null
				&& (!mStyle.getText().toString().equals(""))) {
			intent.putExtra(NotesDbAdapter.KEY_STYLE, mStyle.getText()
					.toString());
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_STYLE);
			if (Logger.isLogEnabled())  Logger.log("type:" + mStyle.getText().toString());
		}
		if (mBrewery.getText().toString() != null
				&& (!mBrewery.getText().toString().equals(""))) {
			intent.putExtra(NotesDbAdapter.KEY_BREWERY, mBrewery.getText()
					.toString());
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_BREWERY);
			if (Logger.isLogEnabled())  Logger.log("brewery:" + mBrewery.getText().toString());
		}
		if (mState.getText().toString() != null
				&& (!mState.getText().toString().equals(""))) {
			intent.putExtra(NotesDbAdapter.KEY_STATE, mState.getText()
					.toString());
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_STATE);
			if (Logger.isLogEnabled())  Logger.log("state:" + mState.getText().toString());
		}
		if (mCountry.getText().toString() != null
				&& (!mCountry.getText().toString().equals(""))) {
			intent.putExtra(NotesDbAdapter.KEY_COUNTRY, mCountry.getText()
					.toString());
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_COUNTRY);
			if (Logger.isLogEnabled())  Logger.log("country:" + mCountry.getText().toString());
		}

		if (mPrice.getText().toString() != null
				&& (!mPrice.getText().toString().equals(""))) {
			intent.putExtra(NotesDbAdapter.KEY_PRICE, mPrice.getText()
					.toString());
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_PRICE);
			if (Logger.isLogEnabled())  Logger.log("price:" + mPrice.getText().toString());
		}

		if (mRatingChanged) {
			intent.putExtra(NotesDbAdapter.KEY_RATING, String.valueOf(mRating
					.getRating()));
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_RATING);
			if (Logger.isLogEnabled())  Logger.log("rating:" + String.valueOf(mRating.getRating()));
		}
		String _share = mShare.isChecked() ? "Y" : "N";
		if (_share.equals("Y")) {
			intent.putExtra(NotesDbAdapter.KEY_SHARE, _share);
			searchCriteria.append(",");
			searchCriteria.append(NotesDbAdapter.KEY_SHARE);
			if (Logger.isLogEnabled())  Logger.log("shared:" + _share);
		}

		String searchCriteriaStr = searchCriteria.toString().replaceAll(",",
				"_");
		if (Logger.isLogEnabled())  Logger.log("Search Criteria:" + searchCriteriaStr);
		mTracker.trackEvent("Search", "Criteria", searchCriteriaStr, 0);
		mTracker.dispatch();

		startActivity(intent);

	}

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating,
			boolean fromUser) {
		mRatingChanged = true;

	}

}
