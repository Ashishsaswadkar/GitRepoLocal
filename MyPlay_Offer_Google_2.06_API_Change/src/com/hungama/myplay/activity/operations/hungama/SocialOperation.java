package com.hungama.myplay.activity.operations.hungama;

import android.text.TextUtils;

import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;

public abstract class SocialOperation extends HungamaOperation {
	
	private static final String UGLY_BROKEN_JSON_PREFIX = "\"response\":{";

	protected String removeUglyResponseWrappingObjectFromResponse(String response) throws InvalidResponseDataException {
		
		if (TextUtils.isEmpty(response))
			throw new InvalidResponseDataException("No response from server");
		
		if (response.contains(UGLY_BROKEN_JSON_PREFIX)) {
			response = response.replace(UGLY_BROKEN_JSON_PREFIX, "");
			response = response.substring(0, response.length() - 1);
		}
		
		return response;
	}

}
