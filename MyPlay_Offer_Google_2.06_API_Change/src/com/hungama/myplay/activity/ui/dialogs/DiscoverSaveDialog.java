package com.hungama.myplay.activity.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.hungama.myplay.activity.R;

public class DiscoverSaveDialog extends Dialog {
	
	private static final String TAG = "DiscoverSaveDialog";
	
	public interface OnDiscoverSaveDialogStateChangedListener {
		
		public void onCancelled(DiscoverSaveDialog dialog);
		
		public void onSaveSelected(DiscoverSaveDialog dialog, String discoverName);
	}
	
	private OnDiscoverSaveDialogStateChangedListener mOnDiscoverSaveDialogStateChangedListener;
	
	private EditText mTextName; 
	private ImageButton mButtonClose;
	private Button mButtonSave;
	
	private InputMethodManager mInputMethodManager;
	
	public DiscoverSaveDialog(Context context, String discoverName, OnDiscoverSaveDialogStateChangedListener listener) {
		super(context);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_discovery_save_discover);
		
		mOnDiscoverSaveDialogStateChangedListener = listener;
		
		// initialize the control on the keyboard.
		mInputMethodManager = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		
		mTextName = (EditText) findViewById(R.id.dialog_discovery_save_text_name);
		mButtonClose = (ImageButton) findViewById(R.id.dialog_discovery_save_title_button_close);
		mButtonSave = (Button) findViewById(R.id.dialog_discovery_save_button_save);
		
		mTextName.setHint(discoverName);
		mButtonClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				cancel();
				
				// closes the keyboard.
				mInputMethodManager.hideSoftInputFromWindow(mTextName.getWindowToken(), 0);
				
				if (mOnDiscoverSaveDialogStateChangedListener != null) {
					mOnDiscoverSaveDialogStateChangedListener.onCancelled(DiscoverSaveDialog.this);
				}
			}
		});
		mButtonSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// closes the keyboard.
				mInputMethodManager.hideSoftInputFromWindow(mTextName.getWindowToken(), 0);
				
				// checks if the given name is valid.
				if (mTextName.getText() == null || TextUtils.isEmpty(mTextName.getText().toString())) {
					// show error discover name is empty.
					Toast.makeText(getContext(), R.string.discovery_message_save_discover_message_error_empty_name, Toast.LENGTH_SHORT).show();
					return;
				}
				
				String discoverName = mTextName.getText().toString();
				discoverName = discoverName.trim();
				
				// checks again if the text is empty.
				if (TextUtils.isEmpty(discoverName)) {
					Toast.makeText(getContext(), R.string.discovery_message_save_discover_message_error_empty_name, Toast.LENGTH_SHORT).show();
					return;
				}
				
//				String validDiscoverNameRegex = "[^\\s]";
//				if (discoverName.matches(validDiscoverNameRegex)) {
//					// shows an error for invalid discover name.
//					Toast.makeText(getContext(), R.string.discovery_message_save_discover_message_error_invalid_name, Toast.LENGTH_SHORT).show();
//					return;
//				}
				
				mTextName.setText(discoverName);
				
				if (mOnDiscoverSaveDialogStateChangedListener != null) {
					mOnDiscoverSaveDialogStateChangedListener.onSaveSelected(DiscoverSaveDialog.this, discoverName);
				}
			}
		});
	}

	
}
