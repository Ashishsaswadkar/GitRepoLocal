package com.hungama.myplay.activity.operations.catchmedia;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.SignOption;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class PartnerInfoReadOperation extends CMOperation {
	
	private static final String TAG = "PartnerInfoReadOperation";
	
	public static final String RESPONSE_KEY_OBJECT_SIGN_OPTIONS = "response_key_sign_options";

	public PartnerInfoReadOperation(Context context) {
		super(context);
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.READ_ALL;
	}

	@Override
	public Map<String, Object> getDescriptor() {
		
		Map<String, Object> descriptorMap = new HashMap<String, Object>();

		descriptorMap.put(ServerConfigurations.PARTNER_ID, pServerConfigurations.getPartnerId());
		descriptorMap.put(ServerConfigurations.APPLICATION_CODE, pServerConfigurations.getAppCode());
		
		return descriptorMap;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.PARTNER_INFO_READ;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {

		Type listType = new TypeToken<ArrayList<SignOption>>() {}.getType();
		Gson gsonParser = new Gson();
		
		try {
			List<SignOption> signOptions = gsonParser.fromJson(response, listType);
		
		
		// if there are no sign options, it means there is an error.
		if (signOptions == null || signOptions.size() == 0) {
			throw new InvalidResponseDataException("No sign options retrieved from response!");
		}
		
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put(RESPONSE_KEY_OBJECT_SIGN_OPTIONS, signOptions);
		
		return responseMap;
		
		} catch (JsonParseException exception) {
			exception.printStackTrace();
			throw new InvalidResponseDataException("Error parsing the response!");
		} catch (IllegalStateException exception) {
			exception.printStackTrace();
			throw new InvalidResponseDataException("Error parsing the response!");
		}
	}

}
