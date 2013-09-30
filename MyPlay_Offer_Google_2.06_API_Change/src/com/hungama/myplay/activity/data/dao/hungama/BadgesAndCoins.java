package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.hungama.myplay.activity.operations.hungama.HungamaOperation;

/**
 * Response of the {@link HungamaOperation} for getting internal protocol messaging.
 */
public class BadgesAndCoins implements Serializable{
	
	public static final int CASE_COINS_2_LINES = 1;
	public static final int CASE_COINS_3_LINES = 2;
	public static final int CASE_COINS_2_LINES_AND_BADGE = 3;
	public static final int CASE_COINS_3_LINES_AND_BADGE = 4;	

	private int pointsEarned;
	private int badgesEarned;
	private int displayCase;
	private String message;
	private String nextDescription;
	private String badgeName;
	private String badgeUrl;
	
	
	public BadgesAndCoins() {}
		

	public int getPointsEarned() {
		return pointsEarned;
	}

	public void setPointsEarned(int pointsEarned) {
		this.pointsEarned = pointsEarned;
	}

	public int getBadgesEarned() {
		return badgesEarned;
	}

	public void setBadgesEarned(int badgesEarned) {
		this.badgesEarned = badgesEarned;
	}

	public int getDisplayCase() {
		return displayCase;
	}

	public void setDisplayCase(int displayCase) {
		this.displayCase = displayCase;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getNextDescription() {
		return nextDescription;
	}

	public void setNextDescription(String nextDescription) {
		this.nextDescription = nextDescription;
	}

	public String getBadgeName() {
		return badgeName;
	}

	public void setBadgeName(String badgeName) {
		this.badgeName = badgeName;
	}

	public String getBadgeUrl() {
		return badgeUrl;
	}

	public void setBadgeUrl(String badgeUrl) {
		this.badgeUrl = badgeUrl;
	}
	
}
