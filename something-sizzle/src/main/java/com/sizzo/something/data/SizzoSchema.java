package com.sizzo.something.data;
public  class SizzoSchema {
	public static final String TABLE_CONTENT = "content";
	public static final String TABLE_PROPERTY = "property";
	public static final String TABLE_CONENT2CONTENT = "content2content";
	public static final String TABLE_FILE = "file";

	public static enum Content {
		_ID, UID, TITLE, DETAIL, TYPE, CRATEDDATE, LASTUPDATEDDATE;
	}

	public static enum Property {
		_ID, UID, TITLE, NAME, VALUE;
	}

	public static enum Content2Content {
		_ID, UIDFROM, RELATION, UIDTO, DETAIL, CRATEDDATE, LASTUPDATEDDATE;
	}

	public static enum FILE {
		_ID, UID, TITLE, NAME, EXT, MIMETYPE, CONTENT, CRATEDDATE, LASTUPDATEDDATE;
	}

}
