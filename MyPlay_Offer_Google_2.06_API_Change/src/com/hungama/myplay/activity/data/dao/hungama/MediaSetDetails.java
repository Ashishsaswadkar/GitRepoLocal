package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents an aggregation of details for Album or Playlist {@link MediaItem} kind
 * from Hungama that contains list of tracks.
 */
public class MediaSetDetails implements Serializable {
	
	@Expose
	@SerializedName("content_id")
	private final long id;
	@Expose
	@SerializedName("image")
	private final String imageUrl;
	@Expose
	@SerializedName("big_image")
	private final String bigImageUrl;
	@Expose
	@SerializedName("title")
	private final String title;
	@Expose
	@SerializedName("release_year")
	private final String releaseYear;
	@Expose
	@SerializedName("language")
	private final String language;
	@Expose
	@SerializedName("music_director")
	private final String director;
	@Expose
	@SerializedName("music_tracks_count")
	private final int numberOfTracks;
	@Expose
	@SerializedName("has_video")
	private final int hasVideo;
	@Expose
	@SerializedName("video_tracks_count")
	private final String numberOfVideos;
	@Expose
	@SerializedName("comments_count")
	private final int numOfComments;
	@Expose
	@SerializedName("fav_count")
	private int numOfFav;
	@Expose
	@SerializedName("plays_count")
	private final int numOfPlays;
	@Expose
	@SerializedName("user_fav")
	private final int isFavorite;
	@Expose
	@SerializedName("track")
	private final List<Track> tracks;
	@Expose
	@SerializedName("video")
	private final List<MediaItem> videos;
	
	private MediaType mediaType;

	public MediaSetDetails(long id, String imageUrl, String bigImageUrl,
							String title, String releaseYear, String language, String director,
							int numberOfTracks, int hasVideo, String numberOfVideos,
							int numOfComments, int numOfFav, int numOfPlays, int isFavorite,
							List<Track> tracks, List<MediaItem> videos, MediaType mediaType) {
		this.id = id;
		this.imageUrl = imageUrl;
		this.bigImageUrl = bigImageUrl;
		this.title = title;
		this.releaseYear = releaseYear;
		this.language = language;
		this.director = director;
		this.numberOfTracks = numberOfTracks;
		this.hasVideo = hasVideo;
		this.numberOfVideos = numberOfVideos;
		this.numOfComments = numOfComments;
		this.numOfFav = numOfFav;
		this.numOfPlays = numOfPlays;
		this.isFavorite = isFavorite;
		this.tracks = tracks;
		this.videos = videos;
		this.mediaType = mediaType;
	}
	
	public long getContentId() {
		return id;
	}

	public String getImageUrl() {
		return imageUrl;
	}
	
	public String getBigImageUrl() {
		return bigImageUrl;
	}

	public String getTitle() {
		return title;
	}

	public String getReleaseYear() {
		return releaseYear;
	}

	public String getLanguage() {
		return language;
	}

	public String getDirector() {
		return director;
	}

	public int getNumberOfTracks() {
		return numberOfTracks;
	}

	public boolean isHasVideo() {
		if (hasVideo > 0){
			return true;
		}
		return false;
	}

	public String getNumberOfVideos() {
		return numberOfVideos;
	}

	public int getNumOfComments() {
		return numOfComments;
	}

	public int getNumOfFav() {
		return numOfFav;
	}

	public int getNumOfPlays() {
		return numOfPlays;
	}

	public boolean IsFavorite() {
		if (isFavorite > 0){
			return true;
		}
		return false;
	}

	public List<Track> getTracks() {
		return tracks;
	}
	
	public List<MediaItem> getVideos() {
		return videos;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}
	
	public void setNumOfFav(int numberOfFavorites) {
		this.numOfFav = numberOfFavorites;
	}
	
}
