package com.hungama.myplay.activity.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.hungama.myplay.activity.R;

public class PushNotificationActivity extends FragmentActivity{

	private static final String TAG = "PushNotificationActivity";
	
	
	public static final String ACTIVITY_EXTRA_MESSAGE = "activity_extra_message";

	private String pushMessage;
	
	// ======================================================
	// ACTIVITY'S LIFECYCLE.
	// ======================================================
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		setContentView(R.layout.activity_badges_and_coins);
		
		// shows the push dialog.
		pushMessage = getIntent().getStringExtra(ACTIVITY_EXTRA_MESSAGE);
		if (pushMessage != null) {
			showPushDialog(pushMessage);			
		}		
				
	}
	
	// ======================================================
	// Helper Methods.
	// ======================================================
	
	private void showPushDialog(String message) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		 
		// set title
//		alertDialogBuilder.setTitle(getResources().getString(R.string.new_version_title));

		// set dialog message
		alertDialogBuilder
			.setMessage(message)
			.setCancelable(true)			
			.setNegativeButton(R.string.discovery_message_error_no_category_or_mood_confirm ,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, just close
					// the dialog box and do nothing
					dialog.cancel();
					finish();
				}
			});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
			try {
		        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		        r.play();
		    } catch (Exception e) {}
			
			alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					dialog.dismiss();
					finish();
					
				}
			});

	}
}
