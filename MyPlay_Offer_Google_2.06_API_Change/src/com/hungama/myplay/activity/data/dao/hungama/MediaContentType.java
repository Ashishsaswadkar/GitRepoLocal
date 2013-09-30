package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

/**
 * Enumeration definition of different media content types.
 */
public enum MediaContentType implements Serializable  {
	MUSIC,
	VIDEO,
	RADIO,
	BADGE;
	
	public static final MediaContentType getCopy(MediaContentType mediaContentType) {
		
		if (mediaContentType == MUSIC) {
			return MUSIC;
		} else if (mediaContentType == VIDEO) {
			return VIDEO;
		} else if (mediaContentType == RADIO) {
			return RADIO;
		} else if (mediaContentType == BADGE) {
			return BADGE;
		}
		
		return MUSIC;
	}
}
