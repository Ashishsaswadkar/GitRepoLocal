package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.operations.hungama.HungamaOperation;

/**
 * Response of the {@link HungamaOperation} for getting internal protocol messaging.
 */
public class Response {
	
	public static final int CODE_RESPONSE_OK = 1;

	@SerializedName("code")
	public final int code;
	
	@SerializedName("message")
	public final String message;
	
	@SerializedName("display")
	public final int display;

	public Response(int code, String message, int display) {
		this.code = code;
		this.message = message;
		this.display = display;
	}
	
}
