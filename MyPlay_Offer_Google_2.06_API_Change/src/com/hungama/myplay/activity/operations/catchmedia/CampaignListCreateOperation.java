package com.hungama.myplay.activity.operations.catchmedia;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Retrieves device ID from CM servers, based on the device's properties.
 */
public class CampaignListCreateOperation extends CMOperation {
	
	private static final String TAG = "CampaignListCreateOperation";
	
	public static final String RESPONSE_KEY_OBJECT_CAMPAIGN_LIST = "response_key_campaign_list";
	
	public CampaignListCreateOperation(Context context) {
		super(context);
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.READ;
	}
	
	@Override
	protected Map<String, Object> getCredentials() {
		
		Map<String, Object> credentials = new HashMap<String, Object>();
				
		credentials.put(ServerConfigurations.APPLICATION_CODE, pServerConfigurations.getAppCode());
		credentials.put(ServerConfigurations.APPLICATION_VERSION, pServerConfigurations.getAppVersion());
		credentials.put(DeviceConfigurations.HARDWARE_ID, pDeviceConfigurations.getHardwareId());
		credentials.put(ApplicationConfigurations.SESSION_ID, pApplicationConfigurations.getSessionID());
		
		return credentials;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.CAMPAIGN_LIST_READ;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException, 
				InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		JSONParser jsonParser = new JSONParser();
		
		try {
			
			Map<String, Object> campainListMap = (Map<String, Object>) jsonParser.parse(response);
			
			Type listType = new TypeToken<ArrayList<String>>() {}.getType();
			Gson gsonParser = new Gson();
			
			List<String> campaignList = gsonParser.fromJson(campainListMap.get("campaign_ids").toString(), listType);
			
			Map<String, Object> responseMap = new HashMap<String, Object>();
			responseMap.put(RESPONSE_KEY_OBJECT_CAMPAIGN_LIST, campaignList);
			
			return responseMap;
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("Device map parsing error.");
		}
	}

	@Override
	public Map<String, Object> getDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

}
