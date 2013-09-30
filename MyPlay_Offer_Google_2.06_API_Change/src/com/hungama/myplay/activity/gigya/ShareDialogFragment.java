package com.hungama.myplay.activity.gigya;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.data.dao.hungama.social.ShareURL;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerConsumerProxyCreateOperation;
import com.hungama.myplay.activity.operations.hungama.SocialGetUrlOperation;
import com.hungama.myplay.activity.ui.fragments.LoadingDialogFragment;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.hungama.myplay.activity.util.images.ImageCache.ImageCacheParams;
import com.squareup.picasso.Picasso;

public class ShareDialogFragment extends SherlockDialogFragment implements OnClickListener,
																   OnGigyaResponseListener,
																   CommunicationOperationListener{

	public static final String FRAGMENT_TAG = "ShareDialogFragment";
	public static final String TRIVIA = "trivia";
	public static final String LYRICS = "lyrics";
	
	private static final String VALUE = "value";
	
	// Statics
	public final static String DATA 		   = "data";
	public final static String THUMB_URL_DATA  = "thumb_url_data";
	public final static String TITLE_DATA 	   = "title_data";
	public final static String SUB_TITLE_DATA  = "sub_title_data";
	public final static String MEDIA_TYPE_DATA = "media_type_data";
	public final static String EDIT_TEXT_DATA  = "edit_text_data";
	public final static String TRACK_NUMBER_DATA  = "track_number_data";
	public final static String CONTENT_ID_DATA  = "content_id_data";
	public final static String TYPE_DATA  = "type_data";
	
	// Views
	private Button facebookPostButton;
	private Button twitterPostButton;
	private Button emailPostButton;
	private Button smsPostButton;
	private Button postButton;
	private ImageButton closeButton;
	private EditText shareEditText;
	private TextView title;
	private TextView subTitle;
	private ImageView thumbImageView;
	private ImageView mediaImageType;
	
	// Data members
	private TwitterLoginFragment mTwitterLoginFragment;
	private Map<String, Object> mData;
	private SocialNetwork provider;
	
	private String thumbUrlStr;
	private String titleStr;
	private String subTitleStr;
	private String editTextStr;
	private String typeStr;
	private String media;
	private String generatedUrl;
	
	private Integer trackNubmerStr;
	
	private Long contentId;
	
	private MediaType mediaType;
	
	// Prefix
	private String trackPrefix;
	private String albumPrefix;
	private String playlistPrefix;
	private String artistPrefix;
	private String videoPrefix;
	private String playlistAlbumSuffix;
	
	private boolean fbClicked = false;
	private boolean ttClicked = false;
	
	// Image Fetcher
//	private ImageFetcher mImageFetcher;
	
	private Context mContext;
	
	// Managers
	private GigyaManager mGigyaManager;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	
	private FragmentManager mFragmentManager;
	private LoadingDialogFragment mLoadingDialogFragment = null;
	
    public static ShareDialogFragment newInstance(Map<String, Object> data) {
    	ShareDialogFragment f = new ShareDialogFragment();
    	
        // Supply data input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(DATA,new HashMap<String, Object>(data));
        
        f.setArguments(args);

        return f;
    }
    
    
    // ======================================================
	// Life cycle callbacks.
	// ======================================================
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = getActivity().getApplicationContext();
		
		setStyle(DialogFragment.STYLE_NO_TITLE, com.actionbarsherlock.R.style.Sherlock___Theme_Dialog);
		
		mGigyaManager = new GigyaManager(getActivity());
		mGigyaManager.setOnGigyaResponseListener(this);
		mGigyaManager.socializeGetUserInfo();
				
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		
		mData = (Map<String,Object>) getArguments().getSerializable(DATA);
		
		thumbUrlStr = (String) mData.get(THUMB_URL_DATA);
		titleStr = (String) mData.get(TITLE_DATA);
		subTitleStr = (String) mData.get(SUB_TITLE_DATA);
		editTextStr = (String) mData.get(EDIT_TEXT_DATA);
		mediaType = (MediaType) mData.get(MEDIA_TYPE_DATA);
		trackNubmerStr =  (Integer) mData.get(TRACK_NUMBER_DATA);
		contentId =  (Long) mData.get(CONTENT_ID_DATA);
		typeStr = (String) mData.get(TYPE_DATA);
		
		// creates the prefixes.
		trackPrefix = getResources().getString(R.string.search_results_layout_bottom_text_for_track);
		albumPrefix = getResources().getString(R.string.search_results_layout_bottom_text_for_album);
		playlistPrefix = getResources().getString(R.string.search_results_layout_bottom_text_for_playlist);
		artistPrefix = getResources().getString(R.string.search_result_line_type_and_name_artist);
		videoPrefix = getResources().getString(R.string.search_results_layout_bottom_text_for_video);
		playlistAlbumSuffix = getResources().getString(R.string.search_results_layout_bottom_text_album_playlist);
		
		// Get the share url
		String contentIdStr = "";
		
		if(contentId != null){
			contentIdStr = String.valueOf(contentId);
		}
		
		mFragmentManager = getActivity().getSupportFragmentManager();
		
		mDataManager.getShareUrl(contentIdStr, mediaType.toString().toLowerCase(), this);
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
		FlurryAgent.logEvent("Share - triggered");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(getActivity());
	}
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_share_dialog, container);
		
		// Fetch views
		facebookPostButton = (Button) view.findViewById(R.id.facebook_post_button);
		twitterPostButton = (Button) view.findViewById(R.id.twitter_post_button);
		emailPostButton = (Button) view.findViewById(R.id.email_post_button);
		smsPostButton = (Button) view.findViewById(R.id.sms_post_button);
		postButton = (Button) view.findViewById(R.id.post_button);
		closeButton = (ImageButton) view.findViewById(R.id.close_button);
		
		shareEditText = (EditText) view.findViewById(R.id.comment_edit_text);
		
		thumbImageView = (ImageView) view.findViewById(R.id.thumb_image_view);
		mediaImageType = (ImageView) view.findViewById(R.id.media_image_type);
		
		title = (TextView) view.findViewById(R.id.title);
		subTitle = (TextView) view.findViewById(R.id.sub_title);
		
		// Set listeners
		facebookPostButton.setOnClickListener(this);
		twitterPostButton.setOnClickListener(this);
		emailPostButton.setOnClickListener(this);
		smsPostButton.setOnClickListener(this);
		postButton.setOnClickListener(this);
		closeButton.setOnClickListener(this);
		
		// Disabling these buttons until socializeGetUserInfo method will triggered it's finish callback
		twitterPostButton.setBackgroundResource(R.drawable.icon_twitter_unselected);
		facebookPostButton.setBackgroundResource(R.drawable.icon_facebook_unselected);
		
		twitterPostButton.setEnabled(false);
		facebookPostButton.setEnabled(false);
		
		// these buttons will be disabled until the share URL will generated via mDataManager.getShareUrl
		emailPostButton.setEnabled(false);
		smsPostButton.setEnabled(false);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Get the thumb image
        ImageCacheParams cacheParams = 
        		new ImageCacheParams(getActivity(), DataManager.FOLDER_TILES_CACHE);

        // Set memory cache to 25% of mem class
        cacheParams.setMemCacheSizePercent(getActivity(), 0.25f);

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
//        mImageFetcher = new ImageFetcher(getActivity(), 50);
//        mImageFetcher.setLoadingImage(R.drawable.background_home_tile_album_default);
//        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
        
        // Load it!
