package com.cm.beer.config;

import java.util.Currency;
import java.util.Locale;

import android.os.Environment;

public class AppConfig
{

	/** BEGIN: SET TO FALSE FOR PRODUCTION **/
	public static final boolean EMULATE_LOGIN = false;
	public static final String EMULATED_USER_ID[] = new String[]
	{ "ElDolor", "UserA", "UserB", "CocoMartini" };
	public static final String EMULATED_USER_NAME[] = new String[]
	{ "El Dolor", "User A", "User B", "Coco Martini" };
	public static final String EMULATED_USER_LINK = "http://google.com";
	public static final String EMULATED_USER_TYPE = "COMMUNITY";
	public static final String ADMIN_USER_EMAIL_ADDRESS = "anshu.gaind@gmail.com";
	public static final String EMULATED_USER_EMAIL = "el_dolor@hotmail.com";
	/** END: SET TO FALSE FOR PRODUCTION **/

	/** BEGIN: MARKETPLACE SETTINGS **/
	public static final String GOOGLE_APPSTORE = "GOOGLE_APPSTORE";
	public static final String AMAZON_APPSTORE = "AMAZON_APPSTORE";

	public static final String DEFAULT_APPSTORE = GOOGLE_APPSTORE;
	/** END: MARKETPLACE SETTINGS **/

	/** BEGIN: CHANGE FOR BEER LITE **/
	public static final boolean IS_BEER_LITE = false;
	public static final String APPLICATION_PACKAGE = "com.cm.beer.activity";
	/** END: CHANGE FOR BEER LITE **/

	/** BEGIN: SHARED PREFERENCES **/
	public static final String SHARED_PREFERENCES_DYNAMIC_CONTEXT = APPLICATION_PACKAGE
			+ "_1";
	/** END: SHARED PREFERENCES **/

	/** BEGIN: FACEBOOK LIKE BUTTON **/
	public static final String FACEBOOK_LIKE_URL_BASE = "http://www.facebook.com/plugins/like.php?href=";
	public static final String FACEBOOK_LIKE_HREF_URL = "http://beercellarcommunity.appspot.com/index.html?beerid=";
	public static final String FACEBOOK_LIKE_URL_ETC = "&layout=button_count&show_faces=false&width=90&action=like&font&colorscheme=light&height=20";
	public static final String FACEBOOK_LIKE_URL_LOCALE = "&locale=";
	public static final String FACEBOOK_LIKE_URL_ACCESS_TOKEN = "&access_token=";
	/** END: FACEBOOK LIKE BUTTON **/

	/** BEGIN: Notification Service **/
	/** 4 hr **/
	public static final long NOTIFICATION_CHECK_INTERVAL = (4) * (60 * 60) * 1000L;
	public static final long GET_RECOMMENDATIONS_NOTIFICATION_CHECK_INTERVAL = (1) * (60 * 60) * 1000L;

	public static final String NEW_BEER_REVIEW_NOTIFICATION_LAST_CHECKED = "NEW_BEER_REVIEW_NOTIFICATION_LAST_CHECKED";
	public static final String NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATION_LAST_CHECKED = "NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATION_LAST_CHECKED";
	public static final String BEER_OF_THE_DAY_NOTIFICATION_LAST_CHECKED = "BEER_OF_THE_DAY_NOTIFICATION_LAST_CHECKED";

	public static final String NEW_BEER_REVIEW_NOTIFICATION_CHECK_BACK_LATER_IN = "NEW_BEER_REVIEW_NOTIFICATION_CHECK_BACK_LATER_IN";
	public static final String NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATION_CHECK_BACK_LATER_IN = "NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATION_CHECK_BACK_LATER_IN";
	public static final String BEER_OF_THE_DAY_NOTIFICATION_CHECK_BACK_LATER_IN = "BEER_OF_THE_DAY_NOTIFICATION_CHECK_BACK_LATER_IN";
	/** END: Notification Service **/

	/** BEGIN: Google Analytics **/
	public static final String GOOGLE_ANALYTICS_WEB_PROPERTY_ID = "UA-10342197-7";
	/** END: Google Analytics **/

	public static final String GOOGLE_API_KEY = "AIzaSyBs22hxkOqH0Fp_f4pdhbQTt3XwZBWMOhI";

	/** BEGIN: Google URL Shortener **/
	public static final String GOOGLE_URL_SHORTENER_URL = "https://www.googleapis.com/urlshortener/v1/url";
	/** END: Google URL Shortener **/

	public static final int SELECT_IMAGE_REQUEST_CODE = 5;
	public static final int UPDATE_USER_PHOTO_REQUEST = 2;

	/** BEGIN: Google Maps **/
	public static final int GOOGLE_MAPS_ZOOM_LEVEL = 7;
	/** END: Google Maps **/

	/** BEGIN: BEER List **/
	public static final int BEER_LIST_ROWS_PER_PAGE = 10;
	/** END: BEER List **/

	/** BEGIN: Google Translate **/
	// public static final String GOOGLE_TRANSLATE_API_KEY =
	// "ABQIAAAAVWY7GdKmSqNHmSBZbrO3qhRHMeJCkjT_mOiVypJw08F6FpQ0sBSyt4I80v6TGencZZLgCoGhMOZHNQ";
	public static final String GOOGLE_TRANSLATE_REFERER = "http://beercellarcommunity.appspot.com/";
	/** END: Google Translate **/

	/** BEGIN: User Type **/
	public static final String USER_TYPE_COMMUNITY = "COMMUNITY";
	public static final String USER_TYPE_FACEBOOK = "FACEBOOK";
	/** END: User Type **/

	/** BEGIN: PREFERENCES **/
	public static final String RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS = "RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS";
	public static final String RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS = "RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS";
	public static final String RECEIVE_BEER_OF_THE_DAY_NOTIFICATION = "RECEIVE_BEER_OF_THE_DAY_NOTIFICATION";
	public static final String PREFERENCE_DO_NOT_SHOW_DID_YOU_KNOW = "DO_NOT_SHOW_DID_YOU_KNOW";

