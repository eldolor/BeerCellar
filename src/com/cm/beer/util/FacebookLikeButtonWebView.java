package com.cm.beer.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.Toast;

import com.cm.beer.activity.R;
import com.cm.beer.facebook.SessionEvents;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

// ----------------------------------------------------------------------//
public class FacebookLikeButtonWebView extends WebView {
	String TAG;
	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_FACEBOOK_LIKE_BUTTON = 4;
	private Facebook mFb;
	private Handler mHandler;
	private String[] mPermissions;
	private Activity mActivity;
	private int mActivityCode;

	public FacebookLikeButtonWebView(Context context) {
		super(context);
	}

	public FacebookLikeButtonWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FacebookLikeButtonWebView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public void init(final Activity activity, final int activityCode,
			final Facebook fb) {
		init(activity, activityCode, fb, new String[] {});
	}

	public void init(final Activity activity, final int activityCode,
			final Facebook fb, final String[] permissions) {
		mActivity = activity;
		mActivityCode = activityCode;
		mFb = fb;
		mPermissions = permissions;
		mHandler = new Handler();
		TAG = mActivity.getString(R.string.app_name) + "::"
				+ this.getClass().getName();

		setBackgroundColor(Color.TRANSPARENT);
		drawableStateChanged();

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if ((ev.getAction() != MotionEvent.ACTION_OUTSIDE)
				&& (ev.getAction() == MotionEvent.ACTION_UP)) {
			// invalid facebook session
			if (!mFb.isSessionValid()) {
				if (Logger.isLogEnabled())  Logger.log("onTouchEvent: Invalid Facebook Session!");
				/** Handle User Not Logged In **/
				// execute SSO on touch event
				mFb.authorize(mActivity, mPermissions, mActivityCode,
						new LoginDialogListener());
			} else {
				if (Logger.isLogEnabled())  Logger.log("onTouchEvent: Valid Facebook Session");
			}

		} else {
			if (Logger.isLogEnabled())  Logger.log("onTouchEvent: event not considered");
		}
		return super.onTouchEvent(ev);
	}

	private final class LoginDialogListener implements DialogListener {
		private final String TAG = LoginDialogListener.class.getName();

		public void onComplete(Bundle values) {
			if (Logger.isLogEnabled())  Logger.log("onComplete::");
			SessionEvents.onLoginSuccess();
		}

		public void onFacebookError(FacebookError error) {
			Log.e(TAG, "onFacebookError::" + error.getMessage());
			Toast.makeText(getContext(),
					"Facebook Error: " + error.getMessage(), Toast.LENGTH_SHORT)
					.show();
			SessionEvents.onLoginError(error.getMessage());
		}

		public void onError(DialogError error) {
			Log.e(TAG, "onError::" + error.getMessage());
			SessionEvents.onLoginError(error.getMessage());
		}

		public void onCancel() {
			if (Logger.isLogEnabled())  Logger.log("onCancel::");
			SessionEvents.onLoginError("Action Canceled");
		}
	}

}
