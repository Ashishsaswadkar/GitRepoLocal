package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;

import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.Response;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Saves the govern {@link Discover}on Hungama's servers, note that the result object is {@link Response}
 * and may not indicate as an error happened.
 */
public class DiscoverSaveOperation extends DiscoverOperation {
	
	private static final String TAG = "DiscoverSaveOperation";
	
	public static final String RESULT_KEY_SUCCESS_RESPONSE_OBJECT = "response_key_success_response_object";
	public static final String RESULT_KEY_RESTART_IF_SUCCESS = "result_key_restart_if_success";
	
	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	
	private final Discover mDiscover;
	private final boolean mShouldRestartIfSuccess;
	
	
	public DiscoverSaveOperation(String serverUrl, String authKey, String userId, Discover discover, boolean shouldRestartIfSuccess) {
		this.mServerUrl = serverUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mDiscover = discover;
		this.mShouldRestartIfSuccess = shouldRestartIfSuccess;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.DISCOVER_SAVE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		StringBuilder urlBuilder = new StringBuilder();
		// adds the base server url.
		urlBuilder.append(mServerUrl);
		// adds the service path.
		urlBuilder.append(URL_SEGMENT_DISCOVER_SAVE);
		// authentication properties.
		urlBuilder.append(PARAMS_AUTH_KEY).append(EQUALS).append(mAuthKey).append(AMPERSAND);
		urlBuilder.append(PARAMS_USER_ID).append(EQUALS).append(mUserId).append(AMPERSAND);
		
		urlBuilder.append(buildURLParametersFromDiscoverObject(mDiscover)).append(AMPERSAND);
		urlBuilder.append(KEY_NAME).append(EQUALS).append(mDiscover.getName());
		
		String request = urlBuilder.toString();
		request = request.replace(" ", "%20");
		
		return request;
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
			// gets the response for get the status about the saving.
			Map<String, Object> reponseMap = (Map<String, Object>) jsonParser.parse(response);
			reponseMap = (Map<String, Object>) reponseMap.get(KEY_RESPONSE); 
			
			int code = ((Long) reponseMap.get(KEY_CODE)).intValue();
			String message = (String) reponseMap.get(KEY_MESSAGE);
			int display = ((Long) reponseMap.get(KEY_DISPLAY)).intValue();
			
			Response responseObject = new Response(code, message, display);
			
			
			if (responseObject.code == Response.CODE_RESPONSE_OK) {
				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(RESULT_KEY_SUCCESS_RESPONSE_OBJECT, responseObject);
				resultMap.put(RESULT_KEY_RESTART_IF_SUCCESS, mShouldRestartIfSuccess);
				
				return resultMap;
				
			} else {
				throw new InvalidRequestParametersException(responseObject.code, responseObject.message);
			}
			
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException("No response ??");
		} catch (ParseException exception) {
			exception.printStackTrace();
			throw new InvalidResponseDataException("No response ??");
		}
	}
	
}
