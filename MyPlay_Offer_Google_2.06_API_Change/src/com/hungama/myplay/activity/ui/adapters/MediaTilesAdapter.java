package com.hungama.myplay.activity.ui.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.hungama.myplay.activity.util.images.ImageCache.ImageCacheParams;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestBuilder;
import com.squareup.picasso.Target;


/**
 * Adapter that binds list of {@link MediaItem} objects in tiles.
 */
public class MediaTilesAdapter extends BaseAdapter implements OnLongClickListener, OnClickListener {
	
	private static final String TAG = "MediaTilesAdapter";
	public static final String VIEW_TAG_ALBUM = "Album";
	
	private Context mContext;
	private FragmentActivity mActivity;
	private Resources mResources;
	private LayoutInflater mInflater;
	
	private int mTileSize = 0;
	
	private boolean mIsEditModeEnabled = true;
	private boolean mIsShowDetailsInOptionsDialogEnabled = true;
	private boolean mShowDeleteButton = true;
	private boolean mShowOptionsDialog = true;
	
	private boolean mOnlyCallbackWhenRemovingItem = false;
	
	private List<MediaItem> mMediaItems;
	
	// Async image loading members.
	//private ImageFetcher mImageFetcher;
	
	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;
	
	private Dialog mediaItemOptionsDialog;
	
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	
	//hints	
	private RelativeLayout homeTileHint;
	
	// ======================================================
	// ADAPTER'S BASIC FUNCTIONALLITY METHODS.
	// ======================================================
	
