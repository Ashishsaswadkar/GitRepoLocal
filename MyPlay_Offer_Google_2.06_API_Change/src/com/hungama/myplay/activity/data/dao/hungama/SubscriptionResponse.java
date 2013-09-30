package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class SubscriptionResponse implements Serializable {
	
	public static final String KEY_CODE = "code";
	public static final String KEY_STATUS = "status";
	public static final String KEY_ORDER_ID = "order_id";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DISPLAY = "display";
	public static final String KEY_PLAN = "plan";
	public static final String KEY_TYPE = "type";
	
	@Expose
	@SerializedName(KEY_CODE)
	private final String code;
	@Expose
	@SerializedName(KEY_STATUS)
	private final String status;
	@Expose
	@SerializedName(KEY_ORDER_ID)
	private final String order_id;
	@Expose
	@SerializedName(KEY_MESSAGE)
	private final String message;
	@Expose
	@SerializedName(KEY_DISPLAY)
	private final String display;
	@Expose
	@SerializedName(KEY_PLAN)
	private List<Plan> plan;
	
	private SubscriptionType subscriptionType = null; 
	
	public SubscriptionResponse(String code, String status, String order_id, String message, String display, List<Plan> plan) {
		this.code = code;
		this.status = status;
		this.order_id = order_id;
		this.message = message;		
		this.display = display;
		this.plan = plan;
	}

	public String getCode() {
		return code;
	}

	public String getStatus() {
		return status;
	}

	public String getOrder_id() {
		return order_id;
	}

	public String getMessage() {
		return message;
	}

	public String getDisplay() {
		return display;
	}
	
	public List<Plan> getPlan() {
		return plan;
	}
	
	public void setPlan(List<Plan> plan) {
		this.plan = plan;
	}

	public SubscriptionType getSubscriptionType() {
		return subscriptionType;
	}
	
	public void setSubscriptionType(SubscriptionType type) {
		this.subscriptionType = type;
	}
	
}
