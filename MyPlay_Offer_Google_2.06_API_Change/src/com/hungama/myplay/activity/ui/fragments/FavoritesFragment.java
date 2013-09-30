package com.hungama.myplay.activity.ui.fragments;

import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileFavoriteMediaItems;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialProfileFavoriteMediaItemsOperation;

/**
 * Loads and shows the given the User's favorites MediaItems.
 */
public class FavoritesFragment extends MediaTileGridFragment implements CommunicationOperationListener {
	
	private static final String TAG = "FavoritesFragment";
	
	public static final String FRAGMENT_ARGUMENT_MEDIA_TYPE = "fragment_argument_media_type";
	public static final String FRAGMENT_ARGUMENT_USER_ID = "fragment_argument_user_id";
	
	public interface OnMediaItemsLoadedListener {
		
		public void onMediaItemsLoaded(MediaType mediaType, String userId, List<MediaItem> mediaItems);
	}
	
	public void setOnMediaItemsLoadedListener(OnMediaItemsLoadedListener listener) {
		mOnMediaItemsLoadedListener = listener;
	}
	
	private DataManager mDataManager;
	private List<MediaItem> mMediaItems = null;
	
	private OnMediaItemsLoadedListener mOnMediaItemsLoadedListener;

	private String mUserId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		MediaType mediaType = null;
		String userId = null;
		
		if (mMediaItems == null) {
			Bundle arguments = getArguments();
			if (arguments != null && arguments.containsKey(FRAGMENT_ARGUMENT_MEDIA_TYPE)) {
				mediaType = (MediaType) arguments.getSerializable(FRAGMENT_ARGUMENT_MEDIA_TYPE);
				
				if (mediaType == MediaType.ARTIST) {
					throw new IllegalArgumentException("MediaType.ARTIST is not supported in: " + TAG);
				}
				
				if(arguments.containsKey(FRAGMENT_ARGUMENT_USER_ID)){
					userId = arguments.getString(FRAGMENT_ARGUMENT_USER_ID);
					mUserId = userId;
					mDataManager.getFavorites(mediaType, userId, this);
				}
				
//				mDataManager.getFavorites(mediaType, this);
			} else {
				throw new IllegalArgumentException(TAG + ": Fragment must contain a madia type in arguments.");
			}
		}
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
		
		boolean isMe;
		if(mDataManager.getApplicationConfigurations().getPartnerUserId().equalsIgnoreCase(userId)){
			isMe = true;
		}else{
			isMe = false;
		}
		
		if(mediaType == MediaType.TRACK){
			
			if(isMe){
				FlurryAgent.logEvent("My Fav Songs");
			}else{
				FlurryAgent.logEvent("Others Fav Songs");
			}
			
		}else if(mediaType == MediaType.ALBUM){
			
			if(isMe){
				FlurryAgent.logEvent("My Fav Albums");
			}else{
				FlurryAgent.logEvent("Others Fav Albums");
			}
			
		}else if(mediaType == MediaType.PLAYLIST){
			
			if(isMe){
				FlurryAgent.logEvent("My Fav Playlists");
			}else{
				FlurryAgent.logEvent("Others Fav Playlists");
			}
			
		}else if(mediaType == MediaType.VIDEO){
			
			if(isMe){
				FlurryAgent.logEvent("My Fav Videos");
			}else{
				FlurryAgent.logEvent("Others Fav Videos");
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onStop()
	 */
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FlurryAgent.onEndSession(getActivity());
	}
	
	
	// ======================================================
	// Communication Operations callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
				showLoadingDialog(R.string.application_dialog_loading_content);
			break;
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
				// gets the media items and populate the adapter.
				ProfileFavoriteMediaItems profileFavoriteMediaItems = (ProfileFavoriteMediaItems) responseObjects
											.get(SocialProfileFavoriteMediaItemsOperation.RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS);
				mMediaItems = profileFavoriteMediaItems.mediaItems;
				// updates the tile's grid.
				setMediaItems(mMediaItems);
				// hide the loading.
				hideLoadingDialog();
				
				MediaType mediType = (MediaType) responseObjects
											.get(SocialProfileFavoriteMediaItemsOperation.RESULT_KEY_MEDIA_TYPE);
				
				if (mOnMediaItemsLoadedListener != null) {
					mOnMediaItemsLoadedListener.onMediaItemsLoaded(mediType, mUserId, mMediaItems);
				}
				
			break;
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
			hideLoadingDialog();
			break;
		}
	}


}
