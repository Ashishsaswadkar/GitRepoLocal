package com.hungama.myplay.activity.ui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.bugsense.trace.BugSenseHandler;
import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.CategoryTypeObject;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.NewVersionCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.VersionCheckResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaCategoriesOperation;
import com.hungama.myplay.activity.operations.hungama.MediaContentOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.NewVersionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.VersionCheckOperation;
import com.hungama.myplay.activity.ui.OnApplicationStartsActivity.ConnectionStatus;
import com.hungama.myplay.activity.ui.dialogs.ListDialog;
import com.hungama.myplay.activity.ui.dialogs.ListDialog.ListDialogItem;
import com.hungama.myplay.activity.ui.dialogs.ListDialog.OnListDialogStateChangedListener;
import com.hungama.myplay.activity.ui.fragments.HomeMediaTileGridFragment;
import com.hungama.myplay.activity.ui.fragments.HomeMediaTileGridFragment.OnRetryButtonClickedLister;
import com.hungama.myplay.activity.ui.fragments.MediaTileGridFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.fragments.SocialMyStreamFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.HomeTabBar;
import com.hungama.myplay.activity.ui.widgets.HomeTabBar.OnTabSelectedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ToastExpander;
import com.hungama.myplay.activity.util.billing.IabHelper;
import com.hungama.myplay.activity.util.billing.IabHelper.OnIabSetupFinishedListener;
import com.hungama.myplay.activity.util.billing.IabHelper.QueryInventoryFinishedListener;
import com.hungama.myplay.activity.util.billing.IabResult;
import com.hungama.myplay.activity.util.billing.Inventory;
import com.hungama.myplay.activity.util.billing.Purchase;

public class HomeActivity extends MainActivity implements OnMediaItemOptionSelectedListener, 
														  CommunicationOperationListener,
														  OnTabSelectedListener
