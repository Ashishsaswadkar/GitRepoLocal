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
import com.hungama.myplay.activity.data.dao.hungama.CommentsPostResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Receives the profile for the given user.
 */
public class GCMRegIDOperation extends SocialOperation {
	
	public static final String RESULT_KEY_GCM_REG_ID = "result_key_gcm_reg_id";
	
	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mDeviceId;
	
	public GCMRegIDOperation(String serviceUrl, String authKey, String deviceId, String userId) {
		
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mDeviceId = deviceId;
		this.mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.GCM_REG_ID;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		String serviceUrl = mServiceUrl + URL_SEGMENT_GCM_REG_ID
							+ "device_id" + EQUALS + mDeviceId + AMPERSAND
							+ "device_os" + EQUALS + "Android" + AMPERSAND
							+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
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
		
			CommentsPostResponse mCommentsPostResponse = gsonParser.fromJson(response, CommentsPostResponse.class);
		
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_GCM_REG_ID, mCommentsPostResponse);
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			return resultMap;
		
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

}
