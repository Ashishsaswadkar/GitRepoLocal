package com.hungama.myplay.activity.ui.fragments;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.DataManager.MoodIcon;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.services.MoodPrefetchingService;
import com.hungama.myplay.activity.ui.DiscoveryActivity;
import com.hungama.myplay.activity.ui.view.moods.DragController;
import com.hungama.myplay.activity.ui.view.moods.DragLayer;
import com.hungama.myplay.activity.ui.view.moods.DropSpot;
import com.hungama.myplay.activity.ui.view.moods.DragController.OnDropListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * Lets the user to select his mood by dragging the moods icons into a
 * centerized circle.
 */
public class DiscoveryMoodFragment extends Fragment implements OnTouchListener, OnDropListener {
	
	private static final String TAG = "DiscoveryMoodFragment";
	
	/**
	 * Interface definition to be invoked when a mood was selected and the 
	 * user wants to continue the process of the discovery.  
	 */
	public interface OnMoodSelectedListener {
		
		/**
		 * The selected mood, might be null if the user has skipped the process.
		 * @param mood
		 */
		public void onMoodSelected(Mood mood);
		
	}
	
	private OnMoodSelectedListener mOnMoodSelectedListener;
	
	public void setOnMoodSelectedListener(OnMoodSelectedListener listener) {
		mOnMoodSelectedListener = listener;
	}
	
	public void setEditMode(boolean isEditMode) {
		mIsEditMode = isEditMode;
	}
	
	public boolean isInEditMood() {
		return mIsEditMode;
	}
	
	
	// ======================================================
	// Fragment life-cycle
	// ======================================================
	
	private boolean mIsEditMode = false;
	
	private DataManager mDataManager;
	private List<Mood> mMoods;
	
	private MoodsPrefetchingReceiver mMoodsPrefetchingReceiver;
	
	private List<ImageView> mImageItems;
	
	private TextView mTextMoodsTitle;
	
	private ImageView mImageSelectedItemIcon;
	private TextView mTextSelectedItemTitle;
	
	private ImageButton mButtonNextSkip;
	
	private ImageView mImageItem1;
	private ImageView mImageItem2;
	private ImageView mImageItem3;
	private ImageView mImageItem4;
	private ImageView mImageItem5;
	private ImageView mImageItem6;
	private ImageView mImageItem7;
	private ImageView mImageItem8;
	
	private DragController mDragController;   // Object that sends out drag-drop events while a view is being moved.
	private DragLayer mDragLayer;             // The ViewGroup that supports drag-drop.
	private DropSpot mDropSpot; 
	
	/*
	 * Stores the last selected mood, so we can retrieve it back
	 * if the user has changed his mind about selecting it.
	 */
	private View mLastSelectedItem;
	
	private MoodsGeneratorGlobalLayoutListener mMoodsGeneratorGlobalLayoutListener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//  initializes data access.
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		mMoods = mDataManager.getStoredMoods();

