package com.hungama.myplay.activity.communication;

import java.util.Map;

import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;

/**
 * Interface definition for callbacks to be invoked in the application's 
 * communication operations processes.
 */
public interface CommunicationOperationListener {
	
	/**
	 * Called when the communication process has been started.
	 * 
	 * @param operationId to differentiate between operations.
	 */
	public void onStart(int operationId);
	
	/**
	 * Called when the communication process has finished successfully.
	 * 
	 * @param operationId to differentiate between operations.
	 * @param responseObjects map of parsed objects.
	 */
	public void onSuccess(int operationId, Map<String, Object> responseObjects);
	
	/**
	 * Called when an error occurred during the process.
	 * 
	 * @param operationId to differentiate between operations.
	 * @param errorType for indicating the type of the error.
	 * @param errorMessage string message of the error.
	 */
	public void onFailure(int operationId, ErrorType errorType, String errorMessage);

}
