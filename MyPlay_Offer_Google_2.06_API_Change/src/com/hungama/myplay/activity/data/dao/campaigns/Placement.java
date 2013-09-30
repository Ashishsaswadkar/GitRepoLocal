/**
 * 
 */
package com.hungama.myplay.activity.data.dao.campaigns;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author DavidSvilem
 *
 */
public class Placement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@SerializedName("control_parameters")
	private ControlParameters controlParameters;
	
	@SerializedName("display_widget_info")
	private DisplayWidgetInfo displayWidgetInfo;
	
	@SerializedName("display_info")
	private DisplayInfo displayInfo;
	
	private String placementType;
	private String bgImageSmall;
	private String bgImageLarge;

	private String campaignID;
	
	public void setCampaignID(String id){
		this.campaignID = id;
	}
	
	public String getPlacementType(){
		return controlParameters.placement_type;
	}
	
	public String getBgImageSmall(){
		return displayInfo.bg_image_small;
	}
	
	public String getBgImageLarge(){
		return displayInfo.bg_image_large;
	}
	
	public List<Action> getActions(){
		return displayWidgetInfo.widget_display_options.actions;
	}
	
	public String getCampaignID(){
		return campaignID;
	}
}
