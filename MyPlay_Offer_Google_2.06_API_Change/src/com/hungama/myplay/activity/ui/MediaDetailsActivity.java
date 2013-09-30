package com.hungama.myplay.activity.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.ui.fragments.MediaDetailsFragment;
import com.hungama.myplay.activity.ui.fragments.MediaTileGridFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;

/**
 * Controller for presenting details of the given MediaItem.
 */
public class MediaDetailsActivity extends MainActivity implements OnMediaItemOptionSelectedListener{
	
	private static final String TAG = "MediaDetailsActivity";
	
	public static final String EXTRA_MEDIA_ITEM = "EXTRA_MEDIA_ITEM";
	
	private FragmentManager mFragmentManager;
	private PlayerBarFragment mPlayerBarFragment;
	private MediaItem mMediaItem;
	
	private TextView mTitleBarText;
	private ImageButton mButtonOptions;	
	
	private Dialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// validate calling intent.
		Intent intent = getIntent();
		if (intent == null) {
			Logger.e(TAG, "No intent for the given Activity.");
			return; 
		}
		
		Bundle data = intent.getExtras();
		if (data != null && data.containsKey(EXTRA_MEDIA_ITEM)) {
				setContentView(R.layout.activity_main_with_title);
				// retrieves the given Media item for the activity.
				mMediaItem = (MediaItem) data.getSerializable(EXTRA_MEDIA_ITEM);
		} else {
			Logger.e(TAG, "No MediaItem set for the given Activity.");
			return; 
		}
		
		if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			// SetS title bar
			mTitleBarText = (TextView) findViewById(R.id.main_title_bar_text);
			mTitleBarText.setText(mMediaItem.getTitle());
			
