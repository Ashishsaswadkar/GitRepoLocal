package com.hungama.myplay.activity.communication.exceptions;

/**
 * Signals that an error has been reached unexpectedly while performing communication operations,
 * due to invalid request. 
 */
public class InvalidRequestException extends Exception {

	private String message = "";
	
	public InvalidRequestException() {}
	
	public InvalidRequestException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
