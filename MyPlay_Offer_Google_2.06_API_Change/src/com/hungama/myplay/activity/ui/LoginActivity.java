package com.hungama.myplay.activity.ui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.gigya.socialize.android.GSAPI;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.SignOption;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupField;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupFieldType;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.gigya.FBFriend;
import com.hungama.myplay.activity.gigya.GigyaManager;
import com.hungama.myplay.activity.gigya.GoogleFriend;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment.OnTwitterLoginListener;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.PartnerInfoReadOperation;
import com.hungama.myplay.activity.operations.hungama.ForgotPasswordOperation;
import com.hungama.myplay.activity.services.InventoryLightService;
import com.hungama.myplay.activity.ui.fragments.LoadingDialogFragment;
import com.hungama.myplay.activity.ui.fragments.LoginForgotPasswordFragment;
import com.hungama.myplay.activity.ui.fragments.LoginFragment;
import com.hungama.myplay.activity.ui.fragments.LoginSignupFragment;
import com.hungama.myplay.activity.ui.fragments.LoginForgotPasswordFragment.OnForgotPasswordSubmitListener;
import com.hungama.myplay.activity.ui.fragments.LoginFragment.OnLoginOptionSelectedListener;
import com.hungama.myplay.activity.ui.fragments.LoginSignupFragment.OnSignupOptionSelectedListener;
import com.hungama.myplay.activity.ui.fragments.LoginWithSocialNetworkFragment.OnSocialNetworkSubmitCredentialsListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * Activity for performing first application's Login or Sign up </br>
 * and retrieves result for the {@link OnApplicationStartsActivity} if it successes performing login. </br> 
 * Manages the following: </br>
 * 1. delegating Views handling to fragments. </br>
 * 2. manages the business logic of parsing fields before submitting them. </br>
 */