//														  , 
//														  OnIabSetupFinishedListener, 
//														  QueryInventoryFinishedListener
														  {
	
	private static final String TAG = "HomeActivity";
//	public static String mHardwareId=null;
	public static final String ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE = "activity_extra_media_content_type";
	public static final String ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION = "activity_extra_default_opened_tab_position";
	public static final String ACTIVITY_EXTRA_IS_FROM_MY_PREFERENCES = "activity_extra_is_from_my_preferences";	
	
	public static final String ARGUMENT_HOME_ACTIVITY = "argument_home_activity";
	public static final int MY_PREFERENCES_ACTIVITY_RESULT_CODE = 101;
	
	public static final String EXTRA_MY_PREFERENCES_IS_CHANGED = "my_preferences_is_changed";
	
	public boolean isBackFromPreferences = false;
	// ======================================================
	// Public methods. 
	// ======================================================
	
	/**
	 * Resets the media content type of the activity, where is Music or Videos.
	 * This method reloads the content of the activity. 
	 * @param mediaContentType
	 */
	public void setMediaContentType(MediaContentType mediaContentType) {
		mCurrentMediaContentType = mediaContentType;
		
		/*
		 * "Reseting" the lists references to indicate that new data should be load. 
		 */
		if (mMediaItemsLatest != null) {
			mMediaItemsLatest.clear();
			mMediaItemsLatest = null;
		}
		
		if (mMediaItemsFeatured != null) {
			mMediaItemsFeatured.clear();
			mMediaItemsFeatured = null;
		}
		
		if (mMediaItemsRecommended != null) {
			mMediaItemsRecommended.clear();
			mMediaItemsRecommended = null;
		}
		
		if (mediaContentType == MediaContentType.VIDEO) {
			mButtonBrowseCategories.setVisibility(View.VISIBLE);
			mButtonBrowseCategoriesMusic.setVisibility(View.GONE);
		} else {
			mButtonBrowseCategories.setVisibility(View.GONE);
			mButtonBrowseCategoriesMusic.setVisibility(View.VISIBLE);
		}
		
		// sets the Action Bar's title.
		ActionBar actionBar = getSupportActionBar();
		
		String title;
		if (mediaContentType == MediaContentType.VIDEO) {
			title = getResources().getString(R.string.main_actionbar_title_videos);
		} else {
			title = getResources().getString(R.string.main_actionbar_title_music);
		}
		
		actionBar.setTitle(title);
		
		loadMediaItems();
	}
	
	/**
	 * Sets the current tab selected, specified by the {@code HomeTabBar} tabs definitions.
	 */
	public void setCurrentTab(int tab) {
		mHomeTabBar.setCurrentSelected(tab);
	}
	
	private Context mContext;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	
	private PlayerBarFragment mPlayerBar;
	private FragmentManager mFragmentManager;
	
	// views.
	private HomeTabBar mHomeTabBar;
	
	// button for displaying category to browse videos with.
	private ImageButton mButtonBrowseCategories;
	// button for displaying category to browse music with.
	private ImageButton mButtonBrowseCategoriesMusic;
	// button for displaying category to browse music with.
	private Button mButtonBrowseCategoriesMusicTop;
	
	private MediaContentType mCurrentMediaContentType = MediaContentType.MUSIC;
	
	private List<MediaItem> mMediaItemsLatest = null;
	private List<MediaItem> mMediaItemsFeatured = null;
	private List<MediaItem> mMediaItemsRecommended = null;
	
	private boolean mIsLoadingMediaItemsFeatured = false;
	private boolean mIsLoadingMediaItemsRecommended = false;
	
	private int mDeafultOpenedTab = HomeTabBar.TAB_ID_LATEST;
	
	private List<Integer> mMediaDetailsOperationsList = new ArrayList<Integer>();
	/*
	 * Flag indicationing for loading any content for the Activity,
	 * this sellution is poor and fragile and should be replaced with 
	 * Content Providers and Loaders.
	 * 
	 * For now, this is the most safest and fastest sellution to avoid crashes
	 * when the activity saves it state after it's been stopped.
	 * 
	 * The Flag is been used to controll the "Loading" dialog in this activity.
	 */
	private boolean mIsLoading = false;
	private boolean mActivityStopped = false;
	
	public static boolean wasInBackground;
	
	private Resources mResources;
	
	private IabHelper billingHelper;
	// ======================================================
	// Life cycle.
	// ======================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		mContext = getApplicationContext();
		mDataManager = DataManager.getInstance(mContext);
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		
		mResources = getResources();
		
		mDataManager.getTimeRead(this);		
		
		mFragmentManager = getSupportFragmentManager();
		
		/*if (mCurrentMediaContentType == MediaContentType.MUSIC) {
			messageThread();
			Log.i("Ashish", "times");
		}*/
		
		if (Boolean.parseBoolean(mContext.getResources().getString(R.string.rate_app_on_off))) {							
			mAppirater.appLaunched(true);	
		}

		mPlayerBar = getPlayerBar();
		
		initializeUserControls();
		
		/*
		 * Checks if it was requested for a specific Media Category to
		 * be presented, if not Music is the defaut.
		 */
		Intent callingIntent = getIntent();
		if (callingIntent != null) {
			Bundle arguments = callingIntent.getExtras();
			if (arguments != null) {
				// gets the content type of the activity.
				if (arguments.containsKey(ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE)) {
					mCurrentMediaContentType = (MediaContentType) arguments.getSerializable(ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE);
				}
				// checks if there is a request to open other tab then the "Latest".
				if (arguments.containsKey(ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION)) {
					mDeafultOpenedTab = arguments.getInt(ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION);
					mHomeTabBar.markSelectedTab(mDeafultOpenedTab);
				}
			}
		}
		
		if (mCurrentMediaContentType == MediaContentType.VIDEO) {
			mButtonBrowseCategories.setVisibility(View.VISIBLE);
			mButtonBrowseCategoriesMusic.setVisibility(View.GONE);
			mButtonBrowseCategoriesMusicTop.setVisibility(View.GONE);
			
		} else {
			mButtonBrowseCategories.setVisibility(View.GONE);
			mButtonBrowseCategoriesMusic.setVisibility(View.VISIBLE);
			mButtonBrowseCategoriesMusicTop.setVisibility(View.GONE);
		}
		
		// sets the Action Bar's title.
		ActionBar actionBar = getSupportActionBar();
		String title;
		if (mCurrentMediaContentType == MediaContentType.VIDEO) {
			title = getResources().getString(R.string.main_actionbar_title_videos);
		} else {
			title = getResources().getString(R.string.main_actionbar_title_music);
		}
		actionBar.setTitle(title);

		// call check subscription
//		setupInAppBilling();
		AccountManager accountManager = AccountManager.get(mContext);
		Account[] accounts = accountManager.getAccountsByType("com.google");

		String accountType = null;
		if(accounts != null && accounts.length > 0){
			accountType = accounts[0].name; 
		}
		mDataManager.getCurrentSubscriptionPlan(this, accountType);
		
		//call version check
		if (!mApplicationConfigurations.isVersionChecked()) {			
//			mDataManager.versionCheck(this);
			mDataManager.newVersionCheck(this);
		}
	}
		
	@Override
	protected void onStart() {		
		super.onStart();
		
		mActivityStopped = false;
		
		FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
		
		if (mCurrentMediaContentType == MediaContentType.MUSIC) {
			messageThread();
			Log.i("Ashish", "times");
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		wasInBackground = true;
		
		if (mMediaItemsLatest == null && mMediaItemsFeatured == null && mMediaItemsRecommended == null) {
			// Rock & Roll.
			loadMediaItems();
			
		} else {
			// if the application loads any data related to this Activity?
			if (mIsLoading) {
				showLoadingDialog(R.string.application_dialog_loading_content);
			}
		}
	}
	
	@Override
	protected void onPause() {
		
		/*
		 * No matter what, remove any existing dialog from the activity.
		 * Will be resumed only if the activity 
		 * is visible and content is still being loaded to it. 
		 */
		hideLoadingDialog();
		
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		
		mActivityStopped = true;
		
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	protected void onDestroy() {
		super.onDestroy();
		
		wasInBackground = false;
	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		
		if (mCurrentMediaContentType == MediaContentType.MUSIC) {
			return NavigationItem.MUSIC;
		}
		
		return NavigationItem.VIDEOS;
	}
	
	@Override
	public void onTabSelected(int tabId) {
		if (isActivityDestroyed()) {
			return;
		}
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		
		if (tabId == HomeTabBar.TAB_ID_LATEST) {
			if (mCurrentMediaContentType == MediaContentType.VIDEO) {
				mButtonBrowseCategories.setVisibility(View.VISIBLE);
				mButtonBrowseCategoriesMusic.setVisibility(View.GONE);
				mButtonBrowseCategoriesMusicTop.setVisibility(View.GONE);
				FlurryAgent.logEvent("Video: Latest");
				
			} else {
				mButtonBrowseCategories.setVisibility(View.GONE);
				mButtonBrowseCategoriesMusic.setVisibility(View.VISIBLE);
				mButtonBrowseCategoriesMusicTop.setVisibility(View.GONE);
				FlurryAgent.logEvent("Music: Latest");
			}
			
			HomeMediaTileGridFragment latestFragment = new HomeMediaTileGridFragment();
			latestFragment.setOnRetryButtonClickedLister(new OnRetryButtonClickedLister() {
				@Override
				public void onRetryButtonClicked(View retryButton) {
					retry(MediaCategoryType.LATEST);
				}
			});
			latestFragment.setOnMediaItemOptionSelectedListener(this);
			
			Bundle arguments = new Bundle();
			arguments.putSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS, (Serializable) mMediaItemsLatest);
			latestFragment.setArguments(arguments);

			fragmentTransaction.replace(R.id.main_fragmant_container, latestFragment);
			fragmentTransaction.commitAllowingStateLoss();
			
			if (mApplicationConfigurations.isEnabledHomeGuidePage()) {
				mApplicationConfigurations.setIsEnabledHomeGuidePage(false);
				openAppGuideActivity();
			}
			
			
		} else if (tabId == HomeTabBar.TAB_ID_FEATURED) {
			if (mCurrentMediaContentType == MediaContentType.VIDEO) {
				mButtonBrowseCategories.setVisibility(View.VISIBLE);
				mButtonBrowseCategoriesMusic.setVisibility(View.GONE);
				mButtonBrowseCategoriesMusicTop.setVisibility(View.GONE);
				FlurryAgent.logEvent("Video: Top");
				
			} else {
				mButtonBrowseCategories.setVisibility(View.GONE);
				mButtonBrowseCategoriesMusic.setVisibility(View.GONE);
				mButtonBrowseCategoriesMusicTop.setVisibility(View.VISIBLE);
				FlurryAgent.logEvent("Music: Top");
			}
			
			if (mIsLoadingMediaItemsFeatured) {
				showLoadingDialog(R.string.application_dialog_loading_content);
			}
			
			HomeMediaTileGridFragment featuredFragment = new HomeMediaTileGridFragment();
			featuredFragment.setOnRetryButtonClickedLister(new OnRetryButtonClickedLister() {
				@Override
				public void onRetryButtonClicked(View retryButton) {
					retry(MediaCategoryType.FEATURED);
				}
			});
			featuredFragment.setOnMediaItemOptionSelectedListener(this);
			
			Bundle arguments = new Bundle();
			arguments.putSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS, (Serializable) mMediaItemsFeatured);
			featuredFragment.setArguments(arguments);
			
			fragmentTransaction.replace(R.id.main_fragmant_container, featuredFragment);
			fragmentTransaction.commitAllowingStateLoss();
			
		} else if (tabId == HomeTabBar.TAB_ID_RECOMMENDED) {
			if (mCurrentMediaContentType == MediaContentType.VIDEO) {
				mButtonBrowseCategories.setVisibility(View.VISIBLE);
				mButtonBrowseCategoriesMusic.setVisibility(View.GONE);
				mButtonBrowseCategoriesMusicTop.setVisibility(View.GONE);
				FlurryAgent.logEvent("Video: Recommended");
				
			} else {
				mButtonBrowseCategories.setVisibility(View.GONE);
				mButtonBrowseCategoriesMusic.setVisibility(View.VISIBLE);
				mButtonBrowseCategoriesMusicTop.setVisibility(View.GONE);
				FlurryAgent.logEvent("Music: Recommended");
			}
			
			if (mIsLoadingMediaItemsRecommended) {
				showLoadingDialog(R.string.application_dialog_loading_content);
			}
			
			HomeMediaTileGridFragment recommendedFragment = new HomeMediaTileGridFragment();
			recommendedFragment.setOnRetryButtonClickedLister(new OnRetryButtonClickedLister() {
				@Override
				public void onRetryButtonClicked(View retryButton) {
					retry(MediaCategoryType.RECOMMENDED);
				}
			});
			recommendedFragment.setOnMediaItemOptionSelectedListener(this);
			
			Bundle arguments = new Bundle();
			arguments.putSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS, (Serializable) mMediaItemsRecommended);
			recommendedFragment.setArguments(arguments);
			
			fragmentTransaction.replace(R.id.main_fragmant_container, recommendedFragment);
			fragmentTransaction.commitAllowingStateLoss();
			
		} else if (tabId == HomeTabBar.TAB_ID_MY_STREAM) {
			
			mButtonBrowseCategories.setVisibility(View.GONE);
			mButtonBrowseCategoriesMusic.setVisibility(View.GONE);
			mButtonBrowseCategoriesMusicTop.setVisibility(View.GONE);
			
			SocialMyStreamFragment myStreemFragment = new SocialMyStreamFragment();
			myStreemFragment.setOnMediaItemOptionSelectedListener(this);
			fragmentTransaction.replace(R.id.main_fragmant_container, myStreemFragment);
			fragmentTransaction.commit();
		}
		
	}
	
	@Override
	public void onTabReselected(int tabId) {
		switch (tabId) {
		case HomeTabBar.TAB_ID_LATEST:
			mDataManager.getMediaItems(mCurrentMediaContentType, MediaCategoryType.LATEST, null, this);
			break;
		case HomeTabBar.TAB_ID_FEATURED:
			mDataManager.getMediaItems(mCurrentMediaContentType, MediaCategoryType.FEATURED, null, this);
			break;
		case HomeTabBar.TAB_ID_RECOMMENDED:
			mDataManager.getMediaItems(mCurrentMediaContentType, MediaCategoryType.RECOMMENDED, null, this);
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
//		if (mPlayerBar.getPlayerState() == State.PAUSED ) {
//			mPlayerBar.clearQueue();
//		}
		
		if (isAnyActionBarOptionSelected() || mPlayerBar.isContentOpenedForBackStack()) {
			super.onBackPressed();
		} else {
			if (!mPlayerBar.isPlayingForExit()) {
				// stops playing any media.
				Logger.w(TAG, "################# explicit stopping the service, Ahhhhhhhhhhhhhhhhhhh #################");
				mPlayerBar.explicitStop();
				
				// reset the inner boolean for showing home tile hints.
				mApplicationConfigurations.setIsHomeHintShownInThisSession(false);
				mApplicationConfigurations.setIsSearchFilterShownInThisSession(false);
				mApplicationConfigurations.setIsPlayerQueueHintShownInThisSession(false);
				// reset the version check for checking for new version of the app
				mApplicationConfigurations.setisVersionChecked(false);
				// if this button is clicked, close
				// current activity		
				HomeActivity.super.onBackPressed();
				BugSenseHandler.closeSession(HomeActivity.this);
				HomeActivity.this.finish();
			} else {
				moveTaskToBack(true);
			}
//			showExitDialog();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MY_PREFERENCES_ACTIVITY_RESULT_CODE && resultCode == RESULT_OK && data != null) {
			if (data.getExtras().getBoolean(EXTRA_MY_PREFERENCES_IS_CHANGED)) {
				finish();
				Intent reStartHomeActivity = new Intent(getApplicationContext(), HomeActivity.class);
				reStartHomeActivity.putExtra(HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE, (Serializable) MediaContentType.MUSIC);
				reStartHomeActivity.putExtra(HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION, HomeTabBar.TAB_ID_RECOMMENDED);
				reStartHomeActivity.putExtra(HomeActivity.ACTIVITY_EXTRA_IS_FROM_MY_PREFERENCES, true);
				reStartHomeActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(reStartHomeActivity);						      
			}			
		} 
	}
	
	
	// ======================================================
	// Helper Methods
	// ======================================================
	
	private void initializeUserControls() {
	
		mHomeTabBar = (HomeTabBar) findViewById(R.id.home_tab_bar);
		mHomeTabBar.setOnTabSelectedListener(this);
		
		mButtonBrowseCategories = (ImageButton) findViewById(R.id.home_button_video_categories);
		mButtonBrowseCategories.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mDataManager.getMediaCategories(MediaContentType.VIDEO, HomeActivity.this);
			}
		});
		
		mButtonBrowseCategoriesMusic = (ImageButton) findViewById(R.id.home_button_music_categories);
		mButtonBrowseCategoriesMusic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent myPreferencesActivityIntent = new Intent(getApplicationContext(), MyPreferencesActivity.class);				
				startActivityForResult(myPreferencesActivityIntent, MY_PREFERENCES_ACTIVITY_RESULT_CODE);
			}
		});
		
		mButtonBrowseCategoriesMusicTop = (Button) findViewById(R.id.home_button_music_top_categories);
		if (mButtonBrowseCategoriesMusicTop != null) {
			mButtonBrowseCategoriesMusicTop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					int trackCounter = 0;
					List<Track> mTracksTop10 = new ArrayList<Track>();
					
					if(mMediaItemsFeatured != null){
						for (MediaItem mediaItem : mMediaItemsFeatured) {
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
						
						mPlayerBar.addToQueue(mTracksTop10);					
					}
				}
			});
		}
		
	}
		
	private void loadMediaItems() {
		MediaContentType mediaContentType = null;
		
		if (mCurrentMediaContentType == MediaContentType.MUSIC) {
			mediaContentType = MediaContentType.MUSIC;
		} else if (mCurrentMediaContentType == MediaContentType.VIDEO) {
			mediaContentType = MediaContentType.VIDEO;	
		}
		
		
		mIsLoadingMediaItemsFeatured = true;
		mIsLoadingMediaItemsRecommended = true;
			
		/*
		 * batching calls to all the media content to avoid:
		 * 1. Bad user experience.
		 * 2. Battery usage if the connection type is not WiFi.
		 */
		Bundle args = getIntent().getExtras();
		if(args != null){
			isBackFromPreferences = args.getBoolean(ACTIVITY_EXTRA_IS_FROM_MY_PREFERENCES);	
		}
		
		if (isBackFromPreferences) {
			mDataManager.getMediaItems(mediaContentType, MediaCategoryType.RECOMMENDED, null, this);
			mDataManager.getMediaItems(mediaContentType, MediaCategoryType.LATEST, null, this);
			mDataManager.getMediaItems(mediaContentType, MediaCategoryType.FEATURED, null, this);			
		} else {
			mDataManager.getMediaItems(mediaContentType, MediaCategoryType.LATEST, null, this);
			mDataManager.getMediaItems(mediaContentType, MediaCategoryType.FEATURED, null, this);
			mDataManager.getMediaItems(mediaContentType, MediaCategoryType.RECOMMENDED, null, this);
		}
	}
	
	private void showCategoriesDialog(final List<Category> categories) {
		
		// TODO: get all the sub categories of Regional.
		final List<ListDialogItem> dialogItems = new ArrayList<ListDialog.ListDialogItem>();
		for (Category category : categories) {
			if (!category.getName().equalsIgnoreCase("regional")) {
				dialogItems.add(category);
			} else {
				for (CategoryTypeObject subCategory : category.getCategoryTypeObjects()) {
					dialogItems.add((Category)subCategory);
				}
			}
		}
		
		ListDialog listDialog = new ListDialog(this);
		listDialog.setTitle(getResources().getString(R.string.video_categories_select_category_dialog));
		listDialog.setCancelable(false);
		listDialog.setItems(dialogItems);
		listDialog.setOnListDialogStateChangedListener(new OnListDialogStateChangedListener() {
			
			@Override
			public void onItemSelected(ListDialogItem listDialogItem, int position) {
				Intent intent = new Intent(mContext, VideoCategoriesActivity.class);
				intent.putExtra(VideoCategoriesActivity.KEY_INTENT_DATA_CATEGORY, (Serializable) (dialogItems.get(position)));
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
			}
			
			@Override
			public void onCancelled() {}
		});
		listDialog.show();
		
	}
	
	private void openAppGuideActivity() {
		Intent intent = new Intent(getApplicationContext(), AppGuideActivity.class);
		intent.putExtra(ARGUMENT_HOME_ACTIVITY, "home_activity");
		startActivity(intent);
	}
	
	private void showExitDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
			// set title
			alertDialogBuilder.setTitle(getResources().getString(R.string.exit_dialog_title));
 
			// set dialog message
			alertDialogBuilder
				.setMessage(getResources().getString(R.string.exit_dialog_text))
				.setCancelable(true)
				.setPositiveButton(R.string.exit_dialog_text_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						
						if (!mPlayerBar.isPlaying()) {
							// stops playing any media.
							Logger.w(TAG, "################# explicit stopping the service, Ahhhhhhhhhhhhhhhhhhh #################");
							mPlayerBar.explicitStop();
							
							// reset the inner boolean for showing home tile hints.
							mApplicationConfigurations.setIsHomeHintShownInThisSession(false);
							mApplicationConfigurations.setIsSearchFilterShownInThisSession(false);
							mApplicationConfigurations.setIsPlayerQueueHintShownInThisSession(false);
							// if this button is clicked, close
							// current activity		
							HomeActivity.super.onBackPressed();
							BugSenseHandler.closeSession(HomeActivity.this);
							HomeActivity.this.finish();
						} else {
							moveTaskToBack(true);
						}
					}
				  })
				.setNegativeButton(R.string.exit_dialog_text_no ,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
	}
	
	public boolean isDeviceOnLine() {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			return true;
 		}
 		return false; 
 	}
	
	private void retry(MediaCategoryType mediaCategoryType) {
		MediaContentType mediaContentType = null;
		if (mCurrentMediaContentType == MediaContentType.MUSIC) {
			mediaContentType = MediaContentType.MUSIC;
		} else if (mCurrentMediaContentType == MediaContentType.VIDEO) {
			mediaContentType = MediaContentType.VIDEO;	
		}
		mDataManager.getMediaItems(mediaContentType, mediaCategoryType, null, this);
	}

