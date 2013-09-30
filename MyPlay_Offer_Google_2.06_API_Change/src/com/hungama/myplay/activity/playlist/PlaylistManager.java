package com.hungama.myplay.activity.playlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.persistance.InventoryContract;
import com.hungama.myplay.activity.operations.catchmedia.JsonRPC2Methods;
import com.hungama.myplay.activity.services.InventoryLightService;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

public class PlaylistManager {

	private static final String TAG = "PlaylistManager";
	
	private static PlaylistManager sIntance;
	
	private Context mContext;
	
	private static SharedPreferences sharedPreferences;
	private static Editor editor;
	
	private DataManager mDataManager;
	
	private PlaylistManager (Context context) {
		mContext = context;
		
		// SharedPrefrences
		sharedPreferences = context.getSharedPreferences("PLAYLIST_OFFLINE_REQUESTS",0);
		editor = sharedPreferences.edit();
		
		mDataManager = DataManager.getInstance(context.getApplicationContext());
	}
	
	public static final synchronized  PlaylistManager getInstance(Context applicationContext) {
		if (sIntance == null) {
			sIntance = new PlaylistManager(applicationContext);
		}
		return sIntance;
	}
	
	private long generatePlaylistId(){
		return getPlaylistOfflineId();
	}
	
	public void handleOfflinePlaylistRequest(long playlistID, String playlistName, String trackList, JsonRPC2Methods method){
		
		// Generate  a temporary id for the new playlist
		if(method == JsonRPC2Methods.CREATE){
			playlistID = generatePlaylistId();
		}
		
		// Create request 
		PlaylistRequest request = new PlaylistRequest(playlistID, playlistName, trackList, method);
		
		// Store request:
		List<PlaylistRequest> requestList;
		
		// Get all playlist requests
		requestList = mDataManager.getPlaylistRequest();
		
		if(requestList == null){
			requestList = new ArrayList<PlaylistRequest>();
		}
		
		// Iterate the saved play-list requests list and check for existence of the new request
		// Update the request if exist
		PlaylistRequest currentRequest = null;
		Boolean exist = false;
		int index = 0;
		
		for(PlaylistRequest pr : requestList){
			if(pr.getPlaylistID() == request.getPlaylistID()){
				currentRequest = pr;
				exist = true;
				break;
			}
			index++;
		}
		
		if(exist){
			PlaylistRequest updatedRequest = updateRequest(currentRequest, request);
			requestList.remove(index);
			if(updatedRequest != null){
				requestList.add(updatedRequest);
			}
		}else{
			requestList.add(request);
		}
		
		// Store the playlist requests
		mDataManager.storePlaylistRequest(requestList);
		
		// Create Playlist object
		Playlist playlist = new Playlist(request.getPlaylistID(), playlistName, trackList);		
		
		// Store the playlist
		String action = null;
		switch (request.getType()) {
		case  CREATE:
			action = InventoryLightService.ADD;
			break;

		case UPDATE: 
			action = InventoryLightService.MOD;
			break;
			
		case DELETE:
			action = InventoryLightService.DEL;
			break;
			
		default:
			action = InventoryLightService.ADD;
			break;
		}
		
		mDataManager.updateItemable(playlist, action);
	}
	
	// This value represent the id of new play-lists created in off-line mode
	private void setPlaylistOfflineId(long id){
		editor.putLong("Playlist_Offline_Id", id);
		editor.commit();
	}
	
	public long getPlaylistOfflineId(){
		
		long id = sharedPreferences.getLong("Playlist_Offline_Id", 0);
		id--;
		setPlaylistOfflineId(id);
		return id; 
	}
	
	public enum RequestType{
		CREATE, UPDATE,DELETE
	}
	
	public void updatePlaylist(long playlistId, String playlistName, String trackList, JsonRPC2Methods method){
		
		// Create Playlist object
		Playlist playlist = new Playlist(playlistId, playlistName, trackList);		
		
		// Store the playlist
		String action = null;
		
		if(method == JsonRPC2Methods.CREATE){
			action = InventoryLightService.ADD;
		}else if(method == JsonRPC2Methods.DELETE){
			action = InventoryLightService.DEL;
		}else if(method == JsonRPC2Methods.UPDATE){
			action = InventoryLightService.MOD;
		}else{
			// Should not get here
		}
		
		mDataManager.updateItemable(playlist, action);
	}
	
	public void updatePlaylist(Map<String, Object> responseObjects){
		
		String tempId = (String) responseObjects.get("playlist_id");
		String tempName = (String) responseObjects.get(InventoryContract.Playlists.NAME);
		String tempTrackList = (String) responseObjects.get(InventoryContract.Playlists.TRACK_LIST);
		
		long id = Long.valueOf(tempId);
		Playlist playlist = new Playlist(id, tempName, tempTrackList);
		
		JsonRPC2Methods method = (JsonRPC2Methods) responseObjects.get(PlaylistOperation.RESPONSE_KEY_METHOD_TYPE);
		
		// Store the Playlist
		String action = null;
		
		if(method == JsonRPC2Methods.CREATE){
			action = InventoryLightService.ADD;
		}else if(method == JsonRPC2Methods.DELETE){
			action = InventoryLightService.DEL;
		}else if(method == JsonRPC2Methods.UPDATE){
			action = InventoryLightService.MOD;
		}else{
			// Should not get here
		}
		
		mDataManager.updateItemable(playlist, action);
		
	}
	
	public PlaylistRequest updateRequest(PlaylistRequest currentRequest, PlaylistRequest newRequest){
		
		switch (currentRequest.getType()) {
		
		case CREATE:
			switch (newRequest.getType()) {
			case UPDATE:
				// Change the newRequest type to CREATE
				newRequest.setMethod(JsonRPC2Methods.CREATE);
				return newRequest;
			case DELETE:
				// Remove the CREATE request it is useless
				return null;
			default:
				return null;
			}
			
		case UPDATE:
			switch (newRequest.getType()) {
			case UPDATE:
				// Change to the new UPDATE request (newRequest)
				return newRequest;
			case DELETE:
				// Remove the UPDATE (it is useless) and change it to DELETE
				return newRequest;
			default:
				return null;
			}
		default:
			return null;
		}
	}
	
	public List<Track> getTracksListByPlaylist(Playlist playlist){
		
		List<Track> playlistTracks = new ArrayList<Track>();
		
		// Get all Tracks
		Map<Long,Track> allTracks = mDataManager.getStoredTracks();
		
		// Get the selected playlist's tracks
		String tracks = playlist.getTrackList();
		String tracksArr[] = null;
		if(!TextUtils.isEmpty(tracks)){
			tracksArr = tracks.split(" ");
		}
		
		// Loop all tracks and add to itemables the tracks that belong to the selected playlist
		if(allTracks != null){
			if(tracksArr != null && tracksArr.length > 0){
				
				for(int index=0; tracksArr.length > index; index++){
					
					long id =  Long.parseLong(tracksArr[index]);
					Track t = allTracks.get(id);
					if(t != null){
						playlistTracks.add(t);						
					}
				}
			}
		}
		
		return playlistTracks;
	}
}
