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
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.DiscoverSearchResultIndexer;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Retrieves list of media items from the Discover properties query. 
 */
public class DiscoverSearchResultsOperation extends DiscoverOperation {
	
	private static final String TAG = "DiscoverSearchResultsOperation";
	
	/**
	 * Key for getting the index properties of the response paging.
	 */
	public static final String RESULT_KEY_DISCOVER_SEARCH_RESULT_INDEXER = "result_key_discover_search_result_indexer";
	
	/**
	 * Key for getting list of media items in type of Tracks.
	 */
	public static final String RESULT_KEY_MEDIA_ITEMS = "result_key_media_items";
	
	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;

	private final Discover mDiscover;
	private final DiscoverSearchResultIndexer mDiscoverSearchResultIndexer;

	public DiscoverSearchResultsOperation(String serverUrl, String authKey, String userId, 
									Discover discover, DiscoverSearchResultIndexer discoverSearchResultIndexer) {
		
		this.mServerUrl = serverUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
	
		this.mDiscover = discover;
		this.mDiscoverSearchResultIndexer = discoverSearchResultIndexer;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.DISCOVER_SEARCH_RESULT;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		StringBuilder serverURL = new StringBuilder();
		// adds the base server url.
		serverURL.append(mServerUrl);
		// adds the service path.
		serverURL.append(URL_SEGMENT_DISCOVER_SEARCH);
		// authentication properties.
		serverURL.append(PARAMS_AUTH_KEY).append(EQUALS).append(mAuthKey).append(AMPERSAND);
		serverURL.append(PARAMS_USER_ID).append(EQUALS).append(mUserId).append(AMPERSAND);
		
		// builds the discover params
		serverURL.append(buildURLParametersFromDiscoverObject(mDiscover));
		
		// builds the indexer.
		serverURL.append(buildURLParametersFromDiscoverSearchResultIndexer(mDiscoverSearchResultIndexer));
		
		return serverURL.toString();
	}

	@Override
	public String getRequestBody() {
		// GET request, nothing to do.
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
						InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		JSONParser jsonParser = new JSONParser();
		try {
			Map<String, Object> responseMap = (Map<String, Object>) jsonParser.parse(response) ;
			
			// checks if the given response is not an error.
			if (responseMap.containsKey(KEY_RESPONSE)) {
				// gets the error message.
				Map<String, Object> errorMap = (Map<String, Object>) responseMap.get(KEY_RESPONSE);
				int code = ((Long) errorMap.get(KEY_CODE)).intValue();
				String message = (String) errorMap.get(KEY_MESSAGE);
				throw new InvalidRequestParametersException(code, message);
			}
			
			// gets the "catalog".
			responseMap = (Map<String, Object>) responseMap.get(KEY_CATALOG);
			
			// gets the maps.
			Map<String, Object> indexerMap = (Map<String, Object>) responseMap.get(KEY_ATTRIBUTES);
			List<Map<String, Object>> mediaItemMapList = (List<Map<String, Object>>) responseMap.get(KEY_CONTENT);
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			
			// gets the indexer.
			int startIndex = ((Long) indexerMap.get(KEY_START_INDEX)).intValue();
			int length = ((Long) indexerMap.get(KEY_LENGTH)).intValue();
			int max = ((Long) indexerMap.get(KEY_MAX)).intValue();
			
			resultMap.put(RESULT_KEY_DISCOVER_SEARCH_RESULT_INDEXER, 
						new DiscoverSearchResultIndexer(startIndex, length, max));
			
			// gets the list of media items.
			List<MediaItem> mediaItems = new ArrayList<MediaItem>();
			int contentId;
			String albumName;
			String title;
			String imageUrl;
			String bigImageUrl;
			String type;
			int trackCount;
			
			MediaItem mediaItem;
			
			for (Map<String, Object> mediaItemMap : mediaItemMapList) {
				contentId = ((Long) mediaItemMap.get(MediaItem.KEY_CONTENT_ID)).intValue();
				albumName = (String) mediaItemMap.get(MediaItem.KEY_ALBUM_NAME);
				title = (String) mediaItemMap.get(MediaItem.KEY_TITLE);
				imageUrl = (String) mediaItemMap.get(MediaItem.KEY_IMAGE);
				bigImageUrl = (String) mediaItemMap.get(MediaItem.KEY_BIG_IMAGE);
				type = (String) mediaItemMap.get(MediaItem.KEY_TYPE);
				
				trackCount = 0;
				
				if (type.equalsIgnoreCase(MediaType.PLAYLIST.toString())){
					if (mediaItemMap.containsKey(MediaItem.KEY_MUSIC_TRACKS_COUNT)) {
						trackCount = ((Long) mediaItemMap.get(MediaItem.KEY_MUSIC_TRACKS_COUNT)).intValue(); 
					}
				}
				
				mediaItem = new MediaItem(contentId, title, albumName, null, imageUrl, bigImageUrl, type, trackCount);
				mediaItem.setMediaContentType(MediaContentType.MUSIC);
				
				mediaItems.add(mediaItem);
			}
			
			resultMap.put(RESULT_KEY_MEDIA_ITEMS, mediaItems);
		
			return resultMap;
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

	
}
