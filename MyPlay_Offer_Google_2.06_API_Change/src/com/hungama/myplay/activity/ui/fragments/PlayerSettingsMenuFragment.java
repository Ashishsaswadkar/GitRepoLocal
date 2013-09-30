package com.hungama.myplay.activity.ui.fragments;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class PlayerSettingsMenuFragment extends Fragment implements OnClickListener {

	public static final String TAG = "PlayerSettingsMenuFragment";
	
	public interface OnModeSelectedListener {
		
		public void onSleepModeSelected();
		
		public void onGymModeSelected();
	}
	
	public void setOnModeSelectedListener(OnModeSelectedListener listener) {
		mOnModeSelectedListener = listener;
	}
	
	private OnModeSelectedListener mOnModeSelectedListener;
	
	private Button sleepModeButon;
	private Button gymModeButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_gym_sleep_mode_menu, container, false);
		
		Button sleepModeButon = (Button) rootView.findViewById(R.id.sleep_mode);
		Button gymModeButton = (Button) rootView.findViewById(R.id.gym_mode);
		
		sleepModeButon.setOnClickListener(this);
		gymModeButton.setOnClickListener(this);
		
		return rootView;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.sleep_mode:
			
			getFragmentManager().popBackStack();
			
			// Show CountDownTimerDialog
			CountDownTimerDialog shareDialogFragment = CountDownTimerDialog.newInstance();
			
			FragmentManager mFragmentManager = getFragmentManager();
			shareDialogFragment.show(mFragmentManager, ShareDialogFragment.FRAGMENT_TAG);
			
			if (mOnModeSelectedListener != null) {
				mOnModeSelectedListener.onSleepModeSelected();
			}
			
			break;
			
		case R.id.gym_mode:
			
			getFragmentManager().popBackStack();
			
			if (mOnModeSelectedListener != null) {
				mOnModeSelectedListener.onGymModeSelected();
			}
			
			break;
		default:
			break;
		}
		
	}
}
