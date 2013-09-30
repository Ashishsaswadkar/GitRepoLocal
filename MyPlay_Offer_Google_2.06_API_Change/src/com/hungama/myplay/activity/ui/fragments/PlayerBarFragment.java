package com.hungama.myplay.activity.ui.fragments;

import java.io.ObjectInputStream.GetField;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.ActionDefinition;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerService.Error;
import com.hungama.myplay.activity.player.PlayerService.LoopMode;
import com.hungama.myplay.activity.player.PlayerService.PlayerBarUpdateListener;
import com.hungama.myplay.activity.player.PlayerService.PlayerSericeBinder;
import com.hungama.myplay.activity.player.PlayerService.PlayerStateListener;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager.ServiceToken;
import com.hungama.myplay.activity.player.PlayingQueue;
import com.hungama.myplay.activity.ui.CommentsActivity;
import com.hungama.myplay.activity.ui.DownloadConnectingActivity;
import com.hungama.myplay.activity.ui.FavoritesActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.MainActivity.NavigationItem;
import com.hungama.myplay.activity.ui.VideoActivity;
import com.hungama.myplay.activity.ui.fragments.PlayerGymModeFragment.OnGymModeExitClickedListener;
import com.hungama.myplay.activity.ui.fragments.PlayerGymModeFragment.OnPlayButtonStateChangedListener;
import com.hungama.myplay.activity.ui.fragments.PlayerInfoFragment.OnInfoItemSelectedListener;
import com.hungama.myplay.activity.ui.fragments.PlayerQueueFragment.OnPlayerQueueClosedListener;
import com.hungama.myplay.activity.ui.fragments.PlayerSettingsMenuFragment.OnModeSelectedListener;
import com.hungama.myplay.activity.ui.listeners.OnLoadMenuItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.ActiveButton;
import com.hungama.myplay.activity.ui.widgets.SlidingDrawer;
import com.hungama.myplay.activity.ui.widgets.SlidingDrawer.OnDrawerCloseListener;
import com.hungama.myplay.activity.ui.widgets.SlidingDrawer.OnDrawerOpenListener;
import com.hungama.myplay.activity.ui.widgets.ThreeStatesActiveButton;
import com.hungama.myplay.activity.ui.widgets.ThreeStatesActiveButton.OnStateChangedListener;
import com.hungama.myplay.activity.ui.widgets.TwoStatesActiveButton;
import com.hungama.myplay.activity.ui.widgets.TwoStatesButton;
import com.hungama.myplay.activity.util.ActionCounter;
import com.hungama.myplay.activity.util.ActionCounter.OnActionCounterPerform;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.AsyncTask;
import com.hungama.myplay.activity.util.images.ImageCache.ImageCacheParams;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

/**
 * Creates and manages the main application's player bar in the bottom of the
 * screen.
 */
