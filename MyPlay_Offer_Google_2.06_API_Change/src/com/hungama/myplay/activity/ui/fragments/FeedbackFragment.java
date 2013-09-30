package com.hungama.myplay.activity.ui.fragments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.FeedbackSubjectsOperation;
import com.hungama.myplay.activity.util.Utils;

public class FeedbackFragment extends Fragment implements CommunicationOperationListener {
	
	private static final String TAG = "FeedbackFragment";
	
	private DataManager mDataManager;
	
	private ProgressDialog mProgressDialog;
	
	private EditText mTextFirstName;
	private EditText mTextLastName;
	private EditText mTextEmail;
	private EditText mTextMobile;
	private EditText mTextFreeText;
	private RatingBar mRatingBar;
	private Button mButtonSubmit;
	
	private Spinner mSpinnerSubjects;
	private ArrayAdapter<String> mSpinnerAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_feedback, container, false);
		
		// initialize the user controls.
		mTextFirstName = (EditText) rootView.findViewById(R.id.feedback_field_first_name);
		mTextLastName = (EditText) rootView.findViewById(R.id.feedback_field_last_name);
		mTextEmail = (EditText) rootView.findViewById(R.id.feedback_field_email);
		mTextMobile = (EditText) rootView.findViewById(R.id.feedback_field_mobile_number);
		mTextFreeText = (EditText) rootView.findViewById(R.id.feedback_field_free_text);
		mRatingBar = (RatingBar) rootView.findViewById(R.id.feedback_field_rating_bar);
		mButtonSubmit = (Button) rootView.findViewById(R.id.feedback_button_submit);
		
		mSpinnerSubjects = (Spinner) rootView.findViewById(R.id.feedback_field_spinner_subjects);
		mSpinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_feedback_subject, R.id.feedback_subject_id);
		
		mSpinnerSubjects.setAdapter(mSpinnerAdapter);
		
		mButtonSubmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				submitFields();
			}
		});
		
		mRatingBar.setMax(5);
		mRatingBar.setStepSize(1.0f);
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		mDataManager.getFeedbackSubjects(this);
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onStop()
	 */
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FlurryAgent.onEndSession(getActivity());
	}
	
	@Override
	public void onStart(int operationId) {
		showLoadingDialog(getResources().getString(R.string.application_dialog_loading));
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.FEEDBACK_SUBJECTS) {
			
			// populates the subjects spinner.
			List<String> subjects = (List<String>) 
					responseObjects.get(FeedbackSubjectsOperation.RESULT_OBJECT_SUBJECTS_LIST);
			// casting it to make it been supported thru all versions.
			for (String string : subjects) {
				mSpinnerAdapter.add(string);
			}
			
			populateUserControls();
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.FEEDBACK_SUBMIT) {
			
		}
		
		hideLoadingDialog();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		if (errorType != ErrorType.OPERATION_CANCELLED) {
			Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
		}
		
		hideLoadingDialog();
	}
	
	private void populateUserControls() {
		/*
		 * Checks if the given user is signed to the application,
		 * if so, tries to pull his information to populate the fields.
		 */
		ApplicationConfigurations applicationConfigurations = mDataManager.getApplicationConfigurations();
		if (applicationConfigurations.isRealUser()) {
			DeviceConfigurations deviceConfigurations = mDataManager.getDeviceConfigurations();
			
			String firstName = applicationConfigurations.getGigyaFBFirstName();
			String lastName = applicationConfigurations.getGigyaFBLastName();
			String email = applicationConfigurations.getExistingDeviceEmail();
			String mobile = deviceConfigurations.getDevicePhoneNumber();
			
			if (!TextUtils.isEmpty(firstName)) {
				mTextFirstName.setText(firstName);
			}
			
			if (!TextUtils.isEmpty(lastName)) {
				mTextLastName.setText(lastName);			
			}
			
			if (!TextUtils.isEmpty(email)) {
				mTextEmail.setText(email);
			}
			
			if (!TextUtils.isEmpty(mobile)) {
				mTextMobile.setText(mobile);
			}
		} // else - do nothing, there are the hints!
	}
	
	private void submitFields() {
		String firstName = "";
		String lastName = "";
		String email = "";
		String subject = "";
		String freeText = "";
		String mobile = "";
		String rate = "";
		
		// first name.
		if (mTextFirstName.getText() != null && !TextUtils.isEmpty(mTextFirstName.getText().toString())) {
			firstName = mTextFirstName.getText().toString();
		}
		// last name.
		if (mTextLastName.getText() != null && !TextUtils.isEmpty(mTextLastName.getText().toString())) {
			lastName = mTextLastName.getText().toString();
		}
		// email name.
		if (mTextEmail.getText() != null && !TextUtils.isEmpty(mTextEmail.getText().toString())) {
			email = mTextEmail.getText().toString();
		}
		// freeText
		if (mTextFreeText.getText() != null && !TextUtils.isEmpty(mTextFreeText.getText().toString())) {
			freeText = mTextFreeText.getText().toString();
		}
		// mobile
		if (mTextMobile.getText() != null && !TextUtils.isEmpty(mTextMobile.getText().toString())) {
			mobile = mTextMobile.getText().toString();
		}
		// subject
		subject = (String) mSpinnerSubjects.getSelectedItem();
		// rate
		rate = Float.toString(mRatingBar.getRating());
		
		/*
		 * validate fields.
		 */
		
		// checks for empty field.
		if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || 
			TextUtils.isEmpty(email) || TextUtils.isEmpty(freeText) || TextUtils.isEmpty(mobile)) {
			showErrorDialog(R.string.feedback_error_message_empty_field);
			return;
		}
		// checks for invalid email format.
		String validMailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
		if (!email.matches(validMailRegex)) {
			showErrorDialog(R.string.feedback_error_message_email_format);
			return;
		}
		
		String validMobileRegex = "[0-9]{10,10}";
		if (!mobile.matches(validMobileRegex)) {
			showErrorDialog(R.string.feedback_error_message_mobile_format);
			return;
		}
		
		// packs the submit fields and kicks them off to the server. 
		Map<String, String> submitParams = new HashMap<String, String>();
		
		ApplicationConfigurations applicationConfigurations = mDataManager.getApplicationConfigurations();
		ServerConfigurations serverConfigurations = mDataManager.getServerConfigurations();
		DeviceConfigurations deviceConfigurations = mDataManager.getDeviceConfigurations();
		
		StringBuilder phoneDetailsBuilder = new StringBuilder();
		phoneDetailsBuilder.append("model-").append(deviceConfigurations.getDeviceModelName()).append(" ,")
						   .append("systemName-").append(deviceConfigurations.getDeviceOS()).append(" ,")
						   .append("systemVersion-").append(Build.VERSION.SDK_INT).append(" ,")
						   .append("appVersion-").append(serverConfigurations.getAppVersion());
		
		if (applicationConfigurations.isRealUser()) {
			submitParams.put("user_id", applicationConfigurations.getPartnerUserId());
			submitParams.put("subject", subject);
			submitParams.put("app_exp", rate);
			submitParams.put("feed_txt", freeText);
			
		} else {
			submitParams.put("first_name", firstName);
			submitParams.put("last_name", lastName);
			submitParams.put("email", email);
			submitParams.put("mobile", mobile);
			submitParams.put("subject", subject);
			submitParams.put("app_exp", rate);
			submitParams.put("feed_txt", freeText);
			
		}
		
		submitParams.put("phone_details", phoneDetailsBuilder.toString());
		
		mDataManager.postFeedback(submitParams, this);
	}
	
	private void showErrorDialog(int errorMessageStringRcs) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setTitle(Utils.TEXT_EMPTY);
		alertDialogBuilder.setMessage(errorMessageStringRcs);
		alertDialogBuilder.setPositiveButton(R.string.feedback_error_message_confirm, 
											 new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = alertDialogBuilder.create();
		dialog.show();
	}
	
	public void showLoadingDialog(String message) {
		if (!getActivity().isFinishing()) {
    		if (mProgressDialog == null) {
    			mProgressDialog = new ProgressDialog(getActivity());
    			mProgressDialog = ProgressDialog.show(getActivity(), "", message, true, true);
    		}
    	}
	}
	
	public void hideLoadingDialog() {
		if (mProgressDialog != null) {
    		mProgressDialog.dismiss();
    		mProgressDialog = null;
		}
	}
	
}
