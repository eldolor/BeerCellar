package com.cm.beer.activity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.Note;
import com.cm.beer.db.NotesDbAdapter;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class SearchResults extends ListActivity {
	String TAG;

	static final int ACTIVITY_SHARE_ON_FACEBOOK = 1;
	static final int ACTIVITY_ABOUT_THIS_BEER = 2;
	static final int ACTIVITY_VIEW = 3;
	static final int ACTIVITY_SHARE_WITH_COMMUNITY = 4;

	static final Handler handler = new Handler();

	static final int MENU_GROUP = 0;
	static final int SHARE_ON_FACEBOOK_ID = Menu.FIRST + 1;
	static final int ABOUT_THIS_BEER_ID = Menu.FIRST + 2;
	static final int SEND_ERROR_REPORT_ID = Menu.FIRST + 3;
	static final int SHOW_LOCATION_ID = Menu.FIRST + 4;
	static final int SHARE_WITH_COMMUNITY_ID = Menu.FIRST + 5;

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
	Bundle mExtras;

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
			Log.i(TAG, "onCreate");
		}

		mMainActivity = this;

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the tracker with dispatch interval
		mTracker.start(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);

		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}

		showDialog(AppConfig.DIALOG_LOADING_ID);
		setContentView(R.layout.beer_list);

		mDbHelper = new NotesDbAdapter(SearchResults.this);
		mDbHelper.open();

		mBeerListView = getListView();
		registerForContextMenu(mBeerListView);
		mBeerListView.setTextFilterEnabled(true);

		mExtras = getIntent().getExtras();
		((TextView) findViewById(android.R.id.empty))
				.setText(R.string.search_result_no_beers);
		// initialize Footer View for the list
		initFooterView();
		fillData();

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
			Log.i(TAG, "onCreateDialog");
		}
		String dialogMessage = null;
		if (id == AppConfig.DIALOG_LOADING_ID) {
			dialogMessage = this.getString(R.string.progress_loading_message);
			ACTIVE_DIALOG = AppConfig.DIALOG_LOADING_ID;
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
			Log.i(TAG, "onResume");
		}
		if ((dialog != null) && (dialog.isShowing())) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onResume:active dialog removed");
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
			Log.i(TAG, "onCreateOptionsMenu");
		}
		super.onCreateOptionsMenu(menu);
		int menuPosition = 0;
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
			Log.i(TAG, "onMenuItemSelected");
		}
		switch (item.getItemId()) {
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
		Log.i(TAG, "onCreateContextMenu");
		int menuPosition = 0;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Cursor cursor = mDbHelper.fetchNote(info.id);
		startManagingCursor(cursor);
		if (cursor != null && (cursor.isAfterLast() == false)) {
			String latitude = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_LATITUDE));
			if ((latitude != null) && (!latitude.equals("0.0"))) {
				String longitude = cursor.getString(cursor
						.getColumnIndex(NotesDbAdapter.KEY_LONGITUDE));
				if ((longitude != null) && (!longitude.equals("0.0"))) {
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
			Log.i(TAG, "onContextItemSelected");
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
			Log.i(TAG, "onListItemClick");
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
		Log.i(TAG, "onActivityResult");
		Bundle extras = (intent != null) ? intent.getExtras() : null;
		if (requestCode == FACEBOOK_LOGIN_INTERCEPT_REQUEST_CODE_FOR_WALL_POST) {
			if (resultCode == RESULT_OK) {
				// pass the rowId along to ShareOnFacebook
				long rowId = (extras != null) ? extras
						.getLong(NotesDbAdapter.KEY_ROWID) : 0L;
				Log.i(TAG, "onActivityResult:Row Id=" + rowId);
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
				Toast.makeText(mMainActivity, R.string.on_facebook_wall_post,
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
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + latitude
				+ "," + longitude + "?z=" + AppConfig.GOOGLE_MAPS_ZOOM_LEVEL));
		startActivity(i);
	}

	/**
	 * 
	 */
	private void fillData() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "fillData(extras)");
		}
		String _beer = mExtras != null ? mExtras
				.getString(NotesDbAdapter.KEY_BEER) : null;
		String _rating = mExtras != null ? mExtras
				.getString(NotesDbAdapter.KEY_RATING) : null;
		String _price = mExtras != null ? mExtras
				.getString(NotesDbAdapter.KEY_PRICE) : null;
		String _alcohol = mExtras != null ? mExtras
				.getString(NotesDbAdapter.KEY_ALCOHOL) : null;
		String _style = mExtras != null ? mExtras
				.getString(NotesDbAdapter.KEY_STYLE) : null;
		String _brewery = mExtras != null ? mExtras
				.getString(NotesDbAdapter.KEY_BREWERY) : null;
		String _state = mExtras != null ? mExtras
				.getString(NotesDbAdapter.KEY_STATE) : null;
		String _country = mExtras != null ? mExtras
				.getString(NotesDbAdapter.KEY_COUNTRY) : null;
		String _share = mExtras != null ? mExtras
				.getString(NotesDbAdapter.KEY_SHARE) : null;

		Cursor notesCursor = null;
		if ((_beer == null) && (_rating == null) && (_price == null)
				&& (_alcohol == null) && (_style == null) && (_brewery == null)
				&& (_state == null) && (_country == null) && (_share == null)) {
			notesCursor = mDbHelper.fetchAllNotes();
		} else {
			notesCursor = mDbHelper.fetchNotes(_beer, _rating, _price,
					_alcohol, _style, _brewery, _state, _country, _share,
					mPageNumber, AppConfig.BEER_LIST_ROWS_PER_PAGE);
		}
		startManagingCursor(notesCursor);
		Log.i(TAG, "fillData::mPreviousCursorCount=" + mPreviousCursorCount);
		Log.i(TAG, "fillData::mCurrentCursorCount=" + notesCursor.getCount());
		// current row count returned is less than what it should have returned
		if ((mLoadMoreBeersAction)
				&& (notesCursor != null)
				&& (notesCursor.getCount() < (mPreviousCursorCount + AppConfig.BEER_LIST_ROWS_PER_PAGE))) {
			Log.i(TAG, "fillData::Removing Footer View");
			mBeerListView.removeFooterView(mFooterView);
		} else {
			mPreviousCursorCount = notesCursor.getCount();
		}
		BeerListResourceCursorAdapter mListAdapter = new BeerListResourceCursorAdapter(
				SearchResults.this, notesCursor);
		setListAdapter(mListAdapter);

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
	private void sendErrorReport() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "Send Error Report");
		}
		Intent intent = new Intent(SearchResults.this.getApplication(),
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
			Log.i(TAG, "shareOnFacebook");
		}
		showDialog(AppConfig.DIALOG_LOADING_ID);
		Intent intent = new Intent(mMainActivity.getApplication(),
				LoginIntercept.class);
		intent.putExtra("FACEBOOK_PERMISSIONS", AppConfig.FACEBOOK_PERMISSIONS);
		intent.putExtra(NotesDbAdapter.KEY_ROWID, rowId);
		Log.i(TAG, "shareOnFacebook:Row Id=" + rowId);
		startActivityForResult(intent,
				FACEBOOK_LOGIN_INTERCEPT_REQUEST_CODE_FOR_WALL_POST);

	}

	/**
	 * 
	 * @param rowId
	 */
	private void shareWithCommunity(long rowId) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "shareOnFacebook");
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

		Log.i(TAG, "Intent Share With Community Started");

	}

	private void aboutThisBeer(long rowId) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "aboutThisBeer");
		}
		Cursor cursor = mDbHelper.fetchNote(rowId);
		startManagingCursor(cursor);
		String _beer = cursor.getString(cursor
				.getColumnIndexOrThrow(NotesDbAdapter.KEY_BEER));

		String _beerUri = _beer.replace(" ", "_");
		String _url = AppConfig.WIKIPEDIA_REF_URL + _beerUri;

		mTracker.trackEvent("BeerList", "MoreAboutThisBeer", _beerUri, 0);
		mTracker.dispatch();

		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "aboutThisBeer:URL:" + _url);
		}
		openBrowser(_url, _beer);
	}

	private void openBrowser(String url, String beer) {
		Intent intent = new Intent(SearchResults.this.getApplication(),
				BeerWebView.class);
		intent.putExtra("URL", url);
		intent.putExtra("TITLE", beer);
		startActivity(intent);

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
			text1 = (!alcohol.equals("")) ? alcohol
					+ " "
					+ cursor.getString(cursor
							.getColumnIndex(NotesDbAdapter.KEY_BEER)) : cursor
					.getString(cursor.getColumnIndex(NotesDbAdapter.KEY_BEER));
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
					Log.i(TAG, "Rating Bar is Null!");
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
							image = BitmapFactory.decodeStream(thumbnail
									.toURL().openStream());
							thumbnailView.setImageBitmap(image);
							if (AppConfig.LOGGING_ENABLED) {
								Log.i(TAG,
										"BeerListResourceCursorAdapter->setThumbnailView():Setting "
												+ thumbnail.getPath() + " for "
												+ mRowId);
							}
							ViewImageOnClickListener _onClickListener = new ViewImageOnClickListener();
							_onClickListener.id = mRowId;
							thumbnailView.setOnClickListener(_onClickListener);
						} catch (MalformedURLException e) {
							Log.e(TAG, (e.getMessage() != null) ? e
									.getMessage().replace(" ", "_") : "", e);
						} catch (IOException e) {
							Log.e(TAG, (e.getMessage() != null) ? e
									.getMessage().replace(" ", "_") : "", e);
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