public class PlayerBarFragment extends MainFragment implements OnClickListener,
								ServiceConnection, PlayerStateListener, OnDrawerOpenListener,
								OnDrawerCloseListener, OnActionCounterPerform,
								CommunicationOperationListener, OnLoadMenuItemOptionSelectedListener,
								OnPlayerQueueClosedListener, OnModeSelectedListener,
								OnGymModeExitClickedListener, PlayerBarUpdateListener
								{

	private static final String TAG = "PlayerFragment";
	
	private static final int ACTION_INTERVAL_MS = 			200;
	
	private static final int ACTION_MESSAGE_NEXT = 		 100001;
	private static final int ACTION_MESSAGE_PREVIOUS = 	 100002;
	
	private static final int DRAWER_CONTENT_ACTION_BUTTON_ID_TRIVIA = 	1004;
	private static final int DRAWER_CONTENT_ACTION_BUTTON_ID_INFO = 	1000;
	private static final int DRAWER_CONTENT_ACTION_BUTTON_ID_SIMILAR = 	1001;
	private static final int DRAWER_CONTENT_ACTION_BUTTON_ID_VIDEO = 	1002;
	private static final int DRAWER_CONTENT_ACTION_BUTTON_ID_LYRICS = 	1003;
	
	private static final String DRAWER_CONTENT_ACTION_BUTTON_FRAGMENT_TAG = "drawer_content_action_button_fragment_tag";
	
	private static final String MEDIA_TYPE_SONG = "song";
	public static final int FAVORITE_SUCCESS = 1;  
	
	private int imageWidth;
	private int imageHeight;
	
	// ======================================================
	// Fragment's public methods.
	// ======================================================
	
	public void playNow(List<Track> tracks) {
		if (!Utils.isListEmpty(tracks)) {
			
			List<Track> queue = getCurrentPlayingList();
			
			/*
			 * identifies if the given list of tracks was for playing
			 * a single track or not. single track will not be played
			 * if it already in the queue.
			 */
			if (tracks.size() > 1) {
				
				ArrayList<Track> tracksNotInQueue = new ArrayList<Track>();
				for (Track track : tracks) {
					if (!queue.contains(track)) {
						tracksNotInQueue.add(track);
					}
				}
				if (tracksNotInQueue.size() > 0) {
					helperPlayNow(tracksNotInQueue);
					
					Toast.makeText(getActivity(), mMessageSongToQueue, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), mMessageSongInQueue, Toast.LENGTH_SHORT).show();
					int trackPosition = queue.lastIndexOf(tracks.get(0));
					helperPlayNowFromPosition(tracksNotInQueue, trackPosition);
				}				
				
			} else {
				if (queue.contains(tracks.get(0))) {
					// the single track is already in the queue.
					int trackPosition = queue.lastIndexOf(tracks.get(0));
//					Toast.makeText(getActivity(), mMessageSongInQueue, Toast.LENGTH_SHORT).show();
					helperPlayNowFromPosition(tracks, trackPosition);
//					startProgressUpdater();
				} else {
					helperPlayNow(tracks);
					
					Toast.makeText(getActivity(), mMessageSongToQueue, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
	
	public void addToQueue(List<Track> tracks) {
		if (!Utils.isListEmpty(tracks)) {
			
			List<Track> queue = getCurrentPlayingList();
			
			if (Utils.isListEmpty(queue) || getPlayMode() != PlayMode.MUSIC) {
				helperPlayNow(tracks);
				
				if (tracks.size() > 1) {
					Toast.makeText(getActivity(), mMessageSongsToQueue, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), mMessageSongToQueue, Toast.LENGTH_SHORT).show();
				}
				
			} else {
				/*
				 * checks if the tracks in the list(to be added) are already in the queue.
				 * tracks which are not in the queue will be added.
				 * if all tracks are already in the queue none will be added.
				 */
				ArrayList<Track> tracksNotInQueue = new ArrayList<Track>();
				for (Track track : tracks) {
					if (!queue.contains(track)) {
						tracksNotInQueue.add(track);
					}
				}
				if (tracksNotInQueue.size() > 0) {
					helperAddToQueue(tracksNotInQueue);
					
					Toast.makeText(getActivity(), mMessageSongToQueue, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), mMessageSongInQueue, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public void playNext(List<Track> tracks) {
		if (!Utils.isListEmpty(tracks)) {
			
			// resets the views for music if the player was playing in different mode.
			if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || 
				mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
				
				initializeBarForMusic();
			}
			
			mPlayerService.playNext(tracks);
			// adding new tracks should update the next / prev buttons.
			updateNextPrevButtonsIfPlaying();
		}
	}

	public void playRadio(List<Track> radioTracks, PlayMode playMode) {
		if (!Utils.isListEmpty(radioTracks)) {
			
			stopProgressUpdater();
			clearPlayer();
			
			if (mPlayerService != null) {
				// resets the views for Radio if the player was playing in different mode.
				if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
					initializeBarForRadio();
				}
				
				mPlayerService.playRadio(radioTracks, playMode);
			}
		}
	}
	
	/**
	 * Indicates whatever the player bar is opened with full mode.
	 */
	public boolean isContentOpened() {
		return mDrawer.isOpened();
	}
	
	/**
	 * Indicates whatever the player bar is opened with full mode by taking in advance
	 * for handling closing content by the "Back" button. 
	 */
	public boolean isContentOpenedForBackStack() {
		return mDrawer.isOpened() && isQueueOpened();
	}

	/**
	 * Closes the content of the player bar from full mode to mini mode.
	 */
	public void closeContent() {	
		
		// closes the Gym Mode Player if opened.
		closeGymMode();
		
		// closes the settings if are opened.
		closeSettings();
		
		// closes the queue if is opened.
		closeQueue();
		
		// closes the drawer.
		mDrawer.animateClose();
		
		
	}
	
	public PlayMode getPlayMode() {
		if (mPlayerService != null)
			return mPlayerService.getPlayMode();
		
		return PlayMode.MUSIC;
	} 
	
	public boolean isPlaying() {
		if (mPlayerService != null) {
			return mPlayerService.isPlaying();
		}
		return false;
	}
	
	public State getPlayerState() {
		return mPlayerService.getState();
	}
	
	public boolean isPlayingForExit() {
		if (mPlayerService != null) {
			return mPlayerService.isPlayingForExit();
		}
		return false;
	}
	
	public boolean isLoading() {
		if (mPlayerService != null) {
			mPlayerService.isLoading();
		}
		return false;
	}
	
	public List<Track> getCurrentPlayingList() {
		if (mPlayerService != null) {
			return mPlayerService.getPlayingQueue();
		}
		return null;
	}
	
	public Track getNextTrack() {
		if (mPlayerService != null) {
			return mPlayerService.getNextTrack();
		}
		return null;
	}
	
	public int getCurrentPlayingInQueuePosition() {
		if (mPlayerService != null) {
			return mPlayerService.getCurrentQueuePosition();
		}
		return PlayingQueue.POSITION_NOT_AVAILABLE;
	}
	
	public void play() {
		if (mPlayerService != null) {
			mPlayerService.play();
		}
	}
	
	public void playFromPosition(int newPosition) {
		if (mPlayerService != null) {
			mPlayerService.playFromPosition(newPosition);
		}
	}

	public void pause() {
		if (mPlayerService != null) {
			mPlayerService.pause();
		}
	}
	
	/**
	 * Stops playing the music and closes the service, and removes the notification.
	 * Call this only when explicitly exiting the application,
	 */
	public void explicitStop() {
		if (mPlayerService != null) {
			Logger.w(TAG, "################# explicit stopping the service, Ahhhhhhhhhhhhhhhhhhh #################");
			mPlayerService.explicitStop();
		}
	}
	
	public Track removeFrom(int position) {
		if (mPlayerService != null) {
			mPlayerService.removeFrom(position);
		}
		return null;
	}
	
	public void clearQueue() {
		if (mPlayerService != null) {
			mPlayerService.stop();
			mPlayerService.setPlayingQueue(new PlayingQueue(null, 0));
		}
	}
	
	
	// ======================================================
	// Public helper methods.
	// ======================================================
	
	private void helperPlayNow(List<Track> tracks) {
		
		// resets the views for music if the player was playing in different mode.
		if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || 
			mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
			
			initializeBarForMusic();
		}
		
		stopProgressUpdater();
		clearPlayer();

		mPlayerService.playNow(tracks);
		// adding new tracks should update the next / prev buttons.
		updateNextPrevButtonsIfPlaying();
	}
	
	private void helperPlayNowFromPosition(List<Track> tracks, int trackPosition) {
		
		// resets the views for music if the player was playing in different mode.
		if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || 
			mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
			
			initializeBarForMusic();
		}
		
		stopProgressUpdater();
		clearPlayer();

		mPlayerService.playNowFromPosition(tracks, trackPosition);
		// adding new tracks should update the next / prev buttons.
		updateNextPrevButtonsIfPlaying();
	}
	
	private void helperAddToQueue(List<Track> tracks) {
		// resets the views for music if the player was playing in different mode.
		if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || 
			mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
			
			initializeBarForMusic();
		}
		
		mPlayerService.addToQueue(tracks);
		// adding new tracks should update the next / prev buttons.
		updateNextPrevButtonsIfPlaying();
	}
	
	protected void openMainSearchFragment(String videoQuery) {
//		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//		MainSearchFragment fragment = new  MainSearchFragment();		
//		Bundle data = new Bundle();
//		data.putString(MainSearchFragment.ARGUMENT_SEARCH_VIDEO, videoQuery);		
//		fragment.setArguments(data);
//		fragmentTransaction.add(R.id.main_navigation_fragmant_container, fragment, MainSearchFragment.FRAGMENT_TAG_VIDEO_SEARCH);
//		fragmentTransaction.addToBackStack(MainSearchFragment.FRAGMENT_TAG_VIDEO_SEARCH);		
//		fragmentTransaction.commit();
		
		((MainActivity) getActivity()).explicitOpenSearch(videoQuery);
	}
	
	protected void openCommentsPage(MediaItem mediaItem) {
		Intent showComments = new Intent(getActivity().getApplicationContext(), CommentsActivity.class);
		showComments.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		showComments.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		
		// sets the args.
		Bundle args = new Bundle();
		args.putSerializable(CommentsActivity.EXTRA_DATA_MEDIA_ITEM, (Serializable) mediaItem);
		args.putBoolean(CommentsActivity.EXTRA_DATA_DO_SHOW_TITLE, true);
		
		showComments.putExtras(args);
		
		getActivity().startActivity(showComments);
	}
	
	
	// ======================================================
	// Fragment's public events.
	// ======================================================
	
	public interface NextTrackUpdateListener {
		
		public void onNextTrackUpdateListener(Track track);
	}
	
	public void registerToNextTrackUpdateListener(NextTrackUpdateListener listener) {
		mNextTrackUpdateListeners.add(listener);
	}
	
	public void unregisterToNextTrackUpdateListener(NextTrackUpdateListener listener) {
		mNextTrackUpdateListeners.remove(listener);
	}
	
	public interface PlayingEventListener {
		
		public void onTrackLoad();
		
		public void onTrackPlay();
		
		public void onTrackFinish();
	}
	
	public void setPlayingEventListener(PlayingEventListener listener) {
		mPlayingEventListener = listener;
	}
	
	
	// ======================================================
	// Fragment's lifecycle.
	// ======================================================
	
	private Context mContext;
	private Resources mResources;

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private FragmentManager mFragmentManager;

	// player connection;
	private ServiceToken mServiceToken = null;
	private PlayerService mPlayerService = null;
	
	private String mMessageSongsToQueue;
	private String mMessageSongToQueue;
	private String mMessageSongInQueue;

	// in playing updater.
	private PlayerProgressCounter mPlayerProgressCounter;

	/*
	 * Action counter for rapidly actions performs on the bar, like Next and
	 * Previous buttons handling.
	 */
	private ActionCounter mActionCounter;

	// player bar user controls.
	private RelativeLayout mPlayerSeekBar; 
	private SeekBar mPlayerSeekBarProgress;
	private TextView mPlayerTextCurrent;
	private TextView mPlayerTextDuration;

	// player bar text.
	private TextView mPlayerTextTitle;
	private TextView mPlayerTextAdditional;
	private ProgressBar mPlayerLoadingIndicator;

	// player bar buttons
	private ActiveButton mPlayerButtonPlay;
	private ActiveButton mPlayerButtonPrevious;
	private ActiveButton mPlayerButtonNext;
	private ActiveButton mPlayerButtonFavorites;
	private ActiveButton mPlayerButtonQueue;
	private TwoStatesActiveButton mPlayerButtonShuffle;
	private ThreeStatesActiveButton mPlayerButtonLoop;
	private ActiveButton mPlayerButtonSettings;
	private Button mPlayerButtonLoad;
	private RelativeLayout mPlayerBarText;

	// Player content containers.
	private SlidingDrawer mDrawer;
	private LinearLayout mDrawerHeaderContent;
	private RelativeLayout mDrawerContent;

	// Player info bar.
	private RelativeLayout mDrawerInfoBar;
	private RelativeLayout mDrawerInfoBarNoContent;
	private RelativeLayout mDrawerInfoBarRadio;
	private View mDrawerInfoBarEmpty;
	
	private ImageView mDrawerMediaArt;
	private LinearLayout mDrawerActionsBar;
	// Player content controllers when playing.
	private TextView mDrawerTextTitle;
	private TextView mDrawerTextAdditional;
	private ProgressBar mDrawerLoadingIndicator;

	private ImageButton mDrawerButtonViewQueue;
	private Button mDrawerButtonComment;
	private Button mDrawerButtonAddFavorites;
	private Button mDrawerButtonLoad;

	
	// Player content action buttons.
	private Button mDrawerActionDownload;
	private Button mDrawerActionShare;
	private Button mDrawerActionPlaylist;
	private TwoStatesButton mDrawerActionTrivia;
	private TwoStatesButton mDrawerActionSimilar;
	private TwoStatesButton mDrawerActionInfo;
	private TwoStatesButton mDrawerActionVideo;
	private TwoStatesButton mDrawerActionLyrics;
	
	// Current playing track's data.

	private MediaTrackDetails mCurrentTrackDetails = null;
	
	// Radio.
	private List<NextTrackUpdateListener> mNextTrackUpdateListeners 
				= new ArrayList<PlayerBarFragment.NextTrackUpdateListener>();
	private TextView mDrawerRadioTitle;
	
	// queue.
	private PlayingEventListener mPlayingEventListener;
	//hints
	private LinearLayout playerQueueHint;
	
	//private ImageFetcher mImageFetcher;
	
	private String blueTitleCelebRadio;
	private String blueTitleLiveRadio;
	
	// updating the favorites states.
	private LocalBroadcastManager mLocalBroadcastManager; 
	private MediaItemFavoriteStateReceiver mMediaItemFavoriteStateReceiver;

	private boolean firstEntry = true;
	private boolean mIsLoadMenuOpened = false;
	
	
	// ======================================================
	// Life cycle.
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		Log.i(TAG, "Parent Activity " + getActivity());
		
		mFragmentManager = getActivity().getSupportFragmentManager();

		mContext = getActivity();
		mResources = getResources();
		mDataManager = DataManager
				.getInstance(mContext.getApplicationContext());
		
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		// sets volume controls.
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
		mMediaItemFavoriteStateReceiver = new MediaItemFavoriteStateReceiver(this);
		
		Resources resources = getResources();
		mMessageSongToQueue = resources.getString(R.string.main_player_bar_message_song_added_to_queue);
		mMessageSongsToQueue = resources.getString(R.string.main_player_bar_message_songs_added_to_queue);
		mMessageSongInQueue = resources.getString(R.string.main_player_bar_message_song_already_in_queue);
		
		blueTitleCelebRadio = resources.getString(R.string.celeb_radio_blue_title);
		blueTitleLiveRadio = resources.getString(R.string.live_radio_blue_title);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_player_bar, container, false);
		initializeUserControls(rootView);

		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//initializeMediaArtLoader();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// acquires a connection to the player service.
		mServiceToken = PlayerServiceBindingManager.bindToService(getActivity(), this);
		
		// listen to changes in the media item's favorition state.
		IntentFilter filter = new IntentFilter(ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
		mLocalBroadcastManager.registerReceiver(mMediaItemFavoriteStateReceiver, filter);
		
		FlurryAgent.onStartSession(getActivity(), getResources().getString(R.string.flurry_app_key));
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// creates the action counter.
		mActionCounter = new ActionCounter(ACTION_INTERVAL_MS);
		mActionCounter.setOnActionCounterPerform(this);
		
		// sets the image loader to kick. 
		//mImageFetcher.setExitTasksEarly(false);
		
		/*
		 * Because there is no Player notification to keep hooking
		 * the service while the application is in the background - 
		 * Binding / Unbinding the service in the onCreate / onDestroy manner,
		 * forces update the Player bar every time. 
		 */
//		adjustCurrentPlayerState();
	}

	@Override
	public void onPause() {
		
		// stops and destroys the action counter.
		mActionCounter.setOnActionCounterPerform(null);
		mActionCounter.cancelAnyAction();
		mActionCounter = null;
		
		stopProgressUpdater();
		
		if (!firstEntry) {
			if (mPlayerService != null) {
				mPlayerService.unregisterPlayerStateListener(this);
			}
		} else {
			firstEntry = false;
		}
				
		
		
		// sets the image loader to sleep early. 
		//mImageFetcher.setExitTasksEarly(true);
			
		super.onPause();
	}
	
	@Override
	public void onStop() {
		
		// disconnects from the player service.
		PlayerServiceBindingManager.unbindFromService(mServiceToken);
		
		// stops listening to changes in the media favorition.
		mLocalBroadcastManager.unregisterReceiver(mMediaItemFavoriteStateReceiver);
		
		super.onStop();
		
		FlurryAgent.onEndSession(getActivity());
	}

	@Override
	public void onDestroyView() {

		// stops any background running process.
		cancelLoadingMediaDetails();
		
//		// closes the Image loader cache.
//		if (mImageFetcher != null) {
//			mImageFetcher.closeCache();
//			// I comment the below line cause it causes crash
//			//mImageFetcher = null;
//		}

		super.onDestroyView();
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		
		if(viewId == R.id.main_player_bar_text_container){
			mDrawer.animateOpen();
		
		} else if (viewId == R.id.main_player_bar_button_play) {
			if (mDataManager.isDeviceOnLine()) {
				if (!mPlayerService.isQueueEmpty()) {
					
					if (mPlayerService.getState() == State.COMPLETED_QUEUE) {
						mPlayerService.replay();
						
					} else {
						// normal play.
						onPlayerPlayClicked(view.isSelected());
					}
					// updates the play button.
					togglePlayerPlayIcon(view.isSelected());
				}
			} else {
				Toast.makeText(mContext, R.string.application_error_no_connectivity, Toast.LENGTH_SHORT).show();
			}

		} else if (viewId == R.id.main_player_bar_button_previous) {
			if (!mPlayerService.isQueueEmpty() && mPlayerService.hasPrevious()) {
				// set previous faked track.
				Track track = mPlayerService.fakePrevious();
				if (track != null) {
					/*
					 * calling this to update the player bar with the next
					 * desired track to being played.
					 */
					populateForFakeTrack(track);
					// register the next action.
					mActionCounter.performAction(ACTION_MESSAGE_PREVIOUS);
				}
			}
		} else if (viewId == R.id.main_player_bar_button_next) {
			if (!mPlayerService.isQueueEmpty() && mPlayerService.hasNext()) {
				// set next faked track.
				Track track = mPlayerService.fakeNext();
				if (track != null) {
					/*
					 * calling this to update the player bar with the next
					 * desired track to being played.
					 */
					populateForFakeTrack(track);
					// register the next action.
					mActionCounter.performAction(ACTION_MESSAGE_NEXT);
				}
			}

		} else if (viewId == R.id.main_player_bar_button_load) {
			// toggles the open / close state of the drawer.
			if (mDrawer.isOpened()) {
				mDrawer.animateClose();
			} else {
				mDrawer.animateOpen();
			}
			
		} else if (viewId == R.id.main_player_bar_button_loop) {
			// gets the new state of the button.
			ThreeStatesActiveButton.State state = ((ThreeStatesActiveButton) view).getState();
			String toastMessage = null;
			
			if (state == ThreeStatesActiveButton.State.ACTIVE) {
				// sets any loop mode - OFF.
				toastMessage = getResources().getString(R.string.player_loop_mode_off);
				mPlayerService.setLoopMode(LoopMode.OFF);
				
			} else if (state == ThreeStatesActiveButton.State.SECOND) {
				// sets any loop mode - REPLAY SONG.
				toastMessage = getResources().getString(R.string.player_loop_mode_replay_song);
				mPlayerService.setLoopMode(LoopMode.REAPLAY_SONG);
				
			} else {
				// sets any loop mode - ON.
				toastMessage = getResources().getString(R.string.player_loop_mode_on);
				mPlayerService.setLoopMode(LoopMode.ON);
			}
			
			Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
			
		} else if (viewId == R.id.main_player_bar_button_shuffle) {
			
			// gets the new state of the button.
			TwoStatesActiveButton.State state = ((TwoStatesActiveButton) view).getState();
			String toastMessage = null;
			
			if (state == TwoStatesActiveButton.State.ACTIVE) {
				// sets any loop mode - OFF.
				toastMessage = getResources().getString(R.string.player_shuffle_mode_off);
				mPlayerService.stopShuffle();
				
			} else if (state == TwoStatesActiveButton.State.SECOND) {
				// sets any loop mode - REPLAY SONG.
				toastMessage = getResources().getString(R.string.player_shuffle_mode_on);
				mPlayerService.startShuffle();
				
			} else {
				// sets any loop mode - ON.
				toastMessage = getResources().getString(R.string.player_loop_mode_on);
				mPlayerService.setLoopMode(LoopMode.ON);
			}
			
			// shows a message to indicate the user.
			Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
			
			// updates the next / prev buttons to the new situation.
			updateNextPrevButtonsIfPlaying();
			
		} else if (viewId == R.id.main_player_bar_button_queue) {
			
			if (!mDrawer.isOpened()) {
				mDrawer.animateOpen();
			}
			
			openQueue();
			
		} else if (viewId == R.id.main_player_content_actions_bar_button_download) {
			downloadCurrentTrack();
			
			FlurryAgent.logEvent(getActivity().getString(R.string.full_player_download));
			
		} else if (viewId == R.id.main_player_content_actions_bar_button_share) {
			if (mPlayerService != null) {
				if (mDataManager.isDeviceOnLine()) {
					Track track = mPlayerService.getCurrentPlayingTrack();
	
					// Prepare data for ShareDialogFragmnet
					Map<String , Object> shareData = new HashMap<String, Object>();
					shareData.put(ShareDialogFragment.TITLE_DATA, track.getTitle());
					shareData.put(ShareDialogFragment.SUB_TITLE_DATA, track.getAlbumName());
					shareData.put(ShareDialogFragment.THUMB_URL_DATA, track.getBigImageUrl());
					shareData.put(ShareDialogFragment.MEDIA_TYPE_DATA, MediaType.TRACK);
					shareData.put(ShareDialogFragment.CONTENT_ID_DATA, track.getId());
					
					// Show ShareFragmentActivity
					ShareDialogFragment shareDialogFragment = ShareDialogFragment.newInstance(shareData);
					shareDialogFragment.show(mFragmentManager, ShareDialogFragment.FRAGMENT_TAG);
					
					FlurryAgent.logEvent(getActivity().getString(R.string.full_player_share));
				} else {
					Toast.makeText(mContext, getResources().getString(R.string.player_error_no_connectivity), Toast.LENGTH_LONG).show();
				}
				
			}
			
		} else if (viewId == R.id.main_player_content_actions_bar_button_playlist) {
			showAddToPlaylistDialog();
			FlurryAgent.logEvent(getActivity().getString(R.string.add_to_playlist_triggered));
			
		} else if (viewId == R.id.main_player_content_info_bar_button_view_queue) {
			openQueue();
			
		} else if (viewId == R.id.main_player_content_info_bar_button_comment) {

			// gets the current playing track and shows the comments for it.
			Track track = mPlayerService.getCurrentPlayingTrack();
			if (track != null) {
				MediaItem mediaItem = new MediaItem(track.getId(), track.getTitle(), track.getAlbumName(), 
						track.getArtistName(), track.getImageUrl(), track.getBigImageUrl(), 
						MediaType.TRACK.name().toLowerCase(), 0);
				openCommentsPage(mediaItem);
			}
			
			closeContent();
			
		} else if (viewId == R.id.main_player_content_info_bar_button_favorite || 
				   viewId == R.id.main_player_bar_button_add_to_favorites) {
			
			view.setClickable(false);
			Track track = mPlayerService.getCurrentPlayingTrack();
			if (view.isSelected()) {
				mDrawerButtonAddFavorites.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.icon_media_details_fav_white), null, null);
				mPlayerButtonFavorites.setImageDrawable(getResources().getDrawable(R.drawable.icon_main_player_favorites_white));
				if (mCurrentTrackDetails != null) {
					mDrawerButtonAddFavorites.setText(String.valueOf(mCurrentTrackDetails.getNumOfFav() - 1));
				}
				mDataManager.removeFromFavorites(String.valueOf(track.getId()), MEDIA_TYPE_SONG, PlayerBarFragment.this);
			} else {
				mDrawerButtonAddFavorites.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.icon_media_details_fav_blue), null, null);
				mPlayerButtonFavorites.setImageDrawable(getResources().getDrawable(R.drawable.icon_main_player_favorites_blue));
				if (mCurrentTrackDetails != null) {
					mDrawerButtonAddFavorites.setText(String.valueOf(mCurrentTrackDetails.getNumOfFav() + 1));
				}
				mDataManager.addToFavorites(String.valueOf(track.getId()), MEDIA_TYPE_SONG, PlayerBarFragment.this);	
			}
			
		} else if (viewId == R.id.main_player_content_info_bar_button_load) {

			if (view.isSelected()) {
				((Button) view).setSelected(false);
			} else {
				((Button) view).setSelected(true);
			}
			toggleLoadMenu();
			
		} else if (viewId == R.id.player_gym_mode_exit_button) {
			closeGymMode();
			
		} else if(viewId == R.id.main_player_bar_button_settings){
			
			Fragment fragment = mFragmentManager.findFragmentByTag(PlayerSettingsMenuFragment.TAG);
			if (fragment != null && fragment.isVisible()) {
				// the settings are visible, hides them.
				closeSettings();
				
			} else {
				// the settings are not visible, shows them.
				openSettings();
			}
		}
		
	}
	
	@Override
	public void onActionCounterPerform(int actionId) {
		mPlayerService.play();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {

		// gets the reference to the service.
		PlayerSericeBinder binder = (PlayerSericeBinder) service;
		mPlayerService = binder.getService();
		mPlayerService.registerPlayerStateListener(this);
		mPlayerService.setPlayerBarUpdateListener(this);
		Logger.d(TAG, "Player bar connected to service.");

		// populate the player bar based on the current state.
//		if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
//
//			// playing, and the drawer is closed.
//			adjustBarWhenClosedAndPlaying();
//
//			// updates the progress.
//			startProgressUpdater();
//
//		} else {
//			/*
//			 * Not playing and not bears. shows default buttons.
//			 */
//			adjustBarWhenClosedAndNotPlaying();
//		}
		
		adjustCurrentPlayerState();
		
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Logger.d(TAG, "Player bar disconnected from service.");
		mPlayerService = null;
	}

	
	// ======================================================
	// Playing Events.
	// ======================================================

	@Override
	public void onStartLoadingTrack(Track track) {
		Logger.i(TAG, "Starts loading track: " + track.getId());
		
		if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
			
			// stops any running updater.
			stopProgressUpdater();
			// clears fields.
			clearPlayer();
			// update the text.
			populateForFakeTrack(track);
			// sets the loading indicator.
			if (mDrawer.isOpened()) {
				
				if(isVisible()){
					mDrawerLoadingIndicator.setVisibility(View.VISIBLE);
					// loads media art.
					//mImageFetcher.setExitTasksEarly(false);
					startLoadingMediaArt(track.getBigImageUrl());
				}
				
			} else {
				mPlayerLoadingIndicator.setVisibility(View.VISIBLE);
				
				// sets the buttons for playing when the bar is closed.
				
				// removes the load button, it's not needed anymore.
				mPlayerButtonLoad.setVisibility(View.GONE);
				
				mPlayerButtonFavorites.setVisibility(View.VISIBLE);
				mPlayerButtonQueue.setVisibility(View.VISIBLE);
			}
			
			startLoadingMediaDetails(track);
			
		} else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO || 
				   mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
			
			// update the text.
			populateForFakeTrack(track);
			mPlayerLoadingIndicator.setVisibility(View.VISIBLE);
		
			if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
				startLoadingMediaDetails(track);
			}
		}
		
		// disables the click on the play button.
		mPlayerButtonPlay.setClickable(false);
		
		if (mPlayingEventListener != null) {
			mPlayingEventListener.onTrackLoad();
		}

	}

	@Override
	public void onTrackLoadingBufferUpdated(Track track, int precent) {		
		// track is always null.
		mPlayerSeekBarProgress.setSecondaryProgress(precent);
		
	}

	@Override
	public void onStartPlayingTrack(Track track) {
		Logger.i(TAG, "Starts playing track: " + track.getId());

		// removes any loading indication.
		if (mDrawer.isOpened()) {
			mDrawerLoadingIndicator.setVisibility(View.GONE);
		} else {
			mPlayerLoadingIndicator.setVisibility(View.GONE);
		}

		// sets the play button icon and enables it.
		mPlayerButtonPlay.activate();
		mPlayerButtonPlay.setClickable(true);
		togglePlayerPlayIcon(true);
		
		mPlayerSeekBarProgress.setProgress(0);
		mPlayerSeekBarProgress.setSecondaryProgress(0);
		mPlayerSeekBarProgress.setEnabled(true);
		
		if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
			// starts running the playing progress.
			startProgressUpdater();

			mPlayerTextDuration.setText(Utils.secondsToString(mPlayerService.getDuration() / 1000));
		}
		
		/*
		 * Updates any listener, like the Radio Details 
		 * Fragment for the if a new next track is available.
		 */
		if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO || 
			mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
			
			mPlayerLoadingIndicator.setVisibility(View.GONE);
			
			// updates any observer with the next track.
			for (NextTrackUpdateListener listener : mNextTrackUpdateListeners) {
				if (listener != null)
					listener.onNextTrackUpdateListener(track);
			}
			
		}
		
		if (mPlayingEventListener != null) {
			mPlayingEventListener.onTrackPlay();
		}
	}

	@Override
	public void onFinishPlayingTrack(Track track) {
		Logger.i(TAG, "Finished playing track: " + track.getId());
		
		if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
			stopProgressUpdater();
		}
		togglePlayerPlayIcon(false);
		
		if (mPlayingEventListener != null) {
			mPlayingEventListener.onTrackFinish();
		}		
	}

	@Override
	public void onFinishPlayingQueue() {
		Logger.i(TAG, "Done with the party, finished playing the queue.");
		
		if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
			stopProgressUpdater();
			showReplayButtonAsPlay();
		}
	}

	@Override
	public void onErrorHappened(Error error) {
		Logger.i(TAG, "An Error occured while playing: " + error.toString());
		String errorMessage = null;
		if (error == Error.NO_CONNECTIVITY) {
			errorMessage = mResources.getString(R.string.player_error_no_connectivity);
		} else if (error == Error.DATA_ERROR || error == Error.SERVER_ERROR) {
			errorMessage = mResources.getString(R.string.player_error_server_issue);
		}

		// a quick fix: remove the toast
		//Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();

		// removes any loading indication.
		if (mDrawer.isOpened()) {
			mDrawerLoadingIndicator.setVisibility(View.GONE);
		} else {
			mPlayerLoadingIndicator.setVisibility(View.GONE);
		}

		mPlayerButtonPlay.setClickable(true);
	}
	
	@Override
	public void onPlayerQueueClosed() {
		
		// enables the button again.
		mDrawerButtonViewQueue.setClickable(true);
		
		// releases the lock of the drawer.
		mDrawer.unlock();
		
		// enables the drawer to be dragable.
		
		if (mPlayerService != null) {
			mPlayerService.registerPlayerStateListener(this);

			// populate the player bar based on the current state.
			if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {

				/*
				 * adjusts the player bar and its content when playing.
				 */
				Logger.d(TAG, "updating from player queue while playing.");
				if (mDrawer.isOpened()) {
					adjustBarWhenOpenedAndPlaying();
					adjustDrawerContentWhenPlaying();
				} else {
					adjustBarWhenClosedAndPlaying();
					clearDrawerContent();
				}

				// updates the progress.
				startProgressUpdater();

			} else {
				/*
				 * Not playing and not bears. shows default buttons.
				 */
				
				List<Track> queue = mPlayerService.getPlayingQueue();
				
				if (Utils.isListEmpty(queue)) {
					clearDrawerContent();
					
					if (mDrawer.isOpened()) {
						adjustBarWhenOpenedAndNotPlaying();
						adjustDrawerContentWhenNotPlaying();
					} else {
						adjustBarWhenClosedAndNotPlaying();
					}
				}
			}

		}
	}

	@Override
	public void onSleepModePauseTrack(Track track) {
		/*
		 * If the player is visible, the music will be paused and
		 * this method will be invoked, updates the play button icon.
		 */
		togglePlayerPlayIcon(false);
	}
	
	
	// ======================================================
	// Initialization.
	// ======================================================

	private void initializeUserControls(View rootView) {
		initializePlayerBar(rootView);
		initializePlayerContent(rootView);
	}
	
	private void initializePlayerBar(View rootView) {
		// progress bar.
		mPlayerSeekBar = (RelativeLayout) rootView.findViewById(R.id.main_player_bar_progress_bar);
		mPlayerSeekBarProgress = (SeekBar) rootView.findViewById(R.id.main_player_bar_progress_bar_seek_bar);
		mPlayerTextCurrent = (TextView) rootView.findViewById(R.id.main_player_bar_progress_bar_scale_text_current);
		mPlayerTextDuration = (TextView) rootView.findViewById(R.id.main_player_bar_progress_bar_scale_text_length);

		// texts.
		mPlayerTextTitle = (TextView) rootView.findViewById(R.id.main_player_bar_text_title);
		mPlayerTextAdditional = (TextView) rootView.findViewById(R.id.main_player_bar_text_additional);
		mPlayerLoadingIndicator = (ProgressBar) rootView.findViewById(R.id.main_player_bar_loading_indicator);

		// control buttons.
		mPlayerButtonPlay = (ActiveButton) rootView.findViewById(R.id.main_player_bar_button_play);
		mPlayerButtonPrevious = (ActiveButton) rootView.findViewById(R.id.main_player_bar_button_previous);
		mPlayerButtonNext = (ActiveButton) rootView.findViewById(R.id.main_player_bar_button_next);
		mPlayerButtonFavorites = (ActiveButton) rootView.findViewById(R.id.main_player_bar_button_add_to_favorites);
		mPlayerButtonQueue = (ActiveButton) rootView.findViewById(R.id.main_player_bar_button_queue);
		mPlayerButtonShuffle = (TwoStatesActiveButton) rootView.findViewById(R.id.main_player_bar_button_shuffle);
		mPlayerButtonLoop = (ThreeStatesActiveButton) rootView.findViewById(R.id.main_player_bar_button_loop);
		mPlayerButtonSettings = (ActiveButton) rootView.findViewById(R.id.main_player_bar_button_settings);
		mPlayerButtonLoad = (Button) rootView.findViewById(R.id.main_player_bar_button_load);
		mPlayerBarText = (RelativeLayout) rootView.findViewById(R.id.main_player_bar_text_container);
		
		// this is 100%, from 0 to 99.
		mPlayerSeekBarProgress.setMax(99);
		mPlayerSeekBarProgress.setOnTouchListener(new SeekBarTouchListener());

		mPlayerButtonPlay.setOnClickListener(this);
		mPlayerButtonPrevious.setOnClickListener(this);
		mPlayerButtonNext.setOnClickListener(this);
		mPlayerButtonFavorites.setOnClickListener(this);
		mPlayerButtonQueue.setOnClickListener(this);
		mPlayerButtonShuffle.setOnClickListener(this);
		mPlayerButtonLoop.setOnClickListener(this);
		mPlayerButtonSettings.setOnClickListener(this);
		mPlayerButtonLoad.setOnClickListener(this);
		mPlayerBarText.setOnClickListener(this);
		
		mPlayerButtonPlay.setSelected(false);
		
		/*
		 * Sets a state listener to the loop button to 
		 * reset the player service loop state when the button is inactive,
		 * not playing at all.
		 */
		mPlayerButtonLoop.setOnStateChangedListener(new OnStateChangedListener() {
			
			@Override public void onThirdState(View view) {}
			@Override public void onSecondState(View view) {}
			@Override public void onActiveState(View view) {}
			
			@Override
			public void onInactiveState(View view) {
				if (mPlayerService != null) {
					mPlayerService.setLoopMode(LoopMode.OFF);
				}
			}
		});
	}
	
	private void initializePlayerContent(View rootView) {

		// gets internal drawer's members for drawer's behavior configuration.

		mDrawerHeaderContent = (LinearLayout) rootView.findViewById(R.id.main_player_drawer_header_content);
		mDrawerContent = (RelativeLayout) rootView.findViewById(R.id.main_player_drawer_content);

		mDrawer = (SlidingDrawer) rootView.findViewById(R.id.main_player_drawer);
		mDrawer.setOnDrawerOpenListener(this);
		mDrawer.setOnDrawerCloseListener(this);

		mDrawerContent.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				return true;
			}
		});
		/*
		 * to open the drawer we handles it by clicking on the text.
		 */
		mDrawerHeaderContent.setClickable(false);

		/*
		 * gets internal drawer's members that play role in the details of the
		 * playing status.
		 */
		mDrawerInfoBar = (RelativeLayout) rootView.findViewById(R.id.main_player_content_info_bar_content);
		mDrawerInfoBarNoContent = (RelativeLayout) rootView.findViewById(R.id.main_player_content_info_bar_no_content);
		mDrawerInfoBarRadio = (RelativeLayout) rootView.findViewById(R.id.main_player_content_info_bar_radio);
		mDrawerInfoBarEmpty = (View) rootView.findViewById(R.id.main_player_content_info_bar_empty);
		
		mDrawerMediaArt = (ImageView) rootView.findViewById(R.id.main_player_content_media_art);
		mDrawerActionsBar = (LinearLayout) rootView.findViewById(R.id.main_player_content_actions);

		/*
		 * makes the whole gang gone, visibility is controlled by the drawer's
		 * opened / closed states.
		 */
		mDrawerInfoBar.setVisibility(View.GONE);
		mDrawerInfoBarNoContent.setVisibility(View.GONE);
		mDrawerInfoBarRadio.setVisibility(View.GONE);
		mDrawerInfoBarEmpty.setVisibility(View.VISIBLE);
		
		mDrawerMediaArt.setVisibility(View.GONE);
		mDrawerActionsBar.setVisibility(View.GONE);

		mDrawerTextTitle = (TextView) rootView.findViewById(R.id.main_player_content_info_bar_text_title);
		mDrawerTextAdditional = (TextView) rootView.findViewById(R.id.main_player_content_info_bar_text_additional);
		mDrawerLoadingIndicator = (ProgressBar) rootView.findViewById(R.id.main_player_content_info_bar_loading_indicator);
		
		mDrawerRadioTitle = (TextView) rootView.findViewById(R.id.main_player_content_info_bar_radio_text);

		// Drawer's header buttons.
		mDrawerButtonViewQueue = (ImageButton) rootView.findViewById(R.id.main_player_content_info_bar_button_view_queue);
		mDrawerButtonComment = (Button) rootView.findViewById(R.id.main_player_content_info_bar_button_comment);
		mDrawerButtonAddFavorites = (Button) rootView.findViewById(R.id.main_player_content_info_bar_button_favorite);
		mDrawerButtonLoad = (Button) rootView.findViewById(R.id.main_player_content_info_bar_button_load);
		
		mDrawerButtonViewQueue.setOnClickListener(this);
		mDrawerButtonComment.setOnClickListener(this);
		mDrawerButtonAddFavorites.setOnClickListener(this);
		mDrawerButtonLoad.setOnClickListener(this);
		
		// Action Buttons.
		mDrawerActionDownload = (Button) rootView.findViewById(R.id.main_player_content_actions_bar_button_download);
		mDrawerActionShare = (Button) rootView.findViewById(R.id.main_player_content_actions_bar_button_share);
		mDrawerActionPlaylist = (Button) rootView.findViewById(R.id.main_player_content_actions_bar_button_playlist);
		mDrawerActionTrivia = (TwoStatesButton) rootView.findViewById(R.id.main_player_content_actions_bar_button_trivia);
		mDrawerActionInfo = (TwoStatesButton) rootView.findViewById(R.id.main_player_content_actions_bar_button_info);
		mDrawerActionSimilar = (TwoStatesButton) rootView.findViewById(R.id.main_player_content_actions_bar_button_similar);
		mDrawerActionVideo = (TwoStatesButton) rootView.findViewById(R.id.main_player_content_actions_bar_button_video);
		mDrawerActionLyrics = (TwoStatesButton) rootView.findViewById(R.id.main_player_content_actions_bar_button_lyrics);
		
		// Hint
		playerQueueHint = (LinearLayout) rootView.findViewById(R.id.player_queue_hint);
		
		// Clicks
		mDrawerActionDownload.setOnClickListener(this);
		mDrawerActionShare.setOnClickListener(this);
		mDrawerActionPlaylist.setOnClickListener(this);
		
		// default visibility - only share and playlist are visible by default.
		
		mDrawerActionDownload.setVisibility(View.GONE);
		mDrawerActionShare.setVisibility(View.VISIBLE);
		mDrawerActionPlaylist.setVisibility(View.VISIBLE);

		mDrawerActionTrivia.setVisibility(View.GONE);
		mDrawerActionInfo.setVisibility(View.GONE);
		mDrawerActionSimilar.setVisibility(View.GONE);
		mDrawerActionVideo.setVisibility(View.GONE);
		mDrawerActionLyrics.setVisibility(View.GONE);
		
		mDrawerActionTrivia.setTag(Integer.valueOf(DRAWER_CONTENT_ACTION_BUTTON_ID_TRIVIA));
		mDrawerActionInfo.setTag(Integer.valueOf(DRAWER_CONTENT_ACTION_BUTTON_ID_INFO));
		mDrawerActionSimilar.setTag(Integer.valueOf(DRAWER_CONTENT_ACTION_BUTTON_ID_SIMILAR));
		mDrawerActionVideo.setTag(Integer.valueOf(DRAWER_CONTENT_ACTION_BUTTON_ID_VIDEO));
		mDrawerActionLyrics.setTag(Integer.valueOf(DRAWER_CONTENT_ACTION_BUTTON_ID_LYRICS));

		mDrawerActionTrivia.setOnClickListener(mDrawerActionsClickListener);
		mDrawerActionSimilar.setOnClickListener(mDrawerActionsClickListener);
		mDrawerActionInfo.setOnClickListener(mDrawerActionsClickListener);
		mDrawerActionVideo.setOnClickListener(mDrawerActionsClickListener);
		mDrawerActionLyrics.setOnClickListener(mDrawerActionsClickListener);
	}
	
	private void initializeMediaArtLoader() {
		
		// gets the size of the media art's image view.
		mDrawerContent.measure(0, 0);
		mDrawerMediaArt.measure(0, 0);
		imageWidth = mDrawerMediaArt.getMeasuredWidth();
		imageHeight = mDrawerMediaArt.getMeasuredHeight();
		
		// sets the cache parameters for the loader.
		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), DataManager.FOLDER_PLAYER_MEDIA_ART_CACHE);
        // Set memory cache to 25% of memory class
        cacheParams.setMemCacheSizePercent(getActivity(), 0.05f);
        cacheParams.compressFormat = CompressFormat.PNG;
        cacheParams.compressQuality = 100;
        
