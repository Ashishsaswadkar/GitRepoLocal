package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;

public class UserFavoriteSongs {
	
	public static final String KEY_SONGS_COUNT = "songs_count";
	public static final String KEY_SONGS = "data";
	
	@SerializedName(KEY_SONGS_COUNT)
	public final int songsCount;
	@SerializedName(KEY_SONGS)
	public final List<MediaItem> songs;
	
	public UserFavoriteSongs(int songsCount, List<MediaItem> songs) {
		this.songsCount = songsCount;
		this.songs = songs;
	}
}
