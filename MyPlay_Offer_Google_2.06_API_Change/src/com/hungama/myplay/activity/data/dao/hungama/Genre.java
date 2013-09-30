package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Genre object given from Hungama server to filter {@link MediaItem}s.
 */
public class Genre implements Serializable, CategoryTypeObject {
	
	@Expose
	@SerializedName("type")
	private final String type = CategoryTypeObject.TYPE_GENRE;
	@Expose
	@SerializedName("id")
	private final long id;
	@Expose
	@SerializedName("name")
	private final String name;
	
	private Category parentCategory = null;
	
	public Genre(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@Override
	public String getType() {
		return type;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public Category getParentCategory() {
		return parentCategory;
	}
	
	public void setParentCategory(Category parentCategory) {
		this.parentCategory = parentCategory;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Genre))
			return false;
		
		Genre genre = (Genre) o;
		if (!(this.id == genre.getId())){
			return false;
		}
		
		if (!(this.name.equalsIgnoreCase(genre.getName()))){
			return false;
		}
		
		return true;
	}
}
