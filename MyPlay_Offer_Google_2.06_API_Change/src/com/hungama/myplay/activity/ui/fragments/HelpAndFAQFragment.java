package com.hungama.myplay.activity.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.ui.HelpAndFAQActivity;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class HelpAndFAQFragment extends Fragment implements OnClickListener{
	
	private static final String TAG = "HelpAndFAQFragment";
	
	private RelativeLayout mSectionAppCrashing;
	private ImageView mButtonAppCrashingOpenContent;
	private TextView mTextApplCrashingContent;
	
	private LinearLayout mContainerSlidingSections;
	private RelativeLayout mSectionReadFAQ;
	private RelativeLayout mSectionReportProblem;
	
	private static final long ANIMATION_DURATION = 500;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_help_faq, container, false);
		
		mSectionAppCrashing = (RelativeLayout) rootView.findViewById(R.id.help_faq_section_app_crashing);
		mButtonAppCrashingOpenContent = (ImageView) rootView.findViewById(R.id.help_faq_section_app_crashing_button_content);
		mTextApplCrashingContent = (TextView) rootView.findViewById(R.id.help_faq_section_app_crashing_text);
		
		mContainerSlidingSections = (LinearLayout) rootView.findViewById(R.id.help_faq_container_sliding_sections);
		mSectionReadFAQ = (RelativeLayout) rootView.findViewById(R.id.help_faq_section_read_faq);
		mSectionReportProblem = (RelativeLayout) rootView.findViewById(R.id.help_faq_section_report_a_problem);
		
		mSectionAppCrashing.setOnClickListener(this);
		mSectionReadFAQ.setOnClickListener(this);
		mSectionReportProblem.setOnClickListener(this);
		
		return rootView;
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		
		if (viewId == R.id.help_faq_section_app_crashing) {
			
			if (mTextApplCrashingContent.getVisibility() == View.VISIBLE) {
				// hides the content.
				hideAppCrashingContent();
			} else {
				// shows the content.
				showAppCrashingContent();
			}
			
		} else if (viewId == R.id.help_faq_section_read_faq) {
			String url = getResources().getString(R.string.hungama_server_url_faq);
			((HelpAndFAQActivity) getActivity()).showWebviewPage(url);
			
		} else if (viewId == R.id.help_faq_section_report_a_problem) {
			((HelpAndFAQActivity) getActivity()).showFeedbackPage();
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
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
	
	private void showAppCrashingContent() {
		
//		final int marginFromSection = mTextApplCrashingContent.getHeight();
		
		// slides down the other sections.
//		ViewPropertyAnimator.animate(mContainerSlidingSections)
//							.setDuration(ANIMATION_DURATION)
//							.yBy(marginFromSection)
//							.setListener(new AnimatorListener() {
//								
//								@Override
//								public void onAnimationStart(Animator animation) {
//									mTextApplCrashingContent.setVisibility(View.VISIBLE);
//								}
//								
//								@Override
//								public void onAnimationEnd(Animator animation) {}
//								
//								@Override
//								public void onAnimationRepeat(Animator animation) {}
//								
//								@Override
//								public void onAnimationCancel(Animator animation) {}
//								
//							}).start();
		
		mTextApplCrashingContent.setVisibility(View.VISIBLE);
		
		// rotates the button.
		ViewPropertyAnimator.animate(mButtonAppCrashingOpenContent)
							.setDuration(ANIMATION_DURATION)
							.rotation(-180)
							.start();
	}
	
	private void hideAppCrashingContent() {
		
//		final int marginFromSection = mTextApplCrashingContent.getHeight();
		
		// slides up the other sections.
//		ViewPropertyAnimator.animate(mContainerSlidingSections)
//							.setDuration(ANIMATION_DURATION)
//							.yBy(-marginFromSection) // beware of the "-" sign.
//							.setListener(new AnimatorListener() {
//								
//								@Override
//								public void onAnimationStart(Animator animation) {}
//								
//								@Override
//								public void onAnimationEnd(Animator animation) {
//									// hides the text.
//									mTextApplCrashingContent.setVisibility(View.INVISIBLE);
//								}
//								
//								@Override
//								public void onAnimationRepeat(Animator animation) {}
//								
//								@Override
//								public void onAnimationCancel(Animator animation) {}
//								
//							}).start();
		
		mTextApplCrashingContent.setVisibility(View.GONE);
		
		// rotates the button.
		ViewPropertyAnimator.animate(mButtonAppCrashingOpenContent)
							.setDuration(ANIMATION_DURATION)
							.rotation(0) // beware of the "-" sign.
							.start();
	}
	
	
}
