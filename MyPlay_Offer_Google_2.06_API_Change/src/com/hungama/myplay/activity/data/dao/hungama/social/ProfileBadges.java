package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ProfileBadges {
	
	public static final String KEY_USER_ID = "user_id";
	public static final String KEY_BADGE_COUNT = "badge_count"; 
	public static final String KEY_CURRENT_BADGE = "current_badge";
	public static final String KEY_BADGES = "badges";

	@SerializedName(KEY_USER_ID)
	public final long userId;
	@SerializedName(KEY_BADGE_COUNT)
	public final int badgeCount;
	@SerializedName(KEY_CURRENT_BADGE)
	public final List<Badge> currentBadge;
	@SerializedName(KEY_BADGES)
	public final List<Badge> badges;
	
	public ProfileBadges(long userId, int badgeCount, List<Badge> currentBadge, List<Badge> badges) {
		this.userId = userId;
		this.badgeCount = badgeCount;
		this.currentBadge = currentBadge;
		this.badges = badges;
	}
}
