package com.hungama.myplay.activity.ui;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.BadgesAndCoins;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Video;
import com.hungama.myplay.activity.data.events.PlayEvent;
import com.hungama.myplay.activity.data.events.PlayEvent.PlayingSourceType;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.MediaHandleOperation;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.RelatedVideoOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.VideoStreamingOperationAdp;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerService.PlayerSericeBinder;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager.ServiceToken;
import com.hungama.myplay.activity.ui.adapters.MediaTilesAdapter;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.saranyu.SaranyuVideo;

/**
 * Controller for presenting details of the given MediaItem.
 */
@SuppressLint("NewApi")
public class VideoActivity extends MainActivity implements CommunicationOperationListener, 
												OnClickListener, OnCompletionListener, 
												SeekBar.OnSeekBarChangeListener, 
												OnPreparedListener, OnMediaItemOptionSelectedListener,
												ServiceConnection,
												OnErrorListener,
												SaranyuVideo.OnProfileInfoListener,
												OnBufferingUpdateListener
												{
	
	private static final String TAG = "VideoActivity";
	
	public static final String ARGUMENT_SEARCH_VIDEO = "argument_search_video";
	protected static final String FRAGMENT_TAG_VIDEO_SEARCH = "fragment_tag_video_search";
	
	public static final String EXTRA_MEDIA_ITEM = "extra_media_item";
	public static final String EXTRA_MEDIA_ITEM_VIDEO = "extra_media_item_video";
	public static final String ARGUMENT_MEDIAITEM = "argument_mediaitem";
	
	public static final int UPGRADE_ACTIVITY_RESULT_CODE = 1001;
	public static final int BADGES_ACTIVITY_RESULT_CODE	 = 1002;
	
	public static final int SUCCESS = 1;
	
	// a token for connecting the player service.
	private ServiceToken mServiceToken = null;
	
	private FragmentManager mFragmentManager;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private Video video;	
	private VideoView videoView;
	public static LinearLayout videoControllersBar;
	private TextView totalDurationLabel;
	private TextView currentDurationLabel;
	private SeekBar videoProgressBar;
	private ImageButton playButton;
	
	public static final int PERIOD = 4*1000;
//	private static TimeoutVideoPlayerTask mTimeoutVideoPlayerTask;
	private CountDownTimer mCountDownTimer;
	
	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();	
	
	private MediaItem mMediaItem;	
	private boolean mHasLoaded = false; 
	
	// INFO PAGE
	private MediaTrackDetails mMediaTrackDetails;
	private RelativeLayout infoPage;
	private Button infoPageButton;
	private Button shareButton;
	private Button downloadButton;
	private boolean infoWasClicked = false;
	private LinearLayout infoAlbum;
	private LinearLayout infoLanguageCategory;
	private LinearLayout infoMood;
	private LinearLayout infoGenre;
	private LinearLayout infoMusic;
	private LinearLayout infoSingers;
	private LinearLayout infoCast;
	private LinearLayout infoLyrics;
	
	private MediaTilesAdapter mHomeMediaTilesAdapter;
	private GridView mTilesGridView;
	private int mTileSize = 0;
	private RelativeLayout relatedVideoPage;
	private Button relatedVideoPageButton;
	
	private Dialog upgradeDialog;
	private Date userCurrentPlanValidityDate;
	private Date today;
	
	// Favorites
	private Drawable whiteHeart;
	private Drawable blueHeart;
	private String mediaType;
	private Button favButton;
	private Button commentsButton;
	
	private boolean isHasToCallBadgeApi = true;
	private boolean isBackFromBadgeApi = false;
	private boolean isInfoPagePopulated= false;	
	private boolean isFavoritePressed = false;
	
	// For testing
	private int playButtonClickCounter;
	
	private int networkSpeed; //bandwidth
	private String networkType;	
	
	private long mFileSize;
    //Calculate bandwidth
    private boolean firstEntry = true;
    private boolean lastEntry = true;
    private int percentStart;
    private long startTimeToCalculateBitrate;
    private long endTimeToCalculateBitrate;
    
    private boolean mIsSendToBackgroundPowerButtonPress = false;
    private boolean mIsSendToBackgroundHomeButtonPress = false;
    private int mCurrentPosition;
    private int mDuration;
    
    private List<MediaItem> mediaItems = new ArrayList<MediaItem>();

	private boolean pauseVideo = true;
    
    private String googleEmailId = null;
    
	// ======================================================
	// Activity life-cycle callbacks. 
	// ======================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_video);
		
		mDataManager = DataManager.getInstance(this.getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		// gets the media item from parent.
		Bundle data = getIntent().getExtras();
		mMediaItem = (MediaItem) data.getSerializable(ARGUMENT_MEDIAITEM);
		mFragmentManager = getSupportFragmentManager();
		// validate calling intent.
		Intent intent = getIntent();
		if (intent == null) {
			Logger.e(TAG, "No intent for the given Activity.");
			return; 
		}
		
		data = intent.getExtras();
		if (data != null && data.containsKey(EXTRA_MEDIA_ITEM_VIDEO)) {
				setContentView(R.layout.activity_video);
				// retrieves the given Media item for the activity.
				mMediaItem = (MediaItem) data.getSerializable(EXTRA_MEDIA_ITEM_VIDEO);			
		} else {
			Logger.e(TAG, "No MediaItem set for the given Activity.");
			return; 
		}	
		
		//For Favorites
		whiteHeart = getResources().getDrawable(R.drawable.icon_media_details_fav_white);
		blueHeart = getResources().getDrawable(R.drawable.icon_media_details_fav_blue);
		
		
		/*
		 * For placing the tiles correctly in the grid, 
		 * calculates the maximum size that a tile can be and the column width.
		 */
		int imageTileSpacing = getResources().getDimensionPixelSize(R.dimen.home_tiles_spacing_vertical);
		// measuring the device's screen width. and setting the grid column width.
        Display display = getWindowManager().getDefaultDisplay();
        int screenWidth = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
        	screenWidth = display.getWidth();
        } else {
        	Point displaySize = new Point();
        	display.getSize(displaySize);
        	screenWidth = displaySize.x;
        }
		
		/*
		 * Binds to the PLayer service to pause it if playing.
		 */
		mServiceToken = PlayerServiceBindingManager.bindToService(this, this);
		
        mTileSize = (int) ((screenWidth - (imageTileSpacing + imageTileSpacing*1.5)) / 2);
        
        Logger.i(TAG, "screenWidth: " + screenWidth + " mTileSize: " + mTileSize);
        
        mHomeMediaTilesAdapter = new MediaTilesAdapter(this, mTilesGridView, mTileSize, mediaItems, false);
		mHomeMediaTilesAdapter.setOnMusicItemOptionSelectedListener(this);
		
		relatedVideoPage = (RelativeLayout) findViewById(R.id.video_related_relativelayout_page);
		relatedVideoPage.setOnClickListener(this);
		
		mTilesGridView = (GridView) findViewById(R.id.video_related_gridview_tiles);
        mTilesGridView.setNumColumns(2);
        mTilesGridView.setColumnWidth(mTileSize);
        mTilesGridView.setAdapter(mHomeMediaTilesAdapter);
	
    	// Get google email id 
		AccountManager accountManager = AccountManager.get(this);
		Account[] accounts = accountManager.getAccountsByType("com.google");
		
		String accountType = null;
		if(accounts != null && accounts.length > 0){
			accountType = accounts[0].name;
			googleEmailId = accountType;
		}
		mDataManager.getCurrentSubscriptionPlan(this, accountType);
	}
	
	@Override
	protected void onPause() {
		Log.i(TAG, "onPause()");
		if (videoView != null) {
			if (pauseVideo) {
				mIsSendToBackgroundPowerButtonPress = true;
				mIsSendToBackgroundHomeButtonPress = true;
				mCurrentPosition = videoView.getCurrentPosition();
				mDuration = videoView.getDuration();
				videoView.pause();
			}
        }
		super.onPause();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		/*
		 * Faking the name of the title due to an 
		 * unsupported deep navigation (more then two items).
		 * This Activity will set the title hardcoded.
		 */
		getSupportActionBar().setTitle(NavigationItem.VIDEOS.title);
	}

	@Override
	protected void onResume() {
		Log.i(TAG,  "onResume()");
		if (!isBackFromBadgeApi) {
			if (!isFavoritePressed) {
				if (!mHasLoaded) {
					SaranyuVideo s = new SaranyuVideo();
					VideoView videoViewSaranyu = (VideoView) findViewById(R.id.vview_saranyu);
//					networkSpeed = s.getCurrentBandwidth();
					networkSpeed = getNetworkBandwidth();
			        networkType = s.getNetworkInfo(getBaseContext());
					networkType = Utils.getNetworkType(this);
			        String contentFormat =  mApplicationConfigurations.getContentFormat();
			        if (TextUtils.isEmpty(contentFormat)) {
			        	s.getProfileCapablity(videoViewSaranyu,this);
			        } else {
			        	
			        	mDataManager.getVideoDetailsAdp(mMediaItem, networkSpeed, networkType, contentFormat, this, googleEmailId);
			        }
			        
//					mDataManager.getVideoDetails(mMediaItem, this);				
				} else {
					// get details for video (video streaming).
					initializeComponents();
					if (!isInfoPagePopulated) {
						isInfoPagePopulated= true;
						populateInfoPage();
					}
					populateUserControls(video);
				}
				onConfigurationChanged(getResources().getConfiguration());
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);	
			}
			
			if (isFavoritePressed){
				isFavoritePressed = false;
			}
		} else {
			isBackFromBadgeApi = false;
		}
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		// disconnects from the player service.
		PlayerServiceBindingManager.unbindFromService(mServiceToken);
		
		stopVideoPlayEvent(false, mCurrentPosition);
		
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// put inside the current duaration.
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		
	}
	
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

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mServiceToken = null;
	}
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		cancelThread();		
		mDataManager.cancelGetSearch();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		closePage(relatedVideoPage, relatedVideoPageButton);
	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG,  "onActivityResult()");
		if (requestCode == UPGRADE_ACTIVITY_RESULT_CODE && resultCode == RESULT_OK) {						
			videoView.stopPlayback();
			mHasLoaded = false;	
		} else if (requestCode == BADGES_ACTIVITY_RESULT_CODE && resultCode == RESULT_OK && data != null) {
			pauseVideo = true;
		}
	}
	
	// ======================================================
	// Operation Callback
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING_ADP) {
			showLoadingDialog(R.string.application_dialog_loading_content);
			mHasLoaded = true;
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			if (mMediaTrackDetails == null) {
				showLoadingDialog(R.string.application_dialog_loading_content);
				mHasLoaded = true;
			}
		} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
			//showLoadingDialog(R.string.application_dialog_loading_content);
			isFavoritePressed = true;
		} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
			//showLoadingDialog(R.string.application_dialog_loading_content);
		}
	}
		
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING_ADP) {
			video = (Video) responseObjects.get(VideoStreamingOperationAdp.RESPONSE_KEY_VIDEO_STREAMING_ADP);
			
			
			// Replacing the CountDownTimer below with this AsynTask
			GetVideoContentLengthAsync asyncTask = new GetVideoContentLengthAsync();
			asyncTask.setVideoUrl(video.getVideoUrl());
			asyncTask.execute();
			
//			CountDownTimer mGetContentLength = new CountDownTimer(1,1) {
//				
//				@Override
//				public void onTick(long millisUntilFinished) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//				@Override
//				public void onFinish() {
//					URL url;
//					try {
//						url = new URL(video.getVideoUrl());
//						URLConnection urlConnection = url.openConnection();
//						urlConnection.connect();
//						mFileSize = urlConnection.getContentLength();	
//						Logger.i("MediaHandleOperation", "File Size = " + mFileSize);
//						cancelThread();
//						
//					} catch (MalformedURLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//				}
//			}.start();
			
			initializeComponents();
//			populateUserControls(video);
			mDataManager.getMediaDetails(mMediaItem, null, this);
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			if (mMediaItem.getMediaType() == MediaType.TRACK || mMediaItem.getMediaType() == MediaType.VIDEO) {
				// get details for track (video).
				mMediaTrackDetails = (MediaTrackDetails) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
				populateUserControls(video);
				if (infoWasClicked) {
					hideLoadingDialog();
					closePage(relatedVideoPage, relatedVideoPageButton);
					openInfoPage();
				} else {
//					hideLoadingDialog();
				}

				mDataManager.getRelatedVideo(mMediaTrackDetails, mMediaItem, this);
				if (!isInfoPagePopulated) {
					isInfoPagePopulated= true;
					populateInfoPage();
				}				
			}
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
						
			//mediaItems = (List<MediaItem>) responseObjects.get(RelatedVideoOperation.RESPONSE_KEY_RELATED_VIDEO);
			//mHomeMediaTilesAdapter.stopLoadingImages();
			//mHomeMediaTilesAdapter.setMediaItems(mediaItems);
			mediaItems.addAll((List<MediaItem>) responseObjects.get(RelatedVideoOperation.RESPONSE_KEY_RELATED_VIDEO));
			mHomeMediaTilesAdapter.notifyDataSetChanged();
			//mHomeMediaTilesAdapter.setOnMusicItemOptionSelectedListener(this);
			//mHomeMediaTilesAdapter.resumeLoadingImages();
								
		} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
			BaseHungamaResponse addToFavoriteResponse = (BaseHungamaResponse) responseObjects.get(AddToFavoriteOperation.RESULT_KEY_ADD_TO_FAVORITE);	
			if (addToFavoriteResponse.getCode() == SUCCESS ) {
//				Toast.makeText(this, getResources().getString(R.string.favorite_saved, mMediaItem.getTitle()), Toast.LENGTH_LONG).show();				
				favButton.setCompoundDrawablesWithIntrinsicBounds(null, blueHeart, null, null);
				if (mMediaTrackDetails != null && mMediaTrackDetails.getNumOfFav() >= 0) {
					favButton.setText(String.valueOf(mMediaTrackDetails.getNumOfFav() + 1));
				}
				favButton.setSelected(true);
				isFavoritePressed = true;
				
				BadgesAndCoins badgesAndCoins = mApplicationConfigurations.getBadgesAndCoinsForVideoActivity();
				if  ( badgesAndCoins != null && addToFavoriteResponse.getDisplay() != 1) {
					pauseVideo  = false;
					startBadgesAndCoinsActivity(badgesAndCoins);
				}
			} else {
				Toast.makeText(this, getResources().getString(R.string.favorite_error_saving, mMediaItem.getTitle()), Toast.LENGTH_LONG).show();
			}
			favButton.setClickable(true);
			hideLoadingDialog();
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
			BaseHungamaResponse removeFromFavoriteResponse = (BaseHungamaResponse) responseObjects.get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);	
			if (removeFromFavoriteResponse.getCode() == SUCCESS ) {
				Toast.makeText(this, getResources().getString(R.string.favorite_removed, mMediaItem.getTitle()), Toast.LENGTH_LONG).show();
				favButton.setCompoundDrawablesWithIntrinsicBounds(null, whiteHeart, null, null);
				if (mMediaTrackDetails != null && mMediaTrackDetails.getNumOfFav() >= 0) {
					favButton.setText(String.valueOf(mMediaTrackDetails.getNumOfFav()));
				}
				favButton.setSelected(false);
				isFavoritePressed = true;
			} else {
				Toast.makeText(this, getResources().getString(R.string.favorite_error_removing, mMediaItem.getTitle()), Toast.LENGTH_LONG).show();
			}
			favButton.setClickable(true);
			hideLoadingDialog();
		
		}
	}	
		
	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING_ADP) {
			Logger.i(TAG, "Failed loading video streaming");
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			Logger.i(TAG, "Failed loading video details");
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
			Logger.i(TAG, "Failed loading related videos");
		
		} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
			Logger.i(TAG, "Failed loading add to favorites");
			favButton.setClickable(true);
		
		}else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
			Logger.i(TAG, "Failed loading remove from favorites");
			favButton.setClickable(true);
		}
		hideLoadingDialog();
		
	}
	
	/**
	 * Initialize Components
	 * */
	public void initializeComponents () {
		videoView = (VideoView) findViewById(R.id.videoview_video_details);
		videoView.setOnErrorListener(this);
		videoControllersBar = (LinearLayout) findViewById(R.id.linearlayout_player_bar);
		totalDurationLabel = (TextView) findViewById(R.id.textview_video_player_scale_length);
		currentDurationLabel = (TextView) findViewById(R.id.textview_video_player_scale_current);
		videoProgressBar = (SeekBar) findViewById(R.id.seekbar_video_player);
		videoProgressBar.setOnSeekBarChangeListener(this);	
		videoView.setOnPreparedListener(this);
		videoView.setOnCompletionListener(this);
		playButton = (ImageButton) findViewById(R.id.button_video_player_play_pause);
		playButton.setOnClickListener(this);
		ImageButton fullScreenButton = (ImageButton) findViewById(R.id.button_video_player_fullscreen);
		fullScreenButton.setOnClickListener(this);
		// Info Page
		infoPage = (RelativeLayout) findViewById(R.id.relativelayout_info_page);
		infoPage.setOnClickListener(this);
		infoPageButton = (Button) findViewById(R.id.video_player_content_actions_bar_button_info);
		infoPageButton.setOnClickListener(this);
		infoAlbum = (LinearLayout) findViewById(R.id.textview_row_1_right);
		infoAlbum.setOnClickListener(this);
		infoLanguageCategory = (LinearLayout) findViewById(R.id.textview_row_2_right);
		infoLanguageCategory.setOnClickListener(this);
		infoMood = (LinearLayout) findViewById(R.id.textview_row_3_right);
		infoMood.setOnClickListener(this);
		infoGenre = (LinearLayout) findViewById(R.id.textview_row_4_right);
		infoGenre.setOnClickListener(this);
		infoMusic = (LinearLayout) findViewById(R.id.textview_row_5_right);
		infoMusic.setOnClickListener(this);
		infoSingers = (LinearLayout) findViewById(R.id.textview_row_6_right);
		infoSingers.setOnClickListener(this);
		infoCast = (LinearLayout) findViewById(R.id.textview_row_7_right);
		infoCast.setOnClickListener(this);
		infoLyrics = (LinearLayout) findViewById(R.id.textview_row_8_right);
		infoLyrics.setOnClickListener(this);
		
		shareButton = (Button) findViewById(R.id.video_player_content_actions_bar_button_share);
		shareButton.setOnClickListener(this);
		
		downloadButton = (Button) findViewById(R.id.video_player_content_actions_bar_button_download);
		
		mCountDownTimer = new CountDownTimer(PERIOD, 1000) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFinish() {
				if (videoControllersBar != null && videoControllersBar.getVisibility() == View.VISIBLE) {
					videoControllersBar.setVisibility(View.GONE);					
				}
				cancelThread();
				
			}
		};
		videoView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				onClick(playButton);
				if (videoControllersBar.getVisibility() == View.VISIBLE) {
					videoControllersBar.setVisibility(View.GONE);
				} else {
					videoControllersBar.setVisibility(View.VISIBLE);
					
//					if (mTimeoutVideoPlayerTask == null) {        	
//						mTimeoutVideoPlayerTask = new TimeoutVideoPlayerTask(PERIOD);
//			        }
//					updateControllersVisibilityThread();				
				}
				return false;
			}
		});
		
		// For Related Video
		//mTilesGridView = (GridView) findViewById(R.id.video_related_gridview_tiles);
		relatedVideoPageButton = (Button) findViewById(R.id.video_player_content_actions_bar_button_related);
		relatedVideoPageButton.setOnClickListener(this);
