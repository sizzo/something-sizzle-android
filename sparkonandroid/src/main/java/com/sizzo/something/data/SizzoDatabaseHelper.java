/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.sizzo.something.data;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Binder;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

/**
 * Database helper for contacts. Designed as a singleton to make sure that all
 * {@link android.content.ContentProvider} users get the same reference.
 * Provides handy methods for maintaining package and mime-type lookup tables.
 */
public class SizzoDatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = "ContactsDatabaseHelper";

	/**
	 * Contacts DB version ranges:
	 * 
	 * <pre>
	 *   0-98    Cupcake/Donut
	 *   100-199 Eclair
	 *   200-299 Eclair-MR1
	 *   300-349 Froyo
	 *   350-399 Gingerbread
	 *   400-499 Honeycomb
	 *   500-549 Honeycomb-MR1
	 *   550-599 Honeycomb-MR2
	 *   600-699 Ice Cream Sandwich
	 * </pre>
	 */
	static final int DATABASE_VERSION = 625;

	private static final String DATABASE_NAME = "sizzo.db";
	private static final String DATABASE_PRESENCE = "presence_db";
	private SQLiteStatement mStatementUpdateWifiNode;

	public interface Tables {
		// public static final String CONTENTS = "contents";
//		public static final String CONTENT2CONTENTS = "content2contents";
		public static final String USERS = "users";
		public static final String USER2USERS = "user2users";
		public static final String USER2CONTENTS = "user2contents";
//		public static final String PROPERTIES = "properties";
		public static final String FILES = "files";

		// This list of tables contains auto-incremented sequences.
		public static final String[] SEQUENCE_TABLES = new String[] { SizzoSchema.CONTENTS.TABLE, SizzoSchema.CONTENT2CONTENTS.TABLE,
				USERS, USER2USERS, USER2CONTENTS, SizzoSchema.PROPERTIES.TABLE, FILES, };

		/**
		 * For {@link ContactsContract.DataUsageFeedback}. The table structure
		 * itself is not exposed outside.
		 */
		public static final String DATA_USAGE_STAT = "data_usage_stat";

		public static final String DATA_JOIN_MIMETYPES = "data "
				+ "JOIN mimetypes ON (data.mimetype_id = mimetypes._id)";

		public static final String DATA_JOIN_RAW_CONTACTS = "data "
				+ "JOIN raw_contacts ON (data.raw_contact_id = raw_contacts._id)";

		public static final String DATA_JOIN_MIMETYPE_RAW_CONTACTS = "data "
				+ "JOIN mimetypes ON (data.mimetype_id = mimetypes._id) "
				+ "JOIN raw_contacts ON (data.raw_contact_id = raw_contacts._id)";

		// NOTE: This requires late binding of GroupMembership MIME-type
		public static final String RAW_CONTACTS_JOIN_SETTINGS_DATA_GROUPS = "raw_contacts "
				+ "LEFT OUTER JOIN settings ON (" + "raw_contacts.account_name = settings.account_name AND "
				+ "raw_contacts.account_type = settings.account_type AND "
				+ "((raw_contacts.data_set IS NULL AND settings.data_set IS NULL) "
				+ "OR (raw_contacts.data_set = settings.data_set))) "
				+ "LEFT OUTER JOIN data ON (data.mimetype_id=? AND " + "data.raw_contact_id = raw_contacts._id) "
				+ "LEFT OUTER JOIN groups ON (groups._id = data." + GroupMembership.GROUP_ROW_ID + ")";

		// NOTE: This requires late binding of GroupMembership MIME-type
		public static final String SETTINGS_JOIN_RAW_CONTACTS_DATA_MIMETYPES_CONTACTS = "settings "
				+ "LEFT OUTER JOIN raw_contacts ON (" + "raw_contacts.account_name = settings.account_name AND "
				+ "raw_contacts.account_type = settings.account_type) "
				+ "LEFT OUTER JOIN data ON (data.mimetype_id=? AND " + "data.raw_contact_id = raw_contacts._id) "
				+ "LEFT OUTER JOIN contacts ON (raw_contacts.contact_id = contacts._id)";

		public static final String DATA_JOIN_PACKAGES_MIMETYPES_RAW_CONTACTS_GROUPS = "data "
				+ "JOIN mimetypes ON (data.mimetype_id = mimetypes._id) "
				+ "JOIN raw_contacts ON (data.raw_contact_id = raw_contacts._id) "
				+ "LEFT OUTER JOIN packages ON (data.package_id = packages._id) " + "LEFT OUTER JOIN groups "
				+ "  ON (mimetypes.mimetype='" + GroupMembership.CONTENT_ITEM_TYPE + "' "
				+ "      AND groups._id = data." + GroupMembership.GROUP_ROW_ID + ") ";

		public static final String GROUPS_JOIN_PACKAGES = "groups "
				+ "LEFT OUTER JOIN packages ON (groups.package_id = packages._id)";

		public static final String ACTIVITIES = "activities";

		public static final String ACTIVITIES_JOIN_MIMETYPES = "activities "
				+ "LEFT OUTER JOIN mimetypes ON (activities.mimetype_id = mimetypes._id)";

		public static final String ACTIVITIES_JOIN_PACKAGES_MIMETYPES_RAW_CONTACTS_CONTACTS = "activities "
				+ "LEFT OUTER JOIN packages ON (activities.package_id = packages._id) "
				+ "LEFT OUTER JOIN mimetypes ON (activities.mimetype_id = mimetypes._id) "
				+ "LEFT OUTER JOIN raw_contacts ON (activities.author_contact_id = " + "raw_contacts._id) "
				+ "LEFT OUTER JOIN contacts ON (raw_contacts.contact_id = contacts._id)";

		public static final String NAME_LOOKUP_JOIN_RAW_CONTACTS = "name_lookup "
				+ "INNER JOIN view_raw_contacts ON (name_lookup.raw_contact_id = " + "view_raw_contacts._id)";
	}

	public interface Joins {
		/**
		 * Join string intended to be used with the GROUPS table/view. The main
		 * table must be named as "groups".
		 * 
		 * Adds the "group_member_count column" to the query, which will be null
		 * if a group has no members. Use ifnull(group_member_count, 0) if 0 is
		 * needed instead.
		 */
		public static final String GROUP_MEMBER_COUNT = " LEFT OUTER JOIN (SELECT "
				+ "data.data1 AS member_count_group_id, " + "COUNT(data.raw_contact_id) AS group_member_count "
				+ "FROM data " + "WHERE " + "data.mimetype_id = (SELECT _id FROM mimetypes WHERE "
				+ "mimetypes.mimetype = '" + GroupMembership.CONTENT_ITEM_TYPE + "')"
				+ "GROUP BY member_count_group_id) AS member_count_table" // End
																			// of
																			// inner
																			// query
				+ " ON (groups._id = member_count_table.member_count_group_id)";
	}

	public interface Views {
		public static final String DATA = "view_data";
		public static final String RAW_CONTACTS = "view_raw_contacts";
		public static final String CONTACTS = "view_contacts";
		public static final String ENTITIES = "view_entities";
		public static final String RAW_ENTITIES = "view_raw_entities";
		public static final String GROUPS = "view_groups";
		public static final String DATA_USAGE_STAT = "view_data_usage_stat";
		public static final String STREAM_ITEMS = "view_stream_items";
	}

	public interface Clauses {

		final String OUTER_RAW_CONTACTS = "outer_raw_contacts";
		final String OUTER_RAW_CONTACTS_ID = OUTER_RAW_CONTACTS + "." + RawContacts._ID;

	}

	public interface PropertiesColumns {
		String PROPERTY_KEY = "property_key";
		String PROPERTY_VALUE = "property_value";
	}

	/** In-memory cache of previously found MIME-type mappings */
	private final HashMap<String, Long> mMimetypeCache = new HashMap<String, Long>();
	/** In-memory cache of previously found package name mappings */
	private final HashMap<String, Long> mContentCache = new HashMap<String, Long>();

	/** Compiled statements for querying and inserting mappings */
	private SQLiteStatement mContactIdQuery;

	private final Context mContext;
	private final boolean mDatabaseOptimizationEnabled;
	private StringBuilder mSb = new StringBuilder();

	private boolean mReopenDatabase = false;

	private SQLiteStatement mStatementContentQueryByUid;

	private SQLiteStatement mStatementContentInitInsert;

	private static SizzoDatabaseHelper sSingleton = null;

	public static synchronized SizzoDatabaseHelper getInstance(Context context) {
		if (sSingleton == null) {
			sSingleton = new SizzoDatabaseHelper(context, DATABASE_NAME, true);
		}
		return sSingleton;
	}

	/**
	 * Private constructor, callers except unit tests should obtain an instance
	 * through {@link #getInstance(android.content.Context)} instead.
	 */
	SizzoDatabaseHelper(Context context) {
		this(context, null, false);
	}

	protected SizzoDatabaseHelper(Context context, String databaseName, boolean optimizationEnabled) {
		super(context, databaseName, null, DATABASE_VERSION);
		mDatabaseOptimizationEnabled = optimizationEnabled;
		Resources resources = context.getResources();

		mContext = context;
	}

	private void refreshDatabaseCaches(SQLiteDatabase db) {

		mContactIdQuery = null;

		populateMimeTypeCache(db);
	}

	private void populateMimeTypeCache(SQLiteDatabase db) {
		mMimetypeCache.clear();
		mContentCache.clear();

	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		refreshDatabaseCaches(db);

		db.execSQL("ATTACH DATABASE ':memory:' AS " + DATABASE_PRESENCE + ";");

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "Bootstrapping database version: " + DATABASE_VERSION);

		db.execSQL("CREATE TABLE " + SizzoSchema.CONTENTS.TABLE + " (" + BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + SizzoSchema.CONTENTS.Columns.UID + " TEXT,"
				+ SizzoSchema.CONTENTS.Columns.TITLE + " TEXT," + SizzoSchema.CONTENTS.Columns.DETAIL + " TEXT,"
				+ SizzoSchema.CONTENTS.Columns.TYPE + " TEXT," 
				+ SizzoSchema.CONTENTS.Columns.RANK + " INTEGER," 
				+ SizzoSchema.CONTENTS.Columns.CRATEDDATE + " TEXT,"
				+ SizzoSchema.CONTENTS.Columns.LASTUPDATEDDATE + " TEXT );");

		db.execSQL("CREATE INDEX content_has_uid_index ON " + SizzoSchema.CONTENTS.TABLE + " ("
				+ SizzoSchema.CONTENTS.Columns.UID + ");");

		// When adding new tables, be sure to also add size-estimates in
		// updateSqliteStats
		createGroupsView(db);

	}

	protected void initializeAutoIncrementSequences(SQLiteDatabase db) {
		// Default implementation does nothing.
	}

	public void createSearchIndexTable(SQLiteDatabase db) {
		// db.execSQL("DROP TABLE IF EXISTS " + Tables.SEARCH_INDEX);
		// db.execSQL("CREATE VIRTUAL TABLE " + Tables.SEARCH_INDEX
		// + " USING FTS4 ("
		// + SearchIndexColumns.CONTACT_ID +
		// " INTEGER REFERENCES contacts(_id) NOT NULL,"
		// + SearchIndexColumns.CONTENT + " TEXT, "
		// + SearchIndexColumns.NAME + " TEXT, "
		// + SearchIndexColumns.TOKENS + " TEXT"
		// + ")");
	}

	/**
	 * Returns the value to be returned when querying the column indicating that
	 * the contact or raw contact belongs to the user's personal profile.
	 * Overridden in the profile DB helper subclass.
	 */
	protected int dbForProfile() {
		return 0;
	}

	private void createGroupsView(SQLiteDatabase db) {
		// db.execSQL("DROP VIEW IF EXISTS " + Views.GROUPS + ";");
		// String groupsColumns = Groups.ACCOUNT_NAME + "," +
		// Groups.ACCOUNT_TYPE + "," + Groups.DATA_SET + ","
		// + "(CASE WHEN " + Groups.DATA_SET + " IS NULL THEN " +
		// Groups.ACCOUNT_TYPE + " ELSE "
		// + Groups.ACCOUNT_TYPE + "||" + Groups.DATA_SET + " END) AS " +
		// Groups.SOURCE_ID + "," + Groups.VERSION
		// + "," + Groups.DIRTY + "," + Groups.TITLE + "," + Groups.NOTES + ","
		// + Groups.SYSTEM_ID + ","
		// + Groups.DELETED + "," + Groups.GROUP_VISIBLE + "," +
		// Groups.SHOULD_SYNC + "," + Groups.AUTO_ADD + ","
		// + Groups.FAVORITES + "," + Groups.GROUP_IS_READ_ONLY + "," +
		// Groups.SYNC1 + "," + Groups.SYNC2 + ","
		// + Groups.SYNC3 + "," + Groups.SYNC4 + ",";
		//
		// String groupsSelect = "SELECT " + groupsColumns + " FROM " +
		// Tables.GROUPS_JOIN_PACKAGES;
		//
		// db.execSQL("CREATE VIEW " + Views.GROUPS + " AS " + groupsSelect);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 99) {
			Log.i(TAG, "Upgrading from version " + oldVersion + " to " + newVersion + ", data will be lost!");

			db.execSQL("DROP TABLE IF EXISTS " + SizzoSchema.CONTENTS.TABLE + ";");
			db.execSQL("DROP TABLE IF EXISTS " + SizzoSchema.PROPERTIES.TABLE + ";");
			db.execSQL("DROP TABLE IF EXISTS " + Tables.USER2USERS + ";");
			db.execSQL("DROP TABLE IF EXISTS " + Tables.USER2CONTENTS + ";");
			db.execSQL("DROP TABLE IF EXISTS " + Tables.ACTIVITIES + ";");

			onCreate(db);
			return;
		}

		Log.i(TAG, "Upgrading from version " + oldVersion + " to " + newVersion);

		boolean upgradeViewsAndTriggers = false;

		if (oldVersion == 99) {
			upgradeViewsAndTriggers = true;
			oldVersion++;
		}

		if (oldVersion != newVersion) {
			throw new IllegalStateException("error upgrading the database to version " + newVersion);
		}
	}

	/**
	 * Inserts a record in the {@link Tables#NAME_LOOKUP} table.
	 */
	public void insertNameLookup(SQLiteStatement stmt, long rawContactId, long dataId, int lookupType, String name) {
		if (TextUtils.isEmpty(name)) {
			return;
		}

		String normalized = name;
		if (TextUtils.isEmpty(normalized)) {
			return;
		}

		insertNormalizedNameLookup(stmt, rawContactId, dataId, lookupType, normalized);
	}

	private void insertNormalizedNameLookup(SQLiteStatement stmt, long rawContactId, long dataId, int lookupType,
			String normalizedName) {
		stmt.bindLong(1, rawContactId);
		stmt.bindLong(2, dataId);
		stmt.bindLong(3, lookupType);
		stmt.bindString(4, normalizedName);
		stmt.executeInsert();
	}

	private void upgradeToVersion622(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE calls ADD formatted_number TEXT DEFAULT NULL;");
	}

	private void bindString(SQLiteStatement stmt, int index, String value) {
		if (value == null) {
			stmt.bindNull(index);
		} else {
			stmt.bindString(index, value);
		}
	}

	private void bindLong(SQLiteStatement stmt, int index, Number value) {
		if (value == null) {
			stmt.bindNull(index);
		} else {
			stmt.bindLong(index, value.longValue());
		}
	}

	/**
	 * Adds index stats into the SQLite database to force it to always use the
	 * lookup indexes.
	 */
	private void updateSqliteStats(SQLiteDatabase db) {

		// Specific stats strings are based on an actual large database after
		// running ANALYZE
		// Important here are relative sizes. Raw-Contacts is slightly bigger
		// than Contacts
		// Warning: Missing tables in here will make SQLite assume to contain
		// 1000000 rows,
		// which can lead to catastrophic query plans for small tables
		try {
			db.execSQL("DELETE FROM sqlite_stat1");
			updateIndexStats(db, SizzoSchema.CONTENTS.TABLE, "contacts_has_phone_index", "9000 500");
			updateIndexStats(db, SizzoSchema.CONTENTS.TABLE, "contacts_name_raw_contact_id_index", "9000 1");

			updateIndexStats(db, SizzoSchema.PROPERTIES.TABLE, "raw_contacts_source_id_index", "10000 1 1 1");
			updateIndexStats(db, SizzoSchema.PROPERTIES.TABLE, "raw_contacts_contact_id_index", "10000 2");
			updateIndexStats(db, SizzoSchema.PROPERTIES.TABLE, "raw_contact_sort_key2_index", "10000 2");
			updateIndexStats(db, SizzoSchema.PROPERTIES.TABLE, "raw_contact_sort_key1_index", "10000 2");
			updateIndexStats(db, SizzoSchema.PROPERTIES.TABLE, "raw_contacts_source_id_data_set_index", "10000 1 1 1 1");
		} catch (SQLException e) {
			Log.e(TAG, "Could not update index stats", e);
		}
	}

	/**
	 * Stores statistics for a given index.
	 * 
	 * @param stats
	 *            has the following structure: the first index is the expected
	 *            size of the table. The following integer(s) are the expected
	 *            number of records selected with the index. There should be one
	 *            integer per indexed column.
	 */
	private void updateIndexStats(SQLiteDatabase db, String table, String index, String stats) {
		if (index == null) {
			db.execSQL("DELETE FROM sqlite_stat1 WHERE tbl=? AND idx IS NULL", new String[] { table });
		} else {
			db.execSQL("DELETE FROM sqlite_stat1 WHERE tbl=? AND idx=?", new String[] { table, index });
		}
		db.execSQL("INSERT INTO sqlite_stat1 (tbl,idx,stat) VALUES (?,?,?)", new String[] { table, index, stats });
	}

	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		SQLiteDatabase db = super.getWritableDatabase();
		if (mReopenDatabase) {
			mReopenDatabase = false;
			close();
			db = super.getWritableDatabase();
		}
		return db;
	}

	/**
	 * Wipes all data except mime type and package lookup tables.
	 */
	public void wipeData() {
		SQLiteDatabase db = getWritableDatabase();

		db.execSQL("DELETE FROM " + SizzoSchema.CONTENTS.TABLE + ";");
		db.execSQL("DELETE FROM " + SizzoSchema.PROPERTIES.TABLE + ";");
		db.execSQL("DELETE FROM " + SizzoSchema.CONTENT2CONTENTS.TABLE + ";");
		db.execSQL("DELETE FROM " + Tables.FILES + ";");
		db.execSQL("DELETE FROM " + Tables.USERS + ";");

		db.execSQL("DELETE FROM " + Tables.ACTIVITIES + ";");
	}

	/**
	 * Return the {@link ApplicationInfo#uid} for the given package name.
	 */
	public static int getUidForPackageName(PackageManager pm, String packageName) {
		try {
			ApplicationInfo clientInfo = pm.getApplicationInfo(packageName, 0 /*
																			 * no
																			 * flags
																			 */);
			return clientInfo.uid;
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public int delete(int matchNode, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void upsertContentWifiNode(ContentValues values, String selection, String[] selectionArgs) {
		long rowID = getContentId(values.getAsString(SizzoSchema.CONTENTS.Columns.UID));
		if (rowID > 0) {
			if (mStatementUpdateWifiNode == null) {
				mStatementUpdateWifiNode = getWritableDatabase().compileStatement(
						"UPDATE " + SizzoSchema.CONTENTS.TABLE + " SET " + SizzoSchema.CONTENTS.Columns.TITLE + "=?, "
								+ SizzoSchema.CONTENTS.Columns.DETAIL + "=?, " + SizzoSchema.CONTENTS.Columns.TYPE
								+ "=?, " + SizzoSchema.CONTENTS.Columns.LASTUPDATEDDATE + "=date('now')" + " WHERE "
								+ SizzoSchema.CONTENTS.Columns.UID + "=?");
			}
			bindString(mStatementUpdateWifiNode, 1, values.getAsString(SizzoSchema.CONTENTS.Columns.TITLE));
			bindString(mStatementUpdateWifiNode, 2, values.getAsString(SizzoSchema.CONTENTS.Columns.DETAIL));
			bindString(mStatementUpdateWifiNode, 3, SizzoSchema.CONTENTS.Types.WIFI.name());
			bindString(mStatementUpdateWifiNode, 4, values.getAsString(SizzoSchema.CONTENTS.Columns.UID));
			mStatementUpdateWifiNode.execute();
		}
	}

	public long getContentId(String uid) {
		if (uid == null || uid.trim().length() == 0) {
			return -1;
		}
		// Try an in-memory cache lookup
		if (mContentCache.containsKey(uid)) {
			return mContentCache.get(uid);
		}
		if (mStatementContentQueryByUid == null) {
			mStatementContentQueryByUid = getWritableDatabase().compileStatement(
					"SELECT " + SizzoSchema.CONTENTS.Columns._ID + " FROM " + SizzoSchema.CONTENTS.TABLE + " WHERE "
							+ SizzoSchema.CONTENTS.Columns.UID + "=?");
		}

		if (mStatementContentInitInsert == null) {
			mStatementContentInitInsert = getWritableDatabase().compileStatement(
					"INSERT INTO " + SizzoSchema.CONTENTS.TABLE + "(" + SizzoSchema.CONTENTS.Columns.UID + ","
							+ SizzoSchema.CONTENTS.Columns.CRATEDDATE + ") VALUES (?,date('now'))");
		}
		return lookupAndCacheId(mStatementContentQueryByUid, mStatementContentInitInsert, uid, mContentCache);
	}

	/**
	 * Perform an internal string-to-integer lookup using the compiled
	 * {@link SQLiteStatement} provided. If a mapping isn't found in database,
	 * it will be created. All new, uncached answers are added to the cache
	 * automatically.
	 * 
	 * @param query
	 *            Compiled statement used to query for the mapping.
	 * @param insert
	 *            Compiled statement used to insert a new mapping when no
	 *            existing one is found in cache or from query.
	 * @param value
	 *            Value to find mapping for.
	 * @param cache
	 *            In-memory cache of previous answers.
	 * @return An unique integer mapping for the given value.
	 */
	private long lookupAndCacheId(SQLiteStatement query, SQLiteStatement insert, String value,
			HashMap<String, Long> cache) {
		long id = -1;
		try {
			// Try searching database for mapping
			DatabaseUtils.bindObjectToProgram(query, 1, value);
			id = query.simpleQueryForLong();
		} catch (SQLiteDoneException e) {
			// Nothing found, so try inserting new mapping
			DatabaseUtils.bindObjectToProgram(insert, 1, value);
			id = insert.executeInsert();
		}
		if (id != -1) {
			// Cache and return the new answer
			cache.put(value, id);
			return id;
		} else {
			// Otherwise throw if no mapping found or created
			throw new IllegalStateException("Couldn't find or create internal " + "lookup table entry for value "
					+ value);
		}
	}

	public static void copyStringValue(ContentValues toValues, String toKey, ContentValues fromValues, String fromKey) {
		if (fromValues.containsKey(fromKey)) {
			toValues.put(toKey, fromValues.getAsString(fromKey));
		}
	}

	public static void copyLongValue(ContentValues toValues, String toKey, ContentValues fromValues, String fromKey) {
		if (fromValues.containsKey(fromKey)) {
			long longValue;
			Object value = fromValues.get(fromKey);
			if (value instanceof Boolean) {
				if ((Boolean) value) {
					longValue = 1;
				} else {
					longValue = 0;
				}
			} else if (value instanceof String) {
				longValue = Long.parseLong((String) value);
			} else {
				longValue = ((Number) value).longValue();
			}
			toValues.put(toKey, longValue);
		}
	}

	/**
	 * Returns the value from the {@link Tables#PROPERTIES} table.
	 */
	public String getProperty(String key, String defaultValue) {
		Cursor cursor = getReadableDatabase().query(SizzoSchema.PROPERTIES.TABLE,
				new String[] { PropertiesColumns.PROPERTY_VALUE }, PropertiesColumns.PROPERTY_KEY + "=?",
				new String[] { key }, null, null, null);
		String value = null;
		try {
			if (cursor.moveToFirst()) {
				value = cursor.getString(0);
			}
		} finally {
			cursor.close();
		}

		return value != null ? value : defaultValue;
	}

	/**
	 * Stores a key-value pair in the {@link Tables#PROPERTIES} table.
	 */
	public void setProperty(String key, String value) {
		setProperty(getWritableDatabase(), key, value);
	}

	private void setProperty(SQLiteDatabase db, String key, String value) {
		ContentValues values = new ContentValues();
		values.put(PropertiesColumns.PROPERTY_KEY, key);
		values.put(PropertiesColumns.PROPERTY_VALUE, value);
		db.replace(SizzoSchema.PROPERTIES.TABLE, null, values);
	}

	/**
	 * Test if any of the columns appear in the given projection.
	 */
	public boolean isInProjection(String[] projection, String... columns) {
		if (projection == null) {
			return true;
		}

		// Optimized for a single-column test
		if (columns.length == 1) {
			String column = columns[0];
			for (String test : projection) {
				if (column.equals(test)) {
					return true;
				}
			}
		} else {
			for (String test : projection) {
				for (String column : columns) {
					if (column.equals(test)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns a detailed exception message for the supplied URI. It includes
	 * the calling user and calling package(s).
	 */
	public String exceptionMessage(Uri uri) {
		return exceptionMessage(null, uri);
	}

	/**
	 * Returns a detailed exception message for the supplied URI. It includes
	 * the calling user and calling package(s).
	 */
	public String exceptionMessage(String message, Uri uri) {
		StringBuilder sb = new StringBuilder();
		if (message != null) {
			sb.append(message).append("; ");
		}
		sb.append("URI: ").append(uri);
		final PackageManager pm = mContext.getPackageManager();
		int callingUid = Binder.getCallingUid();
		sb.append(", calling user: ");
		String userName = pm.getNameForUid(callingUid);
		if (userName != null) {
			sb.append(userName);
		} else {
			sb.append(callingUid);
		}

		final String[] callerPackages = pm.getPackagesForUid(callingUid);
		if (callerPackages != null && callerPackages.length > 0) {
			if (callerPackages.length == 1) {
				sb.append(", calling package:");
				sb.append(callerPackages[0]);
			} else {
				sb.append(", calling package is one of: [");
				for (int i = 0; i < callerPackages.length; i++) {
					if (i != 0) {
						sb.append(", ");
					}
					sb.append(callerPackages[i]);
				}
				sb.append("]");
			}
		}

		return sb.toString();
	}

}
