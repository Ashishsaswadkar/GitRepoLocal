package com.hungama.myplay.activity.data.dao.campaigns;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class WidgetDisplayOptions implements Serializable{

	public boolean show_thumb;
	public boolean show_text;
	public boolean show_bg_img;
	public boolean show_child_media;
	public boolean show_child_text;
	
	@SerializedName("static")
	public boolean is_static;
	
	public String thumb_pos;
	public String text_alignment;
	public String display_location;
	
	public List<Action> actions;
	
}
