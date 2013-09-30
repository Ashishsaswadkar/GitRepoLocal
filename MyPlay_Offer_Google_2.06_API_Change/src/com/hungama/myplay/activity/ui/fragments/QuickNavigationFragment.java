package com.hungama.myplay.activity.ui.fragments;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.ui.MainActivity.NavigationItem;
import com.hungama.myplay.activity.util.Utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Presents the Quick Navigation panel within the application.
 */
public class QuickNavigationFragment extends Fragment implements OnClickListener {
	
	private static final String TAG = "QuickNavigationFragment";
	
	private NavigationItem mCurrentNavigationItem;
	private NavigationItem mLastNavigationItem;
	
	private LinearLayout mLastSelectedItem = null;
	
	private OnQuickNavigationItemSelectedListener mOnQuickNavigationItemSelectedListener;
	
	private LinearLayout mItemMusic;
	private LinearLayout mItemDiscover;
	private LinearLayout mItemRadio;
	private LinearLayout mItemSpecials;
	private LinearLayout mItemVideos;
	private ImageView mBackgroundImage;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_quick_navigation, container, false);
		
		mItemMusic = (LinearLayout) rootView.findViewById(R.id.main_navigation_item_music);
		mItemDiscover = (LinearLayout) rootView.findViewById(R.id.main_navigation_item_discover);
		mItemRadio = (LinearLayout) rootView.findViewById(R.id.main_navigation_item_radio);
		mItemSpecials = (LinearLayout) rootView.findViewById(R.id.main_navigation_item_specials);
		mItemVideos = (LinearLayout) rootView.findViewById(R.id.main_navigation_item_videos);
		mBackgroundImage = (ImageView) rootView.findViewById(R.id.navigation_bg);
		
		rootView.setOnClickListener(this);
		rootView.setSoundEffectsEnabled(false);
		
		mItemMusic.setOnClickListener(this);
		mItemDiscover.setOnClickListener(this);
		mItemRadio.setOnClickListener(this);
		mItemSpecials.setOnClickListener(this);
		mItemVideos.setOnClickListener(this);
		
		int width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
    	int height = (int) (width * 0.6375) ;
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
    	mBackgroundImage.setLayoutParams(params);
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (mCurrentNavigationItem != null) {
			LinearLayout currentItem = null;
			// set selected the item that is the currently viewed.
			if (mCurrentNavigationItem == NavigationItem.MUSIC) {
				currentItem = mItemMusic;
			} else if (mCurrentNavigationItem == NavigationItem.DISCOVER) {
				currentItem = mItemDiscover;
			} else if (mCurrentNavigationItem == NavigationItem.RADIO) {
				currentItem = mItemRadio;
			} else if (mCurrentNavigationItem == NavigationItem.SPECIALS) {
				currentItem = mItemSpecials;
			} else if (mCurrentNavigationItem == NavigationItem.VIDEOS) {
				currentItem = mItemVideos;
			} else {
				if (mLastNavigationItem != null) {
					if (mLastNavigationItem == NavigationItem.MUSIC) {
						currentItem = mItemMusic;
					} else if (mLastNavigationItem == NavigationItem.DISCOVER) {
						currentItem = mItemDiscover;
					} else if (mLastNavigationItem == NavigationItem.RADIO) {
						currentItem = mItemRadio;
					} else if (mLastNavigationItem == NavigationItem.SPECIALS) {
						currentItem = mItemSpecials;
					} else if (mLastNavigationItem == NavigationItem.VIDEOS) {
						currentItem = mItemVideos;
					}
				}
			}
			
			if (currentItem != null) {
				mLastSelectedItem = currentItem;
				currentItem.setBackgroundResource(R.drawable.background_main_navigation_item_selected);
			}
		}
	}
	
	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		LinearLayout currentItem = null;
		
		if (viewId == R.id.main_navigation_item_music) {
			mCurrentNavigationItem = NavigationItem.MUSIC;
			currentItem = mItemMusic;
				
		} else if (viewId == R.id.main_navigation_item_discover) {
			mCurrentNavigationItem = NavigationItem.DISCOVER;
			currentItem = mItemDiscover;
				
		} else if (viewId == R.id.main_navigation_item_radio) {
			mCurrentNavigationItem = NavigationItem.RADIO;
			currentItem = mItemRadio;
				
		} else if (viewId == R.id.main_navigation_item_specials) {
			mCurrentNavigationItem = NavigationItem.SPECIALS;
			currentItem = mItemSpecials;
				
		} else if (viewId == R.id.main_navigation_item_videos) {
			mCurrentNavigationItem = NavigationItem.VIDEOS;
			currentItem = mItemVideos;
				
		}
		
		if (mLastSelectedItem != null) {
			mLastSelectedItem.setBackgroundResource(R.drawable.background_main_navigation_item_selected);
		}
		
		if (currentItem != null) {
			currentItem.setBackgroundResource(R.drawable.background_main_navigation_item_selected);
			if (mOnQuickNavigationItemSelectedListener != null) {
				mOnQuickNavigationItemSelectedListener.onQuickNavigationItemSelected(mCurrentNavigationItem);
			}
		}
	}
	
	
	// ======================================================
	// PUBLIC.
	// ======================================================
	
	public void setCurrentNavigationItem(NavigationItem navigationItem) {
		mCurrentNavigationItem = navigationItem;
	}
	
	public void setLastNavigationItem(NavigationItem navigationItem) {
		mLastNavigationItem = navigationItem;
	}
	
	/**
	 * Interface definition to be invoked when the user has selected one of the Navigation's items.
	 */
	public interface OnQuickNavigationItemSelectedListener {
		
		public void onQuickNavigationItemSelected(NavigationItem navigationItem);
	}
	
	public void setOnQuickNavigationItemSelectedListener(OnQuickNavigationItemSelectedListener listener) {
		mOnQuickNavigationItemSelectedListener = listener;
	}

}
