package com.hungama.myplay.activity.ui.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Dialog that presents an indeterminate spinner with the some text.
 */
public class LoadingDialogFragment extends DialogFragment {
	
	public static final String FRAGMENT_TAG = "LOADING_DIALOG_FRAGMENT";
	public static final int FRAGMENT_ID = 1234567890;
	
	private static final String ARGUMENTS_KEY_MESSAGE = "arguments_key_message";
	
	public static LoadingDialogFragment newInstance(int message) {
		LoadingDialogFragment frag = new LoadingDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARGUMENTS_KEY_MESSAGE, message);
        frag.setArguments(args);
        return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		// creates the dialog itself.
		int messageId = getArguments().getInt(ARGUMENTS_KEY_MESSAGE);
		final ProgressDialog dialog = new ProgressDialog(getActivity());
	    dialog.setMessage(getResources().getString(messageId));
	    dialog.setIndeterminate(true);
	    dialog.setCancelable(false);
	    dialog.setCanceledOnTouchOutside(false);
	    return dialog;
	}
}