	public static final String PREFERENCE_DONE_RATE_AND_REVIEW = "DONE_RATE_AND_REVIEW";
	public static final String PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW = "REMIND_ME_LATER_RATE_AND_REVIEW";
	public static final String PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW_TIME = "REMIND_ME_LATER_RATE_AND_REVIEW_TIME";
	public static final long PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW_DELAY_INTERVAL = (48) * (60 * 60) * 1000L;// 48
	// hrs
	public static final String PREFERENCE_APPLICATION_USAGE_COUNT = "APPLICATION_USAGE_COUNT";
	public static final long APPLICATION_USAGE_COUNT_THRESHOLD_TO_DISPLAY_RATE_AND_REVIEW = 2;
	public static final String PREFERENCE_DO_NOT_SHOW_RATE_AND_REVIEW = "DO_NOT_SHOW_RATE_AND_REVIEW";
	public static final String PREFERENCE_BEER_LIST_ROWS_PER_PAGE = "BEER_LIST_ROWS_PER_PAGE";
	public static final String PREFERENCE_RECOMMENDATIONS_LASTRUNDATE = "RECOMMENDATIONS_LASTRUNDATE";

	/** END: PREFERENCES **/

	/** BEGIN: BEER Community **/
	public static final String COMMUNITY_BASE_URL = "http://beercellarcommunity.appspot.com/beercellarcommunity";
	public static final String COMMUNITY_BASE_SECURE_URL = "https://beercellarcommunity.appspot.com/beercellarcommunity";
	public static final String COMMUNITY_UPLOAD_USER_PHOTO_SECURE_URL = "https://beercellarcommunity.appspot.com";

	public static final String COMMUNITY_BEER_UPLOAD_URL = AppConfig.COMMUNITY_BASE_SECURE_URL
			+ "/beerupload";
	public static final String COMMUNITY_GET_BEERS_URL = AppConfig.COMMUNITY_BASE_URL
			+ "/beers?";
	public static final String COMMUNITY_GET_USER_SERVICE_URL = AppConfig.COMMUNITY_BASE_SECURE_URL
			+ "/users?";
	public static final String COMMUNITY_GET_DAILY_CAMPAIGN_SERVICE_URL = AppConfig.COMMUNITY_BASE_SECURE_URL
			+ "/dailycampaign?";
	public static final String COMMUNITY_COMMENTS_URL = AppConfig.COMMUNITY_BASE_URL
			+ "/comments?";

	public static final String COMMUNITY_GET_IMAGE_Q = "q=getimage&beerid=";
	public static final String COMMUNITY_GET_BEER_Q = "q=getbeer&beerid=";
	public static final String COMMUNITY_GET_BEERS_Q = "q=getbeers";
	public static final String COMMUNITY_GET_BEERS_CS_PARAM = "&cs=";
	public static final String COMMUNITY_GET_TOP_RATED_BEERS_Q = "q=gettopratedbeers";
	public static final String COMMUNITY_GET_WORST_BEERS_Q = "q=getworstratedbeers";
	public static final String COMMUNITY_GET_COUNTRIES_Q = "q=getcountries";
	public static final String COMMUNITY_GET_BEERS_BY_COUNTRY_Q = "q=getbeersbycountry";
	public static final String COMMUNITY_GET_BEERS_COUNTRY_PARAM = "&country=";
	public static final String COMMUNITY_GET_STATES_Q = "q=getstates";
	public static final String COMMUNITY_GET_BEERS_BY_STATES_Q = "q=getbeersbystate";
	public static final String COMMUNITY_GET_BEERS_STATE_PARAM = "&state=";
	public static final String COMMUNITY_SEARCH_BEERS_Q = "q=searchbeers";
	public static final String COMMUNITY_MY_BEERS_Q = "q=mybeers";
	public static final String COMMUNITY_GET_MOST_HELPFUL_BEER_REVIEWS_Q = "q=getmosthelpfulbeerreviews";
	public static final String COMMUNITY_GET_REVIEW_COUNT_Q = "q=getreviewcount&userid=";
	public static final String COMMUNITY_GET_REVIEW_HELPFUL_COUNT_Q = "q=getreviewhelpfulcount&beerid=";
	public static final String COMMUNITY_SET_REVIEW_HELPFUL_Q = "q=setreviewhelpful";
	public static final String COMMUNITY_SET_FOLLOW_Q = "q=setfollow";
	public static final String COMMUNITY_SET_UNFOLLOW_Q = "q=setunfollow";
	public static final String COMMUNITY_GET_FOLLOW_COUNT_Q = "q=getfollowcount&userid=";
	public static final String COMMUNITY_GET_FOLLOW_Q = "q=getfollow&userid=";
	public static final String COMMUNITY_GET_FOLLOWING_Q = "q=getfollowing&userid=";
	public static final String COMMUNITY_GET_FOLLOWERS_Q = "q=getfollowers&userid=";
	public static final String COMMUNITY_GET_NEW_BEER_REVIEWS_NOTIFICATION_Q = "q=getnewbeerreviewsnotification2";
	public static final String COMMUNITY_GET_NEW_BEER_REVIEWS_FROM_FOLLOWING_NOTIFICATION_Q = "q=getnewbeerreviewsfromfollowingnotification2";
	public static final String COMMUNITY_GET_NEW_BEER_REVIEWS_Q = "q=getnewbeerreviews";
	public static final String COMMUNITY_UPDATE_CHECK_Q = "q=updatecheck&market="
			+ DEFAULT_APPSTORE + "&currentversion=";
	public static final String COMMUNITY_ADD_TO_FAVORITES_Q = "q=addtofavorites";
	public static final String COMMUNITY_REMOVE_FROM_FAVORITES_Q = "q=removefromfavorites";
	public static final String COMMUNITY_GET_FAVORITES_Q = "q=getfavorites";
	public static final String COMMUNITY_GET_FAVORITE_BEERS_Q = "q=getfavoritebeers";

	public static final String COMMUNITY_UPLOAD_USER_PROFILE_Q = "q=uploaduserprofile";
	public static final String COMMUNITY_GET_USER_PROFILE_Q = "q=getuserprofile";
	public static final String COMMUNITY_GET_USER_PHOTO_Q = "q=getphoto&userid=";
	public static final String COMMUNITY_GET_UPLOAD_USER_PHOTO_URL_Q = "q=getuploadphotourl";

