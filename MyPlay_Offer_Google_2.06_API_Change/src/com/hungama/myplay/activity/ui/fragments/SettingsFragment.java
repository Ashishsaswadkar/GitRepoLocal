package com.hungama.myplay.activity.ui.fragments;

import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionCheckResponse;
import com.hungama.myplay.activity.gigya.FBFriend;
import com.hungama.myplay.activity.gigya.GigyaManager;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.gigya.GoogleFriend;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment.OnTwitterLoginListener;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerConsumerProxyCreateOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.ui.LoginActivity;
import com.hungama.myplay.activity.ui.SettingsActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;

/**
 * Shows the settings of the application, allows the user to change is accounts,
 * his membership with hungama, edit the stream and controls the music streaming.
 */
public class SettingsFragment extends Fragment implements OnClickListener, 
														  CommunicationOperationListener,
														  OnSeekBarChangeListener,
														  OnCheckedChangeListener,
														  OnGigyaResponseListener,
														  OnTwitterLoginListener{

	public static final int UPGRADE_ACTIVITY_RESULT_CODE = 1001;
	
	private static final String TAG = "SettingsFragment";
	private static final String VALUE = "value";
	
	// Views
	private LinearLayout mLayoutAccountFacebook;
	private LinearLayout mLayoutAccountTwitter;
	private LinearLayout mLayoutAccountGooglePlus; 
	private LinearLayout mLayoutAccountHungama;
	private LinearLayout mLayoutSettingsMembership;
	private LinearLayout mLayoutSettingsMyStream;
	
	private Button mButtonAccountFacebook;
	private Button mButtonAccountTwitter;
	private Button mButtonAccountGooglePlus;
	private Button mButtonAccountHungama;
	
	private ImageView fbLoginSign;
	private ImageView twitterLoginSign;
	private ImageView googlLoginSign;
	private ImageView hungamaLoginSign;
	
	private TextView membershipTextView;
	
	private SeekBar volumeSeekBar;
	private RadioGroup bitrateRadioGroup;
	private RadioGroup appHintsRadioGroup;
	
	private RadioButton bitrateRadioButtonAuto;
	private RadioButton bitrateRadioButtonLow;
	private RadioButton bitrateRadioButtonMedium;
	private RadioButton bitrateRadioButtonHigh;
	
	// Managers
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private AudioManager mAudioManager;
	private GigyaManager mGigyaManager;
	
	// Data members
	private String mSubscriptionPlan;
	private boolean mHasSubscriptionPlan;
	
	private TwitterLoginFragment mTwitterLoginFragment;
	
	private boolean mIsActivityResumed = false;
	
	private boolean isHideLoadingDialog;
	
	
	// ======================================================
	// Life cycle callbacks.
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		mGigyaManager = new GigyaManager(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// Fetch the root view
		View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
		
		mLayoutAccountFacebook =  (LinearLayout) rootView.findViewById(R.id.facebook_view);
		mLayoutAccountTwitter = (LinearLayout) rootView.findViewById(R.id.twitter_view);
		mLayoutAccountGooglePlus = (LinearLayout) rootView.findViewById(R.id.google_plus_view);
		mLayoutAccountHungama = (LinearLayout) rootView.findViewById(R.id.hungama_view);
		mLayoutSettingsMembership = (LinearLayout) rootView.findViewById(R.id.membership_status_view); 
		mLayoutSettingsMyStream = (LinearLayout) rootView.findViewById(R.id.mystream_settings_view);
		
		mButtonAccountFacebook = (Button) rootView.findViewById(R.id.facebook_button);
		mButtonAccountTwitter = (Button) rootView.findViewById(R.id.twitter_button);
		mButtonAccountGooglePlus = (Button) rootView.findViewById(R.id.google_plus_button);
		mButtonAccountHungama = (Button) rootView.findViewById(R.id.hungama_button);
		
		volumeSeekBar = (SeekBar) rootView.findViewById(R.id.volume_seek_bar);
		bitrateRadioGroup = (RadioGroup) rootView.findViewById(R.id.bitrateRadioGroup);
		appHintsRadioGroup = (RadioGroup) rootView.findViewById(R.id.appHintsRadioGroup);
		fbLoginSign = (ImageView) rootView.findViewById(R.id.fb_loging_sign);
		twitterLoginSign = (ImageView) rootView.findViewById(R.id.twitter_login_sign);
		googlLoginSign = (ImageView) rootView.findViewById(R.id.google_login_sign);
		hungamaLoginSign = (ImageView) rootView.findViewById(R.id.hungama_login_sign);
		membershipTextView = (TextView) rootView.findViewById(R.id.membership_textview);
		
		// Volume settings
		setVolume();
		
		// Bit-Rate settings
//		bitrateRadioButtonAuto = (RadioButton) rootView.findViewById(R.id.radio_button_auto);
//		bitrateRadioButtonLow = (RadioButton) rootView.findViewById(R.id.radio_button_low);
//		bitrateRadioButtonMedium = (RadioButton) rootView.findViewById(R.id.radio_button_medium);
//		bitrateRadioButtonHigh = (RadioButton) rootView.findViewById(R.id.radio_button_high);
		
		setBitrate();
		
		// App Hints settings
		setAppHints();
		
		// Membership text settings
		isHideLoadingDialog = false;
		setMembershipText();
		
		// Set listeners
		setViewsListeners();
		
		// Set login check marks
		setSocialLoginStatus();
		
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Start dialog until GetuserInfo returns
		mIsActivityResumed = true;
			
		// Get the social networks info (logged in or not?)
		isHideLoadingDialog = false;
		setMembershipText();
		mGigyaManager.socializeGetUserInfo();
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mIsActivityResumed = false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mIsActivityResumed = true;
	}

	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(getActivity());
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SettingsActivity.LOGIN_ACTIVITY_CODE && resultCode == SettingsActivity.RESULT_OK) {
//			// checks for valid session.
//			String session = mDataManager.getApplicationConfigurations().getSessionID();
//			if (!TextUtils.isEmpty(session)) {
//				mDataManager.getCurrentSubscriptionPlan(this);
//			}
			//mDataManager.getCurrentSubscriptionPlan(this);
		}
		AccountManager accountManager = AccountManager.get(getActivity().getApplicationContext());
		Account[] accounts = accountManager.getAccountsByType("com.google");
		String accountType = null;
		if(accounts != null && accounts.length > 0){
			accountType = accounts[0].name; 
		}
		mDataManager.getCurrentSubscriptionPlan(this, accountType);
		
		isHideLoadingDialog = true;
		setMembershipText();
	};
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.facebook_button:
		case R.id.facebook_view:
			if(mGigyaManager.isFBConnected()){
				addAccountSettingsFragment(SocialNetwork.FACEBOOK);
			}else{
				mGigyaManager.facebookLogin();
			}
			
			break;

		case R.id.twitter_button:
		case R.id.twitter_view:
			if(mGigyaManager.isTwitterConnected()){
				addAccountSettingsFragment(SocialNetwork.TWITTER);
			}else{
				mGigyaManager.twitterLogin();
			}
			
			break;
			
		case R.id.google_plus_button:
		case R.id.google_plus_view:
			if(mGigyaManager.isGoogleConnected()){
				addAccountSettingsFragment(SocialNetwork.GOOGLE);
			}else{
				mGigyaManager.googleLogin();
			}
			
			break;
		
		case R.id.hungama_button:
		case R.id.hungama_view:
			
			boolean isRealUser = mApplicationConfigurations.isRealUser();
			if(isRealUser){
				addAccountSettingsFragment(null);
			}else{
				// Call LoginActivity 
				startLoginActivity();
			}
			
			break;
			
		case R.id.membership_textview:
		case R.id.membership_status_view:
			addMembershipDetailsFragment();