//        mImageFetcher = new ImageFetcher(getActivity(), imageWidth, imageHeight);
//        mImageFetcher.setLoadingImage(R.drawable.icon_main_player_no_content);
//        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
	}
	
	// ======================================================
	// State based methods and helper methods.
	// ======================================================
	
	/**
	 * false = is not playing = shows the play icon. true = is playing shows the
	 * pause icon.
	 * 
	 * @param isSelected
	 */
	private void togglePlayerPlayIcon(boolean isSelected) {
		if (isSelected) {
			mPlayerButtonPlay.setImageDrawable(mResources.getDrawable(R.drawable.icon_main_player_pause_white));
			mPlayerButtonPlay.setSelected(false);
		} else {
			mPlayerButtonPlay.setImageDrawable(mResources.getDrawable(R.drawable.icon_main_player_play_white));
			mPlayerButtonPlay.setSelected(true);
		}

		mPlayerButtonPlay.invalidate();
	}
	
	private void showReplayButtonAsPlay() {
		// show the replay icon
		mPlayerButtonPlay.setImageDrawable(mResources.getDrawable(R.drawable.icon_main_player_repeat_white));
		mPlayerButtonPlay.setSelected(true);
	}
	
	private void updateNextPrevButtonsIfPlaying() {
		if (mPlayerService.isPlaying() || mPlayerService.isLoading()) {
			if (mPlayerService.hasPrevious()) {
				mPlayerButtonPrevious.activate();
			} else {
				mPlayerButtonPrevious.deactivate();
			}

			if (mPlayerService.hasNext()) {
				mPlayerButtonNext.activate();
			} else {
				mPlayerButtonNext.deactivate();
			}
		}
	}

	private void clearPlayer() {
		Log.i(TAG, "CLEAR PLAYER");
		mPlayerSeekBarProgress.setProgress(0);
		mPlayerSeekBarProgress.setSecondaryProgress(0);
		mPlayerTextCurrent.setText(mResources.getString(R.string.main_player_bar_progress_bar_scale_text_current));
		mPlayerTextDuration.setText(mResources.getString(R.string.main_player_bar_progress_bar_scale_text_length));
		mPlayerTextTitle.setText(Utils.TEXT_EMPTY);
		mPlayerTextAdditional.setText(Utils.TEXT_EMPTY);

		togglePlayerPlayIcon(false);
	}

	/**
	 * Populates the player bar's fields based on the given track that is not
	 * even been loaded - while the user has clicked (prev / next) fast.
	 * 
	 * @param track
	 */
	private void populateForFakeTrack(Track track) {
		// checks if the player is still visible.
		if (isAdded()) {
			// titles.
			if (mDrawer.isOpened()) {
				// sets the info bar.
				mDrawerTextTitle.setText(track.getTitle());
				mDrawerTextAdditional.setText(track.getAlbumName());
							
				//mImageFetcher.setExitTasksEarly(true);
				cancelLoadingMediaDetails();

				mDrawerMediaArt.setImageResource(R.drawable.icon_main_player_no_content);
				
				mCurrentTrackDetails = null;

			} else {
				// sets the titles.
				mPlayerTextTitle.setText(track.getTitle());
				
				if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO){
					
					String artistName = track.getArtistName();
									
				    SpannableStringBuilder sb = buildSemiColorString(
				    								blueTitleCelebRadio, 
				    								artistName, 
				    								getResources().getColor(R.color.radio_blue_title_prefix), 
				    								getResources().getColor(R.color.white));
				    
					mPlayerTextAdditional.setText(sb);
					
				} else if(mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO){
					
				    SpannableStringBuilder sb = buildSemiColorString(
							blueTitleLiveRadio, 
							"", 
							getResources().getColor(R.color.radio_blue_title_prefix), 
							getResources().getColor(R.color.white));
					
					mPlayerTextAdditional.setText(sb);
				} else {
					mPlayerTextAdditional.setText(track.getAlbumName());				
				}
			}

			// control buttons.
			if (mPlayerService.hasPrevious()) {
				mPlayerButtonPrevious.activate();
			} else {
				mPlayerButtonPrevious.deactivate();
			}

			if (mPlayerService.hasNext()) {
				mPlayerButtonNext.activate();
			} else {
				mPlayerButtonNext.deactivate();
			}
		}

	}
	
	private SpannableStringBuilder buildSemiColorString(String prefixStr, String suffixStr, int prefixColor, int suffixColor){
		
		int prefLen = prefixStr.length();
		int suffLen = suffixStr.length();
		
		final SpannableStringBuilder sb = new SpannableStringBuilder(prefixStr + suffixStr);
		final ForegroundColorSpan fcsPrefix = new ForegroundColorSpan(prefixColor);
		final ForegroundColorSpan fcsSuffix = new ForegroundColorSpan(suffixColor); 	
		
	    // Set the text color for love word
		sb.setSpan(fcsPrefix, 0, prefLen, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
	    sb.setSpan(fcsSuffix, prefLen, prefLen+suffLen, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
		
	    return sb;
	}

	private void startProgressUpdater() {
		Log.i(TAG, "startProgressUpdater");
		mPlayerService.startProgressUpdater();
//		mPlayerProgressCounter = new  PlayerProgressCounter(this);
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//			mPlayerProgressCounter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//		} else {
//			mPlayerProgressCounter.execute();
//			Log.i(TAG, "EXECUTED - Build VERSION LESS THAN HONEYCOMB");
//		}
	}

	private void stopProgressUpdater() {
		if (mPlayerService != null) {
			mPlayerService.stopProgressUpdater();
		}		
//		if (mPlayerProgressCounter != null
//				&& (mPlayerProgressCounter.getStatus() == AsyncTask.Status.PENDING || 
//					mPlayerProgressCounter.getStatus() == AsyncTask.Status.RUNNING)) {
//			
//			mPlayerProgressCounter.cancel(true);
//			mPlayerProgressCounter = null;
//		}
	}

	private void onPlayerPlayClicked(boolean isSelected) {
		if (isSelected) {
			mPlayerService.play();
		} else {
			mPlayerService.pause();
		}
	}
	
	private void downloadCurrentTrack() {
		Track track = mPlayerService.getCurrentPlayingTrack();
		//check if file is already
		if (track != null) {
			
			// closes the drawer if opened.
			if (mDrawer.isOpened())
				mDrawer.animateClose();
			
			// lunches the activity manages the track downloading.
			MediaItem trackMediaItem  = new MediaItem(track.getId(), track.getTitle(), track.getAlbumName(), 
							track.getArtistName(), track.getImageUrl(), track.getBigImageUrl(), MediaType.TRACK.toString(), 0);
			Intent intent = new Intent(mContext, DownloadConnectingActivity.class);
			intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM, (Serializable) trackMediaItem);
			startActivity(intent);
		}
	}
	
	private void showPlayerQueueHint() {
		
		Animation animationIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_left_enter);
		final Animation animationOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_right_exit_without_dec_interpolator);
		
		playerQueueHint.setVisibility(View.VISIBLE);
		playerQueueHint.startAnimation(animationIn);
		
		final CountDownTimer countDownTimer = new CountDownTimer(7000, 1000) {

		     public void onTick(long millisUntilFinished) {
		    	 
		     }

		     public void onFinish() {
		    	 cancel();
		    	 playerQueueHint.startAnimation(animationOut);
		    	 playerQueueHint.setVisibility(View.GONE);
		     }
		  }.start();
		
		  playerQueueHint.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					playerQueueHint.startAnimation(animationOut);
					countDownTimer.cancel();
					playerQueueHint.setVisibility(View.GONE);
					
				}
			});
	}
	
	/*
	 * Updater for the progress bar and the current playing time.
	 */
	private static class PlayerProgressCounter extends AsyncTask<Void, Void, Void> {
		// TODO: Leak FIX.
		private WeakReference<PlayerBarFragment> playerBarReference = null;
		
		PlayerProgressCounter(PlayerBarFragment playerBarFragment) {
			playerBarReference = new WeakReference<PlayerBarFragment>(playerBarFragment);
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
			
			final PlayerBarFragment playerBarFragment = playerBarReference.get();
			if (playerBarFragment != null && playerBarFragment.mPlayerService != null) {
				
				State state = playerBarFragment.mPlayerService.getState();
				if (state == State.PLAYING) {
					// gets the values.
					final int progress = (int) (((float) playerBarFragment.mPlayerService.getCurrentPlayingPosition() / 
														playerBarFragment.mPlayerService.getDuration()) * 100);
					final String label = Utils.secondsToString(playerBarFragment.mPlayerService.getCurrentPlayingPosition() / 1000) + " / ";

					// updates the views.
					/*
					 * Seems on some devices it might crash for some reason,
					 * seems the device default's AsyncTask implementations are broken for some OEMs.
					 */
					if (playerBarFragment.getActivity() != null) {
						playerBarFragment.getActivity().runOnUiThread(new Runnable() {					
						
							@Override
							public void run() {
								// TODO Auto-generated method stub
								playerBarFragment.mPlayerSeekBarProgress.setProgress(progress);
								playerBarFragment.mPlayerTextCurrent.setText(label);
								Log.i(TAG, "RUN ON UI " + label + " = " + playerBarFragment.mPlayerTextCurrent.getText().toString());
							}
						});
					} else {
						playerBarFragment.mPlayerSeekBarProgress.setProgress(progress);
						playerBarFragment.mPlayerTextCurrent.setText(label);
						Log.i(TAG, label + " = " + playerBarFragment.mPlayerTextCurrent.getText().toString());
					}

					
//					Log.e(TAG, "label:" + label +  "  " + "progress" + progress);
					
					// reports badges and coins for the given playing track.
					int timeMilliseconds = (playerBarFragment.mPlayerService.getDuration() / 100) * playerBarFragment.mPlayerSeekBarProgress.getProgress();
					if (timeMilliseconds >= PlayerService.TIME_REPORT_BADGES_MILLIES)
						playerBarFragment.mPlayerService.reportBadgesAndCoins();
				}
			} else {
				cancel(true);
			}
		}
	}

	private class SeekBarTouchListener implements OnTouchListener {

		@Override
		public boolean onTouch(View view, MotionEvent event) {

			Logger.d(TAG, "Seek bar touched.");

			/*
			 * avoiding seeking when the user didn't finish selecting from where
			 * he wants to play.
			 */
			if (event.getAction() == MotionEvent.ACTION_UP) {
				// updates the playing progress only if we are playing music.
				if (mPlayerService.isPlaying()) {
					int timeMilliseconds = (mPlayerService.getDuration() / 100) * mPlayerSeekBarProgress.getProgress();
					mPlayerService.seekTo(timeMilliseconds);
					
					// reports badges and coins for the given playing track.
					if (timeMilliseconds >= PlayerService.TIME_REPORT_BADGES_MILLIES)
						mPlayerService.reportBadgesAndCoins();
				}
			}
			return false;
		}

	}

	
	// ======================================================
	// Drawer's callbacks.
	// ======================================================
	
	@Override
	public void onDrawerOpened() {
		// enables the image loading.
		//mImageFetcher.setExitTasksEarly(false);
		if (mPlayerService != null) {
			if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
				// adjust player buttons for the playing mode.
				adjustBarWhenOpenedAndPlaying();
				// adjust drawer's content.
				adjustDrawerContentWhenPlaying();
				
				if (mApplicationConfigurations.isFirstVisitToFullPlayer()) {
					mApplicationConfigurations.setIsFirstVisitToFullPlayer(false);
					showPlayerQueueHint();
				} else if (mApplicationConfigurations.getHintsState()){
					if (!mApplicationConfigurations.isPlayerQueueHintShownInThisSession()) {
						mApplicationConfigurations.setIsPlayerQueueHintShownInThisSession(true);
						showPlayerQueueHint();
					} else {
						playerQueueHint.setVisibility(View.GONE);
					}				
				} else {
					playerQueueHint.setVisibility(View.GONE);
				}
	
			} else {
				// adjust player buttons for the not playing mode.
				adjustBarWhenOpenedAndNotPlaying();
				// adjust drawer's content.
				adjustDrawerContentWhenNotPlaying();
			}
		}
	}
	
	@Override
	public void onDrawerClosed() {
		// disables the image loading.
		//mImageFetcher.setExitTasksEarly(true);
		
		if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
			// adjust player buttons for the playing mode.
			adjustBarWhenClosedAndPlaying();

		} else {
			// adjust player buttons for the not playing mode.
			adjustBarWhenClosedAndNotPlaying();
		}
		
		// removes content.
		if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
			clearDrawerContent();
			clearActionButtons();
			closeOpenedContent();
			closeSettings();
			
		} else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO || 
			  mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
			
			mDrawerInfoBarEmpty.setVisibility(View.VISIBLE);
			mDrawerInfoBarRadio.setVisibility(View.GONE);
			
			removeRadioDetails();
		}
		
		if (mIsLoadMenuOpened) {
			
			toggleLoadMenu();
		}
	}
	
	
	// ======================================================
	// player bar and drawer adjustments methods.
	// ======================================================
	
	private void adjustCurrentPlayerState() {
		// listens for playing changes.
		if (mPlayerService != null) {
			mPlayerService.registerPlayerStateListener(this);

			// populate the player bar based on the current state.
			if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
				/*
				 * adjusts the player bar and its content when playing.
				 */
				Logger.d(TAG, "Resuming while playing.");
				if (mDrawer.isOpened()) {
					adjustBarWhenOpenedAndPlaying();
					adjustDrawerContentWhenPlaying();
				} else {
					adjustBarWhenClosedAndPlaying();
					clearDrawerContent();
				}

				// updates the progress.
				startProgressUpdater();

			} else {
				/*
				 * Not playing and not bears. shows default buttons.
				 */
				Logger.d(TAG, "Resuming while not playing.");
				if (mDrawer.isOpened()) {
					Logger.d(TAG, "Drawer is opened");
					adjustBarWhenOpenedAndNotPlaying();
					adjustDrawerContentWhenNotPlaying();
				} else {
					Logger.d(TAG, "Drawer is closed");
					adjustBarWhenClosedAndNotPlaying();
					clearDrawerContent();
				}
			}

		}
	}
	
	private void initializeBarForMusic() {
		
		// sets visibility - Play and Next are always visible.
		mPlayerSeekBar.setVisibility(View.VISIBLE);
		
		mPlayerButtonNext.setVisibility(View.VISIBLE);
		mPlayerButtonPrevious.setVisibility(View.GONE);
		mPlayerButtonFavorites.setVisibility(View.GONE);
		mPlayerButtonQueue.setVisibility(View.GONE);
		mPlayerButtonShuffle.setVisibility(View.GONE);
		mPlayerButtonLoop.setVisibility(View.GONE);
		mPlayerButtonSettings.setVisibility(View.GONE);
		mPlayerButtonLoad.setVisibility(View.VISIBLE);

		mPlayerButtonPlay.deactivate();
		mPlayerButtonNext.deactivate();

		// sets no current playing message
		mPlayerTextTitle.setText(mResources.getString(R.string.main_player_bar_text_not_playing));
		mPlayerTextAdditional.setText(Utils.TEXT_EMPTY);

		// progress bar.
		mPlayerSeekBarProgress.setProgress(0);
		mPlayerSeekBarProgress.setSecondaryProgress(0);
		mPlayerSeekBarProgress.setEnabled(false);
	}
	
	private void initializeBarForRadio() {
		
		// attaches the information for the current playing radio.
		mPlayerSeekBar.setVisibility(View.INVISIBLE);
		
		mPlayerButtonNext.setVisibility(View.GONE);
		mPlayerButtonPrevious.setVisibility(View.GONE);
		mPlayerButtonFavorites.setVisibility(View.GONE);
		mPlayerButtonQueue.setVisibility(View.GONE);
		mPlayerButtonShuffle.setVisibility(View.GONE);
		mPlayerButtonLoop.setVisibility(View.GONE);
		mPlayerButtonSettings.setVisibility(View.GONE);
		mPlayerButtonLoad.setVisibility(View.GONE);
		
		mPlayerButtonPlay.deactivate();
		
		mPlayerTextTitle.setText(mResources.getString(R.string.main_player_bar_text_not_playing));
		mPlayerTextAdditional.setText(Utils.TEXT_EMPTY);
		
		mPlayerSeekBarProgress.setProgress(0);
		mPlayerSeekBarProgress.setSecondaryProgress(0);
	}

	private void adjustBarWhenClosedAndNotPlaying() {
		
		if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
			
			initializeBarForMusic();
			
		} else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO || 
				   mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
			
			initializeBarForRadio();
		}
	}

	private void adjustBarWhenClosedAndPlaying() {
		
		if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
			
			// sets visibility - Play and Next are always visible.
			mPlayerSeekBar.setVisibility(View.VISIBLE);
			
			mPlayerButtonNext.setVisibility(View.VISIBLE);
			mPlayerButtonPrevious.setVisibility(View.GONE);
			mPlayerButtonFavorites.setVisibility(View.VISIBLE);
			mPlayerButtonQueue.setVisibility(View.VISIBLE);
			mPlayerButtonShuffle.setVisibility(View.GONE);
			mPlayerButtonLoop.setVisibility(View.GONE);
			mPlayerButtonSettings.setVisibility(View.GONE);
			mPlayerButtonLoad.setVisibility(View.GONE);

			// activate / deactivate buttons.
			mPlayerButtonPlay.activate();

			if (mPlayerService.hasNext()) {
				mPlayerButtonNext.activate();
			} else {
				mPlayerButtonNext.deactivate();
			}

			// progress bar.
			mPlayerSeekBarProgress.setEnabled(true);

			Track currentTrack = mPlayerService.getCurrentPlayingTrack();
			
			if (currentTrack == null) {
				return;
			}
			
			// update the text.
			mPlayerTextTitle.setText(currentTrack.getTitle());
			mPlayerTextAdditional.setText(currentTrack.getAlbumName());

			// loading indicator.
			if (mPlayerService.isLoading()) {
				mPlayerLoadingIndicator.setVisibility(View.VISIBLE);
			}

			// sets the play button.
			if (mPlayerService.getState() == State.PLAYING) {
				togglePlayerPlayIcon(true);
				// sets the duration of the track.
				mPlayerTextDuration.setText(Utils.secondsToString(mPlayerService.getDuration() / 1000));

			} else if (mPlayerService.getState() == State.PAUSED) {
				togglePlayerPlayIcon(false);
				// sets the current position in the progress.
				int progress = (int) (((float) mPlayerService.getCurrentPlayingPosition() / mPlayerService.getDuration()) * 100);
				mPlayerSeekBarProgress.setProgress(progress);
				// sets the duration of the track.
				mPlayerTextDuration.setText(Utils.secondsToString(mPlayerService.getDuration() / 1000));
				
			} else if  (mPlayerService.getState() == State.COMPLETED_QUEUE) {
				showReplayButtonAsPlay();
			}
			
			if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
				// favorite button
				if (mCurrentTrackDetails != null && mCurrentTrackDetails.getId() == currentTrack.getId()) {
					setPlayerButtonFavorite();
				}
			}
			
		} else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO || 
				   mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
			
			// attaches the information for the current playing radio.
			mPlayerSeekBar.setVisibility(View.INVISIBLE);
			
			mPlayerButtonNext.setVisibility(View.GONE);
			mPlayerButtonPrevious.setVisibility(View.GONE);
			mPlayerButtonQueue.setVisibility(View.GONE);
			mPlayerButtonShuffle.setVisibility(View.GONE);
			mPlayerButtonLoop.setVisibility(View.GONE);
			mPlayerButtonSettings.setVisibility(View.GONE);
			mPlayerButtonLoad.setVisibility(View.GONE);
			
			// favorite button.
			if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
				mPlayerButtonFavorites.setVisibility(View.VISIBLE);
			} else {
				mPlayerButtonFavorites.setVisibility(View.GONE);
			}
			
			mPlayerButtonPlay.activate();
			
			Track currentTrack = mPlayerService.getCurrentPlayingTrack();

			// update the text.
			mPlayerTextTitle.setText(currentTrack.getTitle());
			
			if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO){
				
				String artistName = currentTrack.getArtistName();
				
			    SpannableStringBuilder sb = buildSemiColorString(
						blueTitleCelebRadio, 
						artistName, 
						getResources().getColor(R.color.radio_blue_title_prefix), 
						getResources().getColor(R.color.white));
				
				mPlayerTextAdditional.setText(sb);
			} else {
				
			    SpannableStringBuilder sb = buildSemiColorString(
						blueTitleLiveRadio, 
						"", 
						getResources().getColor(R.color.radio_blue_title_prefix), 
						getResources().getColor(R.color.white));
				
				mPlayerTextAdditional.setText(sb);
			}

			// loading indicator.
			if (mPlayerService.isLoading()) {
				mPlayerLoadingIndicator.setVisibility(View.VISIBLE);
			}
			
			if (mPlayerService.getState() == State.PLAYING) {
				togglePlayerPlayIcon(true);
			} else if (mPlayerService.getState() == State.PAUSED) {
				togglePlayerPlayIcon(false);
			}
		}

	}

	private void adjustBarWhenOpenedAndNotPlaying() {
		
		if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
			// adjust player bar buttons.
			mPlayerSeekBar.setVisibility(View.VISIBLE);
			
			mPlayerButtonNext.setVisibility(View.VISIBLE);
			mPlayerButtonPrevious.setVisibility(View.VISIBLE);
			mPlayerButtonFavorites.setVisibility(View.GONE);
			mPlayerButtonQueue.setVisibility(View.GONE);
			mPlayerButtonShuffle.setVisibility(View.VISIBLE);
			mPlayerButtonLoop.setVisibility(View.VISIBLE);
			mPlayerButtonSettings.setVisibility(View.VISIBLE);
			mPlayerButtonLoad.setVisibility(View.GONE);
			
			clearPlayer();

			// disable buttons.
			mPlayerButtonPlay.deactivate();
			mPlayerButtonNext.deactivate();
			mPlayerButtonPrevious.deactivate();
			mPlayerButtonNext.deactivate();
			mPlayerButtonShuffle.deactivate();
			mPlayerButtonLoop.deactivate();
			mPlayerButtonSettings.deactivate();
			
			mPlayerSeekBarProgress.setEnabled(false);
			
		} else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO || 
				   mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
			
			mPlayerSeekBar.setVisibility(View.INVISIBLE);
			mPlayerButtonNext.setVisibility(View.GONE);
			mPlayerButtonPrevious.setVisibility(View.GONE);
			mPlayerButtonFavorites.setVisibility(View.GONE);
			mPlayerButtonShuffle.setVisibility(View.GONE);
			mPlayerButtonLoop.setVisibility(View.GONE);
			mPlayerButtonSettings.setVisibility(View.GONE);
			mPlayerButtonLoad.setVisibility(View.GONE);
			
			mPlayerButtonPlay.activate();

			// update the text.
			Track currentTrack = mPlayerService.getCurrentPlayingTrack();
			mPlayerTextTitle.setText(currentTrack.getTitle());
			
			if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO){
				
				String artistName = currentTrack.getArtistName();
				
			    SpannableStringBuilder sb = buildSemiColorString(
						blueTitleCelebRadio, 
						artistName, 
						getResources().getColor(R.color.radio_blue_title_prefix), 
						getResources().getColor(R.color.white));
				
				mPlayerTextAdditional.setText(sb);
			} else {
				
			    SpannableStringBuilder sb = buildSemiColorString(
						blueTitleLiveRadio, 
						"", 
						getResources().getColor(R.color.radio_blue_title_prefix), 
						getResources().getColor(R.color.white));
				
				mPlayerTextAdditional.setText(sb);
			}
		}
	}

	private void adjustBarWhenOpenedAndPlaying() {
		
		if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
			
			// adjust player bar buttons.
			mPlayerSeekBar.setVisibility(View.VISIBLE);
			
			mPlayerButtonNext.setVisibility(View.VISIBLE);
			mPlayerButtonPrevious.setVisibility(View.VISIBLE);
			mPlayerButtonFavorites.setVisibility(View.GONE);
			mPlayerButtonQueue.setVisibility(View.GONE);
			mPlayerButtonShuffle.setVisibility(View.VISIBLE);
			mPlayerButtonLoop.setVisibility(View.VISIBLE);
			mPlayerButtonSettings.setVisibility(View.VISIBLE);
			mPlayerButtonLoad.setVisibility(View.GONE);

			// enable buttons.
			mPlayerButtonPlay.activate();

			if (mPlayerService.hasPrevious()) {
				mPlayerButtonPrevious.activate();
			} else {
				mPlayerButtonPrevious.deactivate();
			}

			if (mPlayerService.hasNext()) {
				mPlayerButtonNext.activate();
			} else {
				mPlayerButtonNext.deactivate();
			}

			mPlayerButtonShuffle.activate();
			mPlayerButtonLoop.activate();
			mPlayerButtonSettings.activate();
			
			// sets the state of the loop button based on the service loop mode.
			LoopMode loopMode = mPlayerService.getLoopMode();
			if (loopMode == LoopMode.REAPLAY_SONG) {
				// sets the single loop icon.
				mPlayerButtonLoop.setState(ThreeStatesActiveButton.State.SECOND);
				
			} else if (loopMode == LoopMode.ON) {
				// sets whole player queue to be looped.
				mPlayerButtonLoop.setState(ThreeStatesActiveButton.State.THIRD);
			}
			
			// sets the suffle button.
			if (mPlayerService.isShuffling()) {
				mPlayerButtonShuffle.setState(TwoStatesActiveButton.State.SECOND);
			} else {
				mPlayerButtonShuffle.setState(TwoStatesActiveButton.State.ACTIVE);
			}

			// sets the play button.
			if (mPlayerService.getState() == State.PLAYING) {
				togglePlayerPlayIcon(true);
				// sets the duration of the track.
				mPlayerTextDuration.setText(Utils.secondsToString(mPlayerService.getDuration() / 1000));

			} else if (mPlayerService.getState() == State.PAUSED) {
				togglePlayerPlayIcon(false);
				// sets the current position in the progress.
				int progress = (int) (((float) mPlayerService.getCurrentPlayingPosition() / mPlayerService.getDuration()) * 100);
				mPlayerSeekBarProgress.setProgress(progress);
				// sets the duration of the track.
				mPlayerTextDuration.setText(Utils.secondsToString(mPlayerService.getDuration() / 1000));
				
			} else if  (mPlayerService.getState() == State.COMPLETED_QUEUE) {
				showReplayButtonAsPlay();
			}

			// clears the texts.
			mPlayerTextTitle.setText(Utils.TEXT_EMPTY);
			mPlayerTextAdditional.setText(Utils.TEXT_EMPTY);

			// removes the loading indicator.
			mPlayerLoadingIndicator.setVisibility(View.GONE);

		} else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO || 
				   mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
			
			mPlayerSeekBar.setVisibility(View.INVISIBLE);
			
			mPlayerButtonNext.setVisibility(View.GONE);
			mPlayerButtonPrevious.setVisibility(View.GONE);
			mPlayerButtonShuffle.setVisibility(View.GONE);
			mPlayerButtonLoop.setVisibility(View.GONE);
			mPlayerButtonSettings.setVisibility(View.GONE);
			mPlayerButtonLoad.setVisibility(View.GONE);
			
			// favorite button.
			if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
				mPlayerButtonFavorites.setVisibility(View.VISIBLE);
			} else {
				mPlayerButtonFavorites.setVisibility(View.GONE);
			}
			
			mPlayerButtonPlay.activate();
			
			Track currentTrack = mPlayerService.getCurrentPlayingTrack();
			mPlayerTextTitle.setText(currentTrack.getTitle());
			
			if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO){
				
				String artistName = currentTrack.getArtistName();
				
			    SpannableStringBuilder sb = buildSemiColorString(
						blueTitleCelebRadio, 
						artistName, 
						getResources().getColor(R.color.radio_blue_title_prefix), 
						getResources().getColor(R.color.white));
				
				mPlayerTextAdditional.setText(sb);
			} else {
				
			    SpannableStringBuilder sb = buildSemiColorString(
						blueTitleLiveRadio, 
						"", 
						getResources().getColor(R.color.radio_blue_title_prefix), 
						getResources().getColor(R.color.white));
				
				mPlayerTextAdditional.setText(sb);
			}

			// loading indicator.
			if (mPlayerService.isLoading()) {
				mPlayerLoadingIndicator.setVisibility(View.VISIBLE);
			}
			
			// sets the play button.
			if (mPlayerService.getState() == State.PLAYING) {
				togglePlayerPlayIcon(true);
			} else if (mPlayerService.getState() == State.PAUSED) {
				togglePlayerPlayIcon(false);
			}
		}
	}

	private void adjustDrawerContentWhenPlaying() {
		if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
			
			// sets the visibility.
			mDrawerInfoBar.setVisibility(View.VISIBLE);
			mDrawerInfoBarNoContent.setVisibility(View.GONE);
			mDrawerInfoBarRadio.setVisibility(View.GONE);
			mDrawerInfoBarEmpty.setVisibility(View.GONE);
			
			mDrawerMediaArt.setVisibility(View.VISIBLE);
			mDrawerActionsBar.setVisibility(View.VISIBLE);

			mDrawerMediaArt.setVisibility(View.VISIBLE);

			// populates the data.

			Track track = mPlayerService.getCurrentPlayingTrack();
			
			if (track == null) {
				return;
			}

			// sets the titles.
			mDrawerTextTitle.setText(track.getTitle());
			mDrawerTextAdditional.setText(track.getAlbumName());

			// sets loading indicator.
			if (mPlayerService.isLoading()) {
				mDrawerLoadingIndicator.setVisibility(View.VISIBLE);
			} else {
				mDrawerLoadingIndicator.setVisibility(View.GONE);
			}

			if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
				startLoadingMediaArt(mPlayerService.getCurrentPlayingTrack().getBigImageUrl());
				
				// gets the media details from the server only if the player doesn't have it already.
				if (mCurrentTrackDetails == null || mCurrentTrackDetails.getId() != track.getId()) {
					startLoadingMediaDetails(track);
				} else {
					adjustActionButtonsVisibility(mCurrentTrackDetails);
					// favorite button
					if (mCurrentTrackDetails.getId() == track.getId()) {
						setDrawerButtonFavorite();
					}
				}
			}
			
			// sets the buttons in the info bar.
			mDrawerButtonViewQueue.setClickable(true);
			mDrawerButtonComment.setClickable(true);
			mDrawerButtonAddFavorites.setClickable(true);

		} else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO || 
				   mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {

			// sets the title
			
			mDrawerInfoBarEmpty.setVisibility(View.GONE);
			mDrawerInfoBarRadio.setVisibility(View.VISIBLE);
			
			if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO ) {
				Track track = mPlayerService.getCurrentPlayingTrack();
				MediaItem mediaItem = (MediaItem) track.getTag();
				
				if(mediaItem != null){
					String title = mediaItem.getTitle();
					if(title != null){
						mDrawerRadioTitle.setText(title);
					}
				}
				
			} else {
				mDrawerRadioTitle.setText(R.string.radio_top_artist_radio);
			}
			
			showRadioDetails();
		}

	}
	
	private void adjustDrawerContentWhenNotPlaying() {
		
		if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
			
			// sets the visibility.
			mDrawerInfoBar.setVisibility(View.GONE);
			mDrawerInfoBarNoContent.setVisibility(View.VISIBLE);
			mDrawerInfoBarEmpty.setVisibility(View.GONE);
			mDrawerInfoBarRadio.setVisibility(View.GONE);
			
			mDrawerMediaArt.setVisibility(View.VISIBLE);
			mDrawerActionsBar.setVisibility(View.GONE); // relays on its height..

			mDrawerMediaArt.setVisibility(View.VISIBLE);

			// clears the titles.
			mDrawerTextTitle.setText(Utils.TEXT_EMPTY);
			mDrawerTextAdditional.setText(Utils.TEXT_EMPTY);

			// removes the loading indicator.
			mDrawerLoadingIndicator.setVisibility(View.GONE);

			// sets the media art to the default no content.
			mDrawerMediaArt.setImageResource(R.drawable.icon_main_player_no_content);
			
			// sets the buttons in the info bar.
			mDrawerButtonViewQueue.setClickable(false);
			mDrawerButtonComment.setClickable(false);
			mDrawerButtonAddFavorites.setClickable(false);
			
		} else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO || 
				   mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
			
			if (mDrawerInfoBarEmpty != null) {
				// sets the title
				mDrawerInfoBarEmpty.setVisibility(View.GONE);
				mDrawerInfoBarRadio.setVisibility(View.VISIBLE);
				mDrawerRadioTitle.setText(Utils.TEXT_EMPTY);
			}
			removeRadioDetails();
		}
	}
	
	public void clearDrawerContent() {

		//mImageFetcher.setExitTasksEarly(true);
		cancelLoadingMediaDetails();

		mDrawerMediaArt.setVisibility(View.INVISIBLE);
		mDrawerMediaArt.setBackgroundResource(0);

		mDrawerInfoBar.setVisibility(View.GONE);
		mDrawerInfoBarNoContent.setVisibility(View.GONE);
		mDrawerInfoBarRadio.setVisibility(View.GONE);
		mDrawerInfoBarRadio.setVisibility(View.GONE);
		
		mDrawerInfoBarEmpty.setVisibility(View.VISIBLE);
		
		mDrawerMediaArt.setVisibility(View.GONE);
		mDrawerActionsBar.setVisibility(View.GONE);

		// playing and opened.
		mDrawerTextTitle.setText(Utils.TEXT_EMPTY);
		mDrawerTextAdditional.setText(Utils.TEXT_EMPTY);
	}
	
	// ======================================================
	// Media art loading.
	// ======================================================

	private void startLoadingMediaArt(String imageURL) {
		Logger.d(TAG, "Start Loading image: " + imageURL);
		//mImageFetcher.loadImage(imageURL, mDrawerMediaArt);
		
		Picasso.with(mContext).cancelRequest(mDrawerMediaArt);
		Picasso.with(mContext).load(R.drawable.icon_main_player_no_content);
		if(TextUtils.isEmpty(imageURL)){
			if(mCurrentTrackDetails != null){
				imageURL = mCurrentTrackDetails.getBigImageUrl();
			}
		}
		
		if(mContext != null && !TextUtils.isEmpty(imageURL)){
			Picasso.with(mContext).
			load(imageURL).
			placeholder(R.drawable.icon_main_player_no_content).
			into(mDrawerMediaArt);
		}
	}

	
	// ======================================================
	// Media Details. loading.
	// ======================================================

	private void cancelLoadingMediaDetails() {
		mDataManager.cancelGetMediaDetails();
	}
	
	private void startLoadingMediaDetails(Track track) {
		// cancel any running loading.
		mDataManager.cancelGetMediaDetails();
		
		MediaItem mediaItem = new MediaItem(track.getId(), null, null, null, null,
				null, MediaType.TRACK.toString(), 0);
		mDataManager.getMediaDetails(mediaItem, null, this);
	}
	
	
	// ======================================================
	// Communication Operations events.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		if (!isDetached() && !isRemoving() && getActivity() != null && !getActivity().isFinishing()) {
			if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
				Logger.d(TAG, "Loading media details");
				clearActionButtons();
			} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
				Logger.d(TAG, "Adding to Favorites");
