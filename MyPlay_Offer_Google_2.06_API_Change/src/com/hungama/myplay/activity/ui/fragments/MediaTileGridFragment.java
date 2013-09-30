package com.hungama.myplay.activity.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.ui.adapters.MediaTilesAdapter;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;

/**
 * Presents {@link MediaItem}s in a grid.
 */
public class MediaTileGridFragment extends MainFragment {

	private static final String TAG = "MediaTileGridFragment";
	
	public static final String FRAGMENT_ARGUMENT_MEDIA_ITEMS = "fragment_argument_media_items";
	
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
	
	
	// ======================================================
	// FRAGMENT'S LIFE CYCLE. 
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		Logger.v(TAG, "The device build number is: " + Integer.toString(Build.VERSION.SDK_INT));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mTilesGridView.setBackground(null);
		} else {
			mTilesGridView.setBackgroundDrawable(null);
		}
		
		// sets the gridview's cool margin.
		GridView.MarginLayoutParams params = 
				new GridView.MarginLayoutParams(
						GridView.MarginLayoutParams.MATCH_PARENT, 
						GridView.MarginLayoutParams.MATCH_PARENT);
		params.setMargins(imageTileSpacing, imageTileSpacing, imageTileSpacing, imageTileSpacing);
		
//		FrameLayout.LayoutParams params = 
//				new FrameLayout.LayoutParams(
//						FrameLayout.LayoutParams.MATCH_PARENT, 
//						FrameLayout.LayoutParams.MATCH_PARENT);
//		
//		params.leftMargin = imageTileSpacing;
//		params.topMargin = imageTileSpacing;
//		params.rightMargin = imageTileSpacing;
//		params.bottomMargin = imageTileSpacing;
		
		mTilesGridView.setLayoutParams(params);
		mTilesGridView.setPadding(0, imageTileSpacing, 0, imageTileSpacing);
		
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
        
        if(mHomeMediaTilesAdapter == null){
        	mHomeMediaTilesAdapter = new MediaTilesAdapter(getActivity(), mTilesGridView, mTileSize, mediaItems,false);
        }
        
        mHomeMediaTilesAdapter.setOnMusicItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
		mTilesGridView.setAdapter(mHomeMediaTilesAdapter);
		
		return mTilesGridView;
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
	
	protected GridView getGridView() {
		return mTilesGridView;
	}
	
	protected void setGridView(GridView gridView) {
		mTilesGridView = gridView;
	}

	
}
