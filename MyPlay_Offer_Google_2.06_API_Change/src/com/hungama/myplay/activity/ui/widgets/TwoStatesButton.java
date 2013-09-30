package com.hungama.myplay.activity.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

import com.hungama.myplay.activity.R;

/**
 * Button that changes it selected state when clicked.
 */
public class TwoStatesButton extends Button {
	
	private static final int NOT_AVAILABLE_RESOURCE = -1;
	
	private Drawable mSelectedBackground;
	private Drawable mUnselectedBackground;
	
	
	public TwoStatesButton(Context context) {
		super(context);
	}
	
	public TwoStatesButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize(context, attrs);
	}

	public TwoStatesButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initialize(context, attrs);
	}
	
	
	private void initialize(Context context, AttributeSet attrs) {
		// get the resource of the inactive state.
		TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TwoStatesButton, 0, 0);
		
		mSelectedBackground = typedArray.getDrawable(R.styleable.TwoStatesButton_selectedBackground);
		mUnselectedBackground = typedArray.getDrawable(R.styleable.TwoStatesButton_unselectedBackground);
		
		typedArray.recycle();
		
		setUnselected();
	}
	
	@Override
	public boolean performClick() {
		
		if (isSelected()) { 
			setUnselected();
		} else {
			setSelected();
		}
		
		return super.performClick();
	}
	
	public void setSelectedBackground(Drawable drawable) {
		mSelectedBackground = drawable;
		
		if (isSelected()) {
			setSelected();
		}
	}
	
	public void setUnselectedBackground(Drawable drawable) {
		mUnselectedBackground = drawable;
		
		if (!isSelected()) {
			setUnselected();
		}
	}
	
	public void setUnselected() {
		setSelected(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			setBackground(mUnselectedBackground);
		} else {
			setBackgroundDrawable(mUnselectedBackground);
		}
	}
	
	public void setSelected() {
		setSelected(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			setBackground(mSelectedBackground);
		} else {
			setBackgroundDrawable(mSelectedBackground);
		}
	}
}