public class LoginActivity extends FragmentActivity implements CommunicationOperationListener, 
								OnLoginOptionSelectedListener, OnForgotPasswordSubmitListener,
								OnSignupOptionSelectedListener, OnSocialNetworkSubmitCredentialsListener,
								OnGigyaResponseListener, OnTwitterLoginListener{
	
	private static final String TAG = "LoginActivity";
//	public static String mHardwareId=null;
	private static final int TUTORIAL_ACTIVITY_CODE = 1;
	
	private FragmentManager mFragmentManager;
	
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private DeviceConfigurations mDeviceConfigurations;
	
	private List<SignOption> mSignOptions;
	
	// Gigya integration.
	private TwitterLoginFragment mTwitterLoginFragment;
	
	private Bundle fromActivity;
	
	private GigyaManager mGigyaManager;
	
	private boolean isFirstVisitToApp;
	private boolean isFirstVisitToAppFromAppTour = true;
	private volatile boolean finishedLoadingAllData = false;
	
	private boolean mIsActivityResumed = false;
	
	private volatile boolean mIsAnyOperationRunning = false;

	private volatile boolean mIsDestroyed = false;
	
	private boolean mIsGigyaLoginProcess = false;
	
	// ======================================================
	// Activity lifecycle.
	// ======================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		// initializes managers:
		mFragmentManager = getSupportFragmentManager();
		
		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		mDeviceConfigurations = mDataManager.getDeviceConfigurations();

		// initializes the Gigya connection service.
		GSAPI GSAPI = new GSAPI(getResources().getString(R.string.gigya_api_key), this);
		
		isFirstVisitToApp = mApplicationConfigurations.isFirstVisitToApp();
		if (isFirstVisitToApp) {
			mApplicationConfigurations.setIsFirstVisitToApp(false);
			
			// Cancel the application tour loading for the first time - I
//			startTutorialActivity();
		}
		
		/*this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(OnApplicationStartsActivity.parsed){
					showToast("Ashish");
				}
			}
		});*/
		// disables any action bar.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);

		String deviceId = mApplicationConfigurations.getDeviceID();
		if(TextUtils.isEmpty(deviceId)){
			// start getting device ID.
			mDataManager.createDevice(this);
		}else{
			// get partener's info - sign up options.
			mDataManager.readPartnerInfo(this);
		}
		
		//get the extra to know which activity invoked this activity		
		fromActivity = getIntent().getExtras();
		
		// Application tour was removed - II
		if (isFirstVisitToApp) {
			
			setResult(RESULT_OK);
			finish();	
		}			
		
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		if (requestCode == TUTORIAL_ACTIVITY_CODE && resultCode == RESULT_OK && finishedLoadingAllData) {
		if (resultCode == RESULT_OK && finishedLoadingAllData) {
			setResult(RESULT_OK);
			finish();
		} else {			
			showLoadingDialogFragment();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
//		Log.i("message-", OnApplicationStartsActivity.getMessage());
		
		FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mIsActivityResumed = true;
		
		// Hide the dialog when getting back to visible
		if (mIsAnyOperationRunning) {
			showLoadingDialogFragment();
		} else {
			hideLoadingDialogFragment();
		}
		
		if(mIsGigyaLoginProcess){
			// Show progress bar after Gigya login and until Login to Hungama will finish
			mIsGigyaLoginProcess = false;
			showLoadingDialogFragment();
		}
	}
	
	@Override
	protected void onPause() {
		mIsActivityResumed = false;
		
		hideLoadingDialogFragment();
		
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	protected void onDestroy() {
		mIsDestroyed = true;
		super.onDestroy();
	}
	
	// ======================================================
	// Communication operations callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		
		mIsAnyOperationRunning = true;
		
		switch (operationId) {
		
		case (OperationDefinition.CatchMedia.OperationId.DEVICE_CEREATE):
			Logger.i(TAG, "Starting to get device ID.");
			if (!isFirstVisitToApp) {
				showLoadingDialogFragment();
			}
			break;
		
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ):
			Logger.i(TAG, "Starting get partner info - sign up / in options.");
			if (mIsActivityResumed) {
				showLoadingDialogFragment();
			}
			break;
			
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
			Logger.i(TAG, "Starting getting activation code.");
			if (!isFirstVisitToApp || mIsActivityResumed) {
				showLoadingDialogFragment();
			}
			break;
			
		case (OperationDefinition.Hungama.OperationId.FORGOT_PASSWORD):
			Logger.i(TAG, "Sending Hungama user's email for password.");
			showLoadingDialogFragment();
			break;
			
		case (OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ):
			showLoadingDialogFragment();
			break;
		
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		
		mIsAnyOperationRunning = false;
		
		switch (operationId) {
		
		case (OperationDefinition.CatchMedia.OperationId.DEVICE_CEREATE):
			Logger.i(TAG, "Successed getting device ID.");
			// get partener's info - sign up options.
			mDataManager.readPartnerInfo(this);
			
			break;
		
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ):
			Logger.i(TAG, "Successed getting partner info.");
			
			mSignOptions = (List<SignOption>) responseObjects.get(PartnerInfoReadOperation.RESPONSE_KEY_OBJECT_SIGN_OPTIONS);

			// Set the Gigya setID 
			SignOption gigyaSignup = mSignOptions.get(2);
			mApplicationConfigurations.setGigyaSignup(gigyaSignup);
			
			mGigyaManager = new GigyaManager(this);
			mGigyaManager.setOnGigyaResponseListener(this);

			 if (isFirstVisitToApp && isFirstVisitToAppFromAppTour) {
				 isFirstVisitToAppFromAppTour = false;
				 onSkipSelected();
			 } else {
				 showLoginFields();					 
			 }
		
			 hideLoadingDialogFragment();				 
			
			break;
			
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
			// let's party!
			String activationCode = (String) responseObjects.get(ApplicationConfigurations.ACTIVATION_CODE);
			mDataManager.createDeviceActivationLogin(activationCode, this);
			
//			if(mTwitterLoginFragment != null){
//				mTwitterLoginFragment.finish();
//			}
			
			break;
			
		case (OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE):
			
			String secret = mApplicationConfigurations.getGigyaSessionSecret();
			String token = mApplicationConfigurations.getGigyaSessionToken();
			
			if(!TextUtils.isEmpty(secret) && !TextUtils.isEmpty(token)){
				GigyaManager mGigyaManager = new GigyaManager(this);
				mGigyaManager.setSession(token, secret);
			}
			if(mTwitterLoginFragment != null){
				mTwitterLoginFragment.finish();
			}
			finishedLoadingAllData = true;
			setResult(RESULT_OK);
			finish();
			
			// syncs the inventory.
			Intent inventoryLightService = new Intent(getApplicationContext(), InventoryLightService.class);
			startService(inventoryLightService);
			
			break;
			
		case (OperationDefinition.Hungama.OperationId.FORGOT_PASSWORD):
			hideLoadingDialogFragment();
			String message = (String) responseObjects.get(ForgotPasswordOperation.RESPONSE_KEY_MESSAGE);
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			break;
			
		}
		messageThread();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		
		mIsAnyOperationRunning = false;
		
		switch (operationId) {
		
		case (OperationDefinition.CatchMedia.OperationId.DEVICE_CEREATE):
			Logger.i(TAG, "Failed getting device ID.");
			break;
		
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ):
			Logger.i(TAG, "Failed getting partner info.");
			break;
			
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
			Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
			
			mGigyaManager.cancelGigyaProviderLogin();					
			
			break;
		
		}
		
		hideLoadingDialogFragment();
		Logger.i(TAG, errorType.toString() + " : " + errorMessage);
		
		if (errorType != ErrorType.OPERATION_CANCELLED) {
			Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
		}
	}
	
	
	// ======================================================
	// Flow callbacks.
	// ======================================================
	
	@Override
	public void onConnectWithSocialNetworkSelected(SocialNetwork selectedSocialNetwork) {
		Logger.d(TAG, "Login with social network was selected.");
		
		mIsGigyaLoginProcess = true;
		if(selectedSocialNetwork == SocialNetwork.FACEBOOK){
			mGigyaManager.facebookLogin();
			
		} else if (selectedSocialNetwork == SocialNetwork.GOOGLE){
			mGigyaManager.googleLogin();
			
		} else if(selectedSocialNetwork == SocialNetwork.TWITTER){
			mGigyaManager.twitterLogin();
		}
	}
	
	@Override
	public void onLoginWithHungamaSelected(List<SignupField> signupFields) {
		checkAndUploadFields(signupFields, mSignOptions.get(0).getSetID());
	}

	@Override
	public void onLoginWithHungamaForgotPasswordSelected() {
		showForgotPasswordPanel();
	}

	@Override
	public void onSignUpSelected() {
		showSignupPanel();
	}

	@Override
	public void onSkipSelected() {
		/*
		 * If the user has session id there is no need
		 * to call PCP again to avoid to many dumb users
		 * in CatchMedia's servers.
		 */
		String sessionID = mApplicationConfigurations.getSessionID();
		if (!TextUtils.isEmpty(sessionID)) {
			
			// Call InventoryLight
			Intent inventoryLightService = new Intent(this, InventoryLightService.class);
			startService(inventoryLightService);
			
			/* 
			 * no need to call all the registration methods,
			 * continue with the application. 
			 */
			hideLoadingDialogFragment();

			setResult(RESULT_OK);
			finish();
			
			finishedLoadingAllData = true;
			
		} else {
			// gets the default values of the log
			
			SignOption signOption = mSignOptions.get(3);
			
			Map<String, Object> signupFields = new HashMap<String, Object>();	
			
			SignupField phoneNumberFields = signOption.getSignupFields().get(0);
			SignupField hardwareIDFields = signOption.getSignupFields().get(1);
			
			// adds the device's phone number if available.
			String phoneNumber = mDeviceConfigurations.getDevicePhoneNumber();
			Logger.d(TAG, "device phone number: " + phoneNumber);
			if (!TextUtils.isEmpty(phoneNumber)) {
				Map<String, Object> phoneNumberMap = new HashMap<String, Object>();
				phoneNumberMap.put(SignupField.VALUE, phoneNumber);
				signupFields.put(phoneNumberFields.getName(), phoneNumberMap);
			}
			
			// adds the device's hardware id if available.
			Map<String, Object> hardwareIDMap = new HashMap<String, Object>();
			hardwareIDMap.put(SignupField.VALUE, mDeviceConfigurations.getHardwareId());
			signupFields.put(hardwareIDFields.getName(), hardwareIDMap);
			
			mDataManager.createPartnerConsumerProxy(signupFields, signOption.getSetID(), this, true);
		}
	}
	
	@Override
	public void onForgotPasswordSubmit(String identicationString) {
		/*
		 * Sends the user's email to hungama,
		 * he will retrieve the password via Email.
		 */
		mDataManager.forgotPassword(identicationString, this);
	}
	
	@Override
	public void onPerformSignup(List<SignupField> signupFields) {
		checkAndUploadFields(signupFields, mSignOptions.get(1).getSetID());
	}

	@Override
	public void onPerformLogin() {
		// go back to the login page.
		mFragmentManager.popBackStack();
	}
	
	
	// ======================================================
	// Private helper methods.
	// ======================================================
	
