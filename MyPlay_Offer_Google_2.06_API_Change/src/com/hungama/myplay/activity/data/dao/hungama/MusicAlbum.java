/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.SerializedName;

/**
 * @author offerperetz
 *
 */
public class MusicAlbum {

	@SerializedName("content_id")
	private long contentId;
	
	@SerializedName("image")
	private String image;
	
	@SerializedName("big_image")
	private String bigImage;
	
	@SerializedName("title")
	private String title;
	
	@SerializedName("release_year")
	private String releaseYear;
	
	@SerializedName("language")
	private String language;
	
	@SerializedName("music_director")
	private String musicDirector;
	
	@SerializedName("music_tracks_count")
	private int musicTracksCount;
			
	@SerializedName("has_video")
	private boolean hasVideo;
	
	@SerializedName("video_tracks_count")
	private int videoTracksCount;
	
	@SerializedName("comments_count")
	private int commentsCount;
	
	@SerializedName("fav_count")
	private int favCount;
	
	@SerializedName("plays_count")
	private int playsCount;
	
	@SerializedName("user_fav")
	private int userFav;
	
	public MusicAlbum() { }

	public long getContentId() {
		return contentId;
	}

	public void setContentId(long contentId) {
		this.contentId = contentId;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getBigImage() {
		return bigImage;
	}

	public void setBigImage(String bigImage) {
		this.bigImage = bigImage;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getReleaseYear() {
		return releaseYear;
	}

	public void setReleaseYear(String releaseYear) {
		this.releaseYear = releaseYear;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getMusicDirector() {
		return musicDirector;
	}

	public void setMusicDirector(String musicDirector) {
		this.musicDirector = musicDirector;
	}

	public int getMusicTracksCount() {
		return musicTracksCount;
	}

	public void setMusicTracksCount(int musicTracksCount) {
		this.musicTracksCount = musicTracksCount;
	}

	public boolean isHasVideo() {
		return hasVideo;
	}

	public void setHasVideo(boolean hasVideo) {
		this.hasVideo = hasVideo;
	}

	public int getVideoTracksCount() {
		return videoTracksCount;
	}

	public void setVideoTracksCount(int videoTracksCount) {
		this.videoTracksCount = videoTracksCount;
	}

	public int getCommentsCount() {
		return commentsCount;
	}

	public void setCommentsCount(int commentsCount) {
		this.commentsCount = commentsCount;
	}

	public int getFavCount() {
		return favCount;
	}

	public void setFavCount(int favCount) {
		this.favCount = favCount;
	}

	public int getPlaysCount() {
		return playsCount;
	}

	public void setPlaysCount(int playsCount) {
		this.playsCount = playsCount;
	}

	public int getUserFav() {
		return userFav;
	}

	public void setUserFav(int userFav) {
		this.userFav = userFav;
	}
}
