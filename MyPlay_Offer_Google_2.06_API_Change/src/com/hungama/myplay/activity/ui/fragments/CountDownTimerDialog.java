/**
 * 
 */
package com.hungama.myplay.activity.ui.fragments;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.util.SleepModeManager;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * @author DavidSvilem
 *
 */
public class CountDownTimerDialog extends DialogFragment implements OnClickListener,
														            OnCheckedChangeListener{

	public static final String FRAGMENT_TAG = "CountDownTimerDialog";
	
	public static final String TIME_TO_COUNT = "time_to_count";
	
	// Data Members
	private int time = 15; //default
	
	// Views
	private ImageButton closeButton;
	private Button startTimerButton;
	private TextView remainingTimeText;
	private RadioGroup timesRadioGroup;
	
	private SleepModeManager mSleepModeManager;
	
    public static CountDownTimerDialog newInstance() {
    	CountDownTimerDialog f = new CountDownTimerDialog();
    	
        // Supply data input as an argument.
        Bundle args = new Bundle();
        //args.putInt(TIME_TO_COUNT, time);
        
        //f.setArguments(args);

        return f;
    }
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	mSleepModeManager = SleepModeManager.getInstance(getActivity().getApplicationContext());
    	
    	//setStyle(DialogFragment.STYLE_NO_TITLE, com.actionbarsherlock.R.style.Sherlock___Theme_Dialog);
    	
    	setStyle(DialogFragment.STYLE_NO_TITLE, com.actionbarsherlock.R.style.Theme_Sherlock_Light_Dialog);
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_countdown_timer, container);
	
		closeButton = (ImageButton) view.findViewById(R.id.close_button);
		startTimerButton = (Button) view.findViewById(R.id.start_timer_button);
		remainingTimeText = (TextView) view.findViewById(R.id.remaining_time_text);
		timesRadioGroup = (RadioGroup) view.findViewById(R.id.times_radio_group);
		
		// Set listeners
		closeButton.setOnClickListener(this);
		startTimerButton.setOnClickListener(this);
		timesRadioGroup.setOnCheckedChangeListener(this);
		
		if(mSleepModeManager.isCountingDown()){
			
			remainingTimeText.setVisibility(View.VISIBLE);
			timesRadioGroup.setVisibility(View.GONE);	
			
			// Set text
			String timeLeft = mSleepModeManager.getTimeLeftStr();
			remainingTimeText.setText(timeLeft);
			
			// Set button text
			startTimerButton.setText(R.string.reset_timer_button_text);
			
		}else{
			
			
		}
		
		//time = getArguments().getInt(TIME_TO_COUNT);
		
		return view;
	}

	@Override
	public void onClick(View v) {
	
		switch (v.getId()) {
		case R.id.close_button:
			
			dismiss();
			
			break;

		case R.id.start_timer_button:
			
			if(mSleepModeManager.isCountingDown()){
				mSleepModeManager.cancelCounting();
				dismiss();
			}else{
				mSleepModeManager.startAlarm(time);
				dismiss();
			}
			
			// Show CountDownTimerDialog
			CountDownTimerDialog shareDialogFragment = CountDownTimerDialog.newInstance();
			
			FragmentManager mFragmentManager = getFragmentManager();
			shareDialogFragment.show(mFragmentManager, ShareDialogFragment.FRAGMENT_TAG);
			
			break;
			
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {

		switch (group.getId()) {
		case R.id.times_radio_group:
			
			if(checkedId == R.id.radio15){
				time = 15;
			}else if(checkedId == R.id.radio30){
				time = 30;
			}else if(checkedId == R.id.radio45){
				time = 45;
			}else if(checkedId == R.id.radio60){
				time = 60;
			}
			
			break;

		default:
			break;
		}
	}
	
}
