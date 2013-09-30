package com.hungama.myplay.activity.ui.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;

public class LoginWithSocialNetworkFragment extends Fragment {

	private static final String TAG = "LoginWithSocialNetworkFragment";
	
	
	// ======================================================
	// Public.
	// ======================================================
	
	/**
	 * Interface definition to be invoked when the user has clicked the "Submit" button
	 * and the email and password where filled.
	 */
	public interface OnSocialNetworkSubmitCredentialsListener {
		
		public void onSocialNetworkSubmitCredentials(SocialNetwork socialNetwork, String email, String password);
	}
	
	public void setSocialNetwork(SocialNetwork socialNetwork) {
		mSocialNetwork = socialNetwork;
	}
	
	/**
	 * Register a call-back to be invoked when the user has clicked the "Submit" button
	 * and the email and password where filled.
	 * @param listener
	 */
	public void setOnSocialNetworkSubmitCredentialsListener(OnSocialNetworkSubmitCredentialsListener listener) {
		mOnSocialNetworkSubmitCredentialsListener = listener;
	}
	
	
	// ======================================================
	// Activity life cycle.
	// ======================================================
	
	private TextView mTextTitle;
	private EditText mTextEmail;
	private EditText mTextPassword;
	private Button mButtonContinue;
	
	private SocialNetwork mSocialNetwork ;
	private OnSocialNetworkSubmitCredentialsListener mOnSocialNetworkSubmitCredentialsListener;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_login_social_network_login, container, false);
		initializeUserControls(rootView);
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		populateViews();
	}
	
	
	// ======================================================
	// Private Helper method.
	// ======================================================
	
	private void initializeUserControls(View rootView) {
		
		mTextTitle = (TextView) rootView.findViewById(R.id.main_title_bar_text);
		mTextEmail = (EditText) rootView.findViewById(R.id.login_social_network_login_text_email);
		mTextPassword = (EditText) rootView.findViewById(R.id.login_social_network_login_text_password);
		mButtonContinue = (Button) rootView.findViewById(R.id.login_social_network_login_button_submit);
		
		Resources resources = rootView.getResources();
		// fixes some properties of the text boxes.
		mTextEmail.setTextColor(resources.getColor(R.color.login_text_color));
		mTextEmail.setTextSize(resources.getDimensionPixelSize(R.dimen.login_field_text));
		mTextEmail.setHintTextColor(resources.getColor(R.color.login_hint));
		
		mTextPassword.setTextColor(resources.getColor(R.color.login_text_color));
		mTextPassword.setTextSize(resources.getDimensionPixelSize(R.dimen.login_field_text));
		mTextPassword.setHintTextColor(resources.getColor(R.color.login_hint));
	}
	
	private void populateViews() {
		Resources resources = getView().getResources();
		// sets the title.
		String title = resources.getString(R.string.login_social_network_login_title_prefix) + " " +
				resources.getString(mSocialNetwork.getLableResourceId());
		mTextTitle.setText(title);
		// handles when the user has clicked on the "submit".
		mButtonContinue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// closes the keyboard.
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mTextPassword.getWindowToken(), 0);
				// extracts the credentials from the text fields.
				String email = mTextEmail.getText() != null ? 
									mTextEmail.getText().toString() : null;
				String password = mTextPassword.getText() != null ? 
									mTextPassword.getText().toString() : null;
				if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
					if (mOnSocialNetworkSubmitCredentialsListener != null) {
						mOnSocialNetworkSubmitCredentialsListener.onSocialNetworkSubmitCredentials(mSocialNetwork, email, password);
					}
				}
			}
		});
		
	}
	
	
}