	public static final String COMMUNITY_LOGIN_Q_VALUE = "login";
	public static final String COMMUNITY_RECOVER_PASSWORD_Q_VALUE = "recoverpassword";
	public static final String COMMUNITY_SIGNUP_Q_VALUE = "signup";
	public static final String COMMUNITY_CHANGE_PASSWORD_Q_VALUE = "changepassword";
	public static final String COMMUNITY_UPDATE_USER_PROFILE_Q_VALUE = "updateuserprofile";

	public static final String COMMUNITY_GET_COMMENTS_Q = "q=getcomments";
	public static final String COMMUNITY_ADD_COMMENT_Q_VALUE = "addcomment";
	public static final String COMMUNITY_UPDATE_COMMENT_Q_VALUE = "updatecomment";
	public static final String COMMUNITY_DELETE_COMMENT_Q_VALUE = "deletecomment";

	public static final String COMMUNITY_GET_BEER_OF_THE_DAY_NOTIFICATION_Q = "q=getbeerofthedaynotification2";

	public static final String COMMUNITY_GET_EMAIL_SUBSCRIPTION_STATUS_Q = "q=getemailsubscriptionstatus&userid=";
	public static final String COMMUNITY_UPDATE_EMAIL_SUBSCRIPTION_STATUS_Q = "q=updateemailsubscriptionstatus";
	public static final String COMMUNITY_GET_COMMENT_POSTED_EMAIL_SUBSCRIPTION_STATUS_Q = "q=getcommentpostedemailsubscriptionstatus&userid=";
	public static final String COMMUNITY_UPDATE_COMMENT_POSTED_EMAIL_SUBSCRIPTION_STATUS_Q = "q=updatecommentpostedemailsubscriptionstatus";
	public static final String COMMUNITY_GET_COMPARABLES_Q = "q=getcomparables";
	public static final String COMMUNITY_GET_RECOMMENDATIONS_Q = "q=getrecommendations";

	public static final String COMMUNITY_USERID_PARAM = "&userid=";
	public static final String COMMUNITY_USER_NAME_PARAM = "&username=";
	public static final String COMMUNITY_USER_LINK_PARAM = "&userlink=";
	public static final String COMMUNITY_FOLLOW_USER_ID_PARAM = "&followuserid=";
	public static final String COMMUNITY_FOLLOW_USER_NAME_PARAM = "&followusername=";
	public static final String COMMUNITY_FOLLOW_USER_LINK_PARAM = "&followuserlink=";
	public static final String COMMUNITY_BEERID_PARAM = "&beerid=";
	public static final String COMMUNITY_RATERID_PARAM = "&raterid=";
	public static final String COMMUNITY_REVIEW_HELPFUL_PARAM = "&helpful=";
	public static final String COMMUNITY_LAST_CHECKED_PARAM = "&lastchecked=";
	public static final String COMMUNITY_BEERIDS_PARAM = "&beerids=";
	public static final String COMMUNITY_EMAIL_SUBSCRIPTION_PARAM = "&emailsubscriptionstatus=";
	public static final String COMMUNITY_COMMENT_POSTED_EMAIL_SUBSCRIPTION_PARAM = "&commentpostedemailsubscriptionstatus=";

	public static final String COMMUNITY_R_PARAM = "&r=";
	public static final String COMMUNITY_R_VALUE = "2";
	public static final String COMMUNITY_R = AppConfig.COMMUNITY_R_PARAM
			+ AppConfig.COMMUNITY_R_VALUE;
	public static final String COMMUNITY_BEERS_FROM_AROUND_THE_WORLD = "BEERS_FROM_AROUND_THE_WORLD";
	public static final String COMMUNITY_TOP_RATED_BEERS = "TOP_RATED_BEERS";
	public static final String COMMUNITY_WORST_BEERS = "WORST_BEERS";
	public static final String COMMUNITY_BEERS_BY_COUNTRY = "BEERS_BY_COUNTRY";
	public static final String COMMUNITY_BEERS_BY_STATE = "BEERS_BY_STATE";
	public static final String COMMUNITY_SEARCH_BEERS = "SEARCH_BEERS";
	public static final String COMMUNITY_MY_BEER_REVIEWS = "MY_BEER_REVIEWS";
	public static final String COMMUNITY_FAVORITE_BEER_REVIEWS = "FAVORITE_BEER_REVIEWS";
	public static final String COMMUNITY_MOST_HELPFUL_BEER_REVIEWS = "MOST_HELPFUL_BEER_REVIEWS";
	public static final String COMMUNITY_FOLLOWING = "FOLLOWING";
	public static final String COMMUNITY_FOLLOWERS = "FOLLOWERS";
	public static final String COMMUNITY_NEW_BEER_REVIEWS = "NEW_BEER_REVIEWS";
	public static final String COMMUNITY_BEER_OF_THE_DAY = "BEER_OF_THE_DAY";
	public static final String COMMUNITY_COMPARABLE_BEER_REVIEWS = "COMPARABLE_BEER_REVIEWS";
	public static final String COMMUNITY_RECOMMENDED_BEER_REVIEWS = "RECOMMENDED_BEER_REVIEWS";

	public static final String[] COMMUNITY_OPTIONS = new String[]
	{ "My Profile", "Beers From Around the World", "Top Rated Beers",
			"Worst Beers", "Search Beers", "Beers by Country",
			"Beers by State/Province/Region", "Favorite Beer Reviews",
			"My Beer Reviews", "Following", "Followers" };

