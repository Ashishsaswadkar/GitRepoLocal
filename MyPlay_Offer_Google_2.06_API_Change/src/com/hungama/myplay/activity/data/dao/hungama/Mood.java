package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Mood implements Serializable {

	@Expose
	@SerializedName("id")
	private final int id;
	
	@Expose
	@SerializedName("name")
	private final String name;
	
	@Expose
	@SerializedName("image_big")
	private final String bigImageUrl;
	
	@Expose
	@SerializedName("image_small")
	private final String smallImageUrl;
	
	public Mood(int id, String name, String bigImageUrl, String smallImageUrl) {
		this.id = id;
		this.name = name;
		this.bigImageUrl = bigImageUrl;
		this.smallImageUrl = smallImageUrl;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getBigImageUrl() {
		return bigImageUrl;
	}

	public String getSmallImageUrl() {
		return smallImageUrl;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Mood))
			return false;
		
		Mood tmpMood = (Mood) o;
		
		if (this.id != tmpMood.getId())
			return false;
		
		if (!this.name.equalsIgnoreCase(tmpMood.getName()))
			return false;
		
		return true;
	}
}
