package com.hungama.myplay.activity.ui.fragments.social;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.social.Badge;
import com.hungama.myplay.activity.data.dao.hungama.social.LeaderBoardUser;
import com.hungama.myplay.activity.data.dao.hungama.social.Profile;
import com.hungama.myplay.activity.data.dao.hungama.social.UserBadges;
import com.hungama.myplay.activity.data.dao.hungama.social.UserDiscover;
import com.hungama.myplay.activity.data.dao.hungama.social.UserDisoveries;
import com.hungama.myplay.activity.data.dao.hungama.social.UserFavoriteAlbums;
import com.hungama.myplay.activity.data.dao.hungama.social.UserFavoritePlaylists;
import com.hungama.myplay.activity.data.dao.hungama.social.UserFavoriteSongs;
import com.hungama.myplay.activity.data.dao.hungama.social.UserFavoriteVideos;
import com.hungama.myplay.activity.data.dao.hungama.social.UserLeaderBoardUsers;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialProfileOperation;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.fragments.MainFragment;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends MainFragment implements OnClickListener, CommunicationOperationListener{
	
	private static final String TAG = "ProfileFragment";
	
	// ======================================================
	// Public.
	// ======================================================
	
	public static final String FRAGMENT_ARGUMENT_USER_ID = "fragment_argument_user_id";
	
	public interface OnProfileSectionSelectedListener {
		
		public void onCurrencySectionSelected(String userId, int currency);
		
		public void onDownloadSectionSelected(String userId);
		
		public void onBadgesSectionSelected(String userId);
		
		public void onLeaderboardSectionSelected(String userId);
		
		public void onMyplaylistsSectionSelected(String userId);
		
		public void onFavAlbumsSectionSelected(String userId);
		
		public void onFavSongsSectionSelected(String userId);
		
		public void onFavPlaylistsSectionSelected(String userId);
		
		public void onFavVideosSectionSelected(String userId);
		
		public void onDiscoveriesSectionSelected(String userId);
	}
	
	public void setOnProfileSectionSelectedListener(OnProfileSectionSelectedListener listener) {
		mOnProfileSectionSelectedListener = listener;
	}
	
	private OnProfileSectionSelectedListener mOnProfileSectionSelectedListener;
	
	private DataManager mDataManager;
	private String mUserId;
	private Profile mUserProfile = null;
	
	private boolean mIsApplicationUser = true;
	
	//private ImageFetcher mImageFetcher;
	
	private int mScreenWidth = 0;
	
	// user bar.
	private RelativeLayout mContainerUserBar;
	private ImageView mImageUserThumbnail;
	private TextView mTextUserName;
	private RelativeLayout mContainerUserCurrency;
	private TextView mTextUserCurrencyValue;
	private RelativeLayout mContainerUserDownloads;
	private TextView mTextUserDownloadsValue;
	private TextView mTextUserCurrentLevel;
	
	private TextView mMyCollectionText;
	private TextView mRedeemText;
	
	// Level bar.
	private RelativeLayout mContainerLevelBar;
	private ProgressBar mProgressLevelBar;
	private TextView mTextLevelZero;
	private LinearLayout mContainerLevels;
	
	
	// badges section.
	private LinearLayout mContainerBadges;
	private LinearLayout mHeaderBadges;
	private TextView mTextBadgesValue;
	private ImageView mImageBadge1;
	private TextView mTextBadge1;
	private ImageView mImageBadge2;
	private TextView mTextBadge2;
	private ImageView mImageBadge3;
	private TextView mTextBadge3;
	
	// leaderboard section.
	private RelativeLayout mContainerLeaderboard;
	private LinearLayout mHeaderLeaderboard;
	private TextView mTextLeaderboardValue;
	private RelativeLayout mContainerLeaderboardUser1;
	private RelativeLayout mContainerLeaderboardUser2;
	private RelativeLayout mContainerLeaderboardUser3;
	private TextView mTextLeaderboardUser1Rank;
	private TextView mTextLeaderboardUser1Name;
	private TextView mTextLeaderboardUser1TotalPoints;
	private TextView mTextLeaderboardUser2Rank;
	private TextView mTextLeaderboardUser2Name;
	private TextView mTextLeaderboardUser2TotalPoints;
	private TextView mTextLeaderboardUser3Rank;
	private TextView mTextLeaderboardUser3Name;
	private TextView mTextLeaderboardUser3TotalPoints;
	
	// my playlists section.
	private LinearLayout mContainerMyPlaylists;
	private LinearLayout  mHeaderMyPlaylists;
	private TextView mTextMyPlaylistsValue;
	private TextView mTextMyPlaylist1Name;
	private TextView mTextMyPlaylist2Name;
	private TextView mTextMyPlaylist3Name;
	private ImageView mImageMoreIndicator;
	private TextView mTextMyPlaylistEmpty;
	
	// favorite albums.
	private LinearLayout mContainerFavoriteAlbums;
	private LinearLayout mHeaderFavoriteAlbums;
	private TextView mTextFavoriteFavoriteAlbumsValue;
	private ImageView mTextFavoriteFavoriteAlbum1;
	private ImageView mTextFavoriteFavoriteAlbum2;
	private ImageView mTextFavoriteFavoriteAlbum3;
	
	// favorite songs.
	private LinearLayout mContainerFavoriteSongs;
	private LinearLayout mHeaderFavoriteSongs;
	private TextView mTextFavoriteSongsValue;
	private TextView mTextFavoriteSong1Name;
	private TextView mTextFavoriteSong2Name;
	private TextView mTextFavoriteSong3Name;
	
	// favorite playlists.
	private LinearLayout mContainerFavoritePlaylists;
	private LinearLayout mHeaderFavoritePlaylists;
	private TextView mTextFavoritePlaylistValue;
	private TextView mTextFavoritePlaylist1Name;
	private TextView mTextFavoritePlaylist2Name;
	private TextView mTextFavoritePlaylist3Name;
	
	// favorite videos.
	private LinearLayout mContainerFavoriteVideos;
	private LinearLayout mHeaderFavoriteVideos;
	private TextView mTextFavoriteVideosValue;
	private ImageView mTextFavoriteVideo1;
	private ImageView mTextFavoriteVideo2;
	private ImageView mTextFavoriteVideo3;
	
	// favorite discoveries.
	private LinearLayout mContainerDiscoveries;
	private LinearLayout mHeaderFavoriteDiscoveries;
	private TextView mTextDiscoveriesValue;
	private TextView mTextDiscoveriesItem1Name;
	private TextView mTextDiscoveriesItem2Name;
	private TextView mTextDiscoveriesItem3Name;
	
	private static final String KEY_INSTANCE_STATE_PROGRESS = "key_instance_state_progress"; 
	
	
	// ======================================================
	// Life cycle.
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_social_profile, container, false);
		
		initializeUserControls(rootView);
		
		adjustControllersSizes();
		
		// hides all the sections.
		mTextUserCurrentLevel.setVisibility(View.INVISIBLE);
		mContainerBadges.setVisibility(View.GONE);
		mContainerLeaderboard.setVisibility(View.GONE);
		mContainerMyPlaylists.setVisibility(View.GONE);
		mContainerFavoriteAlbums.setVisibility(View.GONE);
		mContainerFavoriteSongs.setVisibility(View.GONE);
		mContainerFavoritePlaylists.setVisibility(View.GONE);
		mContainerFavoriteVideos.setVisibility(View.GONE);
		mContainerDiscoveries.setVisibility(View.GONE);
		
		setControllersListeners();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (mUserProfile == null) {
			// gets the user id.
			Bundle arguments = getArguments();
			if (arguments != null && arguments.containsKey(FRAGMENT_ARGUMENT_USER_ID)) {
				mUserId = arguments.getString(FRAGMENT_ARGUMENT_USER_ID);
				
				// checks if the given user is the application user.
				ApplicationConfigurations applicationConfigurations = mDataManager.getApplicationConfigurations();
				String applicationUserId = applicationConfigurations.getPartnerUserId();
				if (TextUtils.isEmpty(mUserId) || applicationUserId.equals(mUserId)) {
					mUserId = mDataManager.getApplicationConfigurations().getPartnerUserId();
					mIsApplicationUser = true;
					
				} else {
					mIsApplicationUser = false;
				}
				Logger.i(TAG, "User Id: " + mUserId);
				mDataManager.getUserProfile(mUserId, this);
				
			} else {
				throw new IllegalArgumentException("ProfileFragment must be created with a user id argument for key: " + FRAGMENT_ARGUMENT_USER_ID);
			}
			
		} else {
			// resets the title for this fragment.
			String title = null;
			if (mIsApplicationUser || TextUtils.isEmpty(mUserProfile.name)) {
				title = getResources().getString(R.string.social_profile_title_bar_text_my_plofile);
			} else {
				title = getResources().getString(R.string.social_profile_title_bar_text_user_plofile, mUserProfile.name + "'s");
			}
			
			((ProfileActivity) getActivity()).setTitleBarText(title);
			
			/*
			 * Android is a piece of shit, you can't get views measures like in
			 * any normal platform.
			 */
			ViewTreeObserver viewTreeObserver = getView().getViewTreeObserver();
			if (viewTreeObserver.isAlive()) {
				viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				    @Override
				    public void onGlobalLayout() {
				    	// removes the listener.
				    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				    		getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
				    		
						} else {
							getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
						}
				    	// draws the progress bar. 
				    	populateUserControlls();
				    }
			  });
			}
		}
		
