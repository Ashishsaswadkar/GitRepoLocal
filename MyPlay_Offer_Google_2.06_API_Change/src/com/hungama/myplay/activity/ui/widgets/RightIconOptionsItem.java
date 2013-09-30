package com.hungama.myplay.activity.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;

import com.hungama.myplay.activity.R;

/**
 * A custom options items when the icon is on the right.
 */
public class RightIconOptionsItem extends Button {
	
	private int mItemIconSize = 0;
	private int mItemIconReference = -1;
	
	public RightIconOptionsItem(Context context) {
		super(context);
	}
	
	public RightIconOptionsItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context, attrs);
	}

	public RightIconOptionsItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context, attrs);
	}
	
	private void initialize(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RightIconOptionsItem, 0, 0);
		
		try {
			mItemIconSize = a.getDimensionPixelSize(R.styleable.RightIconOptionsItem_itemIconSize, 30);
			mItemIconReference = a.getResourceId(R.styleable.RightIconOptionsItem_itemIcon, -1);
		} finally {
			a.recycle();
		}
		
		// draws the icon.
		if (mItemIconSize > 0 && mItemIconReference != -1) {
			Drawable icon = getResources().getDrawable(mItemIconReference);
			icon.setBounds(0, 0, mItemIconSize, mItemIconSize);
			setCompoundDrawables(null, null, icon, null);
		}
		
		setClickable(true);
		requestFocus();
	}
	
	public void setIcon(Drawable icon) {
		if (icon != null) {
			icon.setBounds(0, 0, mItemIconSize, mItemIconSize);
		}
		setCompoundDrawables(null, null, icon, null);
		invalidate();
		requestLayout();
	}
	
	public int getIconSize() {
		return mItemIconSize;
	}

}
