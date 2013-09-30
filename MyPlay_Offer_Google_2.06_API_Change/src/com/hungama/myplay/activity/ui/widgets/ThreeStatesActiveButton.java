/**
 * 
 */
package com.hungama.myplay.activity.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import com.hungama.myplay.activity.R;

/**
 * A button which manages active mode and inactive mode, while in active mode the button
 * can be managed button's resource icons by three states - Active, Second state and Third State.
 * 
 * Clicking the button when inactive will not perform any action,
 * while clicking in active mode will toggle between the three states of: Active, Second State, Third State.
 * 
 * Inactivation of a button will resets it mode.
 */
public class ThreeStatesActiveButton extends ImageButton {
	
	private static final String TAG = "ThreeStatesActiveButton";
	
	private static final int NOT_AVAILABLE_RESOURCE = -1;
	
	private boolean mIsActivated = false;
	private State mState = State.INACTIVE;
	
	private OnStateChangedListener mOnStateChangedListener = null;

	private int mInactiveStateSrcResource;
	private int mActiveStateSrcResource;
	private int mSecondStateSrcResource;
	private int mThirdStateSrcResource;
	
	public ThreeStatesActiveButton(Context context) {
		super(context);
	}
	
	public ThreeStatesActiveButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize(context, attrs);
	}

	public ThreeStatesActiveButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initialize(context, attrs);
	}
	
	private void initialize(Context context, AttributeSet attrs) {
		
		TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ThreeStatesActiveButton, 0, 0);
		
		mInactiveStateSrcResource = typedArray.getResourceId(R.styleable.ThreeStatesActiveButton_inactiveStateSrc, NOT_AVAILABLE_RESOURCE);
		mActiveStateSrcResource = typedArray.getResourceId(R.styleable.ThreeStatesActiveButton_activeStateSrc, NOT_AVAILABLE_RESOURCE);
		mSecondStateSrcResource = typedArray.getResourceId(R.styleable.ThreeStatesActiveButton_secondStateSrc, NOT_AVAILABLE_RESOURCE);
		mThirdStateSrcResource = typedArray.getResourceId(R.styleable.ThreeStatesActiveButton_ThirdStateSrc, NOT_AVAILABLE_RESOURCE);
		
		typedArray.recycle();
		
		activate();
	}
	
	// ======================================================
	// Public.
	// ======================================================
	
	public enum State {
		INACTIVE,
		ACTIVE,
		SECOND,
		THIRD
	}
	
	public interface OnStateChangedListener {
		
		public void onInactiveState(View view);
		
		public void onActiveState(View view);
		
		public void onSecondState(View view);
		
		public void onThirdState(View view);
	}
	
	public void setOnStateChangedListener(OnStateChangedListener listener) {
		mOnStateChangedListener = listener;
	}
	
	public ThreeStatesActiveButton.State getState() {
		return mState;
	}
	
	public boolean isActivated() {
		return mIsActivated;
	}
	
	/**
	 * Makes the button active to user clicks.
	 */
	public void activate() {
		setActiveState();
	}
	
	/**
	 * Makes the button inactive to user clicks.
	 */
	public void deactivate() {
		setInactiveState();
	}
	
	public void setState(ThreeStatesActiveButton.State state) {
		if (state == State.INACTIVE) {
			setInactiveState();
			
		} else if (state == State.ACTIVE) {
			setActiveState();
			
		} else if (state == State.SECOND) {
			setSecondState();
			
		} else if (state == State.THIRD) {
			setThirdState();
		}
	}
	
	@Override
	public boolean performClick() {
		if (mIsActivated) {
			
			if (mState == State.ACTIVE) {
				// switches to the second state.
				setSecondState();
				
			} else if (mState == State.SECOND) {
				// switches to the third state.
				setThirdState();
				
			} else {
				// switches to the active state.
				setActiveState();
			}
			
			return super.performClick();
		}

		// not supposed to reach here, inactive mode is not clickable.
		return false;
	}
	
	private void setInactiveState() {
		mIsActivated = false;
		mState = State.INACTIVE;
		
		setClickable(false);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			setEnabled(false);
		}
		
		setImageResource(mInactiveStateSrcResource);
		
		if (mOnStateChangedListener != null) {
			mOnStateChangedListener.onInactiveState(this);
		}
	}
	
	private void setActiveState() {
		mIsActivated = true;
		mState = State.ACTIVE;
		
		setClickable(true);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			setEnabled(true);
		}
		
		setImageResource(mActiveStateSrcResource);
		
		if (mOnStateChangedListener != null) {
			mOnStateChangedListener.onActiveState(this);
		}
	}
	
	private void setSecondState() {
		mState = State.SECOND;
		setImageResource(mSecondStateSrcResource);
		
		if (mOnStateChangedListener != null) {
			mOnStateChangedListener.onSecondState(this);
		}
	}
	
	private void setThirdState() {
		mState = State.THIRD;
		setImageResource(mThirdStateSrcResource);
		
		if (mOnStateChangedListener != null) {
			mOnStateChangedListener.onThirdState(this);
		}
	}

}