//		if (mImageFetcher != null) {
//			mImageFetcher.setExitTasksEarly(false);
//		}
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key));
		FlurryAgent.onPageView();
		
		if(mDataManager.getApplicationConfigurations().getPartnerUserId().equalsIgnoreCase(mUserId)){
			
			FlurryAgent.logEvent("My Profile");
			
		}else{
			FlurryAgent.logEvent("Others Profile");
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
//		if (mImageFetcher != null) {
//			mImageFetcher.setExitTasksEarly(true);
//			mImageFetcher.flushCache();
//		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		FlurryAgent.onEndSession(getActivity());
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
//		if (mImageFetcher != null) {
//			mImageFetcher.closeCache();
//			mImageFetcher = null;
//		}
	}
	
	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		
		if (viewId == R.id.profile_user_bar_currency) {
			if (mOnProfileSectionSelectedListener != null){
				int currency = (int) mUserProfile.points;
				mOnProfileSectionSelectedListener.onCurrencySectionSelected(mUserId, currency);
			}
		} else if (viewId == R.id.profile_user_bar_download) {
			if (mOnProfileSectionSelectedListener != null)
				mOnProfileSectionSelectedListener.onDownloadSectionSelected(mUserId);
			
		} else if (viewId == R.id.social_profile_section_badges) {
			if (mOnProfileSectionSelectedListener != null)
				mOnProfileSectionSelectedListener.onBadgesSectionSelected(mUserId);
			
		} else if (viewId == R.id.social_profile_section_leaderboard) {
			if (mOnProfileSectionSelectedListener != null)
				mOnProfileSectionSelectedListener.onLeaderboardSectionSelected(mUserId);
			
		} else if (viewId == R.id.social_profile_section_my_playlists) {
			if (mOnProfileSectionSelectedListener != null)
				mOnProfileSectionSelectedListener.onMyplaylistsSectionSelected(mUserId);
			
		} else if (viewId == R.id.social_profile_section_fav_albums) {
			if (mOnProfileSectionSelectedListener != null)
				mOnProfileSectionSelectedListener.onFavAlbumsSectionSelected(mUserId);
		
		} else if (viewId == R.id.social_profile_section_fav_songs) {
			if (mOnProfileSectionSelectedListener != null)
				Logger.i(TAG, "User Id: " + mUserId + " FavSongs");
				mOnProfileSectionSelectedListener.onFavSongsSectionSelected(mUserId);
			
		} else if (viewId == R.id.social_profile_section_fav_playlists) {
			if (mOnProfileSectionSelectedListener != null)
				mOnProfileSectionSelectedListener.onFavPlaylistsSectionSelected(mUserId);
			
		} else if (viewId == R.id.social_profile_section_fav_videos) {
			if (mOnProfileSectionSelectedListener != null)
				mOnProfileSectionSelectedListener.onFavVideosSectionSelected(mUserId);
			
		} else if (viewId == R.id.social_profile_section_discoveries) {
			if (mOnProfileSectionSelectedListener != null)
				mOnProfileSectionSelectedListener.onDiscoveriesSectionSelected(mUserId);			
		}
	}
	
	
	// ======================================================
	// Communication callbacks.
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE) {
			showLoadingDialog(R.string.application_dialog_loading);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE) {
			mUserProfile = (Profile) responseObjects.get(SocialProfileOperation.RESULT_KEY_PROFILE);
			
			// sets the title.
			String title = null;
			
			if (mIsApplicationUser || TextUtils.isEmpty(mUserProfile.name)) {
				
				if (!isDetached()) {
					if (getActivity() != null) {
						title = getString(R.string.social_profile_title_bar_text_my_plofile);
					}					
				
				} else {
					
					title = "My Profile";
				}
				
			} else {
				
				if (!isDetached()) {
				
					title = getString(R.string.social_profile_title_bar_text_user_plofile, mUserProfile.name +"'s");
				
				} else {
					
					title = mUserProfile.name + "Profile";
				}
			}
			
			((ProfileActivity) getActivity()).setTitleBarText(title);
			
			// populates the sections based the given profile.
			populateUserControlls();
			
			hideLoadingDialog();
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		
		if (isRemoving() || isDetached() || !isInLayout()) {
			return;
		}
		
		if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE && !TextUtils.isEmpty(errorMessage)) {
			if (!TextUtils.isEmpty(errorMessage)) {
				Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
			}
		}
		
		hideLoadingDialog();
	}
	
	
	// ======================================================
	// Private Helper methods.
	// ======================================================
	
	public void initializeUserControls(View rootView) {
		
		// the user bar.
		mContainerUserBar = (RelativeLayout) rootView.findViewById(R.id.profile_user_bar);
		mImageUserThumbnail = (ImageView) mContainerUserBar.findViewById(R.id.profile_user_bar_user_thumbnail);
		mTextUserName = (TextView) mContainerUserBar.findViewById(R.id.profile_user_bar_text_user_name);
		mContainerUserCurrency = (RelativeLayout) mContainerUserBar.findViewById(R.id.profile_user_bar_currency);
		mTextUserCurrencyValue = (TextView) mContainerUserBar.findViewById(R.id.profile_user_bar_currency_text_value);
		mContainerUserDownloads = (RelativeLayout) mContainerUserBar.findViewById(R.id.profile_user_bar_download);
		mTextUserDownloadsValue = (TextView) mContainerUserBar.findViewById(R.id.profile_user_bar_download_text_value);
		mTextUserCurrentLevel = (TextView) rootView.findViewById(R.id.social_profile_user_bar_text_current_level);
		
		mMyCollectionText = (TextView) rootView.findViewById(R.id.profile_user_my_collection_text);
		mRedeemText = (TextView) rootView.findViewById(R.id.profile_user_redeem_text);
		
		// Level Bar.
		mContainerLevelBar = (RelativeLayout) rootView.findViewById(R.id.social_profile_user_bar_level_bar);
		mProgressLevelBar = (ProgressBar) mContainerLevelBar.findViewById(R.id.social_profile_user_bar_level);
		mTextLevelZero = (TextView) mContainerLevelBar.findViewById(R.id.social_profile_user_bar_level_bar_level1);
		mContainerLevels = (LinearLayout) mContainerLevelBar.findViewById(R.id.social_profile_user_bar_level_bar_level_container);
		
		mTextLevelZero.setVisibility(View.INVISIBLE);
		
		// Badges section.
		mContainerBadges = (LinearLayout) rootView.findViewById(R.id.social_profile_section_badges);
		mHeaderBadges = (LinearLayout) mContainerBadges.findViewById(R.id.social_profile_section_badges_header);
		mTextBadgesValue = (TextView) mContainerBadges.findViewById(R.id.social_profile_section_badges_header_value);
		mImageBadge1 = (ImageView) mContainerBadges.findViewById(R.id.social_profile_section_badges_item1_image);
		mTextBadge1 = (TextView) mContainerBadges.findViewById(R.id.social_profile_section_badges_item1_text);
		mImageBadge2 = (ImageView) mContainerBadges.findViewById(R.id.social_profile_section_badges_item2_image);
		mTextBadge2 = (TextView) mContainerBadges.findViewById(R.id.social_profile_section_badges_item2_text);
		mImageBadge3 = (ImageView) mContainerBadges.findViewById(R.id.social_profile_section_badges_item3_image);
		mTextBadge3 = (TextView) mContainerBadges.findViewById(R.id.social_profile_section_badges_item3_text);
		
		// leaderboard section.
		mContainerLeaderboard = (RelativeLayout) rootView.findViewById(R.id.social_profile_section_leaderboard);
		mHeaderLeaderboard = (LinearLayout) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_header);
		mTextLeaderboardValue = (TextView) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_header_value);
		
		mContainerLeaderboardUser1 = (RelativeLayout) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_item1);
		mContainerLeaderboardUser2 = (RelativeLayout) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_item2);
		mContainerLeaderboardUser3 = (RelativeLayout) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_item3);
		
		mTextLeaderboardUser1Rank = (TextView) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_item1_rank);
		mTextLeaderboardUser1Name = (TextView) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_item1_user_name);
		mTextLeaderboardUser1TotalPoints = (TextView) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_item1_total_points);
		mTextLeaderboardUser2Rank = (TextView) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_item2_rank);
		mTextLeaderboardUser2Name = (TextView) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_item2_user_name);
		mTextLeaderboardUser2TotalPoints = (TextView) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_item2_total_points);
		mTextLeaderboardUser3Rank = (TextView) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_item3_rank);
		mTextLeaderboardUser3Name = (TextView) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_item3_user_name);
		mTextLeaderboardUser3TotalPoints = (TextView) mContainerLeaderboard.findViewById(R.id.social_profile_section_leaderboard_item3_total_points);
		
		// my playlists section.
		mContainerMyPlaylists = (LinearLayout) rootView.findViewById(R.id.social_profile_section_my_playlists);
		mHeaderMyPlaylists = (LinearLayout) rootView.findViewById(R.id.social_profile_section_my_playlists_header);
		mTextMyPlaylistsValue = (TextView) mContainerMyPlaylists.findViewById(R.id.social_profile_section_my_playlists_header_value);
		mTextMyPlaylist1Name = (TextView) mContainerMyPlaylists.findViewById(R.id.social_profile_section_my_playlists_item1);
		mTextMyPlaylist2Name = (TextView) mContainerMyPlaylists.findViewById(R.id.social_profile_section_my_playlists_item2);
		mTextMyPlaylist3Name = (TextView) mContainerMyPlaylists.findViewById(R.id.social_profile_section_my_playlists_item3);
		mImageMoreIndicator = (ImageView) mContainerMyPlaylists.findViewById(R.id.social_profile_section_my_playlists_more_indicator);
		mTextMyPlaylistEmpty = (TextView) mContainerMyPlaylists.findViewById(R.id.social_profile_section_my_playlists_empty);
		
		// favorite albums.
		mContainerFavoriteAlbums = (LinearLayout) rootView.findViewById(R.id.social_profile_section_fav_albums);
		mHeaderFavoriteAlbums = (LinearLayout) rootView.findViewById(R.id.social_profile_section_fav_albums_header);
		mTextFavoriteFavoriteAlbumsValue = (TextView) mContainerFavoriteAlbums.findViewById(R.id.social_profile_section_fav_albums_header_value);
		mTextFavoriteFavoriteAlbum1 = (ImageView) mContainerFavoriteAlbums.findViewById(R.id.social_profile_section_fav_albumes_item1_image);
		mTextFavoriteFavoriteAlbum2 = (ImageView) mContainerFavoriteAlbums.findViewById(R.id.social_profile_section_fav_albumes_item2_image);
		mTextFavoriteFavoriteAlbum3 = (ImageView) mContainerFavoriteAlbums.findViewById(R.id.social_profile_section_fav_albumes_item3_image);
		
		// favorite songs.
		mContainerFavoriteSongs = (LinearLayout) rootView.findViewById(R.id.social_profile_section_fav_songs);
		mHeaderFavoriteSongs = (LinearLayout) rootView.findViewById(R.id.social_profile_section_fav_songs_header);
		mTextFavoriteSongsValue = (TextView) mContainerFavoriteSongs.findViewById(R.id.social_profile_section_fav_songs_header_value);
		mTextFavoriteSong1Name = (TextView) mContainerFavoriteSongs.findViewById(R.id.social_profile_section_fav_songs_item1);
		mTextFavoriteSong2Name = (TextView) mContainerFavoriteSongs.findViewById(R.id.social_profile_section_fav_songs_item2);
		mTextFavoriteSong3Name = (TextView) mContainerFavoriteSongs.findViewById(R.id.social_profile_section_fav_songs_item3);
		
		// favorite playlists.
		mContainerFavoritePlaylists = (LinearLayout) rootView.findViewById(R.id.social_profile_section_fav_playlists);
		mHeaderFavoritePlaylists = (LinearLayout) rootView.findViewById(R.id.social_profile_section_fav_playlists_header);
		mTextFavoritePlaylistValue = (TextView) mContainerFavoritePlaylists.findViewById(R.id.social_profile_section_fav_playlists_header_value);
		mTextFavoritePlaylist1Name = (TextView) mContainerFavoritePlaylists.findViewById(R.id.social_profile_section_fav_playlists_item1);
		mTextFavoritePlaylist2Name = (TextView) mContainerFavoritePlaylists.findViewById(R.id.social_profile_section_fav_playlists_item2);
		mTextFavoritePlaylist3Name = (TextView) mContainerFavoritePlaylists.findViewById(R.id.social_profile_section_fav_playlists_item3);
		
		// favorite videos.
		mContainerFavoriteVideos = (LinearLayout) rootView.findViewById(R.id.social_profile_section_fav_videos);
		mHeaderFavoriteVideos = (LinearLayout) rootView.findViewById(R.id.social_profile_section_fav_videos_header);
		mTextFavoriteVideosValue = (TextView) mContainerFavoriteVideos.findViewById(R.id.social_profile_section_fav_videos_header_value);
		mTextFavoriteVideo1 = (ImageView) mContainerFavoriteVideos.findViewById(R.id.social_profile_section_fav_videos_item1_image);
		mTextFavoriteVideo2 = (ImageView) mContainerFavoriteVideos.findViewById(R.id.social_profile_section_fav_videos_item2_image);
		mTextFavoriteVideo3 = (ImageView) mContainerFavoriteVideos.findViewById(R.id.social_profile_section_fav_videos_item3_image);
		
		// favorite discoveries.
		mContainerDiscoveries = (LinearLayout) rootView.findViewById(R.id.social_profile_section_discoveries);
		mHeaderFavoriteDiscoveries = (LinearLayout) rootView.findViewById(R.id.social_profile_section_discoveries_header);
		mTextDiscoveriesValue = (TextView) mContainerDiscoveries.findViewById(R.id.social_profile_section_discoveries_header_value);
		mTextDiscoveriesItem1Name = (TextView) mContainerDiscoveries.findViewById(R.id.social_profile_section_discoveries_item1);
		mTextDiscoveriesItem2Name = (TextView) mContainerDiscoveries.findViewById(R.id.social_profile_section_discoveries_item2);
		mTextDiscoveriesItem3Name = (TextView) mContainerDiscoveries.findViewById(R.id.social_profile_section_discoveries_item3);
	}
	
	private void adjustControllersSizes() {
		
		/*
		 * Calculating the desired width for any section item
		 * in the page, to do that, we based on the structure of the "Badges" section like
		 * sizes.
		 */
		
		
		int sectionMargin = getResources().getDimensionPixelSize(R.dimen.profile_section_item_margin);
		int sectionIndicationWidth = getResources().getDimensionPixelSize(R.dimen.profile_section_item_more_indicator_width);
		
		// measuring the device's screen width. and setting the grid column width.
        Display display = getSherlockActivity().getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
        	mScreenWidth = display.getWidth();
        } else {
        	Point displaySize = new Point();
        	display.getSize(displaySize);
        	mScreenWidth = displaySize.x;
        }
        
        /*
         * from the width of the screen subtracting the inner item 
         * tiles margin and the more content indicator width, 
         * dividing in the number of tiles in a section row include the header - which is 4.
         */
        int sectionWidth = (mScreenWidth - (sectionMargin * 3) - sectionIndicationWidth) / 4;

        // leaderboard.
        RelativeLayout.LayoutParams leaderboardHeaderParams = (RelativeLayout.LayoutParams) mHeaderLeaderboard.getLayoutParams();
        leaderboardHeaderParams.width = sectionWidth;
        mHeaderLeaderboard.setLayoutParams(leaderboardHeaderParams);
	}
	
	private void setControllersListeners() {
		
		mContainerBadges.setOnClickListener(this);
		mContainerLeaderboard.setOnClickListener(this);
		mContainerMyPlaylists.setOnClickListener(this);
		mContainerFavoriteAlbums.setOnClickListener(this);
		mContainerFavoriteSongs.setOnClickListener(this);
		mContainerFavoritePlaylists.setOnClickListener(this);
		mContainerFavoriteVideos.setOnClickListener(this);
		mContainerDiscoveries.setOnClickListener(this);
	}
	
	private static final String KEY_LEADERBOARD_CONTAINER = "key_leaderboard_container";
	private static final String KEY_LEADERBOARD_RANK = "key_leaderboard_rank";
	private static final String KEY_LEADERBOARD_NAME = "key_leaderboard_name";
	private static final String KEY_LEADERBOARD_TOTAL = "key_leaderboard_total";
	
	private void populateUserControlls() {
		
		/*
		 * sets the only-can-be-visible buttons when there is no connection or
		 * failed to retrieve normal data. 
		 */
		if (mIsApplicationUser) {
			mContainerUserCurrency.setOnClickListener(this);
			mContainerUserDownloads.setOnClickListener(this);
		}
		
		/*
		 * Initializes the image fetcher.
		 */
		
//		// calculates the cache.
//		mImageUserThumbnail.measure(0, 0);
//		int thumbSize = mImageUserThumbnail.getMeasuredHeight();
//		
//		ImageCache.ImageCacheParams cacheParams =
//                new ImageCache.ImageCacheParams(getActivity(), DataManager.FOLDER_THUMBNAILS_CACHE);
//        cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);
//        cacheParams.compressFormat = CompressFormat.PNG;
//        
//		mImageFetcher = new ImageFetcher(getActivity(), thumbSize);
//		mImageFetcher.setLoadingImage(R.color.white);
//		mImageFetcher.addImageCache(getChildFragmentManager(), cacheParams);
//		// WARNING: Do Not set this boolean to true
//        mImageFetcher.setImageFadeIn(false);
        
        // populates the user bar.
        populdateUserBar();

    	// populates the badges section.
        populateBadgesSection();
    	
    	// populates the leader board section.
        populdateLeaderboardSection();
        
        // populates the user's playlists if is the application's user.
        if (mIsApplicationUser) {
        	populateUserPlaylitstsSection();
        	
        } else {
        	mContainerMyPlaylists.setVisibility(View.GONE);
        }
    	
        populateFavoritesSections();
        
        populateUserDiscoveriesSection();
        
        //populdateLevelBar();
        populdateLevelBarNew();
	}
	
	private void populdateUserBar() {
		
		Resources resources = getResources();
		
		if (mIsApplicationUser) {
			mContainerUserCurrency.setClickable(true);
			mContainerUserDownloads.setClickable(true);
		} else {
			mContainerUserCurrency.setClickable(false);
			mContainerUserDownloads.setClickable(false);
		}
        
        if (!TextUtils.isEmpty(mUserProfile.imageUrl)) {
        	//mImageFetcher.loadImage(mUserProfile.imageUrl, mImageUserThumbnail);
        	Picasso.with(getActivity()).cancelRequest(mImageUserThumbnail);
        	if(getActivity() != null && mUserProfile.imageUrl != null){
        		Picasso.with(getActivity()).load(mUserProfile.imageUrl).into(mImageUserThumbnail);
        	}
        }
        if (!TextUtils.isEmpty(mUserProfile.name)) {
        	mTextUserName.setText(mUserProfile.name);
        } else {
        	mTextUserName.setText(Utils.TEXT_EMPTY);
        }
    	mTextUserCurrencyValue.setText(Long.toString(mUserProfile.points));
    	mTextUserDownloadsValue.setText(Long.toString(mUserProfile.collections));
    	String currentLevel = resources.getString(R.string.social_profile_user_bar_current_level, mUserProfile.currentLevel);
    	mTextUserCurrentLevel.setText(currentLevel);
    	mTextUserCurrentLevel.setVisibility(View.VISIBLE);
    	
    	if(mDataManager.getApplicationConfigurations().getPartnerUserId().equalsIgnoreCase(mUserId)){
    		
    		//
    		
    	}else{
    		mRedeemText.setVisibility(View.INVISIBLE);
    		mMyCollectionText.setVisibility(View.INVISIBLE);
    	}
	}

	private void populateBadgesSection() {
		if (mUserProfile.userBadges != null && !Utils.isListEmpty(mUserProfile.userBadges.badges)) {
    		mContainerBadges.setVisibility(View.VISIBLE);
			
    		UserBadges userBadges = mUserProfile.userBadges;
    		mTextBadgesValue.setText(Integer.toString(userBadges.earnedBadges));
			
			// gets the badges, sets their names and icons.
			Badge badge1 = userBadges.badges.get(0);
			Badge badge2 = userBadges.badges.get(1);
			Badge badge3 = userBadges.badges.get(2);
			
			mTextBadge1.setText(badge1.name);
			mTextBadge2.setText(badge2.name);
			mTextBadge3.setText(badge3.name);
			
			mImageBadge1.setBackgroundColor(getResources().getColor(R.color.social_profile_section_content_item_backgorund));
			mImageBadge2.setBackgroundColor(getResources().getColor(R.color.social_profile_section_content_item_backgorund));
			mImageBadge3.setBackgroundColor(getResources().getColor(R.color.social_profile_section_content_item_backgorund));
			
//			mImageFetcher.loadImage(badge1.imageUrl, mImageBadge1);
//			mImageFetcher.loadImage(badge2.imageUrl, mImageBadge2);
//			mImageFetcher.loadImage(badge3.imageUrl, mImageBadge3);
						
			if(getActivity() != null){
				
				if(badge1.imageUrl != null){
					Picasso.with(getActivity()).cancelRequest(mImageBadge1);
					Picasso.with(getActivity()).load(badge1.imageUrl).into(mImageBadge1);
				}
				
				if(badge2.imageUrl != null){
					Picasso.with(getActivity()).cancelRequest(mImageBadge2);
					Picasso.with(getActivity()).load(badge2.imageUrl).into(mImageBadge2);
				}
				
				if(badge3.imageUrl != null){
					Picasso.with(getActivity()).cancelRequest(mImageBadge3);
					Picasso.with(getActivity()).load(badge3.imageUrl).into(mImageBadge3);
				}
				
			}
    		
    	} else {
    		mContainerBadges.setVisibility(View.GONE);
    	}
	}
	
	private void populdateLeaderboardSection() {
		if (mUserProfile.userLeaderBoardUsers != null) {
    		UserLeaderBoardUsers userLeaderBoardUsers = mUserProfile.userLeaderBoardUsers;
    		
    		mContainerLeaderboard.setVisibility(View.VISIBLE);
    		mTextLeaderboardValue.setText(Long.toString(userLeaderBoardUsers.userRank));
    		
    		if (!Utils.isListEmpty(userLeaderBoardUsers.leaderBoardUsers)) {
    			mContainerLeaderboardUser1.setVisibility(View.INVISIBLE);
    			mContainerLeaderboardUser2.setVisibility(View.INVISIBLE);
    			mContainerLeaderboardUser3.setVisibility(View.INVISIBLE);
    			
    			// constructs the views for each user as a set for iteration.
    			Map<String, Object> leaderboardUser1 = new HashMap<String, Object>();
    			leaderboardUser1.put(KEY_LEADERBOARD_CONTAINER, mContainerLeaderboardUser1);
    			leaderboardUser1.put(KEY_LEADERBOARD_RANK, mTextLeaderboardUser1Rank);
    			leaderboardUser1.put(KEY_LEADERBOARD_NAME, mTextLeaderboardUser1Name);
    			leaderboardUser1.put(KEY_LEADERBOARD_TOTAL, mTextLeaderboardUser1TotalPoints);
    			Map<String, Object> leaderboardUser2 = new HashMap<String, Object>();
    			leaderboardUser2.put(KEY_LEADERBOARD_CONTAINER, mContainerLeaderboardUser2);
    			leaderboardUser2.put(KEY_LEADERBOARD_RANK, mTextLeaderboardUser2Rank);
    			leaderboardUser2.put(KEY_LEADERBOARD_NAME, mTextLeaderboardUser2Name);
    			leaderboardUser2.put(KEY_LEADERBOARD_TOTAL, mTextLeaderboardUser2TotalPoints);
    			Map<String, Object> leaderboardUser3 = new HashMap<String, Object>();
    			leaderboardUser3.put(KEY_LEADERBOARD_CONTAINER, mContainerLeaderboardUser3);
    			leaderboardUser3.put(KEY_LEADERBOARD_RANK, mTextLeaderboardUser3Rank);
    			leaderboardUser3.put(KEY_LEADERBOARD_NAME, mTextLeaderboardUser3Name);
    			leaderboardUser3.put(KEY_LEADERBOARD_TOTAL, mTextLeaderboardUser3TotalPoints);
    			
    			Stack<Map<String, Object>> leaderboardUserMaps = new Stack<Map<String,Object>>();
    			leaderboardUserMaps.add(leaderboardUser3);
    			leaderboardUserMaps.add(leaderboardUser2);
    			leaderboardUserMaps.add(leaderboardUser1);
    			
    			Map<String, Object> leaderboardUserMap = null;
    			
    			for (LeaderBoardUser leaderboardUser : userLeaderBoardUsers.leaderBoardUsers) {
					if (leaderboardUserMaps.isEmpty())
						break;
					
					leaderboardUserMap = leaderboardUserMaps.pop();
					((RelativeLayout) leaderboardUserMap.get(KEY_LEADERBOARD_CONTAINER)).setVisibility(View.VISIBLE);
					((TextView) leaderboardUserMap.get(KEY_LEADERBOARD_RANK)).setText(Integer.toString(leaderboardUser.rank));
					((TextView) leaderboardUserMap.get(KEY_LEADERBOARD_NAME)).setText(leaderboardUser.name);
					((TextView) leaderboardUserMap.get(KEY_LEADERBOARD_TOTAL)).setText(Long.toString(leaderboardUser.totalPoint));
					
					if(leaderboardUser.id == Long.valueOf(mUserId)){
						
						((TextView) leaderboardUserMap.get(KEY_LEADERBOARD_RANK))
						.setTextColor(getResources().getColor(R.color.social_leaderboard_user_name_text_color));
						
						((TextView) leaderboardUserMap.get(KEY_LEADERBOARD_NAME))
						.setTextColor(getResources().getColor(R.color.social_leaderboard_user_name_text_color));
						
						((TextView) leaderboardUserMap.get(KEY_LEADERBOARD_TOTAL))
						.setTextColor(getResources().getColor(R.color.social_leaderboard_user_name_text_color));
					}
				}
    			
    		} else {
    			// hides all the users rows.
    			mContainerLeaderboardUser1.setVisibility(View.INVISIBLE);
    			mContainerLeaderboardUser2.setVisibility(View.INVISIBLE);
    			mContainerLeaderboardUser3.setVisibility(View.INVISIBLE);
    		}
    		
    	} else {
    		mContainerLeaderboard.setVisibility(View.GONE);
    	}
	}
	
	private void populateFavoritesSections() {
    	// populates the favorite albums.
    	if (mUserProfile.userFavoriteAlbums != null && !Utils.isListEmpty(mUserProfile.userFavoriteAlbums.albums)) {
    		mContainerFavoriteAlbums.setVisibility(View.VISIBLE);
    		
    		UserFavoriteAlbums userFavoriteAlbums = mUserProfile.userFavoriteAlbums;
    		mTextFavoriteFavoriteAlbumsValue.setText(Integer.toString(userFavoriteAlbums.albumCount));
    		
    		Stack<ImageView> favoriteAlbumsImages = new Stack<ImageView>();
    		favoriteAlbumsImages.add(mTextFavoriteFavoriteAlbum3);
    		favoriteAlbumsImages.add(mTextFavoriteFavoriteAlbum2);
    		favoriteAlbumsImages.add(mTextFavoriteFavoriteAlbum1);

    		ImageView albumImage = null;
    		
    		for (MediaItem mediaItem : userFavoriteAlbums.albums) {
    			if (favoriteAlbumsImages.isEmpty())
    				break;
    			
    			albumImage = favoriteAlbumsImages.pop();
    			
    			//mImageFetcher.loadImage(mediaItem.getImageUrl(), albumImage);
    			
    			Picasso.with(getActivity()).cancelRequest(albumImage);
    			if(getActivity() != null && mediaItem.getImageUrl() != null){
    				Picasso.with(getActivity()).load(mediaItem.getImageUrl()).into(albumImage);	
    			}
			}
    		
    	} else {
    		mContainerFavoriteAlbums.setVisibility(View.GONE);
    	}
    	
    	// populates the favorite songs.
    	if (mUserProfile.userFavoriteSongs != null && !Utils.isListEmpty(mUserProfile.userFavoriteSongs.songs)) {
    		mContainerFavoriteSongs.setVisibility(View.VISIBLE);
    		
    		UserFavoriteSongs userFavoriteSongs = mUserProfile.userFavoriteSongs;
    		mTextFavoriteSongsValue.setText(Integer.toString(userFavoriteSongs.songsCount));
    		
    		Stack<TextView> favoriteSongsNames = new Stack<TextView>();
    		favoriteSongsNames.add(mTextFavoriteSong3Name);
    		favoriteSongsNames.add(mTextFavoriteSong2Name);
    		favoriteSongsNames.add(mTextFavoriteSong1Name);

    		TextView songName = null;
    		
    		for (MediaItem mediaItem : userFavoriteSongs.songs) {
    			if (favoriteSongsNames.isEmpty())
    				break;
    			
    			songName = favoriteSongsNames.pop();
    			songName.setText(mediaItem.getTitle());
			}
    		
    	} else {
    		mContainerFavoriteSongs.setVisibility(View.GONE);
    	}
    	
    	// populates the favorite playlists.
    	if (mUserProfile.userFavoritePlaylists != null && !Utils.isListEmpty(mUserProfile.userFavoritePlaylists.playlists)) {
    		mContainerFavoritePlaylists.setVisibility(View.VISIBLE);
    		
    		UserFavoritePlaylists userFavoritePlaylists = mUserProfile.userFavoritePlaylists;
    		mTextFavoritePlaylistValue.setText(Integer.toString(userFavoritePlaylists.playlistCount));
    		
    		Stack<TextView> favoritePlaylistsNames = new Stack<TextView>();
    		favoritePlaylistsNames.add(mTextFavoritePlaylist3Name);
    		favoritePlaylistsNames.add(mTextFavoritePlaylist2Name);
    		favoritePlaylistsNames.add(mTextFavoritePlaylist1Name);

    		TextView playlistsName = null;
    		
    		for (MediaItem mediaItem : userFavoritePlaylists.playlists) {
    			if (favoritePlaylistsNames.isEmpty())
    				break;
    			
    			playlistsName = favoritePlaylistsNames.pop();
    			playlistsName.setText(mediaItem.getTitle());
			}
    		
    	} else {
    		mContainerFavoritePlaylists.setVisibility(View.GONE);
    	}
    	
    	// populates the favorite videos.
    	if (mUserProfile.userFavoriteVideos != null && !Utils.isListEmpty(mUserProfile.userFavoriteVideos.videos)) {
    		mContainerFavoriteVideos.setVisibility(View.VISIBLE);
    		
    		UserFavoriteVideos userFavoriteVideos = mUserProfile.userFavoriteVideos;
    		mTextFavoriteVideosValue.setText(Integer.toString(userFavoriteVideos.videoCount));
    		
    		Stack<ImageView> favoriteVideosImages = new Stack<ImageView>();
    		favoriteVideosImages.add(mTextFavoriteVideo3);
    		favoriteVideosImages.add(mTextFavoriteVideo2);
    		favoriteVideosImages.add(mTextFavoriteVideo1);

    		ImageView videoImage = null;
    		
    		for (MediaItem mediaItem : userFavoriteVideos.videos) {
    			if (favoriteVideosImages.isEmpty())
    				break;
    			
    			videoImage = favoriteVideosImages.pop();
    			
    			//mImageFetcher.loadImage(mediaItem.getImageUrl(), videoImage);
    			
    			Picasso.with(getActivity()).cancelRequest(videoImage);
    			if(getActivity() != null && !TextUtils.isEmpty(mediaItem.getImageUrl())){
    				Picasso.with(getActivity()).load(mediaItem.getImageUrl()).into(videoImage);    				
    			}
			}
    		
    	} else {
    		mContainerFavoriteVideos.setVisibility(View.GONE);
    	}
	}
	
	private void populateUserPlaylitstsSection() {
    	// gets the playlists from the DB.
    	Playlist dumyPlaylist = new Playlist();
    	Map<Long, Playlist> playlistsMap = mDataManager.getStoredPlaylists();
    	List<Playlist> playlists = new ArrayList<Playlist>();
    	
    	// Convert from Map<Long, Playlist> to List<Itemable>  
    	if (playlistsMap != null && playlistsMap.size() > 0) {
    		for(Map.Entry<Long, Playlist> p : playlistsMap.entrySet()){
    			playlists.add(p.getValue());
    		}
    	}
		
    	// populates the favorite playlists.
    	if (!Utils.isListEmpty(playlists)) {
    		mContainerMyPlaylists.setVisibility(View.VISIBLE);
    		// shows any internal component except the empty text.
    		mTextMyPlaylist1Name.setVisibility(View.VISIBLE);
    		mTextMyPlaylist2Name.setVisibility(View.VISIBLE);
    		mTextMyPlaylist3Name.setVisibility(View.VISIBLE);
    		mImageMoreIndicator.setVisibility(View.VISIBLE);
    		mTextMyPlaylistEmpty.setVisibility(View.GONE);
    		
    		mTextMyPlaylistsValue.setText(Integer.toString(playlists.size()));
    		
    		Stack<TextView> playlistsNames = new Stack<TextView>();
    		playlistsNames.add(mTextMyPlaylist3Name);
    		playlistsNames.add(mTextMyPlaylist2Name);
    		playlistsNames.add(mTextMyPlaylist1Name);

    		TextView songName = null;
    		
    		for (Playlist playlist : playlists) {
    			if (playlistsNames.isEmpty())
    				break;
    			
    			songName = playlistsNames.pop();
    			songName.setText(playlist.getName());
			}
    		
    	} else {
    		mContainerMyPlaylists.setVisibility(View.GONE);
    	}
	}
	
	private void populateUserDiscoveriesSection() {
    	// populates the discoveries.
    	if (mUserProfile.userDisoveries != null && !Utils.isListEmpty(mUserProfile.userDisoveries.userDiscoveries)) {
    		mContainerDiscoveries.setVisibility(View.VISIBLE);
    		
    		UserDisoveries userDisoveries = mUserProfile.userDisoveries;
    		mTextDiscoveriesValue.setText(Integer.toString(userDisoveries.discoveryCount));
    		
    		Stack<TextView> discoveriesNames = new Stack<TextView>();
    		discoveriesNames.add(mTextDiscoveriesItem3Name);
    		discoveriesNames.add(mTextDiscoveriesItem2Name);
    		discoveriesNames.add(mTextDiscoveriesItem1Name);

    		TextView discoverName = null;
    		
    		for (UserDiscover userDiscover : userDisoveries.userDiscoveries) {
    			if (discoveriesNames.isEmpty())
    				break;
    			
    			discoverName = discoveriesNames.pop();
    			discoverName.setText(userDiscover.name);
			}
    		
    	} else {
    		mContainerDiscoveries.setVisibility(View.GONE);
    	}
	}
	
	private void populdateLevelBarNew() {
		
		mTextLevelZero.setVisibility(View.GONE);
		
		int startInterval;
		int currentLevel = (int) mUserProfile.currentLevel;
		int maxLevel = (int) mUserProfile.maxLevel;
		
		Logger.i(TAG, "Current Level: " + Integer.toString(currentLevel) + " Max level: " + Integer.toString(maxLevel));
		
		boolean IsEven = maxLevel % 2 == 0;
		
		// If the maxLevel is even then startInterval = 0 else startInterval = 1
		if (IsEven) {
			startInterval = 0;
		}else{
			startInterval = 1;
		}
		
		TextView levelText = null;
		LinearLayout.LayoutParams levelParams = null;
		
		Context context = getActivity();
		int textColor = getResources().getColor(R.color.white);
		float textSize = (float) getResources().getDimensionPixelSize(R.dimen.profile_user_bar_level_bar_text_size);
		
		mContainerLevels.removeAllViews();
		
		boolean isVisible = true;
		
		for (int i = startInterval; i <= maxLevel; i++) {
			
			levelText = new TextView(context);
			levelText.setTextColor(textColor);
			levelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			levelText.setGravity(Gravity.CENTER);
			
     		levelParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
														LinearLayout.LayoutParams.MATCH_PARENT);
			levelParams.weight = 1;
			
			if(isVisible){
				levelText.setVisibility(View.VISIBLE);
				levelText.setText(String.valueOf(i));
				isVisible = false;
			}else{
				levelText.setVisibility(View.INVISIBLE);
				isVisible = true;
			}
						
			mContainerLevels.addView(levelText, levelParams);
		}
		
		// Set the progress 100% by the screen width
		int levelBarWidth = mContainerLevelBar.getWidth();
		mProgressLevelBar.setMax(levelBarWidth);
		
		// Get how many levels we have (including the invisible ones) 
		int numOfLevelTexts = mContainerLevels.getChildCount(); 
		
		// Get the size of each of them 
		int levelTextSize = levelBarWidth/numOfLevelTexts;
		
		// If its even levels then need to color another leveText
		int numOfLevelTextColoring = currentLevel;
		
		if(IsEven){
			numOfLevelTextColoring++;
		}
		
		// coloring the till the current level
		mProgressLevelBar.setProgress(levelTextSize*numOfLevelTextColoring);
				
	}
	
