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

/**
 * Retrieves device ID from CM servers, based on the device's properties.
 */
public class DeviceCreateOperation extends CMOperation {
	
	public DeviceCreateOperation(Context context) {
		super(context);
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.CREATE;
	}

	@Override
	public Map<String, Object> getDescriptor() {
		
		Map<String, Object> descriptor = new HashMap<String, Object>();
		
		descriptor.put(ServerConfigurations.APPLICATION_CODE, pServerConfigurations.getAppCode());
		descriptor.put(ServerConfigurations.APPLICATION_VERSION, pServerConfigurations.getAppVersion());
		descriptor.put(ApplicationConfigurations.CLIENT_TYPE, pApplicationConfigurations.getClientType());
		descriptor.put(DeviceConfigurations.DEVICE_MODEL_NAME, pDeviceConfigurations.getDeviceModelName());
		descriptor.put(DeviceConfigurations.DEVICE_OS, pDeviceConfigurations.getDeviceOS());
		descriptor.put(DeviceConfigurations.DEVICE_OS_DESCRIPTION, pDeviceConfigurations.getDeviceOSDescription());
		descriptor.put(DeviceConfigurations.HARDWARE_ID, pDeviceConfigurations.getHardwareId());
		descriptor.put(DeviceConfigurations.HARDWARE_ID_TYPE, pDeviceConfigurations.getHardwareIdType());
		
		return descriptor;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.DEVICE_CEREATE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.DEVICE_CEREATE;
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
			Map<String, Object> deviceMap = (Map<String, Object>) jsonParser.parse(response);
			
			if (!deviceMap.containsKey(ApplicationConfigurations.DEVICE_ID) &&
					!deviceMap.containsKey(ApplicationConfigurations.EXISTING_DEVICE)) {
				throw new InvalidResponseDataException("Device map is empty.");
			}
			
			String deviceId = (String) deviceMap.get(ApplicationConfigurations.DEVICE_ID);
			String existingDevice = (String) deviceMap.get(ApplicationConfigurations.EXISTING_DEVICE);
			
			pApplicationConfigurations.setDeviceID(deviceId);
			pApplicationConfigurations.setIfDeviceExist(Boolean.getBoolean(existingDevice));
			
			return deviceMap;
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("Device map parsing error.");
		}
	}

}
