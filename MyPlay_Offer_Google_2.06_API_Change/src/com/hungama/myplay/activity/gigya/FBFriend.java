/**
 * 
 */
package com.hungama.myplay.activity.gigya;

import java.util.List;

/**
 * @author DavidSvilem
 *
 */
public class FBFriend {

	public String UID;
	public String nickname;
	public String photoURL;
	public String thumbnailURL;
	
	public boolean isSiteUser;
	public boolean isSiteUID;
	
	List<Identity> identities;
	
	public FBFriend(){}
}
