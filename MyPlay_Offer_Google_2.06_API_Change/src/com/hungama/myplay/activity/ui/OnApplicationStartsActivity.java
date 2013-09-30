package com.hungama.myplay.activity.ui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockActivity;
import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.gcm.GCMIntentService;
import com.hungama.myplay.activity.services.CampaignsPreferchingService;
import com.hungama.myplay.activity.services.GCMRegistrationService;
import com.hungama.myplay.activity.services.InventoryLightService;
import com.hungama.myplay.activity.services.SubscriptionService;
import com.hungama.myplay.activity.ui.LoginActivity.NewApiWrapper;
import com.hungama.myplay.activity.util.Utils;

/**
 * Invisible controller that delegates to other activities
 * based on the application credential's states.
 */
public class OnApplicationStartsActivity extends SherlockActivity {

	public enum ConnectionStatus {WAITING,READY,FAILED};
	
	private static final String TAG = "OnApplicationStartsActivity";
	
	private static final int RESULT_ACTIVITY_CODE_SLPASH_SCREEN = 1;
	private static final int RESULT_ACTIVITY_CODE_REPLACEMENTS = 2;
	private static final int RESULT_ACTIVITY_CODE_LOGIN = 3;
	public static String mHardwareId=null;
	public static String macAddress=null;
	
	// inserted by Hungama
	public static ConnectionStatus parsed=ConnectionStatus.WAITING;
	
