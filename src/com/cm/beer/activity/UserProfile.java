package com.cm.beer.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cm.beer.activity.slidingmenu.CommunityBeersFragment;
import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.util.ContentManager;
import com.cm.beer.util.DrawableManager;
import com.cm.beer.util.Logger;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class UserProfile extends Activity {
	String TAG;
	ProgressDialog mDialog;
	int ACTIVE_DIALOG;
	static final int SIGN_IN_REQUEST = 1;
	static final int SIGN_UP_REQUEST = 2;
	static final int DIALOG_LOGIN_ID = 1;
	static final int DIALOG_RECOVER_PASSWORD_ID = 2;
	static final int DIALOG_LOADING_USER_PROFILE_ID = 3;

	static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_FOLLOW = 1;
	static final int CHANGE_PASSWORD_REQUEST = 3;
	static final int UPDATE_PROFILE_REQUEST = 4;

	static final int MENU_GROUP = 0;
	static final int CHANGE_PASSWORD_ID = Menu.FIRST;
	static final int UPDATE_PROFILE_ID = Menu.FIRST + 1;

	String mUserId;

	TextView mMessage;
	ImageView mThumbnailView;
	TextView mUpdatePhotoPrompt;
	TextView mUserName;
	TextView mUserReviewCount;
	TextView mFollowCount;
	// TextView mUserFavorites;
	TextView mUserBio;

	Button mUpdatePhoto;
	Button mFollowUser;
	Button mViewUserReviews;
	Button mViewUserFavorites;
	Button mViewUserFollowing;

	ImageView mCommunityIcon;

	JSONObject mUserProfileJson;

	DrawableManager mDrawableManager;
	ContentManager mContentManager;

	boolean mAlreadyFollowingReviewer;
	Handler mFollowReviewerHandler;
	Handler mFollowCountHandler;
	Handler mReviewCountHandler;

	GoogleAnalyticsTracker mTracker;
	UserProfile mMainActivity;
	User mUser;

	String mReviewerName, mReviewerId, mReviewerLink;

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
		mDrawableManager = DrawableManager.getInstance();
		mContentManager = ContentManager.getInstance();

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the mTracker with dispatch interval
		mTracker.startNewSession(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID,
				this);
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Instantiated");
		}
		Bundle extras = getIntent().getExtras();
		mUserId = extras != null ? extras.getString("USERID") : null;

		mUser = new User(this);

		new AsyncGetUserProfile().execute(mUserId);
	}

	private void display() throws JSONException {
		setContentView(R.layout.user_profile);

		/****************************************/
		mMessage = (TextView) findViewById(R.id.message);
		// mMessage.setText(R.string.downloading_user_photo_message);
		/****************************************/
		mThumbnailView = (ImageView) findViewById(R.id.thumbnail);
		/****************************************/
		mUpdatePhotoPrompt = (TextView) findViewById(R.id.update_photo_prompt);
		/****************************************/
		mUpdatePhoto = (Button) findViewById(R.id.update_photo);
		mUpdatePhoto.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mUserName = (TextView) findViewById(R.id.user_name);
		/****************************************/
		mUserReviewCount = (TextView) findViewById(R.id.user_review_count);
		/****************************************/
		mUserBio = (TextView) findViewById(R.id.user_bio);
		/****************************************/
		mFollowUser = (Button) findViewById(R.id.follow_user);
		mFollowUser.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mViewUserReviews = (Button) findViewById(R.id.view_user_reviews);
		mViewUserReviews.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mViewUserFavorites = (Button) findViewById(R.id.view_user_favorites);
		mViewUserFavorites.getBackground().setColorFilter(
				AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mViewUserFollowing = (Button) findViewById(R.id.view_user_following);
		mViewUserFollowing.getBackground().setColorFilter(
				AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mFollowCount = (TextView) findViewById(R.id.user_follow_count);
		/****************************************/
		mCommunityIcon = (ImageView) findViewById(R.id.community_icon);
		/****************************************/

		populateFields();
	}

	/**
	 * @throws JSONException
	 * 
	 */
	private void populateFields() throws JSONException {
		if (Logger.isLogEnabled())  Logger.log("populateFields");
		boolean _displayUserPrompt = false;

		mReviewerId = (mUserProfileJson.has("userId")) ? mUserProfileJson
				.getString("userId") : "";
		mReviewerName = (mUserProfileJson.has("userName")) ? mUserProfileJson
				.getString("userName") : "";
		mReviewerLink = (mUserProfileJson.has("userLink")) ? mUserProfileJson
				.getString("userLink") : "";

		boolean _hasPhoto = ((mUserProfileJson.has("hasPhoto")) && (mUserProfileJson
				.getString("hasPhoto").equalsIgnoreCase("Y"))) ? true : false;
		if (Logger.isLogEnabled())  Logger.log("populateFields:hasPhoto: " + ((_hasPhoto) ? "Y" : "N"));
		if (_hasPhoto) {
			Drawable _drawable = mDrawableManager.fetchDrawable(Util
					.getUserPhotoUrl(mReviewerId));
			if (_drawable != null) {
				mThumbnailView.setImageDrawable(_drawable);
				mThumbnailView.setVisibility(View.VISIBLE);
			}

		} else {
			mThumbnailView.setImageResource(R.drawable.user_profile);
			mThumbnailView.setVisibility(View.VISIBLE);
		}
		if (mReviewerId.equalsIgnoreCase(mUser.getUserId())) {
			mUpdatePhoto.setVisibility(View.VISIBLE);
			mUpdatePhoto.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mMainActivity.getApplication(),
							SDCardExplorer.class);
					intent.putExtra("USERID", mReviewerId);
					intent.putExtra("REQUESTCODE",
							AppConfig.UPDATE_USER_PHOTO_REQUEST);
					startActivityForResult(intent,
							AppConfig.UPDATE_USER_PHOTO_REQUEST);

				}
			});
			mFollowUser.setVisibility(View.GONE);
			(findViewById(R.id.divider_follow_user)).setVisibility(View.GONE);

		} else {
			setupFollowReviewer();
		}
		if ((!_hasPhoto) && (mReviewerId.equalsIgnoreCase(mUser.getUserId()))) {
			_displayUserPrompt = true;
		}

		if (_displayUserPrompt) {
			mUpdatePhotoPrompt.setText(R.string.update_photo_prompt);
			mUpdatePhotoPrompt.setVisibility(View.VISIBLE);

		}
		mUserName.setText(mReviewerName);

		if (mUserProfileJson.has("reviewCount")) {
			int count = mUserProfileJson.getInt("reviewCount");
			String _reviewCount = count + " reviews";
			mUserReviewCount.setText(_reviewCount);
			mViewUserReviews.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (Logger.isLogEnabled())  Logger.log("getAllReviews");
					Intent intent = new Intent(mMainActivity.getApplication(),
							CommunityBeersFragment.class);
					intent.putExtra("OPTION", AppConfig.COMMUNITY_SEARCH_BEERS);
					intent.putExtra(NotesDbAdapter.KEY_USER_ID, mReviewerId);
					startActivity(intent);
				}
			});
		} else {
			mUserReviewCount.setVisibility(View.GONE);
			mViewUserReviews.setVisibility(View.GONE);
			(findViewById(R.id.divider_view_user_reviews))
					.setVisibility(View.GONE);
		}

		if (mUserProfileJson.has("followCount")) {
			String _followCountStr = mUserProfileJson.getString("followCount");
			JSONObject _followCountJson = new JSONObject(_followCountStr);
			int followersCount = _followCountJson.getInt("followers");
			int followingCount = _followCountJson.getInt("following");

			String _followCount = "Followers " + followersCount + " Following "
					+ followingCount;

			mFollowCount.setText(_followCount);
			if (followingCount == 0) {
				mViewUserFollowing.setVisibility(View.GONE);
			}
		} else {
			mFollowCount.setVisibility(View.GONE);
			mViewUserFollowing.setVisibility(View.GONE);
		}

		if (mUserProfileJson.has("bio")) {
			mUserBio.setText(mUserProfileJson.getString("bio"));
		} else {
			mUserBio.setVisibility(View.GONE);
		}

		// if the user has no favorites
		if (!mUserProfileJson.has("favorites")) {
			mViewUserFavorites.setVisibility(View.GONE);
			(findViewById(R.id.divider_view_user_favorites))
					.setVisibility(View.GONE);
		}

		mViewUserFavorites.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Logger.isLogEnabled())  Logger.log("getFavorites");
				Intent intent = new Intent(mMainActivity.getApplication(),
						CommunityBeersFragment.class);
				intent.putExtra("OPTION",
						AppConfig.COMMUNITY_FAVORITE_BEER_REVIEWS);
				intent.putExtra("USERID", mReviewerId);
				startActivity(intent);

			}
		});

		mViewUserFollowing.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Logger.isLogEnabled())  Logger.log("getFollowing");
				Intent intent = new Intent(mMainActivity.getApplication(),
						CommunityFollow.class);
				intent.putExtra("OPTION", AppConfig.COMMUNITY_FOLLOWING);
				intent.putExtra("USERID", mReviewerId);
				startActivity(intent);
			}
		});

		setupFollowCount();

		setupCommunityIcon();

		// clear message
		// mMessage.setText("");
	}

	private void setupCommunityIcon() {
		mCommunityIcon.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (Logger.isLogEnabled())  Logger.log("back to community options menu");

				AlertDialog.Builder dialog = new AlertDialog.Builder(
						mMainActivity);
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.setTitle(R.string.community_progress_dialog_title);
				dialog.setMessage(R.string.back_to_community_menu);
				dialog.setPositiveButton(R.string.yes_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Intent intent = new Intent(mMainActivity
										.getApplication(),
										CommunityOptions.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
								mMainActivity.finish();
							}
						});
				dialog.setNegativeButton(R.string.no_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				dialog.create();
				dialog.show();
			}
		});

	}

	/**
	 * 
	 */
	private void setupFollowReviewer() {
		mFollowReviewerHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				String jsonStr = (String) message.obj;
				if (Logger.isLogEnabled())  Logger.log("setupFollowReviewer(): " + jsonStr);
				if ((jsonStr != null) && (jsonStr.startsWith("{"))) {
					try {
						JSONObject mFollowJson = new JSONObject(jsonStr);
						JSONArray followingList = mFollowJson
								.getJSONArray("followingList");
						Log.i(TAG,
								"Following List Size=" + followingList.length());
						String _str;
						for (int i = 0; i < followingList.length(); i++) {
							_str = followingList.getString(i);
							if (Logger.isLogEnabled())  Logger.log("Following " + _str);
							if ((_str != null)
									&& (_str.equalsIgnoreCase(mReviewerId))) {
								mAlreadyFollowingReviewer = true;
								if (Logger.isLogEnabled())  Logger.log("Already Following Receiver!");
							}
						}

					} catch (Throwable e) {
						Log.e(TAG, "error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
					}
				}
				if (mAlreadyFollowingReviewer) {
					mFollowUser.getBackground().setColorFilter(
							AppConfig.BUTTON_COLOR_RED,
							PorterDuff.Mode.MULTIPLY);
					mFollowUser.setText("Unfollow " + mReviewerName);
				} else {
					mFollowUser.getBackground().setColorFilter(
							AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
					mFollowUser.setText("Follow " + mReviewerName);
				}
				mFollowUser.setOnClickListener(new OnClickListener() {
					String unFollowUrl = Util.getSetUnfollowUrl(
							mUser.getUserId(), mUser.getUserName(),
							mUser.getUserLink(), mReviewerId, mReviewerName,
							mReviewerLink);
					String followUrl = Util.getSetFollowUrl(mUser.getUserId(),
							mUser.getUserName(), mUser.getUserLink(),
							mReviewerId, mReviewerName, mReviewerLink);

					@Override
					public void onClick(View arg0) {
						if (Logger.isLogEnabled())  Logger.log("Follow: " + mReviewerName);

						if (mUser.isLoggedIn()) {

							if (mAlreadyFollowingReviewer) {
								Log.i(TAG,
										"Resetting Button from UNFOLLOW to FOLLOW");
								// change the color and content of the button
								mFollowUser.getBackground().setColorFilter(
										AppConfig.BUTTON_COLOR,
										PorterDuff.Mode.MULTIPLY);
								mFollowUser.setText("Follow " + mReviewerName);
								// reset
								mAlreadyFollowingReviewer = false;
								new AsyncPostFollow().execute(unFollowUrl);
								mTracker.trackEvent("CommunityBeerView",
										"UnFollow", "Clicked", 0);
								mTracker.dispatch();
							} else {
								Log.i(TAG,
										"Resetting Button from FOLLOW to UNFOLLOW");
								// change the color and content of the button
								mFollowUser.getBackground().setColorFilter(
										AppConfig.BUTTON_COLOR_RED,
										PorterDuff.Mode.MULTIPLY);
								mFollowUser
										.setText("Unfollow " + mReviewerName);
								// reset
								mAlreadyFollowingReviewer = true;
								new AsyncPostFollow().execute(followUrl);
								mTracker.trackEvent("CommunityBeerView",
										"Follow", "Clicked", 0);
								mTracker.dispatch();
							}

						} else {
							/** Handle User Not Logged In **/
							Intent intent = new Intent(mMainActivity
									.getApplication(), LoginIntercept.class);
							intent.putExtra("FACEBOOK_PERMISSIONS",
									AppConfig.FACEBOOK_PERMISSIONS);
							startActivityForResult(intent,
									LOGIN_INTERCEPT_REQUEST_CODE_FOR_FOLLOW);

						}
					}
				});

			}
		};

		// trigger point
		String _url = Util.getFollowUrl(mUser.getUserId());
		if (Logger.isLogEnabled())  Logger.log("Follow Url: " + _url);
		mContentManager.fetchContentOnThread(_url, mFollowReviewerHandler);
	}

	private void setupFollowCount() {
		mFollowCountHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				String jsonStr = (String) message.obj;
				if (Logger.isLogEnabled())  Logger.log(jsonStr);
				if ((jsonStr != null) && (jsonStr.startsWith("{"))) {
					JSONObject json;
					try {
						json = new JSONObject(jsonStr);
						String followers = json.getString("followers");
						String following = json.getString("following");

						String _reviewCount = "Followers " + followers
								+ " Following " + following;
						if (Logger.isLogEnabled())  Logger.log(_reviewCount);
						mFollowCount.setText(_reviewCount);
					} catch (JSONException e) {
						Log.e(TAG, "error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
					}
				}
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreateOptionsMenu");
		}
		super.onCreateOptionsMenu(menu);
		int menuPosition = 0;
		if ((mUser != null) && (mUser.getUserId() != null)) {
			if (mReviewerId.equalsIgnoreCase(mUser.getUserId())) {
				if ((mUser.getUserType() != null)
						&& (mUser.getUserType()
								.equals(AppConfig.USER_TYPE_COMMUNITY))) {
					menu.add(MENU_GROUP, CHANGE_PASSWORD_ID, menuPosition++,
							R.string.change_password);
				}
				menu.add(MENU_GROUP, UPDATE_PROFILE_ID, menuPosition++,
						R.string.update_profile);
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onMenuItemSelected");
		}
		switch (item.getItemId()) {
		case CHANGE_PASSWORD_ID:
			changePassword();
			return true;
		case UPDATE_PROFILE_ID:
			updateProfile();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void changePassword() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("changePassword");
		}
		Intent i = new Intent(this, CommunityChangePassword.class);
		i.putExtra("USERID", mUser.getUserId());
		startActivityForResult(i, CHANGE_PASSWORD_REQUEST);
	}

	private void updateProfile() {
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("updateProfile");
		}
		Intent i = new Intent(this, UpdateUserProfile.class);
		i.putExtra("USERID", mUser.getUserId());
		startActivityForResult(i, UPDATE_PROFILE_REQUEST);
	}

	/**
	 * 
	 */

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
		// Stop the mTracker when it is no longer needed.
		mTracker.stop();
		if (AppConfig.LOGGING_ENABLED) {
			if (Logger.isLogEnabled())  Logger.log("onCreate:Google Tracker Stopped!");
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
		if (Logger.isLogEnabled())  Logger.log("onCreateDialog");
		String dialogMessage = null;
		if (id == AppConfig.DIALOG_LOADING_ID) {
			dialogMessage = this.getString(R.string.progress_loading_message);
			ACTIVE_DIALOG = AppConfig.DIALOG_LOADING_ID;
		} else if (id == DIALOG_LOADING_USER_PROFILE_ID) {
			dialogMessage = this.getString(R.string.progress_loading_message);
			ACTIVE_DIALOG = DIALOG_LOADING_USER_PROFILE_ID;
		}

		mDialog = ProgressDialog.show(UserProfile.this, null, dialogMessage,
				true, true);
		mDialog.setCanceledOnTouchOutside(false);
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
			if (Logger.isLogEnabled())  Logger.log("onResume");
		}
		if ((mDialog != null) && (mDialog.isShowing())) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("onResume:active dialog removed");
			}
			// removeDialog(AppConfig.DIALOG_LOADING_ID);
		}
		// reset cache
		DrawableManager.getInstance().clear();
		ContentManager.getInstance().clear();

		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (Logger.isLogEnabled())  Logger.log("onActivityResult: requestCode: " + requestCode
				+ " resultCode: " + resultCode);
		if (requestCode == LOGIN_INTERCEPT_REQUEST_CODE_FOR_FOLLOW) {
			if (resultCode == RESULT_OK) {
				String _setFollowUrl = Util.getSetFollowUrl(mUser.getUserId(),
						mUser.getUserName(), mUser.getUserLink(), mReviewerId,
						mReviewerName, mReviewerLink);
				new AsyncPostFollow().execute(_setFollowUrl);
				mTracker.trackEvent("CommunityBeerView", "Follow", "Clicked", 0);
				mTracker.dispatch();
				mFollowUser.getBackground().setColorFilter(
						AppConfig.BUTTON_COLOR_RED, PorterDuff.Mode.MULTIPLY);
				mFollowUser.setText("Unfollow " + mReviewerName);
				// Get follow
				String _followUrl = Util.getFollowUrl(mUser.getUserId());
				mContentManager.fetchContentOnThread(_followUrl);
			}
		} else if (requestCode == AppConfig.UPDATE_USER_PHOTO_REQUEST) {
			if (resultCode == RESULT_OK) {
				new AsyncGetUserProfile().execute(mUserId);
			}

		} else if (requestCode == UPDATE_PROFILE_REQUEST) {
			if (resultCode == RESULT_OK) {
				if (Logger.isLogEnabled())  Logger.log("User Profile Updated");
				new AsyncGetUserProfile().execute(mUserId);
			}
		}

	}

	/************************************************************************************/
	private class AsyncGetUserProfile extends AsyncTask<Object, Void, Object> {

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (Logger.isLogEnabled())  Logger.log("doInBackground starting");
			String _userId = (String) args[0];
			try {

				String _url = Util.getUserProfileUrl(_userId);
				String response[] = Util.getResult(_url);
				if ((response[0] != null) && (response[0].startsWith("{"))) {
					mUserProfileJson = new JSONObject(response[0]);
				}

			} catch (Throwable e) {
				Log.e(TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
				mTracker.trackEvent(
						"UserProfile",
						"AsyncGetUserProfile",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			if (Logger.isLogEnabled())  Logger.log("doInBackground finished");
			return null;
		}

		@Override
		protected void onPreExecute() {
			if (Logger.isLogEnabled())  Logger.log("onPreExecute starting");
			if (mMainActivity != null) {
				mMainActivity.showDialog(AppConfig.DIALOG_LOADING_ID);
			}
			if (Logger.isLogEnabled())  Logger.log("onPreExecute finished");
		}

		protected void onPostExecute(Object result) {
			if (Logger.isLogEnabled())  Logger.log("onPostExecute starting");
			try {
				if (mUserProfileJson != null) {
					mMainActivity.display();
				} else {
					// TODO:
					AlertDialog.Builder dialog = new AlertDialog.Builder(
							new ContextThemeWrapper(mMainActivity,
									android.R.style.Theme_Dialog));
					dialog.setIcon(android.R.drawable.ic_dialog_alert);
					dialog.setTitle(R.string.unable_to_download_user_profile_message);
					dialog.setPositiveButton(
							R.string.ok_label,
							new android.content.DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent();
									mMainActivity.setResult(RESULT_CANCELED,
											intent);
									mMainActivity.finish();
								}
							});
					dialog.show();

				}
			} catch (Throwable e) {
				Log.e(TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
				mTracker.trackEvent(
						"UserProfile",
						"AsyncGetUserProfile",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			mDialog.cancel();

			if (Logger.isLogEnabled())  Logger.log("onPostExecute finished");
		}

	}

	/************************************************************************************/
	private class AsyncPostFollow extends AsyncTask<Object, Void, Object> {

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (Logger.isLogEnabled())  Logger.log("doInBackground starting");
			String url = (String) args[0];
			try {
				Util.getResult(url);
				/** Remove follow count from cache **/
				mContentManager.removeContent(Util
						.getFollowCountUrl(mReviewerId));
				/** Remove follow from cache **/
				mContentManager.removeContent(Util.getFollowUrl(mUser
						.getUserId()));

				/** Fetch updated Follow Count **/
				mContentManager.fetchContentOnThread(
						Util.getFollowCountUrl(mReviewerId),
						mFollowCountHandler);
				/** Fetch updated Follow for the logged in user **/
				mContentManager.fetchContentOnThread(
						Util.getFollowUrl(mUser.getUserId()),
						mFollowReviewerHandler);
			} catch (Throwable e) {
				Log.e(TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView",
						"AsyncPostFollowError", ((e.getMessage() != null) ? e
								.getMessage().replace(" ", "_") : "").replace(
								" ", "_"), 0);
				mTracker.dispatch();
			}

			if (Logger.isLogEnabled())  Logger.log("doInBackground finished");
			return null;
		}

		protected void onPostExecute(Object result) {
			if (Logger.isLogEnabled())  Logger.log("onPostExecute starting");
			if (Logger.isLogEnabled())  Logger.log("onPostExecute finished");
		}

	}

}
