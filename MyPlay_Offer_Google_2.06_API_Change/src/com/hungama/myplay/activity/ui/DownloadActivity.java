package com.hungama.myplay.activity.ui;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.DownloadOperationType;
import com.hungama.myplay.activity.data.dao.hungama.DownloadPlan;
import com.hungama.myplay.activity.data.dao.hungama.DownloadResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.MobileOperationType;
import com.hungama.myplay.activity.data.dao.hungama.MobileVerificationCountryCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.MobileVerificationResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.DownloadOperation;
import com.hungama.myplay.activity.operations.hungama.MobileVerifyCountryCheckOperation;
import com.hungama.myplay.activity.operations.hungama.MobileVerifyOperation;
import com.hungama.myplay.activity.services.DownloadFileService;
import com.hungama.myplay.activity.ui.fragments.DownloadFragment;
import com.hungama.myplay.activity.ui.fragments.UpgradeFragment;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ToastExpander;

/**
 * Controller for presenting details of the given MediaItem.
 */
public class DownloadActivity extends MainActivity implements CommunicationOperationListener{
	
	private static final String TAG = "DownloadActivity";
	
	protected static final String FRAGMENT_TAG_UPGRADE = "fragment_tag_upgrade";
	private static final int TOAST_SHOW_DELAY = 7*1000;
	private static final int TOAST_SHOW_DELAY_NOT_INDIA = 7*1000;
	
	private static final int LOGIN_ACTIVITY_CODE = 1;
	private static final String NA_DATE = "NA";
		
	public static final String PASSWORD_SMS_SENT = "1";
	private static final String MISSING_PARAMETERS = "2";
	public static final String MSISDN_ALREADY_EXIST_AND_VERIFIED = "3";
	private static final String VERIFICATION_CODE_DELIVERED  = "4";
	private static final String AUTHENTICATION_KEY_BLANK = "5";
	private static final String AUTHENTICATION_KEY_INVALID = "6";
	private static final String INVALID_MSISDN = "7";

	public static final String EXTRA_MEDIA_ITEM = "extra_media_item";
	
	public static final String CONTENT_TYPE_AUDIO = "audio";
	public static final String CONTENT_TYPE_VIDEO = "video";
	
	public static final int MOBILE_NOT_VERIFIED = 0;
	public static final int MOBILE_VERIFIED = 1;
	public static final int MOBILE_NOT_EXIST = -1;
	
	public static final String ARGUMENT_DOWNLOAD_ACTIVITY = "argument_download_activity";
	
	private FragmentManager mFragmentManager;
	private FragmentTransaction fragmentTransaction;
	private DataManager mDataManager;
	
	private MediaItem mMediaItem; 
	private TextView mTitleBarText;	
	private Dialog downloadDialog;
	private ApplicationConfigurations mApplicationConfigurations;
	private DeviceConfigurations mDeviceConfigurations;
	
	private boolean isLoginMobileChecked = false;
	
	public static String mobileToSend;
	
	private boolean isFromMsisdn = false;
	
	private DownloadFragment mDownloadFragment;
	private Bundle mDetailsData;
	
	private boolean hasGoogleAccount = true;
	
	private static WeakReference<DownloadActivity> wrActivity = null;
	
	// ======================================================
	// Activity life-cycle callbacks. 
	// ======================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		wrActivity = new WeakReference<DownloadActivity>(this);
		
