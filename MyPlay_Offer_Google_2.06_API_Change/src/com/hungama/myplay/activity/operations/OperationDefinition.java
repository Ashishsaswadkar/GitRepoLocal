package com.hungama.myplay.activity.operations;

/**
 * General definition of any Communication Operation in the application.
 */
public class OperationDefinition {
	
	// ======================================================
	// Catch media operations definition.
	// ======================================================
	
	public static final class CatchMedia {
		
		/**
		 * Definition of the communication operations ids.
		 */
		public static final class OperationId {
			public static final int DEVICE_CEREATE 					= 100001;
			public static final int PARTNER_INFO_READ				= 100002;
			public static final int PARTNER_CONSUMER_PROXY_CREATE 	= 100003;
			public static final int DEVICE_ACTIVATION_LOGIN_CREATE 	= 100004;
			public static final int MEDIA_HANDLE_CRERATE 			= 100005;
			public static final int PLAY_EVENT_CREATE 				= 100006;
			public static final int CAMPAIGN_PLAY_EVENT_CREATE 		= 100007;
			public static final int CAMPAIGN_LIST_READ 				= 100008;
			public static final int CAMPAIGN_READ 					= 100009;
			public static final int PLAYLIST						= 100010;
			public static final int INVENTORY_LIGHT					= 100011;
			public static final int SESSION_CREATE 					= 100012;
			public static final int TIME_READ 						= 100013;
		}
		
		/**
		 * Definitions of the services names.
		 */
		public static final class ServiceName {
			public static final String DEVICE_CEREATE = "Device.py";
			public static final String PARTNER_INFO_READ = "PartnerInfo.py";
			public static final String PARTNER_CONSUMER_PROXY_CREATE = "PartnerConsumerProxy.py";
			public static final String DEVICE_ACTIVATION_LOGIN_CREATE = "DeviceActivationLogin.py";
			public static final String MEDIA_HANDLE_CRERATE = "MediaHandle.py";
			public static final String PLAY_EVENT_CRERATE = "PlayEvent.py";
			public static final String CAMPAIGN_PLAY_EVENT_CRERATE = "CampaignPlayEvent.py";
			public static final String CAMPAIGN_LIST_READ = "CampaignList.py";
			public static final String CAMPAIGN_READ = "Campaign.py";
			public static final String PLAYLIST = "Playlist.py";
			public static final String INVENTORY_LIGHT = "InventoryLight.py";
			public static final String SESSION = "Session.py";
			public static final String TIME = "Time.py";
		}
		
	}
	
	// ======================================================
		// Hungama operations definition.
		// ======================================================
	
	public static final class Hungama {
		
		/**
		 * Definition of the communication operations ids.
		 */
		public static final class OperationId {
			public static final int FORGOT_PASSWORD		 	 			= 200011;
			public static final int MEDIA_CONTENT_LATEST 				= 200012;
			public static final int MEDIA_CONTENT_RECOMMANDED 			= 200013;
			public static final int MEDIA_CONTENT_FEATURED				= 200014;
			public static final int MEDIA_DETAILS		 				= 200015;
			public static final int MEDIA_CATEGORIES	 				= 200016;
			public static final int PREFERENCES_GET						= 200017;
			
			public static final int SEARCH_POPULAR_KEYWORDS				= 200021;
			public static final int SEARCH_AUTO_SUGGEST					= 200022;
			public static final int SEARCH								= 200023;
			
			public static final int DISCOVER_OPTIONS	 				= 200031;
			public static final int DISCOVER_SEARCH_RESULT				= 200032;
			public static final int DISCOVER_SAVE						= 200033;
			public static final int DISCOVER_RETRIEVE					= 200034;
			
			public static final int VIDEO_STREAMING						= 200041;
			public static final int VIDEO_RELATED						= 200042;
			public static final int VIDEO_STREAMING_ADP					= 200043;
			
			public static final int TRACK_SIMILAR						= 200051;
			public static final int TRACK_LYRICS						= 200052;
			public static final int TRACK_TRIVIA						= 200053;
			
			public static final int RADIO_LIVE_STATIONS					= 200061;
			public static final int RADIO_TOP_ARTISTS					= 200062;
			public static final int RADIO_TOP_ARTISTS_SONGS				= 200063;
			
			public static final int SUBSCRIPTION						= 200071;
			public static final int MOBILE_VERIFICATION					= 200072;
			public static final int SUBSCRIPTION_CHECK					= 200073;
			public static final int MOBILE_VERIFICATION_COUNTRY_CHECK	= 200074;
			public static final int VERSION_CHECK						= 200075;
			public static final int NEW_VERSION_CHECK					= 200076;			
			
			public static final int DOWNLOAD							= 200081;
			
			public static final int SOCIAL_COMMENT_POST					= 200091;
			public static final int SOCIAL_COMMENT_GET					= 200092;
			public static final int SOCIAL_MY_STREAM					= 200093;
			public static final int SOCIAL_PROFILE						= 200094;
			public static final int SOCIAL_PROFILE_BADGES				= 200095;
			public static final int SOCIAL_PROFILE_LEADERBOARD			= 200096;
			public static final int SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS	= 200097;
			public static final int SOCIAL_MY_COLLECTION				= 200098;
			public static final int SOCIAL_SHARE						= 200099;
						
			public static final int PREFERENCES_RETRIEVE				= 200101;
			public static final int PREFERENCES_SAVE					= 200102;
			
			public static final int ADD_TO_FAVORITE						= 200201;
			public static final int REMOVE_FROM_FAVORITE				= 200202;
			
			public static final int GCM_REG_ID							= 200203;
			
			public static final int SHARE_SETTINGS						= 200204;
			public static final int SHARE_SETTINGS_UPDATE 				= 200205;
			
			public static final int MY_STREAM_SETTINGS					= 200206;
			public static final int MY_STREAM_SETTINGS_UPDATE 			= 200207;
			
			public static final int FEEDBACK_SUBJECTS					= 200301;
			public static final int FEEDBACK_SUBMIT						= 200302;
			
			public static final int SOCIAL_BADGE_ALERT					= 200303;
			public static final int SOCIAL_GET_URL						= 200304;
		}
		
	}
	
}
