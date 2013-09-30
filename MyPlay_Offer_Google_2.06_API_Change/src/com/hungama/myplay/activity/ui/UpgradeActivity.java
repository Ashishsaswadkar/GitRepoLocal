package com.hungama.myplay.activity.ui;

import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MobileOperationType;
import com.hungama.myplay.activity.data.dao.hungama.MobileVerificationCountryCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.MobileVerificationResponse;
import com.hungama.myplay.activity.data.dao.hungama.Plan;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionType;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MobileVerifyCountryCheckOperation;
import com.hungama.myplay.activity.operations.hungama.MobileVerifyOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionOperation;
import com.hungama.myplay.activity.ui.fragments.DownloadFragment;
import com.hungama.myplay.activity.ui.fragments.UpgradeFragment;
import com.hungama.myplay.activity.util.Logger;

/**
 * Controller for presenting details of the given MediaItem.
 */
public class UpgradeActivity extends MainActivity implements OnClickListener, CommunicationOperationListener{
	
	private static final String TAG = "UpgradeActivity";
	
	protected static final String FRAGMENT_TAG_UPGRADE = "fragment_tag_upgrade";
	private static final int LOGIN_ACTIVITY_CODE = 1;
	private static final String NA_DATE = "NA";
		
	public static final String PASSWORD_SMS_SENT = "1";
	private static final String MISSING_PARAMETERS = "2";
	public static final String MSISDN_ALREADY_EXIST_AND_VERIFIED = "3";
	public static final String VERIFICATION_CODE_DELIVERED  = "4";
	private static final String AUTHENTICATION_KEY_BLANK = "5";
	private static final String AUTHENTICATION_KEY_INVALID = "6";
	private static final String INVALID_MSISDN = "7";

	public static final int MOBILE_NOT_VERIFIED = 0;
	public static final int MOBILE_VERIFIED = 1;
	public static final int MOBILE_NOT_EXIST = -1;
	
	public static final String ARGUMENT_UPGRADE_ACTIVITY = "argument_upgrade_activity";
	public static final String EXTRA_DATA_ORIGIN_MEDIA_CONTENT_TYPE = "extra_data_origin_media_content_type";
	
	private FragmentManager mFragmentManager;
	private FragmentTransaction fragmentTransaction;
	private DataManager mDataManager;
	
	private TextView mTitleBarText;	
	
	private ApplicationConfigurations mApplicationConfigurations;
	private DeviceConfigurations mDeviceConfigurations;

	private boolean isLoginMobileChecked = false;
	
	public static String mobileToVerify;
	
	private MediaContentType mOriginContentType = MediaContentType.MUSIC;
	
	// ======================================================
	// Activity life-cycle callbacks. 
	// ======================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upgrade);
		
