package com.hungama.myplay.activity.ui.fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.ui.FavoritesActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.MediaDetailsActivity;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment.PlayingEventListener;
import com.hungama.myplay.activity.ui.fragments.PlaylistDialogFragment.OnPlaylistPerformActionListener;
import com.hungama.myplay.activity.ui.listeners.OnLoadMenuItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Utils;

/**
 * Presents the current playing queue from the {@link PlayerBarFragment}.
 */
public class PlayerQueueFragment extends SherlockFragment implements OnClickListener, PlayingEventListener {

	public static final String TAG = "PlayerQueueFragment";
	
	private FragmentManager mFragmentManager;
	
	private PlayerBarFragment mPlayerBar; 
	
	private TextView mTextTitle;
	private ImageButton mButtonOptions;
	
	private int mTileSize;
	private GridView mTilesGridView;
	private PlayerQueueAdapter mQueueAdapter;
	
	private List<Track> mTracks;
	
	private OnPlayerQueueClosedListener mOnPlayerQueueClosedListener;
	
	
	// ======================================================
	// Public interface.
	// ======================================================
	
	public interface OnPlayerQueueClosedListener {
		
		public void onPlayerQueueClosed();
	}
	
	public void setOnPlayerQueueUpdetedListener(OnPlayerQueueClosedListener listener) {
		mOnPlayerQueueClosedListener = listener;
	}

	
	// ======================================================
	// FRAGMENT'S LIFE CYCLE. 
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPlayerBar = ((MainActivity) getActivity()).getPlayerBar();
		mFragmentManager = getChildFragmentManager();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_player_queue, container, false);
		initializeUserControls(rootView);
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		mTracks = mPlayerBar.getCurrentPlayingList();
		if (mTracks == null) {
			mTracks = new ArrayList<Track>();
		}
		
		if (mQueueAdapter == null) {
			mQueueAdapter = new PlayerQueueAdapter();
			mTilesGridView.setAdapter(mQueueAdapter);
		} else {
			mQueueAdapter.notifyDataSetChanged();
		}
		
		// updates the text of the title
		String title = getResources().getString(R.string.player_queue_title, mTracks.size());
		mTextTitle.setText(title);
		
		// registers for playing tracks events.
		mPlayerBar.setPlayingEventListener(this);
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key));
		FlurryAgent.onPageView();
		FlurryAgent.logEvent("Player queue");
	}
	
	@Override
	public void onPause() {
		
		if (isRemoving()) {
			if (mOnPlayerQueueClosedListener != null) {
				mOnPlayerQueueClosedListener.onPlayerQueueClosed();
			}
		}
		
		super.onPause();
	}
	
	@Override
	public void onStop() {
		// unregisters for playing tracks events.
		mPlayerBar.setPlayingEventListener(null);
		super.onStop();
		FlurryAgent.onEndSession(getActivity());
	}
	
	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_top_enter, R.anim.slide_and_show_top_exit, 
												R.anim.slide_and_show_top_enter, R.anim.slide_and_show_top_exit);
		
		if (viewId == R.id.player_queue_title_bar_button_options) {
			
			if (view.isSelected()) {
				// closes the option list.
				view.setSelected(false);
				view.setBackgroundResource(0);
				
				OptionsFragment optionsFragment = (OptionsFragment) mFragmentManager
													.findFragmentByTag(OptionsFragment.TAG);
				if (optionsFragment != null) {
					fragmentTransaction.remove(optionsFragment);
					fragmentTransaction.commit();
				}
				
			} else {
				// opens the option list.
				view.setSelected(true);
				view.setBackgroundResource(R.color.black);
				
				OptionsFragment optionsFragment = new OptionsFragment();
				
				fragmentTransaction.add(R.id.player_queue_content_container, optionsFragment, OptionsFragment.TAG);
				fragmentTransaction.commit();
			}
			
		} else if (viewId == R.id.player_queue_options_save_as_playlist || viewId == R.id.player_queue_options_clear_queue 
					|| viewId == R.id.player_queue_options_load_playlist || viewId == R.id.player_queue_options_load_favorites
					|| viewId == R.id.player_queue_options_exit_queue) {
			
			// closes the option list.
			mButtonOptions.setSelected(false);
			mButtonOptions.setBackgroundResource(0);
			
			OptionsFragment optionsFragment = (OptionsFragment) mFragmentManager
					.findFragmentByTag(OptionsFragment.TAG);
			if (optionsFragment != null) {
					fragmentTransaction.remove(optionsFragment);
					fragmentTransaction.commit();
			}
			
			if (viewId == R.id.player_queue_options_save_as_playlist) {
				
				List<Track> tracks = mPlayerBar.getCurrentPlayingList(); 
        	    boolean isFromLoadMenu = false;
        	    PlaylistDialogFragment playlistDialogFragment = PlaylistDialogFragment.newInstance(tracks, isFromLoadMenu);
        	    playlistDialogFragment.setOnPlaylistPerformActionListener(new OnPlaylistPerformActionListener() {
					@Override public void onSuccessed() { }
					@Override public void onFailed() { }
					@Override public void onCanceled() { }
				});
	        	playlistDialogFragment.show(mFragmentManager, PlaylistDialogFragment.FRAGMENT_TAG);
				
			} else if (viewId == R.id.player_queue_options_clear_queue) {
				
				final AlertDialog.Builder clearDialogBuilder = new AlertDialog.Builder(getActivity());
				clearDialogBuilder.setTitle(Utils.TEXT_EMPTY);
				clearDialogBuilder.setMessage(R.string.player_queue_message_confirm_clear_all);
				clearDialogBuilder.setCancelable(false);
				// sets the OK button.
				clearDialogBuilder.setPositiveButton(R.string.player_queue_message_confirm_clear_all_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						clearQueue();
					}
				});
				// sets the Cancel button.
				clearDialogBuilder.setNegativeButton(R.string.player_queue_message_confirm_clear_all_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				
				clearDialogBuilder.create().show();
				
			} else if (viewId == R.id.player_queue_options_load_playlist) {
				
				List<Track> playingQueue = mPlayerBar.getCurrentPlayingList(); 
				
				PlaylistDialogFragment selectPlaylistDialog = PlaylistDialogFragment.newInstance(playingQueue, true);
				selectPlaylistDialog.setOnLoadMenuItemOptionSelectedListener(new OnLoadMenuItemOptionSelectedListener() {
					
					@Override
					public void onLoadPlaylistFromDialogSelected(List<Track> tracks) {
						if (!Utils.isListEmpty(tracks)) {
							// adds the playlist's tracks to the playing queue.
							mPlayerBar.addToQueue(tracks);
							
							// updates the grid.
							mTracks = mPlayerBar.getCurrentPlayingList();
							mQueueAdapter.notifyDataSetChanged();
							
						}
					}
					
					@Override public void onLoadMenuTop10Selected(List<Track> topTenMediaItems) {}
					
					@Override public void onLoadMenuRadioSelected() {}
					
					@Override public void onLoadMenuMyPlaylistSelected() {}
					
					@Override public void onLoadMenuMyFavoritesSelected() {}
					
				});
		        selectPlaylistDialog.show(mFragmentManager, "PlaylistDialogFragment");
				
			} else if (viewId == R.id.player_queue_options_load_favorites) {
				
				// shows the favorite activity.
				Intent favoritesActivityIntent = new Intent(getActivity().getApplicationContext(), FavoritesActivity.class);
				startActivity(favoritesActivityIntent);
				
				// closes the player bar content.
				mPlayerBar.closeContent();
				
			} else if (viewId == R.id.player_queue_options_exit_queue) {
				// closes the player bar content.
				mPlayerBar.closeContent();
			}
			
		}
	}
	
	
	// ======================================================
	// Playing Events.
	// ======================================================
	
	@Override
	public void onTrackLoad() {
		if (mQueueAdapter != null)
			mQueueAdapter.notifyDataSetChanged();
	}

	@Override
	public void onTrackPlay() {
		if (mQueueAdapter != null)
			mQueueAdapter.notifyDataSetChanged();
	}

	@Override
	public void onTrackFinish() {
		if (mQueueAdapter != null)
			mQueueAdapter.notifyDataSetChanged();
	}
	
	
	// ======================================================
	// Private Helper methods.
	// ======================================================
	
	private void initializeUserControls(View rootView) {
		
		rootView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		int imageTileSpacing = getResources().getDimensionPixelSize(R.dimen.home_tiles_spacing_vertical);
		
		mTilesGridView = (GridView) rootView.findViewById(R.id.player_queue_gridview);
		
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
        
        mTilesGridView.setNumColumns(2);
        mTilesGridView.setColumnWidth(mTileSize);
        
        mTextTitle = (TextView) rootView.findViewById(R.id.player_queue_title_bar_text);
        mButtonOptions = (ImageButton) rootView.findViewById(R.id.player_queue_title_bar_button_options);
        mButtonOptions.setOnClickListener(this);
	}
	
	private void clearQueue() {
		// clears the queue.
		mPlayerBar.clearQueue();
		
		// updates the list.
		mTracks = mPlayerBar.getCurrentPlayingList();
		mQueueAdapter.notifyDataSetChanged();
		
		// updates the text of the title
		String title = getResources().getString(R.string.player_queue_title, mTracks.size());
		mTextTitle.setText(title);
	}
	
	
	private static final class ViewHolder {
		ImageView imageTile;
		ImageButton buttonPlay;
		Button buttonRemove;
		TextView textTitle;
		TextView textDescription;
		TextView textNowPlaying;
	}

	private class PlayerQueueAdapter extends BaseAdapter implements OnLongClickListener, OnClickListener {
		
		private LayoutInflater inflater;
		
		public PlayerQueueAdapter() {
			inflater = (LayoutInflater) getActivity().getApplicationContext()
								.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mTracks.size();
		}

		@Override
		public Object getItem(int position) {
			return mTracks.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mTracks.get(position).getId();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder viewHolder;
			
			if (convertView == null) {
				
				convertView = inflater.inflate(R.layout.list_item_player_queue_track, parent, false);
				viewHolder = new ViewHolder();
				
				viewHolder.imageTile = (ImageView) convertView.findViewById(R.id.player_queue_tile_image);
				viewHolder.buttonPlay = (ImageButton) convertView.findViewById(R.id.player_queue_tile_button_play);
				viewHolder.buttonRemove = (Button) convertView.findViewById(R.id.player_queue_tile_button_remove);
				viewHolder.textTitle = (TextView) convertView.findViewById(R.id.player_queue_tile_title);
				viewHolder.textDescription = (TextView) convertView.findViewById(R.id.player_queue_tile_description);
				viewHolder.textNowPlaying = (TextView) convertView.findViewById(R.id.player_queue_tile_now_playing);
				
				convertView.setTag(R.id.view_tag_view_holder, viewHolder);
				
			} else {
				viewHolder = (ViewHolder) convertView.getTag(R.id.view_tag_view_holder);
			}
			
			/*
			 * colors the tile, if it's the selected one, 
			 * also shows it's "NOW PLAYING" text.
			 */
			if (position == mPlayerBar.getCurrentPlayingInQueuePosition()) {
				// grey it
				viewHolder.imageTile.setBackgroundResource(R.color.player_queue_now_playing_background);
				viewHolder.textNowPlaying.setVisibility(View.VISIBLE);
				
				// makes the remove button gone, we can't delete a playing track.
//				viewHolder.buttonRemove.setVisibility(View.INVISIBLE);
				
				// adjusts the play / pause button based on the current playing state.
				if (mPlayerBar.isPlaying()) {
					// playing, shows the pause button.
					viewHolder.buttonPlay.setBackgroundResource(R.drawable.icon_general_pause_grey);
					viewHolder.buttonPlay.setSelected(true);
				} else {
					// pausing, shows the play button.
					viewHolder.buttonPlay.setBackgroundResource(R.drawable.icon_general_play_grey);
					viewHolder.buttonPlay.setSelected(false);
				}
				
			} else {
				// blue it

				/*
				 * Creates a pattern of coloring the tiles.  
				 */
				int row = position / 2;
				int column = position % 2;
				
				if (((row % 2 == 0) && (column % 2 == 0)) || ((row % 2 != 0) && (column % 2 != 0))) {
					viewHolder.imageTile.setBackgroundResource(R.drawable.background_music_tile_dark);
				} else {
					viewHolder.imageTile.setBackgroundResource(R.drawable.background_music_tile_light);
				}
				
				viewHolder.textNowPlaying.setVisibility(View.GONE);
				
				// enables the remove button.
				viewHolder.buttonRemove.setVisibility(View.VISIBLE);
				
				// sets default the icon of the button to play.
				viewHolder.buttonPlay.setBackgroundResource(R.drawable.icon_general_play_grey);
				viewHolder.buttonPlay.setSelected(false);
			}
			
			Track track = mTracks.get(position);
			
			// sets click listeners to tiles buttons.
			viewHolder.imageTile.setOnClickListener(this);
			viewHolder.buttonPlay.setOnClickListener(this);
			viewHolder.buttonRemove.setOnClickListener(this);
			
			// sets long click listeners to the tile and play button.
			viewHolder.imageTile.setOnLongClickListener(this);		
			viewHolder.buttonPlay.setOnLongClickListener(this);
			
			viewHolder.textTitle.setVisibility(View.VISIBLE);
			viewHolder.textDescription.setVisibility(View.VISIBLE);
			
			
			viewHolder.imageTile.getBackground().setDither(true);
			
			// sets the texts.
			viewHolder.textTitle.setText(track.getTitle());
			viewHolder.textDescription.setText(track.getAlbumName());
			
			/*
			 * sets the media item as the tag to the tile,
			 * so other invoked listeners methods can pull its reference. 
			 */
			convertView.setTag(R.id.view_tag_object, track);
			convertView.setTag(R.id.view_tag_position, position);
			
			// sets the size of the tile before it's being drawn.
			convertView.getLayoutParams().width = mTileSize;
			convertView.getLayoutParams().height = mTileSize;
			
			return convertView;
		}

		@Override
		public void onClick(View view) {
			int viewId = view.getId();
			// a tile was clicked, shows its media item's details.
			if (viewId == R.id.player_queue_tile_image) {
				// start play from beginning or pause
				RelativeLayout tile = (RelativeLayout) view.getParent();

				ViewHolder viewHolder = (ViewHolder) tile.getTag(R.id.view_tag_view_holder);
				int position = (Integer) tile.getTag(R.id.view_tag_position);
				
				handlePlayClick(position, viewHolder);
					
			// play now was selected.	
			} else if (viewId == R.id.player_queue_tile_button_play ) {
				// play or pause
				RelativeLayout tile = (RelativeLayout) view.getParent();
				
				ViewHolder viewHolder = (ViewHolder) tile.getTag(R.id.view_tag_view_holder);
				int position = (Integer) tile.getTag(R.id.view_tag_position);
				
				handlePlayClick(position, viewHolder);
				
			// remove tile was selected.
			} else if (viewId == R.id.player_queue_tile_button_remove) {
				// remove track from queue 
				RelativeLayout tile = (RelativeLayout) view.getParent();
				
				int position = (Integer) tile.getTag(R.id.view_tag_position);
				
				// removes the original from the player.
				mPlayerBar.removeFrom(position);
				
				// updates the current list.
				mTracks = mPlayerBar.getCurrentPlayingList();
				
				notifyDataSetChanged();
				
				// updates the title.
				// updates the text of the title
				String title = getResources().getString(R.string.player_queue_title, mTracks.size());
				mTextTitle.setText(title);
			}
		}

		@Override
		public boolean onLongClick(View view) {
			int viewId = view.getId();
			if (viewId == R.id.player_queue_tile_image || viewId == R.id.player_queue_tile_button_play) {
				// get the item's id from the tile itself.
				RelativeLayout tile = (RelativeLayout) view.getParent();
				
				ViewHolder viewHolder = (ViewHolder) tile.getTag(R.id.view_tag_view_holder);
				Track track = (Track) tile.getTag(R.id.view_tag_object);
				int position = (Integer) tile.getTag(R.id.view_tag_position);
				
				showMediaItemOptionsDialog(track, position, viewHolder);
			}
			
			return false;
		}
		
		private void handlePlayClick(int position, ViewHolder viewHolder) {
			if (mPlayerBar.getCurrentPlayingInQueuePosition() == position) {
				if (mPlayerBar.isPlaying()) {
					// checks if the current tile is in the state of play or pause.
					if (viewHolder.buttonPlay.isSelected()) {
						// currently is playing, pauses and shows the "play" button.
						mPlayerBar.pause();
						// sets the button's icon and state.
						viewHolder.buttonPlay.setBackgroundResource(R.drawable.icon_general_play_grey);
						viewHolder.buttonPlay.setSelected(false);
					} else {
						// currently is paused, resumes playing and shows the "pause" button.
						mPlayerBar.play();
						// sets the button's icon and state.
						viewHolder.buttonPlay.setBackgroundResource(R.drawable.icon_general_pause_grey);
						viewHolder.buttonPlay.setSelected(true);						
					}
				}
			} else {
				// goto the new track
				mPlayerBar.playFromPosition(position);
				// update adapter. 
				notifyDataSetChanged();
			}
		}
		
		private void showMediaItemOptionsDialog(final Track track, final int position, final ViewHolder viewHolder) {
			//set up custom dialog
			final Dialog mediaItemOptionsDialog = new Dialog(getActivity());
			
			mediaItemOptionsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mediaItemOptionsDialog.setContentView(R.layout.dialog_media_playing_options);
			mediaItemOptionsDialog.setCancelable(true);
	        mediaItemOptionsDialog.show();
	        
			// sets the title.
	        TextView title = (TextView) mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_title_text);
	        title.setText(track.getTitle());
	        
	        // sets the cancel button.
	        ImageButton closeButton = (ImageButton) mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_title_image);
	        closeButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mediaItemOptionsDialog.dismiss();				
				}
			});
	        
	        // sets the options buttons.
	        LinearLayout llPlayNow = (LinearLayout) mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_play_now_row);	     
	        LinearLayout llAddtoQueue = (LinearLayout) mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_add_to_queue_row);
	        LinearLayout llDetails = (LinearLayout) mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_details_row);

	        llAddtoQueue.setVisibility(View.GONE);

	        // play now.
	        llPlayNow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mediaItemOptionsDialog.dismiss();
					handlePlayClick(position, viewHolder);
				}
			});        
	        
	        // show details.
	        llDetails.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mediaItemOptionsDialog.dismiss();
					
					MediaItem mediaItem = new MediaItem(track.getId(), track.getTitle(), track.getAlbumName(), 
														track.getArtistName(), track.getImageUrl(), track.getBigImageUrl(), 
														MediaType.TRACK.toString().toLowerCase(), 0);
					
					mediaItem.setMediaContentType(MediaContentType.MUSIC);
					mediaItem.setMediaType(MediaType.TRACK);
					
					Intent intent = new Intent(getActivity().getApplicationContext(), MediaDetailsActivity.class);
					intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM, (Serializable) mediaItem);
					
					getActivity().startActivity(intent);
				}
			});
		}
		
	}
	
	
	// ======================================================
	// Options.
	// ======================================================
	
	private class OptionsFragment extends Fragment {
		
		public static final String TAG = "PlayerQueueFragment.OptionsFragment";
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_player_queue_options, container, false);
			
			Button buttonSaveAsPlaylist = (Button) rootView.findViewById(R.id.player_queue_options_save_as_playlist);
			Button buttonClearQueue = (Button) rootView.findViewById(R.id.player_queue_options_clear_queue);
			Button buttonLoadPlaylist = (Button) rootView.findViewById(R.id.player_queue_options_load_playlist);
			Button buttonLoadFavorites = (Button) rootView.findViewById(R.id.player_queue_options_load_favorites);
			Button buttonExitQueue = (Button) rootView.findViewById(R.id.player_queue_options_exit_queue);
			
			buttonSaveAsPlaylist.setOnClickListener(PlayerQueueFragment.this);
			buttonClearQueue.setOnClickListener(PlayerQueueFragment.this);
			buttonLoadPlaylist.setOnClickListener(PlayerQueueFragment.this);
			buttonLoadFavorites.setOnClickListener(PlayerQueueFragment.this);
			buttonExitQueue.setOnClickListener(PlayerQueueFragment.this);
			
			return rootView;
		}
	}

}
