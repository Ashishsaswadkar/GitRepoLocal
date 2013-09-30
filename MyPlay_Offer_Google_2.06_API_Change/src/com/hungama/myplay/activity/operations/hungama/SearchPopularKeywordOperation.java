package com.hungama.myplay.activity.operations.hungama;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class SearchPopularKeywordOperation extends HungamaOperation {
	
	private static final String TAG = "SearchPopularKeywordOperation";
	
	public static final String RESULT_KEY_LIST_KEYWORDS = "result_key_list_keywords";
	public static final String KEYWORD = "keyword";
	public static final String CATALOG = "catalog";
	
	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	
	public SearchPopularKeywordOperation(String serverUrl, String authKey, String userId) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {		
		return mServerUrl + URL_SEGMENT_SEARCH_POPULAR_KEYWORD + PARAMS_AUTH_KEY + "=" + mAuthKey
															+ "&" + PARAMS_USER_ID + "=" + mUserId;
	}

	@Override
	public String getRequestBody() {		
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {
			
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			JSONParser parser = new JSONParser();
			
			Map<String,Object> responseMap = (Map<String, Object>) parser.parse(response);
			List<Map<String, Object>> catalogMap = (List<Map<String, Object>>) responseMap.get(CATALOG);
			
			if (Utils.isListEmpty(catalogMap)) {
				throw new InvalidResponseDataException("list is empty.");
			}
			
			List<String> keywords = new ArrayList<String>(); 
			
			for (Map<String, Object> keywordMap : catalogMap) {
				keywords.add((String) keywordMap.get(KEYWORD));
			}
			
			resultMap.put(RESULT_KEY_LIST_KEYWORDS, keywords);			
			
			return resultMap;
			
		} catch (JsonSyntaxException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (JsonParseException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (ParseException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		}
	}
}