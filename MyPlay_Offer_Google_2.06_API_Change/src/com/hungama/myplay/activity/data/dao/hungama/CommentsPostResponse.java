package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.Utils;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class CommentsPostResponse implements Serializable {	
	public static final String KEY_CODE = "code";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DISPLAY = "display";
	public static final String KEY_POINTS_EARNED = "points_earned";	
	public static final String KEY_BADGE_URL = "badge_url";
	

	@SerializedName(KEY_CODE)
	private final int code;

	@SerializedName(KEY_MESSAGE)
	private final String message;

	@SerializedName(KEY_DISPLAY)
	private final int display;
	
	@SerializedName(KEY_POINTS_EARNED)
	private final int pointsEarned;
	
	@SerializedName(KEY_BADGE_URL)
	private final String badgeUrl; 
	
	public CommentsPostResponse(int code, String message, int display, int pointsEarned, String badgeUrl) {
		this.code = code;
		this.message = message;		
		this.display = display;
		this.pointsEarned = pointsEarned;
		this.badgeUrl = badgeUrl;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public int getDisplay() {
		return display;
	}

	public int getPointsEarned() {
		return pointsEarned;
	}

	public String getBadgeUrl() {
		return badgeUrl;
	}


}
