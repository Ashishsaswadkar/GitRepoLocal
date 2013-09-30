/**
 * 
 */
package com.hungama.myplay.activity.operations.hungama;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.social.MyStreamResult;
import com.hungama.myplay.activity.data.dao.hungama.social.StreamItem;
import com.hungama.myplay.activity.data.dao.hungama.social.StreamItemCategory;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * Retrieves Stream Items to be presented in the My Stream section if the application.
 */
public class SocialMyStreamOperation extends SocialOperation {
	
	private static final String TAG = "SocialMyStreamOperation";
	
	public static final String RESULT_KEY_STREAM_ITEMS = "result_key_stream_items";
	public static final String RESULT_KEY_STREAM_ITEMS_CATEGORY = "result_key_stream_items_category";
	
	private static final String PARAMS_FOR = "for";
	
	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final StreamItemCategory mStreamItemCategory;

	public SocialMyStreamOperation(String serviceUrl, String authKey, 
									 String userId, StreamItemCategory streamItemCategory) {
		
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mStreamItemCategory = streamItemCategory;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_MY_STREAM;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_MY_STREAM
							+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
							+ PARAMS_FOR + EQUALS + mStreamItemCategory.name().toLowerCase() + AMPERSAND
							+ PARAMS_AUTH_KEY + EQUALS + mAuthKey;
		
		return serviceUrl;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
								  InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		response = removeUglyResponseWrappingObjectFromResponse(response);
		
		if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
		
		Gson gsonParser = new Gson();
		
		try {
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			MyStreamResult myStreamResult = gsonParser.fromJson(response, MyStreamResult.class);
			
//			if (Utils.isListEmpty(myStreamResult.streamItems)) {
//				throw new InvalidResponseDataException();
//			}
			
			List<StreamItem> streamItems = myStreamResult.streamItems;
			MediaContentType mediaContentType = null;
			List<MediaItem> mediaItems;

			// this is ugly.
			Date time = null;
			SimpleDateFormat gmtTimeFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);//Changes by Hungama
			
			for (StreamItem streamItem : streamItems) {
				
				// populates the media items with the related content type.
				if (!Utils.isListEmpty(streamItem.moreSongsItems)) {
					mediaContentType = getMediaContentTypeForType(streamItem.type);
					mediaItems = streamItem.moreSongsItems;
					for (MediaItem mediaItem : mediaItems) {
						mediaItem.setMediaContentType(mediaContentType);
					}
				}
				
				// populates the stream items with dates instead working with string times.
				try {
					time = gmtTimeFormater.parse(streamItem.time);
					streamItem.setDate(time);
				} catch (java.text.ParseException e) {
					Logger.e(TAG, "Bad time data for Stream item: " + Long.toString(streamItem.conentId) + " skipping it.");
					e.printStackTrace();
					continue;
				}
				
			}
			
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_STREAM_ITEMS, myStreamResult.streamItems);
			resultMap.put(RESULT_KEY_STREAM_ITEMS_CATEGORY, mStreamItemCategory);
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			return resultMap;
			
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}
	
	private MediaContentType getMediaContentTypeForType(String type) {
		if (MediaType.VIDEO.name().equalsIgnoreCase(type)) {
			return MediaContentType.VIDEO;
		} else {
			return MediaContentType.MUSIC;
		}
	}

}
