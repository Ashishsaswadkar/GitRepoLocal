/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;

public class UserFavoriteAlbums {
	
	public static final String KEY_ALBUM_COUNT = "album_count";
	public static final String KEY_ALBUMS = "data";
	
	@SerializedName(KEY_ALBUM_COUNT)
	public final int albumCount;
	@SerializedName(KEY_ALBUMS)
	public final List<MediaItem> albums;
	
	public UserFavoriteAlbums(int albumCount, List<MediaItem> albums) {
		this.albumCount = albumCount;
		this.albums = albums;
	}

}
