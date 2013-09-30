package com.hungama.myplay.activity.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.hungama.myplay.activity.R;

/**
 * Implementation of {@link ImageButton} which allows a different src image when the button inactive.
 */
public class ActiveButton extends ImageButton {
	
	private static final int NOT_AVAILABLE_INACTIVE_RESOURCE = -1;
	
	private int mActiveSrcResource;
	private int mInactiveSrcResource;
	
	private boolean mIsActivated;
	
	public ActiveButton(Context context) {
		super(context);
	}
	
	public ActiveButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize(context, attrs);
	}

	public ActiveButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initialize(context, attrs);
	}
	
	private void initialize(Context context, AttributeSet attrs) {
		// get the resource of the inactive state.
		TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ActiveButton, 0, 0);
		
		mActiveSrcResource = typedArray.getResourceId(R.styleable.ActiveButton_activeSrc, NOT_AVAILABLE_INACTIVE_RESOURCE);
		mInactiveSrcResource = typedArray.getResourceId(R.styleable.ActiveButton_inactiveSrc, NOT_AVAILABLE_INACTIVE_RESOURCE);
		
		typedArray.recycle();
		
		// default is that the button is active.
		activate();
	}
	
	
	// ======================================================
	// Public.
	// ======================================================
	
	public boolean isActivated() {
		return mIsActivated;
	}
	
	/**
	 * Makes the button active to user clicks.
	 */
	public void activate() {
		mIsActivated = true;
		
		setClickable(true);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			setEnabled(true);
		}
		
		setImageResource(mActiveSrcResource);
	}
	
	/**
	 * Makes the button inactive to user clicks.
	 */
	public void deactivate() {
		mIsActivated = false;
		
		setClickable(false);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			setEnabled(false);
		}
		
		setImageResource(mInactiveSrcResource);
		
	}

}
