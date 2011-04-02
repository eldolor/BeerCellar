package com.cm.beer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.cm.beer.activity.NotificationService;
import com.cm.beer.activity.R;
import com.cm.beer.config.AppConfig;
import com.cm.beer.db.Note;
import com.cm.beer.db.NotesDbAdapter;
import com.cm.beer.transfer.Beer;
import com.facebook.android.Facebook;
import com.google.ads.AdSenseSpec;
import com.google.ads.GoogleAdView;
import com.google.ads.AdSenseSpec.AdType;
import com.google.ads.AdSenseSpec.ExpandDirection;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class Util {
	private static String TAG = Util.class.getName();

	public static void setGoogleAdSense(final Activity activity,
			String channelId, GoogleAdView adView) {
		if (AppConfig.IS_BEER_LITE) {
			// Set up GoogleAdView.
			AdSenseSpec adSenseSpec = new AdSenseSpec(AppConfig.CLIENT_ID) // Specify
					// client
					// ID.
					// (Required)
					.setCompanyName(AppConfig.COMPANY_NAME) // Set company name.
					// (Required)
					.setAppName(AppConfig.APP_NAME) // Set application name.
					// (Required)
					.setKeywords(AppConfig.KEYWORDS) // Specify keywords.
					.setChannel(channelId) // Set channel
					// ID.
					.setAdType(AdType.TEXT_IMAGE) // Set ad type .
					.setAdTestEnabled(AppConfig.AD_TEST_ENABLED) // Keep true
					// while
					.setExpandDirection(ExpandDirection.TOP); // we placed our
			// ad at
			// the bottom and
			// expected it to
			// expand upwards
			// testing.

			// If application content is equivalent to an existing website URL
			// then
			// set:
			// adSenseSpec.setWebEquivalentUrl(url) to improve ad targeting.

			// Fetch Google ad.
			// PLEASE DO NOT CLICK ON THE AD UNLESS YOU ARE IN TEST MODE.
			// OTHERWISE, YOUR ACCOUNT MAY BE DISABLED.
			if (adView != null) {
				adView.showAds(adSenseSpec);
			}
		}

	}

	public static int getRandomInt(int aStart, int aEnd) {
		// if (aStart > aEnd) {
		// throw new IllegalArgumentException("Start cannot exceed End.");
		// }
		Random aRandom = new Random();
		// get the range, casting to long to avoid overflow problems
		long range = (long) aEnd - (long) aStart + 1;
		// compute a fraction of the range, 0 <= frac < range
		long fraction = (long) (range * aRandom.nextDouble());
		int randomNumber = (int) (fraction + aStart);
		return randomNumber;
	}

	/**
	 * 
	 * @return
	 */
	public static JSONArray getBeers(NotesDbAdapter mDbHelper) {

		JSONArray jsonArray = null;
		Cursor cursor = null;
		Beer beer = null;
		ArrayList<Beer> beerList = new ArrayList<Beer>();
		try {
			String _currencySymbol = Currency.getInstance(Locale.getDefault())
					.getSymbol();

			cursor = mDbHelper.fetchAllNotesData();
			cursor.moveToFirst();

			while (cursor.isAfterLast() == false) {
				beer = new Beer();
				beer.setId(cursor.getLong(cursor
						.getColumnIndex(NotesDbAdapter.KEY_ROWID)));
				beer.setBeer(cursor.getString(cursor
						.getColumnIndex(NotesDbAdapter.KEY_BEER)));
				beer.setAlcohol(cursor.getString(cursor
						.getColumnIndex(NotesDbAdapter.KEY_ALCOHOL)));
				beer.setCurrency(_currencySymbol);
				beer.setPrice(cursor.getString(cursor
						.getColumnIndex(NotesDbAdapter.KEY_PRICE)));
				beer.setStyle(cursor.getString(cursor
						.getColumnIndex(NotesDbAdapter.KEY_STYLE)));
				beer.setBrewery(cursor.getString(cursor
						.getColumnIndex(NotesDbAdapter.KEY_BREWERY)));
				beer.setState(cursor.getString(cursor
						.getColumnIndex(NotesDbAdapter.KEY_STATE)));
				beer.setCountry(cursor.getString(cursor
						.getColumnIndex(NotesDbAdapter.KEY_COUNTRY)));
				beer.setRating(cursor.getString(cursor
						.getColumnIndex(NotesDbAdapter.KEY_RATING)));
				beer.setNotes(cursor.getString(cursor
						.getColumnIndex(NotesDbAdapter.KEY_NOTES)));
				beer.setPicture(cursor.getString(cursor
						.getColumnIndex(NotesDbAdapter.KEY_PICTURE)));
				beer.setCreated(cursor.getLong(cursor
						.getColumnIndex(NotesDbAdapter.KEY_CREATED)));
				beer.setUpdated(cursor.getLong(cursor
						.getColumnIndex(NotesDbAdapter.KEY_UPDATED)));
				beer.setUpdated(cursor.getLong(cursor
						.getColumnIndex(NotesDbAdapter.KEY_SHARE)));

				beerList.add(beer);
			}

			jsonArray = new JSONArray(beerList);

		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return jsonArray;
	}

	public static JSONObject toBeerJson(Note note) throws JSONException {
		JSONObject json = null;
		// These fields can be null
		String beer, alcohol, price, style, brewery, state, country, rating, notes, picture, shared, latitude, longitude, userId, userName, userLink, characteristics, breweryLink;
		String _currencySymbol = Currency.getInstance(Locale.getDefault())
				.getSymbol();
		String _timeZone = String.valueOf(TimeZone.getDefault().getRawOffset());

		json = new JSONObject();

		if (note != null) {
			json.put("id", note.id);
			beer = ((note.beer != null) && (!note.beer.equals(""))) ? note.beer
					.trim() : "";
			json.put("beer", beer);

			alcohol = ((note.alcohol != null) && (!note.alcohol.equals(""))) ? note.alcohol
					.trim()
					: "";
			json.put("alcohol", alcohol);

			json.put("currency", _currencySymbol);

			price = ((note.price != null) && (!note.price.equals(""))) ? note.price
					.trim()
					: "";
			json.put("price", price);

			style = ((note.style != null) && (!note.style.equals(""))) ? note.style
					.trim()
					: "";
			json.put("style", style);

			brewery = ((note.brewery != null) && (!note.brewery.equals(""))) ? note.brewery
					.trim()
					: "";
			json.put("brewery", brewery);

			state = ((note.state != null) && (!note.state.equals(""))) ? note.state
					.trim()
					: "";
			json.put("state", state);

			country = ((note.country != null) && (!note.country.equals(""))) ? note.country
					.trim()
					: "";
			json.put("country", country);

			rating = ((note.rating != null) && (!note.rating.equals(""))) ? note.rating
					.trim()
					: "0.0";
			json.put("rating", rating);

			notes = ((note.notes != null) && (!note.notes.equals(""))) ? note.notes
					.trim()
					: "";
			json.put("notes", notes);

			picture = ((note.picture != null) && (!note.picture.equals(""))) ? note.picture
					.trim()
					: "";
			json.put("picture", picture);

			json.put("created", note.created);
			json.put("updated", note.updated);

			shared = note.share;
			String isShareChecked = ((shared != null) && (shared.equals("Y"))) ? "Y"
					: "N";
			json.put("share", isShareChecked);

			json.put("timeZone", _timeZone);

			latitude = note.latitude;
			latitude = ((latitude != null) && (!latitude.equals("0.0"))) ? latitude
					.trim()
					: "0.0";
			json.put("latitude", latitude);

			longitude = note.longitude;
			longitude = ((longitude != null) && (!longitude.equals("0.0"))) ? longitude
					.trim()
					: "0.0";
			json.put("longitude", longitude);

			userId = ((note.userId != null) && (!note.userId.equals(""))) ? note.userId
					.trim()
					: "";
			json.put("userId", userId);

			userName = ((note.userName != null) && (!note.userName.equals(""))) ? note.userName
					.trim()
					: "";
			json.put("userName", userName);

			userLink = ((note.userLink != null) && (!note.userLink.equals(""))) ? note.userLink
					.trim()
					: "";
			json.put("userLink", userLink);
			characteristics = ((note.characteristics != null) && (!note.characteristics
					.equals(""))) ? note.characteristics.trim() : "";
			json.put("characteristics", characteristics);
			breweryLink = ((note.breweryLink != null) && (!note.breweryLink
					.equals(""))) ? note.breweryLink.trim() : "";
			json.put("breweryLink", breweryLink);

		}

		return json;
	}

	/**
	 * Will return null if the cursor is null
	 * 
	 * @param cursor
	 * @return
	 * @throws JSONException
	 */
	public static Note toNote(Cursor cursor) {
		// These fields can be null
		String shared, latitude, longitude;

		Note note = null;

		if (cursor != null && (!cursor.isAfterLast())) {
			note = new Note();
			note.id = cursor.getLong(cursor
					.getColumnIndex(NotesDbAdapter.KEY_ROWID));
			note.beer = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_BEER));

			note.alcohol = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_ALCOHOL));

			note.price = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_PRICE));

			note.style = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_STYLE));

			note.brewery = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_BREWERY));

			note.state = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_STATE));

			note.country = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_COUNTRY));

			note.rating = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_RATING));

			note.notes = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_NOTES));

			note.picture = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_PICTURE));

			note.created = cursor.getLong(cursor
					.getColumnIndex(NotesDbAdapter.KEY_CREATED));
			note.updated = cursor.getLong(cursor
					.getColumnIndex(NotesDbAdapter.KEY_UPDATED));

			shared = cursor.getString(cursor
					.getColumnIndexOrThrow(NotesDbAdapter.KEY_SHARE));
			String isShareChecked = ((shared != null) && (shared.equals("Y"))) ? "Y"
					: "N";
			note.share = isShareChecked;

			latitude = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_LATITUDE));
			latitude = ((latitude != null) && (!latitude.equals("0.0"))) ? latitude
					: "0.0";
			note.latitude = latitude;

			longitude = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_LONGITUDE));
			longitude = ((longitude != null) && (!longitude.equals("0.0"))) ? longitude
					: "0.0";
			note.longitude = longitude;
			note.userId = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_USER_ID));
			note.userName = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_USER_NAME));
			note.userLink = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_USER_LINK));
			note.characteristics = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_CHARACTERISTICS));
			note.breweryLink = cursor.getString(cursor
					.getColumnIndex(NotesDbAdapter.KEY_BREWERY_LINK));

		}

		return note;
	}

	/**
	 * 
	 * @param url
	 * @param method
	 * @param params
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String openUrl(String url, String method,
			Map<String, String> params) throws MalformedURLException,
			IOException {

		String charset = "UTF-8";
		Log.d(TAG, method + " URL: " + url);

		if (method.equals("GET")) {
			String encodedUrl = encodeUrl(params);
			Log.d(TAG, "Encoded URL:" + encodedUrl);
			url = url + "?" + encodedUrl;
			// } else if (params.containsKey("file")) {
			// // multipart/form-data: Add access_token to the URL
			// url = url + "?"
			// + (Facebook.TOKEN + "=" + params.getString(Facebook.TOKEN));
		}
		HttpURLConnection conn = (HttpURLConnection) new URL(url)
				.openConnection();
		conn.setRequestProperty("User-Agent", System.getProperties()
				.getProperty("http.agent")
				+ " Android");

		if (!method.equals("GET")) {
			if (!params.containsKey("file")) {
				// use method override
				String encodedUrl = encodeUrl(params);
				Log.d(TAG, "Encoded URL:" + encodedUrl);
				params.put("method", method);
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.getOutputStream().write(encodedUrl.getBytes(charset));
			} else {
				Log.d(TAG, "Posting multipart/form-data");

				Log.d(TAG, method + " URL: " + url);

				String CRLF = "\r\n";
				String TWO_HYPHENS = "--";
				String BOUNDARY = Long.toHexString(System.currentTimeMillis());

				OutputStream output = null;

				// use method override
				conn.setRequestMethod("POST");
				// Allow Inputs
				conn.setDoInput(true);
				// Allow Outputs
				conn.setDoOutput(true);
				// Don't use a cached copy.
				conn.setUseCaches(false);
				// send the body in chunks of 1KB
				conn.setChunkedStreamingMode(1024);
				conn.setRequestProperty("Content-Type",
						"multipart/form-data; boundary=\"" + BOUNDARY + "\"");
				try {
					output = conn.getOutputStream();
					// write other parameters
					for (String key : params.keySet()) {
						String value = params.get(key);

						output.write((CRLF + TWO_HYPHENS + BOUNDARY + CRLF)
								.getBytes(charset));

						if (key.equalsIgnoreCase("file")) {
							Log.d(TAG, "POSTING: " + key + "=" + value);
							File photo = new File(value);
							output
									.write(("Content-Disposition: form-data; name=\"file\"; filename=\""
											+ photo.getName() + "\"" + CRLF)
											.getBytes(charset));
							output
									.write(("Content-Type: image/jpg" + CRLF + CRLF)
											.getBytes(charset));

							FileInputStream input = null;
							try {
								input = new FileInputStream(photo);
								int byteCount = 0;
								byte[] buffer = new byte[1024];
								for (int length = 0; (length = input
										.read(buffer)) > 0;) {
									output.write(buffer, 0, length);
									byteCount += length;
								}
								output.flush();
								Log.d(TAG, byteCount + " bytes flushed for "
										+ photo.getName());
							} finally {
								if (input != null)
									try {
										input.close();
									} catch (IOException e) {
										Log.e(TAG, (e.getMessage() != null) ? e
												.getMessage().replace(" ", "_")
												: "", e);
									}
							}
							// Important! Indicates end of binary BOUNDARY.
							output.write(CRLF.getBytes(charset));
						} else {
							// Send normal param. Access_Token is included in
							// the GET String
							if (!key.equalsIgnoreCase(Facebook.TOKEN)) {
								Log.d(TAG, "POSTING: " + key + "=" + value);
								output
										.write(("Content-Disposition: form-data; name=\""
												+ key + "\"" + CRLF + CRLF)
												.getBytes(charset));
								output.write(value.getBytes(charset));
								// Important! Indicates end of text BOUNDARY.
								output.write(CRLF.getBytes(charset));
							}
						}

					}// end for

					// End of multipart/form-data.
					output.write((CRLF + TWO_HYPHENS + BOUNDARY + TWO_HYPHENS)
							.getBytes(charset));

				} finally {
					if (output != null)
						output.close();
				}
			}// end else if (!params.containsKey("file"))
		}// end if (!method.equals("GET"))

		String response = "";

		try {
			response = read(conn.getInputStream());
		} catch (FileNotFoundException e) {
			// Error Stream contains JSON that we can parse to a FB error
			response = read(conn.getErrorStream());
		}
		return response;
	}

	/**
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private static String read(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line);
		}
		in.close();
		return sb.toString();
	}

	/**
	 * 
	 * @param parameters
	 * @return
	 */
	public static String encodeUrl(Map<String, String> parameters) {
		if (parameters == null)
			return "";
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String key : parameters.keySet()) {
			if (first)
				first = false;
			else
				sb.append("&");
			sb.append(key + "=" + parameters.get(key));
		}
		return sb.toString();
	}

	/**
	 * Location Setup
	 */
	public static GpsLocation getLocation(Activity activity,
			GoogleAnalyticsTracker mTracker) {
		String location_context = Context.LOCATION_SERVICE;
		LocationManager locationManager = (LocationManager) activity
				.getSystemService(location_context);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setCostAllowed(true);
		GpsLocation gpsLocation = new GpsLocation();

		try {
			String bestProvider = locationManager.getBestProvider(criteria,
					true);

			Location location = locationManager
					.getLastKnownLocation(bestProvider);
			if (location != null) {
				double latitude = location.getLatitude();
				double longitude = location.getLongitude();
				Log.i(TAG, "Setting latitude:" + latitude + " longitude:"
						+ longitude);
				gpsLocation.latitude = latitude;
				gpsLocation.longitude = longitude;
			}
		} catch (Throwable e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
			mTracker.trackEvent("Util", "GetGpsLocation",
					(e.getMessage() != null) ? e.getMessage().replace(" ", "_")
							: "".replace(" ", "_"), 0);
			mTracker.dispatch();
		}
		return gpsLocation;
	}

	/**
	 * 
	 * @param db
	 */
	public static void onUpgradeToV3FromV1(SQLiteDatabase db) {
		Cursor cursor = null;
		ContentValues initialValues = null;
		int records = 0;
		try {
			// extract data from old table
			cursor = db.query(NotesDbAdapter.DATABASE_TABLE_V1, new String[] {
					NotesDbAdapter.KEY_ROWID, NotesDbAdapter.KEY_BEER,
					NotesDbAdapter.KEY_ALCOHOL, NotesDbAdapter.KEY_PRICE,
					NotesDbAdapter.KEY_STYLE, NotesDbAdapter.KEY_BREWERY,
					NotesDbAdapter.KEY_STATE, NotesDbAdapter.KEY_COUNTRY,
					NotesDbAdapter.KEY_RATING, NotesDbAdapter.KEY_NOTES,
					NotesDbAdapter.KEY_PICTURE, NotesDbAdapter.KEY_CREATED,
					NotesDbAdapter.KEY_UPDATED }, null, null, null, null,
					NotesDbAdapter.KEY_UPDATED + " DESC");
			if (cursor != null) {
				cursor.moveToFirst();
			}

			// insert data into the new table
			for (; (cursor != null) && (cursor.isAfterLast() == false); cursor
					.moveToNext(), records++) {
				initialValues = new ContentValues();
				initialValues.put(NotesDbAdapter.KEY_ROWID, cursor
						.getLong(cursor
								.getColumnIndex(NotesDbAdapter.KEY_ROWID)));
				initialValues.put(NotesDbAdapter.KEY_BEER, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_BEER)));
				initialValues.put(NotesDbAdapter.KEY_ALCOHOL, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_ALCOHOL)));
				initialValues.put(NotesDbAdapter.KEY_PRICE, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_PRICE)));
				initialValues.put(NotesDbAdapter.KEY_STYLE, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_STYLE)));
				initialValues.put(NotesDbAdapter.KEY_BREWERY, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_BREWERY)));
				initialValues.put(NotesDbAdapter.KEY_STATE, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_STATE)));
				initialValues.put(NotesDbAdapter.KEY_COUNTRY, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_COUNTRY)));
				initialValues.put(NotesDbAdapter.KEY_RATING, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_RATING)));
				initialValues.put(NotesDbAdapter.KEY_NOTES, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_NOTES)));
				initialValues.put(NotesDbAdapter.KEY_PICTURE, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_PICTURE)));
				initialValues.put(NotesDbAdapter.KEY_CREATED, cursor
						.getLong(cursor
								.getColumnIndex(NotesDbAdapter.KEY_CREATED)));
				initialValues.put(NotesDbAdapter.KEY_UPDATED, cursor
						.getLong(cursor
								.getColumnIndex(NotesDbAdapter.KEY_UPDATED)));

				db.insert(NotesDbAdapter.DATABASE_TABLE, null, initialValues);
				Log.i(TAG, "Inserted "
						+ initialValues.getAsString(NotesDbAdapter.KEY_BEER)
						+ " from " + NotesDbAdapter.DATABASE_TABLE_V1 + " to "
						+ NotesDbAdapter.DATABASE_TABLE);
			}
			Log.i(TAG, "Records Inserted: " + records);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

	}

	/**
	 * 
	 * @param db
	 */
	public static void onUpgradeToV3FromV2(SQLiteDatabase db) {
		Cursor cursor = null;
		ContentValues initialValues = null;
		int records = 0;
		try {
			// extract data from old table
			cursor = db.query(NotesDbAdapter.DATABASE_TABLE_V2, new String[] {
					NotesDbAdapter.KEY_ROWID, NotesDbAdapter.KEY_BEER,
					NotesDbAdapter.KEY_ALCOHOL, NotesDbAdapter.KEY_PRICE,
					NotesDbAdapter.KEY_STYLE, NotesDbAdapter.KEY_BREWERY,
					NotesDbAdapter.KEY_STATE, NotesDbAdapter.KEY_COUNTRY,
					NotesDbAdapter.KEY_RATING, NotesDbAdapter.KEY_NOTES,
					NotesDbAdapter.KEY_PICTURE, NotesDbAdapter.KEY_SHARE,
					NotesDbAdapter.KEY_LATITUDE, NotesDbAdapter.KEY_LONGITUDE,
					NotesDbAdapter.KEY_USER_ID, NotesDbAdapter.KEY_USER_NAME,
					NotesDbAdapter.KEY_USER_LINK,
					NotesDbAdapter.KEY_CHARACTERISTICS,
					NotesDbAdapter.KEY_CREATED, NotesDbAdapter.KEY_UPDATED },
					null, null, null, null, NotesDbAdapter.KEY_UPDATED
							+ " DESC");

			if (cursor != null) {
				cursor.moveToFirst();
			}

			// insert data into the new table
			for (; (cursor != null) && (cursor.isAfterLast() == false); cursor
					.moveToNext(), records++) {
				initialValues = new ContentValues();
				initialValues.put(NotesDbAdapter.KEY_ROWID, cursor
						.getLong(cursor
								.getColumnIndex(NotesDbAdapter.KEY_ROWID)));
				initialValues.put(NotesDbAdapter.KEY_BEER, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_BEER)));
				initialValues.put(NotesDbAdapter.KEY_ALCOHOL, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_ALCOHOL)));
				initialValues.put(NotesDbAdapter.KEY_PRICE, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_PRICE)));
				initialValues.put(NotesDbAdapter.KEY_STYLE, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_STYLE)));
				initialValues.put(NotesDbAdapter.KEY_BREWERY, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_BREWERY)));
				initialValues.put(NotesDbAdapter.KEY_STATE, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_STATE)));
				initialValues.put(NotesDbAdapter.KEY_COUNTRY, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_COUNTRY)));
				initialValues.put(NotesDbAdapter.KEY_RATING, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_RATING)));
				initialValues.put(NotesDbAdapter.KEY_NOTES, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_NOTES)));
				initialValues.put(NotesDbAdapter.KEY_PICTURE, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_PICTURE)));
				initialValues.put(NotesDbAdapter.KEY_SHARE, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_SHARE)));
				initialValues.put(NotesDbAdapter.KEY_LATITUDE, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_LATITUDE)));
				initialValues.put(NotesDbAdapter.KEY_LONGITUDE, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_LONGITUDE)));
				initialValues.put(NotesDbAdapter.KEY_USER_ID, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_USER_ID)));
				initialValues.put(NotesDbAdapter.KEY_USER_LINK, cursor
						.getString(cursor
								.getColumnIndex(NotesDbAdapter.KEY_USER_LINK)));
				initialValues
						.put(
								NotesDbAdapter.KEY_CHARACTERISTICS,
								cursor
										.getString(cursor
												.getColumnIndex(NotesDbAdapter.KEY_CHARACTERISTICS)));

				initialValues.put(NotesDbAdapter.KEY_CREATED, cursor
						.getLong(cursor
								.getColumnIndex(NotesDbAdapter.KEY_CREATED)));
				initialValues.put(NotesDbAdapter.KEY_UPDATED, cursor
						.getLong(cursor
								.getColumnIndex(NotesDbAdapter.KEY_UPDATED)));

				db.insert(NotesDbAdapter.DATABASE_TABLE, null, initialValues);
				Log.i(TAG, "Inserted "
						+ initialValues.getAsString(NotesDbAdapter.KEY_BEER)
						+ " from " + NotesDbAdapter.DATABASE_TABLE_V2 + " to "
						+ NotesDbAdapter.DATABASE_TABLE);
			}
			Log.i(TAG, "Records Inserted: " + records);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

	}

	/**
	 * Emulates Facebook Login
	 * 
	 * @param activity
	 */
	public static void emulateLogin(final Activity activity) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
		CharSequence[] items = new CharSequence[AppConfig.EMULATED_USER_NAME.length];
		for (int i = 0; i < AppConfig.EMULATED_USER_NAME.length; i++) {
			items[i] = AppConfig.EMULATED_USER_NAME[i];
		}
		dialog.setItems(items, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int which) {
				try {
					User user = new User(activity);
					user.onAuthSucceed(AppConfig.EMULATED_USER_ID[which],
							AppConfig.EMULATED_USER_NAME[which],
							AppConfig.EMULATED_USER_LINK,
							AppConfig.EMULATED_USER_TYPE);
					JSONObject additionalAttributes = new JSONObject();
					additionalAttributes.put("email",
							AppConfig.EMULATED_USER_EMAIL);
					user.setAdditionalUserAttributes(additionalAttributes
							.toString());

					JSONObject json = new JSONObject();
					json.put("userId", user.getUserId());
					json.put("userName", user.getUserName());
					json.put("userLink", user.getUserLink());
					json.put("additionalAttributes", user
							.getAdditionalUserAttributes());

					String userJsonStr = URLEncoder.encode(json.toString(),
							"UTF-8");
					Log.i(TAG, userJsonStr);

					HashMap<String, String> parameters = new HashMap<String, String>();
					parameters.put("userprofile", userJsonStr);

					// Prepare a request object
					String _url = com.cm.beer.util.Util
							.getUploadUserProfileUrl();
					Log.i(TAG, _url);
					{
						boolean retry = true;
						int retryCount = 0;
						while ((retry)
								&& (retryCount < AppConfig.SHARE_WITH_COMMUNITY_BEER_UPLOAD_RETRY_COUNT)) {
							try {

								String response = com.cm.beer.util.Util
										.openUrl(_url, "POST", parameters);
								// Examine the response status
								Log.i(TAG, "Response = " + response);
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
					}

				} catch (Throwable e) {
					Log.e(TAG, "error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);
				}

			}
		});
		dialog.create();
		dialog.show();

	}

	/**
	 * Emulates Facebook Login
	 * 
	 * @param activity
	 */
	public static void emulateLogout(Activity activity) {
		User user = new User(activity);
		user.onLogoutFinish();
	}

	/**
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String convertStreamToString(InputStream is, String charset)
			throws IOException {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader;
		StringBuilder sb = new StringBuilder();

		String line = null;
		reader = new BufferedReader(new InputStreamReader(is, charset));
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static String[] getResult(String url)
			throws ClientProtocolException, IOException {

		InputStream _inStream = null;
		HttpResponse _response;
		// cs cannot be null
		String cs = "";
		String result = null;
		String response[] = new String[2];

		try {
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is
			// established.
			// Set the timeout in milliseconds until a connection is
			// established.
			HttpConnectionParams.setConnectionTimeout(httpParameters,
					AppConfig.HTTP_CONNECTION_TIMEOUT);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters,
					AppConfig.HTTP_SOCKET_TIMEOUT);

			HttpClient httpClient = new DefaultHttpClient(httpParameters);
			// Prepare a request object
			HttpGet httpget = new HttpGet(url);

			// Execute the request
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, url);
			}
			_response = httpClient.execute(httpget);
			// Examine the response status
			if (AppConfig.LOGGING_ENABLED) {
				Log.i(TAG, _response.getStatusLine().toString());
			}

			// Get hold of the response entity
			HttpEntity _entity = _response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (_entity != null) {
				// Get cursor string
				// NOTE: cs cannot be null
				cs = (_response.getLastHeader("cs") != null) ? _response
						.getLastHeader("cs").getValue() : "";
				Log.i(TAG, "Response cs = " + cs);
				// A Simple JSON Response Read
				_inStream = _entity.getContent();
				result = Util.convertStreamToString(_inStream, "UTF-8");
				Log.i(TAG, "Response = " + result);
			}

		} finally {
			if (_inStream != null) {
				// Closing the input stream will trigger connection release
				try {
					_inStream.close();
				} catch (IOException e) {
					Log.e(TAG, "error: "
							+ ((e.getMessage() != null) ? e.getMessage()
									.replace(" ", "_") : ""), e);
				}
			}
		}

		response[0] = result;
		response[1] = cs;
		return response;
	}

	public static String getSetReviewHelpfulUrl(String userId, String beerId,
			String raterId, String helpful) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_SET_REVIEW_HELPFUL_Q
					+ AppConfig.COMMUNITY_USERID_PARAM
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "")
					+ AppConfig.COMMUNITY_BEERID_PARAM
					+ ((beerId != null) ? URLEncoder.encode(beerId, "UTF-8")
							: "")
					+ AppConfig.COMMUNITY_RATERID_PARAM
					+ ((raterId != null) ? URLEncoder.encode(raterId, "UTF-8")
							: "") + AppConfig.COMMUNITY_REVIEW_HELPFUL_PARAM
					+ helpful;
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;
	}

	public static String getSetUnfollowUrl(String userId, String userName,
			String userLink, String followUserId, String followUserName,
			String followUserLink) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_SET_UNFOLLOW_Q
					+ AppConfig.COMMUNITY_USERID_PARAM
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "")
					+ AppConfig.COMMUNITY_USER_NAME_PARAM
					+ ((userName != null) ? URLEncoder
							.encode(userName, "UTF-8") : "")
					+ AppConfig.COMMUNITY_USER_LINK_PARAM
					+ ((userLink != null) ? URLEncoder
							.encode(userLink, "UTF-8") : "")
					+ AppConfig.COMMUNITY_FOLLOW_USER_ID_PARAM
					+ ((followUserId != null) ? URLEncoder.encode(followUserId,
							"UTF-8") : "")
					+ AppConfig.COMMUNITY_FOLLOW_USER_NAME_PARAM
					+ ((followUserName != null) ? URLEncoder.encode(
							followUserName, "UTF-8") : "")
					+ AppConfig.COMMUNITY_FOLLOW_USER_LINK_PARAM
					+ ((followUserLink != null) ? URLEncoder.encode(
							followUserLink, "UTF-8") : "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getSetFollowUrl(String userId, String userName,
			String userLink, String followUserId, String followUserName,
			String followUserLink) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_SET_FOLLOW_Q
					+ AppConfig.COMMUNITY_USERID_PARAM
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "")
					+ AppConfig.COMMUNITY_USER_NAME_PARAM
					+ ((userName != null) ? URLEncoder
							.encode(userName, "UTF-8") : "")
					+ AppConfig.COMMUNITY_USER_LINK_PARAM
					+ ((userLink != null) ? URLEncoder
							.encode(userLink, "UTF-8") : "")
					+ AppConfig.COMMUNITY_FOLLOW_USER_ID_PARAM
					+ ((followUserId != null) ? URLEncoder.encode(followUserId,
							"UTF-8") : "")
					+ AppConfig.COMMUNITY_FOLLOW_USER_NAME_PARAM
					+ ((followUserName != null) ? URLEncoder.encode(
							followUserName, "UTF-8") : "")
					+ AppConfig.COMMUNITY_FOLLOW_USER_LINK_PARAM
					+ ((followUserLink != null) ? URLEncoder.encode(
							followUserLink, "UTF-8") : "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getFollowUrl(String userId) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_GET_FOLLOW_Q
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getReviewHelpfulCountUrl(String beerId) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_GET_REVIEW_HELPFUL_COUNT_Q
					+ ((beerId != null) ? URLEncoder.encode(beerId, "UTF-8")
							: "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getFollowCountUrl(String userId) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_GET_FOLLOW_COUNT_Q
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getReviewCountUrl(String userId) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_GET_REVIEW_COUNT_Q
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getImageUrl(String beerId) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_GET_IMAGE_Q
					+ ((beerId != null) ? URLEncoder.encode(beerId, "UTF-8")
							: "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getUploadUserPhotoURLUrl() {
		return AppConfig.COMMUNITY_GET_USER_SERVICE_URL
				+ AppConfig.COMMUNITY_GET_UPLOAD_USER_PHOTO_URL_Q;
	}

	public static String getUserPhotoUrl(String userId) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_USER_SERVICE_URL
					+ AppConfig.COMMUNITY_GET_USER_PHOTO_Q
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getNewBeerReviewsNotificationUrl(String lastChecked) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_GET_NEW_BEER_REVIEWS_NOTIFICATION_Q
					+ AppConfig.COMMUNITY_LAST_CHECKED_PARAM
					+ ((lastChecked != null) ? URLEncoder.encode(lastChecked,
							"UTF-8") : "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getNewBeerReviewsFromFollowingNotificationUrl(
			String userId, String lastChecked) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_GET_NEW_BEER_REVIEWS_FROM_FOLLOWING_NOTIFICATION_Q
					+ AppConfig.COMMUNITY_USERID_PARAM
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "")
					+ AppConfig.COMMUNITY_LAST_CHECKED_PARAM
					+ ((lastChecked != null) ? URLEncoder.encode(lastChecked,
							"UTF-8") : "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getBeerOfTheDayNotificationUrl(String userId,
			String lastChecked) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_GET_BEER_OF_THE_DAY_NOTIFICATION_Q
					+ AppConfig.COMMUNITY_USERID_PARAM
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "")
					+ AppConfig.COMMUNITY_LAST_CHECKED_PARAM
					+ ((lastChecked != null) ? URLEncoder.encode(lastChecked,
							"UTF-8") : "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;
	}

	public static String getUpdateCheckUrl(String currentVersion) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_UPDATE_CHECK_Q
					+ ((currentVersion != null) ? URLEncoder.encode(
							currentVersion, "UTF-8") : "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getEmailSubscriptionStatusUrl(String userId) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_GET_EMAIL_SUBSCRIPTION_STATUS_Q
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String updateEmailSubscriptionStatusUrl(String userId,
			String emailSubscriptionStatus) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_UPDATE_EMAIL_SUBSCRIPTION_STATUS_Q
					+ AppConfig.COMMUNITY_USERID_PARAM
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "")
					+ AppConfig.COMMUNITY_EMAIL_SUBSCRIPTION_PARAM
					+ ((emailSubscriptionStatus != null) ? URLEncoder.encode(
							emailSubscriptionStatus, "UTF-8") : "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return "";
	}

	public static String getAddToFavoritesUrl(String userId, String userName,
			String userLink, String beerId) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_ADD_TO_FAVORITES_Q
					+ AppConfig.COMMUNITY_USERID_PARAM
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "")
					+ AppConfig.COMMUNITY_USER_NAME_PARAM
					+ ((userName != null) ? URLEncoder
							.encode(userName, "UTF-8") : "")
					+ AppConfig.COMMUNITY_USER_LINK_PARAM
					+ ((userLink != null) ? URLEncoder
							.encode(userLink, "UTF-8") : "")
					+ AppConfig.COMMUNITY_BEERID_PARAM
					+ ((beerId != null) ? URLEncoder.encode(beerId, "UTF-8")
							: "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getRemoveFromFavoritesUrl(String userId,
			String userName, String userLink, String beerId) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_REMOVE_FROM_FAVORITES_Q
					+ AppConfig.COMMUNITY_USERID_PARAM
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "")
					+ AppConfig.COMMUNITY_USER_NAME_PARAM
					+ ((userName != null) ? URLEncoder
							.encode(userName, "UTF-8") : "")
					+ AppConfig.COMMUNITY_USER_LINK_PARAM
					+ ((userLink != null) ? URLEncoder
							.encode(userLink, "UTF-8") : "")
					+ AppConfig.COMMUNITY_BEERID_PARAM
					+ ((beerId != null) ? URLEncoder.encode(beerId, "UTF-8")
							: "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getFavoritesUrl(String userId) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_BEERS_URL
					+ AppConfig.COMMUNITY_GET_FAVORITES_Q
					+ AppConfig.COMMUNITY_USERID_PARAM
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	public static String getUploadUserProfileUrl() {
		return AppConfig.COMMUNITY_GET_USER_SERVICE_URL
				+ AppConfig.COMMUNITY_UPLOAD_USER_PROFILE_Q;
	}

	public static String getUserProfileUrl(String userId) {
		String url = null;
		try {
			url = AppConfig.COMMUNITY_GET_USER_SERVICE_URL
					+ AppConfig.COMMUNITY_GET_USER_PROFILE_Q
					+ AppConfig.COMMUNITY_USERID_PARAM
					+ ((userId != null) ? URLEncoder.encode(userId, "UTF-8")
							: "");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "error: "
					+ ((e.getMessage() != null) ? e.getMessage().replace(" ",
							"_") : ""), e);
		}
		return url;

	}

	/**
	 * 
	 */
	public static void evaluateNotificationService(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(context
				.getString(R.string.app_name), Activity.MODE_PRIVATE);
		// default all preferences to true
		if (!preferences
				.contains(AppConfig.RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS)) {
			preferences.edit().putBoolean(
					AppConfig.RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS, true)
					.commit();
		}
		if (!preferences
				.contains(AppConfig.RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS)) {
			preferences
					.edit()
					.putBoolean(
							AppConfig.RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS,
							true).commit();
		}
		if (!preferences
				.contains(AppConfig.RECEIVE_BEER_OF_THE_DAY_NOTIFICATION)) {
			preferences.edit().putBoolean(
					AppConfig.RECEIVE_BEER_OF_THE_DAY_NOTIFICATION, true)
					.commit();
		}

		// Start service only if any of the preferences permit
		if ((preferences.getBoolean(
				AppConfig.RECEIVE_NEW_BEER_REVIEW_NOTIFICATIONS, true))
				|| (preferences
						.getBoolean(
								AppConfig.RECEIVE_NEW_BEER_REVIEW_FROM_FOLLOWING_NOTIFICATIONS,
								true))
				|| (preferences.getBoolean(
						AppConfig.RECEIVE_BEER_OF_THE_DAY_NOTIFICATION, true))) {
			Log.i(TAG, "Starting Notification Service...");
			context
					.startService(new Intent(context, NotificationService.class));
			Log.i(TAG, "Notification Service Started!");
		} else {
			/** Stop the Notification Service **/
			Log.i(TAG, "Stopping Notification Service...");
			context.stopService(new Intent(context, NotificationService.class));
			Log.i(TAG, "Notification Service Stopped!");
		}
	}

	public static boolean isValidEmailAddress(String email) {
		if (email == null)
			return false;
		boolean result = true;
		if (!hasNameAndDomain(email)) {
			result = false;
		}
		return result;
	}

	private static boolean hasNameAndDomain(String email) {
		String[] tokens = email.split("@");
		if (tokens.length == 2) {
			return true;
		}
		return false;
	}

}
