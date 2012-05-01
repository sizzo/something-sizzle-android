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

	public static final Uri WIFIS_URI = Uri.parse("content://" + AUTHORITY + "/wifis");

	private static final Map<Integer, String> URI_TYPES = Maps.newHashMap();
	static {
		URI_TYPES.put(WIFIS, "vnd.android.cursor.dir/vnd.somethingsizzle.wifis");
		URI_TYPES.put(WIFIS_ID, "vnd.android.cursor.item/vnd.somethingsizzle.wifis");
	}

	private static SizzoUriMatcher matcher = null;

	public static synchronized SizzoUriMatcher getInstance() {
		if (matcher == null) {
			matcher = new SizzoUriMatcher(UriMatcher.NO_MATCH);
			matcher.addURI(AUTHORITY, "wifis", WIFIS);
			matcher.addURI(AUTHORITY, "wifis/#", WIFIS_ID);
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
