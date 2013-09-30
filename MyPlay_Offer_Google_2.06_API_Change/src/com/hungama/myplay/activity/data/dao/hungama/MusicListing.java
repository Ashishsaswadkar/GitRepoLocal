/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author offerperetz
 *
 */
public class MusicListing {

	@SerializedName("track")
	private List<TrackNew> tracks;
	
	public MusicListing() { }

	public List<TrackNew> getTracks() {
		return tracks;
	}

	public void setTracks(List<TrackNew> tracks) {
		this.tracks = tracks;
	}
}
