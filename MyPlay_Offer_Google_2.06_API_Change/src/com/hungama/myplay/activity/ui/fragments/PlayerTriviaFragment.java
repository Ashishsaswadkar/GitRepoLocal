package com.hungama.myplay.activity.ui.fragments;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.TrackTrivia;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.TrackTriviaOperation;

public class PlayerTriviaFragment extends Fragment implements CommunicationOperationListener  {
	
	private static final String TAG = "PlayerTriviaFragment";
	
	public static final String FRAGMENT_ARGUMENT_TRACK = "fragment_argument_track";
	
	private DataManager mDataManager;
	private LayoutInflater mInflater;
	
	private Track mTrack = null;
	private TrackTrivia mTrackTrivia = null;
	
	private RelativeLayout mTitleBar;
	private TextView mTextTitle;
	private ListView mListBubbles;
	
	private BubblesAdapter mBubblesAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// gets the track to load.
		Bundle data = getArguments();
		if (data != null && data.containsKey(FRAGMENT_ARGUMENT_TRACK)) {
			mTrack = (Track) data.getSerializable(FRAGMENT_ARGUMENT_TRACK);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_player_trivia, container, false);
		
		mTitleBar = (RelativeLayout) rootView.findViewById(R.id.player_trivia_title_bar);
		mTextTitle = (TextView) rootView.findViewById(R.id.player_trivia_title_bar_text);
		mListBubbles = (ListView) rootView.findViewById(R.id.player_trivia_list);
		
		mTitleBar.setVisibility(View.INVISIBLE);
		mListBubbles.setVisibility(View.INVISIBLE);
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (mTrack != null && mTrackTrivia == null) {
			mDataManager.getTrackTrivia(mTrack, this);
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		mDataManager.cancelGetTrackTrivia();
	}

	@Override
	public void onStart(int operationId) {}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.TRACK_TRIVIA) {
			mTrackTrivia = (TrackTrivia) responseObjects.get(TrackTriviaOperation.RESULT_KEY_OBJECT_TRACK_TRIVIA);
			if (mTrackTrivia != null) {
				// now we can make them visible.
				mTitleBar.setVisibility(View.VISIBLE);
				mListBubbles.setVisibility(View.VISIBLE);
				
				// title.
				//mTextTitle.setText(mTrackTrivia.title);
				
				// bubbles - let the party begin.
				mBubblesAdapter = new BubblesAdapter();
				mListBubbles.setAdapter(mBubblesAdapter);
			}
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {}
	
	private class BubblesAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mTrackTrivia.trivia.size();
		}

		@Override
		public Object getItem(int position) {
			return mTrackTrivia.trivia.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			
			boolean isOddLocation = (position % 2 > 0) ? true : false ;
			
			if (isOddLocation) {
				// inflates the left bubble.
				convertView = mInflater.inflate(R.layout.list_item_player_trivia_bubble_left, container, false);
				
			} else {
				// inflates the right bubble.
				convertView = mInflater.inflate(R.layout.list_item_player_trivia_bubble_right, container, false);
			}
			
			// populates the bubble's view with the trivia.
			TextView textTrivia = (TextView) convertView.findViewById(R.id.player_trivia_text);
			final String triviaString = (String) getItem(position);
			textTrivia.setText(triviaString);
			
			Button buttonShare = (Button) convertView.findViewById(R.id.player_trivia_button_share);
			buttonShare.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// Show ShareDialogFragment
					showShareDialogFragment(triviaString);
				}
			});
			
			return convertView;
		}
	}
	
	public void showShareDialogFragment(String textTrivia){
		
		// Prepare data for ShareDialogFragmnet
		Map<String , Object> shareData = new HashMap<String, Object>();
		shareData.put(ShareDialogFragment.TITLE_DATA, mTrack.getTitle());
		shareData.put(ShareDialogFragment.SUB_TITLE_DATA, mTrack.getAlbumName());
		shareData.put(ShareDialogFragment.THUMB_URL_DATA, mTrack.getBigImageUrl());
		shareData.put(ShareDialogFragment.MEDIA_TYPE_DATA, MediaType.TRACK);
		shareData.put(ShareDialogFragment.EDIT_TEXT_DATA, textTrivia);
		shareData.put(ShareDialogFragment.CONTENT_ID_DATA, mTrack.getId());
		shareData.put(ShareDialogFragment.TYPE_DATA, ShareDialogFragment.TRIVIA);
		
		
		// Show ShareFragmentActivity
		ShareDialogFragment shareDialogFragment = ShareDialogFragment.newInstance(shareData);
		
		FragmentManager mFragmentManager = getFragmentManager();
		shareDialogFragment.show(mFragmentManager, ShareDialogFragment.FRAGMENT_TAG);
		
	}
	
	
}
