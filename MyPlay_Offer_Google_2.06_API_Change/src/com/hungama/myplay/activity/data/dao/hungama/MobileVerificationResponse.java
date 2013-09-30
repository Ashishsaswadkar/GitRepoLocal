package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class MobileVerificationResponse implements Serializable {
	
	public static final String KEY_CODE = "code";
	public static final String KEY_MSISDN = "msisdn";
	public static final String KEY_MESSAGE = "message";

	@Expose
	@SerializedName(KEY_CODE)
	private final String code;
	@Expose
	@SerializedName(KEY_MSISDN)
	private final String msisdn;
	@Expose
	@SerializedName(KEY_MESSAGE)
	private final String message;	
	
	private MobileOperationType mobileOperationType = null; 
	
	public MobileVerificationResponse(String code, String msisdn, String message) {
		this.code = code;
		this.msisdn = msisdn;
		this.message = message;
	}

	public String getCode() {
		return code;
	}	

	public String getMsisdn() {
		return msisdn;
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
