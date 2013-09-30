package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

/**
 * Enumeration definition types of {@link MediaItem} types.
 */
public enum MediaType implements Serializable  {
	ALBUM,
	TRACK,
	PLAYLIST,
	ARTIST,
	VIDEO,
	BADGE;
	
	
	public static final MediaType getMediaItemByName(String name) {
		if (name.equalsIgnoreCase(ALBUM.toString())) {
			return ALBUM;
			
		} else if (name.equalsIgnoreCase(TRACK.toString())) {
			return TRACK;
			
		} else if (name.equalsIgnoreCase(PLAYLIST.toString())) {
			return PLAYLIST;
			
		} else if (name.equalsIgnoreCase(ARTIST.toString())) {
			return ARTIST;
			
		} else if (name.equalsIgnoreCase(VIDEO.toString())) {
			return VIDEO;
			
		} else if (name.equalsIgnoreCase(BADGE.toString())) {
			return BADGE;
		}
		
		return TRACK;
	}
}
