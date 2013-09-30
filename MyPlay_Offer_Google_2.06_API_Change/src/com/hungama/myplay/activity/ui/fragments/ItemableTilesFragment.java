package com.hungama.myplay.activity.ui.fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.JsonRPC2Methods;
import com.hungama.myplay.activity.playlist.PlaylistManager;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.MediaDetailsActivity;
import com.hungama.myplay.activity.ui.PlaylistsActivity;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.adapters.MediaTilesAdapter;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class ItemableTilesFragment extends MediaTileGridFragment implements 
											OnMediaItemOptionSelectedListener,
											CommunicationOperationListener{

	public static final String TAG = "ItemableTilesFragment";
	
	private DataManager mDataManager;
	private PlaylistManager mPlaylistManager;
	private MediaType mMediaType;
	
	private GridView mTilesGridView;
	private int mTileSize = 0;
	
	private MediaTilesAdapter mMediaTilesAdapter;
	
	private List<Track> mTracks = new ArrayList<Track>();
	private List<Playlist> mPlaylists = new ArrayList<Playlist>();
	private List<MediaItem> mediaItems;
	
	private Playlist selectedPlaylist;
	
	private ProgressDialog mProgressDialog;
	
	int positionToDelete;
	
	public ItemableTilesFragment(MediaType mediaType, Playlist selectedPlaylist){
		this.mMediaType = mediaType;
		this.selectedPlaylist = selectedPlaylist;
	}
	
	
	// ======================================================
	// Fragment callbacks.
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		mPlaylistManager = PlaylistManager.getInstance(getActivity().getApplicationContext());
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		int imageTileSpacing = getResources().getDimensionPixelSize(R.dimen.home_tiles_spacing_vertical);
		
		mTilesGridView = new GridView(getActivity());
		// sets the gird's properties.
		mTilesGridView.setGravity(Gravity.CENTER_HORIZONTAL);
		mTilesGridView.setVerticalSpacing(imageTileSpacing);
		mTilesGridView.setNumColumns(GridView.AUTO_FIT);
		mTilesGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mTilesGridView.setOverScrollMode(GridView.OVER_SCROLL_NEVER);
		}
		// sets the background.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mTilesGridView.setBackground(null);
		} else {
			mTilesGridView.setBackgroundDrawable(null);
		}
		
		// sets the gridview's cool margin.
		GridView.MarginLayoutParams params = 
				new GridView.MarginLayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
		params.setMargins(imageTileSpacing, imageTileSpacing, imageTileSpacing, imageTileSpacing);
		mTilesGridView.setLayoutParams(params);
		mTilesGridView.setPadding(0, imageTileSpacing, 0, imageTileSpacing);
		
		/*
		 * For placing the tiles correctly in the grid, 
		 * calculates the maximum size that a tile can be and the column width.
		 */
		
		// measuring the device's screen width. and setting the grid column width.
        Display display = getSherlockActivity().getWindowManager().getDefaultDisplay();
        int screenWidth = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
        	screenWidth = display.getWidth();
        } else {
        	Point displaySize = new Point();
        	display.getSize(displaySize);
        	screenWidth = displaySize.x;
        }
        
        mTileSize = (int) ((screenWidth - (imageTileSpacing + imageTileSpacing*1.5)) / 2);
        
        Logger.i(TAG, "screenWidth: " + screenWidth + " mTileSize: " + mTileSize);
        mTilesGridView.setNumColumns(2);
        mTilesGridView.setColumnWidth(mTileSize);

		return mTilesGridView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(mMediaType == MediaType.PLAYLIST){
			
			mPlaylists = getPlaylists();
			
			String title = getString(R.string.itemable_text_title, "(" + mPlaylists.size() + ")");
			updateTitle(title, mMediaType);
			
		} else if(mMediaType == MediaType.TRACK){
			
			// Get mTracks by PlayList
			mTracks = mPlaylistManager.getTracksListByPlaylist(selectedPlaylist);
			
			String title = selectedPlaylist.getName() + " (" + mTracks.size() + ")";
			updateTitle(title, mMediaType);
			
		}
		
		buildMediaItemsList();

		/*
		 * Builds the Grids Adapter after we have the list of items to present.
		 */
        mMediaTilesAdapter = new MediaTilesAdapter(getActivity(), mTilesGridView, mTileSize, mediaItems, true);
		mTilesGridView.setAdapter(mMediaTilesAdapter);
		if(mMediaType == MediaType.PLAYLIST){
			mMediaTilesAdapter.setShowDetailsInOptionsDialogEnabled(false);
		}
		// deleting a playlist requires the user's permission by custom dialog.
		// deleting a track requires a success post of deleting.
		mMediaTilesAdapter.setOnlyCallbackWhenRemovingItem(true);
		mMediaTilesAdapter.setOnMusicItemOptionSelectedListener(this);
	}
		
	// ======================================================
	// Tiles Adapter callbacks.
	// ======================================================
	
	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position) {
		if(mMediaType == MediaType.PLAYLIST){
			// gets the selected playlist.
			Playlist playlist = getPlaylistById(mediaItem.getId());
			if (playlist != null) {
				// gets the playlist's track.
				List<Track> tracks = mPlaylistManager.getTracksListByPlaylist(playlist);
				if (!Utils.isListEmpty(tracks)) {
					PlayerBarFragment playerBar = ((MainActivity) getActivity()).getPlayerBar();
					playerBar.addToQueue(tracks);
				}
			}
			
		} else if(mMediaType == MediaType.TRACK) {
			// creates a track from the media item.
			Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
					mediaItem.getAlbumName(), mediaItem.getArtistName(), 
					mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
			List<Track> tracks = new ArrayList<Track>();
			tracks.add(track);
			// plays now the track.
			PlayerBarFragment playerBar = ((MainActivity) getActivity()).getPlayerBar();
			playerBar.playNow(tracks);
		}
	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem, int position) {}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem, int position) {
		if(mMediaType == MediaType.PLAYLIST) {
			// gets the selected playlist.
			Playlist playlist = getPlaylistById(mediaItem.getId());
			if (playlist != null) {
				// gets the playlist's track.
				List<Track> tracks = mPlaylistManager.getTracksListByPlaylist(playlist);
				if (!Utils.isListEmpty(tracks)) {
					PlayerBarFragment playerBar = ((MainActivity) getActivity()).getPlayerBar();
					playerBar.addToQueue(tracks);
				}
			}
			
		} else if(mMediaType == MediaType.TRACK) {
			// creates a track from the media item.
			Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
					mediaItem.getAlbumName(), mediaItem.getArtistName(), 
					mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
			List<Track> tracks = new ArrayList<Track>();
			tracks.add(track);
			// plays now the track.
			PlayerBarFragment playerBar = ((MainActivity) getActivity()).getPlayerBar();
			playerBar.addToQueue(tracks);
		}
	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem, int position) {
		
		if(mMediaType == MediaType.PLAYLIST){

			// Show selected mTracks's play list
			ItemableTilesFragment mTilesFragment = 
					new ItemableTilesFragment(MediaType.TRACK, (Playlist) mPlaylists.get(position));
			
			FragmentManager mFragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit,
	                R.anim.slide_right_enter,
	                R.anim.slide_right_exit);
			
			fragmentTransaction.replace(R.id.main_fragmant_container, mTilesFragment,TAG);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();

		}else if(mMediaType == MediaType.TRACK){
			// Show selected track details 
			Intent intent = new Intent(getActivity(), MediaDetailsActivity.class);
			intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM, (Serializable) mediaItem);
			startActivity(intent);
		}
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position) {
		
		if(mMediaType == MediaType.PLAYLIST){
			
			positionToDelete = position;
			
			final AlertDialog.Builder clearDialogBuilder = new AlertDialog.Builder(getActivity());
			clearDialogBuilder.setTitle(Utils.TEXT_EMPTY);
			clearDialogBuilder.setMessage(R.string.playlists_message_delete);
			clearDialogBuilder.setCancelable(true);
			// sets the OK button.
			clearDialogBuilder.setPositiveButton(R.string.playlists_message_delete_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
//					// Delete selected play list
//					mediaItems.remove(positionToDelete);
//					mMediaTilesAdapter.notifyDataSetChanged();
										
					if (positionToDelete >= 0 && positionToDelete < mPlaylists.size()) {
						
						// Updates the server with the deletion.
						Playlist playlist = (Playlist) mPlaylists.get(positionToDelete);
						mDataManager.playlistOperation(ItemableTilesFragment.this, playlist.getId(), null , null, JsonRPC2Methods.DELETE);
					}										
				}
			});
			
			clearDialogBuilder.create().show();
			
		}else if(mMediaType == MediaType.TRACK){
			// Delete selected track from play list
			Track track = (Track) mTracks.get(position);
			selectedPlaylist.removeTrack(track.getId());
			mDataManager.playlistOperation(this, selectedPlaylist.getId(), 
					selectedPlaylist.getName(), selectedPlaylist.getTrackList(), JsonRPC2Methods.UPDATE);
		}
		
	}
	
	
	// ======================================================
	// Operations callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		showLoadingDialog(getActivity().getString(R.string.processing));
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
	
		switch (operationId) {
		case (OperationDefinition.CatchMedia.OperationId.PLAYLIST):
			
			hideLoadingDialog();
		
			/*
			 * Updates list items and the title.
			 */
			if(mMediaType == MediaType.PLAYLIST) {
				mPlaylists = getPlaylists();
				
				int size = mPlaylists != null ? mPlaylists.size() : 0;
				String title = getString(R.string.itemable_text_title, "(" + size + ")");
				updateTitle(title, mMediaType);
				
			} else if(mMediaType == MediaType.TRACK) {
				mTracks = mPlaylistManager.getTracksListByPlaylist(selectedPlaylist);
				
				int size = mTracks != null ? mTracks.size() : 0;
				String title = selectedPlaylist.getName() + " (" + size + ")";
				updateTitle(title, mMediaType);
			}			
			
			// Delete selected item and updates the grid.
			mediaItems.remove(positionToDelete);
			mMediaTilesAdapter.notifyDataSetChanged();
			
			break;

		default:
			break;
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		
		//getFragmentManager().popBackStack();
		hideLoadingDialog();	
		
	}
	
	
	// ======================================================
	// helper methods.
	// ======================================================
	
	private Playlist getPlaylistById(long id) {
		for (Playlist playlist : mPlaylists) {
			if (playlist.getId() == id)
				return playlist;
		}
		return null;
	}
	
	public void showLoadingDialog(String message) {
		if (!getActivity().isFinishing()) {
    		if (mProgressDialog == null) {
    			mProgressDialog = new ProgressDialog(getActivity());
    			mProgressDialog = ProgressDialog.show(getActivity(), "", message, true, true);
    		}
    	}
	}
	
	public void hideLoadingDialog() {
		if (mProgressDialog != null) {
    		mProgressDialog.dismiss();
    		mProgressDialog = null;
		}
	}
	
	public List<Track> getTracksByPlaylist(){
		
		List<Track> playlistTracks = new ArrayList<Track>();
		
		Map<Long,Track> allTracks = mDataManager.getStoredTracks();
		// Get the selected playlist's mTracks
		
		String tracks = selectedPlaylist.getTrackList();
		String tracksArr[] = null;
		if(!TextUtils.isEmpty(tracks)){
			tracksArr = tracks.split(" ");
		}
		
		// Loop all mTracks and add to mPlaylists the mTracks that belong to the selected playlist
		if(allTracks != null){
			if(tracksArr != null && tracksArr.length > 0){
				
				for(int index=0; tracksArr.length > index; index++){
					
					long id =  Long.parseLong(tracksArr[index]);
					Track t = allTracks.get(id);
					if(t != null){
						playlistTracks.add(t);						
					}
				}
			}
		}
		
		return playlistTracks;
	}
	
	
	/**
	 * Building MediaItems list from list of Playlist/mTracks (mPlaylists)
	 */
	private void buildMediaItemsList() {
	
		mediaItems = new ArrayList<MediaItem>();
		MediaItem mediaItem;
		
		if(mMediaType == MediaType.PLAYLIST){
			if(!Utils.isListEmpty(mPlaylists)){
				for(Playlist item : mPlaylists){
					
					mediaItem = new MediaItem(
							item.getId(), 
							item.getName(), 
							null, 
							null, 
							null, 
							null, 
							mMediaType.name().toLowerCase(), 
							0);
					
					mediaItem.setMediaType(MediaType.PLAYLIST);
					mediaItem.setMusicTrackCount(((Playlist) item).getNumberOfTracks());
					mediaItem.setMediaContentType(MediaContentType.MUSIC);
					
					mediaItems.add(mediaItem);
				}
			}
			
		} else if(mMediaType == MediaType.TRACK){
			if(!Utils.isListEmpty(mTracks)) {
				for(Track track : mTracks) {
					
					mediaItem = new MediaItem(
							track.getId(), 
							track.getTitle(), 
							track.getAlbumName(), 
							track.getArtistName(), 
							track.getImageUrl(), 
							track.getBigImageUrl(), 
							mMediaType.name().toLowerCase(), 
							0);
					
					mediaItem.setMediaType(MediaType.TRACK);
					mediaItem.setMediaContentType(MediaContentType.MUSIC);
					
					mediaItems.add(mediaItem);
				}
			}
		}
	}
	
	private void updateTitle(String title, MediaType mediaType) {
		Activity activity = getActivity();
		if (activity instanceof PlaylistsActivity) {
			PlaylistsActivity playlistsActivity = (PlaylistsActivity) activity;
			playlistsActivity.getMainTitleBarText().setText(title);
			if (mediaType == MediaType.TRACK) {
				ImageView mainTitleBarButtonOptions = playlistsActivity.getMainTitleBarButtonOptions();
				if (mTracks.size() > 0) {
					mainTitleBarButtonOptions.setVisibility(View.VISIBLE);
				}

				
			} else if(mediaType == MediaType.PLAYLIST) {
				ImageView mainTitleBarButtonOptions = playlistsActivity.getMainTitleBarButtonOptions();
				mainTitleBarButtonOptions.setVisibility(View.GONE);
			}
			
		} else if (activity instanceof ProfileActivity) {
			ProfileActivity profileActivity = (ProfileActivity) activity;
			profileActivity.setTitleBarText(title);
		}
	}
	
	private List<Playlist> getPlaylists() {
		
		// Get all playlists
		Playlist dummy = new Playlist();
		Map<Long, Playlist> map = mDataManager.getStoredPlaylists();
		List<Playlist> UpdatedPlaylists = new ArrayList<Playlist>();
		
		// Convert from Map<Long, Playlist> to List<Itemable>
		if (map != null && map.size() > 0) {
			for(Map.Entry<Long, Playlist> p : map.entrySet()){
				UpdatedPlaylists.add(p.getValue());
			}
		}
		
		return UpdatedPlaylists;
	}

	public List<Track> getTracksToPlayAll() {
		if (mTracks != null) {
			return mTracks;
		}
		return null;
	}
	
}