//				showLoadingDialog(R.string.application_dialog_adding_to_favorites);
				
			} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
				Logger.d(TAG, "Removing from favorites");
//				showLoadingDialog(R.string.application_dialog_removing_from_favorites);
			}
		}
	}
	
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			Logger.d(TAG, "Success loading media details");
			
			mCurrentTrackDetails = (MediaTrackDetails) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
			
			/*
			 * Due to bugs with synchronization of the favorite states
			 * we checks like stupid people if the Fragment has not detached from it's activity.
			 */
			if (!isDetached() && !isRemoving() && getActivity() != null && !getActivity().isFinishing()) {
				if (mDrawer.isOpened()) {
					if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
						adjustActionButtonsVisibility(mCurrentTrackDetails);
					}
					// update the favorite button in the drawer info bar.
					setDrawerButtonFavorite();
				} else {
					// update the favorite button in the player bar.
					setPlayerButtonFavorite();
				}
			}

			String url = mCurrentTrackDetails.getBigImageUrl();
			Picasso.with(mContext).load(R.drawable.icon_main_player_no_content);
			if(mContext != null && !TextUtils.isEmpty(url)){
				Picasso.with(mContext).
				load(url).
				placeholder(R.drawable.icon_main_player_no_content).
				into(mDrawerMediaArt);
			}
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
			BaseHungamaResponse addToFavoriteResponse = 
					(BaseHungamaResponse) responseObjects.get(AddToFavoriteOperation.RESULT_KEY_ADD_TO_FAVORITE);
			
			Track track = mPlayerService.getCurrentPlayingTrack();

			if (!isDetached() && !isRemoving() && getActivity() != null && !getActivity().isFinishing()) {
				if (addToFavoriteResponse.getCode() == FAVORITE_SUCCESS ) {
					if (mCurrentTrackDetails != null && 
							mCurrentTrackDetails.getId() == track.getId()) {
						
						MediaItem mediaItem = new MediaItem(track.getId(), track.getTitle(), track.getAlbumName(), 
															track.getArtistName(), track.getImageUrl(), track.getBigImageUrl(), 
															MediaType.TRACK.toString().toLowerCase(), 0);
						
						mCurrentTrackDetails.setNumOfFav(mCurrentTrackDetails.getNumOfFav() + 1);
						
						// packs an added media item intent action.
						Intent intent = new Intent(ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
						Bundle extras = new Bundle();
						extras.putSerializable(ActionDefinition.EXTRA_MEDIA_ITEM, (Serializable) mediaItem);
						extras.putBoolean(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE, true);
						extras.putInt(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT, mCurrentTrackDetails.getNumOfFav());
						intent.putExtras(extras);
						
						mLocalBroadcastManager.sendBroadcast(intent);
					}
				} else {
					// could not add the track from favorite by the server.. shows the error message.
					if (track != null && track.getTitle() != null) {
						Toast.makeText(mContext, getResources().getString(R.string.favorite_error_saving, 
															track.getTitle()), Toast.LENGTH_LONG).show();
					}
				}
			}
				
		} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
			// done removing from favorites.
			BaseHungamaResponse removeFromFavoriteResponse = 
					(BaseHungamaResponse) responseObjects.get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);
			
			Track track = mPlayerService.getCurrentPlayingTrack();
			
			if (!isRemoving() && getActivity() != null && !getActivity().isFinishing()) {
				if (removeFromFavoriteResponse.getCode() == FAVORITE_SUCCESS ) {
					//shows a message.
					Toast toast = Toast.makeText(mContext, getResources().getString(R.string.favorite_removed, track.getTitle()), Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					
					MediaItem mediaItem = new MediaItem(track.getId(), track.getTitle(), track.getAlbumName(), 
							track.getArtistName(), track.getImageUrl(), track.getBigImageUrl(), 
							MediaType.TRACK.toString().toLowerCase(), 0);
					
					mCurrentTrackDetails.setNumOfFav(mCurrentTrackDetails.getNumOfFav() - 1);
					
					// packs an added media item intent action.
					Intent intent = new Intent(ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
					Bundle extras = new Bundle();
					extras.putSerializable(ActionDefinition.EXTRA_MEDIA_ITEM, (Serializable) mediaItem);
					extras.putBoolean(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE, false);
					extras.putInt(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT, mCurrentTrackDetails.getNumOfFav());
					intent.putExtras(extras);
					
					mLocalBroadcastManager.sendBroadcast(intent);
					
				} else {
					// could not remove the track from favorite by the server.. shows the error message.
					if (track != null && track.getTitle() != null) {
						Toast.makeText(mContext, getResources().getString(R.string.favorite_error_removing, 
																track.getTitle()), Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		
		hideLoadingDialog();
	}
	
	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			Logger.d(TAG,
					"Failed loading media details: " + errorType.toString()
							+ " " + errorMessage);
			clearActionButtons();
		} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
			Logger.d(TAG, "Failed Adding to Favorites");
			mDrawerButtonAddFavorites.setClickable(true);
			mPlayerButtonFavorites.setClickable(true);
			hideLoadingDialog();
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
			Logger.d(TAG, "Failed Removing from favorites");
			mDrawerButtonAddFavorites.setClickable(true);
			mPlayerButtonFavorites.setClickable(true);
			hideLoadingDialog();
		}
	}	

	
	// ======================================================
	// Drawer Actions.
	// ======================================================
	
	private void adjustActionButtonsVisibility(MediaTrackDetails mediaTrackDetails) {
		// adjust action buttons in the drawer's content.
		mDrawerActionInfo.setVisibility(View.VISIBLE);
		mDrawerActionSimilar.setVisibility(View.VISIBLE);
		
		ArrayList<TwoStatesButton> buttons = new ArrayList<TwoStatesButton>();
		
		if (mCurrentTrackDetails.hasVideo()) {
			mDrawerActionVideo.setVisibility(View.VISIBLE);
			buttons.add(mDrawerActionVideo);
		} else {
			mDrawerActionVideo.setVisibility(View.GONE);
		}
		
		if (mCurrentTrackDetails.hasLyrics()) {
			mDrawerActionLyrics.setVisibility(View.VISIBLE);
			buttons.add(mDrawerActionLyrics);
		} else {
			mDrawerActionLyrics.setVisibility(View.GONE);
		}
		
		if (mCurrentTrackDetails.hasTrivia()) {
			mDrawerActionTrivia.setVisibility(View.VISIBLE);
			buttons.add(mDrawerActionTrivia);
		} else {
			mDrawerActionTrivia.setVisibility(View.GONE);
		}
		
		if (mCurrentTrackDetails.hasDownload()) {
			mDrawerActionDownload.setVisibility(View.VISIBLE);
		} else {
			mDrawerActionDownload.setVisibility(View.GONE);
		}			
		
		Drawable even = getResources().getDrawable(R.drawable.background_main_player_content_action_button_even_selector);
		Drawable odd = getResources().getDrawable(R.drawable.background_main_player_content_action_button_odd_selector);
		
		boolean toggle = false;
		for(TwoStatesButton b : buttons){
			
			if(toggle){
				// black
				b.setUnselectedBackground(odd);
			}else{
				// grey
				b.setUnselectedBackground(even);
			}
			toggle = !toggle;
		}
	}
	
	private void clearActionButtons() {
		
		// closes any opened internal content.
		closeOpenedContent();
		
		// hides the content's buttons.
		mDrawerActionTrivia.setVisibility(View.GONE);
		mDrawerActionInfo.setVisibility(View.GONE);
		mDrawerActionSimilar.setVisibility(View.GONE);
		mDrawerActionVideo.setVisibility(View.GONE);
		mDrawerActionLyrics.setVisibility(View.GONE);
		
		if (mDrawerActionInfo.isSelected()) {
			mDrawerActionInfo.setUnselected();
		}
		
		if (mDrawerActionSimilar.isSelected()) {
			mDrawerActionSimilar.setUnselected();
		}
		
		if (mDrawerActionVideo.isSelected()) {
			mDrawerActionVideo.setUnselected();
		}
		
		if (mDrawerActionLyrics.isSelected()) {
			mDrawerActionLyrics.setUnselected();
		}
		
	}
	
	private void openConentFor(int actionContentId) {
		// creates the related fragment to the button id.
		Fragment fragment = createFragmentForAction(actionContentId);
		if (fragment != null) {
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			
			// adds the fragment to the Drawer content's container.
			fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit);
			fragmentTransaction.add(R.id.main_player_content_container, fragment, DRAWER_CONTENT_ACTION_BUTTON_FRAGMENT_TAG);
			fragmentTransaction.commit();
		}
	}
	
	private void closeOpenedContent() {
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		Fragment lastOpenedFragment = mFragmentManager.findFragmentByTag(DRAWER_CONTENT_ACTION_BUTTON_FRAGMENT_TAG);

		if (lastOpenedFragment != null) {
			// removes the fragment from the Drawer content's container.
			fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit);
			fragmentTransaction.remove(lastOpenedFragment);
			fragmentTransaction.commitAllowingStateLoss();
		}
	}
	
	private Fragment createFragmentForAction(int actionContentId) {
		
		if (actionContentId == DRAWER_CONTENT_ACTION_BUTTON_ID_INFO) {
			PlayerInfoFragment playerInfoFragment = new PlayerInfoFragment();
			// adds the current track's details as argument.
			Bundle data = new Bundle();
			data.putSerializable(PlayerInfoFragment.FRAGMENT_ARGUMENT_MEDIA_TRACK_DETAILS, (Serializable) mCurrentTrackDetails);
			playerInfoFragment.setArguments(data);
			playerInfoFragment.setOnInfoItemSelectedListener(new OnInfoItemSelectedListener() {
				@Override
				public void onInfoItemSelected(String infoItemText) {
					closeContent();
//					// Open the Search Fragment with the given search query.
					if (infoItemText.contains("(")) {					
						int startPosition = infoItemText.indexOf("(");
						int endPosition = infoItemText.indexOf(")");
						if (endPosition > startPosition) {
							String album = infoItemText.substring(0, startPosition);						
							String year = infoItemText.substring(startPosition + 1 , endPosition);
							if (TextUtils.isDigitsOnly(year)) {
								openMainSearchFragment(album);
							} else {
								openMainSearchFragment(infoItemText);
							}
						}				
					} else {
						openMainSearchFragment(infoItemText);
					}
					//openMainSearchFragment(infoItemText);
				}
			});
			
			FlurryAgent.logEvent(getActivity().getString(R.string.full_player_info));
			
			return playerInfoFragment;
			
		} else if (actionContentId == DRAWER_CONTENT_ACTION_BUTTON_ID_SIMILAR) {
			// sets listener for the tile's options.
			PlayerSimilarFragment playerSimilarFragment = new PlayerSimilarFragment();
			playerSimilarFragment.setOnMediaItemOptionSelectedListener(mOnSimilarMediaItemOptionSelectedListener);
			// passes in the current track.
			Track track = mPlayerService.getCurrentPlayingTrack();
			Bundle data = new Bundle();
			data.putSerializable(PlayerSimilarFragment.FRAGMENT_ARGUMENT_TRACK, (Serializable) track);
			playerSimilarFragment.setArguments(data);
			
			FlurryAgent.logEvent(getActivity().getString(R.string.full_player_similar));
			
			return playerSimilarFragment;
			
		} else if (actionContentId == DRAWER_CONTENT_ACTION_BUTTON_ID_VIDEO) {
			
			PlayerVideoFragment playerVideoFragment = new PlayerVideoFragment();
			playerVideoFragment.setOnMediaItemOptionSelectedListener(mOnVideoMediaItemOptionSelectedListener);
			
			// adds the current track's details as argument.
			Bundle data = new Bundle();
			data.putSerializable(playerVideoFragment.FRAGMENT_ARGUMENT_TRACK_DETAILS, (Serializable) mCurrentTrackDetails);
			playerVideoFragment.setArguments(data);
			
			FlurryAgent.logEvent(getActivity().getString(R.string.full_player_video));
			
			return playerVideoFragment;
			
		} else if (actionContentId == DRAWER_CONTENT_ACTION_BUTTON_ID_LYRICS) {
			PlayerLyricsFragment playerLyricsFragment = new PlayerLyricsFragment();
			
			Track track = mPlayerService.getCurrentPlayingTrack();
			Bundle data = new Bundle();
			data.putSerializable(PlayerSimilarFragment.FRAGMENT_ARGUMENT_TRACK, (Serializable) track);
			
			playerLyricsFragment.setArguments(data);
			
			FlurryAgent.logEvent(getActivity().getString(R.string.full_player_lyrics));
			
			return playerLyricsFragment;
			
		} else if (actionContentId == DRAWER_CONTENT_ACTION_BUTTON_ID_TRIVIA) {
			PlayerTriviaFragment playerTriviaFragment = new PlayerTriviaFragment();
			
			Track track = mPlayerService.getCurrentPlayingTrack();
			Bundle data = new Bundle();
			data.putSerializable(PlayerSimilarFragment.FRAGMENT_ARGUMENT_TRACK, (Serializable) track);
			
			playerTriviaFragment.setArguments(data);
			
			FlurryAgent.logEvent(getActivity().getString(R.string.full_player_trivia));
			
			return playerTriviaFragment;
		}
		
		
		return null;
	}
	
	
	// listeners.
	
	private OnClickListener mDrawerActionsClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			
			LinearLayout parent = (LinearLayout) view.getParent();
			
			View child = null;
			TwoStatesButton button = null;
			int count = parent.getChildCount();
			for (int i = 0; i < count; i++) {
				child = parent.getChildAt(i);
				// marks only the two state buttons which are not the current clicked one.
				if (child.getVisibility() == View.VISIBLE && child instanceof TwoStatesButton) {

					button = ((TwoStatesButton) child);
					
					// if the given child is not the clicked one, only closes it.
					if (!child.equals(view)) {
						if (button.isSelected()) {
							// marks the button as unselected.
							button.setUnselected();
							// closes its referencing fragment.
							closeOpenedContent();
						}
					} else {
						/*
						 * if the given child is the clicked one,
						 * checks if has already selected or not to toggle its
						 * referencing layout visibility.  
						 */
						if (button.isSelected()) {
							// opens its referencing fragment.
							int buttonId = ((Integer) button.getTag()).intValue();
							openConentFor(buttonId);
						} else {
							// closes its referencing fragment.
							closeOpenedContent();
						}
					}
				}
			}
			
			
		}
	};
	
	private OnMediaItemOptionSelectedListener mOnSimilarMediaItemOptionSelectedListener = new OnMediaItemOptionSelectedListener() {
		
		@Override public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem, int position) { }
		@Override public void onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position) { }
		
		@Override
		public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
									mediaItem.getAlbumName(), mediaItem.getArtistName(), 
									mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				playNow(tracks);
			}
		}
		
		@Override
		public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem, int position) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
										mediaItem.getAlbumName(), mediaItem.getArtistName(),  
										mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				playNext(tracks);
			}
		}
		
		@Override
		public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem, int position) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
										mediaItem.getAlbumName(), mediaItem.getArtistName(), 
										mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				addToQueue(tracks);
			}
		}
	};
	
	private OnMediaItemOptionSelectedListener mOnVideoMediaItemOptionSelectedListener = new OnMediaItemOptionSelectedListener() {

		@Override public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position) {}
		@Override public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem, int position) {}
		@Override public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem, int position) {}
		@Override public void onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position) {}

		@Override
		public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem, int position) {
			if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				// closes the drawer.
				closeContent();
				
				// fires the View Video Activity.
				Intent intent = new Intent(getActivity(), VideoActivity.class);
				intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO, (Serializable) mediaItem);
				getActivity().startActivity(intent);
			}
		}

	};
	
	
	// ======================================================
	// Drawer Actions - Radio.
	// ======================================================
	
	private void showRadioDetails() {
		// checks if it's already visible.
		RadioDetailsFragment radioDetailsFragment = 
				(RadioDetailsFragment) mFragmentManager.findFragmentByTag(RadioDetailsFragment.TAG);

		if (radioDetailsFragment == null) {
			
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			radioDetailsFragment = new RadioDetailsFragment();
			
			
			// sets the arguments.
			MediaItem mediaItem = null;
			MediaCategoryType mediaCategoryType = null;
			
			Track currentTrack = mPlayerService.getCurrentPlayingTrack();
			mediaItem = (MediaItem) currentTrack.getTag();
			
			if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
				mediaCategoryType = MediaCategoryType.TOP_ARTISTS_RADIO;
				
			} else {
				mediaCategoryType = MediaCategoryType.LIVE_STATIONS;
			}
			
			if(mediaItem != null) {
				mediaItem.setMediaContentType(MediaContentType.RADIO);
			}
			
			Bundle arguments = new Bundle();
			arguments.putSerializable(RadioDetailsFragment.EXTRA_CATEGORY_TYPE, (Serializable) mediaCategoryType);
			arguments.putSerializable(RadioDetailsFragment.EXTRA_MEDIA_ITEM, (Serializable) mediaItem);
			arguments.putBoolean(RadioDetailsFragment.EXTRA_DO_SHOW_TITLE_BAR, false);
			
			radioDetailsFragment.setArguments(arguments);
			
			// adds the fragment to the Drawer's container.
			fragmentTransaction.add(R.id.main_player_drawer_content, radioDetailsFragment, RadioDetailsFragment.TAG);
			fragmentTransaction.commit();
		}
	}
	
	private void removeRadioDetails() {
		// checks if it's visible.
		RadioDetailsFragment radioDetailsFragment = 
				(RadioDetailsFragment) mFragmentManager.findFragmentByTag(RadioDetailsFragment.TAG);
		
		if (radioDetailsFragment != null) {
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.remove(radioDetailsFragment);
			fragmentTransaction.commit();
		}
	}
	
	
	// ======================================================
	// Load Menu.
	// ======================================================
	
	private void toggleLoadMenu() {
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_top_enter, R.anim.slide_and_show_top_exit, 
												R.anim.slide_and_show_top_enter, R.anim.slide_and_show_top_exit);
		/*
		 * check if the fragment exist in the manager, then will know if it displayed.
		 */
		PlayerLoadMenuFragment  playerLoadMenuFragment = 
				(PlayerLoadMenuFragment) mFragmentManager.findFragmentByTag(PlayerLoadMenuFragment.TAG);
		if (playerLoadMenuFragment != null) {
			fragmentTransaction.remove(playerLoadMenuFragment);
			fragmentTransaction.commit();
			mIsLoadMenuOpened = false;
		} else {
			playerLoadMenuFragment = new PlayerLoadMenuFragment();
			playerLoadMenuFragment.setOnLoadMenuItemOptionSelectedListener(this);
			fragmentTransaction.add(R.id.main_player_content_container, playerLoadMenuFragment, PlayerLoadMenuFragment.TAG);
			fragmentTransaction.commit();
			mIsLoadMenuOpened = true;
		}
	}
	
	@Override
	public void onLoadMenuTop10Selected(List<Track> topTenMediaItems) {
		if (!Utils.isListEmpty(topTenMediaItems)) {
			addToQueue(topTenMediaItems);
			
			// the player's content is opened. adjusts the components.
			adjustBarWhenOpenedAndPlaying();
			adjustDrawerContentWhenPlaying();
		}
		
		toggleLoadMenu();
	}
	
	@Override
	public void onLoadMenuRadioSelected() {
		if (isContentOpened()) {
			closeContent();
		}
		((MainActivity) getActivity()).setNavigationItemSelected(NavigationItem.RADIO);
		
	}
	
	@Override
	public void onLoadMenuMyPlaylistSelected() {
		// Show playlist Dialog
		List<Track> tracks = new ArrayList<Track>(); 
		boolean isFromLoadMenu = true;
		FragmentManager fm = getActivity().getSupportFragmentManager();
        PlaylistDialogFragment playlistDialogFragment = PlaylistDialogFragment.newInstance(tracks, isFromLoadMenu);
        playlistDialogFragment.setOnLoadMenuItemOptionSelectedListener(this);
        playlistDialogFragment.show(fm, "PlaylistDialogFragment");
	}
	
	@Override
	public void onLoadMenuMyFavoritesSelected() {

		// closes the player bar content.
		closeContent();
		
		// shows the favorite activity.
		Intent favoritesActivityIntent = new Intent(getActivity().getApplicationContext(), FavoritesActivity.class);
		startActivity(favoritesActivityIntent);
	}

	@Override
	public void onLoadPlaylistFromDialogSelected(List<Track> tracks) {
		if (!Utils.isListEmpty(tracks)) {
			addToQueue(tracks);	
			
			// the player's content is opened. adjusts the components. 
			adjustBarWhenOpenedAndPlaying();
			adjustDrawerContentWhenPlaying();
		}
		toggleLoadMenu();
	}


	// ======================================================
	// Favorites
	// ======================================================
	
	private void setDrawerButtonFavorite() {
		if (mCurrentTrackDetails.IsFavorite()) {
			mDrawerButtonAddFavorites.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.icon_media_details_fav_blue), null, null);
			mDrawerButtonAddFavorites.setSelected(true);						
		} else {
			mDrawerButtonAddFavorites.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.icon_media_details_fav_white), null, null);
			mDrawerButtonAddFavorites.setSelected(false);
		}
		mDrawerButtonAddFavorites.setText(String.valueOf(mCurrentTrackDetails.getNumOfFav()));
	}
	
	private void setPlayerButtonFavorite() {
		if (mCurrentTrackDetails.IsFavorite()) {
			mPlayerButtonFavorites.setImageDrawable(getResources().getDrawable(R.drawable.icon_main_player_favorites_blue));
			mPlayerButtonFavorites.setSelected(true);
		} else {
			mPlayerButtonFavorites.setImageDrawable(getResources().getDrawable(R.drawable.icon_main_player_favorites_white));
			mPlayerButtonFavorites.setSelected(false);
		}
	}
	
	/**
	 * Handles changes in the favorite state of Media Items, marks the button accordingly.
	 */
	private static final class MediaItemFavoriteStateReceiver extends BroadcastReceiver {
		
		private final WeakReference<PlayerBarFragment> playerBarFragmentReference;
		
		MediaItemFavoriteStateReceiver(PlayerBarFragment playerBarFragment) {
			this.playerBarFragmentReference = new WeakReference<PlayerBarFragment>(playerBarFragment);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED.equalsIgnoreCase(intent.getAction())) {
				Bundle extras = intent.getExtras();
				MediaItem mediaItem = (MediaItem) extras.getSerializable(ActionDefinition.EXTRA_MEDIA_ITEM);
				boolean isFavorite = extras.getBoolean(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE);
				int count = extras.getInt(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT);
				
				PlayerBarFragment playerBarFragment = playerBarFragmentReference.get();
				if (playerBarFragment == null) {
					return;
				}
				
				if (playerBarFragment.mCurrentTrackDetails != null && 
						playerBarFragment.mCurrentTrackDetails.getId() == mediaItem.getId()) {
					
					Resources resources = playerBarFragment.getResources();
					
					// updates the given media details.
					playerBarFragment.mCurrentTrackDetails.setNumOfFav(count);
					
					if (isFavorite) {
						
						Drawable d = resources.getDrawable(R.drawable.icon_media_details_fav_blue);
						// updates the view.
						playerBarFragment.mDrawerButtonAddFavorites.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
						playerBarFragment.mDrawerButtonAddFavorites.setSelected(true);
						playerBarFragment.mDrawerButtonAddFavorites.setText(Integer.toString(count));
						
						playerBarFragment.mPlayerButtonFavorites.setSelected(true);
						playerBarFragment.mPlayerButtonFavorites.setImageDrawable(resources.getDrawable(R.drawable.icon_main_player_favorites_blue));
						
						playerBarFragment.mCurrentTrackDetails.setIsFavorite(true);
						
					} else {
						
						// updates the view.
						playerBarFragment.mDrawerButtonAddFavorites.setCompoundDrawablesWithIntrinsicBounds(null, 
											resources.getDrawable(R.drawable.icon_media_details_fav_white), null, null);
						
						playerBarFragment.mDrawerButtonAddFavorites.setText(Integer.toString(count));
						playerBarFragment.mDrawerButtonAddFavorites.setSelected(false);
						
						playerBarFragment.mPlayerButtonFavorites.setImageDrawable(resources.getDrawable(R.drawable.icon_main_player_favorites_white));
						playerBarFragment.mPlayerButtonFavorites.setSelected(false);
						
						playerBarFragment.mCurrentTrackDetails.setIsFavorite(false);
					}
					playerBarFragment.mDrawerButtonAddFavorites.setClickable(true);
					playerBarFragment.mPlayerButtonFavorites.setClickable(true);
				}
			}
		}
		
	}
	
	
	// ======================================================
	// Queue. 
	// ======================================================
	
	private void openQueue() {
		// disables the button until we close the Queue page.
		mDrawerButtonViewQueue.setClickable(false);
		
		// locks the drawer, will be released on closing.
		mDrawer.lock();
		
		// shows the Queue page.
		PlayerQueueFragment playerQueueFragment = new PlayerQueueFragment();
		playerQueueFragment.setOnPlayerQueueUpdetedListener(this);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit,
												R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit);
		
		fragmentTransaction.add(R.id.main_player_container_addtional, playerQueueFragment, PlayerQueueFragment.TAG);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	private void closeQueue() {
		// enables the button until we close the Queue page.
		
		if (mPlayerService.isPlaying() || mPlayerService.isLoading()){
			// will enabled only if we are still playing music to avoid the LOAD button to be avoided.
			mDrawerButtonViewQueue.setClickable(true);
		} else {
			mDrawerButtonLoad.setClickable(true);
		}
		
		// unlocks the drawer.
		mDrawer.unlock();
		
		// checks if the any additional content is open, if so closes if before the drawer.
		Fragment fragment = mFragmentManager.findFragmentByTag(PlayerQueueFragment.TAG);
		if (fragment != null && fragment.isVisible()) {
			
			// removes the fragment.
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit, 
													R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit);
			fragmentTransaction.remove(fragment);
			fragmentTransaction.commit();
			mFragmentManager.popBackStack();
		}
	}
	
	private boolean isQueueOpened() {
		Fragment fragment = mFragmentManager.findFragmentByTag(PlayerQueueFragment.TAG);
		return (fragment != null && fragment.isVisible());
	}
	
	
	// ======================================================
	// Settings. 
	// ======================================================
	
	private void openSettings() {
		// shows the settings fragment.
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit, 
												R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit);
		
		PlayerSettingsMenuFragment playerSettingsMenuFragment = new PlayerSettingsMenuFragment();
		
		playerSettingsMenuFragment.setOnModeSelectedListener(this);
		fragmentTransaction.add(R.id.main_player_drawer_content, playerSettingsMenuFragment, PlayerSettingsMenuFragment.TAG);
		fragmentTransaction.commit();
	}
	
	private void closeSettings() {
		// if the settings fragment is visible, hides it.
		Fragment fragment = mFragmentManager.findFragmentByTag(PlayerSettingsMenuFragment.TAG);
		
		if (fragment != null && fragment.isVisible()) {
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit, 
													R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit);
			
			PlayerSettingsMenuFragment playerSettingsMenuFragment = (PlayerSettingsMenuFragment) fragment;
			
			playerSettingsMenuFragment.setOnModeSelectedListener(null);
			fragmentTransaction.remove(playerSettingsMenuFragment);
			fragmentTransaction.commit();
		}
	}

	
	// ======================================================
	// Sleep mode / Gym mode. 
	// ======================================================
	
	@Override
	public void onSleepModeSelected() {
		// does nothing, been managed by the SleepModeManager.
		closeSettings();
	}

	@Override
	public void onGymModeSelected() {
		closeSettings();
		// shows the gym mode player.
		openGymMode();
	}
	
	private void openGymMode() {
		
		// disables any button behind it.
		mDrawerButtonViewQueue.setClickable(false);
		mDrawerButtonComment.setClickable(false);
		mDrawerButtonAddFavorites.setClickable(false);
		
		// locks the drawer to avoid it moving while swiping the gym mode.
		mDrawer.lock();
		
		// shows the Gym Mode content.
		PlayerGymModeFragment playerGymModeFragment = new PlayerGymModeFragment();
		playerGymModeFragment.setOnPlayButtonStateChangedListener(new OnPlayButtonStateChangedListener() {
			
			@Override
			public void onPlayClicked() {
				togglePlayerPlayIcon(true);
			}
			
			@Override
			public void onPauseClicked() {
				togglePlayerPlayIcon(false);
			}
		});
		playerGymModeFragment.setOnGymModeExitClickedListener(this);
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit,
												R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit);
		
		fragmentTransaction.add(R.id.main_player_container_addtional, playerGymModeFragment, playerGymModeFragment.TAG);
		fragmentTransaction.commit();
	}
	
	private void closeGymMode() {
		// hides the Gym Mode content.
		Fragment fragment = mFragmentManager.findFragmentByTag(PlayerGymModeFragment.TAG);
		if (fragment != null && fragment.isVisible()) {
			
			PlayerGymModeFragment playerGymModeFragment = (PlayerGymModeFragment) fragment;
			playerGymModeFragment.setOnPlayButtonStateChangedListener(null);
			playerGymModeFragment.setOnGymModeExitClickedListener(null);
			
			// it was opened. closes it.
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit,
													R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit);
			
			fragmentTransaction.remove(fragment);
			fragmentTransaction.commit();
			
			// unlocks the drawer to.
			mDrawer.unlock();
			
			// enables any button behind it.
			mDrawerButtonViewQueue.setClickable(true);
			mDrawerButtonComment.setClickable(true);
			mDrawerButtonAddFavorites.setClickable(true);
		}
	}
	
	@Override
	public void onGymModeExit() {
		closeGymMode();
	}
	
	
	// ======================================================
	// Add to playlists.
	// ======================================================
	
	private void showAddToPlaylistDialog() {
		List<Track> playingTrack = new ArrayList<Track>();
		playingTrack.add(mPlayerService.getCurrentPlayingTrack());
        PlaylistDialogFragment editNameDialog = PlaylistDialogFragment.newInstance(playingTrack, false);
        editNameDialog.show(mFragmentManager, "PlaylistDialogFragment");
	}

	public SeekBar getmPlayerSeekBarProgress() {
		return mPlayerSeekBarProgress;
	}

	public TextView getmPlayerTextCurrent() {
		return mPlayerTextCurrent;
	}

	/* (non-Javadoc)
	 * @see com.hungama.myplay.activity.player.PlayerService.PlayerBarUpdateListener#OnPlayerBarUpdate(int, java.lang.String)
	 */
	@Override
	public void OnPlayerBarUpdate(int progress, String label) {
		
		mPlayerSeekBarProgress.setProgress(progress);
		mPlayerTextCurrent.setText(label);
		
	}

	
}
