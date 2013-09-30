package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class VersionDiscription implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String KEY_VERSION_NUMBER = "versionNumber";
	public static final String KEY_PUBLISHED = "published";
	public static final String KEY_DESC = "desc";
	
	@SerializedName(KEY_VERSION_NUMBER)
	private String versionNumber;
	
	@SerializedName(KEY_PUBLISHED)
	private String published;
	
	@SerializedName(KEY_DESC)
	private String desc;
	
	public VersionDiscription() {

	}

	public String getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}

	public String getPublished() {
		return published;
	}

	public void setPublished(String published) {
		this.published = published;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	
}
