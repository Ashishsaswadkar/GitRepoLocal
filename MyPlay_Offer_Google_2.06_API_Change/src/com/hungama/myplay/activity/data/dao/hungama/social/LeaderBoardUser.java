/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama.social;

import com.google.gson.annotations.SerializedName;

public class LeaderBoardUser {
	
	public static final String KEY_ID = "user_id";
	public static final String KEY_NAME = "user_name";
	public static final String KEY_PROFILE_IMAGE_URL = "user_profile";
	public static final String KEY_RANK = "rank";
	public static final String KEY_TYPE = "type";
	public static final String KEY_BADGE_IMAGE_URL = "badge_url";
	public static final String KEY_POINT_TOTAL = "point_total";
	
	@SerializedName(KEY_ID)
	public final long id;
	@SerializedName(KEY_NAME)
	public final String name;
	@SerializedName(KEY_PROFILE_IMAGE_URL)
	public final String profileImageUrl;
	@SerializedName(KEY_RANK)
	public final int rank;
	@SerializedName(KEY_TYPE)
	public final String type;
	@SerializedName(KEY_BADGE_IMAGE_URL)
	public final String badgeImageUrl;
	@SerializedName(KEY_POINT_TOTAL)
	public final long totalPoint;
	
	public LeaderBoardUser(long id, String name, String profileImageUrl,
						   int rank, String type, String badgeImageUrl, long totalPoint) {
		this.id = id;
		this.name = name;
		this.profileImageUrl = profileImageUrl;
		this.rank = rank;
		this.type = type;
		this.badgeImageUrl = badgeImageUrl;
		this.totalPoint = totalPoint;
	}
}
