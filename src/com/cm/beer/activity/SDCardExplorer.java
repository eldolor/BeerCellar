package com.cm.beer.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.cm.beer.config.AppConfig;

public class SDCardExplorer extends ListActivity {

	private List<String> mItem = null;
	private List<String> mPath = null;
	private String mRoot = "/";
	private TextView mMyPath;
	SDCardExplorer mMainActivity;
	String mRowId;
	String mUserId;
	static String TAG;

	int mRequestCode;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		mMainActivity = this;

		Bundle extras = getIntent().getExtras();
		mRowId = extras != null ? extras.getString("ROWID") : null;
		mUserId = extras != null ? extras.getString("USERID") : null;
		Log.i(TAG, "onCreate: mRowId: " + mRowId);
		mRequestCode = extras.getInt("REQUESTCODE");

		setContentView(R.layout.sdcard_explorer_list);
		mMyPath = (TextView) findViewById(R.id.path);
		getDir(Environment.getExternalStorageDirectory().getPath());
	}

	private void getDir(String dirPath) {
		mMyPath.setText("Location: " + dirPath);

		mItem = new ArrayList<String>();
		mPath = new ArrayList<String>();

		File f = new File(dirPath);
		File[] files = f.listFiles();
		ArrayList<HashMap<String, String>> hashMapListForListView = new ArrayList<HashMap<String, String>>();

		if (!dirPath.equals(mRoot)) {
			HashMap<String, String> entitiesHashMap = new HashMap<String, String>();

			// item.add(root);
			// path.add(root);
			// entitiesHashMap.put("thumbnail", "/");
			// entitiesHashMap.put("text1", "BACK");

			mItem.add("../");
			mPath.add(f.getParent());
			entitiesHashMap.put("thumbnail", "");
			entitiesHashMap.put("text1", "Go Back");

			hashMapListForListView.add(entitiesHashMap);

		}

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			HashMap<String, String> entitiesHashMap = new HashMap<String, String>();
			if (!file.isHidden()) {
				if (file.isDirectory()) {
					mPath.add(file.getPath());
					mItem.add(file.getName() + "/");
					entitiesHashMap.put("thumbnail", "");
					entitiesHashMap.put("text1", file.getName() + "/");
					hashMapListForListView.add(entitiesHashMap);
				} else if ((file.getName().endsWith(".jpg"))
						|| (file.getName().endsWith(".jpeg"))) {
					mPath.add(file.getPath());
					mItem.add(file.getName());
					entitiesHashMap.put("thumbnail", file.getPath());
					entitiesHashMap.put("text1", file.getName());
					hashMapListForListView.add(entitiesHashMap);
				}
			}
		}

		// SimpleAdapter adapterForList = new SimpleAdapter(this,
		// hashMapListForListView, R.layout.sdcard_explorer_row,
		// new String[] { "thumbnail", "text1" }, new int[] {
		// R.id.thumbnail, R.id.text1 });
		MyAdapter adapterForList = new MyAdapter(this, hashMapListForListView,
				R.layout.sdcard_explorer_row, new String[] { "thumbnail",
						"text1" }, new int[] { R.id.thumbnail, R.id.text1 });
		setListAdapter(adapterForList);

		/*
		 * ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,
		 * R.layout.sdcard_explorer_row, item); setListAdapter(fileList);
		 */

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.i(TAG, "onListItemClick: POSITION: " + position + " ID; " + id);

		File file = new File(mPath.get(position));
		Log.i(TAG, "onListItemClick: File: " + file.getPath());

		if (file.isDirectory()) {
			if (file.canRead())
				getDir(mPath.get(position));
			else {
				new AlertDialog.Builder(this).setIcon(R.drawable.icon)
						.setTitle(
								"[" + file.getName()
										+ "] folder can't be read!")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
									}
								}).show();
			}
		} else if (file.isFile()) {
			String _fileName = file.getPath();
			Log.i(TAG, "onListItemClick: FILENAME: " + _fileName);
			if (mRequestCode == AppConfig.SELECT_IMAGE_REQUEST_CODE) {
				Intent intent = new Intent(getApplicationContext(),
						UploadPhoto.class);
				intent.putExtra("ROWID", mRowId);
				intent.putExtra("FILENAME", _fileName);
				startActivityForResult(intent,
						AppConfig.SELECT_IMAGE_REQUEST_CODE);
			} else if (mRequestCode == AppConfig.UPDATE_USER_PHOTO_REQUEST) {
				Intent intent = new Intent(getApplicationContext(),
						UploadUserPhoto.class);
				intent.putExtra("USERID", mUserId);
				intent.putExtra("FILENAME", _fileName);
				startActivityForResult(intent,
						AppConfig.UPDATE_USER_PHOTO_REQUEST);
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
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == AppConfig.SELECT_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				mMainActivity.setResult(RESULT_OK, intent);
				mMainActivity.finish();
			}
		} else if (requestCode == AppConfig.UPDATE_USER_PHOTO_REQUEST) {
			if (resultCode == RESULT_OK) {
				mMainActivity.setResult(RESULT_OK, intent);
				mMainActivity.finish();
			}
		}

	}

	/************************************************/
	private class MyAdapter extends SimpleAdapter {

		public MyAdapter(Context context,
				List<? extends Map<String, String>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = getLayoutInflater().inflate(
					R.layout.sdcard_explorer_row, null);
			HashMap<String, String> data = (HashMap<String, String>) getItem(position);

			((TextView) convertView.findViewById(R.id.text1))
					.setText((String) data.get("text1"));
			ImageView thumbnailView = ((ImageView) convertView
					.findViewById(R.id.list_thumbnail));
			String _filename = data.get("thumbnail");
			Log.i(TAG, "MyAdapter.getView(): Filename: " + _filename);
			if ((mPath != null) && (!mPath.equals(""))) {
				File imageFile = new File(_filename);
				if ((imageFile != null) && (imageFile.exists())) {
					FileInputStream is = null;
					BufferedInputStream bis = null;
					try {
						is = new FileInputStream(new File(_filename));
						bis = new BufferedInputStream(is);
						BitmapFactory.Options bfo = new BitmapFactory.Options();
						bfo.inSampleSize = 8;
						Bitmap bitmap = BitmapFactory.decodeStream(bis, null,
								bfo);
						thumbnailView.setImageBitmap(bitmap);
						if (AppConfig.LOGGING_ENABLED) {
							Log.i(TAG,
									"MyAdapter.getView():->setThumbnailView():Setting "
											+ _filename);
						}
					} catch (Exception e) {
						Log.e(TAG, (e.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "", e);
					} finally {
						try {
							if (bis != null) {
								bis.close();
							}
							if (is != null) {
								is.close();
							}
						} catch (Exception e) {
						}

					}

				} else {
					Log.i(TAG, _filename + " does not exist");
				}
			}

			return convertView;
		}
	}

}