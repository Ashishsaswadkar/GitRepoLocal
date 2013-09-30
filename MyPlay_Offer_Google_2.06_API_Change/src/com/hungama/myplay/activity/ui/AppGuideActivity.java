package com.hungama.myplay.activity.ui;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.ui.fragments.PlayerGymModeFragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;


public class AppGuideActivity extends Activity{

	private static final String TAG = "AppGuideActivity";
	
	public static final String ARGUMENT_APP_GUIDE_ACTIVITY = "argument_app_guide_activity";
	
	public static final int PERIOD = 20*1000;
	
	private CountDownTimer countDownTimer;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_app_guide_home);
		String fromGymModeFragment = getIntent().getStringExtra(PlayerGymModeFragment.ARGUMENT_GYM_MODE_FRAGMENT);
		String fromHomeActivity = getIntent().getStringExtra(HomeActivity.ARGUMENT_HOME_ACTIVITY);
		if (fromHomeActivity != null) {
			setContentView(R.layout.activity_app_guide_home);
		} else if (fromGymModeFragment !=null) {
			setContentView(R.layout.activity_app_guide_gym_mode);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		countDownTimer = new CountDownTimer(PERIOD, 1000) {

		     public void onTick(long millisUntilFinished) {
		         
		     }

		     public void onFinish() {
		    	 stopCounter();
		    	 finish();
		     }
		  }.start(); 
	}
	
	@Override
	protected void onPause() {
		stopCounter();
		super.onPause();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		stopCounter();
   	 	finish();
		return super.onTouchEvent(event);
		
	}
	
	private void stopCounter() {
		if (countDownTimer != null) {
			countDownTimer.cancel();
			countDownTimer = null;
		}
	}
	

}