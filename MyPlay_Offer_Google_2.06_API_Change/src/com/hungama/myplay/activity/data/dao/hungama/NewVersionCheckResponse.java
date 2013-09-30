package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class NewVersionCheckResponse implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String KEY_IS_MANDATORY = "isMandatory";
	public static final String KEY_URL = "url";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DISCRIPTION = "discription";
	
	@SerializedName(KEY_IS_MANDATORY)	
	private String isMandatory;
	
	@SerializedName(KEY_URL)
	private String url;
	
	@SerializedName(KEY_TITLE)
	private String title;
	
	@SerializedName(KEY_DISCRIPTION)
	private List<VersionDiscription> discription;
	
	public NewVersionCheckResponse() {

	}

	public String getIsMandatory() {
		return isMandatory;
	}

	public void setIsMandatory(String isMandatory) {
		this.isMandatory = isMandatory;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<VersionDiscription> getDiscription() {
		return discription;
	}

	public void setDiscription(List<VersionDiscription> discription) {
		this.discription = discription;
	}
	
	public boolean isMandatory () {
		if (isMandatory.equalsIgnoreCase("true")) {
			return true;
		} else {
			return false;
		}
	}
}
