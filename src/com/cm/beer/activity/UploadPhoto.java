package com.cm.beer.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.BitmapScaler;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class UploadPhoto extends Activity {
	String mFileName;
	String mRowId;
	String TAG;
	ProgressDialog mDialog;
	Activity mMainActivity;

	GoogleAnalyticsTracker mTracker;

	ImageView mUserPhotoView;
	Button mUploadPhoto;

	static final int DIALOG_UPLOADING_USER_PHOTO_ID = 1;

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

		Bundle extras = getIntent().getExtras();
		mRowId = extras != null ? extras.getString("ROWID") : null;
		mFileName = extras != null ? extras.getString("FILENAME") : null;
		Log.i(TAG, "onCreate: FILENAME: " + mFileName + " mRowId: " + mRowId);
		setContentView(R.layout.view_photo);
		setup();
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
		if (id == DIALOG_UPLOADING_USER_PHOTO_ID) {
			dialogMessage = this.getString(R.string.progress_saving_message);
		}

		mDialog = ProgressDialog.show(mMainActivity, null, dialogMessage, true,
				true);
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
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

	private void setup() {
		/****************************************/
		mUserPhotoView = (ImageView) findViewById(R.id.user_photo);
		if (mFileName != null) {
			try {
				BitmapScaler bitmapScaler = new BitmapScaler(
						new File(mFileName), AppConfig.THUMBNAIL_WIDTH);
				Bitmap thumbnailBitmap = bitmapScaler.getScaled();
				mUserPhotoView.setImageBitmap(thumbnailBitmap);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}

		}
		/****************************************/
		mUploadPhoto = (Button) findViewById(R.id.upload_photo);
		mUploadPhoto.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mUploadPhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new SavePhotoTask().execute("");
			}
		});

	}

	/************************************************************************************/
	/**
	 * 
	 * @author gaindans
	 * 
	 */
	private class SavePhotoTask extends AsyncTask<String, String, Boolean> {
		private static final String _TAG = "UploadPhoto::SavePhotoTask";

		@Override
		protected void onPreExecute() {
			System.gc();
			Log.i(TAG, "onPreExecute starting");
			if (mMainActivity != null) {
				mMainActivity.showDialog(DIALOG_UPLOADING_USER_PHOTO_ID);
			}
			Log.i(TAG, "onPreExecute finished");
		}

		@Override
		protected Boolean doInBackground(String... args) {
			int result = RESULT_OK;

			if (AppConfig.LOGGING_ENABLED) {
				Log.i(_TAG, "doInBackground");
			}
			FileOutputStream pictureFos = null;
			File baseDir = null;
			File picturesDir = null;
			File thumbnailsDir = null;
			File photo = null;
			FileOutputStream thumbnailFos = null;
			Bitmap image = null;

			try {
				// Save Picture
				baseDir = new File(AppConfig.BASE_APP_DIR);
				picturesDir = new File(AppConfig.PICTURES_DIR);
				thumbnailsDir = new File(AppConfig.PICTURES_THUMBNAILS_DIR);

				// create appropriate directories if necessary
				createDirectories(baseDir, picturesDir, thumbnailsDir);

				photo = new File(picturesDir, mRowId
						+ AppConfig.PICTURES_EXTENSION);

				if (photo != null && photo.exists()) {
					if (AppConfig.LOGGING_ENABLED) {
						Log.i(_TAG, "doInBackground::deleting "
								+ photo.getPath());
					}
					photo.delete();
				}

				pictureFos = new FileOutputStream(photo.getPath());
				FileInputStream input = null;
				try {
					input = new FileInputStream(new File(mFileName));
					int byteCount = 0;
					byte[] buffer = new byte[1024];
					for (int length = 0; (length = input.read(buffer)) > 0;) {
						pictureFos.write(buffer, 0, length);
						byteCount += length;
					}
					pictureFos.flush();
					Log.d(_TAG, byteCount + " bytes flushed for "
							+ photo.getName());
				} finally {
					if (input != null)
						try {
							input.close();
						} catch (IOException e) {
							Log.e(_TAG, (e.getMessage() != null) ? e
									.getMessage().replace(" ", "_") : "", e);
						}
				}
				System.gc();

				if (AppConfig.LOGGING_ENABLED) {
					Log.i(_TAG, "doInBackground::created file "
							+ photo.getPath());
				}
				BitmapScaler bitmapScaler = new BitmapScaler(photo,
						AppConfig.THUMBNAIL_WIDTH);
				Bitmap thumbnailBitmap = bitmapScaler.getScaled();
				File thumbnail = new File(thumbnailsDir, mRowId
						+ AppConfig.PICTURES_THUMBNAILS_EXTENSION);

				if (thumbnail != null && thumbnail.exists()) {
					if (AppConfig.LOGGING_ENABLED) {
						Log.i(_TAG, "doInBackground::deleting "
								+ thumbnail.getPath());
					}
					thumbnail.delete();
				}
				thumbnailFos = new FileOutputStream(thumbnail.getPath());
				if (!thumbnailBitmap.compress(CompressFormat.JPEG, 100,
						thumbnailFos)) {
					mTracker.trackEvent("CameraPreview", "SaveThumbnailError",
							"Unable_To_Save_Thumbnail", 0);
					mTracker.dispatch();
					throw new Exception("Unable_To_Save_Thumbnail");
				}
				thumbnailFos.flush();

				System.gc();

				if (AppConfig.LOGGING_ENABLED) {
					Log.i(_TAG, "doInBackground::created file "
							+ thumbnail.getPath());
				}
			} catch (Exception e) {
				Log.e(_TAG, "Exception in ", e);
				Log.e(_TAG, (e.getMessage() != null) ? e.getMessage().replace(
						" ", "_") : "", e);
				mTracker.trackEvent("BeerEdit", "SavePhotoError", (e
						.getMessage() != null) ? e.getMessage().replace(" ",
						"_") : "".replace(" ", "_"), 0);
				mTracker.dispatch();
				result = RESULT_CANCELED;
			} finally {
				if (pictureFos != null) {
					try {
						pictureFos.close();
					} catch (IOException e) {
						Log.e(_TAG, (e.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "", e);
						mTracker.trackEvent("BeerEdit", "SavePhotoError", ((e
								.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : ""), 0);
						mTracker.dispatch();
					}
				}
				if (thumbnailFos != null) {
					try {
						thumbnailFos.close();
					} catch (IOException e) {
						Log.e(_TAG, (e.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "", e);
						mTracker.trackEvent("BeerEdit", "SavePhotoError", ((e
								.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : ""), 0);
						mTracker.dispatch();
					}
				}

				System.gc();
			}

			setResult(result);

			return (result == RESULT_OK) ? new Boolean(true) : new Boolean(
					false);
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(final Boolean success) {
			Log.i(TAG, "onPostExecute starting");
			mDialog.cancel();
			Intent intent = new Intent();
			mMainActivity.setResult(RESULT_OK, intent);
			mMainActivity.finish();
			Log.i(TAG, "onPostExecute finished");

		}

		/**
		 * Creates appropriate directories if necessary
		 * 
		 * @param baseDir
		 * @param picturesDir
		 * @param thumbnailsDir
		 */
		private void createDirectories(File baseDir, File picturesDir,
				File thumbnailsDir) {
			// 1. Base dir does not exist
			if (baseDir != null && (!baseDir.exists())) {
				// 1.1 Create new base dir
				if (baseDir.mkdir()) {
					if (AppConfig.LOGGING_ENABLED) {
						Log.i(_TAG, "doInBackground::Created "
								+ baseDir.getPath());
					}
					// 1.2 Create new pictures dir
					if (picturesDir.mkdir()) {
						if (AppConfig.LOGGING_ENABLED) {
							Log.i(_TAG, "doInBackground::Created "
									+ picturesDir.getPath());
						}
						// 1.3 Create new thumbnails dir
						if (thumbnailsDir.mkdir()) {
							if (AppConfig.LOGGING_ENABLED) {
								Log.i(_TAG, "doInBackground::Created "
										+ thumbnailsDir.getPath());
							}

						} else {
							if (AppConfig.LOGGING_ENABLED) {
								Log.e(_TAG, "doInBackground::Unable to create "
										+ thumbnailsDir.getPath());
							}
							setResult(RESULT_CANCELED);
							finish();
						}
					} else {
						if (AppConfig.LOGGING_ENABLED) {
							Log.e(_TAG, "doInBackground::Unable to create "
									+ picturesDir.getPath());
						}
						setResult(RESULT_CANCELED);
						finish();
					}
				} else {
					if (AppConfig.LOGGING_ENABLED) {
						Log.e(_TAG, "doInBackground::Unable to create "
								+ baseDir.getPath());
					}
					setResult(RESULT_CANCELED);
					finish();
				}
			} else if (baseDir != null && baseDir.exists()) {
				// 2. Base dir exists
				// 2.1 Pictures dir does not exist
				if (picturesDir != null && (!picturesDir.exists())) {
					// 2.1.1 create new pictures dir
					if (picturesDir.mkdir()) {
						if (AppConfig.LOGGING_ENABLED) {
							Log.i(_TAG, "doInBackground::Created "
									+ picturesDir.getPath());
						}
						// 2.1.2 create new thumbnails dir
						if (thumbnailsDir.mkdir()) {
							if (AppConfig.LOGGING_ENABLED) {
								Log.i(_TAG, "doInBackground::Created "
										+ thumbnailsDir.getPath());
							}

						} else {
							if (AppConfig.LOGGING_ENABLED) {
								Log.e(_TAG, "doInBackground::Unable to create "
										+ thumbnailsDir.getPath());
							}
							setResult(RESULT_CANCELED);
							finish();
						}
					} else {
						if (AppConfig.LOGGING_ENABLED) {
							Log.e(_TAG, "doInBackground::Unable to create "
									+ picturesDir.getPath());
						}
						setResult(RESULT_CANCELED);
						finish();
					}

				} else if (picturesDir != null && (picturesDir.exists())) {
					// 2.2 Pictures dir exists
					// 2.2.1 Thumbnails dir does not exist
					if (thumbnailsDir != null && (!thumbnailsDir.exists())) {
						// 2.2.1.1 create new thumbnails dir
						if (thumbnailsDir.mkdir()) {
							if (AppConfig.LOGGING_ENABLED) {
								Log.i(_TAG, "doInBackground::Created "
										+ thumbnailsDir.getPath());
							}

						} else {
							if (AppConfig.LOGGING_ENABLED) {
								Log.e(_TAG, "doInBackground::Unable to create "
										+ thumbnailsDir.getPath());
							}
							setResult(RESULT_CANCELED);
							finish();
						}
					}
				}

			}
		}

	}

}
