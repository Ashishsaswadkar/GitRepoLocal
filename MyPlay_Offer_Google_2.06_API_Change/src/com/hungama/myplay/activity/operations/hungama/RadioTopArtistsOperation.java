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
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class RadioTopArtistsOperation extends WebRadioOperation {
	
	private static final String TAG = "RadioTopArtistsOperation";
	
	private static final String KEY_ARTIST_ID = "artist_id";
	private static final String KEY_ARTIST_NAME = "artist_name";
	private static final String KEY_IMAGE = "image";
	
	private final String mServerUrl;
	private final String mAuthKey;
	
	public RadioTopArtistsOperation (String serverUrl, String authKey) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serverUrl = mServerUrl + URL_SEGMENT_RADIO_TOP_ARTISTS + 
					PARAMS_AUTH_KEY + EQUALS + mAuthKey; 
		return serverUrl;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
								InvalidRequestParametersException, InvalidRequestTokenException, 
								OperationCancelledException {
		
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
				
				long id;
				String name;
				String imageUrl;
				
				MediaItem mediaItem;
				
				if (contentMap != null) {
					for (Map<String, Object> stationMap : contentMap) {
						
						id = ((Long) stationMap.get(KEY_ARTIST_ID)).longValue();
						name = (String) stationMap.get(KEY_ARTIST_NAME);
						imageUrl = (String) stationMap.get(KEY_IMAGE);
						
						mediaItem = new MediaItem(id, name, null, null, imageUrl, 
									imageUrl, MediaType.ALBUM.toString().toLowerCase(), 0);
						mediaItem.setMediaContentType(MediaContentType.RADIO);
						
						mediaItems.add(mediaItem);
					}
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
