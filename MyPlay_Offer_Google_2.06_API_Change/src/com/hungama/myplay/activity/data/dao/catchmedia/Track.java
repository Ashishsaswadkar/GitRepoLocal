package com.hungama.myplay.activity.data.dao.catchmedia;

import java.io.Serializable;
import java.util.Map;

import com.hungama.myplay.activity.data.persistance.InventoryContract;
import com.hungama.myplay.activity.data.persistance.Itemable;

import android.content.ContentValues;
import android.database.Cursor;

public class Track implements Itemable, Serializable {
	
	public static final String[] COLUMNS = new String[]{
		InventoryContract.Tracks.ID,
		InventoryContract.Tracks.NAME,
		InventoryContract.Tracks.ALBUM_ID,
		InventoryContract.Tracks.ARTIST_ID,
		InventoryContract.Tracks.TRACK_NUMBER,
		InventoryContract.Tracks.IS_CACHED,
		InventoryContract.Tracks.DELIVERY_ID,
		InventoryContract.Tracks.DO_NOT_CACHE,
		InventoryContract.Tracks.LIMIT_DURATION
	};
	
	private long id;
	private String name;
	private long albumId;
	private long artistId;
	private int trackNumber;
	
	private boolean isCached;
	
	private String streamUrl;

	private int limitDuration;
	private long deliveryId; 
	private boolean doNotCache;
	
	private boolean isStreamUrlLodaed = false;
		
	/**
	 * Contractor for factoring new instances,
	 * also it might be for junk creation. 
	 */
	public Track() {
//		this.id = 0;
//		this.name = null;
//		this.albumId = 0;
//		this.artistId = 0;
//		this.trackNumber = 0;
//		this.isCached = false;
	}
	
	public Track(long id, String name, long albumId, long artistId, int trackNumber, boolean isCached) {
		this.id = id;
		this.name = name;
		this.albumId = albumId;
		this.artistId = artistId;
		this.trackNumber = trackNumber;
		this.isCached = isCached;
	}
	
	public Track(long id, String name, long albumId, long artistId, int trackNumber, 
							boolean isCached, int limitDuration, long deliveryId, boolean doNotCache) {
		
		this.id = id;
		this.name = name;
		this.albumId = albumId;
		this.artistId = artistId;
		this.trackNumber = trackNumber;
		this.isCached = isCached;
		
		this.limitDuration = limitDuration;
		this.deliveryId = deliveryId;
		this.doNotCache = doNotCache;
	}
		
	// getters:
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public long getAlbumId() {
		return albumId;
	}
	
	public long getArtistId() {
		return artistId;
	}
	
	public int getTrackNumber() {
		return trackNumber;
	}
	
	public boolean isCached() {
		return isCached;
	}
	
	@Override
	public String getIdColumnName() {
		return InventoryContract.Tracks.ID;
	}

	@Override
	public String getTableName() {
		return InventoryContract.Tables.TRACKS;
	}

	@Override
	public String[] getTableColumns() {
		return COLUMNS;
	}
	
	// mapping methods

	@Override
	public Itemable getInitializedObject(Cursor cursor) {
		long tempId = cursor.getLong(0);
		String tempName = cursor.getString(1);
		long tempAlbumId = cursor.getLong(2);
		long tempArtistId = cursor.getLong(3);
		int tempTrackNumber = cursor.getInt(4);
		boolean tempIsCached = cursor.getInt(5) > 0;

		long tempDeliveryId = cursor.getLong(6);
		boolean tempDoNotCache = cursor.getInt(7) > 0;
		int tempLimitDuration = cursor.getInt(8);
		
		return new Track(tempId, tempName, tempAlbumId, tempArtistId, 
					tempTrackNumber, tempIsCached, tempLimitDuration, tempDeliveryId, tempDoNotCache);
	}
	
	@Override
	public Itemable getInitializedObject(Map map) {
		String tempStringId = (String) map.get(InventoryContract.Tracks.ID);
		Long tempId = Long.valueOf(tempStringId);
		String tempName = (String) map.get(InventoryContract.Tracks.NAME);
		Long tempAlbumId = (Long) map.get(InventoryContract.Tracks.ALBUM_ID);
		Long tempArtistId = (Long) map.get(InventoryContract.Tracks.ARTIST_ID);
		int tempTrackNumber = ((Long) map.get(InventoryContract.Tracks.TRACK_NUMBER)).intValue();
		boolean tempIsCached = false;
		
		return new Track(tempId, tempName, tempAlbumId, tempArtistId, tempTrackNumber, tempIsCached);
	}

	@Override
	public ContentValues getObjectFieldValues() {
		ContentValues values = new ContentValues();
		values.put(InventoryContract.Tracks.ID, id);
		values.put(InventoryContract.Tracks.NAME, name);
		values.put(InventoryContract.Tracks.ALBUM_ID, albumId);
		values.put(InventoryContract.Tracks.ARTIST_ID, artistId);
		values.put(InventoryContract.Tracks.TRACK_NUMBER, trackNumber);
		values.put(InventoryContract.Tracks.IS_CACHED, (isCached == true ? 1 : 0));
		values.put(InventoryContract.Tracks.DELIVERY_ID, deliveryId);
		values.put(InventoryContract.Tracks.DO_NOT_CACHE, (doNotCache == true ? 1 : 0));
		values.put(InventoryContract.Tracks.LIMIT_DURATION, limitDuration);
		
		return values;
	}
	
	// setters:
	
	public void setCached(boolean isCached) {
		this.isCached = isCached;
	}
	
	// lazy loading members:
	
	private String albumName; 
	private String artistName;
					
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	
	public int getLimitDuration() {
		return limitDuration;
	}
	
	public boolean isStreamUrlLoaded() {
		return isStreamUrlLodaed;
	}
	
	/**
	 * Retrieves track's delivery id, calls {@link PlayingTrack.getStreamUrl()}
	 */
	public long getDeliveryId() {
		return deliveryId;
	}
	
	/**
	 * Calls {@link PlayingTrack.getStreamUrl()}
	 */
	public boolean isDoNotCache() {
		return doNotCache;
	}
	
	public Track getNewInstance() {
		
		Track track = new Track(id, name, albumId, artistId, trackNumber, isCached, limitDuration, deliveryId, doNotCache);
		track.setAlbumName(albumName);
		track.setArtistName(artistName);
		
		return track;
	}
	
}
