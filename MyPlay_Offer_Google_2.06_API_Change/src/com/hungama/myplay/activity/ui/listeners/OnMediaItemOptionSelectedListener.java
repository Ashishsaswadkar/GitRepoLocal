package com.hungama.myplay.activity.ui.listeners;

import com.hungama.myplay.activity.data.dao.hungama.MediaItem;

/**
 * Interface definition to be invoked when any MediaItem's option was selected.
 */
public interface OnMediaItemOptionSelectedListener {

	/**
	 * Invoked on the Play Now option was selected.
	 * @param mediaItem to play now.
	 */
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position);
	
	/**
	 * Invoked on the Play Next option was selected.
	 * @param mediaItem to play next.
	 */
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem, int position);
	
	/**
	 * Invoked on the Add To Queue option was selected.
	 * @param mediaItem to add the queue.
	 */
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem, int position);
	
	/**
	 * Invoked on the Show Details option was selected.
	 * @param mediaItem to show its details.
	 */
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem, int position);
	
	/**
	 * Invoked on the item was selected to be removed.
	 * @param mediaItem that was removed.
	 */
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position);
}