//		relatedVideoPage = (RelativeLayout) findViewById(R.id.video_related_relativelayout_page);
//		relatedVideoPage.setOnClickListener(this);
		
//		/*
//		 * For placing the tiles correctly in the grid, 
//		 * calculates the maximum size that a tile can be and the column width.
//		 */
//		int imageTileSpacing = getResources().getDimensionPixelSize(R.dimen.home_tiles_spacing_vertical);
//		// measuring the device's screen width. and setting the grid column width.
//        Display display = getWindowManager().getDefaultDisplay();
//        int screenWidth = 0;
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
//        	screenWidth = display.getWidth();
//        } else {
//        	Point displaySize = new Point();
//        	display.getSize(displaySize);
//        	screenWidth = displaySize.x;
//        }
//       
//        mTileSize = (int) ((screenWidth - (imageTileSpacing + imageTileSpacing*1.5)) / 2);
//        
//        Logger.i(TAG, "screenWidth: " + screenWidth + " mTileSize: " + mTileSize);
//        mTilesGridView.setNumColumns(2);
//        mTilesGridView.setColumnWidth(mTileSize);
//        
//        mHomeMediaTilesAdapter = new MediaTilesAdapter(this, mTilesGridView, mTileSize, null, false);
//		  mTilesGridView.setAdapter(mHomeMediaTilesAdapter);
		
		if(mHomeMediaTilesAdapter != null){
			mHomeMediaTilesAdapter.notifyDataSetChanged();
		}
		
		//For upgrade dialog in landscape
		initializeUpgradeDialog();
		//userCurrentPlanValidityDate = Utils.convertStringToDate(mApplicationConfigurations.getUserSubscriptionPlanDate());
		userCurrentPlanValidityDate = Utils.convertTimeStampToDate(mApplicationConfigurations.getUserSubscriptionPlanDate());
	}
	
	// ======================================================
	// Video Controllers timer thread
	// ======================================================
	
	/**
	 * Update timer on SeekBar
	 * */
	public void updateProgressBar() {
		
        mHandler.postDelayed(mUpdateTimeTask, 100);         
    }	
	
	/**
	 * Background Runnable thread for updating time (total and current time, time slider).
	 * */
	private Runnable mUpdateTimeTask = new Runnable() {
		
		public void run() {
			   
			long totalDuration = videoView.getDuration();
			long currentDuration = videoView.getCurrentPosition();
//			Logger.i(TAG, String.valueOf(currentDuration));
			if (currentDuration > 60000 && isHasToCallBadgeApi) {
//				Logger.i(TAG, String.valueOf(currentDuration) + "Badges HIT!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");
				isHasToCallBadgeApi = false;
				isBackFromBadgeApi = true;
				mDataManager.checkBadgesAlert(String.valueOf(mMediaItem.getId()),	
						"video", 
						"watch_video", 
						VideoActivity.this);
			}
			// Displaying Total Duration time
			totalDurationLabel.setText("" + Utils.milliSecondsToTimer(totalDuration));
			// Displaying time completed playing
			currentDurationLabel.setText("" + Utils.milliSecondsToTimer(currentDuration));
			   
			// Updating progress bar
			int progress = (int)(Utils.getProgressPercentage(currentDuration, totalDuration));			
			videoProgressBar.setProgress(progress);
			int percent = videoView.getBufferPercentage();
			videoProgressBar.setSecondaryProgress(percent);
			
//			long bandwidth = 0;
//			if (firstEntry) {
//				firstEntry = false;
//				startTimeToCalculateBitrate = System.currentTimeMillis();
//				percentStart = percent;
//				Logger.i(TAG, "Percent = " + percent + " Start Time = " + startTimeToCalculateBitrate);
//			} else if (percent == 100 && lastEntry){
//				lastEntry = false;
//				endTimeToCalculateBitrate = System.currentTimeMillis();
//				Logger.i(TAG, "Percent = " + percent + " End Time = " + endTimeToCalculateBitrate);	
//				long dataPercent = (percent - percentStart);
//				if (startTimeToCalculateBitrate != 0 && endTimeToCalculateBitrate != 0) {
//					bandwidth = ((mFileSize*dataPercent/100)/1024) / ((endTimeToCalculateBitrate-startTimeToCalculateBitrate)/1000);
//					Logger.i(TAG, "BANDWIDTH = " + bandwidth);
//					//store for next time
//					mApplicationConfigurations.setBandwidth(bandwidth);
//				}
//			}
//			// Running this thread after 100 milliseconds
			mHandler.postDelayed(this, 100);			
		}
	};
	
	
	private void populateUserControls(Video video) {

		TextView mTitleBarTextVideo = (TextView) findViewById(R.id.main_player_content_info_bar_text_title);
		mTitleBarTextVideo.setText(mMediaItem.getTitle());
		
		TextView mTitleBarTextVideoAlbum = (TextView) findViewById(R.id.main_player_content_info_bar_text_additional);
		mTitleBarTextVideoAlbum.setText(mMediaItem.getAlbumName());		
		
		// Set Comments Button
		commentsButton = (Button) findViewById(R.id.main_player_content_info_bar_button_comment);
		commentsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				openCommentsPage(mMediaItem, mMediaTrackDetails.getNumOfComments());
			}
		});
		if (mMediaTrackDetails != null) {
//			commentsButton.setText(String.valueOf(mMediaTrackDetails.getNumOfComments()));			
		}
		
		// Set Favorite Button
		mediaType = MediaContentType.VIDEO.toString();		
		favButton = (Button) findViewById(R.id.main_player_content_info_bar_button_favorite);		
		favButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				favButton.setClickable(false);
				if (v.isSelected()) {
					mDataManager.removeFromFavorites(String.valueOf(mMediaItem.getId()), mediaType, VideoActivity.this);
				} else {
					mDataManager.addToFavorites(String.valueOf(mMediaItem.getId()), mediaType, VideoActivity.this);	
				}
				
			}
		});
		
		
		if (mMediaTrackDetails != null) {
			favButton.setText(String.valueOf(mMediaTrackDetails.getNumOfFav()));
			if (mMediaTrackDetails.IsFavorite()) {
					favButton.setCompoundDrawablesWithIntrinsicBounds(null, blueHeart, null, null);
					favButton.setSelected(true);
			} else {
				favButton.setCompoundDrawablesWithIntrinsicBounds(null, whiteHeart, null, null);
				favButton.setSelected(false);
			}
			
			//Check if download is enabled
			if (mMediaTrackDetails.hasDownload()) {
				downloadButton.setVisibility(View.VISIBLE);
			} else {
				downloadButton.setVisibility(View.GONE);
			}
		}
						
		try {
			
			if (!mIsSendToBackgroundPowerButtonPress) {
				
				videoView.setVideoURI(Uri.parse(video.getVideoUrl()));
			
			} else {
				
				mIsSendToBackgroundPowerButtonPress = false;
				if (videoView != null) {
					
					videoView.seekTo(mCurrentPosition); 					
		        }
			}
			
			//startPlaying();
						
		} catch (Exception e) {
			Logger.e(TAG, "failed loading video" + e.toString());
			hideLoadingDialog();
		}
