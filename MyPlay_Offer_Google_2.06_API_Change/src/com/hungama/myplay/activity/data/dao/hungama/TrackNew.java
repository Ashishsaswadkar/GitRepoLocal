/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.SerializedName;

/**
 * @author offerperetz
 *
 */
public class TrackNew {
	
	@SerializedName("content_id")
	private long contentId;
	
	@SerializedName("title")
	private String title;
	
	@SerializedName("album_name")
	private String albumName;
	
	@SerializedName("singers")
	private String singers;
	
	@SerializedName("image")
	private String image;
	
	@SerializedName("big_image")
	private String bigImage;
	
	@SerializedName("fav_count")
	private int favCount;
	
	public TrackNew() { }

	public long getContentId() {
		return contentId;
	}

	public void setContentId(long contentId) {
		this.contentId = contentId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getSingers() {
		return singers;
	}

	public void setSingers(String singers) {
		this.singers = singers;
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

	public int getFavCount() {
		return favCount;
	}

	public void setFavCount(int favCount) {
		this.favCount = favCount;
	}
}
