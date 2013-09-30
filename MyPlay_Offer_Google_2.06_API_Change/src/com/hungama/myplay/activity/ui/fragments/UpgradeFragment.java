package com.hungama.myplay.activity.ui.fragments;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MobileOperationType;
import com.hungama.myplay.activity.data.dao.hungama.MobileVerificationCountryCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.MobileVerificationResponse;
import com.hungama.myplay.activity.data.dao.hungama.Plan;
import com.hungama.myplay.activity.data.dao.hungama.PlanType;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionType;
import com.hungama.myplay.activity.data.dao.hungama.Video;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MobileVerifyCountryCheckOperation;
import com.hungama.myplay.activity.operations.hungama.MobileVerifyOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionOperation;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ToastExpander;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.billing.IabHelper.OnIabPurchaseFinishedListener;
import com.hungama.myplay.activity.util.billing.IabHelper.OnIabSetupFinishedListener;
import com.hungama.myplay.activity.util.billing.IabHelper.QueryInventoryFinishedListener;
import com.hungama.myplay.activity.util.billing.IabHelper;
import com.hungama.myplay.activity.util.billing.IabResult;
import com.hungama.myplay.activity.util.billing.Inventory;
import com.hungama.myplay.activity.util.billing.Purchase;

public class UpgradeFragment extends MainFragment implements CommunicationOperationListener, 
																OnClickListener,
																OnIabSetupFinishedListener,
																OnIabPurchaseFinishedListener,
																QueryInventoryFinishedListener{
	
	public static final String TAG = "UpgradeFragment";
	public static boolean isWebViewOpen = false;
	
	public static final String ARGUMENT_MOBILE_REQUIRED = "argument_mobile_required";
	public static final String ARGUMENT_MOBILE_VERIFICATION = "argument_mobile_verification";
	public static final String ARGUMENT_PLANS = "argument_plans";
	
	public static final int DAILY = 1;
	public static final int WEEKLY = 7;
	public static final int TOAST_SHOW_DELAY = 7*1000;
	
	public static final int MOBILE_NOT_VERIFIED = 0;
	public static final int MOBILE_VERIFIED = 1;
	public static final int MOBILE_NOT_EXIST = -1;
	
	private View rootView;
	private Bundle detailsData;
	private DataManager mDataManager;
	
	private EditText mobileNumberField;
	private EditText passwordField;
	private Dialog upgradeDialog;
	private WebView webView;
	private Plan clickedPLan;
	private Video video;
	
	private boolean mHasLoaded = false; 
	
	private List<Plan> plans;
	private Context mContext;
	private DeviceConfigurations mDeviceConfigurations;
	private Resources mResources;
	
	private PlansAdapter plansAdapter;
	
	private ApplicationConfigurations mApplicationConfigurations;
	
	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;
	
	// In App Billing
	private IabHelper billingHelper;
	public static final int PURCHASE_REQUEST_CODE = 123;
	
	// Mobile verification
	public String mobileToVerify;
	private boolean isLoginMobileChecked = false;
	
	private boolean isBindToGoogle = true;
	
	/**
	 * Registers a callback to be invoked when the user has selected an action upon a {@link MediaItem}.
	 * @param listener
	 */
	public void setOnMediaItemOptionSelectedListener(OnMediaItemOptionSelectedListener listener) {
		mOnMediaItemOptionSelectedListener = listener;
	}
	
	
	// ======================================================
	// Fragment lifecycle methods
	// ======================================================
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// initialize components.
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();	
		mContext = getActivity();
		mResources = getResources();
		mDeviceConfigurations = mDataManager.getDeviceConfigurations();
		// gets the media item from parent.
//		Bundle data = getArguments();
//		mMediaItem = (MediaItem) data.getSerializable(ARGUMENT_UPGRADE);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		detailsData = getArguments();
		if (detailsData.getString(ARGUMENT_MOBILE_REQUIRED) != null) {
			rootView = inflater.inflate(R.layout.fragment_upgrade_mobile_required, container, false);
			initializeMobileRquiredControls(rootView);
		} else if (detailsData.getString(ARGUMENT_MOBILE_VERIFICATION) != null) {
			rootView = inflater.inflate(R.layout.fragment_upgrade_mobile_verification, container, false);
			initializeMobileVerificationControls(rootView);
		} else {
			rootView = inflater.inflate(R.layout.fragment_upgrade_plans, container, false);
			initializeMobileUpgradePlansControls(rootView);
		}
		
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onStart() {
		super.onStart();
//		
//		if (!mHasLoaded) {
//			if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
//				mDataManager.getVideoDetails(mMediaItem, this);
//			} else {
//				mDataManager.getMediaDetails(mMediaItem, this);
//			}						
//		} else {
//			if (mMediaItem.getMediaType() == MediaType.VIDEO) {
//				// get details for video (video streaming).
//				populateUserControls(video);
//			} else if (mMediaItem.getMediaType() == MediaType.ALBUM || mMediaItem.getMediaType() == MediaType.PLAYLIST) {
//				// get details for albums / playlists.
////				populateUserControls(mMediaSetDetails);
//				setActionButtons();
//			} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
//				// get details for track (song).
////				populateUserControls(mMediaTrackDetails);
//				setActionButtons();
//			}
//		}
	}
	

	// ======================================================
	// Operation callback methods
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION) {
			showLoadingDialog(R.string.application_dialog_loading_content);
//			mHasLoaded = true;
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION) {
			showLoadingDialog(R.string.application_dialog_loading_content);
		}
	}
	
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION_COUNTRY_CHECK) {
			MobileVerificationCountryCheckResponse countryCheckResponse = 
					(MobileVerificationCountryCheckResponse) responseObjects.get(MobileVerifyCountryCheckOperation.RESPONSE_KEY_MOBILE_VERIFICATION_COUNTRY_CHECK);
			if (countryCheckResponse.isIndia()) {
				mDataManager.getMobileVerification(mobileToVerify, null, MobileOperationType.MOBILE_VERIFY, this);
//				if (mApplicationConfigurations.isMobileNumberVerified(mobileNumberField.getText().toString()) == MOBILE_VERIFIED) {						
//					Bundle data = new Bundle();
//					String upgradePlans = "fragment_upgrade_plans";
//					data.putString(UpgradeFragment.ARGUMENT_PLANS, upgradePlans);
//					((UpgradeActivity) getActivity()).replaceFragment(data);	
//				} else {
//					mApplicationConfigurations.setUserLoginPhoneNumber(mobileNumberField.getText().toString());
//					Bundle data = new Bundle();
//					String verifyMobileNumber = "fragment_upgrade_mobile_verification";			
//					data.putString(UpgradeFragment.ARGUMENT_MOBILE_VERIFICATION, verifyMobileNumber);
//					((UpgradeActivity) getActivity()).replaceFragment(data);
//				}
			} else {
				Toast toast = Toast.makeText(((UpgradeActivity) getActivity()).getApplicationContext(), getResources().getString(R.string.download_country_check_not_india), Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				ToastExpander.showFor(toast, TOAST_SHOW_DELAY);
			}
			hideLoadingDialog();			
		}
		
		if (operationId == OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION) {
			MobileVerificationResponse mobileVerificationResponse = (MobileVerificationResponse) responseObjects.get(MobileVerifyOperation.RESPONSE_KEY_MOBILE_VERIFICATION);
			
			if (mobileVerificationResponse.getMobileOperationType() == MobileOperationType.MOBILE_VERIFY) {
				if (mobileVerificationResponse.getCode().equalsIgnoreCase(UpgradeActivity.PASSWORD_SMS_SENT)) {
					//save number in appConfiguration.
					if (mobileToVerify != null) {
						mApplicationConfigurations.setUserLoginPhoneNumber(mobileToVerify);
						int isVerified = mApplicationConfigurations.isMobileNumberVerified(mobileToVerify);
						if (isVerified == MOBILE_NOT_VERIFIED || isVerified == MOBILE_NOT_EXIST ) {
							mApplicationConfigurations.setMobileNumber(mobileToVerify, MOBILE_NOT_VERIFIED);
						}
					}					
					Bundle data = new Bundle();
					String verifyMobileNumber = "fragment_upgrade_mobile_verification";
					data.putString(UpgradeFragment.ARGUMENT_MOBILE_VERIFICATION, verifyMobileNumber);
					((UpgradeActivity) getActivity()).replaceFragment(data);
//					UpgradeActivity.mobileToVerify = mobileNumberField.getText().toString();
				} else if (mobileVerificationResponse.getCode().equalsIgnoreCase(UpgradeActivity.MSISDN_ALREADY_EXIST_AND_VERIFIED)) {					
					if (mobileToVerify != null) {
						mApplicationConfigurations.setUserLoginPhoneNumber(mobileToVerify);
						int isVerified = mApplicationConfigurations.isMobileNumberVerified(mobileToVerify);
						if (isVerified == MOBILE_NOT_VERIFIED || isVerified == MOBILE_NOT_EXIST ) {
							mApplicationConfigurations.setMobileNumber(mobileToVerify, MOBILE_VERIFIED);
						}
//						UpgradeActivity.mobileToVerify = mobileNumberField.getText().toString();
					}
//					Bundle data = new Bundle();
//					String upgradePlans = "fragment_upgrade_plans";
//					data.putString(UpgradeFragment.ARGUMENT_PLANS, upgradePlans);
//					((UpgradeActivity) getActivity()).replaceFragment(data);
					
				} else {
					Toast.makeText(((UpgradeActivity) getActivity()).getApplicationContext(), mobileVerificationResponse.getMessage() + " " + getResources().getString(R.string.upgrade_mobile_number_verification_failed), TOAST_SHOW_DELAY).show();
					Bundle data = new Bundle();
					String addMobileNumber = "fragment_upgrade_mobile_required";
					data.putString(UpgradeFragment.ARGUMENT_MOBILE_REQUIRED, addMobileNumber);
					((UpgradeActivity) getActivity()).replaceFragment(data);
				}
				hideLoadingDialog();
				
			} else if (mobileVerificationResponse.getMobileOperationType() == MobileOperationType.MOBILE_PASSWORD_VERIFY) {
				if (mobileVerificationResponse.getCode().equalsIgnoreCase(UpgradeActivity.PASSWORD_SMS_SENT)) {
					// If Password Verified Successfully
					int isVerified = mApplicationConfigurations.isMobileNumberVerified(mobileToVerify);
					if (isVerified == MOBILE_NOT_VERIFIED || isVerified == MOBILE_NOT_EXIST ) {
						mApplicationConfigurations.setMobileNumber(mobileToVerify, MOBILE_VERIFIED);
					}
					
					Plan clickedPlanTemp = mApplicationConfigurations.getTempClickedPlan();
					
					mDataManager.getSubscriptionCharge(clickedPlanTemp.getPlanId(), clickedPlanTemp.getType(), SubscriptionType.CHARGE, UpgradeFragment.this, null, null, null);
					
//					Bundle data = new Bundle();
//					String upgradePlans = "fragment_upgrade_plans";
//					data.putString(UpgradeFragment.ARGUMENT_PLANS, upgradePlans);
//					((UpgradeActivity) getActivity()).replaceFragment(data);
//					
//					Toast.makeText(((UpgradeActivity) getActivity()).getApplicationContext(),
//							R.string.upgrade_mobile_number_verification_success2, 
//							TOAST_SHOW_DELAY).show();
					
				} else {
					Toast.makeText(((UpgradeActivity) getActivity()).getApplicationContext(), mobileVerificationResponse.getMessage() + getResources().getString(R.string.upgrade_mobile_number_verification_failed), TOAST_SHOW_DELAY).show();
//					Bundle data = new Bundle();
//					String upgradePlans = "fragment_upgrade_plans";
//					data.putString(UpgradeFragment.ARGUMENT_PLANS, upgradePlans);
//					((UpgradeActivity) getActivity()).replaceFragment(data);
				}
				hideLoadingDialog();
			
			} else if (mobileVerificationResponse.getMobileOperationType() == MobileOperationType.RESEND_PASSWORD) {
				hideLoadingDialog();
				if (mobileVerificationResponse.getCode().equalsIgnoreCase(UpgradeActivity.PASSWORD_SMS_SENT)) { // If Password Verified Successfully
					Toast.makeText(((UpgradeActivity) getActivity()).getApplicationContext(), mobileVerificationResponse.getMessage(), TOAST_SHOW_DELAY).show();
				} else {
					Toast.makeText(((UpgradeActivity) getActivity()).getApplicationContext(), mobileVerificationResponse.getMessage() + " " + getResources().getString(R.string.upgrade_mobile_number_verification_failed), TOAST_SHOW_DELAY).show();					
				}
				
			}
		
		
		} else if (operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK) {
			SubscriptionCheckResponse subscriptionCheckResponse = (SubscriptionCheckResponse) responseObjects.get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);
			if (subscriptionCheckResponse != null) {
				if (subscriptionCheckResponse.getCode().equalsIgnoreCase(UpgradeActivity.PASSWORD_SMS_SENT)) {
					mApplicationConfigurations.setIsUserHasSubscriptionPlan(true);
					mApplicationConfigurations.setUserSubscriptionPlanDate(subscriptionCheckResponse.getPlan().getValidityDate());
					clickedPLan = mApplicationConfigurations.getTempClickedPlan();
					if (clickedPLan != null) {
						clickedPLan.setValidityDate(subscriptionCheckResponse.getPlan().getValidityDate());
						clickedPLan.setPurchaseDate(subscriptionCheckResponse.getPlan().getPurchaseDate());
						subscriptionCheckResponse.setPlan(clickedPLan);
					} else {
						subscriptionCheckResponse.setPlan(new Plan(subscriptionCheckResponse.getPlan().getPlanId(), 
																	Utils.TEXT_EMPTY, Utils.TEXT_EMPTY, Utils.TEXT_EMPTY, 0, 
																	Utils.TEXT_EMPTY, subscriptionCheckResponse.getPlan().getSubscriptionStatus(), 
																	subscriptionCheckResponse.getPlan().getPurchaseDate(), 
																	subscriptionCheckResponse.getPlan().getValidityDate(), 
																	Utils.TEXT_EMPTY, Utils.TEXT_EMPTY, Utils.TEXT_EMPTY));
					}					
					mDataManager.storeCurrentSubscriptionPlan(subscriptionCheckResponse);
					String title = getResources().getString(R.string.upgrade_subscribtion_successful_title);
					String date = clickedPLan.getValidityDate();
					
					// We need to show only yyyy/mm/dd from the date
					Date d = Utils.convertTimeStampToDate(date);
					String dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM).format(d);
					
					String body = getResources().getString(R.string.upgrade_subscribtion_successful_text, dateFormat);
					showUpgradeDialog(title, body, true);
				}
			}
		
		} else if (operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION) {
			SubscriptionResponse subscriptionResponse = (SubscriptionResponse) responseObjects.get(SubscriptionOperation.RESPONSE_KEY_SUBSCRIPTION);
			if (subscriptionResponse != null) {
				if (subscriptionResponse.getSubscriptionType() == SubscriptionType.PLAN) {
					plans = subscriptionResponse.getPlan();
					if (plans != null) {
						setupInAppBilling();
						ListView mPlan = (ListView) rootView.findViewById(R.id.listview_upgrade_plans);
						plansAdapter = new PlansAdapter(plans);	
						mPlan.setAdapter(plansAdapter);
					}
					
				} else if (subscriptionResponse.getSubscriptionType() == SubscriptionType.CHARGE) {
					if (subscriptionResponse.getCode().equalsIgnoreCase(UpgradeActivity.PASSWORD_SMS_SENT) || subscriptionResponse.getCode().equalsIgnoreCase(UpgradeActivity.VERIFICATION_CODE_DELIVERED)) {
//						Toast.makeText(((UpgradeActivity) getActivity()).getApplicationContext(), subscriptionResponse.getMessage(), TOAST_SHOW_DELAY).show();
						AccountManager accountManager = AccountManager.get(getActivity().getApplicationContext());
						Account[] accounts = accountManager.getAccountsByType("com.google");
						
						String accountType = null;
						if(accounts != null && accounts.length > 0){
							accountType = accounts[0].name; 
						}
						mDataManager.getCurrentSubscriptionPlan(this, accountType);
						
//						String title = getResources().getString(R.string.upgrade_subscribtion_successful_title);
//						String body = getResources().getString(R.string.upgrade_subscribtion_successful_text, clickedPLan.getValidityDate().toString());
//						showUpgradeDialog(title, body, true);
					} else {
						Logger.i(TAG, subscriptionResponse.getMessage());
//						Toast.makeText(((UpgradeActivity) getActivity()).getApplicationContext(), subscriptionResponse.getMessage(), TOAST_SHOW_DELAY).show();
						if (subscriptionResponse.getDisplay().equalsIgnoreCase(UpgradeActivity.PASSWORD_SMS_SENT)) {
							String title = getResources().getString(R.string.upgrade_subscribtion_unsuccessful_title);
							String body = subscriptionResponse.getMessage();
							showUpgradeDialog(title, body, false);
						} else {
							((UpgradeActivity) getActivity()).finish();
						}
					}
				}
			}	
			hideLoadingDialog();
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION) {
			Logger.i(TAG, "Failed loading mobile verification");
		}else if (operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION) {
			Logger.i(TAG, "Failed loading upgrade plans");
			}
		hideLoadingDialog();
	}
	
	
	// ======================================================
	// Private helper methods.
	// ======================================================
	
	private void initializeMobileRquiredControls(final View rootView) {
		
		Button submitMobileNumber = (Button) rootView.findViewById(R.id.submit_mobile_number_button);
		submitMobileNumber.setOnClickListener(this);
		
		mobileNumberField = (EditText) rootView.findViewById(R.id.mobile_number_field);
	}
	
	
	private void initializeMobileVerificationControls(final View rootView) {
		
		Button resendSms = (Button) rootView.findViewById(R.id.upgrade_resend_sms_button_submit);
		resendSms.setOnClickListener(this);
		
		Button submitMobileNumber = (Button) rootView.findViewById(R.id.upgrade_verify_password_button_submit);
		submitMobileNumber.setOnClickListener(this);
		
		passwordField = (EditText) rootView.findViewById(R.id.upgrade_password_verification_field);
	}
	
	
	private void initializeMobileUpgradePlansControls(final View rootView) {
//		setupInAppBilling();
        webView = (WebView) rootView.findViewById(R.id.webView);
        
        webView.requestFocus(View.FOCUS_DOWN);
        webView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    if (!v.hasFocus()) {
                        v.requestFocus();
                    }
                    break;
            }
            return false;
			}
		});
        
		mDataManager.getSubscriptionPlans(0, SubscriptionType.PLAN, this);

	}
	
	public void showUpgradeDialog(String header, String body, final boolean isImageVisible) {
		//set up custom dialog
        upgradeDialog = new Dialog(getActivity());
        upgradeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        upgradeDialog.setContentView(R.layout.dialog_upgrade_status_subscription);       
        
        TextView title = (TextView) upgradeDialog.findViewById(R.id.upgrade_custom_dialog_title_text);
        title.setText(header);
        
        TextView text = (TextView) upgradeDialog.findViewById(R.id.upgrade_custom_dialog_text);
        text.setText(body);
        
        TextView text2 = (TextView) upgradeDialog.findViewById(R.id.upgrade_custom_dialog_text2);
        
        if(isImageVisible) {
        	text2.setText(getResources().getString(R.string.upgrade_subscribtion_successful_text2));
        	text2.setVisibility(View.VISIBLE);
        } else {
        	text2.setVisibility(View.GONE);
        }
        
        ImageButton closeButton = (ImageButton) upgradeDialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				upgradeDialog.dismiss();
				getActivity().setResult(UpgradeActivity.RESULT_OK);
				getActivity().finish();
				if (isImageVisible) {
					mDataManager.checkBadgesAlert("",	
							"", 
							"music_subscription", 
							UpgradeFragment.this);
				}
			}
		});
        upgradeDialog.setCancelable(true);
        upgradeDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				upgradeDialog.dismiss();
				getActivity().setResult(UpgradeActivity.RESULT_OK);
				getActivity().finish();
				if (isImageVisible) {
					mDataManager.checkBadgesAlert("",	
							"", 
							"music_subscription", 
							UpgradeFragment.this);
				}
				
			}
		});
        upgradeDialog.show();

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
			mDataManager.checkCountry(mobileToVerify, this);
