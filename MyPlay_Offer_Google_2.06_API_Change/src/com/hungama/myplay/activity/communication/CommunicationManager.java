package com.hungama.myplay.activity.communication;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

//import com.hungama.myplay.activity.R;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.util.Logger;

/**
 * Manages Communication operations with the applications services. Currently
 * now, it can performs only single task per manager's instance.
 */
public class CommunicationManager {

	private static final String TAG = "CommunicationManager";

	/**
	 * Enumeration definitions for errors that may occurred during the process
	 * of the operations.
	 */
	public enum ErrorType implements Serializable {

		/**
		 * Error which indicates that there is no connection to Internet or has
		 * been timeout.
		 */
		NO_CONNECTIVITY,

		/**
		 * Error which indicates that the given parameters to the request's
		 * service are invalid.
		 */
		INVALID_REQUEST_PARAMETERS,

		/**
		 * Error which indicates that the given token given to request is
		 * invalid.
		 */
		EXPIRED_REQUEST_TOKEN,

		/**
		 * Error which indicates that the retrieved server response is invalid
		 * OR the communication protocols among the client / server was broken.
		 */
		INTERNAL_SERVER_APPLICATION_ERROR,

		/**
		 * Error which indicates that the whole operation was cancelled.
		 */
		OPERATION_CANCELLED;

	}

	public static final String ENCODEING_FORMAT_UTF_8 = "UTF-8";

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private static final String PROTOCOL_HTTP = "HTTP";
	private static final String PROTOCOL_HTTPS = "HTTPS";

	private static final String REQUEST_PROPERTY_CONTENT_TYPE_KEY = "Content-Type";
	private static final String REQUEST_PROPERTY_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded;charset=UTF-8";

	private static final int CONNECTION_TIMEOUT_INTERVAL_MILLISECONDS = 45000;

	// private static final String CONNECTION_PROPERTY = "connection";
	// private static final String CONNECTIOaN_VALUE = "close";

	private volatile boolean mIsRunning = false;
	private Thread mOperationWorker = null;

	public CommunicationManager() {
	}

	// ======================================================
	// PUBLIC.
	// ======================================================

	/**
	 * Determines whatever any operation is running.
	 */
	public boolean isRunning() {
		return mIsRunning;
	}

	/**
	 * Cancel any running operation's progress.</br> In bottom line, interrupts
	 * any running worker that performs operations.
	 */
	public void cancelAnyRunningOperation() {
		if (mOperationWorker != null && mOperationWorker.isAlive()) {
			mOperationWorker.interrupt();
		}
	}

	/**
	 * Performs a communication operation asynchronously.
	 * 
	 * @param operation
	 *            to perform.
	 * @param listener
	 *            for retrieving process events.
	 */
	public void performOperationAsync(CommunicationOperation operation,
			CommunicationOperationListener listener, final Context context) {

		mOperationWorker = new Thread(new OperationTask(operation,
				new OperationHandler(listener), context));
		mOperationWorker.start();
	}

	public Map<String, Object> performOperation(CommunicationOperation operation, final Context context)
			throws InvalidRequestException, InvalidResponseDataException,
			OperationCancelledException, NoConnectivityException {

		String url = operation.getServiceUrl(context);
		RequestMethod requestMethod = operation.getRequestMethod();
		String requestBody = operation.getRequestBody();

		String response = null;

		try {

			try {
				response = performRequest(url, requestMethod, requestBody);
			} catch (SocketException exception1) {
				Logger.e(TAG,
						"The connection was reseted... try for the second time.");
				try {
					response = performRequest(url, requestMethod, requestBody);
				} catch (SocketException exception2) {
					Logger.e(TAG,
							"The connection was reseted... try fo the third time.");
					response = performRequest(url, requestMethod, requestBody);
				}
			}

		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			throw new NoConnectivityException();

		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new InvalidRequestException();

		} catch (ProtocolException e) {
			e.printStackTrace();
			throw new InvalidRequestException();

		} catch (IOException e) {
			e.printStackTrace();
			throw new NoConnectivityException();

		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new OperationCancelledException();
		}

		String temp[] = url.split("/");
		Logger.i(TAG , temp[temp.length-1] + " response " + response);

		Map<String, Object> responseData = operation.parseResponse(response);

		return responseData;
	}