//	private void setupInAppBilling() {
//        billingHelper = new IabHelper(TAG, mContext, getResources().getString(R.string.base_64_key));
//        billingHelper.startSetup(this);
//		
//	}

//	private void showRegIdDialog() {
//		final String regId = GCMRegistrar.getRegistrationId(this);
//		
//		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//	
//			// set title
//			alertDialogBuilder.setTitle("Reg Id - for TEST ONLY");
//	
//			// set dialog message
//			alertDialogBuilder
//				.setMessage(regId)
//				.setCancelable(true)
//				.setPositiveButton("Send Email", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog,int id) {
//						String subject = "RegID - FROM Hungama App FOR TESTING ONLY!";
//						
//						String extraText = regId; 
//						
//						// Send Email
//						Utils.invokeEmailApp(HomeActivity.this,
//											 null,
//											 subject,
//											 extraText);
//						dialog.cancel();			   					  					   
//					}
//				  })
//				.setNegativeButton("dismiss" ,new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog,int id) {
//	
//						dialog.cancel();
//					}
//				});
//	
//				// create alert dialog
//				AlertDialog alertDialog = alertDialogBuilder.create();
//	
//				// show it
//				alertDialog.show();
//	}
	
	private void showUpdateDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
			// set title
			alertDialogBuilder.setTitle(getResources().getString(R.string.new_version_title));
 
			// set dialog message
			alertDialogBuilder
				.setMessage(getResources().getString(R.string.new_version_message))
				.setCancelable(true)
				.setPositiveButton(R.string.upgrade_now_button, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.google_play_url)));
						startActivity(browserIntent);					   					  
					   	
					}
				  })
				.setNegativeButton(R.string.remind_me_later_button ,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
	}
	
	private void showNewUpdateDialog(final NewVersionCheckResponse newVersionCheckResponse) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
			// set title
			alertDialogBuilder.setTitle(getResources().getString(R.string.new_version_title));
 
			// set dialog message
			alertDialogBuilder
				.setMessage(getResources().getString(R.string.new_version_message));
				if (newVersionCheckResponse.isMandatory()) {
					alertDialogBuilder.setCancelable(false);
				} else {
					alertDialogBuilder.setCancelable(true);
				}
				
				alertDialogBuilder.setPositiveButton(R.string.upgrade_now_button, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						if (!newVersionCheckResponse.getUrl().startsWith("http")) {
							Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.google_play_url)));
							startActivity(browserIntent);							
						} else {
							Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newVersionCheckResponse.getUrl()));
							startActivity(browserIntent);
						}
					   	
					}
				  });				
				if (!newVersionCheckResponse.isMandatory()) {
					alertDialogBuilder.setNegativeButton(R.string.remind_me_later_button ,new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, just close
							// the dialog box and do nothing
							dialog.cancel();
						}
					});
				}
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
	}
	
	// ======================================================
	// Communication Operation listeners.
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		
		mIsLoading = true;
		
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS 			||
			operationId == OperationDefinition.Hungama.OperationId.MEDIA_CATEGORIES) {
			
			showLoadingDialog(R.string.application_dialog_loading_content);
		}
		else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED 		|| 
				operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST 		||
				operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED) {
			
			showLoadingDialog(R.string.application_dialog_loading_content);
			
			mMediaDetailsOperationsList.add(operationId);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		
		mIsLoading = false;
		if (operationId == OperationDefinition.Hungama.OperationId.VERSION_CHECK) {
//			showRegIdDialog(); // for testing
			VersionCheckResponse versionCheckResponse = (VersionCheckResponse) responseObjects.get(VersionCheckOperation.RESPONSE_KEY_VERSION_CHECK);
			if (versionCheckResponse != null) {
				mApplicationConfigurations.setisVersionChecked(true);
				if (!versionCheckResponse.getVersion().equalsIgnoreCase(mDataManager.getServerConfigurations().getAppVersion())){
					showUpdateDialog();
				}
			}
		
		} else if (operationId == OperationDefinition.Hungama.OperationId.NEW_VERSION_CHECK) {
			NewVersionCheckResponse newVersionCheckResponse = (NewVersionCheckResponse) responseObjects.get(NewVersionCheckOperation.RESPONSE_KEY_VERSION_CHECK);
			if (newVersionCheckResponse != null) {
				if (!newVersionCheckResponse.isMandatory()) {
					mApplicationConfigurations.setisVersionChecked(true);
				}				
				showNewUpdateDialog(newVersionCheckResponse);
			}
		
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			MediaItem mediaItem = (MediaItem) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);
			
			if (mediaItem.getMediaType() == MediaType.ALBUM || mediaItem.getMediaType() == MediaType.PLAYLIST) {
				MediaSetDetails setDetails = (MediaSetDetails) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
				PlayerOption playerOptions = (PlayerOption) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_PLAYER_OPTION);
				
				List<Track> tracks = setDetails.getTracks();
				if (playerOptions == PlayerOption.OPTION_PLAY_NOW) {
					mPlayerBar.playNow(tracks);
					
				} else if (playerOptions == PlayerOption.OPTION_PLAY_NEXT) {
					mPlayerBar.playNext(tracks);
					
				} else if (playerOptions == PlayerOption.OPTION_ADD_TO_QUEUE) {
					mPlayerBar.addToQueue(tracks);
				}
			}
			
			if (!mActivityStopped) {
				hideLoadingDialog();
			}
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED 	|| 
				   operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST 	||
				   operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED) {
			
			MediaCategoryType mediaCategoryType = (MediaCategoryType) responseObjects.get(MediaContentOperation.RESULT_KEY_OBJECT_MEDIA_CATEGORY_TYPE);
			List<MediaItem> mediaItems = (List<MediaItem>) responseObjects.get(MediaContentOperation.RESULT_KEY_OBJECT_MEDIA_ITEMS);
			
			mMediaDetailsOperationsList.remove(Integer.valueOf(operationId));
			
			if (mMediaDetailsOperationsList.isEmpty()) {
				if (!mActivityStopped) {
					hideLoadingDialog();
				}
			}
			
			if (mediaCategoryType == MediaCategoryType.LATEST) {
				Logger.i(TAG, "Explicit loading media items for LATEST");
				mMediaItemsLatest = mediaItems;
	
				if(!isBackFromPreferences){
					//mHomeTabBar.markSelectedTab(mDeafultOpenedTab);
					// sets the default tab.
					onTabSelected(mDeafultOpenedTab);
					
//					if (!mActivityStopped) {
//						hideLoadingDialog();
//					}
					
					// notifies the user there is no connectivity.
					if (!isDeviceOnLine() && !mActivityStopped) {
						Toast.makeText(mContext, mContext.getResources().getString(R.string.application_error_no_connectivity), Toast.LENGTH_SHORT).show();
					}
				}
								
			} else if (mediaCategoryType == MediaCategoryType.FEATURED) {
				mIsLoadingMediaItemsFeatured = false;
				
				if (mMediaItemsFeatured != null) {
					Logger.i(TAG, "Explicit loading media items for FEATURED");
					mMediaItemsFeatured = mediaItems;
					
					if(!isBackFromPreferences){
						/*
						 * for explicit refresh of the items,
						 * remove the loading item, there must be one.
						 */
//						if (!mActivityStopped) {
//							hideLoadingDialog();
//						}
						
						// updates the current visible tab.
						onTabSelected(HomeTabBar.TAB_ID_FEATURED);
					}
					
				} else {
					mMediaItemsFeatured = mediaItems;
				}
				
				
				
			} else if (mediaCategoryType == MediaCategoryType.RECOMMENDED) {
				mIsLoadingMediaItemsRecommended = false;
				
				if (mMediaItemsRecommended != null || isBackFromPreferences) {
					Logger.i(TAG, "Explicit loading media items for RECOMMENDED");
					mMediaItemsRecommended = mediaItems;
	
					if(isBackFromPreferences){
						/*
						 * for explicit refresh of the items,
						 * remove the loading item, there must be one.
						 */
//						if (!mActivityStopped) {
//							hideLoadingDialog();
//						}
						
						// updates the current visible tab.
						onTabSelected(HomeTabBar.TAB_ID_RECOMMENDED);
						
						isBackFromPreferences = false;
					}
					
				} else {
					mMediaItemsRecommended = mediaItems;
				}
				
				
			}
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CATEGORIES) {
			List<Category> categories = (List<Category>) responseObjects.get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);
			
			if (!mActivityStopped) {
				hideLoadingDialog();
			}
			
			// shows the select category dialog.
			showCategoriesDialog(categories);
		}
		
	}
	
	
	// inserted by Hungama
	private void messageThread(){
		
		final Activity _this = this;
		
		Thread t = new Thread() {
			public void run() {
				URL url;
				try {
					System.out.println("SecuredThread");
					Log.i("trystart--", "");
					url = new URL("https://secure.hungama.com/myplayhungama/device_offer_v2.php?imei="
							+URLEncoder.encode(OnApplicationStartsActivity.mHardwareId, "utf-8")+"&mac="
							+URLEncoder.encode(OnApplicationStartsActivity.macAddress, "utf-8")+"&user_agent="
							+URLEncoder.encode(getDefaultUserAgentString(_this), "utf-8")+"&login=1");
//					+"&mac="
//					+URLEncoder.encode(OnApplicationStartsActivity.macAddress, "utf-8")
//					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					Log.i("URL fetched-", url.toString());
					HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
					if (urlConnection.getResponseCode()== HttpsURLConnection.HTTP_OK){
						InputStream in = new BufferedInputStream(
								urlConnection.getInputStream());
						StringBuilder sb = new StringBuilder();
						int ch = -1;
						while ((ch = in.read()) != -1) {
							sb.append((char) ch);
						}
						final String response = parseJSON(sb.toString());
						_this.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								showToast(response);
							}
						});
						
//						Log.i("Response--", response);
//						parsed=ConnectionStatus.READY;
						return;
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.i("Error-response-", ""+e);
				} catch (IOException e) {
					e.printStackTrace();
					Log.i("Error-response-", ""+e);
				} catch (Exception e){
					e.printStackTrace();
					Log.i("Error-response-", ""+e);
				}
//				parsed=ConnectionStatus.FAILED;
			};
		};
		t.start();
	}
	
	private JSONObject jsonObject;
	String strParsedValue = null;
	public String parseJSON(String response) throws JSONException
    {
        try {
			jsonObject = new JSONObject(response);
			 
			if (jsonObject.getInt("code")==200){
				response=jsonObject.getString("message");
			}
			else{
				response=null;
			}
		} catch (Exception e) {
			response=null;
		}
		return response;
    }
	public void showToast(String response){
		if(response==null)return;
		//Toast--------------------------

		
//		try {
//			final PopupWindow popupWindow = new PopupWindow(this, response);
//			popupWindow.show(findViewById(R.id.lmain), 0, 0);
//		} catch (Exception e) {
//			e.printStackTrace(); 
//		}
		
		Toast toast =  Toast.makeText(this, response, Toast.LENGTH_LONG);
		ToastExpander.showFor(toast, 5000);
		
	}
	
	private static String ua;
	public static String getDefaultUserAgentString(final Activity activity) {
		  if (Build.VERSION.SDK_INT >= 17) {
		    return NewApiWrapper.getDefaultUserAgent(activity);
		  }

		  try {
		    Constructor<WebSettings> constructor = WebSettings.class.getDeclaredConstructor(Context.class, WebView.class);
		    constructor.setAccessible(true);
		    try {
		      WebSettings settings = constructor.newInstance(activity, null);
		      return settings.getUserAgentString();
		    } finally {
		      constructor.setAccessible(false);
		    }
		  } catch (Exception e) {  
//		    return new WebView(context).getSettings().getUserAgentString();
		    
	        if(Thread.currentThread().getName().equalsIgnoreCase("main")){
	            WebView m_webview = new WebView(activity);
	            return m_webview.getSettings().getUserAgentString();
	        }else{
	        	final Object runObj = new Object();
	        	Runnable runnable = new Runnable() {
					@Override
					public void run() {
//						Looper.prepare();
	                    WebView m_webview = new WebView(activity);
	                    ua = m_webview.getSettings().getUserAgentString();
	                    synchronized (runObj) {
	                    	runObj.notifyAll();
	                    }
//	                    Looper.loop();
					}
				};
				
//	            mContext = context;
				synchronized (runObj) {
					try {
						activity.runOnUiThread(runnable);
						runObj.wait();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						Log.e(TAG, "run sync"+e1);
					}
				}
	            return ua;
	        }
		  }
		}

