package com.hungama.myplay.activity.data.configurations;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.campaigns.util.Util;
import com.hungama.myplay.activity.data.dao.catchmedia.SignOption;
import com.hungama.myplay.activity.data.dao.hungama.BadgesAndCoins;
import com.hungama.myplay.activity.data.dao.hungama.DownloadPlan;
import com.hungama.myplay.activity.data.dao.hungama.Plan;
import com.hungama.myplay.activity.data.dao.hungama.PlanType;
import com.hungama.myplay.activity.ui.fragments.SettingsFragment;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.billing.SkuDetails;

public class ApplicationConfigurations {

	private static final String PREFERENCES_APPLICATION_CONFIGURATIONS = "preferences_application_configurations";
	private SharedPreferences mPreferences;
	
	public ApplicationConfigurations(Context context) {
		mPreferences = context.getSharedPreferences(PREFERENCES_APPLICATION_CONFIGURATIONS, Context.MODE_PRIVATE);
	}
	
	public static final String REGISTRATION_ID = "registration_id";
	public static final String PASSKEY = "passkey";
    public static final String SESSION_ID = "session_id";
    public static final String CONSUMER_ID = "consumer_id";
    public static final String HOUSEHOLD_ID = "household_id";
    public static final String HOUSEHOLD_CLIENT_REVISION = "household_client_revision";
    public static final String CONSUMER_CLIENT_REVISION = "consumer_client_revision";
    public static final String CONSUMER_SERVER_REVISION = "consumer_server_revision";
    public static final String LOCALE_TIME = "locale_time";
    public static final String HOUSEHOLD_REVISION = "household_revision";
    public static final String HOUSEHOLD_SERVER_REVISION = "household_server_revision";
    public static final String CONSUMER_REVISION = "consumer_revision";
    public static final String DEVICE_ID = "device_id";
    public static final String EXISTING_DEVICE = "existing_device";
    public static final String EXISTING_DEVICE_EMAIL = "existing_device_email";
    public static final String CLIENT_TYPE = "client_type";
    public static final String ACTIVATION_CODE = "activation_code";
    public static final String PARTNER_USER_ID = "partner_user_id";
    public static final String SKIPPED_PARTNER_USER_ID = "skipped_partner_user_id";
    public static final String IS_REAL_USER = "real_user";
    public static final String USER_LOGIN_PHONE_NUMNBER = "user_login_phone_numnber";
    public static final String IS_USER_HAS_SUBSCRIPTION_PLAN = "user_has_subscription_plan";
    public static final String USER_SUBSCRIPTION_PLAN_DATE = "user_subscription_plan_date";
    public static final String USER_SUBSCRIPTION_PLAN_DATE_PURCHASE = "user_subscription_plan_date_purchase";
    public static final String CAMPAIGN_IDS = "campaign_ids";
    public static final String MEDIA_ID_NS = "media_id_ns";
    public static final String PAGE_MAX = "page_max";
    public static final String PAGE_MIN = "page_min";
    public static final String PAGE_OPTIMAL = "page_optimal";
    public static final String IS_FIRST_VISIT_TO_APP = "first_visit_to_app";
    public static final String IS_FIRST_VISIT_TO_HOME_TILE_PAGE = "is_first_visit_to_home_tile_page";
    public static final String IS_FIRST_VISIT_TO_SEARCH_PAGE = "is_first_visit_to_search_page";
    public static final String IS_FIRST_VISIT_TO_FULL_PLAYER = "is_first_visit_to_full_player";
    public static final String IS_HOME_HINT_SHOWN_IN_THIS_SESSION = "is_home_hint_shown_in_this_session";
    public static final String IS_SEARCH_FILTER_SHOWN_IN_THIS_SESSION = "is_search_filter_shown_in_this_session";
    public static final String IS_PLAYER_QUEUE_HINT_SHOWN_IN_THIS_SESSION = "is_player_queue_hint_shown_in_this_session";
    public static final String IS_ENABLED_HOME_GUIDE_PAGE = "is_enabled_home_guide_page";   
    public static final String IS_ENABLED_GYM_MODE_GUIDE_PAGE = "is_enabled_gym_mode_guide_page";
    public static final String GIGYA_SIGNUP = "gigya_signup";
    public static final String GIGYA_FB_FIRST_NAME = "gigya_fb_first_name";
    public static final String GIGYA_FB_LAST_NAME = "gigya_fb_last_name";
    public static final String GIGYA_FB_EMAIL = "gigya_fb_EMAIL";
    public static final String GIGYA_TWITTER_FIRST_NAME = "gigya_twitter_first_name";
    public static final String GIGYA_TWITTER_LAST_NAME = "gigya_twitter_last_name";
    public static final String GIGYA_TWITTER_EMAIL = "gigya_twitter_email";
    public static final String GIGYA_GOOGLE_FIRST_NAME = "gigya_google_first_name";
    public static final String GIGYA_GOOGLE_LAST_NAME = "gigya_google_last_name";
    public static final String GIGYA_GOOGLE_EMAIL = "gigya_google_email";
    public static final String HUNGAMA_FIRST_NAME = "hungama_first_name";
    public static final String HUNGAMA_LAST_NAME = "hungama_last_name";
    public static final String HUNGAMA_EMAIL = "hungama_email";
    public static final String PLAYER_VOLUME = "player_volume";
    public static final String BITRATE_STATE = "bitrate_state";
    public static final String BITRATE = "bitrate";
    public static final String BANDWIDTH = "bandwidth";
    public static final String FILE_SIZE_FOR_BITRATE_CALCULATION = "file_size_for_bitrate_calculation";
    public static final String GIGYA_FB_THUMB_URL = "gigya_fb_thumb_url";
    public static final String GIGYA_TWITTER_THUMB_URL = "gigya_twitter_thumb_url";
    public static final String HINTS_STATE = "hints_state";
    public static final String TIME_READ_DELTA = "time_read_delta";
    public static final String IS_VERSION_CHECKED = "is_version_checked";
    public static final String CONTENT_FORMAT = "content_format";    
    public static final String SUBSCRIPTION_IAB_CODE = "subscription_iab_code";
    public static final String SUBSCRIPTION_IAB_PURCHSE_TOKEN = "subscription_iab_purchse_token";
    
