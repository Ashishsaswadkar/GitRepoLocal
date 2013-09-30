/**
 * 
 */
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
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * @author Idan
 *
 */
public class RemoveFromFavoriteOperation extends SocialOperation {

	public static final String RESULT_KEY_REMOVE_FROM_FAVORITE = "result_key_remove_from_favorite";
	
	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mContentId;
	private final String mMediaType;
	
	public RemoveFromFavoriteOperation(String serviceUrl, String authKey, String userId, String mediaType, String contentId) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mMediaType = mediaType;
		this.mContentId = contentId;
	}
	
	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_REMOVE_FROM_FAVORITE
				+ PARAMS_AUTH_KEY + EQUALS + mAuthKey + AMPERSAND
				+ PARAMS_CONTENT_ID + EQUALS + mContentId + AMPERSAND
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_MEDIA_TYPE + EQUALS + mMediaType;
		
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
		
			BaseHungamaResponse hungamaResponse = gsonParser.fromJson(response, BaseHungamaResponse.class);
		
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_REMOVE_FROM_FAVORITE, hungamaResponse);
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			return resultMap;
		
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

}
