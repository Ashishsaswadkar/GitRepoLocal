package com.hungama.myplay.activity.data.dao.hungama;

public interface CategoryTypeObject {
	public static final String TYPE_CATEGORY = "type_category";
	public static final String TYPE_GENRE = "type_genre";
	
	public String getType();
	
	public long getId();
	
	public String getName();
	
}