    public static final String DISCOVER_PREFETCH_MOODS_SUCCESS_FLAG = "discover_prefetch_moods_success_flag";
    public static final String MOBILE_NUMBER = "mobile_number";
    
    public static final String IS_FIRST_BADGE_DISPLAYED = "is_first_badge_displayed";   
    
    public static final String GIGYA_LOGIN_SESSION_TOKEN = "gigya_login_session_token";
    public static final String GIGYA_LOGIN_SESSION_SECRET = "gigya_login_session_secret";
    
    /*
     * Media Handle and Play Event additional properties.
     */
    public static final String KEY_MEDIA_ID_NS = "media_id_ns";
    public static final String VALUE_MEDIA_ID_NS = "hungama";
    
    public static final int BITRATE_HIGH   = 128;
	public static final int BITRATE_MEDIUM = 64;
	public static final int BITRATE_LOW = 32;
	public static final int BITRATE_AUTO = 1;
	public static final int BITRATE_NONE = 0;
	
	public static final String DOWNLOAD_PLAN_ID = "download_plan_id";
	public static final String DOWNLOAD_PLAN_TYPE = "download_plan_type";
	
	public static final String DOWNLOAD_PLAN_NAME = "download_plan_name";
	public static final String DOWNLOAD_PLAN_PRICE = "download_plan_price";
	public static final String DOWNLOAD_PLAN_CURRENCY = "download_plan_currency";
	public static final String DOWNLOAD_PLAN_DURATION = "download_plan_duration";
	public static final String DOWNLOAD_MSISDN = "download_msisdn";
	public static final String DOWNLOAD_SUBSCRIPTION_STATUS = "download_subscription_status";
	public static final String DOWNLOAD_VALIDITY_DATE = "download_validity_date";
	
	// Badges and coins object
	public static final String BADGES_AND_COINS_BADGES_EARNED ="badges_and_coins_badges_earned"; 
	public static final String BADGES_AND_COINS_DISPLAY_CASE = "badges_and_coins_display_case";
	public static final String BADGES_AND_COINS_POINTS_EARNED = "badges_and_coins_points_earned" ;
	public static final String BADGES_AND_COINS_BADGE_NAME = "badges_and_coins_badge_name" ;
	public static final String BADGES_AND_COINS_BADGE_URL = "badges_and_coins_badge_url";
	public static final String BADGES_AND_COINS_MESSAGE = "badges_and_coins_message";
	public static final String BADGES_AND_COINS_NEXT_DESCRIPTION = "badges_and_coins_next_description";
	
	/**
	 * Preference's key for boolean flag if the prefetching of the moods has successed or not. 
	 */
	public static final String MOODS_PREFETCHING_SUCCESS = "moods_prefetching_success";
    
    
    public String getMediaIdNs(){
    	return "hungama";
    }
    
    public int getPageMax(){
    	return 200000; 
    }
    
    public int getPageMin(){
    	return 100000; 
    }
    
