package com.hungama.myplay.activity.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hungama.myplay.activity.R;

/**
 * Presents the "Global Menu" expandable list in the ActionBar
 */
public class GlobalMenuFragment extends Fragment implements ExpandableListView.OnChildClickListener {
	
	private static final String TAG = "GlobalMenuFragment";
	
	private Context mContext;
	private Resources mResources;
	private LayoutInflater mLayoutInflater;
	
	private ExpandableListView mExpandableListView;
	private SettingsAdapter mSettingsAdapter;
	
	private List<Category> mCategories;
	
	private OnGlobalMenuItemSelectedListener mOnGlobalMenuItemSelectedListener;

	
	// ======================================================
	// FRAGMENTS LIFECYCLE AND PRIVATE HELPER METHODS.
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = getActivity().getApplicationContext();
		mResources = mContext.getResources();
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// creates all the menu items and their categories.
		createSettings();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_global_menu, container, false);
		// catches the click on any other view except the list.
		rootView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		
		View rootView = getView();
		
		mExpandableListView = (ExpandableListView) rootView.findViewById(R.id.main_settings_expandablelistview);
		mExpandableListView.setChildDivider(mResources.getDrawable(R.drawable.main_actionbar_settings_menu_item_separator));
		mExpandableListView.setDivider(mResources.getDrawable(R.drawable.main_actionbar_settings_menu_item_separator));
		mExpandableListView.setDividerHeight(1);
		mExpandableListView.setOnChildClickListener(this);
		mExpandableListView.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
		mSettingsAdapter = new SettingsAdapter();
		
		mExpandableListView.setAdapter(mSettingsAdapter);
		
		// opens the first group
		mExpandableListView.expandGroup(0);
		
		super.onStart();
	}
	
	private void createSettings() {
		
		List<MenuItem> quickLinksItems = createQuickLinksSettings();
		List<MenuItem> myPlayItems = createMyPlaySettings();
		List<MenuItem> moreItems = createMoreSettings();
		
		mCategories = new ArrayList<GlobalMenuFragment.Category>();
		
		mCategories.add(new Category(CATEGORY_QUICK_LINKS, 
				R.string.main_actionbar_settings_category_quick_links, quickLinksItems));
		mCategories.add(new Category(CATEGORY_MY_PLAY, 
				R.string.main_actionbar_settings_category_my_play, myPlayItems));
		mCategories.add(new Category(CATEGORY_MORE, 
				R.string.main_actionbar_settings_category_more, moreItems));
		
	}
	
	private List<MenuItem> createQuickLinksSettings() {
		List<MenuItem> quickLinksItems = new ArrayList<GlobalMenuFragment.MenuItem>();
		
		quickLinksItems.add(new MenuItem(MENU_ITEM_SPECIALS, 
				R.string.main_actionbar_settings_menu_item_specials, 
				R.drawable.icon_main_settings_specials));
		quickLinksItems.add(new MenuItem(MENU_ITEM_APP_TOUR, 
				R.string.main_actionbar_settings_menu_item_app_tour, 
				R.drawable.icon_main_settings_app_tour));
		quickLinksItems.add(new MenuItem(MENU_ITEM_MUSIC, 
				R.string.main_actionbar_settings_menu_item_music, 
				R.drawable.icon_main_settings_music));
		quickLinksItems.add(new MenuItem(MENU_ITEM_VIDEOS, 
				R.string.main_actionbar_settings_menu_item_videos, 
				R.drawable.icon_main_settings_videos));
		quickLinksItems.add(new MenuItem(MENU_ITEM_LIVE_RADIO, 
				R.string.main_actionbar_settings_menu_item_live_radio, 
				R.drawable.icon_main_settings_live_radio));
		quickLinksItems.add(new MenuItem(MENU_ITEM_DISCOVER, 
				R.string.main_actionbar_settings_menu_item_discover, 
				R.drawable.icon_main_settings_discover));
		quickLinksItems.add(new MenuItem(MENU_ITEM_MY_STREAM, 
				R.string.main_actionbar_settings_menu_item_my_stream, 
				R.drawable.icon_main_settings_my_stream));
		
		return quickLinksItems;
	}
	
	private List<MenuItem> createMyPlaySettings() {
		List<MenuItem> myPlayItems = new ArrayList<GlobalMenuFragment.MenuItem>();
		
		myPlayItems.add(new MenuItem(MENU_ITEM_MY_PROFILE, 
				R.string.main_actionbar_settings_menu_item_my_profile, 
				R.drawable.icon_main_settings_my_profile));
		myPlayItems.add(new MenuItem(MENU_ITEM_MY_COLLECTIONS, 
				R.string.main_actionbar_settings_menu_item_my_collections, 
				R.drawable.icon_main_settings_my_collection));
		myPlayItems.add(new MenuItem(MENU_ITEM_MY_FAVORITES, 
				R.string.main_actionbar_settings_menu_item_my_favorites, 
				R.drawable.icon_main_settings_my_favorites));
		myPlayItems.add(new MenuItem(MENU_ITEM_MY_PLAYLISTS, 
				R.string.main_actionbar_settings_menu_item_my_playlists, 
				R.drawable.icon_main_settings_my_playlists));
		myPlayItems.add(new MenuItem(MENU_ITEM_MY_DISCOVERIES, 
				R.string.main_actionbar_settings_menu_item_my_discoveries, 
				R.drawable.icon_main_settings_my_discoveries));
		myPlayItems.add(new MenuItem(MENU_ITEM_MY_PREFERENCES, 
				R.string.main_actionbar_settings_menu_item_my_preferences, 
				R.drawable.icon_main_settings_my_preferences));
		myPlayItems.add(new MenuItem(MENU_ITEM_SETTINGS_AND_ACCOUNTS, 
				R.string.main_actionbar_settings_menu_item_settings_and_accounts, 
				R.drawable.icon_main_settings_settings_and_accounts));
		
		return myPlayItems;
	}
	
	private List<MenuItem> createMoreSettings() {
		List<MenuItem> moreItems = new ArrayList<GlobalMenuFragment.MenuItem>();
		
		moreItems.add(new MenuItem(MENU_ITEM_REWARDS, 
				R.string.main_actionbar_settings_menu_item_rewards, 
				R.drawable.icon_main_settings_rewards));
		moreItems.add(new MenuItem(MENU_ITEM_INVITE_FRIENDS, 
				R.string.main_actionbar_settings_menu_item_invite_friends, 
				R.drawable.icon_main_settings_invite_friends));
		moreItems.add(new MenuItem(MENU_ITEM_RATE_THIS_APP, 
				R.string.main_actionbar_settings_menu_item_rate_this_app, 
				R.drawable.icon_main_settings_rate_this_app));
		moreItems.add(new MenuItem(MENU_ITEM_GIVE_FEEDBACK, 
				R.string.main_actionbar_settings_menu_item_give_feedback, 
				R.drawable.icon_main_settings_give_feedback));
		moreItems.add(new MenuItem(MENU_ITEM_HELP_FAQ, 
				R.string.main_actionbar_settings_menu_item_help_faq, 
				R.drawable.icon_main_settings_help_faq));
		moreItems.add(new MenuItem(MENU_ITEM_ABOUT, 
				R.string.main_actionbar_settings_menu_item_about, 
				R.drawable.icon_main_settings_about));
		
		return moreItems;
	}
	
	
	// ======================================================
	// PRIVATE HELPER CLASSES.
	// ======================================================
	
	private static class MenuItemViewHolder {
		ImageView icon;
		TextView label;
		View separator;
	}
	
	private static class CategoryViewHolder {
		TextView label;
		TextView expandIndicator;
	}
	
	private final class SettingsAdapter extends BaseExpandableListAdapter {
		
		@Override
		public boolean areAllItemsEnabled() {
		    return true;
		}
		
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mCategories.get(groupPosition).getMenuItems().get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return mCategories.get(groupPosition).getMenuItems().get(childPosition).getId();
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			MenuItemViewHolder viewHolder;
			
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.list_item_main_actionbar_settings_menu_item, parent, false);
				
				viewHolder = new MenuItemViewHolder();
				viewHolder.icon = (ImageView) convertView.findViewById(R.id.main_setting_menu_item_icon);
				viewHolder.label = (TextView) convertView.findViewById(R.id.main_setting_menu_item_label);
				viewHolder.separator = (View) convertView.findViewById(R.id.main_setting_menu_item_separator);
				
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (MenuItemViewHolder) convertView.getTag();
			}
			
			MenuItem menuItem = mCategories.get(groupPosition).getMenuItems().get(childPosition);
			// Specials menu item has a different text / icon color then any other item.
			if (menuItem.getId() == MENU_ITEM_SPECIALS) {
				viewHolder.label.setTextColor(
						mResources.getColor(R.color.main_actionbar_settings_menu_item_text_specials));
			} else {
				viewHolder.label.setTextColor(
						mResources.getColor(R.color.main_actionbar_settings_menu_item_text));
			}
			
			viewHolder.label.setText(menuItem.getLabelResourceId());
			viewHolder.icon.setImageDrawable(mResources.getDrawable(menuItem.getIconResourceId()));
			
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mCategories.get(groupPosition).getMenuItems().size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mCategories.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return mCategories.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return mCategories.get(groupPosition).getId();
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			CategoryViewHolder viewHolder;
			
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.list_item_main_actionbar_settings_category, parent, false);
				
				viewHolder = new CategoryViewHolder();
				viewHolder.label = (TextView) convertView.findViewById(R.id.main_setting_category_label);
				viewHolder.expandIndicator = (TextView) convertView.findViewById(R.id.main_setting_category_expand_indicator);
				
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (CategoryViewHolder) convertView.getTag();
			}
			
			Category category = mCategories.get(groupPosition);
			
			viewHolder.label.setText(category.getLabelResourceId());
			if (isExpanded) {
				viewHolder.expandIndicator.setText(R.string.main_actionbar_settings_category_expanded);
			} else {
				viewHolder.expandIndicator.setText(R.string.main_actionbar_settings_category_collapsed);
			}
			
			return convertView;
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
		public void onGroupExpanded(int groupPosition) {
			// closes all the other groups.
			int size = mCategories.size();
			for (int i = 0; i < size; i++) {
				if (i != groupPosition) {
					mExpandableListView.collapseGroup(i);
				}
			}
			
			super.onGroupExpanded(groupPosition);
		}
	}
	
	/*
	 * represents an item in the menu.
	 */
	private final class MenuItem {
		
		private final int id;
		private final int labelResourceId;
		private final int iconResourceId;
		
		public MenuItem(int id, int labelResourceId, int iconResourceId) {
			this.id = id;
			this.labelResourceId = labelResourceId;
			this.iconResourceId = iconResourceId;
		}

		public int getId() {
			return id;
		}

		/**
		 * Retrieves the resource id for the String label.
		 */
		public int getLabelResourceId() {
			return labelResourceId;
		}

		/**
		 * Retrieves the resource id foe the Drawable icon.
		 */
		public int getIconResourceId() {
			return iconResourceId;
		}
	}
	
	/*
	 * represents a category contains items in the settings.
	 */
	private final class Category {
		
		private final int id;
		private final int labelResourceId;
		private final List<MenuItem> menuItems;
		
		public Category(int id, int labelResourceId, List<MenuItem> menuItems) {
			this.id = id;
			this.labelResourceId = labelResourceId;
			this.menuItems = menuItems;
		}
		
		public int getId() {
			return id;
		}
		
		/**
		 * Retrieves the resource id contains the String lable.
		 */
		public int getLabelResourceId() {
			return labelResourceId;
		}
		
		public List<MenuItem> getMenuItems() {
			return menuItems;
		}

	}
	
	// ======================================================
	// PUBLIC.
	// ======================================================
	
	public static final int CATEGORY_QUICK_LINKS			 = 100;
	public static final int CATEGORY_MY_PLAY				 = 200;
	public static final int CATEGORY_MORE					 = 300;
	// QUICK LINKS
	public static final int MENU_ITEM_SPECIALS				 = 101;
	public static final int MENU_ITEM_APP_TOUR				 = 102;
	public static final int MENU_ITEM_MUSIC					 = 103;
	public static final int MENU_ITEM_VIDEOS				 = 104;
	public static final int MENU_ITEM_LIVE_RADIO			 = 105;
	public static final int MENU_ITEM_DISCOVER				 = 106;
	public static final int MENU_ITEM_MY_STREAM				 = 107;
	// MY PLAY
	public static final int MENU_ITEM_MY_PROFILE			 = 201;
	public static final int MENU_ITEM_MY_COLLECTIONS		 = 202;
	public static final int MENU_ITEM_MY_FAVORITES			 = 203;
	public static final int MENU_ITEM_MY_PLAYLISTS			 = 204;
	public static final int MENU_ITEM_MY_DISCOVERIES		 = 205;
	public static final int MENU_ITEM_MY_PREFERENCES		 = 206;
	public static final int MENU_ITEM_SETTINGS_AND_ACCOUNTS	 = 207;
	// MORE
	public static final int MENU_ITEM_REWARDS		 		 = 301;
	public static final int MENU_ITEM_INVITE_FRIENDS		 = 302;
	public static final int MENU_ITEM_RATE_THIS_APP 		 = 303;
	public static final int MENU_ITEM_GIVE_FEEDBACK			 = 304;
	public static final int MENU_ITEM_HELP_FAQ				 = 305;
	public static final int MENU_ITEM_ABOUT					 = 306;
	
	
	/**
	 * Interface definition to be invoked when the user has selected an item.
	 */
	public interface OnGlobalMenuItemSelectedListener {
		
		public void onGlobalMenuItemSelected(int menuItemId);
		
	}
	
	public void setOnGlobalMenuItemSelectedListener(OnGlobalMenuItemSelectedListener listener) {
		mOnGlobalMenuItemSelectedListener = listener;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
		
		
		if (mOnGlobalMenuItemSelectedListener != null) {
			mOnGlobalMenuItemSelectedListener.onGlobalMenuItemSelected((int)id);
		}
		return true;
	}
	
}
