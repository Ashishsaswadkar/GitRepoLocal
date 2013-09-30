package com.hungama.myplay.activity.data;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.util.Utils;

/**
 * Utility class for constructing and accesing compaigns.
 */
public class CampaignsManager {

	private CampaignsManager() {}
	
	/**
	 * Populates the campaign nodes with their campaign's ID.
	 */
	public static void setCampaignIdForNode(Node node, String campaignId){
		node.setCampaignID(campaignId);
		List<Node> nodes = node.getChildNodes();
		if(nodes != null && !nodes.isEmpty()){
			for(Node n : nodes){
				setCampaignIdForNode(n, campaignId);
			}
		}else{
			return;
		}
	}
	
	public static Node findCampaignRootNodeByID(List<Campaign> campaigns, String id){
		Node node = null;
		if(campaigns != null && !campaigns.isEmpty()){
			for(Campaign campaign : campaigns){
				if(campaign.getID().equalsIgnoreCase(id)){
					node = campaign.getNode();
					break;
				}
			}
		}
		return node;
	}
	
	public static List<Placement> getAllPlacementsOfType(List<Campaign> campaigns, String type) {
		if (TextUtils.isEmpty(type))
			return null;
		
		if (Utils.isListEmpty(campaigns))
			return null;
		
		// Build a list of Placement
		// Note: i had to loop each Placement and set it's Campaign's id,
		// cause there is Campaign's id attribute in Placement object.
		List<Placement> campaignPlacements;
		List<Placement> resultPlacements = new ArrayList<Placement>();
	
		if(campaigns != null && !campaigns.isEmpty()) {
			// Loop the Campaigns
			for(Campaign c : campaigns){
				List<Placement> placements = c.getPlacements();
				
				if(!Utils.isListEmpty(placements)){
					campaignPlacements = new ArrayList<Placement>(c.getPlacements());
					// Loop the Placements
					for(Placement placement : campaignPlacements){
						placement.setCampaignID(c.getID());
						if(placement.getPlacementType().equalsIgnoreCase(type)){
							resultPlacements.add(placement);
						}
					}
				}
			}
		}
		
		return resultPlacements;
	}
	
}