	public static final String SHARE_WITH_COMMUNITY_INTERCEPT = "INTERCEPT";
	public static final String SHARE_WITH_COMMUNITY_DO_NOT_INTERCEPT = "DO_NOT_INTERCEPT";
	public static final String SHARE_WITH_COMMUNITY_INTERCEPT_IF_NOT_LOGGED_IN = "INTERCEPT_IF_NOT_LOGGED_IN";
	public static final int SHARE_WITH_COMMUNITY_BEER_UPLOAD_RETRY_COUNT = 5;
	public static final int COMMUNITY_LOGIN_RETRY_COUNT = 5;
	public static final int COMMUNITY_SIGNUP_RETRY_COUNT = 5;
	public static final int COMMUNITY_UPLOAD_USER_PHOTO_RETRY_COUNT = 5;
	public static final String COMMUNITY_SEND_TEST_DAILY_CAMPAIGN_Q = "q=sendtestemailcampaign&beerid=";
	public static final String COMMUNITY_SEND_DAILY_CAMPAIGN_Q = "q=sendemailcampaign&beerid=";
	/** END: Beer Community **/

	/** BEGIN: YELP **/
	// Timeout in milliseconds
	public static final int HTTP_CONNECTION_TIMEOUT = 20000;
	public static final int HTTP_SOCKET_TIMEOUT = 20000;
	public static final String YELP_BASE_URL = "http://api.yelp.com/business_review_search?";
	public static final String YELP_LIMIT = "";// "&limit=10";
	public static final String YELP_RADIUS = "";// "&radius=10";
	public static final String YELP_YWSID = "&ywsid=mqy15FZ5YyiWhL7rGPTG5g";
	public static final String YELP_LAT = "&lat=";
	public static final String YELP_LONG = "&long=";
	public static final String YELP_CATEGORY = "&category=breweries+beer_and_wine";
	public static final String YELP_LOGO_URL = "http://www.yelp.com";
	/** END: YELP **/

	/** BEGIN: AdMob setup **/
	// public static final boolean AD_TEST_ENABLED = false;
	// public static final String CLIENT_ID = "ca-mb-app-pub-0955368542600882";
	// public static final String COMPANY_NAME = "Coconut Martini Inc";
	// public static final String APP_NAME = "Beer Lite";
	// public static final String BEER_CELLAR_LITE_CHANNEL_ID = "6030210181";
	public static final String[] KEYWORDS =
	{ "beer" };
	public static final String INTERSTITIAL_UNIT_ID = "a14ef4f9ea0b7ca";
	public static final boolean ADMOB_TEST_EMULATOR = false;
	/** END: AdMob setup **/

	public static final String ERROR_REPORT_EMAIL = "beercellar@beercellarcommunity.com";

	/** BEGIN: Configurations **/
	public static int DID_YOU_KNOW_DELAY_MS = 2000;
	public static final boolean LOGGING_ENABLED = true;
	public static CharSequence[] DID_YOU_KNOW_MESSAGES =
	{
			"You can Share Beers in your Beer List with your friends on Facebook",
			"You can Backup your Beer List to your SD card, and Restore your Beer List from your Backup",
			"You can find out More Information about your Beers in your Beer List",
			"You can Add, Update, Delete your Beers in your Beer List",
			"You can import your Beer List from \"Beer Lite\" to \"Beer\"",
			"You can Search for Beers in your Beer List",
			"You can View Pictures of your Beers in your Beer List",
			"You can Search for Breweries and Bars around your current location",
			"You can find Beer Reviews by other Beer users",
			"You can view the Location of where you had your Beer, on Google Maps" };
	/** END: Configurations **/

	/** BEGIN: LOG COLLECTOR **/
	public static final String LOG_COLLECTOR_PACKAGE_NAME = "com.xtralogic.android.logcollector";//$NON-NLS-1$
	public static final String ACTION_SEND_LOG = "com.xtralogic.logcollector.intent.action.SEND_LOG";//$NON-NLS-1$
	public static final String EXTRA_SEND_INTENT_ACTION = "com.xtralogic.logcollector.intent.extra.SEND_INTENT_ACTION";//$NON-NLS-1$
	public static final String EXTRA_DATA = "com.xtralogic.logcollector.intent.extra.DATA";//$NON-NLS-1$
	public static final String EXTRA_ADDITIONAL_INFO = "com.xtralogic.logcollector.intent.extra.ADDITIONAL_INFO";//$NON-NLS-1$
	public static final String EXTRA_SHOW_UI = "com.xtralogic.logcollector.intent.extra.SHOW_UI";//$NON-NLS-1$
	public static final String EXTRA_FILTER_SPECS = "com.xtralogic.logcollector.intent.extra.FILTER_SPECS";//$NON-NLS-1$
	public static final String EXTRA_FORMAT = "com.xtralogic.logcollector.intent.extra.FORMAT";//$NON-NLS-1$
	public static final String EXTRA_BUFFER = "com.xtralogic.logcollector.intent.extra.BUFFER";//$NON-NLS-1$
	public static final String LOG_COLLECTOR_DETAILS_PAGE_MARKET_URI = "market://details?id="
			+ AppConfig.LOG_COLLECTOR_PACKAGE_NAME;
	/** END: LOG COLLECTOR **/

	/** BEGIN: Actions **/
	public static final String ACTION_INSERT = "INSERT";
	public static final String ACTION_UPDATE = "UPDATE";
	public static final String ACTION_DELETE = "DELETE";
	public static final int BEER_DELETED_RESULT_CODE = 100;
	/** END: Actions **/

	/** BEGIN: Referenced Urls **/
	public static final String GOOGLE_APPSTORE_APPLICATION_DETAILS_PAGE_URI = "market://details?id="
			+ AppConfig.APPLICATION_PACKAGE;
	public static final String AMAZON_APPSTORE_APPLICATION_DETAILS_PAGE_URI = "http://www.amazon.com/gp/mas/dl/android/"
			+ AppConfig.APPLICATION_PACKAGE;
	public static final String APPLICATION_DETAILS_PAGE_WEBSITE_URI = "http://coconutmartini.com/products/beer";
	public static final String WIKIPEDIA_REF_URL = "http://wikipedia.org/wiki/";
	/** END: Referenced Urls **/

	/** BEGIN: Seed Data **/
	public static final String DEFAULT_CURRENCY_CODE = Currency.getInstance(
			Locale.getDefault()).getCurrencyCode();
	public static final String DEFAULT_CURRENCY_SYMBOL = Currency.getInstance(
			Locale.getDefault()).getSymbol();
	public static final String[] SEED_DATA = new String[]
	{ "1", "Chimay Blanche", "8", DEFAULT_CURRENCY_SYMBOL,
			DEFAULT_CURRENCY_CODE, "9.95", "Trappist beer", "Chimay Brewery",
			"Chimay", "Belgium", "5.0", "1.jpg", "A really nice beer",
			String.valueOf(System.currentTimeMillis()),
			String.valueOf(System.currentTimeMillis()) };

