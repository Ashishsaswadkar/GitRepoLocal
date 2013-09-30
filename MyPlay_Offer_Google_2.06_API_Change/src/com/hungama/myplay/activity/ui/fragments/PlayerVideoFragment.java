package com.hungama.myplay.activity.ui.fragments;

import java.util.List;
import java.util.Map;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.RelatedVideoOperation;
import com.hungama.myplay.activity.operations.hungama.TrackSimilarOperation;
import com.hungama.myplay.activity.ui.adapters.MediaTilesAdapter;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * Shows related videos to the given playing {@link Track}.
 */
public class PlayerVideoFragment extends Fragment implements CommunicationOperationListener {
	
	private static final String TAG = "PlayerVideoFragment";
	
public static final String FRAGMENT_ARGUMENT_TRACK_DETAILS = "fragment_argument_track_details";
	
	private DataManager mDataManager;
	private List<MediaItem> mMediaItems = null;
	
	private MediaTrackDetails mMediaTrackDetails;
	
	private GridView mTilesGridView;
	private int mTileSize = 0;
	
	private MediaTilesAdapter mHomeMediaTilesAdapter;
	
	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;
	
	
	// ======================================================
	// PABLIC.
	// ======================================================
	
	/**
	 * Registers a callback to be invoked when the user has selected an action upon a {@link MediaItem}.
	 * @param listener
	 */
	public void setOnMediaItemOptionSelectedListener(OnMediaItemOptionSelectedListener listener) {
		mOnMediaItemOptionSelectedListener = listener;
	}
	
	
	// ======================================================
	// FRAGMENT'S LIFE CYCLE. 
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		
		// gets the track to load.
		Bundle data = getArguments();
		if (data != null && data.containsKey(FRAGMENT_ARGUMENT_TRACK_DETAILS)) {
			mMediaTrackDetails = (MediaTrackDetails) data.getSerializable(FRAGMENT_ARGUMENT_TRACK_DETAILS);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
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
		mTilesGridView.setBackgroundColor(getResources().getColor(R.color.application_background_grey));
		
		// sets the gridview's cool margin.
		GridView.MarginLayoutParams params = 
				new GridView.MarginLayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
		mTilesGridView.setLayoutParams(params);
		// doubling the top + bottom edges with padding to make the tiles fits well inside. 
		mTilesGridView.setPadding(imageTileSpacing, imageTileSpacing, imageTileSpacing, imageTileSpacing);
		
		/*
		 * For placing the tiles correctly in the grid, 
		 * calculates the maximum size that a tile can be and the column width.
		 */
		
		// measuring the device's screen width. and setting the grid column width.
        Display display = getActivity().getWindowManager().getDefaultDisplay();
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
        
        mHomeMediaTilesAdapter = new MediaTilesAdapter(getActivity(), mTilesGridView, mTileSize, null,false);
        mHomeMediaTilesAdapter.setEditModeEnabled(false);
        mHomeMediaTilesAdapter.setShowDetailsInOptionsDialogEnabled(false);
        mHomeMediaTilesAdapter.setOnMusicItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
		mTilesGridView.setAdapter(mHomeMediaTilesAdapter);
		
		return mTilesGridView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (Utils.isListEmpty(mMediaItems)) {
			
			MediaItem mediaItem = new MediaItem(mMediaTrackDetails.getId(), mMediaTrackDetails.getTitle(), mMediaTrackDetails.getAlbumName(),
								mMediaTrackDetails.getAlbumName(), null, null, MediaType.TRACK.toString().toLowerCase(), 0);
			mediaItem.setMediaContentType(MediaContentType.VIDEO);
			mDataManager.getRelatedVideo(mMediaTrackDetails, mediaItem, this);
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		// cancels loading in the background the similar media items.
		mDataManager.cancelGetRelatedVideo();
	}

	@Override
	public void onStart(int operationId) {}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
			mMediaItems = (List<MediaItem>) responseObjects.get(RelatedVideoOperation.RESPONSE_KEY_RELATED_VIDEO);
			if (!Utils.isListEmpty(mMediaItems)) {
				mHomeMediaTilesAdapter.setMediaItems(mMediaItems);
				mHomeMediaTilesAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {}
}