	// ======================================================
	// BACKROUND PROCESSING MEMBERS.
	// ======================================================

	private static final int MESSAGE_OPERATION_START = 1;
	private static final int MESSAGE_OPERATION_SECCESS = 2;
	private static final int MESSAGE_OPERATION_FAIL = 3;

	private static final String MESSAGE_DATA_KEY_RESPONSE = "message_data_key_response";
	private static final String MESSAGE_DATA_KEY_ERROR_DESCRIPTION = "message_data_key_error_description";
	private static final String MESSAGE_DATA_KEY_ERROR_TYPE = "message_data_key_error_type";

	/**
	 * Handler receiving messages of the execution process.
	 */
	private class OperationHandler extends Handler {

		private CommunicationOperationListener mOnCommunicationOperationListener;

		public OperationHandler(
				CommunicationOperationListener onCommunicationOperationListener) {

			mOnCommunicationOperationListener = onCommunicationOperationListener;
		}

		@Override
		public void handleMessage(Message message) {

			switch (message.what) {

			case MESSAGE_OPERATION_START:

				mIsRunning = true;

				if (mOnCommunicationOperationListener != null) {
					mOnCommunicationOperationListener.onStart(message.arg1);
				}

				break;

			case MESSAGE_OPERATION_SECCESS:

				Bundle seccussData = message.getData();
				HashMap<String, Object> responseData = (HashMap<String, Object>) seccussData
						.getSerializable(MESSAGE_DATA_KEY_RESPONSE);

				if (mOnCommunicationOperationListener != null) {

					if (responseData == null) {
						responseData = new HashMap<String, Object>();
					}
					mOnCommunicationOperationListener.onSuccess(message.arg1,
							responseData);
				}

				mIsRunning = false;

				break;

			case MESSAGE_OPERATION_FAIL:

				Bundle failData = message.getData();

				ErrorType errorType = (ErrorType) failData
						.getSerializable(MESSAGE_DATA_KEY_ERROR_TYPE);
				String errorMessage = failData
						.getString(MESSAGE_DATA_KEY_ERROR_DESCRIPTION);

				if (mOnCommunicationOperationListener != null) {
					mOnCommunicationOperationListener.onFailure(message.arg1,
							errorType, errorMessage);
				}

				mIsRunning = false;

				break;

			}

		}

	}

	/**
	 * Background task for performing communication operations asynchronously.
	 */
	private class OperationTask implements Runnable {

		private CommunicationOperation mCommunicationOperation;
		private OperationHandler mOperationHandler;
		private final Context context;
		public OperationTask(CommunicationOperation communicationOperation,
				OperationHandler operationHandler, final Context context) {
			mCommunicationOperation = communicationOperation;
			mOperationHandler = operationHandler;
			this.context = context;
		}

