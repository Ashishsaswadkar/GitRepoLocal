package com.hungama.myplay.activity.gigya;

import java.io.Serializable;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment.OnTwitterLoginListener;

public class TwitterLoginActivity extends FragmentActivity implements OnTwitterLoginListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main_with_title_without_player_overlay);
		
		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		Bundle b = getIntent().getExtras();
		
		TwitterLoginFragment fragment = 
				new TwitterLoginFragment(
						(Map<String, Object>) b.getSerializable("signup_fields"), 
						b.getLong("set_id", 0));
		fragmentTransaction.replace(R.id.main_fragmant_container, fragment, TwitterLoginFragment.FRAGMENT_TWITTER_LOGIN);
		fragmentTransaction.commit();
		
		// Listen to result from TwitterLoinFragment
		fragment.setOnTwitterLoginListener(this);
	}

	@Override
	public void onTwitterLoginListener(TwitterLoginFragment fragment,
			Map<String, Object> signupFields, long setId) {
		
		Intent data = new Intent();
		Bundle b = new Bundle();
		b.putSerializable("signup_fields", (Serializable) signupFields);
		b.putLong("set_id", setId);
		data.putExtras(b);
		setResult(200, data);
		finish();
	}

	@Override
	public void onCancelLoginListener() {
		setResult(500);
	}

	@Override
	public void onBackPressed() {
		// checks if the webview exists and calls its back button support.
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(TwitterLoginFragment.FRAGMENT_TWITTER_LOGIN);
		if (fragment != null) {
			TwitterLoginFragment twitterLoginFragment = (TwitterLoginFragment) fragment;
			twitterLoginFragment.onBackPressed();
			return;			
		}
		
		super.onBackPressed();
	}
}
