package com.cm.beer.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.util.BitmapScaler;
import com.cm.beer.util.Logger;
import com.cm.beer.util.Reflect;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

// ----------------------------------------------------------------------

public class CameraPreview extends Activity implements SurfaceHolder.Callback {
	String TAG;
	SurfaceHolder mSurfaceHolder;
	Camera mCamera;
	boolean mPreviewRunning;
	SurfaceView mSurfaceView;
	ProgressDialog mDialog;
	MonitorExternalStorageState mStorageMonitor;
	OrientationEventListener mOrientationEventListener;

	int mOrientation;
	int mPictureOrientation;

	// Stateful Field
	Long mRowId;

	boolean isCameraLocked;

	GoogleAnalyticsTracker mTracker;

	CameraPreview mMainActivity;

	private static final int CAMERA_SETTINGS_ACTIVITY_REQUEST_CODE = 0;

	String[] mWhiteBalanceValues;
	String mCurrentWhiteBalance;
	String[] mColorEffectValues;
	String mCurrentColorEffect;
	String[] mAntibandingValues;
	String mCurrentAntibanding;
	String[] mFocusModeValues;
	String mCurrentFocusMode;
	String[] mFlashModeValues;
	String mCurrentFlashMode;
	String[] mSceneModeValues;
	String mCurrentSceneMode;

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

