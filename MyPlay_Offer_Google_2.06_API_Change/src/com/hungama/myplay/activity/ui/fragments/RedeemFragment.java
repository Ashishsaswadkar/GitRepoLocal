package com.hungama.myplay.activity.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.ui.RedeemActivity;
import com.hungama.myplay.activity.util.Logger;

public class RedeemFragment extends Fragment implements OnClickListener {
	
	
	private View rootView;
	private Bundle detailsData;
	private int numOfCoins;
	private TextView mTextViewHungamaLink, mTextViewSendEmail;
	
	// ======================================================
	// Fragment lifecycle methods
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);			
	}
	
	@Override
	public void onStart() {
		
		super.onStart();
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
		FlurryAgent.logEvent("Rewards");
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
	
	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		
		if (v.equals(mTextViewSendEmail)) {
			
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("plain/text");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[] { getActivity().getResources().getString(R.string.redeem_email_to) });
			intent.putExtra(Intent.EXTRA_SUBJECT, (String) getActivity().getString(R.string.redeem_email_subject));
			startActivity(Intent.createChooser(intent, "")); 
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.fragment_redeem_new, container, false);
		LinearLayout myCoinsTitle = (LinearLayout) rootView.findViewById(R.id.left_invite_text_title);
		
		detailsData = getArguments();
		numOfCoins = detailsData.getInt(RedeemActivity.ARGUMENT_REDEEM);
		
		if (numOfCoins == RedeemActivity.FROM_MAIN_MENU) {
			
			myCoinsTitle.setVisibility(View.GONE);
			
		} else {
			
			myCoinsTitle.setVisibility(View.VISIBLE);
			TextView numCoins = (TextView) rootView.findViewById(R.id.free_song_text);
			numCoins.setText(getResources().getString(R.string.redeem_my_coins, numOfCoins));
		}

		mTextViewHungamaLink = (TextView) rootView.findViewById(R.id.text_view_hungama_title);
		if (mTextViewHungamaLink != null) {
			
			mTextViewHungamaLink.setMovementMethod(LinkMovementMethod.getInstance());
		}
		
		mTextViewSendEmail = (TextView) rootView.findViewById(R.id.text_view_email);
		mTextViewSendEmail.setOnClickListener(this);
		
		Logger.i("RedeemFragment", rootView.toString());
		return rootView;
	}
}
