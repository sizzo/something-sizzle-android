package com.sizzo.something.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class SizzoContentProvider extends ContentProvider {
	public static final String PROVIDER_NAME = "com.sizzo.something.data";
	public static final Uri WIFIS_URI = Uri.parse("content://" + PROVIDER_NAME + "/wifis");
	public static final Uri CONTENTS_URI = Uri.parse("content://" + PROVIDER_NAME + "/contents");
	private static final int WIFIS = 1;
	private static final int WIFI_ID = 2;
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "wifis", WIFIS);
		uriMatcher.addURI(PROVIDER_NAME, "wifis/#", WIFI_ID);
	}
	// ---for database use---
	private SQLiteDatabase sizzoDB;
	private static final String DATABASE_NAME = "sizzodb";
	private static final int DATABASE_VERSION = 1;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// Content table
			db.execSQL("CREATE TABLE IF  NOT EXISTS " + SizzoSchema.TABLE_CONTENT + "("
					+ SizzoSchema.Content._ID.name() + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ SizzoSchema.Content.UID.name() + " TEXT, " + SizzoSchema.Content.TITLE.name() + " TEXT, "
					+ SizzoSchema.Content.DETAIL.name() + " TEXT, type TEXT, " + SizzoSchema.Content.CRATEDDATE.name()
					+ " TEXT, " + SizzoSchema.Content.LASTUPDATEDDATE.name() + " TEXT)");
			db.execSQL("CREATE TABLE IF  NOT EXISTS  " + SizzoSchema.TABLE_PROPERTY + "("
					+ SizzoSchema.Property._ID.name() + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ SizzoSchema.Property.NAME.name() + " TEXT, " + SizzoSchema.Property.VALUE.name() + " TEXT)");
			// Content2Content table
			db.execSQL("CREATE TABLE IF  NOT EXISTS " + SizzoSchema.TABLE_CONENT2CONTENT + "("
					+ SizzoSchema.Content2Content._ID.name() + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ SizzoSchema.Content2Content.UIDFROM.name() + " TEXT,  "
					+ SizzoSchema.Content2Content.RELATION.name() + " TEXT, "
					+ SizzoSchema.Content2Content.UIDTO.name() + " TEXT, " + SizzoSchema.Content2Content.DETAIL.name()
					+ " TEXT, " + SizzoSchema.Content2Content.CRATEDDATE.name() + " TEXT, "
					+ SizzoSchema.Content2Content.LASTUPDATEDDATE.name() + " TEXT)");
			// File table
			db.execSQL("CREATE TABLE IF  NOT EXISTS " + SizzoSchema.TABLE_FILE + "(" + SizzoSchema.FILE._ID.name()
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + SizzoSchema.FILE.UID.name() + " TEXT, "
					+ SizzoSchema.FILE.NAME.name() + " TEXT, " + SizzoSchema.FILE.EXT.name() + " TEXT, "
					+ SizzoSchema.FILE.MIMETYPE.name() + " TEXT,  " + SizzoSchema.FILE.CONTENT.name() + " BLOB, "
					+ SizzoSchema.FILE.CRATEDDATE.name() + " TEXT, " + SizzoSchema.FILE.LASTUPDATEDDATE.name()
					+ " TEXT)");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("Content provider database", "Upgrading database from version " + oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + SizzoSchema.TABLE_CONTENT);
			db.execSQL("DROP TABLE IF EXISTS " + SizzoSchema.TABLE_PROPERTY);
			db.execSQL("DROP TABLE IF EXISTS " + SizzoSchema.TABLE_CONENT2CONTENT);
			db.execSQL("DROP TABLE IF EXISTS " + SizzoSchema.TABLE_FILE);
			onCreate(db);
		}
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		sizzoDB = dbHelper.getWritableDatabase();
		return (sizzoDB == null) ? false : true;
	}

	// ...
	// ...

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// arg0 = uri
		// arg1 = selection
		// arg2 = selectionArgs
		int count = 0;
		switch (uriMatcher.match(arg0)) {
		case WIFIS:
			count = sizzoDB.delete(SizzoSchema.TABLE_CONTENT, arg1, arg2);
			break;
		case WIFI_ID:
			String _id = arg0.getPathSegments().get(1);
			count = sizzoDB.delete(SizzoSchema.TABLE_CONTENT,
					SizzoSchema.Content._ID.name() + "=" + _id
							+ (!TextUtils.isEmpty(arg1) ? " AND (" + arg1 + ')' : ""), arg2);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + arg0);
		}
		getContext().getContentResolver().notifyChange(arg0, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case WIFIS:
			// ---get all wifis---
			return "vnd.android.cursor.dir/vnd.somethingsizzle.wifis";
		case WIFI_ID:
			// ---get a particular wifi---
			return "vnd.android.cursor.item/vnd.somethingsizzle.wifis";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// ---add a new book---
		long rowID = sizzoDB.insert(SizzoSchema.TABLE_CONTENT, "", values);
		// ---if added successfully---
		if (rowID > 0) {
			Uri _uri = ContentUris.withAppendedId(WIFIS_URI, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(SizzoSchema.TABLE_CONTENT);
		if (uriMatcher.match(uri) == WIFI_ID)
			// ---if getting a particular book---
			sqlBuilder.appendWhere(SizzoSchema.Content._ID.name() + "=" + uri.getPathSegments().get(1));
		if (sortOrder == null || sortOrder == "")
			sortOrder = SizzoSchema.Content.TITLE.name();
		Cursor c = sqlBuilder.query(sizzoDB, projection, selection, selectionArgs, null, null, sortOrder);
		// ---register to watch a content URI for changes---
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case WIFIS:
			count = sizzoDB.update(SizzoSchema.TABLE_CONTENT, values, selection, selectionArgs);
			break;
		case WIFI_ID:
			count = sizzoDB.update(SizzoSchema.TABLE_CONTENT, values, SizzoSchema.Content._ID + "="
					+ uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
					selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
