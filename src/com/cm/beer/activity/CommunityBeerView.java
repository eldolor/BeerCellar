package com.cm.beer.activity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.cm.beer.config.AppConfig;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.facebook.SessionStore;
import com.cm.beer.transfer.CommunityBeer;
import com.cm.beer.util.ContentManager;
import com.cm.beer.util.DrawableManager;
import com.cm.beer.util.FacebookLikeButtonWebView;
import com.cm.beer.util.HttpParam;
import com.cm.beer.util.Logger;
import com.cm.beer.util.User;
import com.cm.beer.util.Util;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class CommunityBeerView extends Activity {
	protected static final int VIEW_IMAGE_ACTIVITY_REQUEST_CODE = 0;
	static final int ACTIVITY_SHARE_ON_FACEBOOK = 2;

	String _TAG;

	ProgressDialog mDialog;
	ProgressDialog mSplashDialog;
	int mActiveDialog;

	SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM d, yyyy");

	GoogleAnalyticsTracker mTracker;
	CommunityBeerView mMainActivity;

	String mUrl;
	CommunityBeer mCommunityBeer;

	FacebookLikeButtonWebView mFacebookLikeWebView;
	TextView mReviewedByLabel;
	TextView mReviewedBy;
	TextView mReviewedByReviewCount;

	ImageView mShareOnFacebook;
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
	// Button mTranslateNotes;
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
	Button mTranslate;

	Button mShowLocation;

	Facebook mFacebook;
	AsyncFacebookRunner mAsyncRunner;

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
	String mShortenedFacebookLikeHrefUrl;

	// comments
	TableLayout mCommentsLayout;
	Button mAddComments;
	Handler mCommentsHandler;
	List<TextView> mCommentTextViews = new ArrayList<TextView>();

	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_REVIEW_HELPFUL_Y = 0;
	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_REVIEW_HELPFUL_N = 1;
	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_FOLLOW = 2;
	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_ADD_TO_FAVORITES = 3;
	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_FOR_FACEBOOK_LIKE_BUTTON = 4;
	protected static final int LOGIN_INTERCEPT_REQUEST_CODE_TO_POST_A_COMMENT = 5;
	protected static final int REQUEST_CODE_TO_POST_A_COMMENT = 6;

	static final int MENU_GROUP = 0;
	static final int SEND_TEST_DAILY_CAMPAIGN = 1;
	static final int SEND_DAILY_CAMPAIGN = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setup _TAG
		_TAG = this.getString(R.string.app_name) + "::"
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
		mUrl = extras != null ? extras.getString("URL") : null;
		mCommunityBeer = (CommunityBeer) extras
				.getSerializable("COMMUNITY_BEER");

		mSplashDialog = ProgressDialog.show(this,
				getString(R.string.community_progress_dialog_title),
				getString(R.string.community_progress_searching_message), true,
				true);
		setContentView(R.layout.community_beer_view);

		mUser = new User(this);
		mFacebook = new Facebook(AppConfig.FACEBOOK_APP_ID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
		SessionStore.restore(mFacebook, this);

		display();
		populateFields();
		mSplashDialog.cancel();
		String _selection = mCommunityBeer.beer.replace(" ", "_") + ","
				+ mCommunityBeer.beerId;
		mTracker.trackEvent("CommunityBeerView", "View", _selection, 0);
		mTracker.dispatch();
	}

	private void setupGoogleAdSense(Set<String> keywords) {
		HashSet<String> keywordsSet = new HashSet<String>(
				Arrays.asList(AppConfig.KEYWORDS));
		if (keywords != null) {
			keywordsSet.addAll(keywords);
		}
		Util.setGoogleAdSense(this, keywordsSet);

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
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		if (Logger.isLogEnabled())  Logger.log("onResume");
		if ((mUser != null) && (mUser.isLoggedIn())
				&& (mFollowReviewerHandler != null)) {
			mContentManager.fetchContentOnThread(
					Util.getFollowUrl(mUser.getUserId()),
					mFollowReviewerHandler);
		}
		mBrewery.setText(mCommunityBeer.brewery);
		super.onResume();
		/**
		 * With the deprecation of offline_access, you need to extend your
		 * access_token every time a user opens your app. To do this, call
		 * facebook method extendAccessTokenIfNeeded in your Activity's
		 * onResume() function:
		 */
		mFacebook.extendAccessTokenIfNeeded(this, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (Logger.isLogEnabled())  Logger.log("onCreateOptionsMenu: userid "
				+ ((mUser.getUserId() != null) ? mUser.getUserId() : "NULL"));
		super.onCreateOptionsMenu(menu);
		int menuPosition = 0;
		if ((mUser != null && mUser.getUserId() != null)
				&& (mUser.getUserId()
						.equalsIgnoreCase(AppConfig.ADMIN_USER_EMAIL_ADDRESS))) {
			menu.add(MENU_GROUP, SEND_TEST_DAILY_CAMPAIGN, menuPosition++,
					R.string.send_test_daily_campaign);
			menu.add(MENU_GROUP, SEND_DAILY_CAMPAIGN, menuPosition++,
					R.string.send_daily_campaign);
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
		case SEND_TEST_DAILY_CAMPAIGN:
			sendTestDailyCampaign();
			return true;
		case SEND_DAILY_CAMPAIGN:
			sendDailyCampaign();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private void sendTestDailyCampaign() {
		try {
			String url = Util
					.getSendTestDailyCampaignUrl(mCommunityBeer.beerId);
			if (Logger.isLogEnabled())  Logger.log("sendTestDailyCampaign():" + url);
			String response[] = Util.getResult(url);
			if (Logger.isLogEnabled())  Logger.log(response[0]);
		} catch (Throwable e) {
			Log.e(_TAG,
					"error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);
		}

	}

	private void sendDailyCampaign() {
		try {
			String url = Util.getSendDailyCampaignUrl(mCommunityBeer.beerId);
			if (Logger.isLogEnabled())  Logger.log("getSendDailyCampaignUrl():" + url);
			String response[] = Util.getResult(url);
			if (Logger.isLogEnabled())  Logger.log(response[0]);
		} catch (Throwable e) {
			Log.e(_TAG,
					"error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_INTERCEPT_REQUEST_CODE_FOR_FOLLOW) {
			if (resultCode == RESULT_OK) {
				String _setFollowUrl = Util.getSetFollowUrl(mUser.getUserId(),
						mUser.getUserName(), mUser.getUserLink(),
						mCommunityBeer.userId, mCommunityBeer.userName,
						mCommunityBeer.userLink);
				new AsyncPostFollow().execute(_setFollowUrl);
				mTracker.trackEvent("CommunityBeerView", "Follow", "Clicked", 0);
				mTracker.dispatch();
				mFollowReviewer.getBackground().setColorFilter(
						AppConfig.BUTTON_COLOR_RED, PorterDuff.Mode.MULTIPLY);
				mFollowReviewer.setText("Unfollow " + mCommunityBeer.userName);
				// Get follow
				String _followUrl = Util.getFollowUrl(mUser.getUserId());
				mContentManager.fetchContentOnThread(_followUrl);

				// new AsyncSetupFacebookLikeButtonTask().execute();

				if (Logger.isLogEnabled())  Logger.log("onActivityResult:");
			}
		} else if (requestCode == LOGIN_INTERCEPT_REQUEST_CODE_FOR_ADD_TO_FAVORITES) {
			if (resultCode == RESULT_OK) {
				String _addToFavoritesUrl = Util.getAddToFavoritesUrl(
						mUser.getUserId(), mUser.getUserName(),
						mUser.getUserLink(), mCommunityBeer.beerId);
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

				// new AsyncSetupFacebookLikeButtonTask().execute();

				if (Logger.isLogEnabled())  Logger.log("onActivityResult:");
			}
			// } else if (requestCode ==
			// LOGIN_INTERCEPT_REQUEST_CODE_FOR_FACEBOOK_LIKE_BUTTON)
			// {
			// if (resultCode == RESULT_OK)
			// {
			// new AsyncSetupFacebookLikeButtonTask().execute();
			// Log.i(_TAG,
			// "onActivityResult:LOGIN_INTERCEPT_REQUEST_CODE_FOR_FACEBOOK_LIKE_BUTTON");
			// }
		} else if (requestCode == REQUEST_CODE_TO_POST_A_COMMENT) {
			if (resultCode == RESULT_OK) {
				// remove cached contents
				mContentManager.removeContent(Util
						.getCommentsUrl(mCommunityBeer.beerId));
				setupCommentsTable();
				if (Logger.isLogEnabled())  Logger.log("onActivityResult:REQUEST_CODE_TO_POST_A_COMMENT");
			}
		} else if (requestCode == LOGIN_INTERCEPT_REQUEST_CODE_TO_POST_A_COMMENT) {
			if (resultCode == RESULT_OK) {
				Intent intent = new Intent(mMainActivity.getApplication(),
						PostComment.class);
				intent.putExtra("BEER_ID", mCommunityBeer.beerId);
				startActivityForResult(intent, REQUEST_CODE_TO_POST_A_COMMENT);
				Log.i(_TAG,
						"onActivityResult:LOGIN_INTERCEPT_REQUEST_CODE_TO_POST_A_COMMENT");
			}
		} else if (requestCode == AppConfig.FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE) {
			/**
			 * IMPORTANT: This method must be invoked at the top of the calling
			 * activity's onActivityResult() function or Facebook authentication
			 * will not function properly!
			 */
			Log.d(_TAG, "authorizeCallback");
			mFacebook.authorizeCallback(requestCode, resultCode, data);
			new AsyncSetupFacebookLikeButtonTask().execute();
		} else if (requestCode == AppConfig.FACEBOOK_LOGIN_INTERCEPT_REQUEST_CODE_FOR_WALL_POST) {
			if (resultCode == RESULT_OK) {
				Bundle extras = (data != null) ? data.getExtras() : null;
				// pass the rowId along to ShareOnFacebook
				long rowId = (extras != null) ? extras
						.getLong(NotesDbAdapter.KEY_ROWID) : 0L;
				if (Logger.isLogEnabled())  Logger.log("onActivityResult:Row Id=" + rowId);
				Intent newIntent = new Intent(this, ShareOnFacebook.class);
				newIntent.putExtra("SHARE", "COMMUNITY_BEER");
				newIntent.putExtra("COMMUNITY_BEER", mCommunityBeer);
				startActivityForResult(newIntent, ACTIVITY_SHARE_ON_FACEBOOK);
			}
		} else {
			// fillData();
			if (resultCode == AppConfig.FACEBOOK_WALL_POST_SUCCESSFUL_RESULT_CODE) {
				Toast.makeText(CommunityBeerView.this,
						R.string.on_facebook_wall_post, Toast.LENGTH_SHORT)
						.show();
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
			if (Logger.isLogEnabled())  Logger.log("onCreateDialog");
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
			if (Logger.isLogEnabled())  Logger.log("display");
		}
		/****************************************/
		mFavorites = (Button) findViewById(R.id.favorites);
		mFavorites.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mFacebookLikeWebView = (FacebookLikeButtonWebView) findViewById(R.id.facebook_like_webview);
		mFacebookLikeWebView
				.setWebViewClient(new FacebookLikeButtonWebViewClient());
		mFacebookLikeWebView.getSettings().setJavaScriptEnabled(true);
		// initialize
		mFacebookLikeWebView.init(CommunityBeerView.this,
				AppConfig.FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE, mFacebook);
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
		mTranslate = (Button) findViewById(R.id.translate);
		mTranslate.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mCommentsLayout = (TableLayout) findViewById(R.id.comments_table);
		mAddComments = (Button) findViewById(R.id.add_comments);
		mAddComments.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mShowLocation = (Button) findViewById(R.id.show_location);
		mShowLocation.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
				PorterDuff.Mode.MULTIPLY);
		/****************************************/
		mShareOnFacebook = (ImageView) findViewById(R.id.share_on_facebook);

	}

	/**
	 * 
	 */
	private void populateFields() {
		if (Logger.isLogEnabled())  Logger.log("populateFields");
		HashSet<String> keywords = new HashSet<String>();
		try {

			new AsyncSetupFacebookLikeButtonTask().execute();

			// setup view user profile button
			setupViewUserProfile();

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
				setupReviewCount();
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
			keywords.add(mCommunityBeer.beer);
			if (Logger.isLogEnabled())  Logger.log("populateFields::setting:" + mCommunityBeer.beer);
			mAlcohol.setText(mCommunityBeer.alcohol);
			if (Logger.isLogEnabled())  Logger.log("populateFields::setting:" + mCommunityBeer.alcohol);

			if ((mCommunityBeer.price != null)
					&& (!mCommunityBeer.price.trim().equals(""))) {

				if ((mCommunityBeer.currencyCode != null)
						&& (mCommunityBeer.currencySymbol != null)) {
					Log.d(_TAG, "Currency code and symbol are available");
					String _priceText = mCommunityBeer.currencyCode + " "
							+ mCommunityBeer.currencySymbol + " "
							+ mCommunityBeer.price;
					Log.d(_TAG, "Price Text: " + _priceText);
					mPrice.setText(_priceText);
				} else {
					Log.d(_TAG, "Currency code and symbol are NOT available");
					mPrice.setText(((mCommunityBeer.currency != null) ? mCommunityBeer.currency
							: "")
							+ mCommunityBeer.price);
				}
			}

			mStyle.setText(mCommunityBeer.style);
			keywords.add(mCommunityBeer.style);

			mBrewery.setText(mCommunityBeer.brewery);
			keywords.add(mCommunityBeer.brewery);
			if (Logger.isLogEnabled())  Logger.log("populateFields::brewery: " + mCommunityBeer.brewery
					+ " brewery link: " + mCommunityBeer.breweryLink);
			if ((mCommunityBeer.breweryLink != null)
					&& (!mCommunityBeer.breweryLink.trim().equals(""))
					&& (!mCommunityBeer.breweryLink.trim().equalsIgnoreCase(
							"http://"))) {
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
						Intent intent = new Intent(mMainActivity
								.getApplication(), BeerWebView.class);
						intent.putExtra("URL", _link);
						startActivity(intent);

					}
				});
			}

			mState.setText(mCommunityBeer.state);
			keywords.add(mCommunityBeer.state);
			mCountry.setText(mCommunityBeer.country);
			keywords.add(mCommunityBeer.country);
			if (Logger.isLogEnabled())  Logger.log("populateFields::setting:" + mCommunityBeer.country);
			String ratingStr = mCommunityBeer.rating;
			if (ratingStr != null) {
				mRating.setRating(Float.valueOf(ratingStr));
			}
			mNotes.setText(mCommunityBeer.notes);
			try {
				setCharacteristicsTable(keywords);
			} catch (JSONException e1) {
				Log.e(_TAG, e1.getMessage(), e1);
			}

			mTranslate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mTranslate.setText(R.string.translating_label);
					mTranslate.getBackground().setColorFilter(
							AppConfig.BUTTON_COLOR_RED,
							PorterDuff.Mode.MULTIPLY);
					new AsyncTranslateAll().execute();
					mTracker.trackEvent("CommunityBeerView", "Translate",
							"Clicked", 0);
					mTracker.dispatch();
				}
			});

			/*********************************************************/
			if ((mCommunityBeer.latitude != null)
					&& (!mCommunityBeer.latitude.equals("0.0"))) {
				if ((mCommunityBeer.longitude != null)
						&& (!mCommunityBeer.longitude.equals("0.0"))) {
					mShowLocation.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {

							String latitude = mCommunityBeer.latitude;
							String longitude = mCommunityBeer.longitude;

							String _selection = latitude + "," + longitude;

							mTracker.trackEvent("CommunityWineView",
									"ShowLocation", _selection, 0);
							mTracker.dispatch();
							Intent i = new Intent(Intent.ACTION_VIEW, Uri
									.parse("http://maps.google.com/maps?"
											+ "z="
											+ AppConfig.GOOGLE_MAPS_ZOOM_LEVEL
											+ "&t=m" + "&q=loc:" + latitude
											+ "," + longitude));

							startActivity(i);
						}
					});
					mShowLocation.setVisibility(View.VISIBLE);
				}
			}
			/*********************************************************/
			mShareOnFacebook.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Perform action on clicks
					if (Logger.isLogEnabled())  Logger.log("share on facebook");
					// showDialog(AppConfig.DIALOG_LOADING_ID);
					Intent intent = new Intent(mMainActivity.getApplication(),
							LoginIntercept.class);
					intent.putExtra("FACEBOOK_PERMISSIONS",
							AppConfig.FACEBOOK_PERMISSIONS);
					intent.putExtra(NotesDbAdapter.KEY_ROWID,
							mCommunityBeer.beerId);
					if (Logger.isLogEnabled())  Logger.log("shareOnFacebook:Row Id="
							+ mCommunityBeer.beerId);
					startActivityForResult(
							intent,
							AppConfig.FACEBOOK_LOGIN_INTERCEPT_REQUEST_CODE_FOR_WALL_POST);
				}
			});
			/*********************************************************/
			setupAddToFavorites();
			// comments
			setupCommentsTable();

			setupCommunityIcon();
			setupGoogleAdSense(keywords);

		} catch (Throwable e) {
			Log.e(_TAG,
					"error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);
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

	private void setCharacteristicsTable(Set<String> keywords)
			throws JSONException {
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
					keywords.add(_characteristics.getString("color"));
				}
			}
			if (_characteristics.has("clarity")) {
				if (!_characteristics.getString("clarity").equals("")) {
					((TextView) findViewById(R.id.clarity_label))
							.setVisibility(View.VISIBLE);
					mClarityTE.setVisibility(View.VISIBLE);
					mClarityTE.setText(_characteristics.getString("clarity"));
					keywords.add(_characteristics.getString("clarity"));
				}
			}
			if (_characteristics.has("foam")) {
				if (!_characteristics.getString("foam").equals("")) {
					((TextView) findViewById(R.id.foam_label))
							.setVisibility(View.VISIBLE);
					mFoamTE.setVisibility(View.VISIBLE);
					mFoamTE.setText(_characteristics.getString("foam"));
					keywords.add(_characteristics.getString("foam"));
				}
			}
			if (_characteristics.has("aroma")) {
				if (_characteristics.getJSONArray("aroma").length() > 0) {
					JSONArray _aroma = _characteristics.getJSONArray("aroma");
					StringBuilder _aromaText = new StringBuilder();
					for (int i = 0; i < _aroma.length(); i++) {
						_aromaText.append(", ");
						_aromaText.append(_aroma.getString(i));
						keywords.add(_aroma.getString(i));
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
					keywords.add(_characteristics.getString("mouthfeel"));
				}
			}
			if (_characteristics.has("body")) {
				if (!_characteristics.getString("body").equals("")) {
					((TextView) findViewById(R.id.body_label))
							.setVisibility(View.VISIBLE);
					mBodyTE.setVisibility(View.VISIBLE);
					mBodyTE.setText(_characteristics.getString("body"));
					keywords.add(_characteristics.getString("body"));
				}
			}
			if (_characteristics.has("aftertaste")) {
				if (!_characteristics.getString("aftertaste").equals("")) {
					((TextView) findViewById(R.id.aftertaste_label))
							.setVisibility(View.VISIBLE);
					mAftertasteTE.setVisibility(View.VISIBLE);
					mAftertasteTE.setText(_characteristics
							.getString("aftertaste"));
					keywords.add(_characteristics.getString("aftertaste"));
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
				if (Logger.isLogEnabled())  Logger.log(jsonStr);
				if ((jsonStr != null) && (jsonStr.startsWith("["))) {
					JSONArray json;
					try {
						json = new JSONArray(jsonStr);
						Log.d(_TAG, json.toString());
						for (int i = 0; i < json.length(); i++) {
							String _beerId = json.getString(i);
							if (_beerId.equals(mCommunityBeer.beerId)) {
								mAlreadyFavorite = true;
								break;
							}
						}
					} catch (Throwable e) {
						Log.e(_TAG, "error: "
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

					String addToFavoritesUrl = Util.getAddToFavoritesUrl(
							mUser.getUserId(), mUser.getUserName(),
							mUser.getUserLink(), mCommunityBeer.beerId);
					String removeFromFavoritesUrl = Util
							.getRemoveFromFavoritesUrl(mUser.getUserId(),
									mUser.getUserName(), mUser.getUserLink(),
									mCommunityBeer.beerId);

					@Override
					public void onClick(View arg0) {
						if (Logger.isLogEnabled())  Logger.log("Favorites: " + mCommunityBeer.userName);

						if (mUser.isLoggedIn()) {

							if (mAlreadyFavorite) {
								Log.i(_TAG,
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
								Log.i(_TAG,
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
		mContentManager.fetchContentOnThread(
				Util.getFavoritesUrl(mUser.getUserId()), mFavoritesHandler);

	}

	private void setupCommentsTable() {
		// reset the table
		mCommentsLayout.removeAllViews();
		mCommentTextViews.clear();
		mAddComments.setText(R.string.progress_loading_message);
		mAddComments.getBackground().setColorFilter(AppConfig.BUTTON_COLOR_RED,
				PorterDuff.Mode.MULTIPLY);
		mAddComments.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mUser.isLoggedIn()) {
					Intent intent = new Intent(mMainActivity.getApplication(),
							PostComment.class);
					intent.putExtra("BEER_ID", mCommunityBeer.beerId);
					startActivityForResult(intent,
							REQUEST_CODE_TO_POST_A_COMMENT);

				} else {
					/** Handle User Not Logged In **/
					Intent intent = new Intent(mMainActivity.getApplication(),
							LoginIntercept.class);
					intent.putExtra("FACEBOOK_PERMISSIONS",
							AppConfig.FACEBOOK_PERMISSIONS);
					startActivityForResult(intent,
							LOGIN_INTERCEPT_REQUEST_CODE_TO_POST_A_COMMENT);

				}

				mTracker.trackEvent("CommunityBeerView", "AddComment",
						"Clicked", 0);
				mTracker.dispatch();
			}
		});

		mCommentsHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				mAddComments
						.setText(R.string.community_beer_add_comments_label);
				mAddComments.getBackground().setColorFilter(
						AppConfig.BUTTON_COLOR, PorterDuff.Mode.MULTIPLY);
				String jsonStr = (String) message.obj;
				if (Logger.isLogEnabled())  Logger.log(jsonStr);
				if ((jsonStr != null) && (jsonStr.startsWith("["))) {
					JSONArray json;
					try {
						json = new JSONArray(jsonStr);
						Log.d(_TAG, json.toString());
						for (int i = 0; i < json.length(); i++) {
							final JSONObject _comment = json.getJSONObject(i);
							Log.d(_TAG, _comment.toString());
							// add to the tablelayout dynamically
							TextView t1 = new TextView(mMainActivity);
							final String _userId = _comment.getString("userId");
							String _userName = _comment.getString("userName")
									+ " said...";
							Log.d(_TAG, _userName);
							t1.setText(_userName);
							t1.setTextColor(android.graphics.Color.BLUE);
							t1.setPaintFlags(mBrewery.getPaintFlags()
									| Paint.UNDERLINE_TEXT_FLAG);
							t1.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(mMainActivity
											.getApplication(),
											UserProfile.class);
									intent.putExtra("USERID", _userId);
									startActivity(intent);

								}
							});

							mCommentTextViews.add(t1);

							TableRow tableRow1 = new TableRow(mMainActivity);
							tableRow1.addView(t1);
							mCommentsLayout.addView(tableRow1);

							TextView t2 = new TextView(mMainActivity);
							t2.setText(_comment.getString("comment"));
							mCommentTextViews.add(t2);

							TableRow tableRow2 = new TableRow(mMainActivity);
							tableRow2.addView(t2);
							mCommentsLayout.addView(tableRow2);

							TextView t3 = new TextView(mMainActivity);
							long updated = _comment.getLong("updated");
							t3.setText(mDateFormat.format(updated));
							mCommentTextViews.add(t3);

							TableRow tableRow3 = new TableRow(mMainActivity);
							tableRow3.addView(t3);
							mCommentsLayout.addView(tableRow3);

							if ((mUser.isLoggedIn())
									&& (mUser.getUserId()
											.equalsIgnoreCase(_comment
													.getString("userId")))) {
								final TextView t = new TextView(mMainActivity);
								t.setText(mMainActivity
										.getString(R.string.edit_label));
								t.setTextColor(android.graphics.Color.BLUE);
								t.setPaintFlags(mBrewery.getPaintFlags()
										| Paint.UNDERLINE_TEXT_FLAG);
								t.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										try {
											Intent intent = new Intent(
													mMainActivity
															.getApplication(),
													EditComment.class);
											intent.putExtra("COMMENT_ID",
													_comment.getString("id"));
											intent.putExtra("COMMENT", _comment
													.getString("comment"));
											startActivityForResult(intent,
													REQUEST_CODE_TO_POST_A_COMMENT);
										} catch (Throwable e) {
											Log.e(_TAG,
													"error: "
															+ ((e.getMessage() != null) ? e
																	.getMessage()
																	.replace(
																			" ",
																			"_")
																	: ""), e);
										}

									}
								});
								TableRow tableRow = new TableRow(mMainActivity);
								tableRow.addView(t);
								mCommentsLayout.addView(tableRow);

							}

							TextView t4 = new TextView(mMainActivity);
							t4.setText(" ");

							TableRow tableRow4 = new TableRow(mMainActivity);
							tableRow4.addView(t4);
							mCommentsLayout.addView(tableRow4);
						}
					} catch (Throwable e) {
						Log.e(_TAG, "error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
					}
				}
			}
		};
		String url = Util.getCommentsUrl(mCommunityBeer.beerId);
		Log.d(_TAG, "Begin getting comments for beer: " + mCommunityBeer.beerId
				+ " url: " + url);
		mContentManager.fetchContentOnThread(url, mCommentsHandler);
		Log.d(_TAG, "End getting comments for beer: " + mCommunityBeer.beerId
				+ " url: " + url);

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
				if (Logger.isLogEnabled())  Logger.log(jsonStr);
				if ((jsonStr != null) && (jsonStr.startsWith("{"))) {
					try {
						JSONObject mFollowJson = new JSONObject(jsonStr);
						JSONArray followingList = mFollowJson
								.getJSONArray("followingList");
						Log.i(_TAG,
								"Following List Size="
										+ ((followingList != null) ? followingList
												.length() : "NULL"));
						String _str;
						for (int i = 0; ((followingList != null) && (i < followingList
								.length())); i++) {
							_str = followingList.getString(i);
							if (Logger.isLogEnabled())  Logger.log("Following " + _str);
							if ((_str != null)
									&& (_str.equalsIgnoreCase(mCommunityBeer.userId))) {
								mAlreadyFollowingReviewer = true;
								if (Logger.isLogEnabled())  Logger.log("Already Following Receiver!");
							}
						}

					} catch (Throwable e) {
						Log.e(_TAG, "error: "
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
					String unFollowUrl = Util.getSetUnfollowUrl(
							mUser.getUserId(), mUser.getUserName(),
							mUser.getUserLink(), mCommunityBeer.userId,
							mCommunityBeer.userName, mCommunityBeer.userLink);
					String followUrl = Util.getSetFollowUrl(mUser.getUserId(),
							mUser.getUserName(), mUser.getUserLink(),
							mCommunityBeer.userId, mCommunityBeer.userName,
							mCommunityBeer.userLink);

					@Override
					public void onClick(View arg0) {
						if (Logger.isLogEnabled())  Logger.log("Follow: " + mCommunityBeer.userName);

						if (mUser.isLoggedIn()) {

							if (mAlreadyFollowingReviewer) {
								Log.i(_TAG,
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
								Log.i(_TAG,
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
		mContentManager.fetchContentOnThread(
				Util.getFollowUrl(mUser.getUserId()), mFollowReviewerHandler);

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
						Log.d(_TAG, _reviewCount);
						mFollowCount.setText(_reviewCount);
					} catch (JSONException e) {
						Log.e(_TAG, "error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
					}
				}
			}
		};
		mContentManager.fetchContentOnThread(
				Util.getFollowCountUrl(mCommunityBeer.userId),
				mFollowCountHandler);
	}

	private void setupReviewCount() {
		mReviewCountHandler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				String jsonStr = (String) message.obj;
				if (Logger.isLogEnabled())  Logger.log(jsonStr);
				if ((jsonStr != null) && (jsonStr.startsWith("{"))) {
					JSONObject json;
					try {
						json = new JSONObject(jsonStr);
						String count = json.getString("count");
						String _reviewCount = count + " reviews";
						Log.d(_TAG, "Review Count Text:" + _reviewCount);
						mReviewedByReviewCount.setText(_reviewCount);

					} catch (JSONException e) {
						Log.e(_TAG, "error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
					}
				}
			}
		};
		mContentManager.fetchContentOnThread(
				Util.getReviewCountUrl(mCommunityBeer.userId),
				mReviewCountHandler);

	}

	// public void handleUserNotLoggedInFacebook()
	// {
	// if (Logger.isLogEnabled())  Logger.log("handleUserNotLoggedInFacebook");
	// Intent intent = new Intent(mMainActivity, LoginIntercept.class);
	// intent.putExtra("FACEBOOK_PERMISSIONS", AppConfig.FACEBOOK_PERMISSIONS);
	// intent.putExtra("FACEBOOK_ONLY", "Y");
	// mMainActivity.startActivityForResult(intent,
	// LOGIN_INTERCEPT_REQUEST_CODE_FOR_FACEBOOK_LIKE_BUTTON);
	//
	// }

	/************************************************************************************/
	private class AsyncPostFollow extends AsyncTask<Object, Void, Object> {
		private String _TAG = AsyncPostFollow.class.getName();

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
			} catch (Throwable e) {
				Log.e(_TAG,
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
			/** Remove follow count from cache **/
			mContentManager.removeContent(Util
					.getFollowCountUrl(mCommunityBeer.userId));
			/** Remove follow from cache **/
			mContentManager.removeContent(Util.getFollowUrl(mUser.getUserId()));

			// refresh
			mMainActivity.setupFollowReviewer();
			mMainActivity.setupFollowCount();

			if (Logger.isLogEnabled())  Logger.log("onPostExecute finished");
		}

	}

	/************************************************************************************/
	private class AsyncTranslateAll extends AsyncTask<Object, Void, Object> {
		private String _TAG = AsyncTranslateAll.class.getName();

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(Object... args) {
			if (Logger.isLogEnabled())  Logger.log("doInBackground starting");
			try {//
				/** Translate **/
				{
					String baseUrl = "https://www.googleapis.com/language/translate/v2"
							+ "?key="
							+ AppConfig.GOOGLE_API_KEY
							+ "&prettyprint=true"
							+ "&target="
							+ Locale.getDefault().getLanguage();
					List<TextView> views = new ArrayList<TextView>();

					if (!mColorTE.getText().equals("")) {
						// String _language = this.languageDetect(mColorTE
						// .getText().toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mColorTE);
						views.add(mColorTE);
					}
					if (!mClarityTE.getText().equals("")) {
						// String _language = this.languageDetect(mClarityTE
						// .getText().toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mClarityTE);
						views.add(mClarityTE);
					}
					if (!mFoamTE.getText().equals("")) {
						// String _language = this.languageDetect(mFoamTE
						// .getText().toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mFoamTE);
						views.add(mFoamTE);
					}
					if (!mAromaTE.getText().equals("")) {
						// String _language = this.languageDetect(mAromaTE
						// .getText().toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mAromaTE);
						views.add(mAromaTE);
					}
					if (!mBodyTE.getText().equals("")) {
						// String _language = this.languageDetect(mBodyTE
						// .getText().toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mBodyTE);
						views.add(mColorTE);
					}
					if (!mMouthfeelTE.getText().equals("")) {
						// String _language = this.languageDetect(mMouthfeelTE
						// .getText().toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mMouthfeelTE);
						views.add(mMouthfeelTE);
					}
					if (!mAftertasteTE.getText().equals("")) {
						// String _language = this.languageDetect(mAftertasteTE
						// .getText().toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mAftertasteTE);
						views.add(mAftertasteTE);
					}

					if (!mBeer.getText().equals("")) {
						// String _language =
						// this.languageDetect(mBeer.getText()
						// .toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mBeer);
						views.add(mBeer);
					}
					if (!mStyle.getText().equals("")) {
						// String _language =
						// this.languageDetect(mStyle.getText()
						// .toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mStyle);
						views.add(mColorTE);
					}
					if (!mBrewery.getText().equals("")) {
						// String _language = this.languageDetect(mBrewery
						// .getText().toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mBrewery);
						views.add(mBrewery);
					}
					if (!mState.getText().equals("")) {
						// String _language =
						// this.languageDetect(mState.getText()
						// .toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mState);
						views.add(mState);
					}
					if (!mCountry.getText().equals("")) {
						// String _language = this.languageDetect(mCountry
						// .getText().toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mCountry);
						views.add(mCountry);
					}
					if (!mNotes.getText().equals("")) {
						// String _language =
						// this.languageDetect(mNotes.getText()
						// .toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mNotes);
						views.add(mNotes);
					}
					if (!mReviewedBy.getText().equals("")) {
						// String _language = this.languageDetect(mReviewedBy
						// .getText().toString());
						// String _url = baseUrl + "&langpair=" + _language
						// + "%7C" + Locale.getDefault().getLanguage();
						// translate(baseUrl, mReviewedBy);
						views.add(mReviewedBy);
					}
					// comments
					for (Iterator<TextView> iterator = mCommentTextViews
							.iterator(); iterator.hasNext();) {
						TextView textView = iterator.next();
						// translate(baseUrl, textView);
						views.add(textView);
					}

					translate(baseUrl, views);
				}

			} catch (Throwable e) {
				Log.e(_TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
				mTracker.trackEvent(
						"CommunityBeerView",
						"TranslateCharacteristicsError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}

			if (Logger.isLogEnabled())  Logger.log("doInBackground finished");
			return null;
		}

		@Deprecated
		private String languageDetect(String languageHint) throws Exception {
			/** Detect the language **/
			URL url = new URL(
					"http://ajax.googleapis.com/ajax/services/language/detect?v=1.0&"
							+ "q="
							+ URLEncoder.encode(((String) languageHint),
									"UTF-8") + "&key="
							+ AppConfig.GOOGLE_API_KEY + "&hl="
							+ Locale.getDefault().getLanguage() + "&userip="
							+ Util.getLocalIpAddress());
			if (Logger.isLogEnabled())  Logger.log(url.toString());
			URLConnection connection = url.openConnection();
			connection.addRequestProperty("Referer",
					AppConfig.GOOGLE_TRANSLATE_REFERER);

			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			JSONObject json = new JSONObject(builder.toString());
			if (Logger.isLogEnabled())  Logger.log(json.toString());
			JSONObject responseData = json.getJSONObject("responseData");
			String language = responseData.getString("language");
			if (Logger.isLogEnabled())  Logger.log("Language Detected: " + language);

			return language;

		}

		@Deprecated
		private void translate(String url, final TextView textView) {
			try {
				URL _url = new URL(url
						+ "&q="
						+ URLEncoder.encode(((String) textView.getText()),
								"UTF-8"));
				if (Logger.isLogEnabled())  Logger.log(url.toString());
				URLConnection connection = _url.openConnection();
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
				if (Logger.isLogEnabled())  Logger.log(json.toString());
				JSONArray responseData = json.getJSONArray("translations");
				final String _translatedText = responseData.getJSONObject(0)
						.getString("translatedText");

				mMainActivity.runOnUiThread(new Runnable() {
					public void run() {
						textView.setText(_translatedText);
					}
				});

			} catch (Throwable e) {
				Log.e(_TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView",
						"TranslateFinishError", ((e.getMessage() != null) ? e
								.getMessage().replace(" ", "_") : "").replace(
								" ", "_"), 0);
				mTracker.dispatch();
			}

		}

		private void translate(String url, final List<TextView> views) {
			try {
				ArrayList<HttpParam> _params = new ArrayList<HttpParam>();
				for (TextView view : views) {
					HttpParam param = new HttpParam();
					param.name = "q";
					param.value = URLEncoder.encode(((String) view.getText()),
							"UTF-8");
					_params.add(param);
				}
				HashMap<String, String> requestProperties = new HashMap<String, String>();
				requestProperties.put("X-HTTP-Method-Override", "GET");
				String response = Util.openUrl(url, "POST", _params,
						requestProperties);
				if (Logger.isLogEnabled())  Logger.log("Response: " + response);
				JSONObject json = new JSONObject(response);
				if (Logger.isLogEnabled())  Logger.log(json.toString());
				JSONArray responseData = json.getJSONObject("data")
						.getJSONArray("translations");
				for (int i = 0; i < views.size(); i++) {
					final TextView view = views.get(i);
					final String _translatedText = responseData
							.getJSONObject(i).getString("translatedText");
					mMainActivity.runOnUiThread(new Runnable() {
						public void run() {
							view.setText(_translatedText);
						}
					});

				}

			} catch (Throwable e) {
				Log.e(_TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
				mTracker.trackEvent("CommunityBeerView",
						"TranslateFinishError", ((e.getMessage() != null) ? e
								.getMessage().replace(" ", "_") : "").replace(
								" ", "_"), 0);
				mTracker.dispatch();
			}

		}

		protected void onPostExecute(Object result) {
			if (Logger.isLogEnabled())  Logger.log("onPostExecute starting");

			mTranslate.getBackground().setColorFilter(AppConfig.BUTTON_COLOR,
					PorterDuff.Mode.MULTIPLY);
			mTranslate.setText(R.string.translated_label);

			if (Logger.isLogEnabled())  Logger.log("onPostExecute finished");
		}

	}

	/************************************************************************************/
	private class AsyncPostFavorites extends AsyncTask<Object, Void, Object> {

		private String _TAG = AsyncPostFavorites.class.getName();

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
				String _favoritesUrl = Util.getFavoritesUrl(mUser.getUserId());

				/** Remove favorites from cache **/
				mContentManager.removeContent(_favoritesUrl);

				/** Fetch updated favorites **/
				mContentManager.fetchContentOnThread(_favoritesUrl);

			} catch (Throwable e) {
				Log.e(_TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
				mTracker.trackEvent(
						"CommunityBeerView",
						"AsyncPostFavoritesError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
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

	// ----------------------------------------------------------------------//
	private class FacebookLikeButtonWebViewClient extends WebViewClient {
		private String _TAG = FacebookLikeButtonWebViewClient.class.getName();

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
				if (Logger.isLogEnabled())  Logger.log("onPageStarted::" + url);
			}
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("onPageFinished::" + url);
			}
			super.onPageFinished(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("onReceivedError::" + failingUrl + "::" + errorCode
						+ "::" + description);
			}
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("onLoadResource::" + url);
			}
			super.onLoadResource(view, url);
		}
	}

	/**************************************************************************************/
	private class AsyncSetupFacebookLikeButtonTask extends
			AsyncTask<String, Void, Void> {

		private String _TAG = AsyncSetupFacebookLikeButtonTask.class.getName();

		/**
		 * 
		 * @param args
		 * @return null
		 */
		protected Void doInBackground(String... args) {
			if (Logger.isLogEnabled())  Logger.log("doInBackground starting");
			try {
				// execute only if not present
				final String url = AppConfig.FACEBOOK_LIKE_URL_BASE
						+ AppConfig.FACEBOOK_LIKE_HREF_URL
						+ mCommunityBeer.beerId
						+ AppConfig.FACEBOOK_LIKE_URL_ETC
						+ AppConfig.FACEBOOK_LIKE_URL_LOCALE
						+ Locale.getDefault()
						+ AppConfig.FACEBOOK_LIKE_URL_ACCESS_TOKEN
						+ mFacebook.getAccessToken();
				Log.d(_TAG, "FacebookLikeButton URL: " + url);
				CommunityBeerView.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mFacebookLikeWebView.loadUrl(url);
					}
				});

			} catch (Exception e) {
				Log.e(_TAG,
						"error: "
								+ ((e.getMessage() != null) ? e.getMessage()
										.replace(" ", "_") : ""), e);
				mTracker.trackEvent(
						"CommunityBeerView",
						"AsyncSetupFacebookLikeButtonTaskError",
						((e.getMessage() != null) ? e.getMessage().replace(" ",
								"_") : "").replace(" ", "_"), 0);
				mTracker.dispatch();
			}
			if (AppConfig.LOGGING_ENABLED) {
				if (Logger.isLogEnabled())  Logger.log("doInBackground finished");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (Logger.isLogEnabled())  Logger.log("onPostExecute starting");
			if (Logger.isLogEnabled())  Logger.log("onPostExecute finished");
		}

	}

}
