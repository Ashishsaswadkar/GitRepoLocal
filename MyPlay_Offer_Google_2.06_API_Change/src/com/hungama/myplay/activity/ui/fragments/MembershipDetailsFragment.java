/**
 * 
 */
package com.hungama.myplay.activity.ui.fragments;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.campaigns.util.Util;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Plan;
import com.hungama.myplay.activity.data.dao.hungama.PlanType;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionType;
import com.hungama.myplay.activity.gigya.GigyaManager;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionOperation;
import com.hungama.myplay.activity.ui.SettingsActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.util.Utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author DavidSvilem
 * 
 */
public class MembershipDetailsFragment extends Fragment implements
		CommunicationOperationListener {

	public final String TAG = "MembershipDetailsFragment";

	public static final String SUCCESS = "1";

	// Managers
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	// Views
	private TextView planDate;
	private ProgressDialog mProgressDialog;
	private Button unsubscribeButton;
	private Plan plan;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Fetch the root view
		View rootView;
		if (mApplicationConfigurations.isUserHasSubscriptionPlan()) {
			rootView = inflater.inflate(R.layout.fragment_membership_details,
					container, false);
			initializeMembershipPage(rootView);
		} else {
			rootView = inflater.inflate(
					R.layout.fragment_no_membership_details, container, false);
			initializeUpgradePage(rootView);
		}

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		AccountManager accountManager = AccountManager.get(getActivity()
				.getApplicationContext());
		Account[] accounts = accountManager.getAccountsByType("com.google");

		String accountType = null;
		if (accounts != null && accounts.length > 0) {
			accountType = accounts[0].name;
		}
		mDataManager.getCurrentSubscriptionPlan(this, accountType);
		
		if(planDate != null){
			planDate.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onStart(int operationId) {
		showLoadingDialog(getActivity().getResources().getString(
				R.string.processing));
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {

		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK:

			if(planDate != null){
				planDate.setVisibility(View.VISIBLE);
			}
			
			SubscriptionCheckResponse subscriptionCheckResponse = (SubscriptionCheckResponse) responseObjects
					.get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);

			
			if (subscriptionCheckResponse != null
					&& subscriptionCheckResponse.getPlan() != null) {
				mApplicationConfigurations.setIsUserHasSubscriptionPlan(true);
			} else {
				mApplicationConfigurations.setIsUserHasSubscriptionPlan(false);
			}
			
			plan = subscriptionCheckResponse.getPlan();
			String date = "";
			if (plan != null) {
				date = plan.getPurchaseDate();
			}

			if (planDate != null
					&& mApplicationConfigurations.isUserHasSubscriptionPlan()) {
				
				String autoRenew = "";
				
				List<Plan> subscriptionPlans = mDataManager.getStoredSubscriptionPlans();
				
				String planType = "";
				
				if(subscriptionPlans != null){
					for(Plan p : subscriptionPlans){
						if(p.getPlanId() == plan.getPlanId()){
							planType = p.getType();
							break;
						}
					}	
				}
				
				if(planType.equalsIgnoreCase(PlanType.GOOGLE.toString())){
					// Invisible "auto-renew" string
					autoRenew = getString(R.string.auto_renew);
				}else{
					// Invisible the button
					unsubscribeButton.setVisibility(View.GONE);
				}
				
				// We need to show only yyyy/mm/dd from the date
				Date d = Utils.convertTimeStampToDate(date);

				String dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM).format(d);
				
				planDate.setText(getString(R.string.membership_date, dateFormat , autoRenew));
				
			}

			hideLoadingDialog();

			break;

		case OperationDefinition.Hungama.OperationId.SUBSCRIPTION:

			SubscriptionResponse subscriptionResponse = (SubscriptionResponse) responseObjects
					.get(SubscriptionOperation.RESPONSE_KEY_SUBSCRIPTION);

			if (subscriptionResponse != null
					&& subscriptionResponse.getCode().equalsIgnoreCase(SUCCESS)) {

				unsubscribeButton.setVisibility(View.GONE);
				// Toast.makeText(getActivity().getApplicationContext(),
				// subscriptionResponse.getMessage(), Toast.LENGTH_LONG).show();

			} else {
				unsubscribeButton.setEnabled(true);
				// Toast.makeText(getActivity().getApplicationContext(),
				// subscriptionResponse.getMessage(), Toast.LENGTH_LONG).show();
			}

			hideLoadingDialog();

			break;

		default:
			break;
		}

	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {

		hideLoadingDialog();

	}

	public void initializeMembershipPage(View rootView) {
		planDate = (TextView) rootView.findViewById(R.id.membership_plan_date);
		unsubscribeButton = (Button) rootView
				.findViewById(R.id.upgrade_button_unsubscribe);
		unsubscribeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((Button) v).setEnabled(false);
				if (plan != null) {
					String code = mApplicationConfigurations
							.getSubscriptionIABcode();
					String purchaseToken = mApplicationConfigurations
							.getSubscriptionIABpurchseToken();
					AccountManager accountManager = AccountManager
							.get(getActivity().getApplicationContext());
					Account[] accounts = accountManager
							.getAccountsByType("com.google");

					if (accounts != null && accounts.length > 0) {
						String accountType = accounts[0].name;

						mDataManager.getSubscriptionCharge(plan.getPlanId(),
								plan.getType(), SubscriptionType.UNSUBSCRIBE,
								MembershipDetailsFragment.this, code,
								purchaseToken, accountType);
					}
				}
			}
		});
	}

	public void initializeUpgradePage(View rootView) {
		unsubscribeButton = (Button) rootView
				.findViewById(R.id.upgrade_button_subscribe);
		unsubscribeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Check if there are google accounts on the device
//				AccountManager accountManager = AccountManager
//						.get(getActivity());
//				Account[] accounts = accountManager
//						.getAccountsByType("com.google");
//				if (accounts == null || accounts.length == 0) {
//					showDialog(
//							getActivity()
//									.getString(R.string.subscription_error),
//							getActivity()
//									.getString(
//											R.string.there_is_no_google_account_on_this_device));
//					return;
//				}

				((Button) v).setEnabled(false);
				Intent intent = new Intent(getActivity(), UpgradeActivity.class);
				startActivityForResult(intent, 0);

			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SettingsActivity.LOGIN_ACTIVITY_CODE
				&& resultCode == SettingsActivity.RESULT_OK) {
			// // checks for valid session.
			// String session =
			// mDataManager.getApplicationConfigurations().getSessionID();
			// if (!TextUtils.isEmpty(session)) {
			// mDataManager.getCurrentSubscriptionPlan(this);
			// }
			// mDataManager.getCurrentSubscriptionPlan(this);
		}
		AccountManager accountManager = AccountManager.get(getActivity()
				.getApplicationContext());
		Account[] accounts = accountManager.getAccountsByType("com.google");

		String accountType = null;
		if (accounts != null && accounts.length > 0) {
			accountType = accounts[0].name;
		}
		mDataManager.getCurrentSubscriptionPlan(this, accountType);

		getActivity().getSupportFragmentManager().popBackStack();
		addMembershipDetailsFragment();
	};

	public void addMembershipDetailsFragment() {
		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		MembershipDetailsFragment membershipDetailsFragment = new MembershipDetailsFragment();
		fragmentTransaction.replace(R.id.main_fragmant_container,
				membershipDetailsFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	// Dialog help methods
	public void showLoadingDialog(String message) {
		if (!getActivity().isFinishing()) {
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(getActivity());
				mProgressDialog = ProgressDialog.show(getActivity(), "",
						message, true, true);
			}
		}
	}

	public void hideLoadingDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	private void showDialog(String title, String text) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getActivity());

		// set title
		alertDialogBuilder.setTitle(title);

		// set dialog message
		alertDialogBuilder
				.setMessage(text)
				.setCancelable(true)
				.setNegativeButton(R.string.exit_dialog_text_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

}
