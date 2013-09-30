package com.hungama.myplay.activity.communication.exceptions;

/**
 * Signals that an error has been reached unexpectedly while parsing response data. 
 */
public class InvalidResponseDataException extends Exception {
	
	private String message = "";
	
	public InvalidResponseDataException() {}
	
	public InvalidResponseDataException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}

}
