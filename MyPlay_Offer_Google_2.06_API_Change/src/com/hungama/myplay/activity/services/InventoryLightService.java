package com.hungama.myplay.activity.services;

import java.util.ArrayList;
import java.util.Map;
import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

//import com.hungama.myplay.activity.R.string;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.persistance.Itemable;
import com.hungama.myplay.activity.inventory.InventoryLightOperation;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.util.Logger;

/**
 * Background service for getting Catchmedia's InventoryLight
 */
public class InventoryLightService extends IntentService {
	
	private static final String TAG = "InventoryLightService";
	
	private static final String LAST_PAGE = "last_page";
	private static final String DATA = "data";
	private static final String ACTION = "action";
	private static final String ID = "id";
	private static final String NAME = "name";
	
	// Actions type
	public static final String ADD = "Add";
	public static final String DEL = "Del";
	public static final String MOD = "Mod";
	
	private static final String TRACKS = "tracks";
	private static final String PLAYLISTS = "playlists";
	
	private DataManager mDataManager;
	private ServerConfigurations mServerConfigurations;
	protected ApplicationConfigurations pApplicationConfigurations;
	
	public InventoryLightService() {
		super(TAG);
	}

	public InventoryLightService(String name) {
		super(TAG);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mDataManager = DataManager.getInstance(getApplicationContext());
		
		mServerConfigurations = mDataManager.getServerConfigurations();
		pApplicationConfigurations = mDataManager.getApplicationConfigurations();
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Logger.i(TAG, "Starts InventoryLight Service.");
		
		CommunicationManager communicationManager = new CommunicationManager();
		
		int consumerRevision = 0;
		int consumerServerRevision = 0;
		
		int householdRevision = 0;
		int householdServerRevision = 0;
		
		boolean lastPage = true;
		
		do{
			
			try {
				Map<String, Object> resultMap = communicationManager.performOperation(
						new CMDecoratorOperation(
								mServerConfigurations.getServerUrl(), 
								new InventoryLightOperation(this)),getApplicationContext());
				
				// Update Data
				updateInventory(resultMap);
				
				// Assign revisions 
				Map<String,Object> meta = (Map<String, Object>) resultMap.get("meta");
				consumerRevision = ((Long) meta.get(ApplicationConfigurations.CONSUMER_REVISION)).intValue();
				consumerServerRevision = ((Long) meta.get(ApplicationConfigurations.CONSUMER_SERVER_REVISION)).intValue();

				householdRevision = ((Long) meta.get(ApplicationConfigurations.HOUSEHOLD_REVISION)).intValue();
				householdServerRevision = ((Long) meta.get(ApplicationConfigurations.HOUSEHOLD_SERVER_REVISION)).intValue();

				lastPage = (Boolean) meta.get(LAST_PAGE);
				
				pApplicationConfigurations.setConsumerRevision(consumerRevision);
				pApplicationConfigurations.setHouseholdRevision(householdRevision);
				
			} catch (InvalidRequestException e) { 
				e.printStackTrace(); Logger.e(TAG, "Failed in InventoryLight.");
			} catch (InvalidResponseDataException e) {
				e.printStackTrace(); Logger.e(TAG, "Failed InventoryLight.");
			} catch (OperationCancelledException e) {
				e.printStackTrace(); Logger.e(TAG, "Failed InventoryLight.");
			} catch (NoConnectivityException e) {
				e.printStackTrace(); Logger.e(TAG, "Failed InventoryLight."); 
			} 
			
		}while((consumerRevision < consumerServerRevision) && (householdRevision < householdServerRevision) && lastPage);
		
		Logger.i(TAG, "Done InventoryLight Service.");
	}
	
	public boolean updateInventory(Map<String, Object> resultMap){
		
		Map<String, ArrayList<Map<String, Object>>> data = 
				(Map<String, ArrayList<Map<String, Object>>>) resultMap.get(DATA);
		
		boolean updateResult = true;
		boolean tmp;
				
		ArrayList<Map<String, Object>> playlists = data.get(PLAYLISTS);
		
		if(playlists != null && !playlists.isEmpty()){
			tmp = updateMediaItem(playlists,PLAYLISTS);
			if(!tmp){
				updateResult = false;
			}
		}
		
		ArrayList<Map<String, Object>> tracks = data.get(TRACKS);
		
		if(tracks != null && !tracks.isEmpty()){
			updateTracks(tracks, TRACKS);
		}
		
		return updateResult;

	}
	
	public Playlist getItemableType(String type){
		
		if(type.equalsIgnoreCase(PLAYLISTS)){
			return new Playlist();
		}else{
			return null;
		}
	}
	
	/**
	 * Insert/Delete/Update MediaItem PlayLists in cache 
	 * @param arr
	 */
	public boolean updateMediaItem(ArrayList<Map<String, Object>> arr, String type){
		
		boolean updateResult = true;
		
		if(arr != null && !arr.isEmpty()){
			
			Playlist itemable = getItemableType(type);
			
			for(Map<String, Object> map : arr){
				
				if(map != null && !map.isEmpty()){
					
					String action = (String) map.get(ACTION);
					
					if(action == null){
						// If there is no Action then "Add" is the default action
						action = ADD;
					}
					
					boolean tmp = true;
					if(itemable instanceof Playlist){
						// CatchMedia PlayList
						itemable = itemable.getInitializedObject(map);
						tmp = mDataManager.updateItemable((Playlist) itemable, action);
					}
					
					if(!tmp){
						updateResult = false;
					}
				}
			}
		}
		
		return updateResult;
	}
	
	public boolean updateTracks(ArrayList<Map<String, Object>> arr, String type){
		
		boolean updateResult = true;
		
		if(arr != null && !arr.isEmpty()){
			
			for(Map<String, Object> map : arr){
				
				if(map != null && !map.isEmpty()){
					
					String trackID = (String) map.get(ID);
					String trackName = (String) map.get(NAME);
					
					if(!TextUtils.isEmpty(trackID)){
						updateResult = mDataManager.updateTracks(trackID, trackName);
					}
				}
			}
		}
		
		return updateResult;
		
	}
}
