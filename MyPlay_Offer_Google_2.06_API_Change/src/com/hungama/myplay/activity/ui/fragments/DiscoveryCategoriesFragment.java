package com.hungama.myplay.activity.ui.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.DataManager.MoodIcon;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.CategoryTypeObject;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaCategoriesOperation;
import com.hungama.myplay.activity.ui.DiscoveryActivity;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class DiscoveryCategoriesFragment extends MainFragment implements OnClickListener, CommunicationOperationListener {
	
	private static final String TAG = "DiscoveryCategoriesFragment";
	
	public interface OnDoneButtonClickedListener {
		
		public void onDoneButtonClicked(List<CategoryTypeObject> categoryTypeObject);
	}
	
	public void setOnDoneButtonClickedListener(OnDoneButtonClickedListener onDoneButtonClickedListener) {
		mOnDoneButtonClickedListener = onDoneButtonClickedListener;
	}
	
	public void setEditMode(boolean isEditMode) {
		mIsEditMode = isEditMode;
	}
	
	public boolean isInEditMood() {
		return mIsEditMode;
	}
	
	
	// ======================================================
	// Fragment's life cycle. 
	// ======================================================
	
	private boolean mIsEditMode = false;
	
	private DataManager mDataManager;
	private LayoutInflater mLayoutInflater;
	
	private List<Category> mCategories = null;;
	private List<CategoryTypeObject> mSelectedCategories;
	
	private ExpandableListView mListViewCategories;
	private ListView mListViewSelectedCategories;
	private LinearLayout mLayoutImDone;
	private LinearLayout mLayoutSelectedMood;
	private ImageView mImageSelectedMood;
	
	private CategoriesAdapter mCategoriesAdapter;
	private SelectedCategoriesAdapter mSelectedCategoriesAdapter;
	
	private OnDoneButtonClickedListener mOnDoneButtonClickedListener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Context context = getActivity().getApplicationContext();

		mDataManager = DataManager.getInstance(context);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mCategories = new ArrayList<Category>();
		mSelectedCategories = new ArrayList<CategoryTypeObject>();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_discovery_categories, container, false);
		
		initializeUserControls(rootView);
		
		populateUserControls();
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		// gets all the categories.
		mDataManager.getPreferences(this);
		
		// gets the selected categories.
		if (!Utils.isListEmpty(mSelectedCategories)) {
			if (mSelectedCategoriesAdapter == null){
				mSelectedCategoriesAdapter = new SelectedCategoriesAdapter();
				mListViewSelectedCategories.setAdapter(mSelectedCategoriesAdapter);
			} else {
				mSelectedCategoriesAdapter.notifyDataSetChanged();
			}
			
		}
		
		// checks if the activity got some.
		DiscoveryActivity discoveryActivity = (DiscoveryActivity) getActivity();
		if (discoveryActivity.hasCategoriesChanged()){
			mSelectedCategories = discoveryActivity.getCategoryTypeObjects();
			mSelectedCategoriesAdapter.notifyDataSetChanged();
		}
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
		FlurryAgent.logEvent("Discovery - preferences");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		FlurryAgent.onEndSession(getActivity());
	}
	
	@Override
	public void onStart(int operationId) {
		Logger.i(TAG, "Starts getting preferences.");
		showLoadingDialog(R.string.application_dialog_loading_content);
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		Logger.i(TAG, "Successed getting categories.");
		if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_GET) {
			List<CategoryTypeObject> categoryTypeObjects = 
					(List<CategoryTypeObject>) responseObjects.get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);
			
			if (categoryTypeObjects != null && categoryTypeObjects.size() > 0) {
				Logger.i(TAG, "Success! " + categoryTypeObjects.toString());
				
				mCategories.clear();
				
				Category category = null;
				for (CategoryTypeObject categoryTypeObject : categoryTypeObjects) {
					category = (Category) categoryTypeObject;
					category.setIsRoot(true);
					mCategories.add(category);
				}
				
				// updates the categories in the list.
				mCategoriesAdapter = new CategoriesAdapter();
				mListViewCategories.setAdapter(mCategoriesAdapter);
			}
		}
		
		hideLoadingDialog();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		Logger.e(TAG, "Failed getting preferences: " + errorType.toString() + " " + errorMessage);
		
		hideLoadingDialog();
	}
	
	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		
		if (viewId == R.id.discovery_categories_container_imdone) {
			if (mOnDoneButtonClickedListener != null) {
				mOnDoneButtonClickedListener.onDoneButtonClicked(mSelectedCategories);
			}
		}
	}
	
	// ======================================================
	// Private helper methods.
	// ======================================================
	
	private void initializeUserControls(View rootView) {
		
		if (mIsEditMode) {
			rootView.setBackgroundResource(R.drawable.background_discovery);
		}
		
		mListViewCategories = (ExpandableListView) rootView.findViewById(R.id.discovery_categories_categories);
		mListViewSelectedCategories = (ListView) rootView.findViewById(R.id.discovery_categories_selected_categories);
		mLayoutImDone = (LinearLayout) rootView.findViewById(R.id.discovery_categories_container_imdone);
		mLayoutSelectedMood = (LinearLayout) rootView.findViewById(R.id.discovery_categories_container_selected_mood);
		mImageSelectedMood = (ImageView) rootView.findViewById(R.id.discovery_categories_selected_mood);
		
		mSelectedCategoriesAdapter = new SelectedCategoriesAdapter();
		mListViewSelectedCategories.setAdapter(mSelectedCategoriesAdapter);
		
		mLayoutImDone.setOnClickListener(this);
	}
	
	private void populateUserControls() {
		// gets the selected mood from the fragment's arguments.
		Bundle data = getArguments();
		if (data != null) {
			// sets the mood if exists.
			if (data.containsKey(DiscoveryActivity.ARGUMENT_MOOD)) {
				Mood mood = (Mood) data.getSerializable(DiscoveryActivity.ARGUMENT_MOOD);
				// shows the bubble of the selected mood with it's icon.
				mLayoutSelectedMood.setVisibility(View.VISIBLE);
				mImageSelectedMood.setImageDrawable(mDataManager.getMoodIcon(mood, MoodIcon.BIG));
				
			} else {
				mLayoutSelectedMood.setVisibility(View.GONE);
			}
			
			// sets the selected categories if exist.
			if (mIsEditMode && data.containsKey(DiscoveryActivity.ARGUMENT_CATEGORIES)) {
				mSelectedCategories = (List<CategoryTypeObject>) data.getSerializable(DiscoveryActivity.ARGUMENT_CATEGORIES); 
				// updates the list.
				mSelectedCategoriesAdapter.notifyDataSetChanged();
			}
		} else {
			mLayoutSelectedMood.setVisibility(View.GONE);
		}
	}
	
	private void onCategoryTypeSelected(CategoryTypeObject categoryTypeObject) {
		// checks if the given category / genres is not already inserted.
		if (mSelectedCategories.contains(categoryTypeObject)) {
			return;
		}
		/*
		 * Categories can contain sub categories or genres,
		 * which are considered as items in the list by themselves.
		 */
		if (categoryTypeObject instanceof Category) {
			// adds the category.
			mSelectedCategories.add(categoryTypeObject);
			// adds its children too.
			Category category = (Category) categoryTypeObject;
			int size = category.getChildCount();
			if (size > 0) {
				CategoryTypeObject subCategoryTypeObject = null; 
				for (int i = 0; i < size; i++) {
					subCategoryTypeObject = category.getChildAt(i);
					if (!mSelectedCategories.contains(subCategoryTypeObject)) {
						mSelectedCategories.add(subCategoryTypeObject);
					}
				}
			}
		} else {
			mSelectedCategories.add(categoryTypeObject);
		}
		
		mSelectedCategoriesAdapter.notifyDataSetChanged();
	}
	
	private void onCategoryTypeSelectedToRemove(CategoryTypeObject categoryTypeObject) {
		if (categoryTypeObject.getType().equalsIgnoreCase(CategoryTypeObject.TYPE_CATEGORY.toString()) &&
				((Category) categoryTypeObject).getChildCount() > 0) {
			Category category = (Category) categoryTypeObject;
			CategoryTypeObject subCategoryTypeObject = null;
			int size = category.getChildCount();
			// removes all the children of this that were automaticlly added too.
			for (int i = 0; i < size; i++) {
				subCategoryTypeObject = category.getChildAt(i);
				mSelectedCategories.remove(subCategoryTypeObject);
			}
		}
		mSelectedCategories.remove(categoryTypeObject);
		mSelectedCategoriesAdapter.notifyDataSetChanged();
	}
	
	
	// ======================================================
	// Private helper controllers.
	// ======================================================
	
	private static class CategoryViewHolder {
		RelativeLayout layout;
		TextView textIndicator;
		TextView textName;
	}
	
	private final class CategoriesAdapter extends BaseExpandableListAdapter {
		
		public CategoriesAdapter() {}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mCategories.get(groupPosition).getChildAt(childPosition);
		}
		
		@Override
		public Object getGroup(int groupPosition) {
			return mCategories.get(groupPosition);
		}
		
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return mCategories.get(groupPosition).getChildAt(childPosition).getId();
		}
		
		@Override
		public long getGroupId(int groupPosition) {
			return mCategories.get(groupPosition).getId();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mCategories.get(groupPosition).getChildCount();
		}
		
		@Override
		public int getGroupCount() {
			return mCategories.size();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
		
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			CategoryViewHolder viewHolder;
			
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.list_item_discovery_categories_item, parent, false);

				viewHolder = new CategoryViewHolder();
				
				viewHolder.layout = (RelativeLayout) convertView.findViewById(R.id.discovery_categories_category);
				viewHolder.textIndicator = (TextView) convertView.findViewById(R.id.discovery_categories_category_collapse_indicator);
				viewHolder.textName = (TextView) convertView.findViewById(R.id.discovery_categories_category_name);
				
				convertView.setTag(R.id.view_tag_view_holder, viewHolder);
			} else {
				viewHolder = (CategoryViewHolder) convertView.getTag(R.id.view_tag_view_holder);
			}
			
			Category category = mCategories.get(groupPosition);
			convertView.setTag(R.id.view_tag_object, category);
			convertView.setTag(R.id.view_tag_group_position, groupPosition);
			
			/*
			 * Cancels the group expansion by clicking on the 
			 * item and makes it passable to the selected list.
			 */
			viewHolder.layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					CategoryTypeObject categoryTypeObject = (CategoryTypeObject) 
							view.getTag(R.id.view_tag_object);
					onCategoryTypeSelected(categoryTypeObject);
				}
			});
			
			if (category.getChildCount() > 0) {
				viewHolder.textIndicator.setVisibility(View.VISIBLE);
				viewHolder.textIndicator.setText(R.string.application_fold_sign);
				
				viewHolder.textIndicator.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						// get the group id.
						View itemView = (View) view.getParent();
						int groupPos = (Integer) itemView.getTag(R.id.view_tag_group_position);
						
						if (((TextView) view).getText().toString()
								.equals(getResources().getString(R.string.application_collapse_sign))){
							// opens this on and closes all the rest.
							int size = mCategories.size();
							for (int i = 0; i < size; i++) {
								if (i != groupPos) {
									// closes the group.
									mListViewCategories.collapseGroup(i);
								} else {
									if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
										mListViewCategories.expandGroup(groupPos, true);
									} else {
										mListViewCategories.expandGroup(groupPos);
									}
								}
							}
							
						} else {
							// closes this one.
							mListViewCategories.collapseGroup(groupPos);
							
						}
					}
				});
				
			} else {
				viewHolder.textIndicator.setVisibility(View.INVISIBLE);
				viewHolder.textIndicator.setOnClickListener(null);
			}
			
			// background of the category and fold / collapse sign.
			if (isExpanded) {
				viewHolder.layout.setBackgroundResource(R.drawable.background_discovery_categories_item);
				viewHolder.textIndicator.setText(R.string.application_fold_sign);
			} else {
				viewHolder.layout.setBackgroundResource(R.color.transparent);
				viewHolder.textIndicator.setText(R.string.application_collapse_sign);
			}
			
			// name.
			viewHolder.textName.setText(category.getName());

			return convertView;
		}
		
		@Override
		public View getChildView(int groupPosition, int childPosition, 
								boolean isLastChild, View convertView, ViewGroup parent) {
			
			CategoryViewHolder viewHolder;
			
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.list_item_discovery_categories_item, parent, false);

				viewHolder = new CategoryViewHolder();
				
				viewHolder.layout = (RelativeLayout) convertView.findViewById(R.id.discovery_categories_category);
				viewHolder.textIndicator = (TextView) convertView.findViewById(R.id.discovery_categories_category_collapse_indicator);
				viewHolder.textName = (TextView) convertView.findViewById(R.id.discovery_categories_category_name);
				
				convertView.setTag(R.id.view_tag_view_holder, viewHolder);
			} else {
				viewHolder = (CategoryViewHolder) convertView.getTag(R.id.view_tag_view_holder);
			}

			CategoryTypeObject categoryTypeObject = (CategoryTypeObject) getChild(groupPosition, childPosition);
			convertView.setTag(R.id.view_tag_object, categoryTypeObject);
			
			viewHolder.textIndicator.setVisibility(View.INVISIBLE);
			viewHolder.layout.setBackgroundResource(R.color.transparent);
			
			viewHolder.layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					CategoryTypeObject categoryTypeObject = (CategoryTypeObject) view.getTag(R.id.view_tag_object);
					onCategoryTypeSelected(categoryTypeObject);
				}
			});
			
			// name.
			viewHolder.textName.setText(categoryTypeObject.getName());
			viewHolder.textName.setPadding((int) getResources().
					getDimension(R.dimen.discovery_categories_sub_item_padding_left), 0, 0, 0);
			
			return convertView;
		}
		
	}
	
	private static class SelectedCategoryViewHolder {
		RelativeLayout layout;
		RelativeLayout layoutMarginWrapper;
		TextView textName;
	}
	
	private final class SelectedCategoriesAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mSelectedCategories.size();
		}

		@Override
		public Object getItem(int position) {
			return mSelectedCategories.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mSelectedCategories.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SelectedCategoryViewHolder viewHolder;
			CategoryTypeObject categoryTypeObject = mSelectedCategories.get(position);
			
			if (convertView == null) {
				viewHolder = new SelectedCategoryViewHolder();
				// differentiates between a category type that has children and not. 
				convertView = mLayoutInflater.inflate(R.layout.list_item_discovery_categories_selected_item, parent, false);
				
				viewHolder.layout = (RelativeLayout) convertView.findViewById(R.id.discovery_categories_selected_item);
				viewHolder.layoutMarginWrapper = (RelativeLayout) convertView.findViewById(R.id.discovery_categories_selected_item_margin_wrapper);
				viewHolder.textName = (TextView) convertView.findViewById(R.id.discovery_categories_selected_item_name);
				
				convertView.setTag(R.id.view_tag_view_holder, viewHolder);
			} else {
				viewHolder = (SelectedCategoryViewHolder) convertView.getTag(R.id.view_tag_view_holder);
			}
			
			convertView.setTag(R.id.view_tag_object, categoryTypeObject);
			
			viewHolder.layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					CategoryTypeObject categoryTypeObject = (CategoryTypeObject) view.getTag(R.id.view_tag_object);
					// call to remove from the list.
					onCategoryTypeSelectedToRemove(categoryTypeObject);
				}
			});
			viewHolder.textName.setText(categoryTypeObject.getName());
			
			RelativeLayout.LayoutParams params = 
					new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			// set the width and height of the item.
			if (categoryTypeObject instanceof Category && ((Category) categoryTypeObject).isRoot()) {
				params.setMargins(0, 0, 0, 0);
			} else {
				int leftMargin = (int) getResources().getDimension(R.dimen.discovery_categories_sub_item_padding_left);
				params.setMargins(leftMargin, 0, 0, 0);
			}
			
			viewHolder.layoutMarginWrapper.setLayoutParams(params);
			
			return convertView;
		}
		
	}
	
}