//			if(mHasSubscriptionPlan){
//				addMembershipDetailsFragment();
//				
//			}else{
//				Intent intent = new Intent(getActivity(), UpgradeActivity.class);
//				startActivityForResult(intent, 0);
//			}
			
			break;	
			
		case R.id.mystream_settings_view:
			addMyStreamSettingsFragment();
			
			break;
						
		default:
			break;
		}
	}
	
	
	// ======================================================
	// Operations callbacks.
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		showLoadingDialogFragment();
	}
	
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		
		switch (operationId) {
		case (OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK):
			SubscriptionCheckResponse subscriptionCheckResponse = 
			(SubscriptionCheckResponse)responseObjects.get(
					SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);
			
			if (subscriptionCheckResponse != null && subscriptionCheckResponse.getPlan() != null) {
				mApplicationConfigurations.setIsUserHasSubscriptionPlan(true);
			} else {
				mApplicationConfigurations.setIsUserHasSubscriptionPlan(false);
			}
			isHideLoadingDialog = false;
			setMembershipText();
			
			// Start dialog until GetuserInfo returns
			mIsActivityResumed = true;
			
			mGigyaManager = new GigyaManager(getActivity());
			mGigyaManager.setOnGigyaResponseListener(this);
			
			// Get the social networks info (logged in or not?)
			mGigyaManager.socializeGetUserInfo();
			
			break;

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
			
			String secret = mApplicationConfigurations.getGigyaSessionSecret();
			String token = mApplicationConfigurations.getGigyaSessionToken();
			
			if(!TextUtils.isEmpty(secret) && !TextUtils.isEmpty(token)){
				GigyaManager mGigyaManager = new GigyaManager(getActivity());
				mGigyaManager.setSession(token, secret);
			}
			
			mGigyaManager.socializeGetUserInfo();
			break;		
		}
		
