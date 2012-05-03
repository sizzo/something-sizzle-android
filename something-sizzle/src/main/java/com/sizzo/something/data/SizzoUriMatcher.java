package com.sizzo.something.data;

import java.util.Map;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.ContactsContract.Data;

import com.google.common.collect.Maps;

public class SizzoUriMatcher extends UriMatcher {

	public static final String AUTHORITY = "com.sizzo.data";
	public static final int WIFIS = 1;
	public static final int WIFIS_ID = 2;
	public static final int WIFIS_UID = 3;
	public static final int USERS = 4;
	public static final int USERS_ID = 5;
	public static final int USERS_UID = 6;

	public static final Uri WIFIS_URI = Uri.parse("content://" + AUTHORITY + "/wifis");;
	public static final Uri WIFIS_URI_ID = Uri.parse("content://" + AUTHORITY + "/wifis/#");
	public static final Uri WIFIS_URI_UID = Uri.parse("content://" + AUTHORITY + "/wifis/u/*");

	private static final Map<Integer, String> URI_TYPES = Maps.newHashMap();
	
	static {
		URI_TYPES.put(WIFIS, "vnd.android.cursor.dir/vnd.somethingsizzle.wifis");
		URI_TYPES.put(WIFIS_ID, "vnd.android.cursor.item/vnd.somethingsizzle.wifis");
		URI_TYPES.put(WIFIS_UID, "vnd.android.cursor.uitem/vnd.somethingsizzle.wifis");
		URI_TYPES.put(USERS, "vnd.android.cursor.dir/vnd.somethingsizzle.users");
		URI_TYPES.put(USERS_ID, "vnd.android.cursor.item/vnd.somethingsizzle.users");
		URI_TYPES.put(USERS_UID, "vnd.android.cursor.uitem/vnd.somethingsizzle.users");
	}

	private static SizzoUriMatcher matcher = null;

	public static synchronized SizzoUriMatcher getInstance() {
		if (matcher == null) {
			matcher = new SizzoUriMatcher(UriMatcher.NO_MATCH);
			matcher.addURI(AUTHORITY, "wifis", WIFIS);
			matcher.addURI(AUTHORITY, "wifis/#", WIFIS_ID);
			matcher.addURI(AUTHORITY, "wifis/u/*", WIFIS_UID);
		}
		return matcher;
	}

	public static String getUriType(int matchNode) {
		return URI_TYPES.get(matchNode);
	}

	public SizzoUriMatcher(int code) {
		super(code);
	}

}
