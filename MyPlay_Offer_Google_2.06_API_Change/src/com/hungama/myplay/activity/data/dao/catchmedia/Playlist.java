package com.hungama.myplay.activity.data.dao.catchmedia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.persistance.InventoryContract;
import com.hungama.myplay.activity.data.persistance.Itemable;

import android.content.ContentValues;
import android.database.Cursor;

public class Playlist implements Serializable {
	
	public static final String FAVORITE = "Favorite";
	
	public static final String[] COLUMNS = new String[] {
		InventoryContract.Playlists.ID,
		InventoryContract.Playlists.NAME,
		InventoryContract.Playlists.TRACK_LIST,
	};
	
	private long id = 0;
	private String name = null;
	private String trackList = null;
	private List<String> listOfTracksIDs = null;
	
	/**
	 * Contractor for factoring new instances,
	 * also it might be for junk creation. 
	 */
	public Playlist() {}
	
	public Playlist(long id, String name, String trackList) {
		this.id = id;
		this.name = name;
		this.trackList = trackList;
		
		// Convert: String -> String[] -> List<String>
		listOfTracksIDs = new ArrayList<String>();
		
		if(this.trackList != null && !this.trackList.equalsIgnoreCase("")){
			String[] tracksArr = trackList.split(" ");
			if(tracksArr != null && tracksArr.length>0){
				for(String str : tracksArr){
					listOfTracksIDs.add(str);
				}
			}
		}
	}
	

	// getters
	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public String getTrackList() {
		return trackList;
	}
		
	// setters
	public void setName(String name) {
		this.name = name;
	}

	public void setTrackList(String trackList) {
		this.trackList = trackList;
		
		setListOfTracksIDs(trackList);
	}

	// David 18/1/2012
	private void setListOfTracksIDs(String trackList){
		
		String[] trackListArr = trackList.split(" ");
		
		if(trackListArr != null && trackListArr.length>0){
			this.listOfTracksIDs.clear();
			for(String str : trackListArr){
				this.listOfTracksIDs.add(str);
			}
		}
	}
	
	// mapping methods
	public Playlist getInitializedObject(Map map) {
		Long tempId = (Long) map.get(InventoryContract.Playlists.ID);
		String tempName = (String) map.get(InventoryContract.Playlists.NAME);
		String tempTrackList = (String) map.get(InventoryContract.Playlists.TRACK_LIST);
		
		return new Playlist(tempId, tempName, tempTrackList);
	}

//	@Override
//	public ContentValues getObjectFieldValues() {
//		// Playlist is a mutable object, gets updated values every call.
//		ContentValues values = new ContentValues();
//		values.put(InventoryContract.Playlists.ID, id);
//		values.put(InventoryContract.Playlists.NAME, name);
//		values.put(InventoryContract.Playlists.TRACK_LIST, trackList);
//		
//		return values;
//	}
	
	// David 18/1/2012
	public Boolean addTrack(long trackID){
		// 1. get all playlists
		// 2. get the selected playlist by id
		// 3. add the track to tracklist string
		// 4. database.update(PlayList);
		// 5. add a little toast
		
		// If Track exist in Playlist -> Do Not Add it to Playlist
		// If Not -> Add it to Playlist
		
		Boolean exist = isTrackExistInPlaylist(trackID);
		
		if(exist){
			// Do nothing
			return false;
		}else{
			// Add the trackID to trackList data member
			String addTrackID = String.valueOf(trackID);
			listOfTracksIDs.add(addTrackID);
			setTrackList(Join((ArrayList<String>)listOfTracksIDs, " "));
			return true;
		}
		
//		// David 22/7/2012 for now there is no need for checking existens 
//		// Add the trackID to trackList data member
//		String addTrackID = String.valueOf(trackID);
//		listOfTracksIDs.add(addTrackID);
//		setTrackList(Join((ArrayList<String>)listOfTracksIDs, " "));
//		return true;
	}
	
	// David 18/1/2012
	public Boolean removeTrack(long trackID){
		// 1. Delete the id "string" from trackList
		// 2. database.update(PlayList);
		// 3. remove track from the array adapter
		// 4. notifySetDataChange
		
		// If Track exist in Playlist -> Remove it from Playlist
		// If Not -> Do Not Remove it from Playlist
		
		Boolean exist = isTrackExistInPlaylist(trackID);
		
		if(exist){
			// Remove the trackID from trackList data member
			String removeTrackID = String.valueOf(trackID);
			listOfTracksIDs.remove(removeTrackID);
			setTrackList(Join((ArrayList<String>)listOfTracksIDs, " "));
			return true;
		}else{
			// Do nothing
			return false;
		}
	}
	
	// David 18/1/2012
	public Boolean isTrackExistInPlaylist(long id){
		
		String trackId = String.valueOf(id);
		
		if(listOfTracksIDs.contains(trackId)){
			return true;
		}
		
		return false;
	}
	
//	// David 22/7/2012
//	public Boolean addTracksList(List<Itemable> tracks){
//		
//		if(tracks != null && !tracks.isEmpty()){
//			
//			Track track;
//			
//			for(Itemable item : tracks){
//				track = (Track) item;
//				addTrack(track.getId());
//			}
//			
//			return true;
//			
//		}else{
//			return false;
//		}
//	}
	
	public Boolean addTracksList(List<Track> tracks){
		
		boolean trackAdded = false;
		
		if(tracks != null && !tracks.isEmpty()){
			for(Track t : tracks){
				trackAdded = addTrack(t.getId());
			}
			return trackAdded;
		}else{
			return false;
		}
	}
	
	public Boolean removeTracksList(List<Itemable> tracks){
		
		if(tracks != null && !tracks.isEmpty()){
			
			Track track;
			
			for(Itemable item : tracks){
				track = (Track) item;
				removeTrack(track.getId());
			}
			
			return true;
			
		}else{
			return false;
		}
	}
	
	// Convert List to Delimited String
	public static String Join(ArrayList<String> coll, String delimiter)
	{
	    if (coll.isEmpty())
		return "";
	 
	    StringBuilder sb = new StringBuilder();
	 
	    for (String x : coll)
		sb.append(x + delimiter);
	 
	    sb.delete(sb.length()-delimiter.length(), sb.length());
	 
	    return sb.toString();
	}
		
	public int getNumberOfTracks(){
		
		String tracklist = getTrackList();
		
		String arr[] = tracklist.trim().split(" ");
		
		if(arr != null){
			
			if(arr.length == 1){
				if(arr[0].equalsIgnoreCase("")){
					return 0;
				}else{
					return 1;
				}
			}else{
				return arr.length;
			}
			
		}else{
			return 0;
		}
	}

}
