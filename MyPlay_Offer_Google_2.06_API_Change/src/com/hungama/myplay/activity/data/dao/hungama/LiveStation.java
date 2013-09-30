package com.hungama.myplay.activity.data.dao.hungama;

import android.text.TextUtils;

public class LiveStation extends MediaItem {
	
	private final String description;
	private final String streamingUrl;

	public LiveStation (long id, String title, String description, String streamingUrl) {
		super(id, title, null, null, null, null, MediaType.TRACK.toString().toLowerCase(), 0);
		
		this.description = description;
		this.streamingUrl = streamingUrl;
	}

	public String getDescription() {
		return description;
	}

	public String getStreamingUrl() {
		return streamingUrl;
	}
	
	public LiveStation getCopy() {
		long id = Long.valueOf(this.id);
		String title = TextUtils.isEmpty(this.title) ? null : this.title;
		String description = TextUtils.isEmpty(this.description) ? null : this.description;
		String streamingUrl = TextUtils.isEmpty(this.streamingUrl) ? null : this.streamingUrl;
		
		return new LiveStation(id, title, description, streamingUrl);
	}
}
