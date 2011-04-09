package com.cm.beer.util;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.cm.beer.activity.CommunityBeerView;
import com.cm.beer.activity.LoginIntercept;
import com.cm.beer.activity.R;
import com.cm.beer.config.AppConfig;
import com.facebook.android.Facebook;

// ----------------------------------------------------------------------//
public class FacebookLikeButtonWebView extends WebView {
	private CommunityBeerView mContext;
	String TAG;
	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_FACEBOOK_LIKE_BUTTON = 4;

	public FacebookLikeButtonWebView(Context context) {
		super(context);
		init(context);
	}

	public FacebookLikeButtonWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FacebookLikeButtonWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		mContext = (CommunityBeerView)context;
		// setup TAG
		TAG = mContext.getString(R.string.app_name) + "::"
				+ this.getClass().getName();

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if ((ev.getAction() != MotionEvent.ACTION_OUTSIDE)
				&& (ev.getAction() == MotionEvent.ACTION_UP)) {
			// invalid facebook session
			if (!new Facebook().isSessionValid()) {
				Log.i(TAG, "onTouchEvent: Invalid Facebook Session!");
				/** Handle User Not Logged In **/
				mContext.handleUserNotLoggedInFacebook();

			} else {
				Log.i(TAG, "onTouchEvent: Valid Facebook Session");
			}
		} else {
			Log.i(TAG, "onTouchEvent: event not considered");
		}
		return super.onTouchEvent(ev);
	}

}
