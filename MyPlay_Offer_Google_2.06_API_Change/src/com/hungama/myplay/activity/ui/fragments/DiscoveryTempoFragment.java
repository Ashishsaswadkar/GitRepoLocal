package com.hungama.myplay.activity.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.Tempo;
import com.hungama.myplay.activity.ui.DiscoveryActivity;
import com.hungama.myplay.activity.util.Utils;

public class DiscoveryTempoFragment extends Fragment {
	
	private static final String TAG = "DiscoveryTempoFragment";
	
	private Button mButtonAuto;
	private Button mButtonLow;
	private Button mButtonMedium;
	private Button mButtonHigh;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_discovery_tempo, container, false);
		
		/*
		 * This fragment is a floating one above the tiles,
		 * to avoid the problem of touching the surface of it and the tiles
		 * are being touched, we catch the touches.
		 */
		RelativeLayout tempoContainer = (RelativeLayout) rootView.findViewById(R.id.discovery_tempo_container);
		tempoContainer.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		
		mButtonAuto = (Button) rootView.findViewById(R.id.discovery_tempo_button_auto);
		mButtonLow = (Button) rootView.findViewById(R.id.discovery_tempo_button_low);
		mButtonMedium = (Button) rootView.findViewById(R.id.discovery_tempo_button_medium);
		mButtonHigh = (Button) rootView.findViewById(R.id.discovery_tempo_button_high);
		
		mButtonAuto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				enableButton(mButtonAuto);
				
				disableButton(mButtonLow);
				disableButton(mButtonMedium);
				disableButton(mButtonHigh);
			}
		});
		mButtonLow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// disables the auto button.
				if (mButtonAuto.isSelected()){
					disableButton(mButtonAuto);
				}
				// toggles the button.
				if (mButtonLow.isSelected()){
					disableButton(mButtonLow);
				} else {
					enableButton(mButtonLow);
				}
			}
		});
		mButtonMedium.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// disables the auto button.
				if (mButtonAuto.isSelected()){
					disableButton(mButtonAuto);
				}
				// toggles the button.
				if (mButtonMedium.isSelected()){
					disableButton(mButtonMedium);
				} else {
					enableButton(mButtonMedium);
				}
			}
		});
		mButtonHigh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// disables the auto button.
				if (mButtonAuto.isSelected()){
					disableButton(mButtonAuto);
				}
				// toggles the button.
				if (mButtonHigh.isSelected()){
					disableButton(mButtonHigh);
				} else {
					enableButton(mButtonHigh);
				}			
			}
		});

		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// gets stored tempos.
		DiscoveryActivity activity = (DiscoveryActivity) getActivity();
		List<Tempo> tempos = activity.getDiscover().getTempos();
		if (Utils.isListEmpty(tempos) || tempos.contains(Tempo.AUTO)){
			// enables only the auto as default
			enableButton(mButtonAuto);
			disableButton(mButtonLow);
			disableButton(mButtonMedium);
			disableButton(mButtonHigh);
		} else {
			for (Tempo tempo : tempos) {
				if (tempo == Tempo.LOW) {
					enableButton(mButtonLow);
				} else if (tempo == Tempo.MEDIUM) {
					enableButton(mButtonMedium);
				} else if (tempo == Tempo.HIGH) {
					enableButton(mButtonHigh);
				}
			}
		}
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key));
		FlurryAgent.logEvent("Discovery - tempo slider");
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FlurryAgent.onEndSession(getActivity());
	}
	
	
	private void enableButton(Button button) {
		button.setSelected(true);
		button.setBackgroundResource(R.drawable.background_discovery_tempo_item_selected);
	}
	
	private void disableButton(Button button) {
		button.setSelected(false);
		button.setBackgroundResource(R.drawable.background_discovery_tempo_item_unselected);
	}
	
	public List<Tempo> getTempos() {
		List<Tempo> tempos = new ArrayList<Tempo>();
		if (mButtonAuto.isSelected()){
			tempos.add(Tempo.AUTO);
			return tempos;
		}
		if (mButtonLow.isSelected()){
			tempos.add(Tempo.LOW);
		}
		if (mButtonMedium.isSelected()){
			tempos.add(Tempo.MEDIUM);
		}
		if (mButtonHigh.isSelected()){
			tempos.add(Tempo.HIGH);
		}
		return tempos;
	}
}
