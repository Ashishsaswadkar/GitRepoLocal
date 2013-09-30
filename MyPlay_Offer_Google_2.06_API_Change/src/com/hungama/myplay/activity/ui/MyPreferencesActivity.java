package com.hungama.myplay.activity.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.CategoryTypeObject;
import com.hungama.myplay.activity.data.dao.hungama.MyCategory;
import com.hungama.myplay.activity.data.dao.hungama.MyPreferencesResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaCategoriesOperation;
import com.hungama.myplay.activity.operations.hungama.PreferencesRetrieveOperation;
import com.hungama.myplay.activity.operations.hungama.PreferencesSaveOperation;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * Controller for presenting details of the given MediaItem.
 */
public class MyPreferencesActivity extends MainActivity implements CommunicationOperationListener, OnClickListener {
	
	private static final String TAG = "MyPreferencesActivity";
	
	private static final int SUCCESS = 1;
	
	private DataManager mDataManager;
	
	private TextView mTitleBarText;	
	private List<Category> mCategories;
	private List<MyCategory> mCategoriesRetrieve;
	private LinearLayout mFirstRowLayout;
	private LinearLayout mLastRowLayout;
	private LinearLayout mFirstSubRowLayout;
	private LinearLayout.LayoutParams mRowParams;
	private LinearLayout.LayoutParams mSubRowParams;
	private LinearLayout categoriesLayout;
	private LinearLayout subCategoriesLayout;
	private boolean wasHereAlready = true;
	private boolean wasHereAlreadySub = true;
	private boolean wasHereAlreadyLastRow = true;
	private int buttonCenter;
	private List<CategoryTypeObject> subCategories;	
	private int rowWidth;
	private Button buttonWithSubCategories;
	private int buttonIdWithSubCategories;
	private List<Integer> buttonIdsToSave;

	private boolean isPreferencesChanged;
	private int rawPadding;
	
	// ======================================================
	// Activity life-cycle callbacks. 
	// ======================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		overridePendingTransition(R.anim.slide_left_enter, 0);
		
		setContentView(R.layout.activity_my_preferences);
		
		mDataManager = DataManager.getInstance(this.getApplicationContext());
		
		// SetS title bar
		mTitleBarText = (TextView) findViewById(R.id.main_title_bar_text);
		mTitleBarText.setText(getResources().getString(R.string.my_preferences_page_text_title));

		// Hide the Arrow to the right
		ImageButton arrow = (ImageButton) findViewById(R.id.main_title_bar_button_options);
		arrow.setVisibility(View.GONE);
					
		mCategories = new ArrayList<Category>();
		
		
		// Create the first row (LinearLayout) - Categories
		categoriesLayout = (LinearLayout) findViewById(R.id.linearlayout_my_preferences);
		mRowParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		rawPadding = getResources().getDimensionPixelSize(R.dimen.search_popular_keyword_raw_padding);
		
		mFirstRowLayout = new LinearLayout(this);
    	mFirstRowLayout.setPadding(0, 0, 0, rawPadding);
    	mFirstRowLayout.setLayoutParams(mRowParams);
    	categoriesLayout.addView(mFirstRowLayout);
		mFirstRowLayout.measure(0, 0);			
    	
		buttonIdsToSave = new ArrayList<Integer>();
		
