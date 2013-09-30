package com.hungama.myplay.activity.ui.fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
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
import com.hungama.myplay.activity.data.dao.hungama.PlanType;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.DownloadOperation;
import com.hungama.myplay.activity.operations.hungama.MobileVerifyCountryCheckOperation;
import com.hungama.myplay.activity.operations.hungama.MobileVerifyOperation;
import com.hungama.myplay.activity.services.DownloadFileService;
import com.hungama.myplay.activity.ui.DownloadActivity;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ToastExpander;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.billing.IabHelper;
import com.hungama.myplay.activity.util.billing.IabHelper.OnIabPurchaseFinishedListener;
import com.hungama.myplay.activity.util.billing.IabHelper.OnIabSetupFinishedListener;
import com.hungama.myplay.activity.util.billing.IabHelper.QueryInventoryFinishedListener;
import com.hungama.myplay.activity.util.billing.IabResult;
import com.hungama.myplay.activity.util.billing.Inventory;
import com.hungama.myplay.activity.util.billing.Purchase;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

public class DownloadFragment extends MainFragment implements CommunicationOperationListener, 
																OnClickListener,
																OnIabSetupFinishedListener,
																OnIabPurchaseFinishedListener,
																QueryInventoryFinishedListener{
	
	public static final String TAG = "DownloadFragment";
	
	
	public static final String ARGUMENT_MEDIA_ITEM = "argument_media_item";
	public static final String ARGUMENT_MOBILE_REQUIRED = "argument_mobile_required";
	public static final String ARGUMENT_MOBILE_VERIFICATION = "argument_mobile_verification";
	public static final String ARGUMENT_PLANS = "argument_plans";
	public static final String ARGUMENT_PLANS_TO_BUY = "argument_plans_to_buy";
	
	public static final int VALUE_PACK = 2;
	public static final int SINGLE_DOWNLOAD= 3;
	public static final int TOAST_SHOW_DELAY = 7*1000;
	
	public static final int MOBILE_NOT_VERIFIED = 0;
	public static final int MOBILE_VERIFIED = 1;
	public static final int MOBILE_NOT_EXIST = -1;
	
	public static final int POSITION_GOOGLE_PLANS = 0;
	public static final int POSITION_MOBILE_PLANS = 1;
	public static final int POSITION_REDEEM_PLANS = 2;
	
	private Bundle detailsData;
	private DataManager mDataManager;
	private DeviceConfigurations mDeviceConfigurations;
	private Context mContext;
	
	private EditText mobileNumberField;
	private EditText passwordField;
	private Dialog downloadDialog;
	private DownloadPlan clickedPLan;
	private MediaItem mMediaItem;
	private List<DownloadPlan> downloadPlans;
	private DownloadPlan downloadPlanRedeem;
	
	private ApplicationConfigurations mApplicationConfigurations;
	
//	private ImageFetcher mImageFetcher = null;
	
	private Button submitMobileNumber;
	
	public static String mobileToSend;
	private Resources mResources;
	
	// In App Billing
	private IabHelper billingHelper;
	public static final int PURCHASE_REQUEST_CODE = 123;
	
	private DownlaodPlansAdapter plansAdapter;
	private List<List<DownloadPlan>> downloadPlansArranged;

	private boolean isBindToGoogle = true;
	
	/**
	 * Registers a callback to be invoked when the user has selected an action upon a {@link MediaItem}.
	 * @param listener
	 */
	public void setOnMediaItemOptionSelectedListener(OnMediaItemOptionSelectedListener listener) {
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
		mDeviceConfigurations = mDataManager.getDeviceConfigurations();
		mContext = getActivity();
		mResources = getResources();
		// gets the media item from parent.
		Bundle data = getArguments();
		mMediaItem = (MediaItem) data.getSerializable(ARGUMENT_MEDIA_ITEM);
		downloadPlans = (List<DownloadPlan>) data.getSerializable(ARGUMENT_PLANS_TO_BUY);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = null;
		detailsData = getArguments();
		if (detailsData.getString(ARGUMENT_MOBILE_REQUIRED) != null) {
			rootView = inflater.inflate(R.layout.fragment_upgrade_mobile_required, container, false);
			initializeMobileRquiredControls(rootView);
		} else if (detailsData.getString(ARGUMENT_MOBILE_VERIFICATION) != null) {
			rootView = inflater.inflate(R.layout.fragment_upgrade_mobile_verification, container, false);
			initializeMobileVerificationControls(rootView);
		} else if (detailsData.getString(ARGUMENT_PLANS) != null) {
			rootView = inflater.inflate(R.layout.fragment_download_plans, container, false);
			initializeMobileUpgradePlansControls(rootView);
		}
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (submitMobileNumber != null && !submitMobileNumber.isEnabled()) {
			submitMobileNumber.setEnabled(true);
		}
		if (detailsData.getString(ARGUMENT_PLANS) != null) {
//			if (mImageFetcher == null) {
				// creates the image loader.
				
				// creates the cache.
				ImageCache.ImageCacheParams cacheParams =
		                new ImageCache.ImageCacheParams(getActivity(), DataManager.FOLDER_TILES_CACHE);
		        cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);
	
				// gets the image and its size.
				ImageView thumbNail = (ImageView) getView()
							.findViewById(R.id.search_result_media_image);
				int imageSize = Math.min(thumbNail.getMeasuredHeight(), thumbNail.getMeasuredWidth());
				
//				mImageFetcher = new ImageFetcher(getActivity(), imageSize);
//				mImageFetcher.addImageCache(getFragmentManager(), cacheParams);
//		        mImageFetcher.setImageFadeIn(false);
		        
//		        mImageFetcher.loadImage(mMediaItem.getImageUrl(), thumbNail);
				Picasso.with(mContext).cancelRequest(thumbNail);
		        if (getActivity() != null && mMediaItem != null && !TextUtils.isEmpty(mMediaItem.getImageUrl())) {
		        	Picasso.with(getActivity()).load(mMediaItem.getImageUrl()).into(thumbNail);
		        }
		        
//			} else {
//				// refreshes the cache of the image.
//				mImageFetcher.setExitTasksEarly(false);
//				
//			}
		}
	
	}
	
	@Override
	public void onPause() {
		super.onPause();
//		if (mImageFetcher != null) {
//			mImageFetcher.setExitTasksEarly(true);
//	        mImageFetcher.flushCache();
//		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
//		if (mImageFetcher != null) {
//			mImageFetcher.closeCache();
//			mImageFetcher = null;
//		}
	}

	
	// ======================================================
	// Operation callback methods
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		switch (operationId) {
			case OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION:
			case OperationDefinition.Hungama.OperationId.DOWNLOAD:
			case OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION_COUNTRY_CHECK:
				showLoadingDialog(R.string.application_dialog_loading_content);
				break;
		}
	}
	
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		
		if (operationId == OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION_COUNTRY_CHECK) {
			MobileVerificationCountryCheckResponse countryCheckResponse = 
					(MobileVerificationCountryCheckResponse) responseObjects.get(MobileVerifyCountryCheckOperation.RESPONSE_KEY_MOBILE_VERIFICATION_COUNTRY_CHECK);
			if (countryCheckResponse.isIndia()) {				
				mDataManager.getMobileVerification(mobileToSend, null, MobileOperationType.MOBILE_VERIFY, this);
			} else {
				Toast toast = Toast.makeText(((DownloadActivity) getActivity()).getApplicationContext(), getResources().getString(R.string.download_country_check_not_india), Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				ToastExpander.showFor(toast, TOAST_SHOW_DELAY);
			}
			hideLoadingDialog();
		
		} else if (operationId == OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION) {
			MobileVerificationResponse mobileVerificationResponse = (MobileVerificationResponse) responseObjects.get(MobileVerifyOperation.RESPONSE_KEY_MOBILE_VERIFICATION);
			
			if (mobileVerificationResponse.getMobileOperationType() == MobileOperationType.MOBILE_VERIFY) {
				if (mobileVerificationResponse.getCode().equalsIgnoreCase(DownloadActivity.PASSWORD_SMS_SENT)) {
					//save number in appConfiguration.
					if (mobileToSend != null) {
						mApplicationConfigurations.setUserLoginPhoneNumber(mobileToSend);
						int isVerified = mApplicationConfigurations.isMobileNumberVerified(mobileToSend);
						if (isVerified == MOBILE_NOT_VERIFIED || isVerified == MOBILE_NOT_EXIST ) {
							mApplicationConfigurations.setMobileNumber(mobileToSend, MOBILE_NOT_VERIFIED);
						}
					}					
					Bundle data = new Bundle();
					String verifyMobileNumber = "fragment_upgrade_mobile_verification";
					data.putSerializable(ARGUMENT_MEDIA_ITEM, (Serializable)mMediaItem);
					data.putString(ARGUMENT_MOBILE_VERIFICATION, verifyMobileNumber);
					((DownloadActivity) getActivity()).replaceFragment(data);
//					DownloadActivity.mobileToSend = mobileNumberField.getText().toString();
				} else if (mobileVerificationResponse.getCode().equalsIgnoreCase(DownloadActivity.MSISDN_ALREADY_EXIST_AND_VERIFIED)) {
					if (mobileToSend != null) {
						mApplicationConfigurations.setUserLoginPhoneNumber(mobileToSend);
						int isVerified = mApplicationConfigurations.isMobileNumberVerified(mobileToSend);
						if (isVerified == MOBILE_NOT_VERIFIED || isVerified == MOBILE_NOT_EXIST ) {
							mApplicationConfigurations.setMobileNumber(mobileToSend, MOBILE_VERIFIED);
						}
					}
					mDataManager.getDownload(clickedPLan.getPlanId(), mMediaItem.getId(), mobileToSend, 
							clickedPLan.getType(), DownloadOperationType.BUY_CHARGE, DownloadFragment.this);
//					mDataManager.getDownload(0, mMediaItem.getId(), null, null, DownloadOperationType.BUY_PLANS, this);
//					mDataManager.getDownload(0, mMediaItem.getId(), null, "", DownloadOperationType.DOWNLOAD_COUNT, this);
				} else {
					//Toast.makeText(((DownloadActivity) getActivity()).getApplicationContext(), mobileVerificationResponse.getMessage() + getResources().getString(R.string.upgrade_mobile_number_verification_failed), TOAST_SHOW_DELAY).show();
					Bundle data = new Bundle();
					String addMobileNumber = "fragment_upgrade_mobile_required";
					data.putSerializable(ARGUMENT_MEDIA_ITEM, (Serializable)mMediaItem);
					data.putString(ARGUMENT_MOBILE_REQUIRED, addMobileNumber);
					((DownloadActivity) getActivity()).replaceFragment(data);
				}
				hideLoadingDialog();
				
			} else if (mobileVerificationResponse.getMobileOperationType() == MobileOperationType.MOBILE_PASSWORD_VERIFY) {
				if (mobileVerificationResponse.getCode().equalsIgnoreCase(DownloadActivity.PASSWORD_SMS_SENT)) { // If Password Verified Successfully
					
					Toast.makeText(((DownloadActivity) getActivity()).getApplicationContext(), R.string.upgrade_mobile_number_verification_success2, TOAST_SHOW_DELAY).show();
					
					int isVerified = mApplicationConfigurations.isMobileNumberVerified(mobileToSend);
					if (isVerified == MOBILE_NOT_VERIFIED || isVerified == MOBILE_NOT_EXIST ) {
						mApplicationConfigurations.setMobileNumber(mobileToSend, MOBILE_VERIFIED);
					}
					
					DownloadPlan clickedPlanTemp = mApplicationConfigurations.getTempClickedDownloadPlan();
					
					mDataManager.getDownload(clickedPlanTemp.getPlanId(), mMediaItem.getId(), mobileToSend, 
							clickedPlanTemp.getType(), DownloadOperationType.BUY_CHARGE, DownloadFragment.this);
//					mDataManager.getDownload(0, mMediaItem.getId(), null, null, DownloadOperationType.BUY_PLANS, this);
					
//					Bundle data = new Bundle();
//					String showDownloadPlans = "fragment_download_plans";
//					data.putSerializable(ARGUMENT_MEDIA_ITEM, (Serializable)mMediaItem);
//					data.putSerializable(ARGUMENT_PLANS_TO_BUY, (Serializable)downloadPlans);
//					data.putString(ARGUMENT_PLANS, showDownloadPlans);
//					((DownloadActivity) getActivity()).replaceFragment(data);
//					
//					Bundle data = new Bundle();
//					String downloadPLans = "fragment_d_plans";
//					data.putSerializable(ARGUMENT_MEDIA_ITEM, (Serializable)mMediaItem);
//					data.putString(ARGUMENT_MOBILE_VERIFICATION, verifyMobileNumber);
//					((DownloadActivity) getActivity()).replaceFragment(data);
////					mDataManager.getDownload(0, mMediaItem.getId(), null, null, DownloadOperationType.DOWNLOAD_COUNT, this);
////					Bundle data = new Bundle();
////					String upgradePlans = "fragment_upgrade_plans";
////					data.putString(DownloadFragment.ARGUMENT_PLANS, upgradePlans);
////					((DownloadActivity) getActivity()).replaceFragment(data);
				} else {
//					Toast.makeText(((DownloadActivity) getActivity()).getApplicationContext(), mobileVerificationResponse.getMessage() + getResources().getString(R.string.upgrade_mobile_number_verification_failed), TOAST_SHOW_DELAY).show();
//					Bundle data = new Bundle();
//					String upgradePlans = "fragment_upgrade_plans";
//					data.putString(UpgradeFragment.ARGUMENT_PLANS, upgradePlans);
//					((DownloadActivity) getActivity()).replaceFragment(data);
				}
				hideLoadingDialog();
			
			} else if (mobileVerificationResponse.getMobileOperationType() == MobileOperationType.RESEND_PASSWORD) {
				hideLoadingDialog();
				
				if (mobileVerificationResponse.getCode().equalsIgnoreCase(DownloadActivity.PASSWORD_SMS_SENT)) { // If Password Verified Successfully
					Toast.makeText(((DownloadActivity) getActivity()).getApplicationContext(), mobileVerificationResponse.getMessage(), TOAST_SHOW_DELAY).show();
				} else {
					Toast.makeText(((DownloadActivity) getActivity()).getApplicationContext(), mobileVerificationResponse.getMessage() + getResources().getString(R.string.upgrade_mobile_number_verification_failed), TOAST_SHOW_DELAY).show();					
				}
				
			}
			
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.DOWNLOAD) {
			DownloadResponse downloadResponse = (DownloadResponse) responseObjects.get(DownloadOperation.RESPONSE_KEY_DOWNLOAD);
			if (downloadResponse != null) {
				
				if (downloadResponse.getDownloadType() == DownloadOperationType.DOWNLOAD_COUNT) {																	
					if (downloadResponse.getRemainingDownloadCount() > 0) {						
						mDataManager.getDownload(downloadResponse.getPlanId(), mMediaItem.getId(), DownloadActivity.mobileToSend, null, DownloadOperationType.BUY_CHARGE, this);
					} else if (downloadResponse.getBalanceCreditLimit() > 0) {
						mDataManager.getDownload(downloadResponse.getPlanId(), mMediaItem.getId(), DownloadActivity.mobileToSend, null, DownloadOperationType.BUY_CHARGE, this);
						} else {
							mDataManager.getDownload(0, mMediaItem.getId(), null, null, DownloadOperationType.BUY_PLANS, this);
						}
					
				}else if (downloadResponse.getDownloadType() == DownloadOperationType.BUY_PLANS) {
					
					if (downloadResponse.getCode().equalsIgnoreCase(DownloadActivity.PASSWORD_SMS_SENT)) {	
						List<DownloadPlan> downloadPlans = new ArrayList<DownloadPlan>();
						
//						for (int i = 0; i < downloadResponse.getPlan().size(); i++)  {
//							
//							DownloadPlan dp = new DownloadPlan(downloadResponse.getPlan().get(i).getPlanId(), downloadResponse.getPlan().get(i).getPlanName(), 
//									downloadResponse.getPlan().get(i).getPlanCurrency(), downloadResponse.getPlan().get(i).getPlanPrice(), downloadResponse.getPlan().get(i).getMsisdn(), 
//									downloadResponse.getPlan().get(i).getStatus(), downloadResponse.getPlan().get(i).getCreditBalance());
//							
//							downloadPlans.add(dp);
//
//						}
						
						downloadPlans = downloadResponse.getPlan();
						if (downloadPlans != null) {							
							Bundle data = new Bundle();
							String showDownloadPlans = "fragment_download_plans";
							data.putSerializable(ARGUMENT_MEDIA_ITEM, (Serializable)mMediaItem);
							data.putSerializable(ARGUMENT_PLANS_TO_BUY, (Serializable)downloadPlans);
							data.putString(ARGUMENT_PLANS, showDownloadPlans);
							((DownloadActivity) getActivity()).replaceFragment(data);
						}
					}
					
//					List<DownloadPlan> plans = downloadResponse.getPlan();
//					if (plans != null) {
//						ListView mPlan = (ListView) getView().findViewById(R.id.listview_upgrade_plans);
//						DownlaodPlansAdapter plansAdapter = new DownlaodPlansAdapter(getActivity(), plans);	
//						mPlan.setAdapter(plansAdapter);
//					}
					
				} else if (downloadResponse.getDownloadType() == DownloadOperationType.BUY_CHARGE) {
					if (downloadResponse.getCode().equalsIgnoreCase(DownloadActivity.PASSWORD_SMS_SENT)) {
//						Toast.makeText(((DownloadActivity) getActivity()).getApplicationContext(), subscriptionResponse.getMessage(), TOAST_SHOW_DELAY).show();
						
						// set the content type to be sent with buy_charge
						String contentType = null;
						if (!TextUtils.isEmpty(mMediaItem.getMediaType().toString())) {
							if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO || mMediaItem.getMediaType() == MediaType.VIDEO) {
								contentType = DownloadActivity.CONTENT_TYPE_VIDEO;
							} else if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC || mMediaItem.getMediaType() == MediaType.TRACK) {
								contentType = DownloadActivity.CONTENT_TYPE_AUDIO;
							}
						}
						mDataManager.getDownload(0, mMediaItem.getId(), null, contentType, DownloadOperationType.CONTENT_DELIVERY, this);
						String title = getResources().getString(R.string.download_media_success_title);
						String body = getResources().getString(R.string.download_media_success_body);
						showDownloadDialog(title, body, false, true);
//						List<DownloadPlan> subscribedPlan = new ArrayList<DownloadPlan>();
//						subscribedPlan.set(0, clickedPLan);
//						downloadResponse.setPlan(subscribedPlan);
//						mDataManager.storeCurrentSubscriptionPLan(downloadResponse);
					} else {
						Logger.i(TAG, downloadResponse.getMessage());
						if (downloadResponse.getDisplay().equalsIgnoreCase(DownloadActivity.PASSWORD_SMS_SENT)) {
							String title = getResources().getString(R.string.download_unsuccessful_title);
							String body = downloadResponse.getMessage();
							showDownloadDialog(title, body, false, false);
						} else {
							((DownloadActivity) getActivity()).finish();
						}
//						Toast.makeText(((DownloadActivity) getActivity()).getApplicationContext(), downloadResponse.getMessage(), TOAST_SHOW_DELAY).show();						
					}
					
				} else if (downloadResponse.getDownloadType() == DownloadOperationType.CONTENT_DELIVERY) {
//					Toast.makeText(((DownloadActivity) getActivity()).getApplicationContext(), downloadResponse.getMessage(), TOAST_SHOW_DELAY).show();
					
					if (downloadResponse.getCode().equalsIgnoreCase(DownloadActivity.PASSWORD_SMS_SENT) && !downloadResponse.getUrl().equalsIgnoreCase(Utils.TEXT_EMPTY)) {
						
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
						
						Intent intent = new Intent(getActivity().getApplicationContext(), DownloadFileService.class);
						intent.putExtra(DownloadFileService.TRACK_KEY, (Serializable) mMediaItem);
						intent.putExtra(DownloadFileService.DOWNLOAD_URL, downloadResponse.getUrl());
						getActivity().startService(intent);
						
//						DownloadFile downloadFile = new DownloadFile(downloadResponse.getUrl());
//						downloadFile.execute();
					} else {
						Toast.makeText(((DownloadActivity) getActivity()).getApplicationContext(), "Error", TOAST_SHOW_DELAY).show();
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
		} else if (operationId == OperationDefinition.Hungama.OperationId.DOWNLOAD) {
				Logger.i(TAG, "Failed loading download");		
		} else if (operationId == OperationDefinition.Hungama.OperationId.MOBILE_VERIFICATION_COUNTRY_CHECK) {
			Logger.i(TAG, "Failed loading country check");
		}
		hideLoadingDialog();
	}
	
	
	// ======================================================
	// Private helper methods.
	// ======================================================
	
	private void initializeMobileRquiredControls(final View rootView) {
		
		submitMobileNumber = (Button) rootView.findViewById(R.id.submit_mobile_number_button);
		submitMobileNumber.setOnClickListener(this);
		
		mobileNumberField = (EditText) rootView.findViewById(R.id.mobile_number_field);
		// sets the text based 
		TextView title = (TextView) rootView.findViewById(R.id.login_forgot_password_test_message);
		if (mMediaItem != null && 
				mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			title.setText(R.string.upgrade_mobile_number_required_videos);
		}
	}
		
	private void initializeMobileVerificationControls(final View rootView) {
		
		Button resendSms = (Button) rootView.findViewById(R.id.upgrade_resend_sms_button_submit);
		resendSms.setOnClickListener(this);
		
		Button submitMobileNumber = (Button) rootView.findViewById(R.id.upgrade_verify_password_button_submit);
		submitMobileNumber.setOnClickListener(this);
		
		passwordField = (EditText) rootView.findViewById(R.id.upgrade_password_verification_field);
	}
		
	private void initializeMobileUpgradePlansControls(final View rootView) {
		
		setupInAppBilling();
		
//		mDataManager.getDownload(planId, contentId, contentType, downloadOperationType, listener);
		TextView songTitle = (TextView) rootView.findViewById(R.id.search_result_line_top_text);		
		songTitle.setText(mMediaItem.getTitle());
		
		ImageView typeImage = (ImageView) rootView.findViewById(R.id.search_result_media_image_type);		
		TextView TypeAndName = (TextView) rootView.findViewById(R.id.search_result_text_media_type_and_name);
		String bottomRowText;
		// Set Image Type and Text Below title By Type
		
		if (mMediaItem.getMediaType() == MediaType.VIDEO || mMediaItem.getMediaContentType() == MediaContentType.VIDEO){
			typeImage.setBackgroundResource(R.drawable.icon_main_settings_videos);
			bottomRowText = getResources().getString(R.string.search_results_layout_bottom_text_for_video) + " - " + String.valueOf(mMediaItem.getAlbumName());	
			TypeAndName.setText(bottomRowText);
		} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
			typeImage.setBackgroundResource(R.drawable.icon_main_settings_music);
			bottomRowText = getResources().getString(R.string.search_results_layout_bottom_text_for_track) + " - " + mMediaItem.getAlbumName();	
			TypeAndName.setText(bottomRowText);		
		}
		
		ListView mPlan = (ListView) rootView.findViewById(R.id.listview_upgrade_plans);
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
		
	    if (!Utils.isListEmpty(downloadPlans) ) {
			int removeLocation = -1;
			View header = null;
			for (int i=0; i<downloadPlans.size(); i++ ) {
				if (downloadPlans.get(i).getRedeem() == 1) {
					header = inflater.inflate(R.layout.list_item_download_plans_header_redeem, null);
					downloadPlanRedeem = downloadPlans.get(i);
					removeLocation = i;
					break;
				}
			}
			if (header == null) {
				header = inflater.inflate(R.layout.fragment_download_plans_header_no_redeem, null);
			} else {
				initializeRedeemPLanView(header);
			}
			if (removeLocation != -1) {
				downloadPlans.remove(removeLocation);
			}
			View footer = inflater.inflate(R.layout.fragment_download_plans_footer, null);
//			mPlan.addHeaderView(header);
			mPlan.addFooterView(footer);

			downloadPlansArranged = new ArrayList<List<DownloadPlan>>();
			List<DownloadPlan> googleList = null;
			List<DownloadPlan> mobileList = null;
			List<DownloadPlan> redeemList = null;
//			downloadPlansArranged.add(POSITION_GOOGLE_PLANS, new ArrayList<DownloadPlan>());
//			downloadPlansArranged.add(POSITION_MOBILE_PLANS, new ArrayList<DownloadPlan>());
//			downloadPlansArranged.add(POSITION_REDEEM_PLANS, new ArrayList<DownloadPlan>());
			
			for (DownloadPlan downloadPlan : downloadPlans) {
				
				if (downloadPlan.getPlanType() == PlanType.GOOGLE) {
				
					if (Utils.isListEmpty(googleList)){
						googleList = new ArrayList<DownloadPlan>();
						googleList.add(downloadPlan);
						downloadPlansArranged.add(googleList);
					} else {
						googleList.add(downloadPlan);
					}
//					downloadPlansArranged.get(POSITION_GOOGLE_PLANS).add(downloadPlan);
					
				} else if (downloadPlan.getPlanType() == PlanType.MOBILE) {
					if (Utils.isListEmpty(mobileList)){
						mobileList = new ArrayList<DownloadPlan>();
						mobileList.add(downloadPlan);
						downloadPlansArranged.add(mobileList);
					} else {
						mobileList.add(downloadPlan);
					}
				} else if (downloadPlan.getPlanType() == PlanType.REEDEM) {
					if (Utils.isListEmpty(redeemList)){
						redeemList = new ArrayList<DownloadPlan>();
						redeemList.add(downloadPlan);
						downloadPlansArranged.add(redeemList);
					} else {
						redeemList.add(downloadPlan);
					}
				}
			}
			
			plansAdapter = new DownlaodPlansAdapter(getActivity(), downloadPlansArranged);
			mPlan.setAdapter(plansAdapter);
		}
		
		// if The list of plans contains a coins redeem plan
		

	}
	
	private void initializeRedeemPLanView(View header) {
		if (downloadPlanRedeem != null) {
			LinearLayout redeemCoinsPanel = (LinearLayout) header.findViewById(R.id.download_plans_redeem_panel);
			TextView planPeriodName = (TextView) header.findViewById(R.id.upgrade_period);
			TextView planPrice = (TextView) header.findViewById(R.id.upgrade_price);
			Button subscribeButton = (Button) header.findViewById(R.id.upgrade_button_subscribe);
			
			planPeriodName.setText(downloadPlanRedeem.getPlanName());		
			planPrice.setText(String.valueOf(downloadPlanRedeem.getPlanPrice()));
			
			redeemCoinsPanel.setVisibility(View.VISIBLE);
			subscribeButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Check user's balance
					clickedPLan = downloadPlanRedeem;
					mDataManager.getDownload(downloadPlanRedeem.getPlanId(), mMediaItem.getId(), DownloadActivity.mobileToSend, "redeem", DownloadOperationType.BUY_CHARGE, DownloadFragment.this);
					
				}
			});
		}
	}
	
	public void showDownloadDialog(String header, String body, boolean isImageVisible, final boolean isSuccess) {
		//set up custom dialog
        downloadDialog = new Dialog(getActivity());
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
				if (isSuccess) {
					((DownloadActivity)getActivity()).finish();
				}
				 
				
			}
		});
        downloadDialog.setCancelable(true);
        downloadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				downloadDialog.dismiss();
				getFragmentManager().popBackStack();
				((DownloadActivity)getActivity()).finish(); 
				
			}
		});
        downloadDialog.show();

	}
	
	private void setupInAppBilling() {
        billingHelper = new IabHelper(TAG, mContext, getResources().getString(R.string.base_64_key));
        billingHelper.enableDebugLogging(true);
        billingHelper.startSetup(this);
       
//        billingHelper.startSetup(new OnIabSetupFinishedListener() {
//			
//			@Override
//			public void onIabSetupFinished(IabResult result) {
//				System.out.println("");
//			}
//		});
	}

	/**
	 * Method to invoke mobile_verify, if a mobile number was entered by user when login
	 */
	public void checkUserLoginNumber() {
		if (!mApplicationConfigurations.getUserLoginPhoneNumber().equalsIgnoreCase("")) {
			// check if mobile number already verified
			// if yes - 
			// if not - mobile verify
			mobileToSend = mApplicationConfigurations.getUserLoginPhoneNumber();
			int isVerified = mApplicationConfigurations.isMobileNumberVerified(mApplicationConfigurations.getUserLoginPhoneNumber());
			if (isVerified == MOBILE_VERIFIED) {
				// call buy_charge
				mDataManager.getDownload(clickedPLan.getPlanId(), mMediaItem.getId(), mobileToSend, 
						clickedPLan.getType(), DownloadOperationType.BUY_CHARGE, DownloadFragment.this);
//				// get url for downloading
//				String contentType = null;
//				if (!TextUtils.isEmpty(mMediaItem.getMediaType().toString())) {
//					if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO || mMediaItem.getMediaType() == MediaType.VIDEO) {
//						contentType = DownloadActivity.CONTENT_TYPE_VIDEO;
//					} else if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC || mMediaItem.getMediaType() == MediaType.TRACK) {
//						contentType = DownloadActivity.CONTENT_TYPE_AUDIO;
//					}
//				}
//				mDataManager.getDownload(0, mMediaItem.getId(), null, contentType, DownloadOperationType.CONTENT_DELIVERY, this);
			} else {
				mDataManager.checkCountry(mobileToSend, this);
//				mDataManager.getDownload(clickedPLan.getPlanId(), mMediaItem.getId(), DownloadActivity.mobileToSend, 
//						clickedPLan.getType(), DownloadOperationType.BUY_CHARGE, DownloadFragment.this);
			}		
		} else {
			Bundle dataMobileRequired = new Bundle();
			String addMobileNumber = "fragment_upgrade_mobile_required";
			dataMobileRequired.putSerializable(DownloadFragment.ARGUMENT_MEDIA_ITEM, (Serializable)mMediaItem);
			dataMobileRequired.putString(DownloadFragment.ARGUMENT_MOBILE_REQUIRED, addMobileNumber);
			((DownloadActivity) getActivity()).replaceFragment(dataMobileRequired);
		}
	}
	
	private void toggleBuyButtons() {
		
		if (downloadPlansArranged != null) {
			for (List<DownloadPlan> plansListByType : downloadPlansArranged) {
				for (DownloadPlan plan : plansListByType) {
					if (plan.isBuyButtonClickable()) {
						plan.setBuyButtonClickable(false);
					} else {
						plan.setBuyButtonClickable(true);
					}					
				}
//			if (viewHolder.subscribeButton1.isClickable()) {
//				viewHolder.subscribeButton1.setClickable(false);
//			} else {
//				viewHolder.subscribeButton1.setClickable(true);
//			}
//			
//			if (viewHolder.subscribeButton2 != null) {
//				viewHolder.subscribeButton2.setClickable(true);
//			}
			}
			
			if (plansAdapter != null) {
				plansAdapter.notifyDataSetChanged();
			}
		}
	}
	
	private static class ViewHolder {
		RelativeLayout layout1;
		TextView planPeriodName1;
		TextView planPrice1;
		ProgressBar progressBar1;
		Button subscribeButton1;
		ImageView toolTip;
		
		RelativeLayout layout2;
		TextView planPeriodName2;
		TextView planPrice2;
		ProgressBar progressBar2;
		Button subscribeButton2;
	}
	
	public class DownlaodPlansAdapter extends BaseAdapter {
		
		private List<List<DownloadPlan>> mDownloadPlans;
		private LayoutInflater mInflater;
		private Context mContext;
		private Activity mActivity;
		
		public DownlaodPlansAdapter(Activity activity, List<List<DownloadPlan>> downloadPlans) {
			mDownloadPlans = downloadPlans;
			mActivity = activity;
			mContext = mActivity.getApplicationContext();
			
			mContext.getResources();
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			mInflater = (LayoutInflater) mContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}			
		
		@Override
		public int getCount() {
			return mDownloadPlans.size();
		}

		@Override
		public Object getItem(int position) {
			return mDownloadPlans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mDownloadPlans.get(position).get(position).getPlanId();
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
		
			List<DownloadPlan> downloadPlansByType = (List<DownloadPlan>) getItem(position);
			//new
			if (downloadPlansByType.get(0).getPlanType() == PlanType.GOOGLE) {
				convertView = getViewGooglePlans(position, convertView, parent, (List<DownloadPlan>)getItem(position));
			} else if (downloadPlansByType.get(0).getPlanType() == PlanType.MOBILE) {
				convertView = getViewMobilePlans(position, convertView, parent, (List<DownloadPlan>)getItem(position));
			} else if (downloadPlansByType.get(0).getPlanType() == PlanType.REEDEM) {
				convertView = getViewRedeemPlans(position, convertView, parent, (List<DownloadPlan>)getItem(position));
			}
			//end new
			
			return convertView;
		}
		
		private View getViewGooglePlans(int position, View convertView, ViewGroup parent, List<DownloadPlan> googlePlans) {
			convertView = createViewItem(position, convertView, parent, googlePlans);
			return convertView;
		}
		
		private View getViewMobilePlans(int position, View convertView, ViewGroup parent, List<DownloadPlan> mobilePlans) {
			convertView = createViewItem(position, convertView, parent, mobilePlans);
			return convertView;
		}

		private View getViewRedeemPlans(int position, View convertView, ViewGroup parent, List<DownloadPlan> redeemPlans) {
			convertView = createViewItem(position, convertView, parent, redeemPlans);
			return convertView;
		}
		
		private View createViewItem(int position, View convertView, ViewGroup parent, List<DownloadPlan> plans) {


			if (plans.get(0).getPlanType() == PlanType.GOOGLE) {
				convertView = mInflater.inflate(R.layout.list_item_download_plans_header_google, parent, false);
			} else if (plans.get(0).getPlanType() == PlanType.MOBILE) {
				convertView = mInflater.inflate(R.layout.list_item_download_plans_header_mobile, parent, false);
			} else if (plans.get(0).getPlanType() == PlanType.REEDEM) {
				convertView = mInflater.inflate(R.layout.list_item_download_plans_header_redeem, parent, false);
			}
			
			populateOnePlanLayout(convertView, plans);
							
			return convertView;
		}

		// ======================================================
		// Helper Methods
		// ======================================================
		
		private void populateOnePlanLayout(View convertView, List<DownloadPlan> plans) {
			ViewHolder viewHolder;
			viewHolder = new ViewHolder();
			
			viewHolder.layout1 = (RelativeLayout) convertView.findViewById(R.id.upgrade_plans_row);			
			viewHolder.planPeriodName1 = (TextView) convertView.findViewById(R.id.upgrade_period1);
			viewHolder.planPrice1 = (TextView) convertView.findViewById(R.id.upgrade_price1);			
			viewHolder.subscribeButton1 = (Button) convertView.findViewById(R.id.upgrade_button_subscribe1);
			viewHolder.toolTip = (ImageButton) convertView.findViewById(R.id.tool_tip);
//			viewHolder.subscribeButton1.setText(mContext.getString(R.string.download_plan_button_buy));
			convertView.setTag(R.id.view_tag_view_holder, viewHolder);

		
		// populate the view from the Plan's list.
			final DownloadPlan downloadPlan1 = plans.get(0);	
			
			if (downloadPlan1.getPlanType() == PlanType.GOOGLE) {
				viewHolder.progressBar1 = (ProgressBar) convertView.findViewById(R.id.progressBar1); 
			}
			convertView.setTag(R.id.view_tag_object, downloadPlan1);
			viewHolder.planPeriodName1.setText(downloadPlan1.getPlanName());
		
		// stores the object in the view.
			convertView.setTag(R.id.view_tag_object, downloadPlan1);
			
			if (downloadPlan1.getPlanType() == PlanType.GOOGLE && downloadPlan1.getSkudetails() != null) {
				viewHolder.planPrice1.setText(downloadPlan1.getSkudetails().getPrice());
				viewHolder.planPrice1.setVisibility(View.VISIBLE);
				viewHolder.progressBar1.setVisibility(View.GONE);				
			} else {
				
				if(!isBindToGoogle && downloadPlan1.getPlanType() == PlanType.GOOGLE){
					viewHolder.planPrice1.setVisibility(View.VISIBLE);
					viewHolder.progressBar1.setVisibility(View.GONE);	
				}
				
				viewHolder.planPrice1.setText(String.valueOf(downloadPlan1.getPlanPrice()) + " " + downloadPlan1.getPlanCurrency());
			}
			viewHolder.planPeriodName1.setText(downloadPlan1.getPlanName());
			viewHolder.subscribeButton1.setClickable(downloadPlan1.isBuyButtonClickable());
			viewHolder.subscribeButton1.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//Check If User Is Logged In
						ApplicationConfigurations applicationConfigurations = mDataManager.getApplicationConfigurations();
						String sesion = applicationConfigurations.getSessionID();
						boolean isRealUser = applicationConfigurations.isRealUser();
						
						if (!TextUtils.isEmpty(sesion) && isRealUser) {
							clickedPLan = downloadPlan1;
							((Button) v).setClickable(false);  
							toggleBuyButtons();
							if (downloadPlan1.getPlanType() == PlanType.GOOGLE) {
								
								if(isBindToGoogle){
									purchaseItem(clickedPLan.getSkudetails().getSku());									
								}else{
									showDialog(
											getActivity().getString(R.string.google_wallet), 
											getActivity().getString(R.string.for_using_google_wallet_please_accept_terms_in_google_play));
								}
								
							} else if (downloadPlan1.getPlanType() == PlanType.MOBILE) {
								mApplicationConfigurations.setTempClickedDownloadPlan(clickedPLan);
								String msisdn =  mDeviceConfigurations.getDevicePhoneNumber();
								 /*Check if device mobile number(msisdn) is available. 
								 		yes - verify it.  
										no - check if user logged in with a number
													yes - verify it.
													no - ask user to enter a number. (add fragment) */
								if (msisdn != null && !msisdn.equalsIgnoreCase("")) {
									mobileToSend = msisdn;
									mDataManager.getMobileVerification(msisdn, null, MobileOperationType.MOBILE_VERIFY, DownloadFragment.this);
								} else {								
									checkUserLoginNumber();
								}
							} else if (downloadPlan1.getPlanType() == PlanType.REEDEM) {
								mDataManager.getDownload(clickedPLan.getPlanId(), mMediaItem.getId(), mobileToSend, 
										"redeem", DownloadOperationType.BUY_CHARGE, DownloadFragment.this);
							}
	//						mDataManager.getDownload(downloadPlan1.getPlanId(), mMediaItem.getId(), DownloadActivity.mobileToSend, null, DownloadOperationType.BUY_CHARGE, DownloadFragment.this);
						} else {
							Toast.makeText(DownloadFragment.this.mContext, getResources().getString(R.string.download_need_to_login), Toast.LENGTH_LONG).show();
						}
					}
				});
			
			viewHolder.toolTip.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (downloadPlan1.getPlanType() == PlanType.GOOGLE) {
						openGoogleDownloadToolTip(downloadPlan1, v);
					} else if (downloadPlan1.getPlanType() == PlanType.MOBILE) {
						openMobileDownloadToolTip(downloadPlan1, v);
					} else if (downloadPlan1.getPlanType() == PlanType.REEDEM) {
						openRedeemDownloadToolTip(downloadPlan1, v);
					}
				}
			});
			
			if (plans.size() > 1 ) {
				populateSecondPlanLayout(convertView, plans);
				
			} else {
				viewHolder.layout2 = (RelativeLayout) convertView.findViewById(R.id.upgrade_plans_row2);
				viewHolder.layout2.setVisibility(View.GONE);
			}
			
		}
		
		private void populateSecondPlanLayout(View convertView, List<DownloadPlan> plans) {
			ViewHolder viewHolder;
			viewHolder = new ViewHolder();
			
			viewHolder.layout2 = (RelativeLayout) convertView.findViewById(R.id.upgrade_plans_row2);			
			viewHolder.planPeriodName2 = (TextView) convertView.findViewById(R.id.upgrade_period2);
			viewHolder.planPrice2 = (TextView) convertView.findViewById(R.id.upgrade_price2);
			viewHolder.subscribeButton2 = (Button) convertView.findViewById(R.id.upgrade_button_subscribe2);
			
			viewHolder.subscribeButton2.setText(mContext.getString(R.string.download_plan_button_buy));
			convertView.setTag(R.id.view_tag_view_holder, viewHolder);

		
		// populate the view from the Plan's list.
			final DownloadPlan downloadPlan2 = plans.get(1);
			if (downloadPlan2.getPlanType() == PlanType.GOOGLE) {
				viewHolder.progressBar2 = (ProgressBar) convertView.findViewById(R.id.progressBar2); 
			}
			convertView.setTag(R.id.view_tag_object, downloadPlan2);
			viewHolder.planPeriodName2.setText(downloadPlan2.getPlanName());
		
		// stores the object in the view.
			convertView.setTag(R.id.view_tag_object, downloadPlan2);
			
			if (downloadPlan2.getPlanType() == PlanType.GOOGLE && downloadPlan2.getSkudetails() != null) {
				viewHolder.planPrice2.setText(downloadPlan2.getSkudetails().getPrice());
				viewHolder.planPrice2.setVisibility(View.VISIBLE);
				viewHolder.progressBar2.setVisibility(View.GONE);				
			} else {
				if(!isBindToGoogle && downloadPlan2.getPlanType() == PlanType.GOOGLE){
					viewHolder.planPrice2.setVisibility(View.VISIBLE);
					viewHolder.progressBar2.setVisibility(View.GONE);
				}
				viewHolder.planPrice2.setText(String.valueOf(downloadPlan2.getPlanPrice()) + " " + downloadPlan2.getPlanCurrency());
			}	
			viewHolder.planPeriodName2.setText(downloadPlan2.getPlanName());
			viewHolder.subscribeButton2.setClickable(downloadPlan2.isBuyButtonClickable());
			viewHolder.subscribeButton2.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Check user's balance
						ApplicationConfigurations applicationConfigurations = mDataManager.getApplicationConfigurations();
						String sesion = applicationConfigurations.getSessionID();
						boolean isRealUser = applicationConfigurations.isRealUser();
						
						if (!TextUtils.isEmpty(sesion) && isRealUser) {
							clickedPLan = downloadPlan2;
							((Button) v).setClickable(false);
							toggleBuyButtons();						
							if (downloadPlan2.getPlanType() == PlanType.GOOGLE) {
								
								if(isBindToGoogle){
									purchaseItem(clickedPLan.getSkudetails().getSku());									
								}else{
									showDialog(
											getActivity().getString(R.string.google_wallet), 
											getActivity().getString(R.string.for_using_google_wallet_please_accept_terms_in_google_play));
								}
								
							} else if (downloadPlan2.getPlanType() == PlanType.MOBILE) {
								mApplicationConfigurations.setTempClickedDownloadPlan(clickedPLan);
								String msisdn =  mDeviceConfigurations.getDevicePhoneNumber();
								 /*Check if device mobile number(msisdn) is available. 
								 		yes - verify it.  
										no - check if user logged in with a number
													yes - verify it.
													no - ask user to enter a number. (add fragment) */
								if (msisdn != null && !msisdn.equalsIgnoreCase("")) {
									mobileToSend = msisdn;
									mDataManager.getMobileVerification(msisdn, null, MobileOperationType.MOBILE_VERIFY, DownloadFragment.this);
								} else {								
									checkUserLoginNumber();
								}
							}
							
	//						clickedPLan = downloadPlan2;
	//						mDataManager.getDownload(downloadPlan2.getPlanId(), mMediaItem.getId(), DownloadActivity.mobileToSend, null, DownloadOperationType.BUY_CHARGE, DownloadFragment.this);
							
						} else {
							Toast.makeText(DownloadFragment.this.mContext, getResources().getString(R.string.download_need_to_login), Toast.LENGTH_LONG).show();
						}
					}
				});
		}
		
		public void openGoogleDownloadToolTip(DownloadPlan plan, View view) {
			String title = getResources().getString(R.string.download_plans_google_wallet);
			String text;
			if (plan.isIndia()) {
				text = getResources().getString(R.string.download_google_tool_tip_india);
			} else {
				text = getResources().getString(R.string.download_google_tool_tip_non_india);
			}
			showToolTipDialog(title, text);
		}
		
		public void openMobileDownloadToolTip(DownloadPlan plan, View view) {
			String title = getResources().getString(R.string.download_plans_mobile);
			String text = getResources().getString(R.string.download_mobile_tool_tip_india);;			
			showToolTipDialog(title, text);
		}
		
		public void openRedeemDownloadToolTip(DownloadPlan plan, View view) {
			String title = getResources().getString(R.string.download_plans_redeem_coins);
			String text = getResources().getString(R.string.download_redeem_tool_tip);
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
						submitMobileNumber.setEnabled(false);
						mobileToSend = mobileNumberField.getText().toString();
						mDataManager.checkCountry(mobileNumberField.getText().toString(), this);
					} else {
						submitMobileNumber.setEnabled(true);
						Toast toast = Toast.makeText(((DownloadActivity) getActivity()).getApplicationContext(), getResources().getString(R.string.download_mobile_number_wrong_length), Toast.LENGTH_LONG);
						toast.setGravity(Gravity.CENTER, 0, 0);
						ToastExpander.showFor(toast, TOAST_SHOW_DELAY);
					}
