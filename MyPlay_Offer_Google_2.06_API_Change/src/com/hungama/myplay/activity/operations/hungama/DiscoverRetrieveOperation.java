package com.hungama.myplay.activity.operations.hungama;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.text.TextUtils;

import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.CategoryTypeObject;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.Era;
import com.hungama.myplay.activity.data.dao.hungama.Genre;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.Tempo;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Utils;

/**
 * Retrieve saved Discoveries of the application's user.
 */
public class DiscoverRetrieveOperation extends DiscoverOperation {
	
	private static final String TAG = "DiscoverRetrieveOperation";
	
	public static final String RESULT_KEY_DISCOVERIES = "result_key_discoveries";
	
	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	
	private final List<CategoryTypeObject> mCachedPreferences;
	private final List<Mood> mCachedMoods;
	
	public DiscoverRetrieveOperation(String serverUrl, String authKey, String userId, List<CategoryTypeObject> cachedPreferences, List<Mood> cachedMoods) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mUserId = userId;
		
		mCachedPreferences = cachedPreferences;
		mCachedMoods = cachedMoods;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.DISCOVER_RETRIEVE;
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
		serverURL.append(URL_SEGMENT_DISCOVER_RETREIVE);
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
		
		JSONParser jsonParser = new JSONParser();
		
		try {
			Map<String, Object> responseMap = (Map<String, Object>) jsonParser.parse(response) ;
			
			// checks if the given response is not an error.
			if (responseMap.containsKey(KEY_RESPONSE)) {
				// gets the error message.
				Map<String, Object> errorMap = (Map<String, Object>) responseMap.get(KEY_RESPONSE);
				int code = ((Long) errorMap.get(KEY_CODE)).intValue();
				String message = (String) errorMap.get(KEY_MESSAGE);
				throw new InvalidRequestParametersException(code, message);
			}
			
			responseMap = (Map<String, Object>) responseMap.get(KEY_CATALOG);
			List<Map<String, Object>> contentMap = (List<Map<String, Object>>) responseMap.get(KEY_CONTENT);
			
			List<Discover> discoveries = new ArrayList<Discover>();
			
			int id;
			String name;
			
			int fromEra;
			int toEra;
			Era era;
			
			int moodId;
			String tag;
			Mood mood;
			
			List<String> aggregation;
			
			String temposString;
			List<Tempo> tempos;
			
			String genresString;
			List<Genre> genres;
			
			String categoriesString;
			List<Category> categories;
			
			for (Map<String, Object> discoverMap : contentMap) {
				// General.
				id = ((Long) discoverMap.get(KEY_ID)).intValue();
				name = (String) discoverMap.get(KEY_NAME);
				
				// Era.
				fromEra = ((Long) discoverMap.get(KEY_FROM_ERA)).intValue();
				toEra = ((Long) discoverMap.get(KEY_TO_ERA)).intValue();
				era = new Era(fromEra, toEra);
				
				// mood.
				moodId = ((Long) discoverMap.get(KEY_MOOD)).intValue();
				tag = (String) discoverMap.get(KEY_TAG);
				
				if (moodId > 0) {
					// initialize mood by its id.
					mood = findMoodById(moodId);
					
				} else if (!TextUtils.isEmpty(tag)) {
					// initialized mood by its tag / name;
					mood = findMoodByTag(tag);
					
				} else {
					// no mood was selected for this discover.
					mood = null;
				}
				
				// Tempos.
				temposString = (String) discoverMap.get(KEY_TEMPO);
				tempos = new ArrayList<Tempo>();
				if (!TextUtils.isEmpty(temposString)){
					aggregation = aggregateString(temposString);
					if (!Utils.isListEmpty(aggregation)) {
						for (String tempoName : aggregation) {
							tempos.add(Tempo.valueOf(tempoName.toUpperCase()));
						}
					}
				}
				
				// Genres.
				genresString = (String) discoverMap.get(KEY_GENRE);
				genres = new ArrayList<Genre>();
				if (!TextUtils.isEmpty(genresString)){
					aggregation = aggregateString(genresString);
					if (!Utils.isListEmpty(aggregation)) {
						for (String genreName : aggregation) {
							// gets the Genre from the preferences.
							Genre genre = findGenreByName(genreName);
							// adds it.
							if (genre != null)
								genres.add(genre);
						}
					}
				}
				
				// Categories.
				categoriesString = (String) discoverMap.get(KEY_CATEGORY);
				categories = new ArrayList<Category>();
				if (!TextUtils.isEmpty(categoriesString)){
					aggregation = aggregateString(categoriesString);
					if (!Utils.isListEmpty(aggregation)) {
						for (String categoryName : aggregation) {
							Category category = findCategoryByName(categoryName);
							if (category != null)
								categories.add(category);
						}
					}
				}
				
				discoveries.add(new Discover(id, name, mood, genres, categories, tempos, era));
			}
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_DISCOVERIES, discoveries);
			
			return resultMap;
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}
	
	private Mood findMoodById(int id) {
		for (Mood mood : mCachedMoods) {
			if (mood.getId() == id)
				return mood;
		}
		return null;
	}
	
	private Mood findMoodByTag(String tag) {
		for (Mood mood : mCachedMoods) {
			if (mood.getName().equals(tag))
				return mood;
		}
		return null;
	}
	
	private List<String> aggregateString(String stringToAggregate) {
		String [] aggregation = stringToAggregate.split(",");
		return Arrays.asList(aggregation);
	}
	
	private Genre findGenreByName(String genreName) {
		
		Category category;
		CategoryTypeObject genre = null;
		
		for (CategoryTypeObject categoryTypeObject : mCachedPreferences) {
			if (categoryTypeObject instanceof Category){
				category = (Category) categoryTypeObject;
				for (int i = 0; i < category.getChildCount(); i++) {
					genre = category.getChildAt(i);
					if (genre.getName().equals(genreName)) {
						return (Genre) genre;
					}
				}
			}
		}
		return null;
	}
	
	private Category findCategoryByName(String categoryName) {
		for (CategoryTypeObject categoryTypeObject : mCachedPreferences) {
			if (categoryTypeObject instanceof Category && 
						categoryTypeObject.getName().equals(categoryName)){
				return (Category) categoryTypeObject;
			}
		}
		return null;
	}

}
