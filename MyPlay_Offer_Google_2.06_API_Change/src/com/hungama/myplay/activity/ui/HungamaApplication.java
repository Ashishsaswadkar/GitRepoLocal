package com.hungama.myplay.activity.ui;

import android.app.Application;
import android.util.Log;

import com.AdX.tag.AdXConnect;
import com.bugsense.trace.BugSenseHandler;
import com.hungama.myplay.activity.BuildConfig;
import com.hungama.myplay.activity.data.DataManager;

public class HungamaApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		// initializes the Crashes Report Library.
		//BugSenseHandler.initAndStartSession(getApplicationContext(), "25b4d52f");
		BugSenseHandler.initAndStartSession(getApplicationContext(), "062612ae"); //Hungama Key
		
		// initializes the AdX library.
		DataManager mDataManager = DataManager.getInstance(getApplicationContext());
		boolean hasRunBefore = !mDataManager.getApplicationConfigurations().isFirstVisitToApp();

		// sets the log level of the library.
		int logLevel = Log.ERROR;
		if (BuildConfig.DEBUG) {
			logLevel = Log.VERBOSE;
		}
		AdXConnect.getAdXConnectInstance(getApplicationContext(), hasRunBefore, logLevel);
	}
	
}
