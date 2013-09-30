package com.hungama.myplay.activity.data.dao.hungama.social;


import com.google.gson.annotations.SerializedName;

public class UserDiscover {
	
	public static final String KEY_ID = "discovery_id";
	public static final String KEY_NAME = "discovery_name";
	
	@SerializedName(KEY_ID)
	public final long id;
	@SerializedName(KEY_NAME)
	public final String name;
	
	public UserDiscover(int id, String name) {
		this.id = id;
		this.name = name;
	}

}
