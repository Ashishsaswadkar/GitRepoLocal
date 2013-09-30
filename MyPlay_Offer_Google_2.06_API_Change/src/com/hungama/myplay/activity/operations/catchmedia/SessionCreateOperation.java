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
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

/**
 * Retrieves a new Session ID for the user and stores it in {@link ApplicationConfigurations}.
 */
public class SessionCreateOperation extends CMOperation {
	// love cookies.
	
	private static final String TAG = "SessionCreateOperation";
	
	public SessionCreateOperation (Context context) {
		super(context);
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.CREATE;
	}
	
	@Override
	protected Map<String, Object> getCredentials() {
		
		Map<String, Object> credentialsMap = new HashMap<String, Object>();
		
		credentialsMap.put(ServerConfigurations.LC_ID, pServerConfigurations.getLcId());
		credentialsMap.put(ServerConfigurations.PARTNER_ID, pServerConfigurations.getPartnerId());
		credentialsMap.put(ServerConfigurations.WEB_SERVER_VERSION, pServerConfigurations.getWebServiceVersion());
		credentialsMap.put(ServerConfigurations.APPLICATION_VERSION, pServerConfigurations.getAppVersion());
		credentialsMap.put(ServerConfigurations.APPLICATION_CODE, pServerConfigurations.getAppCode());
		credentialsMap.put(DeviceConfigurations.TIMESTAMP, pDeviceConfigurations.getTimeStamp());
		
		return credentialsMap;
	}

	@Override
	public Map<String, Object> getDescriptor() {
		
		Map<String, Object> descriptorMap = new HashMap<String, Object>();
		
		descriptorMap.put(ApplicationConfigurations.PASSKEY, pApplicationConfigurations.getPasskey());
		descriptorMap.put(ApplicationConfigurations.CONSUMER_ID, pApplicationConfigurations.getConsumerID());
		descriptorMap.put(ServerConfigurations.PARTNER_ID, pServerConfigurations.getPartnerId());
		descriptorMap.put(ServerConfigurations.APPLICATION_VERSION, pServerConfigurations.getAppVersion());
		descriptorMap.put(ServerConfigurations.APPLICATION_CODE, pServerConfigurations.getAppCode());
		descriptorMap.put(ApplicationConfigurations.DEVICE_ID, pApplicationConfigurations.getDeviceID());
		
		return descriptorMap;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.SESSION_CREATE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.SESSION;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
									InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		// extracts the session id from the response, checks for its existance.
		JSONParser parser = new JSONParser();
		try {
			Map<String, Object> reponseMap = (Map<String, Object>) parser.parse(response);
			
			if (reponseMap.containsKey(ApplicationConfigurations.SESSION_ID)) {
				
				// stores the session.
				String sessionID = (String) reponseMap.get(ApplicationConfigurations.SESSION_ID);
				pApplicationConfigurations.setSessionID(sessionID);
				
				return reponseMap;
			} else {
				Logger.e(TAG, "No Session was retrieved from CM!");
				throw new InvalidResponseDataException();
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}


}
