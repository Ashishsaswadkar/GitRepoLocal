/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.SerializedName;

/**
 * @author offerperetz
 *
 */
public class Content {

	@SerializedName("content_id")
	private long contentId;
	
	@SerializedName("album_id")
	private long albumId;
	
	@SerializedName("album_name")
	private String albumName;
	
	@SerializedName("title")
	private String title;
	
	@SerializedName("image")
	private String image;
	
	@SerializedName("big_image")
	private String bigImage;
	
	@SerializedName("relyear")
	private String relyear;
	
	@SerializedName("genre")
	private String genre;
	
	@SerializedName("language")
	private String language;
	
	@SerializedName("mood")
	private String mood;
	
	@SerializedName("music_director")
	private String musicDirector;
	
	@SerializedName("singers")
	private String singers;
	
	@SerializedName("lyricist")
	private String lyricist;
	
	@SerializedName("cast")
	private String cast;
	
	@SerializedName("has_lyrics")
	private boolean hasLyrics;
	
	@SerializedName("has_trivia")
	private boolean hasTrivia;
	
	@SerializedName("has_video")
	private boolean hasVideo;
	
	@SerializedName("has_download")
	private boolean hasDownload;
	
	@SerializedName("comments_count")
	private int commentsCount;
	
	@SerializedName("fav_count")
	private int favCount;
	
	@SerializedName("plays_count")
	private int playsCount;
	
	@SerializedName("user_fav")
	private int userFav;
	
	@SerializedName("musicalbum")
	private MusicAlbum musicalbum;
	
	@SerializedName("musiclisting")
	private MusicListing musicListing;
	
	public Content() { }

	public long getContentId() {
		return contentId;
	}

	public void setContentId(long contentId) {
		this.contentId = contentId;
	}

	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public String getRelyear() {
		return relyear;
	}

	public void setRelyear(String relyear) {
		this.relyear = relyear;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getMood() {
		return mood;
	}

	public void setMood(String mood) {
		this.mood = mood;
	}

	public String getMusicDirector() {
		return musicDirector;
	}

	public void setMusicDirector(String musicDirector) {
		this.musicDirector = musicDirector;
	}

	public String getSingers() {
		return singers;
	}

	public void setSingers(String singers) {
		this.singers = singers;
	}

	public String getLyricist() {
		return lyricist;
	}

	public void setLyricist(String lyricist) {
		this.lyricist = lyricist;
	}

	public String getCast() {
		return cast;
	}

	public void setCast(String cast) {
		this.cast = cast;
	}

	public boolean isHasLyrics() {
		return hasLyrics;
	}

	public void setHasLyrics(boolean hasLyrics) {
		this.hasLyrics = hasLyrics;
	}

	public boolean isHasTrivia() {
		return hasTrivia;
	}

	public void setHasTrivia(boolean hasTrivia) {
		this.hasTrivia = hasTrivia;
	}

	public boolean isHasVideo() {
		return hasVideo;
	}

	public void setHasVideo(boolean hasVideo) {
		this.hasVideo = hasVideo;
	}

	public boolean isHasDownload() {
		return hasDownload;
	}

	public void setHasDownload(boolean hasDownload) {
		this.hasDownload = hasDownload;
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

	public MusicAlbum getMusicalbum() {
		return musicalbum;
	}

	public void setMusicalbum(MusicAlbum musicalbum) {
		this.musicalbum = musicalbum;
	}

	public MusicListing getMusicListing() {
		return musicListing;
	}

	public void setMusicListing(MusicListing musicListing) {
		this.musicListing = musicListing;
	}
}