//		final MediaController mc = new MediaController(this);
//		videoView.setMediaController(new MediaController(this) {
//			 @Override
//			    public void hide()
//			    {
//			       mc.show();
//			    }
//
//		});
//		videoView.setMediaController(mc);
		
		
	}
	
	public void openCommentsPage(MediaItem mediaItem, int numOfComments) {
		
		Bundle detailsDataTrack = new Bundle();
		detailsDataTrack.putSerializable(CommentsActivity.EXTRA_DATA_MEDIA_ITEM, (Serializable) mediaItem);
		detailsDataTrack.putBoolean(CommentsActivity.EXTRA_DATA_DO_SHOW_TITLE, true);
		
//		CommentsFragment mCommentsFragment = new CommentsFragment();
//		mCommentsFragment.setArguments(detailsDataTrack);
//
//		mFragmentManager = getSupportFragmentManager();
//		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
//		fragmentTransaction = mFragmentManager.beginTransaction();
//		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
//				R.anim.slide_left_exit,
//                R.anim.slide_right_enter,
//                R.anim.slide_right_exit);			
//		
//		fragmentTransaction.add(R.id.main_fragmant_container, mCommentsFragment);
//		fragmentTransaction.addToBackStack(null);
//		fragmentTransaction.commit();
		
		Intent commentsIntent = new Intent(getApplicationContext(), CommentsActivity.class);
		commentsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		commentsIntent.putExtras(detailsDataTrack);
		startActivity(commentsIntent);
	}

	public void updateControllersVisibilityThread() {

			cancelThread();
			mCountDownTimer.start();
//			if (!(mCountDownTimer.getStatus() == Status.RUNNING)) {
//				cancelThread();
//				mTimeoutVideoPlayerTask = new TimeoutVideoPlayerTask(PERIOD);
//				mTimeoutVideoPlayerTask.timeoutVideoPlayerTaskExecute();
//		    }
//			if (mTimeoutVideoPlayerTask != null) {        	
//				mTimeoutVideoPlayerTask.touch(); 
//		    }
		
	}
	
	public void cancelThread() {
		if (mCountDownTimer != null) {
			mCountDownTimer.cancel();
		}    	
    }
	
	
	// ======================================================
	// Handle Orientation Methods
	// ======================================================
	
	/**
	 * Orientataion Listener.
	 * */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		

		if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
			
			getSupportActionBar().hide();
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
			updateViewToLandscape();
			if (infoPage != null) {
				if (infoPage.getVisibility() == View.VISIBLE) {
					infoPage.setVisibility(View.GONE);
					changeButtonBackground(infoPageButton, true);
	//				onClick(playButton);
					if (infoWasClicked) {
						infoWasClicked = false;
					} else {
						infoWasClicked = true;
					}
				} else if (relatedVideoPage.getVisibility() == View.VISIBLE) {
					relatedVideoPage.setVisibility(View.GONE);
					changeButtonBackground(relatedVideoPageButton, true);
				}
			}
			//TODO: 
