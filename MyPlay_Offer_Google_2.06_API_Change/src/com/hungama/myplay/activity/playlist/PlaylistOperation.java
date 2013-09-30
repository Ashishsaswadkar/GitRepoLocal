package com.hungama.myplay.activity.playlist;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.util.Log;

import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.persistance.InventoryContract;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.JsonRPC2Methods;
import com.hungama.myplay.activity.services.InventoryLightService;

/**
 * Retrieves device ID from CM servers, based on the device's properties.
 */
public class PlaylistOperation extends CMOperation {
	
	private static final String TAG = "PlaylistOperation";
	
	public static final String RESPONSE_KEY_OBJECT_PLAYLIST = "response_key_playlist";
	public static final String RESPONSE_KEY_OBJECT_TRACK_LIST = InventoryContract.Playlists.TRACK_LIST;
	public static final String RESPONSE_KEY_METHOD_TYPE = "method";
	public static final String RESPONSE_KEY_PLAYLIST_NAME = "name";
	
	public static final String RESPONSE_KEY_PLAYLIST = "response_key_playist";
	
	public JsonRPC2Methods method;
	public long playlistId;
	public String playlistName;
	public String trackList;
	
	public PlaylistManager mPlaylistManager;
	
	public PlaylistOperation(Context context, long playlistId, String playlistName, String trackList, JsonRPC2Methods method) {
		super(context);
		
		mPlaylistManager = PlaylistManager.getInstance(context.getApplicationContext());
		
		this.method = method;
		this.playlistId = playlistId;
		this.playlistName = playlistName;
		this.trackList = trackList;
		
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return method;
	}
	
	@Override
	public Map<String, Object> getDescriptor() {
		
		Map<String, Object> descriptor = new HashMap<String, Object>();
		
		descriptor.put(ApplicationConfigurations.MEDIA_ID_NS, pApplicationConfigurations.getMediaIdNs());
		descriptor.put(ApplicationConfigurations.CONSUMER_ID, pApplicationConfigurations.getConsumerID());
		descriptor.put(ApplicationConfigurations.CONSUMER_REVISION, pApplicationConfigurations.getConsumerRevision());
		
		if(method == JsonRPC2Methods.UPDATE){
			
			if(trackList != null){
				descriptor.put("tracklist", trackList);
			}

			// If the update is only rename so no need to send trackList
			descriptor.put("playlist_id", playlistId);	
			descriptor.put("playlist_name", playlistName);
			
		}else if(method == JsonRPC2Methods.CREATE){
			
			descriptor.put("playlist_name", playlistName);
			descriptor.put("tracklist", trackList);			
			
		}else if(method == JsonRPC2Methods.DELETE){
			
			descriptor.put("playlist_id", playlistId);
		}
		
		return descriptor;
	}
	
	@Override
	protected Map<String, Object> getCredentials() {
		
		Map<String, Object> credentials = new HashMap<String, Object>();
		
		credentials.put(ServerConfigurations.APPLICATION_CODE, pServerConfigurations.getAppCode());
		credentials.put(ServerConfigurations.APPLICATION_VERSION, pServerConfigurations.getAppVersion());
		credentials.put(DeviceConfigurations.HARDWARE_ID, pDeviceConfigurations.getHardwareId());
		credentials.put(ApplicationConfigurations.SESSION_ID, pApplicationConfigurations.getSessionID());
		credentials.put(DeviceConfigurations.TIMESTAMP, pDeviceConfigurations.getTimeStamp());
		credentials.put(ServerConfigurations.LC_ID, pServerConfigurations.getLcId());
		credentials.put(ApplicationConfigurations.PARTNER_USER_ID, pApplicationConfigurations.getPartnerUserId());
		credentials.put(ServerConfigurations.WEB_SERVER_VERSION, pServerConfigurations.getWebServiceVersion());
		
		return credentials;
	}
	
	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.PLAYLIST;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.PLAYLIST;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException, 
				InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		Log.i(TAG + OperationDefinition.CatchMedia.ServiceName.PLAYLIST, response);
		
		JSONParser jsonParser = new JSONParser();
		
		try {
			
			Map<String, Object> resultMap = (Map<String, Object>) jsonParser.parse(response);

			// Update the Playlist locally
			
			Playlist playlist = null;
			String action = null;
			
			if(method == JsonRPC2Methods.CREATE){
				action = InventoryLightService.ADD;
				
				String tempId = (String) resultMap.get("playlist_id");
				long id = Long.valueOf(tempId);
				
				playlist = new Playlist(id, playlistName, trackList);	
				
			}else if(method == JsonRPC2Methods.DELETE){
				
				action = InventoryLightService.DEL;
				
				playlist = new Playlist(playlistId, playlistName, trackList);
				
			}else if(method == JsonRPC2Methods.UPDATE){
				action = InventoryLightService.MOD;
				
				playlist = new Playlist(playlistId, playlistName, trackList);
				
			}else{
				// Should not get here
			}
			
			DataManager mDataManager = DataManager.getInstance(getContext().getApplicationContext());
			mDataManager.updateItemable(playlist, action);
			
			Map<String, Object> responseMap = new HashMap<String, Object>();
			responseMap.put(RESPONSE_KEY_PLAYLIST, resultMap);
			responseMap.put(RESPONSE_KEY_METHOD_TYPE, method);
			
			return responseMap;
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("Device map parsing error.");
		}
		
	}

}
