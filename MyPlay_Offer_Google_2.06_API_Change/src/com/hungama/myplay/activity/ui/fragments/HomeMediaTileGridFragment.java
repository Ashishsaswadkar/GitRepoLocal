package com.hungama.myplay.activity.ui.fragments;

import java.util.List;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.ui.adapters.MediaTilesAdapter;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;

/**
 * Implementation of the {@link MediaTileGridFragment} that gives the required behavior
 * for presenting media items in the grid.
 */
public class HomeMediaTileGridFragment extends MainFragment {
	
	private static final String TAG = "MediaTileGridFragment";
	
	public static final String FRAGMENT_ARGUMENT_MEDIA_ITEMS = "fragment_argument_media_items";
	
	private GridView mTilesGridView;
	private int mTileSize = 0;
	
	private MediaTilesAdapter mHomeMediaTilesAdapter;
	
	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;
	private OnRetryButtonClickedLister mOnRetryButtonClickedLister;
	
	
	// ======================================================
	// FRAGMENT'S LIFE CYCLE. 
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home_media_tiles_grid, container, false);
		
		mTilesGridView = (GridView) rootView.findViewById(R.id.media_tile_gridview);
		LinearLayout emptyView = (LinearLayout) rootView.findViewById(R.id.connection_error_empty_view);
		Button retryButton = (Button) rootView.findViewById(R.id.connection_error_empty_view_button_retry);
		retryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mOnRetryButtonClickedLister != null) {
					mOnRetryButtonClickedLister.onRetryButtonClicked(view);
				}
			}
		});
		
		//mTilesGridView.setEmptyView(emptyView);

		// sets the gird's properties.
		int imageTileSpacingVertical = getResources().getDimensionPixelSize(R.dimen.home_tiles_spacing_vertical);
		int imageTileSpacingHorizontal = getResources().getDimensionPixelSize(R.dimen.home_tiles_spacing_horizontal);
		
		mTilesGridView.setGravity(Gravity.CENTER_HORIZONTAL);
		mTilesGridView.setVerticalSpacing(imageTileSpacingVertical);
		mTilesGridView.setHorizontalSpacing(imageTileSpacingHorizontal);
		mTilesGridView.setNumColumns(GridView.AUTO_FIT);
		mTilesGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mTilesGridView.setOverScrollMode(GridView.OVER_SCROLL_NEVER);
		}
		
		// sets the background.
		Logger.v(TAG, "The device build number is: " + Integer.toString(Build.VERSION.SDK_INT));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mTilesGridView.setBackground(null);
		} else {
			mTilesGridView.setBackgroundDrawable(null);
		}
		
		// sets the gridview's cool margin.
//		LinearLayout.MarginLayoutParams params = 
//				new LinearLayout.MarginLayoutParams(
//						GridView.MarginLayoutParams.MATCH_PARENT, 
//						GridView.MarginLayoutParams.MATCH_PARENT);
//		params.setMargins(imageTileSpacing, imageTileSpacing, imageTileSpacing, imageTileSpacing);
//		
//		mTilesGridView.setLayoutParams(params);
		mTilesGridView.setPadding(0, imageTileSpacingVertical, 0, imageTileSpacingVertical);
		
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
        mTileSize = (int) ((screenWidth - (imageTileSpacingVertical + imageTileSpacingVertical*1.5)) / 2);
        
        Logger.i(TAG, "screenWidth: " + screenWidth + " mTileSize: " + mTileSize);
        mTilesGridView.setNumColumns(2);
        mTilesGridView.setColumnWidth(mTileSize);
        
        /*
         * gets the list of the media items from the arguments,
         * and sets it as the source to the adapter.
         */
        List<MediaItem> mediaItems = null;
        
        Bundle data = getArguments();
        if (data != null && data.containsKey(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS)) {
        	mediaItems = (List<MediaItem>) data.getSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS);
        }
        
        if(mediaItems == null){
        	mTilesGridView.setEmptyView(emptyView);
        }
        
        if(mHomeMediaTilesAdapter == null){
        	mHomeMediaTilesAdapter = new MediaTilesAdapter(getActivity(), mTilesGridView, mTileSize, mediaItems,false);	
        }
        
        mHomeMediaTilesAdapter.setOnMusicItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
		mTilesGridView.setAdapter(mHomeMediaTilesAdapter);
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if(mHomeMediaTilesAdapter != null){
			mHomeMediaTilesAdapter.resumeLoadingImages();	
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(mHomeMediaTilesAdapter != null){
			mHomeMediaTilesAdapter.stopLoadingImages();			
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mHomeMediaTilesAdapter != null){
			mHomeMediaTilesAdapter.releaseLoadingImages();			
		}
	}
	
	
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
	
	public void setMediaItems(List<MediaItem> mediaItems) {
		mHomeMediaTilesAdapter.setMediaItems(mediaItems);
		mHomeMediaTilesAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Retrieves the adapter which binds the MediaItems with the GridView.
	 */
	protected BaseAdapter getAdapter() {
		return mHomeMediaTilesAdapter;
	}
	
	protected GridView getGridView() {
		return mTilesGridView;
	}
	
	protected void setGridView(GridView gridView) {
		mTilesGridView = gridView;
	}

	public interface OnRetryButtonClickedLister {
		
		public void onRetryButtonClicked(View retryButton);
	}
	
	public void setOnRetryButtonClickedLister(OnRetryButtonClickedLister listener) {
		mOnRetryButtonClickedLister = listener;
	}
	
	
}
