package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.VersionCheckResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class VersionCheckOperation extends HungamaOperation {
	
	private static final String TAG = "VersionCheckOperation";

	public static final String RESPONSE_KEY_VERSION_CHECK = "response_key_version_check";
	
	private final String mServerUrl;	
	private final String mUserId;
	private final String mAuthKey;
	private final String mReferralId;	
	
	public VersionCheckOperation(String serverUrl, String userId, String authKey, String referalId) {
		mServerUrl = serverUrl;
		mUserId = userId;
		mAuthKey = authKey;
		mReferralId = referalId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.VERSION_CHECK;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		return mServerUrl + HungamaOperation.URL_SEGMENT_VERSION_CHECK;
	}

	@Override
	public String getRequestBody() {
		return 	PARAMS_AUTH_KEY + EQUALS + mAuthKey + 
				HungamaOperation.AMPERSAND + 
				PARAMS_USER_ID + EQUALS + mUserId + 
				HungamaOperation.AMPERSAND +  
				"device" + EQUALS + "android" +
				HungamaOperation.AMPERSAND +
				PARAMS_REFERRAL_ID + EQUALS + mReferralId;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
							InvalidRequestParametersException, InvalidRequestTokenException,
							OperationCancelledException {

		Gson gson = new Gson();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
		
		try {
				response = response.replace("{\"response\":", "");
				response = response.substring(0, response.length() -1);
				
				VersionCheckResponse versionCheckResponse = (VersionCheckResponse) gson.fromJson(response, VersionCheckResponse.class);
				resultMap.put(RESPONSE_KEY_VERSION_CHECK, versionCheckResponse);

			
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();
			
		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		}
					
			
		return resultMap;
	}

}