//		hideLoadingDialogFragment();
	}
	
	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		
		switch (operationId) {
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
			if (GigyaManager.provider != SocialNetwork.TWITTER) {
				mGigyaManager.cancelGigyaProviderLogin();
			}
			break;
		}
		
		hideLoadingDialogFragment();
		
		if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
			Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
		}
	}
	
	
	// ======================================================
	// Helper methods.
	// ======================================================
	
	private void setViewsListeners(){
		
		volumeSeekBar.setOnSeekBarChangeListener(this);
		
		bitrateRadioGroup.setOnCheckedChangeListener(this);
		appHintsRadioGroup.setOnCheckedChangeListener(this);
		
		mLayoutAccountFacebook.setOnClickListener(this);
		mLayoutAccountTwitter.setOnClickListener(this);
		mLayoutAccountGooglePlus.setOnClickListener(this);
		mLayoutAccountHungama.setOnClickListener(this);
		mLayoutSettingsMyStream.setOnClickListener(this);
		membershipTextView.setOnClickListener(this);
		mLayoutSettingsMembership.setOnClickListener(this);
		
		mButtonAccountFacebook.setOnClickListener(this);
		mButtonAccountTwitter.setOnClickListener(this);
		mButtonAccountGooglePlus.setOnClickListener(this);
		mButtonAccountHungama.setOnClickListener(this);
		
		mGigyaManager.setOnGigyaResponseListener(this);
	}
	
	private void setVolume(){
		mAudioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
		int maxVol = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int cutVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, cutVol, 0);
		
		volumeSeekBar.setMax(maxVol);
		volumeSeekBar.setProgress(cutVol);
	}
	
	private void setBitrate(){
		int bitrate = mApplicationConfigurations.getBitRateState();
		if(bitrate == ApplicationConfigurations.BITRATE_AUTO){
			bitrateRadioGroup.check(R.id.radio_button_auto);
			
		} else if(bitrate == ApplicationConfigurations.BITRATE_HIGH){
			bitrateRadioGroup.check(R.id.radio_button_high);
			
		} else if(bitrate == ApplicationConfigurations.BITRATE_MEDIUM) {
			bitrateRadioGroup.check(R.id.radio_button_medium);
			
		} else {
			bitrateRadioGroup.check(R.id.radio_button_low);
		}
	}
	
	private void setAppHints(){
		
		boolean appHints = mApplicationConfigurations.getHintsState();
				
		if(appHints){
			appHintsRadioGroup.check(R.id.radio_button_on);
			
		}else{
			appHintsRadioGroup.check(R.id.radio_button_off);
		}
		
	}
	
	private void setMembershipText(){
		
		mHasSubscriptionPlan = mApplicationConfigurations.isUserHasSubscriptionPlan();
		
		if (getActivity() != null) {
			if(mHasSubscriptionPlan){
				
				mSubscriptionPlan = getActivity().getString(R.string.premium_membership);
				
			}else{
				
				mSubscriptionPlan = getActivity().getString(R.string.free_upgrade_to_remove_ads);
			}
			
			membershipTextView.setText(mSubscriptionPlan);
		}
		
		if(isHideLoadingDialog) {
			hideLoadingDialogFragment();
		}
		//hideLoadingDialog();
	}
	
	private void setSocialLoginStatus(){
		
		// Hungama login
		boolean isRealUser = mApplicationConfigurations.isRealUser();
		if(isRealUser){
			hungamaLoginSign.setVisibility(View.VISIBLE);
			
			// FaceBook login
			if(mGigyaManager.isFBConnected()){
				fbLoginSign.setVisibility(View.VISIBLE);
			}else{
				fbLoginSign.setVisibility(View.INVISIBLE);
			}
			
			// Twitter login
			if(mGigyaManager.isTwitterConnected()){
				twitterLoginSign.setVisibility(View.VISIBLE);
			}else{
				twitterLoginSign.setVisibility(View.INVISIBLE);
			}
			
			// Google Plus login
			if(mGigyaManager.isGoogleConnected()){
				googlLoginSign.setVisibility(View.VISIBLE);
			}else{
				googlLoginSign.setVisibility(View.INVISIBLE);
			}
			
		}else{
			hungamaLoginSign.setVisibility(View.INVISIBLE);
			fbLoginSign.setVisibility(View.INVISIBLE);
			mGigyaManager.setIsFBConnected(false);
			twitterLoginSign.setVisibility(View.INVISIBLE);
			mGigyaManager.setIsTwitterConnected(false);
			googlLoginSign.setVisibility(View.INVISIBLE);
			mGigyaManager.setIsGoogleConnected(false);
		}
		
		
	}
	
	private void startLoginActivity() {
		Intent startLoginActivityIntent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
		startLoginActivityIntent.putExtra(SettingsActivity.ARGUMENT_SETTINGS_ACTIVITY, "settings_activity");
 		startActivityForResult(startLoginActivityIntent,SettingsActivity.LOGIN_ACTIVITY_CODE);
	}
	
	public void addMembershipDetailsFragment(){
		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		MembershipDetailsFragment membershipDetailsFragment = new MembershipDetailsFragment();
		fragmentTransaction.replace(R.id.main_fragmant_container, membershipDetailsFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	private void addAccountSettingsFragment(SocialNetwork provider){
		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		// sets the given account as the argument.
		AccountSettingsFragment accountSettingsFragment = new AccountSettingsFragment(getActivity());
		Bundle b = new Bundle();
		b.putSerializable(AccountSettingsFragment.PROVIDER, provider);
		accountSettingsFragment.setArguments(b);
		
		// bang!
		fragmentTransaction.replace(R.id.main_fragmant_container, accountSettingsFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	private void addMyStreamSettingsFragment(){
		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		MyStreamSettingsFragment myStreamSettingsFragment = new MyStreamSettingsFragment();
		fragmentTransaction.replace(R.id.main_fragmant_container, myStreamSettingsFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	private void showLoadingDialogFragment() {
		FragmentManager fragmentManager = getFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
		if (fragment == null && mIsActivityResumed) {
			LoadingDialogFragment dialogFragment = LoadingDialogFragment.newInstance(R.string.application_dialog_loading);
			dialogFragment.setCancelable(true);
			dialogFragment.show(fragmentManager, LoadingDialogFragment.FRAGMENT_TAG);
		}
	}
	
	private void hideLoadingDialogFragment() {
		FragmentManager fragmentManager = getFragmentManager();
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

	
	// ======================================================
	// Volume SeekBar callbacks. 
	// ======================================================

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}
	
	
	// ======================================================
	// Bitrate radio group callbacks. 
	// ======================================================
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {

		switch (group.getId()) {
		case R.id.bitrateRadioGroup:
			
			if(checkedId == R.id.radio_button_auto){
				mApplicationConfigurations.setBitRateState(ApplicationConfigurations.BITRATE_AUTO);
//				bitrateRadioButtonLow.setChecked(false);
//				bitrateRadioButtonMedium.setChecked(false);
//				bitrateRadioButtonHigh.setChecked(false);
			
			} else if(checkedId == R.id.radio_button_high){
				mApplicationConfigurations.setBitRateState(ApplicationConfigurations.BITRATE_HIGH);
//				bitrateRadioButtonAuto.setChecked(false);
//				bitrateRadioButtonMedium.setChecked(false);
//				bitrateRadioButtonLow.setChecked(false);
			
			} else if(checkedId == R.id.radio_button_medium){
				mApplicationConfigurations.setBitRateState(ApplicationConfigurations.BITRATE_MEDIUM);
//				bitrateRadioButtonAuto.setChecked(false);
//				bitrateRadioButtonHigh.setChecked(false);
//				bitrateRadioButtonLow.setChecked(false);
				
			} else {
				mApplicationConfigurations.setBitRateState(ApplicationConfigurations.BITRATE_LOW);
//				bitrateRadioButtonAuto.setChecked(false);
//				bitrateRadioButtonHigh.setChecked(false);
//				bitrateRadioButtonMedium.setChecked(false);
			}
			
			break;

		case R.id.appHintsRadioGroup: 
			if(checkedId == R.id.radio_button_on){
				mApplicationConfigurations.setHintsState(true);
			}else if(checkedId == R.id.radio_button_off){
				mApplicationConfigurations.setHintsState(false);
			}
			
			break;
		default:
			break;
		}
	
	}

	
	// ======================================================
	// Gigya callbacks.
	// ======================================================

	@Override
	public void onGigyaLoginListener(SocialNetwork provider,
			Map<String, Object> signupFields, long setId) {
		
		if(provider == SocialNetwork.TWITTER){
			// Twitter
			FragmentManager mFragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit,
	                R.anim.slide_right_enter,
	                R.anim.slide_right_exit);
			
			TwitterLoginFragment fragment = new TwitterLoginFragment(signupFields, setId);
			fragmentTransaction.replace(R.id.main_fragmant_container, fragment,TwitterLoginFragment.FRAGMENT_TWITTER_LOGIN);
//			fragment.getView().setOnKeyListener( new OnKeyListener()
//			{
//			    @Override
//			    public boolean onKey( View v, int keyCode, KeyEvent event )
//			    {
//			        if( keyCode == KeyEvent.KEYCODE_BACK )
//			        {
//			        	showLoadingDialogFragment();
//						
//						mGigyaManager.logout();
//			            return true;
//			        }
//			        return false;
//			    }
//			} );
			fragmentTransaction.addToBackStack(TwitterLoginFragment.class.toString());
			fragmentTransaction.commit();
			
			// Listen to result from TwitterLoinFragment
			fragment.setOnTwitterLoginListener(this);
			
		}else{
			// FaceBook, Google
			
			// Call PCP 
			mDataManager.createPartnerConsumerProxy(signupFields, setId, this, false);
		}
	}
	
	@Override
	public void onSocializeGetUserInfoListener() {
		
		setSocialLoginStatus();
		hideLoadingDialogFragment();
	}

	@Override
	public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList) {}

	@Override
	public void onSocializeGetContactsListener(List<GoogleFriend> googleFriendsList) {}
	
	@Override
	public void onGigyaLogoutListener() {}
	
	@Override
	public void onTwitterLoginListener(TwitterLoginFragment fragment, Map<String, Object> signupFields, long setId) {
		// Call PCP 
		// It's include the email and password that user insert in TwitterLoginFragment
		mDataManager.createPartnerConsumerProxy(signupFields, setId, this, false);
		mTwitterLoginFragment = fragment;
		//fragment.getFragmentManager().popBackStack();
	}

	@Override
	public void onCancelLoginListener() {
		mGigyaManager.removeConnetion(SocialNetwork.TWITTER);		
	}

	@Override
	public void onFacebookInvite() {}

	@Override
	public void onTwitterInvite() {}


	
	
}
