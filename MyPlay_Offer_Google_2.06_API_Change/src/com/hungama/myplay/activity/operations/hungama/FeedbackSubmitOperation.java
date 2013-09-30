package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;

import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.operations.OperationDefinition.Hungama.OperationId;
import com.hungama.myplay.activity.util.Logger;

/**
 * 
 * INPUT PARAMETERS (when USER is logged in)
 * user_id
 * subject
 * app_exp
 * feed_txt
 * phone_details
 * debug_txt
 * auth_key
 *
 * INPUT PARAMETERS (when USER is not logged)
 * first_name
 * last_name
 * email
 * mobile
 * subject
 * app_exp
 * feed_txt
 * phone_details
 * debug_txt
 * auth_key
 */
public class FeedbackSubmitOperation extends HungamaOperation {
	
	private static final String TAG = "FeedbackSubmitOperation";
	
	private final String mServerUrl;
	private final String mAuthKey;
	private final Map<String, String> mFeedbackFields;
	
	public FeedbackSubmitOperation(String serverUrl, String authKey, Map<String, String> feedbackFields) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mFeedbackFields = feedbackFields;
	}

	@Override
	public int getOperationId() {
		return OperationId.FEEDBACK_SUBMIT;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		StringBuilder requestUrlBuilder = new StringBuilder();
		requestUrlBuilder.append(mServerUrl).append(URL_SEGMENT_FEEDBACK_SAVE);
		
		// build the params.
		Iterator<Entry<String, String>> iterator = mFeedbackFields.entrySet().iterator();
		Map.Entry<String, String> entry = null;
		while (iterator.hasNext()) {
			entry = (Map.Entry<String, String>) iterator.next();
			requestUrlBuilder.append(entry.getKey()).append(EQUALS).append(entry.getValue())
						 .append(AMPERSAND);
		}
		
		// adds the authentication key.
		requestUrlBuilder.append(PARAMS_AUTH_KEY).append(EQUALS).append(mAuthKey);
		
		String requestUrl = requestUrlBuilder.toString().replace(" ", "%20");
		
		return requestUrl;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException, InvalidRequestParametersException, 
																	 InvalidRequestTokenException, OperationCancelledException {
		// la la la la la la la la la la la la la la la.
		return new HashMap<String, Object>();
	}

}
