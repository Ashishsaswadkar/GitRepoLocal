package com.hungama.myplay.activity.ui.listeners;

import com.hungama.myplay.activity.data.dao.hungama.Track;

/**
 * Interface definition to be invoked when any track's option was selected in queue.
 */
public interface OnQueueItemOptionSelectedListener {

	/**
	 * Invoked on the Play Now option was selected.
	 * @param track to play now.
	 */
	public void onQueueTileSelected(Track track, int currentPlayingIndex);
	
	/**
	 * Invoked on the Play Next option was selected.
	 * @param track to play next.
	 */
	public void onQueuePlayButtonSelected(Track track, int currentPlayingIndex);
	
	/**
	 * Invoked on the Add To Queue option was selected.
	 * @param track to add the queue.
	 */
	public void onQueueItemOptionAddToQueueSelected(Track track);
	
	/**
	 * Invoked on the Show Details option was selected.
	 * @param track to show its details.
	 */
	public void onQueueItemOptionShowDetailsSelected(Track track);
	
	/**
	 * Invoked on the item was selected to be removed.
	 * @param track that was removed.
	 */
	public void onQueueRemoveButtonSelected(Track track, int currentPlayingIndex);
}
