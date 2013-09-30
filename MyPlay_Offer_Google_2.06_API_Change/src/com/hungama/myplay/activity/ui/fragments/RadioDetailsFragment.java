package com.hungama.myplay.activity.ui.fragments;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.campaigns.ForYouActivity;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.campaigns.Action;
import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.hungama.LiveStation;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.RadioTopArtistSongsOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment.NextTrackUpdateListener;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

/**
 * Shows details related to the selected Radio station.
 */
public class RadioDetailsFragment extends MainFragment implements NextTrackUpdateListener, OnClickListener, CommunicationOperationListener {
	
	public static final String TAG = "RadioDetailsFragment";
	
	/**
	 * Extra data of the Radio item.
	 */
	public static final String EXTRA_MEDIA_ITEM = "extra_media_item";
	
	/**
	 * Extra data of the given Radio item kind, if it's Live Station or Top Artist Radio.
	 */
	public static final String EXTRA_CATEGORY_TYPE = "extra_category_type";
	
	/**
	 * Flag for indication if the fragment should present a title bar with the name of the station.
	 * Default is false.
	 */
	public static final String EXTRA_DO_SHOW_TITLE_BAR = "extra_do_show_title_bar";
	
	/**
	 * Flag indicates if when this fragment is launched it will also play the given
	 * radio channel, default is false.
	 */
	public static final String EXTRA_AUTO_PLAY = "extra_auto_play";
	
	private DataManager mDataManager;
    private Placement radioPlacement;
//    private ImageFetcher mImageFetcher = null;
	
	private MediaItem mMediaItem = null;
	private MediaCategoryType mMediaCategoryType = null;
	private boolean mDoShowTitleBar = false;
	private boolean mAutoPlay = false;
	
    // general views.
    private TextView mTextTitle;
    
    private RelativeLayout mComingUpLayout;
    private ImageView mComingUpThumbnail;
    private ImageView mRadioPlacementImage;
    private TextView mComingUpAlbumName;
    private TextView mComingUpSongName;
    
    private Context mContext;
    
    
    // ======================================================
	// Life Cycle callbacks.
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();
		Bundle data = getArguments();
		if (data != null) {
			mMediaItem = (MediaItem) data.getSerializable(EXTRA_MEDIA_ITEM);
			mMediaCategoryType = (MediaCategoryType) data.getSerializable(EXTRA_CATEGORY_TYPE);
			mDoShowTitleBar = data.getBoolean(EXTRA_DO_SHOW_TITLE_BAR, false);
			mAutoPlay = data.getBoolean(EXTRA_AUTO_PLAY, false);
		}
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_radio_details, container, false);
		
		initializeUserControls(rootView);
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
		
		/*
		 * Checks if it should play it for the first time launching the fragment.
		 */
		if (mAutoPlay) {
			// disables it to avoid replaying when coming from the background. 
			mAutoPlay = false;
			
			if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
				/*
				 * gets the songs for the artist, when it finishes,
				 * it will start playing them and will show the details for it.
				 */
				mDataManager.getRadioTopArtistSongs(mMediaItem, this);
				
			} else if (mMediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
				playLiveStation(mMediaItem);
			}
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(getActivity());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
//			if (mImageFetcher == null) {
				// creates the image loader.
				
				// creates the cache.
				ImageCache.ImageCacheParams cacheParams =
		                new ImageCache.ImageCacheParams(getActivity(), DataManager.FOLDER_TILES_CACHE);
		        cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);

				// gets the image and its size.
				ImageView thumbNail = (ImageView) getView().findViewById(R.id.radio_details_thumbnail);
				int imageSize = Math.min(thumbNail.getMeasuredHeight(), thumbNail.getMeasuredWidth());
				
//				mImageFetcher = new ImageFetcher(getActivity(), imageSize);
//				mImageFetcher.addImageCache(getFragmentManager(), cacheParams);
//		        mImageFetcher.setImageFadeIn(false);

//		        mImageFetcher.loadImage(mMediaItem.getImageUrl(), thumbNail);
				Picasso.with(mContext).cancelRequest(thumbNail);
				if(mContext != null && mMediaItem != null && !TextUtils.isEmpty(mMediaItem.getImageUrl())){
			        Picasso.with(mContext)
			        		.load(mMediaItem.getImageUrl())
			        		.into(thumbNail);
				}
		        
		        // coming up
		        PlayerBarFragment playerBarFragment = ((MainActivity) getActivity()).getPlayerBar();
				playerBarFragment.unregisterToNextTrackUpdateListener(this);
				
				populateComingUpPanel(playerBarFragment.getNextTrack());
				
