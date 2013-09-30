package com.hungama.myplay.activity.operations.hungama;

import com.hungama.myplay.activity.communication.CommunicationOperation;

/**
 * A Communication manager that performs in front of Hungama servers.
 */
public abstract class HungamaOperation extends CommunicationOperation {
	
	protected static final String COMMA = ",";
	protected static final String AMPERSAND = "&";
	protected static final String EQUALS = "=";
	protected static final String PARAMS = "?";
	
	/*
	 * Request parameters definition.
	 */
	protected static final String PARAMS_AUTH_KEY = "auth_key";
	protected static final String PARAMS_USER_ID = "user_id";
	protected static final String PARAMS_DEVICE = "device";
	protected static final String PARAMS_SIZE = "size";
	protected static final String PARAMS_CONTENT_ID = "content_id";
	protected static final String PARAMS_PLAN_ID = "plan_id";
	protected static final String PARAMS_MSISDN = "msisdn";
	protected static final String PARAMS_AFFILIATE_ID = "affiliate_id";
	protected static final String PARAMS_HARDWARE_ID = "hardware_id";
	protected static final String PARAMS_PASSWORD = "password";
	protected static final String PARAMS_MEDIA_TYPE = "type";
	protected static final String PARAMS_REFERRAL_ID = "referral_id";
	protected static final String PARAMS_NETWORK_SPEED = "network_speed";
	protected static final String PARAMS_NETWORK_TYPE = "network_type";
	protected static final String PARAMS_CONTENT_FORMAT = "content_format";
	protected static final String PARAMS_LENGTH = "length";	
	protected static final String PARAMS_CLIENT = "client";
	protected static final String PARAMS_CLIENT_VERSION = "client_version";
	protected static final String PARAMS_CODE = "code";
	protected static final String PARAMS_PURCHASE_TOKEN = "purchase_token";
	protected static final String PARAMS_GOOGLE_EMAIL_ID = "google_email_id";
	
	protected static final String VALUE_DEVICE = "android";
	
	/*
	 * Urls segments definition.
	 */
	protected static final String URL_SEGMENT_CONTENT = "content/";
	protected static final String URL_SEGMENT_MUSIC = "music/";
	protected static final String URL_SEGMENT_VIDEO = "video/";
	protected static final String URL_SEGMENT_DETAILS_ALBUM = "album_details/";
	protected static final String URL_SEGMENT_DETAILS_PLAYLIST = "playlist_details/";
	protected static final String URL_SEGMENT_DETAILS_SONG = "song_details/";
	protected static final String URL_SEGMENT_CATEGORIES = "categories";
	protected static final String URL_SEGMENT_DETAILS_VIDEO = "video_details/";
	protected static final String URL_SEGMENT_RELATED_VIDEO = "related/";
	// search.
	protected static final String URL_SEGMENT_SEARCH_POPULAR_KEYWORD = "content/search/popular_keyword?";
	protected static final String URL_SEGMENT_SEARCH_AUTO_SUGGEST = "content/search/auto_suggest?";
	protected static final String URL_SEGMENT_SEARCH = "content/search?";
	
	// video streaming.
	protected static final String URL_SEGMENT_VIDEO_STREAMING = "streaming/video?";
	protected static final String URL_SEGMENT_VIDEO_STREAMING_ADP = "streaming/adpvideo?";
	
	// discovery.
	protected static final String URL_SEGMENT_DISCOVER_OPTIONS = "user/discover/option?";
	protected static final String URL_SEGMENT_DISCOVER_SEARCH = "user/discover/search?";
	protected static final String URL_SEGMENT_DISCOVER_SAVE = "user/discover/save?";
	protected static final String URL_SEGMENT_DISCOVER_RETREIVE = "user/discover/retrieve?";
	
	// Subscription
	protected static final String URL_SEGMENT_SUBSCRIPTION_PLAN = "subscription_plans.php";
	protected static final String URL_SEGMENT_SUBSCRIPTION_CHARGE = "subscription_charge.php";
	protected static final String URL_SEGMENT_SUBSCRIPTION_UNSUBSCRIBE = "unsubscribe.php";
	protected static final String URL_SEGMENT_SUBSCRIPTION_CHECK_SUBSCRIPTION = "check_user_subscription.php";
	protected static final String URL_SEGMENT_VERSION_CHECK = "version/check.php";
	
