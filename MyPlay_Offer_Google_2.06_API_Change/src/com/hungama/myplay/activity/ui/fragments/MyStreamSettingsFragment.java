/**
 * 
 */
package com.hungama.myplay.activity.ui.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MyStreamSettingsResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MyStreamSettingsOperation;
import com.hungama.myplay.activity.ui.adapters.SettingsAdapter;

/**
 * @author DavidSvilem
 *
 */
public class MyStreamSettingsFragment extends Fragment implements CommunicationOperationListener,
																  OnClickListener 	{

	private static final String TAG = "MyStreamSettingsFragment";
	
	public static final String MUSICLISTEN = "musiclisten";
	public static final String LIKES = "likes";
	public static final String DOWNLOADS = "downloads";
	public static final String COMMENTS = "comments";
	public static final String VIDEOWATCHED = "videowatched";
	public static final String SHARES = "shares";
	public static final String BADGES  = "badges";
	
	// Views
	private ListView listview;
	private ProgressDialog mProgressDialog;
	// Data Members
	private Map<String, Integer> settingsMap;
	// Managers 
	private DataManager mDataManager;
	// Adapter
	private SettingsAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// Fetch the root view
		View rootView = inflater.inflate(R.layout.fragment_my_stream_settings, container, false);
		
		listview = (ListView) rootView.findViewById(R.id.listview);
		
		mDataManager.getMyStreamSettings(this, false, "", 0);
		
		return rootView;
	}
	
	// Dialog help methods
	public void showLoadingDialog(String message) {
		if (!getActivity().isFinishing()) {
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(getActivity());
				mProgressDialog = ProgressDialog.show(getActivity(), "", message, true, true);
			}
		}
	}

	public void hideLoadingDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	@Override
	public void onStart(int operationId) {
		showLoadingDialog(getActivity().getResources().getString(R.string.processing));
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		
		switch (operationId) {
		case (OperationDefinition.Hungama.OperationId.MY_STREAM_SETTINGS):
			
			MyStreamSettingsResponse response = 
			(MyStreamSettingsResponse) responseObjects.get(MyStreamSettingsOperation.RESULT_KEY_MY_STREAM_SETTINGS);
	
			// Build the map for the adapter
			settingsMap = new HashMap<String, Integer>();
			
			String[] keys = getResources().getStringArray(R.array.my_stream_settings_properties);
			
			settingsMap.put(keys[0], response.data.musiclisten);
			settingsMap.put(keys[1], response.data.likes);
			settingsMap.put(keys[2], response.data.downloads);
			settingsMap.put(keys[3], response.data.comments);
			settingsMap.put(keys[4], response.data.videowatched);
			settingsMap.put(keys[5], response.data.shares);
			settingsMap.put(keys[6], response.data.badges);
			
			List<String> propList = new ArrayList<String>();
			
			propList = Arrays.asList(keys);  
			
			adapter = new SettingsAdapter(getActivity().getApplicationContext(), propList, settingsMap, this);
			
			TextView headerView = 
					(TextView) ((LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.settings_title_row_layout, null, false);
			
			headerView.setText(R.string.show_feeds_of_);
			
			listview.addHeaderView(headerView);
			
			listview.setAdapter(adapter);
			
			hideLoadingDialog();
		
			break;

		case (OperationDefinition.Hungama.OperationId.MY_STREAM_SETTINGS_UPDATE):
			
			hideLoadingDialog();
		
			break;
		default:
			break;
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		hideLoadingDialog();
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.toggle_button:
			
			ToggleButton tb = (ToggleButton) v;
			
			String str = (String) v.getTag();
			
			updateMyStreamSettings(str, tb.isChecked());
			
			break;

		default:
			break;
		}
	}
	
	private void updateMyStreamSettings(String key, boolean value){
		
		int state;
		String streamSettingType = "";
		
		if(key.equalsIgnoreCase(getActivity().getString(R.string.mystream_settings_music_listened_to))){
			streamSettingType = MUSICLISTEN;
		}else if(key.equalsIgnoreCase(getActivity().getString(R.string.mystream_settings_likes))){
			streamSettingType = LIKES;
		}else if(key.equalsIgnoreCase(getActivity().getString(R.string.mystream_settings_downlods))){
			streamSettingType = DOWNLOADS;
		}else if(key.equalsIgnoreCase(getActivity().getString(R.string.mystream_settings_comments))){
			streamSettingType = COMMENTS;
		}else if(key.equalsIgnoreCase(getActivity().getString(R.string.mystream_settings_videos_watched))){
			streamSettingType = VIDEOWATCHED;
		}else if(key.equalsIgnoreCase(getActivity().getString(R.string.mystream_settings_shares))){
			streamSettingType = SHARES;
		}else if(key.equalsIgnoreCase(getActivity().getString(R.string.mystream_settings_badges_earned))){
			streamSettingType = BADGES;
		}
		
		if(value){
			state = 1;
		}else{
			state = 0;
		}
		
		mDataManager.getMyStreamSettings(this, true, streamSettingType , state);

	}
	
}
