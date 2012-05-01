package com.sizzo.something.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

public class SizzoProvider extends AbstractSizzoProvider {
	private static final String TAG = "SizzoProvider";

	private static final SizzoUriMatcher uriMatcher = SizzoUriMatcher.getInstance();
	// ---for database use---
	// private SQLiteDatabase sizzoDB;
	// private final ThreadLocal<SizzoDatabaseHelper> mDbHelper =
	// new ThreadLocal<SizzoDatabaseHelper>();
	private SizzoDatabaseHelper mSizzoDBHelper;

	@Override
	public boolean onCreate() {
		Log.d(TAG, "onCreate start");
		super.onCreate();
		try {
			return initialize();
		} catch (RuntimeException e) {
			Log.e(TAG, "Cannot start provider", e);
			return false;
		} finally {
			Log.d(TAG, "onCreate finish");
		}

	}

	private boolean initialize() {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());

		Resources resources = getContext().getResources();

		mSizzoDBHelper = getDatabaseHelper(getContext());
		// mDbHelper.set(mContactsHelper);

		return true;
	}

	@Override
	protected SizzoDatabaseHelper getDatabaseHelper(final Context context) {
		return SizzoDatabaseHelper.getInstance(context);
	}

	// ...
	// ...

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// arg0 = uri
		// arg1 = selection
		// arg2 = selectionArgs
		int count = 0;
		int matchNode = uriMatcher.match(uri);
		switch (matchNode) {
		case SizzoUriMatcher.WIFIS:
		case SizzoUriMatcher.WIFIS_ID:
			count = mSizzoDBHelper.delete(matchNode, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		String type = SizzoUriMatcher.getUriType(uriMatcher.match(uri));
		if (type == null) {
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		return type;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int matchNode = uriMatcher.match(uri);
		switch (matchNode) {
		case SizzoUriMatcher.WIFIS:
			long rowID = mSizzoDBHelper.insert(matchNode, values);
			if (rowID > 0) {
				Uri nodeUri = ContentUris.withAppendedId(SizzoUriMatcher.WIFIS_URI, rowID);
				getContext().getContentResolver().notifyChange(uri, null);
				return nodeUri;
			}
			throw new SQLException("Failed to insert row into " + uri);
		default:
			throw new IllegalArgumentException("Unknown Insert URI " + uri);
		}

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(SizzoDatabaseHelper.Tables.CONTENTS);
		if (uriMatcher.match(uri) == SizzoUriMatcher.WIFIS_ID)
			// ---if getting a particular book---
			sqlBuilder.appendWhere(SizzoDatabaseHelper.ContentsColumns._ID + "=" + uri.getPathSegments().get(1));
		if (sortOrder == null || sortOrder == "")
			sortOrder = SizzoDatabaseHelper.ContentsColumns.TITLE;
		Cursor c = sqlBuilder.query(mSizzoDBHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
		// ---register to watch a content URI for changes---
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		int matchNode = uriMatcher.match(uri);
		
		switch (matchNode) {
		case SizzoUriMatcher.WIFIS:
			count = mSizzoDBHelper.update(matchNode, values, selection, selectionArgs);
			break;
		case SizzoUriMatcher.WIFIS_ID:
			count = mSizzoDBHelper.update(matchNode, values, selection,	selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public void onBegin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCommit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRollback() {
		// TODO Auto-generated method stub

	}

	@Override
	protected ThreadLocal<SizzoTransaction> getTransactionHolder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Uri insertInTransaction(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int deleteInTransaction(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int updateInTransaction(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected boolean yield(SizzoTransaction transaction) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void notifyChange() {
		// TODO Auto-generated method stub

	}

}
