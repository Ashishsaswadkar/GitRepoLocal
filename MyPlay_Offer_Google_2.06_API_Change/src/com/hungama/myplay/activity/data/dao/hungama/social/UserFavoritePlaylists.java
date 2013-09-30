package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;

public class UserFavoritePlaylists {

	public static final String KEY_PLAYLIST_COUNT = "playlist_count";
	public static final String KEY_PLAYLISTS = "data";
	
	@SerializedName(KEY_PLAYLIST_COUNT)
	public final int playlistCount;
	@SerializedName(KEY_PLAYLISTS)
	public final List<MediaItem> playlists;
	
	public UserFavoritePlaylists(int playlistCount, List<MediaItem> playlists) {
		this.playlistCount = playlistCount;
		this.playlists = playlists;
	}
}
