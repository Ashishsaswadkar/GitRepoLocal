package com.hungama.myplay.activity.gigya;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.SignOption;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupField;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerConsumerProxyCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerInfoReadOperation;
import com.hungama.myplay.activity.services.InventoryLightService;
import com.hungama.myplay.activity.ui.LoginActivity;
import com.hungama.myplay.activity.ui.fragments.CommentsFragment;
import com.hungama.myplay.activity.ui.fragments.LoadingDialogFragment;
import com.hungama.myplay.activity.ui.fragments.MainFragment;
import com.hungama.myplay.activity.util.FileUtils;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * @author David Svilem
 *
 */
public class TwitterLoginFragment extends DialogFragment implements OnClickListener, OnGigyaResponseListener, CommunicationOperationListener{

	public static final String TAG = "TwitterLoginFragment";
	
	public static final String FRAGMENT_TWITTER_LOGIN = "fragment_twitter_login";
	
	private TextView mTextTitle;
	private EditText mTextEmail;
	private EditText mTextPassword;
	private Button mButtonContinue;
	
	private OnTwitterLoginListener mListener;
	
	private String email;
	private String password;
	
	private SignOption gigyaSignOption;
	private Map<String, Object> fieldMap;
	private Map<String, Object> signupFields;
	private long setId;

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private GigyaManager mGigyaManager;
	
	private boolean mIsActivityResumed = false;
	
	private Resources resources;
	
	public TwitterLoginFragment(Map<String, Object> signupFields, long setId){
		this.signupFields = signupFields;
		this.setId = setId;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGigyaManager = new GigyaManager(getActivity());
		mGigyaManager.setOnGigyaResponseListener(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_login_social_network_login, container, false);
		rootView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		initializeUserControls(rootView);
		rootView.setFocusableInTouchMode(true); //this line is important
		rootView.requestFocus();
//		rootView.setOnKeyListener( new OnKeyListener()
//		{
//		    @Override
//		    public boolean onKey( View v, int keyCode, KeyEvent event )
//		    {
//		        if( keyCode == KeyEvent.KEYCODE_BACK )
//		        {
//		        	showLoadingDialogFragment();
//					
//					mGigyaManager.logout();
//		            return true;
//		        }
//		        return false;
//		    }
//		} );
		
		resources = rootView.getResources();
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mIsActivityResumed = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		
		mIsActivityResumed = false;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		populateViews();
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
	}
	 
	@Override
	public void onStop() {
		super.onStop();
		
		FlurryAgent.onEndSession(getActivity());
	}
	
	private void initializeUserControls(View rootView) {
		mTextTitle = (TextView) rootView.findViewById(R.id.main_title_bar_text);
		mTextEmail = (EditText) rootView.findViewById(R.id.login_social_network_login_text_email);
		mTextPassword = (EditText) rootView.findViewById(R.id.login_social_network_login_text_password);
		mButtonContinue = (Button) rootView.findViewById(R.id.login_social_network_login_button_submit);
	}
	
	private void populateViews() {
//		Resources resources = getView().getResources();
		// sets the title.
		String title = 
				resources.getString(R.string.login_social_network_login_title_prefix) 
				+ " " +
				SocialNetwork.TWITTER.toString();
		
		mTextTitle.setText(title);
		
		// handles when the user has clicked on the "submit".
		mButtonContinue.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// closes the keyboard.
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mTextPassword.getWindowToken(), 0);
		
		// extracts the credentials from the text fields.
		email = mTextEmail.getText() != null ? 
							mTextEmail.getText().toString() : null;
							
		password = mTextPassword.getText() != null ? 
							mTextPassword.getText().toString() : null;
			
