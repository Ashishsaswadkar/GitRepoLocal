package com.hungama.myplay.activity.campaigns;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.EventManager;
import com.hungama.myplay.activity.data.events.CampaignPlayEvent;
import com.hungama.myplay.activity.data.events.PlayEvent;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.player.PlayerService.PlayerSericeBinder;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager.ServiceToken;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * Generic Activity for presenting videos in the campaigns section.   
 */
public class VideoViewActivity extends Activity implements OnPreparedListener, OnCompletionListener, OnErrorListener, OnBufferingUpdateListener, ServiceConnection {
	
	private static final String TAG = "VideoViewActivity";
	
	public static final String VIDEO_URL          = "Video_Url";
	public static final String CAMPAIGN_ID        = "Campaign_Id";
	public static final String CAMPAIGN_MEDIA_ID  = "Campaign_Media_Id";
	public static final String ACTION_TEXT 		  = "Action_Text";
	public static final String ACTION_URI 		  = "Action_Uri";
	
	private VideoView   video;
	private ProgressBar videoProgressBar;
	private TextView    actionTextView;
	
	private String   campaignId;
	private String   campaignMediaId;
	
	private boolean hasCompletePlay;
	private boolean hasStartPlay;
	
	private Activity activity;
	
	private DataManager mDataManager;
	
	// a token for connecting the player service.
	private ServiceToken mServiceToken = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videoview);

		// TODO: stop MyPlay Player
		
		Log.i(TAG, "onCreate()");
		
		activity = this;
		
		mDataManager = DataManager.getInstance(getApplicationContext());
		
		actionTextView = (TextView)findViewById(R.id.action_text);
		
		video = (VideoView) findViewById(R.id.videoView);
		MediaController mediaController = new MediaController(this);
		mediaController.setAnchorView(video);
		
		videoProgressBar = (ProgressBar) findViewById(R.id.videoProgressBar);
		
		// Get the video URL
		String videoUrl = getIntent().getStringExtra(VIDEO_URL);
		String actionText = getIntent().getStringExtra(ACTION_TEXT);
		final String actionUri = getIntent().getStringExtra(ACTION_URI);
		campaignId = getIntent().getStringExtra(CAMPAIGN_ID);
		campaignMediaId = getIntent().getStringExtra(CAMPAIGN_MEDIA_ID);
		
		if(videoUrl == null){
			videoUrl = "";
		}
		
		Uri uri = Uri.parse(videoUrl);
		video.setVideoURI(uri);	
		video.setMediaController(mediaController);
		video.setOnPreparedListener(this);
		video.setOnErrorListener(this);
		video.setOnCompletionListener(this);
		
		if(actionText == null || actionText.equalsIgnoreCase("")){
			actionTextView.setVisibility(View.GONE);
		}
		actionTextView.setText(actionText);
		
		actionTextView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(activity, WebViewActivity.class);
				
				intent.putExtra(ACTION_URI, actionUri);
				
				startActivity(intent);
			}
		});
		
		/*
		 * Binds to the PLayer service to pause it if playing.
		 */
		mServiceToken = PlayerServiceBindingManager.bindToService(this, this);
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		
		Log.i(TAG, "onPrepared()");
		
		video.start();
			
		hasStartPlay = true;
		
		videoProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		
		Log.i(TAG, "onCompletion()");
		
		hasCompletePlay = true;
		
		finish();
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		
		Log.i(TAG, "onError(): " + what + " " + extra);
		
		videoProgressBar.setVisibility(View.VISIBLE);
		
		return false;
	}
	
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.i(TAG, Integer.toString(percent));
	}
	
	@Override
	public void onDestroy() {
		
		// disconnects from the player service.
		PlayerServiceBindingManager.unbindFromService(mServiceToken);
		
		super.onDestroy();

		if(hasStartPlay){
			
			int playCurrentPostion = (video.getCurrentPosition() / 1000);
			int playDuration = playCurrentPostion; 
			int consumerId = mDataManager.getApplicationConfigurations().getConsumerID();
			String deviceId = mDataManager.getApplicationConfigurations().getDeviceID();
			String timeStamp = mDataManager.getDeviceConfigurations().getTimeStamp();
						
			CampaignPlayEvent campaignPlayEvent = 
					new CampaignPlayEvent(consumerId, 
										  deviceId, 
										  hasCompletePlay,
										  playDuration,timeStamp,0,0, 
										  campaignMediaId, 
										  Long.parseLong(campaignId), 
										  EventManager.PLAY);
			
			mDataManager.addEvent(campaignPlayEvent);
		}
	}

	/* (non-Javadoc)
	 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
	 */
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		/*
		 * we've establish a connection to the player service.
		 * if it plays, pause it.
		 */
		PlayerSericeBinder binder = (PlayerSericeBinder) service;
		PlayerService playerService = binder.getService();
		
		if (playerService.isLoading() || playerService.getState() == State.PLAYING) {
			playerService.pause();
		}
		
	}

	/* (non-Javadoc)
	 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
	 */
	@Override
	public void onServiceDisconnected(ComponentName name) {
		mServiceToken = null;
		
	}
	
}
