package com.hungama.myplay.activity.ui.widgets;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.ui.widgets.ThreeStatesActiveButton.OnStateChangedListener;
import com.hungama.myplay.activity.ui.widgets.ThreeStatesActiveButton.State;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;


/**
 * A button which manages active mode and inactive mode, while in active mode the button
 * can be managed button's resource icons by two states - Active and Second state.
 * 
 * Clicking the button when inactive will not perform any action,
 * while clicking in active mode will toggle between the three states of: Active, Second State.
 * 
 * Inactivation of a button will resets it mode.
 */
public class TwoStatesActiveButton extends ImageButton {
	
	private static final String TAG = "TwoStatesActiveButton";
	
	private static final int NOT_AVAILABLE_RESOURCE = -1;
	
	private boolean mIsActivated = false;
	private State mState = State.INACTIVE;
	
	private OnStateChangedListener mOnStateChangedListener = null;

	private int mInactiveStateSrcResource;
	private int mActiveStateSrcResource;
	private int mSecondStateSrcResource;
	
	public TwoStatesActiveButton(Context context) {
		super(context);
	}
	
	public TwoStatesActiveButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize(context, attrs);
	}

	public TwoStatesActiveButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initialize(context, attrs);
	}
	
	private void initialize(Context context, AttributeSet attrs) {
		
		TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TwoStatesActiveButton, 0, 0);
		
		mInactiveStateSrcResource = typedArray.getResourceId(R.styleable.TwoStatesActiveButton_inactiveStateSource, NOT_AVAILABLE_RESOURCE);
		mActiveStateSrcResource = typedArray.getResourceId(R.styleable.TwoStatesActiveButton_activeStateSource, NOT_AVAILABLE_RESOURCE);
		mSecondStateSrcResource = typedArray.getResourceId(R.styleable.TwoStatesActiveButton_secondStateSource, NOT_AVAILABLE_RESOURCE);
		
		typedArray.recycle();
		
		activate();
	}
	
	// ======================================================
	// Public.
	// ======================================================
	
	public enum State {
		INACTIVE,
		ACTIVE,
		SECOND
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
	
	public TwoStatesActiveButton.State getState() {
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
	
	public void setState(TwoStatesActiveButton.State state) {
		if (state == State.INACTIVE) {
			setInactiveState();
			
		} else if (state == State.ACTIVE) {
			setActiveState();
			
		} else if (state == State.SECOND) {
			setSecondState();
			
		}
	}
	
	@Override
	public boolean performClick() {
		if (mIsActivated) {
			
			if (mState == State.ACTIVE) {
				// switches to the second state.
				setSecondState();
				
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

}
