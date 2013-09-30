/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;

/**
 * An item in My Stream
 */
public class StreamItem {
	
	
	public static final String TYPE_BADGE = "badge";
	
	public static final String KEY_ACTION = "action";
	public static final String KEY_USER_ID = "user_id";
	public static final String KEY_USER_NAME = "user_name";
	public static final String KEY_PHOTO_URL = "photo_url";
	public static final String KEY_CONTENT_ID = "content_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_ALBUM_NAME = "album_name";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_BIG_IMAGE = "big_image";
	public static final String KEY_TYPE = "type";
	public static final String KEY_TIME = "time";
	public static final String KEY_MORE_SONGS_COUNT = "more_songs_count";
	public static final String KEY_MORE_SONGS = "more_songs";
	public static final String KEY_MORE_SONGS_DATA = "more_songs_data";
	
	@SerializedName(KEY_ACTION)
	public final String action;
	@SerializedName(KEY_USER_ID)
	public final long userId;
	@SerializedName(KEY_USER_NAME)
	public final String userName;
	@SerializedName(KEY_PHOTO_URL)
	public final String photoUrl;
	@SerializedName(KEY_CONTENT_ID)
	public final long conentId;
	@SerializedName(KEY_TITLE)
	public final String title;
	@SerializedName(KEY_ALBUM_NAME)
	public final String albumName;
	@SerializedName(KEY_IMAGE)
	public final String imageUrl;
	@SerializedName(KEY_BIG_IMAGE)
	public final String bigImageUrl;
	@SerializedName(KEY_TYPE)
	public final String type;
	@SerializedName(KEY_TIME)
	public final String time;
	@SerializedName(KEY_MORE_SONGS_COUNT)
	public final int songsCount;
	@SerializedName(KEY_MORE_SONGS)
	public final String moreSongs;
	@SerializedName(KEY_MORE_SONGS_DATA)
	public final List<MediaItem> moreSongsItems;
	
	private Date date;
	
	public StreamItem(String action, long userId, String userName,
			String photoUrl, long conentId, String title, String albumName,
			String imageUrl, String bigImageUrl, String type, String time,
			int songsCount, String moreSongs, List<MediaItem> moreSongsItems) {
		
		this.action = action;
		this.userId = userId;
		this.userName = userName;
		this.photoUrl = photoUrl;
		this.conentId = conentId;
		this.title = title;
		this.albumName = albumName;
		this.imageUrl = imageUrl;
		this.bigImageUrl = bigImageUrl;
		this.type = type;
		this.time = time;
		this.songsCount = songsCount;
		this.moreSongs = moreSongs;
		this.moreSongsItems = moreSongsItems;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Date getDate() {
		return this.date;
	}
	
	
	
}
