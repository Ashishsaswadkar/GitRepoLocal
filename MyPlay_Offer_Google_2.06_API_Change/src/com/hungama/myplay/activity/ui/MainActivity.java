package com.hungama.myplay.activity.ui;

import java.io.Serializable;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.campaigns.ForYouActivity;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.gigya.InviteFriendsActivity;
import com.hungama.myplay.activity.ui.dialogs.DiscoverListDialog;
import com.hungama.myplay.activity.ui.dialogs.ListDialog.ListDialogItem;
import com.hungama.myplay.activity.ui.dialogs.ListDialog.OnListDialogStateChangedListener;
import com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment;
import com.hungama.myplay.activity.ui.fragments.LoadingDialogFragment;
import com.hungama.myplay.activity.ui.fragments.MainSearchFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.fragments.QuickNavigationFragment;
import com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment.OnGlobalMenuItemSelectedListener;
import com.hungama.myplay.activity.ui.fragments.QuickNavigationFragment.OnQuickNavigationItemSelectedListener;
import com.hungama.myplay.activity.ui.widgets.HomeTabBar;
import com.hungama.myplay.activity.util.Appirater;
import com.hungama.myplay.activity.util.Logger;


/**
 * The Main Activity of the application, supports handling the ActionBar and the Player Bar.
 */
