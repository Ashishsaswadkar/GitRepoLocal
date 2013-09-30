/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama.social;

import com.google.gson.annotations.SerializedName;

public class Badge {
	
	public static final String KEY_NAME = "badge";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_IMAGE_URL = "badge_url";
	
	@SerializedName(KEY_NAME)
	public final String name;
	@SerializedName(KEY_DESCRIPTION)
	public final String description;
	@SerializedName(KEY_IMAGE_URL)
	public final String imageUrl;
	
	public Badge(String name, String description, String imageUrl) {
		this.name = name;
		this.description = description;
		this.imageUrl = imageUrl;
	}
}
