package com.hungama.myplay.activity.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.ui.fragments.AppTourFragment;
import com.hungama.myplay.activity.util.viewpageindicator.CirclePageIndicator;
import com.hungama.myplay.activity.util.viewpageindicator.PageIndicator;

public class AppTourActivity extends SecondaryActivity {

	private static final String TAG = "AppTourActivity";
	
	private AppTourDetailsAdapter mAdapter;
	private ViewPager mPager;
	private PageIndicator mIndicator;
			
	private List<Drawable> mListOfPrevImages;
	private List<String> mListOfTextTitles;
	private List<String> mListOfTextBody;
	
	public static final int PERIOD = 3*1000;
	private CountDownTimer countDownTimer;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_tour);
			
		initPage();			
		
		mAdapter = new AppTourDetailsAdapter(getSupportFragmentManager(), this);
		mPager = (ViewPager) findViewById(R.id.view_pager);
		mPager.setAdapter(mAdapter);
		
		mIndicator =  (CirclePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
		
		mPager.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				if(countDownTimer != null){
					countDownTimer.cancel();
				}
				
				return false;
			}
		});
		
		mPager.setCurrentItem(0);
		mIndicator.setCurrentItem(0);	
		
		ImageButton skipButton = (ImageButton) findViewById(R.id.app_tour_skip_button);
		skipButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
				
			}
		});
						
		int i = 1;
		newCountdownTimer(i, AppTourDetailsAdapter.NUM_ITEMS);
	}
	
	private void newCountdownTimer (final int i, final int numOfPages) {
		countDownTimer = new CountDownTimer(3000, 1000) {

		     public void onTick(long millisUntilFinished) {
		         
		     }

		     public void onFinish() {
		    	 mPager.setCurrentItem(i);
		    	 cancel();
		    	 if (i < numOfPages) {
		    		 int k = i+1;
		    		 newCountdownTimer(k, numOfPages);
		    	 } else {
    				setResult(RESULT_OK);
    				finish();
		    	 }
		     }
		  }.start();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		FlurryAgent.logEvent("App tour");
	}
	
	@Override
	protected void onStop() {
		if (countDownTimer != null) {
			countDownTimer.cancel();
			countDownTimer = null;
		}
		super.onStop();
		
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void initPage() {

			Resources res = getResources();
			
			// Prepare the Titles
			String mTextTitle0 = res.getString(R.string.app_tour_text_title_0);
			String mTextTitle1 = res.getString(R.string.app_tour_text_title_1);
			String mTextTitle2 = res.getString(R.string.app_tour_text_title_2);
			String mTextTitle3 = res.getString(R.string.app_tour_text_title_3);
			String mTextTitle4 = res.getString(R.string.app_tour_text_title_4);
			
			// Prepare the Body Text
			String mTextBody0 = res.getString(R.string.app_tour_text_body_0);
			String mTextBody1 = res.getString(R.string.app_tour_text_body_1);
			String mTextBody2 = res.getString(R.string.app_tour_text_body_2);
			String mTextBody3 = res.getString(R.string.app_tour_text_body_3);
			String mTextBody4 = res.getString(R.string.app_tour_text_body_4);
			
			// Prepare the Images
			Drawable mImage0 = res.getDrawable(R.drawable.icon_app_tour_music_video);
			Drawable mImage1 = res.getDrawable(R.drawable.icon_app_tour_discover);
			Drawable mImage2 = res.getDrawable(R.drawable.icon_app_tour_gamification);
			Drawable mImage3 = res.getDrawable(R.drawable.icon_app_tour_radio);
			Drawable mImage4 = res.getDrawable(R.drawable.icon_app_tour_others);
			
			// Add Images to a new list
			mListOfPrevImages = new ArrayList<Drawable>();
			mListOfPrevImages.add(mImage0);
			mListOfPrevImages.add(mImage1);
			mListOfPrevImages.add(mImage2);
			mListOfPrevImages.add(mImage3);
			mListOfPrevImages.add(mImage4);
			
			// Add Ttles to a new list
			mListOfTextTitles = new ArrayList<String>();
			mListOfTextTitles.add(mTextTitle0);
			mListOfTextTitles.add(mTextTitle1);
			mListOfTextTitles.add(mTextTitle2);
			mListOfTextTitles.add(mTextTitle3);
			mListOfTextTitles.add(mTextTitle4);
			
			// Add Body Text to a new list
			mListOfTextBody = new ArrayList<String>();
			mListOfTextBody.add(mTextBody0);
			mListOfTextBody.add(mTextBody1);
			mListOfTextBody.add(mTextBody2);
			mListOfTextBody.add(mTextBody3);
			mListOfTextBody.add(mTextBody4);
	}

	private class AppTourDetailsAdapter extends FragmentPagerAdapter {

		public static final int NUM_ITEMS = 5;

		public AppTourDetailsAdapter(FragmentManager fm, Context context) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return AppTourFragment.newInstance(mListOfPrevImages.get(position), 
											   mListOfTextTitles.get(position),
											   mListOfTextBody.get(position));
		}

		@Override
		public int getCount() {
			return NUM_ITEMS;
		}
	}

}