		mDataManager = DataManager.getInstance(this.getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		mDeviceConfigurations = mDataManager.getDeviceConfigurations();
		
//		mDataManager.getSubscriptionPlans((long)0, SubscriptionType.PLAN, this);
		
		// validate calling intent.
		Intent intent = getIntent();
		if (intent == null) {
			Logger.e(TAG, "No intent for the given Activity.");
			return; 
		}
		
		setContentView(R.layout.activity_download);
		
		// SetS title bar
		mTitleBarText = (TextView) findViewById(R.id.main_title_bar_text);
		mTitleBarText.setText(getResources().getString(R.string.general_download));

		// Hide the Arrow to the right
		ImageButton arrow = (ImageButton) findViewById(R.id.main_title_bar_button_options);
		arrow.setVisibility(View.GONE);
		
		mFragmentManager = getSupportFragmentManager();	
		fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		Bundle data = intent.getExtras();
		if (data != null && data.containsKey(EXTRA_MEDIA_ITEM)) {
			// retrieves the given Media item for the activity.
			mMediaItem = (MediaItem) data.getSerializable(EXTRA_MEDIA_ITEM);
	
		}
		
		/*  checks if the user already logged in or not by checking
		 *  if the session is empty or not.
		 */
		checkIfUserIsLoggedIn();
//		ApplicationConfigurations applicationConfigurations = mDataManager.getApplicationConfigurations();
//		String sesion = applicationConfigurations.getSessionID();
//		boolean isRealUser = applicationConfigurations.isRealUser();
//		
//		if (!TextUtils.isEmpty(sesion) && isRealUser) {
//			// the user has session, start the main activity of the application.			
//			mDataManager.getDownload(0, mMediaItem.getId(), null, "", DownloadOperationType.DOWNLOAD_COUNT, this);			
//		} else {
//			// pass the user to login / sign up for the application.
////			Toast toast = 
//			Toast.makeText(this, getResources().getString(R.string.download_need_to_login), Toast.LENGTH_LONG).show();
////			toast.setGravity(Gravity.CENTER, 0, 0);
////			ToastExpander.showFor(toast, TOAST_SHOW_DELAY);
//			startLoginActivity();
//		}
		
		// Check if there are google accounts on the device
		AccountManager accountManager = AccountManager.get(this);
		Account[] accounts = accountManager.getAccountsByType("com.google");
		if(accounts == null || accounts.length == 0){
			hasGoogleAccount = false;
		}
	}
		
