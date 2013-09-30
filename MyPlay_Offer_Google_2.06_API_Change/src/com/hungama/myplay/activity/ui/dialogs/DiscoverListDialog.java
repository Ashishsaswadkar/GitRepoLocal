/**
 * 
 */
package com.hungama.myplay.activity.ui.dialogs;

import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.DiscoverRetrieveOperation;
import com.hungama.myplay.activity.util.Utils;

/**
 * Loads and shows List of Discoveries for the given user.
 */
public class DiscoverListDialog extends ListDialog implements CommunicationOperationListener {
	
	private DataManager mDataManager;
	
	private ProgressDialog mProgressDialog;
	private Context mContext;
	
	
	// ======================================================
	// Public.
	// ======================================================
	
	public DiscoverListDialog(String userId, Context context) {
		super(context);
		
		mContext = context;
		
		mDataManager = DataManager.getInstance(context.getApplicationContext());
		
		// sets the title.
		setTitle(R.string.discovery_options_load_my_discoveries);
		
		mDataManager.getDiscoveries(userId, this);
	}
	
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_RETRIEVE) {
			String message = mContext.getResources().getString(R.string.application_dialog_loading);
			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setTitle(Utils.TEXT_EMPTY);
			mProgressDialog.setMessage(message);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
			
			mProgressDialog.show();
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_RETRIEVE) {
			List<ListDialogItem> discoveries = (List<ListDialogItem>) 
					responseObjects.get(DiscoverRetrieveOperation.RESULT_KEY_DISCOVERIES);
			
			if (discoveries != null && discoveries.isEmpty()) {				
				this.dismiss();				
			} else {				
				setItems(discoveries);				
			}
			
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
	    		mProgressDialog = null;
			}
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_RETRIEVE) {
			
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
	    		mProgressDialog = null;
			}
			
    		if (errorType != ErrorType.OPERATION_CANCELLED && !TextUtils.isEmpty(errorMessage)) {
    			Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
    			this.dismiss();		
    		}
		}
	}
	
	
}
