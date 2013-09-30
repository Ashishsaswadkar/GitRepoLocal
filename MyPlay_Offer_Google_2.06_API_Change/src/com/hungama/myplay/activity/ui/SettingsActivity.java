package com.hungama.myplay.activity.ui;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment;
import com.hungama.myplay.activity.ui.fragments.AboutWebViewFragment;
import com.hungama.myplay.activity.ui.fragments.SettingsFragment;

public class SettingsActivity extends SecondaryActivity implements ServiceConnection {

	private final String TAG = "SettingsActivity";
	
	public static final String ARGUMENT_SETTINGS_ACTIVITY = "argument_settings_activity";
	public static final int LOGIN_ACTIVITY_CODE = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		
		// Invite Friends Fragment 
		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		SettingsFragment settingsFragment = new SettingsFragment();
		fragmentTransaction.replace(R.id.main_fragmant_container, settingsFragment);
		fragmentTransaction.commit();
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		/*
		 * Overriding this to make the activity still playing the music,
		 * instead pausing it by default.
		 */
	}
	
	
	@Override
	public void onBackPressed() {
		// checks if the webview exists and calls its back button support.
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(TwitterLoginFragment.FRAGMENT_TWITTER_LOGIN);
		if (fragment != null) {
			TwitterLoginFragment twitterLoginFragment = (TwitterLoginFragment) fragment;
			twitterLoginFragment.onBackPressed();
			return;			
		}
		
		super.onBackPressed();
	}

}
