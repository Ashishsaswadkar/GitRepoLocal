package com.hungama.myplay.activity.ui.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.DataManager.MoodIcon;
import com.hungama.myplay.activity.data.dao.hungama.DiscoverSearchResultIndexer;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.hungama.DiscoverSearchResultsOperation;
import com.hungama.myplay.activity.ui.DiscoveryActivity;
import com.hungama.myplay.activity.ui.DiscoveryActivity.OnFragmentEditModeStateChangedListener;
import com.hungama.myplay.activity.ui.adapters.MediaTilesAdapter;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class DiscoveryGalleryFragment extends SherlockFragment implements OnClickListener, 
									CommunicationOperationListener, OnMediaItemOptionSelectedListener, 
									OnFragmentEditModeStateChangedListener {

	private static final String TAG = "DiscoveryGalleryFragment";
	
	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;
	
	private DataManager mDataManager;
	private FragmentManager mFragmentManager;
	
	private GridView mTilesGridView;
	private int mTileSize = 0;
	private MediaTilesAdapter mMediaTilesAdapter;
	
	private List<MediaItem> mMediaItems = null;
	private DiscoverSearchResultIndexer mDiscoverSearchResultIndexer;
	
	private ImageButton mButtonMoods;
	private ImageButton mButtonCategories;
	private ImageButton mButtonEra;
	private ImageButton mButtonTempo;
	
	
	// ======================================================
	// public.
	// ======================================================
	
	/**
	 * Registers a callback to be invoked when the user has selected an action upon a {@link MediaItem}.
	 * @param listener
	 */
	public void setOnMediaItemOptionSelectedListener(OnMediaItemOptionSelectedListener listener) {
		mOnMediaItemOptionSelectedListener = listener;
	}
	
	public List<Track> getTracks() {
		if (!Utils.isListEmpty(mMediaItems)) {
			List<Track> tracks = new ArrayList<Track>();
			for (MediaItem mediaItem : mMediaItems) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
						mediaItem.getAlbumName(), mediaItem.getArtistName(), 
						mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				tracks.add(track);
			}
			return tracks;
		}
		
		return null;
	}
	
	
	// ======================================================
	// Fragment's life cycle. 
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_discovery_gallery, container, false);
		
		initializeUserConterols(rootView);
		
		setMoodButton();
		
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mFragmentManager = getFragmentManager();
		// registers itself as listener for callbacks when the edit mode states are changed.
		DiscoveryActivity discoveryActivity = (DiscoveryActivity) getActivity();
		discoveryActivity.setOnFragmentEditModeStateChangedListener(this);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		// first time, load media items based on the given search queries.
		if (mMediaItems == null) {
			DiscoveryActivity discoveryActivity = (DiscoveryActivity) getActivity();
			mDataManager.getDiscoverSearchResult(discoveryActivity.getDiscover(), mDiscoverSearchResultIndexer, this);
		}
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
		FlurryAgent.logEvent("Discovery - Results");
	}

	@Override
	public void onStop() {
		super.onStop();
		
		FlurryAgent.onEndSession(getActivity());
	}
	
	@Override
	public void onClick(View view) {

		int viewId = view.getId();
		DiscoveryActivity discoveryActivity = (DiscoveryActivity) getActivity();
		
		if (viewId == R.id.discovery_gallery_button_moods) {
			discoveryActivity.onEditModeMoodSelected();
			
		} else if (viewId == R.id.discovery_gallery_button_categories) {
			discoveryActivity.onEditModeCategorySelected();
			
		} else if (viewId == R.id.discovery_gallery_button_era) {
			discoveryActivity.onEditModeEraSelected();
			
		} else if (viewId == R.id.discovery_gallery_button_tempo) {
			discoveryActivity.onEditModeTempoSelected();
		}
	}
	
	
	// ======================================================
	// Edit mode callbacks.
	// ======================================================
	
	@Override
	public void onStartEditMode(Fragment fragment) {
		// disables edit mode buttons. 
		if (fragment instanceof DiscoveryMoodFragment || 
				fragment instanceof DiscoveryCategoriesFragment) {
			// disables all the buttons.
			mButtonMoods.setOnClickListener(null);
			mButtonCategories.setOnClickListener(null);
			mButtonEra.setOnClickListener(null);
			mButtonTempo.setOnClickListener(null);
			
		} else {
			if (fragment instanceof DiscoveryEraFragment) {
				// disables only the era button.
				mButtonEra.setOnClickListener(null);
				
			} else if (fragment instanceof DiscoveryTempoFragment) {
				// disables only the tempo button.
				mButtonTempo.setOnClickListener(null);
			}
		}
	}

	@Override
	public void onStopEditMode(boolean hasDataChanged) {
		// checks if the where changes in edit mode.
		if (hasDataChanged){
			// updates the mood button.
			setMoodButton();
			// updates the search result by fresh queries.
			DiscoveryActivity discoveryActivity = (DiscoveryActivity) getActivity();
			mDataManager.getDiscoverSearchResult(discoveryActivity.getDiscover(), mDiscoverSearchResultIndexer, this);
		}
		
		// enables edit mode buttons.
		mButtonMoods.setOnClickListener(this);
		mButtonCategories.setOnClickListener(this);
		mButtonEra.setOnClickListener(this);
		mButtonTempo.setOnClickListener(this);
	}
	
	
	// ======================================================
	// Communication operation callbacks.
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		showLoadingDialog();
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		mDiscoverSearchResultIndexer = (DiscoverSearchResultIndexer) 
				 		responseObjects.get(DiscoverSearchResultsOperation.RESULT_KEY_DISCOVER_SEARCH_RESULT_INDEXER);
		mMediaItems = (List<MediaItem>) responseObjects.get(DiscoverSearchResultsOperation.RESULT_KEY_MEDIA_ITEMS);
		
		// updates the grid.
		mMediaTilesAdapter.setMediaItems(mMediaItems);
		mMediaTilesAdapter.notifyDataSetChanged();
		
		if (Utils.isListEmpty(mMediaItems)) {
			Toast.makeText(getActivity(), R.string.discovery_results_error_message_no_results, Toast.LENGTH_SHORT).show();
		}
		
		hideLoadingDialog();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		hideLoadingDialog();
		if (errorType != ErrorType.OPERATION_CANCELLED && isVisible()) {
			Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
		}
	}
	
	
	// ======================================================
	// Media item's options callbacks.
	// ======================================================
	
	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position) {
		if (mOnMediaItemOptionSelectedListener != null) {
			mOnMediaItemOptionSelectedListener.onMediaItemOptionPlayNowSelected(mediaItem, position);
		}
	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem, int position) {
		if (mOnMediaItemOptionSelectedListener != null) {
			mOnMediaItemOptionSelectedListener.onMediaItemOptionPlayNextSelected(mediaItem, position);
		}
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem, int position) {
		if (mOnMediaItemOptionSelectedListener != null) {
			mOnMediaItemOptionSelectedListener.onMediaItemOptionAddToQueueSelected(mediaItem, position);
		}
	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem, int position) {
		if (mOnMediaItemOptionSelectedListener != null) {
			mOnMediaItemOptionSelectedListener.onMediaItemOptionShowDetailsSelected(mediaItem, position);
		}
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position) {
		if (mOnMediaItemOptionSelectedListener != null) {
			mOnMediaItemOptionSelectedListener.onMediaItemOptionRemoveSelected(mediaItem, position);
		}
	}
	
	
	// ======================================================
	// Private helper methods.
	// ======================================================
	
	@SuppressWarnings("deprecation")
	private void initializeUserConterols(View rootView) {
		
		// initializes the buttons.
		mButtonMoods = (ImageButton) rootView.findViewById(R.id.discovery_gallery_button_moods);
		mButtonCategories = (ImageButton) rootView.findViewById(R.id.discovery_gallery_button_categories);
		mButtonEra = (ImageButton) rootView.findViewById(R.id.discovery_gallery_button_era);
		mButtonTempo = (ImageButton) rootView.findViewById(R.id.discovery_gallery_button_tempo);
		
		mButtonMoods.setOnClickListener(this);
		mButtonCategories.setOnClickListener(this);
		mButtonEra.setOnClickListener(this);
		mButtonTempo.setOnClickListener(this);

		// sets initial alpha value to the buttons.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			int alpha_pre_jb = getResources().getInteger(R.dimen.curved_button_alpha_pre_jb);
			
			mButtonMoods.setAlpha(alpha_pre_jb);
			mButtonCategories.setAlpha(alpha_pre_jb);
			mButtonEra.setAlpha(alpha_pre_jb);
			mButtonTempo.setAlpha(alpha_pre_jb);
		} else {
			float alpha = Float.parseFloat(getResources().getString(R.string.curved_button_alpha)); 
			
			mButtonMoods.setAlpha(alpha);
			mButtonCategories.setAlpha(alpha);
			mButtonEra.setAlpha(alpha);
			mButtonTempo.setAlpha(alpha);
		}
		
		mButtonMoods.setOnClickListener(this);
		mButtonCategories.setOnClickListener(this);
		mButtonEra.setOnClickListener(this);
		mButtonTempo.setOnClickListener(this);
		
		// sets the tiles.
		mTilesGridView = (GridView) rootView.findViewById(R.id.discovery_gallery_gridview_tiles);
		/*
		 * For placing the tiles correctly in the grid, 
		 * calculates the maximum size that a tile can be and the column width.
		 */
		int imageTileSpacing = getResources().getDimensionPixelSize(R.dimen.home_tiles_spacing_vertical);
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
        
        mMediaTilesAdapter = new MediaTilesAdapter(getActivity(), mTilesGridView, mTileSize, null, false);
        mMediaTilesAdapter.setOnMusicItemOptionSelectedListener(this);
		mTilesGridView.setAdapter(mMediaTilesAdapter);
		
	}
	
	/**
	 * Sets the mood icon in its button.
	 */
	private void setMoodButton() {
		DiscoveryActivity discoveryActivity = (DiscoveryActivity) getActivity();
		Mood selectedMood = discoveryActivity.getDiscover().getMood();
		if (selectedMood != null) {
			mButtonMoods.setImageDrawable(mDataManager.getMoodIcon(selectedMood, MoodIcon.SMALL));
		} else {
			mButtonMoods.setImageDrawable(getResources().getDrawable(R.drawable.icon_discovery_no_mood));
		}
	}
	
	private void showLoadingDialog() {
		DialogFragment fragmentDialog = (DialogFragment) mFragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
		if (fragmentDialog == null && isVisible()) {
			LoadingDialogFragment fragment = LoadingDialogFragment.newInstance(R.string.application_dialog_loading_content);
			fragment.setCancelable(true);
			fragment.show(mFragmentManager, LoadingDialogFragment.FRAGMENT_TAG);
		}
	}
	
	private void hideLoadingDialog() {
		DialogFragment fragmentDialog = (DialogFragment) mFragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
		if (fragmentDialog != null) {
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.remove(fragmentDialog);
			fragmentDialog.getDialog().dismiss();
		}
	}

}
