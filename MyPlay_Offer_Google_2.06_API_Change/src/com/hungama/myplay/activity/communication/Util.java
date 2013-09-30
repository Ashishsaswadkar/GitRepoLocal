package com.hungama.myplay.activity.communication;

import java.util.Map;

/**
 * Util public helper methods for handling requests and responses. 
 */
public class Util {
	
	/**
	 * Generates request string from the given map 
	 */
	public static String buildParametersString(Map<String, String> parameters) {
		
		StringBuilder requestData = new StringBuilder();
		
		for (Map.Entry<String, String> parameter : parameters.entrySet()) {
			requestData.append(parameter.getKey()).append("=").append(parameter.getValue()).append("&");
		}
		
		String requestString = requestData.toString();
		
		// checks if the last character of the result is ampersand '&', if so remove it.
		if ((requestString.length() > 1) && (requestString.lastIndexOf('&') == requestString.length() - 1)) {
			
			requestData.deleteCharAt(requestString.length() - 1);
			requestString = requestData.toString();
		}
		
		return requestString;
	}

}
