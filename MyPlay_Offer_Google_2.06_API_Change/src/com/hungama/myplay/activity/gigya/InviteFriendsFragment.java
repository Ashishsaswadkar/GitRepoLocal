package com.hungama.myplay.activity.gigya;

import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment.OnTwitterLoginListener;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerConsumerProxyCreateOperation;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * @author DavidSvilem
 *
 */
public class InviteFriendsFragment extends Fragment implements OnClickListener, 
															   CommunicationOperationListener, 
															   OnGigyaResponseListener,
															   OnTwitterLoginListener{

	private static final String TAG = "InviteFriendsFragment";
	private static final String VALUE = "value";
	
	// Views
	private TextView mTextTitle;
	private Button mInviteFacebookFriends;
	private Button mInviteTwitterFriends;
	private Button mInviteGooleFriends;
	private Button mInviteEmailFriends;
	
	// Managers
	private GigyaManager mGigyaManager;
	private DataManager mDataManager;
	
	private ApplicationConfigurations mApplicationConfigurations;
	
	private SocialNetwork provider;
	
	private TwitterLoginFragment mTwitterLoginFragment;
	private ProgressDialog mProgressDialog;
	private String processing;
	
	private SocialNetwork mProvider; 
	private List<FBFriend> mFbFriends; 
	private List<GoogleFriend> mGoogleFriends;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGigyaManager = new GigyaManager(getActivity());
		mGigyaManager.setOnGigyaResponseListener(this);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		
		processing = getActivity().getResources().getString(R.string.processing);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// Fetch the root view
		View rootView = inflater.inflate(R.layout.fragment_invite_friends, container, false);
		
		// Views initialize
		mTextTitle = (TextView) rootView.findViewById(R.id.main_title_bar_text);
		
		mInviteFacebookFriends = (Button) rootView.findViewById(R.id.invite_facebook_friends);
		mInviteTwitterFriends = (Button) rootView.findViewById(R.id.invite_twitter_friends);
		mInviteGooleFriends = (Button) rootView.findViewById(R.id.invite_google_friends);
		mInviteEmailFriends = (Button) rootView.findViewById(R.id.invite_email_friends);
		
		// Set click listener 
		mInviteFacebookFriends.setOnClickListener(this);
		mInviteTwitterFriends.setOnClickListener(this);
		mInviteGooleFriends.setOnClickListener(this);
		mInviteEmailFriends.setOnClickListener(this);
		
		// Set title
		String title = getString(R.string.invite_friends_title);
		mTextTitle.setText(title);
		
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.invite_facebook_friends:
			
//			showInviteFBUnderConstructionDialog();
			provider = SocialNetwork.FACEBOOK;
			mGigyaManager.getSocialNetworkFriends(SocialNetwork.FACEBOOK);
			
			break;

		case R.id.invite_twitter_friends:
			provider = SocialNetwork.TWITTER;
			mGigyaManager.getSocialNetworkFriends(SocialNetwork.TWITTER);
			
			break;
			
		case R.id.invite_google_friends:
			provider = SocialNetwork.GOOGLE;
			mGigyaManager.getSocialNetworkFriends(SocialNetwork.GOOGLE);
			
			break;
			
		case R.id.invite_email_friends:
			// Open Native Email application
			// Send mail to all checked friends
			Utils.invokeEmailApp(this, 
							     null, 
							     getActivity().getString(R.string.invite_friend_mail_subject), 
							     getActivity().getString(
							    		 R.string.invite_friend_mail_text,
							    		 mApplicationConfigurations.getGigyaGoogleFirstName(),
							    		 mApplicationConfigurations.getGigyaGoogleLastName()));
			break;
			
		default:
			break;
		}
		
	}

	private void addFriendsListFragment(SocialNetwork provider, List<FBFriend> fbFriends, List<GoogleFriend> googleFriends) {
		
		mProvider = provider;
		mFbFriends = fbFriends;
		mGoogleFriends = googleFriends;
		
		// For avoiding perform an action after onSaveInstanceState.
		new Handler().post(new Runnable() {
			
            public void run() {
            	if (getActivity() != null) {            		
	            	FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
	        		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
	        		FriendsListFragment friendsListFragment = new FriendsListFragment(mProvider, mFbFriends, mGoogleFriends);
	        		fragmentTransaction.replace(R.id.main_fragmant_container, friendsListFragment);
	        		fragmentTransaction.addToBackStack(null);
	        		fragmentTransaction.commitAllowingStateLoss();
	//        		fragmentTransaction.commit();
            	}
            }
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == 100){
//			mDataManager.checkBadgesAlert("", "", "invite_friends", this);
			//Disabled since we cannot know if an email was sent or not in android. - bug 3002
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onStart(int operationId) {
		// Show Dialog
		showLoadingDialog(processing);
		
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
			
			hideLoadingDialog();
			
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
			
			hideLoadingDialog();
			
		break;
		
		case (OperationDefinition.Hungama.OperationId.SOCIAL_BADGE_ALERT): 
			
			FragmentManager fm = getFragmentManager();
			if(fm != null){
				fm.popBackStack();					
			}
		
			hideLoadingDialog();
			
			break;
	}
		
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		
		switch (operationId) {

		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
			getFragmentManager().popBackStack();
			mGigyaManager.cancelGigyaProviderLogin();
			hideLoadingDialog();
			
			break;
			
		case (OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE):
			getFragmentManager().popBackStack();
			hideLoadingDialog();
			
			break;
			
		default:
			break;
		}
		
	}

	@Override
	public void onGigyaLoginListener(SocialNetwork provider, Map<String, Object> signupFields, long setId) {
		
		if(provider == SocialNetwork.TWITTER){
			// Twitter
			
			FragmentManager mFragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit,
	                R.anim.slide_right_enter,
	                R.anim.slide_right_exit);
			
			TwitterLoginFragment fragment = new TwitterLoginFragment(signupFields, setId);
			fragmentTransaction.replace(R.id.main_fragmant_container, fragment);
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
	public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList) {
		addFriendsListFragment(provider, fbFriendsList, null);
	}

	@Override
	public void onSocializeGetContactsListener(List<GoogleFriend> googleFriendsList) {
		addFriendsListFragment(provider, null, googleFriendsList);
	}

	@Override
	public void onSocializeGetUserInfoListener() {}

	@Override
	public void onGigyaLogoutListener() {}

	@Override
	public void onFacebookInvite() {
		Logger.i(TAG, "onFacebookInvite");
	}

	@Override
	public void onTwitterInvite() {}

	@Override
	public void onTwitterLoginListener(TwitterLoginFragment fragment, 
							Map<String, Object> signupFields, long setId) {
		
		// Call PCP 
		// It's include the email and password that user insert in TwitterLoginFragment
		mDataManager.createPartnerConsumerProxy(signupFields, setId, this, false);
		mTwitterLoginFragment = fragment;
		
	}

	@Override
	public void onCancelLoginListener() {
		mGigyaManager.removeConnetion(SocialNetwork.TWITTER);	
	}

	public void showLoadingDialog(String message) {
		if (!getActivity().isFinishing()) {
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(getActivity());
				mProgressDialog = ProgressDialog.show(getActivity(), "", message, true, true);
			}
		}
	}

	public void hideLoadingDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
	private void showInviteFBUnderConstructionDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
 
			// set title
			alertDialogBuilder.setTitle(getResources().getString(R.string.invite_fb_friends_under_construction_title));
 
			// set dialog message
			alertDialogBuilder
				.setMessage(getResources().getString(R.string.invite_fb_friends_under_construction_text))
				.setCancelable(true)				
				.setNegativeButton(R.string.exit_dialog_text_ok ,new DialogInterface.OnClickListener() {
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
}