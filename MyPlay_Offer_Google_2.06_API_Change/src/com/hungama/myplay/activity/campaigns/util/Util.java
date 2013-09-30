package com.hungama.myplay.activity.campaigns.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.RelativeLayout;

public class Util {

	private static final String TAG = "Util";
	
	/**
	 * 
	 * @return the matching layout parameters for the image according to the device density
	 */
	public static RelativeLayout.LayoutParams getLayoutParams(Context context, Drawable d){
		
		int height = (int) (d.getMinimumHeight());
		int width = (int) (d.getMinimumWidth());
		
		float screenWidth = context.getResources().getDisplayMetrics().widthPixels;
		
		float widthDivide = screenWidth/width;
		
		height = (int) (height * widthDivide);
		
		RelativeLayout.LayoutParams params = 
			new RelativeLayout.LayoutParams((int) (screenWidth), (int) (height));
		
		return params;
	}
	
	public static int getDeviceDensity(Context c){
		return c.getResources().getDisplayMetrics().densityDpi;
	}
}
