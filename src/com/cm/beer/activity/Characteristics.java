package com.cm.beer.activity;

import java.util.Arrays;

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
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.cm.beer.config.AppConfig;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Characteristics extends Activity {
	String TAG;
	ProgressDialog mDialog;

	GoogleAnalyticsTracker mTracker;
	Button mDone;
	Button mCancel;

	Spinner mColor;
	Spinner mClarity;
	Spinner mFoam;
	Button mAromaButton;
	Spinner mMouthfeel;
	Spinner mBody;
	Spinner mAftertaste;

	Characteristics mMainActivity;
	Intent mOriginalIntent;
	JSONObject mOriginalCharacteristicsJson;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		Log.i(TAG, "onCreate");
		mMainActivity = this;

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the mTracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		Log.i(TAG, "onCreate:Google Tracker Instantiated");
		mOriginalIntent = getIntent();
		Bundle extras = mOriginalIntent.getExtras();
		String jsonStr = extras != null ? extras.getString("CHARACTERISTICS")
				: null;
		if (jsonStr != null) {
			try {
				mOriginalCharacteristicsJson = new JSONObject(jsonStr);
				Log.i(TAG,
						"onCreate:mOriginalCharacteristicsJson Instantiated: "
								+ mOriginalCharacteristicsJson.toString());
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		} else {
			mOriginalCharacteristicsJson = new JSONObject();
		}
		setContentView(R.layout.characteristics);
		display();
	}

	/*
	 * 
	 */
	protected void display() {

		Log.i(TAG, "display");
		/****************************************/
		mColor = (Spinner) findViewById(R.id.color);
		ArrayAdapter<CharSequence> adapterColor = ArrayAdapter
				.createFromResource(this, R.array.characteristics_color,
						android.R.layout.simple_spinner_item);
		adapterColor
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mColor.setAdapter(adapterColor);
		{
			final CharSequence[] options = getResources().getStringArray(
					R.array.characteristics_color);
			int position = 0;
			try {
				if (mOriginalCharacteristicsJson.has("color")) {
					String _selected = mOriginalCharacteristicsJson
							.getString("color");
					if (_selected != null) {
						// traverse for a match
						for (int i = 0; i < options.length; i++) {
							if (options[i].equals(_selected)) {
								position = i;
								break;
							}
						}
					}
				}
			} catch (JSONException e1) {
				Log.e(TAG, e1.getMessage(), e1);
			}
			mColor.setSelection(position, true);
		}

		/****************************************/
		mClarity = (Spinner) findViewById(R.id.clarity);
		ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
				this, R.array.characteristics_clarity,
				android.R.layout.simple_spinner_item);
		adapter1
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mClarity.setAdapter(adapter1);
		{
			final CharSequence[] options = getResources().getStringArray(
					R.array.characteristics_clarity);
			int position = 0;
			try {
				if (mOriginalCharacteristicsJson.has("clarity")) {
					String _selected = mOriginalCharacteristicsJson
							.getString("clarity");
					if (_selected != null) {
						// traverse for a match
						for (int i = 0; i < options.length; i++) {
							if (options[i].equals(_selected)) {
								position = i;
								break;
							}
						}
					}
				}
			} catch (JSONException e1) {
				Log.e(TAG, e1.getMessage(), e1);
			}
			mClarity.setSelection(position, true);
		}
		/****************************************/
		mFoam = (Spinner) findViewById(R.id.foam);
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
				this, R.array.characteristics_foam,
				android.R.layout.simple_spinner_item);
		adapter2
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mFoam.setAdapter(adapter2);
		{
			final CharSequence[] options = getResources().getStringArray(
					R.array.characteristics_foam);
			int position = 0;
			try {
				if (mOriginalCharacteristicsJson.has("foam")) {
					String _selected = mOriginalCharacteristicsJson
							.getString("foam");
					if (_selected != null) {
						// traverse for a match
						for (int i = 0; i < options.length; i++) {
							if (options[i].equals(_selected)) {
								position = i;
								break;
							}
						}
					}
				}
			} catch (JSONException e1) {
				Log.e(TAG, e1.getMessage(), e1);
			}
			mFoam.setSelection(position, true);
		}
		/****************************************/
		mBody = (Spinner) findViewById(R.id.body);
		ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
				this, R.array.characteristics_body,
				android.R.layout.simple_spinner_item);
		adapter3
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mBody.setAdapter(adapter3);
		{
			final CharSequence[] options = getResources().getStringArray(
					R.array.characteristics_body);
			int position = 0;
			try {
				if (mOriginalCharacteristicsJson.has("body")) {
					String _selected = mOriginalCharacteristicsJson
							.getString("body");
					if (_selected != null) {
						// traverse for a match
						for (int i = 0; i < options.length; i++) {
							if (options[i].equals(_selected)) {
								position = i;
								break;
							}
						}
					}
				}
			} catch (JSONException e1) {
				Log.e(TAG, e1.getMessage(), e1);
			}
			mBody.setSelection(position, true);
		}
		/****************************************/
		mAromaButton = (Button) findViewById(R.id.aroma_button);
		mAromaButton.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mAromaButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				Log.i(TAG, "aroma button");
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						new ContextThemeWrapper(Characteristics.this,
								android.R.style.Theme_Dialog));
				dialog.setIcon(android.R.drawable.ic_dialog_info);
				dialog.setTitle(R.string.aroma_label);
				JSONArray array = null;
				try {
					if (mOriginalCharacteristicsJson.has("aroma")) {
						array = mOriginalCharacteristicsJson
								.getJSONArray("aroma");
					}
				} catch (JSONException e1) {
					Log.e(TAG, e1.getMessage(), e1);
				}
				final CharSequence[] items = getResources().getStringArray(
						R.array.characteristics_aroma);
				Arrays.sort(items);
				final boolean[] checkedItems = new boolean[items.length];
				for (int i = 0; i < items.length; i++) {
					// default to false
					checkedItems[i] = false;
					if (array != null) {
						// traverse previously selected items for a match
						for (int j = 0; j < array.length(); j++) {
							try {
								if (array.getString(j).equals(items[i])) {
									checkedItems[i] = true;
									break;
								}
							} catch (JSONException e) {
								Log.e(TAG, e.getMessage(), e);
							}
						}
					}
				}

				dialog.setMultiChoiceItems(items, checkedItems,
						new OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								Log.i(TAG, "which=" + which + " isChecked="
										+ ((isChecked) ? "Y" : "N"));
								checkedItems[which] = isChecked;
							}
						});
				dialog.setPositiveButton(R.string.done_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								try {
									JSONArray jsonArray = new JSONArray();
									for (int i = 0; i < checkedItems.length; i++) {
										if (checkedItems[i]) {
											jsonArray.put(items[i]);
										}
									}
									Log.i(TAG, "aroma selected: "
											+ jsonArray.toString());
									mMainActivity.mOriginalCharacteristicsJson
											.put("aroma", jsonArray);
									setAromaText();
								} catch (JSONException e) {
									Log.e(TAG, e.getMessage(), e);
								}
							}
						});
				dialog.create();
				dialog.show();
			}
		});
		setAromaText();
		/****************************************/
		mAftertaste = (Spinner) findViewById(R.id.aftertaste);
		ArrayAdapter<CharSequence> adapter5 = ArrayAdapter.createFromResource(
				this, R.array.characteristics_aftertaste,
				android.R.layout.simple_spinner_item);
		adapter5
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAftertaste.setAdapter(adapter5);
		{
			final CharSequence[] options = getResources().getStringArray(
					R.array.characteristics_aftertaste);
			int position = 0;
			try {
				if (mOriginalCharacteristicsJson.has("aftertaste")) {
					String _selected = mOriginalCharacteristicsJson
							.getString("aftertaste");
					if (_selected != null) {
						// traverse for a match
						for (int i = 0; i < options.length; i++) {
							if (options[i].equals(_selected)) {
								position = i;
								break;
							}
						}
					}
				}
			} catch (JSONException e1) {
				Log.e(TAG, e1.getMessage(), e1);
			}
			mAftertaste.setSelection(position, true);
		}
		/****************************************/
		mMouthfeel = (Spinner) findViewById(R.id.mouthfeel);
		ArrayAdapter<CharSequence> adapter6 = ArrayAdapter.createFromResource(
				this, R.array.characteristics_mouthfeel,
				android.R.layout.simple_spinner_item);
		adapter6
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mMouthfeel.setAdapter(adapter6);
		{
			final CharSequence[] options = getResources().getStringArray(
					R.array.characteristics_mouthfeel);
			int position = 0;
			try {
				if (mOriginalCharacteristicsJson.has("mouthfeel")) {
					String _selected = mOriginalCharacteristicsJson
							.getString("mouthfeel");
					if (_selected != null) {
						// traverse for a match
						for (int i = 0; i < options.length; i++) {
							if (options[i].equals(_selected)) {
								position = i;
								break;
							}
						}
					}
				}
			} catch (JSONException e1) {
				Log.e(TAG, e1.getMessage(), e1);
			}
			mMouthfeel.setSelection(position, true);
		}
		/****************************************/
		mDone = (Button) findViewById(R.id.done);
		mDone.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mDone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// End the activity; pass back the original extras

				try {
					if (!mColor.getSelectedItem().toString().equals("")) {
						mOriginalCharacteristicsJson.put("color", mColor
								.getSelectedItem().toString());
					} else {
						if (mOriginalCharacteristicsJson.has("color")) {
							mOriginalCharacteristicsJson.remove("color");
						}
					}
					if (!mClarity.getSelectedItem().toString().equals("")) {
						mOriginalCharacteristicsJson.put("clarity", mClarity
								.getSelectedItem().toString());
					} else {
						if (mOriginalCharacteristicsJson.has("clarity")) {
							mOriginalCharacteristicsJson.remove("clarity");
						}
					}
					if (!mFoam.getSelectedItem().toString().equals("")) {
						mOriginalCharacteristicsJson.put("foam", mFoam
								.getSelectedItem().toString());
					} else {
						if (mOriginalCharacteristicsJson.has("foam")) {
							mOriginalCharacteristicsJson.remove("foam");
						}
					}
					if (!mBody.getSelectedItem().toString().equals("")) {
						mOriginalCharacteristicsJson.put("body", mBody
								.getSelectedItem().toString());
					} else {
						if (mOriginalCharacteristicsJson.has("body")) {
							mOriginalCharacteristicsJson.remove("body");
						}
					}
					if (!mMouthfeel.getSelectedItem().toString().equals("")) {
						mOriginalCharacteristicsJson.put("mouthfeel",
								mMouthfeel.getSelectedItem().toString());
					} else {
						if (mOriginalCharacteristicsJson.has("mouthfeel")) {
							mOriginalCharacteristicsJson.remove("mouthfeel");
						}
					}
					if (!mAftertaste.getSelectedItem().toString().equals("")) {
						mOriginalCharacteristicsJson.put("aftertaste",
								mAftertaste.getSelectedItem().toString());
					} else {
						if (mOriginalCharacteristicsJson.has("aftertaste")) {
							mOriginalCharacteristicsJson.remove("aftertaste");
						}
					}
					// aroma setup in the dialog
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage(), e);
				}

				Log.i(TAG, mOriginalCharacteristicsJson.toString());
				mOriginalIntent.putExtra("CHARACTERISTICS",
						mOriginalCharacteristicsJson.toString());
				mMainActivity.setResult(RESULT_OK, mOriginalIntent);
				mMainActivity.finish();

				setResult(RESULT_OK);
				finish();
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
	}

	private void setAromaText() {
		try {
			if (mOriginalCharacteristicsJson.has("aroma")) {
				if (mOriginalCharacteristicsJson.getJSONArray("aroma").length() > 0) {
					JSONArray _aroma = mOriginalCharacteristicsJson
							.getJSONArray("aroma");
					StringBuilder _aromaText = new StringBuilder();
					for (int i = 0; i < _aroma.length(); i++) {
						_aromaText.append(", ");
						_aromaText.append(_aroma.getString(i));
					}
					String _aromaStr = _aromaText.toString();
					_aromaStr = _aromaStr.replaceFirst(", ", "");
					((TextView) findViewById(R.id.aroma))
							.setVisibility(View.VISIBLE);
					((TextView) findViewById(R.id.aroma)).setText(_aromaStr);
				} else {
					((TextView) findViewById(R.id.aroma))
							.setVisibility(View.GONE);
				}
			} else {
				((TextView) findViewById(R.id.aroma)).setVisibility(View.GONE);
			}
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
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
		mDialog = ProgressDialog.show(Characteristics.this, null, this
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
