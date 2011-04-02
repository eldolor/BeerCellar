package com.cm.beer.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Interrupt extends Activity {
	String TAG;
	Intent mOriginalIntent;
	Activity mMainActivity;

	int mDialogsToBeProcessed;
	int mDialogsProcessed;

	ArrayList<String> mInterruptKeys;
	ArrayList<String> mTitles;
	ArrayList<String> mMessages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup TAG
		TAG = this.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		mMainActivity = this;
		mOriginalIntent = getIntent();
		Bundle extras = (mOriginalIntent != null) ? mOriginalIntent.getExtras()
				: null;
		if (extras != null) {
			mInterruptKeys = extras.getStringArrayList("INTERRUPT_KEYS");
			mTitles = extras.getStringArrayList("TITLES");
			mMessages = extras.getStringArrayList("MESSAGES");
		}
		// set
		mDialogsToBeProcessed = mInterruptKeys.size();
		Log.i(TAG, "mDialogsToBeProcessed: " + mDialogsToBeProcessed);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mInterruptKeys != null) {
			displayInterrupt(mInterruptKeys.get(mDialogsProcessed), mTitles
					.get(mDialogsProcessed), mMessages.get(mDialogsProcessed));
		}
	}

	private void displayInterrupt(final String interruptKey, String title,
			String message) {
		Log.i(TAG, "Display Dialog : " + mDialogsProcessed);

		AlertDialog.Builder dialog = new AlertDialog.Builder(mMainActivity);
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setPositiveButton(R.string.yes_label,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// End the activity; pass back the original extras
						mOriginalIntent.putExtra(interruptKey, true);
						mMainActivity.setResult(RESULT_OK, mOriginalIntent);
						mMainActivity.finish();
					}
				});
		dialog.setNegativeButton(R.string.no_label,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// End the activity; pass back the original extras
						mOriginalIntent.putExtra(interruptKey, false);
						if (mDialogsProcessed < mDialogsToBeProcessed) {
							dialog.dismiss();
							displayInterrupt(mInterruptKeys
									.get(mDialogsProcessed), mTitles
									.get(mDialogsProcessed), mMessages
									.get(mDialogsProcessed));
						} else {
							mMainActivity.setResult(RESULT_OK, mOriginalIntent);
							mMainActivity.finish();
						}
					}
				});

		dialog.create();
		dialog.show();
		// increment dialogs processed
		mDialogsProcessed++;

	}

}
