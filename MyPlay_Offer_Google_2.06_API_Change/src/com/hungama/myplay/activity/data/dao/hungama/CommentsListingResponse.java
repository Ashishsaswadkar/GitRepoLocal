package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.Utils;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class CommentsListingResponse implements Serializable {	
	public static final String KEY_CODE = "code";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DISPLAY = "display";
	public static final String KEY_COMMENTS_COUNT = "comments_count";	
	public static final String KEY_COMMENTS = "comments";
	

	@SerializedName(KEY_CODE)
	private final int code;

	@SerializedName(KEY_MESSAGE)
	private final String message;

	@SerializedName(KEY_DISPLAY)
	private final boolean display;
	
	@SerializedName(KEY_COMMENTS_COUNT)
	private final int commentsCount;
	
	@SerializedName(KEY_COMMENTS)
	private final List<Comment> comments; 
	
	public CommentsListingResponse(int code, String message, boolean display, int commentsCount, List<Comment> comments) {
		this.code = code;
		this.message = message;		
		this.display = display;
		this.commentsCount = commentsCount;
		this.comments = comments;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public boolean isDisplay() {
		return display;
	}

	public int getTotalCount() {
		return commentsCount;
	}

	public List<Comment> getMyData() {
		return comments;
	}

}
