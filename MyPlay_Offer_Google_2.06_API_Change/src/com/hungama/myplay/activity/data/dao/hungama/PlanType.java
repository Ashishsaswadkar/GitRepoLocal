package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

/**
 * Enumeration definition types of {@link DownloadPlan} types.
 */
public enum PlanType implements Serializable  {
	MOBILE,
	GOOGLE,
	REEDEM;
	
	
	public static final PlanType getPlanByName(String name) {
		if (name.equalsIgnoreCase(GOOGLE.toString())) {
			return GOOGLE;
			
		} else if (name.equalsIgnoreCase(REEDEM.toString())) {
			return REEDEM;			
		
		} else if (name.equalsIgnoreCase(MOBILE.toString())) {
			return MOBILE;			
		}
		
		return GOOGLE;
	}
}
