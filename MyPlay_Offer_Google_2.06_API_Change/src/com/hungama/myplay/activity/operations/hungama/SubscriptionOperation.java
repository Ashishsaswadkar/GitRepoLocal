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
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionType;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class SubscriptionOperation extends HungamaOperation {
	
	private static final String TAG = "SubscriptionOperation";
	
	public static final String RESPONSE_KEY_SUBSCRIPTION = "response_key_subscription";
	
	private final String mServerUrl;	
	private final String mUserId;
	private final String mPlanId;
	private final SubscriptionType mSubscriptionType;
	private final String mAuthKey;
	private final String mPlanType;
	private final String mCode;
	private final String mPurchaseToken;
	private final String mGoogleEmailId;
	private final String mAffiliateId;// Inserted by Hungama
	
	public SubscriptionOperation(String serverUrl, String planId, String planType, String userId, SubscriptionType subscriptionType, String authKey, String code, String purchaseToken, String googleEmailId, String affiliateId) {
		mServerUrl = serverUrl;
		mUserId = userId;
		mPlanId = planId;
		mSubscriptionType = subscriptionType;
		mAuthKey = authKey;
		mPlanType = planType;
		mCode = code;
		mPurchaseToken = purchaseToken;
		mGoogleEmailId = googleEmailId;
		mAffiliateId = affiliateId;// Inserted by Hungama
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SUBSCRIPTION;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		String segmentType = null;		
		if (mSubscriptionType == SubscriptionType.PLAN) {
			segmentType = HungamaOperation.URL_SEGMENT_SUBSCRIPTION_PLAN;
		} else if (mSubscriptionType == SubscriptionType.CHARGE) {
			segmentType = HungamaOperation.URL_SEGMENT_SUBSCRIPTION_CHARGE;
		} else if (mSubscriptionType == SubscriptionType.UNSUBSCRIBE) {
			segmentType = HungamaOperation.URL_SEGMENT_SUBSCRIPTION_UNSUBSCRIBE;
		}
		
		return mServerUrl + 
				segmentType;
	}

	@Override
	public String getRequestBody() {
		String params = null;
		if (mSubscriptionType == SubscriptionType.PLAN) {
			params = PARAMS_AUTH_KEY + EQUALS + mAuthKey + 
					HungamaOperation.AMPERSAND + 
					PARAMS_USER_ID + EQUALS + mUserId+
					HungamaOperation.AMPERSAND + 
					PARAMS_AFFILIATE_ID + EQUALS + mAffiliateId;
		} else if (mSubscriptionType == SubscriptionType.CHARGE) {
			params = PARAMS_MEDIA_TYPE + EQUALS + mPlanType + 
					HungamaOperation.AMPERSAND + 
					PARAMS_AUTH_KEY + EQUALS + mAuthKey + 
					HungamaOperation.AMPERSAND + 
					PARAMS_USER_ID + EQUALS + mUserId + 
					HungamaOperation.AMPERSAND + 
					PARAMS_PLAN_ID + EQUALS + mPlanId +
					HungamaOperation.AMPERSAND + 
					PARAMS_AFFILIATE_ID + EQUALS + mAffiliateId +
					HungamaOperation.AMPERSAND + 
					PARAMS_CODE + EQUALS + mCode + 
					HungamaOperation.AMPERSAND + 
					PARAMS_PURCHASE_TOKEN + EQUALS + mPurchaseToken + 
					HungamaOperation.AMPERSAND + 
					PARAMS_GOOGLE_EMAIL_ID + EQUALS + mGoogleEmailId ;
		} else if (mSubscriptionType == SubscriptionType.UNSUBSCRIBE) {
			params = PARAMS_AUTH_KEY + EQUALS + mAuthKey + 
					HungamaOperation.AMPERSAND + 
					PARAMS_USER_ID + EQUALS + mUserId + 
					HungamaOperation.AMPERSAND + 
					PARAMS_PLAN_ID + EQUALS + mPlanId;
		}
		return params;
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
				
				SubscriptionResponse subscriptionResponse = (SubscriptionResponse) gson.fromJson(response, SubscriptionResponse.class);
				subscriptionResponse.setSubscriptionType(mSubscriptionType);
				resultMap.put(RESPONSE_KEY_SUBSCRIPTION, subscriptionResponse);

			
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();
			
		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		}
					
			
		return resultMap;
	}

}
