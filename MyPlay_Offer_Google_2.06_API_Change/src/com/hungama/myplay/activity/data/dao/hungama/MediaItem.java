package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class MediaItem implements Serializable {
	
	public static final String KEY_CONTENT_ID = "content_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_ALBUM_NAME = "album_name";
	public static final String KEY_ARTIST_NAME = "artist_name";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_BIG_IMAGE = "big_image";
	public static final String KEY_TYPE = "type";
	public static final String KEY_MUSIC_TRACKS_COUNT = "music_tracks_count";
	
	@Expose
	@SerializedName(KEY_CONTENT_ID)
	protected final long id;
	@Expose
	@SerializedName(KEY_TITLE)
	protected final String title;
	@Expose
	@SerializedName(KEY_ALBUM_NAME)
	protected final String albumName;
	@Expose
	@SerializedName(KEY_ARTIST_NAME)
	protected final String artistName;
	@Expose
	@SerializedName(KEY_IMAGE)
	protected final String imageUrl;
	@Expose
	@SerializedName(KEY_BIG_IMAGE)
	protected final String bigImageUrl;
	@Expose
	@SerializedName(KEY_TYPE)
	protected final String type;
	@Expose
	@SerializedName(KEY_MUSIC_TRACKS_COUNT)
	protected int musicTrackCount;
	
	protected MediaType mediaType = null;
	protected MediaContentType mediaContentType = null;
	
	public MediaItem(long id, String title, String albumName, String artistName, 
					String imageUrl, String bigImageUrl, String type, int musicTrackCount) {
		
		this.id = id;
		this.title = title;
		this.albumName = albumName;
		this.artistName = artistName;
		this.imageUrl = imageUrl;
		this.bigImageUrl = bigImageUrl;
		this.type = type;
		this.musicTrackCount = musicTrackCount;
	}
	
	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}
	
	public String getAlbumName() {
		return albumName;
	}
	
	public String getArtistName() {
		return artistName;
	}

	public String getImageUrl() {
		return imageUrl;
	}
	
	public String getBigImageUrl() {
		return bigImageUrl;
	}

	public MediaType getMediaType() {
		if (mediaType == null)
			mediaType = MediaType.getMediaItemByName(type);
		
		return mediaType;
	}

	public MediaContentType getMediaContentType() {
		return mediaContentType;
	}

	public void setMediaContentType(MediaContentType mediaContentType) {
		this.mediaContentType = mediaContentType;
	}

	public int getMusicTrackCount() {
		return musicTrackCount;
	}
	
	public void setMusicTrackCount(int value){
		this.musicTrackCount = value;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}
	
	public MediaItem getCopy() {
		long id = Long.valueOf(this.id);
		String title = TextUtils.isEmpty(this.title) ? null : this.title; 
		String albumName = TextUtils.isEmpty(this.albumName) ? null : this.albumName;
		String artistName = TextUtils.isEmpty(this.artistName) ? null : this.artistName;
		String imageUrl = TextUtils.isEmpty(this.imageUrl) ? null : this.imageUrl;
		String bigImageUrl = TextUtils.isEmpty(this.bigImageUrl) ? null : this.bigImageUrl;
		String type = TextUtils.isEmpty(this.type) ? null : this.type;
		int musicTrackCount = Integer.valueOf(this.musicTrackCount);
		
		MediaType mediaType = MediaType.getMediaItemByName(type);
		MediaContentType mediaContentType = null;
		mediaContentType = MediaContentType.getCopy(mediaContentType);
		
		MediaItem mediaItem = new MediaItem(id, title, albumName, artistName, imageUrl, bigImageUrl, type, musicTrackCount);
		mediaItem.setMediaType(mediaType);
		mediaItem.setMediaContentType(mediaContentType);
		
		return mediaItem;
	}
	
}
