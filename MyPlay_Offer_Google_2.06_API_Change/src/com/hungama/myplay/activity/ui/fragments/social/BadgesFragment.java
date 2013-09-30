package com.hungama.myplay.activity.ui.fragments.social;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.social.Badge;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileBadges;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialProfileBadgesOperation;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

/**
 * Shows grid of Badges for a given user. 
 * For creating the fragment an argument  {@code FRAGMENT_ARGUMENT_USER_ID} must be pased
 * or {@code IllegalArgumentException} will be thrown.
 */
public class BadgesFragment extends Fragment implements CommunicationOperationListener {

	private static final String TAG = "BadgesFragment";
	
	public static final String FRAGMENT_ARGUMENT_USER_ID = "fragment_argument_user_id";
	
	private Context mApplicationContext;
	private DataManager mDataManager;
	
	private String mUserId = null;
	private ProfileBadges mProfileBadges = null;
	private List<Badge> mBadges = new ArrayList<Badge>();
	
//	private ImageFetcher mImageFetcher;
	
	private LinearLayout mCurrentBadgeBar;
	private ImageView mImageSelectedBdge;
	private TextView mTextSelectedBadge;
	
	private GridView mGridViewBadges;
	private BadgesGridAdapter mBadgesGridAdapter;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// gets the user id, to get his badges.
		Bundle arguments = getArguments();
		if (arguments != null && arguments.containsKey(FRAGMENT_ARGUMENT_USER_ID)) {
			mUserId = arguments.getString(FRAGMENT_ARGUMENT_USER_ID);
			
			if (TextUtils.isEmpty(mUserId))
				throw new IllegalArgumentException("Must contain and argument: FRAGMENT_ARGUMENT_USER_ID.");
			
		} else {
			throw new IllegalArgumentException("Must contain and argument: FRAGMENT_ARGUMENT_USER_ID.");
		}
		
		mApplicationContext = getActivity().getApplicationContext();
		mDataManager = DataManager.getInstance(mApplicationContext);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_social_badges, container, false);
		
		intializeUserControls(rootView);
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (mProfileBadges == null) {
			// gets the profile badges for the given user.
			mDataManager.getProfileBadges(mUserId, this);
		}
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key));
		FlurryAgent.onPageView();
		
		if(mDataManager.getApplicationConfigurations().getPartnerUserId().equalsIgnoreCase(mUserId)){
			FlurryAgent.logEvent("My Badges");
		}else{
			FlurryAgent.logEvent("Others Badges");
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
//		if (mImageFetcher != null) {
//			mImageFetcher.setExitTasksEarly(false);
//		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
//		if (mImageFetcher != null) {
//			mImageFetcher.setExitTasksEarly(false);
//			mImageFetcher.flushCache();
//		}
	}
	
	@Override
	public void onStop() {
		
		mDataManager.cancelGetProfileBadges();
		
		super.onStop();
		
		FlurryAgent.onEndSession(getActivity());
	}
	
	@Override
	public void onDestroy() {
		
		mDataManager = null;
		mProfileBadges = null;
		
//		if (mImageFetcher != null) {
//			mImageFetcher.closeCache();
//			mImageFetcher = null;
//		}
		
		super.onDestroy();
	}
	
	private void intializeUserControls(View rootView) {
		
		// initializes the components.
		mCurrentBadgeBar = (LinearLayout) rootView.findViewById(R.id.badges_current_badge_bar);
		mImageSelectedBdge = (ImageView) rootView.findViewById(R.id.badges_current_badge_icon);
		mTextSelectedBadge = (TextView) rootView.findViewById(R.id.badges_current_badge_name);
		mGridViewBadges = (GridView) rootView.findViewById(R.id.badges_gridview);
		mBadgesGridAdapter = new BadgesGridAdapter();
		
		// initializes the image loader.
		mImageSelectedBdge.measure(0, 0);
		int thumbSize = mImageSelectedBdge.getMeasuredHeight();
		
		ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(getActivity(), DataManager.FOLDER_THUMBNAILS_CACHE);
        cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);
        cacheParams.compressFormat = CompressFormat.PNG;
        