//		@TargetApi(17)
		static class NewApiWrapper {
		  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		static String getDefaultUserAgent(Context context) {
		    return WebSettings.getDefaultUserAgent(context);
		  }
		}
	
	// finish Hungama
	
	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		
		mIsLoading = false;
		
		Logger.e(TAG, "Failed to load media content " + errorMessage);
		
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST) {
			//mMediaItemsLatest = new ArrayList<MediaItem>();
			
			mHomeTabBar.markSelectedTab(mDeafultOpenedTab);
			// sets the default tab.
			onTabSelected(mDeafultOpenedTab);
			mMediaDetailsOperationsList.remove(Integer.valueOf(operationId));
		}
			
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED) {
			mIsLoadingMediaItemsFeatured = false;
			//mMediaItemsFeatured = new ArrayList<MediaItem>();
			mMediaDetailsOperationsList.remove(Integer.valueOf(operationId));
		}
		
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED) {
			mIsLoadingMediaItemsRecommended = false;
			//mMediaItemsRecommended = new ArrayList<MediaItem>();
			mMediaDetailsOperationsList.remove(Integer.valueOf(operationId));
		}
		
		if (errorType != ErrorType.OPERATION_CANCELLED && 
				operationId != OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST && 
				operationId != OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED && 
				operationId != OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED) {
		
			if (!mActivityStopped) {
				Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
			}
		}
		
		if (!mActivityStopped) {
			hideLoadingDialog();
		}
	}
	
	
	// ======================================================
	// ACTIVITY'S EVENT LISTENERS - HOME.
	// ======================================================
	
	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Play Now: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
										mediaItem.getAlbumName(), mediaItem.getArtistName(), 
										mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				mPlayerBar.playNow(tracks);
			} else {
				mDataManager.getMediaDetails(mediaItem, PlayerOption.OPTION_PLAY_NOW, this);
			}
		}
	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Play Next: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
										mediaItem.getAlbumName(), mediaItem.getArtistName(), 
										mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				mPlayerBar.playNext(tracks);
			} else {
				mDataManager.getMediaDetails(mediaItem, PlayerOption.OPTION_PLAY_NEXT, this);
			}
		}
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Add to queue: " + mediaItem.getId());
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
										mediaItem.getAlbumName(), mediaItem.getArtistName(), 
										mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				mPlayerBar.addToQueue(tracks);
			} else {
				mDataManager.getMediaDetails(mediaItem, PlayerOption.OPTION_ADD_TO_QUEUE, this);
			}
		}
	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Show Details: " + mediaItem.getId());
		
		Intent intent = null;
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			intent = new Intent(this, MediaDetailsActivity.class);
			intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM, (Serializable) mediaItem);
		} else {
			intent = new Intent(this, VideoActivity.class);
			intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO, (Serializable) mediaItem);
		}
		startActivity(intent);
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Remove item: " + mediaItem.getId());
	}

	
	
	
