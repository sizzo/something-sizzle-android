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
			mSizzoDBHelper.upsertContentWifiNode(values, null, null);
			long rowID = mSizzoDBHelper.getContentId(values.getAsString(SizzoSchema.CONTENTS.Columns.UID));
			if (rowID > 0) {
				Uri nodeUri = ContentUris.withAppendedId(SizzoUriMatcher.WIFIS_URI_ID, rowID);
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
		Cursor cursor = null;
		
		int matchNode = uriMatcher.match(uri);
		switch (matchNode) {
		case SizzoUriMatcher.WIFIS:
		case SizzoUriMatcher.WIFIS_ID:
		case SizzoUriMatcher.WIFIS_UID:
			cursor = queryWifiContents(uri, projection, selection, selectionArgs, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			break;
		case SizzoUriMatcher.USERS:
		case SizzoUriMatcher.USERS_ID:
		case SizzoUriMatcher.USERS_UID:
			cursor = queryUserContents(uri, projection, selection, selectionArgs, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			break;
		default:
			throw new IllegalArgumentException("Unknown Insert URI " + uri);
		}
		return cursor;
	}

	private Cursor queryWifiContents(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor;
		int matchNode = uriMatcher.match(uri);
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(SizzoSchema.CONTENTS.TABLE);
		if (matchNode == SizzoUriMatcher.WIFIS_ID) {
			sqlBuilder.appendWhere(SizzoSchema.CONTENTS.Columns._ID + "=" + uri.getPathSegments().get(1));
		} else if (matchNode == SizzoUriMatcher.WIFIS_UID) {
			sqlBuilder.appendWhere(SizzoSchema.CONTENTS.Columns.UID + "=" + uri.getPathSegments().get(1));
		}
		if (sortOrder == null || sortOrder == "")
			sortOrder = SizzoSchema.CONTENTS.Columns.TITLE;
		cursor = sqlBuilder.query(mSizzoDBHelper.getReadableDatabase(), projection, selection, selectionArgs, null,
				null, sortOrder);
		return cursor;
	}

	private Cursor queryUserContents(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor;
		int matchNode = uriMatcher.match(uri);
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(SizzoSchema.USERS.TABLE);
		if (matchNode == SizzoUriMatcher.WIFIS_ID) {
			sqlBuilder.appendWhere(SizzoSchema.USERS.Columns._ID + "=" + uri.getPathSegments().get(1));
		} else if (matchNode == SizzoUriMatcher.WIFIS_UID) {
			sqlBuilder.appendWhere(SizzoSchema.USERS.Columns.UID + "=" + uri.getPathSegments().get(1));
		}
		if (sortOrder == null || sortOrder == "")
			sortOrder = SizzoSchema.USERS.Columns.TITLE;
		cursor = sqlBuilder.query(mSizzoDBHelper.getReadableDatabase(), projection, selection, selectionArgs, null,
				null, sortOrder);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;
		int matchNode = uriMatcher.match(uri);

		switch (matchNode) {
		case SizzoUriMatcher.WIFIS:
		case SizzoUriMatcher.WIFIS_ID:
		case SizzoUriMatcher.WIFIS_UID:
			mSizzoDBHelper.upsertContentWifiNode(values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return (int) count;
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
