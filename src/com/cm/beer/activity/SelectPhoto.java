package com.cm.beer.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;

public class SelectPhoto extends Activity {
	/** Called when the activity is first created. */
	private Cursor mImageCursor;
	private int mImageColumnIndex, mActualImageColumnIndex;
	GridView mImageGrid;
	private int mCount;
	String mRowId;
	String mStorage;
	ProgressDialog mDialog;

	SelectPhoto mMainActivity;
	static String TAG;

	protected static final int SELECT_IMAGE_REQUEST_CODE = 5;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		mMainActivity = this;

		Bundle extras = getIntent().getExtras();
		mRowId = extras != null ? extras.getString("ROWID") : null;
		Log.i(TAG, "onCreate: mRowId: " + mRowId);
		mStorage = extras != null ? extras.getString("STORAGE") : "EXTERNAL";
		Log.i(TAG, "onCreate: mStorage: " + mStorage);
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
				.parse("file://" + Environment.getExternalStorageDirectory())));
		// new MediaScannerConnection(this, null).scanFile(Environment
		// .getExternalStorageDirectory().getPath(), null);
		if (isExternalStorageAvail()) {
			new AsyncInitImageGrid().execute("");

		} else {
			Toast.makeText(this,
					getString(R.string.external_storage_not_available_message),
					Toast.LENGTH_SHORT).show();
		}

	}

	private boolean isExternalStorageAvail() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	private void initImageGrid() {
		setContentView(R.layout.user_photo_gallery);
		String[] img = { MediaStore.Images.Thumbnails._ID };
		Uri uri = null;
		if (mStorage.equals("EXTERNAL")) {
			uri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
		} else {
			uri = MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI;
		}
		Log.i(TAG, "initImageGrid: Storage Uri: " + uri.toString());
		mImageCursor = managedQuery(uri, img, null, null,
				MediaStore.Images.Thumbnails.IMAGE_ID + "");
		mImageColumnIndex = mImageCursor
				.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
		mCount = mImageCursor.getCount();
		if(mCount == 0){
			Toast.makeText(this,
					getString(R.string.external_storage_no_images),
					Toast.LENGTH_LONG).show();
			
		}
		mImageGrid = (GridView) findViewById(R.id.UserPhotoGrid);
		mImageGrid.setAdapter(new ImageAdapter(getApplicationContext()));
		mImageGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View v, int position,
					long id) {
				System.gc();
				String[] proj = { MediaStore.Images.Media.DATA };
				Uri _uri = null;

				if (mStorage.equals("EXTERNAL")) {
					_uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else {
					_uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
				}
				Log.i(TAG, "onItemClick: Storage Uri: " + _uri.toString());
				Cursor mActualimagecursor = managedQuery(_uri, proj, null,
						null, null);
				mActualImageColumnIndex = mActualimagecursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				mActualimagecursor.moveToPosition(position);
				String _fileName = mActualimagecursor
						.getString(mActualImageColumnIndex);
				System.gc();
				Log.i(TAG, "onItemClick: FILENAME: " + _fileName);

				Intent intent = new Intent(getApplicationContext(),
						UploadPhoto.class);
				intent.putExtra("ROWID", mRowId);
				intent.putExtra("FILENAME", _fileName);
				Log.i(TAG, "mImageGrid.setOnItemClickListener: FILENAME: "
						+ _fileName);
				startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
			}
		});
	}

	/**
	 * 
	 * @author gaindans
	 * 
	 */
	public class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return mCount;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ImageView i;
			if (convertView == null) {
				i = new ImageView(mContext);
			} else {
				i = (ImageView) convertView;
			}
			mImageCursor.moveToPosition(position);
			int id = mImageCursor.getInt(mImageColumnIndex);
			Uri _uri = Uri.withAppendedPath(
					MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, "" + id);
			Log.i(TAG, "ImageAdapter.getView(): FILENAME: " + _uri.toString());
			i.setImageURI(_uri);
			i.setScaleType(ImageView.ScaleType.CENTER_CROP);
			i.setLayoutParams(new GridView.LayoutParams(92, 92));
			System.gc();
			return i;
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
		}

		mDialog = ProgressDialog.show(mMainActivity, null, dialogMessage, true,
				true);
		mDialog.setCanceledOnTouchOutside(false);
		return mDialog;
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
		if (requestCode == SELECT_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				mMainActivity.setResult(RESULT_OK, intent);
				mMainActivity.finish();
			}
		}
	}

	/************************************************************************************/
	private class AsyncInitImageGrid extends AsyncTask<Object, Void, Object> {

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute starting");
			if (mMainActivity != null) {
				mMainActivity.showDialog(AppConfig.DIALOG_LOADING_ID);
			}
			Log.i(TAG, "onPreExecute finished");
		}

		/**
		 * 
		 * @param args
		 * @return null
		 */
		@Override
		protected Void doInBackground(Object... args) {
			Log.i(TAG, "doInBackground starting");
			try {
				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						mMainActivity.initImageGrid();
					}
				});

			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
			}

			Log.i(TAG, "doInBackground finished");
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			Log.i(TAG, "onPostExecute starting");
			mDialog.cancel();
			Log.i(TAG, "onPostExecute finished");
		}

	}

}