//			} else {
//				// refreshes the cache of the image.
//				mImageFetcher.setExitTasksEarly(false);
//			}
		}
		
		/*
		 * Loads the image for the campaigns.
		 */
//		ImageCache.ImageCacheParams cacheParams =
//                new ImageCache.ImageCacheParams(getActivity(), DataManager.FOLDER_CAMPAIGNS_CACHE);
//        cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);
//
//		mImageFetcher = new ImageFetcher(getActivity(), 0);
//		mImageFetcher.addImageCache(getFragmentManager(), cacheParams);
//		mImageFetcher.setLoadingImage(R.drawable.background_home_tile_album_default);
//        mImageFetcher.setImageFadeIn(false);
        
        // Get the Radio Placement List and generate a random number in order to show one 
        // of the Placements.
        List<Placement> radioPlacements = mDataManager.getStoredRadioPlacement();
        
        if(radioPlacements != null && !radioPlacements.isEmpty()){
        	
        	Random myRandom = new Random();
        	int listSize = radioPlacements.size();
        	      	
        	int randomRadioPlacement = (Math.abs(myRandom.nextInt()) % (listSize));
        	
        	radioPlacement = radioPlacements.get(randomRadioPlacement);
        	
//        	mImageFetcher.loadImage(radioPlacement.getBgImageSmall(), mRadioPlacementImage); 
        	Picasso.with(mContext).cancelRequest(mRadioPlacementImage);
        	if(mContext != null && radioPlacement != null && !TextUtils.isEmpty(radioPlacement.getBgImageSmall())){
	        	Picasso.with(mContext)
	        			.load(radioPlacement.getBgImageSmall())
	        			.placeholder(R.drawable.background_home_tile_album_default)
	        			.into(mRadioPlacementImage);
        	}
        }else{
        	mRadioPlacementImage.setClickable(false);
        }
	}
	
	@Override
	public void onPause() {
		
		if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
			PlayerBarFragment playerBarFragment = ((MainActivity) getActivity()).getPlayerBar();
			playerBarFragment.unregisterToNextTrackUpdateListener(this);
		}
		
//		if (mImageFetcher != null) {
//			mImageFetcher.setExitTasksEarly(true);
//	        mImageFetcher.flushCache();
//		}
		
		super.onPause();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
