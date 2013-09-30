package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.Utils;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class MyPreferencesResponse implements Serializable {
	
	public static final String KEY_CODE = "code";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DISPLAY = "display";
	public static final String KEY_MY_CATEGORIES = "preference";
	

	@SerializedName(KEY_CODE)
	private final int code;

	@SerializedName(KEY_MESSAGE)
	private final String message;

	@SerializedName(KEY_DISPLAY)
	private final int display;
	@SerializedName(KEY_MY_CATEGORIES)
	private final List<MyCategory> mycategories; 
	
	public MyPreferencesResponse(int code, String message, int display, List<MyCategory> mycategories) {
		this.code = code;
		this.message = message;		
		this.display = display;
		this.mycategories = mycategories;
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
	
	public List<MyCategory> getMycategories() {
		return mycategories;
	}
	
	public int getChildCount() {
		if (!Utils.isListEmpty(mycategories)){
			return mycategories.size();
		}
		return 0;
	}
	
}
