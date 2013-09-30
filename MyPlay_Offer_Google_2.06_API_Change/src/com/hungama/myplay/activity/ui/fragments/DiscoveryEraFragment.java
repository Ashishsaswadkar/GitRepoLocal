package com.hungama.myplay.activity.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.Era;
import com.hungama.myplay.activity.ui.DiscoveryActivity;
import com.hungama.myplay.activity.util.Logger;

public class DiscoveryEraFragment extends Fragment {
	
	private static final String TAG = "DiscoveryEraFragment";
	
	private Era mEra = null;
	
	// views
	private RelativeLayout mFragmentContainer;
	
	private TextView mTextTimeFrom;
	private TextView mTextTimeSeparator;
	private TextView mTextTimeTo;
	
	private RelativeLayout mSlider;
	private ImageView mImageRulerLeft;
	private ImageView mImageRulerCenter;
	private ImageView mImageRulerRight;
	
	private TextView mTextRulerMinimum;
	private TextView mTextRulerMiddle;
	private TextView mTextRulerCurrent;
	
	private Button mMarkerFrom;
	private Button mMarkerTo;
	
	private int fromYear;
	private int toYear;
	
	
	// ======================================================
	// Fragment's lifecycle callbacks.
	// ======================================================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_discovery_era, container, false);
		
		initializeUserControls(rootView);
		
		adjustTouchingListenerFromMarker();
		adjustTouchingListenerToMarker();
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// gets the Era from the parent activity, if it doesn't have one, create a default.
		DiscoveryActivity discoveryActivity = (DiscoveryActivity) getActivity();
		Era era = discoveryActivity.getDiscover().getEra();
		if (era == null) {
			era = new Era(Era.getDefaultFrom(), Era.getDefaultTo());
		}
		mEra = era;
		
		fromYear = mEra.getFrom();
		toYear = mEra.getTo();
		
		// sets ruler year.
		mTextRulerMinimum.setText(Era.getTime(Era.getDefaultFrom()));
		mTextRulerMiddle.setText(Era.getTime(Era.getDefaultMiddle()));
		mTextRulerCurrent.setText(Era.getTime(Era.getDefaultTo()));
		
		// adjust markers.
		adjustMarker(mMarkerFrom, mEra.getFrom());
		adjustMarker(mMarkerTo, mEra.getTo());
		
