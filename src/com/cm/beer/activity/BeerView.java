package com.cm.beer.activity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class BeerView extends Activity {
	protected static final int BEER_EDIT_ACTIVITY_REQUEST_CODE = 0;
	protected static final int VIEW_IMAGE_ACTIVITY_REQUEST_CODE = 1;

	String TAG;

	ProgressDialog mDialog;

	int ACTIVE_DIALOG;

	Button mEdit;
	TextView mBeer;
	TextView mAlcohol;
	TextView mStyle;
	TextView mBrewery;
	TextView mState;
	TextView mCountry;
	RatingBar mRating;
	TextView mNotes;
	TextView mPriceLabel;
	TextView mPrice;
	ImageView mThumbnailView;
	TextView mDateCreated;
	TextView mDateUpdated;
	CheckBox mShare;
	SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM d, yyyy");

	TextView mColorTE;
	TextView mClarityTE;
	TextView mFoamTE;
	TextView mAromaTE;
	TextView mBodyTE;
	TextView mMouthfeelTE;
	TextView mAftertasteTE;

	JSONObject mCharacteristicsJson;
	NotesDbAdapter mDbHelper;

	Activity mMainActivity;

	// Stateful Field
	Long mRowId;

	GoogleAnalyticsTracker mTracker;

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

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.start(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}

		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();

		mRowId = savedInstanceState != null ? savedInstanceState
				.getLong(NotesDbAdapter.KEY_ROWID) : null;
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
					: null;
		}
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate::_id="
					+ ((mRowId != null) ? mRowId.longValue() : null));
		}
		if (mRowId == 0L) {
			Log.w(TAG, "onCreate::ID is ZERO!!!");
			this.finish();
		}
		setContentView(R.layout.beer_view);
		display();
		populateFields();
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
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "finish");
		}
		// database closed in share with community async task
		if (mDbHelper != null) {
			// close the Db connection
			mDbHelper.close();
		}
		super.finish();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onResume");
		}
		if ((mDialog != null) && (mDialog.isShowing())) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onResume:active dialog removed");
			}
			removeDialog(ACTIVE_DIALOG);
		}
		if (mRowId == 0L) {
			Log.w(TAG, "onResume::ID is ZERO!!!");
			this.finish();
		}
		populateFields();
		super.onResume();
	}

	/*
	 * 
	 */
	protected void display() {

		Log.i(TAG, "display");

		/****************************************/
		mThumbnailView = (ImageView) findViewById(R.id.thumbnail);
		if (mThumbnailView != null) {
			setThumbnailViewOnClickListener();
		}
		/****************************************/
		mDateCreated = (TextView) findViewById(R.id.date_created_label);
		/****************************************/
		mDateUpdated = (TextView) findViewById(R.id.date_updated_label);
		/****************************************/
		mBeer = (TextView) findViewById(R.id.beer);
		/****************************************/
		mShare = (CheckBox) findViewById(R.id.share);
		/****************************************/
		mAlcohol = (TextView) findViewById(R.id.alcohol);
		/****************************************/
		mPriceLabel = (TextView) findViewById(R.id.price_label);
		/****************************************/
		mPrice = (TextView) findViewById(R.id.price);
		/****************************************/
		mStyle = (TextView) findViewById(R.id.style);
		/****************************************/
		mBrewery = (TextView) findViewById(R.id.brewery);
		/****************************************/
		mState = (TextView) findViewById(R.id.state);
		/****************************************/
		mCountry = (TextView) findViewById(R.id.country);
		/****************************************/
		mRating = (RatingBar) findViewById(R.id.rating);
		/****************************************/
		mNotes = (TextView) findViewById(R.id.notes);
		/****************************************/
		mEdit = (Button) findViewById(R.id.edit);
		mEdit.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mEdit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				Log.i(TAG, "edit");
				Intent intent = new Intent(mMainActivity, BeerEdit.class);
				intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				startActivityForResult(intent, BEER_EDIT_ACTIVITY_REQUEST_CODE);

			}
		});
		/****************************************/
		mColorTE = (TextView) findViewById(R.id.color);
		mClarityTE = (TextView) findViewById(R.id.clarity);
		mFoamTE = (TextView) findViewById(R.id.foam);
		mAromaTE = (TextView) findViewById(R.id.aroma);
		mBodyTE = (TextView) findViewById(R.id.body);
		mMouthfeelTE = (TextView) findViewById(R.id.mouthfeel);
		mAftertasteTE = (TextView) findViewById(R.id.aftertaste);
	}

	/**
	 * 
	 */
	private void setThumbnailViewOnClickListener() {
		if (setThumbnailView()) {
			mThumbnailView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {

					Log.i(TAG, "View Image");

					showDialog(AppConfig.DIALOG_LOADING_ID);
					Intent intent = new Intent(BeerView.this.getApplication(),
							ViewImage.class);
					intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);

					startActivityForResult(intent,
							VIEW_IMAGE_ACTIVITY_REQUEST_CODE);
				}
			});
		}

	}

	private void setCharacteristicsTable() throws JSONException {
		if (mCharacteristicsJson != null) {
			if (mCharacteristicsJson.has("color")) {
				if (!mCharacteristicsJson.getString("color").equals("")) {
					((TextView) findViewById(R.id.color_label))
							.setVisibility(View.VISIBLE);
					mColorTE.setVisibility(View.VISIBLE);
					mColorTE.setText(mCharacteristicsJson.getString("color"));
				}
			}
			if (mCharacteristicsJson.has("clarity")) {
				if (!mCharacteristicsJson.getString("clarity").equals("")) {
					((TextView) findViewById(R.id.clarity_label))
							.setVisibility(View.VISIBLE);
					mClarityTE.setVisibility(View.VISIBLE);
					mClarityTE.setText(mCharacteristicsJson
							.getString("clarity"));
				}
			}
			if (mCharacteristicsJson.has("foam")) {
				if (!mCharacteristicsJson.getString("foam").equals("")) {
					((TextView) findViewById(R.id.foam_label))
							.setVisibility(View.VISIBLE);
					mFoamTE.setVisibility(View.VISIBLE);
					mFoamTE.setText(mCharacteristicsJson.getString("foam"));
				}
			}
			if (mCharacteristicsJson.has("aroma")) {
				if (mCharacteristicsJson.getJSONArray("aroma").length() > 0) {
					JSONArray _aroma = mCharacteristicsJson
							.getJSONArray("aroma");
					StringBuilder _aromaText = new StringBuilder();
					for (int i = 0; i < _aroma.length(); i++) {
						_aromaText.append(", ");
						_aromaText.append(_aroma.getString(i));
					}
					String _aromaStr = _aromaText.toString();
					_aromaStr = _aromaStr.replaceFirst(", ", "");
					((TextView) findViewById(R.id.aroma_label))
							.setVisibility(View.VISIBLE);
					mAromaTE.setVisibility(View.VISIBLE);
					mAromaTE.setText(_aromaStr);
				}
			}
			if (mCharacteristicsJson.has("mouthfeel")) {
				if (!mCharacteristicsJson.getString("mouthfeel").equals("")) {
					((TextView) findViewById(R.id.mouthfeel_label))
							.setVisibility(View.VISIBLE);
					mMouthfeelTE.setVisibility(View.VISIBLE);
					mMouthfeelTE.setText(mCharacteristicsJson
							.getString("mouthfeel"));
				}
			}
			if (mCharacteristicsJson.has("body")) {
				if (!mCharacteristicsJson.getString("body").equals("")) {
					((TextView) findViewById(R.id.body_label))
							.setVisibility(View.VISIBLE);
					mBodyTE.setVisibility(View.VISIBLE);
					mBodyTE.setText(mCharacteristicsJson.getString("body"));
				}
			}
			if (mCharacteristicsJson.has("aftertaste")) {
				if (!mCharacteristicsJson.getString("aftertaste").equals("")) {
					((TextView) findViewById(R.id.aftertaste_label))
							.setVisibility(View.VISIBLE);
					mAftertasteTE.setVisibility(View.VISIBLE);
					mAftertasteTE.setText(mCharacteristicsJson
							.getString("aftertaste"));
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "onActivityResult");
		// if the beer being viewed was deleted then go back to the beer list
		if (resultCode == AppConfig.BEER_DELETED_RESULT_CODE) {
			mMainActivity.finish();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {

		Log.i(TAG, "onCreateDialog");

		String dialogMessage = null;
		if (id == AppConfig.DIALOG_LOADING_ID) {
			dialogMessage = this.getString(R.string.progress_loading_message);
			ACTIVE_DIALOG = AppConfig.DIALOG_LOADING_ID;
		}
		mDialog = ProgressDialog.show(this, null, dialogMessage, true, true);
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
	}

	/*
	 * 
	 */
	private void populateFields() {

		Log.i(TAG, "populateFields");

		if (mRowId != null) {
			Cursor cursor = mDbHelper.fetchNote(mRowId);
			startManagingCursor(cursor);
			mDateCreated.setText("Added on "
					+ mDateFormat.format(cursor.getLong(cursor
							.getColumnIndex(NotesDbAdapter.KEY_CREATED))));
			mDateUpdated.setText("Last updated on "
					+ mDateFormat.format(cursor.getLong(cursor
							.getColumnIndex(NotesDbAdapter.KEY_UPDATED))));

			mBeer.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BEER)));
			/** Share **/
			String _shared = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_SHARE));
			boolean isShareChecked = ((_shared != null) && (_shared.equals("Y"))) ? true
					: false;
			mShare.setChecked(isShareChecked);
			/** Share **/

			mAlcohol.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_ALCOHOL)));
			String _currencySymbol = Currency.getInstance(Locale.getDefault())
					.getSymbol();
			mPrice.setText(_currencySymbol
					+ cursor.getString(cursor
							.getColumnIndexOrThrow(NotesDbAdapter.KEY_PRICE)));
			mStyle.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_STYLE)));

			final String link = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BREWERY_LINK));
			mBrewery.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BREWERY)));
			if ((link != null) && (!link.equals(""))) {
				mBrewery.setTextColor(android.graphics.Color.BLUE);
				mBrewery.setPaintFlags(mBrewery.getPaintFlags()
						| Paint.UNDERLINE_TEXT_FLAG);
				mBrewery.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mBrewery.setText(R.string.progress_loading_message);
						Intent intent = new Intent(mMainActivity
								.getApplication(), BeerWebView.class);
						intent.putExtra("URL", link);
						startActivity(intent);

					}
				});
			}

			mState.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_STATE)));
			mCountry.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_COUNTRY)));
			String ratingStr = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_RATING));
			if (ratingStr != null) {
				mRating.setRating(Float.valueOf(ratingStr));
			}
			mNotes.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_NOTES)));
			String _characteristics = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_CHARACTERISTICS));
			if (_characteristics != null && (!_characteristics.equals(""))) {
				try {
					mCharacteristicsJson = new JSONObject(_characteristics);
					Log.i(TAG, "populateFields: mCharacteristicsJson: "
							+ mCharacteristicsJson.toString());
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			try {
				setCharacteristicsTable();
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		} else {
			mDateCreated.setText("Added on "
					+ mDateFormat.format(System.currentTimeMillis()));
			mDateUpdated.setText("Last updated on "
					+ mDateFormat.format(System.currentTimeMillis()));
		}
		setThumbnailView();
	}

	/**
	 * 
	 */
	private boolean setThumbnailView() {
		boolean success = false;
		File thumbnailsDir = new File(AppConfig.PICTURES_THUMBNAILS_DIR);
		File thumbnail = null;
		if (thumbnailsDir != null && thumbnailsDir.exists()) {
			thumbnail = new File(thumbnailsDir, mRowId
					+ AppConfig.PICTURES_THUMBNAILS_EXTENSION);
			if (thumbnail != null && thumbnail.exists()) {
				if (thumbnail != null) {
					Bitmap image;
					try {
						image = BitmapFactory.decodeStream(thumbnail.toURL()
								.openStream());
						mThumbnailView.setImageBitmap(image);
						success = true;
					} catch (MalformedURLException e) {
						Log.e(TAG, (e.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "", e);
					} catch (IOException e) {
						Log.e(TAG, (e.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "", e);
					}
				}
			}
		}
		return success;
	}

}
