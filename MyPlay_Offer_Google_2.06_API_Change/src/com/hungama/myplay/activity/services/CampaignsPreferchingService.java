package com.hungama.myplay.activity.services;

import java.util.List;
import java.util.Map;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.hungama.myplay.activity.campaigns.ForYouActivity;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.operations.catchmedia.CampaignCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.CampaignListCreateOperation;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * Background service for prefetching the campagins.
 */
public class CampaignsPreferchingService extends IntentService {
	
	private static final String TAG = "CampaignsPreferchingService";
	
	public CampaignsPreferchingService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		Logger.i(TAG, "Start prefetching campaigns.");
		
		Context applicationContext = getApplicationContext();
		DataManager dataManager = DataManager.getInstance(applicationContext);
		
		ServerConfigurations serverConfigurations = dataManager.getServerConfigurations();
		
		CommunicationManager communicationManager = new CommunicationManager();
		
		try {
			// gets the list of the campains.
			Map<String, Object> listResult = communicationManager.performOperation(
													new CMDecoratorOperation(serverConfigurations.getServerUrl(), 
													new CampaignListCreateOperation(applicationContext)),applicationContext);
			
			if (listResult != null && 
					listResult.containsKey(CampaignListCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN_LIST)) {
				
				// stores the list of campaigns.
				List<String> campaignList = (List<String>) 
						listResult.get(CampaignListCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN_LIST);
				dataManager.storeCampaignList(campaignList);
				
				// gets the campaigns themselves.
				Map<String, Object> campaignsResult = communicationManager.performOperation(
											new CMDecoratorOperation(serverConfigurations.getServerUrl(), 
											new CampaignCreateOperation(applicationContext, campaignList)),applicationContext);
				
				if (campaignsResult != null && 
						campaignsResult.containsKey(CampaignCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN)) {
					List<Campaign> campaigns = (List<Campaign>) campaignsResult.get(CampaignCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN);
					if (!Utils.isListEmpty(campaigns)) {
						
						// stores the campaigns for case use.
						dataManager.storeCampaign(campaigns);
					    
						// extracts the placements from the campaigns.
						List<Placement> radioPlacements = 
								CampaignsManager.getAllPlacementsOfType(campaigns, ForYouActivity.PLACEMENT_TYPE_RADIO);
						List<Placement> splashPlacements = 
								CampaignsManager.getAllPlacementsOfType(campaigns, ForYouActivity.PLACEMENT_TYPE_SPLASH);
						
						// Store the placements.
						dataManager.storeRadioPlacement(radioPlacements);
						dataManager.storeSplashPlacement(splashPlacements);
						
						Logger.i(TAG, "Done prefetching Campaigns!");
						
					} else {
						Logger.e(TAG, "Campaign list is empty!");
					}
					
				} else {
					Logger.e(TAG, "Campaign list is empty!");
				}
				
			} else {
				Logger.e(TAG, "Campaign list is empty!");
			}
			
		} catch (InvalidRequestException e) {
			e.printStackTrace();
			Logger.e(TAG, "Failed prefetching campaigns!");
		} catch (InvalidResponseDataException e) {
			e.printStackTrace();
			Logger.e(TAG, "Failed prefetching campaigns!");
		} catch (OperationCancelledException e) {
			e.printStackTrace();
			Logger.e(TAG, "Failed prefetching campaigns!");
		} catch (NoConnectivityException e) {
			e.printStackTrace();
			Logger.e(TAG, "Failed prefetching campaigns!");
		}
	}
	
}
