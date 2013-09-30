package com.hungama.myplay.activity.data.dao.campaigns;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class Node implements Serializable{

	private static final long serialVersionUID = 1L;
	
	// Data members
	private String campaignID;
	
	private Boolean showChildMeida;
	private Boolean showChildText;
		
	@SerializedName("display_widget_info")
	private DisplayWidgetInfo displayWidgetInfo;
	
	@SerializedName("child_nodes")
	private List<Node> childNodes;
	
	@SerializedName("display_info")
	private DisplayInfo displayInfo;
	
	@SerializedName("play_widget_info")
	private PlayWidgetInfo playWidgetInfo;
	
	@SerializedName("media_info")
	private MediaInfo mediaInfo;
		
	// Set methods
	public void setCampaignID(String id){
		this.campaignID = id;
	}
		
	// Get methods
	public String getCampaignID(){
		return this.campaignID;
	}
	
	public String getThumbSmall(){
		return this.displayInfo.thumb_small;
	}
	
	public String getThumbLarge(){
		return this.displayInfo.thumb_large;
	}
	
	public String getBgImageLarge(){
		return this.displayInfo.bg_image_large;
	}
	
	public String getBgImageSmall(){
		return this.displayInfo.bg_image_small;
	}
	
	public String getBgImageMedium(){
		return this.displayInfo.bg_image_medium;
	}
	
	public String getBgColor(){
		return this.displayInfo.bg_color;
	}
	
	public String getText1(){
		return this.displayInfo.text1; 
	}
	
	public String getText2(){
		return this.displayInfo.text2;
	}
	
	public String getText3(){
		return this.displayInfo.text3;
	}
	
	public String getText1Color(){
		return this.displayInfo.text1_color;
	}
	
	public String getText2Color(){
		return this.displayInfo.text2_color;
	}
	
	public String getText3Color(){
		return this.displayInfo.text3_color;
	}
	
	public List<Node> getChildNodes(){
		return this.childNodes;
	}
		
	public String getAction(){
		 return displayWidgetInfo.action;
	}
	
	public List<Action> getActionsList(){
		return playWidgetInfo.widget_display_options.actions;
	}
	
	public String getTrackingID(){
		return displayWidgetInfo.tracking_id;
		//return playWidgetInfo.tracking_id;
	}
	
	public Boolean isShowChildMedia(){
		return displayWidgetInfo.widget_display_options.show_child_media;
	}
	
	public Boolean isShowChildText(){
		return displayWidgetInfo.widget_display_options.show_child_text;
	}
	
	public Boolean isShowThumb(){		
		return displayWidgetInfo.widget_display_options.show_thumb;
	}
	
	public Boolean isShowBGImage(){
		return displayWidgetInfo.widget_display_options.show_bg_img;
	}
	
	public Boolean isShowText(){
		return displayWidgetInfo.widget_display_options.show_text;
	}
	
	public Boolean isStatic(){		
		return displayWidgetInfo.widget_display_options.is_static;
	}
	
	public String getThumbPos(){
		return displayWidgetInfo.widget_display_options.thumb_pos;
	}
	
	public String getTextAlignment(){
		return displayWidgetInfo.widget_display_options.text_alignment;
	}
	
	public String getWidgetType(){
		return displayWidgetInfo.widget_type;
	}
		
	public String getCampaignTitle(){
		return displayInfo.text1;
	}
	
	public String getMediaKind(){
		if(mediaInfo == null){
			return null;
		}else{
			return mediaInfo.media_kind;
		}
	}
	
	public String getMediaUrl(){
		return mediaInfo.media_url;
	}
		
}