//	private void populdateLevelBar() {
//		
//		int minLevel = 0;
//		final int currentLevel = (int) mUserProfile.currentLevel;
//		final int maxLevel = (int) mUserProfile.maxLevel;
//		
//		Logger.i(TAG, "Current Level: " + Integer.toString(currentLevel) + " Max level: " + Integer.toString(maxLevel));
//		
//		boolean isEvenIndexSkiping = maxLevel % 2 == 0;
//		
//		// constructs the levels.
//		if (!isEvenIndexSkiping) {
//			minLevel = 1;
//		}
//		
//		mTextLevelZero.setVisibility(View.VISIBLE);
//		mTextLevelZero.setText(Integer.toString(minLevel));
//		
//		TextView levelText = null;
//		LinearLayout.LayoutParams levelParams = null;
//		
//		Context context = getActivity();
//		int textColor = getResources().getColor(R.color.white);
//		float textSize = (float) getResources().getDimensionPixelSize(R.dimen.profile_user_bar_level_bar_text_size);
//		int rightPadding = getResources().getDimensionPixelSize(R.dimen.profile_user_bar_level_bar_item_rigth_padding);
//		
//		mContainerLevels.removeAllViews();
//		
//		for (int i = minLevel + 2; i <= maxLevel; i += 2) {
//			
//			levelText = new TextView(context);
//			levelText.setTextColor(textColor);
//			levelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
//			levelText.setText(Integer.toString(i));
//			levelText.setPadding(0, 0, 0, 0);
//			levelText.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
//			
//			if (i == maxLevel) {
//				levelText.setPadding(0, 0, rightPadding, 0);
//			}
//			
//			levelParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
//														LinearLayout.LayoutParams.MATCH_PARENT);
//			levelParams.weight = 1;
//			
//			mContainerLevels.addView(levelText, levelParams);
//		}
//		
//		mProgressLevelBar.setMax(99);
//		
//		// measures the first static label, might be 1 or 0.
//		float levelBarWidth = mContainerLevelBar.getWidth();
//		float firstLevelWidth = mTextLevelZero.getWidth();
//		float otherLevelWidth = (levelBarWidth -  firstLevelWidth) / (maxLevel - minLevel);
//
//		float firstLevelPercentage = (firstLevelWidth / levelBarWidth) * 100;
//		int levelPercentageInOthers = (int) (((otherLevelWidth / (levelBarWidth-firstLevelWidth)) * 100) * ((100 - firstLevelPercentage) / 100));
//		
//		if (currentLevel < minLevel) {
//			return;
//		}
//		
//		if (currentLevel > maxLevel) {
//			mProgressLevelBar.setProgress(99);
//			return;
//		}
//		
//		if (currentLevel >= minLevel && currentLevel <= maxLevel) {
//			int finalPercentage =  (int) (firstLevelPercentage + ((currentLevel) * levelPercentageInOthers));
//			mProgressLevelBar.setProgress(finalPercentage);
//		}
//		
//	}
	
	
}
