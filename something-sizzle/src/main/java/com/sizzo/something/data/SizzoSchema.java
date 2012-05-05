package com.sizzo.something.data;

import android.provider.BaseColumns;

public class SizzoSchema {
	private static interface BasicColumns {
		public static final String _ID = BaseColumns._ID;
	}

	public static class CONTENTS {
		public static final String TABLE = "contents";

		public interface Columns extends BasicColumns {
			public static final String UID = "UID";
			public static final String TITLE = "TITLE";
			public static final String DETAIL = "DETAIL";
			public static final String TYPE = "TYPE";
			public static final String CRATEDDATE = "CRATEDDATE";
			public static final String LASTUPDATEDDATE = "LASTUPDATEDDATE";
			public static final String RANK="RANK";
		}
		public enum Types{
			WIFI,ARTICLE
		}
	}

	public static class  CONTENT2CONTENTS{
		public static final String TABLE = "content2contents";

		public interface Columns extends BasicColumns {
			public static final String UIDFROM = "UIDFROM";
			public static final String UIDTO = "UIDTO";
			public static final String CRATEDDATE = "CRATEDDATE";
			public static final String LASTUPDATEDDATE = "LASTUPDATEDDATE";
			public static final String RANK="RANK";
		}
	}
	
	public static class USERS {
		public static final String TABLE = "users";
		
		public interface Columns extends BasicColumns {
			public static final String UID = "UID";
			public static final String TITLE = "TITLE";
			public static final String DETAIL = "DETAIL";
			public static final String CRATEDDATE = "CRATEDDATE";
			public static final String LASTUPDATEDDATE = "LASTUPDATEDDATE";
			public static final String RANK="RANK";
		}
	}
	
	public static class  USER2USERS{
		public static final String TABLE = "user2users";

		public interface Columns extends BasicColumns {
			public static final String UIDFROM = "UIDFROM";
			public static final String UIDTO = "UIDTO";
			public static final String CRATEDDATE = "CRATEDDATE";
			public static final String LASTUPDATEDDATE = "LASTUPDATEDDATE";
			public static final String RANK="RANK";
		}
	}
	
	public static class  USER2CONTENTS{
		public static final String TABLE = "user2contents";

		public interface Columns extends BasicColumns {
			public static final String UIDFROM = "UIDFROM";
			public static final String UIDTO = "UIDTO";
			public static final String CRATEDDATE = "CRATEDDATE";
			public static final String LASTUPDATEDDATE = "LASTUPDATEDDATE";
			public static final String RANK="RANK";
		}
	}
	
	public static class  PROPERTIES{
		public static final String TABLE = "properties";
		
		public interface Columns extends BasicColumns {
			public static final String F_ID = "F_ID";
			public static final String NAME = "NAME";
			public static final String VALUE = "VALUE";
			public static final String TYPE = "TYPE";
		}
		public enum Types{
			CONTENTS,CONTENT2CONTENTS,USERS,USER2USERS,USER2CONTENTS
		}
	}
	
}