		mMoodsGeneratorGlobalLayoutListener = new MoodsGeneratorGlobalLayoutListener(this); 
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_discovery_mood, container, false);
		initializeUserControls(rootView);
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (Utils.isListEmpty(mMoods)) {
			mMoods = mDataManager.getStoredMoods();
		}
		
		/*
		 * checks for the current state of the moods, 
		 * are they in the process of being downloaded?
		 */
		IntentFilter intentFilter = new IntentFilter(MoodPrefetchingService.INTENT_ACTION_PREFETCHING_MOODS_SYNC_EVENT);
		Intent currentPrefetchingState = getActivity().registerReceiver(null, intentFilter);
		
		if (currentPrefetchingState != null) {
			// gets the prefetching moods service status.
			boolean isRunnuning = currentPrefetchingState
					.getBooleanExtra(MoodPrefetchingService.EXTRA_PREFETCHING_MOODS_SYNC_STATE_RUNNING, false);
			boolean isSuccess = currentPrefetchingState
					.getBooleanExtra(MoodPrefetchingService.EXTRA_PREFETCHING_MOODS_SYNC_SUCCESS_FLAG, false);
			
			if (isRunnuning) {
				Logger.i(TAG, "Prefetching moods service is still running, listenning");
				// registers this activity to listen when finish.
				mMoodsPrefetchingReceiver = new MoodsPrefetchingReceiver();
				getActivity().registerReceiver(mMoodsPrefetchingReceiver, intentFilter);
				
				// shows a loading indication.
				showLoadingIndication();
				
			} else {
				Logger.i(TAG, "Prefetching moods service has been finish: " + Boolean.toString(isSuccess));
				if (isSuccess && !Utils.isListEmpty(mMoods)) {
					// shows the party.
					hideLoadingIndication();
					generateItems();
					
				} else {
					// show an error toast.
					Toast.makeText(getActivity(), R.string.discovery_mood_no_moods, Toast.LENGTH_SHORT).show();

					// starts prefetching the discover moods.
					mDataManager.prefetchMoodsIfNotExists();
					onStart();
				}
			}
					
		} else {
			// shows the party.
			hideLoadingIndication();
			generateItems();
		}
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key));
		FlurryAgent.onPageView();
		FlurryAgent.logEvent("Discover - Moods");
	}
	
	@Override
	public void onStop() {
		// by by dear receiver.
		if (mMoodsPrefetchingReceiver != null) {
			try {
				getActivity().unregisterReceiver(mMoodsPrefetchingReceiver);
			} catch (IllegalArgumentException exception) {
				// was registered..
			} finally {
				mMoodsPrefetchingReceiver = null;
			}
		}
		
		super.onStop();
		FlurryAgent.onEndSession(getActivity());
	}
	
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		
		final int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			if (isOneOfTheStubs(view.getId())) {
				// Let the DragController initiate a drag-drop sequence.
			    // I use the dragInfo to pass along the object being dragged.
			    // I'm not sure how the Launcher designers do this.
			    Object dragInfo = view;
			    mDragController.startDrag (view, mDragLayer, dragInfo, DragController.DRAG_ACTION_MOVE);
			    return true;
			    
		    /*
		     * Identifying the current state of the current selected icon by its visibility.
		     */
			} else if (view == mImageSelectedItemIcon &&
							mImageSelectedItemIcon.getVisibility() == View.VISIBLE) {
				    
				Object dragInfo = view;
			    mDragController.startDrag (view, mDragLayer, dragInfo, DragController.DRAG_ACTION_MOVE);
				    return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void onDrop(View view) {
		Logger.d(TAG, "drop: " + view.toString());
		if (!view.equals(mImageSelectedItemIcon)) {
			/*
			 * checks if we need to animate back the last selected mood
			 * to its original place.
			 */
			if (mLastSelectedItem != null) {
				animateMoodToItsOriginalPlace(mLastSelectedItem);
			}
			// Disappears the original selected view place, it's now has the thrown. 
			view.setVisibility(View.INVISIBLE);
			// Updates the selected drop zone.
			updateSelectedItemSpot(view);
			// no the current selected mood is taking the thrown.
			mLastSelectedItem = view;
		} else {
			mImageSelectedItemIcon.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onCancelDrop(View view) {
		Logger.d(TAG, "cancel drop: " + view.toString());
		
		if (view.equals(mImageSelectedItemIcon)) {
			
			// clears the selected area.
			updateSelectedItemSpot(null);
			
			// show the last view was selected.
			View moodView = mLastSelectedItem;
			moodView.setVisibility(View.VISIBLE);
			
			// disables the last selections.
			mLastSelectedItem = null;
			
		} else {
			view.setVisibility(View.VISIBLE);
		}
	}
	
	
	// ======================================================
	// Private helper methods.
	// ======================================================
	
	private void initializeUserControls(View rootView) {
		
		if (mIsEditMode) {
			rootView.setBackgroundResource(R.drawable.background_discovery);
		} else {
			rootView.setBackgroundResource(0);
		}
		
		// internal title.
		mTextMoodsTitle = (TextView) rootView.findViewById(R.id.discovery_mood_title);
		
		// the selected item.
		mImageSelectedItemIcon = (ImageView) rootView.findViewById(R.id.discovery_mood_selected_item_icon);
		mTextSelectedItemTitle = (TextView) rootView.findViewById(R.id.discovery_mood_selected_item_title);
		
		mImageSelectedItemIcon.setOnTouchListener(this);
		
		mButtonNextSkip = (ImageButton) rootView.findViewById(R.id.discovery_mood_next_skip_button);
		mButtonNextSkip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				
				Mood mood = null;
				
				if (mLastSelectedItem != null) {
					mood = (Mood) mLastSelectedItem.getTag();
				}
				
				if (mOnMoodSelectedListener != null) {
					mOnMoodSelectedListener.onMoodSelected(mood);
				}
			}
		});
		
		// sets the placeholders for the moods.
		mImageItem1 = (ImageView) rootView.findViewById(R.id.discovery_mood_item1);
		mImageItem2 = (ImageView) rootView.findViewById(R.id.discovery_mood_item2);
		mImageItem3 = (ImageView) rootView.findViewById(R.id.discovery_mood_item3);
		mImageItem4 = (ImageView) rootView.findViewById(R.id.discovery_mood_item4);
		mImageItem5 = (ImageView) rootView.findViewById(R.id.discovery_mood_item5);
		mImageItem6 = (ImageView) rootView.findViewById(R.id.discovery_mood_item6);
		mImageItem7 = (ImageView) rootView.findViewById(R.id.discovery_mood_item7);
		mImageItem8 = (ImageView) rootView.findViewById(R.id.discovery_mood_item8);
		// adds them to the list.
		mImageItems = new ArrayList<ImageView>();
		mImageItems.add(mImageItem1);
		mImageItems.add(mImageItem2);
		mImageItems.add(mImageItem3);
		mImageItems.add(mImageItem4);
		mImageItems.add(mImageItem5);
		mImageItems.add(mImageItem6);
		mImageItems.add(mImageItem7);
		mImageItems.add(mImageItem8);
		
		// initializes the Drag and Drop controller.
		mDragController = new DragController(getActivity());
		// sets the drag and drop functionality.
		mDragLayer = (DragLayer) rootView.findViewById(R.id.discovery_mood_draglayer);
		mDragLayer.setDragController(mDragController);
		
		mDragController.addDropTarget(mDragLayer);
		mDragController.setOnDropListener(this);
		
		mDropSpot = (DropSpot) rootView.findViewById(R.id.discovery_mood_dropspot);
		mDropSpot.setup(mDragLayer, mDragController, 0);
	}
	
	/**
	 * Generates the visibility state and the icons of the items,
	 * also binds between the items and the moods.
	 */
	private void generateItems() {
		
		ViewTreeObserver viewTreeObserver = getView().getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
			viewTreeObserver.addOnGlobalLayoutListener(mMoodsGeneratorGlobalLayoutListener);
		}
	}
	
	/**
	 * Listener for generating the moods and makes them visible.
	 */
	private static final class MoodsGeneratorGlobalLayoutListener implements OnGlobalLayoutListener {
		
		private WeakReference<DiscoveryMoodFragment> discoveryMoodFragmentReference;

		public MoodsGeneratorGlobalLayoutListener(DiscoveryMoodFragment discoveryGalleryFragment) {
			discoveryMoodFragmentReference = new WeakReference<DiscoveryMoodFragment>(discoveryGalleryFragment);
		}
		
		@Override
		public void onGlobalLayout() {
			DiscoveryMoodFragment fragment = discoveryMoodFragmentReference.get();
			if (fragment != null) {
				View rootView = fragment.getView();
				if (rootView != null && rootView.getViewTreeObserver() != null) {
			    	// removes the listener.
			    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			    		rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					} else {
						rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
			    	
			    	// draws the moods.
			    	fragment.helperGenerateItems();
			    	
			    	// draws the selected one, if any.
			    	fragment.setLastMoodSelectedState();
				}
			}
		}
		
	}
	
	private void helperGenerateItems() {
		int size = mImageItems.size();
		Mood mood = null;
		Drawable moodIcon;
		ImageView imageView;
		
		for (int i = 0; i < size; i++) {
			if (i < mMoods.size()) {
				mood = mMoods.get(i);
			} else {
				mood = null;
			}
			imageView = mImageItems.get(i);
			if (mood != null) {
				moodIcon = mDataManager.getMoodIcon(mood, MoodIcon.SMALL);
				// if there is no icon available to the mood, hide the all stub.
				if (moodIcon != null) {
					imageView.setImageDrawable(moodIcon);
					// binds the stub with it's mood.
					imageView.setTag(mood);
					// sets the activity to listen this stub touch events.
					imageView.setOnTouchListener(this);
					imageView.setVisibility(View.VISIBLE);
					
					/*
					 * checks if the last mood is the 7th one, 
					 * if soo, places it in the middle.
					 */
					if (i == 6 && i == mMoods.size() - 1) {
						// get the positions 
						int leftSide = mImageItem5.getRight();
						int rightSide = mImageItem6.getLeft();
						
						/*
						 * sets who much the mood icon should be margin from the left, 
						 * subtracing its half og the width to position it right 
						 * in the middle of the other moods.
						 */
						int leftMargin = (rightSide - leftSide) / 2 - (mImageItem7.getWidth() / 2);
						
						// places it.
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mImageItem7.getLayoutParams();
						params.leftMargin = leftMargin;
						
						// update the view's position.
						mImageItem7.setLayoutParams(params);
					}
					
				} else {
					imageView.setVisibility(View.INVISIBLE);
				}
			} else {
				// makes the item's image not available.
				imageView.setVisibility(View.GONE);
			}
		}
	}
	
	private boolean isOneOfTheStubs(int viewId) {
		for (ImageView imageView : mImageItems) {
			if (imageView.getId() == viewId)
				return true;
		}
		return false;
	}

	/**
	 * Updates the selected spot's icon and text with the mood's icon and label,
	 * if the given value is {@code null}, returns the views to the initial state.
	 * @param view
	 */
	private void updateSelectedItemSpot(View view) {
		
		if (view != null) {
			Mood mood = (Mood) view.getTag();
			Drawable moodIcon = mDataManager.getMoodIcon(mood, MoodIcon.BIG);
			
			// sets the big icon.
			mImageSelectedItemIcon.setVisibility(View.VISIBLE);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				mImageSelectedItemIcon.setBackgroundDrawable(moodIcon);
			} else {
				mImageSelectedItemIcon.setBackground(moodIcon);
			}
			// sets the title.
			mTextSelectedItemTitle.setText(mood.getName());
			// sets the skip's button background.
			mButtonNextSkip.setBackgroundResource(R.drawable.background_discovery_button_next);
		} else {
			// clears any existing selected mood from the selected area.
			mImageSelectedItemIcon.setVisibility(View.GONE);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				mImageSelectedItemIcon.setBackgroundDrawable(null);
			} else {
				mImageSelectedItemIcon.setBackground(null);
			}
			// sets the defaulat text.
			mTextSelectedItemTitle.setText(R.string.discovery_mood_selected_item_default_text);
			// sets the skip's button background.
			mButtonNextSkip.setBackgroundResource(R.drawable.background_discovery_button_skip);
		}
	}
	
	private void animateMoodToItsOriginalPlace(final View view) {
		
		int [] containerLocation = new int [2];
		mImageSelectedItemIcon.getLocationInWindow(containerLocation);

		int containerCenterX = (containerLocation[0] + mImageSelectedItemIcon.getMeasuredWidth()) / 2; 
		int containerCenterY = (containerLocation[1] + mImageSelectedItemIcon.getMeasuredHeight()) / 2;
		
		Logger.v(TAG, "Container: center x: " + containerCenterX + " center y: " + containerCenterY);
		
		int [] targetLocation = new int [2];
		view.getLocationInWindow(targetLocation);
		
		int targetCenterX = (targetLocation[0] + view.getMeasuredWidth()) / 2;
		int targetCenterY = (targetLocation[1] + view.getMeasuredHeight()) / 2;
		
		Logger.v(TAG, "Target: center x: " + targetCenterX + " center y: " + targetCenterY);
		
		TranslateAnimation animation = new TranslateAnimation(Animation.ABSOLUTE, containerCenterX - targetCenterX, 
															  Animation.ABSOLUTE, 0, 
															  Animation.ABSOLUTE, containerCenterY - targetCenterY, 
															  Animation.ABSOLUTE, 0);
		animation.setDuration(300);
		animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				mDropSpot.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mDropSpot.setVisibility(View.VISIBLE);
				view.setVisibility(View.VISIBLE);
			}
		});
		view.startAnimation(animation);
	}
	
	private View findViewByMood(Mood mood) {
		Mood tmpMood;
		for (ImageView imageView : mImageItems) {
			tmpMood = (Mood) imageView.getTag();
			if (mood.equals(tmpMood))
				return imageView;
		}
		return null;
	}
	
	/**
	 * Shows the loading moods idicator and diables the next button.
	 */
	private void showLoadingIndication() {
		mTextMoodsTitle.setText(R.string.discovery_mood_title_loading_moods);
		mButtonNextSkip.setVisibility(View.INVISIBLE);
		
		// shows the loading dialog.
		FragmentManager fragmentManager = getFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag(CancelableLoadingDialog.FRAGMENT_TAG);
		if (fragment == null) {
			CancelableLoadingDialog dialog = new CancelableLoadingDialog(R.string.application_dialog_loading_content, new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					// remove the whole biz, the user wants to go from here.
					getActivity().finish();
				}
			});
			dialog.show(fragmentManager, CancelableLoadingDialog.FRAGMENT_TAG);
		}
	}
	
	/**
	 * Removes the loading moods idicator and enable the next button.
	 */
	private void hideLoadingIndication() {
		mTextMoodsTitle.setText(R.string.discovery_mood_title);
		mButtonNextSkip.setVisibility(View.VISIBLE);
		
		// hides the loading dialog.
		FragmentManager fragmentManager = getFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag(CancelableLoadingDialog.FRAGMENT_TAG);
		if (fragment != null) {
			CancelableLoadingDialog dialog = (CancelableLoadingDialog) fragment;
			dialog.dismiss();
		}
	}
	
	private void setLastMoodSelectedState() {
		// checks if there was any selected mood.
		if (mLastSelectedItem != null) {
			Mood mood = (Mood) mLastSelectedItem.getTag();
			ImageView itemView = (ImageView) findViewByMood(mood);
			
			mLastSelectedItem = itemView;
			updateSelectedItemSpot(mLastSelectedItem);
			
			if (mLastSelectedItem != null)
				mLastSelectedItem.setVisibility(View.INVISIBLE);
		} else {
			// checks if there are any given mood which was selected before.
			if (mIsEditMode) {
				Bundle data = getArguments();
				if (data != null && data.containsKey(DiscoveryActivity.ARGUMENT_MOOD)) {
					Mood mood = (Mood) data.getSerializable(DiscoveryActivity.ARGUMENT_MOOD);
					if (mood != null) {
						ImageView itemView = (ImageView) findViewByMood(mood);
						
						mLastSelectedItem = itemView;
						updateSelectedItemSpot(mLastSelectedItem);
						
						if (mLastSelectedItem != null)
							mLastSelectedItem.setVisibility(View.INVISIBLE);
					}
				}
			}
		}
	}
	
	private class MoodsPrefetchingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (MoodPrefetchingService.INTENT_ACTION_PREFETCHING_MOODS_SYNC_EVENT.equalsIgnoreCase(intent.getAction())) {
				if (intent.getBooleanExtra(MoodPrefetchingService.EXTRA_PREFETCHING_MOODS_SYNC_SUCCESS_FLAG, false)) {
					
					getActivity().removeStickyBroadcast(intent);
					getActivity().unregisterReceiver(this);
					
					// hides the loading indication.
					hideLoadingIndication();
					// shows the moods.
					helperGenerateItems();
				}
			}
		}
		
	}

	private static final class CancelableLoadingDialog extends DialogFragment {
		
		static final String FRAGMENT_TAG = "CANCELABLE_LOADING_DIALOG_FRAGMENT";
		
		private final int message;
		private final OnCancelListener listener;
		
		public CancelableLoadingDialog(int message, OnCancelListener listner) {
			this.message = message;
			this.listener = listner;
		}
		
		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			// creates the dialog itself.
			final ProgressDialog dialog = new ProgressDialog(getActivity());
		    dialog.setMessage(getResources().getString(message));
		    dialog.setIndeterminate(true);
		    dialog.setCanceledOnTouchOutside(false);
		    dialog.setOnKeyListener(new OnKeyListener() {
				
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						if (listener != null) {
							listener.onCancel(dialog);
						}
					    return true;
					}
					 
					return false;
				}
			});
		    return dialog;
		}
	}
	
	
}
