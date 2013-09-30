package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class CollectionItem extends MediaItem implements Serializable {
	
	public static final String KEY_DOWNLOAD_COUNT = "download_count";
	public static final String KEY_ORDER_ID = "order_id";
	public static final String KEY_PLAN_NAME = "plan_name";
	public static final String KEY_DOWNLOAD_DATE = "download_date";
	
	@Expose
	@SerializedName(KEY_DOWNLOAD_COUNT)
	protected final int downloadCount;
	@Expose
	@SerializedName(KEY_ORDER_ID)
	protected final long orderId;
	@Expose
	@SerializedName(KEY_PLAN_NAME)
	protected final String planName;
	@Expose
	@SerializedName(KEY_DOWNLOAD_DATE)
	protected final String downloadDate;
	
	public CollectionItem(long id, String title, String albumName, String artistName, 
			String imageUrl, String bigImageUrl, String type, int musicTrackCount, 
			int downloadCount, long orderId, String planName, String downloadDate) {
		
		super(id, title, albumName, artistName, imageUrl, bigImageUrl, type, musicTrackCount);
		
		this.downloadCount = downloadCount;
		this.orderId = orderId;
		this.planName = planName;
		this.downloadDate = downloadDate;
	}

	public int getDownloadCount() {
		return downloadCount;
	}

	public long getOrderId() {
		return orderId;
	}

	public String getPlanName() {
		return planName;
	}

	public String getDownloadDate() {
		return downloadDate;
	}
	
}
	

