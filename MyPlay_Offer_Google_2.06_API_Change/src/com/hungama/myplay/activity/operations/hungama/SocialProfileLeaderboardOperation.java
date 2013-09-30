package com.hungama.myplay.activity.operations.hungama;

import java.util.ArrayList;
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
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileLeaderboard;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class SocialProfileLeaderboardOperation extends SocialOperation {
	
	public static final String RESULT_KEY_PROFILE_LEADERBOARD = "result_key_profile_leaderboard";
	public static final String RESULT_KEY_PROFILE_LEADERBOARD_TYPE = "result_key_profile_leaderboard_type";
	public static final String RESULT_KEY_PROFILE_LEADERBOARD_PERIOD = "result_key_profile_leaderboard_period";
	
	private static final String KEY_PARAMS_TYPE = "type";
	private static final String KEY_PARAMS_PERIOD = "period";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final ProfileLeaderboard.TYPE mType;
	private final ProfileLeaderboard.PERIOD mPeriod;
	
	public SocialProfileLeaderboardOperation(String serviceUrl, String authKey, String userId, 
											 ProfileLeaderboard.TYPE type, 
											 ProfileLeaderboard.PERIOD period) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mType = type;
		this.mPeriod = period;
	}
	
	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_LEADERBOARD;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_PROFILE_LEADERBOARD
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ KEY_PARAMS_TYPE + EQUALS + mType.name + AMPERSAND;
		
		if (mPeriod == ProfileLeaderboard.PERIOD.SEVEN) {
			serviceUrl = serviceUrl + KEY_PARAMS_PERIOD + EQUALS + mPeriod.name + AMPERSAND;
		}
		
		serviceUrl = serviceUrl + PARAMS_AUTH_KEY + EQUALS + mAuthKey;
		
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
		
			ProfileLeaderboard profileLeaderboard = gsonParser.fromJson(response, ProfileLeaderboard.class);
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_PROFILE_LEADERBOARD, profileLeaderboard);
			resultMap.put(RESULT_KEY_PROFILE_LEADERBOARD_TYPE, mType);
			resultMap.put(RESULT_KEY_PROFILE_LEADERBOARD_PERIOD, mPeriod);
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			return resultMap;
		
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

}
