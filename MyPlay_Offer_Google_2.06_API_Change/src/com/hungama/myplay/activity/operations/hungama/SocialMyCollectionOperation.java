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
import com.hungama.myplay.activity.data.dao.hungama.MyCollectionResponse;
import com.hungama.myplay.activity.data.dao.hungama.social.Profile;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Receives the profile for the given user.
 */
public class SocialMyCollectionOperation extends SocialOperation {
	
	public static final String RESULT_KEY_MY_COLLECTION = "result_key_my_collection";
	
	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	
	public static final String STARTINDEX = "startIndex";
	
	public SocialMyCollectionOperation(String serviceUrl, String authKey, String userId) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_MY_COLLECTION;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_PROFILE_MY_COLLECTION
							+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
							+ STARTINDEX + EQUALS + "0" + AMPERSAND
							+ PARAMS_LENGTH + EQUALS + "1000" + AMPERSAND
							+ PARAMS_AUTH_KEY + EQUALS + mAuthKey;
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
		
			MyCollectionResponse mMyCollectionResponse = gsonParser.fromJson(response, MyCollectionResponse.class);
		
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_MY_COLLECTION, mMyCollectionResponse);
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			return resultMap;
		
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

}
