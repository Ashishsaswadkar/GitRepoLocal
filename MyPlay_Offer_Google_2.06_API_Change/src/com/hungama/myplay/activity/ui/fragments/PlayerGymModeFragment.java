package com.hungama.myplay.activity.ui.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.DisplayMetrics;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerService.Error;
import com.hungama.myplay.activity.player.PlayerService.PlayerSericeBinder;
import com.hungama.myplay.activity.player.PlayerService.PlayerStateListener;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager.ServiceToken;
import com.hungama.myplay.activity.ui.AppGuideActivity;
import com.hungama.myplay.activity.ui.widgets.ActiveButton;
import com.hungama.myplay.activity.util.ActionCounter;
import com.hungama.myplay.activity.util.ActionCounter.OnActionCounterPerform;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.AsyncTask;

public class PlayerGymModeFragment extends Fragment implements OnClickListener, ServiceConnection, 
															   PlayerStateListener, OnActionCounterPerform, 
															   OnGestureListener {
	
	public static final String TAG = "PlayerGymModeFragment";
	
	public interface OnGymModeExitClickedListener {
		
		public void onGymModeExit();
	}
	
	public void setOnGymModeExitClickedListener(OnGymModeExitClickedListener listener) {
		mOnGymModeExitClickedListener = listener;
	}
	
	
	private static final int ACTION_INTERVAL_MS = 		 	200;
	private static final int ACTION_MESSAGE_NEXT = 		 100001;
	private static final int ACTION_MESSAGE_PREVIOUS = 	 100002;
	
	private OnGymModeExitClickedListener mOnGymModeExitClickedListener;
	
	private Resources mResources;
	
	private ServiceToken mServiceToken = null;
	private PlayerService mPlayerService = null;
	
	private Button mDrawerButtonGymModeExit;
	
	private ActiveButton mButtonPlay;
	private ActiveButton mButtonPrevious;
	private ActiveButton mButtonNext;
	
	private SeekBar mSeekbar;
	private TextView mTextCurrent;
	private TextView mTextTotal;
	
	private ActionCounter mActionCounter;
	private PlayerProgressCounter mPlayerProgressCounter;
	
	private OnPlayButtonStateChangedListener mOnPlayButtonStateChangedListener;
	
	/*
	 * For detecting the Right / Left Swipes.
	 */
	private GestureDetectorCompat mDetector; 
	private float MINMUM_DISTANCE;
	
	/*
	 * For App Guide
	 */
	public static final String ARGUMENT_GYM_MODE_FRAGMENT = "argument_gym_mode_fragment";
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mResources = getResources();
		
		final ViewConfiguration vc = ViewConfiguration.get(getActivity());
        DisplayMetrics dm = mResources.getDisplayMetrics();
        MINMUM_DISTANCE = vc.getScaledPagingTouchSlop() * dm.density;
        
        //For App Guide
        mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_player_gym_mode, container, false);
		
		// initializes the detector for the swipings.
		mDetector = new GestureDetectorCompat(getActivity(), this);

		// overrides the touch events to avoid fragments under this to handle touches.
		rootView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// makes the whole view to detect the swipes.
				mDetector.onTouchEvent(event);
				return true;
			}
		});
		
		mDrawerButtonGymModeExit = (Button) rootView.findViewById(R.id.player_gym_mode_exit_button);
		
		mDrawerButtonGymModeExit.setOnClickListener(this);
		
		// initializes the user controls.
		mButtonPlay = (ActiveButton) rootView.findViewById(R.id.player_gym_mode_controll_button_play);
		mButtonPrevious = (ActiveButton) rootView.findViewById(R.id.player_gym_mode_controll_button_previous);
		mButtonNext = (ActiveButton) rootView.findViewById(R.id.player_gym_mode_controll_button_next);
		
		mSeekbar = (SeekBar) rootView.findViewById(R.id.player_gym_mode_progress_bar_seek_bar);
		mTextCurrent = (TextView) rootView.findViewById(R.id.player_gym_mode_progress_bar_scale_text_current);
		mTextTotal = (TextView) rootView.findViewById(R.id.player_gym_mode_progress_bar_scale_text_length);
		
		mButtonPlay.setOnClickListener(this);
		mButtonPrevious.setOnClickListener(this);
		mButtonNext.setOnClickListener(this);
		
		mButtonPlay.setSelected(false);
		togglePlayerPlayIcon(false);
		
		// this is 100%, from 0 to 99.
		mSeekbar.setMax(99);
		mSeekbar.setOnTouchListener(new SeekBarTouchListener());
		mSeekbar.setEnabled(false);
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (mApplicationConfigurations.isEnabledGymModeGuidePage()) {
			mApplicationConfigurations.setIsEnabledGymModeGuidePage(false);
			openAppGuideActivity();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// initializes the action counter for the next / prev buttons.
		mActionCounter = new ActionCounter(ACTION_INTERVAL_MS);
		mActionCounter.setOnActionCounterPerform(this);
		
		// requests a bind to the player service.
		mServiceToken = PlayerServiceBindingManager.bindToService(getActivity(), this);
	}
	
	@Override
	public void onPause() {
		
		// stops the Next / Prev buttons counter listener.
		mActionCounter.setOnActionCounterPerform(null);
		mActionCounter.cancelAnyAction();
		mActionCounter = null;

		// stops updating the progress.
		stopProgressUpdater();
		
		// stops listening to any changes from the service.
		if (mPlayerService != null) {
			mPlayerService.unregisterPlayerStateListener(this);
		}

		/*
		 * disconnects from the bound service, we allow to perform
		 * it here because this client is a temporary client that
		 * dosen't being replaced due to Activities replaces. 
		 */
		PlayerServiceBindingManager.unbindFromService(mServiceToken);
		
		super.onPause();
	}
	
	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		
		if (viewId == R.id.player_gym_mode_controll_button_play) {
			if (!mPlayerService.isQueueEmpty()) {
				
				// notifies the client to the changes. 
				if (view.isSelected()) {
					if (mOnPlayButtonStateChangedListener != null)
						mOnPlayButtonStateChangedListener.onPlayClicked();
				} else {
					if (mOnPlayButtonStateChangedListener != null)
						mOnPlayButtonStateChangedListener.onPauseClicked();
				}
				
				// updates the server.
				onPlayerPlayClicked(view.isSelected());
				// udaptes the button.
				togglePlayerPlayIcon(view.isSelected());
			}
			
		} else if (viewId == R.id.player_gym_mode_controll_button_previous) {
			performPrevious();
			
		} else if (viewId == R.id.player_gym_mode_controll_button_next) {
			performNext();
			
		} else if (viewId == R.id.player_gym_mode_exit_button) {
			if (mOnGymModeExitClickedListener != null)
				mOnGymModeExitClickedListener.onGymModeExit();
		}
	}
	
	
	// ======================================================
	// Logic callbacks.
	// ======================================================
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// gets hook on the service.
		PlayerSericeBinder binder = (PlayerSericeBinder) service;
		mPlayerService = binder.getService();
		mPlayerService.registerPlayerStateListener(this);
		
		updateUserControllersWhenConnected();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// releases the hook on the service.
		mPlayerService = null;
		mServiceToken = null;
	}
	
	@Override
	public void onActionCounterPerform(int actionId) {
		mPlayerService.play();
	}

	@Override
	public void onStartLoadingTrack(Track track) {
		
		// stops updating the progress texts.
		// resets the seek bar.
		stopProgressUpdater();
		
		// disables the seek bar.
		mSeekbar.setEnabled(false);
		
		// disables the play button.
		mButtonPlay.setClickable(false);
		
		// sets the play button with the "play" icon.
		togglePlayerPlayIcon(false);
		
		// updates next / prev buttons.
		updateNextPrevButtonsIfPlaying();
	}

	@Override
	public void onStartPlayingTrack(Track track) {
		
		// enables the play button.
		mButtonPlay.setClickable(true);
		
		// sets the play button with the "pause" icon.
		togglePlayerPlayIcon(true);
		
		// enables the seek bar.
		mSeekbar.setEnabled(true);
		
		// starts updating the progress.
		startProgressUpdater();
	}

	@Override
	public void onFinishPlayingTrack(Track track) {
		// stops updating the progress.
		stopProgressUpdater();
		togglePlayerPlayIcon(false);
	}

	@Override
	public void onFinishPlayingQueue() {
		// stops updating the progress.
		stopProgressUpdater();
		togglePlayerPlayIcon(false);
	}

	@Override
	public void onSleepModePauseTrack(Track track) {
		/*
		 * If the player is visible, the music will be paused and
		 * this method will be invoked, updates the play button icon.
		 */
		togglePlayerPlayIcon(false);
	}

	@Override
	public void onErrorHappened(Error error) {
		mButtonPlay.setClickable(true);
	}
	
	@Override
	public void onTrackLoadingBufferUpdated(Track track, int precent) {
		// updates the seek bar.
		// track is always null.
		mSeekbar.setSecondaryProgress(precent);
	}
	
	
	// ======================================================
	// Helper methods.
	// ======================================================
	
	private void updateUserControllersWhenConnected() {
		
		// updates the play button.
		if (mPlayerService.isPlaying()) {

			// updates the play buttons.
			mButtonPlay.activate();
			// sets the icon.
			if (mPlayerService.getState() == State.PLAYING) {
				togglePlayerPlayIcon(true);
				
			} else if (mPlayerService.getState() == State.PAUSED) {
				togglePlayerPlayIcon(false);
			}
			
			// enables the seek bar.
			mSeekbar.setEnabled(true);
			
			// updates the progress.
			startProgressUpdater();
			
		} else if (mPlayerService.isLoading()) {
			togglePlayerPlayIcon(false);
			mButtonPlay.deactivate();
			
			// disables the seek bar.
			mSeekbar.setEnabled(true);
			
			// stops / clears the progress.
			stopProgressUpdater();
		}
	
		
		// updates the next / prev buttons.
		updateNextPrevButtonsIfPlaying();
	}
	
	/**
	 * false = is not playing = shows the play icon. true = is playing shows the
	 * pause icon.
	 * 
	 * @param isSelected
	 */
	private void togglePlayerPlayIcon(boolean isSelected) {
		if (isSelected) {
			mButtonPlay.setImageDrawable(mResources.getDrawable(R.drawable.icon_main_player_pause_white));
			mButtonPlay.setSelected(false);
		} else {
			mButtonPlay.setImageDrawable(mResources.getDrawable(R.drawable.icon_main_player_play_white));
			mButtonPlay.setSelected(true);
		}

		mButtonPlay.invalidate();
	}
	
	private void onPlayerPlayClicked(boolean isSelected) {
		if (isSelected) {
			mPlayerService.play();
		} else {
			mPlayerService.pause();
		}
	}
	
	private void updateNextPrevButtonsIfPlaying() {
		if (mPlayerService.isPlaying() || mPlayerService.isLoading()) {
			if (mPlayerService.hasPrevious()) {
				mButtonPrevious.activate();
			} else {
				mButtonPrevious.deactivate();
			}

			if (mPlayerService.hasNext()) {
				mButtonNext.activate();
			} else {
				mButtonNext.deactivate();
			}
		}
	}

	
	private void performPrevious() {
		if (!mPlayerService.isQueueEmpty() && mPlayerService.hasPrevious()) {
			Track track = mPlayerService.fakePrevious();
			if (track != null) {
				// register the next action.
				mActionCounter.performAction(ACTION_MESSAGE_PREVIOUS);
			}
		}
		
		updateNextPrevButtonsIfPlaying();
	}
	
	private void performNext() {
		if (!mPlayerService.isQueueEmpty() && mPlayerService.hasNext()) {
			// set next faked track.
			Track track = mPlayerService.fakeNext();
			if (track != null) {
				// register the next action.
				mActionCounter.performAction(ACTION_MESSAGE_NEXT);
			}
		}
		
		updateNextPrevButtonsIfPlaying();
	}
	
	private void openAppGuideActivity() {
		Intent intent = new Intent(getActivity().getApplicationContext(), AppGuideActivity.class);
		intent.putExtra(ARGUMENT_GYM_MODE_FRAGMENT, "gym_mode_fragment");
		startActivity(intent);
	}
	// ======================================================
	// Seek bar update tasks and listeners.
	// ======================================================
	
	private void startProgressUpdater() {
		mPlayerProgressCounter = new PlayerProgressCounter();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mPlayerProgressCounter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			mPlayerProgressCounter.execute();
		}
	}

	private void stopProgressUpdater() {
		if (mPlayerProgressCounter != null
				&& (mPlayerProgressCounter.getStatus() == AsyncTask.Status.PENDING || 
					mPlayerProgressCounter.getStatus() == AsyncTask.Status.RUNNING)) {
			
			mPlayerProgressCounter.cancel(true);
			mPlayerProgressCounter = null;
			
			mSeekbar.setProgress(0);
			mSeekbar.setSecondaryProgress(0);
			mTextCurrent.setText(mResources.getString(R.string.main_player_bar_progress_bar_scale_text_current));
			mTextTotal.setText(mResources.getString(R.string.main_player_bar_progress_bar_scale_text_length));
		}
	}

	private class PlayerProgressCounter extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			while (!isCancelled()) {
				try {
					publishProgress(null);
					if (isCancelled()) {
						break;
					}
					Thread.sleep(500);
				} catch (InterruptedException e) {
					break;
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			State state = mPlayerService.getState();
			if (state == State.PLAYING) {
				final int progress = (int) (((float) mPlayerService.getCurrentPlayingPosition() / mPlayerService.getDuration()) * 100);
				final String label = Utils.secondsToString(mPlayerService.getCurrentPlayingPosition() / 1000) + " / ";
				
				// updates the views.
				mSeekbar.setProgress(progress);
				mTextCurrent.setText(label);
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
					int timeMilliseconds = (mPlayerService.getDuration() / 100) * mSeekbar.getProgress();
					mPlayerService.seekTo(timeMilliseconds);
				}
			}
			return false;
		}

	}


	// ======================================================
	// Public
	// ======================================================
	
	public interface OnPlayButtonStateChangedListener {
		
		public void onPlayClicked();
		
		public void onPauseClicked();
	}
	
	public void setOnPlayButtonStateChangedListener(OnPlayButtonStateChangedListener listener) {
		mOnPlayButtonStateChangedListener = listener;
	}

	
	
	// ======================================================
	// Gestures callbacks.
	// ======================================================

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		Logger.v(TAG, "Swipe ws detected!!! Event 1: " + e1.getX() + " Event 2: " + 
					e2.getX() + " velocityX: " + velocityX + " velocityY: " + velocityY);
		
		final float delta = e1.getX() - e2.getX();
		
		// the distance must be more then the minimum.
		// the x velocity must be more the the minimum.
		// the  velocity must be less the the minimum.
		if (Math.abs(delta) >= MINMUM_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)) {
			if (delta > 0) {
				// the user has flinged from left to right.
				Logger.v(TAG, "the user has flinged from left to right.");
				performNext();
				
			} else {
				// the user has flinged from right to left.
				Logger.v(TAG, "the user has flinged from right to left.");
				performPrevious();
			}
		}
		
		
		return false;
	}

	
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	
	@Override
	public void onShowPress(MotionEvent e) {}

	
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

 
	@Override
	public void onLongPress(MotionEvent e) {
	}

	
}
