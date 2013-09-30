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
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.CategoryTypeObject;
import com.hungama.myplay.activity.data.dao.hungama.Genre;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Utils;

/**
 * Retrieves Categories for filtering Media Items.
 */
public class MediaCategoriesOperation extends HungamaOperation {
	
	private static final String TAG = "MediaCategoriesOperation";
	
	public static final String RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE = "result_key_object_media_content_type";
	public static final String RESULT_KEY_OBJECT_CATEGORIES = "result_key_object_categories";
	
	private final String mServerUrl;
	private final String mAuthKey;
	private final MediaContentType mMediaContentType;
	
	public MediaCategoriesOperation(String serverUrl, String authKey, MediaContentType mediaContentType) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mMediaContentType = mediaContentType;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.MEDIA_CATEGORIES;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return mServerUrl + URL_SEGMENT_CONTENT + mMediaContentType.toString().toLowerCase()
				+ "/" + URL_SEGMENT_CATEGORIES + "?" + PARAMS_AUTH_KEY + "=" + mAuthKey;
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
		
		List<Category> resultCategories;
		
		try {
			Map<String, Object> categoriesMap = (Map<String, Object>) jsonParser.parse(response);
			List<Map<String, Object>> categoriesMapList = (List<Map<String, Object>>)
						((Map<String, Object>) categoriesMap.get("categories")).get("category");
			
			resultCategories = new ArrayList<Category>();
			Map<String, Object> attributeMap;
			long id;
			String name;
			List<CategoryTypeObject> genres;
			List<CategoryTypeObject> subCategories;
			
			for (Map<String, Object> categoryMap : categoriesMapList) {
				attributeMap = (Map<String, Object>) categoryMap.get(KEY_ATTRIBUTES);
				id = (Long) attributeMap.get(KEY_ID);
				name = (String) attributeMap.get(KEY_NAME);
				genres = parseGenres(categoryMap);
				subCategories = parseSubCategories(categoryMap);
				
				if (!Utils.isListEmpty(subCategories)) {
					// sets a reference to the parent for each child.
					Category parentCategory = new Category(id, name, subCategories);
					
					for (CategoryTypeObject categoryTypeObject : subCategories) {
						Category childCategory = (Category) categoryTypeObject;
						childCategory.setParentCategory(parentCategory);
					}
					
					// adds the parent category to the root list.
					resultCategories.add(parentCategory);
					
				} else if (!Utils.isListEmpty(genres)) {
					// sets a reference to the parent for each child.
					Category parentCategory = new Category(id, name, genres);
					
					for (CategoryTypeObject categoryTypeObject : genres) {
						Genre childGenre = (Genre) categoryTypeObject;
						childGenre.setParentCategory(parentCategory);
					}
					
					// adds the parent category to the root list.
					resultCategories.add(parentCategory);
					
				} else {
					resultCategories.add(new Category(id, name, null));
				}
			}
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_OBJECT_CATEGORIES, resultCategories);
			resultMap.put(RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE, mMediaContentType);
			
			return resultMap;
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("Parsing error.");
		}
	}
	
	private List<CategoryTypeObject> parseGenres(Map<String, Object> map){
		if (map.containsKey("genre")) {
			
			Map<String, Object> attributeMap;
			long id;
			String name;
			List<CategoryTypeObject> genres = new ArrayList<CategoryTypeObject>();
			
			List<Map<String, Object>> genreMaps = (List<Map<String, Object>>) map.get("genre");
			for (Map<String, Object> genreMap : genreMaps) {
				attributeMap = (Map<String, Object>) genreMap.get(KEY_ATTRIBUTES);
				id = (Long) attributeMap.get(KEY_ID);
				name = (String) attributeMap.get(KEY_NAME);
				
				genres.add(new Genre(id, name));
			}
			
			return genres;
		}
		
		return null;
	}
	
	private List<CategoryTypeObject> parseSubCategories(Map<String, Object> map){
		if (map.containsKey("subcategory")) {
			
			Map<String, Object> attributeMap;
			long id;
			String name;
			List<CategoryTypeObject> subcategories = new ArrayList<CategoryTypeObject>();
			
			List<Map<String, Object>> categoriesMaps = (List<Map<String, Object>>) map.get("subcategory");
			for (Map<String, Object> categoryMap : categoriesMaps) {
				attributeMap = (Map<String, Object>) categoryMap.get(KEY_ATTRIBUTES);
				id = (Long) attributeMap.get(KEY_ID);
				name = (String) attributeMap.get(KEY_NAME);
				
				subcategories.add(new Category(id, name, null));
			}
			
			return subcategories;
		}
		
		return null;
	}

}
