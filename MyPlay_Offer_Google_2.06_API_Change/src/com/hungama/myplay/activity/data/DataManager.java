package com.hungama.myplay.activity.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.campaigns.ForYouActivity;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationOperation;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.CategoryTypeObject;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.DiscoverSearchResultIndexer;
import com.hungama.myplay.activity.data.dao.hungama.DownloadOperationType;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.MobileOperationType;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.Plan;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileLeaderboard;
import com.hungama.myplay.activity.data.dao.hungama.social.StreamItemCategory;
import com.hungama.myplay.activity.data.events.Event;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.OperationDefinition.Hungama.OperationId;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.operations.catchmedia.CampaignCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.CampaignListCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.DeviceActivationLoginCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.DeviceCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.JsonRPC2Methods;
import com.hungama.myplay.activity.operations.catchmedia.PartnerConsumerProxyCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerInfoReadOperation;
import com.hungama.myplay.activity.operations.catchmedia.TimeReadOperation;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.DiscoverRetrieveOperation;
import com.hungama.myplay.activity.operations.hungama.DiscoverSaveOperation;
import com.hungama.myplay.activity.operations.hungama.DiscoverSearchResultsOperation;
import com.hungama.myplay.activity.operations.hungama.DownloadOperation;
import com.hungama.myplay.activity.operations.hungama.FeedbackSubjectsOperation;
import com.hungama.myplay.activity.operations.hungama.FeedbackSubmitOperation;
import com.hungama.myplay.activity.operations.hungama.ForgotPasswordOperation;
import com.hungama.myplay.activity.operations.hungama.HungamaWrapperOperation;
import com.hungama.myplay.activity.operations.hungama.MediaCategoriesOperation;
import com.hungama.myplay.activity.operations.hungama.MediaContentOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.MobileVerifyCountryCheckOperation;
import com.hungama.myplay.activity.operations.hungama.MobileVerifyOperation;
import com.hungama.myplay.activity.operations.hungama.MyStreamSettingsOperation;
import com.hungama.myplay.activity.operations.hungama.NewVersionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.PreferencesRetrieveOperation;
import com.hungama.myplay.activity.operations.hungama.PreferencesSaveOperation;
import com.hungama.myplay.activity.operations.hungama.RadioLiveStationsOperation;
import com.hungama.myplay.activity.operations.hungama.RadioTopArtistSongsOperation;
import com.hungama.myplay.activity.operations.hungama.RadioTopArtistsOperation;
import com.hungama.myplay.activity.operations.hungama.RelatedVideoOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.SearchAutoSuggestOperation;
import com.hungama.myplay.activity.operations.hungama.SearchKeyboardOperation;
import com.hungama.myplay.activity.operations.hungama.SearchPopularKeywordOperation;
import com.hungama.myplay.activity.operations.hungama.ShareOperation;
import com.hungama.myplay.activity.operations.hungama.ShareSettingsOperation;
import com.hungama.myplay.activity.operations.hungama.SocialBadgeAlertOperation;
import com.hungama.myplay.activity.operations.hungama.SocialCommentsListingOperation;
import com.hungama.myplay.activity.operations.hungama.SocialCommentsPostOperation;
import com.hungama.myplay.activity.operations.hungama.SocialGetUrlOperation;
import com.hungama.myplay.activity.operations.hungama.SocialMyCollectionOperation;
import com.hungama.myplay.activity.operations.hungama.SocialMyStreamOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileBadgesOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileFavoriteMediaItemsOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileLeaderboardOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionOperation;
import com.hungama.myplay.activity.operations.hungama.TrackLyricsOperation;
import com.hungama.myplay.activity.operations.hungama.TrackSimilarOperation;
import com.hungama.myplay.activity.operations.hungama.TrackTriviaOperation;
import com.hungama.myplay.activity.operations.hungama.VersionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.VideoStreamingOperation;
import com.hungama.myplay.activity.operations.hungama.VideoStreamingOperationAdp;
import com.hungama.myplay.activity.player.PlayingQueue;
import com.hungama.myplay.activity.playlist.PlaylistOperation;
import com.hungama.myplay.activity.playlist.PlaylistRequest;
import com.hungama.myplay.activity.services.InventoryLightService;
import com.hungama.myplay.activity.services.MoodPrefetchingService;
import com.hungama.myplay.activity.ui.LoginActivity;
import com.hungama.myplay.activity.ui.OnApplicationStartsActivity;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * Global application's data provider.
 */
public class DataManager {

	private static final String TAG = "DataManager";
	
	public static final String FOLDER_MOODS_IMAGES = CacheManager.FOLDER_MOODS_IMAGES;
	public static final int CACHE_SIZE_MOODS_IMAGES = CacheManager.CACHE_SIZE_MOODS_IMAGES;
	
	public static final String FOLDER_TILES_CACHE = "tiles";
	public static final String FOLDER_PLAYER_MEDIA_ART_CACHE = "player_media_art";
	public static final String FOLDER_THUMBNAILS_CACHE = "thumbnails";
	public static final String FOLDER_CAMPAIGNS_CACHE = "campaigns";
	public static final String FOLDER_THUMBNAILS_FRIENDS = "social_friends_thumbnail";
	
	public static final String DEVICE = "android";
	
	private static final String VALUE = "value";
	
	private static DataManager sIntance;
	private boolean mIsTimeReadAlreadyCalled;

	private Context mContext;
	
	private final ServerConfigurations mServerConfigurations;
	private final ApplicationConfigurations mApplicationConfigurations;
	private final DeviceConfigurations mDeviceConfigurations;
	
	private CacheManager mCacheManager;
	private EventManager mEventManager;
	
	private static String mDeviceDensity;
	
	private CommunicationManager mMediaDetailsCommunicationManager = null;
	
	private CommunicationManager mTrackSimilarCommunicationManager = null;
	private CommunicationManager mTrackLyricsCommunicationManager = null;
	private CommunicationManager mTrackTriviaCommunicationManager = null;
	
	private CommunicationManager mSearchSuggestedCommunicationManager = null;
	private CommunicationManager mSearchCommunicationManager = null;
	private CommunicationManager mRelatedVideoCommunicationManager = null;
	private CommunicationManager mSubscriptionPlansCommunicationManager = null;
	
	
	// ======================================================
	// General.
	// ======================================================
	
	public static final synchronized DataManager getInstance(Context applicationContext) {
		if (sIntance == null) {
			sIntance = new DataManager(applicationContext);			
		}
		return sIntance;
	}
	
	public boolean ismIsTimeReadAlreadyCalled() {
		return mIsTimeReadAlreadyCalled;
	}

	public void setmIsTimeReadAlreadyCalled(boolean mIsTimeReadAlreadyCalled) {
		this.mIsTimeReadAlreadyCalled = mIsTimeReadAlreadyCalled;
	}
	
	private DataManager (Context context) {
		mContext = context;
		
		// initializes application's configuration managers.
		mServerConfigurations = new ServerConfigurations(mContext);
		mApplicationConfigurations = new ApplicationConfigurations(mContext);
		mDeviceConfigurations = new DeviceConfigurations(mContext);
		
		// initializes application's resources managers. 
		mCacheManager = new CacheManager(mContext);
		
		// sets the scale name
		mDeviceDensity = getDisplayDensity();
		
		// sets the time read to false for the first time.
		setmIsTimeReadAlreadyCalled(false);
	}
	
	public Context getApplicationContext() {
		return mContext;
	}

	public ServerConfigurations getServerConfigurations() {
		return mServerConfigurations;
	}

	public ApplicationConfigurations getApplicationConfigurations() {
		return mApplicationConfigurations;
	}

	public DeviceConfigurations getDeviceConfigurations() {
		return mDeviceConfigurations;
	}
	
	public static String getDisplayDensityLabel() {
		return mDeviceDensity;
	}
	
	/**
	 * Notifies the module that the application has been in the process to be started
	 * when the user clicked on it's launcher icon.
	 */
	public void notifyApplicationStarts() {
		List<Event> events = mCacheManager.getStoredEvents();
		mEventManager = new EventManager(mContext, mServerConfigurations.getServerUrl(), events);
		// flush all stored evens.
		if (isDeviceOnLine())
			mEventManager.flushEvents();
	}
	
