package com.hungama.myplay.activity.operations.hungama;

import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;

import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class ForgotPasswordOperation extends HungamaOperation {
	
	public static final String RESPONSE_KEY_CODE = "code";
	public static final String RESPONSE_KEY_MESSAGE = "message";
	
	private final String mServiceUrl;
	private final String mForgotPasswordAuthKey;
	private final String mUserEmail;
	
	public ForgotPasswordOperation(String serviceUrl, String forgotPasswordAuthKey, String userEmail) {
		mServiceUrl = serviceUrl;
		mForgotPasswordAuthKey = forgotPasswordAuthKey;
		mUserEmail = userEmail;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.FORGOT_PASSWORD;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serviceUrl = mServiceUrl + "?" + "email=" + mUserEmail +
				"&key=" + mForgotPasswordAuthKey +
				"&format=json";
		return serviceUrl;
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
		try {
			Map<String, Object> responseMap = (Map<String, Object>) jsonParser.parse(response);
			responseMap = (Map<String, Object>) responseMap.get("response");
			return responseMap;
			
		} catch (ParseException e) { e.printStackTrace(); throw new InvalidResponseDataException(); }
	}

}
