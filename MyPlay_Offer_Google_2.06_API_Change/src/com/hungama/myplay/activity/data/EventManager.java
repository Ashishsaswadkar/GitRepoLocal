package com.hungama.myplay.activity.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.events.Event;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.operations.catchmedia.EventCreateOperation;
import com.hungama.myplay.activity.util.Logger;

/**
 * Manages posting user events to CM servers.
 */
public class EventManager {
	
	private static final String TAG = "EventManager";
	
	public static final String PLAY = "play";
	public static final String VIEW = "view";
	public static final String SCROLL = "scroll";
	public static final String CLICK = "click";
	
	private final Context mContext;
	private final ConnectivityManager mConnectivityManager;
	
	private final String mServerUrl;
	
	private final List<Event> mEventsQueue;
	private final ExecutorService mEventsPosterExecutor;
	private DataManager mDataManager;
	
	
	// ======================================================
	// Public.
	// ======================================================
	
	public EventManager(Context applicationContezt, String serverUrl, List<Event> eventsQueue) {
		
		mContext = applicationContezt;
		
		mDataManager = DataManager.getInstance(mContext);
		
		mServerUrl = serverUrl;
		mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		// synchronizing the list itself to make it been changed from different threads.
		if (eventsQueue == null) {
			eventsQueue = new ArrayList<Event>();
		}
		mEventsQueue = Collections.synchronizedList(eventsQueue);
		mEventsPosterExecutor = Executors.newSingleThreadExecutor();
	}
	
	/**
	 * Adds an Event for posting to the servers.
	 * @param event
	 */
	public void addEvent(Event event) {
		if (event == null) {
			Logger.e(TAG, "Event is null, skipping this one.");
			return;
		}
		
		if (TextUtils.isEmpty(event.getRegularTimestamp())) {
			Logger.e(TAG, "Event's timestamp is empty, skipping this one.");
			return;
		}
		
		if (isDeviceOnLine()) {
			if (!(mEventsPosterExecutor.isShutdown() || mEventsPosterExecutor.isTerminated())) {
				//Logger.v(TAG, "Posting event to server: " + event.getMediaId());
				mEventsQueue.add(event);
				postEvent(event);
			} else {
				Logger.v(TAG, "Adding event to queue: " + event.getMediaId());
				mEventsQueue.add(event);
			}
		} else {
			Logger.v(TAG, "Adding event to queue: " + event.getMediaId());
			mEventsQueue.add(event);
		}
	}
	
	/**
	 * Stops any panding and running tasks which posts events.
	 */
	public void stopPostingEvents() {
		//mEventsPosterExecutor.shutdownNow();
		mEventsPosterExecutor.shutdown();
	}
	
	public List<Event> getEvents() {
		return mEventsQueue;
	}
	
	public void clearQueue() {
		mEventsQueue.clear();
	}
	
	public void flushEvents() {
		for (Event event : mEventsQueue) {
			Logger.v(TAG, "Flushing Event in Queue " + event.getMediaId());
			postEvent(event);
			//mEventsPosterExecutor.execute(new EventPoster(event));
		}
	}
	
	
	// ======================================================
	// Private.
	// ======================================================
	
	private void postEvent(Event event) {
		mEventsPosterExecutor.execute(new EventPoster(event));
	}
	
	private boolean isDeviceOnLine() {
 		NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			return true;
 		}
 		return false; 
 	}
	
	/**
	 * Task posting an event to CM servers.
	 */
	private class EventPoster implements Runnable {
		
		private final Event event;
		
		public EventPoster(Event event) {
			this.event = event;
		}
		
		@Override
		public void run() {
			// post the first one requested first.
			Logger.v(TAG, "Try Post Event: " + event.getMediaId() + " " +event.getTimestamp(mContext));
			boolean success = postEvent(event);
			if (success) {
				Logger.v(TAG, "Success posting event: " + event.getMediaId() + " " +  event.getTimestamp(mContext));
				mEventsQueue.remove(event);
				mDataManager.storeEvents(mEventsQueue);
			} else {
				Logger.v(TAG, "Failed posting event: " + event.getMediaId() + " " + event.getTimestamp(mContext));
				//mEventsQueue.add(event);
			}
		}
		
		private boolean postEvent(Event event) {
			CommunicationManager communicationManager = new CommunicationManager();
			Map<String, Object> result = null;
			try {
				SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ENGLISH);//Changes by Hungama
			    String mEventStartTimestamp = null;
			    sSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("utc"));
				mEventStartTimestamp = sSimpleDateFormat.format(new Date());
			    
				//Logger.i(TAG, "Event Timestamp: " + event.getTimestamp(mContext) + " ------ Real Timestamp: " + mEventStartTimestamp );
				
				result = communicationManager.performOperation(new CMDecoratorOperation(mServerUrl, 
						new EventCreateOperation(mContext, event)),mContext);
			} catch (InvalidRequestException e) {
				e.printStackTrace();
				// does nothing, this should not happen in production.
				return false;
			} catch (InvalidResponseDataException e) {
				e.printStackTrace();
				// do nothing, temporarily server error, try later
				return false;
			} catch (OperationCancelledException e) {
				e.printStackTrace();
				// does nothing.
				return false;
			} catch (NoConnectivityException e) {
				e.printStackTrace();
				// does nothing.
				return false;
			}
			// if the post has successes, no result will return, else it's an error message
			String responseResult = (String) result.get(EventCreateOperation.RESULT_KEY_OBJECT);
			if (responseResult.equalsIgnoreCase(EventCreateOperation.RESULT_KEY_OBJECT_OK)) {
				return true;
			}
			return false;
		}

	}
	
}
