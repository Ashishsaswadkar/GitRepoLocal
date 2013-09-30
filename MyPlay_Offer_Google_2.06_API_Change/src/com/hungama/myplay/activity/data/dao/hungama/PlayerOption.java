/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama;

/**
 * Definition of player operations that where requested to be performed 
 * on the player when the media item types are Albums or Playlists,
 * which requires additional WS call to get their track list before they can been played
 * with the player.
 */
public enum PlayerOption {
	OPTION_PLAY_NOW,
	OPTION_PLAY_NEXT,
	OPTION_ADD_TO_QUEUE;
}
