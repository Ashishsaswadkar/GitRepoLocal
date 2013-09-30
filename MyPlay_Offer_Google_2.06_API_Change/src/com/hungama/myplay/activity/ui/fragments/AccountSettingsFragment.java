package com.hungama.myplay.activity.ui.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.catchmedia.SignOption;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupField;
import com.hungama.myplay.activity.data.dao.hungama.ShareSettingsResponse;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.gigya.FBFriend;
import com.hungama.myplay.activity.gigya.GigyaManager;
import com.hungama.myplay.activity.gigya.GoogleFriend;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerConsumerProxyCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerInfoReadOperation;
import com.hungama.myplay.activity.operations.hungama.ShareSettingsOperation;
import com.hungama.myplay.activity.services.InventoryLightService;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.adapters.SettingsAdapter;
import com.hungama.myplay.activity.util.FileUtils;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

/**
 * Allows to view properties about the given user singed to the application with the given
 * account.
 */
public class AccountSettingsFragment extends Fragment implements OnClickListener,
																 CommunicationOperationListener,
																 OnGigyaResponseListener{

	private static final String TAG = "AccountSettingsFragment";
	
	private static final String SONGS_LISTEN = "songs_listen";
	private static final String MY_FAVORITES = "my_favorites";
	private static final String SONGS_DOWNLOAD = "songs_download";
	private static final String MY_COMMENTS = "my_comments";
	private static final String MY_BADGES = "my_badges";
	private static final String VIDEOS_WATCHED = "videos_watched";
	private static final String VIDEOS_DOWNLOAD = "videos_download";
	
	public static final String PROVIDER = "provider";
	
	private Context mContext;
	
	// Views
	private ImageView thumbImageView;
	private TextView nameTextView;
	private TextView emailTextView;
	private TextView logoutTextView;
	private RelativeLayout secondaryLayout;
	private RelativeLayout sharingSettingsLayout;
	private RelativeLayout accountDetailsLayout;
	private ListView settingsListView;
	
	// Managers 
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private GigyaManager mGigyaManager;
	
	// Data members
	private SocialNetwork providerType;
	private Map<String, Integer> settingsMap;
	private String fname = "";
	private String lname = "";
	private String email = "";

	// Image Fetcher
//	private ImageFetcher mImageFetcher = null;
	
	// Adapter
	private SettingsAdapter adapter;
	private boolean mIsActivityResumed = false;
	
	// 
	private boolean mIsVisible;	
	
	// ======================================================
	// Life cycle callbacks.
	// ======================================================
	
	public AccountSettingsFragment(Context context) {
		
		mContext = context;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		mGigyaManager = new GigyaManager(getActivity());
		mGigyaManager.setOnGigyaResponseListener(this);
		
		providerType = (SocialNetwork) getArguments().get(PROVIDER);
		
		// initializes the image loader.
		int imageSize = getResources().getDimensionPixelSize(R.dimen.search_result_line_image_size);

		// creates the cache.
		ImageCache.ImageCacheParams cacheParams =
				new ImageCache.ImageCacheParams(getActivity(), DataManager.FOLDER_THUMBNAILS_FRIENDS);
		cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);

//		mImageFetcher = new ImageFetcher(getActivity(), imageSize);
//		mImageFetcher.setLoadingImage(R.drawable.background_home_tile_album_default);
//		mImageFetcher.addImageCache(getChildFragmentManager(), cacheParams);
//		mImageFetcher.setImageFadeIn(true);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// Fetch the root view
		View rootView = inflater.inflate(R.layout.fragment_account_settings, container, false);
		
		thumbImageView = (ImageView) rootView.findViewById(R.id.thumbnail_imageview);
		nameTextView = (TextView) rootView.findViewById(R.id.name_textview);
		emailTextView = (TextView) rootView.findViewById(R.id.email_textview);
		logoutTextView = (TextView) rootView.findViewById(R.id.logout_textview);
		secondaryLayout = (RelativeLayout) rootView.findViewById(R.id.secondary_layout);
		sharingSettingsLayout = (RelativeLayout) rootView.findViewById(R.id.sharing_settings_layout);
		settingsListView = (ListView) rootView.findViewById(R.id.settings_listview);
		accountDetailsLayout = (RelativeLayout) rootView.findViewById(R.id.acccount_details_layout);
		
		if(providerType == null){
			secondaryLayout.setVisibility(View.VISIBLE);
		}
		
		if(providerType != null && providerType == SocialNetwork.FACEBOOK){
			// Get the share settings for FaceBook
			mDataManager.getSharingSettings(this, false,"",0);
			
			// Show the share settings 
			sharingSettingsLayout.setVisibility(View.VISIBLE);
		}
		
		setViewsListeners();
		
		setViews();
		
		setAdapter();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
//		if (mImageFetcher != null) {
//			mImageFetcher.setExitTasksEarly(false);
//		}
		
		mIsActivityResumed = true;
	}

	@Override
	public void onPause() {
		super.onPause();
//		if (mImageFetcher != null) {
//			mImageFetcher.setExitTasksEarly(true);
//			mImageFetcher.flushCache();
//		}
		
		mIsActivityResumed = false;
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
	
		switch (view.getId()) {
		case R.id.logout_textview:
			
			showLogoutDialog();

			break;
			
		case R.id.toggle_button:

			ToggleButton tb = (ToggleButton) view;
			String str = (String) view.getTag();
			updateSharingSettings(str, tb.isChecked());
			
			break;
		default:
			break;
		}
	}

	
	// ======================================================
	// Operation callbacks.
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		showLoadingDialogFragment();
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {

		switch (operationId) {
		case (OperationDefinition.Hungama.OperationId.SHARE_SETTINGS):

			ShareSettingsResponse response = 
			(ShareSettingsResponse) responseObjects.get(ShareSettingsOperation.RESULT_KEY_SHARE_SETTINGS);
	
			// Build the map for the adapter
			settingsMap = new HashMap<String, Integer>();
			
			if (getActivity() != null) {
				String[] keys = getResources().getStringArray(R.array.facebook_sharing_properties);
				
				settingsMap.put(keys[0], response.data.songs_listen);
				settingsMap.put(keys[1], response.data.my_favorites);
				settingsMap.put(keys[2], response.data.songs_download);
				settingsMap.put(keys[3], response.data.my_comments);
				settingsMap.put(keys[4], response.data.my_badges);
				settingsMap.put(keys[5], response.data.videos_watched);
				settingsMap.put(keys[6], response.data.videos_download);
				
				List<String> propList = new ArrayList<String>();
				
				propList = Arrays.asList(keys);  
				
				adapter = new SettingsAdapter(getActivity(), propList, settingsMap, this);
				
				TextView headerView = (TextView) ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.settings_title_row_layout, null, false);
				
				headerView.setText(R.string.sharing_settings);
				 
				settingsListView.addHeaderView(headerView);
				
				settingsListView.setAdapter(adapter);
			}
			
			hideLoadingDialogFragment();

		break;

		case (OperationDefinition.Hungama.OperationId.SHARE_SETTINGS_UPDATE):
			
			//hideLoadingDialog();
			hideLoadingDialogFragment();
			
			break;
			
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ):
			Logger.i(TAG, "Successed getting partners info.");
			
			List<SignOption> signOptions = (List<SignOption>) responseObjects.get(PartnerInfoReadOperation.RESPONSE_KEY_OBJECT_SIGN_OPTIONS);

			// Set the Gigya setID 
			SignOption gigyaSignup = signOptions.get(2);
			mApplicationConfigurations.setGigyaSignup(gigyaSignup);
			
			/*
			 * performs silent login.
			 */
			SignOption signOption = signOptions.get(3);
			
			Map<String, Object> signupFields = new HashMap<String, Object>();	
			SignupField phoneNumberFields = signOption.getSignupFields().get(0);
			SignupField hardwareIDFields = signOption.getSignupFields().get(1);
			
			DeviceConfigurations deviceConfigurations = mDataManager.getDeviceConfigurations(); 
			// adds the device's phone number if available.
			String phoneNumber = deviceConfigurations.getDevicePhoneNumber();
			Logger.d(TAG, "device phone number: " + phoneNumber);
			if (!TextUtils.isEmpty(phoneNumber)) {
				Map<String, Object> phoneNumberMap = new HashMap<String, Object>();
				phoneNumberMap.put(SignupField.VALUE, phoneNumber);
				signupFields.put(phoneNumberFields.getName(), phoneNumberMap);
			}
			// adds the device's hardware id if available.
			Map<String, Object> hardwareIDMap = new HashMap<String, Object>();
			hardwareIDMap.put(SignupField.VALUE, deviceConfigurations.getHardwareId());
			signupFields.put(hardwareIDFields.getName(), hardwareIDMap);
			
			hideLoadingDialogFragment();
			
			mDataManager.createPartnerConsumerProxy(signupFields, signOption.getSetID(), this, true);
			
			break;
		
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
			
			resetUserDetails();
			
			String activationCode = (String) responseObjects.get(ApplicationConfigurations.ACTIVATION_CODE);
			String partnerUserId = (String) responseObjects.get(ApplicationConfigurations.PARTNER_USER_ID);
			boolean isRealUser = (Boolean) responseObjects.get(ApplicationConfigurations.IS_REAL_USER);
			Map<String, Object> signupFieldsMap = (Map<String, Object>) 
					responseObjects.get(PartnerConsumerProxyCreateOperation.RESPONSE_KEY_OBJECT_SIGNUP_FIELDS);
			
			/*
			 * iterates thru the original signup fields, 
			 * looking for the registered phone number, if exists,
			 * stores it in the application configuration 
			 * as part of the user's credentials.
			 */
			Map<String, Object> fieldMap = (Map<String, Object>) signupFieldsMap.get("phone_number");
			String value = "";
			if (fieldMap != null) {
				value = (String) fieldMap.get(SignupField.VALUE);
			}
			mApplicationConfigurations.setUserLoginPhoneNumber(value);
			
			// stores partner user id to connect with Hungama REST API.
			mApplicationConfigurations.setPartnerUserId(partnerUserId);
			mApplicationConfigurations.setIsRealUser(isRealUser);
			
			mDataManager.createDeviceActivationLogin(activationCode, this);
			
			hideLoadingDialogFragment();
			
			break;
			
		case (OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE):
			
			Map<String, Object> responseMap = (Map<String, Object>) responseObjects.get(CMOperation.RESPONSE_KEY_GENERAL_OBJECT);
			// stores the session and other crucial properties.
			String sessionID = (String) responseMap.get(ApplicationConfigurations.SESSION_ID);
			int householdID = ((Long) responseMap.get(ApplicationConfigurations.HOUSEHOLD_ID)).intValue();
			int consumerID = ((Long) responseMap.get(ApplicationConfigurations.CONSUMER_ID)).intValue();
			String passkey = (String) responseMap.get(ApplicationConfigurations.PASSKEY);
			
			mApplicationConfigurations.setSessionID(sessionID);
			mApplicationConfigurations.setHouseholdID(householdID);
			mApplicationConfigurations.setConsumerID(consumerID);
			mApplicationConfigurations.setPasskey(passkey);
			
			String secret = mApplicationConfigurations.getGigyaSessionSecret();
			String token = mApplicationConfigurations.getGigyaSessionToken();
			
			if(!TextUtils.isEmpty(secret) && !TextUtils.isEmpty(token)){
				GigyaManager mGigyaManager = new GigyaManager(getActivity());
				mGigyaManager.setSession(token, secret);
			}
			