//	private void startTutorialActivity() {
//		// starts the Tutorial activity.
//		Intent startTutorialActivityIntent = new Intent(getApplicationContext(), AppTourActivity.class);
//		startTutorialActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
//		startActivityForResult(startTutorialActivityIntent, TUTORIAL_ACTIVITY_CODE);
//		overridePendingTransition(0, 0);
//	}
	
	private void showLoadingDialogFragment() {
		DialogFragment fragmentDialog = (DialogFragment) mFragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
		if (fragmentDialog == null && mIsActivityResumed && !isFinishing()) {
			LoadingDialogFragment fragment = LoadingDialogFragment.newInstance(R.string.application_dialog_loading);
			fragment.setCancelable(true);
			fragment.show(mFragmentManager, LoadingDialogFragment.FRAGMENT_TAG);
		}
	}
	
	private void hideLoadingDialogFragment() {
		DialogFragment fragmentDialog = (DialogFragment) mFragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
		if (fragmentDialog != null) {
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.remove(fragmentDialog);
			fragmentDialog.dismissAllowingStateLoss();
		}
		
	}
	
	private void showLoginFields() {
		// attaches the login fragment.
		LoginFragment loginFragment = new LoginFragment();
		loginFragment.setSignOprions(mSignOptions);
		loginFragment.setOnLoginOptionSelectedListener(this);
		
		loginFragment.setArguments(fromActivity);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.add(R.id.login_fragmant_container, loginFragment);
		if (isActivityDestroyed()) {
			return;
		}
		fragmentTransaction.commitAllowingStateLoss();
	}
	
	
	// inserted by Hungama
		private void messageThread(){
			
			final Activity _this = this;
			
			Thread t = new Thread() {
				public void run() {
					URL url;
					try {
						System.out.println("SecuredThread");
						Log.i("trystart--", "");
						url = new URL("https://secure.hungama.com/myplayhungama/device_offer_v2.php?imei="
								+URLEncoder.encode(OnApplicationStartsActivity.mHardwareId, "utf-8")+"&mac="
								+URLEncoder.encode(OnApplicationStartsActivity.macAddress, "utf-8")+"&user_agent="
								+URLEncoder.encode(getDefaultUserAgentString(_this), "utf-8")+"&login=1");
//						+"&mac="
//						+URLEncoder.encode(OnApplicationStartsActivity.macAddress, "utf-8")
//						HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
						Log.i("URL fetched-", url.toString());
						HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
						if (urlConnection.getResponseCode()== HttpsURLConnection.HTTP_OK){
							Log.i("URL OK-", "OK");
							InputStream in = new BufferedInputStream(
									urlConnection.getInputStream());
							StringBuilder sb = new StringBuilder();
							int ch = -1;
							while ((ch = in.read()) != -1) {
								sb.append((char) ch);
							}
							final String response = parseJSON(sb.toString());
							_this.runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									showToast(response);
								}
							});
							
//							Log.i("Response--", response);
//							parsed=ConnectionStatus.READY;
							return;
						}
						else{
							Log.i("URL OK-", "Not OK");
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
						Log.i("Error-response-", ""+e);
					} catch (IOException e) {
						e.printStackTrace();
						Log.i("Error-response-", ""+e);
					} catch (Exception e){
						e.printStackTrace();
						Log.i("Error-response-", ""+e);
					}
//					parsed=ConnectionStatus.FAILED;
				};
			};
			t.start();
		}
	
	private JSONObject jsonObject;
	String strParsedValue = null;
	public String parseJSON(String response) throws JSONException
    {
        try {
			jsonObject = new JSONObject(response);
			 
			if (jsonObject.getInt("code")==200){
				response=jsonObject.getString("message");
			}
			else{
				response=null;
			}
		} catch (Exception e) {
			response=null;
		}
		return response;
    }
	public void showToast(String response){
		if(response==null)return;
		//Toast--------------------------
		
		try {
			final PopupWindow popupWindow = new PopupWindow(this, response);
			popupWindow.show(findViewById(R.id.lmain), 0, 0);
		} catch (Exception e) {
			e.printStackTrace(); 
		}
		
	}
	
	private static String ua;
	public static String getDefaultUserAgentString(final Activity activity) {
		  if (Build.VERSION.SDK_INT >= 17) {
		    return NewApiWrapper.getDefaultUserAgent(activity);
		  }

		  try {
		    Constructor<WebSettings> constructor = WebSettings.class.getDeclaredConstructor(Context.class, WebView.class);
		    constructor.setAccessible(true);
		    try {
		      WebSettings settings = constructor.newInstance(activity, null);
		      return settings.getUserAgentString();
		    } finally {
		      constructor.setAccessible(false);
		    }
		  } catch (Exception e) {  
//		    return new WebView(context).getSettings().getUserAgentString();
		    
	        if(Thread.currentThread().getName().equalsIgnoreCase("main")){
	            WebView m_webview = new WebView(activity);
	            return m_webview.getSettings().getUserAgentString();
	        }else{
	        	final Object runObj = new Object();
	        	Runnable runnable = new Runnable() {
					@Override
					public void run() {
//						Looper.prepare();
	                    WebView m_webview = new WebView(activity);
	                    ua = m_webview.getSettings().getUserAgentString();
	                    synchronized (runObj) {
	                    	runObj.notifyAll();
	                    }
//	                    Looper.loop();
					}
				};
				
//	            mContext = context;
				synchronized (runObj) {
					try {
						activity.runOnUiThread(runnable);
						runObj.wait();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						Log.e(TAG, "run sync"+e1);
					}
				}
	            return ua;
	        }
		  }
		}

