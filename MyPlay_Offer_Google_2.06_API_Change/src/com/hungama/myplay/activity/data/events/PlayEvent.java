package com.hungama.myplay.activity.data.events;

import java.io.Serializable;

/**
 * Implementation of {@link Event} for logging Playing actions like "play track", "stop track".
 */
public class PlayEvent extends Event implements Serializable {
	
	public enum PlayingSourceType {
		STREAM,
		DOWNLOAD,
		CACHED,
		SAVED
	}

	private final long mediaId;
	private final String mediaKind;
	private final PlayingSourceType playingSourceType;
	private final long deliveryId;

	private final int startPosition;
	private final int stopPosition;
	
 	public PlayEvent(long consumerId, String deviceId, long deliveryId, boolean completePlay, int duration, String timestamp, float latitude, float longitude, 
			long mediaId, String mediaKind, PlayingSourceType playingSourceType, int startPosition, int stopPosition) {
		
		super(consumerId, deviceId, completePlay, duration, timestamp, latitude, longitude, mediaId);
		
		this.mediaId = mediaId;
		this.mediaKind = mediaKind;
		this.playingSourceType = playingSourceType;
		this.deliveryId = deliveryId;
		this.startPosition = startPosition;
		this.stopPosition = stopPosition;
	}
	
	// getters:
	
	public long getMediaId() {
		return mediaId;
	}

	public String getMediaKind() {
		return mediaKind;
	}

	public long getDeliveryId() {
		return deliveryId;
	}
	
	public PlayingSourceType getPlayingSourceType() {
		return playingSourceType;
	}

	public boolean isCompletePlay() {
		return completePlay;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public int getStopPosition() {
		return stopPosition;
	}
	
	public PlayEvent newInstance() {
		return new PlayEvent(consumerId, deviceId, deliveryId, completePlay, 
				duration, timestamp, latitude, longitude, mediaId, 
				mediaKind, playingSourceType, startPosition, stopPosition);
	}

}
