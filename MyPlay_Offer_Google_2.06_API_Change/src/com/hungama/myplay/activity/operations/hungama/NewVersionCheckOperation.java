package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.MobileOperationType;
import com.hungama.myplay.activity.data.dao.hungama.NewVersionCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.VersionCheckResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Utils;

public class NewVersionCheckOperation extends HungamaOperation {
	
	private static final String TAG = "NewVersionCheckOperation";

	public static final String RESPONSE_KEY_VERSION_CHECK = "response_key_version_check";
	
	private final String mServerUrl;	
	private final String mPackageName;
	private final String mClientVersion;	
	
	private final String KEY_UPDATE = "update";
	
	public NewVersionCheckOperation(String serverUrl, String packageName, String clientVersion) {
		mServerUrl = serverUrl;
		mPackageName = packageName;
		mClientVersion = clientVersion;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.NEW_VERSION_CHECK;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String urlParams = null;
				
		urlParams = PARAMS_CLIENT + HungamaOperation.EQUALS + mPackageName + 
					HungamaOperation.AMPERSAND +
					PARAMS_CLIENT_VERSION + HungamaOperation.EQUALS + mClientVersion;
		
		return mServerUrl + "?" + urlParams;			
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
							InvalidRequestParametersException, InvalidRequestTokenException,
							OperationCancelledException {

		JSONParser jsonParser = new JSONParser();
		Gson gson = new Gson();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
		
		try {
//				response = response.replace("{\"response\":", "");
//				response = response.substring(0, response.length() -1);
				if (!response.equalsIgnoreCase(Utils.TEXT_EMPTY)) {
					Map<String, Object> responseMap = (Map<String, Object>) jsonParser.parse(response);
	
					if (responseMap.containsKey(KEY_UPDATE)) {
						response = responseMap.get(KEY_UPDATE).toString();
						NewVersionCheckResponse newVersionCheckResponse = (NewVersionCheckResponse) gson.fromJson(response, NewVersionCheckResponse.class);
						resultMap.put(RESPONSE_KEY_VERSION_CHECK, newVersionCheckResponse);
					} else {
						resultMap = null;
					}
				}

			
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();
			
		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		} catch (ParseException e) {
			Log.i(TAG, e.getMessage());
			e.printStackTrace();
		}
					
			
		return resultMap;
	}

}