	@Override
	protected NavigationItem getNavigationItem() {
		// TODO: Do MediaItem must be valid ?
		if (mMediaItem != null) {
			if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				return NavigationItem.VIDEOS;
				
			} else if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				return NavigationItem.MUSIC;
			}
		}
		
		return NavigationItem.OTHER;
	}	
	
	// ======================================================
	// Helper Methods 
	// ======================================================
	
	public void startLoginActivity() {
		Intent startLoginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
		startLoginActivityIntent.putExtra(ARGUMENT_DOWNLOAD_ACTIVITY, "download_activity");
 		startActivityForResult(startLoginActivityIntent, LOGIN_ACTIVITY_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == LOGIN_ACTIVITY_CODE && resultCode == RESULT_OK) {
			// checks for valid session.
			String session = mDataManager.getApplicationConfigurations().getSessionID();
			if (!TextUtils.isEmpty(session)) {
				mDataManager.getDownload(0, mMediaItem.getId(), null, "", DownloadOperationType.DOWNLOAD_COUNT, this);
			} else {
				Toast toast = Toast.makeText(this, "You need to login first in order to upgrade", UpgradeFragment.TOAST_SHOW_DELAY);
				toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();
//				startLoginActivity();
			}
			
		} else if (requestCode == DownloadFragment.PURCHASE_REQUEST_CODE) {
			DownloadFragment downloadFragment = (DownloadFragment) getSupportFragmentManager().findFragmentByTag(DownloadFragment.TAG);
			downloadFragment.onActivityResult(requestCode, resultCode, data);
		} else {
		
			finish();
		}
	}
	
	public void addFragment(Bundle detailsData) {
		
		mDetailsData = detailsData;
		
		// For avoiding perform an action after onSaveInstanceState.
		new Handler().post(new Runnable() {
			
	        public void run() {
	        	
	        	mDownloadFragment = new DownloadFragment();
	    		mDownloadFragment.setArguments(mDetailsData);
	    		if ((wrActivity.get() != null) && !(wrActivity.get().isFinishing())) {
	                FragmentManager fm = wrActivity.get().getSupportFragmentManager();
	                FragmentTransaction ft = fm.beginTransaction();
	                ft.add(R.id.main_fragmant_container, mDownloadFragment, DownloadFragment.TAG);
	                ft.commitAllowingStateLoss();
	    		}
	        }
	    });		
	}
	
	/**
	 * Method to invoke mobile_verify, if a mobile number was entered by user when login
	 */
	public void checkUserLoginNumber() {
		if (!mApplicationConfigurations.getUserLoginPhoneNumber().equalsIgnoreCase("")) {
			isLoginMobileChecked = false;
			// check if mobile number already verified
			// if yes - 
			// if not - mobile verify
			mobileToSend = mApplicationConfigurations.getUserLoginPhoneNumber();
			isFromMsisdn = false;
			int isVerified = mApplicationConfigurations.isMobileNumberVerified(mApplicationConfigurations.getUserLoginPhoneNumber());
			if (isVerified == MOBILE_VERIFIED) {
				mDataManager.getDownload(0, mMediaItem.getId(), null, null, DownloadOperationType.BUY_PLANS, this);
			} else {
				mDataManager.checkCountry(mobileToSend, this);
			}		
//			mDataManager.getMobileVerification(mApplicationConfigurations.getUserLoginPhoneNumber(), null, MobileOperationType.MOBILE_VERIFY, this);			
//			mDataManager.getMobileVerification(mApplicationConfigurations.getUserLoginPhoneNumber(), "9sYK", MobileOperationType.MOBILE_PASSWORD_VERIFY, this);
		} else {
			Bundle dataMobileRequired = new Bundle();
			String addMobileNumber = "fragment_upgrade_mobile_required";
			dataMobileRequired.putSerializable(DownloadFragment.ARGUMENT_MEDIA_ITEM, (Serializable)mMediaItem);
			dataMobileRequired.putString(DownloadFragment.ARGUMENT_MOBILE_REQUIRED, addMobileNumber);
			addFragment(dataMobileRequired);
		}
	}
	
	public void showDownloadDialog(String header, String body, boolean isLeftButtonVisible, boolean isRightButtonVisible) {
		//set up custom dialog
        downloadDialog = new Dialog(this);
        downloadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        downloadDialog.setContentView(R.layout.dialog_download_same_song);
        
        TextView title = (TextView) downloadDialog.findViewById(R.id.download_custom_dialog_title_text);
        title.setText(header);
        
        TextView text = (TextView) downloadDialog.findViewById(R.id.download_custom_dialog_text);
        text.setText(body);
                       
        Button goToMyCollectionButton = (Button) downloadDialog.findViewById(R.id.download_left_button);
        Button downloadAgainButton = (Button) downloadDialog.findViewById(R.id.download_right_button);
        
        LinearLayout ButtonsPanel = (LinearLayout) downloadDialog.findViewById(R.id.buttons_panel);
        if (!isLeftButtonVisible && !isRightButtonVisible) {
        	ButtonsPanel.setVisibility(View.GONE);
        } else {
        	ButtonsPanel.setVisibility(View.VISIBLE);
        	if (isLeftButtonVisible) {
        		goToMyCollectionButton.setVisibility(View.VISIBLE);
        		goToMyCollectionButton.setOnClickListener(new OnClickListener() {
        			
        			@Override
        			public void onClick(View v) {
        				// TODO Auto-generated method stub
        				
        			}
        		});
        	} else {
        		goToMyCollectionButton.setVisibility(View.GONE);
        	}
        	
        	if (isRightButtonVisible) {
	        	downloadAgainButton.setVisibility(View.VISIBLE);
	        	downloadAgainButton.setOnClickListener(new OnClickListener() {
	    			
	    			@Override
	    			public void onClick(View v) {
	    				// TODO Auto-generated method stub
	    				
	    			}
	    		});
	        } else {
	        	downloadAgainButton.setVisibility(View.GONE);
	        }
        }
        
        ImageButton closeButton = (ImageButton) downloadDialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				downloadDialog.dismiss();
				onBackPressed();
			}
		});
        downloadDialog.setCancelable(true);
        downloadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				downloadDialog.dismiss();
				onBackPressed();				
			}
		});
        downloadDialog.show();

	}
	
	public void showDownloadDialog(String header, String body, boolean isImageVisible) {
		//set up custom dialog
        downloadDialog = new Dialog(this);
        downloadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        downloadDialog.setContentView(R.layout.dialog_upgrade_status_subscription);
        
        TextView title = (TextView) downloadDialog.findViewById(R.id.upgrade_custom_dialog_title_text);
        title.setText(header);
        
        TextView text = (TextView) downloadDialog.findViewById(R.id.upgrade_custom_dialog_text);
        text.setText(body);
        
        TextView text2 = (TextView) downloadDialog.findViewById(R.id.upgrade_custom_dialog_text2);
        
        if(isImageVisible) {
        	text2.setText(body);
        	text2.setVisibility(View.VISIBLE);
        } else {
        	text2.setVisibility(View.GONE);
        }
        
        ImageButton closeButton = (ImageButton) downloadDialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				downloadDialog.dismiss();
				finish(); 
				
			}
		});
        downloadDialog.setCancelable(true);
        downloadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				downloadDialog.dismiss();
				getFragmentManager().popBackStack();
				finish(); 
				
			}
		});
        downloadDialog.show();

	}
	
	public void openMyCollectionDirectory(View view) {
		
	}
	
	public void downloadAgain(View view) {
		
	}
	
	public void replaceFragment(Bundle data) {
		// replace the current fragment with the forgot password submission fragment.
		DownloadFragment downloadFragment = new DownloadFragment();
		downloadFragment.setArguments(data);	
//		DownloadFragment.setOnForgotPasswordSubmitListener(this);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		try {
			fragmentTransaction.replace(R.id.main_fragmant_container, downloadFragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}  catch (Exception e) {
			Logger.e(TAG, e.getMessage());
		}
		
	}

	public void checkIfUserIsLoggedIn() {
		ApplicationConfigurations applicationConfigurations = mDataManager.getApplicationConfigurations();
		String sesion = applicationConfigurations.getSessionID();
		boolean isRealUser = applicationConfigurations.isRealUser();
		
		if (!TextUtils.isEmpty(sesion) && isRealUser) {
			// the user has session, start the main activity of the application.			
			mDataManager.getDownload(0, mMediaItem.getId(), null, "", DownloadOperationType.DOWNLOAD_COUNT, this);			
		} else {
			// pass the user to login / sign up for the application.
//			Toast toast = 
			Toast.makeText(this, getResources().getString(R.string.download_need_to_login), Toast.LENGTH_LONG).show();
//			toast.setGravity(Gravity.CENTER, 0, 0);
//			ToastExpander.showFor(toast, TOAST_SHOW_DELAY);
			startLoginActivity();
		}
	}
	
	// ======================================================
	// Operation Methods 
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		switch(operationId) {
			case OperationDefinition.Hungama.OperationId.DOWNLOAD:
			case OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION:
				showLoadingDialog(R.string.application_dialog_loading_content);
				break;
		}
		
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		switch(operationId) {			
			
			case OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION_COUNTRY_CHECK : {
				MobileVerificationCountryCheckResponse countryCheckResponse = 
						(MobileVerificationCountryCheckResponse) responseObjects.get(MobileVerifyCountryCheckOperation.RESPONSE_KEY_MOBILE_VERIFICATION_COUNTRY_CHECK);
				if (countryCheckResponse.isIndia()) {
					if (isFromMsisdn) {						
						mDataManager.getDownload(0, mMediaItem.getId(), null, null, DownloadOperationType.BUY_PLANS, this);
					} else {
						mDataManager.getMobileVerification(mApplicationConfigurations.getUserLoginPhoneNumber(), null, MobileOperationType.MOBILE_VERIFY, this);
					}
				} else {
					Toast toast = Toast.makeText(this, getResources().getString(R.string.download_country_check_not_india), Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					ToastExpander.showFor(toast, TOAST_SHOW_DELAY);
				}
				break;
			}
		
			case OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION : {
				MobileVerificationResponse mobileVerificationResponse = (MobileVerificationResponse) responseObjects.get(MobileVerifyOperation.RESPONSE_KEY_MOBILE_VERIFICATION);
				if (mobileVerificationResponse.getMobileOperationType() == MobileOperationType.MOBILE_VERIFY) {
					if (mobileVerificationResponse.getCode().equalsIgnoreCase(PASSWORD_SMS_SENT)) {
						Bundle data = new Bundle();
						String verifyMobileNumber = "fragment_upgrade_mobile_verification";
						data.putSerializable(DownloadFragment.ARGUMENT_MEDIA_ITEM, (Serializable)mMediaItem);
						data.putString(DownloadFragment.ARGUMENT_MOBILE_VERIFICATION, verifyMobileNumber);
						addFragment(data);						
					} else if (mobileVerificationResponse.getCode().equalsIgnoreCase(MSISDN_ALREADY_EXIST_AND_VERIFIED)) {
						mDataManager.getDownload(0, mMediaItem.getId(), null, "", DownloadOperationType.DOWNLOAD_COUNT, this);
					} else {
						if (!isLoginMobileChecked) {
							checkUserLoginNumber();
							isLoginMobileChecked = true;
						} else {					
							Toast.makeText(this, mobileVerificationResponse.getMessage() + " " + getResources().getString(R.string.upgrade_mobile_number_verification_failed), Toast.LENGTH_LONG).show();
							Bundle data = new Bundle();
							String addMobileNumber = "fragment_upgrade_mobile_required";
							data.putSerializable(DownloadFragment.ARGUMENT_MEDIA_ITEM, (Serializable)mMediaItem);
							data.putString(DownloadFragment.ARGUMENT_MOBILE_REQUIRED, addMobileNumber);
							addFragment(data); 
						}
					}
					hideLoadingDialog();
				}
				break;
			}
			
			case OperationDefinition.Hungama.OperationId.DOWNLOAD : {
				DownloadResponse downloadResponse = (DownloadResponse) responseObjects.get(DownloadOperation.RESPONSE_KEY_DOWNLOAD);
				
					if (downloadResponse.getDownloadType() == DownloadOperationType.DOWNLOAD_COUNT) {
						// check if success response from Hungama. code = 1
//						if (downloadResponse.getCode().equalsIgnoreCase(PASSWORD_SMS_SENT)) {
							if (downloadResponse.getMsisdn() != null) {
								mobileToSend = downloadResponse.getMsisdn();
							}						
							if (downloadResponse.getRemainingDownloadCount() > 0) {									
								mDataManager.getDownload(downloadResponse.getPlanId(), mMediaItem.getId(), mobileToSend, downloadResponse.getType(), DownloadOperationType.BUY_CHARGE, this);
							} else if (downloadResponse.getBalanceCreditLimit() > 0) {
								mDataManager.getDownload(downloadResponse.getPlanId(), mMediaItem.getId(), mobileToSend, downloadResponse.getType(), DownloadOperationType.BUY_CHARGE, this);
								} else {
									// User has no credit Balance									
									// New changes
									mDataManager.getDownload(0, mMediaItem.getId(), null, null, DownloadOperationType.BUY_PLANS, this);
									// End new Changes
									
//	//								mDataManager.getDownload(0, mMediaItem.getId(), null, null, DownloadOperationType.BUY_PLANS, this); OLD
//									String msisdn =  mDeviceConfigurations.getDevicePhoneNumber();
//									 /*Check if device mobile number(msisdn) is available. 
//									 		yes - verify it.  
//											no - check if user logged in with a number
//														yes - verify it.
//														no - ask user to enter a number. (add fragment) */
//									if (msisdn != null && !msisdn.equalsIgnoreCase("")) {
//										mobileToSend = msisdn;
//										isFromMsisdn = true;
//										mDataManager.checkCountry(msisdn, this);
//	//									mDataManager.getMobileVerification(msisdn, null, MobileOperationType.MOBILE_VERIFY, this); OLD
//	//									mDataManager.getDownload(0, mMediaItem.getId(), null, DownloadOperationType.DOWNLOAD_COUNT, this);
//									} else {
//										
//										checkUserLoginNumber();
//									}	
								}
//						} 
//						else {
//							String msisdn =  mDeviceConfigurations.getDevicePhoneNumber();
//							 /*Check if device mobile number(msisdn) is available. 
//							 		yes - verify it.  
//									no - check if user logged in with a number
//												yes - verify it.
//												no - ask user to enter a number. (add fragment) */
//							if (msisdn != null && !msisdn.equalsIgnoreCase("")) {
//								mobileToSend = msisdn;
//								isFromMsisdn = true;
//								mDataManager.checkCountry(msisdn, this);
////									mDataManager.getMobileVerification(msisdn, null, MobileOperationType.MOBILE_VERIFY, this); OLD
////									mDataManager.getDownload(0, mMediaItem.getId(), null, DownloadOperationType.DOWNLOAD_COUNT, this);
//							} else {
//								
//								checkUserLoginNumber();
//							}	
//						}
						
						
						
					} else if (downloadResponse.getDownloadType() == DownloadOperationType.BUY_CHARGE) {						
						if (downloadResponse.getCode().equalsIgnoreCase(PASSWORD_SMS_SENT)) {
							// set the content type to be sent with buy_charge
							String contentType = null;
							if (!TextUtils.isEmpty(mMediaItem.getMediaType().toString())) {
								if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO || mMediaItem.getMediaType() == MediaType.VIDEO) {
									contentType = CONTENT_TYPE_VIDEO;
								} else if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC || mMediaItem.getMediaType() == MediaType.TRACK) {
									contentType = CONTENT_TYPE_AUDIO;
								}
							}
							mDataManager.getDownload(0, mMediaItem.getId(), null, contentType, DownloadOperationType.CONTENT_DELIVERY, this);
						} else {
							hideLoadingDialog();
							Logger.i(TAG, downloadResponse.getMessage());
							if (downloadResponse.getDisplay().equalsIgnoreCase(DownloadActivity.PASSWORD_SMS_SENT)) {
								String title = getResources().getString(R.string.upgrade_subscribtion_unsuccessful_title);
								String body = downloadResponse.getMessage();
								showDownloadDialog(title, body, false);
							} else {
								finish();
							}
//							Toast.makeText(this, "Error Downloading", Toast.LENGTH_LONG).show();
//							finish();
						}
						
					} else if (downloadResponse.getDownloadType() == DownloadOperationType.BUY_PLANS) {
						if (downloadResponse.getCode().equalsIgnoreCase(PASSWORD_SMS_SENT)) {	
							List<DownloadPlan> downloadPlans = new ArrayList<DownloadPlan>();
							
							for (int i = 0; i < downloadResponse.getPlan().size(); i++)  {
								
								DownloadPlan downloadPlan = downloadResponse.getPlan().get(i);
								DownloadPlan dp = new DownloadPlan(downloadPlan.getPlanId(), downloadPlan.getPlanName(), 
										downloadPlan.getPlanCurrency(), downloadPlan.getPlanPrice(), downloadPlan.getMsisdn(), 
										downloadPlan.getStatus(), downloadPlan.getCreditBalance(), downloadPlan.getRedeem(), downloadPlan.getType(), downloadPlan.getIsIndia());
								
								if(downloadPlan.getType().equalsIgnoreCase(DownloadPlan.ACCOUNT_TYPE_GOOGLE)){
									downloadPlans.add(dp);
								}else{
									downloadPlans.add(dp);
								}
							}
							
	//						downloadPlans = downloadResponse.getPlan();
							if (downloadPlans != null) {							
								Bundle data = new Bundle();
								String showDownloadPlans = "fragment_download_plans";
								data.putSerializable(DownloadFragment.ARGUMENT_MEDIA_ITEM, (Serializable)mMediaItem);
								data.putSerializable(DownloadFragment.ARGUMENT_PLANS_TO_BUY, (Serializable)downloadPlans);
								data.putString(DownloadFragment.ARGUMENT_PLANS, showDownloadPlans);
//								if (fromBuyButton) {
//									replaceFragment(data);
//								} else {
									addFragment(data);
//								}							
							}
						} else {
							hideLoadingDialog();
							Logger.i(TAG, downloadResponse.getMessage());
							Toast.makeText(this, "Error Downloading", Toast.LENGTH_LONG).show();
							finish();
						}
						
					
					} else if (downloadResponse.getDownloadType() == DownloadOperationType.CONTENT_DELIVERY) {
//						Toast.makeText(this, downloadResponse.getMessage(), DownloadFragment.TOAST_SHOW_DELAY).show();
						if (downloadResponse.getCode().equalsIgnoreCase(DownloadActivity.PASSWORD_SMS_SENT)) {

							String contentType = null;
							if (!TextUtils.isEmpty(mMediaItem.getMediaType().toString())) {
								if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO || mMediaItem.getMediaType() == MediaType.VIDEO) {
									contentType = MediaContentType.VIDEO.toString().toLowerCase();
								} else if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC || mMediaItem.getMediaType() == MediaType.TRACK) {
									contentType = "song";
								}
							}
							
							mDataManager.checkBadgesAlert(String.valueOf(mMediaItem.getId()),	
															contentType, 
															"music_video_download", 
															this);
							
							if (mMediaItem != null) {
								if (mMediaItem.getMediaContentType() == null || mMediaItem.getMediaContentType() != MediaContentType.VIDEO) {
									mMediaItem.setMediaContentType(MediaContentType.MUSIC);
								}
								Intent intent = new Intent(this, DownloadFileService.class);
								intent.putExtra(DownloadFileService.TRACK_KEY, (Serializable) mMediaItem);
								intent.putExtra(DownloadFileService.DOWNLOAD_URL, downloadResponse.getUrl());
								startService(intent);
								
								// show a download success dialog							
								showDownloadDialog(getResources().getString(R.string.download_media_success_title_thank_you), 
													getResources().getString(R.string.download_media_success_body), 
													false, false);
							} else {
							
								// show a download success dialog							
								showDownloadDialog(getResources().getString(R.string.download_media_unsucceded_toast), 
												getResources().getString
												(R.string.download_media_unsucceded_toast_text), false, false);
							
	//							DownloadFile downloadFile = new DownloadFile(downloadResponse.getUrl());
	//							downloadFile.execute();
							}
						} else {
							hideLoadingDialog();
							Logger.i(TAG, downloadResponse.getMessage());
							Toast.makeText(this, "Error Downloading", Toast.LENGTH_LONG).show();
							finish();
						}
//						
					}
					
					
					hideLoadingDialog();
				
				break;
			}
			
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		
		hideLoadingDialog();
		
		if (operationId == OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION) {
			Logger.i(TAG, "Failed loading mobile verification");
		}else if (operationId == OperationDefinition.Hungama.OperationId.DOWNLOAD) {
			Logger.i(TAG, "Failed loading Download" + " : " + errorMessage);
			showDialog(getString(R.string.download_error), getString(R.string.error_while_downloading_the_content_please_try_again));
		}
	}
	
	private void showDialog(String title, String text) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		 
		// set title
		alertDialogBuilder.setTitle(title);

		// set dialog message
		alertDialogBuilder
			.setMessage(text)
			.setCancelable(true)				
			.setNegativeButton(R.string.exit_dialog_text_ok ,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, just close
					// the dialog box and do nothing
					dialog.cancel();
					finish();
				}
			});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
	}
	
}