	public MediaTilesAdapter (FragmentActivity activity, GridView gridView, int tileSize, List<MediaItem> mediaItems, boolean showDeleteButton) {
		
		this.mShowDeleteButton = showDeleteButton;
		
		mActivity = activity;
		mContext = mActivity.getApplicationContext();
		
		mResources = mContext.getResources();
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mDataManager = DataManager.getInstance(mContext.getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		
//		if (mediaItems != null) {
//			mMediaItems = mediaItems;
//		} else {
//			mMediaItems = new ArrayList<MediaItem>();
//		}
		
		mMediaItems = mediaItems;
		
		mTileSize = tileSize;
		
		/*
		 * Asynchronously images loading initialization stuff.
		 * Note that temporally it stores the images on the external storage.   
		 */
//        ImageCacheParams cacheParams = new ImageCacheParams(mActivity, DataManager.FOLDER_TILES_CACHE);
//        
//        // Set memory cache to 25% of mem class
//        cacheParams.setMemCacheSizePercent(mActivity, 0.10f);
//
//        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
//        mImageFetcher = new ImageFetcher(mActivity, mTileSize);
//        mImageFetcher.setLoadingImage(R.drawable.background_home_tile_album_default);
//        mImageFetcher.addImageCache(mActivity.getSupportFragmentManager(), cacheParams);
//		
//        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
//                // Pause fetcher to ensure smoother scrolling when flinging
//                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
//                    mImageFetcher.setPauseWork(true);
//                } else {
//                    mImageFetcher.setPauseWork(false);
//                }
//            }
//
//            @Override
//            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
//        });
	}
		
	@Override
	public int getCount() {
		return mMediaItems != null ? mMediaItems.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return mMediaItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mMediaItems.get(position).getId();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		MediaItem mediaItem = mMediaItems.get(position);
		MediaType mediaType = mediaItem.getMediaType();
		MediaContentType mediaContentType = mediaItem.getMediaContentType();
		
		if(mediaContentType == MediaContentType.RADIO){
			convertView = getMusicView(mediaItem, mediaType, convertView, parent, position, true);
			
		}else if (mediaContentType == MediaContentType.MUSIC 
				|| mediaType == MediaType.ALBUM 
				|| mediaType == MediaType.PLAYLIST 
				|| (mediaType == MediaType.TRACK && mediaContentType != MediaContentType.VIDEO)) {
			
			convertView = getMusicView(mediaItem, mediaType, convertView, parent, position, false);
			
		} else if (mediaContentType == MediaContentType.VIDEO || mediaType == MediaType.VIDEO) {
			convertView = getVideosView(mediaItem, mediaType, convertView, parent, position);
		}

		/*
		 * sets the media item as the tag to the tile,
		 * so other invoked listeners methods can pull its reference. 
		 */
		convertView.setTag(R.id.view_tag_object, mediaItem);
		convertView.setTag(R.id.view_tag_position, position);
		
		// sets the size of the tile before it's being drawn.
		convertView.getLayoutParams().width = mTileSize;
		convertView.getLayoutParams().height = mTileSize;
		
		return convertView;
	}
	
	@Override
	public void onClick(View view) {
		Logger.d(TAG, "Simple click on: " + view.toString());
		int viewId = view.getId();
		// a tile was clicked, shows its media item's details.
		if (viewId == R.id.home_music_tile_image || viewId == R.id.home_videos_tile_image || 
												viewId == R.id.home_videos_tile_button_play) {
			
			
			RelativeLayout tile = (RelativeLayout) view.getParent();
			MediaItem mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
			int position = (Integer) tile.getTag(R.id.view_tag_position);
			
			if (viewId == R.id.home_music_tile_image) {
				boolean isFirstVisitToPage = mApplicationConfigurations.isFirstVisitToHomeTilePage();
				homeTileHint = (RelativeLayout) tile.findViewById(R.id.home_tile_hint);
				if ((isFirstVisitToPage && 
						(mediaItem.getMediaContentType() != MediaContentType.VIDEO &&
						mediaItem.getMediaContentType() != MediaContentType.RADIO)))  {
					isFirstVisitToPage = false;
					mApplicationConfigurations.setIsFirstVisitToHomeTilePage(false);
					showHomeTileHint();
				} else if (mApplicationConfigurations.getHintsState()){
					if (!mApplicationConfigurations.isHomeHintShownInThisSession()) {
						mApplicationConfigurations.setIsHomeHintShownInThisSession(true);
						showHomeTileHint();						
					} else {
						
						homeTileHint.setVisibility(View.GONE);
						Logger.d(TAG, "Show details of: " + mediaItem.getId());
						
						if (mOnMediaItemOptionSelectedListener != null) {
							
							if (mediaItem.getMediaContentType() == MediaContentType.VIDEO || mediaItem.getMediaType() == MediaType.ALBUM || mediaItem.getMediaType() == MediaType.PLAYLIST ) {
								mOnMediaItemOptionSelectedListener.onMediaItemOptionShowDetailsSelected(mediaItem, position);
							} else {
								mOnMediaItemOptionSelectedListener.onMediaItemOptionAddToQueueSelected(mediaItem, position);
							}							
						}						
					}				
				} else {
					
					homeTileHint.setVisibility(View.GONE);
					Logger.d(TAG, "Show details of: " + mediaItem.getId());
					
					if (mOnMediaItemOptionSelectedListener != null) {
						
						if (mediaItem.getMediaContentType() == MediaContentType.VIDEO || mediaItem.getMediaType() == MediaType.ALBUM || mediaItem.getMediaType() == MediaType.PLAYLIST ) {
							mOnMediaItemOptionSelectedListener.onMediaItemOptionShowDetailsSelected(mediaItem, position);
						} else {
							mOnMediaItemOptionSelectedListener.onMediaItemOptionAddToQueueSelected(mediaItem, position);
						}
						
					}
				}
			} else {
				Logger.d(TAG, "Show details of: " + mediaItem.getId());
				
				if (mOnMediaItemOptionSelectedListener != null) {
					
					if (mediaItem.getMediaContentType() == MediaContentType.VIDEO || mediaItem.getMediaType() == MediaType.ALBUM || mediaItem.getMediaType() == MediaType.PLAYLIST ) {
						mOnMediaItemOptionSelectedListener.onMediaItemOptionShowDetailsSelected(mediaItem, position);
					} else {
						mOnMediaItemOptionSelectedListener.onMediaItemOptionAddToQueueSelected(mediaItem, position);
					}
					
				}
			}
						
		// play now was selected.	
		} else if (viewId == R.id.home_music_tile_button_play ) {
			
			RelativeLayout tile = (RelativeLayout) view.getParent();
			MediaItem mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
			int position = (Integer) tile.getTag(R.id.view_tag_position);
			
			Logger.d(TAG, "Play now item: " + mediaItem.getId());
			
			// Check if first time page is shown
			  // yes - show hint
			
			boolean isFirstVisitToPage = mApplicationConfigurations.isFirstVisitToHomeTilePage();
			homeTileHint = (RelativeLayout) tile.findViewById(R.id.home_tile_hint);
			if ((isFirstVisitToPage && 
					(mediaItem.getMediaContentType() != MediaContentType.VIDEO &&
					mediaItem.getMediaContentType() != MediaContentType.RADIO)))  {
				isFirstVisitToPage = false;
				mApplicationConfigurations.setIsFirstVisitToHomeTilePage(false);
				showHomeTileHint();
			} else if (mApplicationConfigurations.getHintsState()){
				if (!mApplicationConfigurations.isHomeHintShownInThisSession()) {
					mApplicationConfigurations.setIsHomeHintShownInThisSession(true);
					showHomeTileHint();						
				} else {
					
					homeTileHint.setVisibility(View.GONE);
					if (mOnMediaItemOptionSelectedListener != null) {
						mOnMediaItemOptionSelectedListener.onMediaItemOptionAddToQueueSelected(mediaItem, position);
					}					
				}				
			} else {
				
				homeTileHint.setVisibility(View.GONE);
				if (mOnMediaItemOptionSelectedListener != null) {
					mOnMediaItemOptionSelectedListener.onMediaItemOptionAddToQueueSelected(mediaItem, position);
				}
			}
		
		// remove tile was selected.
		} else if (viewId == R.id.home_music_tile_button_remove ||
							viewId == R.id.home_videos_tile_button_remove) {

			RelativeLayout tile = (RelativeLayout) view.getParent();
			MediaItem mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
			int position = (Integer) tile.getTag(R.id.view_tag_position);
			
			Logger.d(TAG, "Remove item: " + mediaItem.getId());
			
			if (!mOnlyCallbackWhenRemovingItem) {
				mMediaItems.remove(mediaItem);
				notifyDataSetChanged();
			}
			
			if (mOnMediaItemOptionSelectedListener != null) {
				mOnMediaItemOptionSelectedListener.onMediaItemOptionRemoveSelected(mediaItem, position);
			}
		}
	}
	
	@Override
	public boolean onLongClick(View view) {
		Logger.d(TAG, "Long click on: " + view.toString());
		int viewId = view.getId();
		// get the item's id from the tile itself.
		RelativeLayout tile = (RelativeLayout) view.getParent();
		MediaItem mediaItem = (MediaItem) tile.getTag(R.id.view_tag_object);
		int position = (Integer) tile.getTag(R.id.view_tag_position);
		
		// show tile's option was selected.
		if (viewId == R.id.home_music_tile_button_play || 
				viewId == R.id.home_music_tile_image ) {
			
			if (mShowOptionsDialog) {
				// sets its tile's options visible.
				showMediaItemOptionsDialog(mediaItem, position);
			}
			return true;
			
		} 
		
		return false;
	}
	
	private View getMusicView(MediaItem mediaItem, MediaType mediaType, View convertView, ViewGroup parent, int position, boolean isRadio) {
		
		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.list_item_home_music_tile, parent, false);

		}

