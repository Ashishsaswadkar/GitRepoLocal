package com.hungama.myplay.activity.operations.hungama;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Utils;

public class MediaContentOperation extends HungamaOperation {
	
	private static final String TAG = "MediaContentOperation";
	
	public static final String RESULT_KEY_OBJECT_MEDIA_ITEMS = "result_key_object_media_items";
	public static final String RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE = "result_key_object_media_content_type";
	public static final String RESULT_KEY_OBJECT_MEDIA_CATEGORY_TYPE = "result_key_object_media_category_type";
	public static final String RESULT_KEY_OBJECT_MEDIA_CATEGORY = "result_key_object_media_category";
	
	private final Context mContext;
	
	private final String mServerUrl;
	private final String mAuthKey;
	private final MediaContentType mContentType;
	private final MediaCategoryType mItemCategoryType;
	private final Category mCategory;
	private final String mUserId;
	
	public MediaContentOperation(Context context, String serverUrl, String authKey, MediaContentType contentType, 
									MediaCategoryType itemCategoryType, Category category, String userId) {
		
		mContext = context;
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mContentType = contentType;
		mItemCategoryType = itemCategoryType;
		mCategory = category;
		mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return DataManager.getOperationIdForMediaCategoryType(mItemCategoryType);
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		String stringContentType = mContentType.toString().toLowerCase();
		String stringItemCategoryType = mItemCategoryType.toString().toLowerCase();
		
		String serviceUrl = mServerUrl + URL_SEGMENT_CONTENT + stringContentType + "/" + stringItemCategoryType + 
				"?" + PARAMS_AUTH_KEY + "=" + mAuthKey + "&" + PARAMS_USER_ID + "=" + mUserId;
		
		if (mCategory != null && !TextUtils.isEmpty(mCategory.getName())) {
			serviceUrl = serviceUrl + "&category=" + mCategory.getName();
		}
		
		return serviceUrl;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
									InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		// removes the: {"catalog":{"content": string and the last }} .
		String itemsString = "";
		if (response.length() > 22 ) {
			itemsString = response.substring(22, response.length() - 2);
		}
		
		Type listType = new TypeToken<ArrayList<MediaItem>>() {}.getType();
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		List<MediaItem> items = null;
		
		try {
			if (!itemsString.equalsIgnoreCase("")) {
				items = gson.fromJson(itemsString, listType);
			}
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();
			
		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		}
		
		if (items == null) {
			items = new ArrayList<MediaItem>();
		}
		
		// TODO: temporally solving the differentiating issue between Music and Videos, solve this when inserting also campaigns.
		for (MediaItem mediaItem : items) {
			if (mediaItem != null) {
				mediaItem.setMediaContentType(mContentType);
			}
		}
		
		// stores it in the cache manager.
		if (!Utils.isListEmpty(items)) {
			DataManager dataManager = DataManager.getInstance(mContext);
			dataManager.storeMediaItems(mContentType, mItemCategoryType, items);
		}
		
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		
		resultMap.put(RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE, mContentType);
		resultMap.put(RESULT_KEY_OBJECT_MEDIA_CATEGORY_TYPE, mItemCategoryType);
		resultMap.put(RESULT_KEY_OBJECT_MEDIA_ITEMS, items);
		
		return resultMap;
	}

}