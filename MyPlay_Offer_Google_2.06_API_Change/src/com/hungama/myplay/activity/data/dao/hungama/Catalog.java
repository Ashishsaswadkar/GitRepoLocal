/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.SerializedName;

/**
 * @author offerperetz
 *
 */
public class Catalog {

	@SerializedName("content")
	private Content content;
	
	public Catalog() { }

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}
}
