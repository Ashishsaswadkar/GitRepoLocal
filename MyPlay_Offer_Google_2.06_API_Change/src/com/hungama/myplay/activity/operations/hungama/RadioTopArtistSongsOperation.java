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
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Utils;

public class RadioTopArtistSongsOperation extends WebRadioOperation {
	
	private static final String TAG = "RadioTopArtistSongsOperation";
	
	private static final String PARAMS_ARTIST_ID = "artist_id";
	
	public static final String RESULT_KEY_OBJECT_TRACKS = "result_key_object_tracks";
	public static final String RESULT_KEY_OBJECT_MEDIA_ITEM = "result_key_object_media_item";
	
	private final String mServerUrl;
	private final String mAuthKey;
	private final MediaItem mArtistItem;
	
	public RadioTopArtistSongsOperation (String serverUrl, String authKey, MediaItem artistItem) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mArtistItem = artistItem;
	}
	

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serverUrl = mServerUrl + URL_SEGMENT_RADIO_TOP_ARTIST_SONGS + 
				PARAMS_ARTIST_ID + EQUALS + Long.toString(mArtistItem.getId()) + AMPERSAND + 
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
			//response = "{\"response\":{\"code\":3,\"message\":\"There are no songs for this artist\",\"display\":0}}"; // TESTING
			//response = "{\"catalog\":{\"results\":48,\"content\":[]}}"; // TESTING
			Map<String, Object> catalogMap = (Map<String, Object>) parser.parse(response);
			
			if (catalogMap.containsKey(KEY_CATALOG)) {
				catalogMap = (Map<String, Object>) catalogMap.get(KEY_CATALOG);
			} else {
				if (catalogMap.containsKey(KEY_RESPONSE)) {
					catalogMap = (Map<String, Object>) catalogMap.get(KEY_RESPONSE);
					if (catalogMap.containsKey(KEY_MESSAGE)) {						
						throw new InvalidResponseDataException((String) catalogMap.get(KEY_MESSAGE));
					}
				}
			}
			
			if (catalogMap.containsKey(KEY_CONTENT)) {
				
				List<Map<String, Object>> contentMap = (List<Map<String, Object>>) catalogMap.get(KEY_CONTENT);
				
				if (Utils.isListEmpty(contentMap)) {
					throw new InvalidResponseDataException("There are no songs for this artist");
				}
				
				List<Track> radioTracks = new ArrayList<Track>();
				
				long id;
				String title;
				String albumName;
				String artistName;
				String imageUrl;
				
				Track track;
				
				for (Map<String, Object> stationMap : contentMap) {
					
					id = ((Long) stationMap.get(MediaItem.KEY_CONTENT_ID)).longValue();
					title = (String) stationMap.get(MediaItem.KEY_TITLE);
					albumName = (String) stationMap.get(MediaItem.KEY_ALBUM_NAME);
					artistName = (String) stationMap.get(MediaItem.KEY_ARTIST_NAME);
					imageUrl = (String) stationMap.get(MediaItem.KEY_IMAGE);
					
					track = new Track(id, title, albumName, artistName, imageUrl, imageUrl);
					radioTracks.add(track);
				}
				
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(RESULT_KEY_OBJECT_TRACKS, radioTracks);
				resultMap.put(RESULT_KEY_OBJECT_MEDIA_ITEM, mArtistItem);
				
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
