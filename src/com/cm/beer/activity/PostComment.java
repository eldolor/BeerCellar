package com.cm.beer.activity;

import java.net.URLEncoder;
import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.User;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class PostComment extends Activity {
	String TAG;
	ProgressDialog mDialog;
	int ACTIVE_DIALOG;
	static final int POST_COMMENT_REQUEST = 1;
	static final int DIALOG_POST_COMMENT_ID = 1;

	String mBeerId;
	
	TextView mMessage;
	TextView mComment;
	Button mPost;
	Button mCancel;

	User mUser;

	GoogleAnalyticsTracker mTracker;
	Activity mMainActivity;

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
		mMainActivity = this;
		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the mTracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID,
				this);
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}
		Bundle extras = getIntent().getExtras();
		mBeerId = extras != null ? extras.getString("BEER_ID") : null;
		
		mUser = new User(this);
		setContentView(R.layout.post_comment);

		display();
	}

	private void display() {
		mMessage = (TextView) findViewById(R.id.message);
		mComment = (TextView) findViewById(R.id.comment);

		mPost = (Button) findViewById(R.id.post_comment);
		mPost.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mPost.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//
				String _comment = mComment.getText().toString();

				if ((_comment == null) || (_comment.equals(""))) {
					mMessage.setText(mMainActivity
							.getString(R.string.comment_missing_message));
					return;
				} else {
					
					new AsyncPostCommentTask().execute(mUser.getUserId(), mUser.getUserName(),
							mUser.getUserLink(), mBeerId, _comment);
				}
			}
		});
		mCancel = (Button) findViewById(R.id.cancel);
		mCancel.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		mCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				mMainActivity.setResult(RESULT_OK, intent);
				mMainActivity.finish();
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
		String dialogMessage = null;
		if (id == DIALOG_POST_COMMENT_ID) {
			dialogMessage = this.getString(R.string.progress_saving_message);
			ACTIVE_DIALOG = DIALOG_POST_COMMENT_ID;
		}

		mDialog = ProgressDialog.show(PostComment.this, null, dialogMessage,
				true, true);
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

	/************************************************************************************/
	private class AsyncPostCommentTask extends AsyncTask<Object, Void, Object> {

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			Log.i(TAG, "doInBackground starting");

			String _userId = (String) args[0];
			String _userName = (String) args[1];
			String _userLink = (String) args[2];
			String _beerId = (String) args[3];
			String _comment = (String) args[4];
			String _response = null;

			try {

				JSONObject comment = new JSONObject();
				comment.put("userId", _userId);
				comment.put("userName", _userName);
				comment.put("userLink",_userLink);
				comment.put("beerId", _beerId);
				comment.put("comment", _comment);

				String _commentStr = comment.toString();
				_commentStr = URLEncoder.encode(_commentStr, "UTF-8");
				Log.i(TAG, _commentStr);

				HashMap<String, String> parameters = new HashMap<String, String>();
				parameters.put("q", AppConfig.COMMUNITY_ADD_COMMENT_Q_VALUE);
				parameters.put("comment", _commentStr);

				// Prepare a request object
				String _url = AppConfig.COMMUNITY_COMMENTS_URL;
				Log.i(TAG, _url);
				{
					boolean retry = true;
					int retryCount = 0;
					while ((retry)
							&& (retryCount < AppConfig.SHARE_WITH_COMMUNITY_BEER_UPLOAD_RETRY_COUNT)) {
						try {

							_response = com.cm.beer.util.Util.openUrl(_url,
									"POST", parameters);
							// Upload successful
							retry = false;
						} catch (Throwable e) {
							Log.e(TAG, "error: "
									+ ((e.getMessage() != null) ? e
											.getMessage().replace(" ", "_")
											: ""), e);
							// increment retry count
							retryCount++;
							Log.e(TAG, "Retrying... Retry Count = "
									+ retryCount);
						}
					}
					Log.d(TAG, "Final Retry Count = " + retryCount);
					if (retryCount > 0) {
						mTracker.trackEvent("PostComment", "Post",
								"RetryCount", retryCount);
						mTracker.dispatch();
					}
				}

				// Examine the response status
				Log.i(TAG, "Response = " + _response);

			} catch (Throwable e) {
				Log.e(TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
				mTracker.trackEvent(
						"PostComment",
						"AsyncPostCommentTaskError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			Log.i(TAG, "doInBackground finished");
			return null;
		}

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute starting");
			if (mMainActivity != null) {
				mMainActivity.showDialog(DIALOG_POST_COMMENT_ID);
			}
			Log.i(TAG, "onPreExecute finished");
		}

		@Override
		protected void onPostExecute(Object result) {
			Log.i(TAG, "onPostExecute starting");
			mDialog.cancel();
			Toast.makeText(mMainActivity,
					R.string.on_sharing_with_community, Toast.LENGTH_LONG)
					.show();
			Intent intent = new Intent();
			mMainActivity.setResult(RESULT_OK, intent);
			mMainActivity.finish();
			
			Log.i(TAG, "onPostExecute finished");
		}

	}

}