	/**
	 * Notifies the module that the application has been in the process to be finished
	 * when the user has clicked the last "Back" button to exit it.
	 */
	public void notifyApplicationExits() {
		if (mEventManager != null) {
			// stop any posting events and stores the rest.
			mEventManager.stopPostingEvents();
			// stores the events in the internal storage.
			List<Event> pandingEvents = mEventManager.getEvents();
			if (!Utils.isListEmpty(pandingEvents)) {
				mCacheManager.storeEvents(pandingEvents);
			}
			// clears the queue.
			mEventManager.clearQueue();
		}
	}
	
	
	// ======================================================
	// Application internal data flow methods.
	// ======================================================

	public void createDevice(CommunicationOperationListener listener) {

		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new CMDecoratorOperation(mServerConfigurations.getServerUrl(), 
												   new DeviceCreateOperation(getApplicationContext())), listener,mContext);
	}
	
	public void readPartnerInfo(CommunicationOperationListener listener) {
		
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new CMDecoratorOperation(mServerConfigurations.getServerUrl(), 
												   new PartnerInfoReadOperation(getApplicationContext())), listener,mContext);
	}
	
	/**
	 * Posts signup / login fields to retrieve an activation code for the application.
	 */
	public void createPartnerConsumerProxy(Map<String, Object> signupFields, 
									long setId, CommunicationOperationListener listener, boolean isSkipSelected) {
		// populates the hidden fields.
		
		if (signupFields.containsKey(DeviceConfigurations.HARDWARE_ID)) {
			// creates the new value for it.
			Map<String, Object> valueMap = new HashMap<String, Object>();
			valueMap.put(VALUE, mDeviceConfigurations.getHardwareId());
			// override the existing one. 
			signupFields.put(DeviceConfigurations.HARDWARE_ID, valueMap);
		}
		
		if (signupFields.containsKey(ApplicationConfigurations.PARTNER_USER_ID)) {
			// creates the new value for it.
			Map<String, Object> valueMap = new HashMap<String, Object>();
			valueMap.put(VALUE, mApplicationConfigurations.getPartnerUserId());
			// override the existing one. 
			signupFields.put(ApplicationConfigurations.PARTNER_USER_ID, valueMap);
		}
		
		// performs the execution to the web service. 
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new CMDecoratorOperation(mServerConfigurations.getServerUrl(), 
												   new PartnerConsumerProxyCreateOperation(mContext, signupFields, setId, isSkipSelected)), listener,mContext);
	}
	
	public void createDeviceActivationLogin(String activationCode, CommunicationOperationListener listener) {
		
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new CMDecoratorOperation(mServerConfigurations.getServerUrl(), 
												   new DeviceActivationLoginCreateOperation(mContext, activationCode)), listener,mContext);
	}
	
	public void forgotPassword(String userEmail, CommunicationOperationListener listener) {
		
		String serviceUrl = mContext.getResources().getString(R.string.hungama_forgot_password_server_url);
		String forgotPasswordAuthKey = mContext.getResources().getString(R.string.hungama_forgot_password_key);
		
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new ForgotPasswordOperation(serviceUrl, forgotPasswordAuthKey, userEmail), listener,mContext);
	}
	
	
	// ======================================================
	// Campaign
	// ======================================================
	
	public void getCampignsList(CommunicationOperationListener listener){
		
		if (isDeviceOnLine()) {
			
			CommunicationManager communicationManager = new CommunicationManager();
			communicationManager.performOperationAsync(
					new CMDecoratorOperation(mServerConfigurations.getServerUrl(), 
							new CampaignListCreateOperation(mContext)), new WrapperCampaignListOperationListener(listener),mContext);
		}else{
			
			listener.onStart(OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ);
			
			List<String> list = getStoredCampaignList();
			if (list != null) {
				
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(CampaignListCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN_LIST, list);
				
				listener.onSuccess(OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ, resultMap);
			} else {
				// No internet connectivity and no cached media items, failure.
				listener.onFailure(OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ, 
						ErrorType.NO_CONNECTIVITY, mContext.getResources().getString(R.string.application_error_no_connectivity));
			}
		}
	}
	
	public void getCampigns(CommunicationOperationListener listener, List<String> campaignList){
		
		if (isDeviceOnLine()) {
			if(campaignList != null && !campaignList.isEmpty()){
				CommunicationManager communicationManager = new CommunicationManager();
				communicationManager.performOperationAsync(
						new CMDecoratorOperation(mServerConfigurations.getServerUrl(), 
								new CampaignCreateOperation(mContext, campaignList)), 
								new WrapperCampaignOperationListener(listener),mContext);
			}else{
				// Fake empty Campaigns cause the call to CamapignsList got back empty
				Map<String, Object> resultMap = new HashMap<String, Object>();
				listener.onSuccess(OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ, resultMap);
			}
			
		}else{
			listener.onStart(OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ);
			
			List<Campaign> list = getStoredCampaign();
			if (list != null) {
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(CampaignCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN, list);
				
				listener.onSuccess(OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ, resultMap);
			} else {
				// No internet connectivity and no cached media items, failure.
				listener.onFailure(OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ, 
						ErrorType.NO_CONNECTIVITY, mContext.getResources().getString(R.string.application_error_no_connectivity));
			}
			
		}
	}
	

	// ======================================================
	// Playlist
	// ======================================================
	
	public void playlistOperation(CommunicationOperationListener listener, long playlistId, String playlistName, String trackList, JsonRPC2Methods method) {
		if(isDeviceOnLine()){
			CommunicationManager communicationManager = new CommunicationManager();
			communicationManager.performOperationAsync(new CMDecoratorOperation(mServerConfigurations.getServerUrl(), 
													new PlaylistOperation(
															getApplicationContext(),
															playlistId, 
															playlistName,
															trackList,
															method)), listener,mContext);
		}
	}
	
	
	// ======================================================
	// Time Read from CM
	// ======================================================
	
	public void getTimeRead(CommunicationOperationListener listener) {		
		
		if (!ismIsTimeReadAlreadyCalled()) {
		
			if (isDeviceOnLine()) {
								
				CommunicationManager communicationManager = new CommunicationManager();
				communicationManager.performOperationAsync(new CMDecoratorOperation(mServerConfigurations.getServerUrl(), new TimeReadOperation(mContext)), listener,mContext);
				setmIsTimeReadAlreadyCalled(true);
				
//				Toast.makeText(getApplicationContext(), "Time read called ! ", Toast.LENGTH_LONG).show();
			}
		}
	}

	// ======================================================
	// Application data getters - Media getters.
	// ======================================================
	
	/**
	 * Retrieves a list of media items to be presented as tiles.</br>
	 * This method checks if there is connectivity, if so, it will performs a server request
	 * to get the items, if not. it will check if there are cached media items in the application's
	 * internal storage. If it does not have any media items in the internal storage, the:
	 * {@code CommunicationOperationListener.onFailure} will be invoked.
	 * 
	 *  This function stores automatically media items in the internal storage for cache.
	 *  
	 * @param contentType
	 * @param itemCategoryType
	 * @param category
	 * @param listener
	 */
	public void getMediaItems(MediaContentType mediaContentType, MediaCategoryType mediaCategoryType, 
											Category category, CommunicationOperationListener listener) {
//		if (isDeviceOnLine()) {
			// performs server call to get the media items.
			CommunicationManager communicationManager = new CommunicationManager();
			communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
					new MediaContentOperation(mContext, mServerConfigurations.getHungamaServerUrl(),
							mServerConfigurations.getHungamaAuthKey(), 
							mediaContentType,
							mediaCategoryType, 
							category, 
							mApplicationConfigurations.getPartnerUserId())), 
							listener,mContext);
