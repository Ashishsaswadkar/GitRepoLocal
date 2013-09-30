package com.hungama.myplay.activity.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.ui.fragments.FavoritesFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.fragments.FavoritesFragment.OnMediaItemsLoadedListener;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class FavoritesActivity extends MainActivity implements CommunicationOperationListener, 
																OnMediaItemOptionSelectedListener,
																OnMediaItemsLoadedListener {

	private static final String TAG = "FavoritesActivity";
	
	
	private DataManager mDataManager;
	private FragmentManager mFragmentManager;
	
	private PlayerBarFragment mPlayerBar;
	
	private TextView mTitle;
	private Button playAllButton;
	
	// ======================================================
	// ACTIVITY'S LIFECYCLE.
	// ======================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_with_title);
		
		mDataManager = DataManager.getInstance(getApplicationContext());						
		mFragmentManager = getSupportFragmentManager();
		mPlayerBar = getPlayerBar();
		
		// adjust the title.
		mTitle = (TextView) findViewById(R.id.main_title_bar_text);
		mTitle.setText(Utils.TEXT_EMPTY);
		
		playAllButton = (Button) findViewById(R.id.home_button_music_top_categories);
		
		// shows the favorite type selection dialog.
		showFavoritesMediaTypeSelectionDialog();
	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}
	
	// ======================================================
	// Helper Methods.
	// ======================================================

	private void showFavoritesMediaTypeSelectionDialog() {
		
		//set up custom dialog
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_my_favorites_options);
		dialog.setCancelable(true);
		dialog.show();       
        
        // sets the cancel button.
        ImageButton closeButton = (ImageButton) dialog.findViewById(R.id.long_click_custom_dialog_title_image);
        closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();	
				finish();
			}
		});
        
        // sets the options buttons.
        LinearLayout favSongs = (LinearLayout) dialog.findViewById(R.id.long_click_custom_dialog_play_now_row1);
        LinearLayout favAlbums = (LinearLayout) dialog.findViewById(R.id.long_click_custom_dialog_add_to_queue_row2);
        LinearLayout favPlaylists = (LinearLayout) dialog.findViewById(R.id.long_click_custom_dialog_details_row3);
        LinearLayout favVideos = (LinearLayout) dialog.findViewById(R.id.long_click_custom_dialog_details_row4);
        
        // fav songs.
        favSongs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showFavoriteFragmentFor(MediaType.TRACK);			
				dialog.dismiss();
			}
		});        
                
        // fav albums.
        favAlbums.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showFavoriteFragmentFor(MediaType.ALBUM);
				dialog.dismiss();			
			}
		});
        
        // fav playlists.
        favPlaylists.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showFavoriteFragmentFor(MediaType.PLAYLIST);
				dialog.dismiss();
			}
		});
        
        // fav videos.
        favVideos.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showFavoriteFragmentFor(MediaType.VIDEO);
				dialog.dismiss();
			}
		});
	}

	private void setTitle(MediaType mMediaType, int size) {
		// SetS title bar
		if (mMediaType ==  MediaType.ALBUM) {
			mTitle.setText(getResources().getString(R.string.favorite_fragment_title_albums, size));
			playAllButton.setVisibility(View.GONE);
		} else if (mMediaType ==  MediaType.TRACK) {
			mTitle.setText(getResources().getString(R.string.favorite_fragment_title_songs, size));
			playAllButton.setVisibility(View.VISIBLE);
		} else if (mMediaType ==  MediaType.PLAYLIST) {
			mTitle.setText(getResources().getString(R.string.favorite_fragment_title_playlists, size));
			playAllButton.setVisibility(View.GONE);
		} else if (mMediaType ==  MediaType.VIDEO) {
			mTitle.setText(getResources().getString(R.string.favorite_fragment_title_videos, size));
			playAllButton.setVisibility(View.GONE);
		}
	}
	
	private void showFavoriteFragmentFor(MediaType mediaType) {
		
		FavoritesFragment favoritesFragment = new FavoritesFragment();
		favoritesFragment.setOnMediaItemOptionSelectedListener(this);
		favoritesFragment.setOnMediaItemsLoadedListener(this);
		
		Bundle arguments = new Bundle();
		arguments.putSerializable(FavoritesFragment.FRAGMENT_ARGUMENT_MEDIA_TYPE, (Serializable) mediaType);
		arguments.putString(FavoritesFragment.FRAGMENT_ARGUMENT_USER_ID, mDataManager.getApplicationConfigurations().getPartnerUserId());
		favoritesFragment.setArguments(arguments);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
												R.anim.slide_left_exit,
								                R.anim.slide_right_enter,
								                R.anim.slide_right_exit);
		fragmentTransaction.add(R.id.main_fragmant_container, favoritesFragment);
		fragmentTransaction.commit();
	}
	
	
	// ======================================================
	// Communication Operations events.
	// ======================================================
		
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS)
			showLoadingDialog(R.string.application_dialog_loading_content);
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			MediaItem mediaItem = (MediaItem) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);
			
			if (mediaItem.getMediaType() == MediaType.ALBUM || mediaItem.getMediaType() == MediaType.PLAYLIST) {
				
				MediaSetDetails setDetails = (MediaSetDetails) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
				PlayerOption playerOptions = (PlayerOption) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_PLAYER_OPTION);
				
				List<Track> tracks = setDetails.getTracks();
				if (playerOptions == PlayerOption.OPTION_PLAY_NOW) {
					mPlayerBar.playNow(tracks);
					
				} else if (playerOptions == PlayerOption.OPTION_PLAY_NEXT) {
					mPlayerBar.playNext(tracks);
					
				} else if (playerOptions == PlayerOption.OPTION_ADD_TO_QUEUE) {
					mPlayerBar.addToQueue(tracks);
				}
			}			
		}
		hideLoadingDialog();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		if (errorType != ErrorType.OPERATION_CANCELLED)
			hideLoadingDialog();
	}

	@Override
	public void onMediaItemsLoaded(final MediaType mediaType, final String userId, final List<MediaItem> mediaItems) {
		setTitle(mediaType, (mediaItems != null ? mediaItems.size() : 0));
		playAllButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mediaType == MediaType.TRACK) {
					List<Track> tracks = new ArrayList<Track>();
					for (MediaItem mediaItem : mediaItems) {
						if (mediaItem.getMediaType() == MediaType.TRACK) {
							Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), mediaItem.getAlbumName(), 
													mediaItem.getArtistName(), mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
							tracks.add(track);

						}
					}
					
					mPlayerBar.addToQueue(tracks);
				}
				
			}
		});
		
	}
	
	
	// ======================================================
	// ACTIVITY'S EVENT LISTENERS - HOME.
	// ======================================================
	
	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Play Now: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
										mediaItem.getAlbumName(), mediaItem.getArtistName(), 
										mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				mPlayerBar.playNow(tracks);
			} else {
				mDataManager.getMediaDetails(mediaItem, PlayerOption.OPTION_PLAY_NOW, this);
			}
		}
	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Play Next: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
										mediaItem.getAlbumName(), mediaItem.getArtistName(), 
										mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				mPlayerBar.playNext(tracks);
			} else {
				mDataManager.getMediaDetails(mediaItem, PlayerOption.OPTION_PLAY_NEXT, this);
			}
		}
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Add to queue: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
										mediaItem.getAlbumName(), mediaItem.getArtistName(), 
										mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				mPlayerBar.addToQueue(tracks);
			} else {
				mDataManager.getMediaDetails(mediaItem, PlayerOption.OPTION_ADD_TO_QUEUE, this);
			}
		}
	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Show Details: " + mediaItem.getId());
		Intent intent = null;
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			intent = new Intent(this, MediaDetailsActivity.class);
			intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM, (Serializable) mediaItem);
		} else {
			intent = new Intent(this, VideoActivity.class);
			intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO, (Serializable) mediaItem);
		}
		startActivity(intent);
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Remove item: " + mediaItem.getId());
	}

}
