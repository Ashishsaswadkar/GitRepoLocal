package com.hungama.myplay.activity.inventory;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.util.Log;

import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.JsonRPC2Methods;

/**
 * Retrieves device ID from CM servers, based on the device's properties.
 */
public class InventoryLightOperation extends CMOperation {
	
	private static final String TAG = "InventoryLightOperation";
	
	public static final String RESPONSE_KEY_OBJECT_INVENTORY = "response_key_inventory";
	
	public InventoryLightOperation(Context context) {
		super(context);
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.READ;
	}
	
	@Override
	public Map<String, Object> getDescriptor() {
		
		Map<String, Object> descriptor = new HashMap<String, Object>();
		
		descriptor.put(ServerConfigurations.API, pServerConfigurations.getAPI());
		descriptor.put(ServerConfigurations.FORMAT, pServerConfigurations.getFormat());
		descriptor.put(ApplicationConfigurations.CONSUMER_ID, pApplicationConfigurations.getConsumerID());
		descriptor.put(ApplicationConfigurations.HOUSEHOLD_ID, pApplicationConfigurations.getHouseholdID());
		descriptor.put(ApplicationConfigurations.MEDIA_ID_NS, pApplicationConfigurations.getMediaIdNs());
		descriptor.put(ApplicationConfigurations.PAGE_MAX, pApplicationConfigurations.getPageMax());
		descriptor.put(ApplicationConfigurations.PAGE_MIN, pApplicationConfigurations.getPageMin());
		descriptor.put(ApplicationConfigurations.PAGE_OPTIMAL, pApplicationConfigurations.getPageOptimal());
		
		descriptor.put(ApplicationConfigurations.CONSUMER_REVISION, pApplicationConfigurations.getConsumerRevision());
		descriptor.put(ApplicationConfigurations.HOUSEHOLD_REVISION, pApplicationConfigurations.getHouseholdRevision());
		
		return descriptor;
	}
	
	@Override
	protected Map<String, Object> getCredentials() {
		
		Map<String, Object> credentials = new HashMap<String, Object>();
		
		credentials.put(ServerConfigurations.APPLICATION_CODE, pServerConfigurations.getAppCode());
		credentials.put(ServerConfigurations.APPLICATION_VERSION, pServerConfigurations.getAppVersion());
		credentials.put(DeviceConfigurations.HARDWARE_ID, pDeviceConfigurations.getHardwareId());
		credentials.put(ServerConfigurations.LC_ID, pServerConfigurations.getLcId());
		credentials.put(ApplicationConfigurations.PARTNER_USER_ID, pApplicationConfigurations.getPartnerUserId());
		credentials.put(ApplicationConfigurations.SESSION_ID, pApplicationConfigurations.getSessionID());
		credentials.put(DeviceConfigurations.TIMESTAMP, pDeviceConfigurations.getTimeStamp());
		credentials.put(ServerConfigurations.WEB_SERVER_VERSION, pServerConfigurations.getWebServiceVersion());
		
		return credentials;
	}
	
	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.INVENTORY_LIGHT;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.INVENTORY_LIGHT;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException, 
				InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		Log.i(TAG, response);
		
		JSONParser jsonParser = new JSONParser();
		
		try {
			
			Map<String, Object> campainMap = (Map<String, Object>) jsonParser.parse(response);
			
//			Type listType = new TypeToken<ArrayList<Campaign>>() {}.getType();
//			Gson gsonParser = new Gson();
//			
//			List<Campaign> campaigns = gsonParser.fromJson(campainMap.get("campaigns").toString(), listType);
//			
//			Map<String, Object> responseMap = new HashMap<String, Object>();
//			responseMap.put(RESPONSE_KEY_OBJECT_PLAYLIST, campaigns);
//			
//			return responseMap;
			
			return campainMap;
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("Device map parsing error.");
		}
		
	}

}