		// mRowId = savedInstanceState != null ? savedInstanceState
		// .getLong(NotesDbAdapter.KEY_ROWID) : null;
		mRowId = (Long) getLastNonConfigurationInstance();

		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
					: null;
		}
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate::_id="
					+ ((mRowId != null) ? mRowId.longValue() : null));
		}

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.camera_surface);

		mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		ImageView shutter = (ImageView) findViewById(R.id.shutter);
		shutter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pictureTaken();
			}
		});
		// ORIENTATION LISTENER
		mOrientationEventListener = new OrientationEventListener(
				CameraPreview.this, SensorManager.SENSOR_DELAY_UI) {
			public void onOrientationChanged(int orientation) {
				CameraPreview.this.mOrientation = orientation;
			}
		};
		mOrientationEventListener.enable();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate: enabled orientation listener");
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
		if (requestCode == CAMERA_SETTINGS_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(mMainActivity,
						R.string.on_sharing_with_community, Toast.LENGTH_SHORT)
						.show();
				Bundle extras = getIntent().getExtras();
				mCurrentFlashMode = extras != null ? extras
						.getString("CURRENT_FLASH_MODE") : null;
				if (Logger.isLogEnabled())  Logger.log("onActivityResult():mCurrentFlashMode:"
						+ mCurrentFlashMode);
				mCurrentSceneMode = extras != null ? extras
						.getString("CURRENT_SCENE_MODE") : null;
				if (Logger.isLogEnabled())  Logger.log("onActivityResult():mCurrentSceneMode:"
						+ mCurrentSceneMode);
				mCurrentWhiteBalance = extras != null ? extras
						.getString("CURRENT_WHITE_BALANCE") : null;
				if (Logger.isLogEnabled())  Logger.log("onActivityResult():mCurrentWhiteBalance:"
						+ mCurrentWhiteBalance);
				mCurrentColorEffect = extras != null ? extras
						.getString("CURRENT_COLOR_EFFECT") : null;
				if (Logger.isLogEnabled())  Logger.log("onActivityResult():mCurrentColorEffect:"
						+ mCurrentColorEffect);
				mCurrentAntibanding = extras != null ? extras
						.getString("CURRENT_ANTIBANDING") : null;
				if (Logger.isLogEnabled())  Logger.log("onActivityResult():mCurrentAntibanding:"
						+ mCurrentAntibanding);
				mCurrentFocusMode = extras != null ? extras
						.getString("CURRENT_FOCUS_MODE") : null;
				if (Logger.isLogEnabled())  Logger.log("onActivityResult():mCurrentFocusMode:"
						+ mCurrentFocusMode);
			}
		}
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

	Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("Camera.PictureCallback");
			}
			new SavePhotoTask().execute(data);
			camera.startPreview();
		}
	};

	Camera.ErrorCallback errorCallback = new Camera.ErrorCallback() {
		@Override
		public void onError(int arg0, Camera arg1) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.e(TAG, "Camera.ErrorCallback::Error Code:" + arg0);
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRetainNonConfigurationInstance()
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		return mRowId;
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
		String _text = null;
		switch (id) {
		case AppConfig.EXTERNAL_STORAGE_NOT_AVAILABLE:
			_text = this
					.getString(R.string.external_storage_not_available_message);
			break;
		case AppConfig.EXTERNAL_STORAGE_NOT_WRITABLE:
			_text = this
					.getString(R.string.external_storage_not_writable_message);
			;
		case AppConfig.SAVING_PICTURE:
			_text = this.getString(R.string.saving_picture);
			;
		}

		mDialog = ProgressDialog.show(CameraPreview.this,
				getString(R.string.app_name), _text, true, true);
		return mDialog;
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
		if (mOrientationEventListener != null) {
			mOrientationEventListener.disable();
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("finish: disabled orientation listener");
			}
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
			if (Logger.isLogEnabled())  Logger.log("onPause");
		}
		super.onPause();
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
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onKeyDown");
		}
		if (keyCode == KeyEvent.KEYCODE_CAMERA
				|| keyCode == KeyEvent.KEYCODE_SEARCH) {
			return pictureTaken();
		} else {
			return (super.onKeyDown(keyCode, event));
		}
	}

	/**
	 * 
	 * @return
	 */
	private synchronized boolean pictureTaken() {
		if (!isCameraLocked) {
			// acquire a lock on the camera
			isCameraLocked = true;
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("pictureTaken::Camera Locked!!");
			}
			mCamera.autoFocus(new Camera.AutoFocusCallback() {
				Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
					public void onShutter() {
						// Play your sound here.
						if (AppConfig.LOGGING_ENABLED) {
							if (Logger.isLogEnabled())  Logger.log("Camera.ShutterCallback");
						}
					}
				};

				public void onAutoFocus(boolean success, Camera camera) {
					if (AppConfig.LOGGING_ENABLED) {
						if (Logger.isLogEnabled())  Logger.log("onAutoFocus");
					}
					mPictureOrientation = mOrientation;
					Log.i(TAG,
							"Camera.ShutterCallback:Orientation when picture was taken ="
									+ mOrientation);
					camera.takePicture(shutterCallback, null, pictureCallback);
					mTracker
							.trackEvent("CameraPreview", "PictureTaken", "Y", 0);
					mTracker.dispatch();
				}
			});
			return true;
		} else {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("pictureTaken::Could NOT acquire Lock on Camera");
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder
	 * )
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("surfaceCreated");
		}
		mCamera = Camera.open();

		mMainActivity.extractCameraAdvancedFeatures(mCamera.getParameters());
		ImageView settings = (ImageView) findViewById(R.id.settings);
		if ((mWhiteBalanceValues == null) && (mColorEffectValues == null)
				&& (mAntibandingValues == null) && (mFocusModeValues == null)
				&& (mFlashModeValues == null) && (mSceneModeValues == null)) {
			settings.setVisibility(View.GONE);
		} else {
			settings.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(mMainActivity,
							CameraSettings.class);
					intent
							.putExtra("WHITE_BALANCE_VALUES",
									mWhiteBalanceValues);
					intent.putExtra("CURRENT_WHITE_BALANCE",
							mCurrentWhiteBalance);
					intent.putExtra("COLOR_EFFECT_VALUES", mColorEffectValues);
					intent
							.putExtra("CURRENT_COLOR_EFFECT",
									mCurrentColorEffect);
					intent.putExtra("ANTIBANDING_VALUES", mAntibandingValues);
					intent.putExtra("CURRENT_ANTIBANDING", mCurrentAntibanding);
					intent.putExtra("FLASH_MODE_VALUES", mFlashModeValues);
					intent.putExtra("CURRENT_FLASH_MODE", mCurrentFlashMode);
					intent.putExtra("FOCUS_MODE_VALUES", mFocusModeValues);
					intent.putExtra("CURRENT_FOCUS_MODE", mCurrentFocusMode);
					intent.putExtra("SCENE_MODE_VALUES", mSceneModeValues);
					intent.putExtra("CURRENT_SCENE_MODE", mCurrentSceneMode);
					startActivityForResult(intent,
							CAMERA_SETTINGS_ACTIVITY_REQUEST_CODE);
				}
			});
		}

		// Create our Preview view and set it as the content of our activity.
		mStorageMonitor = new MonitorExternalStorageState();
		mStorageMonitor.startWatchingExternalStorage();
		// set error callback
		mCamera.setErrorCallback(errorCallback);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeandroid.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.
	 * SurfaceHolder)
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("surfaceDestroyed");
		}
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
		mPreviewRunning = false;
		if (mStorageMonitor != null) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("surfaceDestroyed:stopWatchingExternalStorage()");
			}
			mStorageMonitor.stopWatchingExternalStorage();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder
	 * , int, int, int)
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (Logger.isLogEnabled())  Logger.log("surfaceChanged");
		if (mPreviewRunning) {
			mCamera.stopPreview();
		}
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {

			Log.e(TAG, (e.getMessage() != null) ? e.getMessage().replace(" ",
					"_") : "", e);
			mTracker.trackEvent("CameraPreview", "SurfaceChangedError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: ""), 0);
			mTracker.dispatch();
		}
		Camera.Parameters p = mCamera.getParameters();

		p.setPreviewSize(w, h);
		p.setPictureFormat(PixelFormat.JPEG);

		Size origSize = p.getPictureSize();
		if (Logger.isLogEnabled())  Logger.log("surfaceChanged:Original Picture width:" + origSize.width
				+ " height:" + origSize.height);

		mMainActivity.setCameraAdvancedParameters(p);

		mCamera.startPreview();
		mPreviewRunning = true;
	}

	/**
	 * Sets all camera parameters
	 * 
	 * @param w
	 * @param h
	 */
	private void setCameraAdvancedParameters(Camera.Parameters p) {

		/******************************************************************/
		try {
			if (mCurrentFlashMode != null) {
				Reflect.setFlashMode(p, mCurrentFlashMode);
				if (Logger.isLogEnabled())  Logger.log("Flash Mode set to " + mCurrentFlashMode);
			} else {
				Reflect.setFlashMode(p, "auto");
				if (Logger.isLogEnabled())  Logger.log("Default Flash Mode set to auto");
			}
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			mTracker.trackEvent("CameraPreview", "SetFlashModeError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: ""), 0);
			mTracker.dispatch();
		}
		/******************************************************************/
		try {
			if (mCurrentSceneMode != null) {
				Reflect.setSceneMode(p, mCurrentSceneMode);
				if (Logger.isLogEnabled())  Logger.log("Scene Mode set to " + mCurrentSceneMode);
			}
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			mTracker.trackEvent("CameraPreview", "SetSceneModeError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: ""), 0);
			mTracker.dispatch();
		}
		/******************************************************************/
		try {
			if (mCurrentAntibanding != null) {
				Reflect.setAntibanding(p, mCurrentAntibanding);
				if (Logger.isLogEnabled())  Logger.log("Antibanding set to " + mCurrentAntibanding);
			}
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			mTracker.trackEvent("CameraPreview", "SetAntibandingError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: ""), 0);
			mTracker.dispatch();
		}
		/******************************************************************/
		try {
			if (mCurrentColorEffect != null) {
				Reflect.setColorEffect(p, mCurrentColorEffect);
				if (Logger.isLogEnabled())  Logger.log("Color Effect set to " + mCurrentColorEffect);
			}
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			mTracker.trackEvent("CameraPreview", "SetColorEffectsError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: ""), 0);
			mTracker.dispatch();
		}
		/******************************************************************/
		try {
			if (mCurrentFocusMode != null) {
				Reflect.setFocusMode(p, mCurrentFocusMode);
				if (Logger.isLogEnabled())  Logger.log("Focus Mode set to " + mCurrentFocusMode);
			}
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			mTracker.trackEvent("CameraPreview", "SetFocusModeError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: ""), 0);
			mTracker.dispatch();
		}
		/******************************************************************/
		try {
			if (mCurrentWhiteBalance != null) {
				Reflect.setWhiteBalance(p, mCurrentWhiteBalance);
				if (Logger.isLogEnabled())  Logger.log("White Balance set to " + mCurrentWhiteBalance);
			}
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			mTracker.trackEvent("CameraPreview", "SetWhiteBalanceError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: ""), 0);
			mTracker.dispatch();
		}
		/******************************************************************/

		try {
			mCamera.setParameters(p);
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			mTracker.trackEvent("CameraPreview", "SetParametersError", ((e
					.getMessage() != null) ? e.getMessage().replace(" ", "_")
					: ""), 0);
			mTracker.dispatch();
		}
	}

	/**
	 * Extracts camera advanced features
	 * 
	 * @param p
	 */
	private void extractCameraAdvancedFeatures(Camera.Parameters p) {
		/******************************************************************/
		try {
			List<String> list = Reflect.getSupportedFlashModes(p);
			if (list != null) {
				mFlashModeValues = (String[]) list.toArray();
			} else {
				if (Logger.isLogEnabled())  Logger.log("Camera does not support FLASH MODES");
			}
			mCurrentFlashMode = Reflect.getFlashMode(p);
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		/******************************************************************/
		try {
			List<String> list = Reflect.getSupportedSceneModes(p);
			if (list != null) {
				mSceneModeValues = (String[]) list.toArray();
			} else {
				if (Logger.isLogEnabled())  Logger.log("Camera does not support SCENE MODES");
			}
			mCurrentSceneMode = Reflect.getSceneMode(p);
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		/******************************************************************/
		try {
			List<String> list = Reflect.getSupportedAntibanding(p);
			if (list != null) {
				mAntibandingValues = (String[]) list.toArray();
			} else {
				if (Logger.isLogEnabled())  Logger.log("Camera does not support ANTIBANDING");
			}
			mCurrentAntibanding = Reflect.getAntibanding(p);
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		/******************************************************************/
		try {
			List<String> list = Reflect.getSupportedColorEffects(p);
			if (list != null) {
				mColorEffectValues = (String[]) list.toArray();
			} else {
				if (Logger.isLogEnabled())  Logger.log("Camera does not support COLOR EFFECTS");
			}
			mCurrentColorEffect = Reflect.getColorEffect(p);
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		/******************************************************************/
		try {
			List<String> list = Reflect.getSupportedFocusModes(p);
			if (list != null) {
				mFocusModeValues = (String[]) list.toArray();
			} else {
				if (Logger.isLogEnabled())  Logger.log("Camera does not support FOCUS MODES");
			}
			mCurrentFocusMode = Reflect.getFocusMode(p);
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		/******************************************************************/
		try {
			List<String> list = Reflect.getSupportedWhiteBalance(p);
			if (list != null) {
				mWhiteBalanceValues = (String[]) list.toArray();
			} else {
				if (Logger.isLogEnabled())  Logger.log("Camera does not support WHITE BALANCE");
			}
			mCurrentWhiteBalance = Reflect.getWhiteBalance(p);
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		/******************************************************************/
	}

	// ----------------------------------------------------------------------
	/**
	 * 
	 * @author gaindans
	 * 
	 */
	private class SavePhotoTask extends AsyncTask<byte[], String, Boolean> {

		@Override
		protected void onPreExecute() {
			System.gc();
			CameraPreview.this.showDialog(AppConfig.SAVING_PICTURE);
		}

		@Override
		protected Boolean doInBackground(byte[]... jpeg) {
			int result = RESULT_OK;

			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("doInBackground");
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
						if (Logger.isLogEnabled())  Logger.log("doInBackground::deleting "
								+ photo.getPath());
					}
					photo.delete();
				}

				pictureFos = new FileOutputStream(photo.getPath());
				BitmapScaler bitmapScaler = new BitmapScaler(jpeg, AppConfig.PICTURE_WIDTH);
				image = bitmapScaler.getScaled();
				// Rotate Picture if needed
				image = this.rotatePhoto(image);

				pictureFos = new FileOutputStream(photo.getPath());
				if (!image.compress(CompressFormat.JPEG, 100, pictureFos)) {
					mTracker.trackEvent("CameraPreview", "SavePhotoError",
							"Unable_To_Save_Photo", 0);
					mTracker.dispatch();
					throw new Exception("Unable_To_Save_Photo");
				}
				pictureFos.flush();

				System.gc();

				if (AppConfig.LOGGING_ENABLED) {
					if (Logger.isLogEnabled())  Logger.log("doInBackground::created file "
							+ photo.getPath());
				}

				// Create Thumbnail
				BitmapScaler thumbnailBitmapScaler = new BitmapScaler(photo,
						AppConfig.THUMBNAIL_WIDTH);
				Bitmap thumbnailBitmap = thumbnailBitmapScaler.getScaled();

				File thumbnail = new File(thumbnailsDir, mRowId
						+ AppConfig.PICTURES_THUMBNAILS_EXTENSION);

				if (thumbnail != null && thumbnail.exists()) {
					if (AppConfig.LOGGING_ENABLED) {
						if (Logger.isLogEnabled())  Logger.log("doInBackground::deleting "
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
					if (Logger.isLogEnabled())  Logger.log("doInBackground::created file "
							+ thumbnail.getPath());
				}
			} catch (Exception e) {
				Log.e(TAG, "Exception in ", e);
				Log.e(TAG, (e.getMessage() != null) ? e.getMessage().replace(
						" ", "_") : "", e);
				mTracker.trackEvent("CameraPreview", "SavePhotoError", (e
						.getMessage() != null) ? e.getMessage().replace(" ",
						"_") : "".replace(" ", "_"), 0);
				mTracker.dispatch();
				result = RESULT_CANCELED;
			} finally {
				if (pictureFos != null) {
					try {
						pictureFos.close();
					} catch (IOException e) {
						Log.e(TAG, (e.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "", e);
						mTracker.trackEvent("CameraPreview", "SavePhotoError",
								((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), 0);
						mTracker.dispatch();
					}
				}
				if (thumbnailFos != null) {
					try {
						thumbnailFos.close();
					} catch (IOException e) {
						Log.e(TAG, (e.getMessage() != null) ? e.getMessage()
								.replace(" ", "_") : "", e);
						mTracker.trackEvent("CameraPreview", "SavePhotoError",
								((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), 0);
						mTracker.dispatch();
					}
				}

				System.gc();
			}

			setResult(result);

			// release the lock on the camera
			isCameraLocked = false;
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("SavePhotoTask::doInBackground::Camera UNLocked!!");
			}
			return (result == RESULT_OK) ? new Boolean(true) : new Boolean(
					false);
		}

		// can use UI thread here
		@Override
		protected void onPostExecute(final Boolean success) {
			if (CameraPreview.this.mDialog.isShowing()) {
				CameraPreview.this.mDialog.dismiss();
			}

			int messageId = (success.booleanValue()) ? R.string.picture_saved
					: R.string.picture_not_saved;
			if (mMainActivity != null) {

				new AlertDialog.Builder(CameraPreview.this).setTitle(
						getString(R.string.app_name)).setIcon(
						android.R.drawable.ic_dialog_info)
						.setMessage(messageId).setPositiveButton(
								R.string.picture_ok_button_label,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										Intent intent = new Intent();
										intent.putExtra("PICTURE_TAKEN", true);
										CameraPreview.this.setResult(RESULT_OK,
												intent);
										CameraPreview.this.finish();
									}
								}).setNegativeButton(
								R.string.picture_redo_button_label,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
									}
								}).show();
			}

		}

		/**
		 * Rotates picture if necessary
		 * 
		 * @param jpeg
		 * @return
		 */
		private Bitmap rotatePhoto(Bitmap image) {

			// The intent is to take a picture in a portrait orientation
			if ((CameraPreview.this.mPictureOrientation <= AppConfig.PORTRAIT_ORIENTATION_INTENT_END)
					|| (CameraPreview.this.mPictureOrientation >= AppConfig.PORTRAIT_ORIENTATION_INTENT_BEGIN)) {

				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG,
							"doInBackground::BEGIN Rotating. Picture Orientation was "
									+ CameraPreview.this.mPictureOrientation);
				}
				int width = image.getWidth();
				int height = image.getHeight();

				// create a matrix for the manipulation
				Matrix matrix = new Matrix();
				// rotate the Bitmap
				matrix.postRotate(90);

				// recreate the new Bitmap
				image = Bitmap.createBitmap(image, 0, 0, width, height, matrix,
						true);

				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG,
							"doInBackground::END Rotating. Picture Orientation was "
									+ CameraPreview.this.mPictureOrientation);
				}
			} else {
				if (AppConfig.LOGGING_ENABLED) {
					Log.i(TAG,
							"doInBackground::Image NOT Rotated. Picture Orientation was "
									+ CameraPreview.this.mPictureOrientation);
				}
			}
			return image;
		}

		// /**
		// *
		// * @param bitmap
		// * @return
		// */
		// private Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		// Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
		// .getHeight(), Config.ARGB_8888);
		// Canvas canvas = new Canvas(output);
		// final int color = 0xff424242;
		// final Paint paint = new Paint();
		// final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap
		// .getHeight());
		// final RectF rectF = new RectF(rect);
		// final float roundPx = 12;
		// paint.setAntiAlias(true);
		// canvas.drawARGB(0, 0, 0, 0);
		// paint.setColor(color);
		// canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		// paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		// canvas.drawBitmap(bitmap, rect, rect, paint);
		// return output;
		// }

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
						if (Logger.isLogEnabled())  Logger.log("doInBackground::Created "
								+ baseDir.getPath());
					}
					// 1.2 Create new pictures dir
					if (picturesDir.mkdir()) {
						if (AppConfig.LOGGING_ENABLED) {
							if (Logger.isLogEnabled())  Logger.log("doInBackground::Created "
									+ picturesDir.getPath());
						}
						// 1.3 Create new thumbnails dir
						if (thumbnailsDir.mkdir()) {
							if (AppConfig.LOGGING_ENABLED) {
								if (Logger.isLogEnabled())  Logger.log("doInBackground::Created "
										+ thumbnailsDir.getPath());
							}

						} else {
							if (AppConfig.LOGGING_ENABLED) {
								Log.e(TAG, "doInBackground::Unable to create "
										+ thumbnailsDir.getPath());
							}
							setResult(RESULT_CANCELED);
							finish();
						}
					} else {
						if (AppConfig.LOGGING_ENABLED) {
							Log.e(TAG, "doInBackground::Unable to create "
									+ picturesDir.getPath());
						}
						setResult(RESULT_CANCELED);
						finish();
					}
				} else {
					if (AppConfig.LOGGING_ENABLED) {
						Log.e(TAG, "doInBackground::Unable to create "
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
							if (Logger.isLogEnabled())  Logger.log("doInBackground::Created "
									+ picturesDir.getPath());
						}
						// 2.1.2 create new thumbnails dir
						if (thumbnailsDir.mkdir()) {
							if (AppConfig.LOGGING_ENABLED) {
								if (Logger.isLogEnabled())  Logger.log("doInBackground::Created "
										+ thumbnailsDir.getPath());
							}

						} else {
							if (AppConfig.LOGGING_ENABLED) {
								Log.e(TAG, "doInBackground::Unable to create "
										+ thumbnailsDir.getPath());
							}
							setResult(RESULT_CANCELED);
							finish();
						}
					} else {
						if (AppConfig.LOGGING_ENABLED) {
							Log.e(TAG, "doInBackground::Unable to create "
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
								if (Logger.isLogEnabled())  Logger.log("doInBackground::Created "
										+ thumbnailsDir.getPath());
							}

						} else {
							if (AppConfig.LOGGING_ENABLED) {
								Log.e(TAG, "doInBackground::Unable to create "
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

	// ----------------------------------------------------------------------
	/**
	 * 
	 */
	private class MonitorExternalStorageState {
		BroadcastReceiver mExternalStorageReceiver;
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;

		void updateExternalStorageState() {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("updateExternalStorageState");
			}
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				mExternalStorageAvailable = mExternalStorageWriteable = true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				mExternalStorageAvailable = true;
				mExternalStorageWriteable = false;
			} else {
				mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
			handleExternalStorageState(mExternalStorageAvailable,
					mExternalStorageWriteable);
		}

		void handleExternalStorageState(boolean mExternalStorageAvailable,
				boolean mExternalStorageWriteable) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("handleExternalStorageState");
			}
			if (!mExternalStorageAvailable) {
				if (AppConfig.LOGGING_ENABLED) {
					Log
							.i(TAG,
									"handleExternalStorageState::EXTERNAL_STORAGE_NOT_AVAILABLE");
				}
				CameraPreview.this
						.showDialog(AppConfig.EXTERNAL_STORAGE_NOT_AVAILABLE);
				return;
			} else {
				if (CameraPreview.this.mDialog != null) {
					CameraPreview.this.mDialog.cancel();
				}
			}
			if (!mExternalStorageWriteable) {
				if (AppConfig.LOGGING_ENABLED) {
					Log
							.i(TAG,
									"handleExternalStorageState::EXTERNAL_STORAGE_NOT_WRITABLE");
				}
				CameraPreview.this
						.showDialog(AppConfig.EXTERNAL_STORAGE_NOT_WRITABLE);
			} else {
				if (CameraPreview.this.mDialog != null) {
					CameraPreview.this.mDialog.cancel();
				}
			}
		}

		void startWatchingExternalStorage() {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("startWatchingExternalStorage");
			}
			mExternalStorageReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (Logger.isLogEnabled())  Logger.log("Storage: " + intent.getData());
					updateExternalStorageState();
				}
			};
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
			filter.addAction(Intent.ACTION_MEDIA_REMOVED);
			registerReceiver(mExternalStorageReceiver, filter);
			updateExternalStorageState();
		}

		void stopWatchingExternalStorage() {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("stopWatchingExternalStorage");
			}
			unregisterReceiver(mExternalStorageReceiver);
		}

	}
}
