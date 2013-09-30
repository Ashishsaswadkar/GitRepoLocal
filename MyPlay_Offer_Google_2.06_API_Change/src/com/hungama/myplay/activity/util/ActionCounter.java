package com.hungama.myplay.activity.util;

import android.os.Handler;
import android.os.Message;

/**
 * Helper class for performing an action by delayed interval invoked by {@link OnActionCounterPerform}.
 */
public class ActionCounter {
	
	/**
	 * Interface definition for a callback to be invoked when the {@link ActionCounter} performs the action.
	 */
	public interface OnActionCounterPerform {
		
		/**
		 * Callback method to be invoked when {@link ActionCounter} performs the action.
		 */
		public void onActionCounterPerform(int actionId);
	}
	
	private final int mIntervalToPerformInMilliseconds;

	// handler to post execution to OnActionCounterPerform implementor.
	private static final int CLIENT_MESSAGE_PERFORM_ACTION = 1;
	private Handler mClientHandler = new Handler() {
		
		public void handleMessage(Message message) {
			if (message.what == CLIENT_MESSAGE_PERFORM_ACTION) {
				if (mOnActionCounterPerform != null) {
					mOnActionCounterPerform.onActionCounterPerform(message.arg1);
				}
			}
		};
	};
	
	
	private Handler mInternalHandler;	// handler to post execution in the interval.
	private Runnable mNotification;		// executor task after post.
	
	public ActionCounter(int intervalToPerformInMilliseconds) {
		mIntervalToPerformInMilliseconds = intervalToPerformInMilliseconds;
	}
	
	private OnActionCounterPerform mOnActionCounterPerform;
	
	/**
	 * Sets a listener to be notified when an action performed.
	 */
	public void setOnActionCounterPerform(OnActionCounterPerform listener) {
		mOnActionCounterPerform = listener;
	}
	
	/**
	 * Starts to count and perform any action, any recent actions will be cancelled. 
	 */
	public void performAction(final int actionId) {
		
		// resets any post actions.
		if (mInternalHandler != null && mNotification != null) {
			mInternalHandler.removeCallbacks(mNotification);
			mInternalHandler = null;
			mNotification = null;
		}
		
		mInternalHandler = new Handler();
		mNotification = new Runnable() {
			
			@Override
			public void run() {
				if (mClientHandler != null) {
					Message message = Message.obtain();
					message.what = CLIENT_MESSAGE_PERFORM_ACTION;
					message.arg1 = actionId;
					mClientHandler.sendMessage(message);
				}
			}
		};
		
		mInternalHandler.postDelayed(mNotification, mIntervalToPerformInMilliseconds);
	}
	
	/**
	 * Cancel to count and perform any action last pending action. 
	 */
	public void cancelAction() {
		// resets any post actions.
		if (mInternalHandler != null && mNotification != null) {
			
			mInternalHandler.removeCallbacks(mNotification);
			
			mInternalHandler = null;
			mNotification = null;
		}
	}
	
	/**
	 * Stops any action, after calling this function you must create new {@link ActionCounter}.
	 */
	public void cancelAnyAction() {
		// resets any post actions.
		if (mInternalHandler != null && mNotification != null) {
			
			mInternalHandler.removeCallbacks(mNotification);
			
			mInternalHandler = null;
			mNotification = null;
		}
		
		if (mClientHandler != null) {
			mClientHandler.removeCallbacks(null);
			mClientHandler = null;
		}
	}

}