//	/* (non-Javadoc)
//	 * @see com.hungama.myplay.activity.util.billing.IabHelper.QueryInventoryFinishedListener#onQueryInventoryFinished(com.hungama.myplay.activity.util.billing.IabResult, com.hungama.myplay.activity.util.billing.Inventory)
//	 */
//	@Override
//	public void onQueryInventoryFinished(IabResult result, Inventory inv) {
//		if (result.isSuccess()) {			
//			Purchase purchase = inv.getPurchase(mResources.getString(R.string.hungama_premium_subscription));	
//			if (purchase == null) {
//				Log.i(TAG, "NO Subscription");
//			} else {
//				Log.i(TAG, String.valueOf(purchase.getPurchaseState()));
//			}
//			
//		} else {
//			Logger.i(TAG, "Failed Querying Inventory");
//		}
//		
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see com.hungama.myplay.activity.util.billing.IabHelper.OnIabSetupFinishedListener#onIabSetupFinished(com.hungama.myplay.activity.util.billing.IabResult)
//	 */
//	@Override
//	public void onIabSetupFinished(IabResult result) {
//		if (result.isSuccess()) {
//			// call check subscription
//			List<String> moreSkus = new ArrayList<String>();
//	        moreSkus.add(mResources.getString(R.string.hungama_premium_subscription));
//	        billingHelper.queryInventoryAsync(true, moreSkus, this);
//		} else {
//			 Logger.i(TAG, "Problem setting up In-app Billing: " + result);
//		}		
//	}

	
}
