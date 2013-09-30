package com.hungama.myplay.activity.operations.hungama;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.SearchResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class SearchKeyboardOperation extends HungamaOperation {
	
	private static final String TAG = "SearchKeyboardOperation";
	
	public static final String RESPONSE_KEY_SEARCH = "response_key_search";
	public static final String RESPONSE_KEY_QUERY = "response_key_query";
	public static final String RESPONSE_KEY_TYPE = "response_key_type";
	
	public static final String KEYWORD = "keyword";
	public static final String TYPE = "type";
	public static final String STARTINDEX = "startIndex";
	public static final String LENGTH = "length";
	public static final String CATALOG = "catalog";
	
	private final String mServerUrl;
	private final String mKeyword;
	private final String mType;
	private final String mStartIndex;
	private final String mLength;
	private final String mAuthKey;
	private final String mUserId;
	
	public SearchKeyboardOperation(String serverUrl, String keyword, String type, 
								   String startIndex, String length, String authKey, String userId) {
		mServerUrl = serverUrl;
		mKeyword = keyword;
		mType = type;
		mStartIndex = startIndex;
		mLength = length;
		mAuthKey = authKey;
		mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SEARCH;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {	
		
		String encodedQuery = mKeyword;
		
		// Querying like a bous!
		try {
			encodedQuery = URLEncoder.encode(encodedQuery, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return mServerUrl + URL_SEGMENT_SEARCH + 
				KEYWORD + "=" + encodedQuery + "&" + 
				TYPE + "=" + mType + "&" + 
				STARTINDEX + "=" + mStartIndex + "&" + 
				LENGTH + "=" + mLength + "&" + 
				PARAMS_AUTH_KEY + "=" + mAuthKey
				+ "&" + 
				PARAMS_USER_ID + "=" + mUserId;
	}

	@Override
	public String getRequestBody() {		
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
										InvalidRequestParametersException, InvalidRequestTokenException,
										OperationCancelledException {	
		
		if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
		
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
		try {
			
				response = response.replace("{\"catalog\":", "");
				response = response.substring(0, response.length() - 1);
				
				if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
				
				JSONParser jsonParser = new JSONParser();
				Map<String, Object> responseMap = (Map<String, Object>) jsonParser.parse(response);
				
				if (responseMap.containsKey(KEY_CONTENT)) {
					Object contentObj = (Object) responseMap.get(KEY_CONTENT);
					String contentStr = String.valueOf(contentObj);
					if (contentStr.equals("0")) {
						response = response.replace(",\"content\":0", ",\"content\":[]");
					}
					
					SearchResponse searchResponse = (SearchResponse) gson.fromJson(response, SearchResponse.class);
					List<MediaItem> mediaItems = searchResponse.getContent();
					
					for (MediaItem mediaItem : mediaItems) {
						if (mediaItem.getMediaType() == MediaType.VIDEO) {
							mediaItem.setMediaContentType(MediaContentType.VIDEO);
							
						} else if (mediaItem.getMediaType() == MediaType.ARTIST) {
							mediaItem.setMediaContentType(MediaContentType.RADIO);
							
						} else {
							mediaItem.setMediaContentType(MediaContentType.MUSIC);
						}
					}
					
					String query = mKeyword;
					
					// Querying like a bous!
					try {
						query = URLDecoder.decode(query, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					
					resultMap.put(RESPONSE_KEY_SEARCH, searchResponse);
					resultMap.put(RESPONSE_KEY_QUERY, query);
					resultMap.put(RESPONSE_KEY_TYPE, mType);
				}
				if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
				
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