package com.sizzo.something.data;

import android.provider.BaseColumns;

public class SizzoSchema {
	private static interface BasicColumns {
		public static final String _ID = BaseColumns._ID;
		public static final String UID = "UID";
		public static final String CRATEDDATE = "CRATEDDATE";
		public static final String LASTUPDATEDDATE = "LASTUPDATEDDATE";
	}

	public static class Contents {
		public static final String TABLE = "contents";

		public interface Columns extends BasicColumns {
			public static final String TITLE = "TITLE";
			public static final String DETAIL = "DETAIL";
			public static final String TYPE = "TYPE";
		}
	}

}
