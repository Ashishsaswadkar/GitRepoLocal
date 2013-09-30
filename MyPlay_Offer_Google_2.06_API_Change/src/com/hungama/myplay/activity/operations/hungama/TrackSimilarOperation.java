package com.hungama.myplay.activity.operations.hungama;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

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
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

/**
 * Retrieves {@link MediaItem}s similar to the given {@link Track}
 */
public class TrackSimilarOperation extends HungamaOperation {
	
	private static final String TAG = "TrackSimilarOperation";
	
	public static final String RESULT_KEY_OBJECT_MEDIA_ITEMS = "result_key_object_media_items";
	
	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final Track mTrack;

	public TrackSimilarOperation(String serverUrl, String authKey, String userId, Track track) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mUserId = userId;
		mTrack = track;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.TRACK_SIMILAR;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serviceUrl = mServerUrl + URL_SEGMENT_CONTENT + URL_SEGMENT_MUSIC + URL_SEGMENT_SIMILAR + 
							Long.toString(mTrack.getId()) + "?" + 
							PARAMS_AUTH_KEY + EQUALS + mAuthKey + AMPERSAND +
							PARAMS_USER_ID + EQUALS + mUserId;
		return serviceUrl;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
								  InvalidRequestParametersException, InvalidRequestTokenException,
								  OperationCancelledException {
		
		if (TextUtils.isEmpty(response)) {
			throw new InvalidResponseDataException("Response is Empty!");
		}
		
		// removes the: {"catalog":{"content": string and the last }} .
		String itemsString = response.substring(22, response.length() - 2);
		
		Type listType = new TypeToken<ArrayList<MediaItem>>() {}.getType();
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		List<MediaItem> items = null;
		
		try {
			items = gson.fromJson(itemsString, listType);
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();
			
		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		}
		
		// TODO: temporally solving the differentiating issue between Music and Videos, solve this when inserting also campaigns.
		for (MediaItem mediaItem : items) {
			mediaItem.setMediaContentType(MediaContentType.MUSIC);
		}
		
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(RESULT_KEY_OBJECT_MEDIA_ITEMS, items);
		
		return resultMap;
	}

}