		if(TextUtils.isEmpty(email)){
			Toast.makeText(getActivity().getApplicationContext(), 
						   "Email " + resources.getString(R.string.login_signup_error_mandatory), 
						   Toast.LENGTH_LONG).show();
		}else{
			
			if(!Utils.validateEmailAddress(email)){
		    	Toast.makeText(getActivity().getApplicationContext(), 
		    			resources.getString(R.string.login_signup_error_email), 
		    			Toast.LENGTH_LONG).show();
			}else{
				
				// The email is not empty and valid, (password is optional)
				if (mListener != null) {
					
					gigyaSignOption = mApplicationConfigurations.getGigyaSignup();
					
					fieldMap = new HashMap<String, Object>();
					SignupField gigyaEmail = gigyaSignOption.getSignupFields().get(7);
					fieldMap.put("value", email);
					signupFields.put(gigyaEmail.getName(), fieldMap);
					
					fieldMap = new HashMap<String, Object>();
					SignupField gigyaPassword = gigyaSignOption.getSignupFields().get(8); // optional
					fieldMap.put("value", password);
					signupFields.put(gigyaPassword.getName(), fieldMap);
					if (!(mListener instanceof CommentsFragment)) {
						showLoadingDialogFragment();
					} else {
						((CommentsFragment) mListener).showLoadingDialog(R.string.application_dialog_loading);
					}
					mListener.onTwitterLoginListener(this, signupFields, setId);
					
					mApplicationConfigurations.setGigyaTwitterEmail(email);
				}
			}
		}
	}
	
	public void finish(){
//		hideLoadingDialogFragment();
		// Pop the current fragment 
		FragmentManager fm = getFragmentManager();
		if(fm != null){
//			if (mListener instanceof CommentsFragment) {
//				((CommentsFragment) mListener).toggleFragmentTitle();
//			}
			fm.popBackStack();			
		}else{
			if (getActivity() != null) {
//				if (mListener instanceof CommentsFragment) {
//					((CommentsFragment) mListener).toggleFragmentTitle();
//				}
				getActivity().getSupportFragmentManager().popBackStack();
			}			
		}
	}
	
	// Interface
	public interface OnTwitterLoginListener{
		public void onTwitterLoginListener(TwitterLoginFragment fragment, Map<String, Object> signupFields, long setId);
		public void onCancelLoginListener();
	}
	
	public void setOnTwitterLoginListener(OnTwitterLoginListener l){
		this.mListener = l;
	}
	
	@Override
	public void onDestroy() {
		
		if(mListener != null){
			if(TextUtils.isEmpty(email)){
				// If we ended up here so the user clicked the back button
				mListener.onCancelLoginListener();
			}
		}
		if (mListener instanceof CommentsFragment) {
			((CommentsFragment) mListener).toggleFragmentTitle();
		}
		super.onDestroy();
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
		if(getActivity() instanceof LoginActivity) {
//			if (mListener instanceof CommentsFragment) {
//				((CommentsFragment) mListener).toggleFragmentTitle();
//			}
			hideLoadingDialogFragment();
			
			getFragmentManager().popBackStack();
		} else {
			performSielntLogin();
		}		
		
	}

	@Override
	public void onFacebookInvite() {}

	@Override
	public void onTwitterInvite() {}
	
	

	// ======================================================
	// Private Helper Methods
	// ======================================================
	
	private void performSielntLogin() {
		mDataManager.readPartnerInfo(this);
	}
	
	private void resetUserDetails() {
		//Delete media folder
		FileUtils fileUtils = new FileUtils(getActivity());
		String hungamaFolder = resources.getString(R.string.download_media_folder);
		File directory = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/" + hungamaFolder);

		if (directory.exists()) {
        	fileUtils.deleteDirectoryRecursively(directory);
        }
        
        mApplicationConfigurations.setIsRealUser(false);
        mApplicationConfigurations.setIsUserHasSubscriptionPlan(false);
        mApplicationConfigurations.setGigyaSessionSecret("");
        mApplicationConfigurations.setGigyaSessionToken("");
        
        // When logging out we need to restore the skipped partner user id
        String skippedPartnerUserId = mApplicationConfigurations.getSkippedPartnerUserId();
        if(!TextUtils.isEmpty(skippedPartnerUserId)){
        	mApplicationConfigurations.setPartnerUserId(skippedPartnerUserId);
        }
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
		Fragment fragment = fragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
		if (fragment != null) {
			DialogFragment fragmentDialog = (DialogFragment) fragment;
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.remove(fragmentDialog);
			fragmentDialog.dismissAllowingStateLoss();
		}
	}
	
	public void onBackPressed() {		
		
		showLoadingDialogFragment();			
		
		if (getActivity() instanceof LoginActivity) {
			mGigyaManager.logout();
		} else {
			if(mApplicationConfigurations.isRealUser()) {
				mGigyaManager.removeConnetion(SocialNetwork.TWITTER);			
			} else {
				mGigyaManager.logout();
			}
		}
			
		mGigyaManager.setIsTwitterConnected(false);
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
			
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ):
			Logger.i(TAG, "Successed getting partner info.");
			
			List<SignOption> signOptions = (List<SignOption>) responseObjects.get(PartnerInfoReadOperation.RESPONSE_KEY_OBJECT_SIGN_OPTIONS);

			// Set the Gigya setID 
			SignOption gigyaSignup = signOptions.get(2);
			mApplicationConfigurations.setGigyaSignup(gigyaSignup);
			
			/*
			 * performs sielnt login.
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
			
//			hideLoadingDialogFragment();
			
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
			
//			hideLoadingDialogFragment();
			
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
			
			String secret = mApplicationConfigurations.getGigyaSessionSecret();
			String token = mApplicationConfigurations.getGigyaSessionToken();
			
			if(!TextUtils.isEmpty(secret) && !TextUtils.isEmpty(token)){
				GigyaManager mGigyaManager = new GigyaManager(getActivity());
				mGigyaManager.setSession(token, secret);
			}
			
			if (getActivity() != null) {
				// syncs the inventory.
				Intent inventoryLightService = new Intent(getActivity().getApplicationContext(), InventoryLightService.class);
				getActivity().startService(inventoryLightService);
			}
			
			hideLoadingDialogFragment();
			
			getFragmentManager().popBackStack();
			
			if(getActivity() instanceof TwitterLoginActivity) {
				getActivity().finish();
			}
			
			break;
		}

	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		hideLoadingDialogFragment();
		
		if (!TextUtils.isEmpty(errorMessage)) {
			Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
		}
	}

	
}
