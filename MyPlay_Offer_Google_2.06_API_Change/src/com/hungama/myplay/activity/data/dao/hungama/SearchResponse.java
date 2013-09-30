package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class SearchResponse implements Serializable {
	
	public static final String KEY_START_INDEX = "startIndex";
	public static final String KEY_LENGTH = "length";
	public static final String KEY_TOTAL_COUNT = "total";
	public static final String KEY_SONGS_COUNT = "songs";
	public static final String KEY_ALBUM_COUNT = "album";
	public static final String KEY_ARTIST_COUNT = "artist";
	public static final String KEY_VIDEO_COUNT = "video";
	public static final String KEY_PLAYLIST_COUNT = "playlist";
	public static final String KEY_CONTENT = "content";
	
	@Expose
	@SerializedName(KEY_START_INDEX)
	private final long startIndex;
	@Expose
	@SerializedName(KEY_LENGTH)
	private final int length;
	@Expose
	@SerializedName(KEY_TOTAL_COUNT)
	private final int totalCount;
	@Expose
	@SerializedName(KEY_SONGS_COUNT)
	private final int songsCount;
	@Expose
	@SerializedName(KEY_ALBUM_COUNT)
	private final int albumCount;
	@Expose
	@SerializedName(KEY_ARTIST_COUNT)
	private final int artistCount;
	@Expose
	@SerializedName(KEY_VIDEO_COUNT)
	private final int videoCount;
	@Expose
	@SerializedName(KEY_PLAYLIST_COUNT)
	private final int playlistCount;
	@Expose
	@SerializedName(KEY_CONTENT)
	private final List<MediaItem> content;
	
	private String query;
	private String type;
	
	public SearchResponse(long startIndex, int length, int totalCount, int songsCount, int albumCount, 
						  int artistCount, int videoCount, int playlistCount, List<MediaItem> content) {
		this.startIndex = startIndex;
		this.length = length;
		this.totalCount = totalCount;
		this.songsCount = songsCount;
		this.albumCount = albumCount;
		this.artistCount = artistCount;
		this.videoCount = videoCount;
		this.playlistCount = playlistCount;
		this.content = content;
	}

	public long getStartIndex() {
		return startIndex;
	}

	public int getLength() {
		return length;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public int getSongsCount() {
		return songsCount;
	}

	public int getAlbumCount() {
		return albumCount;
	}

	public int getArtistCount() {
		return artistCount;
	}

	public int getVideoCount() {
		return videoCount;
	}

	public int getPlaylistCount() {
		return playlistCount;
	}

	public List<MediaItem> getContent() {
		return content;
	}

	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
}
