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
import com.hungama.myplay.activity.data.dao.hungama.MyStreamSettingsResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Receives the profile for the given user.
 */
public class MyStreamSettingsOperation extends SocialOperation {
	
	public static final String RESULT_KEY_MY_STREAM_SETTINGS = "result_key_my_stream_settings";
	
	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final boolean mIsUpdate;
	private final String mKey;
	private final Integer mValue;
	
	public MyStreamSettingsOperation(String serviceUrl, String authKey, String userId, boolean isUpdate, String key, Integer value) {
		
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mIsUpdate = isUpdate;
		this.mKey = key;
		this.mValue = value;
	}

	@Override
	public int getOperationId() {
		
		if(mIsUpdate){
			return OperationDefinition.Hungama.OperationId.MY_STREAM_SETTINGS_UPDATE;
		}else{
			return OperationDefinition.Hungama.OperationId.MY_STREAM_SETTINGS;	
		}
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		String serviceUrl = "";
		
		if(mIsUpdate){
			serviceUrl = mServiceUrl + URL_SEGMENT_MY_STREAM_SETTINGS_UPDATE
					+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
					+ PARAMS_AUTH_KEY + EQUALS + mAuthKey + AMPERSAND
					+ mKey + EQUALS + mValue;	
			
		}else{
			serviceUrl = mServiceUrl + URL_SEGMENT_MY_STREAM_SETTINGS
					+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
					+ PARAMS_AUTH_KEY + EQUALS + mAuthKey;	
		}
		
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
		
			MyStreamSettingsResponse myStreamSettingsResponse = gsonParser.fromJson(response, MyStreamSettingsResponse.class);
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_MY_STREAM_SETTINGS, myStreamSettingsResponse);
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			return resultMap;
		
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

}
