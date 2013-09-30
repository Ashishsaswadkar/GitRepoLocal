package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.DownloadOperationType;
import com.hungama.myplay.activity.data.dao.hungama.DownloadResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class DownloadOperation extends HungamaOperation {
	
	private static final String TAG = "DownloadOperation";

	public static final String RESPONSE_KEY_DOWNLOAD = "response_key_download";

	private static final String CONTENT_TYPE = "content_type";	
	
	private final String mServerUrl;	
	private final String mUserId;
	private final String mPlanId;
	private final String mContentId;
	private final String mContentType; //(audio / video)
	private final String mDevice;
	private final String mSize;
	private final String mAuthKey;
	private final String mMsisdn;
	private final DownloadOperationType mDownloadType;
	private final String mAffiliateId;
	private final String mHardwareId;
	
	String urlParams = null;
	
	public DownloadOperation(String serverUrl, String userId, String msisdn, String planId, String contentId, String contentType, String device, String size, DownloadOperationType downloadOperationType, String authKey, String affiliateId, String hardwareId) {
		mServerUrl = serverUrl;
		mUserId = userId;
		mPlanId = planId;
		mContentId = contentId;
		mContentType = contentType; 
		mDevice = device;
		mSize = size;
		mAuthKey = authKey;
		mDownloadType = downloadOperationType;
		mMsisdn = msisdn;
		mAffiliateId = affiliateId;
		mHardwareId = hardwareId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.DOWNLOAD;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		String segmentType = null;
		
		if (mDownloadType == DownloadOperationType.DOWNLOAD_COUNT) {
			segmentType = HungamaOperation.URL_SEGMENT_DOWNLOAD_COUNT;
			urlParams = PARAMS_AUTH_KEY + HungamaOperation.EQUALS + mAuthKey + HungamaOperation.AMPERSAND +
						PARAMS_USER_ID +  HungamaOperation.EQUALS + mUserId + HungamaOperation.AMPERSAND +
						PARAMS_CONTENT_ID +  HungamaOperation.EQUALS + mContentId + HungamaOperation.AMPERSAND +
						PARAMS_HARDWARE_ID +  HungamaOperation.EQUALS + mHardwareId;
			Log.i(TAG, urlParams);
		} else if (mDownloadType == DownloadOperationType.BUY_PLANS) {
			segmentType = HungamaOperation.URL_SEGMENT_BUY_PLANS;
			urlParams = PARAMS_AUTH_KEY + HungamaOperation.EQUALS + mAuthKey + HungamaOperation.AMPERSAND +
						PARAMS_USER_ID +  HungamaOperation.EQUALS + mUserId + HungamaOperation.AMPERSAND +
						PARAMS_CONTENT_ID +  HungamaOperation.EQUALS + mContentId + HungamaOperation.AMPERSAND +
						PARAMS_HARDWARE_ID +  HungamaOperation.EQUALS + mHardwareId;
		} else if (mDownloadType == DownloadOperationType.BUY_CHARGE) {
			segmentType = HungamaOperation.URL_SEGMENT_BUY_CHARGE;
			urlParams = PARAMS_AUTH_KEY + HungamaOperation.EQUALS + mAuthKey + HungamaOperation.AMPERSAND +
						PARAMS_USER_ID +  HungamaOperation.EQUALS + mUserId + HungamaOperation.AMPERSAND +						
						PARAMS_CONTENT_ID +  HungamaOperation.EQUALS + mContentId + HungamaOperation.AMPERSAND +
						PARAMS_PLAN_ID +  HungamaOperation.EQUALS + mPlanId + HungamaOperation.AMPERSAND +
						PARAMS_MSISDN +  HungamaOperation.EQUALS + mMsisdn + HungamaOperation.AMPERSAND +
						PARAMS_MEDIA_TYPE + HungamaOperation.EQUALS + mContentType + HungamaOperation.AMPERSAND +
						PARAMS_AFFILIATE_ID +  HungamaOperation.EQUALS + mAffiliateId + HungamaOperation.AMPERSAND +
						PARAMS_HARDWARE_ID +  HungamaOperation.EQUALS + mHardwareId;
			if (mContentType == "redeem") {
				urlParams += HungamaOperation.AMPERSAND + "redeem" +  HungamaOperation.EQUALS + "1";
			}//Hungama
		} else if (mDownloadType == DownloadOperationType.CONTENT_DELIVERY) {
			segmentType = HungamaOperation.URL_SEGMENT_CONTENT_DELIVERY;
			urlParams = PARAMS_AUTH_KEY + HungamaOperation.EQUALS + mAuthKey + HungamaOperation.AMPERSAND +
						PARAMS_USER_ID +  HungamaOperation.EQUALS + mUserId + HungamaOperation.AMPERSAND +
						PARAMS_CONTENT_ID +  HungamaOperation.EQUALS + mContentId + HungamaOperation.AMPERSAND +
						CONTENT_TYPE +  HungamaOperation.EQUALS + mContentType + HungamaOperation.AMPERSAND +
						PARAMS_DEVICE +  HungamaOperation.EQUALS + mDevice + HungamaOperation.AMPERSAND +
						PARAMS_SIZE +  HungamaOperation.EQUALS + mSize + HungamaOperation.AMPERSAND +
						PARAMS_HARDWARE_ID +  HungamaOperation.EQUALS + mHardwareId;
		}
		
		return mServerUrl + 
				segmentType;
	}

	@Override
	public String getRequestBody() {
		return urlParams;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
							InvalidRequestParametersException, InvalidRequestTokenException,
							OperationCancelledException {

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
		
		try {
				response = response.replace("{\"response\":", "");
				response = response.substring(0, response.length() -1);
				
				Logger.i(TAG, response);			
				
				DownloadResponse downloadResponse = (DownloadResponse) gson.fromJson(response, DownloadResponse.class);
				downloadResponse.setDownloadType(mDownloadType);
				resultMap.put(RESPONSE_KEY_DOWNLOAD, downloadResponse);
			
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();
			
		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		}
					
			
		return resultMap;
	}

}