		ImageView imageTile = (ImageView) convertView.findViewById(R.id.home_music_tile_image);
		ImageButton buttonPlay = (ImageButton) convertView.findViewById(R.id.home_music_tile_button_play);
		Button buttonRemove = (Button) convertView.findViewById(R.id.home_music_tile_button_remove);
		final TextView textTitle = (TextView) convertView.findViewById(R.id.home_music_tile_title);
		TextView textDescription = (TextView) convertView.findViewById(R.id.home_music_tile_description);
		final TextView textTitleSongOrPlaylist = (TextView) convertView.findViewById(R.id.home_music_tile_title_song_or_playlist);

		RelativeLayout radioTranslucentStripLayout = 
				(RelativeLayout) convertView.findViewById(R.id.radio_translucent_strip_layout);
		TextView radioArtist = (TextView) convertView.findViewById(R.id.radio_artist);

		// sets click listeners to tiles buttons.
		imageTile.setOnClickListener(this);
		buttonPlay.setOnClickListener(this);

		// sets long click listeners to the tile and play button.
		if (!isRadio) {
			imageTile.setOnLongClickListener(this);
			buttonPlay.setOnLongClickListener(this);
		}

		buttonRemove.setOnClickListener(null);
		
		if(mShowDeleteButton){
			buttonRemove.setOnClickListener(this);
			buttonRemove.setVisibility(View.VISIBLE);
		} else {
			if (buttonRemove.getVisibility() == View.VISIBLE) {
				buttonRemove.setVisibility(View.INVISIBLE);
			}
		}
		
