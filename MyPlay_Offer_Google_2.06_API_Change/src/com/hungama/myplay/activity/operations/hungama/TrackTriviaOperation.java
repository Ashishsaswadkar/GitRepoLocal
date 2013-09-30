package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
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
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.TrackTrivia;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class TrackTriviaOperation extends HungamaOperation {
	
	private static final String TAG = "TrackTriviaOperation";
	
	public static final String RESULT_KEY_OBJECT_TRACK_TRIVIA = "result_key_object_track_trivia";
	
	private final String mServerUrl;
	private final String mAuthkey;
	private final Track mTrack;
	

	public TrackTriviaOperation(String serverUrl, String authkey, Track track) {
		this.mServerUrl = serverUrl;
		this.mAuthkey = authkey;
		this.mTrack = track;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.TRACK_TRIVIA;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serviceUrl = mServerUrl + URL_SEGMENT_CONTENT + URL_SEGMENT_MUSIC + URL_SEGMENT_TRIVIA + 
				Long.toString(mTrack.getId()) + "?" + 
				PARAMS_AUTH_KEY + EQUALS + mAuthkey;
		
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
		
		// removes the: {"content": string and the last } .
		String reponseString = response.substring(11, response.length() - 1);
		
		Gson gson = new GsonBuilder().create();
		
		try {
			TrackTrivia trackTrivia = gson.fromJson(reponseString, TrackTrivia.class);
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_OBJECT_TRACK_TRIVIA, trackTrivia);
			
			return resultMap;
			
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();
			
		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		}
		
	}

}
