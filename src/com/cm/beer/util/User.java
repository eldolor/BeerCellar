package com.cm.beer.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.cm.beer.activity.R;
import com.cm.beer.config.AppConfig;
import com.cm.beer.facebook.SessionStore;

public class User {
	String TAG;
	Context mContext;
	private SharedPreferences mPreferences;

	public User(Context context) {
		mContext = context;

		mPreferences = mContext.getSharedPreferences(
				AppConfig.SHARED_PREFERENCES_DYNAMIC_CONTEXT,
				Activity.MODE_PRIVATE);
		TAG = context.getString(R.string.app_name) + "::"
				+ this.getClass().getName();
		Log.i(TAG, "onCreate::!");
	}

	public String getUserId() {
		return mPreferences.getString(AppConfig.USER_ID, null);
	}

	public String getUserName() {
		return mPreferences.getString(AppConfig.USER_NAME, null);
	}

	public String getUserLink() {
		return mPreferences.getString(AppConfig.USER_LINK, null);
	}

	public String getUserType() {
		return mPreferences.getString(AppConfig.USER_TYPE, null);
	}

	public String getAdditionalUserAttributes() {
		return mPreferences.getString(AppConfig.USER_ADDITIONAL_ATTRIBUTES,
				null);
	}

	/**
	 * Additional user attributes in JSON Object format
	 * 
	 * @param additionalAttributes
	 */
	public void setAdditionalUserAttributes(String additionalAttributes) {
		mPreferences.edit().putString(AppConfig.USER_ADDITIONAL_ATTRIBUTES,
				additionalAttributes).commit();
		Log.i(TAG, "setAdditionalUserAttributes::");

	}

	public boolean isLoggedIn() {
		if ((this.getUserId() == null) || (this.getUserId().equals(""))) {
			Log.i(TAG, "isLoggedIn::false");
			return false;
		} else {
			Log.i(TAG, "isLoggedIn::true");
			return true;
		}
	}

	public void onAuthSucceed(String userId, String userName, String userLink, String userType) {
		Log.i(TAG, "onAuthSucceed::");
		mPreferences.edit().putString(AppConfig.USER_ID, userId).commit();
		mPreferences.edit().putString(AppConfig.USER_NAME, userName).commit();
		mPreferences.edit().putString(AppConfig.USER_LINK, userLink).commit();
		mPreferences.edit().putString(AppConfig.USER_TYPE, userType).commit();
	}

	public void onLogoutFinish() {
		Log.i(TAG, "onLogoutFinish::");
		mPreferences.edit().remove(AppConfig.USER_ID).commit();
		mPreferences.edit().remove(AppConfig.USER_NAME).commit();
		mPreferences.edit().remove(AppConfig.USER_LINK).commit();
		mPreferences.edit().remove(AppConfig.USER_TYPE).commit();
		// clear facebook session
		SessionStore.clear(mContext);
	}

}