    public int getPageOptimal(){
    	return 150000; 
    }
    
    public String getRegistrationId() {
    	
    	return mPreferences.getString(REGISTRATION_ID, null);
    }
    
    public void setRegistrationId(String registrationId) {
    	
		Editor editor = mPreferences.edit();
		editor.putString(REGISTRATION_ID, registrationId);
		editor.commit();
	}
    
    public String getPasskey(){
		return mPreferences.getString(PASSKEY, null);
	}
	
	public void setPasskey(String passkey){
		Editor editor = mPreferences.edit();
		editor.putString(PASSKEY, passkey);
		editor.commit();
	}
	
	public String getSessionID(){
		return mPreferences.getString(SESSION_ID, null);
	}
	
	public void setSessionID(String sessionID){
		Editor editor = mPreferences.edit();
		editor.putString(SESSION_ID, sessionID);
		editor.commit();
	}
	
	public int getConsumerID(){
		return mPreferences.getInt(CONSUMER_ID, 0);
	}
	
	public void setConsumerID(int consumerID){
		Editor editor = mPreferences.edit();
		editor.putInt(CONSUMER_ID, consumerID);
		editor.commit();
	}
	
	public int getHouseholdID(){
		return mPreferences.getInt(HOUSEHOLD_ID, 0);
	}
	
	public void setHouseholdID(int householdID){
		Editor editor = mPreferences.edit();
		editor.putInt(HOUSEHOLD_ID, householdID);
		editor.commit();
	}
	
//	public int getHouseholdClientRevision() {
//		return mPreferences.getInt(HOUSEHOLD_CLIENT_REVISION, 0);
//	}
//
//	public void setHouseholdClientRevision(int value) {
//		Editor editor = mPreferences.edit();
//		editor.putInt(HOUSEHOLD_CLIENT_REVISION, value);
//		editor.commit();
//	}
	
	public int getHouseholdRevision() {
		return mPreferences.getInt(HOUSEHOLD_REVISION, 0);
	}

	public void setHouseholdRevision(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(HOUSEHOLD_REVISION, value);
		editor.commit();
	}
	
	// TODO: ??? static ???
	
//	public int getConsumerClientRevision() {
//		return mPreferences.getInt(CONSUMER_CLIENT_REVISION, 0);
//	}
//	
//	public void setConsumerClientRevision(int value) {
//		Editor editor = mPreferences.edit();
//		editor.putInt(CONSUMER_CLIENT_REVISION, value);
//		editor.commit();
//	}

	public int getConsumerRevision() {
		return mPreferences.getInt(CONSUMER_REVISION, 0);
	}
	
	public void setConsumerRevision(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(CONSUMER_REVISION, value);
		editor.commit();
	}
	
	public String getDeviceID(){
		return mPreferences.getString(DEVICE_ID, null);
	}
	
	public void setDeviceID(String deviceID){
		Editor editor = mPreferences.edit();
		editor.putString(DEVICE_ID, deviceID);
		editor.commit();
	}
	
	public void setIfDeviceExist(boolean isDeviceExist){
		Editor editor = mPreferences.edit();
		editor.putBoolean(EXISTING_DEVICE, isDeviceExist);
		editor.commit();
	}
	
	public boolean isDeviceExist(){
		return mPreferences.getBoolean(EXISTING_DEVICE, false);
	}
	
	public String getExistingDeviceEmail(){
		return mPreferences.getString(EXISTING_DEVICE_EMAIL, "");
	}
	
	public void setExistingDeviceEmail(String email){
		Editor editor = mPreferences.edit();
		editor.putString(EXISTING_DEVICE_EMAIL, email);
		editor.commit();
	}
	
	// TODO: implement this.
	public String getClientType() {
		return "full";
	}
	
	public String getPartnerUserId(){
		return mPreferences.getString(PARTNER_USER_ID, "");
	}
	
	public void setPartnerUserId(String partnerUserId){
		Editor editor = mPreferences.edit();
		editor.putString(PARTNER_USER_ID, partnerUserId);
		editor.commit();
	}
	
	public String getSkippedPartnerUserId(){
		return mPreferences.getString(SKIPPED_PARTNER_USER_ID, "");
	}
	
	public void setSkippedPartnerUserId(String partnerUserId){
		Editor editor = mPreferences.edit();
		editor.putString(SKIPPED_PARTNER_USER_ID, partnerUserId);
		editor.commit();
	}
	
