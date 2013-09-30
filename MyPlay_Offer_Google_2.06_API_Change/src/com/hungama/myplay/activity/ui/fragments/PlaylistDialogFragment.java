package com.hungama.myplay.activity.ui.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.persistance.InventoryContract;
import com.hungama.myplay.activity.data.persistance.Itemable;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.JsonRPC2Methods;
import com.hungama.myplay.activity.operations.hungama.HungamaOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.playlist.PlaylistManager;
import com.hungama.myplay.activity.playlist.PlaylistOperation;
import com.hungama.myplay.activity.playlist.PlaylistsAdapter;
import com.hungama.myplay.activity.ui.DownloadActivity;
import com.hungama.myplay.activity.ui.listeners.OnLoadMenuItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Utils;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Lets the user select in which playlist he would like to save the given tracks.
 */
public class PlaylistDialogFragment extends DialogFragment implements CommunicationOperationListener ,
																	  OnClickListener, 
																	  OnItemClickListener{

	private final static String TRACK_LIST = "track_list";
	
	public static final String FRAGMENT_TAG = "PlaylistDialogFragment";
	
	public interface OnPlaylistPerformActionListener {
		
		public void onCanceled();
		
		public void onSuccessed();
		
		public void onFailed();
	}
	
	// Layout members
	private TextView title;
	private Button saveButton;
	private ImageButton closeButton;
	private EditText playlistEditText;
	private ListView listview;
	
	private ProgressDialog mProgressDialog;
	
	private DataManager mDataManager;
	private PlaylistManager mPlaylistManager;
	
	private PlaylistsAdapter mAdapter;
	private List<Playlist> list = new ArrayList<Playlist>();
	
	private List<Playlist> playlists;
	private List<Track> tracks;
	private static boolean mIsFromLoadMenu;
		
	private OnPlaylistPerformActionListener mOnPlaylistPerformActionListener = null;
	
	private OnLoadMenuItemOptionSelectedListener mOnLoadMenuItemOptionSelectedListener = null; 
	
    public static PlaylistDialogFragment newInstance(List<Track> tracks, boolean isFromLoadMenu) {
    	PlaylistDialogFragment f = new PlaylistDialogFragment();
    	mIsFromLoadMenu = isFromLoadMenu;
        // Supply data input as an argument.
        Bundle args = new Bundle();
        args.putSerializable(TRACK_LIST, new ArrayList<Track>(tracks));
        
        f.setArguments(args);

        return f;
    }
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, com.actionbarsherlock.R.style.Sherlock___Theme_Dialog);
		
		tracks = (List<Track>) getArguments().getSerializable(TRACK_LIST);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_playlist_dialog, container);

		saveButton = (Button) view.findViewById(R.id.save_button);
		
		playlistEditText = (EditText)view.findViewById(R.id.play_list_edit_text);
		
		closeButton = (ImageButton) view.findViewById(R.id.close_button);
		closeButton.setOnClickListener(this);
		
		listview = (ListView)view.findViewById(R.id.list_view);
		listview.setOnItemClickListener(this);
		
		title = (TextView) view. findViewById(R.id.download_custom_dialog_title_text);
		
		if (mIsFromLoadMenu) {
			saveButton.setVisibility(View.GONE);
			playlistEditText.setVisibility(View.GONE);
			title = (TextView) view. findViewById(R.id.download_custom_dialog_title_text);
			title.setText(getResources().getString(R.string.player_load_menu_my_playlist_dialog_title_load));
		} else {
			saveButton.setVisibility(View.VISIBLE);
			saveButton.setOnClickListener(this);
			title.setText(getResources().getString(R.string.player_load_menu_my_playlist_dialog_title_add));
			playlistEditText.setVisibility(View.VISIBLE);
		}
		
		return view; 
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		
		mPlaylistManager = PlaylistManager.getInstance(getActivity().getApplicationContext()); 
		
