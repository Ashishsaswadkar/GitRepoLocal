package com.hungama.myplay.activity.data.events;

import java.io.Serializable;

/**
 * Implementation of {@link Event} for logging campaign Playing actions like "play campaign video".
 */
public class CampaignPlayEvent extends Event implements Serializable {
	
	private final String campaignMediaId;
	private final long campaignId;
	private final String playType;
	
	public CampaignPlayEvent(long consumerId, String deviceId, boolean completePlay, int duration, 
			String timestamp, float latitude, float longitude, String campaignMediaId, long campaignId, String playType) {
		
		super(consumerId, deviceId, completePlay, duration, timestamp, latitude, longitude, campaignId);
		
		this.campaignMediaId = campaignMediaId;
		this.campaignId = campaignId;
		this.playType = playType;
	}

	
	// getters:
	
	public String getCampaignMediaId() {
		return campaignMediaId;
	}

	public long getCampaignId() {
		return campaignId;
	}
	
	public String getPlayType(){
		return playType;
	}
	
}