		// media type different viewing of the tile.
		if (mediaType == MediaType.ALBUM) {
			// hides the texts.
			textTitleSongOrPlaylist.setVisibility(View.VISIBLE);
			textTitle.setVisibility(View.VISIBLE);
			textDescription.setVisibility(View.GONE);
			
			// sets the title.
			textTitle.setText(mediaItem.getTitle());
			textTitleSongOrPlaylist.setText(mResources.getString(R.string.home_music_tile_album_decription_title));
			
			// loads the image for it.			
			imageTile.setTag(R.id.view_tag_type, VIEW_TAG_ALBUM);
			//mImageFetcher.loadImageForTiles(mediaItem.getImageUrl(), holder.imageTile, holder.textTitleSongOrPlaylist, holder.textTitle);
			
			Target myTarget = new Target() {
				
				@Override
				public void onSuccess(Bitmap arg0) {
					textTitleSongOrPlaylist.setVisibility(View.INVISIBLE);
					textTitle.setVisibility(View.INVISIBLE);
				}
				
				@Override
				public void onError() {
					
				}
			};
			
			Picasso.with(mContext).cancelRequest(imageTile);
			if (mContext != null && mediaItem.getImageUrl() != null && !TextUtils.isEmpty(mediaItem.getImageUrl())) {
				RequestBuilder rb = Picasso.with(mContext).load(mediaItem.getImageUrl());
				rb.fetch(myTarget);
				rb.placeholder(R.drawable.background_home_tile_album_default).into(imageTile);
			}
			
			if(isRadio){
				textTitleSongOrPlaylist.setVisibility(View.GONE);
				textTitle.setVisibility(View.GONE);
				// Adding title if the type is Radio 
				radioTranslucentStripLayout.setVisibility(View.VISIBLE);
				radioArtist.setText(mediaItem.getTitle());
			}
			
		} else if (mediaType == MediaType.PLAYLIST) {
			
			// shows the texts.
			textTitleSongOrPlaylist.setVisibility(View.VISIBLE);
			textTitle.setVisibility(View.VISIBLE);
			textDescription.setVisibility(View.VISIBLE);
			
			// loads the image.
			imageTile.setImageResource(R.drawable.background_playlist_main_thumb);
			
			// sets the title.
			textTitle.setText(mediaItem.getTitle());
			textTitleSongOrPlaylist.setText(mResources.getString(R.string.home_music_tile_playlist_decription_title));
			
			// sets the description.
			textDescription.setText(mResources.getString(R.string.home_music_tile_playlist_decription_songs_amount, 
																			mediaItem.getMusicTrackCount()));
			
		} else if (mediaType == MediaType.TRACK) {
			// shows the texts.
			
			// For Radio the title should not appear.
			if (mediaItem.getMediaContentType() != MediaContentType.RADIO) {
				textTitleSongOrPlaylist.setVisibility(View.VISIBLE);
			} else {
				textTitleSongOrPlaylist.setVisibility(View.GONE);
			}
			
			textTitle.setVisibility(View.VISIBLE);
			textDescription.setVisibility(View.VISIBLE);
			
			/*
			 * Creates a pattern of coloring the tiles.  
			 */
			int row = position / 2;
			int column = position % 2;
			
			if (((row % 2 == 0) && (column % 2 == 0)) || ((row % 2 != 0) && (column % 2 != 0))) {
				imageTile.setImageResource(R.drawable.background_music_tile_dark);
			} else {
				imageTile.setImageResource(R.drawable.background_music_tile_light);
			}
			
			// sets the texts.
			textTitleSongOrPlaylist.setText(mResources.getString(R.string.search_results_layout_bottom_text_for_track));
			textTitle.setText(mediaItem.getTitle());
			textDescription.setText(mediaItem.getAlbumName());
		}			
		