	public static final String ASSET_SEED_PICTURE = "seedpicture";
	public static final String ASSET_SEED_THUMBNAIL = "seedthumbnail";
	/** END: Seed Data **/

	/** BEGIN: EULA **/
	public static final String ASSET_INSTRUCTIONS = "instructions";
	public static final String ASSET_DID_YOU_KNOW_INSTRUCTIONS = "didyouknow";
	public static final String ASSET_EULA = "EULA";
	public static final String PREFERENCE_EULA_ACCEPTED = "EULA_ACCEPTED";
	public static final String PREFERENCES_EULA = "HTD_EULA";
	/** END: EULA **/

	/** BEGIN: Preferences **/
	// public static final String PREFERENCES_BEER = "PREFERENCES_BEER";
	public static final String PREFERENCES_BEER_SEED_DATA_PICTURE_INITIALIZED = "PREFERENCES_BEER_SEED_DATA_PICTURE_INITIALIZED";
	public static final String PREFERENCES_BEER_SORT_BY = "PREFERENCES_BEER_SORT_BY";
	/** END: Preferences **/

	/** BEGIN: Pictures **/
	public static final int PICTURE_WIDTH = 1024;
	public static final int THUMBNAIL_WIDTH = 400;
	public static final int LIST_THUMBNAIL_WIDTH = 50;
	public static final int PORTRAIT_ORIENTATION_INTENT_BEGIN = 325;
	public static final int PORTRAIT_ORIENTATION_INTENT_END = 45;
	public static final String PATH_SEPARATOR = Environment
			.getExternalStorageDirectory().separator;
	public static final int EXTERNAL_STORAGE_NOT_AVAILABLE = 1;
	public static final int EXTERNAL_STORAGE_NOT_WRITABLE = 2;
	public static final int SAVING_PICTURE = 3;
	public static final String DATABASE_INTERNAL_LOCATION = Environment
			.getDataDirectory().getPath()
			+ PATH_SEPARATOR
			+ APPLICATION_PACKAGE
			+ PATH_SEPARATOR
			+ "databases"
			+ PATH_SEPARATOR;
	public static String BASE_APP_DIR = Environment
			.getExternalStorageDirectory().getPath()
			+ PATH_SEPARATOR
			+ "beercellar";
	public static String PICTURES_DIR = BASE_APP_DIR + PATH_SEPARATOR
			+ "pictures";
	public static String BACKUP_DIR = BASE_APP_DIR + PATH_SEPARATOR + "backup";
	public static final String PICTURES_THUMBNAILS_DIR = PICTURES_DIR
			+ PATH_SEPARATOR + "thumbnails";
	public static final String PICTURES_EXTENSION = ".jpg";
	public static final String PICTURES_THUMBNAILS_EXTENSION = "_thumbnail.jpg";
	/** END: Pictures **/

	/** BEGIN: Facebook **/
	public static final int FACEBOOK_LOGIN_INTERCEPT_REQUEST_CODE_FOR_WALL_POST = 44798;
	public static final int FACEBOOK_AUTHORIZE_ACTIVITY_RESULT_CODE = 357111;
	public static int FACEBOOK_MAX_IMAGE_DIMENSION = 720;
	public static final String FACEBOOK_HACK_ICON_URL = "http://www.facebookmobileweb.com/hackbook/img/facebook_icon_large.png";

	public static final int FACEBOOK_WALL_POST_SUCCESSFUL_RESULT_CODE = 99;
	public static final String FACEBOOK_APP_ID = "164683223557190";
	public static final String[] FACEBOOK_PERMISSIONS = new String[]
	{ "publish_stream, email" };
	// public static final String[] FACEBOOK_PERMISSIONS_WALL_POST = new
	// String[] { "publish_stream" };
	// public static final String[] FACEBOOK_PERMISSIONS_EMAIL = new String[] {
	// "email" };
	public static final String FACEBOOK_POST_LINK = "http://coconutmartini.com/products/img/beer/icon.png";
	public static final String FACEBOOK_ACCESS_TOKEN = "FACEBOOK_ACCESS_TOKEN";
	public static final String FACEBOOK_EXPIRES_IN = "FACEBOOK_EXPIRES_IN";
	/** END: Facebook **/

	/** BEGIN: USER **/
	public static final String USER_ID = "USER_ID";
	public static final String USER_NAME = "USER_NAME";
	public static final String USER_LINK = "USER_LINK";
	public static final String USER_TYPE = "USER_TYPE";
	public static final String USER_ADDITIONAL_ATTRIBUTES = "USER_ADDITIONAL_ATTRIBUTES";
	/** END: USER **/

	/** BEGIN: Dialogs **/
	public static final int DIALOG_LOADING_ID = 1;
	public static final int DIALOG_SAVING_ID = 2;
	public static final int DIALOG_DELETING_ID = 3;
	public static final int DIALOG_POSTING_ID = 4;
	public static final int DIALOG_SEARCHING_ID = 5;
	/** END: Dialogs **/

	/** BEGIN: BUTTON COLOR **/
	public static final int BUTTON_COLOR = 0xFFf8ce1e;
	public static final int BUTTON_COLOR_RED = 0xFFff0000;
	/** END: BUTTON COLOR **/

