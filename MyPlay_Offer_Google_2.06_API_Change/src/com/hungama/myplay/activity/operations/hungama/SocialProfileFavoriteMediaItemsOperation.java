/**
 * 
 */
package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.List;
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
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileFavoriteMediaItems;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class SocialProfileFavoriteMediaItemsOperation extends SocialOperation {

	public static final String RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS = "result_key_profile_favorite_media_items";
	public static final String RESULT_KEY_MEDIA_TYPE = "result_key_profile_leaderboard";
	
	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final MediaType mMediaType;
	
	public SocialProfileFavoriteMediaItemsOperation(String serviceUrl, String authKey, String userId, MediaType mediaType) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mMediaType = mediaType;
	}
	
	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		
		String serviceName = null;
		
		if (mMediaType == MediaType.ALBUM) {
			serviceName = URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_ALBUMES;
		} else if (mMediaType == MediaType.TRACK) {
			serviceName = URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_SONGS;
		} else if (mMediaType == MediaType.PLAYLIST) {
			serviceName = URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_PLAYLISTS;
		} else {
			serviceName = URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_VIDEOS;
		}
		
		String serviceUrl = mServiceUrl + serviceName
				+ PARAMS_LENGTH + EQUALS + "1000" + AMPERSAND
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_AUTH_KEY + EQUALS + mAuthKey;
		
		return serviceUrl;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException, InvalidRequestParametersException, 
																	 InvalidRequestTokenException, OperationCancelledException {
		
		response = removeUglyResponseWrappingObjectFromResponse(response);
		
		if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
		
		Gson gsonParser = new Gson();
		
		try {
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
		
			ProfileFavoriteMediaItems profileFavoriteMediaItems = gsonParser.fromJson(response, ProfileFavoriteMediaItems.class);
		
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			// fixes the mediaItems
			List<MediaItem> mediaItems = profileFavoriteMediaItems.mediaItems;
			
			for (MediaItem mediaItem : mediaItems) {
				if (mMediaType == MediaType.ALBUM || mMediaType == MediaType.PLAYLIST || mMediaType == MediaType.TRACK) {
					mediaItem.setMediaContentType(MediaContentType.MUSIC);
				} else if (mMediaType == MediaType.VIDEO) {
					mediaItem.setMediaContentType(MediaContentType.VIDEO);
				} else {
					continue;
				}
				
				mediaItem.setMediaType(mMediaType);
			}
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS, profileFavoriteMediaItems);
			resultMap.put(RESULT_KEY_MEDIA_TYPE, mMediaType);
			
			if (Thread.currentThread().isInterrupted()) { throw new OperationCancelledException(); }
			
			return resultMap;
		
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

}
