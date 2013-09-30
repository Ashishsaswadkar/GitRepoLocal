package com.hungama.myplay.activity.ui.adapters;

import java.util.List;
import java.util.Map;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.DataManager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SettingsAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<String> list;
	private Map<String, Integer> map;
	private Context context;
	private Fragment mFragmnet;
	private DataManager mDataManager;
	
	public SettingsAdapter(Context context, List<String> settings, Map<String, Integer> map, Fragment fragment){
		
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.list = settings;
		this.map = map;
		this.context = context;
		//this.mDataManager = dataManager;
		this.mFragmnet = fragment;
	}
	
	@Override
	public int getCount() {
		return list != null ? list.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
					
		convertView = mInflater.inflate(R.layout.settings_row_layout, parent, false);
		
		TextView property = (TextView) convertView.findViewById(R.id.property);
		final ToggleButton button = (ToggleButton) convertView.findViewById(R.id.toggle_button);
		
		final int pos = position;
		
//		button.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				
//				Toast.makeText(context, pos + " " + button.isChecked() , Toast.LENGTH_LONG).show();
//				mDataManager.getShareSettings((CommunicationOperationListener) mActivity, null);
//			}
//		});
		
		button.setOnClickListener((OnClickListener) mFragmnet);
		
		String value = (String) list.get(position);
		property.setText(value);
		
		button.setTag(value);
		button.setChecked(map.get(value) == 1 ? true : false);
		
		return convertView;
	}

	private static class ViewHolder {
		TextView property;
		ToggleButton toggleButton;
	}

}
