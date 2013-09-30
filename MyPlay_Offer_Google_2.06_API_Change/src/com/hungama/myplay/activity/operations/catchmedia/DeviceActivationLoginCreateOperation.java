package com.hungama.myplay.activity.operations.catchmedia;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;

import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class DeviceActivationLoginCreateOperation extends CMOperation {
	
	private static final String TAG = "DeviceActivationLoginCreateOperation";
	
	private final String mActivationCode;

	public DeviceActivationLoginCreateOperation(Context context, String activationCode) {
		super(context);
		mActivationCode = activationCode;
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.CREATE;
	}

	@Override
	public Map<String, Object> getDescriptor() {
		Map<String, Object> descriptorMap = new HashMap<String, Object>();
		
		descriptorMap.put(ServerConfigurations.APPLICATION_VERSION, pServerConfigurations.getAppVersion());
		descriptorMap.put(ServerConfigurations.PARTNER_ID, pServerConfigurations.getPartnerId());
		descriptorMap.put(ServerConfigurations.APPLICATION_CODE, pServerConfigurations.getAppCode());
		descriptorMap.put(ApplicationConfigurations.ACTIVATION_CODE, mActivationCode);
		descriptorMap.put(ApplicationConfigurations.DEVICE_ID, pApplicationConfigurations.getDeviceID());
		
		return descriptorMap;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.DEVICE_ACTIVATION_LOGIN_CREATE;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
						InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		JSONParser parser = new JSONParser();
		try {
			Map<String, Object> activationResponseMap = (Map<String, Object>) parser.parse(response);
			
			// stores the session and other crucial properties.
			String sessionID = (String) activationResponseMap.get(ApplicationConfigurations.SESSION_ID);
			int householdID = ((Long) activationResponseMap.get(ApplicationConfigurations.HOUSEHOLD_ID)).intValue();
			int consumerID = ((Long) activationResponseMap.get(ApplicationConfigurations.CONSUMER_ID)).intValue();
			String passkey = (String) activationResponseMap.get(ApplicationConfigurations.PASSKEY);
			
			pApplicationConfigurations.setSessionID(sessionID);
			pApplicationConfigurations.setHouseholdID(householdID);
			pApplicationConfigurations.setConsumerID(consumerID);
			pApplicationConfigurations.setPasskey(passkey);
			
			// sends the params in the result.
			Map<String, Object> responseMap = new HashMap<String, Object>();
			responseMap.put(RESPONSE_KEY_GENERAL_OBJECT, activationResponseMap);
			return responseMap;
			
		} catch (ParseException exception) {
			exception.printStackTrace();
		}
		
		return new HashMap<String, Object>();
	}

}
