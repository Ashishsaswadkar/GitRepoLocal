package com.hungama.myplay.activity.operations.hungama;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

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
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class RelatedVideoOperation extends HungamaOperation {
	
	private static final String TAG = "RelatedVideoOperation";
	
	public static final String RESPONSE_KEY_RELATED_VIDEO = "response_key_related_video";
	public static final String RESPONSE_KEY_RELATED_VIDEO_MEDIA_CONTENT_TYPE = "response_key_related_video_media_content_type";
	
	private final String mServerUrl;
	private final String mAlbumId;
	private final String mAuthKey;
	private final MediaContentType mMediaContentType;
	private final MediaType mMediaType;
	
	public RelatedVideoOperation(String serverUrl, String albumId,  MediaContentType mediaContentType, MediaType mediaType, String authKey) {
		mServerUrl = serverUrl;
		mAlbumId = albumId;
		mMediaContentType = mediaContentType;
		mMediaType = mediaType;
		mAuthKey = authKey;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.VIDEO_RELATED;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return mServerUrl + 
				URL_SEGMENT_CONTENT + 
				URL_SEGMENT_VIDEO + 
				URL_SEGMENT_RELATED_VIDEO + 
				mAlbumId + "?" + 
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
		List<MediaItem> items = null;
		
		if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
		
		try {
			response = response.replace("{\"catalog\":{\"content\":", "");
			response = response.substring(0, response.length() - 2);
			
			Type listType = new TypeToken<ArrayList<MediaItem>>() {}.getType();
			items = gson.fromJson(response, listType);
			
			} catch (JsonSyntaxException exception) {
				throw new InvalidResponseDataException();
				
			} catch (JsonParseException exception) {
				throw new InvalidResponseDataException();
			}
			
			// TODO: temporally solving the differentiating issue between Music and Videos, solve this when inserting also campaigns.
			for (MediaItem mediaItem : items) {
				mediaItem.setMediaContentType(mMediaContentType);
				mediaItem.setMediaType(MediaType.TRACK);
			}			
			
			resultMap.put(RESPONSE_KEY_RELATED_VIDEO_MEDIA_CONTENT_TYPE, mMediaContentType);
//			resultMap.put(RESULT_KEY_OBJECT_MEDIA_CATEGORY_TYPE, mItemCategoryType);
			resultMap.put(RESPONSE_KEY_RELATED_VIDEO, items);
			
			return resultMap;
	}

}
