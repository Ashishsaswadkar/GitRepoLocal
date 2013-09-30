package com.hungama.myplay.activity.ui.fragments;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.catchmedia.SignOption;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupField;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.ui.DownloadActivity;
import com.hungama.myplay.activity.ui.LoginActivity;
import com.hungama.myplay.activity.ui.OnApplicationStartsActivity;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.SettingsActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class LoginFragment extends Fragment implements OnClickListener {
	
	private static final String TAG = "LoginFragment";
	
	
	// ======================================================
	// Public.
	// ======================================================
	
	/**
	 * Interface definition to be invoked when the user has selected one of the sign in / up options
	 * from the screen.
	 */
	public interface OnLoginOptionSelectedListener {
		
		/**
		 * Invoked when the user has selected to sign up / login with a social network.
		 * @param selectedSocialNetwork to connect with.
		 */
		public void onConnectWithSocialNetworkSelected(SocialNetwork selectedSocialNetwork);
		
		/**
		 * Invoked when the user has selected to login with Hungama.
		 */
		public void onLoginWithHungamaSelected(List<SignupField> signupFields);
		
		/**
		 * Invoked when the user clicked on the "forgot password" in the Hungama login fields.
		 */
		public void onLoginWithHungamaForgotPasswordSelected();
		
		/**
		 * Invoked when the user has selected to sign up.
		 */
		public void onSignUpSelected();
		
		/**
		 * Invoked when the user has selected to skip the process of login / sign up.
		 */
		public void onSkipSelected();
	}
	
	public void setSignOprions(List<SignOption> signOptions) {
		mSignOptions = signOptions;
	}
	
	/**
	 * Register a callback to be invoked when the user selected a signing option.
	 */
	public void setOnLoginOptionSelectedListener(OnLoginOptionSelectedListener listener) {
		mOnLoginOptionSelectedListener = listener;
	}
	
	
	// ======================================================
	// Fragment life cycle and listeners.
	// ======================================================
	
	private OnLoginOptionSelectedListener mOnLoginOptionSelectedListener;
	
	private List<SignOption> mSignOptions;
	
	private LinearLayout mHungamaLoginFieldsContainer;
	
	private RelativeLayout mButtonConnectFacebook;
	private RelativeLayout mButtonConnectTwitter;
	private RelativeLayout mButtonConnectGoogle;
	
	private Button mButtonHungamaLogin;
	private Button mButtonHungamaForgotPassword;
	
	private Button mButtonSignUp;
	private Button mButtonSkip;
	
	private LinearLayout subtitleTextLayout;
	private TextView titleText;
	private LinearLayout socialNetworkButtonsLayout;
	private RelativeLayout dividerLayout;
	
	private Bundle fromActivity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_login, container, false); 
		
			
		fromActivity = getArguments();
		
		initializeUserControls(rootView);
		
		if (!Utils.isListEmpty(mSignOptions)) {
			LoginActivity.buildTextFieldsFromSignupFields(mHungamaLoginFieldsContainer, 
												mSignOptions.get(0).getSignupFields());
		}
		
		return rootView;
	}
	
	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		
		if (viewId == R.id.login_button_facebook) {
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener.onConnectWithSocialNetworkSelected(SocialNetwork.FACEBOOK);
			}
		} else if (viewId == R.id.login_button_twitter) {
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener.onConnectWithSocialNetworkSelected(SocialNetwork.TWITTER);
			}
		} else if (viewId == R.id.login_button_google) {
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener.onConnectWithSocialNetworkSelected(SocialNetwork.GOOGLE);
			}
		} else if (viewId == R.id.login_hungama_login_button_login) {
			List<SignupField> signupFields = LoginActivity.generateSignupFieldsFromTextFields(mHungamaLoginFieldsContainer);
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener.onLoginWithHungamaSelected(signupFields);
			}
			
		} else if (viewId == R.id.login_hungama_login_button_forgot_password) {
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener.onLoginWithHungamaForgotPasswordSelected();
			}
		} else if (viewId == R.id.login_button_sign_up) {
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener.onSignUpSelected();
			}
		} else if (viewId == R.id.login_button_skip) {
			Logger.i(TAG, "Skipping.");
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener.onSkipSelected();
			}
		}
	}
	
	
	// ======================================================
	// Private helper methods.
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
		
		mButtonConnectFacebook.setOnClickListener(this);
		mButtonConnectTwitter.setOnClickListener(this);
		mButtonConnectGoogle.setOnClickListener(this);
		
		
		mHungamaLoginFieldsContainer = (LinearLayout) rootView.findViewById(R.id.login_hungama_login_fields_container);
		mButtonHungamaLogin = (Button) rootView.findViewById(R.id.login_hungama_login_button_login);
		mButtonHungamaForgotPassword = (Button) rootView.findViewById(R.id.login_hungama_login_button_forgot_password);
		mButtonSignUp = (Button) rootView.findViewById(R.id.login_button_sign_up);
		mButtonSkip = (Button) rootView.findViewById(R.id.login_button_skip);
		
		mButtonHungamaLogin.setOnClickListener(this);
		mButtonHungamaForgotPassword.setOnClickListener(this);
		mButtonSignUp.setOnClickListener(this);
		
		// initializes the titles / texts.
		titleText = (TextView) rootView.findViewById(R.id.title_text);
		subtitleTextLayout = (LinearLayout) rootView.findViewById(R.id.sub_title_text_layout);
		socialNetworkButtonsLayout = (LinearLayout) rootView.findViewById(R.id.social_network_buttons_layout);
		dividerLayout = (RelativeLayout) rootView.findViewById(R.id.divider_layout);

		// sets the title.
//		final SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getString(R.string.login_header_title_line_one));
//		final ForegroundColorSpan fcs = new ForegroundColorSpan(getResources().getColor(R.color.login_title_text_love_color)); 	
//		final ForegroundColorSpan fcsBegin = new ForegroundColorSpan(getResources().getColor(R.color.login_text_color));
//	    // Set the text color for love word
//	    sb.setSpan(fcs, 33, 37, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
//	    sb.setSpan(fcsBegin, 0, 32, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
//	    // make them also bold
//	    titleText.setText(sb);
		
		titleText.setText(R.string.login_header_title_line_one);
		
		/*
		 * The Login page can be shown from different contexts of the application
		 * toggles visibility of specific views based on it.
		 */
		if (fromActivity != null && (fromActivity.getString(UpgradeActivity.ARGUMENT_UPGRADE_ACTIVITY) != null
										|| fromActivity.getString(DownloadActivity.ARGUMENT_DOWNLOAD_ACTIVITY) != null)
										|| fromActivity.getString(ProfileActivity.ARGUMENT_PROFILE_ACTIVITY) != null) {
			mButtonSkip.setVisibility(View.GONE);			
		} else if (fromActivity != null && fromActivity.getString(OnApplicationStartsActivity.ARGUMENT_ON_APPLICATION_START_ACTIVITY) != null) {
			mButtonSkip.setVisibility(View.VISIBLE);
			mButtonSkip.setOnClickListener(this);
		} else if(fromActivity != null && fromActivity.getString(SettingsActivity.ARGUMENT_SETTINGS_ACTIVITY) != null){
			mButtonSkip.setVisibility(View.GONE);
			titleText.setVisibility(View.GONE);
			subtitleTextLayout.setVisibility(View.GONE);
			mButtonConnectFacebook.setVisibility(View.GONE);
			socialNetworkButtonsLayout.setVisibility(View.GONE);
			dividerLayout.setVisibility(View.GONE);
		}
	}
		
}
