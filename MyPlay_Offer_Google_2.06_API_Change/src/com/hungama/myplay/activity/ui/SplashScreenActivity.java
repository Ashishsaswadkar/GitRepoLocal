package com.hungama.myplay.activity.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.hungama.myplay.activity.R;

/**
 * Shows the application's splash screen when opening it from the launcher application.
 */
public class SplashScreenActivity extends SherlockActivity {
	
	private TextView text0;
	private TextView text1;
	private TextView text2;
	private TextView text3;
	private TextView text4;
	
	private ImageView splashPlusImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.application_splash_layout);
		
		// initializes the animations components and animates.
		text0 = (TextView) findViewById(R.id.text_0);
		text1 = (TextView) findViewById(R.id.text_1);
		text2 = (TextView) findViewById(R.id.text_2);
		text3 = (TextView) findViewById(R.id.text_3);
		text4 = (TextView) findViewById(R.id.text_4);
		splashPlusImageView = (ImageView) findViewById(R.id.splash_plus_image_view);
		
		// Starts the sequence of the animations.
		runAnimation();
	}
	
	private void finishSplash() {
		// finishes the Slash screen, returning back. 
		setResult(RESULT_OK);
		finish();
	}
	
	private void runAnimation(){
		// Create Animation object for each view
		Animation text0Animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
		text0Animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) { }
			
			@Override
			public void onAnimationRepeat(Animation animation) { }
			
			@Override
			public void onAnimationEnd(Animation animation) {
				text0.setVisibility(View.VISIBLE);
				splashPlusImageView.setVisibility(View.VISIBLE);
				runText1Animation();
			}
		});
		text0.startAnimation(text0Animation);
		splashPlusImageView.startAnimation(text0Animation);
	}
	
	private void runText1Animation(){
		// Create Animation object for each view
		Animation text1Animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
		text1Animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				text1.setVisibility(View.VISIBLE);
				runText2Animation();
			}
		});
		text1.startAnimation(text1Animation);
	}
	
	private void runText2Animation(){
		// Create Animation object for each view
		Animation text2Animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
		text2Animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) { }
			
			@Override
			public void onAnimationRepeat(Animation animation) { }
			
			@Override
			public void onAnimationEnd(Animation animation) {
				text2.setVisibility(View.VISIBLE);
				runText3Animation();
			}
		});
		text2.startAnimation(text2Animation);
	}
	
	private void runText3Animation(){
		// Create Animation object for each view
		Animation text3Animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
		text3Animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				text3.setVisibility(View.VISIBLE);
				runText4Animation();
			}
		});
		text3.startAnimation(text3Animation);
	}
	
	private void runText4Animation(){
		// Create Animation object for each view
		Animation text4Animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
		text4Animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				text4.setVisibility(View.VISIBLE);
				
				final Handler mHandler = new Handler(){
					
					@Override
					public void handleMessage(Message msg) {
						finishSplash();
					}
				};
				mHandler.sendEmptyMessageDelayed(0, 1000);
			}
		});
		text4.startAnimation(text4Animation);
	}

}
