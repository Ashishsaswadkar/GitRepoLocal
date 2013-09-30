package com.hungama.myplay.activity.data.dao.campaigns;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class Campaign implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@SerializedName("id")
	private String ID;
	
	@SerializedName("root_node")
	private Node rootNode;
		
	@SerializedName("control_parameters")
	private ControlParameters controlParameters;
	
	private List<Placement> placements;
	
 	// Empty C'tor
	public Campaign(){}
	
	// C'tor
	public Campaign(Map mapNode, List<Map> mapPlacements, Map<String, Object> mapCP ,String id){
		setID(id);
				
		//rootNode = new Node(mapNode, mapCP,id);
				
		//placements = (List<Map>) mapPlacements;
	}
	
	// Set methods
	public void setID(String id){
		this.ID = id;
	}
	
	// Get methods
	public String getID(){
		return this.ID;
	}
	
	public Node getNode(){
		return this.rootNode;
	}
	
	public List<Placement> getPlacements(){
		return this.placements;
	}
	
}
