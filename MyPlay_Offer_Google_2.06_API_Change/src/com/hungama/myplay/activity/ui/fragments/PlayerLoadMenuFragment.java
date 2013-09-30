package com.hungama.myplay.activity.ui.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaContentOperation;
import com.hungama.myplay.activity.ui.listeners.OnLoadMenuItemOptionSelectedListener;

/**
 * Presents the "Settings" expandable list in the ActionBar
 */
public class PlayerLoadMenuFragment extends MainFragment implements OnClickListener, CommunicationOperationListener{
	
	public static final String TAG = "PlayerLoadMenuFragment";
	
	private Context mContext;
	private Resources mResources;
	private LayoutInflater mLayoutInflater;
	private DataManager mDataManager;
	
	private View rootView;
	private List<Track> mTracksTop10 = null;
	
	private OnMainSettingsMenuItemSelectedListener mOnMainSettingsMenuItemSelectedListener;

	private OnLoadMenuItemOptionSelectedListener mOnLoadMenuItemOptionSelectedListener;

	
	// ======================================================
	// FRAGMENTS LIFECYCLE AND PRIVATE HELPER METHODS.
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = getActivity().getApplicationContext();
		mResources = mContext.getResources();
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mDataManager = DataManager.getInstance(mContext);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_player_load_menu, container, false);
		initializeComponents();
		return rootView;
	}
	
	
	// ======================================================
	// PRIVATE HELPER CLASSES.
	// ======================================================
	
	private void initializeComponents() {
		
		Button buttonTop10 = (Button) rootView.findViewById(R.id.load_top_ten);
		Button buttonRadio = (Button) rootView.findViewById(R.id.load_radio);
		Button buttonMyPlaylist = (Button) rootView.findViewById(R.id.load_my_playlist);
		Button buttonMyFavorites = (Button) rootView.findViewById(R.id.load_my_favorites);
		
		buttonTop10.setOnClickListener(this);
		buttonRadio.setOnClickListener(this);
		buttonMyPlaylist.setOnClickListener(this);
		buttonMyFavorites.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		
		switch (viewId) {
		case R.id.load_top_ten:
			
			mDataManager.getMediaItems(MediaContentType.MUSIC, MediaCategoryType.FEATURED, null, this);
			break;
			
		case R.id.load_radio:
			
			getFragmentManager().popBackStack();
			
			if (mOnLoadMenuItemOptionSelectedListener != null) {
				mOnLoadMenuItemOptionSelectedListener.onLoadMenuRadioSelected();
			}
			break;

		case R.id.load_my_playlist:
			
			getFragmentManager().popBackStack();
			if (mOnLoadMenuItemOptionSelectedListener != null) {
				mOnLoadMenuItemOptionSelectedListener.onLoadMenuMyPlaylistSelected();
			}
			break;

		case R.id.load_my_favorites:
			if (mOnLoadMenuItemOptionSelectedListener != null) {
				mOnLoadMenuItemOptionSelectedListener.onLoadMenuMyFavoritesSelected();
			}
			break;

		default:
			break;
		}
		
	}
	
	/**
	 * Interface definition to be invoked when the user has selected an item.
	 */
	public interface OnMainSettingsMenuItemSelectedListener {
		
		public void onMainSettingsMenuItemSelected(int menuItemId);
		
	}
	
	public void setOnMainSettingsMenuItemSelectedListener(OnMainSettingsMenuItemSelectedListener listener) {
		mOnMainSettingsMenuItemSelectedListener = listener;
	}
	
	public void setOnLoadMenuItemOptionSelectedListener(OnLoadMenuItemOptionSelectedListener listener) {
		mOnLoadMenuItemOptionSelectedListener = listener;
	}

	
	
	
	// ======================================================
	// Operation Methods
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		switch (operationId) {
			case OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED:
			case OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST:
			case OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED:
				showLoadingDialog(R.string.application_dialog_loading_content);
				break;
		}
	}

	/* (non-Javadoc)
	 * @see com.hungama.myplay.activity.communication.CommunicationOperationListener#onSuccess(int, java.util.Map)
	 */
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		MediaCategoryType mediaCategoryType = (MediaCategoryType) responseObjects.get(MediaContentOperation.RESULT_KEY_OBJECT_MEDIA_CATEGORY_TYPE);
		List<MediaItem> mediaItems = (List<MediaItem>) responseObjects.get(MediaContentOperation.RESULT_KEY_OBJECT_MEDIA_ITEMS);		 
		int trackCounter = 0;
		mTracksTop10 = new ArrayList<Track>();
		for (MediaItem mediaItem : mediaItems) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), mediaItem.getAlbumName(), 
										mediaItem.getArtistName(), mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				mTracksTop10.add(track);
				trackCounter++;
				if (trackCounter == 10) {
					break;
				}
			}
		}
		
		if (mOnLoadMenuItemOptionSelectedListener != null) {
			mOnLoadMenuItemOptionSelectedListener.onLoadMenuTop10Selected(mTracksTop10);
		}
		 getFragmentManager().popBackStack();
		 hideLoadingDialog();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED:
		case OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST:
		case OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED:
			
			hideLoadingDialog();
			
			break;
		}
	}	

	
}
