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
import com.hungama.myplay.activity.data.dao.hungama.Video;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class VideoStreamingOperationAdp extends HungamaOperation {
	
	private static final String TAG = "VideoStreamingOperationAdp";
	
	public static final String RESPONSE_KEY_VIDEO_STREAMING_ADP = "response_key_video_streaming_adp";
	
	private final String mServerUrl;	
	private final String mUserId;
	private final String mContentId;
	private final String mSize;
	private final String mAuthKey;
	private final int mNetworkSpeed;
	private final String mNetworkType;
	private final String mContentFormat;
	private final String mGoogleEmailId;
	
	public VideoStreamingOperationAdp(String serverUrl, String userId, String contentId, String size, 
			String authKey, int networkSpeed, String networkType, String contentFormat, String googleEmailId) {
		mServerUrl = serverUrl;
		mUserId = userId;
		mContentId = contentId;
		mSize = size;
		mAuthKey = authKey;
		mNetworkSpeed = networkSpeed;
		mNetworkType = networkType;
		mContentFormat = contentFormat;
		mGoogleEmailId = googleEmailId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.VIDEO_STREAMING_ADP;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		StringBuilder url = new StringBuilder();
		
		url.append(mServerUrl).append(URL_SEGMENT_VIDEO_STREAMING_ADP).
		append(PARAMS_USER_ID).append("=").append(mUserId).append("&").
		append(PARAMS_CONTENT_ID).append("=").append(mContentId).append("&").
		append(PARAMS_DEVICE).append("=").append(VALUE_DEVICE).append("&").
		append(PARAMS_SIZE).append("=").append(mSize).append("&").
		append(PARAMS_AUTH_KEY).append("=").append(mAuthKey).append("&").
		append(PARAMS_NETWORK_SPEED).append("=").append(mNetworkSpeed).append("&").
		append(PARAMS_NETWORK_TYPE).append("=").append(mAuthKey).append("&").
		append(PARAMS_CONTENT_FORMAT).append("=").append(mContentFormat);
		
		if(mGoogleEmailId != null){
			url.append("&").append(PARAMS_GOOGLE_EMAIL_ID).append("=").append(mGoogleEmailId);
		}
		
		return url.toString();
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
			response = response.replace("{\"catalog\":", "");
			response = response.substring(0, response.length() - 1);
			
			Video video = (Video) gson.fromJson(response, Video.class);
			resultMap.put(RESPONSE_KEY_VIDEO_STREAMING_ADP, video);
	
			return resultMap;
		
		} catch (JsonSyntaxException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (JsonParseException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		}
	}

}
