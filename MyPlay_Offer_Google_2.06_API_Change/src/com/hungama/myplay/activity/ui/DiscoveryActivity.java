package com.hungama.myplay.activity.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.CategoryTypeObject;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.Era;
import com.hungama.myplay.activity.data.dao.hungama.Genre;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.Tempo;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.DiscoverSaveOperation;
import com.hungama.myplay.activity.ui.dialogs.DiscoverListDialog;
import com.hungama.myplay.activity.ui.dialogs.DiscoverSaveDialog;
import com.hungama.myplay.activity.ui.dialogs.DiscoverSaveDialog.OnDiscoverSaveDialogStateChangedListener;
import com.hungama.myplay.activity.ui.dialogs.ListDialog.ListDialogItem;
import com.hungama.myplay.activity.ui.dialogs.ListDialog.OnListDialogStateChangedListener;
import com.hungama.myplay.activity.ui.fragments.DiscoveryCategoriesFragment;
import com.hungama.myplay.activity.ui.fragments.DiscoveryEraFragment;
import com.hungama.myplay.activity.ui.fragments.DiscoveryGalleryFragment;
import com.hungama.myplay.activity.ui.fragments.DiscoveryMoodFragment;
import com.hungama.myplay.activity.ui.fragments.DiscoveryTempoFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.fragments.DiscoveryCategoriesFragment.OnDoneButtonClickedListener;
import com.hungama.myplay.activity.ui.fragments.DiscoveryMoodFragment.OnMoodSelectedListener;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class DiscoveryActivity extends MainActivity implements OnMoodSelectedListener, 
									OnDoneButtonClickedListener, OnClickListener, 
									OnMediaItemOptionSelectedListener, CommunicationOperationListener {
	
	private static final String TAG = "DiscoveryActivity";
	
	public static final String DATA_EXTRA_DISCOVER = "data_extra_discover";
	public static final String DATA_EXTRA_DISCOVER_USERID = "data_extra_discover_userid";
	
	public static final String ARGUMENT_MOOD = "argument_mood";
	public static final String ARGUMENT_CATEGORIES = "argument_categories";
	
	private static final String FRAGMENT_TAG_GALLERY = "fragment_tag_gallery";
	
	public enum CategoryType {
		SUB_CATEGORY,
		GENRE
	}

	private FragmentManager mFragmentManager;
	private PlayerBarFragment mPlayerBarFragment;
	
	private DataManager mDataManager;
	
	private TextView mTextTitle;
	private ImageButton mButtonOptions;
	
	private Discover mDiscover;
	private String mUserId;
	private List<CategoryTypeObject> mCategoryTypeObjects;
	
	private OnFragmentEditModeStateChangedListener mOnFragmentEditModeStateChangedListener;
	
	private boolean mHasCategoriesChanged = false;
	
	// storing the last fragment that was in selected mood.
	private Fragment mLastSelectedEditModeFragment;
	
	private LinearLayout mOptions;
	
	private ImageButton mButtonCloseMoods;
	private ImageButton mButtonCloseCategories;
	private ImageButton mButtonCloseTempo;
	private ImageButton mButtonCloseEra;
	
	
	// ======================================================
	// Activity life-cycle.
	// ======================================================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discovery);

		/*
		 * Initializes internal activity components.
		 */
		
		mDataManager = DataManager.getInstance(getApplicationContext());
		mFragmentManager = getSupportFragmentManager();
		mPlayerBarFragment = (PlayerBarFragment) mFragmentManager.findFragmentById(R.id.main_fragmant_player_bar);
		
		initializeUserControls();
		
		/*
		 * Checks if calling this activity was to start new Discover or to
		 * view results of a given one. 
		 */
		Bundle arguments = getIntent().getExtras();
		if (arguments != null && arguments.containsKey(DATA_EXTRA_DISCOVER)) {
			mDiscover = (Discover) arguments.getSerializable(DATA_EXTRA_DISCOVER);
			mUserId = (String) arguments.getString(DATA_EXTRA_DISCOVER_USERID);
			
			// sets the title.
			if (!TextUtils.isEmpty(mDiscover.getName())) {
				setTextInTitleBar(mDiscover.getName());
			} else {
				setTextInTitleBar(R.string.discovery_title);
			}
			
			/*
			 * Shows results of a given Discover. 
			 */
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit,
	                R.anim.slide_right_enter,
	                R.anim.slide_right_exit);
			
			DiscoveryGalleryFragment fragment = new DiscoveryGalleryFragment();
			fragment.setOnMediaItemOptionSelectedListener(this);
			
			// sets the current fragment.
			fragmentTransaction.add(R.id.main_fragmant_container, fragment, FRAGMENT_TAG_GALLERY);
			fragmentTransaction.commit();
			
		} else {
			
			// Set the UserId
			mUserId = mDataManager.getApplicationConfigurations().getPartnerUserId();
			
			// sets the title.
			setTextInTitleBar(R.string.discovery_title);
			
			/*
			 * Starts a new Discover. 
			 */
			mDiscover = Discover.createNewDiscover();
			
			// starts with the mood selection.
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
													R.anim.slide_left_exit,
									                R.anim.slide_right_enter,
									                R.anim.slide_right_exit);
			
			DiscoveryMoodFragment discoveryMoodFragment = new DiscoveryMoodFragment();
			discoveryMoodFragment.setOnMoodSelectedListener(this);
			fragmentTransaction.add(R.id.main_fragmant_container, discoveryMoodFragment);
			fragmentTransaction.commit();
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		/*
		 * Clears the flag of the last selected edit fragment.
		 */
		if (mLastSelectedEditModeFragment != null) {
			
			// for each instance of child fragment, does its magic...
			
			if (mLastSelectedEditModeFragment instanceof DiscoveryCategoriesFragment) {
				mHasCategoriesChanged = false;
			}
			
			mLastSelectedEditModeFragment = null;

			// notifies the gallery to be update its data.
			if (mOnFragmentEditModeStateChangedListener != null) {
				mOnFragmentEditModeStateChangedListener.onStopEditMode(false);
			}
		}
		
		disableAnyClosingButton();
		
		// hides the options if visible.
		if (mOptions.getVisibility() == View.VISIBLE){
			onOptionsClicked(mButtonOptions);
		}
	}
	
	@Override
	public void onMoodSelected(Mood mood) {
		mDiscover.setMood(mood);
		
		// hides the options if visible.
		if (mOptions.getVisibility() == View.VISIBLE){
			onOptionsClicked(mButtonOptions);
		}
		
		// checks if we are in edit mode.
		if (mLastSelectedEditModeFragment != null) {
			// sets the title by the discover's properties.
			String title = getTitleForAnonymousDoscover();
			if (!TextUtils.isEmpty(title)) {
				setTextInTitleBar(title);
			} else {
				setTextInTitleBar(R.string.discovery_title);
			}
			
			disableAnyClosingButton();
			
			DiscoveryMoodFragment discoveryMoodFragment = (DiscoveryMoodFragment) mLastSelectedEditModeFragment;
			if (discoveryMoodFragment.isInEditMood()){
				// removes the current fragment.
				mLastSelectedEditModeFragment = null;
				mFragmentManager.popBackStack();
				// notifies the gallery to be update its data.
				if (mOnFragmentEditModeStateChangedListener != null) {
					mOnFragmentEditModeStateChangedListener.onStopEditMode(true);
				}
				return;
			}
			
			mLastSelectedEditModeFragment = null;
			
		} else {
			
			// starts the categories selection fragment.
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit,
	                R.anim.slide_right_enter,
	                R.anim.slide_right_exit);
			
			DiscoveryCategoriesFragment discoveryCategoriesFragment = new DiscoveryCategoriesFragment();
			discoveryCategoriesFragment.setOnDoneButtonClickedListener(this);
			
			// adds the selected mood as an argument to the fragment.
			if (mood != null) {
				Bundle data = new Bundle();
				data.putSerializable(ARGUMENT_MOOD, (Serializable) mood);
				discoveryCategoriesFragment.setArguments(data);
			}
			
			// sets the current fragment.
			fragmentTransaction.replace(R.id.main_fragmant_container, discoveryCategoriesFragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
			
			// sets the title with the discover name.
//			if (!TextUtils.isEmpty(mDiscover.getName())) {
//				setTextInTitleBar(mDiscover.getName());
//			} else {
//				setTextInTitleBar(R.string.discovery_title);
//			}
		}
	}
	
	@Override
	public void onDoneButtonClicked(List<CategoryTypeObject> categoryTypeObjects) {
		/*
		 * Checks if the user has selected any Mood or category / genre before proceeding.
		 * If not, shows an Alert dialog that disables him from proceeding the flow,
		 * no matter if he edits an existing Discover or creating one. 
		 */
		Mood selectedMood = mDiscover.getMood();
//		if ( ! (selectedMood != null || !Utils.isListEmpty(categoryTypeObjects))) {
//			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//			alertDialogBuilder.setTitle(Utils.TEXT_EMPTY);
//			alertDialogBuilder.setMessage(R.string.discovery_message_error_no_category_or_mood);
//			alertDialogBuilder.setPositiveButton(R.string.discovery_message_error_no_category_or_mood_confirm, 
//												 new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//				}
//			});
//			AlertDialog dialog = alertDialogBuilder.create();
//			dialog.show();
//			return;
//		}
		
		if(Utils.isListEmpty(categoryTypeObjects)){
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(Utils.TEXT_EMPTY);
			alertDialogBuilder.setMessage(R.string.discovery_message_error_no_category_or_mood);
			alertDialogBuilder.setPositiveButton(R.string.discovery_message_error_no_category_or_mood_confirm, 
												 new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			AlertDialog dialog = alertDialogBuilder.create();
			dialog.show();
			return;
		}
		
		// populates the current Discover with the given categories.
		mCategoryTypeObjects = new ArrayList<CategoryTypeObject>(categoryTypeObjects);
		mDiscover.setCategories(getCategories());
		mDiscover.setGenres(getGenres());
		
		// hides the options if visible.
		if (mOptions.getVisibility() == View.VISIBLE){
			onOptionsClicked(mButtonOptions);
		}
		
		//sets the title by the discover's properties.
		String title = getTitleForAnonymousDoscover();
		if (!TextUtils.isEmpty(title)) {
			setTextInTitleBar(title);
		} else {
			setTextInTitleBar(R.string.discovery_title);
		}
		
		// checks if we are in edit mode.
		if (mLastSelectedEditModeFragment != null) {
			DiscoveryCategoriesFragment discoveryCategoriesFragment = (DiscoveryCategoriesFragment) mLastSelectedEditModeFragment;
			if (discoveryCategoriesFragment.isInEditMood()){
				// removes the current fragment.
				mFragmentManager.popBackStack();
				mLastSelectedEditModeFragment = null;
				
				// notifies the gallery to be update its data.
				if (mOnFragmentEditModeStateChangedListener != null) {
					mOnFragmentEditModeStateChangedListener.onStopEditMode(true);
				}
				
				disableAnyClosingButton();
				
				mHasCategoriesChanged = true;
				return;
			}  
			
			mHasCategoriesChanged = false;
			mLastSelectedEditModeFragment = null;
			
		} else {
			
			// starts the Gallery (Discovery Search Result) Fragment.
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit,
	                R.anim.slide_right_enter,
	                R.anim.slide_right_exit);
			
			DiscoveryGalleryFragment fragment = new DiscoveryGalleryFragment();
			fragment.setOnMediaItemOptionSelectedListener(this);
			
			// sets the current fragment.
			fragmentTransaction.replace(R.id.main_fragmant_container, fragment, FRAGMENT_TAG_GALLERY);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.DISCOVER;
	}
	
	@Override
	public void onClick(View view) {
		// hides the options if visible.
		if (mOptions.getVisibility() == View.VISIBLE){
			onOptionsClicked(mButtonOptions);
		}
		
		int viewId = view.getId();
		if (viewId == R.id.discovery_button_close_moods || viewId == R.id.discovery_button_close_categories) {
			// disables the button.
			view.setVisibility(View.GONE);
			// closes the fragment.
			closeAnyEditModeFragment();
			
		} else if (viewId == R.id.discovery_button_close_tempo || viewId == R.id.discovery_button_close_era) {
			if (mLastSelectedEditModeFragment != null){

				// if it was the tempo, gets the tempos.
				if (mLastSelectedEditModeFragment instanceof DiscoveryTempoFragment) {
					// gets the tempos.
					DiscoveryTempoFragment discoveryTempoFragment = (DiscoveryTempoFragment) mLastSelectedEditModeFragment;
					
					// checks if there where changes in the tempo.
					List<Tempo> originalTempos = mDiscover.getTempos();
					List<Tempo> newTempos = discoveryTempoFragment.getTempos();
					
					if (areEqualTempos(originalTempos, newTempos)) {
						
						if (mOnFragmentEditModeStateChangedListener != null) {
							mOnFragmentEditModeStateChangedListener.onStopEditMode(false);
						}
						
					} else {
						
						if (mOnFragmentEditModeStateChangedListener != null) {
							mOnFragmentEditModeStateChangedListener.onStopEditMode(true);
						}
					}
					
					
					mDiscover.setTempos(newTempos);
				}
				
				// if it was era, gets the era.
				if (mLastSelectedEditModeFragment instanceof DiscoveryEraFragment) {
					DiscoveryEraFragment discoveryEraFragment = (DiscoveryEraFragment) mLastSelectedEditModeFragment;
					
					Era originalEra = mDiscover.getEra();
					Era newEra = discoveryEraFragment.getEra();
					
					if (areEqualEras(originalEra, newEra)) {
						if (mOnFragmentEditModeStateChangedListener != null) {
							mOnFragmentEditModeStateChangedListener.onStopEditMode(false);
						}
					} else {
						if (mOnFragmentEditModeStateChangedListener != null) {
							mOnFragmentEditModeStateChangedListener.onStopEditMode(true);
						}
					}
					
					mDiscover.setEra(discoveryEraFragment.getEra());
				}
				
				// clears the state of the last selected (opened) edit mode fragment.
				mLastSelectedEditModeFragment = null;
				// disables the button.
				view.setVisibility(View.GONE);
				// closes the fragment.
				mFragmentManager.popBackStack();
			}
		}
	}

	
	// ======================================================
	// Fragment's Communication Manager callbacks.
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_RETRIEVE ||
			operationId == OperationDefinition.Hungama.OperationId.DISCOVER_SAVE) {
			showLoadingDialog(R.string.application_dialog_loading);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_RETRIEVE){
			
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_SAVE) {
//			Toast.makeText(this, getResources().getString(R.string.discovery_message_discover_saved), Toast.LENGTH_SHORT).show();
			
			if (responseObjects.containsKey(DiscoverSaveOperation.RESULT_KEY_RESTART_IF_SUCCESS)) {
				boolean shouldRestart = (Boolean) responseObjects.get(DiscoverSaveOperation.RESULT_KEY_RESTART_IF_SUCCESS);
				if (shouldRestart) {
					startNewDiscover();
				} else {
					
					if (!TextUtils.isEmpty(mDiscover.getName())) {
						setTextInTitleBar(mDiscover.getName());
					} else {
						setTextInTitleBar(R.string.discovery_title);
					}
				}
			}
		}
		
		hideLoadingDialog();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_SAVE) {
			hideLoadingDialog();
			
			Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	
	// ======================================================
	// Fragment's events callbacks.
	// ======================================================
	
	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Play Now: " + mediaItem.getId());
		Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
								mediaItem.getAlbumName(), mediaItem.getArtistName(),  
								mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
		List<Track> tracks = new ArrayList<Track>();
		tracks.add(track);
		mPlayerBarFragment.playNow(tracks);
	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Play Next: " + mediaItem.getId());
		Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
								mediaItem.getAlbumName(), mediaItem.getArtistName(), 
								mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
		List<Track> tracks = new ArrayList<Track>();
		tracks.add(track);
		mPlayerBarFragment.playNext(tracks);
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Add to queue: " + mediaItem.getId());
		Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
								mediaItem.getAlbumName(), mediaItem.getArtistName(), 
								mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
		List<Track> tracks = new ArrayList<Track>();
		tracks.add(track);
		mPlayerBarFragment.addToQueue(tracks);
	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Show Details: " + mediaItem.getId());
		Intent intent = new Intent(this, MediaDetailsActivity.class);
		intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM, (Serializable) mediaItem);
		startActivity(intent);
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem, int position) {
		Logger.i(TAG, "Remove item: " + mediaItem.getId());
	}
	
	
	// ======================================================
	// Fragment's options callbacks.
	// ======================================================
	
	public void onOptionsClicked(View view) {
		if (view.isSelected()) {
			// closes the option list.
			view.setSelected(false);
			view.setBackgroundResource(0);
			
			hideOptions();
			
		} else {
			// opens the option list.
			view.setSelected(true);
			view.setBackgroundResource(R.color.black);
			
			Fragment galleryFragment = mFragmentManager.findFragmentByTag(FRAGMENT_TAG_GALLERY);
			if (galleryFragment != null && galleryFragment.isVisible()) {
				showOptions();
			} else {
				showOptionsOnlyLoadDiscoveries();
			}
			
		}
	}
	
	public void onOptionsItemAddAllToQueueClicked(View view) {
		DiscoveryGalleryFragment galleryFragment = (DiscoveryGalleryFragment) mFragmentManager.findFragmentByTag(FRAGMENT_TAG_GALLERY);
		if (galleryFragment != null && galleryFragment.isVisible()) {
			List<Track> tracks = galleryFragment.getTracks();
			if (!Utils.isListEmpty(tracks)){
				mPlayerBarFragment.addToQueue(tracks);
			}
		}
		// closes the options list.
		onOptionsClicked(mButtonOptions);
	}
	
	public void onOptionsItemLoadMyDiscoveriesClicked(View view) {
		// shows the items in a dialog;
		DiscoverListDialog discoverListDialog = new DiscoverListDialog(mUserId, this);
		discoverListDialog.setOnListDialogStateChangedListener(new OnListDialogStateChangedListener() {
			
			@Override
			public void onItemSelected(ListDialogItem listDialogItem, int position) {
				// sets the current discover as the selected one.
				mDiscover = (Discover) listDialogItem;
				onDiscoverySelected(mDiscover);
			}
			
			@Override
			public void onCancelled() {}
		});
		
		discoverListDialog.show();
		
		// closes the options list.
		onOptionsClicked(mButtonOptions);
	}
	
	public void onOptionsItemSaveDiscoveryClicked(View view) {
		// closes the options list.
		onOptionsClicked(mButtonOptions);
		
		// gets the default name of the discovery.
		String discoverName = "Discovery Name";
		
		DiscoverSaveDialog discoverSaveDialog = new DiscoverSaveDialog(this, discoverName, new OnDiscoverSaveDialogStateChangedListener() {
			
			@Override
			public void onSaveSelected(DiscoverSaveDialog dialog, String discoverName) {
				dialog.dismiss();
				
				mDiscover.setName(discoverName);
				// saves the discover.
				mDataManager.saveDiscover(mDiscover, false, DiscoveryActivity.this);
			}
			
			@Override
			public void onCancelled(DiscoverSaveDialog dialog) {}
		});
		discoverSaveDialog.show();
	}
	
	public void onOptionsItemStartNewDiscovery(View view) {
		// closes the options list.
		onOptionsClicked(mButtonOptions);
		
		// shows the save confirmation message.
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.discovery_message_start_new_discover_message)
			   .setCancelable(false)
			   .setPositiveButton(R.string.discovery_message_start_new_discover_confirm, 
						   						new DialogInterface.OnClickListener() {
					   
				   public void onClick(DialogInterface dialog, int id) {
								// shows the save discover dialog.
								// gets the default name of the discovery.
								String discoverName = "Discovery Name";
								DiscoverSaveDialog discoverSaveDialog = new DiscoverSaveDialog(
										DiscoveryActivity.this,
										discoverName,
										new OnDiscoverSaveDialogStateChangedListener() {

											@Override
											public void onSaveSelected(DiscoverSaveDialog dialog, String discoverName) {
												dialog.dismiss();

												mDiscover.setName(discoverName);
												// saves the discover.
												mDataManager.saveDiscover(mDiscover, true, DiscoveryActivity.this);
											}

											@Override
											public void onCancelled(
													DiscoverSaveDialog dialog) {
											}
										});
								discoverSaveDialog.show();
							}

						})
				.setNegativeButton(R.string.discovery_message_start_new_discover_cancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {
								startNewDiscover();
							}
						});
			
			builder.show();
	}
	
	
	// ======================================================
	// Public methods.
	// ======================================================
	
	/**
	 * Interface definition to be invoked when the states of the edit mode
	 * has been changed.
	 */
	public interface OnFragmentEditModeStateChangedListener {
		
		public void onStartEditMode(Fragment fragment);
		
		public void onStopEditMode(boolean hasDataChanged);
	}
	
	public void setOnFragmentEditModeStateChangedListener(OnFragmentEditModeStateChangedListener listener) {
		mOnFragmentEditModeStateChangedListener = listener;
	}
	
	public void setTextInTitleBar(String title) {
		mTextTitle.setText(title);
	}
	
	public void setTextInTitleBar(int title) {
		mTextTitle.setText(getResources().getString(title));
	}
	
	public Discover getDiscover() {
		return mDiscover;
	}
	
	public List<CategoryTypeObject> getCategoryTypeObjects() {
		if (!Utils.isListEmpty(mCategoryTypeObjects)){
			return new ArrayList<CategoryTypeObject>(mCategoryTypeObjects);
		} else {
			return new ArrayList<CategoryTypeObject>();
		}
	}
	
	public boolean hasCategoriesChanged() {
		return mHasCategoriesChanged;
	}
	
	public void onEditModeMoodSelected() {
		// closes any opened fragment in edit mode.
		closeAnyEditModeFragment();
		
		// hides the options if visible.
		if (mOptions.getVisibility() == View.VISIBLE){
			onOptionsClicked(mButtonOptions);
		}
		
		// opens the mood selection fragment.
		DiscoveryMoodFragment discoveryMoodFragment = new DiscoveryMoodFragment();
		discoveryMoodFragment.setOnMoodSelectedListener(this);
		discoveryMoodFragment.setEditMode(true);
		
		if (mDiscover.getMood() != null) {
			Bundle data = new Bundle();
			data.putSerializable(ARGUMENT_MOOD, (Serializable) mDiscover.getMood());
			discoveryMoodFragment.setArguments(data);
		}
		
		// stores the references for later use.
		mLastSelectedEditModeFragment = discoveryMoodFragment;
		
		// invokes the callback for edit mode changes.
		if (mOnFragmentEditModeStateChangedListener != null) {
			mOnFragmentEditModeStateChangedListener.onStartEditMode(mLastSelectedEditModeFragment);
		}
		
		// starts the fragment.
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_right_enter, 
													0, 0, R.anim.slide_and_show_left_exit);
		
		fragmentTransaction.add(R.id.main_fragmant_container, discoveryMoodFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
		
		disableAnyClosingButton();
		
		mButtonCloseMoods.setVisibility(View.VISIBLE);
	}
	
	public void onEditModeCategorySelected() {
		// closes any opened fragment in edit mode.
		closeAnyEditModeFragment();
		
		// hides the options if visible.
		if (mOptions.getVisibility() == View.VISIBLE){
			onOptionsClicked(mButtonOptions);
		}
		
		// opens the categories selection fragment.
		DiscoveryCategoriesFragment discoveryCategoriesFragment = new DiscoveryCategoriesFragment();
		discoveryCategoriesFragment.setOnDoneButtonClickedListener(this);
		discoveryCategoriesFragment.setEditMode(true);
		
		// adds the selected mood and categories as an argument to the fragment.
		Bundle data = new Bundle();
		if (mDiscover.getMood() != null) {
			data.putSerializable(ARGUMENT_MOOD, (Serializable) mDiscover.getMood());
		}
		if (mCategoryTypeObjects != null) {
			List<CategoryTypeObject> copyOfCategoryTypeObjects = new ArrayList<CategoryTypeObject>(mCategoryTypeObjects);
			data.putSerializable(ARGUMENT_CATEGORIES, (Serializable) copyOfCategoryTypeObjects);
		}
		discoveryCategoriesFragment.setArguments(data);
		
		// stores the references for later use.
		mLastSelectedEditModeFragment = discoveryCategoriesFragment;
		
		// invokes the callback for edit mode changes.
		if (mOnFragmentEditModeStateChangedListener != null) {
			mOnFragmentEditModeStateChangedListener.onStartEditMode(mLastSelectedEditModeFragment);
		}
		
		// starts the fragment.
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_left_enter, 
													0, 0, R.anim.slide_and_show_right_exit);
		
		fragmentTransaction.add(R.id.main_fragmant_container, discoveryCategoriesFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
		
		disableAnyClosingButton();
		
		mButtonCloseCategories.setVisibility(View.VISIBLE);
	}
	
	public void onEditModeEraSelected() {
		// closes any opened fragment in edit mode.
		closeAnyEditModeFragment();
		
		// hides the options if visible.
		if (mOptions.getVisibility() == View.VISIBLE){
			onOptionsClicked(mButtonOptions);
		}
		
		// opens the era selection fragment.
		DiscoveryEraFragment discoveryEraFragment = new DiscoveryEraFragment();
		
		// stores the references for later use.
		mLastSelectedEditModeFragment = discoveryEraFragment;
		
		// invokes the callback for edit mode changes.
		if (mOnFragmentEditModeStateChangedListener != null) {
			mOnFragmentEditModeStateChangedListener.onStartEditMode(mLastSelectedEditModeFragment);
		}
		
		// starts the fragment.
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.rotate_from_buttom_to_left, 
				0, 0, R.anim.rotate_from_left_to_bottom);
		
		fragmentTransaction.add(R.id.main_fragmant_container, discoveryEraFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
		
		disableAnyClosingButton();
		
		mButtonCloseEra.setVisibility(View.VISIBLE);
	}
	
	public void onEditModeTempoSelected() {
		// closes any opened fragment in edit mode.
		closeAnyEditModeFragment();
		
		// hides the options if visible.
		if (mOptions.getVisibility() == View.VISIBLE){
			onOptionsClicked(mButtonOptions);
		}
		
		// opens the tempo selection fragment.
		DiscoveryTempoFragment discoveryTempoFragment = new DiscoveryTempoFragment();
		
		// stores the references for later use.
		mLastSelectedEditModeFragment = discoveryTempoFragment;
		
		// invokes the callback for edit mode changes.
		if (mOnFragmentEditModeStateChangedListener != null) {
			mOnFragmentEditModeStateChangedListener.onStartEditMode(mLastSelectedEditModeFragment);
		}
		
		// starts the fragment.
		FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.rotate_from_bottom_to_right, 
				0, 0, R.anim.rotate_from_right_to_bottom);
		
		fragmentTransaction.add(R.id.main_fragmant_container, discoveryTempoFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
		
		disableAnyClosingButton();
		
		mButtonCloseTempo.setVisibility(View.VISIBLE);
	}
	
	
	// ======================================================
	// Private helper methods.
	// ======================================================
	
	private void initializeUserControls() {
		// initializes the title bar text.
		mTextTitle = (TextView) findViewById(R.id.main_title_bar_text);
		
		// options.
		mButtonOptions = (ImageButton) findViewById(R.id.main_title_bar_button_options);
		// disable the options button for moods and categories.
		mButtonOptions.setVisibility(View.VISIBLE);
		mButtonOptions.setSelected(false);
		
		mOptions = (LinearLayout) findViewById(R.id.discovery_options);
		mOptions.setVisibility(View.GONE);
		
		mButtonCloseMoods = (ImageButton) findViewById(R.id.discovery_button_close_moods);
		mButtonCloseCategories = (ImageButton) findViewById(R.id.discovery_button_close_categories);
		mButtonCloseTempo = (ImageButton) findViewById(R.id.discovery_button_close_tempo);
		mButtonCloseEra = (ImageButton) findViewById(R.id.discovery_button_close_era);
		
		mButtonCloseMoods.setOnClickListener(this);
		mButtonCloseCategories.setOnClickListener(this);
		mButtonCloseTempo.setOnClickListener(this);
		mButtonCloseEra.setOnClickListener(this);
	}
	
	private void closeAnyEditModeFragment() {
		if (mLastSelectedEditModeFragment != null) {
			
			// clears the state of the last selected (opened) edit mode fragment.
			mLastSelectedEditModeFragment = null;
			
			// notifies the gallery to be updated as well.
			if (mOnFragmentEditModeStateChangedListener != null) {
				mOnFragmentEditModeStateChangedListener.onStopEditMode(false);
			}
			
			// close the fragment.
			mFragmentManager.popBackStack();
		}
	}

	private void disableAnyClosingButton() {
		if (mButtonCloseMoods.getVisibility() == View.VISIBLE) {
			mButtonCloseMoods.setVisibility(View.GONE);
			return;
		}
		
		if (mButtonCloseCategories.getVisibility() == View.VISIBLE) {
			mButtonCloseCategories.setVisibility(View.GONE);
			return;
		}
		
		if (mButtonCloseTempo.getVisibility() == View.VISIBLE) {
			mButtonCloseTempo.setVisibility(View.GONE);
			return;
		}
		
		if (mButtonCloseEra.getVisibility() == View.VISIBLE) {
			mButtonCloseEra.setVisibility(View.GONE);
			return;
		}
	}
	
	private void showOptions() {
		/*
		 *  iterates thru all the buttons and separator lines
		 *  and shows them.
		 */
		int size = mOptions.getChildCount();
		View child;
		for (int i = 0; i < size; i++) {
			child = mOptions.getChildAt(i);
			if (child.getVisibility() == View.GONE){
				child.setVisibility(View.VISIBLE);
			}
		}
		
		mOptions.setVisibility(View.VISIBLE);
	}
	
	private void showOptionsOnlyLoadDiscoveries() {
		/*
		 *  iterates thru all the buttons and separator lines
		 *  and hides them.
		 */
		int size = mOptions.getChildCount();
		View child;
		for (int i = 0; i < size; i++) {
			child = mOptions.getChildAt(i);
			if (!(child.getId() == R.id.discovery_options_load_my_discoveries)){
				child.setVisibility(View.GONE);
			}
		}
		
		mOptions.setVisibility(View.VISIBLE);
	}
	
	private void hideOptions() {
		mOptions.setVisibility(View.GONE);
	}
	
	private List<Genre> getGenres() {
		if (!Utils.isListEmpty(mCategoryTypeObjects)){
			List<Genre> genres = new ArrayList<Genre>();
			for (CategoryTypeObject categoryTypeObject : mCategoryTypeObjects) {
				if (categoryTypeObject.getType().equals(CategoryTypeObject.TYPE_GENRE)) {
					genres.add((Genre) categoryTypeObject);
				}
			}
			return genres;
		}
		return null;
	}
	
	private List<Category> getCategories() {
		if (!Utils.isListEmpty(mCategoryTypeObjects)){
			List<Category> categories = new ArrayList<Category>();
			for (CategoryTypeObject categoryTypeObject : mCategoryTypeObjects) {
				if (categoryTypeObject.getType().equals(CategoryTypeObject.TYPE_CATEGORY)) {
					categories.add((Category) categoryTypeObject);
				}
			}
			return categories;
		}
		return null;
	}
	
	private boolean areEqualTempos(List<Tempo> originalTempos, List<Tempo> newTempos) {
		
		if (!Utils.isListEmpty(originalTempos) && !Utils.isListEmpty(newTempos)) {
				return originalTempos.toString().equalsIgnoreCase(newTempos.toString());
		} else {
			if (Utils.isListEmpty(originalTempos) && !Utils.isListEmpty(newTempos)) {
				return false;
			}
			
			if (!Utils.isListEmpty(originalTempos) && Utils.isListEmpty(newTempos)) {
				return false;
			}
		}
		
		// both are nulls.
		return false;
	}
	
	private boolean areEqualEras(Era originalEra, Era newEra) {
		if (originalEra != null && newEra != null) {
			return originalEra.equals(newEra);
		} else {
			if (originalEra == null && newEra != null) {
				return false;
			}
			
			if (originalEra != null && newEra == null) {
				return false;
			}
		}
		
		// both are nulls.
		return true;
	}
	
	private void onDiscoverySelected(Discover selectedDiscover) {
		// sets the title.
		if (!TextUtils.isEmpty(mDiscover.getName())) {
			setTextInTitleBar(mDiscover.getName());
		} else {
			setTextInTitleBar(R.string.discovery_title);
		}
		// checks if the current visible fragment is the Gallery.
		Fragment galleryFragment = mFragmentManager.findFragmentByTag(FRAGMENT_TAG_GALLERY);
		if (galleryFragment != null && galleryFragment.isVisible()) {
			
			closeAnyEditModeFragment();
			
			mFragmentManager.popBackStack();
			
			// recreates it.
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(0, 0, R.anim.slide_right_enter,
	                									  R.anim.slide_right_exit);
			
			DiscoveryGalleryFragment discoveryGalleryFragment = new DiscoveryGalleryFragment();
			discoveryGalleryFragment.setOnMediaItemOptionSelectedListener(DiscoveryActivity.this);
			
			// sets the current fragment.
			fragmentTransaction.replace(R.id.main_fragmant_container, discoveryGalleryFragment, FRAGMENT_TAG_GALLERY);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
			
			
		} else {
			// goes for it.
			// starts the Gallery (Discovery Search Result) Fragment.
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit,
	                R.anim.slide_right_enter,
	                R.anim.slide_right_exit);
			
			DiscoveryGalleryFragment discoveryGalleryFragment = new DiscoveryGalleryFragment();
			discoveryGalleryFragment.setOnMediaItemOptionSelectedListener(DiscoveryActivity.this);
			
			// sets the current fragment.
			fragmentTransaction.replace(R.id.main_fragmant_container, discoveryGalleryFragment, FRAGMENT_TAG_GALLERY);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
	}
	
	private void startNewDiscover() {
		// restarts the activity.
		Intent intent = getIntent();
		
		// removes any last selected discover.
		Bundle arguments = getIntent().getExtras();
		if (arguments != null && arguments.containsKey(DATA_EXTRA_DISCOVER)) {
			arguments.remove(DATA_EXTRA_DISCOVER);
		}
		intent.replaceExtras(arguments);
		
		// restarts the activity with no discover.
		finish();
		startActivity(intent);
	}
	
	private String getTitleForAnonymousDoscover() {
		StringBuilder titleBuilder = new StringBuilder();
		
		if (mDiscover.getMood() != null) {
			titleBuilder.append(mDiscover.getMood().getName()); 
		}
		
		// populates with categories.
		List<Category> categories = mDiscover.getCategories();
		if (!Utils.isListEmpty(categories)) {
			
			for (Category category : categories) {
				if (titleBuilder.length() > 0) {
					titleBuilder.append(", ");
				}
				titleBuilder.append(category.getName());
			}
		}
		
		return titleBuilder.toString();
	}

}
