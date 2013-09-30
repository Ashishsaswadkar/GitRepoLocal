package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.SerializedName;

/**
 * Lyrics of a given {@link Track} as a response of {@link TrackLyricsOperation}. 
 */
public class TrackLyrics {

	public static final String KEY_CONTENT_ID = "content_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_LYRICS = "lyrics";
	
	@SerializedName(KEY_CONTENT_ID)
	private final long id;
	@SerializedName(KEY_TITLE)
	private final String title;
	@SerializedName(KEY_LYRICS)
	private final String lyrics;
	
	public TrackLyrics(long id, String title, String lyrics) {
		this.id = id;
		this.title = title;
		this.lyrics = lyrics;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getLyrics() {
		return lyrics;
	}
}