//			if (getActivity() != null) {
//												
//				// For avoiding perform an action after onSaveInstanceState.
//				new Handler().post(new Runnable() {
//					
//		            public void run() {
//		            	
//		            	// Sync's the inventory.
//						Intent inventoryLightService = new Intent(getActivity().getApplicationContext(), InventoryLightService.class);
//		            	getActivity().startService(inventoryLightService);
//						getFragmentManager().popBackStack();
//		            }
//		        });
//			}
			
			performInventoryLightService();								
			break;
		}
	}
	
	private void performInventoryLightService() {
		
		// Sync's the inventory.			
		Intent inventoryLightService = new Intent(mContext, InventoryLightService.class);
		mContext.startService(inventoryLightService);			
		FragmentManager fragmentManager = getFragmentManager();			
		if (fragmentManager != null) {
			
			fragmentManager.popBackStack();				
			hideLoadingDialogFragment();
		}
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		
		mIsVisible = isVisibleToUser;
	}
	
	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		hideLoadingDialogFragment();
		
		if (!TextUtils.isEmpty(errorMessage)) {
			Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
		}
	}

	
	// ======================================================
	// Helper methods.
	// ======================================================
	
	private void setViews(){
		
		// Get the FirstName, LastNamt, Email  
		
		// Get the ThumbUrl
		if(providerType == SocialNetwork.FACEBOOK){
			String fbThumbUrl = mApplicationConfigurations.getGiGyaFBThumbUrl();	
			loadThumbnailUrl(fbThumbUrl);
			
			setFacebookDetails();
			
		}else if(providerType == SocialNetwork.TWITTER){
			String twitterThumbUrl = mApplicationConfigurations.getGiGyaTwitterThumbUrl();
			loadThumbnailUrl(twitterThumbUrl);
			
			setTwitterDetails();
			
		}else if(providerType == SocialNetwork.GOOGLE){
			
			thumbImageView.setVisibility(View.GONE);
			
			setGoogleDetails();
			
			if(TextUtils.isEmpty(fname) && TextUtils.isEmpty(lname)){
				fname = email;
				emailTextView.setVisibility(View.INVISIBLE);
			}
			
		}else{
			
			thumbImageView.setVisibility(View.GONE);

			setHungamaDetails();
			
			if(TextUtils.isEmpty(fname) && TextUtils.isEmpty(lname)){
			
				if(mGigyaManager.isFBConnected()){
					
					setFacebookDetails();
					
				}else if(mGigyaManager.isTwitterConnected()){
					
					setTwitterDetails();
					
				}else if(mGigyaManager.isGoogleConnected()){
					
					setGoogleDetails();
					
				}else{
					// We have no first name and last name to show so invisible this layout
					accountDetailsLayout.setVisibility(View.INVISIBLE);
				}
			}
		}
		
		nameTextView.setText(fname + " " + lname);
		emailTextView.setText(email);
		
	}
	
	private void setAdapter(){
		
	}
	
	private void setViewsListeners(){
		
		logoutTextView.setOnClickListener(this);
		
	}
	
	private void loadThumbnailUrl(String url){
		
		Picasso.with(getActivity()).cancelRequest(thumbImageView);
		if (getActivity().getApplicationContext() != null && !TextUtils.isEmpty(url)) {
//			mImageFetcher.loadImage(url, thumbImageView);
			Picasso.with(getActivity())
					.load(url)
					.placeholder(R.drawable.background_home_tile_album_default)
					.into(thumbImageView);
		} 
	}
	
	private void setFacebookDetails(){
	
		// Get the FirstName LastNamt, Email  
		fname = mApplicationConfigurations.getGigyaFBFirstName();
		lname = mApplicationConfigurations.getGigyaFBLastName();
		email = mApplicationConfigurations.getGigyaFBEmail();
	}
	
	private void setTwitterDetails(){
		
		// Get the FirstName LastNamt, Email  
		fname = mApplicationConfigurations.getGigyaTwitterFirstName();
		lname = mApplicationConfigurations.getGigyaTwitterLastName();
		email = mApplicationConfigurations.getGigyaTwitterEmail();
		
	}
	
	private void setGoogleDetails(){
		
		// Get the FirstName LastNamt, Email 
		fname = mApplicationConfigurations.getGigyaGoogleFirstName();
		lname = mApplicationConfigurations.getGigyaGoogleLastName();
		email = mApplicationConfigurations.getGigyaGoogleEmail();
	}
	
	private void setHungamaDetails(){
		
		// Get the FirstName LastNamt, Email 
		fname = mApplicationConfigurations.getHungmaFirstName();
		lname = mApplicationConfigurations.getHungamaLastName();
		email = mApplicationConfigurations.getHungamaEmail();
	}
	
	private void updateSharingSettings(String key, boolean value){
	
		int state;
		String shareSettingType = "";
		
		if(key.equalsIgnoreCase(getActivity().getString(R.string.songs_i_listen_to))){
			shareSettingType = SONGS_LISTEN;
		}else if(key.equalsIgnoreCase(getActivity().getString(R.string.my_favorite))){
			shareSettingType = MY_FAVORITES;
		}else if(key.equalsIgnoreCase(getActivity().getString(R.string.songs_i_downloaded))){
			shareSettingType = SONGS_DOWNLOAD;
		}else if(key.equalsIgnoreCase(getActivity().getString(R.string.my_comments))){
			shareSettingType = MY_COMMENTS;
		}else if(key.equalsIgnoreCase(getActivity().getString(R.string.my_badges))){
			shareSettingType = MY_BADGES;
		}else if(key.equalsIgnoreCase(getActivity().getString(R.string.videos_watched))){
			shareSettingType = VIDEOS_WATCHED;
		}else if(key.equalsIgnoreCase(getActivity().getString(R.string.videos_downloaded))){
			shareSettingType = VIDEOS_DOWNLOAD;
		}
		
		if(value){
			state = 1;
		}else{
			state = 0;
		}
		
		mDataManager.getSharingSettings(this, true, shareSettingType , state);
	}
	
	private void showLoadingDialogFragment() {
		
		FragmentManager fragmentManager = getFragmentManager();
		
		if (fragmentManager != null) {
		
			Fragment fragment = fragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
			
			if (fragment == null && mIsActivityResumed) {
				
				LoadingDialogFragment dialogFragment = LoadingDialogFragment.newInstance(R.string.application_dialog_loading);
				dialogFragment.setCancelable(true);
				dialogFragment.show(fragmentManager, LoadingDialogFragment.FRAGMENT_TAG);
			}
		}
	}
	
	private void hideLoadingDialogFragment() {
		
		if (getActivity() != null) {
			
			FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
			
			if (fragmentManager != null) {
				
				Fragment fragment = fragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
				
				if (fragment != null) {
					
					DialogFragment fragmentDialog = (DialogFragment) fragment;
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.remove(fragmentDialog);
					fragmentDialog.dismissAllowingStateLoss();
				}
			}			
		}
	}

	private void showLogoutDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());
 
			// set title
			alertDialogBuilder.setTitle(getResources().getString(R.string.logout_dialog_title));
 
			// set dialog message
			alertDialogBuilder
				.setMessage(getResources().getString(R.string.logout_dialog_text))
				.setCancelable(true)
				.setPositiveButton(R.string.exit_dialog_text_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						showLoadingDialogFragment();
						
						// Log out from Gigya
						mGigyaManager.logout();

						// 
						mApplicationConfigurations.setSessionID(null);
						mApplicationConfigurations.setConsumerID(0);
						mApplicationConfigurations.setConsumerRevision(0);
						mApplicationConfigurations.setHouseholdID(0);
						mApplicationConfigurations.setHouseholdRevision(0);
						mApplicationConfigurations.setPasskey(null);
						mApplicationConfigurations.setIsRealUser(false);
						mApplicationConfigurations.setPartnerUserId(mApplicationConfigurations.getSkippedPartnerUserId());
						mApplicationConfigurations.setGigyaSessionSecret(null);
						mApplicationConfigurations.setGigyaSessionToken(null);
						
						// Delete all locale playlists on device
						DataManager mDataManager = DataManager.getInstance(getActivity());
						Map<Long, Playlist> empty = new HashMap<Long, Playlist>();
						mDataManager.storePlaylists(empty);
						
						// Silent Login to Hungama 
						performSilentLogin();
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
	
	// ======================================================
	// Gigya callbacks.
	// ======================================================
	
	@Override
	public void onGigyaLoginListener(SocialNetwork provider,
			Map<String, Object> signupFields, long setId) {}

	@Override
	public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList) {}

	@Override
	public void onSocializeGetContactsListener(
			List<GoogleFriend> googleFriendsList) {}

	@Override
	public void onSocializeGetUserInfoListener() {}

	@Override
	public void onGigyaLogoutListener() {		
        //performSilentLogin();
	}

	@Override
	public void onFacebookInvite() {}

	@Override
	public void onTwitterInvite() {}
	
	
	private void performSilentLogin() {
		mDataManager.readPartnerInfo(this);
	}
	
	private void resetUserDetails() {
		//Delete media folder
//		FileUtils fileUtils = new FileUtils(getActivity());
//		String hungamaFolder = getResources().getString(R.string.download_media_folder);
//		File directory = new File(Environment.getExternalStorageDirectory()
//				.getPath() + "/" + hungamaFolder);
//
//		if (directory.exists()) {
//        	fileUtils.deleteDirectoryRecursively(directory);
//        }
        
        mApplicationConfigurations.setIsRealUser(false);
        mApplicationConfigurations.setIsUserHasSubscriptionPlan(false);
        mApplicationConfigurations.setGigyaSessionSecret("");
        mApplicationConfigurations.setGigyaSessionToken("");
        
        // When logging out we need to restore the skipped partner user id
        String skippedPartnerUserId = mApplicationConfigurations.getSkippedPartnerUserId();
        if(!TextUtils.isEmpty(skippedPartnerUserId)){
        	mApplicationConfigurations.setPartnerUserId(skippedPartnerUserId);
        }
        
        mDataManager.deleteCurrentSubscriptionPlan();
        mApplicationConfigurations.setIsUserHasSubscriptionPlan(false);
	}
}
