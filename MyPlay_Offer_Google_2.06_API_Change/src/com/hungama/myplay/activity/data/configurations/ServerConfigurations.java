package com.hungama.myplay.activity.data.configurations;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.ui.OnApplicationStartsActivity;
import com.hungama.myplay.activity.util.Utils;

import android.content.Context;

public class ServerConfigurations {
	
	/*
	 * Definition of CM server configurations.
	 */
	private final String mServerUrl;
	private final String mServerVersion;
	private final String mLcId;
	private final String mPartnerId;
	private final String mWebServiceVersion;
	private final String mAPI;
	private final String mAppVersion;
//	private final String mAppCode;
	private final String mFormat;
	private final String mReferralId;
	
	/*
	 * Definition of Hungama servers configurations.
	 */
	private final String mHungamaServerUrl;
	private final String mHungamaSocialServerUrl;
	private final String mHungamaSubscriptionServerUrl;
	private final String mHungamaMobileVerificationServerUrl;
	private final String mHungamaDownloadServerUrl;
	private final String mHungamaAuthKey;
	private final String mHungamaVersionCheckServerUrl;
	
	public ServerConfigurations(Context context) {
		/*
		 * CM initialization.
		 */
		mServerVersion = context.getResources().getString(R.string.web_service_url_version);
		mLcId = context.getResources().getString(R.string.lc_id);
		mPartnerId = context.getResources().getString(R.string.partner_id);
		mWebServiceVersion = context.getResources().getString(R.string.ws_ver);
		mAPI = context.getResources().getString(R.string.api);
		mAppVersion = context.getResources().getString(R.string.app_ver);
//		mAppCode = context.getResources().getString(R.string.app_code);
//		mAppCode = OnApplicationStartsActivity.getAppCode();
		mFormat = context.getResources().getString(R.string.format);
		mReferralId = context.getResources().getString(R.string.referal_id);

		String serviceUrl = context.getResources().getString(R.string.web_service_url);
		mServerUrl = "http://" + serviceUrl + "/web_services/apps/" + mServerVersion + "/jsonrpc/";
		
		/*
		 * Hungama initialization.
		 */
		mHungamaServerUrl = context.getResources().getString(R.string.hungama_server_url);
		mHungamaSocialServerUrl = context.getResources().getString(R.string.hungama_social_server_url);
		mHungamaSubscriptionServerUrl = context.getResources().getString(R.string.hungama_subscription_server_url);
		mHungamaMobileVerificationServerUrl = context.getResources().getString(R.string.hungama_mobile_verification_server_url);
//		mHungamaDownloadServerUrl = context.getResources().getString(R.string.hungama_download_server_url_v2);
		mHungamaDownloadServerUrl = context.getResources().getString(R.string.hungama_download_server_url_v3);
		mHungamaAuthKey = Utils.toMD5(context.getResources().getString(R.string.hungama_auth_key));
		mHungamaVersionCheckServerUrl = context.getResources().getString(R.string.hungama_version_check_server_url);
	}

	public static final String LC_ID = "lc_id";
	public static final String PARTNER_ID = "partner_id";
	public static final String WEB_SERVER_VERSION = "ws_ver";
	public static final String API = "api";
	public static final String APPLICATION_VERSION = "app_ver";
	public static final String APPLICATION_CODE = "app_code";
	public static final String FORMAT = "format";
	
	public String getServerUrl() {
		return mServerUrl;
	}

	public String getServerVersion() {
		return mServerVersion;
	}

	public String getLcId() {
		return mLcId;
	}

	public String getPartnerId() {
		return mPartnerId;
	}

	public String getWebServiceVersion() {
		return mWebServiceVersion;
	}

	public String getAPI() {
		return mAPI;
	}

	public String getAppVersion() {
		return mAppVersion;
	}

	public String getAppCode() {
		return OnApplicationStartsActivity.getAppCode();
	}

	public String getFormat() {
		return mFormat;
	}
	
	public String getReferralId() {
		return mReferralId;
	}
	
	
	// ======================================================
	// Hungama Server Configurations.
	// ======================================================
	
	public String getHungamaServerUrl() {
		return mHungamaServerUrl;
	}
	
	public String getHungamaSocialServerUrl() {
		return mHungamaSocialServerUrl;
	}

	public String getHungamaSubscriptionServerUrl() {
		return mHungamaSubscriptionServerUrl;
	}
	
	public String getHungamaMobileVerificationServerUrl() {
		return mHungamaMobileVerificationServerUrl;
	}

	public String getHungamaDownloadServerUrl() {
		return mHungamaDownloadServerUrl;
	}

	public String getHungamaAuthKey() {
		return mHungamaAuthKey;
	}

	public String getmHungamaVersionCheckServerUrl() {
		return mHungamaVersionCheckServerUrl;
	}
	
}
