package com.hungama.myplay.activity.operations.catchmedia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import android.content.Context;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationOperation;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.ServerCommandsManager;
import com.hungama.myplay.activity.util.Logger;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;

/**
 * Decorator Operation for adding support of the Json-RPC2 protocol for CM requests / responses.
 */
public class CMDecoratorOperation extends CommunicationOperation {
	
	private static final String TAG = "CMDecoratorOperation";
	
	private static final String JSON_RPC2_ID = "jsonrpc";
	/*
	 * Responses from CM servers are build like: "error","result","id" 
	 */
	private static final String REPONSE_KEY_RESULT = "result";
	private static final String REPONSE_KEY_ERROR = "error";
	private static final String REPONSE_KEY_ID = "id";
	private static final String REPONSE_KEY_DATA = "data";
	
	private static final String SERVER_COMMANDS = "server_commands";
	
	private final JSONParser mJSONParser = new JSONParser();
	
	private final String mServerUrl;
	private final CMOperation mCMOperation;
	
	public CMDecoratorOperation(String serverUrl, CMOperation cmOperation) {
		mServerUrl = serverUrl;
		mCMOperation = cmOperation;
	}

	@Override
	public int getOperationId() {
		return mCMOperation.getOperationId();
	}

	@Override
	public RequestMethod getRequestMethod() {
		return mCMOperation.getRequestMethod();
	}

	@Override
	public String getServiceUrl(final Context context) {
		return (mServerUrl + mCMOperation.getServiceUrl(context));
	}

	@Override
	public String getRequestBody() {

		List<Map<String, Object>> requestBodyMap = new ArrayList<Map<String, Object>>();
		requestBodyMap.add(mCMOperation.getCredentials());
		requestBodyMap.add(mCMOperation.getDescriptor());
		
		JSONRPC2Request request = new JSONRPC2Request(
				mCMOperation.getMethod().toString(), requestBodyMap, JSON_RPC2_ID);

		return request.toString();
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
					InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		//Logger.i(TAG, response);
		
		try {
			Map<String, Object> responseRootMap = (Map<String, Object>) mJSONParser.parse(response);
			
			if (!responseRootMap.containsKey(REPONSE_KEY_RESULT)) {
				throw new InvalidResponseDataException("Result is empty.");
			}
			
			Map<String, Object> resultMap = (Map<String, Object>) responseRootMap.get(REPONSE_KEY_RESULT);
			
			int code = ((Long) resultMap.get(CMOperation.CODE)).intValue();
			String message = (String) resultMap.get(CMOperation.MESSAGE);
			
			switch (code) {
			case CMOperation.ERROR_CODE_GENERAL:
				throw new InvalidResponseDataException(message);
				
			case CMOperation.ERROR_CODE_THIRD_PARTY_AUTH_INVALID:
				throw new InvalidResponseDataException("Error: " + 
							Integer.toString(CMOperation.ERROR_CODE_THIRD_PARTY_AUTH_INVALID) + " message: " + message);
				
			case CMOperation.ERROR_CODE_PLAYLIST_WITH_THE_SAME_EXIST:
				throw new InvalidResponseDataException(message);
				
			case CMOperation.ERROR_CODE_SESSION:
				/*
				 * Performs creation of the session again,
				 */
				CommunicationManager communicationManager = new CommunicationManager();
				try {
					// gets the new session for the user.
					Map<String, Object> result = 
							communicationManager.performOperation(
									new CMDecoratorOperation(mServerUrl, new SessionCreateOperation(mCMOperation.getContext())),mCMOperation.getContext());
				} catch (InvalidRequestException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed recreating session id!");
					throw new InvalidRequestParametersException();
				} catch (NoConnectivityException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed recreating session id due to connectivity error!");
					throw new InvalidRequestParametersException();
				}
				
				/*
				 * if no session was given in response, the SessionCreateOperation
				 * where thrown an exception for that.
				 * So reaching here means that we have got one and it was stored in the configuration.
				 * 
				 * Recreating the original operation that was failed for that.
				 */
				try {
					Map<String, Object> operationResult = communicationManager.performOperation(
							new CMDecoratorOperation(mServerUrl, mCMOperation),mCMOperation.getContext());
					
					return operationResult;
					
				} catch (InvalidRequestException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed recalling operation!");
					throw new InvalidRequestParametersException();
					
				} catch (NoConnectivityException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed recalling operation due to connectivity error!");
					throw new InvalidRequestParametersException();
				}
			}
			
			if (!resultMap.containsKey(REPONSE_KEY_DATA)) {
				throw new InvalidResponseDataException("Result is empty.");
			}
			
			if(resultMap.containsKey(SERVER_COMMANDS)){
				
				List<Map<String, String>> serverCommands = 
						(List<Map<String, String>>) resultMap.get(SERVER_COMMANDS);
				
				ServerCommandsManager.sendServerCommands(mCMOperation.getContext(), serverCommands);
			}
			
			/*
			 * Some of the web services retrieves the responses as Map and some as List.
			 */
			Object reslutObject = resultMap.get(REPONSE_KEY_DATA);
			
			if (reslutObject instanceof Map) {
				return mCMOperation.parseResponse(JSONValue.toJSONString((Map<String, Object>) reslutObject));
			} else if(reslutObject instanceof List) {
				return mCMOperation.parseResponse(JSONValue.toJSONString((List<Map<String, Object>>) reslutObject));
			} else {
				return mCMOperation.parseResponse(JSONValue.toJSONString((Map<String, Object>) resultMap));
			}
			  
			
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new InvalidResponseDataException(exception.getMessage());
		}
	}
	
	
}
