package com.cm.beer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cm.beer.config.AppConfig;
import com.cm.beer.util.Logger;
import com.cm.beer.util.Util;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class NotesDbAdapter
{
	static String TAG = "Beer::NotesDbAdapter";

	public static final String KEY_ROWID = "_id";
	public static final String KEY_BEER = "beer";
	public static final String KEY_ALCOHOL = "alcohol";
	public static final String KEY_PRICE = "price";
	public static final String KEY_STYLE = "style";
	public static final String KEY_BREWERY = "brewery";
	public static final String KEY_STATE = "state";
	public static final String KEY_COUNTRY = "country";
	public static final String KEY_RATING = "rating";
	public static final String KEY_NOTES = "notes";
	public static final String KEY_PICTURE = "picture";
	public static final String KEY_CREATED = "created";
	public static final String KEY_UPDATED = "updated";

	/** NEW Columns for version 2 **/
	public static final String KEY_SHARE = "share";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_USER_ID = "userid";
	public static final String KEY_USER_NAME = "username";
	public static final String KEY_USER_LINK = "userlink";

	/** NEW Columns for version 2 **/
	public static final String KEY_CHARACTERISTICS = "characteristics";

	/** NEW Columns for version 3 **/
	public static final String KEY_BREWERY_LINK = "brewerylink";

	/** NEW Columns for version 4 **/
	public static final String KEY_CURRENCY_SYMBOL = "currencysymbol";
	public static final String KEY_CURRENCY_CODE = "currencycode";

	public static final String DATABASE_NAME = "beercellar_db.sqlite";
	public static final int DATABASE_VERSION = 4;
	public static final String DATABASE_TABLE = "beer_list_" + DATABASE_VERSION;
	public static final String DATABASE_TABLE_V1 = "beer_list_1";
	public static final String DATABASE_TABLE_V2 = "beer_list_2";
	public static final String DATABASE_TABLE_V3 = "beer_list_3";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE = "create table if not exists "
			+ DATABASE_TABLE + " (" + KEY_ROWID + " integer primary key,"
			+ KEY_BEER + " text default null," + KEY_ALCOHOL
			+ " text default null," + KEY_PRICE + " text default null,"
			+ KEY_STYLE + " text default null," + KEY_BREWERY
			+ " text default null," + KEY_STATE + " text default null,"
			+ KEY_COUNTRY + " text default null," + KEY_RATING
			+ " text default null," + KEY_NOTES + " text default null,"
			+ KEY_PICTURE + " text default null," + KEY_SHARE
			+ " text default null," + KEY_LATITUDE + " text default null,"
			+ KEY_LONGITUDE + " text default null," + KEY_USER_ID
			+ " text default null," + KEY_USER_NAME + " text default null,"
			+ KEY_USER_LINK + " text default null," + KEY_CHARACTERISTICS
			+ " text default null," + KEY_BREWERY_LINK + " text default null,"
			+ KEY_CURRENCY_SYMBOL + " text default null," + KEY_CURRENCY_CODE
			+ " text default null," + KEY_CREATED + " text default null,"
			+ KEY_UPDATED + " text default null);";

	private static final String INSERT_SEED_DATA_SQL = "Insert into "
			+ DATABASE_TABLE + "(" + KEY_ROWID + "," + KEY_BEER + ","
			+ KEY_ALCOHOL + "," + KEY_CURRENCY_SYMBOL + "," + KEY_CURRENCY_CODE
			+ "," + KEY_PRICE + "," + KEY_STYLE + "," + KEY_BREWERY + ","
			+ KEY_STATE + "," + KEY_COUNTRY + "," + KEY_RATING + ","
			+ KEY_PICTURE + "," + KEY_NOTES + "," + KEY_CREATED + ","
			+ KEY_UPDATED + ") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper
	{

		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);

			if (Logger.isLogEnabled())  Logger.log("DatabaseHelper");

		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			if (Logger.isLogEnabled())  Logger.log("DatabaseHelper::onCreate");
			db.execSQL(DATABASE_CREATE);
			if (Logger.isLogEnabled())  Logger.log("DatabaseHelper::onCreate: Database " + DATABASE_TABLE
					+ " created");
			db.execSQL(INSERT_SEED_DATA_SQL, AppConfig.SEED_DATA);
			if (Logger.isLogEnabled())  Logger.log("DatabaseHelper::onCreate: Seed Data Inserted");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			if (Logger.isLogEnabled())  Logger.log("DatabaseHelper::onUpgrade");
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion);
			db.execSQL(DATABASE_CREATE);
			if (Logger.isLogEnabled())  Logger.log("DatabaseHelper::onUpgrade: Database " + DATABASE_TABLE
					+ " created");
			if (oldVersion == 1)
			{
				Util.onUpgradeToV4FromV1(db);
			} else if (oldVersion == 2)
			{
				Util.onUpgradeToV4FromV2(db);
			} else if (oldVersion == 3)
			{
				Util.onUpgradeToV4FromV3(db);
			}
			if (Logger.isLogEnabled())  Logger.log("DatabaseHelper::onUpgrade: Data imported");
		}

	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public NotesDbAdapter(Context ctx)
	{
		if (Logger.isLogEnabled())  Logger.log("NotesDbAdapter");
		this.mCtx = ctx;
	}

	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public NotesDbAdapter open() throws SQLException
	{

		if (Logger.isLogEnabled())  Logger.log("open");

		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * 
	 * @return SQLiteDatabase
	 */
	public SQLiteDatabase getDatabase()
	{
		this.open();
		return this.mDb;
	}

	public void close()
	{

		if (Logger.isLogEnabled())  Logger.log("close");

		if (mDbHelper != null)
		{
			mDbHelper.close();
		}
	}

	/**
	 * Create a new note using the title and body provided. If the note is
	 * successfully created return the new rowId for that note, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @return rowId or -1 if failed
	 */
	public long createNote(Note note)
	{

		if (Logger.isLogEnabled())  Logger.log("createNote");

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_ROWID, note.id);
		initialValues.put(KEY_BEER, note.beer.trim());
		initialValues.put(KEY_ALCOHOL, note.alcohol.trim());
		initialValues.put(KEY_PRICE, note.price.trim());
		initialValues.put(KEY_STYLE, note.style.trim());
		initialValues.put(KEY_BREWERY, note.brewery.trim());
		initialValues.put(KEY_STATE, note.state.trim());
		initialValues.put(KEY_COUNTRY, note.country.trim());
		initialValues.put(KEY_RATING, note.rating.trim());
		initialValues.put(KEY_NOTES, note.notes.trim());
		initialValues.put(KEY_PICTURE, note.picture.trim());
		initialValues.put(KEY_CREATED,
				String.valueOf(System.currentTimeMillis()));
		initialValues.put(KEY_UPDATED,
				String.valueOf(System.currentTimeMillis()));
		/** New columns for Version 2 **/
		initialValues.put(KEY_SHARE, note.share.trim());
		initialValues.put(KEY_LATITUDE, note.latitude.trim());
		initialValues.put(KEY_LONGITUDE, note.longitude.trim());
		initialValues.put(KEY_USER_ID, note.userId.trim());
		initialValues.put(KEY_USER_NAME, note.userName.trim());
		initialValues.put(KEY_USER_LINK, note.userLink.trim());

		/** New columns for Version 3 **/
		initialValues.put(KEY_CHARACTERISTICS, note.characteristics.trim());
		initialValues.put(KEY_BREWERY_LINK, note.breweryLink.trim());

		/** New columns for Version 4 **/
		initialValues.put(KEY_CURRENCY_CODE, note.currencyCode.trim());
		initialValues.put(KEY_CURRENCY_SYMBOL, note.currencySymbol.trim());
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Delete the note with the given rowId
	 * 
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteNote(long rowId)
	{

		if (Logger.isLogEnabled())  Logger.log("deleteNote");

		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllNotes()
	{

		if (Logger.isLogEnabled())  Logger.log("fetchAllNotes");

		Cursor cursor = mDb.query(DATABASE_TABLE, new String[]
		{ KEY_ROWID, KEY_ALCOHOL, KEY_BEER, KEY_RATING, KEY_UPDATED, KEY_STATE,
				KEY_COUNTRY, KEY_PICTURE, KEY_SHARE, KEY_LATITUDE,
				KEY_LONGITUDE, KEY_USER_ID, KEY_USER_NAME, KEY_USER_LINK,
				KEY_CHARACTERISTICS, KEY_CURRENCY_CODE, KEY_CURRENCY_SYMBOL },
				null, null, null, null, KEY_UPDATED + " DESC");
		return cursor;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllNotes(int page, int rowsPerPage)
	{

		if (Logger.isLogEnabled())  Logger.log("fetchAllNotes");
		String limit = String.valueOf((page * rowsPerPage));

		Cursor cursor = mDb.query(DATABASE_TABLE, new String[]
		{ KEY_ROWID, KEY_ALCOHOL, KEY_BEER, KEY_RATING, KEY_UPDATED, KEY_STATE,
				KEY_COUNTRY, KEY_PICTURE, KEY_SHARE, KEY_LATITUDE,
				KEY_LONGITUDE, KEY_USER_ID, KEY_USER_NAME, KEY_USER_LINK,
				KEY_CHARACTERISTICS, KEY_BREWERY_LINK, KEY_CURRENCY_CODE,
				KEY_CURRENCY_SYMBOL }, null, null, null, null, KEY_UPDATED
				+ " DESC", limit);
		if (cursor != null)
		{
			if (page > 1)
			{
				cursor.moveToPosition(((page - 1) * rowsPerPage));
			}
		}

		return cursor;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllNotes(int page, int rowsPerPage, String sortBy)
	{

		if (Logger.isLogEnabled())  Logger.log("fetchAllNotes");
		String limit = String.valueOf((page * rowsPerPage));

		Cursor cursor = mDb.query(DATABASE_TABLE, new String[]
		{ KEY_ROWID, KEY_ALCOHOL, KEY_BEER, KEY_RATING, KEY_UPDATED, KEY_STATE,
				KEY_COUNTRY, KEY_PICTURE, KEY_SHARE, KEY_LATITUDE,
				KEY_LONGITUDE, KEY_USER_ID, KEY_USER_NAME, KEY_USER_LINK,
				KEY_CHARACTERISTICS, KEY_BREWERY_LINK, KEY_CURRENCY_CODE,
				KEY_CURRENCY_SYMBOL }, null, null, null, null, sortBy, limit);
		if (cursor != null)
		{
			if (page > 1)
			{
				cursor.moveToPosition(((page - 1) * rowsPerPage));
			}
		}

		return cursor;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllNotesData()
	{

		if (Logger.isLogEnabled())  Logger.log("fetchAllNotesData");

		return mDb.query(DATABASE_TABLE, new String[]
		{ KEY_ROWID, KEY_BEER, KEY_ALCOHOL, KEY_PRICE, KEY_STYLE, KEY_BREWERY,
				KEY_STATE, KEY_COUNTRY, KEY_RATING, KEY_NOTES, KEY_PICTURE,
				KEY_CREATED, KEY_UPDATED, KEY_SHARE, KEY_LATITUDE,
				KEY_LONGITUDE, KEY_USER_ID, KEY_USER_NAME, KEY_USER_LINK,
				KEY_CHARACTERISTICS, KEY_BREWERY_LINK, KEY_CURRENCY_CODE,
				KEY_CURRENCY_SYMBOL }, null, null, null, null, KEY_UPDATED
				+ " DESC");

	}

	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchNote(long rowId) throws SQLException
	{

		if (Logger.isLogEnabled())  Logger.log("fetchNote:id=" + rowId);

		Cursor cursor =

		mDb.query(false, DATABASE_TABLE, new String[]
		{ KEY_ROWID, KEY_BEER, KEY_ALCOHOL, KEY_PRICE, KEY_STYLE, KEY_BREWERY,
				KEY_STATE, KEY_COUNTRY, KEY_RATING, KEY_NOTES, KEY_PICTURE,
				KEY_CREATED, KEY_UPDATED, KEY_SHARE, KEY_LATITUDE,
				KEY_LONGITUDE, KEY_USER_ID, KEY_USER_NAME, KEY_USER_LINK,
				KEY_CHARACTERISTICS, KEY_BREWERY_LINK, KEY_CURRENCY_CODE,
				KEY_CURRENCY_SYMBOL }, KEY_ROWID + "=" + rowId, null, null,
				null, null, null);
		if (cursor != null)
		{
			cursor.moveToFirst();
		}
		return cursor;

	}

	/**
	 * Return a Cursor positioned at the note that matches the given parameters
	 * 
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchNotes(String beer, String rating, String price,
			String alcohol, String style, String BREWERY, String state,
			String country, String share, int page, int rowsPerPage)
			throws SQLException
	{

		if (Logger.isLogEnabled())  Logger.log("fetchNotes:: beer:" + beer + " rating:" + rating
				+ " price:" + price + " alcohol:" + alcohol + " style:" + style
				+ " BREWERY:" + BREWERY + " state:" + state + " country:"
				+ country + " share:" + share);
		String limit = String.valueOf((page * rowsPerPage));

		StringBuilder sbSelectionCriteria = new StringBuilder();
		if (beer != null && (!beer.equals("")))
		{
			sbSelectionCriteria.append("lower(" + KEY_BEER + ")");
			sbSelectionCriteria.append(" like '%");
			sbSelectionCriteria.append(beer.toLowerCase());
			sbSelectionCriteria.append("%' and ");
		}
		if (rating != null && (!rating.equals("")))
		{
			sbSelectionCriteria.append(KEY_RATING);
			sbSelectionCriteria.append(" = '");
			sbSelectionCriteria.append(rating);
			sbSelectionCriteria.append("' and ");
		}
		if (price != null && (!price.equals("")))
		{
			sbSelectionCriteria.append(KEY_PRICE);
			sbSelectionCriteria.append(" = '");
			sbSelectionCriteria.append(price);
			sbSelectionCriteria.append("' and ");
		}
		if (alcohol != null && (!alcohol.equals("")))
		{
			sbSelectionCriteria.append(KEY_ALCOHOL);
			sbSelectionCriteria.append(" = '");
			sbSelectionCriteria.append(alcohol);
			sbSelectionCriteria.append("' and ");
		}
		if (style != null && (!style.equals("")))
		{
			sbSelectionCriteria.append("lower(" + KEY_STYLE + ")");
			sbSelectionCriteria.append(" like '%");
			sbSelectionCriteria.append(style.toLowerCase());
			sbSelectionCriteria.append("%' and ");
		}
		if (BREWERY != null && (!BREWERY.equals("")))
		{
			sbSelectionCriteria.append("lower(" + KEY_BREWERY + ")");
			sbSelectionCriteria.append(" like '%");
			sbSelectionCriteria.append(BREWERY.toLowerCase());
			sbSelectionCriteria.append("%' and ");
		}
		if (state != null && (!state.equals("")))
		{
			sbSelectionCriteria.append("lower(" + KEY_STATE + ")");
			sbSelectionCriteria.append(" like '%");
			sbSelectionCriteria.append(state.toLowerCase());
			sbSelectionCriteria.append("%' and ");
		}
		if (country != null && (!country.equals("")))
		{
			sbSelectionCriteria.append("lower(" + KEY_COUNTRY + ")");
			sbSelectionCriteria.append(" like '%");
			sbSelectionCriteria.append(country.toLowerCase());
			sbSelectionCriteria.append("%' and ");
		}
		if (share != null && (!share.equals("")))
		{
			sbSelectionCriteria.append(KEY_SHARE);
			sbSelectionCriteria.append(" = '");
			sbSelectionCriteria.append(share);
			sbSelectionCriteria.append("' and ");
		}

		String sSelectionCriteria = sbSelectionCriteria.substring(0,
				sbSelectionCriteria.lastIndexOf("and"));

		if (Logger.isLogEnabled())  Logger.log("selection criteria:" + sSelectionCriteria);

		Cursor cursor = mDb.query(DATABASE_TABLE, new String[]
		{ KEY_ROWID, KEY_ALCOHOL, KEY_BEER, KEY_RATING, KEY_UPDATED, KEY_STATE,
				KEY_COUNTRY, KEY_PICTURE, KEY_SHARE, KEY_LATITUDE,
				KEY_LONGITUDE, KEY_USER_ID, KEY_USER_NAME, KEY_USER_LINK,
				KEY_CHARACTERISTICS, KEY_BREWERY_LINK, KEY_CURRENCY_CODE,
				KEY_CURRENCY_SYMBOL }, sSelectionCriteria, null, null, null,
				KEY_UPDATED + " DESC", limit);

		return cursor;
	}

	/**
	 * Update the note using the details provided. The note to be updated is
	 * specified using the rowId, and it is altered to use the title and body
	 * values passed in
	 * 
	 * @param rowId
	 *            id of note to update
	 * @param title
	 *            value to set note title to
	 * @param body
	 *            value to set note body to
	 * @return true if the note was successfully updated, false otherwise
	 */
	public boolean updateNote(Note note)
	{

		if (Logger.isLogEnabled())  Logger.log("updateNote");

		ContentValues args = new ContentValues();
		/*
		 * Identify deltas
		 */
		if ((note.beer != null) && ((!note.beer.equals(""))))
		{
			args.put(KEY_BEER, note.beer.trim());
		}
		if ((note.alcohol != null) && ((!note.alcohol.equals(""))))
		{
			args.put(KEY_ALCOHOL, note.alcohol.trim());
		}
		if ((note.price != null) && ((!note.price.equals(""))))
		{
			args.put(KEY_PRICE, note.price.trim());
		}
		if ((note.style != null) && ((!note.style.equals(""))))
		{
			args.put(KEY_STYLE, note.style.trim());
		}
		if ((note.brewery != null) && ((!note.brewery.equals(""))))
		{
			args.put(KEY_BREWERY, note.brewery.trim());
		}
		if ((note.breweryLink != null) && ((!note.breweryLink.equals(""))))
		{
			args.put(KEY_BREWERY_LINK, note.breweryLink.trim());
		}
		if ((note.state != null) && ((!note.state.equals(""))))
		{
			args.put(KEY_STATE, note.state.trim());
		}
		if ((note.country != null) && ((!note.country.equals(""))))
		{
			args.put(KEY_COUNTRY, note.country.trim());
		}
		if ((note.rating != null) && ((!note.rating.equals("0.0"))))
		{
			args.put(KEY_RATING, note.rating.trim());
		}
		if ((note.notes != null) && ((!note.notes.equals(""))))
		{
			args.put(KEY_NOTES, note.notes.trim());
		}
		if ((note.picture != null) && ((!note.picture.equals(""))))
		{
			args.put(KEY_PICTURE, note.picture.trim());
		}
		if ((note.share != null) && ((!note.share.equals(""))))
		{
			args.put(KEY_SHARE, note.share.trim());
		}
		if ((note.latitude != null) && ((!note.latitude.equals("0.0"))))
		{
			args.put(KEY_LATITUDE, note.latitude.trim());
		}
		if ((note.longitude != null) && ((!note.longitude.equals("0.0"))))
		{
			args.put(KEY_LONGITUDE, note.longitude.trim());
		}
		if ((note.userId != null) && ((!note.userId.equals(""))))
		{
			args.put(KEY_USER_ID, note.userId.trim());
		}
		if ((note.userName != null) && ((!note.userName.equals(""))))
		{
			args.put(KEY_USER_NAME, note.userName.trim());
		}
		if ((note.userLink != null) && ((!note.userLink.equals(""))))
		{
			args.put(KEY_USER_LINK, note.userLink.trim());
		}
		if ((note.characteristics != null)
				&& ((!note.characteristics.equals(""))))
		{
			args.put(KEY_CHARACTERISTICS, note.characteristics.trim());
		}
		if ((note.currencyCode != null) && ((!note.currencyCode.equals(""))))
		{
			args.put(KEY_CURRENCY_CODE, note.currencyCode.trim());
		}
		if ((note.currencySymbol != null)
				&& ((!note.currencySymbol.equals(""))))
		{
			args.put(KEY_CURRENCY_SYMBOL, note.currencySymbol.trim());
		}
		args.put(KEY_UPDATED, String.valueOf(System.currentTimeMillis()));
		return mDb
				.update(DATABASE_TABLE, args, KEY_ROWID + "=" + note.id, null) > 0;
	}

}
