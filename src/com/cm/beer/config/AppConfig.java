package com.cm.beer.config;

import android.os.Environment;

public class AppConfig {

	/** BEGIN: SET TO FALSE FOR PRODUCTION **/
	public static final boolean EMULATE_LOGIN = false;
	public static final String EMULATED_USER_ID[] = new String[] { "ElDolor",
			"UserA", "UserB", "CocoMartini" };
	public static final String EMULATED_USER_NAME[] = new String[] {
			"El Dolor", "User A", "User B", "Coco Martini" };
	public static final String EMULATED_USER_LINK = "http://google.com";
	public static final String EMULATED_USER_TYPE = "COMMUNITY";
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

	/** BEGIN: Notification Service **/
	public static final long NOTIFICATION_CHECK_INTERVAL = ((60 * 60) * 1000L);
	/** 1 hr **/
	public static final String NOTIFICATION_LAST_CHECKED = "NOTIFICATION_LAST_CHECKED";
	public static final String NOTIFICATION_CHECK_BACK_LATER_IN = "NOTIFICATION_CHECK_BACK_LATER_IN";
	/** END: Notification Service **/

	/** BEGIN: Google Analytics **/
	public static final String GOOGLE_ANALYTICS_WEB_PROPERTY_ID = "UA-10342197-7";
	/** END: Google Analytics **/

	public static final int SELECT_IMAGE_REQUEST_CODE = 5;
	public static final int UPDATE_USER_PHOTO_REQUEST = 2;

	/** BEGIN: Google Maps **/
	public static final int GOOGLE_MAPS_ZOOM_LEVEL = 7;
	/** END: Google Maps **/

	/** BEGIN: BEER List **/
	public static final int BEER_LIST_ROWS_PER_PAGE = 10;
	/** END: BEER List **/

	/** BEGIN: Google Translate **/
	public static final String GOOGLE_TRANSLATE_API_KEY = "ABQIAAAAVWY7GdKmSqNHmSBZbrO3qhRHMeJCkjT_mOiVypJw08F6FpQ0sBSyt4I80v6TGencZZLgCoGhMOZHNQ";
	public static final String GOOGLE_TRANSLATE_REFERER = "http://beercellarcommunity.appspot.com/";
	/** END: Google Translate **/

	/** BEGIN: User Type **/
	public static final String USER_TYPE_COMMUNITY = "COMMUNITY";
	public static final String USER_TYPE_FACEBOOK = "FACEBOOK";
	/** END: User Type **/

	/** BEGIN: PREFERENCES **/
	public static final String RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS = "RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS";
	public static final String RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS = "RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS";
	public static final String RECEIVE_BEER_OF_THE_DAY_NOTIFICATION = "RECEIVE_BEER_OF_THE_DAY_NOTIFICATIONS";
	public static final String PREFERENCE_DO_NOT_SHOW_DID_YOU_KNOW = "DO_NOT_SHOW_DID_YOU_KNOW";

	public static final String PREFERENCE_DONE_RATE_AND_REVIEW = "DONE_RATE_AND_REVIEW";
	public static final String PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW = "REMIND_ME_LATER_RATE_AND_REVIEW";
	public static final String PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW_TIME = "REMIND_ME_LATER_RATE_AND_REVIEW_TIME";
	public static final long PREFERENCE_REMIND_ME_LATER_RATE_AND_REVIEW_DELAY_INTERVAL = (48) * (60 * 60) * 1000L;// 48
	// hrs
	public static final String PREFERENCE_APPLICATION_USAGE_COUNT = "APPLICATION_USAGE_COUNT";
	public static final long APPLICATION_USAGE_COUNT_THRESHOLD_TO_DISPLAY_RATE_AND_REVIEW = 2;
	public static final String PREFERENCE_DO_NOT_SHOW_RATE_AND_REVIEW = "DO_NOT_SHOW_RATE_AND_REVIEW";
	/** END: PREFERENCES **/

	/** BEGIN: BEER Community **/

	// public static final String COMMUNITY_BASE_URL =
	// "http://10.239.17.24:8888/beercellarcommunity"; public static final
	// String COMMUNITY_BASE_SECURE_URL =
	// "http://10.239.17.24:8888/beercellarcommunity"; public static final
	// String COMMUNITY_UPLOAD_USER_PHOTO_SECURE_URL =
	// "http://10.239.17.24:8888";

	public static final String COMMUNITY_BASE_URL = "http://beercellarcommunity.appspot.com/beercellarcommunity";
	public static final String COMMUNITY_BASE_SECURE_URL = "https://beercellarcommunity.appspot.com/beercellarcommunity";
	public static final String COMMUNITY_UPLOAD_USER_PHOTO_SECURE_URL = "https://beercellarcommunity.appspot.com";