//		} else {
//			/*
//			 * Invoking the listener callback to fake the behavior of the operation.
//			 */
//			int operationId = getOperationIdForMediaCategoryType(mediaCategoryType);
//			listener.onStart(operationId);
//			// gets the media items from the cache.
//			List<MediaItem> cachedMediaItems = mCacheManager.getStoredMediaItems(mediaContentType, mediaCategoryType);
//			if (!Utils.isListEmpty(cachedMediaItems)) {
//				// packages the the media items as a result map.
//				Map<String, Object> resultMap = new HashMap<String, Object>();
//				resultMap.put(MediaContentOperation.RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE, mediaContentType);
//				resultMap.put(MediaContentOperation.RESULT_KEY_OBJECT_MEDIA_CATEGORY_TYPE, mediaCategoryType);
//				resultMap.put(MediaContentOperation.RESULT_KEY_OBJECT_MEDIA_ITEMS, cachedMediaItems);
//				// got some cache media items, retrieves them to client.
//				listener.onSuccess(operationId, resultMap);
//				
//			} else {
//				// No internet connectivity and no cached media items, failure.
//				listener.onFailure(operationId, ErrorType.NO_CONNECTIVITY, 
//						mContext.getResources().getString(R.string.application_error_no_connectivity));
//			}
//		}
	}
	
	public static int getOperationIdForMediaCategoryType(MediaCategoryType mediaCategoryType) {
		
		if (mediaCategoryType == MediaCategoryType.LATEST) {
			return OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST;
		}
		
		if (mediaCategoryType == MediaCategoryType.FEATURED) {
			return OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED;
		}
		
		return OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED;
	}
	
	public void storeMediaItems(MediaContentType mediaContentType, MediaCategoryType mediaCategoryType, List<MediaItem> mediaItems) {
		mCacheManager.storeMediaItems(mediaContentType, mediaCategoryType, mediaItems);
	}
	
	public void getMediaDetails(MediaItem mediaItem, PlayerOption playerOption ,CommunicationOperationListener listener) {
		mMediaDetailsCommunicationManager = new CommunicationManager();
		mMediaDetailsCommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new MediaDetailsOperation(mServerConfigurations.getHungamaServerUrl(), 
				mServerConfigurations.getHungamaAuthKey(), mApplicationConfigurations.getPartnerUserId(), mediaItem, playerOption)), listener,mContext);
	}
	
	public void cancelGetMediaDetails() {
		if (mMediaDetailsCommunicationManager != null && mMediaDetailsCommunicationManager.isRunning()) {
			mMediaDetailsCommunicationManager.cancelAnyRunningOperation();
			mMediaDetailsCommunicationManager = null;
		}
	}
	
	public void getMediaCategories(MediaContentType mediaContentType, CommunicationOperationListener listener) {
		
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new MediaCategoriesOperation(mServerConfigurations.getHungamaServerUrl(), 
				mServerConfigurations.getHungamaAuthKey(), mediaContentType)), listener,mContext);
	}

	
	// ======================================================
	// Application data getters - Search media.
	// ======================================================
	
	public void getSearchPopularSerches(CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new SearchPopularKeywordOperation(mServerConfigurations.getHungamaServerUrl(), 
				mServerConfigurations.getHungamaAuthKey(), mApplicationConfigurations.getPartnerUserId())), listener,mContext);
	}
	
	public void getSearchAutoSuggest(String query, String queryLength, CommunicationOperationListener listener) {
		
		mSearchSuggestedCommunicationManager = new CommunicationManager();
		mSearchSuggestedCommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new SearchAutoSuggestOperation(mServerConfigurations.getHungamaServerUrl(), 
				query, queryLength, mServerConfigurations.getHungamaAuthKey(), mApplicationConfigurations.getPartnerUserId())), listener,mContext);
	}
	
	/**
     * Cancels the search operation of suggested keywords.
     */
    public void cancelGetSearchAutoSuggest() {
        if (mSearchSuggestedCommunicationManager != null && mSearchSuggestedCommunicationManager.isRunning()) {
        	mSearchSuggestedCommunicationManager.cancelAnyRunningOperation();
        	mSearchSuggestedCommunicationManager = null;
        }
    }
    
    public void getSearchKeyboard(String query, String type, String startIndex, String queryLength, CommunicationOperationListener listener) {
    	mSearchCommunicationManager = new CommunicationManager();
    	mSearchCommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new SearchKeyboardOperation(mServerConfigurations.getHungamaServerUrl(), 
				query, type, startIndex, queryLength, mServerConfigurations.getHungamaAuthKey(), mApplicationConfigurations.getPartnerUserId())), listener,mContext);
	}
    
    /**
     * Cancels the search operation.
     */
    public void cancelGetSearch() {
        if (mSearchCommunicationManager != null && mSearchCommunicationManager.isRunning()) {
        	mSearchCommunicationManager.cancelAnyRunningOperation();
        	mSearchCommunicationManager = null;
        }
    }
    
    
	// ======================================================
	// Application data getters - Video.
	// ======================================================
    
    public void getVideoDetailsAdp(MediaItem mediaItem, int networkSpeed, String networkType, String contentFormat, CommunicationOperationListener listener,String googleEmailId) {
		CommunicationManager communicationManager = new CommunicationManager();

		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, 
				mContext, 
				new VideoStreamingOperationAdp(
						mServerConfigurations.getHungamaServerUrl(), 
				mApplicationConfigurations.getPartnerUserId(), String.valueOf(mediaItem.getId()), 
				getDisplayDensity(), mServerConfigurations.getHungamaAuthKey(), 
				networkSpeed, networkType, contentFormat, googleEmailId)), 
				listener,mContext);

	}
    
	public void getVideoDetails(MediaItem mediaItem, CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new VideoStreamingOperation(mServerConfigurations.getHungamaServerUrl(), 
				mApplicationConfigurations.getPartnerUserId(), String.valueOf(mediaItem.getId()), getDisplayDensity(), mServerConfigurations.getHungamaAuthKey())), listener,mContext);
	}
	
	public void getRelatedVideo(MediaTrackDetails mediaTrackDetails, MediaItem mediaItem, CommunicationOperationListener listener) {
		mRelatedVideoCommunicationManager = new CommunicationManager();
		mRelatedVideoCommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new RelatedVideoOperation(mServerConfigurations.getHungamaServerUrl(), 
				String.valueOf(mediaTrackDetails.getAlbumId()), mediaItem.getMediaContentType(), mediaItem.getMediaType(), mServerConfigurations.getHungamaAuthKey())), listener,mContext);
	}
	
	public void cancelGetRelatedVideo() {
		if (mRelatedVideoCommunicationManager != null && mRelatedVideoCommunicationManager.isRunning()) {
			mRelatedVideoCommunicationManager.cancelAnyRunningOperation();
			mRelatedVideoCommunicationManager = null;
		}
	}
	
	
	// ======================================================
	// Upgrade (Subscription)
	// ======================================================
    
    public void getCurrentSubscriptionPlan(CommunicationOperationListener listener, String googleEmailId) {
    	// checks in the cache first.
    	SubscriptionCheckResponse currentPlan = mCacheManager.getStoredCurrentPlan();
		if (currentPlan != null && !isDeviceOnLine()) {
			listener.onStart(OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK);
			// creates the result map.
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK, currentPlan);
			listener.onSuccess(OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK, resultMap);
			
		} else {		
			mSubscriptionPlansCommunicationManager = new CommunicationManager();
			mSubscriptionPlansCommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
					new SubscriptionCheckOperation(mContext, 
							mServerConfigurations.getHungamaSubscriptionServerUrl(), 
							mApplicationConfigurations.getPartnerUserId(), 
							mServerConfigurations.getHungamaAuthKey(), googleEmailId)), 
					listener,mContext);
		}
	}
    
    public boolean deleteCurrentSubscriptionPlan() {
    	return mCacheManager.deleteStoredCurrentPlan();
    }
        
    public void getSubscriptionPlans(int planId, SubscriptionType subscriptionType, CommunicationOperationListener listener) {
    	// checks in the cache first.
		List<Plan> subscriptionPlans = mCacheManager.getStoredPlans();
		if (!Utils.isListEmpty(subscriptionPlans) && !isDeviceOnLine()) {
			listener.onStart(OperationDefinition.Hungama.OperationId.SUBSCRIPTION);
			// creates the result map.
			Map<String, Object> resultMap = new HashMap<String, Object>();
			SubscriptionResponse subscriptionResponse = new SubscriptionResponse(null, null, null, null, null, subscriptionPlans);
			subscriptionResponse.setSubscriptionType(subscriptionType);
			resultMap.put(SubscriptionOperation.RESPONSE_KEY_SUBSCRIPTION, subscriptionResponse);
			listener.onSuccess(OperationDefinition.Hungama.OperationId.SUBSCRIPTION, resultMap);
			
		} else {		
			mSubscriptionPlansCommunicationManager = new CommunicationManager();
			mSubscriptionPlansCommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new SubscriptionOperation(mServerConfigurations.getHungamaSubscriptionServerUrl(), 
					String.valueOf(planId), Utils.TEXT_EMPTY, mApplicationConfigurations.getPartnerUserId(), subscriptionType, mServerConfigurations.getHungamaAuthKey(),"0",null,null, null)), //inserted by Hungama "0"
					new WrapperMediaContentOperationListener(listener),mContext);
		}
	}
    
    public void getSubscriptionCharge(int planId, String planType, SubscriptionType subscriptionType, CommunicationOperationListener listener, String code, String purchaseToken, String googleEmailId) {	
			mSubscriptionPlansCommunicationManager = new CommunicationManager();
			mSubscriptionPlansCommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new SubscriptionOperation(mServerConfigurations.getHungamaSubscriptionServerUrl(), 
					String.valueOf(planId), planType, mApplicationConfigurations.getPartnerUserId(), subscriptionType, mServerConfigurations.getHungamaAuthKey(), code, purchaseToken, googleEmailId, OnApplicationStartsActivity.getAffiliateId())), listener,mContext);//inserted by Hungama "tac"
		}
    
    public void storeCurrentSubscriptionPlan(SubscriptionCheckResponse subscriptionCheckResponse) {
    	if (mCacheManager.storeSubscriptionCurrentPlan(subscriptionCheckResponse)) {
    		mApplicationConfigurations.setIsUserHasSubscriptionPlan(true);
    		mApplicationConfigurations.setUserSubscriptionPlanDate(subscriptionCheckResponse.getPlan().getValidityDate());
    		mApplicationConfigurations.setUserSubscriptionPlanDatePurchase(subscriptionCheckResponse.getPlan().getPurchaseDate());
		} else {
			mApplicationConfigurations.setIsUserHasSubscriptionPlan(false);
		}
    }
    
    public SubscriptionCheckResponse getStoredCurrentPlan() {
    	return mCacheManager.getStoredCurrentPlan();
    }
    
    public boolean storeSubscriptionCurrentPlan(SubscriptionCheckResponse subscriptionCheckResponse) {
    	return mCacheManager.storeSubscriptionCurrentPlan(subscriptionCheckResponse);
    }
    
    
    public List<Plan> getStoredSubscriptionPlans() {
    	return mCacheManager.getStoredPlans();
    }
    
    public boolean storeSubscriptionPlans(List<Plan> plans) {
    	return mCacheManager.storeSubscriptionPlans(plans);
    }

	    
    public void versionCheck(CommunicationOperationListener listener) {		
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
				new VersionCheckOperation(mServerConfigurations.getHungamaServerUrl(), 
						mApplicationConfigurations.getPartnerUserId(), 
						mServerConfigurations.getHungamaAuthKey(),
						mServerConfigurations.getReferralId())),
    			listener,mContext);
	}
    
    public void newVersionCheck(CommunicationOperationListener listener) {		
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
				new NewVersionCheckOperation(mServerConfigurations.getmHungamaVersionCheckServerUrl(), 
						OnApplicationStartsActivity.getOemPackageName(),//mContext.getPackageName(), 
						mContext.getResources().getString(R.string.app_ver))),
    			listener,mContext);
	}
    
	// ======================================================
	// Mobile Verification.
	// ======================================================
	    
    public void getMobileVerification(String msisdn, String password, MobileOperationType mobileOperationType, CommunicationOperationListener listener) {
    	CommunicationManager communicationManager = new CommunicationManager();
    	communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new MobileVerifyOperation(mServerConfigurations.getHungamaMobileVerificationServerUrl(), 
				msisdn, password, mApplicationConfigurations.getPartnerUserId(), mobileOperationType, mServerConfigurations.getHungamaAuthKey())), listener,mContext);
    }
    
    public void checkCountry(String msisdn, CommunicationOperationListener listener) {
    	CommunicationManager communicationManager = new CommunicationManager();
    	communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
    			new MobileVerifyCountryCheckOperation(mServerConfigurations.getHungamaMobileVerificationServerUrl(), 
				msisdn, mServerConfigurations.getHungamaAuthKey())), listener,mContext);
    }
    
    
    // ======================================================
    // Download
    // ======================================================
 // inserted by Hungama affiliate Id
    public void getDownload(int planId, long contentId, String msisdn, String contentType, DownloadOperationType downloadOperationType, CommunicationOperationListener listener) {
    	CommunicationManager communicationManager = new CommunicationManager();
    	communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new DownloadOperation(mServerConfigurations.getHungamaDownloadServerUrl(), 
    			mApplicationConfigurations.getPartnerUserId(), msisdn, String.valueOf(planId), String.valueOf(contentId), 
    			contentType, "android", getDisplayDensity(), downloadOperationType,  mServerConfigurations.getHungamaAuthKey(),OnApplicationStartsActivity.getAffiliateId(), OnApplicationStartsActivity.mHardwareId)), listener,mContext);
    }
    
    
	// ======================================================
	// Music.
	// ======================================================
	
	public PlayingQueue getStoredPlayingQueue() {
		return new PlayingQueue(null, 0);
	}
	
	public void getTrackSimilar(Track track, CommunicationOperationListener listener) {
		mTrackSimilarCommunicationManager = new CommunicationManager();
		mTrackSimilarCommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new TrackSimilarOperation(mServerConfigurations.getHungamaServerUrl(), 
							mServerConfigurations.getHungamaAuthKey(), mApplicationConfigurations.getPartnerUserId(), track)), listener,mContext);
	}
	
	public void cancelGetTrackSimilar() {
		if (mTrackSimilarCommunicationManager != null && mTrackSimilarCommunicationManager.isRunning()) {
			mTrackSimilarCommunicationManager.cancelAnyRunningOperation();
			mTrackSimilarCommunicationManager = null;
		}
	}
	
	public void getTrackLyrics(Track track, CommunicationOperationListener listener) {
		mTrackLyricsCommunicationManager = new CommunicationManager();
		mTrackLyricsCommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new TrackLyricsOperation(mServerConfigurations.getHungamaServerUrl(), 
																		mServerConfigurations.getHungamaAuthKey(), track)), listener,mContext);
	}
	
	public void cancelGetTrackLyrics() {
		if (mTrackLyricsCommunicationManager != null && mTrackLyricsCommunicationManager.isRunning()) {
			mTrackLyricsCommunicationManager.cancelAnyRunningOperation();
			mTrackLyricsCommunicationManager = null;
		}
	}
	
	public void getTrackTrivia(Track track, CommunicationOperationListener listener) {
		mTrackTriviaCommunicationManager = new CommunicationManager();
		mTrackTriviaCommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new TrackTriviaOperation(mServerConfigurations.getHungamaServerUrl(), 
																		mServerConfigurations.getHungamaAuthKey(), track)), listener,mContext);
	}
	
	public void cancelGetTrackTrivia() {
		if (mTrackTriviaCommunicationManager != null && mTrackTriviaCommunicationManager.isRunning()) {
			mTrackTriviaCommunicationManager.cancelAnyRunningOperation();
			mTrackTriviaCommunicationManager = null;
		}
	}
	
	
	// ======================================================
	// Web Radio.
	// ======================================================
	
	/**
	 * Retrieves all the Live stations as Media items.
	 */
	public void getRadioLiveStations(CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new RadioLiveStationsOperation(mServerConfigurations.getHungamaServerUrl(), 
														mServerConfigurations.getHungamaAuthKey())), listener,mContext);
	}
	
	/**
	 * Retrieves all the Top Artists as Media items.
	 */
	public void getRadioTopArtists(CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new RadioTopArtistsOperation(mServerConfigurations.getHungamaServerUrl(), 
														mServerConfigurations.getHungamaAuthKey())), listener,mContext);
	}
	
	/**
	 * Retrieves all the Artists songs as Media items.
	 */
	public void getRadioTopArtistSongs(MediaItem artistItem, CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
														new RadioTopArtistSongsOperation(mServerConfigurations.getHungamaServerUrl(), 
														mServerConfigurations.getHungamaAuthKey(), artistItem)), 
														listener,mContext);
	}
	
	
	// ======================================================
	// Events.
	// ======================================================	
	
	private final Object mEventMutext = new Object();
	
	/**
	 * Posts an event in the CM servers.
	 * @param event
	 */
	public void addEvent(Event event) {
		synchronized (mEventMutext) {
			if(mEventManager == null){
				notifyApplicationStarts();
			}
			
			mEventManager.addEvent(event);		
		}
	}

	
	// ======================================================
	// Moods.
	// ======================================================
	
	public enum MoodIcon {
		SMALL,
		BIG;
	}
	
	public boolean storeMoods(List<Mood> moods) {
		return mCacheManager.storeMoods(moods);
	}
	
	public List<Mood> getStoredMoods() {
		return mCacheManager.getStoredMoods();
	}
	
	/**
	 * Starts prefetching moods if they are not exist in the cache.
	 */
	public void prefetchMoodsIfNotExists() {
		List<Mood> moods = mCacheManager.getStoredMoods();
		boolean hasSuccessed = mApplicationConfigurations.hasSuccessedPrefetchingMoods();
		if (Utils.isListEmpty(moods) || hasSuccessed) {
			Intent prefetchMoods = new Intent(getApplicationContext(), MoodPrefetchingService.class);
			mContext.startService(prefetchMoods);
		}
	}
	
	public Drawable getMoodIcon(Mood mood, MoodIcon moodIcon) {
		try {
			return mCacheManager.getMoodIcon(mood, moodIcon);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	// ======================================================
	// Preferences.
	// ======================================================
	
	public void getPreferences(final CommunicationOperationListener listener) {
		/*
		 * Most of the time the preferences should be get from the cache
		 * and won't be necessary to perform the "online ? webserice : cache" pattern. 
		 */
		
		// checks in the cache first.
		List<CategoryTypeObject> categoryTypeObject = mCacheManager.getStoredPreferences();
		if (categoryTypeObject != null && categoryTypeObject.size() > 0) {
			listener.onStart(OperationDefinition.Hungama.OperationId.PREFERENCES_GET);
			// creates the result map.
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES, categoryTypeObject);
			resultMap.put(MediaCategoriesOperation.RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE, MediaContentType.MUSIC);
			
			// done, invokes success.
			listener.onSuccess(OperationDefinition.Hungama.OperationId.PREFERENCES_GET, resultMap);
		} else {
			if (isDeviceOnLine()) {
				CommunicationManager communicationManager = new CommunicationManager();
				communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
						new MediaCategoriesOperation(mServerConfigurations.getHungamaServerUrl(), 
													 mServerConfigurations.getHungamaAuthKey(), 
													 MediaContentType.MUSIC)), 
													 new CommunicationOperationListener() {

									@Override
									public void onStart(int operationId) {
										listener.onStart(OperationDefinition.Hungama.OperationId.PREFERENCES_GET);
									}
							
									@Override
									public void onSuccess(int operationId, Map<String, Object> responseObjects) {
										if (responseObjects.containsKey(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES)) {
											// gets the categoris and cache them.
											List<CategoryTypeObject> categories = 
													(List<CategoryTypeObject>) responseObjects.get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);
											mCacheManager.storePreferences(categories);
										}
										listener.onSuccess(OperationDefinition.Hungama.OperationId.PREFERENCES_GET, responseObjects);
									}
									
									@Override
									public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
										listener.onFailure(OperationDefinition.Hungama.OperationId.PREFERENCES_GET, errorType, errorMessage);
									}
								},mContext);
			}
		}
		
	}
	
	
	// ======================================================
	// Discover.
	// ======================================================
	
	public void getDiscoverSearchResult(Discover discover, DiscoverSearchResultIndexer discoverSearchResultIndexer, CommunicationOperationListener listener) {
		
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
				new DiscoverSearchResultsOperation(mServerConfigurations.getHungamaServerUrl(), 
												   mServerConfigurations.getHungamaAuthKey(), 
												   mApplicationConfigurations.getPartnerUserId(), 
												   discover,
												   discoverSearchResultIndexer)), listener,mContext);
	}
	
	public void saveDiscover(Discover discover, boolean shouldRestartIfSuccess, CommunicationOperationListener listener) {
		
		CommunicationManager communicationManager = new CommunicationManager();
		
		/*
		 * Due to a weird crash causes by the BadgesAndCoinsActivity,
		 * if we should restart the Discover activity after saving,
		 * we disable the ability to launch the BadgesAndCoinsActivity checks.
		 */
		CommunicationOperation operation = null;
		if (!shouldRestartIfSuccess) {
			operation = new HungamaWrapperOperation(listener, mContext, 
					new DiscoverSaveOperation(mServerConfigurations.getHungamaServerUrl(), 
							  mServerConfigurations.getHungamaAuthKey(), 
							  mApplicationConfigurations.getPartnerUserId(), 
							  discover, shouldRestartIfSuccess));
		} else {
			operation = new DiscoverSaveOperation(mServerConfigurations.getHungamaServerUrl(), 
					  mServerConfigurations.getHungamaAuthKey(), 
					  mApplicationConfigurations.getPartnerUserId(), 
					  discover, shouldRestartIfSuccess);
		}
		
		communicationManager.performOperationAsync(operation, listener,mContext);
	}
	
	public void getDiscoveries(String userId, final CommunicationOperationListener listener) {
		
		/*
		 * To get the saved discoveries for the user we must get his
		 * preferences first.
		 */
		List<CategoryTypeObject> categoryTypeObjects = mCacheManager.getStoredPreferences();
		
		final CommunicationManager communicationManager = new CommunicationManager();
		
		if (!Utils.isListEmpty(categoryTypeObjects)) {
			// can retrieves the discoveries.
			DiscoverRetrieveOperation discoverRetrieveOperation = new DiscoverRetrieveOperation(
																		mServerConfigurations.getHungamaServerUrl(), 
																		mServerConfigurations.getHungamaAuthKey(), 
																		userId,
																		categoryTypeObjects, 
																		mCacheManager.getStoredMoods());
			communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, discoverRetrieveOperation), listener,mContext);
		} else {
			/*
			 * Wrapping the listener with a custom one to catch the success of storing the
			 * preferences in the cache / getting from the WS - and then calling
			 * the get discoveries.
			 */
			getPreferences(new WrapperGetPreferencesOperationListener(listener));
		}
		
	}
	
	
	// ======================================================
	// My Preferences.
	// ======================================================	
	
	public void getMyPreferences(final CommunicationOperationListener listener) {
		if (isDeviceOnLine()) {
			CommunicationManager communicationManager = new CommunicationManager();
			communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
					new PreferencesRetrieveOperation(mServerConfigurations.getHungamaServerUrl(), 
												 mServerConfigurations.getHungamaAuthKey(), 
												 mApplicationConfigurations.getPartnerUserId())), listener,mContext);
		}	
	}
	
	public void saveMyPreferences(String preferencesIdList, final CommunicationOperationListener listener) {
		if (isDeviceOnLine()) {
			CommunicationManager communicationManager = new CommunicationManager();
			communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
					new PreferencesSaveOperation(mServerConfigurations.getHungamaServerUrl(), 
												 mServerConfigurations.getHungamaAuthKey(), 
												 mApplicationConfigurations.getPartnerUserId(), preferencesIdList)), listener,mContext);
		}	
	}
	
	
	// ======================================================
	// Social.
	// ======================================================
	
	private CommunicationManager mMyStreamItemsCommunicationManager = null;
	private CommunicationManager mProfileLeaderboardCommunicationManager = null;
	private CommunicationManager mProfileBadgescommunicationManager = null;
	
	/**
	 * Retrieves the StreamItems to the given category.
	 */
	public void getMyStreamItems(StreamItemCategory streamItemCategory, CommunicationOperationListener listener) {
		SocialMyStreamOperation socialMyStreamOperation = 
				new SocialMyStreamOperation(mServerConfigurations.getHungamaSocialServerUrl(),
											mServerConfigurations.getHungamaAuthKey(),
											mApplicationConfigurations.getPartnerUserId(), streamItemCategory);
		
		mMyStreamItemsCommunicationManager = new CommunicationManager();
		mMyStreamItemsCommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, socialMyStreamOperation), listener,mContext);
	}
	
	public void cancelGetMyStreamItems() {
		if (mMyStreamItemsCommunicationManager != null && mMyStreamItemsCommunicationManager.isRunning())
			mMyStreamItemsCommunicationManager.cancelAnyRunningOperation();
			mMyStreamItemsCommunicationManager = null;
	}
	
	public void getUserProfile(String userId, CommunicationOperationListener listener) {
		
		if (TextUtils.isEmpty(userId)) {
			// sets the application user's id as default.
			userId = mApplicationConfigurations.getPartnerUserId();
		}
		
		SocialProfileOperation socialProfileOperation = 
				new SocialProfileOperation(mServerConfigurations.getHungamaSocialServerUrl(), 
										   mServerConfigurations.getHungamaAuthKey(), userId);
		
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, socialProfileOperation), listener,mContext);
	}
	
	public void getProfileLeaderboard(String userId, ProfileLeaderboard.TYPE type,
													 ProfileLeaderboard.PERIOD period,
													 CommunicationOperationListener listener) {
		SocialProfileLeaderboardOperation operation = 
				new SocialProfileLeaderboardOperation(mServerConfigurations.getHungamaSocialServerUrl(), 
													  mServerConfigurations.getHungamaAuthKey(), userId, type, period);
		mProfileLeaderboardCommunicationManager = new CommunicationManager();
		mProfileLeaderboardCommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, operation), listener,mContext);
	}
	
	public void cancelGetProfileLeaderboard() {
		if (mProfileLeaderboardCommunicationManager != null && 
				mProfileLeaderboardCommunicationManager.isRunning()) {
			
			mProfileLeaderboardCommunicationManager.cancelAnyRunningOperation();
			mProfileLeaderboardCommunicationManager = null;
		}
	}
	
	public void getProfileBadges(String userId, CommunicationOperationListener listener) {
		mProfileBadgescommunicationManager = new CommunicationManager();
		mProfileBadgescommunicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new SocialProfileBadgesOperation(mServerConfigurations.getHungamaSocialServerUrl(), 
																					mServerConfigurations.getHungamaAuthKey(), 
																					userId)), listener,mContext);
	}
	
	public void cancelGetProfileBadges() {
		if (mProfileBadgescommunicationManager != null && mProfileBadgescommunicationManager.isRunning()) {
			mProfileBadgescommunicationManager.cancelAnyRunningOperation();
		}
	}
	
	public void checkBadgesAlert(String contentId, String mediaType, String action,CommunicationOperationListener listener){
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
				new SocialBadgeAlertOperation(mServerConfigurations.getHungamaSocialServerUrl(), 
						mServerConfigurations.getHungamaAuthKey(), 
						mApplicationConfigurations.getPartnerUserId(), 
						contentId, 
						mediaType, 
						action)),
    			listener,mContext);
	}
	
	
	// ======================================================
	// Favorites
	// ======================================================
	
 	 public void addToFavorites(String contentId, String mediaType, CommunicationOperationListener listener) {
	    	CommunicationManager communicationManager = new CommunicationManager();
	    	communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new AddToFavoriteOperation(mServerConfigurations.getHungamaServerUrl(),  
	    			mServerConfigurations.getHungamaAuthKey(), mApplicationConfigurations.getPartnerUserId(), mediaType, contentId)),
	    			 listener,mContext);
	 }
	 
	 public void removeFromFavorites(String contentId, String mediaType, CommunicationOperationListener listener) {
	    	CommunicationManager communicationManager = new CommunicationManager();
	    	communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new RemoveFromFavoriteOperation(mServerConfigurations.getHungamaServerUrl(),  
	    			mServerConfigurations.getHungamaAuthKey(), mApplicationConfigurations.getPartnerUserId(), mediaType, contentId)),
	    			 listener,mContext);
	 }
	 
	 public void getFavorites(MediaType mediaType, String userId, CommunicationOperationListener listener) {
	    	CommunicationManager communicationManager = new CommunicationManager();
	    	communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new SocialProfileFavoriteMediaItemsOperation(mServerConfigurations.getHungamaSocialServerUrl(),  
	    			mServerConfigurations.getHungamaAuthKey(), userId, mediaType)),
	    			 listener,mContext);
	 }
	
	
	// ======================================================
	// My Collection
	// ======================================================
	 
	 public void getMyCollection(CommunicationOperationListener listener) {
	    	CommunicationManager communicationManager = new CommunicationManager();
	    	communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
	    			new SocialMyCollectionOperation(mServerConfigurations.getHungamaSocialServerUrl(),  
	    											mServerConfigurations.getHungamaAuthKey(), 
	    											mApplicationConfigurations.getPartnerUserId())),
	    											listener,mContext);
	 }
	 
	 
	// ======================================================
	// Comments
	// ======================================================
	 
	 public void getComments(long contentId, MediaType type, int startIndex, int length, CommunicationOperationListener listener) {
	    	CommunicationManager communicationManager = new CommunicationManager();
	    	communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
	    			new SocialCommentsListingOperation(mServerConfigurations.getHungamaSocialServerUrl(),  
	    											   mServerConfigurations.getHungamaAuthKey(), 
	    											   String.valueOf(contentId), 
	    											   type.toString().toLowerCase(), 
	    											   String.valueOf(startIndex),
	    											   String.valueOf(length))), 
	    											   listener,mContext);
	 }
	 
	 public void postComment(long contentId, MediaType type, String provider, String comment, CommunicationOperationListener listener) {
	    	CommunicationManager communicationManager = new CommunicationManager();
	    	communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
	    					new SocialCommentsPostOperation(mServerConfigurations.getHungamaSocialServerUrl(),  
	    													mServerConfigurations.getHungamaAuthKey(), 
	    													String.valueOf(contentId), 
	    													type.toString().toLowerCase(), 
	    													mApplicationConfigurations.getPartnerUserId(), 
	    													provider, 
	    													comment)), 
	    													listener,mContext);
	 }
	 
	 
	// ======================================================
	// Share
	// ======================================================
	 
	 public void share(int contentId, String type, String provider, String userText, CommunicationOperationListener listener) {
	    	CommunicationManager communicationManager = new CommunicationManager();
	    	communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
	    			new ShareOperation(mServerConfigurations.getHungamaSocialServerUrl(),  
	    							   mServerConfigurations.getHungamaAuthKey(), 
	    							   String.valueOf(contentId), 
	    							   type, 
	    							   mApplicationConfigurations.getPartnerUserId(), 
	    							   provider, 
	    							   userText)), 
	    							   listener,mContext);
	 }
	 
	 
	public void getShareUrl(String contentId, String mediaType,CommunicationOperationListener listener){
		
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener,
				mContext, 
				new SocialGetUrlOperation(
						mServerConfigurations.getHungamaSocialServerUrl(), 
						mServerConfigurations.getHungamaAuthKey(), 
						mApplicationConfigurations.getPartnerUserId(), contentId, mediaType)),
    			listener,mContext);
		
	}
	 
	 
	// ======================================================
	// Feedback. 
	// ======================================================
	 
	public void getFeedbackSubjects(final CommunicationOperationListener listener) {
		// checks if we already have the subjects to perform some nitty tests upon.
		List<String> cahcedSubjects = mCacheManager.getStoredFeedbackSubjects();
		if (!Utils.isListEmpty(cahcedSubjects)) {
			// Yes! we've got some suckers, just like Apple funboz - pack them' up and snd to the Test Chamber.
			listener.onStart(OperationId.FEEDBACK_SUBJECTS);
			
			Map<String, Object> suckersMap = new HashMap<String, Object>();
			suckersMap.put(FeedbackSubjectsOperation.RESULT_OBJECT_SUBJECTS_LIST, cahcedSubjects);
			
			// Bum... Bumm.. Bum.. enoter one bites the dust...
			listener.onSuccess(OperationId.FEEDBACK_SUBJECTS, suckersMap);
			
		} else {
			// Crapp!!! we've run out of suckers, searching for volunteers.
			CommunicationManager communicationManager = new CommunicationManager();
			/*
			 * Sheep go in, sheep go out.  
			 */
			communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
					new FeedbackSubjectsOperation(mServerConfigurations.getHungamaServerUrl(), 
												  mServerConfigurations.getHungamaAuthKey())), 
												  new CommunicationOperationListener() {
													
													@Override
													public void onSuccess(int operationId, Map<String, Object> responseObjects) {
														/*
														 * Putting our new test subjects in the cache, promising them there will be a cake.
														 * No!!!! it's not a lie!~
														 */
														List<String> brandNewSuckerTestSubjects = (List<String>) 
																responseObjects.get(FeedbackSubjectsOperation.RESULT_OBJECT_SUBJECTS_LIST);
														mCacheManager.storeFeedbackSubjects(brandNewSuckerTestSubjects);
														
														listener.onSuccess(operationId, responseObjects);
													}
													
													@Override
													public void onStart(int operationId) {
														listener.onStart(operationId);
													}
													
													@Override
													public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
														listener.onFailure(operationId, errorType, errorMessage);
													}
												},mContext);
			// The answer is 42.
			
		}
	}
	
	public void postFeedback(Map<String, String> feedback, CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, 
				new FeedbackSubmitOperation(mServerConfigurations.getHungamaServerUrl(), 
											mServerConfigurations.getHungamaAuthKey(), 
											feedback)), listener,mContext);
	}	
	 
	 
	// ======================================================
	// Sharing Settings
	// ======================================================
	 
	public void getSharingSettings(CommunicationOperationListener listener, boolean isUpdate, String key, Integer value) {
	    	CommunicationManager communicationManager = new CommunicationManager();
	    	communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new ShareSettingsOperation(
	    			mServerConfigurations.getHungamaServerUrl(), 
	    			mServerConfigurations.getHungamaAuthKey(),
	    			mApplicationConfigurations.getPartnerUserId(), isUpdate, key, value)),listener,mContext);
	}

 
	// ======================================================
	// My Stream Settings
	// ======================================================
	 
	public void getMyStreamSettings(CommunicationOperationListener listener, boolean isUpdate, String key, Integer value) {
	    	CommunicationManager communicationManager = new CommunicationManager();
	    	communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, new MyStreamSettingsOperation(
	    			mServerConfigurations.getHungamaServerUrl(), 
	    			mServerConfigurations.getHungamaAuthKey(),
	    			mApplicationConfigurations.getPartnerUserId(), isUpdate, key, value)),listener,mContext);
	}
	 
	// ======================================================
	// Public helper methods.
	// ======================================================	
	
	public boolean isDeviceOnLine() {
		ConnectivityManager connectivityManager = 
				(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
 		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			return true;
 		}
 		return false; 
 	}
	
	
	// ======================================================
	// Private helper methods.
	// ======================================================
	private String getDisplayDensity() {
		int densityDpi =  mContext.getResources().getDisplayMetrics().densityDpi;
		switch (densityDpi) {
			case DisplayMetrics.DENSITY_LOW:
				return "ldpi";
			case DisplayMetrics.DENSITY_MEDIUM:
				return "mdpi";
			case DisplayMetrics.DENSITY_HIGH:
				return "hdpi";
			case DisplayMetrics.DENSITY_XHIGH:
			case DisplayMetrics.DENSITY_XXHIGH:
				return "xdpi";
		}
		return "hdpi";
	}
	
	
	// ======================================================
	// Private Operations listeners.
	// ======================================================
	
	/**
	 * Wrapper CommunicationOperationListener for caching the media items before they are being sent
	 * to the client listener.
	 */
	private class WrapperMediaContentOperationListener implements CommunicationOperationListener {

		private final CommunicationOperationListener listener;
		
		public WrapperMediaContentOperationListener(CommunicationOperationListener listener) {
			this.listener = listener;
		}
		
		@Override
		public void onStart(int operationId) {
			listener.onStart(operationId);
		}

		@Override
		public void onSuccess(int operationId, Map<String, Object> responseObjects) {
			switch(operationId) {
				
				case (OperationDefinition.Hungama.OperationId.SUBSCRIPTION): {
					Logger.i(TAG, "******************** SUBSCRIPTION *********************");
					SubscriptionResponse subscriptionResponse = 
							(SubscriptionResponse) responseObjects.get(SubscriptionOperation.RESPONSE_KEY_SUBSCRIPTION);
					if (subscriptionResponse != null  && subscriptionResponse.getPlan() != null && subscriptionResponse.getPlan().size() > 0) {
						
						if (subscriptionResponse.getSubscriptionType() == SubscriptionType.PLAN) {
							mCacheManager.storeSubscriptionPlans(subscriptionResponse.getPlan());
						}
					}
					break;
				
				}
			}
			listener.onSuccess(operationId, responseObjects);
		}

		@Override
		public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
			listener.onFailure(operationId, errorType, errorMessage);
		}
		
	}
	
	private class WrapperCampaignListOperationListener implements CommunicationOperationListener {
		
		private final CommunicationOperationListener listener;
		
		public WrapperCampaignListOperationListener(CommunicationOperationListener listener) {
			this.listener = listener;
		}
		
		@Override
		public void onStart(int operationId) {
			listener.onStart(operationId);
		}

		@Override
		public void onSuccess(int operationId, Map<String, Object> responseObjects) {
			switch(operationId) {
				
				
			case OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ:

				List<String> list = (List<String>) responseObjects.get(
						CampaignListCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN_LIST);
				
				storeCampaignList(list);
				
				break;
			
			}
			listener.onSuccess(operationId, responseObjects);
		}

		@Override
		public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
			listener.onFailure(operationId, errorType, errorMessage);
		}
	}
	
	private class WrapperCampaignOperationListener implements CommunicationOperationListener {
		
		private final CommunicationOperationListener listener;
		
		public WrapperCampaignOperationListener(CommunicationOperationListener listener) {
			this.listener = listener;
		}
		
		@Override
		public void onStart(int operationId) {
			listener.onStart(operationId);
		}

		@Override
		public void onSuccess(int operationId, Map<String, Object> responseObjects) {
			switch(operationId) {
				
				
			case OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ:

				List<Campaign> campaigns = (List<Campaign>) responseObjects.get(
						CampaignCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN);
				
			    storeCampaign(campaigns);
			    
				// stores the campaigns for case use.
				storeCampaign(campaigns);
			    
				// extracts the placements from the campaigns.
				List<Placement> radioPlacements = 
						CampaignsManager.getAllPlacementsOfType(campaigns, ForYouActivity.PLACEMENT_TYPE_RADIO);
				List<Placement> splashPlacements = 
						CampaignsManager.getAllPlacementsOfType(campaigns, ForYouActivity.PLACEMENT_TYPE_SPLASH);
				
				// Store the placements.
				storeRadioPlacement(radioPlacements);
				storeSplashPlacement(splashPlacements);
				
				break;
			
			}
			listener.onSuccess(operationId, responseObjects);
		}

		@Override
		public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
			listener.onFailure(operationId, errorType, errorMessage);
		}
	}
	
	/**
	 * Wrapper listener for the getting the discoveries if there are not stored preferences
	 * for the user.
	 * 
	 * This in the onSucces() will call to the get Discoveries operation and will take car on
	 * invoking the client's listener callback.
	 */
	private class WrapperGetPreferencesOperationListener implements CommunicationOperationListener {
		
		private final CommunicationOperationListener listener;
		
		public WrapperGetPreferencesOperationListener(CommunicationOperationListener listener) {
			this.listener = listener;
		}

		@Override
		public void onStart(int operationId) {
			this.listener.onStart(operationId);
		}

		@Override
		public void onSuccess(int operationId, Map<String, Object> responseObjects) {
			
			CommunicationManager communicationManager = new CommunicationManager();
			List<CategoryTypeObject> categoryTypeObjects = mCacheManager.getStoredPreferences();
			
			DiscoverRetrieveOperation discoverRetrieveOperation = new DiscoverRetrieveOperation(
					mServerConfigurations.getHungamaServerUrl(), mServerConfigurations.getHungamaAuthKey(), 
					mApplicationConfigurations.getPartnerUserId(), categoryTypeObjects, mCacheManager.getStoredMoods());
			
			/*
			 * Creates internal listener to avoid recalling the client's onStart() and this onSuccess().
			 */
			communicationManager.performOperationAsync(new HungamaWrapperOperation(listener, mContext, discoverRetrieveOperation), 
					new CommunicationOperationListener() {
						
						@Override
						public void onSuccess(int operationId, Map<String, Object> responseObjects) {
							WrapperGetPreferencesOperationListener.this.listener.onSuccess(operationId, responseObjects);
						}
						
						@Override
						public void onStart(int operationId) {}
						
						@Override
						public void onFailure(int operationId, ErrorType errorType,String errorMessage) {
							WrapperGetPreferencesOperationListener.this.listener.onFailure(operationId, errorType, errorMessage);
						}
					},mContext);
		}

		@Override
		public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
			this.listener.onFailure(operationId, errorType, errorMessage);
		}
		
		
	}
	
	
	// ======================================================
	// Campaigns.
	// ======================================================
	
	public boolean storeCampaignList(List<String> list) {
		return mCacheManager.storeCampaignList(list);
	}
	
	public List<String> getStoredCampaignList() {
		return mCacheManager.getStoredCampaignList();
	}

	public boolean storeCampaign(List<Campaign> list) {
		return mCacheManager.storeCampaign(list);
	}
	
	public List<Campaign> getStoredCampaign() {
		return mCacheManager.getStoredCampaign();
	}
	 
	public boolean storeRadioPlacement(List<Placement> list) {
		return mCacheManager.storeRadioPlacement(list);
	}
	
	public List<Placement> getStoredRadioPlacement() {
		return mCacheManager.getStoredRadioPlacement();
	}

	public boolean storeSplashPlacement(List<Placement> list) {
		return mCacheManager.storeSplashPlacement(list);
	}
	
	public List<Placement> getStoredSplashPlacement() {
		return mCacheManager.getStoredSplashPlacement();
	}
	
	public Map<Long, Playlist> getStoredPlaylists(){
		return mCacheManager.getStoredPlaylists();

	}
	
	public boolean storePlaylists(Map<Long, Playlist> list){
		return mCacheManager.storePlaylists(list);
	}
	
	public Map<Long,Track> getStoredTracks(){
		return mCacheManager.getStoredTracks();
	}
	
	public boolean storeTracks(Map<Long,Track> tracks){		
		return mCacheManager.storeTrackList(tracks);
	}
	
	public boolean storeEvents(List<Event> events){
		return mCacheManager.storeEvents(events);
	}
	
	public List<Event> getStoredEvents(){
		return mCacheManager.getStoredEvents();
	}
	
	public boolean storeEvent(Event event){
		return mCacheManager.storeEvent(event);
	}
	/**
	 * Update the list of Playlists that are stored in the cache
	 * 
	 * @param item
	 * @return
	 */
	public synchronized boolean updateItemable(Playlist newItem, String action){
		
		boolean updated = false;
		
		Map<Long, Playlist> itemables = getStoredPlaylists();

		
		if(itemables == null){
			// Guess it's the first time using that stored list
			itemables = new HashMap<Long, Playlist>();
		}
		
		// Check if the Playlist (item) exist
		Playlist playlist = (Playlist) itemables.get(newItem.getId());
		
		if(action.equalsIgnoreCase(InventoryLightService.ADD)){
			
			Log.i(TAG, "Playlist: " + newItem.getName() + " " + newItem.getId() +" "+ action);
			
			if(playlist != null){
				// Do Nothing
			}else{
				itemables.put(newItem.getId(), newItem);
				updated = mCacheManager.storePlaylists(itemables);
			}
			
		}else if(action.equalsIgnoreCase(InventoryLightService.MOD)){
			
			Log.i(TAG, "Playlist: " + newItem.getName() + " " + newItem.getId() +" "+ action);
			
			if(playlist != null){
				
				if(newItem.getName() != null){
					playlist.setName(newItem.getName());
				}
				
				if(newItem.getTrackList() != null){
					playlist.setTrackList(newItem.getTrackList());	
				}
				
				itemables.put(playlist.getId(), playlist);
				
			}else{
				// MOD is equal to ADD when the PlayList does not exist
				itemables.put(newItem.getId(), newItem);
			}
			
			updated = mCacheManager.storePlaylists(itemables);
			
		}else if(action.equalsIgnoreCase(InventoryLightService.DEL)){
			
			if(playlist != null){
				Log.i(TAG, "Playlist: " + newItem.getName() +" "+ newItem.getId() +" "+ action);
				
				itemables.remove(newItem.getId());
				updated = mCacheManager.storePlaylists(itemables);
				
			}else{
				// Do Nothing
			}
		}
		
		return updated;
	}
		
	public synchronized boolean updateTracks(String trackID, String trackName){
		
		Map<Long, Track> tracks = getStoredTracks();
		
		if(tracks == null){
			// Guess it's the first time using that stored list
			tracks = new HashMap<Long, Track>();
		}
		
		Track track = tracks.get(trackID);
		
		if(track == null){
			long id = Long.valueOf(trackID);
			Track newTrack = new Track(id, trackName, "", "", "", "");
			tracks.put(id, newTrack);
		}
		
		return storeTracks(tracks);
	}
	
	public synchronized boolean updateTracks(MediaTrackDetails mediaTrackDetails){
		
		if(mediaTrackDetails == null){
			return false;
		}
		
		Map<Long, Track> tracks = getStoredTracks();
		
		if(tracks == null){
			return false;
		}
		
		Track track = tracks.get(mediaTrackDetails.getId());
		
		if(track == null){
			return false;
		}
		
		track = new Track(mediaTrackDetails.getId(), mediaTrackDetails.getTitle(), 
						  mediaTrackDetails.getAlbumName(), mediaTrackDetails.getSingers(), 
						  mediaTrackDetails.getImageUrl(), mediaTrackDetails.getBigImageUrl());
		
		tracks.put(track.getId(), track);
		
		return storeTracks(tracks);
	}
	
	// PlayList request
	public boolean storePlaylistRequest(List<PlaylistRequest> list) {
		return mCacheManager.storeRequestList(list);
	}
	
	public List<PlaylistRequest> getPlaylistRequest() {
		return mCacheManager.getStoredRequestList();
	}
	
	
	public Intent cretaeForYouActivityIntent(Context context , Node node){
		
		if(node != null){
			Intent intent = new Intent(context, ForYouActivity.class);
			
			// Save the campaign's text_1 for the header title
			intent.putExtra(ForYouActivity.CAMPAIGN_TITLE, node.getCampaignTitle());
			
			if(node.getChildNodes() != null && node.getChildNodes().size() > 0){
				//Node has children
				intent.putExtra(ForYouActivity.CLICKED_NODES_CHILDS, (ArrayList<Node>)node.getChildNodes());
				return intent;
				//context.startActivity(intent);
				
			}else{
				// Node has no children
				intent.putExtra(ForYouActivity.CLICKED_NODE, node);
				
				if(node.getAction() != null){
					Toast.makeText(context, "Action: " + node.getAction(), Toast.LENGTH_LONG).show();
					return intent;
					//context.startActivity(intent);
				}else{
					// Do nothing for now
					return null;
				}
			}
		}else{
			return null;
		}
	}
	
}
