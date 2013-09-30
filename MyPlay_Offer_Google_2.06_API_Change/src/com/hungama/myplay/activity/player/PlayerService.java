package com.hungama.myplay.activity.player;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.TrackInfo;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.Scopes;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.events.PlayEvent;
import com.hungama.myplay.activity.data.events.PlayEvent.PlayingSourceType;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.operations.catchmedia.MediaHandleOperation;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.fragments.FavoritesFragment.OnMediaItemsLoadedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.SleepModeManager;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.AsyncTask;

public class PlayerService extends Service implements OnAudioFocusChangeListener,
MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, 
MediaPlayer.OnErrorListener {

	private static final String TAG = "PlayerService";

	public enum State {

		/**
		 * When the Player is created and no {@link Track} 
		 * is registered to be played. 
		 */
		IDLE,

		/**
		 * When the selected {@link Track} is in the state of
		 * being loaded before playing.
		 */
		INTIALIZED,

		/**
		 * When the selected {@link Track} has been loaded, prepared
		 * and is ready to be played. 
		 */
		PREPARED,

		/**
		 * When the selected {@link Track} is being played.
		 */
		PLAYING,

		/**
		 * When the selected {@link Track} is being paused.
		 */
		PAUSED,

		/**
		 * When the selected {@link Track} is being stopped
		 * and the whole process of loading this / new track is required.
		 */
		STOPPED,

		/**
		 * When the selected {@link Track} has been done playing due
		 * to completion of the track.
		 */
		COMPLETED,

		/**
		 * When the selected {@link PlayingQueue} has been done playing.
		 */
		COMPLETED_QUEUE;
	}

	public enum LoopMode {
		OFF,
		ON,
		REAPLAY_SONG
	}

	public enum Error implements Serializable {
		NO_CONNECTIVITY	(1),
		SERVER_ERROR	(2),
		DATA_ERROR		(3);

		private final int id;

		Error (int id){
			this.id = id;
		}

		public int getId() {
			return this.id;
		}

		public static final Error getErrorById(int id) {
			if (id == NO_CONNECTIVITY.getId()) {
				return NO_CONNECTIVITY;
			} else if (id == SERVER_ERROR.getId()) {
				return SERVER_ERROR;
			} else {
				return DATA_ERROR;
			}
		}

	}

	/**
	 * Interface definition to be invoked when the state of the player has been changed.
	 */
	public interface PlayerStateListener {

		public void onStartLoadingTrack(Track track);

		public void onTrackLoadingBufferUpdated(Track track, int precent);

		public void onStartPlayingTrack(Track track);

		public void onFinishPlayingTrack(Track track);

		public void onFinishPlayingQueue();

		public void onSleepModePauseTrack(Track track);

		public void onErrorHappened(PlayerService.Error error);
	}

	public class PlayerSericeBinder extends Binder {
		public PlayerService getService() {
			return PlayerService.this;
		}
	}

	private Context mContext;
	private String mCMServerUrl;
	// identification of the service in the system.
	private int mServiceStartId;

	private DataManager mDataManager;

	// binder for controlling the service from other components.
	private final IBinder mPlayerSericeBinder = new PlayerSericeBinder();

	// audio handler members:
	private AudioManager mAudioManager;
	private WakeLock mWakeLock;

	private MediaPlayer mMediaPlayer;
	private volatile State mCurrentState;
	private volatile Track mCurrentTrack;

	private Thread mMediaLoaderWorker = null;
	private MediaLoaderHandler mMediaLoaderHandler = null;

	private ServiceHandler mServiceHandler;
	private Set<PlayerStateListener> mOnPlayerStateChangedListeners 
	= new HashSet<PlayerService.PlayerStateListener>();

	// Event logging fields.
	private static final SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ENGLISH);//Changes by Hungama
	private String mEventStartTimestamp = null;

	// playing mode, identifies if playing music or radio. Deafult is Music.
	private volatile PlayMode mPlayMode = PlayMode.MUSIC;

	private volatile LoopMode mLoopMode  = LoopMode.OFF;

	private SleepReciever mSleepReciever;
	private volatile boolean mShouldPauseAfterLoading = false;

	// shuffling - every day :)
	private boolean mIsShuffling = false;

	private PlayingQueue mPlayingQueue = null;
	private PlayingQueue mOriginalPlayingQueue = null;

	public static final int TIME_REPORT_BADGES_MILLIES = 120000;
	private long mReportedTrack = -1;

	private boolean mIsPausedByAudiofocusLoss = false;

	private volatile boolean mIsExplicitMarkedExit = false;

	private final ExecutorService mTracksMediaHandleExecutor = Executors.newSingleThreadExecutor();
	private Future<?> mTracksMediaHandleExecutionRecord = null;

	private String mMediaHandleLink;
	private long mFileSize;
	//Calculate bandwidth
	private boolean firstEntry = true;
	private boolean lastEntry = true;
	private int percentStart;
	private long startTimeToCalculateBitrate;
	private long endTimeToCalculateBitrate;
	/*
	 * The Media Handle of any playing track should be updated after 30 minutes. 
	 */
	private static final long MEDIA_HANDLE_UPDATE_TIME_DELTA_MILLIS = 1000 * 60 * 30;

	private ApplicationConfigurations mApplicationConfigurations; 

	// in playing updater.
	private PlayerProgressCounter mPlayerProgressCounter;

	private PlayerBarFragment mPlayerBarFragment = null;
	
	private PlayerBarUpdateListener mOnPlayerBarUpdateListener;
	
	// ======================================================
	// Service life cycle.
	// ======================================================

	@Override
	public void onCreate() {
		super.onCreate();

		// creates binder to the service to interface between other controlling components.

		mContext = getApplicationContext();
		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();

		mPlayingQueue = mDataManager.getStoredPlayingQueue();

		mCMServerUrl = DataManager.getInstance(getApplicationContext()).getServerConfigurations().getServerUrl();

		// initializing the audio manager to gain audio focus.
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

		// creates a lock on the CPU to avoid the OS stops playing in state of idling. 
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
		mWakeLock.setReferenceCounted(false);

		// initializes the service's handler.
		mServiceHandler = new ServiceHandler();

		// initializes the media player.
		initializeMediaPlayer();

		// registers a receiver for sleep requests.
		mSleepReciever = new SleepReciever(this);
		IntentFilter sleepFilter = new IntentFilter(SleepModeManager.COUNT_DOWN_TIMER_FINISH_INTENT);
		registerReceiver(mSleepReciever, sleepFilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mServiceStartId = startId;

		return START_STICKY;
	}

	@Override
	public void onDestroy() {

		Logger.d(TAG, "Destroying the service.");

		// stops any playing / loading track.
		stop();

		// unregisters the receiver for sleep requests.
		unregisterReceiver(mSleepReciever);
		mSleepReciever = null;

		// notifies the data manager that the user has terminated the application.
		if (mDataManager != null) {
			mDataManager.notifyApplicationExits();
		}

		// destroy the service's handler.
		mServiceHandler.removeCallbacksAndMessages(null);
		mServiceHandler = null;

		// destroy the media player.
		destroyMediaPlayer();

		// release the lock on the CPU.
		mWakeLock.release();
		mWakeLock = null;

		dismissNotification();

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mPlayerSericeBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Logger.d(TAG, "Unbiding the service, destroy it!");

		if (isAllowSelfTermination()) {
			if (mPlayMode == PlayMode.MUSIC) {
				// shut down the media handle prefetching.
				stopPrefetchingMediaHandles(true);
			}
			stopSelf(mServiceStartId);
		}


		return false;
	}


	// ======================================================
	// Playing Listeners and Callbacks.
	// ======================================================

	private static final String MESSAGE_VALUE = "message_value";
	private static final String MESSAGE_ERROR_VALUE = "message_error_value";

	/*
	 * starts loading track's media handle from CM 
	 * servers to get the playing URL / internal path to play. 
	 */
	private static final int MESSAGE_START_LOADING_TRACK			 = 1;
	/*
	 * indication for updating the buffer of the 
	 * loading track before / while it been played.
	 */
	private static final int MESSAGE_LOADING_TRACK_BUFFER_UPDATE	 = 2;
	/*
	 * Indication of that the track that is been initially been loaded
	 * and ready to been played.
	 */
	private static final int MESSAGE_LOADING_TRACK_PREPARED			 = 3;
	/*
	 * Indication that the current loading track process has been
	 * cancelled, generally to play another track.
	 */
	private static final int MESSAGE_LOADING_TRACK_CANCELLED		 = 4;
	/*
	 * The track has been finished to being played,
	 * generally moving to the next track in the queue. 
	 */
	private static final int MESSAGE_FINISH_PLAYING_TRACK			 = 5;
	/*
	 * Done playing all the queue of tracks.
	 */
	private static final int MESSAGE_FINISH_PLAYING_QUEUE			 = 6;
	/*
	 * Another application temporarlly requests the focus on the audio.
	 */
	private static final int MESSAGE_AUDIOFOCUS_LOSS_TRANSIENT		 = 7;
	/*
	 * The audio focus gained back to the player.
	 */
	private static final int MESSAGE_AUDIOFOCUS_GAIN				 = 8;
	/*
	 * No more audio focus to the player.
	 */
	private static final int MESSAGE_AUDIOFOCUS_LOSS				 = 9;
	/*
	 * An error has occurred.
	 */
	private static final int MESSAGE_ERROR				  			 = 10;

	/**
	 * Handles all the service's components messages and performs the logic business.
	 */
	private class ServiceHandler extends Handler {

		@Override
		public void handleMessage(Message message) {
			int what = message.what;
			switch (what) {
			case MESSAGE_START_LOADING_TRACK:

				mCurrentState = mCurrentState.INTIALIZED;

				Track loadingTrack = (Track) message.getData().getSerializable(MESSAGE_VALUE);
				for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
					listener.onStartLoadingTrack(loadingTrack);
				}
				break;

			case MESSAGE_LOADING_TRACK_BUFFER_UPDATE:
				for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
					listener.onTrackLoadingBufferUpdated(null, message.arg1);
				}
				break;

			case MESSAGE_LOADING_TRACK_PREPARED:

				/*
				 * Checks if we are pending to exit the application.
				 */
				if (mIsExplicitMarkedExit) {
					/*
					 * get out of here, the service will 
					 * handle the Media Player's state.
					 */
					return;
				}

				// starts playing the track.
				mCurrentState = State.PLAYING;
				mAudioManager.requestAudioFocus(PlayerService.this,
						// Use the music stream.
						AudioManager.STREAM_MUSIC,
						// Request permanent focus.
						AudioManager.AUDIOFOCUS_GAIN);
				mMediaPlayer.start();
				// stores the timestamp
				startLoggingEvent();

				if (mPlayMode == PlayMode.MUSIC) {
					startPrefetchingMediaHandles();
				}

				Track preparedTrack = (Track) message.getData().getSerializable(MESSAGE_VALUE);
				for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
					listener.onStartPlayingTrack(preparedTrack);
				}

				/*
				 * The loading and preparing was while we received the sleep
				 * message, pauses the playing right away.
				 * 
				 * Also letting the client that we paused from the service.
				 */
				if (mShouldPauseAfterLoading) {
					// resets the flag.
					mShouldPauseAfterLoading = false;
					// pauses the the playing.
					pause();

					for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
						listener.onSleepModePauseTrack(preparedTrack);
					}
				}

				break;

			case MESSAGE_LOADING_TRACK_CANCELLED:
				// TODO: currently does nothing, check this.
				break;

			case MESSAGE_FINISH_PLAYING_TRACK:
				mCurrentState = State.COMPLETED;

				Track finishedTrack = (Track) message.getData().getSerializable(MESSAGE_VALUE);
				for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
					listener.onFinishPlayingTrack(finishedTrack);
				}

				// play next track.

				if (mPlayMode == PlayMode.MUSIC && mLoopMode == LoopMode.REAPLAY_SONG) {
					stop();
					play();

				} else {
					stop();
					next();
				}

				break;

			case MESSAGE_FINISH_PLAYING_QUEUE:
				mCurrentState = State.COMPLETED_QUEUE;

				for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
					listener.onFinishPlayingQueue();
				}

				if (mPlayMode == PlayMode.MUSIC && mLoopMode == LoopMode.ON) {
					// recreates the queue and starts to play from the begging.
					List<Track> playedQueue = mPlayingQueue.getCopy();
					mPlayingQueue = new PlayingQueue(playedQueue, 0);

					play();
				}

				break;

			case MESSAGE_AUDIOFOCUS_LOSS_TRANSIENT:
				// is it playing or loading to play?
				if (mCurrentState == State.PLAYING || 
				mCurrentState == State.INTIALIZED || 
				mCurrentState == State.PREPARED) {
					// Pause playback
					Logger.d(TAG, "AUDIOFOCUS LOSS TRANSIENT - pausing");
					pause();
					mIsPausedByAudiofocusLoss = true;
				}
				break;

			case MESSAGE_AUDIOFOCUS_GAIN:
				// Resume playback
				if (mIsPausedByAudiofocusLoss) {
					mIsPausedByAudiofocusLoss = false;
					Logger.d(TAG, "AUDIOFOCUS GAIN - resuming play.");
					play();
				}
				break;

			case MESSAGE_AUDIOFOCUS_LOSS:
				// is it playing or loading to play?
				if (mCurrentState == State.PLAYING || 
				mCurrentState == State.INTIALIZED || 
				mCurrentState == State.PREPARED) {
					// Pause playback
					Logger.d(TAG, "AUDIOFOCUS LOSS - stop playing.");
					pause();
					mIsPausedByAudiofocusLoss = true;
				}
				break;

			case MESSAGE_ERROR:
				int errorId = message.getData().getInt(MESSAGE_ERROR_VALUE);
				PlayerService.Error error = PlayerService.Error.getErrorById(errorId);
				Logger.e(TAG, "Player Error: " + error.toString());

				// resets the media player.
				mMediaPlayer.reset();
				mCurrentState = State.PAUSED;

				for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
					listener.onErrorHappened(error);
				}

				break;
			}
		}
	}

	/**
	 * Listens for changing in the playing volume focus.
	 */
	@Override
	public void onAudioFocusChange(int focusChange) {
		if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
			Message message = Message.obtain(mServiceHandler, MESSAGE_AUDIOFOCUS_LOSS_TRANSIENT);
			message.sendToTarget();

		} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
			Message message = Message.obtain(mServiceHandler, MESSAGE_AUDIOFOCUS_GAIN);
			message.sendToTarget();

		} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
			Message message = Message.obtain(mServiceHandler, MESSAGE_AUDIOFOCUS_LOSS);
			message.sendToTarget();
		}
	}

	/**
	 * Listens for completion of the current playing track.
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {

		if (mCurrentTrack != null) {
		
			Track trackCopy = mCurrentTrack.newCopy();
		
			if (trackCopy != null) {
			
				Message message = Message.obtain(mServiceHandler, MESSAGE_FINISH_PLAYING_TRACK);
				Bundle data = new Bundle();
				data.putSerializable(MESSAGE_VALUE, (Serializable) trackCopy);
				message.setData(data);
				message.sendToTarget();
			}
		}
	}

	/**
	 * Listens for buffering updates in the current playing track that is being prepared.
	 */
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

		Message message = Message.obtain(mServiceHandler, MESSAGE_LOADING_TRACK_BUFFER_UPDATE);
		message.arg1 = percent;
		message.sendToTarget();
	}

	/**
	 * Listens for errors when trying to prepare the current track.
	 */
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		switch (what) {

		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			Message message = Message.obtain(mServiceHandler, MESSAGE_ERROR);
			Bundle data = new Bundle();
			data.putInt(MESSAGE_ERROR_VALUE, Error.SERVER_ERROR.getId());
			message.setData(data);
			message.sendToTarget();
			return true;

		default:
			break;
		}
		return false;
	}


	// ======================================================
	// Service public controlling methods.
	// ======================================================

	public void setPlayingQueue(PlayingQueue playingQueue) {
		mPlayingQueue = playingQueue;
		// resets the swapper queues.
		mOriginalPlayingQueue = null;

		/*
		 * if the new playing queue is empty,
		 * removes any ongoing playing notification from the foreground.
		 */
		if (mPlayingQueue.size() == 0) {
			dismissNotification();
		}
	}

	public void registerPlayerStateListener(PlayerStateListener listner) {
		mOnPlayerStateChangedListeners.add(listner);
	}

	public void unregisterPlayerStateListener(PlayerStateListener listner) {
		mOnPlayerStateChangedListeners.remove(listner);
	}

	/**
	 * Stops the player if playing and adds to the queue the given tracks
	 * after it stops and continue to play. 
	 * @param tracks
	 */
	public void playNow(List<Track> tracks) {

		// checks if it's playing in other mode.
		if (mPlayMode != PlayMode.MUSIC) {
			stop();
			// resets the service to play music.
			mPlayMode = PlayMode.MUSIC;
			mPlayingQueue = new PlayingQueue(null, 0);
		}


		// adds the tracks to be next to the current position.
		if (mPlayingQueue.size() > 0) {
			mPlayingQueue.addNext(tracks);
			// starts playing the next.
			next();
		} else {
			mPlayingQueue.addToQueue(tracks);
			play();
		}
	}

	/**
	 * Stops the player if playing and adds to the queue the given tracks
	 * after it stops and continue to play. 
	 * @param tracks
	 */
	public void playNowFromPosition(List<Track> tracks, int trackPosition) {

		// checks if it's playing in other mode.
		if (mPlayMode != PlayMode.MUSIC) {
			stop();
			// resets the service to play music.
			mPlayMode = PlayMode.MUSIC;
			mPlayingQueue = new PlayingQueue(null, 0);
		}

		mPlayingQueue.goTo(trackPosition);
		play();
		//		// adds the tracks to be next to the current position.
		//		if (mPlayingQueue.size() > 0) {
		//			mPlayingQueue.addNext(tracks);
		//			// starts playing the next.
		//			next();
		//		} else {
		//			mPlayingQueue.addToQueue(tracks);
		//			play();
		//		}
	}

	/**
	 * Adds the given tracks to the play queue after this current playing track.
	 * @param tracks
	 */
	public void playNext(List<Track> tracks) {

		// checks if it's playing in other mode.
		if (mPlayMode != PlayMode.MUSIC) {
			stop();
			// resets the service to play music.
			mPlayMode = PlayMode.MUSIC;
			mPlayingQueue = new PlayingQueue(null, 0);
		}


		if (mPlayingQueue.size() > 0) {
			mPlayingQueue.addNext(tracks);
		} else {
			playNow(tracks);
		}
	}

	/**
	 * Adds these tracks to the end of the queue to been played.
	 * @param tracks
	 */
	public void addToQueue(List<Track> tracks) {

		// checks if it's playing in other mode.
		if (mPlayMode != PlayMode.MUSIC) {
			stop();
			// resets the service to play music.
			mPlayMode = PlayMode.MUSIC;
			mPlayingQueue = new PlayingQueue(null, 0);
		}


		if (mPlayingQueue.size() > 0) {
			mPlayingQueue.addToQueue(tracks);
		} else {
			/*
			 * The user has added tracks to the queue when it was empty.
			 * Adds the tracks to the queue, and force him to presents it.
			 */
			mPlayingQueue.addToQueue(tracks);
			mCurrentTrack = mPlayingQueue.getCurrentTrack();

			// fake invocation to make the client updates its text.
			for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
				listener.onStartLoadingTrack(mCurrentTrack);
			}
		}
	}

	public void playRadio(List<Track> radioTracks, PlayMode playMode) {
		if (!Utils.isListEmpty(radioTracks) && 
				(playMode == PlayMode.LIVE_STATION_RADIO || playMode == PlayMode.TOP_ARTISTS_RADIO)) {

			// stops any playing music.
			if (isPlaying() || isLoading()) {
				stop();
			}

			// sets the service to the correct
			mPlayMode = playMode;

			// clears old playlist and creates a new one.
			mPlayingQueue = new PlayingQueue(radioTracks, 0);

			// Rock & Roll.
			play();
		}
	}

	public boolean isQueueEmpty() {
		return mPlayingQueue.size() == 0;
	}

	/**
	 * Plays the current track if it was paused, if not,
	 * Plays a new one (loads and everything..) .
	 */
	public void play() {
		if (mPlayingQueue.size() > 0) {
			mAudioManager.requestAudioFocus(this,
					// Use the music stream.
					AudioManager.STREAM_MUSIC,
					// Request permanent focus.
					AudioManager.AUDIOFOCUS_GAIN);

			// checks if it's currently playing.
			if (mCurrentState == State.PAUSED) {
				mCurrentState = State.PLAYING;

				mMediaPlayer.start();

				// Logging Events are only for music.
				if (mPlayMode == PlayMode.MUSIC) {
					startLoggingEvent();
				}

				
			} else {
				/*
				 * Starts the loading of the track, when it will be prepared,
				 * it will be played automatically.
				 */
				mCurrentTrack = mPlayingQueue.getCurrentTrack();
				startLoadingTrack();
			}

			updateNotificationForTrack(mCurrentTrack);
		}
	}

	public void playFromPosition(int newPosition) {
		if (mPlayingQueue.size() > 0 && 
				mPlayingQueue.getCurrentPosition() != newPosition) {

			stop();

			mPlayingQueue.goTo(newPosition);

			play();
		}
	}

	/**
	 * Stops playing current track.
	 */
	public void stop() {
		if (mPlayingQueue.size() > 0) {
			// stop loading if any.
			stopLoadingTrack();

			mAudioManager.abandonAudioFocus(this);

			// stop playing if any.
			if (mCurrentState == State.PREPARED || 
					mCurrentState == State.PAUSED || 
					mCurrentState == State.PLAYING || 
					mCurrentState == State.COMPLETED || 
					mCurrentState == State.COMPLETED_QUEUE)  {

				// Logging Events are only for music.
				if (mPlayMode == PlayMode.MUSIC) {
					
					if(mMediaPlayer.getCurrentPosition() > 0){
						// Send an Event only if the track played
						if (mCurrentState == State.COMPLETED || mCurrentState == State.COMPLETED_QUEUE) {
							stopLoggingEvent(true);
						} else {
							stopLoggingEvent(false);
						}
					}
					// stop prefetching media handles.
					if (mCurrentState == State.PAUSED || mCurrentState == State.PLAYING) {
						stopPrefetchingMediaHandles(false);
					}
				}

				mCurrentState = State.STOPPED;

				mMediaPlayer.stop();
				mMediaPlayer.reset();
			}

			// resets the reporting flag for hungama.
			mReportedTrack = -1;
		}
	}

	public void explicitStop() {
		Logger.w(TAG, "################# explicit stopping the service #################");

		// this state is been questioned in the service's handler before playing.
		mIsExplicitMarkedExit = true;

		// stops playing.
		stop();
		// dismisses the notification.
		dismissNotification();

		if (mPlayMode == PlayMode.MUSIC) {
			// shut down the media handle prefetching.
			stopPrefetchingMediaHandles(true);
		}

		// bye bye dear service.
		stopSelf();
	}

	/**
	 * Pauses the current track if it was playing
	 */
	public void pause() {
		// checks if it's currently playing.
		if (mCurrentState == State.PLAYING) {
			mCurrentState = State.PAUSED;
			mMediaPlayer.pause();

			// dismisses the notification.
			dismissNotification();
		} 
	}

	/**
	 * Plays the next track in the queue.
	 */
	public void next() {
		// stops any playing / loading.
		stop();

		if (mPlayingQueue.hasNext()) {
			mCurrentTrack = mPlayingQueue.next();
			play();
		} else {
			mServiceHandler.sendEmptyMessage(PlayerService.MESSAGE_FINISH_PLAYING_QUEUE);
		}

	}

	/**
	 * Restarts the player and starts replaying the queue.
	 */
	public void replay() {
		// stops playing.
		stop();
		// resets the queue and Rock & Roll.
		mPlayingQueue = new PlayingQueue(mPlayingQueue.getCopy(), 0);
		play();

		// resets the report list.
		mReportedTrack = -1;
	}

	/**
	 * Sets the next track to being played without
	 * playing it.
	 * 
	 * @return Track to be played.
	 */
	public Track fakeNext() {
		// stops any playing / loading.
		stop();

		mCurrentTrack = mPlayingQueue.next();

		return mCurrentTrack;
	}

	/**
	 * Plays the previous track in the queue.
	 */
	public void previous() {
		// stops any playing / loading.
		stop();

		mCurrentTrack = mPlayingQueue.previous();

		if (mCurrentTrack != null) {
			play();
		} else {
			mServiceHandler.sendEmptyMessage(PlayerService.MESSAGE_FINISH_PLAYING_QUEUE);
		}
	}

	/**
	 * Sets the previous track to being played without
	 * playing it.
	 * 
	 * @return Track to be played.
	 */
	public Track fakePrevious() {
		// stops any playing / loading.
		stop();

		mCurrentTrack = mPlayingQueue.previous();

		return mCurrentTrack;
	}

	public boolean hasNext() {
		return mPlayingQueue.hasNext();
	}

	public boolean hasPrevious() {
		return mPlayingQueue.hasPrevious();
	}

	public Track getCurrentPlayingTrack() {

		if (mCurrentTrack != null) {
			mCurrentTrack = mPlayingQueue.getCurrentTrack();
		}

		return mCurrentTrack;
	}

	public int getDuration() {
		return mMediaPlayer.getDuration();
	}

	public int getCurrentPlayingPosition() {
		return mMediaPlayer.getCurrentPosition();
	}

	public State getState() {
		return mCurrentState;
	}

	public void seekTo(int timeMilliseconds) {
		mMediaPlayer.seekTo(timeMilliseconds);
	}

	/**
	 * Determines if the player is in the middle of loading
	 * of preparing a {@link Track} before playing.
	 * 
	 * @return true if the player is in the state of {@code State.INTIALIZED} or {@code State.PREPARED}, false otherwise.
	 */
	public boolean isLoading() {
		if (mCurrentState == State.INTIALIZED || mCurrentState == State.PREPARED) {
			return true;
		}

		return false;
	}

	/**
	 * Determines if the player is in the middle of playing a {@link Track}.
	 * or the played track is paused it will return true too.
	 * 
	 * @return true if the player is in the state of {@code State.PLAYING} or {@code State.PAUSED}, false otherwise.
	 */
	public boolean isPlaying() {
		if (mCurrentState == State.PLAYING || 
				mCurrentState == State.PAUSED || 
				mCurrentState == State.COMPLETED_QUEUE) {
			return true;
		}
		return false;
	}

	public boolean isPlayingForExit() {
		if (mCurrentState == State.PLAYING) {
			return true;
		}
		return false;
	}

	public PlayMode getPlayMode() {
		return mPlayMode;
	}

	public List<Track> getPlayingQueue() {
		if (mPlayingQueue != null) {
			return mPlayingQueue.getCopy();
		}
		return null;
	}

	public Track getNextTrack() {
		if (mPlayingQueue != null) {
			return mPlayingQueue.getNextTrack();
		}
		return null;
	}

	public Track getPreviousTrack() {
		if (mPlayingQueue != null) {
			return mPlayingQueue.getPreviousTrack();
		}
		return null;
	}

	public int getCurrentQueuePosition() {
		if (mPlayingQueue != null && (isPlaying() || isLoading())) {
			return mPlayingQueue.getCurrentPosition();
		}
		return PlayingQueue.POSITION_NOT_AVAILABLE;
	}

	public Track removeFrom(int position) {
		if (mPlayingQueue != null) {
			if (position == mPlayingQueue.getCurrentPosition()) {
				stop();
				Track lastTrack = mPlayingQueue.removeFrom(position);

				return lastTrack;

			} else {
				return mPlayingQueue.removeFrom(position);
			}
		}
		return null;
	}

	public void setLoopMode(LoopMode loopMode) {
		mLoopMode = loopMode;
	}

	public LoopMode getLoopMode() {
		return mLoopMode;
	}

	public void startShuffle() {
		mIsShuffling = true;

		// swap the queue.
		mOriginalPlayingQueue = mPlayingQueue;
		mPlayingQueue = PlayingQueue.createShuffledQueue(mOriginalPlayingQueue);
	}

	public void stopShuffle() {
		mIsShuffling = false;

		// revert to the original queue.
		mPlayingQueue = mOriginalPlayingQueue;
	}

	public boolean isShuffling() {
		return mIsShuffling;
	}

	public void reportBadgesAndCoins() {
		if (mCurrentTrack != null && mCurrentTrack.getId() != mReportedTrack) {
			mReportedTrack = mCurrentTrack.getId();

			mDataManager.checkBadgesAlert(Long.toString(mCurrentTrack.getId()), "song", "musicstreaming", null);
		}
	}

	public boolean isAllowSelfTermination() {
		if (mCurrentState == State.INTIALIZED || 
				mCurrentState == State.PREPARED ||
				mCurrentState == State.PLAYING ||
				mCurrentState == State.PAUSED ||
				mCurrentState == State.COMPLETED_QUEUE) {

			return false;
		}

		return true;
	}


	// ======================================================
	// Private helper methods.
	// ======================================================

	private void initializeMediaPlayer() {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnBufferingUpdateListener(this);
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
		mMediaPlayer.reset();

		mCurrentState = State.IDLE;
	}

	private void destroyMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.setOnBufferingUpdateListener(null);
			mMediaPlayer.setOnCompletionListener(null);
			mMediaPlayer.setOnErrorListener(null);
			mMediaPlayer.release();
		}
		mCurrentState = State.IDLE;
	}

	private void startLoadingTrack() {
		mMediaLoaderHandler = new MediaLoaderHandler();

		if (mPlayMode == PlayMode.MUSIC || mPlayMode == PlayMode.TOP_ARTISTS_RADIO) {
			mMediaLoaderWorker = new Thread(new MusicTrackLoaderTask(mMediaLoaderHandler, mCurrentTrack));
		} else {
			// plays the music as "Normal" music - Live Radio.
			mMediaLoaderWorker = new Thread(new RadioTrackLoaderTask(mMediaLoaderHandler, mCurrentTrack));
		}

		mMediaLoaderWorker.start();
	}

	private void stopLoadingTrack() {
		if (mMediaLoaderWorker != null && mMediaLoaderWorker.isAlive()) {
			mMediaLoaderHandler.removeCallbacksAndMessages(null);
			mMediaLoaderWorker.interrupt();

			mMediaLoaderWorker = null;
		}
	}

	private void startPrefetchingMediaHandles() {
		// performs prefetching to other tracks in the queue.
		mTracksMediaHandleExecutionRecord = mTracksMediaHandleExecutor.submit(new MusicTrackHandlesPrefetchingTask());
	}

	private void stopPrefetchingMediaHandles(boolean shutDown) {
		// cancels any
		if (mTracksMediaHandleExecutionRecord != null && !mTracksMediaHandleExecutionRecord.isDone()) {
			mTracksMediaHandleExecutionRecord.cancel(true);
			mTracksMediaHandleExecutionRecord = null;
		}

		if (shutDown) {
			mTracksMediaHandleExecutor.shutdownNow();
		}
	}


	// ======================================================
	// Media handle background operation.
	// ======================================================

	private class MediaLoaderHandler extends Handler {

		public static final int MESSAGE_INITIALIZED 	= 1;
		public static final int MESSAGE_LOADED 			= 2;
		public static final int MESSAGE_PREPARED 		= 3;
		public static final int MESSAGE_ERROR			= 4;
		public static final int MESSAGE_CANCELLED		= 5;

		@Override
		public void handleMessage(Message msg) {
			// resets the data. before obtaining the message.
			Bundle args = msg.getData();
			Message message = Message.obtain(msg);

			if (args != null) {
				message.setData(args);
			}

			switch (msg.what) {
			case MESSAGE_INITIALIZED:
				message.what = PlayerService.MESSAGE_START_LOADING_TRACK;
				if (mServiceHandler != null) {
					mServiceHandler.sendMessage(message);
				}
				break;

			case MESSAGE_LOADED:
				// nothing happened here, the general process of playing a track doesn't care about this.
				break;

			case MESSAGE_PREPARED:
				message.what = PlayerService.MESSAGE_LOADING_TRACK_PREPARED;
				if (mServiceHandler != null) {
					mServiceHandler.sendMessage(message);
				}
				break;

			case MESSAGE_ERROR:
				message.what = PlayerService.MESSAGE_ERROR;
				if (mServiceHandler != null) {
					mServiceHandler.sendMessage(message);
				}
				break;

			case MESSAGE_CANCELLED:
				message.what = PlayerService.MESSAGE_LOADING_TRACK_CANCELLED;
				if (mServiceHandler != null) {
					mServiceHandler.sendMessage(message);
				}
				break;
			}

		}

	}

	private abstract class MediaLoaderTask implements Runnable {

		protected final Handler handler;
		protected final Track track;

		public MediaLoaderTask(Handler handler, Track track) {
			this.handler = handler;
			this.track = track;
		}

		protected void obtainMessage(int what) {
			Message message = Message.obtain(handler, what);
			Bundle data = new Bundle();
			data.putSerializable(MESSAGE_VALUE, (Serializable) track);
			message.setData(data);
			message.sendToTarget();
		}

		protected void obtainErrorMessage(PlayerService.Error error) {
			Message message = Message.obtain(handler, MediaLoaderHandler.MESSAGE_ERROR);
			Bundle data = new Bundle();
			data.putSerializable(MESSAGE_VALUE, (Serializable) track);
			data.putInt(MESSAGE_ERROR_VALUE, error.getId());
			message.setData(data);
			message.sendToTarget();
		}
	}

	/*
	 * Task that loads the tracks playing properties and prepares it to play Music.
	 */
	private class MusicTrackLoaderTask extends MediaLoaderTask {

		public MusicTrackLoaderTask(Handler handler, Track track) {
			super(handler, track);
		}

		@Override
		public void run() {
			// start loading data.
			obtainMessage(MediaLoaderHandler.MESSAGE_INITIALIZED);
			
			if (Thread.currentThread().isInterrupted()) { 
				obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); 
				return; 
			}

			/*
			 * Track's media handle should only been updated if it doesn't hold one,
			 * or it's been obsolete after 30 minutes.
			 */
			Calendar rightNow = Calendar.getInstance();
			boolean timeToRefresh = 
					rightNow.getTimeInMillis() - MEDIA_HANDLE_UPDATE_TIME_DELTA_MILLIS >= track.getCurrentPrefetchTimestamp();
					
			if (TextUtils.isEmpty(track.getMediaHandle()) || timeToRefresh) {
				/*
				 * Retrieves for the given track its media handle string and updated it
				 * with the relevant playing properties.
				 */
				CommunicationManager communicationManager = new CommunicationManager();
				Map<String, Object> mediaHandleProperties = null;

				if (Thread.currentThread().isInterrupted()) { 
					obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); 
					return;
				}

				try {
					mediaHandleProperties = communicationManager.performOperation(
							new CMDecoratorOperation(mCMServerUrl, new MediaHandleOperation(mContext, track.getId())),mContext);
					// if any error occurs, broadcasts an error and terminates.  
				} catch (InvalidRequestException e) { 
					e.printStackTrace(); 
					obtainErrorMessage(PlayerService.Error.DATA_ERROR); 
					return;
				} catch (InvalidResponseDataException e) {
					e.printStackTrace(); 
					obtainErrorMessage(PlayerService.Error.SERVER_ERROR); 
					return;
				} catch (OperationCancelledException e) {
					e.printStackTrace(); 
					obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); 
					return;
				} catch (NoConnectivityException e) { 
					e.printStackTrace(); 
					obtainErrorMessage(PlayerService.Error.NO_CONNECTIVITY); 
					return; 
				}

				if (Thread.currentThread().isInterrupted()) {
					obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
					return;
				}
				// populates the track with its playing properties.
				track.setMediaHandle((String) mediaHandleProperties.get(MediaHandleOperation.RESPONSE_KEY_HANDLE));
				track.setDeliveryId((Long) mediaHandleProperties.get(MediaHandleOperation.RESPONSE_KEY_DELIVERY_ID));
				track.setDoNotCache((Boolean)mediaHandleProperties.get(MediaHandleOperation.RESPONSE_KEY_DO_NOT_CACHE));
				
				if (mediaHandleProperties.get(MediaHandleOperation.RESPONSE_KEY_FILE_SIZE) != null) {
					mFileSize = ((Long) mediaHandleProperties.get(MediaHandleOperation.RESPONSE_KEY_FILE_SIZE));
				}

				/*
				 * sets the track's time when it was updated with the media handle.
				 */
				rightNow = Calendar.getInstance();
				track.setCurrentPrefetchTimestamp(rightNow.getTimeInMillis());
			}

			if (Thread.currentThread().isInterrupted()) { 
				obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); 
				return; 
			}
			
			// media properties are loaded, prepare.
			obtainMessage(MediaLoaderHandler.MESSAGE_LOADED);

			if (!TextUtils.isEmpty(track.getMediaHandle())) {
				
				if (Thread.currentThread().isInterrupted()) { 
					obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
					return;
				}
				
				try {

					mMediaPlayer.reset();
					
					try {
						mMediaHandleLink = track.getMediaHandle();
						mMediaPlayer.setDataSource(track.getMediaHandle());
						mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

					} catch (IllegalStateException e) {
						mMediaPlayer.setDataSource(track.getMediaHandle());
						mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					}

					//					mMediaPlayer.reset();
					//					mMediaPlayer.setDataSource(track.getMediaHandle());
					//					mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					if (Thread.currentThread().isInterrupted()) { obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); return; }
					// prepare the media to play the track.
					try {
						mMediaPlayer.prepare();
					} catch (InterruptedIOException exception) {
						obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); 
						return; 
					}
					
					obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);

					if (Thread.currentThread().isInterrupted()) {
						obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); 
						return; 
					}

				} catch (IllegalArgumentException e) { 
					e.printStackTrace(); obtainErrorMessage(PlayerService.Error.SERVER_ERROR); 
					return;
				} catch (SecurityException e) { 
					e.printStackTrace(); obtainErrorMessage(PlayerService.Error.SERVER_ERROR); 
					return;
				} catch (IllegalStateException e) { 
					e.printStackTrace(); obtainMessage(MediaLoaderHandler.MESSAGE_ERROR); 
					return;
				} catch (IOException e) { 
					e.printStackTrace(); obtainErrorMessage(PlayerService.Error.NO_CONNECTIVITY);
					return;
				}

			} else {
				// no uri for loading data.
				Logger.e(TAG, "No loading uri for media item: " + track.getId());
				obtainErrorMessage(PlayerService.Error.DATA_ERROR);
				return;
			}
		}

	}

	/*
	 * Task that loads the tracks playing ad-hoc tracks from URL to Web Radio.
	 */
	private class RadioTrackLoaderTask extends MediaLoaderTask {

		public RadioTrackLoaderTask(Handler handler, Track track) {
			super(handler, track);
		}

		@Override
		public void run() {
			// start loading data.
			obtainMessage(MediaLoaderHandler.MESSAGE_INITIALIZED);
			if (Thread.currentThread().isInterrupted()) { obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); return; }
			obtainMessage(MediaLoaderHandler.MESSAGE_LOADED);
			if (Thread.currentThread().isInterrupted()) { obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); return; }

			if (!TextUtils.isEmpty(track.getMediaHandle())) {

				//				track.setMediaHandle(track.getMediaHandle() + ";stream.mp3");

				Logger.e(TAG, "Playing Live Radio URL: " + track.getMediaHandle());

				if (Thread.currentThread().isInterrupted()) { obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); return; }

				try {
					mMediaPlayer.setDataSource(track.getMediaHandle());
					mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					if (Thread.currentThread().isInterrupted()) { obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); return; }
					// prepare the media to play the track.
					try {
						mMediaPlayer.prepare();
					} catch (InterruptedIOException exception) { obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); return; }
					obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);

					if (Thread.currentThread().isInterrupted()) { obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED); return; }

				} catch (IllegalArgumentException e) {
					e.printStackTrace(); obtainErrorMessage(PlayerService.Error.SERVER_ERROR); 
					return;
				} catch (SecurityException e) { 
					e.printStackTrace(); obtainErrorMessage(PlayerService.Error.SERVER_ERROR);
					return;
				} catch (IllegalStateException e) {
					e.printStackTrace(); obtainMessage(MediaLoaderHandler.MESSAGE_ERROR);
					return;
				} catch (IOException e) { 
					e.printStackTrace(); obtainErrorMessage(PlayerService.Error.NO_CONNECTIVITY); 
					return; }

			} else {
				// no uri for loading data.
				Logger.e(TAG, "No loading uri for media item: " + track.getId());
				obtainErrorMessage(PlayerService.Error.DATA_ERROR);
				return;
			}
		}

	}

	/*
	 * Prefetches media handles for the prev and the next 
	 * of the current playing track.
	 */
	private class MusicTrackHandlesPrefetchingTask implements Runnable {

		@Override
		public void run() {

			if (Thread.currentThread().isInterrupted()) { return; }

			if (mPlayingQueue.hasPrevious()) {
				Track prevTrack = mPlayingQueue.getPreviousTrack();
				if (prevTrack != null) {
					try {
						prevTrack = prefetchTrackMediaHandle(prevTrack);
					} catch (InterruptedException e) {
						return;
					}
				}
			}

			if (Thread.currentThread().isInterrupted()) { return; }

			if (mPlayingQueue.hasNext()) {
				Track nextTrack = mPlayingQueue.getNextTrack();
				if (nextTrack != null) {
					try {
						nextTrack = prefetchTrackMediaHandle(nextTrack);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}

		private final Track prefetchTrackMediaHandle(Track track) throws InterruptedException {

			Logger.d(TAG, "Prefetching Media handle for track: " + Long.toString(track.getId()) + " has started.");

			/*
			 * Track's media handle should only been updated if it doesn't hold one,
			 * or it's been obsolete after 30 minutes.
			 */
			Calendar rightNow = Calendar.getInstance();
			boolean timeToRefresh = rightNow.getTimeInMillis() - 
					MEDIA_HANDLE_UPDATE_TIME_DELTA_MILLIS >= track.getCurrentPrefetchTimestamp();

					if (Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }

					if (TextUtils.isEmpty(track.getMediaHandle()) || timeToRefresh) {
						try {
							if (Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }

							Logger.d(TAG, "Start prefetching Media handle for track: " + Long.toString(track.getId()));

							CommunicationManager communicationManager = new CommunicationManager();
							Map<String, Object> mediaHandleProperties = communicationManager.performOperation(
									new CMDecoratorOperation(mCMServerUrl, new MediaHandleOperation(mContext, track.getId())),mContext);

							/*
							 * Too late, if we've reached so far without interrupting there is no point to cancel it now.
							 * Populates the track with its playing properties.
							 */
							track.setMediaHandle((String) mediaHandleProperties.get(MediaHandleOperation.RESPONSE_KEY_HANDLE));
							track.setDeliveryId((Long) mediaHandleProperties.get(MediaHandleOperation.RESPONSE_KEY_DELIVERY_ID));
							track.setDoNotCache(Boolean.parseBoolean((String) mediaHandleProperties.get(MediaHandleOperation.RESPONSE_KEY_DO_NOT_CACHE)));
							/*
							 * sets the track's time when it was updated with the media handle.
							 */
							rightNow = Calendar.getInstance();
							track.setCurrentPrefetchTimestamp(rightNow.getTimeInMillis());

						} catch (InvalidRequestException e) {
							e.printStackTrace();
							return null;
						} catch (InvalidResponseDataException e) {
							e.printStackTrace();
							return null;
						} catch (OperationCancelledException e) {
							e.printStackTrace();
							return null;
						} catch (NoConnectivityException e) {
							e.printStackTrace();
							return null;
						}

					}

					return track;
		}
	}


	// ======================================================
	// Logging PlayEvents.
	// ======================================================

	private void startLoggingEvent() {
		sSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("utc"));
		mEventStartTimestamp = sSimpleDateFormat.format(new Date());
	}

	private void stopLoggingEvent(boolean hasCompletePlay) {
		if (mCurrentTrack != null) {
			PlayingSourceType playingSourceType;
			if (mCurrentTrack.isCached()) {
				playingSourceType = PlayingSourceType.CACHED;
			} else {
				playingSourceType = PlayingSourceType.STREAM;
			}

			int playCurrentPostion = (mMediaPlayer.getCurrentPosition() / 1000);
			int playDuration = playCurrentPostion; 
			int consumerId = mDataManager.getApplicationConfigurations().getConsumerID();
			String deviceId = mDataManager.getApplicationConfigurations().getDeviceID();

			Log.i(TAG, "delivery id:" + mCurrentTrack.getDeliveryId() + " id:" + mCurrentTrack.getId());
			
			PlayEvent playEvent = new PlayEvent(consumerId, deviceId, mCurrentTrack.getDeliveryId(), 
					hasCompletePlay, playDuration, mEventStartTimestamp, 
					0, 0, mCurrentTrack.getId(), 
					"track", playingSourceType, 
					0, playCurrentPostion);

			mDataManager.addEvent(playEvent);
		}
		mEventStartTimestamp = null;
	}


	// ======================================================
	// Sleep Receiver.
	// ======================================================

	private static final class SleepReciever extends BroadcastReceiver {

		private final WeakReference<PlayerService> playerServiceReference;

		SleepReciever(PlayerService playerService) {
			this.playerServiceReference = new WeakReference<PlayerService>(playerService);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(SleepModeManager.COUNT_DOWN_TIMER_FINISH_INTENT)) {
				// gets the instance of the player and contolls it.
				PlayerService playerService = playerServiceReference.get();
				if (playerService != null) {
					if (playerService.isPlaying()) {
						playerService.pause();
					} else if (playerService.isLoading()) {
						playerService.mShouldPauseAfterLoading = true;
					}
				}
			}
		}

	}


	// ======================================================
	// Notification helper methods.
	// ======================================================

	private static final int NOTIFICATION_PLAYING_CODE = 123456;

	private void updateNotificationForTrack(Track track) {
		Notification notification = 
				new Notification(R.drawable.icon_launcher, null, System.currentTimeMillis());

		Intent startHomeIntent = new Intent(this, HomeActivity.class);
		startHomeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
				Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent startHomePendingIntent = PendingIntent.getActivity(this, 0, startHomeIntent, 0);

		// sets the artist /+ album text.
		String artistalbum = "";
		if (track != null) {
			if (!TextUtils.isEmpty(track.getArtistName())) {
				artistalbum = track.getArtistName();
				if (!TextUtils.isEmpty(track.getAlbumName())) {
					artistalbum = artistalbum + " - " + track.getAlbumName();
				}
			} else {
				if (!TextUtils.isEmpty(track.getAlbumName())) {
					artistalbum = track.getAlbumName();
				}
			}

			notification.setLatestEventInfo(this, track.getTitle(), artistalbum, startHomePendingIntent);
			notification.flags |= Notification.FLAG_NO_CLEAR;

			startForeground(NOTIFICATION_PLAYING_CODE, notification);
		} else {
			Logger.i(TAG, "Track is null - no notification visible");
		}
	}

	private void dismissNotification() {
		stopForeground(true);
	}

	
	/*
	 * Updater for the progress bar and the current playing time.
	 */
	public static class PlayerProgressCounter extends AsyncTask<Void, Void, Void> {
		// TODO: Leak FIX.
		private WeakReference<PlayerService> playerServiceReference = null;
		
		PlayerProgressCounter(PlayerService playerService) {
			playerServiceReference = new WeakReference<PlayerService>(playerService);
		}

		@Override
		protected Void doInBackground(Void... params) {
			while (!isCancelled()) {
				try {
					this.publishProgress(null);
					if (isCancelled()) {
						break;
					}
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Logger.d(TAG, "Cancelling playing progress update.");
					break;
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			
			final PlayerService playerService = playerServiceReference.get();
			if (playerService != null) {
				
				State state = playerService.getState();
				if (state == State.PLAYING) {
					// gets the values.
					final int progress = (int) (((float) playerService.getCurrentPlayingPosition() / 
							playerService.getDuration()) * 100);
					final String label = Utils.secondsToString(playerService.getCurrentPlayingPosition() / 1000) + " / ";

					// updates the views.
					/*
					 * Seems on some devices it might crash for some reason,
					 * seems the device default's AsyncTask implementations are broken for some OEMs.
					 */
					if (playerService.mOnPlayerBarUpdateListener != null) {
						playerService.mOnPlayerBarUpdateListener.OnPlayerBarUpdate(progress, label);
					}
//					if (playerBarFragment.getActivity() != null) {
//						playerBarFragment.getActivity().runOnUiThread(new Runnable() {					
//						
//							@Override
//							public void run() {
//								// TODO Auto-generated method stub
//								playerBarFragment.getmPlayerSeekBarProgress().setProgress(progress);
//								playerBarFragment.getmPlayerTextCurrent().setText(label);
////								Log.i(TAG, "RUN ON UI " + label + " = " + playerBarFragment.mPlayerTextCurrent.getText().toString());
//							}
//						});
//					} 
//					else {
//						playerBarFragment.mPlayerSeekBarProgress.setProgress(progress);
//						playerBarFragment.mPlayerTextCurrent.setText(label);
//						Log.i(TAG, label + " = " + playerBarFragment.mPlayerTextCurrent.getText().toString());
//					}

					
//					Log.e(TAG, "label:" + label +  "  " + "progress" + progress);
					
					// reports badges and coins for the given playing track.
					int timeMilliseconds = (playerService.getDuration() / 100) * progress;
					if (timeMilliseconds >= PlayerService.TIME_REPORT_BADGES_MILLIES)
						playerService.reportBadgesAndCoins();
				}
			} else {
				cancel(true);
			}
		}
	}
	
	public void startProgressUpdater() {
		mPlayerProgressCounter = new  PlayerProgressCounter(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mPlayerProgressCounter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			mPlayerProgressCounter.execute();
			Log.i(TAG, "EXECUTED - Build VERSION LESS THAN HONEYCOMB");
		}
	}
	
	public void stopProgressUpdater() {
		if (mPlayerProgressCounter != null
				&& (mPlayerProgressCounter.getStatus() == AsyncTask.Status.PENDING || 
					mPlayerProgressCounter.getStatus() == AsyncTask.Status.RUNNING)) {
			
			mPlayerProgressCounter.cancel(true);
			mPlayerProgressCounter = null;
		}
	}
	
	public interface PlayerBarUpdateListener {
		
		public void OnPlayerBarUpdate(int progress, String label);
	}
	
	public void setPlayerBarUpdateListener(PlayerBarUpdateListener listener) {
		mOnPlayerBarUpdateListener = listener;
	}

}
