package com.hungama.myplay.activity.ui.fragments;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.BadgesAndCoins;
import com.hungama.myplay.activity.ui.BadgesAndCoinsActivity;

/**
 * Loads and shows the given the User's favorites MediaItems.
 */
public class BadgesAndCoinsFragment extends Fragment {
	
	private static final String TAG = "BadgesAndCoinsFragment";
	
	public static final String FRAGMENT_ARGUMENT_BADGES_AND_COINS = "fragment_argument_badges_and_coins";
	
	public static final int TWO_LINES_NOTIFICATION = 2;
	public static final int THREE_LINES_NOTIFICATION = 3;
	
	public interface OnNotificationFinishedListener {
		
		public void onNotificationFinishedListener();
	}
	
	public void setOnNotificationFinishedListener(OnNotificationFinishedListener listener) {
		mOnNotificationFinishedListener = listener;
	}
	
	private Bundle detailsData;
	private View rootView;
	private BadgesAndCoins mBadgesAndCoins;
	private LinearLayout coinsNotification;
	
	private OnNotificationFinishedListener mOnNotificationFinishedListener;
	
	private static WeakReference<BadgesAndCoinsActivity> mWRActivity = null;

	// ======================================================
	// FRAGMENT'S LIFECYCLE.
	// ======================================================
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		detailsData = getArguments();
		mWRActivity = new WeakReference<BadgesAndCoinsActivity>((BadgesAndCoinsActivity) getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.badge_coins_notification, container, false);
		
		mBadgesAndCoins = (BadgesAndCoins) detailsData.getSerializable(FRAGMENT_ARGUMENT_BADGES_AND_COINS);
		if (mBadgesAndCoins != null) {
			if (mBadgesAndCoins.getDisplayCase() == BadgesAndCoins.CASE_COINS_2_LINES) {				
				initializeCoinsNotification(TWO_LINES_NOTIFICATION);
			} else if (mBadgesAndCoins.getDisplayCase() == BadgesAndCoins.CASE_COINS_3_LINES) {
				initializeCoinsNotification(THREE_LINES_NOTIFICATION);
			} else if (mBadgesAndCoins.getDisplayCase() == BadgesAndCoins.CASE_COINS_2_LINES_AND_BADGE) {
				initializeCoinsNotification(TWO_LINES_NOTIFICATION);
			} else if (mBadgesAndCoins.getDisplayCase() == BadgesAndCoins.CASE_COINS_3_LINES_AND_BADGE) {
				initializeCoinsNotification(THREE_LINES_NOTIFICATION);
			}
		}
		return rootView;
	}
	
	// ======================================================
	// Helper Method.
	// ======================================================
	private void initializeCoinsNotification(int numOfLines) {
		
		//set top text
		TextView topText = (TextView) rootView.findViewById(R.id.coins_notification_text_top);
		topText.setText(mBadgesAndCoins.getMessage());
		
		//set number of cois earned
		TextView numOfCoinsEarned = (TextView) rootView.findViewById(R.id.coins_image);
		numOfCoinsEarned.setText(String.valueOf(mBadgesAndCoins.getPointsEarned()));
		
		LinearLayout nextDescriptionLine = (LinearLayout) rootView.findViewById(R.id.coins_notification_bottom);		
		if (numOfLines == TWO_LINES_NOTIFICATION) {			
			nextDescriptionLine.setVisibility(ViewGroup.GONE);			
		} else {
			nextDescriptionLine.setVisibility(ViewGroup.VISIBLE);
			TextView nextDescriptionText = (TextView) rootView.findViewById(R.id.coins_notification_bottom_text);
			nextDescriptionText.setText(mBadgesAndCoins.getNextDescription());
		}
		
		coinsNotification = (LinearLayout) rootView.findViewById(R.id.coins_notification);
		
		final CountDownTimer countDownTimer = new CountDownTimer(4000, 1000) {

		     public void onTick(long millisUntilFinished) { }

		     public void onFinish() {
		    	 
		    	 this.cancel();
		    	 
		    	 if ((BadgesAndCoinsActivity) getActivity() != null && !((BadgesAndCoinsActivity) getActivity()).isActivityDestroyed()) {
		    		 
		    		 new Handler().post(new Runnable() {
						
						@Override
						public void run() {
							
							getActivity().getSupportFragmentManager().popBackStack();
							((BadgesAndCoinsActivity) getActivity()).getSupportFragmentManager().beginTransaction().remove(BadgesAndCoinsFragment.this).commitAllowingStateLoss();
//							((BadgesAndCoinsActivity) getActivity()).getSupportFragmentManager().beginTransaction().remove(BadgesAndCoinsFragment.this).commit();							
						}
					});		    		 
		    	 }
		    	 
		    	 if (mOnNotificationFinishedListener != null && (mBadgesAndCoins.getDisplayCase() == BadgesAndCoins.CASE_COINS_2_LINES_AND_BADGE ||
		    			 	mBadgesAndCoins.getDisplayCase() == BadgesAndCoins.CASE_COINS_3_LINES_AND_BADGE)) {
		    		 
						mOnNotificationFinishedListener.onNotificationFinishedListener();
						
		    	 } else if ((BadgesAndCoinsActivity) getActivity() != null) {
		    		 
		    		 ((BadgesAndCoinsActivity) getActivity()).finish(); 
		    	 }		    		 		    	 
		     }
		     
		  }.start();
		
		  coinsNotification.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					countDownTimer.cancel();
					if ((BadgesAndCoinsActivity) getActivity() != null) {
						getFragmentManager().popBackStack();
						((BadgesAndCoinsActivity) getActivity()).getSupportFragmentManager().beginTransaction().remove(BadgesAndCoinsFragment.this).commit();
					}
					if (mOnNotificationFinishedListener != null && 
			    			 (mBadgesAndCoins.getDisplayCase() == BadgesAndCoins.CASE_COINS_2_LINES_AND_BADGE ||
			    			 	mBadgesAndCoins.getDisplayCase() == BadgesAndCoins.CASE_COINS_3_LINES_AND_BADGE)) {
						mOnNotificationFinishedListener.onNotificationFinishedListener();
					} else if ((BadgesAndCoinsActivity) getActivity() != null) {
			    			 ((BadgesAndCoinsActivity) getActivity()).finish(); 			    		 
			    		}
				}
			});
		
		
	}	

}
