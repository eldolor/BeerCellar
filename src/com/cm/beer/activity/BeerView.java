package com.cm.beer.activity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.util.Logger;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class BeerView extends Activity
{
	protected static final int BEER_EDIT_ACTIVITY_REQUEST_CODE = 0;
	protected static final int VIEW_IMAGE_ACTIVITY_REQUEST_CODE = 1;
	static final int ACTIVITY_SHARE_ON_FACEBOOK = 2;
	
	String TAG;

	ProgressDialog mDialog;

	int ACTIVE_DIALOG;

	ImageView mShareOnFacebook;
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

	Button mShowLocation;

	JSONObject mCharacteristicsJson;
	NotesDbAdapter mDbHelper;

	Activity mMainActivity;

	// Stateful Field
	Long mRowId;

	GoogleAnalyticsTracker mTracker;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();

		if (AppConfig.LOGGING_ENABLED)
		{
			if (Logger.isLogEnabled())  Logger.log("onCreate");
		}
		mMainActivity = this;

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID,
				this);
		if (AppConfig.LOGGING_ENABLED)
		{
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Instantiated");
		}

		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();

		mRowId = savedInstanceState != null ? savedInstanceState
				.getLong(NotesDbAdapter.KEY_ROWID) : null;
		if (mRowId == null)
		{
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
					: null;
		}
		if (AppConfig.LOGGING_ENABLED)
		{
			Log.i(TAG,
					"onCreate::_id="
							+ ((mRowId != null) ? mRowId.longValue() : null));
		}
		if (mRowId == 0L)
		{
			Log.w(TAG, "onCreate::ID is ZERO!!!");
			this.finish();
		}
		setup();
	}

	private void setup() {
		setContentView(R.layout.beer_view);
		display();
		populateFields();

	}


	private void setupGoogleAdSense(Set<String> keywords)
	{
		HashSet<String> keywordsSet = new HashSet<String>(
				Arrays.asList(AppConfig.KEYWORDS));
		if (keywords != null)
		{
			keywordsSet.addAll(keywords);
		}
		Util.setGoogleAdSense(this, keywordsSet);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy()
	{
		if (AppConfig.LOGGING_ENABLED)
		{
			if (Logger.isLogEnabled())  Logger.log("onDestroy");
		}
		// Stop the tracker when it is no longer needed.
		mTracker.stop();
		if (AppConfig.LOGGING_ENABLED)
		{
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Stopped!");
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
	public void onConfigurationChanged(Configuration newConfig)
	{
		// DO NOTHING
		super.onConfigurationChanged(newConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish()
	{
		if (AppConfig.LOGGING_ENABLED)
		{
			if (Logger.isLogEnabled())  Logger.log("finish");
		}
		// database closed in share with community async task
		if (mDbHelper != null)
		{
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
	protected void onResume()
	{
		if (AppConfig.LOGGING_ENABLED)
		{
			if (Logger.isLogEnabled())  Logger.log("onResume");
		}
		if ((mDialog != null) && (mDialog.isShowing()))
		{
			if (AppConfig.LOGGING_ENABLED)
			{
				if (Logger.isLogEnabled())  Logger.log("onResume:active dialog removed");
			}
			removeDialog(ACTIVE_DIALOG);
		}
		if (mRowId == 0L)
		{
			Log.w(TAG, "onResume::ID is ZERO!!!");
			this.finish();
		}
		populateFields();
		super.onResume();
	}

	/*
	 * 
	 */
	protected void display()
	{

		if (Logger.isLogEnabled())  Logger.log("display");

		/****************************************/
		mThumbnailView = (ImageView) findViewById(R.id.thumbnail);
		if (mThumbnailView != null)
		{
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
		mEdit.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				// Perform action on clicks
				if (Logger.isLogEnabled())  Logger.log("edit");
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
		/****************************************/
		mShowLocation = (Button) findViewById(R.id.show_location);
		mShowLocation.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mShareOnFacebook = (ImageView) findViewById(R.id.share_on_facebook);
		/****************************************/
	}

	/**
	 * 
	 */
	private void setThumbnailViewOnClickListener()
	{
		if (setThumbnailView())
		{
			mThumbnailView.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{

					if (Logger.isLogEnabled())  Logger.log("View Image");

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

	private void setCharacteristicsTable(Set<String> keywords) throws JSONException
	{
		if (mCharacteristicsJson != null)
		{
			if (mCharacteristicsJson.has("color"))
			{
				if (!mCharacteristicsJson.getString("color").equals(""))
				{
					((TextView) findViewById(R.id.color_label))
							.setVisibility(View.VISIBLE);
					mColorTE.setVisibility(View.VISIBLE);
					mColorTE.setText(mCharacteristicsJson.getString("color"));
					keywords.add(mCharacteristicsJson.getString("color"));
				}
			}
			if (mCharacteristicsJson.has("clarity"))
			{
				if (!mCharacteristicsJson.getString("clarity").equals(""))
				{
					((TextView) findViewById(R.id.clarity_label))
							.setVisibility(View.VISIBLE);
					mClarityTE.setVisibility(View.VISIBLE);
					mClarityTE.setText(mCharacteristicsJson
							.getString("clarity"));
					keywords.add(mCharacteristicsJson
							.getString("clarity"));
				}
			}
			if (mCharacteristicsJson.has("foam"))
			{
				if (!mCharacteristicsJson.getString("foam").equals(""))
				{
					((TextView) findViewById(R.id.foam_label))
							.setVisibility(View.VISIBLE);
					mFoamTE.setVisibility(View.VISIBLE);
					mFoamTE.setText(mCharacteristicsJson.getString("foam"));
					keywords.add(mCharacteristicsJson.getString("foam"));
				}
			}
			if (mCharacteristicsJson.has("aroma"))
			{
				if (mCharacteristicsJson.getJSONArray("aroma").length() > 0)
				{
					JSONArray _aroma = mCharacteristicsJson
							.getJSONArray("aroma");
					StringBuilder _aromaText = new StringBuilder();
					for (int i = 0; i < _aroma.length(); i++)
					{
						_aromaText.append(", ");
						_aromaText.append(_aroma.getString(i));
						keywords.add(_aroma.getString(i));
					}
					String _aromaStr = _aromaText.toString();
					_aromaStr = _aromaStr.replaceFirst(", ", "");
					((TextView) findViewById(R.id.aroma_label))
							.setVisibility(View.VISIBLE);
					mAromaTE.setVisibility(View.VISIBLE);
					mAromaTE.setText(_aromaStr);
				}
			}
			if (mCharacteristicsJson.has("mouthfeel"))
			{
				if (!mCharacteristicsJson.getString("mouthfeel").equals(""))
				{
					((TextView) findViewById(R.id.mouthfeel_label))
							.setVisibility(View.VISIBLE);
					mMouthfeelTE.setVisibility(View.VISIBLE);
					mMouthfeelTE.setText(mCharacteristicsJson
							.getString("mouthfeel"));
					keywords.add(mCharacteristicsJson
							.getString("mouthfeel"));
				}
			}
			if (mCharacteristicsJson.has("body"))
			{
				if (!mCharacteristicsJson.getString("body").equals(""))
				{
					((TextView) findViewById(R.id.body_label))
							.setVisibility(View.VISIBLE);
					mBodyTE.setVisibility(View.VISIBLE);
					mBodyTE.setText(mCharacteristicsJson.getString("body"));
					keywords.add(mCharacteristicsJson.getString("body"));
				}
			}
			if (mCharacteristicsJson.has("aftertaste"))
			{
				if (!mCharacteristicsJson.getString("aftertaste").equals(""))
				{
					((TextView) findViewById(R.id.aftertaste_label))
							.setVisibility(View.VISIBLE);
					mAftertasteTE.setVisibility(View.VISIBLE);
					mAftertasteTE.setText(mCharacteristicsJson
							.getString("aftertaste"));
					keywords.add(mCharacteristicsJson
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (Logger.isLogEnabled())  Logger.log("onActivityResult");
		// if the beer being viewed was deleted then go back to the beer list
		if (resultCode == AppConfig.BEER_DELETED_RESULT_CODE)
		{
			mMainActivity.finish();
		} else if (requestCode == BeerView.BEER_EDIT_ACTIVITY_REQUEST_CODE) {
			setup();
		} else if (requestCode == AppConfig.FACEBOOK_LOGIN_INTERCEPT_REQUEST_CODE_FOR_WALL_POST) {
			if (resultCode == RESULT_OK) {
				Bundle extras = (data != null) ? data.getExtras() : null;
				// pass the rowId along to ShareOnFacebook
				long rowId = (extras != null) ? extras
						.getLong(NotesDbAdapter.KEY_ROWID) : 0L;
				if (Logger.isLogEnabled())  Logger.log("onActivityResult:Row Id=" + rowId);
				Intent newIntent = new Intent(this, ShareOnFacebook.class);
				newIntent.putExtra(NotesDbAdapter.KEY_ROWID, rowId);
				startActivityForResult(newIntent, ACTIVITY_SHARE_ON_FACEBOOK);
			}
		} else {
			// fillData();
			if (resultCode == AppConfig.FACEBOOK_WALL_POST_SUCCESSFUL_RESULT_CODE) {
				Toast.makeText(BeerView.this, R.string.on_facebook_wall_post,
						Toast.LENGTH_SHORT).show();
			}		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id)
	{

		if (Logger.isLogEnabled())  Logger.log("onCreateDialog");

		String dialogMessage = null;
		if (id == AppConfig.DIALOG_LOADING_ID)
		{
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
	private void populateFields()
	{

		if (Logger.isLogEnabled())  Logger.log("populateFields");
		HashSet<String> keywords = new HashSet<String>();

		if (mRowId != null)
		{
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
			keywords.add(cursor.getString(cursor
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

			String _currencySymbol = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_CURRENCY_SYMBOL));
			String _currencyCode = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_CURRENCY_CODE));

			mPrice.setText(_currencyCode
					+ " "
					+ _currencySymbol
					+ cursor.getString(cursor
							.getColumnIndexOrThrow(NotesDbAdapter.KEY_PRICE)));
			mStyle.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_STYLE)));
			keywords.add(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_STYLE)));

			final String link = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BREWERY_LINK));
			mBrewery.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BREWERY)));
			keywords.add(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BREWERY)));
			
			if ((link != null) && (!link.equals(""))
					&& (!link.equalsIgnoreCase("http://")))
			{
				mBrewery.setTextColor(android.graphics.Color.BLUE);
				mBrewery.setPaintFlags(mBrewery.getPaintFlags()
						| Paint.UNDERLINE_TEXT_FLAG);
				mBrewery.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
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
			keywords.add(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_STATE)));
			mCountry.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_COUNTRY)));
			keywords.add(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_COUNTRY)));
			
			String ratingStr = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_RATING));
			if (ratingStr != null)
			{
				mRating.setRating(Float.valueOf(ratingStr));
			}
			mNotes.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_NOTES)));
			String _characteristics = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_CHARACTERISTICS));
			if (_characteristics != null && (!_characteristics.equals("")))
			{
				try
				{
					mCharacteristicsJson = new JSONObject(_characteristics);
					if (Logger.isLogEnabled())  Logger.log("populateFields: mCharacteristicsJson: "
							+ mCharacteristicsJson.toString());
				} catch (JSONException e)
				{
					Log.e(TAG, e.getMessage(), e);
				}
			}
			try
			{
				setCharacteristicsTable(keywords);
			} catch (JSONException e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
			/*********************************************************/
			final String latitude = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_LATITUDE));
			final String longitude = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_LONGITUDE));
			if ((latitude != null) && (!latitude.equals("0.0")))
			{
				if ((longitude != null) && (!longitude.equals("0.0")))
				{
					mShowLocation.setOnClickListener(new OnClickListener()
					{
						public void onClick(View v)
						{

							String _selection = latitude + "," + longitude;

							mTracker.trackEvent("CommunityWineView",
									"ShowLocation", _selection, 0);
							mTracker.dispatch();
							Intent i = new Intent(
									Intent.ACTION_VIEW,
									Uri.parse("http://maps.google.com/maps?"
											+ "z="
											+ AppConfig.GOOGLE_MAPS_ZOOM_LEVEL
											+ "&t=m"
											+ "&q=loc:"
											+ latitude
											+ "," + longitude));

							startActivity(i);
						}
					});
					mShowLocation.setVisibility(View.VISIBLE);
				}
			}
			/*********************************************************/
			mShareOnFacebook.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Perform action on clicks
					if (Logger.isLogEnabled())  Logger.log("share on facebook");
					//showDialog(AppConfig.DIALOG_LOADING_ID);
					Intent intent = new Intent(mMainActivity.getApplication(),
							LoginIntercept.class);
					intent.putExtra("FACEBOOK_PERMISSIONS",
							AppConfig.FACEBOOK_PERMISSIONS);
					intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
					if (Logger.isLogEnabled())  Logger.log("shareOnFacebook:Row Id=" + mRowId);
					startActivityForResult(
							intent,
							AppConfig.FACEBOOK_LOGIN_INTERCEPT_REQUEST_CODE_FOR_WALL_POST);
				}
			});
			/*********************************************************/
		} else
		{
			mDateCreated.setText("Added on "
					+ mDateFormat.format(System.currentTimeMillis()));
			mDateUpdated.setText("Last updated on "
					+ mDateFormat.format(System.currentTimeMillis()));
		}
		setThumbnailView();
		setupGoogleAdSense(keywords);
	}

	/**
	 * 
	 */
	private boolean setThumbnailView()
	{
		boolean success = false;
		File thumbnailsDir = new File(AppConfig.PICTURES_THUMBNAILS_DIR);
		File thumbnail = null;
		if (thumbnailsDir != null && thumbnailsDir.exists())
		{
			thumbnail = new File(thumbnailsDir, mRowId
					+ AppConfig.PICTURES_THUMBNAILS_EXTENSION);
			if (thumbnail != null && thumbnail.exists())
			{
				if (thumbnail != null)
				{
					Bitmap image;
					try
					{
						image = BitmapFactory.decodeStream(thumbnail.toURL()
								.openStream());
						mThumbnailView.setImageBitmap(image);
						success = true;
					} catch (MalformedURLException e)
					{
						Log.e(TAG, (e.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "", e);
					} catch (IOException e)
					{
						Log.e(TAG, (e.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "", e);
					}
				}
			}
		}
		return success;
	}

}
