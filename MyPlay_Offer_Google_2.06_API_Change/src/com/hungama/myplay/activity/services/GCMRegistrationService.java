package com.hungama.myplay.activity.services;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationOperation;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.operations.hungama.GCMRegIDOperation;
import com.hungama.myplay.activity.operations.hungama.HungamaWrapperOperation;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Performs the application registration to Google Cloud Message services.
 */
public class GCMRegistrationService extends IntentService {
	
	private static final String TAG = "GCMRegistrationService";
	
	public static final String EXTRA_REGISTRATION_ID = "extra_registration_id";
	
	private String mHungamaServerUrl;
	private String mHungamaAuthKey;
	private String mPartnerUserId;
	
	public GCMRegistrationService() {
		
		super(TAG);
	}
	
	@Override
	public void onCreate() {
		
		super.onCreate();
		
		// sets the argument for the web service call.
		DataManager dataManager = DataManager.getInstance(getApplicationContext());
		
		mHungamaServerUrl = dataManager.getServerConfigurations().getHungamaServerUrl();
		mHungamaAuthKey = dataManager.getServerConfigurations().getHungamaAuthKey();
		mPartnerUserId = dataManager.getApplicationConfigurations().getPartnerUserId();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		if (intent.hasExtra(EXTRA_REGISTRATION_ID)) {
			
			String registrationId = intent.getStringExtra(EXTRA_REGISTRATION_ID);
			
			// creates the request.
			CommunicationOperation gcmRegistrationOperation = new HungamaWrapperOperation(null, getApplicationContext(), 
					new GCMRegIDOperation(mHungamaServerUrl, mHungamaAuthKey, registrationId, mPartnerUserId));
			
			// performs the operation.
	    	CommunicationManager communicationManager = new CommunicationManager();
	    	try {
				communicationManager.performOperation(gcmRegistrationOperation,getApplicationContext());
				
			} catch (InvalidRequestException e) {
				e.printStackTrace();
				Log.i(TAG, "Device Failed on registeration to GCM via Hungama");
				return;
				
			} catch (InvalidResponseDataException e) {
				e.printStackTrace();
				Log.i(TAG, "Device Failed on registeration to GCM via Hungama");
				return;
				
			} catch (OperationCancelledException e) {
				e.printStackTrace();
				Log.i(TAG, "Device Failed on registeration to GCM via Hungama");
				return;
				
			} catch (NoConnectivityException e) {
				e.printStackTrace();
				Log.i(TAG, "Device Failed on registeration to GCM via Hungama");
				return;
			}
	    	
	    	// success.
	    	Log.i(TAG, "Device has been registered to GCM via Hungama");
		}
	}

}
