package com.hungama.myplay.activity.ui.fragments;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.WebRadioOperation;
import com.hungama.myplay.activity.ui.RadioActivity;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Utils;

public class BrowseRadioFragment extends Fragment implements OnClickListener, 
													CommunicationOperationListener, 
													OnMediaItemOptionSelectedListener {
	
	private static final String TAG = "BrowseRadioFragment";
	
	private DataManager mDataManager;
	
	private Button mTabButtonLiveRadio;
	private Button mTabButtonTopArtistsRadio;
	
	private List<MediaItem> mMediaItemsLiveRadio = null;
	private List<MediaItem> mMediaItemsTopArtists = null;
	
	private MediaCategoryType mCurrentMediaCategoryType = MediaCategoryType.LIVE_STATIONS;
	
	private Stack<Integer> mDataLoadingCountDown = null;
	private static final int COUNT_DONW_MAX = 2;

	// ======================================================
	// FRAGMENT'S LIFE CYCLE. 
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		
		/*
		 * This fragment loads the Live Radio and Top Artists asynchronously at the same time.
		 * To handle the "Loading" indication correctly, this fragment uses a count down
		 * that when it's size will reach zero, we will know for sure that both web services
		 * has been called and now it the time to show their data.   
		 */
		mDataLoadingCountDown = new Stack<Integer>();
		mDataLoadingCountDown.add(OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS);
		mDataLoadingCountDown.add(OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_radio, container, false);
		initializeControls(rootView);
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();

		// checks if the data has already been loaded from the server or not.
		if (mDataLoadingCountDown.size() == COUNT_DONW_MAX) {
			
			// loads the Live Radio items.
			mDataManager.getRadioLiveStations(this);
			// loads the Top Artist items.
			mDataManager.getRadioTopArtists(this);
		}  
		else if (mDataLoadingCountDown.size() == 0) {
			if (mCurrentMediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
				
				mTabButtonLiveRadio.setSelected(true);
				setContentForLiveRadio();
				
			} else if (mCurrentMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {
				
				
				mTabButtonTopArtistsRadio.setSelected(true);
				setContentForTopArtistRadio();
			}
		}
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key));
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		FlurryAgent.onEndSession(getActivity());
	}
	
	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		
		if (viewId == R.id.radio_tab_button_live) {
			
			if (!mTabButtonLiveRadio.isSelected()) {
				mTabButtonLiveRadio.setSelected(true);
			}
			
			if (mTabButtonTopArtistsRadio.isSelected()) {
				mTabButtonTopArtistsRadio.setSelected(false);
			}
			
			setContentForLiveRadio();
			
			FlurryAgent.logEvent("Live Radio");
			
		} else if (viewId == R.id.radio_tab_button_top_artist) {
			
			if (!mTabButtonTopArtistsRadio.isSelected()) {
				mTabButtonTopArtistsRadio.setSelected(true);
			}
			
			if (mTabButtonLiveRadio.isSelected()) {
				mTabButtonLiveRadio.setSelected(false);
			}
			
			setContentForTopArtistRadio();
			
			FlurryAgent.logEvent("Top Artist Radio");
		}
	}
	
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS || 
				operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS) {
			/*
			 * shows the loading indication only if the count down is freshed new.
			 */
			if (mDataLoadingCountDown.size() == COUNT_DONW_MAX) {
				showLoadingDialog(R.string.application_dialog_loading_content);
			
			}
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS || 
				operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS) {
			
			if (getActivity() == null || getActivity().isFinishing()) {
				return;
			}
			
			// gets the media items.
			List<MediaItem> mediaItems = (List<MediaItem>) responseObjects.get(WebRadioOperation.RESULT_KEY_OBJECT_MEDIA_ITEMS);
			
			if (operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS) {
				mMediaItemsLiveRadio = mediaItems;
			} else if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS) {
				mMediaItemsTopArtists = mediaItems;
			}
			
			// pops our counter.
			mDataLoadingCountDown.pop();
			
			if (Utils.isListEmpty(mDataLoadingCountDown)) {
				
				// sets the Live Radio as current presented tab. 
				mTabButtonLiveRadio.setSelected(true);
				setContentForLiveRadio();
				
				// hides the loading indicator.
				hideLoadingDialog();
			}
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS || 
				operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS) {
			
			mDataLoadingCountDown.pop();
			
			if (Utils.isListEmpty(mDataLoadingCountDown)) {
				hideLoadingDialog();
			}
		}
	}
	
	
	// ======================================================
	// PRIVATE HELPER METHODS.
	// ======================================================
	
	private void initializeControls(View rootView) {

		mTabButtonLiveRadio = (Button) rootView.findViewById(R.id.radio_tab_button_live);
		mTabButtonTopArtistsRadio = (Button) rootView.findViewById(R.id.radio_tab_button_top_artist);
		
		mTabButtonLiveRadio.setOnClickListener(this);
		mTabButtonTopArtistsRadio.setOnClickListener(this);
	}
	
	private void setContentForLiveRadio() {
		mCurrentMediaCategoryType = MediaCategoryType.LIVE_STATIONS;
		
		MediaTileGridFragment mediaItemsGridFragment = new MediaTileGridFragment();
		mediaItemsGridFragment.setOnMediaItemOptionSelectedListener(this);
		
		Bundle arguments = new Bundle();
		arguments.putSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS, (Serializable) mMediaItemsLiveRadio);
		mediaItemsGridFragment.setArguments(arguments);

		FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.radio_fragment_container, mediaItemsGridFragment);
		fragmentTransaction.commitAllowingStateLoss();
	}
	
	private void setContentForTopArtistRadio() {
		mCurrentMediaCategoryType = MediaCategoryType.TOP_ARTISTS_RADIO;
		
		MediaTileGridFragment mediaItemsGridFragment = new MediaTileGridFragment();
		mediaItemsGridFragment.setOnMediaItemOptionSelectedListener(this);
		
		Bundle arguments = new Bundle();
		arguments.putSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS, (Serializable) mMediaItemsTopArtists);
		mediaItemsGridFragment.setArguments(arguments);
		
		FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.radio_fragment_container, mediaItemsGridFragment);
		fragmentTransaction.commitAllowingStateLoss();
	}

	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position) {
		((RadioActivity) getActivity()).showDetailsOfRadio(mediaItem, mCurrentMediaCategoryType);
	}
	
	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem, int position) {
		((RadioActivity) getActivity()).showDetailsOfRadio(mediaItem, mCurrentMediaCategoryType);
	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem, int position) {
		((RadioActivity) getActivity()).showDetailsOfRadio(mediaItem, mCurrentMediaCategoryType);
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem, int position) {
		((RadioActivity) getActivity()).showDetailsOfRadio(mediaItem, mCurrentMediaCategoryType);
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position) {}
	
	// ======================================================
	// Inner dialog
	// ======================================================
	
	private LoadingDialogFragment mLoadingDialogFragment = null;
	
	protected void showLoadingDialog(int messageResource) {
		if (mLoadingDialogFragment == null && getActivity() != null && !getActivity().isFinishing()) {
			mLoadingDialogFragment = LoadingDialogFragment.newInstance(R.string.application_dialog_loading_content);
			mLoadingDialogFragment.setCancelable(true);
			mLoadingDialogFragment.show(getFragmentManager(), LoadingDialogFragment.FRAGMENT_TAG);
		}
	}
	
	protected void hideLoadingDialog() {
		if (mLoadingDialogFragment != null && getActivity() != null && !getActivity().isFinishing()) {
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			fragmentTransaction.remove(mLoadingDialogFragment);
			fragmentTransaction.commitAllowingStateLoss();
			mLoadingDialogFragment = null;
		}
	}
}