	private static String oem=null;
	private static String message=null;
	private static String appCode=null;
	private static String affiliateId=null;
	private static String oemPackageName=null;
	//End Hungama
	public static final String ARGUMENT_ON_APPLICATION_START_ACTIVITY = "argument_on_application_start_activity";
	
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	
	public OnApplicationStartsActivity() {
//		setAppCode(this.getResources().getString(R.string.app_code));
//		setOemPackageName(this.getResources().getString(R.string.oem_package_name));
//		setAffiliateId(this.getResources().getString(R.string.affiliate_id));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
				
		Log.i("Check hardwareid", "enter start");
		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = new ApplicationConfigurations(getApplicationContext());
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(this.TELEPHONY_SERVICE);
        /*
         * If the device has a telephony device id, that will be enough for identification.
         */
        if (telephonyManager != null && !TextUtils.isEmpty(telephonyManager.getDeviceId())) {
            mHardwareId = telephonyManager.getDeviceId();
            
        } 
        /*else {
            
             *  The device doesn't have any telephony device id, it means that it will be the mac address
             
            WifiManager wimanager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
            macAddress = wimanager.getConnectionInfo().getMacAddress();
//            mHardwareId = macAddress;
        }*/
        WifiManager wimanager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
        macAddress = wimanager.getConnectionInfo().getMacAddress();
		// Initializes internal application components.
		mDataManager.notifyApplicationStarts();
		
		// starts prefetching the discover moods.
		mDataManager.prefetchMoodsIfNotExists();
		trackIMEI();
		// Starts the Splash screen.
		Intent splashIntent = new Intent(getApplicationContext(), SplashScreenActivity.class);
		splashIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivityForResult(splashIntent, RESULT_ACTIVITY_CODE_SLPASH_SCREEN);
		
		// Verifies that the device supports GCM
		GCMRegistrar.checkDevice(this);
		// Verifies that the manifest contains all the requirements
		// TODO: Removes the this call when publishing the app
		GCMRegistrar.checkManifest(this);
			
		final String regId = GCMRegistrar.getRegistrationId(this);
		
		if(regId.equalsIgnoreCase("")){
			GCMRegistrar.register(this, GCMIntentService.SENDER_ID);
		}else{
			Log.i(TAG, "Already registered");
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		Intent i = getIntent();
		boolean isGCMIntent = i.getBooleanExtra(GCMIntentService.GCM_INTENT, false);
		if(isGCMIntent){
			FlurryAgent.onStartSession(this, getResources().getString(R.string.flurry_app_key));
			FlurryAgent.logEvent("Push notification pushed");
		}
	}
	
	@Override
	protected void onPause() {
		FlurryAgent.onEndSession(this);
		super.onPause();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		// checks if the retrieved activity is the Splash Screen
		if (requestCode == RESULT_ACTIVITY_CODE_SLPASH_SCREEN) {
			
			if (resultCode == RESULT_OK) {
				// checks if we can present any replacements of CatchMedia.
				List<Placement> splashPlacements = mDataManager.getStoredSplashPlacement();
				if (!Utils.isListEmpty(splashPlacements)) {
					// Shows some splashments for the user.
					Intent replacementsIntent = new Intent(this, PlacementSplashActivity.class);
					replacementsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					startActivityForResult(replacementsIntent, RESULT_ACTIVITY_CODE_REPLACEMENTS);
				} else {
					continueApplicationFlow();
				}
			} else {
				// the splash screen was canceled. exit the application.
				finish();
				return;
			}
		}
		
		// checks if the retrieved activity is the CatchMedia's Replacements.
		if (requestCode == RESULT_ACTIVITY_CODE_REPLACEMENTS && resultCode == RESULT_OK) {
			continueApplicationFlow();
		}
		
		// checks if the retrieved activity is the Login Activity.
		if (requestCode == OnApplicationStartsActivity.RESULT_ACTIVITY_CODE_LOGIN) {
			if (resultCode == RESULT_OK) {
				// the user has session, start the main activity of the
				// application.
				startMainActivity();
			} else {
				
				finish();
				return;
			}
		}
	}
	
	
	// inserted by Hungama
	private void trackIMEI(){
		
		final Activity _this = this;
		
		Thread t = new Thread() {
			public void run() {
				URL url;
				try {
					System.out.println("SecuredThread");
					Log.i("trystart--", "");
					url = new URL("https://secure.hungama.com/myplayhungama/device_offer_v2.php?imei="
							+URLEncoder.encode(mHardwareId, "utf-8")+"&mac="
							+URLEncoder.encode(macAddress, "utf-8")+"&user_agent="
							+URLEncoder.encode(getDefaultUserAgentString(_this), "utf-8")+"&login=1");
//					+"&mac="
//					+URLEncoder.encode(OnApplicationStartsActivity.macAddress, "utf-8")
//					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					Log.i("URL fetched-", url.toString());
					HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
					if (urlConnection.getResponseCode()== HttpsURLConnection.HTTP_OK){
						InputStream in = new BufferedInputStream(
								urlConnection.getInputStream());
						StringBuilder sb = new StringBuilder();
						int ch = -1;
						while ((ch = in.read()) != -1) {
							sb.append((char) ch);
						}
						parseJSON(sb.toString());
						/*_this.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
//								showToast(response);
							}
						});*/
						
//						Log.i("Response--", response);
						parsed=ConnectionStatus.READY;
						return;
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
				parsed=ConnectionStatus.FAILED;
			};
		};
		t.start();
	}
	
	private JSONObject jsonObject;
//	private JSONObject conObject;
	String strParsedValue = null;
	public String parseJSON(String responce) throws JSONException
    {
        try {
			jsonObject = new JSONObject(responce);
			 
			if (jsonObject.getInt("code")==200){
				setMessage(jsonObject.getString("message"));
//				message=jsonObject.getString("message");
//				conObject = new JSONObject(responce);
//				oem=jsonObject.getString("oem");
				
				setOem(jsonObject.getString("oem"));
				setAppCode(jsonObject.getString("app_code"));
				setAffiliateId(jsonObject.getString("affiliate_id"));
				setOemPackageName(jsonObject.getString("package_name"));
//				appCode=jsonObject.getString("app_code");
//				affiliateId=jsonObject.getString("affiliate_id");
//				oemPackageName=jsonObject.getString("package_name");
				Log.i("Response--", getMessage()+"/"+getOem()+"/"+getAppCode()+"/"+getAffiliateId()+"/"+getOemPackageName());
			}
			else if (jsonObject.getInt("code")==100){
				setOem(jsonObject.getString("oem"));
				setAppCode(jsonObject.getString("app_code"));
				setAffiliateId(jsonObject.getString("affiliate_id"));
				setOemPackageName(jsonObject.getString("package_name"));
			}
			else{
				setMessage(null);
				setOemPackageName(this.getResources().getString(R.string.oem_package_name));
				setAffiliateId(this.getResources().getString(R.string.affiliate_id));
				setAppCode(this.getResources().getString(R.string.app_code));
				responce=null;
			}
		} catch (Exception e) {
			responce=null;
		}
		return responce;
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
	
	private void continueApplicationFlow(){
		
		/*
		 *  checks if the user already logged in or not by checking
		 *  if the session is empty or not.
		 */
		ApplicationConfigurations applicationConfigurations = mDataManager.getApplicationConfigurations();
		String session = applicationConfigurations.getSessionID();
		boolean isRealUser = applicationConfigurations.isRealUser();
		boolean isFirstVisitToApp = applicationConfigurations.isFirstVisitToApp();
		
		if (!isFirstVisitToApp && !TextUtils.isEmpty(session) && isRealUser) {
			
			startMainActivity();
			
			// Grab the GCM registration id and send to Hungama.
			Intent gcmRegistrationServiceIntent = new Intent(getApplicationContext(), GCMRegistrationService.class);
			gcmRegistrationServiceIntent.putExtra(GCMRegistrationService.EXTRA_REGISTRATION_ID, mApplicationConfigurations.getRegistrationId());			
			startService(gcmRegistrationServiceIntent); // Fire
			
		} else {
			
			// pass the user to login / sign up for the application.
			startLoginActivity();
		}
	}
	
	private void startLoginActivity() {
		Intent startLoginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
		startLoginActivityIntent.putExtra(OnApplicationStartsActivity.ARGUMENT_ON_APPLICATION_START_ACTIVITY, "on_application_start_activity");
		startLoginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivityForResult(startLoginActivityIntent, RESULT_ACTIVITY_CODE_LOGIN);
	}
	
	private void startMainActivity() {
		
		ApplicationConfigurations applicationConfigurations = mDataManager.getApplicationConfigurations();
		boolean isRealUser = applicationConfigurations.isRealUser();
		String session = applicationConfigurations.getSessionID();
		
		// prefetches the subscription plans.
		if (isRealUser) {
			Intent subscriptionPlansService = new Intent(getApplicationContext(), SubscriptionService.class);
			startService(subscriptionPlansService);
		}
		
		// syncs the inventory.
		Intent inventoryLightService = new Intent(getApplicationContext(), InventoryLightService.class);
		startService(inventoryLightService);
		
		// prefetches the campaigns.
		if (!TextUtils.isEmpty(session)) {
			Intent campaignsService = new Intent(getApplicationContext(), CampaignsPreferchingService.class);
			startService(campaignsService);
		}
		
		// starts the main activity.
		Intent startHomeActivityIntent = new Intent(getApplicationContext(), HomeActivity.class);
		startHomeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(startHomeActivityIntent);
		overridePendingTransition(0, 0);
		
		finish();
		return;
	}

	private static void setOem(String oem) {
		OnApplicationStartsActivity.oem = oem;
	}

	private static String getOem() {
		return oem;
	}

	private static void setMessage(String message) {
		OnApplicationStartsActivity.message = message;
	}

	public static String getMessage() {
		return message;
	}
	
	public static String getAppCode() {
		return appCode==null?"MYPLAY-ANDROID":appCode;
	}

	public static void setAppCode(String appCode) {
		OnApplicationStartsActivity.appCode = appCode;
	}
	
	public static String getAffiliateId() {
		return affiliateId;
	}

	public static void setAffiliateId(String affiliateId) {
		OnApplicationStartsActivity.affiliateId = affiliateId;
	}

	public static String getOemPackageName() {
		return oemPackageName;
	}

	public static void setOemPackageName(String oemPackageName) {
		OnApplicationStartsActivity.oemPackageName = oemPackageName;
	}
	
}
