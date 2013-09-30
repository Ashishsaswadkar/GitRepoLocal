/**
 * 
 */
package com.hungama.myplay.activity.gcm;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import com.google.android.gcm.GCMBaseIntentService;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.services.GCMRegistrationService;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.OnApplicationStartsActivity;
import com.hungama.myplay.activity.ui.PushNotificationActivity;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ToastExpander;

/**
 * @author DavidSvilem
 *
 */
public class GCMIntentService extends GCMBaseIntentService {

	public static final String GCM_INTENT = "gcm_intent";
	
	//public static final String SENDER_ID = "632603807834";
	public static final String SENDER_ID = "556683560964";
	
	private static final String TAG = "GCMIntentService";
	
	private final int NOTIFICATION_ID = 1;
		
	private Handler mHandler;
	private Context mContext;
	private ApplicationConfigurations mApplicationConfigurations;
	
	private static int notificationId = 0;
	
	public GCMIntentService(){
		
		super(SENDER_ID);		
	}
	
	@Override
	public void onCreate() {
		
		super.onCreate();
		
		mHandler = new Handler();
		mContext = this;	
		mApplicationConfigurations = new ApplicationConfigurations(mContext);
	}
	
	@Override
	protected void onRegistered(Context context, String regId) {
		
		Log.i(TAG, "Device registered: regId= " + regId);
		mApplicationConfigurations.setRegistrationId(regId);
	}
	
	@Override
	protected void onUnregistered(Context context, String regId) {
		Log.i(TAG, "Device unregistered");
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i(TAG, "Received message");
		Bundle bundle = intent.getExtras();
		
		String message = "";
		if(bundle != null){
			message = (String) bundle.get("message");
		}
		
		if(isApplicationVisible(context)){
			
	    	// Display message
			if(message != null){
				Intent notificationActivity = new Intent(getApplicationContext(), PushNotificationActivity.class);
				notificationActivity.putExtra(PushNotificationActivity.ACTIVITY_EXTRA_MESSAGE, message);
				notificationActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(notificationActivity);
				
//				mHandler.post(new DisplayToast(message));
			}
			
		}else{
			
			if(HomeActivity.wasInBackground){
				  
		    	// Get Application Name
		    	Logger.i(TAG, "BACKGROUND");
		        NotificationCompat.Builder mBuilder =
		                new NotificationCompat.Builder(this)
		                .setSmallIcon(R.drawable.icon_launcher)
		                .setContentTitle(getString(R.string.application_name))
		                .setContentText(message)
		                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
		                .setAutoCancel(false);
		        
		        // Creates an explicit intent for an Activity in your app
		        Intent resultIntent = new Intent(this, HomeActivity.class);
		        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
						 Intent.FLAG_ACTIVITY_SINGLE_TOP);
		        resultIntent.putExtra(GCM_INTENT, true);
		        
		        // The stack builder object will contain an artificial back stack for the
		        // started Activity.
		        // This ensures that navigating backward from the Activity leads out of
		        // your application to the Home screen.
		        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		        // Adds the back stack for the Intent (but not the Intent itself)
		        stackBuilder.addParentStack(HomeActivity.class);
		        // Adds the Intent that starts the Activity to the top of the stack
		        stackBuilder.addNextIntent(resultIntent);
		        PendingIntent resultPendingIntent =
		                stackBuilder.getPendingIntent(
		                    0,
		                    PendingIntent.FLAG_UPDATE_CURRENT);
		        mBuilder.setContentIntent(resultPendingIntent);
		        NotificationManager mNotificationManager =
		            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		        
		        // mId allows you to update the notification later on.
//		        notificationId++;
		        Logger.i(TAG, String.valueOf(notificationId));
		        mNotificationManager.notify(notificationId++, mBuilder.build());
		    
			}else{
		    	
		    	// Get Application Name
		    	
		        NotificationCompat.Builder mBuilder =
		                new NotificationCompat.Builder(this)
		                .setSmallIcon(R.drawable.icon_launcher)
		                .setContentTitle(getString(R.string.application_name))
		                .setContentText(message)
		                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
		                .setAutoCancel(false);
		        
		        // Creates an explicit intent for an Activity in your app
		        Intent resultIntent = new Intent(this, OnApplicationStartsActivity.class);
		        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
						 Intent.FLAG_ACTIVITY_SINGLE_TOP);
		        resultIntent.putExtra(GCM_INTENT, true);
		        
		        // The stack builder object will contain an artificial back stack for the
		        // started Activity.
		        // This ensures that navigating backward from the Activity leads out of
		        // your application to the Home screen.
		        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		        // Adds the back stack for the Intent (but not the Intent itself)
		        stackBuilder.addParentStack(OnApplicationStartsActivity.class);
		        // Adds the Intent that starts the Activity to the top of the stack
		        stackBuilder.addNextIntent(resultIntent);
		        PendingIntent resultPendingIntent =
		                stackBuilder.getPendingIntent(
		                    0,
		                    PendingIntent.FLAG_UPDATE_CURRENT			                );
		        mBuilder.setContentIntent(resultPendingIntent);
		        NotificationManager mNotificationManager =
		            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		        
		        // mId allows you to update the notification later on.
//		        notificationId++;
		        Logger.i(TAG, String.valueOf(notificationId));
		        mNotificationManager.notify(notificationId++, mBuilder.build());
		    	
			}
		}
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.i(TAG, "Received deleted messages notification, count: " + total);
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		Log.i(TAG, "Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}
		
	private class DisplayToast implements Runnable{
		
		String mText;
		
		public DisplayToast(String text){
			this.mText = text;
		}
		
		@Override
		public void run() {
			
			Toast toast = new Toast(mContext);
			toast = Toast.makeText(mContext, "GCM Message: " + mText, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			
			ToastExpander.showFor(
					toast,
					2000);
		}
	}

    public static boolean isApplicationVisible(final Context context) {
        final String packageName = context.getPackageName();
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equalsIgnoreCase(packageName)
                    && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                return true;
        }
        return false;
    }
	
}