public abstract class MainActivity extends SherlockFragmentActivity implements 
											OnGlobalMenuItemSelectedListener, 
											OnQuickNavigationItemSelectedListener {
	
	private static final String TAG = "MainActivity";
	
	protected static final String FRAGMENT_TAG_HOME = "fragment_tag_home";
	protected static final String FRAGMENT_TAG_MAIN_GLOBAL_MENU = "fragment_tag_main_global_menu";
	protected static final String FRAGMENT_TAG_MAIN_QUICK_NAVIGATION = "fragment_tag_main_quick_navigation";
	protected static final String FRAGMENT_TAG_MAIN_SEARCH = "fragment_tag_main_search";
	
	protected Appirater mAppirater;
	
	private FragmentManager mFragmentManager;
	private ActionBar mActionBar;
	private Menu mMenu;
	private MenuItem mLastSelectedMenuItem;
	private PlayerBarFragment mPlayerBarFragment = null;
	
	private boolean mIsResumed = false;
	
	/*
	 * Manages all the Quick Navigation Current / Selected Item.
	 */
	private static NavigationItem mCurrentNavigationItem = null;
	private static NavigationItem mLastNavigationItem = null;
	
	private volatile boolean mIsDestroyed = false; 
	
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	// ======================================================
	// ACTIVITY'S LIFECYCLE.
	// ======================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// initializes internal components.
		mFragmentManager = getSupportFragmentManager();
		// for rating the app
		mAppirater = new Appirater(this);

		// some GUI stuff initialization.
		// creates the action bar.
		mActionBar = getSupportActionBar();
		mActionBar.setIcon(R.drawable.icon_actionbar_logo);
		mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_actionbar));
		
		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		/*
		 * Updates the current navigation item here
		 * to make it most up - to - date when switching between sections. 
		 */
		mLastNavigationItem = mCurrentNavigationItem;
		// sets the current quick navigation item to be selected.
		mCurrentNavigationItem = getNavigationItem();
		if (mCurrentNavigationItem == null) {
			mCurrentNavigationItem = NavigationItem.MUSIC;
		}
		if (mCurrentNavigationItem == NavigationItem.OTHER && mLastNavigationItem != null) {
			mActionBar.setTitle(mLastNavigationItem.title);
		} else {
			mActionBar.setTitle(mCurrentNavigationItem.title);
		}
		
		/*
		 * Updates the flags of the navigation items,
		 */
		if (mCurrentNavigationItem == NavigationItem.MUSIC || 
				mCurrentNavigationItem == NavigationItem.VIDEOS) {
			mLastNavigationItem = null;
		}
		
		// sets the "Up" button.
		if (mLastNavigationItem != null) {
 	 		mActionBar.setDisplayHomeAsUpEnabled(true);
		} else {
			mActionBar.setDisplayHomeAsUpEnabled(false);
		}
		
		FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mIsResumed = true;
	}
	
	@Override
	protected void onPause() {
		mIsResumed = false;
		
		super.onPause();
		// disables any animations when pressing "Back" button.
		overridePendingTransition(0, 0);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	protected void onDestroy() {
		mIsDestroyed = true;
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == HomeActivity.MY_PREFERENCES_ACTIVITY_RESULT_CODE && resultCode == RESULT_OK && data != null) {
			if (data.getExtras().getBoolean(HomeActivity.EXTRA_MY_PREFERENCES_IS_CHANGED)) {
				finish();
				Intent reStartHomeActivity = new Intent(getApplicationContext(), HomeActivity.class);
				reStartHomeActivity.putExtra(HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE, (Serializable) MediaContentType.MUSIC);
				reStartHomeActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(reStartHomeActivity);						      
			}			
		} 
	}
	
	// ======================================================
	// ACTION BAR'S LISTENERS.
	// ======================================================
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.menu_main_actionbar, menu);
	    // storing a reference to the action bar's menu.
	    mMenu = menu;
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		
		// when the "UP" button was clicked.
		if (itemId == android.R.id.home) {
			
			Intent startEntryActivity = null;
			if (mLastNavigationItem != null && 
					(mLastNavigationItem == NavigationItem.MUSIC || mLastNavigationItem == NavigationItem.VIDEOS)) {
				
				if (mLastNavigationItem == NavigationItem.MUSIC) {
					// resets the flag.
					mLastNavigationItem = null;
					// sets properties to launch the last entry activity.
					startEntryActivity = new Intent(getApplicationContext(), HomeActivity.class);
					startEntryActivity.putExtra(HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE, (Serializable) MediaContentType.MUSIC);
					
				} else if (mLastNavigationItem == NavigationItem.VIDEOS) {
					// resets the flag.
					mLastNavigationItem = null;
					// sets properties to launch the last entry activity.
					startEntryActivity = new Intent(getApplicationContext(), HomeActivity.class);
					startEntryActivity.putExtra(HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE, (Serializable) MediaContentType.VIDEO);
				}
				
			} else {
				// sets properties to launch the last entry activity.
				startEntryActivity = new Intent(getApplicationContext(), HomeActivity.class);
				startEntryActivity.putExtra(HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE, (Serializable) MediaContentType.MUSIC);
			}
			 
			startEntryActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(startEntryActivity);
			
			return true;
		}
		
		// close the last selected item if it's not the new one.
		if (mLastSelectedMenuItem != null && 
				mLastSelectedMenuItem.getItemId() != item.getItemId()) {
			closeFragmentOfMenuItem(mLastSelectedMenuItem);
			mLastSelectedMenuItem.setChecked(false);
			// colors back the item's background to transparent.
			View view = findViewById(mLastSelectedMenuItem.getItemId());
			if (view != null) {
				view.setBackgroundResource(R.drawable.transparent_background);
			}
		}
		mLastSelectedMenuItem = item;

		// toggle any other selected
		if (itemId == R.id.menu_item_main_actionbar_navigation) {
			// toggles the navigation visibility.
			if (item.isChecked()) {
				closeQuickNavigation();
				item.setIcon(R.drawable.background_actionbar_plus);
				item.setChecked(false);
				// colors back the quick navigation background to transparent. 
				View view = findViewById(itemId);
				view.setBackgroundResource(R.drawable.transparent_background);
				
			} else {
				openQuickNavigation();
				item.setIcon(R.drawable.background_actionbar_minus);
				item.setChecked(true);
				// colors the search background. 
				View view = findViewById(itemId);
				view.setBackgroundResource(R.drawable.action_bar_selected_item_background_quick_navigation);
				
				closePlayerBarContent();
			}
			
			return true;
			
		} else if (itemId == R.id.menu_item_main_actionbar_search) {
			// toggles the search visibility.
			if (item.isChecked()) {
				closeMainSearch();
				item.setChecked(false);
				// colors back the search background to transparent. 
				View view = findViewById(itemId);
				view.setBackgroundResource(R.drawable.transparent_background);
				
			} else {
				openMainSearch(FRAGMENT_TAG_MAIN_SEARCH);
				item.setChecked(true);
				// colors the search background. 
				View view = findViewById(itemId);
				view.setBackgroundResource(R.drawable.action_bar_selected_item_background_search);
				
				closePlayerBarContent();
			}
			
			return true;
			
		} else if (itemId == R.id.menu_item_main_actionbar_settings) {
			// toggles the setting visibility.
			if (item.isChecked()) {
				closeGlobalMenu();
				item.setChecked(false);
				// colors back the global settings background to transparent. 
				View view = findViewById(itemId);
				view.setBackgroundResource(R.drawable.transparent_background);
				
			} else {
				openGlobalMenu();
				item.setChecked(true);
				// colors the global settings background. 
				View view = findViewById(itemId);
				view.setBackgroundResource(R.drawable.action_bar_selected_item_background_global_menu);
				
				closePlayerBarContent();
			}
			
			return true;
		
		} else if (itemId == R.id.menu_item_main_actionbar_settings_menu) {
			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(intent);
			return true;
		
		} else if (itemId == R.id.menu_item_main_actionbar_my_profile) {
			Intent profileActivityIntent = new Intent(getApplicationContext(), ProfileActivity.class);
			startActivity(profileActivityIntent);
			return true;
		
		} else if (itemId == R.id.menu_item_main_actionbar_preferences) {
			Intent myPreferencesActivityIntent = new Intent(getApplicationContext(), MyPreferencesActivity.class);
			startActivityForResult(myPreferencesActivityIntent, HomeActivity.MY_PREFERENCES_ACTIVITY_RESULT_CODE);
			return true;
		
		} else if (itemId == R.id.menu_item_main_actionbar_help_faq) {
			Intent helpAndFaqActivityIntent = new Intent(getApplicationContext(), HelpAndFAQActivity.class);
			startActivity(helpAndFaqActivityIntent);
			return true;
			
		} else if (itemId == R.id.menu_item_main_actionbar_rate_app) {
			mAppirater.rateAppClick();
			return true;
		
		} else if (itemId == R.id.menu_item_main_actionbar_invite_friends) {
			Intent inviteFriendsActivity = new Intent(this, InviteFriendsActivity.class);
			startActivity(inviteFriendsActivity);
			return true;
		
		} 		
		
		return false;
	}
	
	
	// ======================================================
	// ACTIVITY'S EVENT LISTENERS.
	// ======================================================
	
	@Override
	public void onBackPressed() {
		Logger.d(TAG, "Back button was pressesd, closing any opened, Action bar menu item.");
		// changes only the last menu item's checking state.
		if (mLastSelectedMenuItem != null) {
			/*
			 * Resets the last state of the menu item.
			 */
			if (mLastSelectedMenuItem.getItemId() == R.id.menu_item_main_actionbar_navigation) {
				mLastSelectedMenuItem.setIcon(R.drawable.background_actionbar_plus);
			}
			mLastSelectedMenuItem.setChecked(false);
			View view = findViewById(mLastSelectedMenuItem.getItemId());
			if (view != null) {
				view.setBackgroundResource(R.drawable.transparent_background);
			}
			
			// sets it to null to indicate that there is no ActionBar's menu item opened.
			mLastSelectedMenuItem = null;
		}
		
		super.onBackPressed();
	}
		
	@Override
	public void onGlobalMenuItemSelected(int menuItemId) {
		
		// closes the main settings first.
		MenuItem settingItem = mMenu.findItem(R.id.menu_item_main_actionbar_settings);
		settingItem.setChecked(false);
		// colors back the item's background to transparent.
		View view = findViewById(settingItem.getItemId());
		view.setBackgroundResource(R.drawable.transparent_background);
		
		closeGlobalMenu();
		
		// selects which item to select.
		switch (menuItemId) {
		
		/*
		 * Quick Link.
		 */
		case GlobalMenuFragment.MENU_ITEM_SPECIALS:
			Intent forYouActivityIntent = new Intent(getApplicationContext(), ForYouActivity.class);
			startActivity(forYouActivityIntent);
			
			FlurryAgent.logEvent("Specials");
			
			break;
			
		case GlobalMenuFragment.MENU_ITEM_APP_TOUR:
			Intent appTourActivityIntent = new Intent(getApplicationContext(), AppTourActivity.class);
			startActivity(appTourActivityIntent);
			break;
			
		case GlobalMenuFragment.MENU_ITEM_MUSIC:
			Intent startHomeActivityMusic = new Intent(getApplicationContext(), HomeActivity.class);
			startHomeActivityMusic.putExtra(HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE, (Serializable) MediaContentType.MUSIC);
			startHomeActivityMusic.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(startHomeActivityMusic);
			break;
		
		case GlobalMenuFragment.MENU_ITEM_VIDEOS:
			Intent startHomeActivity = new Intent(getApplicationContext(), HomeActivity.class);
			startHomeActivity.putExtra(HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE, (Serializable) MediaContentType.VIDEO);
			startHomeActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(startHomeActivity);
			break;
			
		case GlobalMenuFragment.MENU_ITEM_LIVE_RADIO:
			Intent startRadioIntent = new Intent(getApplicationContext(), RadioActivity.class);
			startRadioIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(startRadioIntent);
			break;
			
		case GlobalMenuFragment.MENU_ITEM_DISCOVER:
			Intent startDiscoveryIntent = new Intent(getApplicationContext(), DiscoveryActivity.class);
			startDiscoveryIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(startDiscoveryIntent);
			break;
			
		case GlobalMenuFragment.MENU_ITEM_MY_STREAM:
			/*
			 * My Stream is a section inside the Home Screen for Music and Video
			 * If the Current Section is Music or Video, requests to show the 
			 * My Stream tab, if not launch the Home Activity For Music with the 
			 * My Stream tab opened. 
			 */
			if (mCurrentNavigationItem == NavigationItem.MUSIC || mCurrentNavigationItem == NavigationItem.VIDEOS) {
				// shows the my stream.
				if (this instanceof HomeActivity) {
					HomeActivity homeActivity = (HomeActivity) this;
					homeActivity.setCurrentTab(HomeTabBar.TAB_ID_MY_STREAM);
				}
			} else {
				// calls the Home Activity with Music content and the My Stream tab opened.
				Intent startHomeActivityMySrteam = new Intent(getApplicationContext(), HomeActivity.class);
				startHomeActivityMySrteam.putExtra(HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE, (Serializable) MediaContentType.MUSIC);
				startHomeActivityMySrteam.putExtra(HomeActivity.ACTIVITY_EXTRA_DEFAULT_OPENED_TAB_POSITION, HomeTabBar.TAB_ID_MY_STREAM);
				startHomeActivityMySrteam.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				
				startActivity(startHomeActivityMySrteam);
			}
			break;
			
		/*
		 * My Play.
		 */
		case GlobalMenuFragment.MENU_ITEM_MY_PROFILE:
			Intent profileActivityIntent = new Intent(getApplicationContext(), ProfileActivity.class);
			startActivity(profileActivityIntent);
			break;
			
		case GlobalMenuFragment.MENU_ITEM_MY_COLLECTIONS:
			Intent myCollectionActivityIntent = new Intent(getApplicationContext(), MyCollectionActivity.class);
			startActivity(myCollectionActivityIntent);
			break;
			
		case GlobalMenuFragment.MENU_ITEM_MY_FAVORITES:
			Intent favoritesActivityIntent = new Intent(getApplicationContext(), FavoritesActivity.class);
			startActivity(favoritesActivityIntent);
			break;
			
		case GlobalMenuFragment.MENU_ITEM_MY_PLAYLISTS:
			Intent playlistsActivityIntent = new Intent(getApplicationContext(), PlaylistsActivity.class);
			startActivity(playlistsActivityIntent);
			break;
			
		case GlobalMenuFragment.MENU_ITEM_MY_DISCOVERIES:
			// shows a dialog for selecting a discover to present.
			DiscoverListDialog discoverListDialog = new DiscoverListDialog(mApplicationConfigurations.getPartnerUserId(), this);
			discoverListDialog.setOnListDialogStateChangedListener(new OnListDialogStateChangedListener() {
				@Override
				public void onItemSelected(ListDialogItem listDialogItem, int position) {
					// calls the discovery activity with the given selected discovery.
					Discover discover = (Discover) listDialogItem;
					Bundle arguments = new Bundle();
					arguments.putSerializable(DiscoveryActivity.DATA_EXTRA_DISCOVER, (Serializable) discover);
					
					Intent startDiscoveryIntent = new Intent(getApplicationContext(), DiscoveryActivity.class);
					startDiscoveryIntent.putExtras(arguments);
					
					startDiscoveryIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					startActivity(startDiscoveryIntent);
					
					FlurryAgent.logEvent("My Discoveries");
				}
				@Override
				public void onCancelled() {}
			});
			discoverListDialog.show();
			break;

		case GlobalMenuFragment.MENU_ITEM_MY_PREFERENCES:
			Intent myPreferencesActivityIntent = new Intent(getApplicationContext(), MyPreferencesActivity.class);
			startActivityForResult(myPreferencesActivityIntent, HomeActivity.MY_PREFERENCES_ACTIVITY_RESULT_CODE);
			break;
			
		case  GlobalMenuFragment.MENU_ITEM_SETTINGS_AND_ACCOUNTS: 
			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(intent);
			break;
			
		/*
		 * More.
		 */
		case GlobalMenuFragment.MENU_ITEM_REWARDS:
			Intent redeemActivityIntent = new Intent(getApplicationContext(), RedeemActivity.class);
			startActivity(redeemActivityIntent);
			break;
			
		case GlobalMenuFragment.MENU_ITEM_INVITE_FRIENDS:
			Intent inviteFriendsActivity = new Intent(this, InviteFriendsActivity.class);
			startActivity(inviteFriendsActivity);
			break;
		
		case GlobalMenuFragment.MENU_ITEM_RATE_THIS_APP:
			mAppirater.rateAppClick();
			break;
			
		case GlobalMenuFragment.MENU_ITEM_GIVE_FEEDBACK:
			Intent feedbackActivityIntent = new Intent(getApplicationContext(), FeedbackActivity.class);
			startActivity(feedbackActivityIntent);
			break;
			
		case GlobalMenuFragment.MENU_ITEM_HELP_FAQ:
			Intent helpAndFaqActivityIntent = new Intent(getApplicationContext(), HelpAndFAQActivity.class);
			startActivity(helpAndFaqActivityIntent);
			break;
			
		case GlobalMenuFragment.MENU_ITEM_ABOUT:
			Intent aboutActivityIntent = new Intent(getApplicationContext(), AboutActivity.class);
			startActivity(aboutActivityIntent);
			break;
			
		default:
			break;
		}
	}
	
	@Override
	public void onQuickNavigationItemSelected(NavigationItem navigationItem) {
		// closes the navigation panel.
		closeQuickNavigation();
		// handles the last interacted ActionBar's component.
		mLastSelectedMenuItem.setIcon(R.drawable.background_actionbar_plus);
		mLastSelectedMenuItem.setChecked(false);
		
		Logger.d(TAG, navigationItem.toString());
		
		/*
		 * This is the trick: we use the activityNavigationItem member to know where are we now
		 * and by this to control the activity's fragments or to kick off the activity. 
		 */
		setNavigationItemSelected(navigationItem);
	}
	
	public void setNavigationItemSelected (NavigationItem navigationItem) {
		
		// toggles the states of the navigation.  
		mLastNavigationItem = mCurrentNavigationItem;
		mCurrentNavigationItem = navigationItem;
		
		/*
		 * colors back the item's background to transparent.
		 */
		if (mLastSelectedMenuItem != null) {
			View view = findViewById(mLastSelectedMenuItem.getItemId());
			if (view != null) {
				view.setBackgroundResource(R.drawable.transparent_background);
			}
		}
		
		if (navigationItem == NavigationItem.VIDEOS || navigationItem == NavigationItem.MUSIC) {
			// restarts the Home Activity.
			Intent startHomeActivity = new Intent(getApplicationContext(), HomeActivity.class);
			if (navigationItem == NavigationItem.VIDEOS) {
				startHomeActivity.putExtra(HomeActivity.ACTIVITY_EXTRA_MEDIA_CONTENT_TYPE, (Serializable) MediaContentType.VIDEO);
			}
			startHomeActivity.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(startHomeActivity);
		
		} else if (navigationItem == NavigationItem.DISCOVER) {
			Intent startDiscoveryIntent = new Intent(getApplicationContext(), DiscoveryActivity.class);
			startDiscoveryIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(startDiscoveryIntent);
			
		} else if (navigationItem == NavigationItem.RADIO) {
			Intent startRadioIntent = new Intent(getApplicationContext(), RadioActivity.class);
			startRadioIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(startRadioIntent);
		
		} else if (navigationItem == NavigationItem.SPECIALS) {
			Intent forYouActivityIntent = new Intent(getApplicationContext(), ForYouActivity.class);
			startActivity(forYouActivityIntent);
			
			FlurryAgent.logEvent("Specials");
		}
		
	}
	
	
	// ======================================================
	// PRIVATE HELPER METHODS.
	// ======================================================
	
	private boolean closePlayerBarContent() {
		if (mPlayerBarFragment == null) {
			mPlayerBarFragment = 
					(PlayerBarFragment) mFragmentManager.findFragmentById(R.id.main_fragmant_player_bar);
		}
		
		if (mPlayerBarFragment != null && mPlayerBarFragment.isVisible()
									   && mPlayerBarFragment.isContentOpened()) {
			
			mPlayerBarFragment.closeContent();
			return true;
		}
		
		return false;
	}
	
	private void closeFragmentOfMenuItem(MenuItem menuItem) {
		int itemId = menuItem.getItemId();
		if (itemId == R.id.menu_item_main_actionbar_navigation) {
			menuItem.setIcon(R.drawable.background_actionbar_plus);
			closeQuickNavigation();
			
		} else if (itemId == R.id.menu_item_main_actionbar_search) {
			closeMainSearch();
			
		} else if (itemId == R.id.menu_item_main_actionbar_settings) {
			closeGlobalMenu();
		}
	}
	
	private void openGlobalMenu() {
		GlobalMenuFragment mainSettingsFragment = new GlobalMenuFragment();
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		mainSettingsFragment.setOnGlobalMenuItemSelectedListener(this);
		
		fragmentTransaction.add(R.id.main_navigation_fragmant_container, mainSettingsFragment, FRAGMENT_TAG_MAIN_GLOBAL_MENU);
		fragmentTransaction.addToBackStack(FRAGMENT_TAG_MAIN_GLOBAL_MENU);
		fragmentTransaction.commit();
	}
	
	private void openQuickNavigation() {
		
		QuickNavigationFragment fragment = new  QuickNavigationFragment();
		// register this activity to handle new Navigation items selections. 
		fragment.setOnQuickNavigationItemSelectedListener(this);
		fragment.setCurrentNavigationItem(mCurrentNavigationItem);
		fragment.setLastNavigationItem(mLastNavigationItem);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.main_navigation_fragmant_container, fragment, FRAGMENT_TAG_MAIN_QUICK_NAVIGATION);
		fragmentTransaction.addToBackStack(FRAGMENT_TAG_MAIN_QUICK_NAVIGATION);
		fragmentTransaction.commit();
		
		FlurryAgent.logEvent("Plus menu");
	}
	
	private void openMainSearch(String query) {
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		MainSearchFragment fragment = new  MainSearchFragment();
		
		if (!query.equalsIgnoreCase(FRAGMENT_TAG_MAIN_SEARCH)) {	
			Bundle data = new Bundle();
			data.putString(VideoActivity.ARGUMENT_SEARCH_VIDEO, query);		
			fragment.setArguments(data);		
			fragmentTransaction.add(R.id.main_navigation_fragmant_container, fragment, FRAGMENT_TAG_MAIN_SEARCH);
			fragmentTransaction.addToBackStack(null);
			
		} else {
			fragmentTransaction.add(R.id.main_navigation_fragmant_container, fragment, FRAGMENT_TAG_MAIN_SEARCH);
			fragmentTransaction.addToBackStack(null);
		}
		
		fragmentTransaction.commit();
	}
	
	public void explicitOpenSearch(String query) {
		// removes the old one if visible.
		Fragment searchFragment = mFragmentManager.findFragmentByTag(FRAGMENT_TAG_MAIN_SEARCH);
		if (searchFragment != null) {
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.remove(searchFragment);
			fragmentTransaction.commit();
		}
		
		// shows the new one with the query.
		openMainSearch(query);
		
		// updates the actionbar's icon for it.
		MenuItem item = mMenu.findItem(R.id.menu_item_main_actionbar_search);
		item.setChecked(true);
		// colors the search background. 
		View view = findViewById(item.getItemId());
		view.setBackgroundResource(R.drawable.action_bar_selected_item_background_search);
		
		// stores this as the last selected one to handle back button actionbar's icon background.
		mLastSelectedMenuItem = item;
	}
	
	private void closeGlobalMenu() {
		mFragmentManager.popBackStack();
	}
	
	private void closeQuickNavigation() {
		mFragmentManager.popBackStack();
	}
	
	private void closeMainSearch() {
		mFragmentManager.popBackStack();
	}
	
	
	// ======================================================
	// PUBLIC.
	// ======================================================
	
	public enum NavigationItem {
		MUSIC				(R.string.main_actionbar_navigation_music),
		VIDEOS				(R.string.main_actionbar_navigation_videos),
		DISCOVER			(R.string.main_actionbar_navigation_discover),
		RADIO				(R.string.main_actionbar_navigation_radio),
		SPECIALS			(R.string.main_actionbar_navigation_specials),
		PROFILE				(R.string.main_actionbar_navigation_profile),
		OTHER				(R.string.main_actionbar_navigation_music);
		
		public final int title;
		
		NavigationItem (int title) {
			this.title = title;
		}
	}

	public PlayerBarFragment getPlayerBar() {
		if (mPlayerBarFragment == null) {
			if (mFragmentManager != null) {
				mPlayerBarFragment = 
						(PlayerBarFragment) mFragmentManager.findFragmentById(R.id.main_fragmant_player_bar);
			}
		}
		return mPlayerBarFragment;
	}
	
	/**
	 * Retrieves the Navigation item of the current visible activity.
	 */
	protected abstract NavigationItem getNavigationItem();
	
	private LoadingDialogFragment mLoadingDialogFragment = null;
	
	protected void showLoadingDialog(int messageResource) {
		if (mLoadingDialogFragment == null && mIsResumed && !isFinishing() && !mIsDestroyed) {
			mLoadingDialogFragment = LoadingDialogFragment.newInstance(R.string.application_dialog_loading_content);
			mLoadingDialogFragment.setCancelable(true);
			mLoadingDialogFragment.show(mFragmentManager, LoadingDialogFragment.FRAGMENT_TAG);
		}
	}
	
	protected void hideLoadingDialog() {
		
		if (mLoadingDialogFragment != null && !isFinishing() && !mIsDestroyed) {
			
			mLoadingDialogFragment.dismiss();
			mLoadingDialogFragment = null;
			
//			// For avoiding perform an action after onSaveInstanceState.
//			new Handler().post(new Runnable() {
//				
//	            public void run() {
//	            	
//	            	
//	            }
//	        });
		}
	}
	
	/**
	 * Indicates whatever any of the ActionBar's item is selected.
	 */
	protected boolean isAnyActionBarOptionSelected() {
		// iterates thru the menu and checks for checked menu items.
		if (mMenu != null) {
			MenuItem menuItem = null;
			int menuSize = mMenu.size();
			for (int i = 0; i < menuSize; i++) {
				menuItem = mMenu.getItem(i);
				/*
				 * we care only if one of the Quick Navigation, Search and Global Menu is checked -
				 * visible on the screen.
				 */
				int itemId = menuItem.getItemId();
				if (itemId == R.id.menu_item_main_actionbar_navigation ||
					itemId == R.id.menu_item_main_actionbar_search ||
					itemId == R.id.menu_item_main_actionbar_settings) {
					
					if(menuItem.isChecked())
						return true;
				}
			}
		}
		return false;
	}
	
	protected NavigationItem getCurrentNavigationItem() {
		return mCurrentNavigationItem;
	}
	
	protected boolean isActivityDestroyed() {
		return mIsDestroyed;
	}
	
}
