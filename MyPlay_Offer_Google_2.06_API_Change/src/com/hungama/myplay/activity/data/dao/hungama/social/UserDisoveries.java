package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class UserDisoveries {

	public static final String KEY_DISCOVERY_COUNT = "discovery_count";
	public static final String KEY_DISCOVERIES = "data";
	
	@SerializedName(KEY_DISCOVERY_COUNT)
	public final int discoveryCount;
	@SerializedName(KEY_DISCOVERIES)
	public final List<UserDiscover> userDiscoveries;
	
	public UserDisoveries(int discoveryCount, List<UserDiscover> userDiscoveries) {
		this.discoveryCount = discoveryCount;
		this.userDiscoveries = userDiscoveries;
	}
}
