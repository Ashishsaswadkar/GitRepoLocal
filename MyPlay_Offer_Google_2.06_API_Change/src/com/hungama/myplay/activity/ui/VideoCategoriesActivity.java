package com.hungama.myplay.activity.ui;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaContentOperation;
import com.hungama.myplay.activity.ui.fragments.MediaTileGridFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.RightIconOptionsItem;
import com.hungama.myplay.activity.util.Logger;

/**
 * Presents Video tiles with selection of the latest and featured.
 */
public class VideoCategoriesActivity extends MainActivity implements OnMediaItemOptionSelectedListener, 
																CommunicationOperationListener{
	
	private static final String TAG = "VideoCategoriesActivity";
	
	public static final String KEY_INTENT_DATA_CATEGORY = "key_intent_data_categories";
	
	private Context mContext;
	private FragmentManager mFragmentManager;
	private DataManager mDataManager;
	
	private MediaTileGridFragment mTilesFragment;
	
	private ImageButton mButtonToggleCategoryTypes;
	private LinearLayout mLayoutCategoryTypeContainer;
	private RightIconOptionsItem mOptionLatest;
	private RightIconOptionsItem mOptionFeatured;
	
	private List<MediaItem> mMediaItemsLatest = null;
	private List<MediaItem> mMediaItemsFeatured = null;
	
	private Category mSelectedCategory;
	private MediaCategoryType mSelectedMediaCategoryType;
	
	
	// ======================================================
	// ACTIVITY'S LIFE CYCLE. 
	// ======================================================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_categories);
		
		Intent intent = getIntent();
		
		mSelectedCategory = (Category) intent.getSerializableExtra(KEY_INTENT_DATA_CATEGORY);
		if (mSelectedCategory == null) {
			Logger.e(TAG, "No category included with calling intent.");
			finish();
			return;
		}
		
		// initializes components.
		mContext = getApplicationContext();
		mFragmentManager = getSupportFragmentManager();
		mDataManager = DataManager.getInstance(mContext);
		
		mButtonToggleCategoryTypes = (ImageButton) findViewById(R.id.video_categories_button_select_category);
		mLayoutCategoryTypeContainer = (LinearLayout) findViewById(R.id.video_categories_select_category_type_panel);
		mOptionLatest = (RightIconOptionsItem) findViewById(R.id.video_categories_category_latest);
		mOptionFeatured = (RightIconOptionsItem) findViewById(R.id.video_categories_category_featured);
		
		// Initializes user controls.
		TextView titleText = (TextView) findViewById(R.id.main_title_bar_text);
		titleText.setText(mSelectedCategory.getName());
		
		mTilesFragment = new MediaTileGridFragment();
		mTilesFragment.setOnMediaItemOptionSelectedListener(this);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.main_fragmant_container, mTilesFragment);
		fragmentTransaction.commit();
		
		mSelectedMediaCategoryType = MediaCategoryType.LATEST;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if (mMediaItemsLatest == null && mMediaItemsFeatured == null) {
			// get videos for thus category typse and category.
			mDataManager.getMediaItems(MediaContentType.VIDEO, MediaCategoryType.LATEST, mSelectedCategory, this);
			mDataManager.getMediaItems(MediaContentType.VIDEO, MediaCategoryType.FEATURED, mSelectedCategory, this);
		}
		
		FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		
	}
	
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
	
	public void onSelectCategoryTypeClicked(View view) {
		if (mLayoutCategoryTypeContainer.getVisibility() == View.GONE) {
			// open the panel.
			mLayoutCategoryTypeContainer.setVisibility(View.VISIBLE);
			
			// sets the button icon.
			mButtonToggleCategoryTypes.setImageResource(R.drawable.icon_white_content_collapse_up);
			mButtonToggleCategoryTypes.setBackgroundResource(R.color.black);
			// marks the current category type.
			if (mSelectedMediaCategoryType == MediaCategoryType.LATEST) {
				mOptionLatest.setIcon(getResources().getDrawable(R.drawable.icon_green_tick));
				mOptionFeatured.setIcon(null);
			} else {
				mOptionLatest.setIcon(null);
				mOptionFeatured.setIcon(getResources().getDrawable(R.drawable.icon_green_tick));
			}
		} else {
			closeCategoryTypesPanel();
		}
	}
	
	public void onCategoryTypeLatestClicked(View view) {
		mSelectedMediaCategoryType = MediaCategoryType.LATEST;
		mTilesFragment.setMediaItems(mMediaItemsLatest);
		closeCategoryTypesPanel();
		
		FlurryAgent.logEvent("Video - Category Latest");
	}

	public void onCategoryTypeFeaturedClicked(View view) {
		mSelectedMediaCategoryType = MediaCategoryType.FEATURED;
		mTilesFragment.setMediaItems(mMediaItemsFeatured);
		closeCategoryTypesPanel();
		
		FlurryAgent.logEvent("Video - Category Recommended");
	}
	
	private void closeCategoryTypesPanel() {
		// closes the panel.
		mLayoutCategoryTypeContainer.setVisibility(View.GONE);
		
		// sets the button icon.
		mButtonToggleCategoryTypes.setImageResource(R.drawable.icon_white_content_collapse_down);
		mButtonToggleCategoryTypes.setBackgroundResource(0);
	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}
	
	
	// ======================================================
	// ACTIVITY'S EVENT LISTENERS - HOME.
	// ======================================================
	
	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position) {
		
	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem, int position) {
		
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem, int position) {
		
	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem, int position) {
		Intent intent = new Intent(this, VideoActivity.class);
		intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO, (Serializable) mediaItem);
		startActivity(intent);
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position) {
		
	}

	
	// ======================================================
	// Communication Operation listeners.
	// ======================================================

	
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED 	|| 
			operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST 	||
			operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED) {
			
			showLoadingDialog(R.string.application_dialog_loading_content);
		}
	}
	
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED 	|| 
			operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST 	||
			operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED) {
			
			MediaCategoryType mediaCategoryType = (MediaCategoryType) responseObjects.get(MediaContentOperation.RESULT_KEY_OBJECT_MEDIA_CATEGORY_TYPE);
			if (mediaCategoryType == MediaCategoryType.LATEST) {
				mMediaItemsLatest = (List<MediaItem>) responseObjects.get(MediaContentOperation.RESULT_KEY_OBJECT_MEDIA_ITEMS);
				// updates the grid with the latest media items.
				mTilesFragment.setMediaItems(mMediaItemsLatest);
			} else if (mediaCategoryType == MediaCategoryType.FEATURED) {
				// stores this list for later use. 
				mMediaItemsFeatured = (List<MediaItem>) responseObjects.get(MediaContentOperation.RESULT_KEY_OBJECT_MEDIA_ITEMS);
			}
		}
		hideLoadingDialog();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		hideLoadingDialog();
	}

}