	public static final String COMMUNITY_BEER_UPLOAD_URL = AppConfig.COMMUNITY_BASE_SECURE_URL
			+ "/beerupload";
	public static final String COMMUNITY_GET_BEERS_URL = AppConfig.COMMUNITY_BASE_URL
			+ "/beers?";
	public static final String COMMUNITY_GET_USER_SERVICE_URL = AppConfig.COMMUNITY_BASE_SECURE_URL
			+ "/users?";

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
	public static final String COMMUNITY_GET_NEW_BEER_REVIEWS_NOTIFICATION_Q = "q=getnewbeerreviewsnotification";
	public static final String COMMUNITY_GET_NEW_BEER_REVIEWS_FROM_FOLLOWING_NOTIFICATION_Q = "q=getnewbeerreviewsfromfollowingnotification";
	public static final String COMMUNITY_GET_NEW_BEER_REVIEWS_Q = "q=getnewbeerreviews";
	public static final String COMMUNITY_UPDATE_CHECK_Q = "q=updatecheck&currentversion=";
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

	public static final String COMMUNITY_GET_BEER_OF_THE_DAY_NOTIFICATION_Q = "q=getbeerofthedaynotification";

	public static final String COMMUNITY_GET_EMAIL_SUBSCRIPTION_STATUS_Q = "q=getemailsubscriptionstatus&userid=";
	public static final String COMMUNITY_UPDATE_EMAIL_SUBSCRIPTION_STATUS_Q = "q=updateemailsubscriptionstatus";

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

	public static final String COMMUNITY_R_PARAM = "&r=";
	public static final String COMMUNITY_R_VALUE = "10";
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

	public static final String[] COMMUNITY_OPTIONS = new String[] {
			"My Profile", "Beers From Around the World", "Top Rated Beers",
			"Most Helpful Beer Reviews", "Worst Beers", "Search Beers",
			"Beers by Country", "Beers by State/Province/Region",
			"Favorite Beer Reviews", "My Beer Reviews", "Following",
			"Followers" };

	public static final String SHARE_WITH_COMMUNITY_INTERCEPT = "INTERCEPT";
	public static final String SHARE_WITH_COMMUNITY_DO_NOT_INTERCEPT = "DO_NOT_INTERCEPT";
	public static final String SHARE_WITH_COMMUNITY_INTERCEPT_IF_NOT_LOGGED_IN = "INTERCEPT_IF_NOT_LOGGED_IN";
	public static final int SHARE_WITH_COMMUNITY_BEER_UPLOAD_RETRY_COUNT = 5;
	public static final int COMMUNITY_LOGIN_RETRY_COUNT = 5;
	public static final int COMMUNITY_SIGNUP_RETRY_COUNT = 5;
	public static final int COMMUNITY_UPLOAD_USER_PHOTO_RETRY_COUNT = 5;
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

	/** BEGIN: Adsense setup **/
	public static final boolean AD_TEST_ENABLED = false;
	public static final String CLIENT_ID = "ca-mb-app-pub-0955368542600882";
	public static final String COMPANY_NAME = "Coconut Martini Inc";
	public static final String APP_NAME = "Beer Lite";
	public static final String BEER_CELLAR_LITE_CHANNEL_ID = "6030210181";
	public static final String KEYWORDS = "beer";
	/** END: Adsense setup **/

	public static final String ERROR_REPORT_EMAIL = "beercellar@beercellarcommunity.com";

