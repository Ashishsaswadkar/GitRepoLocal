package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class Subscription implements Serializable {
	
	public static final String KEY_PLAN_ID = "plan_id";
	public static final String KEY_USER_ID = "user_id";
	
	
	@Expose
	@SerializedName(KEY_PLAN_ID)
	private final long plan_id;
	@Expose
	@SerializedName(KEY_USER_ID)
	private final long user_id;	
	
	private SubscriptionType subscriptionType = null;
	
	
	public Subscription(long plan_id, long user_id) {
		this.plan_id = plan_id;
		this.user_id = user_id;		
	}

	public long getPlan_id() {
		return plan_id;
	}

	public long getUser_id() {
		return user_id;
	}

	public SubscriptionType getSubscriptionType() {
		return subscriptionType;
	}

	public void setSubscriptionType(SubscriptionType subscriptionType) {
		this.subscriptionType = subscriptionType;
	}

	
}