//		mDataManager.storeItemables(new HashMap<Long, Playlist>());
//		mDataManager.storeTracks(new HashMap<Long, Track>());
		
		// Get all playlists
		Playlist dummy = new Playlist();
		Map<Long, Playlist> map = mDataManager.getStoredPlaylists();
		
		if(map != null){
			// Convert from Map<Long, Playlist> to List<Itemable>  
			for(Map.Entry<Long, Playlist> p : map.entrySet()){
				list.add(p.getValue());
			}
		}
		
		if(list != null && list.isEmpty()){
			
			Toast emptyListToast = Toast.makeText(getActivity().getApplicationContext(),
					R.string.you_do_not_have_any_playlist_saved, Toast.LENGTH_LONG);
			
			emptyListToast.setGravity(Gravity.CENTER, 0, 0);
			
			emptyListToast.show();
			
			if (mIsFromLoadMenu) {
				this.dismiss();
			}			
			
		}
		
		if(list != null && !list.isEmpty()){
			mAdapter = new PlaylistsAdapter(getActivity(), list);
			listview.setAdapter(mAdapter);
		}else{
			listview.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		
		case R.id.save_button:
			
			// "SAVE" button is clicked -> then need to create a new Playlist
			
			// Create
			String newPlaylistName = playlistEditText.getText().toString();
			
			newPlaylistName = newPlaylistName.trim();
			
			if(!TextUtils.isEmpty(newPlaylistName)){
				
				String tracksStr = tracksStringBuilder(tracks);
				mDataManager.playlistOperation(this, 0, newPlaylistName , tracksStr, JsonRPC2Methods.CREATE);
				
//				if (Utils.isAlphaNumeric(newPlaylistName)) {
//					String tracksStr = tracksStringBuilder(tracks);
//					mDataManager.playlistOperation(this, 0, newPlaylistName , tracksStr, JsonRPC2Methods.CREATE);
//				} else {
//					Toast toast = Toast.makeText(getActivity(), R.string.new_playist_error_alert_not_alphnumeric, Toast.LENGTH_LONG);
//					toast.setGravity(Gravity.CENTER, 0, 0);
//					toast.show();
//				}
				
			}else{
				Toast.makeText(getActivity(), R.string.new_playist_error_alert, Toast.LENGTH_LONG).show();
				
				if (mOnPlaylistPerformActionListener != null) {
					mOnPlaylistPerformActionListener.onFailed();
				}
			}
			
			break;
			
		case R.id.close_button:
			
			dismiss();
			
			if (mOnPlaylistPerformActionListener != null) {
				mOnPlaylistPerformActionListener.onCanceled();
			}
			
			break;
			
		default:
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		if (mIsFromLoadMenu) {
						
			Playlist playlist = (Playlist) list.get(position);
			
			// Get Tracks by Playlist
//			mPlaylistManager.getTracksListByPlaylist(playlist);
			
			// Add Tracks to queue
			if (mOnLoadMenuItemOptionSelectedListener != null) {
				mOnLoadMenuItemOptionSelectedListener.onLoadPlaylistFromDialogSelected(mPlaylistManager.getTracksListByPlaylist(playlist));
			}
			dismiss();
			
		} else {
		
			// list item is clicked -> then need to add the track to the clicked Playlist
			
			// Update
			Boolean trackAdded = false;
			Playlist playlist = (Playlist) list.get(position);
			trackAdded = playlist.addTracksList(tracks);
			if(trackAdded){
				mDataManager.playlistOperation(this, playlist.getId(), playlist.getName(), playlist.getTrackList(), JsonRPC2Methods.UPDATE);				
			}else{
				Toast.makeText(getActivity(), R.string.song_already_exists_in_playlist, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	@Override
	public void onStart(int operationId) {
		switch (operationId) {
		case OperationDefinition.CatchMedia.OperationId.PLAYLIST:
			showLoadingDialog(getActivity().getString(R.string.processing));
			break;
			
		case OperationDefinition.Hungama.OperationId.MEDIA_DETAILS:
			showLoadingDialog(getActivity().getString(R.string.loading_playlist));
			break;
			
		default:
			break;
		}
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.DialogFragment#onStop()
	 */
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		FlurryAgent.onEndSession(getActivity());
	}
	
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		
		switch (operationId) {
		case (OperationDefinition.CatchMedia.OperationId.PLAYLIST):
			
			Map<String, Object> response = (Map<String, Object>) responseObjects.get(PlaylistOperation.RESPONSE_KEY_PLAYLIST);
			JsonRPC2Methods methodType = (JsonRPC2Methods) responseObjects.get(PlaylistOperation.RESPONSE_KEY_METHOD_TYPE);
			
			// Convert from List<Track> to Map<Long, Track>
			Map<Long, Track> map = mDataManager.getStoredTracks();
			if(map == null){
				map = new HashMap<Long, Track>();
			}
			
			// Add the Tracks to map
			for(Track t : tracks){
				map.put(t.getId(), t);
			}
			
			// Store the Tracks in cache
			mDataManager.storeTracks(map);

			if(methodType != null){
				
				if(methodType == JsonRPC2Methods.CREATE){
					
					// Badges and Coins
					Object contentId = (Object) response.get("playlist_id");
					
					if(contentId != null){
						String contentIdStr = String.valueOf(contentId);
						
						mDataManager.checkBadgesAlert(contentIdStr,
								MediaType.PLAYLIST.toString().toLowerCase(), 
								"create_playlist", 
								this);	
					}
					
					Toast.makeText(getActivity(), R.string.song_s_added_to_your_playlist , Toast.LENGTH_LONG)
					.show();
					
				}else if(methodType == JsonRPC2Methods.UPDATE){
									
					Toast.makeText(getActivity(), R.string.song_s_added_to_your_playlist , Toast.LENGTH_LONG)
							.show();
				}
			}
			
			dismiss();
		
			if (mOnPlaylistPerformActionListener != null) {
				mOnPlaylistPerformActionListener.onSuccessed();
			}
			
			break;
			
		default:
			break;
		}
		
		hideLoadingDialog();
		
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		
		switch (operationId) {
		case OperationDefinition.CatchMedia.OperationId.PLAYLIST:
			dismiss();
			
			hideLoadingDialog();
			
			if (mOnPlaylistPerformActionListener != null) {
				mOnPlaylistPerformActionListener.onFailed();
			}
			
			if (getActivity() != null) {
				Toast taost = Toast.makeText(getActivity().getApplicationContext(), errorMessage, Toast.LENGTH_LONG);
				taost.setGravity(Gravity.CENTER, 0, 0);
				taost.show();
			}
			break;
			
		case OperationDefinition.Hungama.OperationId.MEDIA_DETAILS:
			dismiss();
			hideLoadingDialog();
			break;
			
		default:
			break;
		}
		
	}
	
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

	// Build string of track id's from a list of Track's
	public String tracksStringBuilder(List<Track> tracks){
		
		StringBuilder tracksStr = new StringBuilder();
		
		for(Track t : tracks){
			tracksStr.append(t.getId()).append(" ");
		}
		
		return tracksStr.toString().trim();
	}
	
	public void setOnPlaylistPerformActionListener(OnPlaylistPerformActionListener listener) {
		mOnPlaylistPerformActionListener = listener;
	}
	
	public void setOnLoadMenuItemOptionSelectedListener(OnLoadMenuItemOptionSelectedListener listener) {
		mOnLoadMenuItemOptionSelectedListener = listener;
	}

	
}
