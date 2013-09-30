package com.hungama.myplay.activity.ui;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.player.PlayerService.PlayerSericeBinder;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager.ServiceToken;
import com.bugsense.trace.BugSenseHandler;

/**
 * Handles the connection to the player service, if it's playing. pauses it.
 */
public class SecondaryActivity extends SherlockFragmentActivity implements ServiceConnection {
	
	// a token for connecting the player service.
	private ServiceToken mServiceToken = null;

	@Override
	protected void onStart() {
		super.onStart();
		
		/*
		 * Binds to the PLayer service to pause it if playing.
		 */
		mServiceToken = PlayerServiceBindingManager.bindToService(this, this);
	}
	
	@Override
	protected void onStop() {
		// disconnects from the player service.
		PlayerServiceBindingManager.unbindFromService(mServiceToken);
		super.onStop();
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		/*
		 * we've establish a connection to the player service.
		 * if it plays, pause it.
		 */
//		PlayerSericeBinder binder = (PlayerSericeBinder) service;
//		PlayerService playerService = binder.getService();
//		
//		if (playerService.isLoading() || playerService.getState() == State.PLAYING) {
//			playerService.pause();
//		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mServiceToken = null;
	}
}
