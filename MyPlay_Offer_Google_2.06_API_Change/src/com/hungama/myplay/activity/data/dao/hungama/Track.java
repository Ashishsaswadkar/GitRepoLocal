package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.Map;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a single song to be played from a {@link MediaItem}.
 */
public class Track implements Serializable {
	
	public static final String KEY_CONTENT_ID = "content_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_ALBUM_NAME = "album_name";
	public static final String KEY_ARTIST_NAME = "artist_name";
	public static final String KEY_IMAGE_URL = "image";
	public static final String KEY_BIG_IMAGE_URL = "big_image";
	
	public static final String ID = "id";
	public static final String NAME = "name";
	
	@Expose
	@SerializedName(KEY_CONTENT_ID)
	private final long id;
	@Expose
	@SerializedName(KEY_TITLE)
	private final String title;
	@Expose
	@SerializedName(KEY_ALBUM_NAME)
	private final String albumName;
	@Expose
	@SerializedName(KEY_ARTIST_NAME)
	private final String artistName;
	@Expose
	@SerializedName(KEY_IMAGE_URL)
	private final String imageUrl;
	@Expose
	@SerializedName(KEY_BIG_IMAGE_URL)
	private final String bigImageUrl;

	private boolean isCached = false;
	private String mediaHandle = null;
	private long deliveryId = -1; 
	private boolean doNotCache = true;
	/*
	 * Timestamp pointining for the last time this track has updated
	 * his media handle, we use this time stamp as the device's time stamp
	 * to compare if it's the time to recall Media Handle's create operation.  
	 */
	private long currentPrefetchTimestamp = -1;

	private Object tag;
	
	public Track(long id, String title, String albumName, String artistName, String imageUrl, String bigImageUrl) {
		this.id = id;
		this.title = title;
		this.albumName = albumName;
		this.artistName = artistName;
		this.imageUrl = imageUrl;
		this.bigImageUrl = bigImageUrl;
	}
	
	// getters.
	
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
	
	
	public synchronized boolean isCached() {
		return isCached;
	}
	
	public synchronized String getMediaHandle() {
		return mediaHandle;
	}
	
	public synchronized long getDeliveryId() {
		return deliveryId;
	}
	
	public synchronized boolean isDoNotCache() {
		return doNotCache;
	}
	
	public synchronized long getCurrentPrefetchTimestamp() {
		return currentPrefetchTimestamp;
	}
	
	public Object getTag() {
		return tag;
	}
	
	
	// setters.

	public synchronized void setCached(boolean isCached) {
		this.isCached = isCached;
	}

	public synchronized void setMediaHandle(String mediaHandle) {
		this.mediaHandle = mediaHandle;
	}

	public synchronized void setDeliveryId(long deliveryId) {
		this.deliveryId = deliveryId;
	}

	public synchronized void setDoNotCache(boolean doNotCache) {
		this.doNotCache = doNotCache;
	}
	
	public synchronized void setCurrentPrefetchTimestamp(long currentPrefetchTimestamp) {
		this.currentPrefetchTimestamp = currentPrefetchTimestamp;
	}
	
	public void setTag(Object tag) {
		this.tag = tag;
	}
	
	public Track newCopy() {
		Track track = new Track(id, TextUtils.isEmpty(title) ? "" : new String(title),
									TextUtils.isEmpty(albumName) ? "" : new String(albumName), 
									TextUtils.isEmpty(artistName) ? "" : new String(artistName), 
									TextUtils.isEmpty(imageUrl) ? "" : new String(imageUrl), 
									TextUtils.isEmpty(bigImageUrl) ? "" : new String(bigImageUrl));
		track.setCached(isCached);
		track.setMediaHandle((TextUtils.isEmpty(mediaHandle) ? null : new String(mediaHandle)));
		track.setDeliveryId(deliveryId);
		track.setDoNotCache(doNotCache);
		track.setCurrentPrefetchTimestamp(currentPrefetchTimestamp);
		
		return track;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Track)) {
			return false;
		}
		
		Track other = (Track) o;
		
		if (this.id != other.getId()) {
			return false;
		}
		
		return true;
	}
	
}
