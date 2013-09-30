package com.hungama.myplay.activity.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.ui.dialogs.DiscoverListDialog;
import com.hungama.myplay.activity.ui.dialogs.ListDialog.ListDialogItem;
import com.hungama.myplay.activity.ui.dialogs.ListDialog.OnListDialogStateChangedListener;
import com.hungama.myplay.activity.ui.fragments.FavoritesFragment;
import com.hungama.myplay.activity.ui.fragments.GlobalMenuFragment;
import com.hungama.myplay.activity.ui.fragments.ItemableTilesFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.fragments.RedeemFragment;
import com.hungama.myplay.activity.ui.fragments.FavoritesFragment.OnMediaItemsLoadedListener;
import com.hungama.myplay.activity.ui.fragments.social.BadgesFragment;
import com.hungama.myplay.activity.ui.fragments.social.LeaderboardFragment;
import com.hungama.myplay.activity.ui.fragments.social.ProfileFragment;
import com.hungama.myplay.activity.ui.fragments.social.LeaderboardFragment.OnLeaderboardUserSelectedListener;
import com.hungama.myplay.activity.ui.fragments.social.ProfileFragment.OnProfileSectionSelectedListener;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;

public class ProfileActivity extends MainActivity implements OnProfileSectionSelectedListener, 
															 OnLeaderboardUserSelectedListener,
															 CommunicationOperationListener, 
															 OnMediaItemOptionSelectedListener,
														     OnMediaItemsLoadedListener{
	
	private static final String TAG = "ProfileActivity";
	
	public static final String DATA_EXTRA_USER_ID = "data_extra_user_id";
	
	public static final String ARGUMENT_PROFILE_ACTIVITY = "argument_profile_activity";
	
	private final int LOGIN_ACTIVITY_CODE = 1;
	
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private FragmentManager mFragmentManager;
	private PlayerBarFragment mPlayerBar;
	
	private TextView mTitle;
	
	/*
	 * Creating Fragments from onActivityResult causes the application to crash
	 * due to the android-support library.
	 * 
	 * To make the Activity presents the user's
	 * profile after a redirection to the Login page, the activity assigns this
	 * flag to TRUE in the onActivityResult and resets it in the onResume,
	 * then shows this activity's content.
	 * 
	 * Note that this is necessary only when this activity launches the Login
	 * Activity to force the User to sign in and retrieves a response that the
	 * user has successfully Logged in / Signed in the application. 
	 */
	private boolean mDoShowContentDueAndroidBug = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_with_title);

		// initializes activity's components.
		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		mFragmentManager = getSupportFragmentManager();
		
		mPlayerBar = getPlayerBar();
		// sets the default title.
		mTitle = (TextView) findViewById(R.id.main_title_bar_text);
		
		// gets the user id.
		String requestedUesrId = "";
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey(DATA_EXTRA_USER_ID)) {
			requestedUesrId = extras.getString(DATA_EXTRA_USER_ID);
		}

		/*
		 * To show the user's profile he must be logged in to the application.
		 * if he is not, pop up the login page, then directs him to here again.
		 */
		boolean applicationRealUser = mDataManager.getApplicationConfigurations().isRealUser();
		
		/*
		 * Shows the profile only if the application user is signed in or
		 * the requested profile is not of him. 
		 */
		if (applicationRealUser || !(TextUtils.isEmpty(requestedUesrId))) {
			showProfileContent(requestedUesrId);
		} else {
			// launches the Login page.
			Intent startLoginActivityIntent = new Intent(this, LoginActivity.class);
			startLoginActivityIntent.putExtra(ARGUMENT_PROFILE_ACTIVITY, "profile_activity");
	 		startActivityForResult(startLoginActivityIntent, LOGIN_ACTIVITY_CODE);
		}
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();

		/*
		 * Fixes the issue of returning from the application settings and loging off from
		 * any account, like launching this activity for the first time,
		 * it does not allow you to see it if you are not logged in.
		 * finishing..... 
		 */
		boolean isRealUser = mDataManager.getApplicationConfigurations().isRealUser();
		if (!isRealUser) {
			finish();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		/*
		 * Happens only for the first time this Activity was launched
		 * when the user was not a real one, and he signed / logged in the app
		 * via the a redirected LoginActivity from here.
		 */
		if (mDoShowContentDueAndroidBug) {
			mDoShowContentDueAndroidBug = false;
			
			// gets the user id.
			String userID = "";
			Bundle extras = getIntent().getExtras();
			if (extras != null && extras.containsKey(DATA_EXTRA_USER_ID)) {
				userID = extras.getString(DATA_EXTRA_USER_ID);
			}
			
			showProfileContent(userID);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_ACTIVITY_CODE) {
			if (resultCode == RESULT_OK) {
				boolean realUser = mDataManager.getApplicationConfigurations().isRealUser();
				if (realUser) {
					// the user has logged in, shows his profile.
					mDoShowContentDueAndroidBug = true;
				} else {
					// the user has tricked us.
					finish();
				}
				
			} else {
				// closes this activity.
				finish();
			}
		}
	}
	
	private void showProfileContent(String userId) {

		// the operation sets by default the application's user id if the given is empty.
		
		ProfileFragment profileFragment = new ProfileFragment();
		profileFragment.setOnProfileSectionSelectedListener(this);
		
		Bundle arguments = new Bundle();
		arguments.putString(ProfileFragment.FRAGMENT_ARGUMENT_USER_ID, userId);
		profileFragment.setArguments(arguments);
		
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(R.id.main_fragmant_container, profileFragment);
		fragmentTransaction.commit();
	}
	
	public void setTitleBarText(String text) {
		if (TextUtils.isEmpty(text)) {
			mTitle.setText(R.string.social_profile_title_bar_text_my_plofile);
		} else {
			mTitle.setText(text);
		}
	}

	private void setTitleForFavorite(MediaType mMediaType, String userId, int size) {
		// SetS title bar
		boolean isMe;
		String title = "";
		if (mApplicationConfigurations.getPartnerUserId().equalsIgnoreCase(userId)) {
			isMe = true;
		} else {
			isMe = false;
		}

		if (isMe) {			
			if (mMediaType ==  MediaType.ALBUM) {
				title = getResources().getString(R.string.favorite_fragment_title_albums, size);
			} else if (mMediaType ==  MediaType.TRACK) {
				title = getResources().getString(R.string.favorite_fragment_title_songs, size);
			} else if (mMediaType ==  MediaType.PLAYLIST) {
				title = getResources().getString(R.string.favorite_fragment_title_playlists, size);
			} else if (mMediaType ==  MediaType.VIDEO) {
				title = getResources().getString(R.string.favorite_fragment_title_videos, size);
			}
		} else {
			if (mMediaType ==  MediaType.ALBUM) {
				title = getResources().getString(R.string.favorite_fragment_title_albums_other, size);
			} else if (mMediaType ==  MediaType.TRACK) {
				title = getResources().getString(R.string.favorite_fragment_title_songs_other, size);
			} else if (mMediaType ==  MediaType.PLAYLIST) {
				title = getResources().getString(R.string.favorite_fragment_title_playlists_other, size);
			} else if (mMediaType ==  MediaType.VIDEO) {
				title = getResources().getString(R.string.favorite_fragment_title_videos_other, size);
			}			
		}
		
		setTitleBarText(title);
	}
	
	private void showFavoriteFragmentFor(MediaType mediaType, String userId) {
		FavoritesFragment favoritesFragment = new FavoritesFragment();
		favoritesFragment.setOnMediaItemOptionSelectedListener(this);
		favoritesFragment.setOnMediaItemsLoadedListener(this);
		
		Bundle arguments = new Bundle();
		arguments.putSerializable(FavoritesFragment.FRAGMENT_ARGUMENT_MEDIA_TYPE, (Serializable) mediaType);
		arguments.putString(FavoritesFragment.FRAGMENT_ARGUMENT_USER_ID, userId);
		favoritesFragment.setArguments(arguments);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
												R.anim.slide_left_exit,
								                R.anim.slide_right_enter,
								                R.anim.slide_right_exit);
		fragmentTransaction.replace(R.id.main_fragmant_container, favoritesFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	
	// ======================================================
	// Profile Fragment callbacks.
	// ======================================================

	@Override
	public void onCurrencySectionSelected(String userId, int currency) {
		// shows the redeem page.
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		RedeemFragment redeemFragment = new RedeemFragment();
		
		Bundle arguments = new Bundle();
		arguments.putInt(RedeemActivity.ARGUMENT_REDEEM, currency);
		redeemFragment.setArguments(arguments);
		
		fragmentTransaction.replace(R.id.main_fragmant_container, redeemFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onDownloadSectionSelected(String userId) {
		Intent intent = new Intent(getApplicationContext(), MyCollectionActivity.class);
		startActivity(intent);
	}

	@Override
	public void onBadgesSectionSelected(String userId) {
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		BadgesFragment badgesFragment = new BadgesFragment();
		
		Bundle arguments = new Bundle();
		arguments.putString(BadgesFragment.FRAGMENT_ARGUMENT_USER_ID, userId);
		badgesFragment.setArguments(arguments);
		
		fragmentTransaction.replace(R.id.main_fragmant_container, badgesFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onLeaderboardSectionSelected(String userId) {
		// Shows the leaderboard to the given user.
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);

		LeaderboardFragment leaderboardFragment = new LeaderboardFragment();
		leaderboardFragment.setOnLeaderboardUserSelectedListener(this);
		
		Bundle arguments = new Bundle();
		arguments.putString(ProfileFragment.FRAGMENT_ARGUMENT_USER_ID, userId);
		leaderboardFragment.setArguments(arguments);
		
		fragmentTransaction.replace(R.id.main_fragmant_container, leaderboardFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onMyplaylistsSectionSelected(String userId) {
		ItemableTilesFragment mTilesFragment = new ItemableTilesFragment(MediaType.PLAYLIST, null);
		
		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		fragmentTransaction.replace(R.id.main_fragmant_container, mTilesFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	@Override
	public void onFavAlbumsSectionSelected(String userId) {
		showFavoriteFragmentFor(MediaType.ALBUM, userId);
	}

	@Override
	public void onFavSongsSectionSelected(String userId) {
		showFavoriteFragmentFor(MediaType.TRACK, userId);
	}

	@Override
	public void onFavPlaylistsSectionSelected(String userId) {
		showFavoriteFragmentFor(MediaType.PLAYLIST, userId);
	}

	@Override
	public void onFavVideosSectionSelected(String userId) {
		showFavoriteFragmentFor(MediaType.VIDEO, userId);
	}

	@Override
	public void onDiscoveriesSectionSelected(final String userId) {
		//onGlobalMenuItemSelected(GlobalMenuFragment.MENU_ITEM_MY_DISCOVERIES);
		// shows a dialog for selecting a discover to present.
		DiscoverListDialog discoverListDialog = new DiscoverListDialog(userId, this);
		discoverListDialog.setOnListDialogStateChangedListener(new OnListDialogStateChangedListener() {
			@Override
			public void onItemSelected(ListDialogItem listDialogItem, int position) {
				// calls the discovery activity with the given selected discovery.
				Discover discover = (Discover) listDialogItem;
				Bundle arguments = new Bundle();
				arguments.putSerializable(DiscoveryActivity.DATA_EXTRA_DISCOVER, (Serializable) discover);
				arguments.putSerializable(DiscoveryActivity.DATA_EXTRA_DISCOVER_USERID, userId);
				
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
	}

	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.PROFILE;
	}


	// ======================================================
	// Leaderboard Callbacks. 
	// ======================================================
	
	@Override
	public void onLeaderboardUserSelectedListener(String selectedUserId) {
		// shows the profile of the selected user.
		
		ProfileFragment profileFragment = new ProfileFragment();
		profileFragment.setOnProfileSectionSelectedListener(this);
		
		Bundle arguments = new Bundle();
		arguments.putString(ProfileFragment.FRAGMENT_ARGUMENT_USER_ID, selectedUserId);
		profileFragment.setArguments(arguments);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		fragmentTransaction.replace(R.id.main_fragmant_container, profileFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	
	// ======================================================
	//  Profile Favorites callbacks.
	// ======================================================
	
	@Override
	public void onMediaItemsLoaded(MediaType mediaType, String userId, List<MediaItem> mediaItems) {
		setTitleForFavorite(mediaType, userId, (mediaItems != null ? mediaItems.size() : 0));
	}

	
	// ======================================================
	// Communication callbacks.
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS)
			showLoadingDialog(R.string.application_dialog_loading_content);
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
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
		}
		hideLoadingDialog();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		if (errorType != ErrorType.OPERATION_CANCELLED)
			hideLoadingDialog();
	}
	
	
	// ======================================================
	// MediaDetails callbacks.
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



}
