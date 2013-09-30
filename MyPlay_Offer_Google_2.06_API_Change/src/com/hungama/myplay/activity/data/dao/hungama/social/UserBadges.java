/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class UserBadges {
	
	public static final String KEY_EARNED_COINS = "earned_badge";
	public static final String KEY_BADGES = "data";
	
	@SerializedName(KEY_EARNED_COINS)
	public final int earnedBadges;
	@SerializedName(KEY_BADGES)
	public final List<Badge> badges;
	
	public UserBadges(int earnedBadges, List<Badge> badges) {
		this.earnedBadges = earnedBadges;
		this.badges = badges;
	}

}
