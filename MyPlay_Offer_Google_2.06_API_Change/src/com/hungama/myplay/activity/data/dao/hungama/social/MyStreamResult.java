package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.data.dao.hungama.social.StreamItem;

/**
 * Holder Class for the MyStream services responses.
 */
public class MyStreamResult {
	
	public final String KEY_CODE = "code";
	public final String KEY_MESSAGE = "message";
	public final String KEY_FOR = "for";
	public final String KEY_DATA = "data";
	
	@SerializedName(KEY_CODE)
	public final int code;
	@SerializedName(KEY_MESSAGE)
	public final String message;
	@SerializedName(KEY_FOR)
	public final String category;
	@SerializedName(KEY_DATA)
	public final List<StreamItem> streamItems;
	
	public MyStreamResult(int code, String message, String category, List<StreamItem> streamItems) {
		this.code = code;
		this.message = message;
		this.category = category;
		this.streamItems = streamItems;
	}

}
