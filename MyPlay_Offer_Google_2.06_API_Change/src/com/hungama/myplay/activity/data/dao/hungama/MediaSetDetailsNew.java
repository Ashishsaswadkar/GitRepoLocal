/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * @author offerperetz
 *
 */
public class MediaSetDetailsNew implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SerializedName("catalog")
	private Catalog catalog;
	
	@SerializedName("content")
	private Content content;
	
	public MediaSetDetailsNew() { }

	public Catalog getCatalog() {
		return catalog;
	}

	public void setCatalog(Catalog catalog) {
		this.catalog = catalog;
	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}
}