//		if (mImageFetcher != null) {
//			mImageFetcher.closeCache();
//			mImageFetcher = null;
//		}
			
	}
	
	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.radio_placement_image:
			List<Action> actions = radioPlacement.getActions();
			
			if(actions != null && !actions.isEmpty()){
				
				Action action = actions.get(0);
				
				if(action != null){
					
					URI uri = URI.create(action.action_uri);
					String authotiry = uri.getAuthority();
					
					if(authotiry.equalsIgnoreCase(ForYouActivity.ACTION_TYPE_CAMPAIGN)){
						
						List<Campaign> campaigns = mDataManager.getStoredCampaign();
						Node node = CampaignsManager.findCampaignRootNodeByID(campaigns, radioPlacement.getCampaignID());
						
						// Build an Intent to start ForYouActivity
						Intent intent = 
								mDataManager.cretaeForYouActivityIntent(
										getActivity().getApplicationContext(),
										node);
						
						startActivity(intent);
					}
				}
			}
			
			break;

		default:
			break;
		}
		
	}
	
	
	// ======================================================
	// Communication Callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS) {
			showLoadingDialog(R.string.application_dialog_loading_content);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS) {
			// gets the radio tracks
			List<Track> radioTracks = (List<Track>) responseObjects.get(RadioTopArtistSongsOperation.RESULT_KEY_OBJECT_TRACKS);
			MediaItem mediaItem = (MediaItem) responseObjects.get(RadioTopArtistSongsOperation.RESULT_KEY_OBJECT_MEDIA_ITEM);
			
			/*
			 * sets to each track a reference to a copy of the original radio item.
			 * This to make sure that the player bar can get source Radio item without
			 * leaking this activity!
			 */
			for (Track track : radioTracks) {
				track.setTag(mediaItem);
			}
			
			hideLoadingDialog();

			// starts to play.
			PlayerBarFragment playerBar = ((MainActivity) getActivity()).getPlayerBar();
			playerBar.playRadio(radioTracks, PlayMode.TOP_ARTISTS_RADIO);
			
			// updates the coming up section.
			populateComingUpPanel(playerBar.getNextTrack());
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		
		hideLoadingDialog();
		
		if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
		
			Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();		
		}
		
		this.getFragmentManager().popBackStack();
	}

	
	// ======================================================
	// Helper methods.
	// ======================================================
	
	private void initializeUserControls(View rootView) {
		
		if (mMediaItem != null && mMediaCategoryType != null) {
			
			// sets the title bar.
			RelativeLayout titleBar = (RelativeLayout) rootView.findViewById(R.id.radio_details_title_bar);
			if (mDoShowTitleBar) {
				titleBar.setVisibility(View.VISIBLE);
				
				// sets the title bar's text.
				mTextTitle = (TextView) rootView.findViewById(R.id.radio_details_title_bar_text);
				
				if (mMediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
					mTextTitle.setText(mMediaItem.getTitle());
					
				} else if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
					mTextTitle.setText(R.string.radio_top_artist_radio);
				}
			}
			
			// sets the content.
			if (mMediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
				
				RelativeLayout container = (RelativeLayout) rootView
							.findViewById(R.id.radio_details_layout_live_station);
				
				container.setVisibility(View.VISIBLE);
				
				// shows the details of the Live Station.
				
				TextView description = (TextView) rootView
						.findViewById(R.id.radio_details_live_station_text_radio_name);
				
				LiveStation liveStation = (LiveStation) mMediaItem;
				description.setText(liveStation.getDescription());
				
				FlurryAgent.logEvent("Live Radio details");
				
			} else if (mMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
				
				RelativeLayout container = (RelativeLayout) rootView
							.findViewById(R.id.radio_details_layout_top_artists_radio);
				
				container.setVisibility(View.VISIBLE);
				
				// shows the details of the Artist.
				
				TextView textArtistName = (TextView) rootView
								.findViewById(R.id.radio_details_top_artists_text_radio_name);
				
				textArtistName.setText(mMediaItem.getTitle());
				
				// coming up layout.
				mComingUpLayout = (RelativeLayout) rootView.findViewById(R.id.radio_details_layout_coming_up);
			    mComingUpThumbnail = (ImageView) rootView.findViewById(R.id.radio_details_coming_up_thumbnail);
			    mComingUpAlbumName = (TextView) rootView.findViewById(R.id.radio_details_coming_up_album_name);
			    mComingUpSongName = (TextView) rootView.findViewById(R.id.radio_details_coming_up_song_name);
			    
			    FlurryAgent.logEvent("Top Artists Radio details");
			}
		}
		
		mRadioPlacementImage = (ImageView) rootView.findViewById(R.id.radio_placement_image);
		mRadioPlacementImage.setOnClickListener(this);
	}
	
	private void populateComingUpPanel(Track track) {
		if (track != null) {
			if (mComingUpLayout.getVisibility() != View.VISIBLE) {
				mComingUpLayout.setVisibility(View.VISIBLE);
			}
			
			String songPrefix = getResources().getString(R.string.radio_details_coming_up_song);
			songPrefix = songPrefix + track.getAlbumName();
			
			mComingUpSongName.setText(track.getTitle());
			mComingUpAlbumName.setText(songPrefix);
			
//			mImageFetcher.loadImage(track.getImageUrl(), mComingUpThumbnail);
			Picasso.with(mContext).cancelRequest(mComingUpThumbnail);
			if(mContext != null && track != null && !TextUtils.isEmpty(track.getImageUrl())){
				Picasso.with(mContext)
	    				.load(track.getImageUrl())
	    				.into(mComingUpThumbnail);
			}
				
		} else {
			mComingUpLayout.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onNextTrackUpdateListener(Track track) {
		populateComingUpPanel(track);
	}
	
	private void playLiveStation(MediaItem mediaItem) {
		
		LiveStation liveStation = (LiveStation) mediaItem;
		
		Track liveStationTrack = new Track(liveStation.getId(), liveStation.getTitle(), liveStation.getDescription(), 
										   null, liveStation.getImageUrl(), liveStation.getImageUrl());
		liveStationTrack.setMediaHandle(liveStation.getStreamingUrl());
		
		List<Track> liveStationList = new ArrayList<Track>();
		liveStationList.add(liveStationTrack);
		
		/*
		 * sets to each track a reference to a copy of the original radio item.
		 * This to make sure that the player bar can get source Radio item without
		 * leaking this activity!
		 */
		for (Track track : liveStationList) {
			track.setTag(liveStation);
		}
		
		// starts to play.
		((MainActivity) getActivity()).getPlayerBar().playRadio(liveStationList, PlayMode.LIVE_STATION_RADIO);
	}
	

}
