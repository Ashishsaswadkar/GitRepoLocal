package com.hungama.myplay.activity.gigya;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.MainActivity;

/**
 * @author DavidSvilem
 */
public class InviteFriendsActivity extends MainActivity{
	
	private static final String TAG = "InviteFriendsActivity";
	
	private static final String VALUE = "value";
	
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		
		// Invite Friends Fragment 
		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		InviteFriendsFragment inviteFriendsFragment = new InviteFriendsFragment();
		fragmentTransaction.replace(R.id.main_fragmant_container, inviteFriendsFragment);
		fragmentTransaction.commit();
		
	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		FlurryAgent.logEvent("Invite Friends");
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
	
}