	public void setIsRealUser(boolean isRealUser) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_REAL_USER, isRealUser);
		editor.commit();
	}
	
	public boolean isRealUser() {
		return mPreferences.getBoolean(IS_REAL_USER, false);
	}
	
	public String getUserLoginPhoneNumber(){
		return mPreferences.getString(USER_LOGIN_PHONE_NUMNBER, "");
	}
	
	public void setUserLoginPhoneNumber(String phoneNumber) {
		Editor editor = mPreferences.edit();
		editor.putString(USER_LOGIN_PHONE_NUMNBER, phoneNumber);
		editor.commit();
	}
	
	public synchronized void setIsUserHasSubscriptionPlan(boolean isUserHasSubscriptionPlan) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_USER_HAS_SUBSCRIPTION_PLAN, isUserHasSubscriptionPlan);
		editor.commit();
	}
	
	public boolean isUserHasSubscriptionPlan() {
		return mPreferences.getBoolean(IS_USER_HAS_SUBSCRIPTION_PLAN, false);
	}
	
	public synchronized void setUserSubscriptionPlanDate(String userSubscriptionPlanDate) {
		Editor editor = mPreferences.edit();
		editor.putString(USER_SUBSCRIPTION_PLAN_DATE, userSubscriptionPlanDate);
		editor.commit();
	}
	
	public String getUserSubscriptionPlanDate() {
		return mPreferences.getString(USER_SUBSCRIPTION_PLAN_DATE, "");
	}
	
	public synchronized void setUserSubscriptionPlanDatePurchase(String userSubscriptionPlanDatePurchase) {
		Editor editor = mPreferences.edit();
		editor.putString(USER_SUBSCRIPTION_PLAN_DATE_PURCHASE, userSubscriptionPlanDatePurchase);
		editor.commit();
	}
	
	public String getUserSubscriptionPlanDatePurchase() {
		return mPreferences.getString(USER_SUBSCRIPTION_PLAN_DATE_PURCHASE, "");
	}
	
	public void setIsFirstVisitToApp(boolean isFirstVisitToApp) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_FIRST_VISIT_TO_APP, isFirstVisitToApp);
		editor.commit();
	}
	
	public boolean isFirstVisitToApp() {
		return mPreferences.getBoolean(IS_FIRST_VISIT_TO_APP, true);
	}
	
	public void setIsFirstVisitToHomeTilePage(boolean isFirstVisitToHomeTilePage) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_FIRST_VISIT_TO_HOME_TILE_PAGE, isFirstVisitToHomeTilePage);
		editor.commit();
	}
	
	public boolean isFirstVisitToHomeTilePage() {
		return mPreferences.getBoolean(IS_FIRST_VISIT_TO_HOME_TILE_PAGE, true);
	}
	
	public void setIsHomeHintShownInThisSession(boolean isHomeHintShownInThisSession) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_HOME_HINT_SHOWN_IN_THIS_SESSION, isHomeHintShownInThisSession);
		editor.commit();
	}
	
	public boolean isHomeHintShownInThisSession() {
		return mPreferences.getBoolean(IS_HOME_HINT_SHOWN_IN_THIS_SESSION, false);
	}
	
	public void setIsSearchFilterShownInThisSession(boolean isSearchFilterShownInThisSession) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_SEARCH_FILTER_SHOWN_IN_THIS_SESSION, isSearchFilterShownInThisSession);
		editor.commit();
	}
	
	public boolean isSearchFilterShownInThisSession() {
		return mPreferences.getBoolean(IS_SEARCH_FILTER_SHOWN_IN_THIS_SESSION, false);
	}
	
	
	public void setIsPlayerQueueHintShownInThisSession(boolean isPlayerQueueHintShownInThisSession) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_PLAYER_QUEUE_HINT_SHOWN_IN_THIS_SESSION, isPlayerQueueHintShownInThisSession);
		editor.commit();
	}
	
	public boolean isPlayerQueueHintShownInThisSession() {
		return mPreferences.getBoolean(IS_PLAYER_QUEUE_HINT_SHOWN_IN_THIS_SESSION, false);
	}
	
	
	
	
	public void setIsFirstVisitToSearchPage(boolean isFirstVisitToSearchPage) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_FIRST_VISIT_TO_SEARCH_PAGE, isFirstVisitToSearchPage);
		editor.commit();
	}
	
	public boolean isFirstVisitToSearchPage() {
		return mPreferences.getBoolean(IS_FIRST_VISIT_TO_SEARCH_PAGE, true);
	}
	
	public void setIsFirstVisitToFullPlayer(boolean isFirstVisitToFullPlayer) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_FIRST_VISIT_TO_FULL_PLAYER, isFirstVisitToFullPlayer);
		editor.commit();
	}
	
	public boolean isFirstVisitToFullPlayer() {
		return mPreferences.getBoolean(IS_FIRST_VISIT_TO_FULL_PLAYER, true);
	}
	
	public void setIsEnabledHomeGuidePage(boolean isEnabledHomeGuidePage) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_ENABLED_HOME_GUIDE_PAGE, isEnabledHomeGuidePage);
		editor.commit();
	}
	
	public boolean isEnabledHomeGuidePage() {
		return mPreferences.getBoolean(IS_ENABLED_HOME_GUIDE_PAGE, true);
	}
	
	public void setIsEnabledGymModeGuidePage(boolean isEnabledGymModeGuidePage) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_ENABLED_GYM_MODE_GUIDE_PAGE, isEnabledGymModeGuidePage);
		editor.commit();
	}
	
	public boolean isEnabledGymModeGuidePage() {
		return mPreferences.getBoolean(IS_ENABLED_GYM_MODE_GUIDE_PAGE, true);
	}
	
	public void setGigyaSignup(SignOption value){
		Gson gson = new Gson();
		String json = gson.toJson(value);
		 
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_SIGNUP, json);
		editor.commit();
	}

	public SignOption getGigyaSignup(){
		SignOption signOption = null;
		Gson gson = new Gson();
		
		String json = mPreferences.getString(GIGYA_SIGNUP, "");
		signOption = gson.fromJson(json, SignOption.class);
		return signOption;
	}
	
	// FaceBook first name and last name
	public void setGigyaFBFirstName(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_FB_FIRST_NAME, value);
		editor.commit();
	}
	
	public String getGigyaFBFirstName(){
		String value = mPreferences.getString(GIGYA_FB_FIRST_NAME, "");
		return value;
	}
	
	public void setGigyaFBLastName(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_FB_LAST_NAME, value);
		editor.commit();
	}
	
	public String getGigyaFBLastName(){
		String value = mPreferences.getString(GIGYA_FB_LAST_NAME, "");
		return value;
	}
	
	public void setGigyaFBEmail(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_FB_EMAIL, value);
		editor.commit();
	}
	
	public String getGigyaFBEmail(){
		String value = mPreferences.getString(GIGYA_FB_EMAIL, "");
		return value;
	}
	
	public void setGigyaSessionToken(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_LOGIN_SESSION_TOKEN, value);
		editor.commit();
	}
	
	public String getGigyaSessionToken(){
		String value = mPreferences.getString(GIGYA_LOGIN_SESSION_TOKEN, "");
		return value;
	}
	
	public void setGigyaSessionSecret(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_LOGIN_SESSION_SECRET, value);
		editor.commit();
	}
	
	public String getGigyaSessionSecret(){
		String value = mPreferences.getString(GIGYA_LOGIN_SESSION_SECRET, "");
		return value;
	}
	
	//
	
	// Twitter first name and last name
	public void setGigyaTwitterFirstName(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_TWITTER_FIRST_NAME, value);
		editor.commit();
	}
	
	public String getGigyaTwitterFirstName(){
		String value = mPreferences.getString(GIGYA_TWITTER_FIRST_NAME, "");
		return value;
	}
	
	public void setGigyaTwitterLastName(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_TWITTER_LAST_NAME, value);
		editor.commit();
	}
	
	public String getGigyaTwitterLastName(){
		String value = mPreferences.getString(GIGYA_TWITTER_LAST_NAME, "");
		return value;
	}
	
	public void setGigyaTwitterEmail(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_TWITTER_EMAIL, value);
		editor.commit();
	}
	
	public String getGigyaTwitterEmail(){
		String value = mPreferences.getString(GIGYA_TWITTER_EMAIL, "");
		return value;
	}
	//
	
	// Google first name and last name
	public void setGigyaGoogleFirstName(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_GOOGLE_FIRST_NAME, value);
		editor.commit();
	}
	
	public String getGigyaGoogleFirstName(){
		String value = mPreferences.getString(GIGYA_GOOGLE_FIRST_NAME, "");
		return value;
	}

	
	public void setGigyaGoogleLastName(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_GOOGLE_LAST_NAME, value);
		editor.commit();
	}
	
	public String getGigyaGoogleLastName(){
		String value = mPreferences.getString(GIGYA_GOOGLE_LAST_NAME, "");
		return value;
	}
	
	public void setGigyaGoogleEmail(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_GOOGLE_EMAIL, value);
		editor.commit();
	}
	
	public String getGigyaGoogleEmail(){
		String value = mPreferences.getString(GIGYA_GOOGLE_EMAIL, "");
		return value;
	}
	//
	
	// Hungama first name and last name
	public void setHungmaFirstName(String value){
		Editor editor = mPreferences.edit();
		editor.putString(HUNGAMA_FIRST_NAME, value);
		editor.commit();
	}
	
	public String getHungmaFirstName(){
		String value = mPreferences.getString(HUNGAMA_FIRST_NAME, "");
		return value;
	}
	
	public void setHungamaLastName(String value){
		Editor editor = mPreferences.edit();
		editor.putString(HUNGAMA_LAST_NAME, value);
		editor.commit();
	}
	
	public String getHungamaLastName(){
		String value = mPreferences.getString(HUNGAMA_LAST_NAME, "");
		return value;
	}
	
	public void setHungamaEmail(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_GOOGLE_EMAIL, value);
		editor.commit();
	}
	
	public String getHungamaEmail(){
		String value = mPreferences.getString(GIGYA_GOOGLE_EMAIL, "");
		return value;
	}
	
	public void setGiGyaFBThumbUrl(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_FB_THUMB_URL, value);
		editor.commit();
	}
	
	public String getGiGyaFBThumbUrl(){
		String value = mPreferences.getString(GIGYA_FB_THUMB_URL, "");
		return value;
	}
	
	public void setGiGyaTwitterThumbUrl(String value){
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_TWITTER_THUMB_URL, value);
		editor.commit();
	}
	
	public String getGiGyaTwitterThumbUrl(){
		String value = mPreferences.getString(GIGYA_TWITTER_THUMB_URL, "");
		return value;
	}
	
	public void setPlayerVolume(int value){
		Editor editor = mPreferences.edit();
		editor.putInt(PLAYER_VOLUME, value);
		editor.commit();
	}
	
	public int getPlayerVolume(){
		int value = mPreferences.getInt(PLAYER_VOLUME, 50);
		return value;
	}
	
	public void setBitRateState(int value){
		Editor editor = mPreferences.edit();
		editor.putInt(BITRATE_STATE, value);
		editor.commit();
	}
	
	public int getBitRateState(){
		int value = mPreferences.getInt(BITRATE_STATE, BITRATE_AUTO);
		return value;
	}
	
	public void setBitRate(int value){
		Editor editor = mPreferences.edit();
		editor.putInt(BITRATE, value);
		editor.commit();
	}
	
	public int getBitRate(){
		int value = mPreferences.getInt(BITRATE, BITRATE_NONE);
		return value;
	}
	
	public void setBandwidth(long value){
		Editor editor = mPreferences.edit();
		editor.putLong(BANDWIDTH, value);
		editor.commit();
	}
	
	public long getBandwidth(){
		long value = mPreferences.getLong(BANDWIDTH,0);
		return value;
	}
	
	public void setFileSizeForBitrateCalculation(int value){
		Editor editor = mPreferences.edit();
		editor.putInt(FILE_SIZE_FOR_BITRATE_CALCULATION, value);
		editor.commit();
	}
	
	public int getFileSizeForBitrateCalculation(){
		int value = mPreferences.getInt(FILE_SIZE_FOR_BITRATE_CALCULATION, BITRATE_NONE);
		return value;
	}
	
	public void setHintsState(boolean value){
		Editor editor = mPreferences.edit();
		editor.putBoolean(HINTS_STATE, value);
		editor.commit();
	}
	
	public boolean getHintsState(){
		boolean value = mPreferences.getBoolean(HINTS_STATE, false);
		return value;
	}
	
	// ======================================================
	// Settings for syncing prefetched moods, due to heavy long process.
	// ======================================================
	
	private Object mPrefetchMoodsSuccessFlagMutext = new Object();
	
	public void setDiscoverPrefetchMoodsSuccess(boolean isSucces) {
		synchronized (mPrefetchMoodsSuccessFlagMutext) {
			Editor editor = mPreferences.edit();
			editor.putBoolean(DISCOVER_PREFETCH_MOODS_SUCCESS_FLAG, isSucces);
			editor.commit();
		}
	}
	
	public boolean isDiscoverPrefetchMoodsSuccess() {
		synchronized (mPrefetchMoodsSuccessFlagMutext) {
			return mPreferences.getBoolean(DISCOVER_PREFETCH_MOODS_SUCCESS_FLAG, false);
		}
	}
	
	
	// ======================================================
	// Mobile Verified - in Download and Upgrade
	// ======================================================		
	
	public void setMobileNumber(String mobile, int isVerified){		
		 
		Editor editor = mPreferences.edit();
		editor.putInt(mobile, isVerified);
		editor.commit();
	}

	public int isMobileNumberVerified(String mobile){
		return mPreferences.getInt(mobile, -1);
	}
	
	public void setTempClickedDownloadPlan(DownloadPlan clickedPlan){		
		 
		Editor editor = mPreferences.edit();
		editor.putInt(DOWNLOAD_PLAN_ID, clickedPlan.getPlanId());
		editor.putString(DOWNLOAD_PLAN_TYPE, clickedPlan.getType());
		editor.commit();
	}

	public DownloadPlan getTempClickedDownloadPlan(){
		
		int planId = mPreferences.getInt(DOWNLOAD_PLAN_ID, 0);
		String type = mPreferences.getString(DOWNLOAD_PLAN_TYPE, Utils.TEXT_EMPTY);
		
		if (planId != 0 && !TextUtils.isEmpty(type)) {			  
		   return new DownloadPlan(planId, Utils.TEXT_EMPTY, Utils.TEXT_EMPTY, 
				   Utils.TEXT_EMPTY, Utils.TEXT_EMPTY, Utils.TEXT_EMPTY, 0, 0, type, 0);
		}
		return null;
	}
	
	public void setTempClickedPlan(Plan clickedPlan){		
		 
		Editor editor = mPreferences.edit();
		editor.putInt(DOWNLOAD_PLAN_ID, clickedPlan.getPlanId());
		editor.putString(DOWNLOAD_PLAN_TYPE, clickedPlan.getType());
		editor.putString(DOWNLOAD_PLAN_NAME, clickedPlan.getPlanName());
		editor.putString(DOWNLOAD_PLAN_PRICE, clickedPlan.getPlanPrice());
		editor.putString(DOWNLOAD_PLAN_CURRENCY, clickedPlan.getPlanCurrency());
		editor.putInt(DOWNLOAD_PLAN_DURATION, clickedPlan.getPlanDuration());
		editor.putString(DOWNLOAD_MSISDN, clickedPlan.getMsisdn());
		editor.putString(DOWNLOAD_SUBSCRIPTION_STATUS, clickedPlan.getSubscriptionStatus());
		editor.putString(DOWNLOAD_VALIDITY_DATE, clickedPlan.getValidityDate());
		editor.commit();
	}

	public Plan getTempClickedPlan(){
		
		int planId = mPreferences.getInt(DOWNLOAD_PLAN_ID, 0);
		String type = mPreferences.getString(DOWNLOAD_PLAN_TYPE, Utils.TEXT_EMPTY);
		
		String PlanName = mPreferences.getString(DOWNLOAD_PLAN_NAME, Utils.TEXT_EMPTY);
		String planPrice = mPreferences.getString(DOWNLOAD_PLAN_PRICE, Utils.TEXT_EMPTY);
		String planCurrency = mPreferences.getString(DOWNLOAD_PLAN_CURRENCY, Utils.TEXT_EMPTY);
		int planDuration = mPreferences.getInt(DOWNLOAD_PLAN_DURATION, 0);
		String msisdn = mPreferences.getString(DOWNLOAD_MSISDN, Utils.TEXT_EMPTY);
		String subscriptionStatus = mPreferences.getString(DOWNLOAD_SUBSCRIPTION_STATUS, Utils.TEXT_EMPTY);
		String validityDate = mPreferences.getString(DOWNLOAD_VALIDITY_DATE, Utils.TEXT_EMPTY);
		
		if (planId != 0 && !TextUtils.isEmpty(type)) {			  
		   return new Plan(planId, PlanName, planPrice, planCurrency, planDuration, msisdn, subscriptionStatus, Utils.TEXT_EMPTY, validityDate, Utils.TEXT_EMPTY, Utils.TEXT_EMPTY, type);
		}
		return null;
	}
	
	public void setBadgesAndCoinsForVideoActivity(BadgesAndCoins objFromOperation) {
		Editor editor = mPreferences.edit();
		editor.putInt(BADGES_AND_COINS_BADGES_EARNED, objFromOperation.getBadgesEarned());
		editor.putInt(BADGES_AND_COINS_DISPLAY_CASE, objFromOperation.getDisplayCase());
		editor.putInt(BADGES_AND_COINS_POINTS_EARNED, objFromOperation.getPointsEarned());
		editor.putString(BADGES_AND_COINS_BADGE_NAME, objFromOperation.getBadgeName());
		editor.putString(BADGES_AND_COINS_BADGE_URL, objFromOperation.getBadgeUrl());
		editor.putString(BADGES_AND_COINS_MESSAGE, objFromOperation.getMessage());
		editor.putString(BADGES_AND_COINS_NEXT_DESCRIPTION, objFromOperation.getNextDescription());			
		editor.commit();
	}
	
	public BadgesAndCoins getBadgesAndCoinsForVideoActivity() {
		int badgesEarned = mPreferences.getInt(BADGES_AND_COINS_BADGES_EARNED, 0);
		int displayCase = mPreferences.getInt(BADGES_AND_COINS_DISPLAY_CASE, 0);
		int pointsEarned = mPreferences.getInt(BADGES_AND_COINS_POINTS_EARNED, 0);
		String badgeName = mPreferences.getString(BADGES_AND_COINS_BADGE_NAME, Utils.TEXT_EMPTY);
		String badgeUrl = mPreferences.getString(BADGES_AND_COINS_BADGE_URL, Utils.TEXT_EMPTY);
		String message = mPreferences.getString(BADGES_AND_COINS_MESSAGE, Utils.TEXT_EMPTY);
		String nextDescription = mPreferences.getString(BADGES_AND_COINS_NEXT_DESCRIPTION, Utils.TEXT_EMPTY);
		
		BadgesAndCoins badgesAndCoins = new BadgesAndCoins();
		badgesAndCoins.setBadgeName(badgeName);
		badgesAndCoins.setBadgesEarned(badgesEarned);
		badgesAndCoins.setBadgeUrl(badgeUrl);
		badgesAndCoins.setDisplayCase(displayCase);
		badgesAndCoins.setMessage(message);
		badgesAndCoins.setNextDescription(nextDescription);
		badgesAndCoins.setPointsEarned(pointsEarned);
		
		if (!badgesAndCoins.getMessage().equalsIgnoreCase(Utils.TEXT_EMPTY)) {
			return badgesAndCoins;
		}
		return null;
	}
	
	// ======================================================
	// One Time Badge appirater
	// ======================================================	
	
	public void setIsFirstBadgeDisplayed(boolean isFirstBadgeDisplayed) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_FIRST_BADGE_DISPLAYED, isFirstBadgeDisplayed);
		editor.commit();
	}
	
	public boolean isFirstBadgeDisplayed() {
		return mPreferences.getBoolean(IS_FIRST_BADGE_DISPLAYED, true);
	}
	
	
	// ======================================================
	// Moods Prefetching
	// ======================================================
	
	public void setHasSuccessedPrefetchingMoods(boolean hasSuccess) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(MOODS_PREFETCHING_SUCCESS, hasSuccess);
		editor.commit();
	}
	
	public boolean hasSuccessedPrefetchingMoods() {
		return mPreferences.getBoolean(MOODS_PREFETCHING_SUCCESS, false);
	}
	
	// ======================================================
	// Time Read from CM
	// ======================================================
	
	public void setTimeReadDelta(long value){
		Editor editor = mPreferences.edit();
		editor.putLong(TIME_READ_DELTA, value);
		editor.commit();
	}
	
	public long getTimeReadDelta(){
		Date date = new Date();		
		long value = mPreferences.getLong(TIME_READ_DELTA, date.getTime());
		return value;
	}

	// ======================================================
	// Version Check
	// ======================================================
	
	public synchronized void setisVersionChecked(boolean isVersionChecked) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_VERSION_CHECKED, isVersionChecked);
		editor.commit();
	}
	
	public boolean isVersionChecked() {
		return mPreferences.getBoolean(IS_VERSION_CHECKED, false);
	}
	
	// ======================================================
	// Time Read from CM
	// ======================================================
	
	public void setContentFormat(String value){
		Editor editor = mPreferences.edit();
		editor.putString(CONTENT_FORMAT, value);
		editor.commit();
	}
	
	public String getContentFormat(){
		String value = mPreferences.getString(CONTENT_FORMAT, Utils.TEXT_EMPTY);
		return value;
	}
	
	public void setSubscriptionIABcode(String code){
		Editor editor = mPreferences.edit();
		editor.putString(SUBSCRIPTION_IAB_CODE, code);
		editor.commit();
	}
	
	public String getSubscriptionIABcode(){
		String code = mPreferences.getString(SUBSCRIPTION_IAB_CODE, Utils.TEXT_EMPTY);
		return code;
	}
	
	public void setSubscriptionIABpurchseToken(String purchseToken){
		Editor editor = mPreferences.edit();
		editor.putString(SUBSCRIPTION_IAB_PURCHSE_TOKEN, purchseToken);
		editor.commit();
	}
	
	public String getSubscriptionIABpurchseToken(){
		String purchseToken = mPreferences.getString(SUBSCRIPTION_IAB_PURCHSE_TOKEN, Utils.TEXT_EMPTY);
		return purchseToken;
	}
}