//		mImageFetcher = new ImageFetcher(getActivity(), thumbSize);
//		mImageFetcher.addImageCache(getChildFragmentManager(), cacheParams);
//		// WARNING: Do Not set this boolean to true
//        mImageFetcher.setImageFadeIn(false);
        
        // binds the data adapter with the list.
        mGridViewBadges.setAdapter(mBadgesGridAdapter);
        
        // sets the title.
        String title = getResources().getString(R.string.badges_title);
        ((ProfileActivity) getActivity()).setTitleBarText(title);
	}
	
	private void populateUserControlls() {
		if (mProfileBadges.currentBadge != null) {
			mTextSelectedBadge.setText(mProfileBadges.currentBadge.get(0).name);
//			mImageFetcher.loadImage(mProfileBadges.currentBadge.get(0).imageUrl, mImageSelectedBdge);
			Picasso.with(mApplicationContext).cancelRequest(mImageSelectedBdge);
			if (mApplicationContext != null && mProfileBadges.currentBadge.get(0).imageUrl != null && !TextUtils.isEmpty(mProfileBadges.currentBadge.get(0).imageUrl)) {
				Picasso.with(mApplicationContext)
						.load(mProfileBadges.currentBadge.get(0).imageUrl)
						.into(mImageSelectedBdge);
			}
			
		} else {
			mTextSelectedBadge.setVisibility(View.INVISIBLE);
			mImageSelectedBdge.setVisibility(View.INVISIBLE);
		}
		
		mCurrentBadgeBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialogForBadge(mProfileBadges.currentBadge.get(0));
			}
		});
		
		mGridViewBadges.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Badge badge = mProfileBadges.badges.get(position);
				showDialogForBadge(badge);
			}
		});
		
		mBadgesGridAdapter.notifyDataSetChanged();
	}
	
	private void showDialogForBadge(Badge badge) {
		// creates a dialog that shows the badge's information.
		final Dialog dialog = new Dialog(getActivity());
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_badge_info);
		dialog.setCancelable(true);
		
		TextView title = (TextView) dialog.findViewById(R.id.badges_info_dialog_title_text);
		ImageButton closeButton = (ImageButton) dialog.findViewById(R.id.badges_info_dialog_title_close_button);
		ImageView badgeIcon = (ImageView) dialog.findViewById(R.id.badges_info_dialog_badge_icon);
		TextView badgeDescription = (TextView) dialog.findViewById(R.id.badges_info_dialog_badge_description);
		
		title.setText(badge.name);
		badgeDescription.setText(badge.description);
//		mImageFetcher.loadImage(badge.imageUrl, badgeIcon);
		Picasso.with(mApplicationContext).cancelRequest(badgeIcon);
		if (mApplicationContext != null && badge.imageUrl != null && !TextUtils.isEmpty(badge.imageUrl)) {
			Picasso.with(mApplicationContext)
					.load(badge.imageUrl)
					.into(badgeIcon);
		}
		
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
			}
		});
		
		dialog.show();
	}
	
	// ======================================================
	// Communication callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_BADGES) {
			mProfileBadges = (ProfileBadges) responseObjects.get(SocialProfileBadgesOperation.RESULT_KEY_PROFILE_BADGES);
			mBadges = mProfileBadges.badges;
			
			populateUserControlls();
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		if (errorType != ErrorType.OPERATION_CANCELLED)
			Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
	}
	
	
	// ======================================================
	// Badges Grid Adapter.
	// ======================================================
	
	private static class ViewHolder {
		ImageView icon;
		TextView name;
	}
	
	private class BadgesGridAdapter extends BaseAdapter {
		
		private LayoutInflater layoutInflater;
		
		public BadgesGridAdapter() {
			layoutInflater = (LayoutInflater) getActivity()
					.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mBadges.size();
		}

		@Override
		public Object getItem(int position) {
			return mBadges.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.list_item_badges_single_badge, parent, false);
				viewHolder = new ViewHolder();
				
				viewHolder.icon = (ImageView) convertView.findViewById(R.id.badges_list_item_badge_icon);
				viewHolder.name = (TextView) convertView.findViewById(R.id.badges_list_item_badge_name);
				
				convertView.setTag(viewHolder);
				
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			Badge badge = mBadges.get(position);
			
			viewHolder.name.setText(badge.name);
//			mImageFetcher.loadImage(badge.imageUrl, viewHolder.icon);
			Picasso.with(mApplicationContext).cancelRequest(viewHolder.icon);
			if (mApplicationContext != null && badge.imageUrl != null && !TextUtils.isEmpty(badge.imageUrl)) {
				Picasso.with(mApplicationContext).load(badge.imageUrl).into(viewHolder.icon);
			}
			return convertView;
		}
	}
	
}
