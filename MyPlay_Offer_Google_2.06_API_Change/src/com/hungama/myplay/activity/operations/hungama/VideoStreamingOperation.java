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

public class VideoStreamingOperation extends HungamaOperation {
	
	private static final String TAG = "VideoStreamingOperation";
	
	public static final String RESPONSE_KEY_VIDEO_STREAMING = "response_key_video_streaming";
	
	private final String mServerUrl;	
	private final String mUserId;
	private final String mContentId;
	private final String mSize;
	private final String mAuthKey;
	
	public VideoStreamingOperation(String serverUrl, String userId, String contentId, String size, String authKey) {
		mServerUrl = serverUrl;
		mUserId = userId;
		mContentId = contentId;
		mSize = size;
		mAuthKey = authKey;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.VIDEO_STREAMING;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return mServerUrl + URL_SEGMENT_VIDEO_STREAMING + 
				PARAMS_USER_ID + "=" + mUserId + "&" +
				PARAMS_CONTENT_ID + "=" + mContentId + "&" +
				PARAMS_DEVICE + "=" + VALUE_DEVICE + "&" +
				PARAMS_SIZE + "=" + mSize + "&" +
				PARAMS_AUTH_KEY + "=" + mAuthKey ;
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
			resultMap.put(RESPONSE_KEY_VIDEO_STREAMING, video);
	
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