		@Override
		public void run() {

			try {

				// notifying for starting the operation.
				Message startMessage = Message.obtain();
				startMessage.arg1 = mCommunicationOperation.getOperationId();
				startMessage.what = MESSAGE_OPERATION_START;
				mOperationHandler.sendMessage(startMessage);

				String url = mCommunicationOperation.getServiceUrl(context);
				RequestMethod requestMethod = mCommunicationOperation
						.getRequestMethod();
				String requestBody = mCommunicationOperation.getRequestBody();

				String response = null;

				try {
					response = performRequest(url, requestMethod, requestBody);
				} catch (SocketException exception1) {
					Logger.e(TAG,
							"The connection was reseted... try for the second time.");
					try {
						response = performRequest(url, requestMethod,
								requestBody);
					} catch (SocketException exception2) {
						Logger.e(TAG,
								"The connection was reseted... try fo the third time.");
						response = performRequest(url, requestMethod,
								requestBody);
					}
				}

				String temp[] = url.split("/");
				Logger.i(TAG ,temp[temp.length-1] +" response "+  response);
				
				Map<String, Object> responseData = mCommunicationOperation
						.parseResponse(response);

				// notifying for finishing the operation.
				Message sucessMessage = Message.obtain();
				sucessMessage.arg1 = mCommunicationOperation.getOperationId();
				sucessMessage.what = MESSAGE_OPERATION_SECCESS;
				Bundle data = new Bundle();
				data.putSerializable(MESSAGE_DATA_KEY_RESPONSE,
						(Serializable) responseData);
				sucessMessage.setData(data);
				mOperationHandler.sendMessage(sucessMessage);

			} catch (OperationCancelledException exception) {
				// operation has been cancelled.
				exception.printStackTrace();
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.OPERATION_CANCELLED,
						"Operation has been cancelled.");

			} catch (InterruptedException exception) {
				// operation has been cancelled.
				exception.printStackTrace();
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.OPERATION_CANCELLED,
						"Operation has been cancelled.");

			} catch (MalformedURLException exception) {
				// Bad Url.
				exception.printStackTrace();
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.INTERNAL_SERVER_APPLICATION_ERROR,
						"Invalid service URL.");

			} catch (SocketTimeoutException exception) {
				// timeout.
				exception.printStackTrace();
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.NO_CONNECTIVITY, "Timeout.");

			} catch (IOException exception) {
				// File cannot be accessed or is corrupted..
				exception.printStackTrace();
				Logger.e(TAG, "No Internet Connection");
//				sendErrorMessageToHanlder(
//						mCommunicationOperation.getOperationId(),
//						ErrorType.NO_CONNECTIVITY, "No Internet Connection.");

				// Logger.e(TAG, "File cannot be accessed or is corrupted.");
				// sendErrorMessageToHanlder(mCommunicationOperation.getOperationId(),
				// ErrorType.NO_CONNECTIVITY,
				// " File cannot be accessed or is corrupted.");

			} catch (InvalidResponseDataException exception) {
				// Bad data from servers.
				Logger.e(TAG, "Bad response data from servers.");
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.INTERNAL_SERVER_APPLICATION_ERROR,
						exception.getMessage());

			} catch (InvalidRequestParametersException exception) {
				// Bad request parameters.
				Logger.e(
						TAG,
						"Bad request parameters. "
								+ Integer.toString(exception.getCode()) + " "
								+ exception.getMessage());
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.INVALID_REQUEST_PARAMETERS,
						exception.getMessage());

			} catch (InvalidRequestTokenException exception) {
				// Invalid request token.
				Logger.e(TAG, "Invalid request token.");
				sendErrorMessageToHanlder(
						mCommunicationOperation.getOperationId(),
						ErrorType.EXPIRED_REQUEST_TOKEN,
						"Invalid request token.");
			}
		}

		/**
		 * Notifies the operation's handler that an error has occurred.
		 */
		private void sendErrorMessageToHanlder(int operationId,
				ErrorType errorType, String errorMessage) {
			Message message = Message.obtain();
			message.what = MESSAGE_OPERATION_FAIL;
			message.arg1 = operationId;

			Bundle data = new Bundle();
			data.putSerializable(MESSAGE_DATA_KEY_ERROR_TYPE, errorType);
			data.putString(MESSAGE_DATA_KEY_ERROR_DESCRIPTION, errorMessage);

			message.setData(data);
			mOperationHandler.sendMessage(message);
		}

	}

	// ======================================================
	// SERVERS OPERATIONS.
	// ======================================================

	/**
	 * Executes webservice calls and retrieves responses.
	 * 
	 * @param stringUrl
	 *            to the webservice.
	 * @param parameters
	 *            for HTTP POST request.
	 * @return response as string, formatted in "UTF-8".
	 * 
	 * @throws MalformedURLException
	 *             for invalid given URL.
	 * @throws ProtocolException
	 *             for problems with parameters when performing HTTP POST calls.
	 * @throws IOException
	 *             for connectivity and general I/O problems.
	 * @throws OperationCancelledException
	 */
	private String performRequest(String stringUrl,
			RequestMethod requestMethod, String requestBody)
			throws MalformedURLException, ProtocolException, IOException,
			SocketTimeoutException, InterruptedException,
			OperationCancelledException {

		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}

		// System.setProperty("http.keepAlive", "false");

		HttpURLConnection connection = null;
		OutputStreamWriter outputStream = null;
		BufferedReader inputBufferedReader = null;
		StringBuilder responseBuilder = new StringBuilder();
		String response = "";

		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}

		try {
			// builds the request.
			URL url = new URL(stringUrl);
			connection = createConnectionForURL(url);

			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}

			connection.setRequestMethod(requestMethod.toString());
			connection
					.setConnectTimeout(CONNECTION_TIMEOUT_INTERVAL_MILLISECONDS);
			connection.setReadTimeout(CONNECTION_TIMEOUT_INTERVAL_MILLISECONDS);
			// connection.setRequestProperty(CONNECTION_PROPERTY,
			// CONNECTION_VALUE);
			// connection.setRequestProperty("User-Agent","Hungama 3.0 (iPhone; iPhone OS 6.1.2; en_US)");

			// requests the connection to be closed after and not header clear.
			connection.setDefaultUseCaches(false);
			connection.setUseCaches(false);

			// adds additional properties to the request if the method is POST.
			if (requestMethod == RequestMethod.POST) {
				connection.setRequestProperty(
						REQUEST_PROPERTY_CONTENT_TYPE_KEY,
						REQUEST_PROPERTY_CONTENT_TYPE_VALUE);
				connection.setDoOutput(true);
				connection.setFixedLengthStreamingMode(requestBody.length());
			}

			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}

			// posts the request and gets the response.
			if (requestMethod == RequestMethod.POST) {

				String temp[] = stringUrl.split("/");

//				Logger.i(TAG , temp[temp.length - 1] + " request "+ requestBody + " to URL: "
//						+ stringUrl);
				
				Log.i(TAG , temp[temp.length - 1] + " request "+ requestBody + " to URL: "
						+ stringUrl);

				BufferedOutputStream bufferedOutputStream = null;
				/*
				 * By default pre gingerbread devices, not explicitly define a
				 * safe to use buffer size. seems like a bug.
				 */
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
					bufferedOutputStream = new BufferedOutputStream(
							connection.getOutputStream(), DEFAULT_BUFFER_SIZE);
				} else {
					bufferedOutputStream = new BufferedOutputStream(
							connection.getOutputStream());
				}

				outputStream = new OutputStreamWriter(bufferedOutputStream);
				outputStream.write(requestBody);
				outputStream.flush();

			} else {
				Logger.i(TAG, "Getting from URL: " + stringUrl);
			}

			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}

			InputStreamReader inputStreamReader = new InputStreamReader(
					connection.getInputStream());

			/*
			 * By default pre gingerbread devices, not explicitly define a safe
			 * to use buffer size. seems like a bug.
			 */
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
				inputBufferedReader = new BufferedReader(inputStreamReader,
						DEFAULT_BUFFER_SIZE);
			} else {
				inputBufferedReader = new BufferedReader(inputStreamReader);
			}

			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}

			for (String line = null; (line = inputBufferedReader.readLine()) != null
					&& !Thread.currentThread().isInterrupted();) {
				responseBuilder.append(line);
			}

			response = responseBuilder.toString();

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

		} finally {

			if (inputBufferedReader != null) {
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
					inputBufferedReader.close();
				}
				inputBufferedReader = null;
			}

			try {
				if (outputStream != null) {
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
						outputStream.close();
					}
					outputStream = null;
				}
			} catch (SocketException exception) { /* does nothing. */
			}

			if (connection != null) {
				Logger.v(TAG, "releasing connection.");
				connection.disconnect();
				connection = null;
			}
		}

		return response;
	}

	private static HttpURLConnection createConnectionForURL(URL url)
			throws IOException {
		// instantiating the URL Connection based on the protocol.
		HttpURLConnection connection = null;
		if (url.getProtocol().equalsIgnoreCase(PROTOCOL_HTTP)) {
			connection = (HttpURLConnection) url.openConnection();

		} else if (url.getProtocol().equalsIgnoreCase(PROTOCOL_HTTPS)) {
			// sets host verifier.
			HttpsURLConnection
					.setDefaultHostnameVerifier(new AllowAllHostnameVerifier());
			connection = (HttpsURLConnection) url.openConnection();
		} else {
			throw new ProtocolException(
					"Only http and https protocols are supported.");
		}

		return connection;
	}

}