		// sets labels.
		setTimeLabels(mEra.getFrom(), mEra.getTo());
		
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key));
		FlurryAgent.logEvent("Discovery - era slider");
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
	
	// ======================================================
	// Private helper methods.
	// ======================================================
	
	private void initializeUserControls(View rootView) {
		/* 
		 * disables the delegating touches to avoid committing 
		 * actions on the below layers (like the grid of tiles).
		 */
		mFragmentContainer = (RelativeLayout) rootView.findViewById(R.id.discovery_era_container);
		mFragmentContainer.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				return true;
			}
		});
		// force request to update its measurements.
		mFragmentContainer.measure(0, 0);
		
		mTextTimeFrom = (TextView) rootView.findViewById(R.id.discovery_era_time_from);
		mTextTimeSeparator = (TextView) rootView.findViewById(R.id.discovery_era_time_separator);
		mTextTimeTo = (TextView) rootView.findViewById(R.id.discovery_era_time_to);
		
		mSlider = (RelativeLayout) rootView.findViewById(R.id.discovery_era_slider);
		mImageRulerLeft = (ImageView) rootView.findViewById(R.id.discovery_era_slider_ruler_side_left);
		mImageRulerCenter = (ImageView) rootView.findViewById(R.id.discovery_era_slider_ruler_side_center);
		mImageRulerRight = (ImageView) rootView.findViewById(R.id.discovery_era_slider_ruler_side_right);
		
		mTextRulerMinimum = (TextView) rootView.findViewById(R.id.discovery_era_slider_ruler_text_time_minumum_year);
		mTextRulerMiddle = (TextView) rootView.findViewById(R.id.discovery_era_slider_ruler_text_time_middle_year);
		mTextRulerCurrent = (TextView) rootView.findViewById(R.id.discovery_era_slider_ruler_text_time_current_year);
		
		mMarkerFrom = (Button) rootView.findViewById(R.id.discovery_era_slider_marker_from);
		mMarkerTo = (Button) rootView.findViewById(R.id.discovery_era_slider_marker_to);
	}
	
	private void adjustTouchingListenerFromMarker() {
		
		mMarkerFrom.setOnTouchListener(new OnTouchListener() {
			
			private int screenX;
			private View parentView;
			private RelativeLayout.LayoutParams params;
			private RelativeLayout.LayoutParams borderingMarkerparams;
			
			private int markerCenterX;
			private int borderLeftX;
			private int borderRightX;
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				// gets the x position from the screen.
				screenX = (int) event.getRawX();
				
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_DOWN:
						if (params == null) {
							params = (RelativeLayout.LayoutParams) view.getLayoutParams();
							borderingMarkerparams = (RelativeLayout.LayoutParams) mMarkerTo.getLayoutParams();
							
							// gets the marker's center.
							view.measure(0, 0);
							markerCenterX = view.getMeasuredWidth() / 2;
							// gets its boundaries.
							parentView = (View) view.getParent();
							borderLeftX = parentView.getLeft() + markerCenterX;
							borderRightX = parentView.getRight();
						
						}
						break;
						
					case MotionEvent.ACTION_MOVE:
						// checks if the touching position is within the borders.
						if (screenX > borderLeftX && screenX < borderRightX){
							params.leftMargin = screenX - parentView.getLeft() - markerCenterX;
							view.setLayoutParams(params);
						} else {
							// exceeding left.
							if (screenX <= borderLeftX) {
								// adjusts to the minimum border.
								params.leftMargin = 0;
								view.setLayoutParams(params);
							
								// exceeding right.
							}else if (screenX >= borderRightX) {
								// adjusts to the maximum border.
								params.leftMargin = parentView.getWidth() - markerCenterX;
								view.setLayoutParams(params);
							}
						}
						
						fromYear = adjustEraYearFromMarker(mMarkerFrom);
						
						mTextTimeFrom.setText(Era.getTime(fromYear));
						
						// fits the border marker to scroll with if exceeding.
						if (params.leftMargin > borderingMarkerparams.leftMargin) {
							borderingMarkerparams.leftMargin = params.leftMargin;
							mMarkerTo.setLayoutParams(borderingMarkerparams);
							
							mTextTimeTo.setText(Era.getTime(adjustEraYearFromMarker(mMarkerTo)));
						}
						
						break;
				}
				
				return true;
			}
		});
	}
	
	private void adjustTouchingListenerToMarker() {
		
		mMarkerTo.setOnTouchListener(new OnTouchListener() {
			
			private int screenX;
			private View parentView;
			private RelativeLayout.LayoutParams params;
			private RelativeLayout.LayoutParams borderingMarkerparams;
			
			private int markerCenterX;
			private int borderLeftX;
			private int borderRightX;
			
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				// gets the x position from the screen.
				screenX = (int) event.getRawX();
				
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_DOWN:
						if (params == null) {
							params = (RelativeLayout.LayoutParams) view.getLayoutParams();
							borderingMarkerparams = (RelativeLayout.LayoutParams) mMarkerFrom.getLayoutParams();
							
							// gets the marker's center.
							view.measure(0, 0);
							markerCenterX = view.getMeasuredWidth() / 2;
							// gets its boundaries.
							parentView = (View) view.getParent();
							borderLeftX = parentView.getLeft() + markerCenterX;
							borderRightX = parentView.getRight();
						
						}
						break;
						
					case MotionEvent.ACTION_MOVE:
						// checks if the touching position is within the borders.
						if (screenX > borderLeftX && screenX < borderRightX){
							params.leftMargin = screenX - parentView.getLeft() - markerCenterX;
							view.setLayoutParams(params);
						} else {
							// exceeding left.
							if (screenX <= borderLeftX) {
								// adjusts to the minimum border.
								params.leftMargin = 0;
								view.setLayoutParams(params);
							
								// exceeding right.
							}else if (screenX >= borderRightX) {
								// adjusts to the maximum border.
								params.leftMargin = parentView.getWidth() - markerCenterX;
								view.setLayoutParams(params);
							}
						}
						
						toYear = adjustEraYearFromMarker(mMarkerTo);
						
						mTextTimeTo.setText(Era.getTime(toYear));
						
						// fits the border marker to scroll with if exceeding.
						if (params.leftMargin < borderingMarkerparams.leftMargin) {
							borderingMarkerparams.leftMargin = params.leftMargin;
							mMarkerFrom.setLayoutParams(borderingMarkerparams);
							
							mTextTimeFrom.setText(Era.getTime(adjustEraYearFromMarker(mMarkerFrom)));
						}
						
						break;
				}
				
				return true;
			}
		});
	}
	
	private void adjustMarker(View marker, int year) {
		
		marker.measure(0, 0);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) marker.getLayoutParams();
		int markerCenterX = marker.getMeasuredWidth() / 2;
		
		// sets the maximum.
		if (year == Era.getDefaultTo()) {
			params.leftMargin = mSlider.getMeasuredWidth() - markerCenterX;
			marker.setLayoutParams(params);
			return;
		}
		
		// sets the middle.
		if (year == Era.getDefaultMiddle()) {
			params.leftMargin = (mSlider.getMeasuredWidth()/ 2) - markerCenterX;
			marker.setLayoutParams(params);
			return;
		}
		
		// sets the minimum.
		if (year == Era.getDefaultFrom()) {
			params.leftMargin = 0;
			marker.setLayoutParams(params);
			return;
		}
		
		int deltaYear = 0;
		
		// sets between the minimum to middle
		if (year > Era.getDefaultFrom() && year < Era.getDefaultMiddle()) {
			deltaYear = (year - Era.getDefaultFrom()) / 10;
			int centuryPixelsSize = ((mSlider.getMeasuredWidth() / 2) - markerCenterX) / ((Era.getDefaultMiddle() - Era.getDefaultFrom()) / 10);
			
			params.leftMargin = deltaYear * centuryPixelsSize;
			marker.setLayoutParams(params);
			return;
		}
		
		// sets between the middle to maximum
		if (year > Era.getDefaultMiddle() && year < Era.getDefaultTo()) {
			deltaYear = year - Era.getDefaultMiddle();
			int yearPixelsSize = ((mSlider.getMeasuredWidth() / 2) - marker.getMeasuredWidth()) / ((Era.getDefaultTo() - Era.getDefaultMiddle()) + 1);
			
			params.leftMargin = (mSlider.getMeasuredWidth() / 2) + (deltaYear * yearPixelsSize); 
			marker.setLayoutParams(params);
			return;
		}
	}
	
	private void setTimeLabels(int from, int to) {
		if (from != to) {
			mTextTimeSeparator.setVisibility(View.VISIBLE);
			mTextTimeTo.setVisibility(View.VISIBLE);
			
			mTextTimeFrom.setText(Era.getTime(from));
			mTextTimeTo.setText(Era.getTime(to));
			
		} else {
			mTextTimeSeparator.setVisibility(View.GONE);
			mTextTimeTo.setVisibility(View.GONE);
			
			mTextTimeFrom.setText(Era.getTime(from));
		}
	}
	
	private int adjustEraYearFromMarker(View marker) {
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) marker.getLayoutParams();
		int margin = params.leftMargin;
		int markerCenterX = marker.getMeasuredWidth() / 2;
		int middleSlider = mSlider.getMeasuredWidth() / 2;
		
		if (margin == 0) {
			return  Era.getDefaultFrom();
		}
		
		if (margin == (mSlider.getMeasuredWidth() - markerCenterX)) {
			return Era.getDefaultTo();
		}
			
		if (margin < middleSlider) {
			int centuryPixelsSize = (middleSlider - markerCenterX) / ((Era.getDefaultMiddle() - Era.getDefaultFrom()) / 10);
			int year = Era.getDefaultFrom() + ((int) (margin / centuryPixelsSize)) * 10;
			return year;
		}
		
		if (margin >= middleSlider) {
			int yearPixelsSize = (middleSlider - marker.getMeasuredWidth()) / ((Era.getDefaultTo() - Era.getDefaultMiddle()) + 1);
			int year = Era.getDefaultMiddle() + ((int) ((margin - middleSlider) / yearPixelsSize));
			return year;
		}
		
		Logger.e(TAG, "Error getting year from slider.");
		return Era.getDefaultTo();
	}
	
	// ======================================================
	// Public.
	// ======================================================

	public Era getEra() {
//		int from = adjustEraYearFromMarker(mMarkerFrom);
//		int to = adjustEraYearFromMarker(mMarkerTo);
		
		int from = fromYear;
		int to = toYear;
		
		Logger.v(TAG, "From: " + from + " " + " To: " + to);
		
		mEra = new Era(from, to);
		
		return mEra;
	}
	
}
