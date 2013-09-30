package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class Comment implements Serializable {
		
	public static final String KEY_USER_ID = "user_id";
	public static final String KEY_USER_NAME = "user_name";
	public static final String KEY_PHOTO_URL = "photo_url";
	public static final String KEY_COMMENT = "comment";
	public static final String KEY_TIME = "time";
	
	@Expose
	@SerializedName(KEY_USER_ID)
	protected final long userId;
	@Expose
	@SerializedName(KEY_USER_NAME)
	protected final String userName;
	@Expose
	@SerializedName(KEY_PHOTO_URL)
	protected final String photoUrl;
	@Expose
	@SerializedName(KEY_COMMENT)
	protected final String comment;
	@Expose
	@SerializedName(KEY_TIME)
	protected final String time;
	
	public Comment(long userId, String userName, String photoUrl, String comment, String time) {
		
		this.userId = userId;
		this.userName = userName;
		this.photoUrl = photoUrl;
		this.comment = comment;
		this.time = time;
	}

	public long getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public String getComment() {
		return comment;
	}

	public String getTime() {
		return time;
	}

	
}
	

