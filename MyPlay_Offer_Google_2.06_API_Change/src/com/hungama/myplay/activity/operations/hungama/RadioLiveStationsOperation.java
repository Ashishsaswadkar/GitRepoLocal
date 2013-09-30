package com.hungama.myplay.activity.operations.hungama;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;

import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.LiveStation;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Retrieves the available live stations from Hungama as list of {@link MediaItem} implementation.
 * The driven retrieved {@link MediaItem} is of the type {@link LiveStation}.
 */
public class RadioLiveStationsOperation extends WebRadioOperation {
	
	private static final String TAG = "RadioLiveStationsOperation";

	private static final String KEY_TITLE = "title";
	private static final String KEY_DESCRIPTION = "description";
	private static final String KEY_STREAMING_URL = "streaming_url";
	
	private final String mServerUrl;
	private final String mAuthKey;
	
	public RadioLiveStationsOperation (String serverUrl, String authKey) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serverUrl = mServerUrl + URL_SEGMENT_RADIO_LIVE_STATIONS + 
						   				PARAMS_AUTH_KEY + EQUALS + mAuthKey; 
		return serverUrl;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		JSONParser parser = new JSONParser();
		
		try {
			Map<String, Object> catalogMap = (Map<String, Object>) parser.parse(response);
			
			if (catalogMap.containsKey(KEY_CATALOG)) {
				catalogMap = (Map<String, Object>) catalogMap.get(KEY_CATALOG);
			} else {
				throw new InvalidResponseDataException("Parsing error - no catalog available");
			}
			
			if (catalogMap.containsKey(KEY_CONTENT)) {
				
				List<Map<String, Object>> contentMap = (List<Map<String, Object>>) catalogMap.get(KEY_CONTENT);
				
				List<MediaItem> mediaItems = new ArrayList<MediaItem>();
				
				/*
				 * sets fake ids to make the adapters distinguish
				 * between different media items (web radios). 
				 */
				long id = 0;
				
				String title;
				String description;
				String streamingUrl;
				MediaItem mediaItem;
				
				for (Map<String, Object> stationMap : contentMap) {
					
					title = (String) stationMap.get(KEY_TITLE);
					description = (String) stationMap.get(KEY_DESCRIPTION);
					streamingUrl = (String) stationMap.get(KEY_STREAMING_URL);
					
					mediaItem = new LiveStation(id, title, description, streamingUrl);
					mediaItem.setMediaContentType(MediaContentType.RADIO);
					
					mediaItems.add(mediaItem);
					
					id++;
				}
				
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(RESULT_KEY_OBJECT_MEDIA_ITEMS, mediaItems);
				
				return resultMap;
				
			} else {
				throw new InvalidResponseDataException("Parsing error - no content available");
			}
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("Parsing error.");
		}
	}

}
