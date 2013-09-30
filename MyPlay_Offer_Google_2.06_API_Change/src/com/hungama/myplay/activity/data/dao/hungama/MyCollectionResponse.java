package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.Utils;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class MyCollectionResponse implements Serializable {	
	public static final String KEY_CODE = "code";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DISPLAY = "display";
	public static final String KEY_TOTAL_COUNT = "total_count";	
	public static final String KEY_DATA = "data";
	

	@SerializedName(KEY_CODE)
	private final int code;

	@SerializedName(KEY_MESSAGE)
	private final String message;

	@SerializedName(KEY_DISPLAY)
	private final int display;
	
	@SerializedName(KEY_TOTAL_COUNT)
	private final int totalCount;
	
	@SerializedName(KEY_DATA)
	private final List<CollectionItem> myData; 
	
	public MyCollectionResponse(int code, String message, int display, int totalCount, List<CollectionItem> myData) {
		this.code = code;
		this.message = message;		
		this.display = display;
		this.totalCount = totalCount;
		this.myData = myData;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public int getDisplay() {
		return display;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public List<CollectionItem> getMyData() {
		return myData;
	}

}