	public static final String[] BEERS = new String[]
	{};
	public static final String[] STYLES = new String[]
	{ "Aged Beer", "American Belgo Style Ale", "American Belgo Style Dark Ale",
			"American Belgo Style Pale Ale", "American Rye Ale",
			"American Style Amber Lager", "American Style Amber/Red Ale",
			"American Style Barley Wine Ale", "American Style Brown Ale",
			"American Style Cream Ale or Lager", "American Style Dark Lager",
			"American Style Ice Lager", "American Style Imperial Stout",
			"American Style India Pale Ale", "American Style Lager",
			"American Style Light Low Calorie Lager",
			"American Style Low Carbohydrate Light Lager",
			"American Style Malt Liquor", "American Style Märzen/Oktoberfest",
			"American Style Pale Ale", "American Style Pilsener",
			"American Style Premium Lager", "American Style Sour Ale",
			"American Style Specialty Lager", "American Style Stout",
			"American Style Strong Pale Ale", "American Style Wheat Beer",
			"American Style Wheat Wine Ale", "Australasian Pale Ale",
			"Baltic Style Porter", "Bamberg Style Rauchbier", "Banana beer",
			"Bappir", "Barley Wine Style Ale", "Belgian Style Abbey Ale",
			"Belgian Style Blonde Ale", "Belgian Style Dark Strong Ale",
			"Belgian Style Dubbel",
			"Belgian Style Flanders/Oud Bruin or Oud Red Ale",
			"Belgian Style Fruit Lambic", "Belgian Style Gueuze Lambic",
			"Belgian Style Lambic", "Belgian Style Pale Ale",
			"Belgian Style Pale Strong Ale", "Belgian Style Sour Ale",
			"Belgian Style Strong Specialty Ale", "Belgian Style Table Beer",
			"Belgian Style Tripel", "Belgian Style Witbier",
			"Berliner Style Weisse", "Berliner Weisse",
			"Bohemian Style Pilsener", "Boza", "Brem",
			"British Style Imperial Stout", "Brown Porter", "Brunswick Mum",
			"California Common Beer", "Cauim", "Chhaang", "Chicha",
			"Chocolate/Cocoa Flavored Beer", "Choujiu", "Chuak", "Cider",
			"Classic Irish Style Dry Stout", "Coffee Flavored Beer", "Cuirm",
			"Dark American Wheat Ale", "Dark American Wheat Ale",
			"Dortmunder/European Style Export", "Draught Beer", "Dry Lager",
			"English Style Barley Wine Ale", "English Style Brown Ale",
			"English Style Dark Mild Ale", "English Style India Pale Ale",
			"English Style Mild Ale", "English Style Pale Ale",
			"English Style Pale Mild Ale", "English Style Summer Ale",
			"European Style Dark/Münchner Dunkel",
			"European Style Low Alcohol Lager/German Style Leicht Bier",
			"Experimental Beer", "Extra Special Bitter or Strong Bitter",
			"Field Beer", "Foreign Style Stout",
			"French & Belgian Style Saison", "French Style Bière de Garde",
			"Fresh Hop Ale", "Fruit Beer", "Fruit Wheat Beer",
			"Fruited American Style Sour Ale",
			"Fruited Wood and Barrel Aged Sour Beer",
			"German Style Brown Ale/Düsseldorf Style Altbier",
			"German Style Dark Wheat Ale", "German Style Doppelbock",
			"German Style Eisbock", "German Style Heller Bock/Maibock",
			"German Style Kölsch/Köln Style Kölsch",
			"German Style Leichtes Weizen/Weissbier", "German Style Märzen",
			"German Style Oktoberfest/Wiesen Meadow",
			"German Style Pale Wheat Ale", "German Style Pilsener",
			"German Style Rye Ale", "German Style Schwarzbier",
			"German Style Sour Ale", "Gluten Free Beer",
			"Golden or Blonde Ale", "Gose", "Gruit", "Gueuze", "Happoshu",
			"Heller Bock", "Herb and Spice Beer", "High Gravity Beer",
			"Huangjiu", "Ibwatu", "Ice Beer", "Ice Cider",
			"Imperial India Pale Ale", "Imperial Red Ale",
			"International India Pale Ale", "International Pale Ale",
			"International Strong Pale Ale", "International Style Lager",
			"International Style Pilsener", "Irish Style Red Ale",
			"Kellerbier", "Kentucky Common Beer", "Leipzig Style Gose",
			"Light American Wheat Ale", "Low Alcohol beer", "Malt beer",
			"Malzbier", "Mbege", "Millet Beer", "Münchner Style Helles",
			"Oatmeal Stout", "Old Ale", "Ordinary Bitter", "Oshikundu",
			"Peated Scotch Ale", "Perry", "Pito", "Pumpkin Beer", "Radler",
			"Robust Porter", "Roggenbier", "Sahti", "Sake", "Sake Daiginjo",
			"Sake Futsu shu", "Sake Genshu", "Sake Ginjo", "Sake Honjozo",
			"Sake Infused", "Sake Junmai", "Sake Koshu", "Sake Namasake",
			"Sake Nigori", "Sake Taru", "Sake Tokubetsu", "Sato",
			"Scottish Style Ale", "Scottish Style Export Ale",
			"Scottish Style Heavy Ale", "Scottish Style Light Ale",
			"Session Beer", "Shandy", "Smoked Beer", "Smoked Porter",
			"South German Style Bernsteinfarbenes Weizen/Weissbier",
			"South German Style Dunkel Weizen/Dunkel Weissbier",
			"South German Style Hefeweizen/Hefeweissbier",
			"South German Style Kristal Weizen/Kristal Weissbier",
			"South German Style Weizenbock/Weissbock",
			"Special Bitter or Best Bitter", "Specialty Beer",
			"Specialty Honey Beer", "Steam Beer", "Steinbier", "Strong Ale",
			"Strong Ale or Lager", "Strong Scotch Ale", "Sweet Stout", "Tella",
			"Tiswin", "Tongba", "Traditional German Style Bock",
			"Traditionally Brewed Beer", "Unfiltered German Style Ale",
			"Unfiltered German Style Lager", "Vienna Style Lager",
			"Wood and Barrel Aged Beer", "Wood and Barrel Aged Dark Beer",
			"Wood and Barrel Aged Pale to Amber Beer",
			"Wood and Barrel Aged Sour Beer",
			"Wood and Barrel Aged Strong Beer", "Zozu", "Zutho", "Zwickelbier" };
	public static final String[] COUNTRIES = new String[]
	{ "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra",
			"Angola", "Anguilla", "Antarctica", "Antigua and Barbuda",
			"Argentina", "Armenia", "Aruba", "Australia", "Austria",
			"Azerbaijan", "Bahrain", "Bangladesh", "Barbados", "Belarus",
			"Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia",
			"Bosnia and Herzegovina", "Botswana", "Bouvet Island", "Brazil",
			"British Indian Ocean Territory", "British Virgin Islands",
			"Brunei", "Bulgaria", "Burkina Faso", "Burundi", "Cote d'Ivoire",
			"Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands",
			"Central African Republic", "Chad", "Chile", "China",
			"Christmas Island", "Cocos (Keeling) Islands", "Colombia",
			"Comoros", "Congo", "Cook Islands", "Costa Rica", "Croatia",
			"Cuba", "Cyprus", "Czech Republic",
			"Democratic Republic of the Congo", "Denmark", "Djibouti",
			"Dominica", "Dominican Republic", "East Timor", "Ecuador", "Egypt",
			"El Salvador", "Equatorial Guinea", "Eritrea", "Estonia",
			"Ethiopia", "Faeroe Islands", "Falkland Islands", "Fiji",
			"Finland", "Former Yugoslav Republic of Macedonia", "France",
			"French Guiana", "French Polynesia", "French Southern Territories",
			"Gabon", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece",
			"Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala",
			"Guinea", "Guinea-Bissau", "Guyana", "Haiti",
			"Heard Island and McDonald Islands", "Honduras", "Hong Kong",
			"Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq",
			"Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan",
			"Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos",
			"Latvia", "Lebanon", "Lesotho", "Liberia", "Libya",
			"Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Madagascar",
			"Malawi", "Malaysia", "Maldives", "Mali", "Malta",
			"Marshall Islands", "Martinique", "Mauritania", "Mauritius",
			"Mayotte", "Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia",
			"Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia",
			"Nauru", "Nepal", "Netherlands", "Netherlands Antilles",
			"New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria",
			"Niue", "Norfolk Island", "North Korea", "Northern Marianas",
			"Norway", "Oman", "Pakistan", "Palau", "Panama",
			"Papua New Guinea", "Paraguay", "Peru", "Philippines",
			"Pitcairn Islands", "Poland", "Portugal", "Puerto Rico", "Qatar",
			"Reunion", "Romania", "Russia", "Rwanda", "Sqo Tome and Principe",
			"Saint Helena", "Saint Kitts and Nevis", "Saint Lucia",
			"Saint Pierre and Miquelon", "Saint Vincent and the Grenadines",
			"Samoa", "San Marino", "Saudi Arabia", "Senegal", "Seychelles",
			"Sierra Leone", "Singapore", "Slovakia", "Slovenia",
			"Solomon Islands", "Somalia", "South Africa",
			"South Georgia and the South Sandwich Islands", "South Korea",
			"Spain", "Sri Lanka", "Sudan", "Suriname",
			"Svalbard and Jan Mayen", "Swaziland", "Sweden", "Switzerland",
			"Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand",
			"The Bahamas", "The Gambia", "Togo", "Tokelau", "Tonga",
			"Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan",
			"Turks and Caicos Islands", "Tuvalu", "Virgin Islands", "Uganda",
			"Ukraine", "United Arab Emirates", "United Kingdom",
			"United States", "United States Minor Outlying Islands", "Uruguay",
			"Uzbekistan", "Vanuatu", "Vatican City", "Venezuela", "Vietnam",
			"Wallis and Futuna", "Western Sahara", "Yemen", "Yugoslavia",
			"Zambia", "Zimbabwe" };
	public static final String[] STATES = new String[]
	{};