//			mDataManager.getMobileVerification(mApplicationConfigurations.getUserLoginPhoneNumber(), null, MobileOperationType.MOBILE_VERIFY, this);	OLD		
		} else {
			Bundle dataMobileRequired = new Bundle();
			String addMobileNumber = "fragment_upgrade_mobile_required";
			dataMobileRequired.putString(UpgradeFragment.ARGUMENT_MOBILE_REQUIRED, addMobileNumber);
			((UpgradeActivity) getActivity()).replaceFragment(dataMobileRequired);
		}
	}
	
	public void hideWebView() {
		if (webView != null) {
			webView.setVisibility(View.GONE);
		}
	}
	// ======================================================
	// Adapter classes
	// ======================================================
	
	private static class ViewHolder {
		RelativeLayout layout;
		TextView planPeriodName;
		TextView planPrice;
		ProgressBar progressBar;
		Button subscribeButton;
		ImageButton toolTip;
	}
	
	private class PlansAdapter extends BaseAdapter {
		
		private List<Plan> mPlans;
		private LayoutInflater mInflater;
		
		public PlansAdapter(List<Plan> plans) {
			mPlans = plans;
			mInflater = (LayoutInflater) getActivity().
					getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}			
		
		@Override
		public int getCount() {
			return mPlans.size();
		}

		@Override
		public Object getItem(int position) {
			return mPlans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mPlans.get(position).getPlanId();
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
		
			final Plan plan = (Plan) getItem(position);
			if (plan.getPlanType() == PlanType.GOOGLE) {
				convertView = mInflater.inflate(R.layout.list_item_upgrade_plans_header_google, parent, false);
			} else if (plan.getPlanType() == PlanType.MOBILE) {
				convertView = mInflater.inflate(R.layout.list_item_upgrade_plans_header_mobile, parent, false);
			}
			ViewHolder viewHolder;
			// create view
			
			viewHolder = new ViewHolder();
			viewHolder.layout = (RelativeLayout) convertView.findViewById(R.id.upgrade_plans_row);
			
			viewHolder.planPeriodName = (TextView) convertView.findViewById(R.id.upgrade_period1);
			viewHolder.planPrice = (TextView) convertView.findViewById(R.id.upgrade_price1);
			viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar1);
			viewHolder.subscribeButton = (Button) convertView.findViewById(R.id.upgrade_button_subscribe1);
			viewHolder.toolTip = (ImageButton) convertView.findViewById(R.id.tool_tip);
			convertView.setTag(R.id.view_tag_view_holder, viewHolder);

			
			// populate the view from the Plan's list.
			// stores the object in the view.
			convertView.setTag(R.id.view_tag_object, plan);
			
			viewHolder.planPeriodName.setText(plan.getPlanName());
			if (plan.getPlanType() == PlanType.GOOGLE && plan.getSkudetails() != null) {
				viewHolder.planPrice.setText(plan.getSkudetails().getPrice());
				viewHolder.planPrice.setVisibility(View.VISIBLE);
				viewHolder.progressBar.setVisibility(View.GONE);				
			} else {
				
				if(!isBindToGoogle && plan.getPlanType() == PlanType.GOOGLE){
					viewHolder.planPrice.setVisibility(View.VISIBLE);
					viewHolder.progressBar.setVisibility(View.GONE);	
				}
				
				viewHolder.planPrice.setText(String.valueOf(plan.getPlanPrice()) + " " + plan.getPlanCurrency());
			}
			viewHolder.subscribeButton.setOnClickListener(new OnClickListener() {
				
//				private String AUTH_TOKEN_TYPE = "Manage your tasks";

				@Override
				public void onClick(View v) {
					// TODO Check user's balance
					clickedPLan = plan;
					mApplicationConfigurations.setTempClickedPlan(clickedPLan);
					if (plan.getPlanType() == PlanType.GOOGLE) {
						
						//##########################################################
						if(isBindToGoogle){
							
//							Toast.makeText(mContext, "Please authorize Hungama to check the status of and cancel user subscriptions", Toast.LENGTH_LONG).show();
//							
//							webView.setVisibility(View.VISIBLE);
//							webView.getSettings().setJavaScriptEnabled(true);
//							webView.setWebViewClient(new GoogleWebViewClient());
//							webView.loadUrl("https://accounts.google.com/o/oauth2/auth?scope=https://www.googleapis.com/auth/androidpublisher&response_type=code&access_type=offline&redirect_uri=http://api.hungama.com/hungama_app_auth/&client_id=556683560964.apps.googleusercontent.com");
//							isWebViewOpen = true;
							
							if (clickedPLan.getSkudetails() != null) {
								purchaseItem(clickedPLan.getSkudetails().getSku());
							} else {
								purchaseItem(mResources.getString(R.string.hungama_premium_subscription));	
							}
							
						}else{
							showDialog(
									getActivity().getString(R.string.google_wallet), 
									getActivity().getString(R.string.for_using_google_wallet_please_accept_terms_in_google_play));
						}
						
							
//						final AccountManager accountManager = AccountManager.get(mContext);
//						final Account[] accounts = accountManager.getAccountsByType("com.google");	
//						AsyncTask task = new AsyncTask() {
//
//							@Override
//							protected Void doInBackground(Object... params) {
//													
//								try {
//									String token = GoogleAuthUtil.getToken(mContext, accounts[0].name, "https://www.googleapis.com/auth/androidpublisher");
//									Log.i(TAG, "TOKEN : " + token);
//								    Toast.makeText(mContext, "TOKEN : " + token, Toast.LENGTH_LONG).show();
//								} catch (UserRecoverableAuthException e1) {
//									// TODO Auto-generated catch block
//									e1.printStackTrace();
//								} catch (IOException e1) {
//									// TODO Auto-generated catch block
//									e1.printStackTrace();
//								} catch (GoogleAuthException e1) {
//									// TODO Auto-generated catch block
//									e1.printStackTrace();
//								}
//								return null;
//							}
//					       };
//					       task.execute((Void)null);
//					       
//						Account accountToUse;
//						if (accounts.length >= 0) {							
//							AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//							builder.setTitle("Select a Google account");
//							//					      final Account[] accounts = accountManager.getAccountsByType("com.google");
//							final int size = accounts.length;
//							String[] names = new String[size];
//							for (int i = 0; i < size; i++) {
//								names[i] = accounts[i].name;
//							}
//							builder.setItems(names, new DialogInterface.OnClickListener() {
//								public void onClick(DialogInterface dialog, int which) {
//									// Stuff to do when the account is selected by the user
////									accountToUse = accounts[which];
//									accountManager.getAuthToken(accounts[which], AUTH_TOKEN_TYPE , null,
//											getActivity(), new AccountManagerCallback<Bundle>() {									
//											@Override
//											public void run(AccountManagerFuture<Bundle> future) {
//												try {
//												      // If the user has authorized your application to use the tasks API
//												      // a token is available.
//												      String token = future.getResult().getString(
//												        AccountManager.KEY_AUTHTOKEN);
//												      Log.i(TAG, "TOKEN : " + token);
//												      Toast.makeText(mContext, "TOKEN : " + token, Toast.LENGTH_LONG).show();
//												      // Now you can use the Tasks API...
////												      useTasksAPI(token);
//												      
//												    //##########################################################
//														if (plan.getSkudetails() != null) {
//															purchaseItem(plan.getSkudetails().getSku());
//														} else {
//															purchaseItem(mResources.getString(R.string.hungama_premium_subscription));	
//														}
//												      
//												    } catch (OperationCanceledException e) {
//												      // TODO: The user has denied you access to the API, you
//												      // should handle that
//												    } catch (Exception e) {
////												      handleException(e);
//												    }
//											}
//											}, null);
//								}
//							});
//							builder.create().show();
//
////							AccountManager.get(mContext).getAuthTokenByFeatures("com.google", AUTH_TOKEN_TYPE, features, activity, addAccountOptions, getAuthTokenOptions, callback, handler)
//							
//						}
						
						
													
					} else if (plan.getPlanType() == PlanType.MOBILE) {						
						startMobileVerificationFlow ();
					} 					
					
				}
			});
			
			viewHolder.toolTip.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (plan.getPlanType() == PlanType.GOOGLE) {
						openGoogleUpgradeToolTip(v);
					} else if (plan.getPlanType() == PlanType.MOBILE) {
						openMobileUpgradeToolTip(v);
					}
				}
			});

			return convertView;
		}
		
		public void openGoogleUpgradeToolTip(View view) {
			String title = getResources().getString(R.string.download_plans_google_wallet) + " " + getResources().getString(R.string.download_plans_google_wallet_subscription);
			String text = getResources().getString(R.string.upgrade_google_tool_tip);
			showToolTipDialog(title, text);
		}
		
		public void openMobileUpgradeToolTip(View view) {
			String title = getResources().getString(R.string.download_plans_mobile);
			String text = getResources().getString(R.string.upgrade_mobile_tool_tip);
			showToolTipDialog(title, text);
		}
		
		private void showToolTipDialog(String title, String text) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			 
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
					}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
		}
	}


	// ======================================================
	// onClick Method
	// ======================================================
	
	@Override
	public void onClick(View v) {
		
		int viewid = v.getId();		
		switch(viewid) {
			
			case (R.id.submit_mobile_number_button): {
				if (!TextUtils.isEmpty(mobileNumberField.getText().toString())) {
					// Verify mobile number
					if (mobileNumberField.getText().toString().length() == 10) {
						mobileToVerify = mobileNumberField.getText().toString();
						mDataManager.checkCountry(mobileToVerify, this);
					} else {
						Toast toast = Toast.makeText(((MainActivity) getActivity()).getApplicationContext(), getResources().getString(R.string.download_mobile_number_wrong_length), Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						ToastExpander.showFor(toast, TOAST_SHOW_DELAY);
					}
//					mDataManager.getMobileVerification(mobileNumberField.getText().toString(), null, MobileOperationType.MOBILE_VERIFY, this);
				}
				break;
			}
			
			case (R.id.upgrade_resend_sms_button_submit): {
				if (!TextUtils.isEmpty(mApplicationConfigurations.getUserLoginPhoneNumber())) {
					// Verify mobile number
					mDataManager.getMobileVerification(mApplicationConfigurations.getUserLoginPhoneNumber(), null, MobileOperationType.RESEND_PASSWORD, this);
				}
				break;
			}
			
			case (R.id.upgrade_verify_password_button_submit): {
				if (!TextUtils.isEmpty(passwordField.getText().toString())) {
					// Verify mobile number
					mDataManager.getMobileVerification(mApplicationConfigurations.getUserLoginPhoneNumber(), passwordField.getText().toString(), MobileOperationType.MOBILE_PASSWORD_VERIFY, this);
				}
				break;
			}
		}
		
	}


	/* (non-Javadoc)
	 * @see com.hungama.myplay.activity.util.billing.IabHelper.QueryInventoryFinishedListener#onQueryInventoryFinished(com.hungama.myplay.activity.util.billing.IabResult, com.hungama.myplay.activity.util.billing.Inventory)
	 */
	@Override
	public void onQueryInventoryFinished(IabResult result, Inventory inv) {
		if (result.isSuccess()) {
			
			for (Plan plan : plans) {
				if (plan.getPlanType() == PlanType.GOOGLE) {
					plan.setSkudetails(inv.getSkuDetails(mResources.getString(R.string.hungama_premium_subscription)));					
				}
			}
			
			if (plansAdapter != null) {
				plansAdapter.notifyDataSetChanged();
			}
			
		} else {
			Logger.i(TAG, "Failed Querying Inventory");
		}
		
	}


	/* (non-Javadoc)
	 * @see com.hungama.myplay.activity.util.billing.IabHelper.OnIabPurchaseFinishedListener#onIabPurchaseFinished(com.hungama.myplay.activity.util.billing.IabResult, com.hungama.myplay.activity.util.billing.Purchase)
	 */
	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		if (result.isFailure()) {
//			if (result.getResponse() == 7) {
//				mDataManager.getSubscriptionCharge(clickedPLan.getPlanId(), clickedPLan.getType(), SubscriptionType.CHARGE, UpgradeFragment.this);
//			}
			Logger.i(TAG, "Failed to Purchase Item");
        } else if (clickedPLan.getSkudetails().getSku().equals(info.getSku())) {
//        	String code = mApplicationConfigurations.getSubscriptionIABcode(); // not needed anymore
        	AccountManager accountManager = AccountManager.get(mContext);
			Account[] accounts = accountManager.getAccountsByType("com.google");
			mApplicationConfigurations.setSubscriptionIABpurchseToken(info.getToken());
			
			if(accounts != null && accounts.length > 0){
				String accountType = accounts[0].name; 

				mDataManager.getSubscriptionCharge(
						clickedPLan.getPlanId(), 
						clickedPLan.getType(), 
						SubscriptionType.CHARGE, 
						UpgradeFragment.this, 
						Utils.TEXT_EMPTY,
//						code, //empty text instead
						info.getToken(), 
						accountType);
			}
        }
	}


	/* (non-Javadoc)
	 * @see com.hungama.myplay.activity.util.billing.IabHelper.OnIabSetupFinishedListener#onIabSetupFinished(com.hungama.myplay.activity.util.billing.IabResult)
	 */
	@Override
	public void onIabSetupFinished(IabResult result) {
		if (result.isSuccess()) {
            Logger.i(TAG, "In-app Billing set up" + result);
            List<String> moreSkus = new ArrayList<String>();
            moreSkus.add(mResources.getString(R.string.hungama_premium_subscription));
            billingHelper.queryInventoryAsync(true, moreSkus, this);
        } else {
            Logger.i(TAG, "Problem setting up In-app Billing: " + result);
    		isBindToGoogle = false;
    		
    		if (plansAdapter != null) {
    			plansAdapter.notifyDataSetChanged();
    		}
        }
	}
	
	public void getActivityResult(int requestCode, int resultCode, Intent data) {
      billingHelper.handleActivityResult(requestCode, resultCode, data);
  }
	
	protected void purchaseItem(String sku) {
		billingHelper.launchPurchaseFlow(getActivity(), sku, PURCHASE_REQUEST_CODE, UpgradeFragment.this);
	}
	
	private void setupInAppBilling() {
        billingHelper = new IabHelper(TAG, mContext, getResources().getString(R.string.base_64_key));
        billingHelper.startSetup(this);
		
	}

	
	 public class GoogleWebViewClient extends WebViewClient{

//		  private boolean flag = false;

		@Override
		  public boolean shouldOverrideUrlLoading(WebView view, String url) {

//			if(flag) { 
//
//	            URL aURL;
//				try {
//					aURL = new URL(url);
//					URLConnection conn = aURL.openConnection(); 
//		            conn.connect(); 
//		            InputStream is = conn.getInputStream(); 
//		            // read inputstream to get the json..
//
//		            return true;
//		            
//				} catch (MalformedURLException e) {					
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				} 	            
//			}

			return false;
		  }

		  @Override
		  public void onPageFinished(WebView view, String urlStr) {
			  
		   if (urlStr.contains("?code=")) {
			   try {
				URL url = new URL(urlStr);
				String urlQuery = url.getQuery();
				String[] urlQueryArray = urlQuery.split("=");
				webView.setVisibility(View.GONE);
				isWebViewOpen = false;
				mApplicationConfigurations.setSubscriptionIABcode(urlQueryArray[1]);
				
				if (clickedPLan.getSkudetails() != null) {
					purchaseItem(clickedPLan.getSkudetails().getSku());
				} else {
					purchaseItem(mResources.getString(R.string.hungama_premium_subscription));	
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			   
//		        flag  = true;
		    } else if (urlStr.contains("?error=")) {
		    	webView.setVisibility(View.GONE);
				isWebViewOpen = false;
		    }
		 }
	 }


	@Override
	public void onIabFailedBindToService(boolean isBind) {
		isBindToGoogle = isBind;
		
		if (plansAdapter != null) {
			plansAdapter.notifyDataSetChanged();
		}
	}
	
	private void showDialog(String title, String text) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		 
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
				}
			});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
	}
}
