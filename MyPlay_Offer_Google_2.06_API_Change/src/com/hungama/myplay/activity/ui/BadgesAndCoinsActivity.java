package com.hungama.myplay.activity.ui;

import java.io.Serializable;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.BadgesAndCoins;
import com.hungama.myplay.activity.ui.MainActivity.NavigationItem;
import com.hungama.myplay.activity.ui.fragments.BadgesAndCoinsFragment;
import com.hungama.myplay.activity.ui.fragments.BadgesAndCoinsFragment.OnNotificationFinishedListener;
import com.hungama.myplay.activity.util.Appirater;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

public class BadgesAndCoinsActivity extends FragmentActivity implements OnNotificationFinishedListener {

	private static final String TAG = "BadgesAndCoinsActivity";
	
	public static final String ARGUMENT_OBJECT = "argument_object";
	public static final String ARGUMENT_IS_FINISHED_BADGES = "argument_is_finished_badges";	
	
	private FragmentManager mFragmentManager;
	private FragmentTransaction fragmentTransaction;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	
	private BadgesAndCoins mBadgesAndCoins;
//	private ImageFetcher mImageFetcher = null;
	private String url;
	
	private volatile boolean mIsDestroyed = false; 
	
	// ======================================================
	// ACTIVITY'S LIFECYCLE.
	// ======================================================
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		setContentView(R.layout.activity_badges_and_coins);
		
		mDataManager = DataManager.getInstance(this.getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		// shows the favorite type selection dialog.
		mBadgesAndCoins = (BadgesAndCoins) getIntent().getSerializableExtra(ARGUMENT_OBJECT);
		if (mBadgesAndCoins != null) {
			Bundle data = new Bundle();				
			data.putSerializable(BadgesAndCoinsFragment.FRAGMENT_ARGUMENT_BADGES_AND_COINS, (Serializable) mBadgesAndCoins);
			addFragment(data);
		}	
		if (mBadgesAndCoins.getBadgeUrl() != null) {
			Logger.i(TAG, mBadgesAndCoins.getBadgeUrl().toString());
	        url = mBadgesAndCoins.getBadgeUrl();
	        Logger.i(TAG, url);
		}		
				
	}
	
	@Override
	public void onBackPressed() {}
	
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
//		if (mImageFetcher != null) {
//			mImageFetcher.setExitTasksEarly(false);
//		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
//		if (mImageFetcher != null) {
//			mImageFetcher.setExitTasksEarly(true);
//	        mImageFetcher.flushCache();
//		}
	}
	
	@Override
	protected void onDestroy() {
		mIsDestroyed = true;
		Intent intent = new Intent(this, BadgesAndCoinsActivity.class);
		intent.putExtra(BadgesAndCoinsActivity.ARGUMENT_IS_FINISHED_BADGES, true);
		setResult(RESULT_OK, intent);
		super.onDestroy();
		
//		if (mImageFetcher != null) {
//			mImageFetcher.closeCache();
//			mImageFetcher = null;
//		}
	}
	
	// ======================================================
	// Helper Methods.
	// ======================================================

	public void addFragment(Bundle detailsData) {
		
		mFragmentManager = getSupportFragmentManager();	
		fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter,
				R.anim.slide_and_show_bottom_exit,
                R.anim.slide_and_show_bottom_enter,
                R.anim.slide_and_show_bottom_exit);
		
		BadgesAndCoinsFragment mBadgesAndCoinsFragment = new BadgesAndCoinsFragment();
		mBadgesAndCoinsFragment.setOnNotificationFinishedListener(this);
		mBadgesAndCoinsFragment.setArguments(detailsData);		
		fragmentTransaction.add(R.id.main_fragmant_container, mBadgesAndCoinsFragment);
		fragmentTransaction.commit();
	}
		
	private void showBadgeDialog() {
		//set up custom dialog
		final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_badge_notification);
        
        TextView text = (TextView) dialog.findViewById(R.id.badge_bottom_inner_text_3);
        text.setText(mBadgesAndCoins.getBadgeName());
        
        // initializes the image loader.
        ImageView imageBadge = (ImageView) dialog.findViewById(R.id.badge_dialog_image);
 		
 		// creates the cache.
// 		ImageCache.ImageCacheParams cacheParams =
//                 new ImageCache.ImageCacheParams(this, DataManager.FOLDER_THUMBNAILS_CACHE);
// 		cacheParams.compressFormat = Bitmap.CompressFormat.PNG;
//         cacheParams.setMemCacheSizePercent(this, 0.10f);
// 		
// 		mImageFetcher = new ImageFetcher(this, imageBadge.getMeasuredWidth(), imageBadge.getMeasuredHeight());
// 		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
// 		mImageFetcher.setImageFadeIn(true);
//        mImageFetcher.loadImage(mBadgesAndCoins.getBadgeUrl(), imageBadge);
        Picasso.with(this).cancelRequest(imageBadge);
        if (this != null && mBadgesAndCoins.getBadgeUrl() != null && !TextUtils.isEmpty(mBadgesAndCoins.getBadgeUrl())) {
	        Picasso.with(this)
	        		.load(mBadgesAndCoins.getBadgeUrl())
	        		.into(imageBadge);
        }
        
        ImageButton closeButton = (ImageButton) dialog.findViewById(R.id.badges_info_dialog_title_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				finish();
			}
		});
        
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				finish();
				
			}
		});
        
        dialog.show();
        if (mApplicationConfigurations.isFirstBadgeDisplayed()) {
        	Appirater appirater = new Appirater(this);
            appirater.userDidSignificantEvent(true);
            mApplicationConfigurations.setIsFirstBadgeDisplayed(false);
        }        
        
        
	}

	public boolean isActivityDestroyed() {
		return mIsDestroyed;
	}
	
	// ======================================================
	// ACTIVITY'S Listener.
	// ======================================================
	
	@Override
	public void onNotificationFinishedListener() {
		showBadgeDialog();
	}	

}
