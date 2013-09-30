package com.hungama.myplay.activity.playlist;

import java.io.Serializable;

import com.hungama.myplay.activity.operations.catchmedia.JsonRPC2Methods;
import com.hungama.myplay.activity.playlist.PlaylistManager.RequestType;

public class PlaylistRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long playlistID;
	private String playlistName;
	private String trackList;
	private JsonRPC2Methods method;
	private RequestType type;
	
	public PlaylistRequest(){
		
	}
	
	public PlaylistRequest(long playlistID, String playlistName, String trackList, JsonRPC2Methods method){
		
		this.setMethod(method);
		this.setPlaylistID(playlistID);
		this.setPlaylistName(playlistName);
		this.setTrackList(trackList);
	}
	
	public void setPlaylistID(long playlistID){
		this.playlistID = playlistID;
	}
	
	public long getPlaylistID(){
		return this.playlistID;
	}
	
	public void setPlaylistName(String playlistName){
		this.playlistName = playlistName;
	}
	
	public String getPlaylistName(){
		return this.playlistName;
	}
	
	public void setTrackList(String trackList){
		this.trackList = trackList;
	}
	
	public String getTrackList(){
		return this.trackList;
	}	
	
	public void setMethod(JsonRPC2Methods method){
		this.method = method;
		
		// Set also the type
		if(method ==  JsonRPC2Methods.CREATE){
			type = RequestType.CREATE;
		}else if(method ==  JsonRPC2Methods.UPDATE){
			type = RequestType.UPDATE;
		}else if(method ==  JsonRPC2Methods.DELETE){
			type = RequestType.DELETE;
		}
	}
	
	public JsonRPC2Methods getMethod(){
		return this.method;
	}
	
	public RequestType getType(){
		return this.type;
	}
}
