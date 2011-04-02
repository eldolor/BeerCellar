package com.cm.beer.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class ViewImage extends Activity {
	String TAG;
	ProgressDialog mDialog;

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

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the mTracker with dispatch interval
		mTracker.start(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}

		setContentView(R.layout.view_image);
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

		File picturesDir = new File(AppConfig.PICTURES_DIR);
		File photo = null;
		if (picturesDir != null && picturesDir.exists()) {
			photo = new File(picturesDir, mRowId + AppConfig.PICTURES_EXTENSION);
		}
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "VVWListAdapter->bindView():Image file "
					+ photo.getPath());
		}

		if (photo != null) {
			ImageView photoView = (ImageView) findViewById(R.id.beer_picture);
			if (photoView != null) {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG, "VVWListAdapter->bindView():Binding file "
							+ photo.getPath());
				}
				Bitmap image;
				BufferedInputStream phInStream = null;
				try {
					phInStream = new BufferedInputStream(photo.toURL()
							.openStream());
					BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
					bitmapFactoryOptions.inSampleSize = 2;
					image = BitmapFactory.decodeStream(phInStream, null,
							bitmapFactoryOptions);
					photoView.setImageBitmap(image);
				} catch (Throwable e) {
					Log.e(TAG, e.getMessage(), e);
					mTracker.trackEvent("ViewImage", "ViewImageError", (e
							.getMessage() != null) ? e.getMessage().replace(
							" ", "_") : "", 0);
					mTracker.dispatch();
				} finally {
					try {
						if (phInStream != null) {
							phInStream.close();
						}
					} catch (IOException e) {
						Log.e(TAG, e.getMessage(), e);
						mTracker.trackEvent("ViewImage", "ViewImageError", (e
								.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "", 0);
						mTracker.dispatch();
					}
				}
			}

		}
		ImageView backButton = (ImageView) this.findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				finish();
			}
		});

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
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreateDialog");
		}
		mDialog = ProgressDialog.show(ViewImage.this, null, this
				.getString(R.string.progress_loading_message), true, true);
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
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
			removeDialog(AppConfig.DIALOG_LOADING_ID);
		}
		super.onResume();
	}

}