//        mImageFetcher.loadImage(thumbUrlStr, thumbImageView);
        Picasso.with(mContext).cancelRequest(thumbImageView);
        if (mContext != null && thumbUrlStr != null && !TextUtils.isEmpty(thumbUrlStr)) {
	        Picasso.with(mContext)
	        		.load(thumbUrlStr)
	        		.placeholder(R.drawable.background_home_tile_album_default)
	        		.into(thumbImageView);
        }
		
		// Render the sub title and it's suitable icon (track/album/play list)
		if (mediaType == MediaType.TRACK) {
			mediaImageType.setBackgroundResource(R.drawable.icon_main_settings_music);
			subTitle.setText(trackPrefix + " - " + subTitleStr);
			
			media = trackPrefix.toLowerCase();
			
		}else if (mediaType == MediaType.ALBUM) {
			mediaImageType.setBackgroundResource(R.drawable.icon_main_search_album);
			subTitle.setText(albumPrefix + " - " 
								+ String.valueOf(trackNubmerStr
								+ " " + playlistAlbumSuffix));
			
			media = albumPrefix.toLowerCase();
			
		} else if (mediaType == MediaType.PLAYLIST) {
			mediaImageType.setBackgroundResource(R.drawable.icon_home_music_tile_playlist);
			subTitle.setText(playlistPrefix   + " - " 
									+ String.valueOf(trackNubmerStr 
									+ " " + playlistAlbumSuffix));
			
			media = playlistPrefix.toLowerCase();
			
		} else if (mediaType == MediaType.ARTIST) {
			mediaImageType.setBackgroundResource(R.drawable.icon_main_settings_live_radio);			
			subTitle.setText(artistPrefix);
			
			media = artistPrefix.toLowerCase();
			
		} else if (mediaType == MediaType.VIDEO){
			mediaImageType.setBackgroundResource(R.drawable.icon_main_settings_videos);
			subTitle.setText(videoPrefix + " - " + subTitleStr);
			
			media = videoPrefix.toLowerCase();
		}
		
		// Set the title text
		title.setText(titleStr);
		
		// Set the edit text
		shareEditText.setText(editTextStr);
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
//		if(mImageFetcher != null){
//			mImageFetcher.setExitTasksEarly(false);			
//		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
//		if(mImageFetcher != null){
//			mImageFetcher.setExitTasksEarly(true);
//		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
//		if(mImageFetcher != null){
//			mImageFetcher.closeCache();	
//		}
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.close_button:
			
			dismiss();
			
			break;
		case R.id.email_post_button:
			
			String editTextStr = "";
			editTextStr = shareEditText.getText().toString();
			
			if(typeStr != null){
				if(typeStr.equalsIgnoreCase(LYRICS)){
					// Share only ~4 lines of the Lyrics text (200 chars)
					editTextStr = editTextStr.substring(0,200).trim();
					editTextStr = editTextStr + "...";
					media = LYRICS;
				}else if(typeStr.equalsIgnoreCase(TRIVIA)){
					// Share the whole Trivia text
					media = TRIVIA;
				}
			}
			
			String subject = getString(R.string.share_subject, media, titleStr);
					
			String extraText = getString(R.string.share_email_body, media, generatedUrl,titleStr, 
					mApplicationConfigurations.getGigyaGoogleFirstName(),
					mApplicationConfigurations.getGigyaGoogleLastName(),
					editTextStr); 
			
			// Send Email
			Utils.invokeEmailApp(this,
								 null,
								 subject,
								 extraText);
			
			dismiss();
			
			break;
			
		case R.id.sms_post_button:
			if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
				  // THIS PHONE HAS SMS FUNCTIONALITY
				
				String smsText = getString(R.string.share_sms_text, media, generatedUrl, titleStr);
				
				// Send SMS
				Utils.invokeSMSApp(
						getActivity(), 
						smsText);
				
				dismiss();
			}else{
				  // NO SMS HERE
				Toast.makeText(getActivity(), R.string.share_dialog_no_sms_capabilities, Toast.LENGTH_LONG).show();
			}
			
			break;

		case R.id.facebook_post_button:
			
			facebookButtonClicked();
			
			break;
		
		case R.id.twitter_post_button:
			
			twitterButtonClicked();
			
			break;
			
			case R.id.post_button:
				
				if(fbClicked || ttClicked){
					
					StringBuilder providers = new StringBuilder();
					if(fbClicked){
						providers.append(SocialNetwork.FACEBOOK.toString().toLowerCase()); 
					}
					if(ttClicked){
						
						if(providers.length() != 0){
							providers.append(",");
						}
						
						providers.append(SocialNetwork.TWITTER.toString().toLowerCase());
					}
					
					String shareType;
					if(typeStr != null){
						shareType = typeStr;
					}else{
						shareType = mediaType.toString().toLowerCase();
					}
					
					// contentId: media item id
					// type: "facebook" or "twitter", if both then "facebook,twitter" 
					// provider: track video album playlist lyrics trivia
					// userText: 
					
					String encodedTextToPost = ""; 
					String editTextToPost = shareEditText.getText().toString();
					
					if(!TextUtils.isEmpty(editTextToPost)){
						try {
							encodedTextToPost = URLEncoder.encode(
									shareEditText.getText().toString(), 
									"UTF-8");
							
						} catch (UnsupportedEncodingException e) {
							
							e.printStackTrace();
						}
					}
					
					// Post it!
					mDataManager.share(contentId.intValue(), 
									   shareType, 
									   providers.toString(), 
									   encodedTextToPost, 
									   this);
					
				}else{
					
					// Non of facebook or twitter selected
					Toast.makeText(getActivity(), R.string.please_select_social_network, Toast.LENGTH_LONG).show();
				}
				
				break;
			
		default:
			break;
		}
	}
	
	
	// ======================================================
	// Communication Callbacks.
	// ======================================================
	
	@Override
	public void onStart(int operationId) {		
		showLoadingDialog(R.string.processing);
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		
		switch (operationId) {
		
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
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
				value = (String) fieldMap.get(VALUE);
			}
			mApplicationConfigurations.setUserLoginPhoneNumber(value);
	
			// stores partner user id to connect with Hungama REST API.
			mApplicationConfigurations.setPartnerUserId(partnerUserId);
			mApplicationConfigurations.setIsRealUser(isRealUser);
			// let's party!
			mDataManager.createDeviceActivationLogin(activationCode, this);

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

			if(mTwitterLoginFragment != null){
				mTwitterLoginFragment.finish();
			}
			
			if(provider == SocialNetwork.FACEBOOK){
				
				facebookPostButton.setBackgroundResource(R.drawable.icon_facebook_selected);
				fbClicked = true;
				
			}else if(provider == SocialNetwork.TWITTER){
				
				twitterPostButton.setBackgroundResource(R.drawable.icon_twitter_selected);
				ttClicked = true;
			}
			
			break;

		case (OperationDefinition.Hungama.OperationId.SOCIAL_SHARE):
			dismiss();
			
			break;
			
		case (OperationDefinition.Hungama.OperationId.SOCIAL_GET_URL):
			ShareURL shareURL = (ShareURL) responseObjects.get(SocialGetUrlOperation.RESULT_KEY_GET_SOCIAL_URL);
			if(shareURL != null){
				generatedUrl = 	shareURL.url;
			}
			
			emailPostButton.setEnabled(true);
			smsPostButton.setEnabled(true);
		
			break;
		}
		
		hideLoadingDialog();
		
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		
		hideLoadingDialog();
		
		switch (operationId) {
		case (OperationDefinition.Hungama.OperationId.SOCIAL_GET_URL):
			// No URL exist in response
			generatedUrl = "";
			emailPostButton.setEnabled(true);
			smsPostButton.setEnabled(true);
		
			break;
			
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
			mGigyaManager.cancelGigyaProviderLogin();
			
			if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
				Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
			}
			break;
			
		default:
			break;
		}
		
	}
	
	
	// ======================================================
	// Helper methods.
	// ======================================================
	
	private void facebookButtonClicked(){
		
		if(fbClicked){
			fbClicked = false;
			facebookPostButton.setBackgroundResource(R.drawable.icon_invite_facebook);
			
		}else{
			
			if(mGigyaManager.isFBConnected()){
				fbClicked = true;
				facebookPostButton.setBackgroundResource(R.drawable.icon_facebook_selected);
			}else{
				mGigyaManager.facebookLogin();
			}
		}	
	}
	
	private void twitterButtonClicked(){
		
		if(ttClicked){
			ttClicked = false;
			twitterPostButton.setBackgroundResource(R.drawable.icon_invite_twitter);
			
		}else{
			
			if(mGigyaManager.isTwitterConnected()){
				ttClicked = true;
				twitterPostButton.setBackgroundResource(R.drawable.icon_twitter_selected);
			}else{
				mGigyaManager.twitterLogin();
			}
		}	
	}

	@Override
	public void onGigyaLoginListener(SocialNetwork provider, 
			Map<String, Object> signupFields, long setId) {
		
		if(provider == SocialNetwork.TWITTER){
			// Twitter
			
//			FragmentManager mFragmentManager = getFragmentManager();
//			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
//			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
//					R.anim.slide_left_exit,
//	                R.anim.slide_right_enter,
//	                R.anim.slide_right_exit);
//			
//			TwitterLoginFragment fragment = new TwitterLoginFragment(signupFields, setId);
//			fragmentTransaction.replace(R.id.main_fragmant_container, fragment);
//			fragmentTransaction.addToBackStack(TwitterLoginFragment.class.toString());
//			fragmentTransaction.commit();
//			
//			// Listen to result from TwitterLoinFragment
//			fragment.setOnTwitterLoginListener(this);
			
			Intent i = new Intent(getActivity(), TwitterLoginActivity.class);
			Bundle b = new Bundle();
			b.putSerializable("signup_fields", (Serializable) signupFields);
			b.putLong("set_id", setId);
			i.putExtras(b);
			startActivityForResult(i, 0);
			
		}else{
			// FaceBook, Google
			
			// Call PCP 
			mDataManager.createPartnerConsumerProxy(signupFields, setId, this, false);
		}
		
		this.provider = provider;
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(resultCode == 200){
			// Call PCP 
			// It's include the email and password that user insert in TwitterLoginFragment
			Bundle b = data.getExtras();
			mDataManager.createPartnerConsumerProxy(
					(Map<String, Object>) b.getSerializable("signup_fields"),
					b.getLong("set_id"), this, false);
			
		}else if(resultCode == 500){
			mGigyaManager.removeConnetion(SocialNetwork.TWITTER);	
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList) {}

	@Override
	public void onSocializeGetContactsListener(List<GoogleFriend> googleFriendsList) {}

	@Override
	public void onGigyaLogoutListener() {}
	
	@Override
	public void onSocializeGetUserInfoListener() {
		
		if(mGigyaManager.isFBConnected()){
			facebookPostButton.setBackgroundResource(R.drawable.icon_facebook_selected);
			fbClicked = true;
		}else{
			facebookPostButton.setBackgroundResource(R.drawable.icon_facebook_unselected);			
		}
		
		if(mGigyaManager.isTwitterConnected()){
			twitterPostButton.setBackgroundResource(R.drawable.icon_twitter_selected);
			ttClicked = true;
		}else{
			twitterPostButton.setBackgroundResource(R.drawable.icon_twitter_unselected);
		}
	
		twitterPostButton.setEnabled(true);
		facebookPostButton.setEnabled(true);
		
	}
	
	protected void showLoadingDialog(int messageResource) {
		if (mLoadingDialogFragment == null && getActivity() != null && !getActivity().isFinishing()) {
			mLoadingDialogFragment = LoadingDialogFragment.newInstance(R.string.application_dialog_loading_content);
			mLoadingDialogFragment.setCancelable(true);
			mLoadingDialogFragment.show(mFragmentManager, LoadingDialogFragment.FRAGMENT_TAG);
		}
	}
	
	protected void showLoadingDialogWithoutVisibleCheck(int messageResource) {
		if (mLoadingDialogFragment == null && getActivity() != null && !getActivity().isFinishing()) {
			mLoadingDialogFragment = LoadingDialogFragment.newInstance(R.string.application_dialog_loading_content);
			mLoadingDialogFragment.setCancelable(true);
			mLoadingDialogFragment.show(mFragmentManager, LoadingDialogFragment.FRAGMENT_TAG);
		}
	}
	
	protected void hideLoadingDialog() {
		if (mLoadingDialogFragment != null && getActivity() != null && !getActivity().isFinishing()) {
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.remove(mLoadingDialogFragment);
			fragmentTransaction.commitAllowingStateLoss();
			mLoadingDialogFragment = null;
		}
	}
	
	@Override
	public void onFacebookInvite() {}
	
	@Override
	public void onTwitterInvite() {}
	
}
