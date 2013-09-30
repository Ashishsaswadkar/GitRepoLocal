package com.hungama.myplay.activity.operations.hungama;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.hungama.myplay.activity.util.Utils;

public class FeedbackSubjectsOperation extends HungamaOperation {
	
	private static final String TAG = "FeedbackSubjectsOperation";
	
	public static final String RESULT_OBJECT_SUBJECTS_LIST = "result_object_subjects_list"; 
	
	
	private static final String KEY_SUBJECT = "subject";
	private static final String KEY_TITLE = "title";
	
	private final String mServerUrl;
	private final String mAuthKey;
	
	public FeedbackSubjectsOperation(String serverUrl, String authKey) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.FEEDBACK_SUBJECTS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String uri = mServerUrl + URL_SEGMENT_FEEDBACK_SUBJECTS
				+ PARAMS_AUTH_KEY + EQUALS + mAuthKey;
		return uri;
	}

	@Override
	public String getRequestBody() {
		return null;
	}
	
	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException, InvalidRequestParametersException, 
																	 InvalidRequestTokenException, OperationCancelledException {
		
		JSONParser jsonParser = new JSONParser();
		
		try {
			Map<String, Object> catalogMap = (Map<String, Object>) jsonParser.parse(response);
			// gets the "actual" catalog.
			if (!catalogMap.containsKey(KEY_CATALOG)) {
				throw new InvalidResponseDataException(TAG + " Catalog is missing, not as defined!!!!");
			}
			
			catalogMap = (Map<String, Object>) catalogMap.get(KEY_CATALOG);

			// gets the "actual" subjects.
			if (!catalogMap.containsKey(KEY_SUBJECT)) {
				throw new InvalidResponseDataException(TAG + " Subject is missing, not as defined!!!!");
			}
			
			List<Map<String, Object>> subjectMapList = (List<Map<String, Object>>) catalogMap.get(KEY_SUBJECT);
			
			if (Utils.isListEmpty(subjectMapList)) {
				throw new InvalidResponseDataException(TAG + " Ohh No!!! the List of Subjects is empty!!!! We can't test our new large hadron collider.");
			}
			
			List<String> subjects = new ArrayList<String>();
			String subject = null;
			
			for (Map<String, Object> subjectMap : subjectMapList) {
				subject = (String) subjectMap.get(KEY_TITLE);
				subjects.add(subject);
			}
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_OBJECT_SUBJECTS_LIST, subjects);
			
			return resultMap;
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

}
