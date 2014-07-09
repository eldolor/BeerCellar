/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cm.beer.facebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cm.beer.activity.R;
import com.cm.beer.facebook.SessionEvents.AuthListener;
import com.cm.beer.facebook.SessionEvents.LogoutListener;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class LoginButton extends ImageButton {

	private Facebook mFb;
	private Handler mHandler;
	private SessionListener mSessionListener = new SessionListener();
	private String[] mPermissions;
	private Activity mActivity;
	private int mActivityCode;

	public LoginButton(Context context) {
		super(context);
	}

	public LoginButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LoginButton(Context context, AttributeSet attrs, int defStyle) {
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

		setBackgroundColor(Color.TRANSPARENT);
		setAdjustViewBounds(true);
		setImageResource(fb.isSessionValid() ? R.drawable.logout_button
				: R.drawable.login_button);
		drawableStateChanged();

		SessionEvents.addAuthListener(mSessionListener);
		SessionEvents.addLogoutListener(mSessionListener);
		setOnClickListener(new ButtonOnClickListener());
	}

	private final class ButtonOnClickListener implements OnClickListener {

		public void onClick(View arg0) {
			Toast.makeText(getContext(), R.string.on_facebook_login_begin,
					Toast.LENGTH_SHORT).show();
			if (mFb.isSessionValid()) {
				SessionEvents.onLogoutBegin();
				AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(mFb);
				asyncRunner.logout(getContext(), new LogoutRequestListener());
			} else {
				mFb.authorize(mActivity, mPermissions, mActivityCode,
						new LoginDialogListener());

			}
		}
	}

	private final class LoginDialogListener implements DialogListener {
		private final String TAG = LoginDialogListener.class.getName();

		public void onComplete(Bundle values) {
			Log.d(TAG, "onComplete::");
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
			Log.d(TAG, "onCancel::");
			SessionEvents.onLoginError("Action Canceled");
		}
	}

	private class LogoutRequestListener extends BaseRequestListener {
		private final String TAG = LogoutRequestListener.class.getName();

		@Override
		public void onComplete(String response, Object state) {
			// callback should be run in the original thread,
			// not the background thread
			mHandler.post(new Runnable() {
				public void run() {
					SessionEvents.onLogoutFinish();
				}
			});
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Log.e(TAG, "onIOException::" + e.getMessage());
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			Log.e(TAG, "onFileNotFoundException::" + e.getMessage());

		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			Log.e(TAG, "onMalformedURLException::" + e.getMessage());

		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			Log.e(TAG, "onFacebookError::" + e.getMessage());

		}
	}

	private class SessionListener implements AuthListener, LogoutListener {
		private final String TAG = SessionListener.class.getName();

		public void onAuthSucceed() {
			setImageResource(R.drawable.logout_button);
			SessionStore.save(mFb, getContext());
		}

		public void onAuthFail(String error) {
			SessionStore.clear(getContext());
			Log.e(TAG, "onAuthFail::" + error);
			setImageResource(R.drawable.login_button);
		}

		public void onLogoutBegin() {
		}

		public void onLogoutFinish() {
			SessionStore.clear(getContext());
			setImageResource(R.drawable.login_button);
		}
	}

}
