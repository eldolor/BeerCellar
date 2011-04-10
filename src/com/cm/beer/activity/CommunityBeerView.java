package com.cm.beer.activity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cm.beer.config.AppConfig;
import com.cm.beer.transfer.CommunityBeer;
import com.cm.beer.util.ContentManager;
import com.cm.beer.util.DrawableManager;
import com.cm.beer.util.FacebookLikeButtonWebView;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class CommunityBeerView extends Activity {
	protected static final int VIEW_IMAGE_ACTIVITY_REQUEST_CODE = 0;

	String TAG;

	ProgressDialog mDialog;
	ProgressDialog mSplashDialog;
	int mActiveDialog;

	SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM d, yyyy");

	GoogleAnalyticsTracker mTracker;
	CommunityBeerView mMainActivity;

	String mUrl;
	CommunityBeer mCommunityBeer;

	// Button mYes;
	// Button mNo;
	FacebookLikeButtonWebView mFacebookLikeWebView;
	// TextView mRatingThankYouMessage;
	// TextView mReviewHelpfulMessage;
	TextView mReviewedByLabel;
	TextView mReviewedBy;
	TextView mReviewedByReviewCount;

	Button mFavorites;

	Button mFollowReviewer;
	TextView mFollowCount;
	LinearLayout mUserProfileHeader;
	Button mViewUserProfile;

	TextView mBeer;
	TextView mAlcohol;
	TextView mStyle;
	TextView mBrewery;
	TextView mState;
	TextView mCountry;
	RatingBar mRating;
	TextView mNotes;
	Button mTranslateNotes;
	TextView mPrice;
	ImageView mThumbnailView;
	TextView mDateCreated;
	TextView mDateUpdated;

	TextView mColorTE;
	TextView mClarityTE;
	TextView mFoamTE;
	TextView mAromaTE;
	TextView mBodyTE;
	TextView mMouthfeelTE;
	TextView mAftertasteTE;
	Button mTranslateCharacteristics;

	DrawableManager mDrawableManager;
	ContentManager mContentManager;

	ImageView mCommunityIcon;

	User mUser;

	boolean mAlreadyFollowingReviewer;
	Handler mFollowReviewerHandler;
	Handler mFollowCountHandler;
	Handler mReviewCountHandler;
	Handler mReviewHelpfulCountHandler;

	boolean mAlreadyFavorite;
	Handler mFavoritesHandler;

	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_REVIEW_HELPFUL_Y = 0;
	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_REVIEW_HELPFUL_N = 1;
	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_FOLLOW = 2;
	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_ADD_TO_FAVORITES = 3;
	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_FACEBOOK_LIKE_BUTTON = 4;

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
		mDrawableManager = DrawableManager.getInstance();
		mContentManager = ContentManager.getInstance();

		mTracker = GoogleAnalyticsTracker.getInstance();
		// Start the mTracker with dispatch interval
		mTracker.start(AppConfig.GOOGLE_ANALYTICS_WEB_PROPERTY_ID, this);
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "onCreate:Google Tracker Instantiated");
		}

		Bundle extras = getIntent().getExtras();
		mUrl = extras != null ? extras.getString("URL") : null;
		mCommunityBeer = (CommunityBeer) extras
				.getSerializable("COMMUNITY_BEER");

		mSplashDialog = ProgressDialog.show(this,
				getString(R.string.community_progress_dialog_title),
				getString(R.string.community_progress_searching_message), true,
				true);
		setContentView(R.layout.community_beer_view);

		mUser = new User(this);

		display();
		populateFields();
		mSplashDialog.cancel();
		String _selection = mCommunityBeer.beer.replace(" ", "_") + ","
				+ mCommunityBeer.beerId;
		mTracker.trackEvent("CommunityBeerView", "View", _selection, 0);
		mTracker.dispatch();
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
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.i(TAG, "onResume");
		if ((mUser != null) && (mUser.isLoggedIn())
				&& (mFollowReviewerHandler != null)) {
			mContentManager.fetchContentOnThread(Util.getFollowUrl(mUser
					.getUserId()), mFollowReviewerHandler);
		}
		mBrewery.setText(mCommunityBeer.brewery);
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
		// if (requestCode == LOGIN_INTERCEPT_REQUEST_CODE_FOR_REVIEW_HELPFUL_Y)
		// {
		// if (resultCode == RESULT_OK) {
		// String _url = Util.getSetReviewHelpfulUrl(
		// mCommunityBeer.userId, mCommunityBeer.beerId, mUser
		// .getUserId(), "Y");
		// new AsyncPostReviewHelpfulRating().execute(_url);
		// // mRatingThankYouMessage.setVisibility(View.VISIBLE);
		//
		// Log.i(TAG, "onActivityResult:");
		// }
		// } else if (requestCode ==
		// LOGIN_INTERCEPT_REQUEST_CODE_FOR_REVIEW_HELPFUL_N) {
		// if (resultCode == RESULT_OK) {
		// String _url = Util.getSetReviewHelpfulUrl(
		// mCommunityBeer.userId, mCommunityBeer.beerId, mUser
		// .getUserId(), "N");
		// new AsyncPostReviewHelpfulRating().execute(_url);
		// // mRatingThankYouMessage.setVisibility(View.VISIBLE);
		//
		// Log.i(TAG, "onActivityResult:");
		// }
		if (requestCode == LOGIN_INTERCEPT_REQUEST_CODE_FOR_FOLLOW) {
			if (resultCode == RESULT_OK) {
				String _setFollowUrl = Util.getSetFollowUrl(mUser.getUserId(),
						mUser.getUserName(), mUser.getUserLink(),
						mCommunityBeer.userId, mCommunityBeer.userName,
						mCommunityBeer.userLink);
				new AsyncPostFollow().execute(_setFollowUrl);
				mTracker
						.trackEvent("CommunityBeerView", "Follow", "Clicked", 0);
				mTracker.dispatch();
				mFollowReviewer.getBackground().setColorFilter(
						AppConfig.BUTTON_COLOR_RED, PorterDuff.Mode.MULTIPLY);
				mFollowReviewer.setText("Unfollow " + mCommunityBeer.userName);
				// Get follow
				String _followUrl = Util.getFollowUrl(mUser.getUserId());
				mContentManager.fetchContentOnThread(_followUrl);

				setupFacebookLikeButton();

				Log.i(TAG, "onActivityResult:");
			}
		} else if (requestCode == LOGIN_INTERCEPT_REQUEST_CODE_FOR_ADD_TO_FAVORITES) {
			if (resultCode == RESULT_OK) {
				String _addToFavoritesUrl = Util.getAddToFavoritesUrl(mUser
						.getUserId(), mUser.getUserName(), mUser.getUserLink(),
						mCommunityBeer.beerId);
				new AsyncPostFavorites().execute(_addToFavoritesUrl);
				mTracker.trackEvent("CommunityBeerView", "AddToFavorites",
						"Clicked", 0);
				mTracker.dispatch();

				mFavorites.getBackground().setColorFilter(
						AppConfig.BUTTON_COLOR_RED, PorterDuff.Mode.MULTIPLY);
				mFollowReviewer.setText(R.string.remove_from_favorites_label);
				// Get favorites
				String _favoritesUrl = Util.getFavoritesUrl(mUser.getUserId());
				mContentManager.fetchContentOnThread(_favoritesUrl);

				setupFacebookLikeButton();

				Log.i(TAG, "onActivityResult:");
			}
		} else if (requestCode == LOGIN_INTERCEPT_REQUEST_CODE_FOR_FACEBOOK_LIKE_BUTTON) {
			if (resultCode == RESULT_OK) {
				setupFacebookLikeButton();
				Log
						.i(TAG,
								"onActivityResult:LOGIN_INTERCEPT_REQUEST_CODE_FOR_FACEBOOK_LIKE_BUTTON");
			}
		}
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
		if (id == AppConfig.DIALOG_LOADING_ID) {
			dialogMessage = this.getString(R.string.progress_loading_message);
			mActiveDialog = AppConfig.DIALOG_LOADING_ID;
		} else if (id == AppConfig.DIALOG_SAVING_ID) {
			dialogMessage = this.getString(R.string.progress_saving_message);
			mActiveDialog = AppConfig.DIALOG_SAVING_ID;
		} else if (id == AppConfig.DIALOG_DELETING_ID) {
			dialogMessage = this.getString(R.string.progress_deleting_message);
			mActiveDialog = AppConfig.DIALOG_DELETING_ID;
		}
		mDialog = ProgressDialog.show(this, null, dialogMessage, true, true);
		mDialog.setCanceledOnTouchOutside(true);
		return mDialog;
	}

	/*
	 * 
	 */
	protected void display() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "display");
		}
		/****************************************/
		mFavorites = (Button) findViewById(R.id.favorites);
		mFavorites.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		/****************************************/
		// mYes = (Button) findViewById(R.id.review_helpful_yes);
		// mYes.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
		// PorterDuff.Mode.MULTIPLY);
		/****************************************/
		// mNo = (Button) findViewById(R.id.review_helpful_no);
		// mNo.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
		// PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mFacebookLikeWebView = (FacebookLikeButtonWebView) findViewById(R.id.facebook_like_webview);
		mFacebookLikeWebView.getSettings().setJavaScriptEnabled(true);
		/****************************************/
		// mRatingThankYouMessage = (TextView)
		// findViewById(R.id.rating_thank_you_message);
		/****************************************/
		// mReviewHelpfulMessage = (TextView)
		// findViewById(R.id.review_helpful_message);
		/****************************************/
		mThumbnailView = (ImageView) findViewById(R.id.thumbnail);
		/****************************************/
		mDateCreated = (TextView) findViewById(R.id.date_created_label);
		/****************************************/
		mDateUpdated = (TextView) findViewById(R.id.date_updated_label);
		/****************************************/
		mUserProfileHeader = (LinearLayout) findViewById(R.id.user_profile_header);
		mViewUserProfile = (Button) findViewById(R.id.view_user_profile);
		mViewUserProfile.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mReviewedByLabel = (TextView) findViewById(R.id.reviewed_by_label);
		/****************************************/
		mReviewedBy = (TextView) findViewById(R.id.reviewed_by);
		/****************************************/
		mReviewedByReviewCount = (TextView) findViewById(R.id.reviewed_by_review_count);
		/****************************************/
		mFollowReviewer = (Button) findViewById(R.id.follow_reviewer);
		/****************************************/
		mFollowCount = (TextView) findViewById(R.id.follow_count);
		/****************************************/
		mBeer = (TextView) findViewById(R.id.beer);
		/****************************************/
		mAlcohol = (TextView) findViewById(R.id.alcohol);
		/****************************************/
		mPrice = (TextView) findViewById(R.id.price);
		/****************************************/
		mStyle = (TextView) findViewById(R.id.style);
		/****************************************/
		mBrewery = (TextView) findViewById(R.id.brewery);
		/****************************************/
		mState = (TextView) findViewById(R.id.state);
		/****************************************/
		mCountry = (TextView) findViewById(R.id.country);
		/****************************************/
		mRating = (RatingBar) findViewById(R.id.rating);
		/****************************************/
		mNotes = (TextView) findViewById(R.id.notes);
		/****************************************/
		mTranslateNotes = (Button) findViewById(R.id.translate);
		mTranslateNotes.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mCommunityIcon = (ImageView) findViewById(R.id.community_icon);
		/****************************************/
		mColorTE = (TextView) findViewById(R.id.color);
		mClarityTE = (TextView) findViewById(R.id.clarity);
		mFoamTE = (TextView) findViewById(R.id.foam);
		mAromaTE = (TextView) findViewById(R.id.aroma);
		mBodyTE = (TextView) findViewById(R.id.body);
		mMouthfeelTE = (TextView) findViewById(R.id.mouthfeel);
		mAftertasteTE = (TextView) findViewById(R.id.aftertaste);
		/****************************************/
		mTranslateCharacteristics = (Button) findViewById(R.id.translate_characteristics);
		mTranslateCharacteristics.getBackground().setColorFilter(
				AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
	}

	/**
	 * 
	 */
	private void populateFields() {
		if (AppConfig.LOGGING_ENABLED) {
			Log.i(TAG, "populateFields");
		}
		// GET USER ID
		// setupYes();
		// setupNo();

		setupFacebookLikeButton();

		// setup view user profile button
		setupViewUserProfile();

		// setup review helpful count message
		// setupReviewHelpfulCount();

		mThumbnailView.setImageDrawable(mDrawableManager.fetchDrawable(Util
				.getImageUrl(mCommunityBeer.beerId)));

		mDateCreated.setText("Added on "
				+ mDateFormat.format(new Date(Long
						.parseLong(mCommunityBeer.created))));
		mDateUpdated.setText("Last updated on "
				+ mDateFormat.format(new Date(Long
						.parseLong(mCommunityBeer.updated))));

		if ((mCommunityBeer.userId != null)
				&& (!mCommunityBeer.userId.equals(""))) {
			mReviewedBy.setText(mCommunityBeer.userName);
			// setup review count
			// setupReviewCount();
			// setup follow reviewer button
			setupFollowReviewer();

			// setup follow count
			setupFollowCount();

		} else {
			mReviewedByLabel.setVisibility(View.GONE);
			mReviewedBy.setVisibility(View.GONE);
			mReviewedByReviewCount.setVisibility(View.GONE);
			mFollowCount.setVisibility(View.GONE);
			mFollowReviewer.setVisibility(View.GONE);
		}

		mBeer.setText(mCommunityBeer.beer);
		Log.i(TAG, "populateFields::setting:" + mCommunityBeer.beer);
		mAlcohol.setText(mCommunityBeer.alcohol);
		Log.i(TAG, "populateFields::setting:" + mCommunityBeer.alcohol);
		mPrice.setText(mCommunityBeer.currency + mCommunityBeer.price);
		mStyle.setText(mCommunityBeer.style);

		mBrewery.setText(mCommunityBeer.brewery);

		if ((mCommunityBeer.breweryLink != null)
				&& (!mCommunityBeer.breweryLink.equals(""))
				&& (!mCommunityBeer.breweryLink.equals("null"))) {
			// if link does not start with http:// then add to it
			final String _link = (!mCommunityBeer.breweryLink
					.startsWith("http://")) ? ("http://" + mCommunityBeer.breweryLink)
					: mCommunityBeer.breweryLink;
			mBrewery.setTextColor(android.graphics.Color.BLUE);
			mBrewery.setPaintFlags(mBrewery.getPaintFlags()
					| Paint.UNDERLINE_TEXT_FLAG);
			mBrewery.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mBrewery.setText(R.string.progress_loading_message);
					Intent intent = new Intent(mMainActivity.getApplication(),
							BeerWebView.class);
					intent.putExtra("URL", _link);
					startActivity(intent);

				}
			});
		}

		mState.setText(mCommunityBeer.state);
		mCountry.setText(mCommunityBeer.country);
		Log.i(TAG, "populateFields::setting:" + mCommunityBeer.country);
		String ratingStr = mCommunityBeer.rating;
		if (ratingStr != null) {
			mRating.setRating(Float.valueOf(ratingStr));
		}
		mNotes.setText(mCommunityBeer.notes);
		mTranslateNotes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mTranslateNotes.setText(R.string.translating_label);
				mTranslateNotes.getBackground().setColorFilter(
						AppConfig.BUTTON_COLOR_RED, PorterDuff.Mode.MULTIPLY);
				new AsyncTranslate().execute();
				mTracker.trackEvent("CommunityBeerView", "TranslateNotes",
						"Clicked", 0);
				mTracker.dispatch();
			}
		});
		try {
			setCharacteristicsTable();
		} catch (JSONException e1) {
			Log.e(TAG, e1.getMessage(), e1);
		}

		mTranslateCharacteristics.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mTranslateCharacteristics.setText(R.string.translating_label);
				mTranslateCharacteristics.getBackground().setColorFilter(
						AppConfig.BUTTON_COLOR_RED, PorterDuff.Mode.MULTIPLY);
				new AsyncTranslateCharacteristics().execute();
				mTracker.trackEvent("CommunityBeerView",
						"TranslateCharacteristics", "Clicked", 0);
				mTracker.dispatch();
			}
		});

		setupAddToFavorites();

		setupCommunityIcon();
	}

	private void setupFacebookLikeButton() {
		try {
			String facebookToken = this.getSharedPreferences(
					((AppConfig.SHARED_PREFERENCES_DYNAMIC_CONTEXT.replace(" ",
							"_")) + "_FACEBOOK"), Context.MODE_PRIVATE)
					.getString(AppConfig.FACEBOOK_ACCESS_TOKEN, null);
			String url = AppConfig.FACEBOOK_LIKE_URL_BASE
					+ mCommunityBeer.beerId + AppConfig.FACEBOOK_LIKE_URL_ETC
					+ AppConfig.FACEBOOK_LIKE_URL_LOCALE + Locale.getDefault()
					+ AppConfig.FACEBOOK_LIKE_URL_ACCESS_TOKEN + facebookToken;
			// String encodedUrl = URLEncoder.encode(url, "UTF-8");
			Log.d(TAG, "FacebookLikeButton URL: " + url);
			mFacebookLikeWebView
					.setWebViewClient(new FacebookLikeButtonWebViewClient());
			mFacebookLikeWebView.loadUrl(url);
		} catch (Exception e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}

	}

	private void setupViewUserProfile() {
		String _userId = mCommunityBeer.userId;
		if ((_userId != null) && (!_userId.equals(""))) {
			mUserProfileHeader.setVisibility(View.VISIBLE);
			mViewUserProfile.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(mMainActivity.getApplication(),
							UserProfile.class);
					intent.putExtra("USERID", mCommunityBeer.userId);
					startActivity(intent);
				}
			});
		}

	}

	private void setCharacteristicsTable() throws JSONException {
		if ((mCommunityBeer.characteristics != null)
				&& mCommunityBeer.characteristics.startsWith("{")) {
			JSONObject _characteristics = new JSONObject(
					mCommunityBeer.characteristics);

			if (_characteristics.has("color")) {
				if (!_characteristics.getString("color").equals("")) {
					((TextView) findViewById(R.id.color_label))
							.setVisibility(View.VISIBLE);
					mColorTE.setVisibility(View.VISIBLE);
					mColorTE.setText(_characteristics.getString("color"));
				}
			}
			if (_characteristics.has("clarity")) {
				if (!_characteristics.getString("clarity").equals("")) {
					((TextView) findViewById(R.id.clarity_label))
							.setVisibility(View.VISIBLE);
					mClarityTE.setVisibility(View.VISIBLE);
					mClarityTE.setText(_characteristics.getString("clarity"));
				}
			}
			if (_characteristics.has("foam")) {
				if (!_characteristics.getString("foam").equals("")) {
					((TextView) findViewById(R.id.foam_label))
							.setVisibility(View.VISIBLE);
					mFoamTE.setVisibility(View.VISIBLE);
					mFoamTE.setText(_characteristics.getString("foam"));
				}
			}
			if (_characteristics.has("aroma")) {
				if (_characteristics.getJSONArray("aroma").length() > 0) {
					JSONArray _aroma = _characteristics.getJSONArray("aroma");
					StringBuilder _aromaText = new StringBuilder();
					for (int i = 0; i < _aroma.length(); i++) {
						_aromaText.append(", ");
						_aromaText.append(_aroma.getString(i));
					}
					String _aromaStr = _aromaText.toString();
					_aromaStr = _aromaStr.replaceFirst(", ", "");
					((TextView) findViewById(R.id.aroma_label))
							.setVisibility(View.VISIBLE);
					mAromaTE.setVisibility(View.VISIBLE);
					mAromaTE.setText(_aromaStr);
				}
			}
			if (_characteristics.has("mouthfeel")) {
				if (!_characteristics.getString("mouthfeel").equals("")) {
					((TextView) findViewById(R.id.mouthfeel_label))
							.setVisibility(View.VISIBLE);
					mMouthfeelTE.setVisibility(View.VISIBLE);
					mMouthfeelTE.setText(_characteristics
							.getString("mouthfeel"));
				}
			}
			if (_characteristics.has("body")) {
				if (!_characteristics.getString("body").equals("")) {
					((TextView) findViewById(R.id.body_label))
							.setVisibility(View.VISIBLE);
					mBodyTE.setVisibility(View.VISIBLE);
					mBodyTE.setText(_characteristics.getString("body"));
				}
			}
			if (_characteristics.has("aftertaste")) {
				if (!_characteristics.getString("aftertaste").equals("")) {
					((TextView) findViewById(R.id.aftertaste_label))
							.setVisibility(View.VISIBLE);
					mAftertasteTE.setVisibility(View.VISIBLE);
					mAftertasteTE.setText(_characteristics
							.getString("aftertaste"));
				}
			}
		}

	}

	private void setupAddToFavorites() {
		// setup review count
		mFavoritesHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				String jsonStr = (String) message.obj;
				Log.i(TAG, jsonStr);
				if ((jsonStr != null) && (jsonStr.startsWith("["))) {
					JSONArray json;
					try {
						json = new JSONArray(jsonStr);
						Log.d(TAG, json.toString());
						for (int i = 0; i < json.length(); i++) {
							String _beerId = json.getString(i);
							if (_beerId.equals(mCommunityBeer.beerId)) {
								mAlreadyFavorite = true;
								break;
							}
						}
					} catch (Throwable e) {
						Log.e(TAG, "error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
					}
				}
				if (mAlreadyFavorite) {
					mFavorites.getBackground().setColorFilter(
							AppConfig.BUTTON_COLOR_RED,
							PorterDuff.Mode.MULTIPLY);
					mFavorites.setText(R.string.remove_from_favorites_label);
				} else {
					mFavorites.getBackground().setColorFilter(
							AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
					mFavorites.setText(R.string.add_to_favorites_label);
				}
				mFavorites.setOnClickListener(new OnClickListener() {

					String addToFavoritesUrl = Util.getAddToFavoritesUrl(mUser
							.getUserId(), mUser.getUserName(), mUser
							.getUserLink(), mCommunityBeer.beerId);
					String removeFromFavoritesUrl = Util
							.getRemoveFromFavoritesUrl(mUser.getUserId(), mUser
									.getUserName(), mUser.getUserLink(),
									mCommunityBeer.beerId);

					@Override
					public void onClick(View arg0) {
						Log.i(TAG, "Favorites: " + mCommunityBeer.userName);

						if (mUser.isLoggedIn()) {

							if (mAlreadyFavorite) {
								Log
										.i(TAG,
												"Resetting Button from FAVORITE to REMOVE FROM FAVORITE");
								// change the color and content of the button
								mFavorites.getBackground().setColorFilter(
										AppConfig.BUTTON_COLOR,
										PorterDuff.Mode.MULTIPLY);
								mFavorites
										.setText(R.string.add_to_favorites_label);
								// reset
								mAlreadyFavorite = false;
								new AsyncPostFavorites()
										.execute(removeFromFavoritesUrl);
								mTracker.trackEvent("CommunityBeerView",
										"RemoveFromFavorites", "Clicked", 0);
								mTracker.dispatch();
							} else {
								Log
										.i(TAG,
												"Resetting Button from REMOVE FROM FAVORITE to FAVORITE");
								// change the color and content of the button
								mFavorites.getBackground().setColorFilter(
										AppConfig.BUTTON_COLOR_RED,
										PorterDuff.Mode.MULTIPLY);
								mFavorites
										.setText(R.string.remove_from_favorites_label);
								// reset
								mAlreadyFavorite = true;
								new AsyncPostFavorites()
										.execute(addToFavoritesUrl);
								mTracker.trackEvent("CommunityBeerView",
										"AddToFavorites", "Clicked", 0);
								mTracker.dispatch();
							}

						} else {
							/** Handle User Not Logged In **/
							Intent intent = new Intent(mMainActivity
									.getApplication(), LoginIntercept.class);
							intent.putExtra("FACEBOOK_PERMISSIONS",
									AppConfig.FACEBOOK_PERMISSIONS);
							startActivityForResult(intent,
									LOGIN_INTERCEPT_REQUEST_CODE_FOR_ADD_TO_FAVORITES);

						}
					}
				});

			}
		};
		mContentManager.fetchContentOnThread(Util.getFavoritesUrl(mUser
				.getUserId()), mFavoritesHandler);

	}

	private void setupCommunityIcon() {
		mCommunityIcon.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				Log.i(TAG, "back to community options menu");

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

	// private void setupYes() {
	// mYes.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View arg0) {
	// // If user id does not exist
	// if (mUser.isLoggedIn()) {
	// String _url = Util.getSetReviewHelpfulUrl(
	// mCommunityBeer.userId, mCommunityBeer.beerId, mUser
	// .getUserId(), "Y");
	// new AsyncPostReviewHelpfulRating().execute(_url);
	// mTracker.trackEvent("CommunityBeerView", "ReviewHelpful",
	// "Clicked", 0);
	// mTracker.dispatch();
	// /** Remove content from cache **/
	// mContentManager.removeContent(Util
	// .getReviewHelpfulCountUrl(mCommunityBeer.beerId));
	// /** Fetch updated Review Helpful Count **/
	// mContentManager.fetchContentOnThread(Util
	// .getReviewHelpfulCountUrl(mCommunityBeer.beerId),
	// mReviewHelpfulCountHandler);
	// mRatingThankYouMessage.setVisibility(View.VISIBLE);
	// mReviewHelpfulMessage.setVisibility(View.VISIBLE);
	// } else {
	// Intent intent = new Intent(mMainActivity.getApplication(),
	// LoginIntercept.class);
	// intent.putExtra("FACEBOOK_PERMISSIONS",
	// AppConfig.FACEBOOK_PERMISSIONS);
	// startActivityForResult(intent,
	// LOGIN_INTERCEPT_REQUEST_CODE_FOR_REVIEW_HELPFUL_Y);
	//
	// }
	// }
	// });
	//
	// }
	//
	// private void setupNo() {
	// mNo.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View arg0) {
	// // If user id does not exist
	// if (mUser.isLoggedIn()) {
	// String _url = Util.getSetReviewHelpfulUrl(
	// mCommunityBeer.userId, mCommunityBeer.beerId, mUser
	// .getUserId(), "N");
	// new AsyncPostReviewHelpfulRating().execute(_url);
	// mTracker.trackEvent("CommunityBeerView",
	// "ReviewNotHelpful", "Clicked", 0);
	// mTracker.dispatch();
	// mRatingThankYouMessage.setVisibility(View.VISIBLE);
	// mReviewHelpfulMessage.setVisibility(View.VISIBLE);
	// } else {
	// Intent intent = new Intent(mMainActivity.getApplication(),
	// LoginIntercept.class);
	// intent.putExtra("FACEBOOK_PERMISSIONS",
	// AppConfig.FACEBOOK_PERMISSIONS);
	// startActivityForResult(intent,
	// LOGIN_INTERCEPT_REQUEST_CODE_FOR_REVIEW_HELPFUL_N);
	//
	// }
	// }
	// });
	// }

	/**
	 * 
	 */
	private void setupFollowReviewer() {
		mFollowReviewerHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				String jsonStr = (String) message.obj;
				Log.i(TAG, jsonStr);
				if ((jsonStr != null) && (jsonStr.startsWith("{"))) {
					try {
						JSONObject mFollowJson = new JSONObject(jsonStr);
						JSONArray followingList = mFollowJson
								.getJSONArray("followingList");
						Log.i(TAG, "Following List Size="
								+ followingList.length());
						String _str;
						for (int i = 0; i < followingList.length(); i++) {
							_str = followingList.getString(i);
							Log.i(TAG, "Following " + _str);
							if ((_str != null)
									&& (_str
											.equalsIgnoreCase(mCommunityBeer.userId))) {
								mAlreadyFollowingReviewer = true;
								Log.i(TAG, "Already Following Receiver!");
							}
						}

					} catch (Throwable e) {
						Log.e(TAG, "error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
					}
				}
				if (mAlreadyFollowingReviewer) {
					mFollowReviewer.getBackground().setColorFilter(
							AppConfig.BUTTON_COLOR_RED,
							PorterDuff.Mode.MULTIPLY);
					mFollowReviewer.setText("Unfollow "
							+ mCommunityBeer.userName);
				} else {
					mFollowReviewer.getBackground().setColorFilter(
							AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
					mFollowReviewer
							.setText("Follow " + mCommunityBeer.userName);
				}
				mFollowReviewer.setOnClickListener(new OnClickListener() {
					String unFollowUrl = Util.getSetUnfollowUrl(mUser
							.getUserId(), mUser.getUserName(), mUser
							.getUserLink(), mCommunityBeer.userId,
							mCommunityBeer.userName, mCommunityBeer.userLink);
					String followUrl = Util.getSetFollowUrl(mUser.getUserId(),
							mUser.getUserName(), mUser.getUserLink(),
							mCommunityBeer.userId, mCommunityBeer.userName,
							mCommunityBeer.userLink);

					@Override
					public void onClick(View arg0) {
						Log.i(TAG, "Follow: " + mCommunityBeer.userName);

						if (mUser.isLoggedIn()) {

							if (mAlreadyFollowingReviewer) {
								Log
										.i(TAG,
												"Resetting Button from UNFOLLOW to FOLLOW");
								// change the color and content of the button
								mFollowReviewer.getBackground().setColorFilter(
										AppConfig.BUTTON_COLOR,
										PorterDuff.Mode.MULTIPLY);
								mFollowReviewer.setText("Follow "
										+ mCommunityBeer.userName);
								// reset
								mAlreadyFollowingReviewer = false;
								new AsyncPostFollow().execute(unFollowUrl);
								mTracker.trackEvent("CommunityBeerView",
										"UnFollow", "Clicked", 0);
								mTracker.dispatch();
							} else {
								Log
										.i(TAG,
												"Resetting Button from FOLLOW to UNFOLLOW");
								// change the color and content of the button
								mFollowReviewer.getBackground().setColorFilter(
										AppConfig.BUTTON_COLOR_RED,
										PorterDuff.Mode.MULTIPLY);
								mFollowReviewer.setText("Unfollow "
										+ mCommunityBeer.userName);
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
		mContentManager.fetchContentOnThread(Util.getFollowUrl(mUser
				.getUserId()), mFollowReviewerHandler);

	}

	private void setupFollowCount() {
		mFollowCountHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				String jsonStr = (String) message.obj;
				Log.i(TAG, jsonStr);
				if ((jsonStr != null) && (jsonStr.startsWith("{"))) {
					JSONObject json;
					try {
						json = new JSONObject(jsonStr);
						String followers = json.getString("followers");
						String following = json.getString("following");

						String _reviewCount = "Followers " + followers
								+ " Following " + following;
						Log.d(TAG, _reviewCount);
						mFollowCount.setText(_reviewCount);
					} catch (JSONException e) {
						Log.e(TAG, "error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
					}
				}
			}
		};
		mContentManager.fetchContentOnThread(Util
				.getFollowCountUrl(mCommunityBeer.userId), mFollowCountHandler);
	}

	private void setupReviewCount() {
		mReviewCountHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				String jsonStr = (String) message.obj;
				Log.i(TAG, jsonStr);
				if ((jsonStr != null) && (jsonStr.startsWith("{"))) {
					JSONObject json;
					try {
						json = new JSONObject(jsonStr);
						String count = json.getString("count");
						String _reviewCount = count + " reviews";
						Log.d(TAG, "Review Count Text:" + _reviewCount);
						mReviewedByReviewCount.setText(_reviewCount);

					} catch (JSONException e) {
						Log.e(TAG, "error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
					}
				}
			}
		};
		mContentManager.fetchContentOnThread(Util
				.getReviewCountUrl(mCommunityBeer.userId), mReviewCountHandler);

	}

	private void setupReviewHelpfulCount() {
		mReviewHelpfulCountHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				String jsonStr = (String) message.obj;
				Log.i(TAG, jsonStr);
				if ((jsonStr != null) && (jsonStr.startsWith("{"))) {
					JSONObject json;
					try {
						json = new JSONObject(jsonStr);
						int yes = json.getInt("yes");
						int no = json.getInt("no");
						String _reviewHelpfulMessage = yes + " out of "
								+ (yes + no) + " found this review helpful:";
						Log.d(TAG, _reviewHelpfulMessage);
						// mReviewHelpfulMessage.setText(_reviewHelpfulMessage);
					} catch (JSONException e) {
						Log.e(TAG, "error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
					}
				} else {
					// mReviewHelpfulMessage.setVisibility(View.GONE);
				}
			}
		};
		mContentManager.fetchContentOnThread(Util
				.getReviewHelpfulCountUrl(mCommunityBeer.beerId),
				mReviewHelpfulCountHandler);

	}

	public void handleUserNotLoggedInFacebook() {
		Log.i(TAG, "handleUserNotLoggedInFacebook");
		Intent intent = new Intent(mMainActivity, LoginIntercept.class);
		intent.putExtra("FACEBOOK_PERMISSIONS", AppConfig.FACEBOOK_PERMISSIONS);
		intent.putExtra("FACEBOOK_ONLY", "Y");
		mMainActivity.startActivityForResult(intent,
				LOGIN_INTERCEPT_REQUEST_CODE_FOR_FACEBOOK_LIKE_BUTTON);

	}

	/************************************************************************************/
	private class AsyncPostReviewHelpfulRating extends
			AsyncTask<Object, Void, Object> {

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			Log.i(TAG, "doInBackground starting");
			String url = (String) args[0];
			try {
				Util.getResult(url);
				/** Remove content from cache **/
				mContentManager.removeContent(Util
						.getReviewHelpfulCountUrl(mCommunityBeer.beerId));
				/** Fetch updated Review Helpful Count **/
				mContentManager.fetchContentOnThread(Util
						.getReviewHelpfulCountUrl(mCommunityBeer.beerId),
						mReviewHelpfulCountHandler);
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView",
						"PostReviewHelpfulRatingError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			Log.i(TAG, "doInBackground finished");
			return null;
		}

		protected void onPostExecute(Object result) {
			Log.i(TAG, "onPostExecute starting");

			Log.i(TAG, "onPostExecute finished");
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
			Log.i(TAG, "doInBackground starting");
			String url = (String) args[0];
			try {
				Util.getResult(url);
				/** Remove follow count from cache **/
				mContentManager.removeContent(Util
						.getFollowCountUrl(mCommunityBeer.userId));
				/** Remove follow from cache **/
				mContentManager.removeContent(Util.getFollowUrl(mUser
						.getUserId()));

				/** Fetch updated Follow Count **/
				mContentManager.fetchContentOnThread(Util
						.getFollowCountUrl(mCommunityBeer.userId),
						mFollowCountHandler);
				/** Fetch updated Follow for the logged in user **/
				mContentManager.fetchContentOnThread(Util.getFollowUrl(mUser
						.getUserId()), mFollowReviewerHandler);
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView",
						"AsyncPostFollowError", ((e.getMessage() != null) ? e
								.getMessage().replace(" ", "_") : "").replace(
								" ", "_"), 0);
				mTracker.dispatch();
			}

			Log.i(TAG, "doInBackground finished");
			return null;
		}

		protected void onPostExecute(Object result) {
			Log.i(TAG, "onPostExecute starting");
			Log.i(TAG, "onPostExecute finished");
		}

	}

	/************************************************************************************/
	private class AsyncTranslate extends AsyncTask<Object, Void, Object> {
		String mTranslatedText = (String) mNotes.getText();

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			Log.i(TAG, "doInBackground starting");
			String language = "";
			try {// 
				/** Detect the language **/
				{
					URL url = new URL(
							"http://ajax.googleapis.com/ajax/services/language/detect?v=1.0&"
									+ "q="
									+ URLEncoder.encode(((String) mNotes
											.getText()), "UTF-8") + "&key="
									+ AppConfig.GOOGLE_TRANSLATE_API_KEY
									+ "&hl="
									+ Locale.getDefault().getLanguage()
									+ "&userip=" + Util.getLocalIpAddress());
					Log.i(TAG, url.toString());
					URLConnection connection = url.openConnection();
					connection.addRequestProperty("Referer",
							AppConfig.GOOGLE_TRANSLATE_REFERER);

					String line;
					StringBuilder builder = new StringBuilder();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}

					JSONObject json = new JSONObject(builder.toString());
					Log.i(TAG, json.toString());
					JSONObject responseData = json
							.getJSONObject("responseData");
					language = responseData.getString("language");

				}
				/** Translate **/
				{
					URL url = new URL(
							"http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&"
									+ "q="
									+ URLEncoder.encode(((String) mNotes
											.getText()), "UTF-8") + "&key="
									+ AppConfig.GOOGLE_TRANSLATE_API_KEY
									+ "&hl="
									+ Locale.getDefault().getLanguage()
									+ "&userip=" + Util.getLocalIpAddress()
									+ "&langpair=" + language + "%7C"
									+ Locale.getDefault().getLanguage());
					Log.i(TAG, url.toString());
					URLConnection connection = url.openConnection();
					connection.addRequestProperty("Referer",
							AppConfig.GOOGLE_TRANSLATE_REFERER);

					String line;
					StringBuilder builder = new StringBuilder();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}

					JSONObject json = new JSONObject(builder.toString());
					Log.i(TAG, json.toString());
					JSONObject responseData = json
							.getJSONObject("responseData");
					mTranslatedText = responseData.getString("translatedText");

				}

			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView", "TranslateError", ((e
						.getMessage() != null) ? e.getMessage().replace(" ",
						"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			Log.i(TAG, "doInBackground finished");
			return null;
		}

		protected void onPostExecute(Object result) {
			Log.i(TAG, "onPostExecute starting");

			mNotes.setText(mTranslatedText);
			mTranslateNotes.setText(R.string.translated_label);
			Log.i(TAG, "onPostExecute finished");
		}

	}

	/************************************************************************************/
	private class AsyncTranslateCharacteristics extends
			AsyncTask<Object, Void, Object> {
		String _mTranslatedColor = (String) mColorTE.getText();
		String _mTranslatedClarity = (String) mClarityTE.getText();
		String _mTranslatedFoam = (String) mFoamTE.getText();
		String _mTranslatedAroma = (String) mAromaTE.getText();
		String _mTranslatedBody = (String) mBodyTE.getText();
		String _mTranslatedMouthfeel = (String) mMouthfeelTE.getText();
		String _mTranslatedAftertaste = (String) mAftertasteTE.getText();

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			Log.i(TAG, "doInBackground starting");
			String language = "";
			try {// 
				/** Detect the language **/
				{
					String languageHint = mColorTE.getText() + " "
							+ mClarityTE.getText() + " " + mFoamTE.getText()
							+ " " + mAromaTE.getText() + " "
							+ mBodyTE.getText() + " " + mMouthfeelTE.getText()
							+ " " + mAftertasteTE.getText();

					URL url = new URL(
							"http://ajax.googleapis.com/ajax/services/language/detect?v=1.0&"
									+ "q="
									+ URLEncoder.encode(
											((String) languageHint), "UTF-8")
									+ "&key="
									+ AppConfig.GOOGLE_TRANSLATE_API_KEY
									+ "&hl="
									+ Locale.getDefault().getLanguage()
									+ "&userip=" + Util.getLocalIpAddress());
					Log.i(TAG, url.toString());
					URLConnection connection = url.openConnection();
					connection.addRequestProperty("Referer",
							AppConfig.GOOGLE_TRANSLATE_REFERER);

					String line;
					StringBuilder builder = new StringBuilder();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}

					JSONObject json = new JSONObject(builder.toString());
					Log.i(TAG, json.toString());
					JSONObject responseData = json
							.getJSONObject("responseData");
					language = responseData.getString("language");

				}
				/** Translate **/
				{
					String baseUrl = "http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&"
							+ "&key="
							+ AppConfig.GOOGLE_TRANSLATE_API_KEY
							+ "&hl="
							+ Locale.getDefault().getLanguage()
							+ "&userip="
							+ Util.getLocalIpAddress()
							+ "&langpair="
							+ language
							+ "%7C"
							+ Locale.getDefault().getLanguage();
					if (!mColorTE.getText().equals("")) {
						translateColor(baseUrl);
					}
					if (!mClarityTE.getText().equals("")) {
						translateClarity(baseUrl);
					}
					if (!mFoamTE.getText().equals("")) {
						translateFoam(baseUrl);
					}
					if (!mAromaTE.getText().equals("")) {
						translateAroma(baseUrl);
					}
					if (!mBodyTE.getText().equals("")) {
						translateBody(baseUrl);
					}
					if (!mMouthfeelTE.getText().equals("")) {
						translateMouthfeel(baseUrl);
					}
					if (!mAftertasteTE.getText().equals("")) {
						translateAftertaste(baseUrl);
					}

				}

			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView",
						"TranslateCharacteristicsError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			Log.i(TAG, "doInBackground finished");
			return null;
		}

		private void translateColor(String baseUrl) {
			try {
				URL url = new URL(baseUrl
						+ "&q="
						+ URLEncoder.encode(((String) mColorTE.getText()),
								"UTF-8"));
				Log.i(TAG, url.toString());
				URLConnection connection = url.openConnection();
				connection.addRequestProperty("Referer",
						AppConfig.GOOGLE_TRANSLATE_REFERER);
				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				JSONObject json = new JSONObject(builder.toString());
				Log.i(TAG, json.toString());
				JSONObject responseData = json.getJSONObject("responseData");
				_mTranslatedColor = responseData.getString("translatedText");
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView", "TranslateColorError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

		}

		private void translateClarity(String baseUrl) {
			try {
				URL url = new URL(baseUrl
						+ "&q="
						+ URLEncoder.encode(((String) mClarityTE.getText()),
								"UTF-8"));
				Log.i(TAG, url.toString());
				URLConnection connection = url.openConnection();
				connection.addRequestProperty("Referer",
						AppConfig.GOOGLE_TRANSLATE_REFERER);
				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				JSONObject json = new JSONObject(builder.toString());
				Log.i(TAG, json.toString());
				JSONObject responseData = json.getJSONObject("responseData");
				_mTranslatedClarity = responseData.getString("translatedText");
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView",
						"TranslateSweetnessError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

		}

		private void translateFoam(String baseUrl) {
			try {
				URL url = new URL(baseUrl
						+ "&q="
						+ URLEncoder.encode(((String) mFoamTE.getText()),
								"UTF-8"));
				Log.i(TAG, url.toString());
				URLConnection connection = url.openConnection();
				connection.addRequestProperty("Referer",
						AppConfig.GOOGLE_TRANSLATE_REFERER);
				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				JSONObject json = new JSONObject(builder.toString());
				Log.i(TAG, json.toString());
				JSONObject responseData = json.getJSONObject("responseData");
				_mTranslatedFoam = responseData.getString("translatedText");
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView",
						"TranslateTanninError", ((e.getMessage() != null) ? e
								.getMessage().replace(" ", "_") : "").replace(
								" ", "_"), 0);
				mTracker.dispatch();
			}

		}

		private void translateAroma(String baseUrl) {
			try {
				URL url = new URL(baseUrl
						+ "&q="
						+ URLEncoder.encode(((String) mAromaTE.getText()),
								"UTF-8"));
				Log.i(TAG, url.toString());
				URLConnection connection = url.openConnection();
				connection.addRequestProperty("Referer",
						AppConfig.GOOGLE_TRANSLATE_REFERER);
				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				JSONObject json = new JSONObject(builder.toString());
				Log.i(TAG, json.toString());
				JSONObject responseData = json.getJSONObject("responseData");
				_mTranslatedAroma = responseData.getString("translatedText");
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView",
						"TranslateBouquetError", ((e.getMessage() != null) ? e
								.getMessage().replace(" ", "_") : "").replace(
								" ", "_"), 0);
				mTracker.dispatch();
			}

		}

		private void translateBody(String baseUrl) {
			try {
				URL url = new URL(baseUrl
						+ "&q="
						+ URLEncoder.encode(((String) mBodyTE.getText()),
								"UTF-8"));
				Log.i(TAG, url.toString());
				URLConnection connection = url.openConnection();
				connection.addRequestProperty("Referer",
						AppConfig.GOOGLE_TRANSLATE_REFERER);
				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				JSONObject json = new JSONObject(builder.toString());
				Log.i(TAG, json.toString());
				JSONObject responseData = json.getJSONObject("responseData");
				_mTranslatedBody = responseData.getString("translatedText");
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView", "TranslateBodyError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

		}

		private void translateMouthfeel(String baseUrl) {
			try {
				URL url = new URL(baseUrl
						+ "&q="
						+ URLEncoder.encode(((String) mMouthfeelTE.getText()),
								"UTF-8"));
				Log.i(TAG, url.toString());
				URLConnection connection = url.openConnection();
				connection.addRequestProperty("Referer",
						AppConfig.GOOGLE_TRANSLATE_REFERER);
				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				JSONObject json = new JSONObject(builder.toString());
				Log.i(TAG, json.toString());
				JSONObject responseData = json.getJSONObject("responseData");
				_mTranslatedMouthfeel = responseData
						.getString("translatedText");
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView",
						"TranslateMouthfeelError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

		}

		private void translateAftertaste(String baseUrl) {
			try {
				URL url = new URL(baseUrl
						+ "&q="
						+ URLEncoder.encode(((String) mAftertasteTE.getText()),
								"UTF-8"));
				Log.i(TAG, url.toString());
				URLConnection connection = url.openConnection();
				connection.addRequestProperty("Referer",
						AppConfig.GOOGLE_TRANSLATE_REFERER);
				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				JSONObject json = new JSONObject(builder.toString());
				Log.i(TAG, json.toString());
				JSONObject responseData = json.getJSONObject("responseData");
				_mTranslatedAftertaste = responseData
						.getString("translatedText");
			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView",
						"TranslateFinishError", ((e.getMessage() != null) ? e
								.getMessage().replace(" ", "_") : "").replace(
								" ", "_"), 0);
				mTracker.dispatch();
			}

		}

		protected void onPostExecute(Object result) {
			Log.i(TAG, "onPostExecute starting");

			mColorTE.setText(_mTranslatedColor);
			mClarityTE.setText(_mTranslatedClarity);
			mFoamTE.setText(_mTranslatedFoam);
			mAromaTE.setText(_mTranslatedAroma);
			mBodyTE.setText(_mTranslatedBody);
			mMouthfeelTE.setText(_mTranslatedMouthfeel);
			mAftertasteTE.setText(_mTranslatedAftertaste);

			mTranslateCharacteristics.setText(R.string.translated_label);
			Log.i(TAG, "onPostExecute finished");
		}

	}

	/************************************************************************************/
	private class AsyncPostFavorites extends AsyncTask<Object, Void, Object> {

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			Log.i(TAG, "doInBackground starting");
			String url = (String) args[0];
			try {
				Util.getResult(url);
				String _favoritesUrl = Util.getFavoritesUrl(mUser.getUserId());

				/** Remove favorites from cache **/
				mContentManager.removeContent(_favoritesUrl);

				/** Fetch updated favorites **/
				mContentManager.fetchContentOnThread(_favoritesUrl);

			} catch (Throwable e) {
				Log.e(TAG, "error: "
						+ ((e.getMessage() != null) ? e.getMessage().replace(
								" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView",
						"AsyncPostFavoritesError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			Log.i(TAG, "doInBackground finished");
			return null;
		}

		protected void onPostExecute(Object result) {
			Log.i(TAG, "onPostExecute starting");
			Log.i(TAG, "onPostExecute finished");
		}

	}

	// ----------------------------------------------------------------------//
	private class FacebookLikeButtonWebViewClient extends WebViewClient {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit
		 * .WebView, java.lang.String)
		 */
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPageStarted::" + url);
			}
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onPageFinished::" + url);
			}
			super.onPageFinished(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onReceivedError::" + failingUrl + "::" + errorCode
						+ "::" + description);
			}
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, "onLoadResource::" + url);
			}
			super.onLoadResource(view, url);
		}
	}

}
