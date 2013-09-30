package com.hungama.myplay.activity.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.CollectionItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.MyCollectionResponse;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialMyCollectionOperation;
import com.hungama.myplay.activity.ui.fragments.MediaTileGridFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

public class MyCollectionActivity extends MainActivity implements CommunicationOperationListener, 
																OnMediaItemOptionSelectedListener{

	private static final String TAG = "MyCollectionActivity";
	
	private Context mContext;
	private DataManager mDataManager;
	
	private PlayerBarFragment mPlayerBar;
	
	private TextView mTitleBarText;
	
	private MediaTileGridFragment mMediaTileGridFragment;
	
	
	// ======================================================
	// ACTIVITY'S LIFECYCLE.
	// ======================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favorites);
		
		mDataManager = DataManager.getInstance(mContext);						
		
		mDataManager.getMyCollection(this);
		
		mPlayerBar = getPlayerBar();
		
		// SetS title bar
		mTitleBarText = (TextView) findViewById(R.id.main_title_bar_text);
		mTitleBarText.setText(getResources().getString(R.string.my_collection_title, " "));
		
	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}
	
	// ======================================================
	// Helper Methods.
	// ======================================================
	
	public void addFragment(Bundle detailsData) {
		
		mMediaTileGridFragment = new MediaTileGridFragment();
		mMediaTileGridFragment.setArguments(detailsData);
		mMediaTileGridFragment.setOnMediaItemOptionSelectedListener(this);
		
		// For avoiding perform an action after onSaveInstanceState.
		new Handler().post(new Runnable() {
			
            public void run() {
            	
            	FragmentManager mFragmentManager = getSupportFragmentManager();
        		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter, R.anim.slide_left_exit, R.anim.slide_right_enter,R.anim.slide_right_exit);
        		fragmentTransaction.add(R.id.main_fragmant_container, mMediaTileGridFragment);
        		fragmentTransaction.commitAllowingStateLoss();
//        		fragmentTransaction.commit();
            }
        });
	}	
	
	// ======================================================
	// Communication Operations events.
	// ======================================================
		
	@Override
	public void onStart(int operationId) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_MY_COLLECTION:
				showLoadingDialog(R.string.application_dialog_loading_content);
			break;

		default:
			break;
		}
		
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		Logger.i(TAG, "Success Loading My Collection ");
		
		MyCollectionResponse mMyCollectionResponse = (MyCollectionResponse) responseObjects.get(SocialMyCollectionOperation.RESULT_KEY_MY_COLLECTION);
		
		List<CollectionItem> mediaItems = new ArrayList<CollectionItem>();
		mediaItems = mMyCollectionResponse.getMyData();
		
		Bundle data = new Bundle();
		int listSize;
		if (mediaItems != null) {
			for (CollectionItem mediaItem : mediaItems) {
				MediaType mMediaType = mediaItem.getMediaType();
				if (mMediaType == MediaType.ALBUM || mMediaType == MediaType.PLAYLIST || 
						mMediaType == MediaType.TRACK || mMediaType == MediaType.ARTIST) {
					
					mediaItem.setMediaContentType(MediaContentType.MUSIC);
				} else {
					mediaItems.remove(mediaItem);
//					mediaItem.setMediaContentType(MediaContentType.VIDEO);
				}
			}
			
			listSize = mediaItems.size();
			data.putSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS, (Serializable) mediaItems);
		} else {
			listSize = 0;
		}
		
//		setActivityLayout(listSize);
		mTitleBarText.setText(getResources().getString(R.string.my_collection_title, listSize));
		addFragment(data);
			
		hideLoadingDialog();
		
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_MY_COLLECTION:
			hideLoadingDialog();
			break;

		default:
			break;
		}
		
	}

	
	// ======================================================
	// ACTIVITY'S EVENT LISTENERS - HOME.
	// ======================================================
	
	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Play Now: " + mediaItem.getId());
		mPlayerBar = getPlayerBar();
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
		mPlayerBar = getPlayerBar();
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
		mPlayerBar = getPlayerBar();
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
