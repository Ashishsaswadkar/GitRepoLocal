/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class UserLeaderBoardUsers {
	
	public static final String KEY_USER_RANK = "user_rank";
	public static final String KEY_LEADERBOARD_USERS = "data";

	@SerializedName(KEY_USER_RANK)
	public final int userRank;
	@SerializedName(KEY_LEADERBOARD_USERS)
	public final List<LeaderBoardUser> leaderBoardUsers;
	
	public UserLeaderBoardUsers(int userRank, List<LeaderBoardUser> leaderBoardUsers) {
		this.userRank = userRank;
		this.leaderBoardUsers = leaderBoardUsers;
	}
}
