package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;

public class UserFavoriteVideos {

	public static final String KEY_VIDEO_COUNT = "video_count";
	public static final String KEY_VIDEOS = "data";
	
	@SerializedName(KEY_VIDEO_COUNT)
	public final int videoCount;
	@SerializedName(KEY_VIDEOS)
	public final List<MediaItem> videos;
	
	public UserFavoriteVideos(int videoCount, List<MediaItem> videos) {
		this.videoCount = videoCount;
		this.videos = videos;
	}

}