			mButtonOptions = (ImageButton) findViewById(R.id.main_title_bar_button_options);
			mButtonOptions.setVisibility(View.GONE);
		}
		
		mFragmentManager = getSupportFragmentManager();
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		Bundle detailsData = new Bundle();
		detailsData.putSerializable(MediaDetailsFragment.ARGUMENT_MEDIAITEM, (Serializable) mMediaItem);
		
		MediaDetailsFragment mediaDetailsFragment = new MediaDetailsFragment();
		mediaDetailsFragment.setArguments(detailsData);
		
		fragmentTransaction.add(R.id.main_fragmant_container, mediaDetailsFragment);
		fragmentTransaction.commit();
	}

	@Override
	public void onBackPressed() {
		mTitleBarText.setText(mMediaItem.getTitle());
		super.onBackPressed();
	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}
	
	public void openTrackPage (MediaItem trackMediaItem) {
		
		mTitleBarText.setText(trackMediaItem.getTitle());
		
		Bundle detailsDataTrack = new Bundle();		
		detailsDataTrack.putSerializable(MediaDetailsFragment.ARGUMENT_MEDIAITEM, (Serializable) trackMediaItem);
		
		MediaDetailsFragment mediaDetailsFragmentTrack = new MediaDetailsFragment();
		mediaDetailsFragmentTrack.setArguments(detailsDataTrack);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);			
		
		fragmentTransaction.replace(R.id.main_fragmant_container, mediaDetailsFragmentTrack);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	/**
	 * Method to open Related Videos from ALBUM or PLAYLIST
	 * @param mediaSetDetails
	 */
	public void openVideoPage (MediaSetDetails mediaSetDetails) {
		
		mTitleBarText.setText(mMediaItem.getTitle());
		
		Bundle detailsDataVideos = new Bundle();	
		int listSize = mediaSetDetails.getVideos().size();
		for (int i=0; i < listSize; i++) {
			mediaSetDetails.getVideos().get(i).setMediaContentType(MediaContentType.VIDEO);
			mediaSetDetails.getVideos().get(i).setMediaType(MediaType.VIDEO);
		}
		detailsDataVideos.putSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS, (Serializable) mediaSetDetails.getVideos());		
		
		MediaTileGridFragment mediaTileGridFragment = new MediaTileGridFragment();
		mediaTileGridFragment.setOnMediaItemOptionSelectedListener(this);
		mediaTileGridFragment.setArguments(detailsDataVideos);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);			
		
		fragmentTransaction.replace(R.id.main_fragmant_container, mediaTileGridFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	/**
	 * Method to open Related Videos from TRACK
	 * @param mediaItemsVideos
	 */
	public void openVideoPageTrack (List<MediaItem> mediaItemsVideos) {
		
		mTitleBarText.setText(mMediaItem.getTitle());
		
		Bundle detailsDataVideos = new Bundle();	
		int listSize = mediaItemsVideos.size();
		for (int i=0; i < listSize; i++) {
			mediaItemsVideos.get(i).setMediaContentType(MediaContentType.VIDEO);
			mediaItemsVideos.get(i).setMediaType(MediaType.VIDEO);
		}
		detailsDataVideos.putSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS, (Serializable) mediaItemsVideos);
		
		MediaTileGridFragment mediaTileGridFragment = new MediaTileGridFragment();
		mediaTileGridFragment.setOnMediaItemOptionSelectedListener(this);
		mediaTileGridFragment.setArguments(detailsDataVideos);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);			
		
		fragmentTransaction.replace(R.id.main_fragmant_container, mediaTileGridFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	public void openCommentsPage(MediaItem mediaItem, int numOfComments) {
		
		mTitleBarText.setText(getResources().getString(R.string.comments_title));
		
		Bundle detailsDataTrack = new Bundle();
		detailsDataTrack.putSerializable(CommentsActivity.EXTRA_DATA_MEDIA_ITEM, (Serializable) mediaItem);
		detailsDataTrack.putBoolean(CommentsActivity.EXTRA_DATA_DO_SHOW_TITLE, false);
		
		Intent commentsIntent = new Intent(getApplicationContext(), CommentsActivity.class);
		commentsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		commentsIntent.putExtras(detailsDataTrack);
		startActivity(commentsIntent);
	}	
	
	public void showLongClickDialog(final Track track) {
		//set up custom dialog
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_media_playing_options);
        
        TextView title = (TextView) dialog.findViewById(R.id.long_click_custom_dialog_title_text);
        title.setText(track.getTitle());
        
        ImageButton closeButton = (ImageButton) dialog.findViewById(R.id.long_click_custom_dialog_title_image);
        closeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();				
			}
		});
        
        dialog.setCancelable(true);
        dialog.show();
        
        LinearLayout llPlayNow = (LinearLayout) dialog.findViewById(R.id.long_click_custom_dialog_play_now_row);
        LinearLayout llAddtoQueue = (LinearLayout) dialog.findViewById(R.id.long_click_custom_dialog_add_to_queue_row);
        LinearLayout llDetails = (LinearLayout) dialog.findViewById(R.id.long_click_custom_dialog_details_row);
        
        llPlayNow.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				playButtonClickActivity(tracks);
				dialog.dismiss();
			}
		});                    
        
        llAddtoQueue.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				addToQueueButtonClickActivity(tracks);
				dialog.dismiss();			
			}
		});
        
        llDetails.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MediaItem trackMediaItem  = 
						new MediaItem(track.getId(), track.getTitle(), 
								null, null, null, null, MediaType.TRACK.toString(), 0);
				trackMediaItem.setMediaContentType(MediaContentType.MUSIC);
				trackMediaItem.setMediaType(MediaType.TRACK);
				
				openTrackPage(trackMediaItem);
				dialog.dismiss();
			}
		});
	}
	
	// Left panel buttons clicks
	public void playButtonClickActivity(List<Track> trackList) {
		mPlayerBarFragment = (PlayerBarFragment) mFragmentManager.findFragmentById(R.id.main_fragmant_player_bar);
		mPlayerBarFragment.playNow(trackList);
	}
	
	public void playNextButtonClickActivity(List<Track> trackList) {
		mPlayerBarFragment = (PlayerBarFragment) mFragmentManager.findFragmentById(R.id.main_fragmant_player_bar);
		mPlayerBarFragment.playNext(trackList);
	}
	
	public void addToQueueButtonClickActivity(List<Track> trackList) {
		mPlayerBarFragment = (PlayerBarFragment) mFragmentManager.findFragmentById(R.id.main_fragmant_player_bar);
		mPlayerBarFragment.addToQueue(trackList);
	}
	
	public void addToPlaylistButtonClickActivity(List<Track> trackList) {
		mPlayerBarFragment = (PlayerBarFragment) mFragmentManager.findFragmentById(R.id.main_fragmant_player_bar);
		// TODO:  implement this.
	}

	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position) { }

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem, int position) { }

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem, int position) { }

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem, int position) {
		Intent intent = new Intent(this, VideoActivity.class);
		intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO, (Serializable) mediaItem);
		startActivity(intent);
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position) { }

	
}