	public static final String[] CURRENCY_COUNTRY =
	{ "United States of America, Dollars", "Albania, Leke",
			"Afghanistan, Afghanis", "Argentina, Pesos", "Aruba, Guilders",
			"Australia, Dollars", "Azerbaijan, New Manats", "Bahamas, Dollars",
			"Barbados, Dollars", "Belarus, Rubles", "Belgium, Euro",
			"Belize, Dollars", "Bermuda, Dollars", "Bolivia, Bolivianos",
			"Bosnia and Herzegovina, Convertible Marka", "Botswana, Pulas",
			"Bulgaria, Leva", "Brazil, Reais",
			"Britain (United Kingdom), Pounds", "Brunei Darussalam, Dollars",
			"Cambodia, Riels", "Canada, Dollars", "Cayman Islands, Dollars",
			"Chile, Pesos", "China, Yuan Renminbi", "Colombia, Pesos",
			"Costa Rica, Colón", "Croatia, Kuna", "Cuba, Pesos",
			"Cyprus, Euro", "Czech Republic, Koruny", "Denmark, Kroner",
			"Dominican Republic, Pesos", "East Caribbean, Dollars",
			"Egypt, Pounds", "El Salvador, Colones",
			"England (United Kingdom), Pounds", "Euro",
			"Falkland Islands, Pounds", "Fiji, Dollars", "France, Euro",
			"Ghana, Cedis", "Gibraltar, Pounds", "Greece, Euro",
			"Guatemala, Quetzales", "Guernsey, Pounds", "Guyana, Dollars",
			"Holland (Netherlands), Euro", "Honduras, Lempiras",
			"Hong Kong, Dollars", "Hungary, Forint", "Iceland, Kronur",
			"India, Rupees", "Indonesia, Rupiahs", "Iran, Rials",
			"Ireland, Euro", "Isle of Man, Pounds", "Israel, New Shekels",
			"Italy, Euro", "Jamaica, Dollars", "Japan, Yen", "Jersey, Pounds",
			"Kazakhstan, Tenge", "Korea (North), Won", "Korea (South), Won",
			"Kyrgyzstan, Soms", "Laos, Kips", "Latvia, Lati",
			"Lebanon, Pounds", "Liberia, Dollars",
			"Liechtenstein, Switzerland Francs", "Lithuania, Litai",
			"Luxembourg, Euro", "Macedonia, Denars", "Malaysia, Ringgits",
			"Malta, Euro", "Mauritius, Rupees", "Mexico, Pesos",
			"Mongolia, Tugriks", "Mozambique, Meticais", "Namibia, Dollars",
			"Nepal, Rupees",
			"Netherlands Antilles, Guilders (also called Florins)",
			"Netherlands, Euro", "New Zealand, Dollars", "Nicaragua, Cordobas",
			"Nigeria, Nairas", "North Korea, Won", "Norway, Krone",
			"Oman, Rials", "Pakistan, Rupees", "Panama, Balboa",
			"Paraguay, Guarani", "Peru, Nuevos Soles", "Philippines, Pesos",
			"Poland, Zlotych", "Qatar, Rials", "Romania, New Lei",
			"Russia, Rubles", "Saint Helena, Pounds", "Saudi Arabia, Riyals",
			"Serbia, Dinars", "Seychelles, Rupees", "Singapore, Dollars",
			"Slovenia, Euro", "Solomon Islands, Dollars", "Somalia, Shillings",
			"South Africa, Rand", "South Korea, Won", "Spain, Euro",
			"Sri Lanka, Rupees", "Sweden, Kronor", "Switzerland, Francs",
			"Suriname, Dollars", "Syria, Pounds", "Taiwan, New Dollars",
			"Thailand, Baht", "Trinidad and Tobago, Dollars", "Turkey, Lira",
			"Turkey, Liras", "Tuvalu, Dollars", "Ukraine, Hryvnia",
			"United Kingdom, Pounds", "United States of America, Dollars",
			"Uruguay, Pesos", "Uzbekistan, Sums", "Vatican City, Euro",
			"Venezuela, Bolivares Fuertes", "Vietnam, Dong", "Yemen, Rials",
			"Zimbabwe, Zimbabwe Dollars" };
	public static final String[] CURRENCY_SYMBOL =
	{ "\u0024", "\u004c\u0065\u006b", "\u060b", "\u0024", "\u0192", "\u0024",
			"\u043c\u0430\u043d", "\u0024", "\u0024", "\u0070\u002e", "\u20ac",
			"\u0042\u005a\u0024", "\u0024", "\u0024\u0062", "\u004b\u004d",
			"\u0050", "\u043b\u0432", "\u0052\u0024", "\u00a3", "\u0024",
			"\u17db", "\u0024", "\u0024", "\u0024", "\u00a5", "\u0024",
			"\u20a1", "\u006b\u006e", "\u20b1", "\u20ac", "\u004b\u010d",
			"\u006b\u0072", "\u0052\u0044\u0024", "\u0024", "\u00a3", "\u0024",
			"\u00a3", "\u20ac", "\u00a3", "\u0024", "\u20ac", "\u00a2",
			"\u00a3", "\u20ac", "\u0051", "\u00a3", "\u0024", "\u20ac",
			"\u004c", "\u0024", "\u0046\u0074", "\u006b\u0072", "\u20b9",
			"\u0052\u0070", "\ufdfc", "\u20ac", "\u00a3", "\u20aa", "\u20ac",
			"\u004a\u0024", "\u00a5", "\u00a3", "\u043b\u0432", "\u20a9",
			"\u20a9", "\u043b\u0432", "\u20ad", "\u004c\u0073", "\u00a3",
			"\u0024", "\u0043\u0048\u0046", "\u004c\u0074", "\u20ac",
			"\u0434\u0435\u043d", "\u0052\u004d", "\u20ac", "\u20a8", "\u0024",
			"\u20ae", "\u004d\u0054", "\u0024", "\u20a8", "\u0192", "\u20ac",
			"\u0024", "\u0043\u0024", "\u20a6", "\u20a9", "\u006b\u0072",
			"\ufdfc", "\u20a8", "\u0042\u002f\u002e", "\u0047\u0073",
			"\u0053\u002f\u002e", "\u0050\u0068\u0070", "\u007a\u0142",
			"\ufdfc", "\u006c\u0065\u0069", "440\u0443\u0431", "\u00a3",
			"\ufdfc", "\u0414\u0438\u043d\u002e", "\u20a8", "\u0024", "\u20ac",
			"\u0024", "\u0053", "\u0052", "\u20a9", "\u20ac", "\u20a8",
			"\u006b\u0072", "\u0043\u0048\u0046", "\u0024", "\u00a3",
			"\u004e\u0054\u0024", "\u0e3f", "\u0054\u0054\u0024",
			"\u0054\u004c", "\u20a4", "\u0024", "\u20b4", "\u00a3", "\u0024",
			"\u0024\u0055", "\u043b\u0432", "\u20ac", "\u0042\u0073", "\u20ab",
			"\ufdfc", "\u005a\u0024" };

