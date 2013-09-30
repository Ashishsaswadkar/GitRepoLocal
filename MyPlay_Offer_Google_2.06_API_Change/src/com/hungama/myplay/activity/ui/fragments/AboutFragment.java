package com.hungama.myplay.activity.ui.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.ui.AboutActivity;

public class AboutFragment extends Fragment {
	
	private static final String TAG = "AboutFragment";
	
	private DataManager mDataManager;
	
	private String mFacebookUrl;
	private String mTwitterUrl;
	private String mGoogleUrl;
	private String mTermsUrl;
	private String mPrivacyUrl;
	
	private TextView mTextTitle;
	private ImageButton mButtonFacebook;
	private ImageButton mButtonTwitter;
	private ImageButton mButtonGooglePlus;
	
	private Button mButtonTerms;
	private Button mButtonPrivacy;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		
		Resources resources = getResources();
		
		mFacebookUrl = resources.getString(R.string.hungama_server_url_follow_facebook);
		mTwitterUrl = resources.getString(R.string.hungama_server_url_follow_twitter);
		mGoogleUrl = resources.getString(R.string.hungama_server_url_follow_google);
		mTermsUrl = resources.getString(R.string.hungama_server_url_term_of_use);
		mPrivacyUrl = resources.getString(R.string.hungama_server_url_privacy_policy);
		 
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		FlurryAgent.onEndSession(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_about, container, false);
		
		// initializes and populates the views.
		
		mTextTitle = (TextView) rootView.findViewById(R.id.about_title_version);
		mButtonFacebook = (ImageButton) rootView.findViewById(R.id.about_button_facebook);
		mButtonTwitter = (ImageButton) rootView.findViewById(R.id.about_button_twitter);
		mButtonGooglePlus = (ImageButton) rootView.findViewById(R.id.about_button_google_plus);
		
		mButtonTerms = (Button) rootView.findViewById(R.id.about_button_terms);
		mButtonPrivacy = (Button) rootView.findViewById(R.id.about_button_privacy);
		
		String appVersion = mDataManager.getServerConfigurations().getAppVersion();
		String title = getResources().getString(R.string.about_title_version, appVersion);
		mTextTitle.setText(title);
		
		mButtonFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(mFacebookUrl));
				startActivity(i);
			}
		});
		mButtonTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(mTwitterUrl));
				startActivity(i);
			}
		});
		mButtonGooglePlus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(mGoogleUrl));
				startActivity(i);
			}
		});
		
		mButtonTerms.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showWebviewPage(mTermsUrl);
			}
		});
		
		mButtonPrivacy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showWebviewPage(mPrivacyUrl);
			}
		});
		
		return rootView;
	}
	
	private void showWebviewPage(String url) {
		((AboutActivity) getActivity()).showWebviewPage(url);
	}
}