//					mDataManager.getMobileVerification(mobileNumberField.getText().toString(), null, MobileOperationType.MOBILE_VERIFY, this); OLD
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


	
	// ======================================================
	// In App Billing Methods
	// ======================================================
	
	@Override
	public void onIabSetupFinished(IabResult result) {
		if (result.isSuccess()) {
            Logger.i(TAG, "In-app Billing set up" + result);
            List<String> moreSkus = new ArrayList<String>();
            
            moreSkus.add(mResources.getString(R.string.hungama_valuepack));
            moreSkus.add(mResources.getString(R.string.hungama_alacarte));
            billingHelper.queryInventoryAsync(true, moreSkus, this);
//            dealWithIabSetupSuccess();
        } else {
            Logger.i(TAG, "Problem setting up In-app Billing: " + result);
    		isBindToGoogle = false;
    		
    		if (plansAdapter != null) {
    			plansAdapter.notifyDataSetChanged();
    		}
//          dealWithIabSetupFailure();
        }
	}

	/* (non-Javadoc)
	 * @see com.hungama.myplay.activity.util.billing.IabHelper.OnIabPurchaseFinishedListener#onIabPurchaseFinished(com.hungama.myplay.activity.util.billing.IabResult, com.hungama.myplay.activity.util.billing.Purchase)
	 */
	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		if (result.isFailure()) {
			Logger.i(TAG, "Failed to Purchase Item");
			toggleBuyButtons();
        } else if (clickedPLan.getSkudetails().getSku().equals(info.getSku())) {
        	Logger.i(TAG, "Success to Purchase Item");
    		billingHelper.consumeAsync(info, null);
        	mDataManager.getDownload(clickedPLan.getPlanId(), mMediaItem.getId(), "", 
					clickedPLan.getType(), DownloadOperationType.BUY_CHARGE, DownloadFragment.this);
        }
	}

	/* (non-Javadoc)
	 * @see com.hungama.myplay.activity.util.billing.IabHelper.QueryInventoryFinishedListener#onQueryInventoryFinished(com.hungama.myplay.activity.util.billing.IabResult, com.hungama.myplay.activity.util.billing.Inventory)
	 */
	@Override
	public void onQueryInventoryFinished(IabResult result, Inventory inv) {
		if (result.isSuccess()) {
			for (List<DownloadPlan> plansListByType : downloadPlansArranged) {
				for (DownloadPlan plan : plansListByType) {
					if (plan.getPlanType() == PlanType.GOOGLE) {
						String sku = null;
						if (plan.getPlanName().contains("Pack")) {
							sku = mResources.getString(R.string.hungama_valuepack);
							plan.setSkudetails(inv.getSkuDetails(sku));							
						} else if (plan.getPlanName().contains("Single")){
							sku = mResources.getString(R.string.hungama_alacarte);
							plan.setSkudetails(inv.getSkuDetails(sku));
						}
						if (sku != null && inv.hasPurchase(sku)) {
							billingHelper.consumeAsync(inv.getPurchase(sku), null);
						}
					}
				}
			}
			if (plansAdapter != null) {
				plansAdapter.notifyDataSetChanged();
			}
//			skuDetailsValuePack = inv.getSkuDetails(mResources.getString(R.string.hungama_valuepack));
//			skuDetailsAlacarte = inv.getSkuDetails(mResources.getString(R.string.hungama_alacarte));
			
		} else {
			Logger.i(TAG, "Failed Querying Inventory");
		}
		
	}
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        billingHelper.handleActivityResult(requestCode, resultCode, data);
    }
	
	protected void purchaseItem(String sku) {
		billingHelper.launchPurchaseFlow(getActivity(), sku, PURCHASE_REQUEST_CODE, DownloadFragment.this);
	}


	/* (non-Javadoc)
	 * @see com.hungama.myplay.activity.util.billing.IabHelper.OnIabSetupFinishedListener#onIabFailedBindToService(boolean)
	 */
	@Override
	public void onIabFailedBindToService(boolean isBind) {
		
//		showDialog(
//				getActivity().getString(R.string.google_wallet), 
//				getActivity().getString(R.string.for_using_google_wallet_please_accept_terms_in_google_play));
		
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
