package com.hungama.myplay.activity.data;

/**
 * Defines aplication's custom Intent / Event actions being notified by vary component of the application.
 */
public abstract class ActionDefinition {
	
	/**
	 * Activity Action: Notifies when a Media Item has been added to / removed from favorites. 
	 */
	public static final String ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED = "action_media_item__favorite_state_changed";
	
	/**
	 * Extra Media Item of the given action.
	 */
	public static final String EXTRA_MEDIA_ITEM = "extra_media_item";
	
	/**
	 * Extra flag for iindicating if the Media Item has marked as favorite or not.
	 */
	public static final String EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE = "extra_media_item_favorite_is_favorite";
	
	/**
	 * Extra number of the new favorites for the given Media Item.
	 */
	public static final String EXTRA_MEDIA_ITEM_FAVORITE_COUNT = "extra_media_item_favorite_count";

}
