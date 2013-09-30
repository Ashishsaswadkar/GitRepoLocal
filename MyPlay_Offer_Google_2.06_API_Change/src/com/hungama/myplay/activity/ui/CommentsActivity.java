package com.hungama.myplay.activity.ui;

import java.io.Serializable;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RelativeLayout;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.player.PlayerService.PlayerSericeBinder;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager.ServiceToken;
import com.hungama.myplay.activity.ui.fragments.CommentsFragment;

/**
 * Shows the comments per media item,
 * This Activity usually been called explicitly by the player bar's comment buttons.  
 */
public class CommentsActivity extends MainActivity implements ServiceConnection {
	
	public static final String EXTRA_DATA_MEDIA_ITEM = "extra_data_media_item";
	public static final String EXTRA_DATA_DO_SHOW_TITLE = "extra_data_do_show_title";
	
	// a token for connecting the player service.
	private ServiceToken mServiceToken = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comments);
		
		/*
		 * called for the first time,
		 * adds the given comments fragment.
		 */
		if (getIntent().getExtras() == null) {
			return;
		}
		
		// pulls the argument for the activity, and packs them as args for the fragment.
		Bundle incomingArgs = getIntent().getExtras();
		MediaItem mediaItem = (MediaItem) incomingArgs.getSerializable(EXTRA_DATA_MEDIA_ITEM);
//		boolean doShowTitle = incomingArgs.getBoolean(EXTRA_DATA_DO_SHOW_TITLE, true);
		
		// creates the fragment.
		Bundle outcomingArgs = new Bundle();
		outcomingArgs.putSerializable(EXTRA_DATA_MEDIA_ITEM, (Serializable) mediaItem);
		
		CommentsFragment commentsFragment = new CommentsFragment();
		commentsFragment.setArguments(outcomingArgs);
		
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.comments_fragmant_container, commentsFragment, CommentsFragment.FRAGMENT_COMMENTS);
		fragmentTransaction.commit();
		
	}
	
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
		PlayerSericeBinder binder = (PlayerSericeBinder) service;
		PlayerService playerService = binder.getService();
		
		// does nothing, just holds the connection to the playing service.
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mServiceToken = null;
	}
		
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		/*
		 * Invoked when the activity was visible,
		 * replace the old fragment with the new one to update the visible data.
		 */
		
		if (intent.getExtras() == null) {
			return;
		}
		
		// pulls the argument for the activity, and packs them as args for the fragment.
		Bundle incomingArgs = getIntent().getExtras();
		MediaItem mediaItem = (MediaItem) incomingArgs.getSerializable(EXTRA_DATA_MEDIA_ITEM);
		boolean doShowTitle = incomingArgs.getBoolean(EXTRA_DATA_DO_SHOW_TITLE, true);
		
		
		// creates the fragment.
		Bundle outcomingArgs = new Bundle();
		outcomingArgs.putSerializable(EXTRA_DATA_MEDIA_ITEM, (Serializable) mediaItem);
		outcomingArgs.putBoolean(EXTRA_DATA_DO_SHOW_TITLE, doShowTitle);
		
		CommentsFragment commentsFragment = new CommentsFragment();
		commentsFragment.setArguments(outcomingArgs);
		
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);			
		
		fragmentTransaction.replace(R.id.comments_fragmant_container, commentsFragment, CommentsFragment.FRAGMENT_COMMENTS);
		fragmentTransaction.commit();
	}

	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.MUSIC;
	}

	@Override
	public void onBackPressed() {
		// checks if the webview exists and calls its back button support.
//		Fragment fragmentParent = getSupportFragmentManager().findFragmentByTag(CommentsFragment.FRAGMENT_COMMENTS);
//		if (fragmentParent != null) {
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(TwitterLoginFragment.FRAGMENT_TWITTER_LOGIN);
			if (fragment != null) {
				TwitterLoginFragment twitterLoginFragment = (TwitterLoginFragment) fragment;
				twitterLoginFragment.onBackPressed();
				return;			
			}
//		}
		super.onBackPressed();
	}
	
	public void toggleActivityTitle() {
		RelativeLayout titleBar = (RelativeLayout) findViewById(R.id.comments_title_bar);
		if (titleBar.getVisibility() == View.VISIBLE) {
			titleBar.setVisibility(View.GONE);
		} else {
			titleBar.setVisibility(View.VISIBLE);
		}
		
	}

}
