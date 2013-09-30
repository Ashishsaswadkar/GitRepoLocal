package com.hungama.myplay.activity.ui;

import java.io.Serializable;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.ui.fragments.BrowseRadioFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.fragments.PlaylistDialogFragment;
import com.hungama.myplay.activity.ui.fragments.RadioDetailsFragment;
import com.hungama.myplay.activity.ui.fragments.PlaylistDialogFragment.OnPlaylistPerformActionListener;

/**
 * Allows to browse for the available web radios from Hungama, play and show their details.
 */
public class RadioActivity extends MainActivity {
	
	private static final String TAG = "RadioActivity";
	
	public static final String EXTRA_SHOW_DETAILS_MEDIA_ITEM = "extra_show_details_media_item";
	public static final String EXTRA_SHOW_DETAILS_CATEGORY_TYPE = "extra_show_details_category_type";
	
	private FragmentManager mFragmentManager;
	
	private boolean mIfDetailsRequestedImmediately = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/*
		 * Checks if the calling intent contains a MediaItem to
		 * show its Radio Details and inflates the Radio Details Fragment for it.
		 * If not, shows the brows Radio channels as usual. 
		 */
		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.containsKey(EXTRA_SHOW_DETAILS_MEDIA_ITEM) 
						   && bundle.containsKey(EXTRA_SHOW_DETAILS_CATEGORY_TYPE)) {
			MediaItem mediaItem = (MediaItem) bundle.getSerializable(EXTRA_SHOW_DETAILS_MEDIA_ITEM);
			MediaCategoryType mediaCategoryType = (MediaCategoryType) bundle.getSerializable(EXTRA_SHOW_DETAILS_CATEGORY_TYPE);
			
			mIfDetailsRequestedImmediately = true;
			
			showDetailsOfRadio(mediaItem, mediaCategoryType);
			
		} else {
			
			// shows by default the tiles of radio channels.
			mFragmentManager = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit,
	                R.anim.slide_right_enter,
	                R.anim.slide_right_exit);
			
			BrowseRadioFragment radioFragment = new BrowseRadioFragment();
			
			fragmentTransaction.add(R.id.main_fragmant_container, radioFragment);
			fragmentTransaction.commit();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		FlurryAgent.onPageView();
		FlurryAgent.logEvent("Live Radio");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.RADIO;
	}
	
	public void showDetailsOfRadio(final MediaItem mediaItem, final MediaCategoryType mediaCategoryType) {
		
		PlayerBarFragment playerBarFragment = getPlayerBar();
		
		if (playerBarFragment.getPlayMode() == PlayMode.MUSIC && (playerBarFragment.isPlaying() || playerBarFragment.isLoading())) {
			
			// show dialog.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.radio_confirm_dialog_title)
				   .setCancelable(true)
				   .setPositiveButton(R.string.radio_confirm_dialog_play_channel, 
						   						new DialogInterface.OnClickListener() {
					   
			           public void onClick(DialogInterface dialog, int id) {
			        	   
							// shows the radio details for the given item. 
							showDetailsOfRadioHelper(mediaItem, mediaCategoryType);
			           }
			           
			       }).setNegativeButton(R.string.radio_confirm_dialog_save_them, 
			    		   						new DialogInterface.OnClickListener() {
			    	   
			           public void onClick(DialogInterface dialog, int id) {
			               // open the playlists dialog.
			        	   List<Track> tracks = getPlayerBar().getCurrentPlayingList(); 
			        	   boolean isFromLoadMenu = false;
			        	   PlaylistDialogFragment playlistDialogFragment = PlaylistDialogFragment.newInstance(tracks, isFromLoadMenu);
			        	   playlistDialogFragment.setOnPlaylistPerformActionListener(new OnPlaylistPerformActionListener() {
							
							@Override
							public void onSuccessed() {
								// shows the radio details for the given item. 
								showDetailsOfRadioHelper(mediaItem, mediaCategoryType);
							}
							
							@Override
							public void onFailed() {
								if (mIfDetailsRequestedImmediately) {
									finish();
								}
							}
							
							@Override
							public void onCanceled() {
								if (mIfDetailsRequestedImmediately) {
									finish();
								}
							}
							
						});
			        	   
			        	   playlistDialogFragment.show(mFragmentManager, PlaylistDialogFragment.FRAGMENT_TAG);
			           }
			       });
			
			builder.show();
			
		} else {
			// shows the radio details for the given item. 
			showDetailsOfRadioHelper(mediaItem, mediaCategoryType);
		}
	
	}
	
	private void showDetailsOfRadioHelper(MediaItem mediaItem, MediaCategoryType mediaCategoryType) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		
		RadioDetailsFragment radioDetailsFragment = new RadioDetailsFragment();
		// sets it's arguments.
		Bundle data = new Bundle();
		data.putSerializable(RadioDetailsFragment.EXTRA_MEDIA_ITEM, (Serializable) mediaItem);
		data.putSerializable(RadioDetailsFragment.EXTRA_CATEGORY_TYPE, (Serializable) mediaCategoryType);
		data.putBoolean(RadioDetailsFragment.EXTRA_DO_SHOW_TITLE_BAR, true);
		data.putBoolean(RadioDetailsFragment.EXTRA_AUTO_PLAY, true);
		
		radioDetailsFragment.setArguments(data);
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
												R.anim.slide_left_exit,
								                R.anim.slide_right_enter,
								                R.anim.slide_right_exit);
		/*
		 * We want when the user goes to the Radio Details
		 * from external Activity and presses "back"to exit the whole activity.
		 */
		if (mIfDetailsRequestedImmediately) {
			fragmentTransaction.add(R.id.main_fragmant_container, radioDetailsFragment);
			fragmentTransaction.commit();
			
		} else {
			fragmentTransaction.replace(R.id.main_fragmant_container, radioDetailsFragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
	}

}
