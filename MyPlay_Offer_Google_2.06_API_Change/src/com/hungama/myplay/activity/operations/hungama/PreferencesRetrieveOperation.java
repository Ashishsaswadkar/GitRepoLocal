package com.hungama.myplay.activity.operations.hungama;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.MyPreferencesResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Retrieve saved Discoveries of the application's user.
 */
public class PreferencesRetrieveOperation extends HungamaOperation {
	
	private static final String TAG = "PreferencesRetrieveOperation";
	
	public static final String RESPONSE_KEY_PREFERENCES_RETRIEVE = "response_key_preferences";
	
	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	
	public PreferencesRetrieveOperation(String serverUrl, String authKey, String userId) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.PREFERENCES_RETRIEVE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		StringBuilder serverURL = new StringBuilder();
		// adds the base server url.
		serverURL.append(mServerUrl);
		// adds the service path.
		serverURL.append(URL_SEGMENT_PREFERENCES_RETREIVE);
		// authentication properties.
		serverURL.append(PARAMS_AUTH_KEY).append(EQUALS).append(mAuthKey).append(AMPERSAND);
		serverURL.append(PARAMS_USER_ID).append(EQUALS).append(mUserId);
		
		return serverURL.toString();
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
					InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		Gson gson = new GsonBuilder().create();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
		
		try {
				response = response.replace("{\"catalog\":", "");
				response = response.substring(0, response.length() - 1);
				
				MyPreferencesResponse myPreferencesResponse = (MyPreferencesResponse) gson.fromJson(response, MyPreferencesResponse.class);
				resultMap.put(RESPONSE_KEY_PREFERENCES_RETRIEVE, myPreferencesResponse);
			
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();
			
		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		}
					
			
		return resultMap;
	}
	
//	private Mood findMoodById(int id) {
//		for (Mood mood : mCachedMoods) {
//			if (mood.getId() == id)
//				return mood;
//		}
//		return null;
//	}
//	
//	private Mood findMoodByTag(String tag) {
//		for (Mood mood : mCachedMoods) {
//			if (mood.getName().equals(tag))
//				return mood;
//		}
//		return null;
//	}
	
	private List<String> aggregateString(String stringToAggregate) {
		String [] aggregation = stringToAggregate.split(",");
		return Arrays.asList(aggregation);
	}
	
//	private Genre findGenreByName(String genreName) {
//		
//		Category category;
//		CategoryTypeObject genre = null;
//		
//		for (CategoryTypeObject categoryTypeObject : mCachedMyPreferences) {
//			if (categoryTypeObject instanceof Category){
//				category = (Category) categoryTypeObject;
//				for (int i = 0; i < category.getChildCount(); i++) {
//					genre = category.getChildAt(i);
//					if (genre.getName().equals(genreName)) {
//						return (Genre) genre;
//					}
//				}
//			}
//		}
//		return null;
//	}
//	
//	private Category findCategoryByName(String categoryName) {
//		for (CategoryTypeObject categoryTypeObject : mCachedMyPreferences) {
//			if (categoryTypeObject instanceof Category && 
//						categoryTypeObject.getName().equals(categoryName)){
//				return (Category) categoryTypeObject;
//			}
//		}
//		return null;
//	}

}
