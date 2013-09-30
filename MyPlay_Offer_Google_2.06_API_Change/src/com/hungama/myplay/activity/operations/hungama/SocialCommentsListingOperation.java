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
import com.hungama.myplay.activity.data.dao.hungama.CommentsListingResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Receives the profile for the given user.
 */
public class SocialCommentsListingOperation extends SocialOperation {
	
	public static final String RESULT_KEY_COMMENTS_LISTING = "result_key_comments_listing";
	
	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mContentId;
	private final String mType;
	private final String mStartIndex;
	private final String mLength;
	
	public SocialCommentsListingOperation(String serviceUrl, String authKey,
			String contentId, String type, String startIndex, String length) {
		
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mContentId = contentId;
		this.mType = type;
		this.mStartIndex = startIndex;
		this.mLength = length;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_GET;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_COMMENT_GET
							+ PARAMS_CONTENT_ID + EQUALS + mContentId + AMPERSAND
							+ "type" + EQUALS + mType + AMPERSAND
							+ "start_index" + EQUALS + mStartIndex + AMPERSAND
							+ "length" + EQUALS + mLength + AMPERSAND
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
		
			CommentsListingResponse mCommentsListingResponse = gsonParser.fromJson(response, CommentsListingResponse.class);
		
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_COMMENTS_LISTING, mCommentsListingResponse);
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			return resultMap;
		
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

}
