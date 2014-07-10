package com.cm.beer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.Logger;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class CameraSettings extends Activity {
	String TAG;

	ProgressDialog mDialog;
	int ACTIVE_DIALOG;
	Activity mMainActivity;
	GoogleAnalyticsTracker mTracker;

	Button mDone;
	TextView mSelectWhiteBalance;
	TextView mCurrentWhiteBalanceTV;
	TextView mSelectColorEffect;
	TextView mCurrentColorEffectTV;
	TextView mSelectAntibanding;
	TextView mCurrentAntibandingTV;
	TextView mSelectFlashMode;
	TextView mCurrentFlashModeTV;
	TextView mSelectFocusMode;
	TextView mCurrentFocusModeTV;
	TextView mSelectSceneMode;
	TextView mCurrentSceneModeTV;

	String mCurrentWhiteBalance;
	String mCurrentColorEffect;
	String mCurrentAntibanding;
	String mCurrentFlashMode;
	String mCurrentFocusMode;
	String mCurrentSceneMode;
	String[] mWhiteBalanceValues;
	String[] mColorEffectValues;
	String[] mAntibandingValues;
	String[] mFlashModeValues;
	String[] mFocusModeValues;
	String[] mSceneModeValues;

	/** Called when the activity is first created. */
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
		if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Instantiated");

		Bundle extras = getIntent().getExtras();
		mFlashModeValues = extras != null ? extras
				.getStringArray("FOCUS_MODE_VALUES") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: mFlashModeValues:"
				+ ((mFlashModeValues != null) ? mFlashModeValues.toString()
						: null));
		mCurrentFlashMode = extras != null ? extras
				.getString("CURRENT_FLASH_MODE") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: mCurrentFlashMode:" + mCurrentFlashMode);
		mSceneModeValues = extras != null ? extras
				.getStringArray("SCENE_MODE_VALUES") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: mSceneModeValues:"
				+ ((mSceneModeValues != null) ? mSceneModeValues.toString()
						: null));
		mCurrentSceneMode = extras != null ? extras
				.getString("CURRENT_SCENE_MODE") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: mCurrentSceneMode:" + mCurrentSceneMode);
		mWhiteBalanceValues = extras != null ? extras
				.getStringArray("WHITE_BALANCE_VALUES") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: mWhiteBalanceValues:"
				+ ((mWhiteBalanceValues != null) ? mWhiteBalanceValues
						.toString() : null));
		mCurrentWhiteBalance = extras != null ? extras
				.getString("CURRENT_WHITE_BALANCE") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: mCurrentWhiteBalance:" + mCurrentWhiteBalance);
		mColorEffectValues = extras != null ? extras
				.getStringArray("COLOR_EFFECT_VALUES") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: mColorEffectValues:"
				+ ((mColorEffectValues != null) ? mColorEffectValues.toString()
						: null));
		mCurrentColorEffect = extras != null ? extras
				.getString("CURRENT_COLOR_EFFECT") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: mCurrentColorEffect:" + mCurrentColorEffect);
		mAntibandingValues = extras != null ? extras
				.getStringArray("ANTIBANDING_VALUES") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: mAntibandingValues:"
				+ ((mAntibandingValues != null) ? mAntibandingValues.toString()
						: null));
		mCurrentAntibanding = extras != null ? extras
				.getString("CURRENT_ANTIBANDING") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: mCurrentAntibanding:" + mCurrentAntibanding);
		mFocusModeValues = extras != null ? extras
				.getStringArray("FOCUS_MODE_VALUES") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: mFocusModeValues:"
				+ ((mFocusModeValues != null) ? mFocusModeValues.toString()
						: null));
		mCurrentFocusMode = extras != null ? extras
				.getString("CURRENT_FOCUS_MODE") : null;
		if (Logger.isLogEnabled())  Logger.log("onCreate: mCurrentFocusMode:" + mCurrentFocusMode);

		setContentView(R.layout.camera_settings);
		display();
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

	private void display() {
		if (Logger.isLogEnabled())  Logger.log("display");

		mDone = (Button) findViewById(R.id.done);
		mDone.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mDone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("CURRENT_WHITE_BALANCE", mCurrentWhiteBalance);
				intent.putExtra("CURRENT_COLOR_EFFECT", mCurrentColorEffect);
				intent.putExtra("CURRENT_ANTIBANDING", mCurrentAntibanding);
				intent.putExtra("CURRENT_FLASH_MODE", mCurrentFlashMode);
				intent.putExtra("CURRENT_FOCUS_MODE", mCurrentFocusMode);
				intent.putExtra("CURRENT_SCENE_MODE", mCurrentSceneMode);
				mMainActivity.setResult(RESULT_OK, intent);
				mMainActivity.finish();
			}
		});
		/****************************************/
		mSelectWhiteBalance = (TextView) findViewById(R.id.select_white_balance);
		mSelectWhiteBalance.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (Logger.isLogEnabled())  Logger.log("Select White Balance");
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						mMainActivity);
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.setTitle(R.string.select_white_balance_label);
				dialog.setSingleChoiceItems(
						((CharSequence[]) mWhiteBalanceValues), -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mCurrentWhiteBalance = mWhiteBalanceValues[which];
								if (Logger.isLogEnabled())  Logger.log("Selected: "
										+ mWhiteBalanceValues[which]);

								mCurrentWhiteBalanceTV
										.setVisibility(View.VISIBLE);
								mCurrentWhiteBalanceTV
										.setText(mWhiteBalanceValues[which]);
							}
						});
				dialog.setNegativeButton(R.string.cancel_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				dialog.create();
				dialog.show();
			}
		});
		mCurrentWhiteBalanceTV = (TextView) findViewById(R.id.current_white_balance);
		try {
			String value = mCurrentWhiteBalance;// Reflect.getWhiteBalance(mCamera.getParameters());
			if (value != null) {
				mCurrentWhiteBalanceTV.setText(value);
			} else {
				mCurrentWhiteBalanceTV.setVisibility(View.INVISIBLE);
			}

		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}

		/****************************************/
		mSelectAntibanding = (TextView) findViewById(R.id.select_antibanding);
		mSelectAntibanding.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (Logger.isLogEnabled())  Logger.log("Select Antibanding");
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						mMainActivity);
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.setTitle(R.string.select_antibanding_label);
				dialog.setSingleChoiceItems(
						((CharSequence[]) mAntibandingValues), -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mCurrentAntibanding = mAntibandingValues[which];
								if (Logger.isLogEnabled())  Logger.log("Selected: "
										+ mAntibandingValues[which]);

								mCurrentAntibandingTV
										.setVisibility(View.VISIBLE);
								mCurrentAntibandingTV
										.setText(mAntibandingValues[which]);
							}
						});
				dialog.setNegativeButton(R.string.cancel_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				dialog.create();
				dialog.show();
			}
		});
		mCurrentAntibandingTV = (TextView) findViewById(R.id.current_antibanding);
		try {
			String value = mCurrentAntibanding;// Reflect.getAntibanding(mCamera.getParameters());
			if (value != null) {
				mCurrentAntibandingTV.setText(value);
			} else {
				mCurrentAntibandingTV.setVisibility(View.INVISIBLE);
			}

		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		/****************************************/
		mSelectColorEffect = (TextView) findViewById(R.id.select_color_effect);
		mSelectColorEffect.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (Logger.isLogEnabled())  Logger.log("Select Color Effect");
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						mMainActivity);
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.setTitle(R.string.select_color_effect_label);
				dialog.setSingleChoiceItems(
						((CharSequence[]) mColorEffectValues), -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mCurrentColorEffect = mColorEffectValues[which];
								if (Logger.isLogEnabled())  Logger.log("Selected: "
										+ mColorEffectValues[which]);

								mCurrentColorEffectTV
										.setVisibility(View.VISIBLE);
								mCurrentColorEffectTV
										.setText(mColorEffectValues[which]);
							}
						});
				dialog.setNegativeButton(R.string.cancel_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				dialog.create();
				dialog.show();
			}
		});
		mCurrentColorEffectTV = (TextView) findViewById(R.id.current_color_effect);
		try {
			String value = mCurrentColorEffect;// Reflect.getColorEffect(mCamera.getParameters());
			if (value != null) {
				mCurrentColorEffectTV.setText(value);
			} else {
				mCurrentColorEffectTV.setVisibility(View.INVISIBLE);
			}

		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		/****************************************/
		mSelectFlashMode = (TextView) findViewById(R.id.select_flash_mode);
		mSelectFlashMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (Logger.isLogEnabled())  Logger.log("Select Flash Mode");
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						mMainActivity);
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.setTitle(R.string.select_flash_mode_label);
				dialog.setSingleChoiceItems(
						((CharSequence[]) mFlashModeValues), -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mCurrentFlashMode = mFlashModeValues[which];
								if (Logger.isLogEnabled())  Logger.log("Selected: "
										+ mFlashModeValues[which]);

								mCurrentFlashModeTV.setVisibility(View.VISIBLE);
								mCurrentFlashModeTV
										.setText(mFlashModeValues[which]);
							}
						});
				dialog.setNegativeButton(R.string.cancel_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				dialog.create();
				dialog.show();
			}
		});
		mCurrentFlashModeTV = (TextView) findViewById(R.id.current_flash_mode);
		try {
			String value = mCurrentFlashMode;// Reflect.getFlashMode(mCamera.getParameters());
			if (value != null) {
				mCurrentFlashModeTV.setText(value);
			} else {
				mCurrentFlashModeTV.setVisibility(View.INVISIBLE);
			}

		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		/****************************************/
		mSelectFocusMode = (TextView) findViewById(R.id.select_focus_mode);
		mSelectFocusMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (Logger.isLogEnabled())  Logger.log("Select Focus Mode");
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						mMainActivity);
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.setTitle(R.string.select_focus_mode_label);
				dialog.setSingleChoiceItems(
						((CharSequence[]) mFocusModeValues), -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mCurrentFocusMode = mFocusModeValues[which];
								if (Logger.isLogEnabled())  Logger.log("Selected: "
										+ mFocusModeValues[which]);

								mCurrentFocusModeTV.setVisibility(View.VISIBLE);
								mCurrentFocusModeTV
										.setText(mFocusModeValues[which]);
							}
						});
				dialog.setNegativeButton(R.string.cancel_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				dialog.create();
				dialog.show();
			}
		});
		mCurrentFocusModeTV = (TextView) findViewById(R.id.current_focus_mode);
		try {
			String value = mCurrentFocusMode;// Reflect.getFocusMode(mCamera.getParameters());
			if (value != null) {
				mCurrentFocusModeTV.setText(value);
			} else {
				mCurrentFocusModeTV.setVisibility(View.INVISIBLE);
			}

		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		/****************************************/
		mSelectSceneMode = (TextView) findViewById(R.id.select_scene_mode);
		mSelectSceneMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (Logger.isLogEnabled())  Logger.log("Select Scene Mode");
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						mMainActivity);
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.setTitle(R.string.select_scene_mode_label);
				dialog.setSingleChoiceItems(
						((CharSequence[]) mSceneModeValues), -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mCurrentSceneMode = mSceneModeValues[which];
								if (Logger.isLogEnabled())  Logger.log("Selected: "
										+ mSceneModeValues[which]);

								mCurrentSceneModeTV.setVisibility(View.VISIBLE);
								mCurrentSceneModeTV
										.setText(mSceneModeValues[which]);
							}
						});
				dialog.setNegativeButton(R.string.cancel_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				dialog.create();
				dialog.show();
			}
		});
		mCurrentSceneModeTV = (TextView) findViewById(R.id.current_scene_mode);
		try {
			String value = mCurrentSceneMode;// Reflect.getSceneMode(mCamera.getParameters());
			if (value != null) {
				mCurrentSceneModeTV.setText(value);
			} else {
				mCurrentSceneModeTV.setVisibility(View.INVISIBLE);
			}

		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		/****************************************/

	}

}
