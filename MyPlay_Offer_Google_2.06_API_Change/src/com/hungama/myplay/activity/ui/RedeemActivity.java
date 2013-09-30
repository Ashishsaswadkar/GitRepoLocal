package com.hungama.myplay.activity.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.ui.fragments.RedeemFragment;

public class RedeemActivity extends MainActivity {

	public static final int FROM_MAIN_MENU = -1 ;
	public static final String ARGUMENT_REDEEM = "argument_redeem";
	
	// ======================================================
	// ACTIVITY'S LIFECYCLE.
	// ======================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		setContentView(R.layout.activity_redeem);
		
		// SetS title bar
		TextView mTitleBarText = (TextView) findViewById(R.id.main_title_bar_text);
		mTitleBarText.setText(getResources().getString(R.string.redeem_title));	
		
		ImageButton arrow = (ImageButton) findViewById(R.id.main_title_bar_button_options);
		arrow.setVisibility(View.GONE);
		
		Bundle data = new Bundle();
		data.putInt(ARGUMENT_REDEEM, FROM_MAIN_MENU);
		addFragment(data);
		
	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}
	
	// ======================================================
	// Helper Methods.
	// ======================================================
	
	public void addFragment(Bundle detailsData) {
		
		RedeemFragment mRedeemFragment = new RedeemFragment();
		mRedeemFragment.setArguments(detailsData);
//		mMediaTileGridFragment.setOnMediaItemOptionSelectedListener(this);
		
		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		fragmentTransaction.add(R.id.main_fragmant_container, mRedeemFragment);
		fragmentTransaction.commit();
	}	
}