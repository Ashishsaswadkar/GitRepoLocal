package com.hungama.myplay.activity.services;

import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

//import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Plan;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionType;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionOperation;
import com.hungama.myplay.activity.ui.OnApplicationStartsActivity;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * Checks if the User is subscribed to the application and caches his subscription plans.
 */
public class SubscriptionService extends IntentService {

	private static final String TAG = "SubscriptionService";
	
	private DataManager mDataManager;
	
	private ApplicationConfigurations mApplicationConfigurations;
	
	private String mHungamaSubscriptionServerUrl;
	private String mPartnerUserId;
	private String mAuthKey;
	
	public SubscriptionService() {
		super(TAG);
	}
	
	public SubscriptionService(String name) {
		super(TAG);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mDataManager = DataManager.getInstance(getApplicationContext());
		
		ServerConfigurations serverConfigurations = mDataManager.getServerConfigurations();
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		
		mHungamaSubscriptionServerUrl = serverConfigurations.getHungamaSubscriptionServerUrl();
		mPartnerUserId = mApplicationConfigurations.getPartnerUserId();
		mAuthKey = serverConfigurations.getHungamaAuthKey();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		Logger.i(TAG, "Getting subscription plans");
		AccountManager accountManager = AccountManager.get(getApplicationContext());
		Account[] accounts = accountManager.getAccountsByType("com.google");

		CommunicationManager communicationManager = new CommunicationManager();
		SubscriptionCheckOperation subscriptionCheckOperation = null;
		
		if(accounts != null && accounts.length > 0){
			String accountType = accounts[0].name; 
		
			subscriptionCheckOperation = new SubscriptionCheckOperation(
					getApplicationContext(), 
					mHungamaSubscriptionServerUrl, 
					mPartnerUserId, mAuthKey, accountType);
		}else{
			
			//Toast.makeText(getApplicationContext(),  "There is no Google account on this device", Toast.LENGTH_LONG).show();
			return;
		}
		
		SubscriptionOperation subscriptionOperation = new SubscriptionOperation(mHungamaSubscriptionServerUrl, 
														   String.valueOf(0), Utils.TEXT_EMPTY, mPartnerUserId, SubscriptionType.PLAN, mAuthKey,OnApplicationStartsActivity.getAffiliateId(),null,null, null);
		
		try {
			
			/*
			 * Checks if the user has got already subscription plan - is subscribed, if not gets it.
			 */
			SubscriptionCheckResponse oldSubscriptionCheckResponse = mDataManager.getStoredCurrentPlan();
			if (oldSubscriptionCheckResponse == null) {
				
				// checks if the user is subscribed.
				Map<String, Object> currentSubscriptionPlanResult = communicationManager.performOperation(subscriptionCheckOperation,getApplicationContext());
				
				SubscriptionCheckResponse subscriptionCheckResponse = 
						(SubscriptionCheckResponse) currentSubscriptionPlanResult.get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);
				
				
				// if store in cache succeeded - set UserHasSubscriptionPlan=true and plan validity date in ApplicationConfigurations
				if (mDataManager.storeSubscriptionCurrentPlan(subscriptionCheckResponse)) {
					
					mApplicationConfigurations.setIsUserHasSubscriptionPlan(true);
					mApplicationConfigurations.setUserSubscriptionPlanDate(subscriptionCheckResponse.getPlan().getValidityDate());
				} else {
					mApplicationConfigurations.setIsUserHasSubscriptionPlan(false);
				}
			}
			
			/*
			 * Checks if there are subscription plans available for the user, if not gets them.
			 */
			List<Plan> subscriptionPlans = mDataManager.getStoredSubscriptionPlans();
			
			if (Utils.isListEmpty(subscriptionPlans)) {
				
				Map<String, Object> SubscriptionPlansResult = communicationManager.performOperation(subscriptionOperation,getApplicationContext());
				
				SubscriptionResponse subscriptionResponse = 
						(SubscriptionResponse) SubscriptionPlansResult.get(SubscriptionOperation.RESPONSE_KEY_SUBSCRIPTION);
				
				if (subscriptionResponse != null  && subscriptionResponse.getPlan() != null && subscriptionResponse.getPlan().size() > 0) {
					if (subscriptionResponse.getSubscriptionType() == SubscriptionType.PLAN) {
						mDataManager.storeSubscriptionPlans(subscriptionResponse.getPlan());
					}
				}
			}
		
		} catch (InvalidRequestException e) {
			e.printStackTrace();
		} catch (InvalidResponseDataException e) {
			e.printStackTrace();
		} catch (OperationCancelledException e) {
			e.printStackTrace();
		} catch (NoConnectivityException e) {
			e.printStackTrace();
		}
	}

}
