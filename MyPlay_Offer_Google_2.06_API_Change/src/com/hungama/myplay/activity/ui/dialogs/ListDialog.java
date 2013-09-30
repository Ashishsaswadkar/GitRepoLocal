package com.hungama.myplay.activity.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.hungama.myplay.activity.R;

public class ListDialog extends Dialog {
	
	private static final String TAG = "ListDialog";
	
	/**
	 * Adapter interface to present objects in the dialog.
	 */
	public interface ListDialogItem {
		
		/**
		 * Retrieves the id of the object.
		 */
		public long getId();
		
		/**
		 * Retrieves the presential string of the object.
		 */
		public String getName();
	}
	
	public interface OnListDialogStateChangedListener {
		
		public void onCancelled();
		
		public void onItemSelected(ListDialogItem listDialogItem, int position);
	}
	
	private OnListDialogStateChangedListener mOnListDialogStateChangedListener = null;
	private List<ListDialogItem> mListDialogItems = new ArrayList<ListDialog.ListDialogItem>();
	
	private Resources mResources;
	
	private TextView mTextTitle;
	private ImageButton mButtonClose;
	private ListView mListViewItems;
	
	private int mContentMarging;
	private int mListItemHeight;
	
	private ListDialogAdapter mListDialogAdapter;
	
	
	// ======================================================
	// Public methods.
	// ======================================================

	public ListDialog(Context context) {
		super(context);
		
		mResources = getContext().getResources();
		mContentMarging = mResources.getDimensionPixelSize(R.dimen.dialog_content_margin);
		mListItemHeight = mResources.getDimensionPixelSize(R.dimen.dialog_list_item_height);
		
		// initialize dialog controls.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_list_dialog);
		
		setCancelable(false);
		setCanceledOnTouchOutside(false);
		
		mTextTitle = (TextView) findViewById(R.id.list_dialog_title_text);
		mButtonClose = (ImageButton) findViewById(R.id.list_dialog_title_button_close);
		mListViewItems = (ListView) findViewById(R.id.list_dialog_list);
		
		mButtonClose.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				cancel();
				if (mOnListDialogStateChangedListener != null) {
					mOnListDialogStateChangedListener.onCancelled();
				}
			}
		});
		
		mListDialogAdapter = new ListDialogAdapter();
		
		mListViewItems.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View itemView, int position, long id) {
				dismiss();
				if (mOnListDialogStateChangedListener != null) {
					mOnListDialogStateChangedListener.onItemSelected(mListDialogItems.get(position), position);
				}
			}
		});
		
		mListViewItems.setAdapter(mListDialogAdapter);
	}
	
	public void setItems(List<ListDialogItem> dialogItems){
		mListDialogItems = dialogItems;
		mListDialogAdapter.notifyDataSetChanged();
	}
	
	public void setOnListDialogStateChangedListener(OnListDialogStateChangedListener listener) {
		mOnListDialogStateChangedListener = listener;
	}
	
	@Override
	public void setTitle(CharSequence title) {
		mTextTitle.setText(title);
	}
	
	@Override
	public void setTitle(int titleId) {
		mTextTitle.setText(titleId);
	}
	
	private class ListDialogAdapter extends BaseAdapter {
		
		private final float textSize;
		
		public ListDialogAdapter() {
			textSize = mResources.getDimension(R.dimen.dialog_content_text_size);
		}

		@Override
		public int getCount() {
			return mListDialogItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mListDialogItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mListDialogItems.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			// builds the item's view.
			TextView text = new TextView(getContext());
			text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
			text.setTextColor(mResources.getColor(R.color.application_background_dark_grey));
			text.setBackgroundColor(mResources.getColor(R.color.white));
			text.setGravity(Gravity.CENTER_VERTICAL);
			text.setPadding(mContentMarging, 0, mContentMarging, 0);
			text.setSingleLine(true);
			text.setEllipsize(TruncateAt.END);
			
			AbsListView.LayoutParams params = 
					new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mListItemHeight);
			
			text.setLayoutParams(params);
			
			// populates it.
			ListDialogItem listDialogItem = (ListDialogItem) getItem(position);
			text.setText(listDialogItem.getName());
			
			return text;
		}
		
	}
	

}
