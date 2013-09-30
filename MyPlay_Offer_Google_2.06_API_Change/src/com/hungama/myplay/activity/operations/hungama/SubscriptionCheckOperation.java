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
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionCheckResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class SubscriptionCheckOperation extends HungamaOperation {
	
	private static final String TAG = "SubscriptionCheckOperation";

	public static final String RESPONSE_KEY_SUBSCRIPTION_CHECK = "response_key_subscription_check";
	
	private final Context mContext;
	private final String mServerUrl;	
	private final String mUserId;
	private final String mAuthKey;
	private final String mGoogleEmailId;
	
	public SubscriptionCheckOperation(Context context, String serverUrl, String userId, String authKey, String googleEmailId) {
		mContext = context;
		mServerUrl = serverUrl;
		mUserId = userId;
		mAuthKey = authKey;
		mGoogleEmailId = googleEmailId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		return mServerUrl + HungamaOperation.URL_SEGMENT_SUBSCRIPTION_CHECK_SUBSCRIPTION;
	}

	@Override
	public String getRequestBody() {
		
		StringBuilder urlBuilder = new StringBuilder();
		
		urlBuilder
		.append(PARAMS_AUTH_KEY).append(EQUALS).append(mAuthKey).append(HungamaOperation.AMPERSAND)
		.append(PARAMS_USER_ID).append(EQUALS).append(mUserId);
		
		if(mGoogleEmailId != null){
			urlBuilder.append(AMPERSAND).append(PARAMS_GOOGLE_EMAIL_ID).append(EQUALS).append(mGoogleEmailId);
		}
		
		return urlBuilder.toString();
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
				
				// gets the response object, and caches it.
				SubscriptionCheckResponse subscriptionCheckResponse = (SubscriptionCheckResponse) gson.fromJson(response, SubscriptionCheckResponse.class);
				
				DataManager dataManager = DataManager.getInstance(mContext);
				
				// if store in cache succeeded - set UserHasSubscriptionPlan=true and plan validity date in ApplicationConfigurations
				dataManager.storeCurrentSubscriptionPlan(subscriptionCheckResponse);
				
				resultMap.put(RESPONSE_KEY_SUBSCRIPTION_CHECK, subscriptionCheckResponse);

			
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();
			
		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		}
					
			
		return resultMap;
	}

}