//			if (related is open) {
//				close it;s
//			}
		} else {			
			getSupportActionBar().show();
			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			updateViewToPortrait();	
		}
	}
	
	private void updateViewToLandscape() {
		LinearLayout llTop = (LinearLayout) findViewById(R.id.main_player_content_info_bar);
		llTop.setVisibility(View.GONE);
		
		FrameLayout flNavigation = (FrameLayout) findViewById(R.id.main_navigation_fragmant_container);
		flNavigation.setVisibility(View.GONE);

		LinearLayout llBottomTabs = (LinearLayout) findViewById(R.id.main_player_content_actions_full);
		llBottomTabs.setVisibility(View.GONE);		
        
		LinearLayout llUpgradeBar = (LinearLayout) findViewById(R.id.linearlayout_upgrade_bar);
		llUpgradeBar.setVisibility(View.GONE);
		
		if (upgradeDialog != null) {
			upgradeDialog.dismiss();
		}
		
//		today = new Date();		
//		if (mApplicationConfigurations.isUserHasSubscriptionPlan() && !today.after(userCurrentPlanValidityDate)) {
//			upgradeDialog.dismiss();
//		} else {
//			upgradeDialog.show();
//		}
		
//		showDialog(R.layout.dialog_upgrade_subscription);
				
		View viewSeperator = (View) findViewById(R.id.bottom_tabs_seperator);
		viewSeperator.setVisibility(View.GONE);
				
        VideoView videoView = (VideoView) findViewById(R.id.videoview_video_details);
        RelativeLayout videoLayout = (RelativeLayout)findViewById(R.id.relativeLayout_videoview);
        RelativeLayout.LayoutParams rlParams = new  RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlParams.setMargins(0, 0, 0, 0);
        videoLayout.updateViewLayout(videoView, rlParams);
        
	}
	
	private void updateViewToPortrait() {
		LinearLayout rlTop = (LinearLayout) findViewById(R.id.main_player_content_info_bar);
		rlTop.setVisibility(View.VISIBLE);
		
		FrameLayout flNavigation = (FrameLayout) findViewById(R.id.main_navigation_fragmant_container);
		flNavigation.setVisibility(View.VISIBLE);
		
		LinearLayout llBottomTabs = (LinearLayout) findViewById(R.id.main_player_content_actions_full);
		llBottomTabs.setVisibility(View.VISIBLE);

		LinearLayout llUpgradeBar = (LinearLayout) findViewById(R.id.linearlayout_upgrade_bar);
		today = new Date();		
		if (userCurrentPlanValidityDate == null) {
			//For upgrade dialog in landscape
			//userCurrentPlanValidityDate = Utils.convertStringToDate(mApplicationConfigurations.getUserSubscriptionPlanDate());
			userCurrentPlanValidityDate = Utils.convertTimeStampToDate(mApplicationConfigurations.getUserSubscriptionPlanDate());
		}
		if (mApplicationConfigurations.isUserHasSubscriptionPlan() && userCurrentPlanValidityDate != null && !today.after(userCurrentPlanValidityDate)) {
			llUpgradeBar.setVisibility(View.GONE);
		} else {
			llUpgradeBar.setVisibility(View.VISIBLE);
		}
		if (upgradeDialog != null) {
			upgradeDialog.dismiss();
		}
				
		View viewSeperator = (View) findViewById(R.id.bottom_tabs_seperator);
		viewSeperator.setVisibility(View.VISIBLE);
		
        VideoView videoView = (VideoView) findViewById(R.id.videoview_video_details);
        RelativeLayout videoLayout = (RelativeLayout)findViewById(R.id.relativeLayout_videoview);
        RelativeLayout.LayoutParams rlParams = new  RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlParams.addRule(RelativeLayout.CENTER_IN_PARENT);        
        rlParams.setMargins(0, 0, 0, Utils.convertDPtoPX(this, 105));
        videoLayout.updateViewLayout(videoView, rlParams);
	}

	
	// ======================================================
	// onClick Method
	// ======================================================
	
	@Override
	public void onClick(View v) {
		Logger.d(TAG, "Simple click on: " + v.toString());
		int viewId = v.getId();
		
		if (viewId == R.id.button_video_player_play_pause) {
			//for testing
			playButtonClickCounter++;
			Logger.i(TAG, "Play/Pause button was clicked: " + playButtonClickCounter + " times");
			if (videoView != null && videoView.isPlaying()) {				
				
				videoView.pause();
				((ImageButton) v).setImageResource(R.drawable.icon_main_player_play_white); // Changing button image to play button					
			
			} else if (videoView != null) { // Resume video.
					
				startPlaying();					
				((ImageButton) v).setImageResource(R.drawable.icon_main_player_pause_white); // Changing button image to pause button
				
				// Updating progress bar
//				updateProgressBar();							
			}
			updateControllersVisibilityThread();
//			setDisplayTimer(videoControllersBar, 3000);
			
		} else if (viewId == R.id.button_video_player_fullscreen) {
//			Configuration config = new Configuration();
//			config.orientation = Configuration.ORIENTATION_LANDSCAPE;
//			getResources().getConfiguration().setTo(config);
//			onConfigurationChanged(config);
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			
//			updateViewToLandscape();
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//			WindowManager wm = getWindowManager(); 
//			Display d = wm.getDefaultDisplay(); 
//			if (d.getWidth() > d.getHeight()){ //---landscape mode--- 
//				Log.d(�Orientation�, �Landscape mode�); 
//				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//			} else { //---portrait mode--- 
//				Log.d(�Orientation�, �Portrait mode�); }
//			}
		}	else if (viewId == R.id.video_player_content_actions_bar_button_info) {
				if (videoView != null && videoView.isPlaying() && infoPage.getVisibility() == View.GONE) {									
					videoView.pause();
					playButton.setImageResource(R.drawable.icon_main_player_play_white); // Changing button image to play button						
				} else if (videoView != null && !videoView.isPlaying() && infoPage.getVisibility() == View.VISIBLE) {
					startPlaying();					
					playButton.setImageResource(R.drawable.icon_main_player_pause_white); // Changing button image to pause button
					if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
					}
				}
				if (infoWasClicked) {
					infoWasClicked = false;
				} else {
					infoWasClicked = true;
				}
				
				if (mMediaTrackDetails != null) {
					closePage(relatedVideoPage, relatedVideoPageButton);
					openInfoPage();
				} else {
	//				showLoadingDialog(getResources().getString(R.string.application_dialog_loading_content));
				}
				
				FlurryAgent.logEvent("Video details - info tab");
				
		}	else if (viewId == R.id.video_player_content_actions_bar_button_related) {
				if (videoView != null && videoView.isPlaying() && relatedVideoPage.getVisibility() == View.GONE) {									
					videoView.pause();
					playButton.setImageResource(R.drawable.icon_main_player_play_white); // Changing button image to play button						
				} else if (videoView != null && !videoView.isPlaying() && relatedVideoPage.getVisibility() == View.VISIBLE) {
					startPlaying();					
					playButton.setImageResource(R.drawable.icon_main_player_pause_white); // Changing button image to pause button
				}
				if (mMediaTrackDetails != null) {
					closePage(infoPage, infoPageButton);
					openRelatedVideoPage();
				}
				
				FlurryAgent.logEvent("Video details - related videos");
			
		} else if (viewId == R.id.textview_row_1_right || 
					viewId == R.id.textview_row_2_right || 
					viewId == R.id.textview_row_3_right || 
					viewId == R.id.textview_row_4_right ||
					viewId == R.id.textview_row_5_right ||
					viewId == R.id.textview_row_6_right ||
					viewId == R.id.textview_row_7_right ||
					viewId == R.id.textview_row_8_right) {	
//			openMainSearchFragment(((TextView) v).getText().toString());
			explicitOpenSearch(((TextView) v).getText().toString());
//			Intent intent = new Intent(this, HomeActivity.class);
//			intent.putExtra(HomeActivity.EXTRA_MEDIA_ITEM, (Serializable) mediaItem);
//			startActivity(intent);
		
		} else if (viewId == R.id.button_upgrade) {
			openUpgrade(v);
		
		} else if (viewId == R.id.close_button) {
			upgradeDialog.dismiss();
			
		}else if(viewId == R.id.video_player_content_actions_bar_button_share){
			if (videoView != null && videoView.isPlaying() && infoPage.getVisibility() == View.GONE) {									
				videoView.pause();
			}
			// Prepare data for ShareDialogFragmnet
			Map<String , Object> shareData = new HashMap<String, Object>();
			shareData.put(ShareDialogFragment.TITLE_DATA, mMediaItem.getTitle());
			shareData.put(ShareDialogFragment.SUB_TITLE_DATA, mMediaItem.getAlbumName());
			shareData.put(ShareDialogFragment.THUMB_URL_DATA, mMediaItem.getBigImageUrl());
			shareData.put(ShareDialogFragment.MEDIA_TYPE_DATA, MediaType.VIDEO);
			shareData.put(ShareDialogFragment.CONTENT_ID_DATA, mMediaItem.getId());
			
			// Show ShareFragmentActivity
			ShareDialogFragment shareDialogFragment = ShareDialogFragment.newInstance(shareData);
			
			FragmentManager mFragmentManager = getSupportFragmentManager();
			shareDialogFragment.show(mFragmentManager, ShareDialogFragment.FRAGMENT_TAG);
			
		}
	}

		
	// ======================================================
	// Media players & SeekBar listeners
	// ======================================================
	/* (non-Javadoc)
	 * @see android.media.MediaPlayer.OnErrorListener#onError(android.media.MediaPlayer, int, int)
	 */
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		hideLoadingDialog();
		//Toast.makeText(this, this.getResources().getString(R.string.application_error_no_connectivity), Toast.LENGTH_SHORT).show();
		// For testing
		Logger.e(TAG, "No connection error Toast was shown");
		Logger.e(TAG, "What : " + String.valueOf(what));
		Logger.e(TAG, "Extra : " + String.valueOf(extra));
		
		videoView.pause();
		
		return true;
	}
	
	@Override
	public void onPrepared(MediaPlayer player) {
//		if (mTimeoutVideoPlayerTask == null) {        	
//			mTimeoutVideoPlayerTask = new TimeoutVideoPlayerTask(PERIOD);
//        }
		totalDurationLabel.setText("" + Utils.milliSecondsToTimer(videoView.getDuration()));
		currentDurationLabel.setText("" + Utils.milliSecondsToTimer(videoView.getCurrentPosition()));
		if (isActivityDestroyed()) {
			return;
		}
		hideLoadingDialog();
//		onClick(playButton);
		
		onStopTrackingTouch(videoProgressBar);
		
		LinearLayout upgradeBar = (LinearLayout) findViewById(R.id.linearlayout_upgrade_bar);
		today = new Date();				
		if (mApplicationConfigurations.isUserHasSubscriptionPlan() && userCurrentPlanValidityDate != null && !today.after(userCurrentPlanValidityDate)) {
			upgradeBar.setVisibility(View.GONE);
		} else {
			upgradeBar.setVisibility(View.VISIBLE);
		}		
//		updateControllersVisibilityThread();
//		mDataManager.getMediaDetails(mMediaItem, null, this);	
		
		player.setOnBufferingUpdateListener(this);
		
		startPlaying();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		
		stopVideoPlayEvent(true, videoView.getDuration());
		
		mHandler.removeMessages(0);
		videoProgressBar.setProgress(0);
		videoProgressBar.setSecondaryProgress(0);
		playButton.setImageResource(R.drawable.icon_main_player_play_white); // Changing button image to play button
		
		onStopTrackingTouch(videoProgressBar);
		
		updateControllersVisibilityThread();
		//open related page
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//			getResources().getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
			
			today = new Date();		
			if (mApplicationConfigurations.isUserHasSubscriptionPlan() && userCurrentPlanValidityDate != null && !today.after(userCurrentPlanValidityDate)) {
				upgradeDialog.dismiss();
				getResources().getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				openRelatedVideoPage();
			} else {
				upgradeDialog.show();
			}
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			today = new Date();		
			if (mApplicationConfigurations.isUserHasSubscriptionPlan() && userCurrentPlanValidityDate != null && !today.after(userCurrentPlanValidityDate)) {
				openRelatedVideoPage();
			}
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser) {
			updateControllersVisibilityThread();
		}		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
		updateControllersVisibilityThread();