		mDataManager = DataManager.getInstance(this.getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		mDeviceConfigurations = mDataManager.getDeviceConfigurations();
		
		
		Bundle args = getIntent().getExtras();
		if (args != null && args.containsKey(EXTRA_DATA_ORIGIN_MEDIA_CONTENT_TYPE)) {
			mOriginContentType = (MediaContentType) args.getSerializable(EXTRA_DATA_ORIGIN_MEDIA_CONTENT_TYPE);
		}
		
		// SetS title bar
		mTitleBarText = (TextView) findViewById(R.id.main_title_bar_text);
		mTitleBarText.setText(getResources().getString(R.string.video_player_upgrade_button_text));

		// Hide the Arrow to the right
		ImageButton arrow = (ImageButton) findViewById(R.id.main_title_bar_button_options);
		arrow.setVisibility(View.GONE);
		
		// check which page to open

		/*  checks if the user already logged in or not by checking
		 *  if the session is empty or not.
		 */
		ApplicationConfigurations applicationConfigurations = mDataManager.getApplicationConfigurations();
		String sesion = applicationConfigurations.getSessionID();
		boolean isRealUser = applicationConfigurations.isRealUser();
		
		if (!TextUtils.isEmpty(sesion) && isRealUser) {
			// the user has session, start the main activity of the application.
			
			openPlansPage();
			
		} else {
			// pass the user to login / sign up for the application.
//			Toast toast = 
			Toast.makeText(this, getResources().getString(R.string.upgrade_need_to_login), UpgradeFragment.TOAST_SHOW_DELAY).show();
//			toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
//			toast.show();
			startLoginActivity();
			
//				if (applicationConfigurations.isRealUser()) {
//					mDataManager.getCurrentSubscriptionPlan(0, SubscriptionType.CHECK_SUBSCRIPTION, this);
//				} else {
//					Toast.makeText(this, "You need to login first in order to upgrade", UpgradeFragment.TOAST_SHOW_DELAY).show();
//					onBackPressed();
//				}
		}
		
		mFragmentManager = getSupportFragmentManager();	
		fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
			
	}
		


	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (UpgradeFragment.isWebViewOpen) {
			UpgradeFragment upgradeFragment = (UpgradeFragment) getSupportFragmentManager().findFragmentByTag(UpgradeFragment.TAG);
			if (upgradeFragment != null) {
				upgradeFragment.hideWebView();
			}
		} else {
			mFragmentManager.popBackStack();
		}		
	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		if (mOriginContentType != null) {
			if (mOriginContentType == MediaContentType.VIDEO) {
				return NavigationItem.VIDEOS;
			}
		}
		return NavigationItem.MUSIC;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == LOGIN_ACTIVITY_CODE && resultCode == RESULT_OK) {
			// checks for valid session.
			String session = mDataManager.getApplicationConfigurations().getSessionID();
			Boolean isRealUser = mDataManager.getApplicationConfigurations().isRealUser();
			if (!TextUtils.isEmpty(session) && isRealUser) {
				AccountManager accountManager = AccountManager.get(getApplicationContext());
				Account[] accounts = accountManager.getAccountsByType("com.google");
				
				String accountType = null;
				if(accounts != null && accounts.length > 0){
					accountType = accounts[0].name; 
				}
				mDataManager.getCurrentSubscriptionPlan(this, accountType);
				
//				openPlansPage();
			} else {
				Toast toast = Toast.makeText(this, "You need to login first in order to upgrade", UpgradeFragment.TOAST_SHOW_DELAY);
				toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();
//				startLoginActivity();
			}
		} else if (requestCode == UpgradeFragment.PURCHASE_REQUEST_CODE && resultCode == RESULT_OK) {
			UpgradeFragment upgradeFragment = (UpgradeFragment) getSupportFragmentManager().findFragmentByTag(UpgradeFragment.TAG);
			upgradeFragment.getActivityResult(requestCode, resultCode, data);
		} else {
			finish();
		}
	}
	
	// ======================================================
	// onClick Method 
	// ======================================================
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
	
	// ======================================================
	// Helper Methods 
	// ======================================================
	
	private void startLoginActivity() {
		Intent startLoginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
		startLoginActivityIntent.putExtra(ARGUMENT_UPGRADE_ACTIVITY, "upgrade_activity");
 		startActivityForResult(startLoginActivityIntent, LOGIN_ACTIVITY_CODE);
	}
	
	public void addFragment(Bundle detailsData) {
		
		mFragmentManager = getSupportFragmentManager();	
		fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		UpgradeFragment upgradeFragment = new UpgradeFragment();
		upgradeFragment.setArguments(detailsData);		
		fragmentTransaction.add(R.id.main_fragmant_container, upgradeFragment, UpgradeFragment.TAG);
		fragmentTransaction.commit();
	}

	public void replaceFragment(Bundle data) {
		// replace the current fragment with the forgot password submission fragment.
		UpgradeFragment upgradeFragment = new UpgradeFragment();
		upgradeFragment.setArguments(data);	
//		upgradeFragment.setOnForgotPasswordSubmitListener(this);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		try {
			fragmentTransaction.replace(R.id.main_fragmant_container, upgradeFragment, UpgradeFragment.TAG);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}  catch (Exception e) {
			Logger.e(TAG, e.getMessage());
		}
		
	}

	public void openPlansPage () {
		Bundle data = new Bundle();	
		String upgradePlans = "fragment_upgrade_plans";
		data.putString(UpgradeFragment.ARGUMENT_PLANS, upgradePlans);
		addFragment(data);
	}
	
	public void startMobileVerificationFlow () {
		String msisdn =  mDeviceConfigurations.getDevicePhoneNumber();
		if (msisdn != null && !msisdn.equalsIgnoreCase("")) {
			mobileToVerify = msisdn;
			mDataManager.getMobileVerification(msisdn, null, MobileOperationType.MOBILE_VERIFY, this);
		} else {
			checkUserLoginNumber();
		}
	}
	
	/**
	 * Method to invoke mobile_verify, if a mobile number was entered by user when login
	 */
	public void checkUserLoginNumber() {
		if (!mApplicationConfigurations.getUserLoginPhoneNumber().equalsIgnoreCase("")) {
			isLoginMobileChecked = false;
			mobileToVerify = mApplicationConfigurations.getUserLoginPhoneNumber();
			mDataManager.checkCountry(mApplicationConfigurations.getUserLoginPhoneNumber(), this);
//			mDataManager.getMobileVerification(mApplicationConfigurations.getUserLoginPhoneNumber(), null, MobileOperationType.MOBILE_VERIFY, this);	OLD		
		} else {
			Bundle dataMobileRequired = new Bundle();
			String addMobileNumber = "fragment_upgrade_mobile_required";
			dataMobileRequired.putString(UpgradeFragment.ARGUMENT_MOBILE_REQUIRED, addMobileNumber);
			addFragment(dataMobileRequired);
		}
	}
	
	// ======================================================
	// Operation methods
	// ======================================================
	@Override
	public void onStart(int operationId) {
		switch(operationId) {
			case OperationDefinition.Hungama.OperationId.SUBSCRIPTION : {
				showLoadingDialog(R.string.application_dialog_loading_content);
				break;	
			}
			case OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION : {
				showLoadingDialog(R.string.application_dialog_loading_content);
				break;
			}
			case OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION_COUNTRY_CHECK : {
				showLoadingDialog(R.string.application_dialog_loading_content);
				break;
			}
		}
		
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		switch(operationId) {
		case OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION_COUNTRY_CHECK : {
			MobileVerificationCountryCheckResponse countryCheckResponse = 
					(MobileVerificationCountryCheckResponse) responseObjects.get(MobileVerifyCountryCheckOperation.RESPONSE_KEY_MOBILE_VERIFICATION_COUNTRY_CHECK);
			if (countryCheckResponse.isIndia()) {
				if (mApplicationConfigurations.isMobileNumberVerified(mobileToVerify) == MOBILE_VERIFIED) {						
					Bundle data = new Bundle();
					String upgradePlans = "fragment_upgrade_plans";
					data.putString(UpgradeFragment.ARGUMENT_PLANS, upgradePlans);
					addFragment(data);		
				} else {
					Bundle data = new Bundle();
					String verifyMobileNumber = "fragment_upgrade_mobile_verification";			
					data.putString(UpgradeFragment.ARGUMENT_MOBILE_VERIFICATION, verifyMobileNumber);
					addFragment(data);
				}
			} else {				
				// TODO: go to URL
//				Toast toast = Toast.makeText(this, getResources().getString(R.string.download_country_check_not_india), Toast.LENGTH_LONG);
//				toast.setGravity(Gravity.CENTER, 0, 0);
//				ToastExpander.showFor(toast, TOAST_SHOW_DELAY);
			}
			hideLoadingDialog();
			break;
		}
		case OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK : {
			SubscriptionCheckResponse subscriptionCheckResponse = (SubscriptionCheckResponse) responseObjects.get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);
			if (subscriptionCheckResponse != null) {
				if (subscriptionCheckResponse.getCode().equalsIgnoreCase(UpgradeActivity.PASSWORD_SMS_SENT)) {
					mApplicationConfigurations.setIsUserHasSubscriptionPlan(true);
					mApplicationConfigurations.setUserSubscriptionPlanDate(subscriptionCheckResponse.getPlan().getValidityDate());
					Plan subscriptionPlan = subscriptionCheckResponse.getPlan();
					subscriptionPlan.setValidityDate(subscriptionCheckResponse.getPlan().getValidityDate());
					subscriptionPlan.setPurchaseDate(subscriptionCheckResponse.getPlan().getPurchaseDate());
					subscriptionCheckResponse.setPlan(subscriptionPlan);
					mDataManager.storeCurrentSubscriptionPlan(subscriptionCheckResponse);
//					String title = getResources().getString(R.string.upgrade_subscribtion_successful_title);
//					String body = getResources().getString(R.string.upgrade_subscribtion_successful_text, clickedPLan.getValidityDate().toString());
//					showUpgradeDialog(title, body, true);
					Toast.makeText(this, "Already Subscribed", Toast.LENGTH_SHORT).show();
					finish();
				} else {
					openPlansPage();
				}
			}
			break;
		}
		case OperationDefinition.Hungama.OperationId.SUBSCRIPTION : {
			SubscriptionResponse currentUserPlan = (SubscriptionResponse) responseObjects.get(SubscriptionOperation.RESPONSE_KEY_SUBSCRIPTION);
			if (currentUserPlan.getPlan() == null) {
				// Check if real user is logged in and user did not press skip
				if (mApplicationConfigurations.isRealUser()) {
					//Check if date is valid (if subscription is still valid)
	//				if (!currentUserPlan.getValidityDate().equalsIgnoreCase(NA_DATE)  ) {				
	//					Date today = new Date();				
	//					Date validityDate = Utils.convertStringToDate(mApplicationConfigurations.getUserSubscriptionPlanDate());
	//					if (validityDate != null) {
	//						if (!validityDate.after(today)) {
							String msisdn =  mDeviceConfigurations.getDevicePhoneNumber();
							if (msisdn != null && !msisdn.equalsIgnoreCase("")) {
								mDataManager.getMobileVerification(msisdn, null, MobileOperationType.MOBILE_VERIFY, this);								
							} else {
								checkUserLoginNumber();
							}								
								
	//						}
	//					}
	//				}
				} else {
					startLoginActivity();
				}
			} else {
				
			}
			hideLoadingDialog();
			break;
		} 
		
		case OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION : {
			MobileVerificationResponse mobileVerificationResponse = (MobileVerificationResponse) responseObjects.get(MobileVerifyOperation.RESPONSE_KEY_MOBILE_VERIFICATION);
			if (mobileVerificationResponse.getMobileOperationType() == MobileOperationType.MOBILE_VERIFY) {
				if (mobileVerificationResponse.getCode().equalsIgnoreCase(PASSWORD_SMS_SENT)) {
					Bundle data = new Bundle();
					String verifyMobileNumber = "fragment_upgrade_mobile_verification";			
					data.putString(UpgradeFragment.ARGUMENT_MOBILE_VERIFICATION, verifyMobileNumber);
					addFragment(data);
				} else if (mobileVerificationResponse.getCode().equalsIgnoreCase(MSISDN_ALREADY_EXIST_AND_VERIFIED)) {
					Bundle data = new Bundle();
					String upgradePlans = "fragment_upgrade_plans";
					data.putString(UpgradeFragment.ARGUMENT_PLANS, upgradePlans);
					addFragment(data);
				} else {
					if (!isLoginMobileChecked) {
						checkUserLoginNumber();
						isLoginMobileChecked = true;
					} else {					
						Toast.makeText(this, mobileVerificationResponse.getMessage() + " " + getResources().getString(R.string.upgrade_mobile_number_verification_failed), Toast.LENGTH_LONG).show();
						Bundle data = new Bundle();
						String addMobileNumber = "fragment_upgrade_mobile_required";
						data.putString(UpgradeFragment.ARGUMENT_MOBILE_REQUIRED, addMobileNumber);
						addFragment(data); 
					}
				}
				hideLoadingDialog();
			}
			break;
		}
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		hideLoadingDialog();
		
	}

}
