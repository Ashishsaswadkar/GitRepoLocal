package com.hungama.myplay.activity.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragment;
import com.hungama.myplay.activity.R;

/**
 * Adds to any descendant to the ability to show a loading dialog.  
 */
public class MainFragment extends SherlockFragment {

	private FragmentManager mFragmentManager;
	private LoadingDialogFragment mLoadingDialogFragment = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFragmentManager = getFragmentManager();
	}
	
	// ======================================================
	// Dialog helper methods.
	// ======================================================
	
	public void showLoadingDialog(int messageResource) {
		
		if (mLoadingDialogFragment == null && getActivity() != null && !getActivity().isFinishing()) {
					
			mLoadingDialogFragment = LoadingDialogFragment.newInstance(R.string.application_dialog_loading_content);
			mLoadingDialogFragment.setCancelable(true);
        	mLoadingDialogFragment.show(mFragmentManager, LoadingDialogFragment.FRAGMENT_TAG);

//			// For avoiding perform an action after onSaveInstanceState.
//			new Handler().post(new Runnable() {
//				
//		        public void run() {
//		        	mLoadingDialogFragment = LoadingDialogFragment.newInstance(R.string.application_dialog_loading_content);
//					mLoadingDialogFragment.setCancelable(true);
//		        	mLoadingDialogFragment.show(mFragmentManager, LoadingDialogFragment.FRAGMENT_TAG);
//		        	
//		        }
//		    });							
		}
	}
	
	protected void showLoadingDialogWithoutVisibleCheck(int messageResource) {
		
		if (mLoadingDialogFragment == null && getActivity() != null && !getActivity().isFinishing()) {
					
			mLoadingDialogFragment = LoadingDialogFragment.newInstance(R.string.application_dialog_loading_content);
			mLoadingDialogFragment.setCancelable(true);
        	mLoadingDialogFragment.show(mFragmentManager, LoadingDialogFragment.FRAGMENT_TAG);
			
//			// For avoiding perform an action after onSaveInstanceState.
//			new Handler().post(new Runnable() {
//				
//		        public void run() {
//		        	
//		        	
//		        }
//		    });							
		}
	}
	
	protected void hideLoadingDialog() {
		
		if (mLoadingDialogFragment != null && getActivity() != null && !getActivity().isFinishing()) {
			
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.remove(mLoadingDialogFragment);
			fragmentTransaction.commitAllowingStateLoss();
			mLoadingDialogFragment = null;
			
//			// For avoiding perform an action after onSaveInstanceState.
//			new Handler().post(new Runnable() {
//				
//		        public void run() {
//		        	
//		        	
//		        }
//		    });	
		}
	}
	
//	private void showLoadingDialogFragment() {
//		
//		FragmentManager fragmentManager = getFragmentManager();
//		
//		if (fragmentManager != null) {
//			
//			Fragment fragment = fragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
//			
//			if (fragment == null) {
//				
//				LoadingDialogFragment dialogFragment = LoadingDialogFragment.newInstance(R.string.application_dialog_loading);
//				dialogFragment.setCancelable(true);
//				dialogFragment.show(fragmentManager, LoadingDialogFragment.FRAGMENT_TAG);
//			}
//		}						
//	}
//	
//	private void hideLoadingDialogFragment() {
//		
//		FragmentManager fragmentManager = getFragmentManager();
//		
//		if (fragmentManager != null) {
//			
//			Fragment fragment = fragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
//			
//			if (fragment != null) {
//				
//				DialogFragment fragmentDialog = (DialogFragment) fragment;
//				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//				fragmentTransaction.remove(fragmentDialog);
//				fragmentDialog.dismissAllowingStateLoss();
//			}
//		}
//	}

}