	/** BEGIN: Configurations **/
	public static int DID_YOU_KNOW_DELAY_MS = 2000;
	public static final boolean LOGGING_ENABLED = true;
	public static CharSequence[] DID_YOU_KNOW_MESSAGES = {
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
	public static final String[] SEED_DATA = new String[] { "1",
			"Chimay Blanche", "8", "9.95", "Trappist beer", "Chimay Brewery",
			"Chimay", "Belgium", "5.0", "1.jpg",
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
	public static final String PREFERENCES_BEER = "PREFERENCES_BEER";
	public static final String PREFERENCES_BEER_SEED_DATA_PICTURE_INITIALIZED = "PREFERENCES_BEER_SEED_DATA_PICTURE_INITIALIZED";
	public static final String PREFERENCES_BEER_SORT_BY = "PREFERENCES_BEER_SORT_BY";
	/** END: Preferences **/

	/** BEGIN: Pictures **/
	public static final int PICTURE_WIDTH = 1024;
	public static final int PICTURE_HEIGHT = 768;
	public static final int PORTRAIT_ORIENTATION_INTENT_BEGIN = 325;
	public static final int PORTRAIT_ORIENTATION_INTENT_END = 45;
	public static final int THUMBNAIL_WIDTH = 200;
	public static final int THUMBNAIL_HEIGHT = 200;
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
			+ "databases" + PATH_SEPARATOR;
	public static String BASE_APP_DIR = Environment
			.getExternalStorageDirectory().getPath()
			+ PATH_SEPARATOR + "beercellar";
	public static String PICTURES_DIR = BASE_APP_DIR + PATH_SEPARATOR
			+ "pictures";
	public static String BACKUP_DIR = BASE_APP_DIR + PATH_SEPARATOR + "backup";
	public static final String PICTURES_THUMBNAILS_DIR = PICTURES_DIR
			+ PATH_SEPARATOR + "thumbnails";
	public static final String PICTURES_EXTENSION = ".jpg";
	public static final String PICTURES_THUMBNAILS_EXTENSION = "_thumbnail.jpg";
	/** END: Pictures **/

	/** BEGIN: Facebook **/
	public static final int FACEBOOK_WALL_POST_SUCCESSFUL_RESULT_CODE = 99;
	public static final String FACEBOOK_APP_ID = "164683223557190";
	public static final String[] FACEBOOK_PERMISSIONS = new String[] { "publish_stream, email" };
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

	public static final String[] BEERS = new String[] {};
	public static final String[] STYLES = new String[] { "Banana beer",
			"Abbey Dubbel", "Mild Ale", "Sato", "Saké-Daiginjo", "Amber ale",
			"Dubbel", "Baltic porter", "Pale ale", "Sahti", "Imperial Porter",
			"Saké-Ginjo", "Belgian White", "Mbege", "Pilsener", "Dunkel",
			"American Strong Ale", "Brem", "Golden ale", "High gravity beer",
			"Lambic-Unblended", "Helles", "Malt Liquor", "Black IPA",
			"Draught beer", "Kolsch", "Sour beer", "Shandy", " Abbey Tripel",
			"German Hefeweizen", "Smoked", "Scotch ale", "Bappir", "Kriek",
			"Steinbier", "Imperial Stout", "Light ale", "Belgian Strong Ale",
			"Sake-Honjozo", "Old Ale", "Vegetable", "Abt", "Brown ale",
			"Malt beverage", "Malt beer", "Brown Ale", "Saké-Honjozo",
			"Roggenbier", "Choujiu", "American pale ale", "Chuak",
			"Scotch Ale", "Ice Cider", "Sake-Daiginjo", "Ibwatu",
			"Baltic Porter", "Cuirm", "Huangjiu", "Dortmunder",
			"English Strong Ale", "Pilsner", "Lambic", "Saké-Infused",
			"Millet beer", "Amber Ale", "Sake-Tokubetsu", "Tongba",
			"Imperial Pils", "Weissbier", "Imperial IPA", "Specialty Grain",
			"Gluten-free beer", "Biere de Garde", "California Common",
			"Vienna", "Porter", "Pito", "Saké-Taru", "Smoked beer", "Old ale",
			"Happoshu", "Heller Bock", "Sweet Stout",
			"Classic German Pilsener", "Abbey Tripel", "Kölsch", "Wheat beer",
			"Marzen", "Strong Pale Lager", "Tiswin", "English Pale Ale",
			"Mead", "Herb", "American Strong Ale ", "Lambic-Faro", "Dry Stout",
			"Cream ale", "Altbier", "Fruit Beer", "Bitter", "Cider",
			"Sake-Genshu", "Strong Porter", "Traditional Ale", "Tella",
			"Märzen", "Doppelbock", "Dunkler Bock", "German Kristallweizen",
			"Low-alcohol beer", "Saké-Namasaké", "Eisbock", "Sake-Namasake",
			"Premium Bitter", "Irish red ale", "Lambic-Gueuze", "Malzbier",
			"American Dark Lager", "Sour Ale", "Ice beer", "Barley Wine",
			"American wild ale", "India Pale Ale", "Abbey", "Perry", "Radler",
			"Kentucky Common Beer", "Bohemian Pilsener", "Cream Ale", "Saison",
			"Vienna lager", "American-style lager", "Schwarzbier", "Irish Ale",
			"Keller", "Stout", "Sake", "Sake-Ginjo", "Saké-Nigori",
			"Blonde ale", "Framboise", "Spice", "Barley wine", "Wheat Ale",
			"Dortmunder Export", "Pale lager", "Quadrupel", "Golden Ale",
			"Cauim", "Lambic-Fruit", "Landbier", "Sake-Infused", "Sake-Taru",
			"Pale Lager", "Saké-Futsu-shu", "Oshikundu", "Kellerbier",
			"Oud bruin", "Zozu", "Sake-Futsu-shu", "Zutho", "Tripel", "Ale",
			"Witbier", "American lager", "Steam beer", "Oktoberfest", "ESB",
			"Sake-Junmai", "Lager", "Dunkelweizen", "American Pale Ale",
			"Rye beer", "Chhaang", "Mild ale", "Weizen Bock", "Belgian Dubbel",
			"Foreign Stout", "Gueuze", "Double IPA", "Chicha",
			"Belgian Trippel", "Berliner Weisse", "Gruit", "Flanders red ale",
			"Wild Ale", "Brunswick Mum", "Boza", "Gose", "Scottish Ale",
			"Malt liquor", "Saké-Genshu", "Bière de Garde", "Premium Lager",
			"Bock", "Sake-Koshu", "Saké-Tokubetsu", "Belgian Ale",
			"Sake-Nigori", "Saké-Junmai", "Saké-Koshu", "Blond Ale", "Zwickel" };
	public static final String[] COUNTRIES = new String[] { "Afghanistan",
			"Albania", "Algeria", "American Samoa", "Andorra", "Angola",
			"Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina",
			"Armenia", "Aruba", "Australia", "Austria", "Azerbaijan",
			"Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium",
			"Belize", "Benin", "Bermuda", "Bhutan", "Bolivia",
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
	public static final String[] STATES = new String[] {};

}
