package com.cm.beer.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.Note;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.util.BitmapScaler;
import com.cm.beer.util.Logger;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class BeerList extends ListActivity {
	String TAG;

	static final int ACTIVITY_CREATE = 0;
	static final int ACTIVITY_EDIT = 1;
	static final int ACTIVITY_SHARE_ON_FACEBOOK = 2;
	static final int ACTIVITY_BACKUP = 3;
	static final int ACTIVITY_SEARCH = 4;
	static final int ACTIVITY_UPGRADE = 5;
	static final int ACTIVITY_ABOUT_THIS_BEER = 6;
	static final int ACTIVITY_VIEW = 7;
	static final int ACTIVITY_SHARE_WITH_COMMUNITY = 8;

	static final Handler handler = new Handler();

	static final int MENU_GROUP = 0;
	static final int INSERT_ID = Menu.FIRST;
	static final int SHARE_ON_FACEBOOK_ID = Menu.FIRST + 1;
	static final int BACKUP_ID = Menu.FIRST + 2;
	static final int SEARCH_ID = Menu.FIRST + 3;
	static final int SORT_ID = Menu.FIRST + 4;
	static final int ABOUT_THIS_BEER_ID = Menu.FIRST + 5;
	static final int SEND_ERROR_REPORT_ID = Menu.FIRST + 6;
	static final int SHOW_LOCATION_ID = Menu.FIRST + 7;
	static final int SHARE_WITH_COMMUNITY_ID = Menu.FIRST + 8;

	static final int FACEBOOK_LOGIN_INTERCEPT_REQUEST_CODE_FOR_WALL_POST = 99;
	protected static final int SHARE_WITH_COMMUNITY_ACTIVITY_REQUEST_CODE = 98;

	NotesDbAdapter mDbHelper;
	SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
	File thumbnailsDir = new File(AppConfig.PICTURES_THUMBNAILS_DIR);
	ProgressDialog dialog;

	int ACTIVE_DIALOG;

	// start at 1
	int mPageNumber = 1;
	View mFooterView;
	ListView mBeerListView;
	int mPreviousCursorCount;
	boolean mLoadMoreBeersAction;

	GoogleAnalyticsTracker mTracker;
	Activity mMainActivity;

	SharedPreferences mPreferences;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
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

		showDialog(AppConfig.DIALOG_LOADING_ID);
		setContentView(R.layout.beer_list);
		mPreferences = this.getSharedPreferences(this
				.getString(R.string.app_name),
				Activity.MODE_PRIVATE);
		if (!mPreferences
				.getBoolean(
						AppConfig.PREFERENCES_BEER_SEED_DATA_PICTURE_INITIALIZED,
						false)) {
			insertSeedData(mPreferences);
		}

		mDbHelper = new NotesDbAdapter(BeerList.this);
		mDbHelper.open();

		mBeerListView = getListView();
		registerForContextMenu(mBeerListView);
		mBeerListView.setTextFilterEnabled(true);

		// initialize Footer View for the list
		initFooterView();
		fillData();

		Util.setGoogleAdSense(this);
		Util.loadInterstitialAd(this);

	}

	/**
	 * 
	 */
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
				// increment the page number
				++mPageNumber;
				mLoadMoreBeersAction = true;
				fillData();
			}
		});
		// Call this before calling setAdapter
		mBeerListView.addFooterView(mFooterView);

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
	 * @see
	 * android.app.Activity#onConfigurationChanged(android.content.res.Configuration
	 * )
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// DO NOTHING
		super.onConfigurationChanged(newConfig);
	}

	private void insertSeedData(SharedPreferences preferences) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		try {
			File baseDir = new File(AppConfig.BASE_APP_DIR);
			File picturesDir = new File(AppConfig.PICTURES_DIR);
			File thumbnailsDir = new File(AppConfig.PICTURES_THUMBNAILS_DIR);

			if (createDirectories(baseDir, picturesDir, thumbnailsDir)) {
				{
					File pictureOnSD = new File(picturesDir, "1"
							+ AppConfig.PICTURES_EXTENSION);
					bis = new BufferedInputStream(this.getAssets().open(
							AppConfig.ASSET_SEED_PICTURE));
					bos = new BufferedOutputStream(new FileOutputStream(
							pictureOnSD));
					byte[] b = new byte[128];
					while (bis.read(b) != -1) {
						bos.write(b);
					}
					bis.close();
					bos.close();
				}
				{
					// Thumbnail
					File thumbnailOnSD = new File(thumbnailsDir, "1"
							+ AppConfig.PICTURES_THUMBNAILS_EXTENSION);
					bis = new BufferedInputStream(this.getAssets().open(
							AppConfig.ASSET_SEED_THUMBNAIL));
					bos = new BufferedOutputStream(new FileOutputStream(
							thumbnailOnSD));
					byte[] b = new byte[128];
					while (bis.read(b) != -1) {
						bos.write(b);
					}
				}

				preferences
						.edit()
						.putBoolean(
								AppConfig.PREFERENCES_BEER_SEED_DATA_PICTURE_INITIALIZED,
								true).commit();
			}
		} catch (IOException e) {
			Log.e(TAG, (e.getMessage() != null) ? e.getMessage().replace(" ",
					"_") : "", e);
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
					Log.e(TAG, (e.getMessage() != null) ? e.getMessage()
							.replace(" ", "_") : "", e);
				}
			if (bos != null)
				try {
					bos.close();
				} catch (IOException e) {
					Log.e(TAG, (e.getMessage() != null) ? e.getMessage()
							.replace(" ", "_") : "", e);
				}
		}

	}

	/**
	 * Creates appropriate directories if necessary
	 * 
	 * @param baseDir
	 * @param picturesDir
	 * @param thumbnailsDir
	 */
	private boolean createDirectories(File baseDir, File picturesDir,
			File thumbnailsDir) {
		boolean result = true;
		try {
			// 1. Base dir does not exist
			if (baseDir != null && (!baseDir.exists())) {
				// 1.1 Create new base dir
				if (baseDir.mkdir()) {
					if (AppConfig.LOGGING_ENABLED) {
						Log.i(TAG,
								"DatabaseHelper::createDirectories::Created "
										+ baseDir.getPath());
					}
					// 1.2 Create new pictures dir
					if (picturesDir.mkdir()) {
						if (AppConfig.LOGGING_ENABLED) {
							Log.i(TAG,
									"DatabaseHelper::createDirectories::Created "
											+ picturesDir.getPath());
						}
						// 1.3 Create new thumbnails dir
						if (thumbnailsDir.mkdir()) {
							if (AppConfig.LOGGING_ENABLED) {
								Log.i(TAG,
										"DatabaseHelper::createDirectories::Created "
												+ thumbnailsDir.getPath());
							}

						} else {
							if (AppConfig.LOGGING_ENABLED) {
								Log.e(TAG,
										"DatabaseHelper::createDirectories::Unable to create "
												+ thumbnailsDir.getPath());
							}
							result = false;
						}
					} else {
						if (AppConfig.LOGGING_ENABLED) {
							Log.e(TAG,
									"DatabaseHelper::createDirectories::Unable to create "
											+ picturesDir.getPath());
						}
						result = false;
					}
				} else {
					if (AppConfig.LOGGING_ENABLED) {
						Log.e(TAG,
								"DatabaseHelper::createDirectories::Unable to create "
										+ baseDir.getPath());
					}
					result = false;
				}
			} else if (baseDir != null && baseDir.exists()) {
				// 2. Base dir exists
				// 2.1 Pictures dir does not exist
				if (picturesDir != null && (!picturesDir.exists())) {
					// 2.1.1 create new pictures dir
					if (picturesDir.mkdir()) {
						if (AppConfig.LOGGING_ENABLED) {
							Log.i(TAG,
									"DatabaseHelper::createDirectories::Created "
											+ picturesDir.getPath());
						}
						// 2.1.2 create new thumbnails dir
						if (thumbnailsDir.mkdir()) {
							if (AppConfig.LOGGING_ENABLED) {
								Log.i(TAG,
										"DatabaseHelper::createDirectories::Created "
												+ thumbnailsDir.getPath());
							}

						} else {
							if (AppConfig.LOGGING_ENABLED) {
								Log.e(TAG,
										"DatabaseHelper::createDirectories::Unable to create "
												+ thumbnailsDir.getPath());
							}
							result = false;
						}
					} else {
						if (AppConfig.LOGGING_ENABLED) {
							Log.e(TAG,
									"DatabaseHelper::createDirectories::Unable to create "
											+ picturesDir.getPath());
						}
						result = false;
					}

				} else if (picturesDir != null && (picturesDir.exists())) {
					// 2.2 Pictures dir exists
					// 2.2.1 Thumbnails dir does not exist
					if (thumbnailsDir != null && (!thumbnailsDir.exists())) {
						// 2.2.1.1 create new thumbnails dir
						if (thumbnailsDir.mkdir()) {
							if (AppConfig.LOGGING_ENABLED) {
								Log.i(TAG,
										"DatabaseHelper::createDirectories::Created "
												+ thumbnailsDir.getPath());
							}

						} else {
							if (AppConfig.LOGGING_ENABLED) {
								Log.e(TAG,
										"DatabaseHelper::createDirectories::Unable to create "
												+ thumbnailsDir.getPath());
							}
							result = false;
						}
					}
				}

			}
		} catch (Throwable e) {
			Log.e(TAG, (e.getMessage() != null) ? e.getMessage().replace(" ",
					"_") : "", e);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("finish");
		}
		if (mDbHelper != null) {
			// close the Db connection
			mDbHelper.close();
		}
		super.finish();
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
		if (id == AppConfig.DIALOG_LOADING_ID) {
			dialogMessage = this.getString(R.string.progress_loading_message);
			ACTIVE_DIALOG = AppConfig.DIALOG_LOADING_ID;
		} else if (id == AppConfig.DIALOG_DELETING_ID) {
			dialogMessage = this.getString(R.string.progress_deleting_message);
			ACTIVE_DIALOG = AppConfig.DIALOG_DELETING_ID;
		}
		dialog = ProgressDialog.show(this, null, dialogMessage, true, true);
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onResume");
		}
		if ((dialog != null) && (dialog.isShowing())) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("onResume:active dialog removed");
			}
			removeDialog(ACTIVE_DIALOG);
		}
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreateOptionsMenu");
		}
		super.onCreateOptionsMenu(menu);
		int menuPosition = 0;
		menu.add(MENU_GROUP, INSERT_ID, menuPosition++, R.string.menu_insert);
		menu.add(MENU_GROUP, SEARCH_ID, menuPosition++, R.string.menu_search);
		menu.add(MENU_GROUP, BACKUP_ID, menuPosition++, R.string.menu_backup);
		menu.add(MENU_GROUP, SORT_ID, menuPosition++, R.string.menu_sort);
		if (AppConfig.DEFAULT_APPSTORE.equals(AppConfig.GOOGLE_APPSTORE)) {
			menu.add(MENU_GROUP, SEND_ERROR_REPORT_ID, menuPosition++,
					R.string.menu_send_error_report);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onMenuItemSelected");
		}
		switch (item.getItemId()) {
		case INSERT_ID:
			addBeer();
			return true;
		case BACKUP_ID:
			backup();
			return true;
		case SEARCH_ID:
			search();
			return true;
		case SORT_ID:
			mTracker.trackEvent("BeerList", "Sort", "Clicked", 0);
			mTracker.dispatch();
			sort();
			return true;
		case SEND_ERROR_REPORT_ID:
			mTracker.trackEvent("BeerList", "SendErrorReport", "Clicked", 0);
			mTracker.dispatch();
			sendErrorReport();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
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
		if (Logger.isLogEnabled())  Logger.log("onCreateContextMenu");
		int menuPosition = 0;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Cursor cursor = mDbHelper.fetchNote(info.id);
		startManagingCursor(cursor);
		if (cursor != null && (cursor.isAfterLast() == false)) {
			String _latitude = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_LATITUDE));
			if ((_latitude != null) && (!_latitude.equals("0.0"))) {
				String _longitude = cursor.getString(cursor
						.getColumnIndex(NotesDbAdapter.KEY_LONGITUDE));
				if ((_longitude != null) && (!_longitude.equals("0.0"))) {
					menu.add(MENU_GROUP, SHOW_LOCATION_ID, menuPosition++,
							R.string.menu_show_location);
				}
			}
			String _beerStyle = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_STYLE));
			if ((_beerStyle != null) && (!_beerStyle.equals(""))) {
				menu.add(MENU_GROUP, ABOUT_THIS_BEER_ID, menuPosition++,
						R.string.menu_about_this_beer);
			}

		}
		menu.add(MENU_GROUP, SHARE_ON_FACEBOOK_ID, menuPosition++,
				R.string.menu_share_on_facebook);
		menu.add(MENU_GROUP, SHARE_WITH_COMMUNITY_ID, menuPosition++,
				R.string.menu_share_with_community);

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
			if (Logger.isLogEnabled())  Logger.log("onContextItemSelected");
		}
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case SHARE_ON_FACEBOOK_ID:
			mTracker.trackEvent("BeerList", "ShareOnFacebook", "Clicked", 0);
			mTracker.dispatch();
			shareOnFacebook(info.id);
			return true;
		case ABOUT_THIS_BEER_ID:
			aboutThisBeer(info.id);
			return true;
		case SHOW_LOCATION_ID:
			mTracker.trackEvent("BeerList", "ShowLocation", "Clicked", 0);
			mTracker.dispatch();
			viewMap(info.id);
			return true;
		case SHARE_WITH_COMMUNITY_ID:
			mTracker.trackEvent("BeerList", "ShareWithCommunity", "Clicked", 0);
			mTracker.dispatch();
			shareWithCommunity(info.id);
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
	protected void onListItemClick(ListView l, View v, int menuPosition, long id) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onListItemClick");
		}
		super.onListItemClick(l, v, menuPosition, id);
		Intent i = new Intent(this, BeerView.class);
		i.putExtra(NotesDbAdapter.KEY_ROWID, id);
		startActivityForResult(i, ACTIVITY_VIEW);
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
		if (Logger.isLogEnabled())  Logger.log("onActivityResult; request code = " + requestCode
				+ " result code = " + resultCode);
		Bundle extras = (intent != null) ? intent.getExtras() : null;
		if (requestCode == FACEBOOK_LOGIN_INTERCEPT_REQUEST_CODE_FOR_WALL_POST) {
			if (resultCode == RESULT_OK) {
				// pass the rowId along to ShareOnFacebook
				long rowId = (extras != null) ? extras
						.getLong(NotesDbAdapter.KEY_ROWID) : 0L;
				if (Logger.isLogEnabled())  Logger.log("onActivityResult:Row Id=" + rowId);
				Intent newIntent = new Intent(this, ShareOnFacebook.class);
				newIntent.putExtra(NotesDbAdapter.KEY_ROWID, rowId);
				startActivityForResult(newIntent, ACTIVITY_SHARE_ON_FACEBOOK);
			}
		} else if (requestCode == SHARE_WITH_COMMUNITY_ACTIVITY_REQUEST_CODE) {
			// Toast thanks for sharing
			Toast.makeText(mMainActivity, R.string.on_sharing_with_community,
					Toast.LENGTH_LONG).show();
			fillData();
		} else {
			fillData();
			if (resultCode == AppConfig.FACEBOOK_WALL_POST_SUCCESSFUL_RESULT_CODE) {
				Toast.makeText(BeerList.this, R.string.on_facebook_wall_post,
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	/**
	 * 
	 * @param id
	 */
	private void viewMap(long id) {
		Cursor cursor = mDbHelper.fetchNote(id);
		startManagingCursor(cursor);
		String latitude = cursor.getString(cursor
				.getColumnIndex(NotesDbAdapter.KEY_LATITUDE));
		String longitude = cursor.getString(cursor
				.getColumnIndex(NotesDbAdapter.KEY_LONGITUDE));

		String _selection = latitude + "," + longitude;

		mTracker.trackEvent("BeerList", "ShowLocation", _selection, 0);
		mTracker.dispatch();
//		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + latitude
//				+ "," + longitude + "?z=" + AppConfig.GOOGLE_MAPS_ZOOM_LEVEL));
		Intent i = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps?" + "z="
						+ AppConfig.GOOGLE_MAPS_ZOOM_LEVEL + "&t=m" + "&q=loc:"
						+ latitude + "," + longitude));
		startActivity(i);
	}

	/**
	 * 
	 */
	private void fillData() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("fillData");
		}
		// default to key_updated
		String _sortBy = mPreferences.getString("SORT_BY", null);
		if (_sortBy == null) {
			mPreferences.edit().putString("SORT_BY",
					NotesDbAdapter.KEY_UPDATED + " DESC").commit();
			_sortBy = NotesDbAdapter.KEY_UPDATED + " DESC";
		}
		if (Logger.isLogEnabled())  Logger.log("fillData::sort by:: " + _sortBy);
		int rowsPerPage = mPreferences.getInt(
				AppConfig.PREFERENCE_BEER_LIST_ROWS_PER_PAGE,
				AppConfig.BEER_LIST_ROWS_PER_PAGE);

		Cursor notesCursor = mDbHelper.fetchAllNotes(mPageNumber, rowsPerPage, _sortBy);
		startManagingCursor(notesCursor);
		if (Logger.isLogEnabled())  Logger.log("fillData::mPreviousCursorCount=" + mPreviousCursorCount);
		if (Logger.isLogEnabled())  Logger.log("fillData::mCurrentCursorCount=" + notesCursor.getCount());
		// current row count returned is less than what it should have returned
		if ((mLoadMoreBeersAction)
				&& (notesCursor != null)
				&& (notesCursor.getCount() < (mPreviousCursorCount + rowsPerPage))) {
			if (Logger.isLogEnabled())  Logger.log("fillData::Removing Footer View");
			mBeerListView.removeFooterView(mFooterView);
		} else {
			mPreviousCursorCount = notesCursor.getCount();
		}
		BeerListResourceCursorAdapter listAdapter = new BeerListResourceCursorAdapter(
				BeerList.this, notesCursor);

		setListAdapter(listAdapter);
		// Display only for the first page load and not for each page load
		if (!mLoadMoreBeersAction) {
			Toast.makeText(this, R.string.hint_beer_list_page,
					Toast.LENGTH_LONG).show();
		}

		// make spinner invisible
		((ProgressBar) mFooterView.findViewById(R.id.spinner))
				.setVisibility(View.INVISIBLE);
		// reset mLoadMoreBeersAction
		mLoadMoreBeersAction = false;

	}

	/**
	 * 
	 */
	private void backup() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("backup");
		}
		showDialog(AppConfig.DIALOG_LOADING_ID);
		Intent intent = new Intent(this, ManageData.class);
		startActivityForResult(intent, ACTIVITY_BACKUP);

	}

	private void search() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("search");
		}
		showDialog(AppConfig.DIALOG_LOADING_ID);
		Intent intent = new Intent(this, Search.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(intent);

	}

	private void sort() {
		if (Logger.isLogEnabled())  Logger.log("sort");
		final Dialog dialog = new Dialog(mMainActivity,
				android.R.style.Theme_Dialog);
		dialog.setContentView(R.layout.sort_list);
		dialog.setTitle(R.string.sort_by_dialog_title);

		final RadioButton byUpdated = (RadioButton) dialog
				.findViewById(R.id.sort_by_updated);
		final RadioButton byBeer = (RadioButton) dialog
				.findViewById(R.id.sort_by_beer);
		final RadioButton byStyle = (RadioButton) dialog
				.findViewById(R.id.sort_by_style);
		final RadioButton byCountry = (RadioButton) dialog
				.findViewById(R.id.sort_by_country);
		Button sort = (Button) dialog.findViewById(R.id.sort_button);
		sort.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String _sortBy = null;
				if (byUpdated.isChecked() == true) {
					_sortBy = NotesDbAdapter.KEY_UPDATED + " DESC";
				} else if (byBeer.isChecked() == true) {
					_sortBy = NotesDbAdapter.KEY_BEER;
				} else if (byStyle.isChecked() == true) {
					_sortBy = NotesDbAdapter.KEY_STYLE;
				} else if (byCountry.isChecked() == true) {
					_sortBy = NotesDbAdapter.KEY_COUNTRY;
				}

				mPreferences.edit().putString("SORT_BY", _sortBy).commit();
				fillData();
				dialog.cancel();
			}
		});

		String _sortByPreference = mPreferences.getString("SORT_BY",
				NotesDbAdapter.KEY_UPDATED + " DESC");
		if (Logger.isLogEnabled())  Logger.log("sort::sort by preference:: " + _sortByPreference);

		if (_sortByPreference.equals(NotesDbAdapter.KEY_UPDATED + " DESC")) {
			byUpdated.setChecked(true);
		} else if (_sortByPreference.equals(NotesDbAdapter.KEY_BEER)) {
			byBeer.setChecked(true);
		} else if (_sortByPreference.equals(NotesDbAdapter.KEY_STYLE)) {
			byStyle.setChecked(true);
		} else if (_sortByPreference.equals(NotesDbAdapter.KEY_COUNTRY)) {
			byCountry.setChecked(true);
		}
		dialog.show();

	}

	/**
	 * 
	 */
	private void sendErrorReport() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("Send Error Report");
		}
		Intent intent = new Intent(BeerList.this.getApplication(),
				CollectAndSendLog.class);
		// intent.putExtra("LOCATION", selectedLocation);
		startActivity(intent);

	}

	/**
	 * 
	 * @param rowId
	 */
	private void shareOnFacebook(long rowId) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("shareOnFacebook");
		}
		showDialog(AppConfig.DIALOG_LOADING_ID);
		// Intent intent = new Intent(this, ShareOnFacebook.class);
		// intent.putExtra(NotesDbAdapter.KEY_ROWID, rowId);
		// startActivityForResult(intent, ACTIVITY_SHARE);
		Intent intent = new Intent(mMainActivity.getApplication(),
				LoginIntercept.class);
		intent.putExtra("FACEBOOK_PERMISSIONS", AppConfig.FACEBOOK_PERMISSIONS);
		intent.putExtra(NotesDbAdapter.KEY_ROWID, rowId);
		if (Logger.isLogEnabled())  Logger.log("shareOnFacebook:Row Id=" + rowId);
		startActivityForResult(intent,
				FACEBOOK_LOGIN_INTERCEPT_REQUEST_CODE_FOR_WALL_POST);

	}

	/**
	 * 
	 * @param rowId
	 */
	private void shareWithCommunity(long rowId) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("shareOnFacebook");
		}
		showDialog(AppConfig.DIALOG_LOADING_ID);
		{
			// update note
			Note note = new Note();
			note.id = rowId;
			note.share = "Y";
			mDbHelper.updateNote(note);
		}
		Intent intent = new Intent(mMainActivity.getApplication(),
				ShareWithCommunity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(NotesDbAdapter.KEY_ROWID, rowId);
		intent.putExtra("UPLOAD_PHOTO", true);
		intent.putExtra("INTERCEPT",
				AppConfig.SHARE_WITH_COMMUNITY_INTERCEPT_IF_NOT_LOGGED_IN);
		intent.putExtra("ACTION", AppConfig.ACTION_UPDATE);
		startActivityForResult(intent,
				SHARE_WITH_COMMUNITY_ACTIVITY_REQUEST_CODE);

		if (Logger.isLogEnabled())  Logger.log("Intent Share With Community Started");

	}

	private void aboutThisBeer(long rowId) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("aboutThisBeer");
		}
		Cursor cursor = mDbHelper.fetchNote(rowId);
		startManagingCursor(cursor);
		String _beerStyle = cursor.getString(cursor
				.getColumnIndexOrThrow(NotesDbAdapter.KEY_STYLE));

		String _beerStyleUri = _beerStyle.replace(" ", "_");
		String _url = AppConfig.WIKIPEDIA_REF_URL + _beerStyleUri;

		mTracker.trackEvent("BeerList", "MoreAboutThisBeer", _beerStyleUri, 0);
		mTracker.dispatch();

		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("aboutThisBeer:URL:" + _url);
		}
		openBrowser(_url, _beerStyle);
	}

	private void openBrowser(String url, String beer) {
		Intent intent = new Intent(BeerList.this.getApplication(),
				BeerWebView.class);
		intent.putExtra("URL", url);
		intent.putExtra("TITLE", beer);
		startActivity(intent);

	}

	private void addBeer() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("addBeer");
		}
		Intent i = new Intent(this, BeerEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}

	/**
	 * 
	 * @author gaindans
	 * 
	 */
	private class BeerListResourceCursorAdapter extends ResourceCursorAdapter {

		public BeerListResourceCursorAdapter(Context context, Cursor cur) {
			super(context, R.layout.beer_list_row, cur);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.widget.ResourceCursorAdapter#newView(android.content.Context,
		 * android.database.Cursor, android.view.ViewGroup)
		 */
		@Override
		public View newView(Context context, Cursor cur, ViewGroup parent) {
			LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return li.inflate(R.layout.beer_list_row, parent, false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.CursorAdapter#bindView(android.view.View,
		 * android.content.Context, android.database.Cursor)
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			String text1 = null;

			String alcohol = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_ALCOHOL));
			text1 = (!alcohol.equals("")) ? cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_BEER))
					+ " " + alcohol + "%" : cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_BEER));
			int _rating = cursor.getInt(cursor
					.getColumnIndex(NotesDbAdapter.KEY_RATING));

			TextView beer = (TextView) view.findViewById(R.id.text1);
			if (beer != null) {
				beer.setText(text1);
			}
			String state = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_STATE));
			String country = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_COUNTRY));
			String text2 = (!state.equals("")) ? (state + " " + country)
					: country;
			TextView textView2 = ((TextView) view.findViewById(R.id.text2));
			if (textView2 != null) {
				textView2.setText(text2);
			}
			TextView beerDate = (TextView) view.findViewById(R.id.text3);
			if (beerDate != null) {
				beerDate.setText(dateFormat.format(cursor.getLong(cursor
						.getColumnIndex(NotesDbAdapter.KEY_UPDATED))));
			}
			RatingBar ratingBar = (RatingBar) view
					.findViewById(R.id.list_ratingbar);

			if (ratingBar != null) {
				ratingBar.setRating(_rating);
			} else {
				if (AppConfig.LOGGING_ENABLED) {
					if (Logger.isLogEnabled())  Logger.log("Rating Bar is Null!");
				}
			}

			String _mRowId = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_ROWID));

			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG,
						"BeerListResourceCursorAdapter->bindView(): Cursor count::"
								+ cursor.getPosition() + "::" + text1 + "::"
								+ _rating + "::" + _mRowId);
			}
			ImageView thumbnailView = (ImageView) view
					.findViewById(R.id.list_thumbnail);
			if (thumbnailView != null) {
				setThumbnailView(thumbnailView, _mRowId);
				// thumbnailView.setScaleType(ImageView.ScaleType.CENTER);
			}

		}

		/**
		 * 
		 */
		private void setThumbnailView(ImageView thumbnailView, String mRowId) {
			File thumbnail = null;
			if (thumbnailsDir != null && thumbnailsDir.exists()) {
				thumbnail = new File(thumbnailsDir, mRowId
						+ AppConfig.PICTURES_THUMBNAILS_EXTENSION);
				if (thumbnail != null && thumbnail.exists()) {
					if (thumbnail != null) {
						Bitmap image;
						try {
							BitmapScaler bitmapScaler = new BitmapScaler(
									thumbnail, AppConfig.LIST_THUMBNAIL_WIDTH);
							Bitmap thumbnailBitmap = bitmapScaler.getScaled();
							thumbnailView.setImageBitmap(thumbnailBitmap);
							if (AppConfig.LOGGING_ENABLED) {
								Log.i(TAG,
										"BeerListResourceCursorAdapter->setThumbnailView():Setting "
												+ thumbnail.getPath() + " for "
												+ mRowId);
							}
							ViewImageOnClickListener _onClickListener = new ViewImageOnClickListener();
							_onClickListener.id = mRowId;
							thumbnailView.setOnClickListener(_onClickListener);
						} catch (Throwable e) {
							Log.e(TAG, e.getMessage(), e);
						}
					}
				} else {
					if (AppConfig.LOGGING_ENABLED) {
						Log.i(TAG,
								"BeerListResourceCursorAdapter->setThumbnailView(): Thumbnail not found for "
										+ mRowId);
					}
					// if not then bind the default icon
					thumbnailView.setImageResource(R.drawable.bottle);
					if (AppConfig.LOGGING_ENABLED) {
						Log
								.i(
										TAG,
										"BeerListResourceCursorAdapter->setThumbnailView(): Setting default thumbnail for "
												+ mRowId);
					}
				}
			} else {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG,
							"BeerListResourceCursorAdapter->setThumbnailView():"
									+ AppConfig.PICTURES_THUMBNAILS_DIR
									+ " not found");
				}
			}
		}
	}

	/***************************************************************/
	class ViewImageOnClickListener implements View.OnClickListener {
		String id;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG,
						"ViewImageOnClickListener->onClick()::View Image for "
								+ id);
			}
			showDialog(AppConfig.DIALOG_LOADING_ID);
			Intent intent = new Intent(v.getContext(), ViewImage.class);
			intent.putExtra(NotesDbAdapter.KEY_ROWID, Long.valueOf(id));
			startActivity(intent);
		}

	}

	/***************************************************************/
	class ShareOnFacebookOnClickListener implements View.OnClickListener {
		Long id;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG,
						"ShareOnFacebookOnClickListener->onClick()::Share on Facebook "
								+ id);
			}
			mTracker
					.trackEvent("BeerList", "ShareOnFacebookIcon", "Clicked", 0);
			mTracker.dispatch();
			showDialog(AppConfig.DIALOG_LOADING_ID);
			shareOnFacebook(id);
		}

	}
}
