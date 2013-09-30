package com.hungama.myplay.activity.operations.hungama;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.hardware.Camera.Size;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.MobileOperationType;
import com.hungama.myplay.activity.data.dao.hungama.MobileVerificationResponse;
import com.hungama.myplay.activity.data.dao.hungama.SearchResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.Video;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class MobileVerifyOperation extends HungamaOperation {
	
	private static final String TAG = "MobileVerifyOperation";
	
	public static final String RESPONSE_KEY_MOBILE_VERIFICATION_MOBILE_VERIFY = "response_key_mobile_verification_mobile_verify";
	public static final String RESPONSE_KEY_MOBILE_VERIFICATION_MOBILE_PASSWORD_VERIFY = "response_key_mobile_verification_mobile_password_verify";
	public static final String RESPONSE_KEY_MOBILE_VERIFICATION_RESEND_PASSWORD = "response_key_mobile_verification_resend_password";
	public static final String RESPONSE_KEY_MOBILE_VERIFICATION = "response_key_mobile_verification";
	
	private final String mServerUrl;
	private final String mMsisdn;
	private final String mPassword;
	private final String mUserId;
	private final MobileOperationType mMobileOperationType;
	private final String mAuthKey;
	
	public MobileVerifyOperation(String serverUrl, String msisdn, String password, String userId, MobileOperationType mobileOperationType, String authKey) {
		mServerUrl = serverUrl;
		mMsisdn = msisdn;
		mPassword = password;
		mUserId = userId;
		mMobileOperationType = mobileOperationType;
		mAuthKey = authKey;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		String segmentType = null;	
		String urlParams = null;
		
		if (mMobileOperationType == MobileOperationType.MOBILE_VERIFY) {
			segmentType = HungamaOperation.URL_SEGMENT_MOBILE_VERIFY;
			urlParams = PARAMS_MSISDN + "=" + mMsisdn + "&" + PARAMS_USER_ID + "=" + mUserId + "&" + PARAMS_AUTH_KEY + "=" + mAuthKey ;
		} else if (mMobileOperationType == MobileOperationType.MOBILE_PASSWORD_VERIFY) {
			segmentType = HungamaOperation.URL_SEGMENT_MOBILE_PASSWORD_VERIFY;
			urlParams = PARAMS_MSISDN + "=" + mMsisdn + "&" + PARAMS_PASSWORD + "=" + mPassword + "&" + PARAMS_AUTH_KEY + "=" + mAuthKey + "&" + PARAMS_USER_ID + "=" + mUserId ;
		} else if (mMobileOperationType == MobileOperationType.RESEND_PASSWORD) {
			segmentType = HungamaOperation.URL_SEGMENT_RESEND_PASSWORD;
			urlParams = PARAMS_MSISDN + "=" + mMsisdn + "&" + PARAMS_AUTH_KEY + "=" + mAuthKey + "&" + PARAMS_USER_ID + "=" + mUserId;
		}
		
		return mServerUrl + segmentType + "?" + urlParams;
				
				
	}

	@Override
	public String getRequestBody() {
		return null;
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
				
				MobileVerificationResponse mobileVerificationResponse = (MobileVerificationResponse) gson.fromJson(response, MobileVerificationResponse.class);
				mobileVerificationResponse.setMobileOperationType(mMobileOperationType);
				resultMap.put(RESPONSE_KEY_MOBILE_VERIFICATION, mobileVerificationResponse);
				if (mMobileOperationType == MobileOperationType.MOBILE_VERIFY) {					
					resultMap.put(RESPONSE_KEY_MOBILE_VERIFICATION_MOBILE_VERIFY, mMobileOperationType);
				} else if (mMobileOperationType == MobileOperationType.MOBILE_PASSWORD_VERIFY) {
					resultMap.put(RESPONSE_KEY_MOBILE_VERIFICATION_MOBILE_PASSWORD_VERIFY, mMobileOperationType);
				} else if (mMobileOperationType == MobileOperationType.RESEND_PASSWORD) {
					resultMap.put(RESPONSE_KEY_MOBILE_VERIFICATION_RESEND_PASSWORD, mMobileOperationType);
				} 

			
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();
			
		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		}
					
			
		return resultMap;
	}

}