	// Mobile verify
	protected static final String URL_SEGMENT_MOBILE_VERIFY = "mobile_verify.php";
	protected static final String URL_SEGMENT_MOBILE_PASSWORD_VERIFY = "mobile_password_verify.php";
	protected static final String URL_SEGMENT_RESEND_PASSWORD = "resend_sms.php";
	protected static final String URL_SEGMENT_COUNTRY_CHECK = "check_msisdn.php";
	
	// Download
	protected static final String URL_SEGMENT_DOWNLOAD_COUNT = "download_count.php";
	protected static final String URL_SEGMENT_BUY_PLANS = "buy_plans.php";
	protected static final String URL_SEGMENT_BUY_CHARGE = "buy_charge.php";
	protected static final String URL_SEGMENT_CONTENT_DELIVERY = "content_delivery.php";
		
	// player.
	protected static final String URL_SEGMENT_SIMILAR = "similar/";
	protected static final String URL_SEGMENT_LYRICS = "lyrics/";
	protected static final String URL_SEGMENT_TRIVIA = "trivia/";
	
	protected static final String URL_SEGMENT_RADIO_LIVE_STATIONS = "content/radio/live_stations?";
	protected static final String URL_SEGMENT_RADIO_TOP_ARTISTS = "content/radio/top_artist?";
	protected static final String URL_SEGMENT_RADIO_TOP_ARTIST_SONGS = "content/radio/top_artist_songs?";
	

	// my preferences.
	protected static final String URL_SEGMENT_PREFERENCES_SAVE = "user/preferences/save?";
	protected static final String URL_SEGMENT_PREFERENCES_RETREIVE = "user/preferences/retrieve?";
	

	// SOCIAL.
	
	protected static final String URL_SEGMENT_SOCIAL_COMMENT_POST = "comments/post_comment.php";
	protected static final String URL_SEGMENT_SOCIAL_COMMENT_GET = "comments/get_comments.php?";
	
	protected static final String URL_SEGMENT_SOCIAL_MY_STREAM = "my_stream.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE = "my_profile.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE_BADGES = "my_badges.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE_LEADERBOARD = "leader_board.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_ALBUMES = "fav_albums.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_SONGS = "fav_songs.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_VIDEOS = "fav_videos.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_PLAYLISTS = "fav_playlists.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE_MY_COLLECTION = "my_collection.php?";
	protected static final String URL_SEGMENT_SOCIAL_SHARE = "share.php?";
	protected static final String URL_SEGMENT_SOCIAL_BADGE_ALERT = "badge_alert.php?";
	protected static final String URL_SEGMENT_SOCIAL_GET_URL = "share_url.php?";
	
	protected static final String URL_SEGMENT_SOCIAL_ADD_TO_FAVORITE = "user/favorite/add_favorite?";
	protected static final String URL_SEGMENT_SOCIAL_REMOVE_FROM_FAVORITE = "user/favorite/remove_favorite?";
	protected static final String URL_SEGMENT_GCM_REG_ID = "user/device_register?";
	protected static final String URL_SEGMENT_SHARE_SETTINGS = "user/share_settings/retrieve_share?";
	protected static final String URL_SEGMENT_SHARE_SETTINGS_UPDATE = "user/share_settings/update_share?";
	protected static final String URL_SEGMENT_MY_STREAM_SETTINGS = "user/stream_settings/retrieve_stream?";
	protected static final String URL_SEGMENT_MY_STREAM_SETTINGS_UPDATE = "user/stream_settings/update_stream?";
	
	
	// FEEDBACK.
	protected static final String URL_SEGMENT_FEEDBACK_SUBJECTS = "user/feedback/subject?";
	protected static final String URL_SEGMENT_FEEDBACK_SAVE = "user/feedback/save?";
	
	/*
	 * General parsing keys.
	 */
	protected static final String KEY_CATALOG = "catalog";
	protected static final String KEY_CONTENT = "content";
	protected static final String KEY_ID = "id";
	protected static final String KEY_NAME = "name";
	protected static final String KEY_ATTRIBUTES = "@attributes";
	
	/*
	 * Error parsing keys.
	 */
	protected static final String KEY_RESPONSE = "response";
	protected static final String KEY_CODE = "code";
	protected static final String KEY_MESSAGE = "message";
	protected static final String KEY_DISPLAY = "display";
	
	
}
