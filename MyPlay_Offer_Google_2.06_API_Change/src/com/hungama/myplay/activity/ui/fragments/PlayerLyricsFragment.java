package com.hungama.myplay.activity.ui.fragments;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.TrackLyrics;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.TrackLyricsOperation;

public class PlayerLyricsFragment extends Fragment implements CommunicationOperationListener {
	
	private static final String TAG = "PlayerLyricsFragment";
	
	public static final String FRAGMENT_ARGUMENT_TRACK = "fragment_argument_track";
	
	private DataManager mDataManager;
	private Track mTrack = null;
	private TrackLyrics mTrackLyrics = null;
	
	private RelativeLayout mTitleBar;
	private TextView mTextTitle;
	private TextView mTextLyrics;
	private Button mButtonShare;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		
		// gets the track to load.
		Bundle data = getArguments();
		if (data != null && data.containsKey(FRAGMENT_ARGUMENT_TRACK)) {
			mTrack = (Track) data.getSerializable(FRAGMENT_ARGUMENT_TRACK);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_player_lyrics, container, false);
		
		mTitleBar = (RelativeLayout) rootView.findViewById(R.id.player_lyrics_title_bar);
		mTextTitle = (TextView) rootView.findViewById(R.id.player_lyrics_title_bar_text);
		mTextLyrics = (TextView) rootView.findViewById(R.id.player_lyrics_text);
		mButtonShare = (Button) rootView.findViewById(R.id.player_lyrics_title_bar_button_share);
		
		mTitleBar.setVisibility(View.INVISIBLE);
		
		mButtonShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
			
				// Prepare data for ShareDialogFragmnet
				Map<String , Object> shareData = new HashMap<String, Object>();
				shareData.put(ShareDialogFragment.TITLE_DATA, mTrack.getTitle());
				shareData.put(ShareDialogFragment.SUB_TITLE_DATA, mTrack.getAlbumName());
				shareData.put(ShareDialogFragment.THUMB_URL_DATA, mTrack.getBigImageUrl());
				shareData.put(ShareDialogFragment.MEDIA_TYPE_DATA, MediaType.TRACK);
				shareData.put(ShareDialogFragment.EDIT_TEXT_DATA, mTextLyrics.getText());
				shareData.put(ShareDialogFragment.CONTENT_ID_DATA, mTrack.getId());
				shareData.put(ShareDialogFragment.TYPE_DATA, ShareDialogFragment.LYRICS);
				
				// Show ShareFragmentActivity
				ShareDialogFragment shareDialogFragment = ShareDialogFragment.newInstance(shareData);
				
				FragmentManager mFragmentManager = getFragmentManager();
				shareDialogFragment.show(mFragmentManager, ShareDialogFragment.FRAGMENT_TAG);
				
			}
		});
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (mTrack != null && mTrackLyrics == null) {
			mDataManager.getTrackLyrics(mTrack, this);
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		mDataManager.cancelGetTrackLyrics();
	}

	@Override
	public void onStart(int operationId) { }

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.TRACK_LYRICS) {
			mTrackLyrics = (TrackLyrics) responseObjects.get(TrackLyricsOperation.RESPONSE_KEY_TRACK_LYRICS);
			if (mTrackLyrics != null) {
				mTitleBar.setVisibility(View.VISIBLE);
				//mTextTitle.setText(mTrackLyrics.getTitle());
				mTextLyrics.setText(mTrackLyrics.getLyrics());
			}
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) { }
}
