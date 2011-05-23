package com.cm.beer.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.Note;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.util.BitmapScaler;
import com.cm.beer.util.GpsLocation;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class BeerEdit extends Activity implements
		RatingBar.OnRatingBarChangeListener {
	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
	protected static final int VIEW_IMAGE_ACTIVITY_REQUEST_CODE = 1;
	protected static final int SHARE_WITH_COMMUNITY_ACTIVITY_REQUEST_CODE = 2;
	protected static final int INTERCEPT_REQUEST_CODE_FOR_BEER_EDIT = 3;
	protected static final int BEER_CHARACTERSTICS_REQUEST_CODE = 4;

	String TAG;

	ProgressDialog mDialog;

	int ACTIVE_DIALOG;

	ImageView mCamera;
	AutoCompleteTextView mBeer;
	EditText mAlcohol;
	AutoCompleteTextView mStyle;
	EditText mBrewery;
	EditText mBreweryLink;
	AutoCompleteTextView mState;
	AutoCompleteTextView mCountry;
	Button mSave;
	Button mCancel;
	Button mDelete;
	RatingBar mRating;
	EditText mNotes;
	TextView mPriceLabel;
	TextView mCurrencyLabel;
	EditText mPrice;
	ImageView mThumbnailView;
	TextView mDateCreated;
	TextView mDateUpdated;
	CheckBox mShare;
	SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM d, yyyy");

	Button mCharacteristicsButton;
	JSONObject mCharacteristicsJson;
	TextView mColorTE;
	TextView mClarityTE;
	TextView mFoamTE;
	TextView mAromaTE;
	TextView mBodyTE;
	TextView mMouthfeelTE;
	TextView mAftertasteTE;

	NotesDbAdapter mDbHelper;
	boolean mIsNew;
	boolean mPictureTaken;
	boolean mWasShared;
	boolean mDeleteBeer;
	boolean mRemoveFromCommunity;

	String mLatitude;
	String mLongitude;

	String mCurrencySymbol;
	String mCurrencyCode;

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
		// generate a new id
		if (mRowId == null) {
			mRowId = Long.valueOf(Math.abs(new Random(System
					.currentTimeMillis()).nextLong()));
			mIsNew = true;
		}
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate::_id="
					+ ((mRowId != null) ? mRowId.longValue() : null));
		}
		if (mRowId == 0L) {
			Log.w(TAG, "onCreate::ID is ZERO!!!");
			this.finish();
		}
		Log.i(TAG, "onCreate: mRowId: " + mRowId);
		setContentView(R.layout.beer_edit);
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
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	// @Override
	// protected void onSaveInstanceState(Bundle outState) {
	// saveState();
	// }

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
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onPause");
		}
		super.onPause();
		// saveState();
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
	private void saveIntermediateState() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "saveIntermediateState");
		}
		Note note = new Note();
		note.id = mRowId;
		note.beer = mBeer.getText().toString();
		note.alcohol = mAlcohol.getText().toString();
		note.price = mPrice.getText().toString();
		note.style = mStyle.getText().toString();
		note.brewery = mBrewery.getText().toString();
		String _link = ((!mBreweryLink.getText().toString().equals("")) && (!mBreweryLink
				.getText().toString().startsWith("http://"))) ? ("http://" + mBreweryLink
				.getText().toString())
				: mBreweryLink.getText().toString();
		// Bug Fix: revert to empty if the link contains only http://
		_link = (mBreweryLink.getText().toString().equalsIgnoreCase("http://")) ? ""
				: mBreweryLink.getText().toString();
		note.breweryLink = _link;
		note.state = mState.getText().toString();
		note.country = mCountry.getText().toString();
		note.rating = String.valueOf(mRating.getRating());
		note.notes = mNotes.getText().toString();
		note.picture = mRowId + AppConfig.PICTURES_EXTENSION;
		note.share = mShare.isChecked() ? "Y" : "N";
		if (mCharacteristicsJson != null) {
			note.characteristics = mCharacteristicsJson.toString();
		}
		note.currencyCode = mCurrencyCode;
		note.currencySymbol = mCurrencySymbol;
		if (mIsNew) {
			mDbHelper.createNote(note);
			mIsNew = false;
		} else {
			mDbHelper.updateNote(note);
		}
	}

	/*
	 * 
	 */
	private void saveState() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "saveState");
		}
		Note note = new Note();
		note.id = mRowId;
		note.beer = mBeer.getText().toString();
		note.alcohol = mAlcohol.getText().toString();
		note.price = mPrice.getText().toString();
		note.style = mStyle.getText().toString();
		note.brewery = mBrewery.getText().toString();
		String _link = ((!mBreweryLink.getText().toString().equals("")) && (!mBreweryLink
				.getText().toString().startsWith("http://"))) ? ("http://" + mBreweryLink
				.getText().toString())
				: mBreweryLink.getText().toString();
		// Bug Fix: revert to empty if the link contains only http://
		_link = (mBreweryLink.getText().toString().equals("http://")) ? ""
				: mBreweryLink.getText().toString();
		note.breweryLink = _link;
		note.state = mState.getText().toString();
		note.country = mCountry.getText().toString();
		note.rating = String.valueOf(mRating.getRating());
		note.notes = mNotes.getText().toString();
		note.picture = mRowId + AppConfig.PICTURES_EXTENSION;
		note.share = mShare.isChecked() ? "Y" : "N";
		if (mCharacteristicsJson != null) {
			note.characteristics = mCharacteristicsJson.toString();
		}
		note.currencyCode = mCurrencyCode;
		note.currencySymbol = mCurrencySymbol;

		if (mIsNew) {
			mTracker.trackEvent("BeerEdit", "Beer", "Added", 0);
			mTracker.dispatch();
			// GET LAT & LONG
			GpsLocation gpsLocation = Util.getLocation(mMainActivity, mTracker);

			// save lat & long
			note.latitude = String.valueOf(gpsLocation.latitude);
			note.longitude = String.valueOf(gpsLocation.longitude);

			mDbHelper.createNote(note);
			mIsNew = false;
			Log.i(TAG, "INSERTED " + note.beer + "::" + note.alcohol + "::"
					+ note.style + "::" + note.brewery + "::" + note.state
					+ "::" + note.country + "::" + note.rating + "::"
					+ note.notes + "::" + note.share + "::" + note.latitude
					+ "::" + note.longitude);

			if (note.share.equals("Y")) {
				Log.i(TAG, "is_new => upload_photo, intercept, action_insert");
				Intent intent = new Intent(BeerEdit.this.getApplication(),
						ShareWithCommunity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
				intent.putExtra("ACTION", AppConfig.ACTION_INSERT);
				// upload the picture all the time
				intent.putExtra("UPLOAD_PHOTO", true);
				intent
						.putExtra(
								"INTERCEPT",
								AppConfig.SHARE_WITH_COMMUNITY_INTERCEPT_IF_NOT_LOGGED_IN);
				startActivityForResult(intent,
						SHARE_WITH_COMMUNITY_ACTIVITY_REQUEST_CODE);
			} else {
				mTracker.trackEvent("BeerEdit", "NewBeer",
						"DoNotShareWithCommunitySelected", 0);
				mTracker.dispatch();
				Log.i(TAG, "is_new => do_not_share");
			}

			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "record added");
			}
		} else {
			mTracker.trackEvent("BeerEdit", "Beer", "Updated", 0);
			mTracker.dispatch();
			/*
			 * Note: Latitude and Longitude are only updated when a picture is
			 * taken
			 */
			if (this.mPictureTaken) {
				// GET LAT & LONG
				GpsLocation gpsLocation = Util.getLocation(mMainActivity,
						mTracker);

				// save lat & long
				note.latitude = String.valueOf(gpsLocation.latitude);
				note.longitude = String.valueOf(gpsLocation.longitude);
			} else {
				note.latitude = mLatitude;
				note.longitude = mLongitude;
			}
			mDbHelper.updateNote(note);
			Log.i(TAG, "UPDATED " + note.beer + "::" + note.alcohol + "::"
					+ note.style + "::" + note.brewery + "::" + note.state
					+ "::" + note.country + "::" + note.rating + "::"
					+ note.notes + "::" + note.share + "::" + note.latitude
					+ "::" + note.longitude);

			if (this.mWasShared && (note.share.equals("N"))) {
				Log
						.i(
								TAG,
								"previously shared but now not shared => do_not_upload_photo, do_not_intercept, action_delete");
				mTracker.trackEvent("BeerEdit", "UpdateBeer",
						"DoNotShareWithCommunitySelected", 0);
				mTracker.dispatch();
				Intent intent = new Intent(BeerEdit.this.getApplication(),
						ShareWithCommunity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
				//do not upload photo as the action is DELETE
				intent.putExtra("UPLOAD_PHOTO", false);
				intent.putExtra("INTERCEPT",
						AppConfig.SHARE_WITH_COMMUNITY_DO_NOT_INTERCEPT);
				intent.putExtra("ACTION", AppConfig.ACTION_DELETE);
				startActivityForResult(intent,
						SHARE_WITH_COMMUNITY_ACTIVITY_REQUEST_CODE);

				Log.i(TAG, "Intent Share With Community Started");

			} else if ((!this.mWasShared) && (note.share.equals("Y"))) {
				Log
						.i(
								TAG,
								"previously not shared but now shared => upload_photo, intercept, action_update");
				mTracker.trackEvent("BeerEdit", "UpdateBeer",
						"ShareWithCommunitySelected", 0);
				mTracker.dispatch();
				Intent intent = new Intent(BeerEdit.this.getApplication(),
						ShareWithCommunity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
				intent.putExtra("UPLOAD_PHOTO", true);
				intent
						.putExtra(
								"INTERCEPT",
								AppConfig.SHARE_WITH_COMMUNITY_INTERCEPT_IF_NOT_LOGGED_IN);
				intent.putExtra("ACTION", AppConfig.ACTION_UPDATE);
				startActivityForResult(intent,
						SHARE_WITH_COMMUNITY_ACTIVITY_REQUEST_CODE);

				Log.i(TAG, "Intent Share With Community Started");

//			} else if ((note.share.equals("Y")) && (this.mPictureTaken)) {
//				Log
//						.i(TAG,
//								"shared and photo taken => upload_photo, do_not_intercept, action_update");
//				Intent intent = new Intent(BeerEdit.this.getApplication(),
//						ShareWithCommunity.class);
//				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//				intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
//				intent.putExtra("UPLOAD_PHOTO", true);
//				intent.putExtra("INTERCEPT",
//						AppConfig.SHARE_WITH_COMMUNITY_DO_NOT_INTERCEPT);
//				intent.putExtra("ACTION", AppConfig.ACTION_UPDATE);
//				startActivityForResult(intent,
//						SHARE_WITH_COMMUNITY_ACTIVITY_REQUEST_CODE);
//
//				Log.i(TAG, "Intent Share With Community Started");

			} else if (note.share.equals("Y")) {
				Log
						.i(TAG,
								"shared => upload_photo, intercept_if_not_logged_in, action_update");
				Intent intent = new Intent(BeerEdit.this.getApplication(),
						ShareWithCommunity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
				//upload photo all the time
				intent.putExtra("UPLOAD_PHOTO", true);
				intent
						.putExtra(
								"INTERCEPT",
								AppConfig.SHARE_WITH_COMMUNITY_INTERCEPT_IF_NOT_LOGGED_IN);
				intent.putExtra("ACTION", AppConfig.ACTION_UPDATE);
				startActivityForResult(intent,
						SHARE_WITH_COMMUNITY_ACTIVITY_REQUEST_CODE);
				Log.i(TAG, "Intent Share With Community Started");
			}
		}

	}

	/**
	 * Handles delete
	 */
	private void handleDelete() {

		mDelete.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				Log.i(TAG, "delete");

				mWasShared = isSharedWithCommunity();
				Log.i(TAG, "handleDelete(): Beer Shared = "
						+ ((mWasShared) ? "Y" : "N"));

				AlertDialog.Builder dialog = new AlertDialog.Builder(
						new ContextThemeWrapper(BeerEdit.this,
								android.R.style.Theme_Dialog));
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.setTitle(R.string.delete_note_dialog_title);

				if (mWasShared) {
					CharSequence[] items = new CharSequence[] {
							getString(R.string.delete_note_list_item_dialog_message),
							getString(R.string.delete_note_from_community_list_item_dialog_message) };
					// set delete to true
					mDeleteBeer = true;
					boolean[] checkedItems = new boolean[] { mDeleteBeer,
							mRemoveFromCommunity };
					dialog.setMultiChoiceItems(items, checkedItems,
							new OnMultiChoiceClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									Log.i(TAG, "which=" + which + " isChecked="
											+ ((isChecked) ? "Y" : "N"));
									switch (which) {
									case 0:
										mDeleteBeer = isChecked;
										return;
									case 1:
										mRemoveFromCommunity = isChecked;
										return;
									}
								}
							});
					dialog.setPositiveButton(R.string.yes_label,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* User clicked OK so do some stuff */
									showDialog(AppConfig.DIALOG_DELETING_ID);
									// delete the record
									if (mDeleteBeer) {
										delete();
									}
									if (mRemoveFromCommunity) {
										removeFromCommunity();
									}
									setResult(AppConfig.BEER_DELETED_RESULT_CODE);
									finish();
								}
							});
					dialog.setNegativeButton(R.string.no_label,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							});
				} else {
					dialog.setMessage(R.string.delete_note_dialog_message);
					dialog.setPositiveButton(R.string.yes_label,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* User clicked OK so do some stuff */
									showDialog(AppConfig.DIALOG_DELETING_ID);
									// delete the record
									delete();
									setResult(AppConfig.BEER_DELETED_RESULT_CODE);
									finish();
								}
							});
					dialog.setNegativeButton(R.string.no_label,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							});
				}
				dialog.create();
				dialog.show();
			}
		});

	}

	/**
	 * Indicates whether or not the given beer is shared with the community
	 * 
	 * @param rowId
	 * @return
	 */
	private boolean isSharedWithCommunity() {
		// Fetch the note
		Cursor cursor = null;
		try {
			cursor = mDbHelper.fetchNote(mRowId);
			String _shared = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_SHARE));
			boolean isShareChecked = ((_shared != null) && (_shared.equals("Y"))) ? true
					: false;
			return isShareChecked;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

	}

	/*
	 * Removes the beer from the community
	 */
	private void removeFromCommunity() {
		Log.i(TAG, "remove from community");
		mTracker.trackEvent("BeerEdit", "Beer", "RemoveFromCommunity", 0);
		mTracker.dispatch();
		Log
				.i(TAG,
						"delete => do_not_upload_photo, do_not_intercept, action_delete");
		Intent intent = new Intent(BeerEdit.this.getApplication(),
				ShareWithCommunity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
		intent.putExtra("ACTION", AppConfig.ACTION_DELETE);
		intent.putExtra("UPLOAD_PHOTO", false);
		intent.putExtra("INTERCEPT",
				AppConfig.SHARE_WITH_COMMUNITY_DO_NOT_INTERCEPT);
		startActivityForResult(intent,
				SHARE_WITH_COMMUNITY_ACTIVITY_REQUEST_CODE);
		Log.i(TAG, "Intent Share With Community Started");

	}

	/*
	 * 
	 */
	private void delete() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "delete");
		}
		if (mRowId != null) {
			mDbHelper.deleteNote(mRowId);
			Log.i(TAG, "record deleted");
			mTracker.trackEvent("BeerEdit", "Beer", "Deleted", 0);
			mTracker.dispatch();
		} else {
			Log.i(TAG, "no record to delete");
		}

	}

	/*
	 * 
	 */
	protected void display() {

		Log.i(TAG, "display");

		/****************************************/
		mCamera = (ImageView) findViewById(R.id.camera);
		mCamera.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Dialog dialog = new Dialog(mMainActivity,
						android.R.style.Theme_Dialog);
				dialog.setContentView(R.layout.upload_or_capture_photo);
				dialog.setTitle(R.string.upload_or_capture_photo_dialog_title);
				Button capture = (Button) dialog
						.findViewById(R.id.upload_or_capture_photo_capture_button);
				capture.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Log
								.i(TAG,
										"BeerEdit: The user wants to capture a photo! ");
						Intent intent = new Intent(BeerEdit.this
								.getApplication(), CameraPreview.class);
						intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
						startActivityForResult(intent,
								CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

						dialog.cancel();
					}
				});
				Button upload = (Button) dialog
						.findViewById(R.id.upload_or_capture_photo_upload_button);
				upload.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.i(TAG,
								"BeerEdit: The user wants to upload a photo!");

						Intent intent = new Intent(mMainActivity
								.getApplication(), SDCardExplorer.class);
						intent.putExtra("ROWID", String.valueOf(mRowId
								.longValue()));
						intent.putExtra("STORAGE", "EXTERNAL");
						intent.putExtra("REQUESTCODE",
								AppConfig.SELECT_IMAGE_REQUEST_CODE);
						startActivityForResult(intent,
								AppConfig.SELECT_IMAGE_REQUEST_CODE);

						dialog.cancel();
					}
				});

				dialog.show();

			}
		});
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
		mBeer = (AutoCompleteTextView) findViewById(R.id.beer);
		ArrayAdapter<String> beerAdapter = new ArrayAdapter<String>(this,
				R.layout.list_item, AppConfig.BEERS);
		mBeer.setAdapter(beerAdapter);
		/****************************************/
		mShare = (CheckBox) findViewById(R.id.share);
		/****************************************/
		mAlcohol = (EditText) findViewById(R.id.alcohol);
		/****************************************/
		mPriceLabel = (TextView) findViewById(R.id.price_label);
		mCurrencyLabel = (TextView) findViewById(R.id.currency);
		String _currencySymbol = Currency.getInstance(Locale.getDefault())
				.getSymbol();
		String _currencyCode = Currency.getInstance(Locale.getDefault())
				.getCurrencyCode();
		// set default country
		mCurrencyCode = _currencyCode;
		mCurrencySymbol = _currencySymbol;

		mCurrencyLabel.setText(_currencyCode + " " + _currencySymbol);
		mCurrencyLabel.setTextColor(android.graphics.Color.BLUE);
		mCurrencyLabel.setPaintFlags(mCurrencyLabel.getPaintFlags()
				| Paint.UNDERLINE_TEXT_FLAG);
		mCurrencyLabel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						new ContextThemeWrapper(BeerEdit.this,
								android.R.style.Theme_Dialog));
				dialog.setIcon(android.R.drawable.ic_dialog_info);
				dialog.setTitle(R.string.select_currency_label);

				final CharSequence[] items = new CharSequence[AppConfig.CURRENCY_COUNTRY.length];
				for (int i = 0; i < items.length; i++) {

					items[i] = AppConfig.CURRENCY_CODE[i] + " "
							+ AppConfig.CURRENCY_SYMBOL[i] + " "
							+ AppConfig.CURRENCY_COUNTRY[i];
				}
				dialog.setSingleChoiceItems(items, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// change the select country
								mCurrencyCode = AppConfig.CURRENCY_SYMBOL[which];
								mCurrencySymbol = AppConfig.CURRENCY_CODE[which];
								mCurrencyLabel
										.setText(AppConfig.CURRENCY_CODE[which]
												+ " "
												+ AppConfig.CURRENCY_SYMBOL[which]);
								mCurrencyLabel
										.setTextColor(android.graphics.Color.BLUE);
								mCurrencyLabel.setPaintFlags(mCurrencyLabel
										.getPaintFlags()
										| Paint.UNDERLINE_TEXT_FLAG);
							}
						});

				dialog.setPositiveButton(R.string.done_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				dialog.create();
				dialog.show();
			}
		});

		/****************************************/
		mPrice = (EditText) findViewById(R.id.price);
		/****************************************/
		mStyle = (AutoCompleteTextView) findViewById(R.id.style);
		ArrayAdapter<String> styleAdapter = new ArrayAdapter<String>(this,
				R.layout.list_item, AppConfig.STYLES);
		mStyle.setAdapter(styleAdapter);
		/****************************************/
		mBrewery = (EditText) findViewById(R.id.brewery);
		/****************************************/
		mBreweryLink = (EditText) findViewById(R.id.brewery_link);
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
		mRating.setOnRatingBarChangeListener(BeerEdit.this);
		/****************************************/
		mNotes = (EditText) findViewById(R.id.notes);
		/****************************************/
		mSave = (Button) findViewById(R.id.save);
		mSave.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks

				Log.i(TAG, "save");
				// save the state
				saveIntermediateState();

				Log.i(TAG, "save: Rating=" + mRating.getRating());
				Log.i(TAG, "save: Characteristics=" + mCharacteristicsJson);
				boolean interruptRating = false;
				boolean interruptCharacteristics = false;
				ArrayList<String> interruptKeys = new ArrayList<String>();
				ArrayList<String> titles = new ArrayList<String>();
				ArrayList<String> messages = new ArrayList<String>();
				if (mRating.getRating() == 0.0f) {
					Log.i(TAG, "save: Adding Rating Interrupt");
					interruptRating = true;
					interruptKeys.add("RATING");
					titles
							.add(getString(R.string.beer_edit_interrupt_rating_dialog_title));
					messages
							.add(getString(R.string.beer_edit_interrupt_rating_dialog_message));
				}
				if (mCharacteristicsJson == null
						|| mCharacteristicsJson.equals("")) {
					Log.i(TAG, "save: Adding Characteristics Interrupt");
					interruptCharacteristics = true;
					interruptKeys.add("CHARACTERISTICS");
					titles
							.add(getString(R.string.beer_edit_interrupt_characteristics_dialog_title));
					messages
							.add(getString(R.string.beer_edit_interrupt_characteristics_dialog_message));
				}
				Log.i(TAG, "save: InterruptKeys=" + interruptKeys.size());
				// check
				if (interruptRating || interruptCharacteristics) {
					Intent intent = new Intent(mMainActivity.getApplication(),
							Interrupt.class);
					intent.putStringArrayListExtra("INTERRUPT_KEYS",
							interruptKeys);
					intent.putStringArrayListExtra("TITLES", titles);
					intent.putStringArrayListExtra("MESSAGES", messages);
					startActivityForResult(intent,
							INTERCEPT_REQUEST_CODE_FOR_BEER_EDIT);
				} else {
					showDialog(AppConfig.DIALOG_SAVING_ID);
					saveState();
					setResult(RESULT_OK);
					finish();
				}

			}
		});
		/****************************************/
		mCancel = (Button) findViewById(R.id.cancel);
		mCancel.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks

				Log.i(TAG, "cancel");

				showDialog(AppConfig.DIALOG_LOADING_ID);
				setResult(RESULT_OK);
				finish();
			}
		});
		/****************************************/
		mDelete = (Button) findViewById(R.id.delete);
		mDelete.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		handleDelete();
		/****************************************/
		mCharacteristicsButton = (Button) findViewById(R.id.characteristics_button);
		mCharacteristicsButton.getBackground().setColorFilter(
				AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
		mCharacteristicsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				Log.i(TAG, "characterstics");
				saveIntermediateState();
				Intent intent = new Intent(mMainActivity.getApplication(),
						Characteristics.class);
				if (mCharacteristicsJson != null) {
					intent.putExtra("CHARACTERISTICS", mCharacteristicsJson
							.toString());
				}
				startActivityForResult(intent, BEER_CHARACTERSTICS_REQUEST_CODE);
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

	private void resetCharacteristicsTable() {
		((TextView) findViewById(R.id.color_label)).setVisibility(View.GONE);
		mColorTE.setVisibility(View.GONE);
		((TextView) findViewById(R.id.clarity_label)).setVisibility(View.GONE);
		mClarityTE.setVisibility(View.GONE);
		((TextView) findViewById(R.id.foam_label)).setVisibility(View.GONE);
		mFoamTE.setVisibility(View.GONE);
		((TextView) findViewById(R.id.aroma_label)).setVisibility(View.GONE);
		mAromaTE.setVisibility(View.GONE);
		((TextView) findViewById(R.id.mouthfeel_label))
				.setVisibility(View.GONE);
		mMouthfeelTE.setVisibility(View.GONE);
		((TextView) findViewById(R.id.aftertaste_label))
				.setVisibility(View.GONE);
		mAftertasteTE.setVisibility(View.GONE);
		((TextView) findViewById(R.id.body_label)).setVisibility(View.GONE);
		mBodyTE.setVisibility(View.GONE);

	}

	private void setCharacteristicsTable() throws JSONException {
		resetCharacteristicsTable();
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

	/**
	 * 
	 */
	private void setThumbnailViewOnClickListener() {
		if (setThumbnailView()) {
			mThumbnailView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {

					Log.i(TAG, "View Image");

					showDialog(AppConfig.DIALOG_LOADING_ID);
					Intent intent = new Intent(BeerEdit.this.getApplication(),
							ViewImage.class);
					intent.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);

					startActivityForResult(intent,
							VIEW_IMAGE_ACTIVITY_REQUEST_CODE);
				}
			});
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		Bundle extras = (intent != null) ? intent.getExtras() : null;
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				this.mPictureTaken = (extras != null) ? extras
						.getBoolean("PICTURE_TAKEN") : false;
				Log.i(TAG, "onActivityResult:Picture Taken="
						+ ((this.mPictureTaken) ? "Y" : "N"));
				setThumbnailViewOnClickListener();
			}
		} else if (requestCode == SHARE_WITH_COMMUNITY_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// Toast thanks for sharing
				Toast.makeText(mMainActivity,
						R.string.on_sharing_with_community, Toast.LENGTH_LONG)
						.show();
			}
		} else if (requestCode == INTERCEPT_REQUEST_CODE_FOR_BEER_EDIT) {
			if (resultCode == RESULT_OK) {
				boolean interruptRating = (extras != null) ? extras
						.getBoolean("RATING") : false;
				boolean interruptCharacteristics = (extras != null) ? extras
						.getBoolean("CHARACTERISTICS") : false;
				if ((!interruptRating) && (!interruptCharacteristics)) {
					saveState();
					setResult(RESULT_OK);
					mMainActivity.finish();
				} else if (interruptRating) {
					mRating.setFocusableInTouchMode(true);
				} else if (interruptCharacteristics) {
					mCharacteristicsButton.setFocusableInTouchMode(true);
				}
			}
		} else if (requestCode == BEER_CHARACTERSTICS_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String chars = (extras != null) ? extras
						.getString("CHARACTERISTICS") : "";
				if (chars != null && (chars.startsWith("{"))) {
					try {
						mCharacteristicsJson = new JSONObject(chars);
						Log.i(TAG, "onActivityResult:CHARACTERISTICS: "
								+ mCharacteristicsJson.toString());
						setCharacteristicsTable();
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
		} else if (requestCode == AppConfig.SELECT_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				this.mPictureTaken = true;
				Log.i(TAG, "onActivityResult: SELECT_IMAGE_REQUEST_CODE");
				setThumbnailViewOnClickListener();
			}

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
		ACTIVE_DIALOG = id;
		String dialogMessage = null;
		if (id == AppConfig.DIALOG_LOADING_ID) {
			dialogMessage = this.getString(R.string.progress_loading_message);
		} else if (id == AppConfig.DIALOG_SAVING_ID) {
			dialogMessage = this.getString(R.string.progress_saving_message);
		} else if (id == AppConfig.DIALOG_DELETING_ID) {
			dialogMessage = this.getString(R.string.progress_deleting_message);
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

		if (mRowId != null && (!mIsNew)) {
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
			this.mWasShared = isShareChecked;
			/** Share **/

			mAlcohol.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_ALCOHOL)));
			mPrice.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_PRICE)));
			mStyle.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_STYLE)));
			mBrewery.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BREWERY)));
			mBreweryLink.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_BREWERY_LINK)));
			mState.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_STATE)));
			mCountry.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_COUNTRY)));
			String ratingStr = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_RATING));
			if (ratingStr != null) {
				mRating.setRating(Float.valueOf(ratingStr));
			}
			if (mCharacteristicsJson == null) {
				String _characteristics = cursor
						.getString(cursor
								.getColumnIndexOrThrow(NotesDbAdapter.KEY_CHARACTERISTICS));
				if (_characteristics != null && (!_characteristics.equals(""))) {
					try {
						mCharacteristicsJson = new JSONObject(_characteristics);
						setCharacteristicsTable();
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			} else {
				try {
					setCharacteristicsTable();
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage(), e);
				}

			}
			mNotes.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_NOTES)));
			/********************/
			mLatitude = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_LATITUDE));
			mLongitude = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_LONGITUDE));
			/********************/
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
					try {
						BitmapScaler bitmapScaler = new BitmapScaler(thumbnail,
								AppConfig.THUMBNAIL_WIDTH);
						Bitmap thumbnailBitmap = bitmapScaler.getScaled();
						mThumbnailView.setImageBitmap(thumbnailBitmap);
						success = true;
					} catch (Throwable e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
		}
		return success;
	}

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating,
			boolean fromUser) {
		// TODO Auto-generated method stub

	}

}
