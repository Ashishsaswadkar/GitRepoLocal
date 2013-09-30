package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileBadges;
import com.hungama.myplay.activity.data.dao.hungama.social.ShareURL;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class SocialGetUrlOperation extends SocialOperation {

	public static final String RESULT_KEY_GET_SOCIAL_URL = "result_key_get_social_url";
	
	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mContentId;
	private final String mType; //(track/album/playlist/video)
	
	public SocialGetUrlOperation(String serviceUrl, String authKey, String userId, String contentId, String type) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mContentId = contentId;
		this.mType = type.toLowerCase();
	}
	
	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_GET_URL;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_GET_URL
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_AUTH_KEY + EQUALS + mAuthKey + AMPERSAND
				+ "type" + EQUALS + mType + AMPERSAND
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
		
		if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
		
		Gson gsonParser = new Gson();
		
		try {
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
		
			ShareURL shareURL = gsonParser.fromJson(response, ShareURL.class);
		
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_GET_SOCIAL_URL, shareURL);
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			return resultMap;
		
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

}
