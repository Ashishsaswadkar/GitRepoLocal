package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;

import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.TrackLyrics;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class TrackLyricsOperation extends HungamaOperation {
	
	private static final String TAG = "TrackLyricsOperation";
	
	public static final String RESPONSE_KEY_TRACK_LYRICS = "response_key_track_lyrics";
	
	private final String mServerUrl;
	private final String mAuthKey;
	private final Track mTrack;
	
	public TrackLyricsOperation (String serverUrl, String authKey, Track track) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mTrack = track;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.TRACK_LYRICS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serviceUrl = mServerUrl + URL_SEGMENT_CONTENT + 
						URL_SEGMENT_MUSIC + URL_SEGMENT_LYRICS +
						Long.toString(mTrack.getId()) + "?" +
						PARAMS_AUTH_KEY + EQUALS + mAuthKey;
		
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

		JSONParser parser = new JSONParser();
		
		try {
			Map<String, Object> responseMap = (Map<String, Object>) parser.parse(response);
			
			// checks if the given response is not an error.
			if (responseMap.containsKey(KEY_RESPONSE)) {
				// gets the error message.
				Map<String, Object> errorMap = (Map<String, Object>) responseMap.get(KEY_RESPONSE);
				int code = ((Long) errorMap.get(KEY_CODE)).intValue();
				String message = (String) errorMap.get(KEY_MESSAGE);
				throw new InvalidRequestParametersException(code, message);
			}
			
			// parse parse revolution !
			Map<String, Object> contentMap = (Map<String, Object>) responseMap.get(KEY_CONTENT);
			long id = (Long) contentMap.get(TrackLyrics.KEY_CONTENT_ID);
			String title = (String) contentMap.get(TrackLyrics.KEY_TITLE);
			String lyrics = (String) contentMap.get(TrackLyrics.KEY_LYRICS);
			
			TrackLyrics trackLyrics = new TrackLyrics(id, title, lyrics);
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESPONSE_KEY_TRACK_LYRICS, trackLyrics);
			
			return resultMap;
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
		
	}

}
