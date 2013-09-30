package com.hungama.myplay.activity.ui;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.ui.fragments.FeedbackFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

public class FeedbackActivity extends SecondaryActivity {
	
	private static final String TAG = "FeedbackActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_with_title);
		
		// sets the Title.
		TextView title = (TextView) findViewById(R.id.main_title_bar_text);
		title.setText(R.string.feedback_title);
		
		// adds the feedback fragment.
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		
		FeedbackFragment feedbackFragment = new FeedbackFragment();
		fragmentTransaction.add(R.id.main_fragmant_container, feedbackFragment);
		fragmentTransaction.commit();
	}

}