		mDataManager.getMyPreferences(this);	
				
	}	

	/* (non-Javadoc)
	 * @see com.hungama.myplay.activity.ui.MainActivity#onStart()
	 */
	@Override
	protected void onStart() {
		Button saveButton = (Button) findViewById(R.id.my_preferences_button_done);
		saveButton.setOnClickListener(this);
		super.onStart();
	}
	@Override
	protected NavigationItem getNavigationItem() {
		return NavigationItem.OTHER;
	}

	
	// ======================================================
	// Operation Callback
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_RETRIEVE) {
			showLoadingDialog(R.string.application_dialog_loading_content);
		} else if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_GET) {
			showLoadingDialog(R.string.application_dialog_loading_content);
		} else if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_SAVE) {
			showLoadingDialog(R.string.application_dialog_loading_content);
		}
	}
		
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		
		if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_RETRIEVE) {
			Logger.i(TAG, "Successed getting users Preferences.");
			
			MyPreferencesResponse myPreferencesResponse = (MyPreferencesResponse) responseObjects.get(PreferencesRetrieveOperation.RESPONSE_KEY_PREFERENCES_RETRIEVE);
			
			if (myPreferencesResponse.getMycategories() != null && myPreferencesResponse.getMycategories().size() > 0) {
				Logger.i(TAG, "Success! " + myPreferencesResponse.getMycategories().toString());
				
				mCategoriesRetrieve = new ArrayList<MyCategory>();
				mCategoriesRetrieve = myPreferencesResponse.getMycategories();
			}
			
			mDataManager.getPreferences(this);
												
		}
				
		else if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_GET) {
			Logger.i(TAG, "Successed getting categories.");
			List<CategoryTypeObject> categoryTypeObjects = (List<CategoryTypeObject>) responseObjects.get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);
			if (categoryTypeObjects != null && categoryTypeObjects.size() > 0) {
				Logger.i(TAG, "Success! " + categoryTypeObjects.toString());
				
				mCategories.clear();
				
				Category category = null;
				for (CategoryTypeObject categoryTypeObject : categoryTypeObjects) {
					category = (Category) categoryTypeObject;
					category.setIsRoot(true);
					mCategories.add(category);
				}
				
				if (mCategories != null) {
					populateCategories(mCategories);
				}
				
//				ViewTreeObserver vto = mFirstRowLayout.getViewTreeObserver(); 
//				vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//					
//					@Override
//					public void onGlobalLayout() {
//						if (mCategories != null && wasHereAlready) {
//							wasHereAlready = false;
//							populateCategories(mCategories);
//						}
//						
//					}
//				});			
			}
		}
		
		else if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_SAVE) {
			Logger.i(TAG, "Successed saving my preferences.");
			
			MyPreferencesResponse myPreferencesResponse = 
					(MyPreferencesResponse) responseObjects.get(PreferencesSaveOperation.RESPONSE_KEY_PREFERENCES_SAVE);
			
			if (myPreferencesResponse.getCode() == SUCCESS) { 
				Toast.makeText(this, getResources().getString(R.string.my_preferences_saved_categories), Toast.LENGTH_LONG).show();
				hideLoadingDialog();
				Intent resultData = new Intent();
				resultData.putExtra(HomeActivity.EXTRA_MY_PREFERENCES_IS_CHANGED, isPreferencesChanged);
				setResult(HomeActivity.RESULT_OK, resultData);
				finish();
			}
		}
		
		hideLoadingDialog();
	}	
		
	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_RETRIEVE) {
			Logger.i(TAG, "Failed to retrieve my preferences");
		} else if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_GET) {
			Logger.i(TAG, "Failed loading global preferences");	
		} else if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_SAVE) {
			Logger.i(TAG, "Failed saving my preferences");
			
		}
		hideLoadingDialog();
		
	}
	
	
	// ======================================================
	// onClick Method
	// ======================================================
	
	@Override
	public void onClick(View v) {
		Logger.d(TAG, "Simple click on: " + v.toString());
		int viewId = v.getId();
		
		switch (viewId) {
		case R.id.my_preferences_button_done:
			if (isUserPreferencesChange()) {
				savePreferences();
			} else {
				Toast.makeText(this, getResources().getString(R.string.my_preferences_saved_categories), Toast.LENGTH_LONG).show();
				Intent resultData = new Intent();
				resultData.putExtra(HomeActivity.EXTRA_MY_PREFERENCES_IS_CHANGED, isPreferencesChanged);
				setResult(HomeActivity.RESULT_OK, resultData);
				finish();
			}
			break;

		default:
			break;
		}
	}

		
	// ======================================================
	// Helper methods
	// ======================================================
	
	private void populateCategories(List<Category> categories) {
		if (categories != null) {
	    	
	    	Resources resources = getResources();
	    	
	    	// creates the properties for constructing the buttons and their rows.
	    	//final int rawPadding = resources.getDimensionPixelSize(R.dimen.search_popular_keyword_raw_padding);
	    	final int keywordMargin = resources.getDimensionPixelSize(R.dimen.search_popular_keyword_margin);
	    	final int keywordHeight = resources.getDimensionPixelSize(R.dimen.search_popular_keyword_height);
	    	final float keywordTextSize = (float) resources.getDimension(R.dimen.search_popular_keyword_text_size);
	    	final int layoutMargin = resources.getDimensionPixelSize(R.dimen.search_content_margin);
	    			
	    	// creates properties for layouting the buttons within the rows.
	    	
	    	rowWidth = mFirstRowLayout.getWidth();
	    	int currentWidth = 0;
		    int keywordButtonWidth = 0;
		    
		    Button keywordButton = null;
	    	int rowId = 1;
	    	int buttonId = 0;
			for (Category category : categories) {
				// constructs the button.
				keywordButton = new Button(this);
				keywordButton.setText(category.getName());
				keywordButton.setId((int)(category.getId()));
				buttonId++;
				keywordButton.setBackgroundResource(R.drawable.background_my_preferences_buttons);
				if (!Utils.isListEmpty(mCategoriesRetrieve)) {
					for (MyCategory categoryRetrieved : mCategoriesRetrieve ) {
						if (categoryRetrieved.getId() == category.getId()) {
							keywordButton.setSelected(true);
							buttonIdsToSave.add((int) categoryRetrieved.getId());
							break;
						} else {
							keywordButton.setSelected(false);
						}
					}	
				} else {
					keywordButton.setSelected(false);
				}
							
				keywordButton.setTextSize(keywordTextSize);
				keywordButton.setTextColor(resources.getColorStateList(R.color.main_search_popular_keyword_text));				
				keywordButton.setSingleLine(true);
				keywordButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {						
						if (v.isSelected()) {
							v.setSelected(false);
							// Remove from list
							for (Integer id : buttonIdsToSave) {
								if (id == v.getId()) {
									Logger.i(TAG, buttonIdsToSave.toString());
									buttonIdsToSave.remove(id);
									Logger.i(TAG, buttonIdsToSave.toString());
									break;
								}
							}
							
						} else {
							v.setSelected(true);
							buttonIdsToSave.add(v.getId());
						}
						
					}
				});
				
				// set margins for the button
				LayoutParams paramsButton = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//				paramsButton.setMargins(0, 0, keywordMargin, 0);
				keywordButton.setLayoutParams(paramsButton);							
				
				// measures it to the sum width of the row.
				keywordButton.measure(0, 0);
				keywordButtonWidth = keywordButton.getMeasuredWidth()/* + keywordMargin*/;							
				
				if (rowWidth - currentWidth >= keywordButtonWidth) {
					mFirstRowLayout.addView(keywordButton);
					View viewMargin = new View(this);
					LayoutParams paramsView = new LayoutParams(keywordMargin, keywordHeight);
					viewMargin.setLayoutParams(paramsView);
					mFirstRowLayout.addView(viewMargin);
					currentWidth += keywordButtonWidth + keywordMargin;
				} else {
					
					LinearLayout llNewRow = new LinearLayout(this);			
					llNewRow.setPadding(0, 0, 0, rawPadding);
					llNewRow.setId(rowId);
					rowId++;
					llNewRow.setLayoutParams(mRowParams);					
					categoriesLayout.addView(llNewRow);
					llNewRow.addView(keywordButton);
					View viewMargin = new View(this);
					LayoutParams paramsView = new LayoutParams(keywordMargin, keywordHeight);
					viewMargin.setLayoutParams(paramsView);
					llNewRow.addView(viewMargin);
					currentWidth = keywordButtonWidth + keywordMargin;
					mFirstRowLayout = llNewRow;
					mLastRowLayout = llNewRow;
				}
				
				if (category.getChildCount() != 0) {
					buttonIdWithSubCategories = buttonId;
					buttonWithSubCategories = new Button(this);
					buttonWithSubCategories = keywordButton;
					buttonCenter = currentWidth - (keywordMargin/2);
					subCategories = category.getCategoryTypeObjects();						
				}
				
				if (category.getName().equalsIgnoreCase("Regional")) {
					keywordButton.setClickable(false);	
					keywordButton.setBackgroundResource(R.drawable.background_my_preferences_buttons_open);
				}
				
			}
			
			
			
			
			
			
			// Start populate the bottom part
			ViewTreeObserver vto = mLastRowLayout.getViewTreeObserver(); 
			vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				
				@Override
				public void onGlobalLayout() {
					// Create the first row (LinearLayout) - SubCategories
					if (wasHereAlreadyLastRow) {											
						
						wasHereAlreadyLastRow = false;
					
						subCategoriesLayout = (LinearLayout) findViewById(R.id.linearlayout_my_preferences_bottom);
						mSubRowParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);				
						
						mFirstSubRowLayout = new LinearLayout(MyPreferencesActivity.this);
						mFirstSubRowLayout.setBackgroundColor(getResources().getColor(R.color.background_sub_categories));
						mFirstSubRowLayout.setPadding(0, 0, 0, rawPadding);
						mFirstSubRowLayout.setLayoutParams(mSubRowParams);
				    	subCategoriesLayout.addView(mFirstSubRowLayout);

				    	
						ViewTreeObserver vto = mFirstSubRowLayout.getViewTreeObserver(); 
						vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
							
							@Override
							public void onGlobalLayout() {
								if (wasHereAlreadySub) {
									
									//set the arrow image in the right offset
									ImageView greyArrow = (ImageView) findViewById(R.id.image_my_preferences_up_arrow);
									Button button = (Button) findViewById(buttonWithSubCategories.getId());
									
									int[] location = {0,0};
									button.getLocationOnScreen(location);
									int paddingLeft = location[0] + layoutMargin;
									greyArrow.setPadding(paddingLeft, 0, 0, 0);
									
									wasHereAlreadySub = false;
									populateSubCategories(subCategories);
								}
								
							}
						});
					}
	
				}
			});
	    }
	}
	
	private void populateSubCategories(List<CategoryTypeObject> subCategories) {
		if (subCategories != null) {
	    	
			Resources resources = getResources();	    		    		 			
			
	    	// creates the properties for constructing the buttons and their rows.
	    	//final int rawPadding = resources.getDimensionPixelSize(R.dimen.search_popular_keyword_raw_padding);
	    	final int keywordMargin = resources.getDimensionPixelSize(R.dimen.search_popular_keyword_margin);
	    	final int keywordHeight = resources.getDimensionPixelSize(R.dimen.search_popular_keyword_height);
	    	final float keywordTextSize = (float) resources.getDimension(R.dimen.search_popular_keyword_text_size);
	    	
	    	// creates properties for layouting the buttons within the rows.
	    	
//	    	final int rowWidth = mFirstSubtRowLayout.getWidth();

	    	int currentWidth = 0;
		    int keywordButtonWidth = 0;
		    
		    
//		    Context context = getActivity(); 
		    Button keywordButton = null;
	    	
			for (CategoryTypeObject subCategory : subCategories) {
				// constructs the button.
				keywordButton = new Button(this);
				keywordButton.setText(subCategory.getName());
				keywordButton.setId((int)subCategory.getId());
				if (!Utils.isListEmpty(mCategoriesRetrieve)) {
					for (MyCategory categoryRetrieved : mCategoriesRetrieve ) {
						if (categoryRetrieved.getId() == (subCategory.getId())) {
							keywordButton.setSelected(true);
							buttonIdsToSave.add((int) categoryRetrieved.getId());
							break;
						} else {
							keywordButton.setSelected(false);
						}
					}
				} else {
					keywordButton.setSelected(false);
				}				
				keywordButton.setTextSize(keywordTextSize);
				keywordButton.setTextColor(resources.getColorStateList(R.color.main_search_popular_keyword_text));
				keywordButton.setBackgroundResource(R.drawable.background_my_preferences_buttons_sub_categories);
				keywordButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {						
						if (v.isSelected()) {
							v.setSelected(false);
							for (Integer id : buttonIdsToSave) {
								if (id == v.getId()) {
									buttonIdsToSave.remove(id);
									break;
								}
							}
							
						} else {
							v.setSelected(true);
							buttonIdsToSave.add(v.getId());
						}
						
					}
				});
				keywordButton.setSingleLine(true);
				
				// set margins for the button
				LayoutParams paramsButton = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//				paramsButton.setMargins(0, 0, keywordMargin, 0);
				keywordButton.setLayoutParams(paramsButton);							
				
				// measures it to the sum width of the row.
				keywordButton.measure(0, 0);
				keywordButtonWidth = keywordButton.getMeasuredWidth()/* + keywordMargin*/;
				
				if (rowWidth - currentWidth >= keywordButtonWidth) {
					mFirstSubRowLayout.addView(keywordButton);
					View view = new View(this);
					LayoutParams paramsView = new LayoutParams(keywordMargin, keywordHeight);
					view.setLayoutParams(paramsView);
					mFirstSubRowLayout.addView(view);
					currentWidth += keywordButtonWidth + keywordMargin;
				} else {
					LinearLayout llNewRow = new LinearLayout(this);	
					llNewRow.setBackgroundColor(getResources().getColor(R.color.background_sub_categories));
					llNewRow.setPadding(0, 0, 0, rawPadding);
					llNewRow.setLayoutParams(mSubRowParams);					
					subCategoriesLayout.addView(llNewRow);
					llNewRow.addView(keywordButton);
					View view = new View(this);
					LayoutParams paramsView = new LayoutParams(keywordMargin, keywordHeight);
					view.setLayoutParams(paramsView);
					llNewRow.addView(view);
					currentWidth = keywordButtonWidth + keywordMargin;
					mFirstSubRowLayout = llNewRow;
				}
				
			}
			
			// Start populate the bottom part
			
	    }
	}
	
	private void savePreferences() {
		StringBuilder buttonIdsToSaveString = new StringBuilder();		
//		if (!buttonIdsToSave.isEmpty()) {
			int listLength = buttonIdsToSave.size();
			if (listLength == 1) {
				buttonIdsToSaveString.append(buttonIdsToSave.get(0));
			} else {
				for (int i=0; i < listLength; i++) {
					if (i == listLength-1) {
						buttonIdsToSaveString.append(buttonIdsToSave.get(i));
					} else {
						buttonIdsToSaveString.append(buttonIdsToSave.get(i) + ",");
					}
				}
			}
			mDataManager.saveMyPreferences(buttonIdsToSaveString.toString(), this);
//		} else {
//			Toast.makeText(this, getResources().getString(R.string.my_preferences_error_no_categories), Toast.LENGTH_LONG).show();
//		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	  // TODO Auto-generated method stub
	  super.onWindowFocusChanged(hasFocus);
	  //Here you can get the size!
	 }

	public boolean isUserPreferencesChange () {
		if (Utils.isListEmpty(mCategoriesRetrieve)) {
			mCategoriesRetrieve = new ArrayList<MyCategory>();
		}
		if (buttonIdsToSave.size() != mCategoriesRetrieve.size()) { // if the 2 lists are not the same size = CHANGED
//			Toast.makeText(this, "CHANGED", Toast.LENGTH_LONG).show();
			isPreferencesChanged = true;
			return isPreferencesChanged;
		} else if (buttonIdsToSave.size() == 0 && mCategoriesRetrieve.size() == 0) {
//			Toast.makeText(this, "NOT-CHANGED", Toast.LENGTH_LONG).show();
			isPreferencesChanged = false; // if the 2 lists sizes equal 0 , means nothing changed = NOT CHANGED
			return isPreferencesChanged;
		} else { // if the 2 lists are the same size and not 0 - need to check
			for (int buttonIdToSave : buttonIdsToSave){
				for (MyCategory userRetrivedPreference : mCategoriesRetrieve) {
					if (buttonIdToSave != (int)userRetrivedPreference.getId()) {
						isPreferencesChanged = true;
//						Toast.makeText(this, "CHANGED", Toast.LENGTH_LONG).show();
						return isPreferencesChanged;
					}
				}
			}
		
			isPreferencesChanged = false;
//			Toast.makeText(this, "NOT-CHANGED", Toast.LENGTH_LONG).show();
			return isPreferencesChanged;
		}
	}
	
}
