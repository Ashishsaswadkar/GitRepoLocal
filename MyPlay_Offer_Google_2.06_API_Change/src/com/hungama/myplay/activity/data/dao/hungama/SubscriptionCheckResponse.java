package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class SubscriptionCheckResponse implements Serializable {
	
	public static final String KEY_CODE = "code";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DISPLAY = "display";
	public static final String KEY_PLAN = "plan";
	
	@Expose
	@SerializedName(KEY_CODE)
	private final String code;
	@Expose
	@SerializedName(KEY_MESSAGE)
	private final String message;
	@Expose
	@SerializedName(KEY_DISPLAY)
	private final String display;
	@Expose
	@SerializedName(KEY_PLAN)
	private Plan plan;

	private SubscriptionType subscriptionType = null; 
	
	public SubscriptionCheckResponse(String code, String status, String order_id, String message, String display, Plan plan) {
		this.code = code;
		this.message = message;		
		this.display = display;
		this.plan = plan;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public String getDisplay() {
		return display;
	}

	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}
	
	public SubscriptionType getSubscriptionType() {
		return subscriptionType;
	}
	
	public void setSubscriptionType(SubscriptionType type) {
		this.subscriptionType = type;
	}
	
}
