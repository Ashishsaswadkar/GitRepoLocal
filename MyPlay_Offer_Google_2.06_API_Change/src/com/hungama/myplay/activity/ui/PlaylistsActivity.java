package com.hungama.myplay.activity.ui;

import java.util.ArrayList;
import java.util.List;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.ui.fragments.ItemableTilesFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlaylistsActivity extends MainActivity implements OnClickListener{

	private TextView mainTitleBarText;
	private ImageView mainTitleBarButtonOptions;
	private FrameLayout mainContainer;
	private LinearLayout playlistTracksOptions;
	private Button playAllButton;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_playlists);
		
		ItemableTilesFragment mTilesFragment = 
				new ItemableTilesFragment(MediaType.PLAYLIST, null);
		
		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit,
                R.anim.slide_right_enter,
                R.anim.slide_right_exit);
		
		fragmentTransaction.add(R.id.main_fragmant_container, mTilesFragment);
		fragmentTransaction.commit();
		
		mainTitleBarText = (TextView) findViewById(R.id.main_title_bar_text);
		mainTitleBarButtonOptions = (ImageView) findViewById(R.id.main_title_bar_button_options);
		mainTitleBarButtonOptions.setOnClickListener(this);
		mainContainer = (FrameLayout) findViewById(R.id.player_queue_content_container);
		playlistTracksOptions = (LinearLayout) findViewById(R.id.playlist_tracks_options);
		playAllButton = (Button) findViewById(R.id.playlist_tracks_play_all);
		playAllButton.setOnClickListener(this);
		
		
	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}
	
	public TextView getMainTitleBarText(){
		return mainTitleBarText;
	}

	public ImageView getMainTitleBarButtonOptions(){
		return mainTitleBarButtonOptions;
	}
	
	public FrameLayout getMainContainer() {
		return mainContainer;
	}
	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		FlurryAgent.onPageView();
		FlurryAgent.logEvent("My Playlists");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	
	@Override
	public void onClick(View view) {		
		int viewId = view.getId();		
		switch (viewId) {
		
		case R.id.main_title_bar_button_options:
			if (view.isSelected()) {
				// closes the option list.
				view.setSelected(false);
				view.setBackgroundResource(0);
				playlistTracksOptions.setVisibility(View.GONE);

				
			} else {
				// opens the option list.
				view.setSelected(true);
				view.setBackgroundResource(R.color.black);
				playlistTracksOptions.setVisibility(View.VISIBLE);

			}
			break;
			
		case R.id.playlist_tracks_play_all:
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(ItemableTilesFragment.TAG);
			if (fragment != null) {
				List<Track> mTracks = new ArrayList<Track>();
				mTracks = ((ItemableTilesFragment) fragment).getTracksToPlayAll();
				getPlayerBar().addToQueue(mTracks);
			
			break;

		}
	}
		
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (mainTitleBarButtonOptions != null && mainTitleBarButtonOptions.isSelected()) {
			mainTitleBarButtonOptions.setSelected(false);
			mainTitleBarButtonOptions.setBackgroundResource(0);
			playlistTracksOptions.setVisibility(View.GONE);
		}
		
	}
	
//	// ======================================================
//	// Options.
//	// ======================================================
//	
//	private class OptionsFragment extends Fragment {
//		
//		public static final String TAG = "ItemableTilesFragment.OptionsFragment";
//		
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_playlist_tracks_options, container, false);
//			
//			Button buttonPlayAll = (Button) rootView.findViewById(R.id.player_queue_options_save_as_playlist);
//			buttonPlayAll.setOnClickListener(ItemableTilesFragment);
//
//			return rootView;
//		}
//	}
}
