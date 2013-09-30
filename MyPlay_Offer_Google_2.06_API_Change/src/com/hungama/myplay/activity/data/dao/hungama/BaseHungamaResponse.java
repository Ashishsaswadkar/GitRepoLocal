package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

/**
 * Base Response Class for Hungama Operations
 */
public class BaseHungamaResponse implements Serializable {
	
	public static final String KEY_CODE = "code";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DISPLAY = "display";
	
	

	@SerializedName(KEY_CODE)
	private final int code;

	@SerializedName(KEY_MESSAGE)
	private final String message;

	@SerializedName(KEY_DISPLAY)
	private final int display; 
	
	public BaseHungamaResponse(int code, String message, int display) {
		this.code = code;
		this.message = message;		
		this.display = display;
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
	
}
