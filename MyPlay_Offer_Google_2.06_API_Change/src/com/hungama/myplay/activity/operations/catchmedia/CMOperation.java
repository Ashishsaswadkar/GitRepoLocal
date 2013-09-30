package com.hungama.myplay.activity.operations.catchmedia;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.hungama.myplay.activity.communication.CommunicationOperation;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;

/**
 * Abstract class defining additional behavior of a {@link CommunicationOperation} to be supported
 * by Json-RPC2 protocol and CM servers.
 */
public abstract class CMOperation extends CommunicationOperation {
	
	public static final String CODE = "code";
	public static final String MESSAGE = "message";
	
	/*
	 * public definitions of error codes that can been retrieved from CM. 
	 */
	public static final int CODE_SUCCESS = 200;
	public static final int ERROR_CODE_SESSION = 503;
	public static final int ERROR_CODE_GENERAL = 500;
	public static final int ERROR_CODE_THIRD_PARTY_AUTH_INVALID = 506;
	public static final int ERROR_CODE_PLAYLIST_WITH_THE_SAME_EXIST = 642;
	
	public static final String RESPONSE_KEY_GENERAL_OBJECT = "response_key_general_object";
	
	private Context mContext;
	protected DataManager pDataManager;
	
	protected ServerConfigurations pServerConfigurations;
	protected DeviceConfigurations pDeviceConfigurations;
	protected ApplicationConfigurations pApplicationConfigurations;
	
	public CMOperation(Context context) {
		mContext = context.getApplicationContext();
		pDataManager = DataManager.getInstance(mContext);
		
		pServerConfigurations = pDataManager.getServerConfigurations();
		pDeviceConfigurations = pDataManager.getDeviceConfigurations();
		pApplicationConfigurations = pDataManager.getApplicationConfigurations();
	}
	
	public Context getContext() {
		return mContext;
	}
	
	public abstract JsonRPC2Methods getMethod();
	
	/**
	 * Retrieves credentials of the operation, that will be transformed to be part of the request's body content. 
	 * @return descriptor.
	 */
	protected Map<String, Object> getCredentials() {
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		// default credentials.
        
        params.put(ServerConfigurations.LC_ID, pServerConfigurations.getLcId());
        params.put(ServerConfigurations.PARTNER_ID, pServerConfigurations.getPartnerId());
        params.put(ServerConfigurations.WEB_SERVER_VERSION, pServerConfigurations.getWebServiceVersion());
        params.put(DeviceConfigurations.TIMESTAMP, pDeviceConfigurations.getTimeStamp());
                       
        return params;		
	}

	/**
	 * Retrieves descriptor of the operation, that will be transformed to be part of the request's body content. 
	 * @return descriptor.
	 */
	public abstract Map<String, Object> getDescriptor();

}
