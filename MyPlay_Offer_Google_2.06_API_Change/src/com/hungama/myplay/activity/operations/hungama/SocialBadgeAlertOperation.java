package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class SocialBadgeAlertOperation extends SocialOperation {

	public static final String RESULT_KEY_BADGE_ALERT = "result_key_badge_alert";
	
	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mContentId;
	private final String mType; //album / song / video / playlis
	private final String mAction; // watch_video / create_playlist / musicstreaming / music_video_download / music_subscription / invite_friends
	
	public SocialBadgeAlertOperation(String serviceUrl, String authKey, String userId, String contentId, String type, String action) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mContentId = contentId;
		this.mType = type.toLowerCase();
		this.mAction = action;
	}
	
	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_BADGE_ALERT;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_BADGE_ALERT
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_AUTH_KEY + EQUALS + mAuthKey + AMPERSAND
				+ "type" + EQUALS + mType + AMPERSAND
				+ "action" + EQUALS + mAction + AMPERSAND
				+ "content_id" + EQUALS + mContentId;
		return serviceUrl;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException, InvalidRequestParametersException,
																	 InvalidRequestTokenException, OperationCancelledException {
		response = removeUglyResponseWrappingObjectFromResponse(response);
		
		try {
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			return resultMap;
		
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

}
