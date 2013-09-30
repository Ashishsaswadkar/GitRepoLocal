package com.hungama.myplay.activity.ui.listeners;

import java.util.List;

import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.Track;

/**
 * Interface definition to be invoked when any track's option was selected in queue.
 */
public interface OnLoadMenuItemOptionSelectedListener {

	/**
	 * Invoked on the Play Now option was selected.
	 * @param track to play now.
	 */
	public void onLoadMenuTop10Selected(List<Track> topTenMediaItems);
	
	/**
	 * Invoked on the Play Next option was selected.
	 * @param track to play next.
	 */
	public void onLoadMenuRadioSelected();
	
	/**
	 * Invoked on the Add To Queue option was selected.
	 * @param track to add the queue.
	 */
	public void onLoadMenuMyPlaylistSelected();
	
	/**
	 * Invoked on the Show Details option was selected.
	 * @param track to show its details.
	 */
	public void onLoadMenuMyFavoritesSelected();
	
	/**
	 * Invoked on the Show Details option was selected.
	 * @param track to show its details.
	 */
	public void onLoadPlaylistFromDialogSelected(List<Track> tracks);

}
