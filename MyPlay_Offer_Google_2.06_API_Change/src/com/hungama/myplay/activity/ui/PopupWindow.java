/**
 * 
 */
package com.hungama.myplay.activity.ui;

import com.hungama.myplay.activity.R;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * @author ashish
 * 
 */
public class PopupWindow extends android.widget.PopupWindow {
	Context ctx;
	ImageButton btnDismiss;
	TextView lblText;
	TextView offerText;
	View popupView;

	public PopupWindow(Context context, String offer) {
		super(context);
		ctx = context;
		
		
		popupView = LayoutInflater.from(context).inflate(R.layout.offer_popup, null);
		setContentView(popupView);
		
//		popupView.setScaleX(getWidth()-30);
		
		btnDismiss = (ImageButton) popupView.findViewById(R.id.btn_dismiss);
		offerText = (TextView) popupView.findViewById(R.id.offer_text);
		lblText = (TextView) popupView.findViewById(R.id.text);
		
		offerText.setText(offer);

		setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		setWidth(WindowManager.LayoutParams.WRAP_CONTENT);

		// Closes the popup window when touch outside of it - when looses focus
		setOutsideTouchable(true);
		setFocusable(true);

		// Removes default black background
		setBackgroundDrawable(new BitmapDrawable());

		btnDismiss.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				dismiss();
			}
		});

		// Closes the popup window when touch it
		/*
		 * this.setTouchInterceptor(new View.OnTouchListener() {
		 * 
		 * @Override public boolean onTouch(View v, MotionEvent event) {
		 * 
		 * if (event.getAction() == MotionEvent.ACTION_MOVE) { dismiss(); }
		 * return true; } });
		 */
	} // End constructor

	// Attaches the view to its parent anchor-view at position x and y
	public void show(View anchor, int x, int y) {
		showAtLocation(anchor, Gravity.CENTER, x, y);
	}
}