//		setDisplayTimer(videoControllersBar, 3000);
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
		
		int totalDuration = videoView.getDuration();
		int currentPosition = Utils.progressToTimer(seekBar.getProgress(), totalDuration);
		
		// Forward or backward to certain seconds
		if (!mIsSendToBackgroundHomeButtonPress) {
		
			videoView.seekTo(currentPosition);
		
		} else {
			
			mIsSendToBackgroundHomeButtonPress = false;
			videoView.seekTo(mCurrentPosition);
		}
		// Update timer progress again
		updateProgressBar();
		updateControllersVisibilityThread();
	}		
	
		
	// ======================================================
	// Helper methods
	// ======================================================
	private void openInfoPage() {
//		populateInfoPage();
		hideLoadingDialog();
		if (infoPage.getVisibility() == View.VISIBLE) {
			infoPage.setVisibility(View.GONE);
			changeButtonBackground(infoPageButton, true);			
		} else {
			infoPage.setVisibility(View.VISIBLE);
			changeButtonBackground(infoPageButton, false);				
		}
	}
	
	private void populateInfoPage() {
		View seperator;
		if (mMediaTrackDetails != null) {
			if (!TextUtils.isEmpty(mMediaTrackDetails.getAlbumName())  &&
					!TextUtils.isEmpty(mMediaTrackDetails.getReleaseYear())) {
				String albumAndYear = mMediaTrackDetails.getAlbumName() + " (" + mMediaTrackDetails.getReleaseYear() + ")";
				setTextForTextViewButton (albumAndYear, infoAlbum);
	//			infoAlbum.setText();
			} else {
				hideTableRow(infoAlbum);
				seperator = (View) findViewById(R.id.seperator_1);
				seperator.setVisibility(View.GONE);
			}
			
			if (!TextUtils.isEmpty(mMediaTrackDetails.getLanguage())) {
				String language = mMediaTrackDetails.getLanguage();
				setTextForTextViewButton (language, infoLanguageCategory);
	//			infoLanguageCategory.setText(mMediaTrackDetails.getLanguage());
			} else {
				hideTableRow(infoLanguageCategory);
				seperator = (View) findViewById(R.id.seperator_2);
				seperator.setVisibility(View.GONE);
			}
			
			if (!TextUtils.isEmpty(mMediaTrackDetails.getMood())) {
				String mood = mMediaTrackDetails.getMood();
				setTextForTextViewButton (mood, infoMood);
	//			infoMood.setText(mMediaTrackDetails.getMood());
			} else {
				hideTableRow(infoMood);
				seperator = (View) findViewById(R.id.seperator_3);
				seperator.setVisibility(View.GONE);
			}
			
			if (!TextUtils.isEmpty(mMediaTrackDetails.getGenre())) {
				String genre = mMediaTrackDetails.getGenre();
				setTextForTextViewButton (genre, infoGenre);
	//			infoGenre.setText(mMediaTrackDetails.getGenre());
			} else {
				hideTableRow(infoGenre);
				seperator = (View) findViewById(R.id.seperator_4);
				seperator.setVisibility(View.GONE);
			}
			
			if (!TextUtils.isEmpty(mMediaTrackDetails.getMusicDirector())) {
				String musicDirector = mMediaTrackDetails.getMusicDirector();
				setTextForTextViewButton (musicDirector, infoMusic);
	//			infoMusic.setText(mMediaTrackDetails.getMusicDirector());
			} else {
				hideTableRow(infoMusic);
				seperator = (View) findViewById(R.id.seperator_5);
				seperator.setVisibility(View.GONE);
			}		
			
			if (!TextUtils.isEmpty(mMediaTrackDetails.getSingers())) {
				String singers = mMediaTrackDetails.getSingers();
				setTextForTextViewButton (singers, infoSingers);				
			} else {
				hideTableRow(infoSingers);
				seperator = (View) findViewById(R.id.seperator_6);
				seperator.setVisibility(View.GONE);
			}
			
			if (!TextUtils.isEmpty(mMediaTrackDetails.getCast())) {
				String cast = mMediaTrackDetails.getCast();
				setTextForTextViewButton (cast, infoCast);
	//			infoCast.setText(mMediaTrackDetails.getCast());
			} else {
				hideTableRow(infoCast);
				seperator = (View) findViewById(R.id.seperator_7);
				seperator.setVisibility(View.GONE);
			}
			
			if (!TextUtils.isEmpty(mMediaTrackDetails.getLyricist())) {
				String lyricist = mMediaTrackDetails.getLyricist();
				setTextForTextViewButton (lyricist, infoLyrics);
	//			infoLyrics.setText(mMediaTrackDetails.getLyricist());
			} else {
				hideTableRow(infoLyrics);
				seperator = (View) findViewById(R.id.seperator_8);
				seperator.setVisibility(View.GONE);
			}
		}
	}
	
	private void setTextForTextViewButton (String text, LinearLayout row) {
		boolean isOneWord = true;
		TextView keywordButton = null;
		if (text.contains(",")) {
			String[] parts = text.split(",");
			int i = 0;
			for (final String keyword : parts) {
				boolean lastPosition =  i == parts.length-1 ? true : false;
				if (lastPosition) {
					keywordButton = createTextViewButtonInfo(keyword, isOneWord);
				} else {
					keywordButton = createTextViewButtonInfo(keyword, !isOneWord);
				}
				row.addView(keywordButton);
				i++;
			}
		} else {
			keywordButton = createTextViewButtonInfo(text, isOneWord);
			row.addView(keywordButton);
		}				
	}
	
	private TextView createTextViewButtonInfo(final String keyword ,boolean isOneWord) {
		TextView keywordButton = new TextView(this);
		keywordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (keyword.contains("(")) {					
					int startPosition = keyword.indexOf("(");
					int endPosition = keyword.indexOf(")");
					if (endPosition > startPosition) {
						String album = keyword.substring(0, startPosition);						
						String year = keyword.substring(startPosition + 1 , endPosition);
						if (TextUtils.isDigitsOnly(year)) {
							openMainSearchFragment(album);
						} else {
							openMainSearchFragment(keyword);
						}
					}				
				} else {
					openMainSearchFragment(keyword);
				}
			}
		});
		if (isOneWord) {
			keywordButton.setText(keyword);
		} else {
			keywordButton.setText(keyword + ",");
		}
		keywordButton.setTextAppearance(this, R.style.videoPlayeInfoRowText);
		keywordButton.setTypeface(null,Typeface.BOLD);
		keywordButton.setSingleLine(false);
		return keywordButton;
	}
	
	private void changeButtonBackground(View view, boolean visible) {
		if (visible) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				if (view.getId() == R.id.video_player_content_actions_bar_button_info) {
					view.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_main_player_content_action_button_even_selector));
				} else {
					view.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_main_player_content_action_button_odd_selector));
				}
				
			} else {
				if (view.getId() == R.id.video_player_content_actions_bar_button_info) {
					view.setBackground(getResources().getDrawable(R.drawable.background_main_player_content_action_button_even_selector));
				} else {
					view.setBackground(getResources().getDrawable(R.drawable.background_main_player_content_action_button_odd_selector));
				}
				
			}
		} else {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {			
				view.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_main_player_button_pressed));
			} else {				
				view.setBackground(getResources().getDrawable(R.drawable.background_main_player_button_pressed));
			}
		}
	}
	
	private void openRelatedVideoPage() {
//		populateInfoPage();
//		hideLoadingDialog();
		if (relatedVideoPage.getVisibility() == View.VISIBLE) {
			relatedVideoPage.setVisibility(View.GONE);
			changeButtonBackground(relatedVideoPageButton, true);			
		} else {
			relatedVideoPage.setVisibility(View.VISIBLE);
			mHomeMediaTilesAdapter.notifyDataSetChanged();
			changeButtonBackground(relatedVideoPageButton, false);				
		}
	}
	
	private void hideTableRow(View v) {
		TableRow tableRow = (TableRow) v.getParent();
		tableRow.setVisibility(View.GONE);		
	}
	
	private void closePage(RelativeLayout page, Button button) {
		if (page.getVisibility() == View.VISIBLE) {
			page.setVisibility(View.GONE);
			changeButtonBackground(button, true);
		}
	}
	
	protected void openMainSearchFragment(String videoQuery) {
		explicitOpenSearch(videoQuery);
	}

	public LinearLayout getVideoControllersBar() {
		return videoControllersBar;
	}

	public void setVideoControllersBar(LinearLayout videoControllersBar) {
		this.videoControllersBar = videoControllersBar;
	}
	
	public void openUpgrade(View v) {
		
		// Check if there are google accounts on the device
		AccountManager accountManager = AccountManager.get(this);
		Account[] accounts = accountManager.getAccountsByType("com.google");
		
//		if(accounts == null || accounts.length == 0){
//			showDialog(getString(R.string.subscription_error), getString(R.string.there_is_no_google_account_on_this_device));
//			return;
//		}
		
		Intent intent = new Intent(this, UpgradeActivity.class);
		intent.putExtra(UpgradeActivity.EXTRA_DATA_ORIGIN_MEDIA_CONTENT_TYPE, (Serializable) MediaContentType.VIDEO);
		startActivityForResult(intent, UPGRADE_ACTIVITY_RESULT_CODE);
	}
	
	public void startDownloadProcess(View v) {
		Intent intent = new Intent(this, DownloadConnectingActivity.class);
		intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM, (Serializable) mMediaItem);
		startActivity(intent);
	}
	public void initializeUpgradeDialog() {
		//set up custom dialog
        upgradeDialog = new Dialog(this);
        upgradeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        upgradeDialog.setContentView(R.layout.dialog_upgrade_subscription);
        
        TextView title = (TextView) upgradeDialog.findViewById(R.id.video_upgrade_custom_dialog_title_text);
        title.setText(getResources().getString(R.string.video_player_upgrade_button_text).toUpperCase());
        
        TextView text = (TextView) upgradeDialog.findViewById(R.id.video_upgrade_custom_dialog_text);
        text.setText(getResources().getString(R.string.video_player_upgrade_text));
        
        ImageView closeButton = (ImageView) upgradeDialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(this);
        
        Button upgradeButton = (Button) upgradeDialog.findViewById(R.id.button_upgrade);
        upgradeButton.setOnClickListener(this);
        upgradeDialog.setCancelable(true);
        upgradeDialog.setCanceledOnTouchOutside(true);
	}
	
	public void startPlaying () {
		if (videoView != null) {
			videoView.start();
			startVideoPlayEvent();
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
		}
	}
	
	public int getNetworkBandwidth() {
		mDataManager = DataManager.getInstance(this);
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		String networkType = Utils.getNetworkType(this);
		if (!TextUtils.isEmpty(networkType)) {
			long bandwidth = mApplicationConfigurations.getBandwidth();
 			if (networkType.equalsIgnoreCase(Utils.NETWORK_WIFI)  || networkType.equalsIgnoreCase(Utils.NETWORK_3G)) {  				 				
 				if (bandwidth == 0) {
 					Logger.i(TAG, networkType + " - First Time - 3G No bandwidth. bandwidth should be 192");
 					return 192;
 				} else {
 					Logger.i(TAG, networkType + " - Bandwidth from previous = " + bandwidth);
 					return (int) bandwidth;
 				}
	 		} else if (networkType.equalsIgnoreCase(Utils.NETWORK_2G)) {
 					Logger.i(TAG, networkType + " - 2G - bandwidth should be 80");
 					return 80; 				
	 		}
 		}
 		Logger.i(TAG, "Not WIFI & Not Mobile - bandwidth = 64");
 		return 64; // Not WIFI & Not Mobile - bandwidth = 64 
 	}
	
	private void startBadgesAndCoinsActivity(BadgesAndCoins badgesAndCoins) {
		Intent intent = new Intent(this, BadgesAndCoinsActivity.class);
		intent.putExtra(BadgesAndCoinsActivity.ARGUMENT_OBJECT, (Serializable) badgesAndCoins);
		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(intent, BADGES_ACTIVITY_RESULT_CODE);
	}
	
	// ======================================================
	// OnMediaItemOptionSelectedListener Callbacks
	// ======================================================
	
	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position) {
		Logger.d(TAG, "onMediaItemOption PlayNowSelected");
	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem, int position) {
		Logger.d(TAG, "onMediaItemOption PlayNextSelected");
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem, int position) {
		Logger.d(TAG, "onMediaItemOption AddToQueueSelected");
	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem, int position) {
		Logger.d(TAG, "onMediaItemOption ShowDetailsSelected");
		Intent intent = new Intent(this, VideoActivity.class);
		intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO, (Serializable) mediaItem);
		startActivity(intent);
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position) {
		Logger.d(TAG, "onMediaItemOption RemoveSelected");
	}

	/* (non-Javadoc)
	 * @see com.saranyu.SaranyuVideo.OnProfileInfoListener#onInfo(int)
	 */
	@Override
	public void onInfo(int profileInfo) {
		String contentFormat = ""; 
		if(profileInfo == 0) {
			contentFormat = "high";
		} else if (profileInfo == 1)
			contentFormat = "baseline";
		mApplicationConfigurations.setContentFormat(contentFormat);
		mDataManager.getVideoDetailsAdp(mMediaItem, networkSpeed, networkType, contentFormat, this, googleEmailId);
		
	}

	private class GetVideoContentLengthAsync extends AsyncTask<Void, Void, Void>{
		
		private String mVideoUrl;
		
		public void setVideoUrl(String videoUrl){
			this.mVideoUrl = videoUrl;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		

		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				
				URL url = new URL(mVideoUrl);
				URLConnection urlConnection = url.openConnection();
				urlConnection.connect();
				mFileSize = urlConnection.getContentLength();	
				Logger.i(TAG, "File Size = " + mFileSize);
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}
		

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}


	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		
		long bandwidth = 0;
		if (firstEntry) {
			firstEntry = false;
			startTimeToCalculateBitrate = System.currentTimeMillis();
			percentStart = percent;
			Logger.i(TAG, "Percent = " + percent + " Start Time = " + startTimeToCalculateBitrate);
		} else if (percent == 100 && lastEntry){
			lastEntry = false;
			endTimeToCalculateBitrate = System.currentTimeMillis();
			Logger.i(TAG, "Percent = " + percent + " End Time = " + endTimeToCalculateBitrate);	
			long dataPercent = (percent - percentStart);
			if (startTimeToCalculateBitrate != 0 && endTimeToCalculateBitrate != 0) {
				
				float timeDiff = endTimeToCalculateBitrate-startTimeToCalculateBitrate;
				
				long fileSizeInBits = mFileSize*8;
				
				float per = dataPercent/100f;
				
				bandwidth = (long) (((fileSizeInBits*per)/1024f) / (timeDiff/1000));
				Logger.i(TAG, "BANDWIDTH = " + bandwidth);
				
				if(bandwidth == 0){
					// If bandwidth == 0 then store the maximum band width
					mApplicationConfigurations.setBandwidth(MediaHandleOperation.MAX_BANDWIDTH);					
				}else if (bandwidth > 0){
					// If bandwidth > 0 then store for next time 
					mApplicationConfigurations.setBandwidth(bandwidth);		
				}
			}
		}		
	}
	
	// ======================================================
	// Logging PlayEvents.
	// ======================================================
	// Event logging fields.
	private static final SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ENGLISH);//Changes by Hungama
	private String mEventStartTimestamp = null;
	
	private void startVideoPlayEvent() {
		Log.i(TAG, "Start Video Play Event: " + mMediaItem.getId());
		sSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("utc"));
		mEventStartTimestamp = sSimpleDateFormat.format(new Date());
	}

	private void stopVideoPlayEvent(boolean hasCompletePlay, int currentPoisition) {
		
		// If the current play event already reported then start time stamp should be null.
		if(mEventStartTimestamp !=  null){
			// If the VideoView is null them do not report play event.
			if (videoView != null) {
				// If the VideoView did not played then do not report
				if(currentPoisition > 0){
					Log.i(TAG, "Stop Video Play Event " + mMediaItem.getId());
					float playCurrentPostion = (float) (currentPoisition / 1000.0);
					int playDuration = mDuration/1000;
					int consumerId = mDataManager.getApplicationConfigurations().getConsumerID();
					String deviceId = mDataManager.getApplicationConfigurations().getDeviceID();

					PlayEvent playEvent = new PlayEvent(consumerId, deviceId, 0, 
							hasCompletePlay, playDuration, mEventStartTimestamp, 
							0, 0, mMediaItem.getId(), 
							"video", PlayingSourceType.STREAM, 
							0, (int)playCurrentPostion);

					mDataManager.addEvent(playEvent);
				}
			}
			mEventStartTimestamp = null;
		}
	}
	
	private void showDialog(String title, String text) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		 
		// set title
		alertDialogBuilder.setTitle(title);

		// set dialog message
		alertDialogBuilder
			.setMessage(text)
			.setCancelable(true)				
			.setNegativeButton(R.string.exit_dialog_text_ok ,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, just close
					// the dialog box and do nothing
					dialog.cancel();
				}
			});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
	}
}