	public static final String[] CURRENCY_CODE =
	{ "USD", "ALL", "AFN", "ARS", "AWG", "AUD", "AZN", "BSD", "BBD", "BYR",
			"EUR", "BZD", "BMD", "BOB", "BAM", "BWP", "BGN", "BRL", "GBP",
			"BND", "KHR", "CAD", "KYD", "CLP", "CNY", "COP", "CRC", "HRK",
			"CUP", "EUR", "CZK", "DKK", "DOP", "XCD", "EGP", "SVC", "GBP",
			"EUR", "FKP", "FJD", "EUR", "GHC", "GIP", "EUR", "GTQ", "GGP",
			"GYD", "EUR", "HNL", "HKD", "HUF", "ISK", "INR", "IDR", "IRR",
			"EUR", "IMP", "ILS", "EUR", "JMD", "JPY", "JEP", "KZT", "KPW",
			"KRW", "KGS", "LAK", "LVL", "LBP", "LRD", "CHF", "LTL", "EUR",
			"MKD", "MYR", "EUR", "MUR", "MXN", "MNT", "MZN", "NAD", "NPR",
			"ANG", "EUR", "NZD", "NIO", "NGN", "KPW", "NOK", "OMR", "PKR",
			"PAB", "PYG", "PEN", "PHP", "PLN", "QAR", "RON", "RUB", "SHP",
			"SAR", "RSD", "SCR", "SGD", "EUR", "SBD", "SOS", "ZAR", "KRW",
			"EUR", "LKR", "SEK", "CHF", "SRD", "SYP", "TWD", "THB", "TTD",
			"TRY", "TRL", "TVD", "UAH", "GBP", "USD", "UYU", "UZS", "EUR",
			"VEF", "VND", "YER", "ZWD" };
}
