package com.hungama.myplay.activity.ui.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hungama.myplay.activity.R;

/**
 * Tab Bar of the {@link HomeFragment}.
 */
public class HomeTabBar extends LinearLayout implements OnClickListener {
	
	public static final int TAB_ID_LATEST = 		1000001;
	public static final int TAB_ID_FEATURED = 		1000002;
	public static final int TAB_ID_RECOMMENDED = 	1000003;
	public static final int TAB_ID_MY_STREAM = 		1000004;
	
	private Button mButtonLatest;
	private Button mButtonFeatured;
	private Button mButtonRecommended;
	private Button mButtonMyStream;
	
	private OnTabSelectedListener mOnTabSelectedListener;
	private int mCurrentTabId;
	

	public HomeTabBar(Context context) {
		super(context);
		initialize();
	}
	
	public HomeTabBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}
	
	public HomeTabBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}
	
	
	// ======================================================
	// PRIVATE HELPER METHODS.
	// ======================================================

	private void initialize() {
		
		this.setOrientation(LinearLayout.HORIZONTAL);
		
		Resources resources = getResources();
		
		// creates the separators between the tabs.
		View lineSeparator1 = new View(getContext());
		lineSeparator1.setBackgroundColor(resources.getColor(R.color.home_tabwidget_tab_separator));
		
		View lineSeparator2 = new View(getContext());
		lineSeparator2.setBackgroundColor(resources.getColor(R.color.home_tabwidget_tab_separator));
		
		View lineSeparator3 = new View(getContext());
		lineSeparator3.setBackgroundColor(resources.getColor(R.color.home_tabwidget_tab_separator));
		
		LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(2, LayoutParams.MATCH_PARENT);
		
		float labelSize = resources.getDimensionPixelSize(R.dimen.home_tabwidget_tab_label_text_size);
		
		// Latest.
		mButtonLatest = new Button(getContext());
		mButtonLatest.setId(TAB_ID_LATEST);
		mButtonLatest.setBackgroundResource(R.drawable.background_home_tabwidget_tab_regular_selector);
		mButtonLatest.setSingleLine(true);
		mButtonLatest.setGravity(Gravity.CENTER);
		mButtonLatest.setPadding(0, 0, 0, 0);
		mButtonLatest.setTextColor(resources.getColor(R.color.home_tabwidget_tab_label_regular_selected));
		mButtonLatest.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelSize);
		mButtonLatest.setText(R.string.home_tab_label_latest);
		mButtonLatest.setOnClickListener(this);
		
		// Featured.
		mButtonFeatured = new Button(getContext());
		mButtonFeatured.setId(TAB_ID_FEATURED);
		mButtonFeatured.setBackgroundResource(R.drawable.background_home_tabwidget_tab_regular_selector);
		mButtonFeatured.setSingleLine(true);
		mButtonFeatured.setGravity(Gravity.CENTER);
		mButtonFeatured.setPadding(0, 0, 0, 0);
		mButtonFeatured.setTextColor(resources.getColor(R.color.home_tabwidget_tab_label_regular_selected));
		mButtonFeatured.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelSize);
		mButtonFeatured.setText(R.string.home_tab_label_featured);
		mButtonFeatured.setOnClickListener(this);
		
		// Recommended.
		mButtonRecommended = new Button(getContext());
		mButtonRecommended.setId(TAB_ID_RECOMMENDED);
		mButtonRecommended.setBackgroundResource(R.drawable.background_home_tabwidget_tab_regular_selector);
		mButtonRecommended.setSingleLine(true);
		mButtonRecommended.setGravity(Gravity.CENTER);
		mButtonRecommended.setPadding(0, 0, 0, 0);
		mButtonRecommended.setTextColor(resources.getColor(R.color.home_tabwidget_tab_label_regular_selected));
		mButtonRecommended.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelSize);
		mButtonRecommended.setText(R.string.home_tab_label_recommended);
		mButtonRecommended.setSingleLine(true);
		mButtonRecommended.setOnClickListener(this);
		
		// My Stream.
		mButtonMyStream = new Button(getContext());
		mButtonMyStream.setId(TAB_ID_MY_STREAM);
		mButtonMyStream.setBackgroundResource(R.drawable.background_home_tabwidget_tab_stream_selector);
		mButtonMyStream.setSingleLine(true);
		mButtonMyStream.setGravity(Gravity.CENTER);
		mButtonMyStream.setPadding(0, 0, 0, 0);
		mButtonMyStream.setTextColor(resources.getColor(R.color.home_tabwidget_tab_label_stream_selected));
		mButtonMyStream.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelSize);
		mButtonMyStream.setText(R.string.home_tab_label_my_stream);
		mButtonMyStream.setSingleLine(true);
		mButtonMyStream.setOnClickListener(this);
		
		LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
																LinearLayout.LayoutParams.MATCH_PARENT);
		
		tabParams.weight = 1;
		
		this.addView(mButtonLatest, tabParams);
		this.addView(lineSeparator1, separatorParams);
		this.addView(mButtonFeatured, tabParams);
		this.addView(lineSeparator2, separatorParams);
		this.addView(mButtonRecommended, tabParams);
		this.addView(lineSeparator3, separatorParams);
		this.addView(mButtonMyStream, tabParams);
		
		this.invalidate();

		setCurrentSelected(0);
	}
	
	@Override
	public void onClick(View view) {
		int tabId = view.getId();
		switch (tabId) {
			case TAB_ID_LATEST:
				
				if (view.isSelected()) {
					// updates the listener that the tab was retapped.
					if (mOnTabSelectedListener != null) {
						mOnTabSelectedListener.onTabReselected(TAB_ID_LATEST);
					}
					
				} else {
					// updates the tab's view.
					mButtonLatest.setSelected(true);
					mButtonFeatured.setSelected(false);
					mButtonRecommended.setSelected(false);
					mButtonMyStream.setSelected(false);
					this.invalidate();
					
					mCurrentTabId = TAB_ID_LATEST;
					
					// updates the listener to the view.
					if (mOnTabSelectedListener != null) {
						mOnTabSelectedListener.onTabSelected(TAB_ID_LATEST);
					}
				}
				
				break;
				
			case TAB_ID_FEATURED:
				
				if (view.isSelected()) {
					// updates the listener that the tab was retapped.
					if (mOnTabSelectedListener != null) {
						mOnTabSelectedListener.onTabReselected(TAB_ID_FEATURED);
					}
					
				} else {
					// updates the tab's view.
					mButtonLatest.setSelected(false);
					mButtonFeatured.setSelected(true);
					mButtonRecommended.setSelected(false);
					mButtonMyStream.setSelected(false);
					this.invalidate();
					
					mCurrentTabId = TAB_ID_FEATURED;
					
					// updates the listener to the view.
					if (mOnTabSelectedListener != null) {
						mOnTabSelectedListener.onTabSelected(TAB_ID_FEATURED);
					}
				}
				
				break;
				
			case TAB_ID_RECOMMENDED:
				
				if (view.isSelected()) {
					// updates the listener that the tab was retapped.
					if (mOnTabSelectedListener != null) {
						mOnTabSelectedListener.onTabReselected(TAB_ID_RECOMMENDED);
					}
					
				} else {
					// updates the tab's view.
					mButtonLatest.setSelected(false);
					mButtonFeatured.setSelected(false);
					mButtonRecommended.setSelected(true);
					mButtonMyStream.setSelected(false);
					this.invalidate();
					
					mCurrentTabId = TAB_ID_RECOMMENDED;
					
					// updates the listener to the view.
					if (mOnTabSelectedListener != null) {
						mOnTabSelectedListener.onTabSelected(TAB_ID_RECOMMENDED);
					}
				}
		
				break;
		
			case TAB_ID_MY_STREAM:
				
				if (view.isSelected()) {
					// updates the listener that the tab was retapped.
					if (mOnTabSelectedListener != null) {
						mOnTabSelectedListener.onTabReselected(TAB_ID_MY_STREAM);
					}
					
				} else {
					// updates the tab's view.
					mButtonLatest.setSelected(false);
					mButtonFeatured.setSelected(false);
					mButtonRecommended.setSelected(false);
					mButtonMyStream.setSelected(true);
					this.invalidate();
					
					mCurrentTabId = TAB_ID_MY_STREAM;
					
					// updates the listener to the view.
					if (mOnTabSelectedListener != null) {
						mOnTabSelectedListener.onTabSelected(TAB_ID_MY_STREAM);
					}
				}
		
				break;
		}
		
	}
	
	
	// ======================================================
	// PUBLIC.
	// ======================================================
	
	/**
	 * Interface callbacks to be invoked 
	 * when the user has interact with the tabs / selected a tab.
	 */
	public interface OnTabSelectedListener {
		
		/**
		 * Invoked when the tab was selected.
		 */
		public void onTabSelected(int tabId);
		
		/**
		 * Invoked when the tab was clicked when was already selected.
		 */
		public void onTabReselected(int tabId);
	}

	public void setOnTabSelectedListener(OnTabSelectedListener listener) {
		mOnTabSelectedListener = listener;
	}
			
	public void setCurrentSelected(int position) {
		switch (position) {
			case 0:
				mButtonLatest.performClick();
				break;
			
			case 1:
				mButtonFeatured.performClick();		
				break;
						
			case 2:
				mButtonRecommended.performClick();
				break;
	
			case TAB_ID_MY_STREAM:
			case 3:
				mButtonMyStream.performClick();
				break;
		}
	}
	
	public void markSelectedTab(int tabId) {
		switch (tabId) {
		case TAB_ID_LATEST:
			mButtonLatest.setSelected(true);
			mButtonFeatured.setSelected(false);
			mButtonRecommended.setSelected(false);
			mButtonMyStream.setSelected(false);
			break;
		
		case TAB_ID_FEATURED:
			mButtonLatest.setSelected(false);
			mButtonFeatured.setSelected(true);
			mButtonRecommended.setSelected(false);
			mButtonMyStream.setSelected(false);	
			break;
					
		case TAB_ID_RECOMMENDED:
			mButtonLatest.setSelected(false);
			mButtonFeatured.setSelected(false);
			mButtonRecommended.setSelected(true);
			mButtonMyStream.setSelected(false);
			break;

		case TAB_ID_MY_STREAM:
			mButtonLatest.setSelected(false);
			mButtonFeatured.setSelected(false);
			mButtonRecommended.setSelected(false);
			mButtonMyStream.setSelected(true);
			break;
	}
	}
	
	public int getSelectedTab() {
		return mCurrentTabId;
	}
	
}
