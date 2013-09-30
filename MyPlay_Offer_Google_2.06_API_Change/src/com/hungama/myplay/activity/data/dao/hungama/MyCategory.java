package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.Utils;

/**
 * Users Categories Preferences given from Hungama server to filter {@link MediaItem}s.
 */
public class MyCategory implements Serializable {
	
	public static final String KEY_CATEGORY_ID = "category_id";
	public static final String KEY_CATEGORY_NAME = "category_name";
	
	
	@SerializedName(KEY_CATEGORY_ID)
	private final String category_id;
	@SerializedName(KEY_CATEGORY_NAME)
	private final String category_name;
	
	public MyCategory(String category_id, String category_name) {
		this.category_id = category_id;
		this.category_name = category_name;
	}

	public long getId() {
		return Long.valueOf(category_id);
	}

	public String getName() {
		return category_name;
	}
	

}
