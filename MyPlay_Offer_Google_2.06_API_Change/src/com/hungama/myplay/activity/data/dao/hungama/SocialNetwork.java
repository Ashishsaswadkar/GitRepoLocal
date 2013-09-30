package com.hungama.myplay.activity.data.dao.hungama;

import com.hungama.myplay.activity.R;

/**
 * Enumeration definition of social networks to sign in / up with to the application. 
 */
public enum SocialNetwork {
	
	FACEBOOK	(R.string.social_network_facebook),
	TWITTER		(R.string.social_network_twitter),
	GOOGLE		(R.string.social_network_google),
	NONE		(R.string.social_network_none); 
	
	private final int lableResourceId;
	
	private SocialNetwork(int lableResourceId) {
		this.lableResourceId = lableResourceId;
	}
	
	public int getLableResourceId() {
		return lableResourceId;
	}
}
