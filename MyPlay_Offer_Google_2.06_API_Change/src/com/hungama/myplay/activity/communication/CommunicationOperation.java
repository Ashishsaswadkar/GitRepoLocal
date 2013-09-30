package com.hungama.myplay.activity.communication;

import java.util.Map;

import android.content.Context;

import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;

/**
 * Interface definition of operations that can be performed to communicate
 * with the application's servers.
 */
public abstract class CommunicationOperation {
	
	/**
	 * Retrieves unique operation's implementation identifier.</br>
	 * <p>
	 * This method helps the client code to differentiate between 
	 * application's communication operations within the {@link CommunicationOperationListener}.
	 * callback methods.
	 * 
	 * @return index of the operation implementation.
	 */
	public abstract int getOperationId();
	
	/**
	 * Retrieves the operations request method type, defined in {@link RequestMethod}.
	 * @return RequestMethod.
	 */
	public abstract RequestMethod getRequestMethod();
	
	/**
	 * Retrieves the service's full path to communicate with application's server.</br>
	 * @return service's full URL.
	 */
	public abstract String getServiceUrl(final Context context);
	
	/**
	 * Retrieves the request's body to send the server.
	 * @return string containing the request data.
	 */
	public abstract String getRequestBody();
	
	/**
	 * Parses the response from the service call, and retrieves a map of the desired objects.
	 * 
	 * @param response string from the service.
	 * @return map containing parsed objects for use within the application.
	 * 
	 * @throws InvalidResponseDataException when response data from server is invalid.
	 * @throws InvalidRequestParametersException when response data indicates an error has occurred due invalid request parameters.
	 * @throws InvalidRequestTokenException when response data indicates the application session was expired.
	 * @throws OperationCancelledException when the operation has been requested to be cancelled.
	 */
	public abstract Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
														InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException;

}
