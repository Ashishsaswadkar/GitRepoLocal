package com.hungama.myplay.activity.communication.exceptions;

/**
 * Signals that an error has been reached unexpectedly while performing communication operations,
 * due to invalid request parameters. 
 */
public class InvalidRequestParametersException extends InvalidRequestException {
	
	private int code = 0;
	private String message = "";
	
	public InvalidRequestParametersException(){}
	
	public InvalidRequestParametersException(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}

}
