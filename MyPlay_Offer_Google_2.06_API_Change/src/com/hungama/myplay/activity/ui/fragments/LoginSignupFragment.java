package com.hungama.myplay.activity.ui.fragments;

import java.util.List;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupField;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.ui.LoginActivity;
import com.hungama.myplay.activity.ui.SettingsActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LoginSignupFragment extends Fragment implements OnClickListener {
	
	
	// ======================================================
	// Public.
	// ======================================================
	
	/**
	 * Interface definition to be invoked when the user has selected one of the signup options
	 * from the screen.
	 */
	public interface OnSignupOptionSelectedListener {
		
		/**
		 * Invoked when the user has selected to sign up / login with a social network.
		 * @param selectedSocialNetwork to connect with.
		 */
		public void onConnectWithSocialNetworkSelected(SocialNetwork selectedSocialNetwork);
		
		/**
		 * Invoked when the user has selected to perform the signup.
		 */
		public void onPerformSignup(List<SignupField> signupFields);
		
		/**
		 * Invoked when the user has selected to login from the signup page.
		 */
		public void onPerformLogin();
	}
	
	public void setOnSignupOptionSelectedListener(OnSignupOptionSelectedListener listener) {
		mOnSignupOptionSelectedListener = listener;
	}
	
	public void setSignupFields(List<SignupField> signupFields) {
		mSignupFields = signupFields;
	}
	
	// ======================================================
	// Activity life cycle.
	// ======================================================
	
	private OnSignupOptionSelectedListener mOnSignupOptionSelectedListener;
	
	private List<SignupField> mSignupFields;
	
	private LinearLayout mSignupFieldsContainer;
	
	private RelativeLayout mButtonConnectFacebook;
	private RelativeLayout mButtonConnectTwitter;
	private RelativeLayout mButtonConnectGoogle;
	private Button mButtonSignup;
	private Button mButtonLogin;
	
	private Bundle fromActivity;
	private LinearLayout socialNetworkButtonsLayout;
	private RelativeLayout dividerLayout;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_login_signup, container, false);
		
		fromActivity = getArguments();
		
		initializeUserControls(rootView);
		
		LoginActivity.buildTextFieldsFromSignupFields(mSignupFieldsContainer, mSignupFields);
		
		return rootView;
	}
	
	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		
		case R.id.login_button_facebook:
			if (mOnSignupOptionSelectedListener != null) {
				mOnSignupOptionSelectedListener.onConnectWithSocialNetworkSelected(SocialNetwork.FACEBOOK);
			}
			break;
			
		case R.id.login_button_twitter:
			if (mOnSignupOptionSelectedListener != null) {
				mOnSignupOptionSelectedListener.onConnectWithSocialNetworkSelected(SocialNetwork.TWITTER);
			}
			break;
			
		case R.id.login_button_google:
			if (mOnSignupOptionSelectedListener != null) {
				mOnSignupOptionSelectedListener.onConnectWithSocialNetworkSelected(SocialNetwork.GOOGLE);
			}
			break;
	
		case R.id.login_signup_button_signup:
			List<SignupField> signupFields = LoginActivity.generateSignupFieldsFromTextFields(mSignupFieldsContainer);

			if (mOnSignupOptionSelectedListener != null) {
				mOnSignupOptionSelectedListener.onPerformSignup(signupFields);
			}
			
			break;
	
		case R.id.login_signup_button_login:
			if (mOnSignupOptionSelectedListener != null) {
				mOnSignupOptionSelectedListener.onPerformLogin();
			}
			break;
		default:
			break;
		}
	}
	
	
	// ======================================================
	// Private Helper method.
	// ======================================================
	
	private void initializeUserControls(View rootView) {
		
		// initializes the Social networks buttons.
		mButtonConnectFacebook = (RelativeLayout) rootView.findViewById(R.id.login_button_facebook);
		mButtonConnectTwitter = (RelativeLayout) rootView.findViewById(R.id.login_button_twitter);
		mButtonConnectGoogle = (RelativeLayout) rootView.findViewById(R.id.login_button_google);
		
		// generates their titles.
		Resources resources = getResources();
		TextView facebookTitle = (TextView) mButtonConnectFacebook.findViewById(R.id.login_button_facebook_title);
		facebookTitle.setText(Html.fromHtml(resources.getString(R.string.login_facebook_title)));
		
		mButtonSignup = (Button) rootView.findViewById(R.id.login_signup_button_signup);
		mButtonLogin = (Button) rootView.findViewById(R.id.login_signup_button_login);
		
		mSignupFieldsContainer = (LinearLayout) rootView.findViewById(R.id.login_signup_fields_contailner);
		
		mButtonConnectFacebook.setOnClickListener(this);
		mButtonConnectTwitter.setOnClickListener(this);
		mButtonConnectGoogle.setOnClickListener(this);
		mButtonSignup.setOnClickListener(this);
		mButtonLogin .setOnClickListener(this);
		
		socialNetworkButtonsLayout = (LinearLayout) rootView.findViewById(R.id.social_network_buttons_layout);
		dividerLayout = (RelativeLayout) rootView.findViewById(R.id.divider_layout);
		
		if(fromActivity != null && fromActivity.getString(SettingsActivity.ARGUMENT_SETTINGS_ACTIVITY) != null){
			mButtonConnectFacebook.setVisibility(View.GONE);
			socialNetworkButtonsLayout.setVisibility(View.GONE);
			dividerLayout.setVisibility(View.GONE);
		}
	}

}