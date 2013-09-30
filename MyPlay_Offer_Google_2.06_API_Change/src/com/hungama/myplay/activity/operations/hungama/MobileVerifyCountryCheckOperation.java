package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.MobileVerificationCountryCheckResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class MobileVerifyCountryCheckOperation extends HungamaOperation {
	
	private static final String TAG = "MobileVerifyCountryCheckOperation";
	
	public static final String RESPONSE_KEY_MOBILE_VERIFICATION_COUNTRY_CHECK = "response_key_mobile_verification_country_check";
	
	private final String mServerUrl;
	private final String mMsisdn;
	private final String mAuthKey;
	
	public MobileVerifyCountryCheckOperation(String serverUrl, String msisdn, String authKey) {
		mServerUrl = serverUrl;
		mMsisdn = msisdn;
		mAuthKey = authKey;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION_COUNTRY_CHECK;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {		
				
		return mServerUrl + HungamaOperation.URL_SEGMENT_COUNTRY_CHECK;
						
	}

	@Override
	public String getRequestBody() {
		return PARAMS_MSISDN + EQUALS + mMsisdn + AMPERSAND +
				PARAMS_AUTH_KEY + EQUALS + mAuthKey;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
							InvalidRequestParametersException, InvalidRequestTokenException,
							OperationCancelledException {

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
		
		try {
				response = response.replace("{\"response\":", "");
				response = response.substring(0, response.length() -1);
				
				MobileVerificationCountryCheckResponse countryCheckResponse = (MobileVerificationCountryCheckResponse) gson.fromJson(response, MobileVerificationCountryCheckResponse.class);
				resultMap.put(RESPONSE_KEY_MOBILE_VERIFICATION_COUNTRY_CHECK, countryCheckResponse);
			
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();
			
		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		}				
			
		return resultMap;
	}

}
