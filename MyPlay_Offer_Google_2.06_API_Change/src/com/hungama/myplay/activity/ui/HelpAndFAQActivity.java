package com.hungama.myplay.activity.ui;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.ui.fragments.AboutWebViewFragment;
import com.hungama.myplay.activity.ui.fragments.FeedbackFragment;
import com.hungama.myplay.activity.ui.fragments.HelpAndFAQFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

public class HelpAndFAQActivity extends SecondaryActivity {
	
	private FragmentManager mFragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_with_title);
		
		mFragmentManager = getSupportFragmentManager();;
		
		// sets the Title.
		TextView title = (TextView) findViewById(R.id.main_title_bar_text);
		title.setText(R.string.help_faq_title);
		
		// adds the feedback fragment.
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		
		HelpAndFAQFragment feedbackFragment = new HelpAndFAQFragment();
		fragmentTransaction.add(R.id.main_fragmant_container, feedbackFragment);
		fragmentTransaction.commit();
	}
	
	@Override
	public void onBackPressed() {
		// checks if the webview exists and calls its back button support.
		Fragment fragment = mFragmentManager.findFragmentByTag(AboutWebViewFragment.FRAGMENT_WEBVIEW);
		if (fragment != null) {
			AboutWebViewFragment aboutWebViewFragment = (AboutWebViewFragment) fragment;
			boolean hasSupported = aboutWebViewFragment.onBackPressed();
			if (hasSupported) {
				return;
			}
		}
		
		super.onBackPressed();
	}
	
	public void showWebviewPage(String url) {
		
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		
		AboutWebViewFragment aboutWebViewFragment = new AboutWebViewFragment();
		
		Bundle arguments = new Bundle();
		arguments.putString(AboutWebViewFragment.FRAGMENT_ARGUMENT_URL, url);
		aboutWebViewFragment.setArguments(arguments);
		
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		fragmentTransaction.replace(R.id.main_fragmant_container, aboutWebViewFragment, AboutWebViewFragment.FRAGMENT_WEBVIEW);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	public void showFeedbackPage() {
		// adds the feedback fragment.
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		FeedbackFragment feedbackFragment = new FeedbackFragment();
		fragmentTransaction.replace(R.id.main_fragmant_container, feedbackFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
}