		return convertView;
	}
	
	private View getVideosView(MediaItem mediaItem, MediaType mediaType, View convertView, ViewGroup parent, int position) {
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_home_videos_tile, parent, false);
		}
		
		ImageView tileImage = (ImageView) convertView.findViewById(R.id.home_videos_tile_image);
		ImageView playImage = (ImageView) convertView.findViewById(R.id.home_videos_tile_button_play);
		Button removeButton = (Button) convertView.findViewById(R.id.home_videos_tile_button_remove);
		TextView textBig = (TextView) convertView.findViewById(R.id.home_videos_tile_track_text_big);
		TextView textSmall = (TextView) convertView.findViewById(R.id.home_videos_tile_track_text_small);
		
		/*
		 * The adapter handled to different tile modes, one handling their functionality
		 * (TILES_MODE_REGULAR) and one for removing tiles from the list (TILES_MODE_SELECTION_FOR_REMOVE).  
		 */
		tileImage.setOnClickListener(this);
		playImage.setOnClickListener(this);
		tileImage.setOnLongClickListener(this);
		removeButton.setOnClickListener(null);
			
		textBig.setText(mediaItem.getTitle());
		textSmall.setText(mediaItem.getAlbumName());
		
		//mImageFetcher.loadImage(mediaItem.getImageUrl(), tileImage);
		
		Target myTarget = new Target() {
			
			@Override
			public void onSuccess(Bitmap arg0) {
				
			}
			
			@Override
			public void onError() {
				
			}
		};
			
		Picasso.with(mContext).cancelRequest(tileImage);
		if (mContext != null && mediaItem.getImageUrl() != null && !TextUtils.isEmpty(mediaItem.getImageUrl())) {
			RequestBuilder rb = Picasso.with(mContext).load(mediaItem.getImageUrl());
			rb.fetch(myTarget);
			rb.placeholder(R.drawable.background_home_tile_album_default).into(tileImage);
		}
		
		return convertView;
	}
	
	
	
	private void showMediaItemOptionsDialog(final MediaItem mediaItem, final int position) {
		//set up custom dialog
		mediaItemOptionsDialog = new Dialog(mActivity);
		mediaItemOptionsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mediaItemOptionsDialog.setContentView(R.layout.dialog_media_playing_options);
		mediaItemOptionsDialog.setCancelable(true);
        mediaItemOptionsDialog.show();
        
		// sets the title.
        TextView title = (TextView) mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_title_text);
        title.setText(mediaItem.getTitle());
        
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
        
        if (mIsShowDetailsInOptionsDialogEnabled) {
        	llDetails.setVisibility(View.VISIBLE);
        } else {
        	llDetails.setVisibility(View.GONE);
        }

        // play now.
        llPlayNow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mOnMediaItemOptionSelectedListener != null){
					mOnMediaItemOptionSelectedListener.onMediaItemOptionPlayNowSelected(mediaItem, position);
				}
				mediaItemOptionsDialog.dismiss();
			}
		});        
                
        // add to queue.
        llAddtoQueue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mOnMediaItemOptionSelectedListener != null){
					mOnMediaItemOptionSelectedListener.onMediaItemOptionAddToQueueSelected(mediaItem, position);
				}
				mediaItemOptionsDialog.dismiss();			
			}
		});
        
        // show details.
        llDetails.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				if (mOnMediaItemOptionSelectedListener != null){
					mOnMediaItemOptionSelectedListener.onMediaItemOptionShowDetailsSelected(mediaItem, position);
				}
				mediaItemOptionsDialog.dismiss();
			}
		});
	}

	
	// ======================================================
	// PUBLIC METHODS.
	// ======================================================
	
	/**
	 * Register a callback to be invoked when an action was selected to be performed on the tile.
	 * @param listener to be invoked.
	 */
	public void setOnMusicItemOptionSelectedListener(OnMediaItemOptionSelectedListener listener) {
		mOnMediaItemOptionSelectedListener = listener;
	}
	
	/**
	 * Sets the media items to be presented as tiles in the grid.</br>
	 * For updating the change, call {@code BaseAdapter.notifyDataSetChanged()}/
	 * @param mediaItems to be presented.
	 */
	public void setMediaItems(List<MediaItem> mediaItems) {
		if (mediaItems != null) {
			mMediaItems = mediaItems;
		} else {
			mMediaItems = new ArrayList<MediaItem>();
		}
	}
	
	
	// ======================================================
	// Images Loading control methods.
	// ======================================================
	
	public void resumeLoadingImages() {
		
		//mImageFetcher.setExitTasksEarly(false);
		//notifyDataSetChanged();
	}

	public void stopLoadingImages() {
		
//        mImageFetcher.setPauseWork(false);
//        mImageFetcher.setExitTasksEarly(true);
//        mImageFetcher.flushCache();
		
//		mImageFetcher.setExitTasksEarly(true);
//		mImageFetcher.flushCache();
	}
	
	public void releaseLoadingImages() {
		//mImageFetcher.closeCache();
	}
	
	
	/**
	 * Sets if the Grid's item list can be edited or not. 
	 * @param isEditModeEnabled
	 */
	public void setEditModeEnabled(boolean isEditModeEnabled) {
		mIsEditModeEnabled = isEditModeEnabled;
	}
	
	/**
	 * Retrieves if the Grid's item list can be edited or not. 
	 * @return
	 */
	public boolean isEditModeEnabled() {
		return mIsEditModeEnabled;
	}

	/**
	 * Sets if the Grid's item can suggest showing its details. 
	 * @param isShowDetailsEnabled
	 */
	public void setShowDetailsInOptionsDialogEnabled(boolean isShowDetailsEnabled) {
		mIsShowDetailsInOptionsDialogEnabled = isShowDetailsEnabled;
	}
	
	/**
	 * Retrieves if the Grid's item can suggest showing its details. 
	 */
	public boolean isShowDetailsInOptionsDialogEnabled() {
		return mIsShowDetailsInOptionsDialogEnabled;
	}
	
	/**
	 * Sets if clicking on removing an item only will invoke the 
	 * {@code OnMediaItemOptionSelectedListener.onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position)} method,
	 * if setting to true, the callback will be invoked and you will have to remove the item yourself and update the Adapter.
	 */
	public void setOnlyCallbackWhenRemovingItem(boolean onlyCallbackWhenRemovingItem) {
		mOnlyCallbackWhenRemovingItem = onlyCallbackWhenRemovingItem;
	}
	
	public void setShowOptionsDialog(boolean showOptionsDialog) {
		mShowOptionsDialog = showOptionsDialog;
	}
	
	private void showHomeTileHint() {
		
		Animation animationIn = AnimationUtils.loadAnimation(mContext, R.anim.slide_and_show_bottom_enter);
		final Animation animationOut = AnimationUtils.loadAnimation(mContext, R.anim.slide_and_show_bottom_exit);
		
		homeTileHint.setVisibility(View.VISIBLE);
		homeTileHint.startAnimation(animationIn);
		
		final CountDownTimer countDownTimer = new CountDownTimer(7000, 1000) {

		     public void onTick(long millisUntilFinished) {
		    	 
		     }

		     public void onFinish() {
		    	 cancel();
		    	 homeTileHint.startAnimation(animationOut);
		    	 homeTileHint.setVisibility(View.GONE);
		     }
		  }.start();
		
		  homeTileHint.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					homeTileHint.startAnimation(animationOut);
					countDownTimer.cancel();
					homeTileHint.setVisibility(View.GONE);
					
				}
			});
	}
	
}