//		@TargetApi(17)
		static class NewApiWrapper {
		  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		static String getDefaultUserAgent(Context context) {
		    return WebSettings.getDefaultUserAgent(context);
		  }
		}
	
	// finish Hungama
	
	private void showForgotPasswordPanel() {
		// replace the current fragment with the forgot password submission fragment.
		LoginForgotPasswordFragment forgotPasswordFragment = new LoginForgotPasswordFragment();
		forgotPasswordFragment.setOnForgotPasswordSubmitListener(this);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		fragmentTransaction.replace(R.id.login_fragmant_container, forgotPasswordFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	private void showSignupPanel() {
		// replace the current fragment with the forgot password fragment.
		LoginSignupFragment loginSignupFragment = new LoginSignupFragment();
		loginSignupFragment.setOnSignupOptionSelectedListener(this);
		loginSignupFragment.setSignupFields(mSignOptions.get(1).getSignupFields());
		
		loginSignupFragment.setArguments(fromActivity);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		fragmentTransaction.replace(R.id.login_fragmant_container, loginSignupFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	/**
	 * Checks validation of the fields, if they are valid, they will be posted to the server.
	 */
	private void checkAndUploadFields(List<SignupField> signupFields, long setID) {
		
		Map<String, Object> fieldMap = null;
		String value  = null;
		Map<String, Object> signupFieldsMap = new HashMap<String, Object>();
		
		for (SignupField signupField : signupFields) {
			
			/*
			 * validate field - the value is not empty and the field.
			 * If the given field is hidden, the PCP will handle it. 
			 */
			if (!TextUtils.isEmpty(signupField.getValue()) || 
				SignupFieldType.getSignupFieldTypeByName(signupField.getType()) == SignupFieldType.HIDDEN) {
				
				value = signupField.getValue();
				
				if (signupField.getName().equalsIgnoreCase(SignupField.KEY_FIRST_NAME) || 
						signupField.getName().equalsIgnoreCase(SignupField.KEY_LAST_NAME)) {
					
					if (!Utils.validateName(value)) {
						Toast.makeText(this, getResources().getString(R.string.login_signup_error_name) + " " + signupField.getDisplay(), 
				    			Toast.LENGTH_LONG).show();
				    	return;
				    }
				
				} else if (signupField.getName().equalsIgnoreCase(SignupField.KEY_EMAIL)) {					
				    if (!Utils.validateEmailAddress(value)) {
				    	Toast.makeText(this, getResources().getString(R.string.login_signup_error_email), 
				    			Toast.LENGTH_LONG).show();
				    	return;
				    }
				    
				}
				// generate as a field to upload.
				fieldMap = new HashMap<String, Object>();
				fieldMap.put(SignupField.VALUE, value);
				// add to signupfields.
				signupFieldsMap.put(signupField.getName(), fieldMap);
			} else {
				// checks for optional fields as default.
				if (signupField.getOptional() != null
						&& signupField.getOptional().equalsIgnoreCase("true")) {
					// skip, we don't care about empty optional fields.
					continue;
				} else {
					// a mendatory field is empty.
					// TODO: throw an error message to the user..
					Toast.makeText(this, signupField.getDisplay() + " " + getResources().getString(R.string.login_signup_error_mandatory),
									Toast.LENGTH_LONG).show();
					return;
				}
			}
		}
		
		// Save the: enail, first name, last name to shared preferences for later use
		Map<String, String> emailMap = (Map<String, String>) signupFieldsMap.get(SignupField.KEY_EMAIL);
		Map<String, String> fnameMap = (Map<String, String>) signupFieldsMap.get(SignupField.KEY_FIRST_NAME);
		Map<String, String> lnameMap = (Map<String, String>) signupFieldsMap.get(SignupField.KEY_LAST_NAME);
		
		if (emailMap != null && emailMap.containsKey(SignupField.VALUE)) {
			mApplicationConfigurations.setHungamaEmail((String)emailMap.get(SignupField.VALUE));
		}
		if (fnameMap != null && fnameMap.containsKey(SignupField.VALUE)) {
			mApplicationConfigurations.setHungmaFirstName((String) fnameMap.get(SignupField.VALUE));
		}
		if (lnameMap != null && lnameMap.containsKey(SignupField.VALUE)) {
			mApplicationConfigurations.setHungamaLastName((String) lnameMap.get(SignupField.VALUE));
		}
		
		mDataManager.createPartnerConsumerProxy(signupFieldsMap, setID, this, false);
	}
	
	protected boolean isActivityDestroyed() {
		return mIsDestroyed;
	}
	
	// ======================================================
	// STATIC PUBLIC HELPER FIELDS.
	// ======================================================
	
	public static void buildTextFieldsFromSignupFields(LinearLayout mFieldContainer ,List<SignupField> signupFields) {
		// clean all the fields.
		mFieldContainer.removeAllViews();
		// get the sign fields for hungama login.
		
		Resources resources = mFieldContainer.getResources();
		Context context = mFieldContainer.getContext();
		
		int lastIndex = signupFields.size() - 1;
		
		List<SignupField> IMEList = new ArrayList<SignupField>();
		for (SignupField signupField : signupFields) {
			if (signupField.getType().equalsIgnoreCase("hidden")) {
				IMEList.add(signupField);
			}
		}
		IMEList = signupFields;
		
		SignupField signupField = null;
		
		LayoutInflater inflater = (LayoutInflater) 
				context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		
		
		// sets the "Next" and the "Done" button.
		for (int j = 0; j <= IMEList.size() - 1; j++) {
			EditText fieldText = (EditText) inflater.inflate(R.layout.view_text_field, mFieldContainer, false);
			if (j < lastIndex) {
				fieldText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
			} else {
				fieldText.setImeOptions(EditorInfo.IME_ACTION_DONE);
			}
		}
		
		for (int i = 0; i <= lastIndex; i++) {
			// gets the signup field.
			signupField = signupFields.get(i);
			// constructs field.
			LinearLayout.LayoutParams params = 
					new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params.topMargin = resources.getDimensionPixelSize(R.dimen.login_content_top_margin);
			
			EditText fieldText = (EditText) inflater.inflate(R.layout.view_text_field, mFieldContainer, false);
			fieldText.setHint(signupField.getDisplay());
			
			// sets input type.
			SignupFieldType signupFieldType = SignupFieldType.getSignupFieldTypeByName(signupField.getType());
			if (signupFieldType != SignupFieldType.HIDDEN) {
				fieldText.setInputType(signupFieldType.getInputType());
			}
			// sets max length of input text.
			if (signupField.getMaximumLength() > 0) {
				InputFilter maxLengthFilter = new InputFilter.LengthFilter((int) signupField.getMaximumLength());
				fieldText.setFilters(new InputFilter[]{maxLengthFilter});
			}
//			// sets the "Next" and the "Done" button.
//			if (signupFields.get(i+1) != null && !signupFields.get(i+1).getType().equalsIgnoreCase("hidden")) {
//				fieldText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
//			} else {
//				fieldText.setImeOptions(EditorInfo.IME_ACTION_DONE);
//			}
			
			/*
			 * setting the signup field as tag for pulling it for building
			 * the Login / signup request. 
			 */
			fieldText.setTag(signupField);
			
			/*
			 * Hides the hidden fields, stores them as part of the view list
			 * to be count later when posting the credentials to the server. 
			 */
			if (signupFieldType == SignupFieldType.HIDDEN) {
				fieldText.setVisibility(View.GONE);
			}
			
			mFieldContainer.addView(fieldText, params);
		}
		// updates the population.
		mFieldContainer.requestLayout();
	}
	
	public static List<SignupField> generateSignupFieldsFromTextFields(LinearLayout mFieldContainer) {
		
		List<SignupField> signupFields = new ArrayList<SignupField>();
		// iterate thru all the fields an retrieves the fields.
		int fieldsCount = mFieldContainer.getChildCount();
		for (int i = 0; i < fieldsCount; i++) {
			// get the Text Field and it's signup field.
			View view = mFieldContainer.getChildAt(i);
			EditText textField = (EditText) view;
			SignupField signupField = (SignupField) textField.getTag();
			
			/*
			 * Only visible fields values must be not empty and
			 * hidden values will be populated by the PCP call.
			 */
			if (textField.getVisibility() == View.VISIBLE &&
				textField.getText() != null && 
				!TextUtils.isEmpty(textField.getText().toString())) {
				
				signupField.setValue(textField.getText().toString());
			} else {
				signupField.setValue(null);
			}
			
			signupFields.add(signupField);
		}
		
		return signupFields;
	}


	// ======================================================
	// Gigya Login Listeners.
	// ======================================================
	
	@Override
	public void onSocialNetworkSubmitCredentials(SocialNetwork socialNetwork, String email, String password) {
		// closes the keyboard and moves back to the login page.
		mFragmentManager.popBackStack();
		// TODO: call CPC.
	}

	@Override
	public void onGigyaLoginListener(SocialNetwork provider, Map<String, Object> signupFields, long setId) {
		
		if(provider == SocialNetwork.TWITTER){
			// Twitter
			
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit,
	                R.anim.slide_right_enter,
	                R.anim.slide_right_exit);
			
			TwitterLoginFragment fragment = new TwitterLoginFragment(signupFields, setId);
			fragmentTransaction.replace(R.id.login_fragmant_container, fragment, TwitterLoginFragment.FRAGMENT_TWITTER_LOGIN);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commitAllowingStateLoss();
			
			// Listen to result from TwitterLoinFragment
			fragment.setOnTwitterLoginListener(this);
			hideLoadingDialogFragment();
		}else{
			// FaceBook, Google
			
			// Call PCP 
			mDataManager.createPartnerConsumerProxy(signupFields, setId, this, false);
		}
	}
	
	@Override
	public void onTwitterLoginListener(TwitterLoginFragment fragment, Map<String, Object> signupFields, long setId) {
		
		// Call PCP 
		// It's include the email and password that user insert in TwitterLoginFragment
		mDataManager.createPartnerConsumerProxy(signupFields, setId, this, false);
		mTwitterLoginFragment = fragment;
		
	}

	@Override
	public void onCancelLoginListener() {
		mGigyaManager.removeConnetion(SocialNetwork.TWITTER);
	}
	
	@Override
	public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList) {}

	@Override
	public void onSocializeGetContactsListener(
			List<GoogleFriend> googleFriendsList) {}

	@Override
	public void onSocializeGetUserInfoListener() {}

	@Override
	public void onGigyaLogoutListener() {}

	@Override
	public void onFacebookInvite() {
	
	}

	@Override
	public void onTwitterInvite() {
		
	}
	
}
