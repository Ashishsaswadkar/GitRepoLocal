package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class MobileVerificationCountryCheckResponse implements Serializable {
	
	public static final String KEY_CODE = "code";
	public static final String KEY_INDIA = "india";
	public static final String KEY_MESSAGE = "message";

	@Expose
	@SerializedName(KEY_CODE)
	private final int code;
	@Expose
	@SerializedName(KEY_INDIA)
	private final int india;
	@Expose
	@SerializedName(KEY_MESSAGE)
	private final String message;	
	
	private MobileOperationType mobileOperationType = null; 
	
	public MobileVerificationCountryCheckResponse(int code, int india, String message) {
		this.code = code;
		this.india = india;
		this.message = message;
	}

	public int getCode() {
		return code;
	}	

	public boolean isIndia() {
		return india == 1 ? true : false;
	}

	public String getMessage() {
		return message;
	}
	
	public MobileOperationType getMobileOperationType() {
		return mobileOperationType;
	}

	public void setMobileOperationType(MobileOperationType mobileOperationType) {
		this.mobileOperationType = mobileOperationType;
	}
}
