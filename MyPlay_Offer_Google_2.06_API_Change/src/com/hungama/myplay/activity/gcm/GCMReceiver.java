package com.hungama.myplay.activity.gcm;

import android.content.Context;
import com.google.android.gcm.GCMBroadcastReceiver;
import com.hungama.myplay.activity.R;


public class GCMReceiver extends GCMBroadcastReceiver {

	@Override
	protected String getGCMIntentServiceClassName(Context context) {
		return context.getString(R.string.gcm_service_class);
	}
}

