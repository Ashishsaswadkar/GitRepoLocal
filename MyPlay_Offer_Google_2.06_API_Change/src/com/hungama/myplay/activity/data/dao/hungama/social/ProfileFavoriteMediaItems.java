package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;

public class ProfileFavoriteMediaItems {

	public static final String KEY_START_INDEX = "startIndex";
	public static final String KEY_LENGTH = "length";
	public static final String KEY_TOTAL_COUNT = "total_count";
	public static final String KEY_ALBUMS = "data";

	@SerializedName(KEY_START_INDEX)
	public final int startIndex;
	@SerializedName(KEY_LENGTH)
	public final int length;
	@SerializedName(KEY_TOTAL_COUNT)
	public final int totalCount;
	@SerializedName(KEY_ALBUMS)
	public final List<MediaItem> mediaItems;
	
	private MediaType mediaType;
	
	public ProfileFavoriteMediaItems(int startIndex, int length, int totalCount, List<MediaItem> mediaItems) {
		this.startIndex = startIndex;
		this.length = length;
		this.totalCount = totalCount;
		this.mediaItems = mediaItems;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}
	
}
