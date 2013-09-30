package com.hungama.myplay.activity.ui.fragments;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.SearchAutoSuggestOperation;
import com.hungama.myplay.activity.operations.hungama.SearchPopularKeywordOperation;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.MediaDetailsActivity;
import com.hungama.myplay.activity.ui.RadioActivity;
import com.hungama.myplay.activity.ui.VideoActivity;
import com.hungama.myplay.activity.ui.fragments.MainSearchResultsFragment.OnSearchResultsOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.SearchBar;
import com.hungama.myplay.activity.ui.widgets.SearchBar.OnSearchBarStateChangedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * Presents the Quick Navigation panel within the application.
 */
public class MainSearchFragment extends MainFragment implements CommunicationOperationListener, 
															OnSearchBarStateChangedListener, 
															OnSearchResultsOptionSelectedListener {
	
	private static final String TAG = "MainSearchFragment";
	
	private static final int MAXIMUM_SUGGESTIONS_TO_PRESENT = 5;
	
	public static final String ARGUMENT_SEARCH_VIDEO = "argument_search_video";
	protected static final String FRAGMENT_TAG_VIDEO_SEARCH = "fragment_tag_video_search";
	public static final String FRAGMENT_ARGUMENT_SEARCH_VIDEO = "fragment_argument_media_track_details";
	
	private static final String SEARCH_FILTER_TYPE_ALL = ""; // should be empty
	private static final String SEARCH_FILTER_TYPE_SONGS = "Song";
	private static final String SEARCH_FILTER_TYPE_ALBUMS = "Album";
	private static final String SEARCH_FILTER_TYPE_PLAYLISTS = "Playlist";
	private static final String SEARCH_FILTER_TYPE_VIDEOS = "Videos";
	
	private DataManager mDataManager;
	private PlayerBarFragment mPlayerBarFragment;
	
	private boolean mHasLoaded = false;
	
	private InputMethodManager mInputMethodManager;

	private LinearLayout searchLayout;
	private LinearLayout mFirstRowLayout;
	private LinearLayout.LayoutParams mRowParams;
	
	private TextView tvSearchCategory;
	private SearchBar mSearchBar;

	// panels.
	public LinearLayout mCategoriesPanel; 
	public ListView mSearchSuggestionsList;
	
	// panels animations.
	
	private static final int ANIMATION_DUARATION_MILLIES = 250;
	
	private AlphaAnimation mFadeInAnimation;
	private AlphaAnimation mFadeOutAnimation;
	
	private ApplicationConfigurations applicationConfigurations;
	private View rootView;
	
	private boolean isFirstVisitToPage;
	
	private LinearLayout filterHint;
	
	private int rawPadding;
	
	// ======================================================
	// Activity life-cycle callbacks. 
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// initialize components.
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		applicationConfigurations = mDataManager.getApplicationConfigurations();
		mPlayerBarFragment = ((MainActivity) getActivity()).getPlayerBar();
		
		mInputMethodManager = 
				(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		
		// initializes the animations.
		mFadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
		mFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
		mFadeInAnimation.setDuration(ANIMATION_DUARATION_MILLIES);
		mFadeOutAnimation.setDuration(ANIMATION_DUARATION_MILLIES);
		
		rawPadding = getResources().getDimensionPixelSize(R.dimen.search_popular_keyword_raw_padding);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_main_search, container, false);	
		initializeUserControls(rootView);
		
		return rootView;
	}
	
	private void initializeUserControls(final View rootView) {
		
		rootView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
		
		searchLayout = (LinearLayout) rootView.findViewById(R.id.linearlayout_search);
		mSearchSuggestionsList = (ListView) rootView.findViewById(R.id.search_auto_suggest_keywords);
		mSearchSuggestionsList.setHeaderDividersEnabled(false);
		
		tvSearchCategory = (TextView) rootView.findViewById(R.id.search_popular_searches_category);
		tvSearchCategory.setText(R.string.search_popular_searches_search_category_all);			
		
		mSearchBar = (SearchBar) rootView.findViewById(R.id.search_searchbar);
		mSearchBar.setOnSearchBarStateChangedListener(this);
		 
		mSearchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					closeSearchSuggestionsPanel();
				}				
				
			}
		});
		
		//set onClickListener
		mCategoriesPanel = (LinearLayout) rootView.findViewById(R.id.search_categories);
		LinearLayout leftImagesButton = (LinearLayout) rootView.findViewById(R.id.search_searchbar_filter);
		leftImagesButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mSearchSuggestionsList.getVisibility() == View.VISIBLE) {
					closeSearchSuggestionsPanel();
				}
				toggleCategoriesPanel();											
			}
		});
		
		leftImagesButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					closeCategoriesPanel();
				}
				
			}
		});
		
		//Set mCategoriesPanel clicks
		LinearLayout llall = (LinearLayout) rootView.findViewById(R.id.linearlayout_categories_all);
		LinearLayout llsongs = (LinearLayout) rootView.findViewById(R.id.linearlayout_categories_songs);
		LinearLayout llalbum = (LinearLayout) rootView.findViewById(R.id.linearlayout_categories_album);
		LinearLayout llplaylist = (LinearLayout) rootView.findViewById(R.id.linearlayout_categories_playlist);
		LinearLayout llvideo = (LinearLayout) rootView.findViewById(R.id.linearlayout_categories_video);
		final ImageView ivSearchImageCategory = (ImageView) rootView.findViewById(R.id.search_searchbar_filter_category_icon);
		
		final ImageView greenTickAll = (ImageView) rootView.findViewById(R.id.imagebutton_categories_all_v_image);
		final ImageView greenTickSongs = (ImageView) rootView.findViewById(R.id.imagebutton_categories_songs_v_image);
		final ImageView greenTickAlbum = (ImageView) rootView.findViewById(R.id.imagebutton_categories_album_v_image);
		final ImageView greenTickPlaylist = (ImageView) rootView.findViewById(R.id.imagebutton_categories_playlist_v_image);		
		final ImageView greenTickVideo = (ImageView) rootView.findViewById(R.id.imagebutton_categories_video_v_image);
		
		llall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {								
				tvSearchCategory.setText(R.string.search_popular_searches_search_category_all);
				
				ImageView tvSearchCategoryOption = (ImageView) rootView.findViewById(R.id.imagebutton_categories_all);	
				ivSearchImageCategory.setImageDrawable(tvSearchCategoryOption.getDrawable());
				
				greenTickAll.setVisibility(View.VISIBLE);
				greenTickSongs.setVisibility(View.GONE);
				greenTickAlbum.setVisibility(View.GONE);
				greenTickPlaylist.setVisibility(View.GONE);
				greenTickVideo.setVisibility(View.GONE);
				
				toggleCategoriesPanel();
				
				String query = mSearchBar.getSearchQueryText();
				if (!TextUtils.isEmpty(query)) {
					openSearchResults(query, SEARCH_FILTER_TYPE_ALL);
				}
				
				FlurryAgent.logEvent("Filter tap - All");
			}
		});
		
		llsongs.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView tvSearchCategory = (TextView) rootView.findViewById(R.id.search_popular_searches_category);				
				tvSearchCategory.setText(R.string.search_popular_searches_search_category_songs);
				ImageView tvSearchCategoryOption = (ImageView) rootView.findViewById(R.id.imagebutton_categories_songs);	
				ivSearchImageCategory.setImageDrawable(tvSearchCategoryOption.getDrawable());
				
				greenTickAll.setVisibility(View.GONE);
				greenTickSongs.setVisibility(View.VISIBLE);
				greenTickAlbum.setVisibility(View.GONE);
				greenTickPlaylist.setVisibility(View.GONE);
				greenTickVideo.setVisibility(View.GONE);
				
				toggleCategoriesPanel();
				
				String query = mSearchBar.getSearchQueryText();
				if (!TextUtils.isEmpty(query)) {
					openSearchResults(query, SEARCH_FILTER_TYPE_SONGS);
				}
				
				FlurryAgent.logEvent("Filter tap - Songs");
			}
		});
		
		llalbum.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView tvSearchCategory = (TextView) rootView.findViewById(R.id.search_popular_searches_category);				
				tvSearchCategory.setText(R.string.search_popular_searches_search_category_album);
				
				ImageView tvSearchCategoryOption = (ImageView) rootView.findViewById(R.id.imagebutton_categories_album);	
				ivSearchImageCategory.setImageDrawable(tvSearchCategoryOption.getDrawable());
				
				greenTickAll.setVisibility(View.GONE);
				greenTickSongs.setVisibility(View.GONE);
				greenTickAlbum.setVisibility(View.VISIBLE);
				greenTickPlaylist.setVisibility(View.GONE);
				greenTickVideo.setVisibility(View.GONE);
				
				toggleCategoriesPanel();
				
				String query = mSearchBar.getSearchQueryText();
				if (!TextUtils.isEmpty(query)) {
					openSearchResults(query, SEARCH_FILTER_TYPE_ALBUMS);
				}
				
				FlurryAgent.logEvent("Filter tap - Albums");
			}
		});
		
		llplaylist.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView tvSearchCategory = (TextView) rootView.findViewById(R.id.search_popular_searches_category);				
				tvSearchCategory.setText(R.string.search_popular_searches_search_category_playlist);
				
				ImageView tvSearchCategoryOption = (ImageView) rootView.findViewById(R.id.imagebutton_categories_playlist);	
				ivSearchImageCategory.setImageDrawable(tvSearchCategoryOption.getDrawable());
				
				greenTickAll.setVisibility(View.GONE);
				greenTickSongs.setVisibility(View.GONE);
				greenTickAlbum.setVisibility(View.GONE);
				greenTickPlaylist.setVisibility(View.VISIBLE);
				greenTickVideo.setVisibility(View.GONE);
				
				toggleCategoriesPanel();
				
				String query = mSearchBar.getSearchQueryText();
				if (!TextUtils.isEmpty(query)) {
					openSearchResults(query, SEARCH_FILTER_TYPE_PLAYLISTS);
				}
				
				FlurryAgent.logEvent("Filter tap - Playlists");
			}
		});
		
		
		llvideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView tvSearchCategory = (TextView) rootView.findViewById(R.id.search_popular_searches_category);				
				tvSearchCategory.setText(R.string.search_popular_searches_search_category_video);
				ImageView tvSearchCategoryOption = (ImageView) rootView.findViewById(R.id.imagebutton_categories_video);	
				ivSearchImageCategory.setImageDrawable(tvSearchCategoryOption.getDrawable());
				
				greenTickAll.setVisibility(View.GONE);
				greenTickSongs.setVisibility(View.GONE);
				greenTickAlbum.setVisibility(View.GONE);
				greenTickPlaylist.setVisibility(View.GONE);
				greenTickVideo.setVisibility(View.VISIBLE);
				
				toggleCategoriesPanel();
				
				String query = mSearchBar.getSearchQueryText();
				if (!TextUtils.isEmpty(query)) {
					openSearchResults(query, SEARCH_FILTER_TYPE_VIDEOS);
				}
				
				FlurryAgent.logEvent("Filter tap - Videos");
			}
		});
		greenTickVideo.setVisibility(View.GONE);
		
		// Create the first row (LinearLayout)
		mRowParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	
    	mFirstRowLayout = new LinearLayout(getActivity());
    	mFirstRowLayout.setPadding(0, 0, 0, rawPadding);
    	mFirstRowLayout.setLayoutParams(mRowParams);
		searchLayout.addView(mFirstRowLayout);
		mFirstRowLayout.measure(0, 0);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (!mHasLoaded) {
			mDataManager.getSearchPopularSerches(this);
			
		} else {			
			Logger.e(TAG, "START POPULATE HERE");
		}
		
		filterHint = (LinearLayout) rootView.findViewById(R.id.search_hint);
		isFirstVisitToPage = applicationConfigurations.isFirstVisitToSearchPage();
		if (isFirstVisitToPage) {
			isFirstVisitToPage = false;
			applicationConfigurations.setIsFirstVisitToSearchPage(false);
			showSearchFilterHint();
		} else if (applicationConfigurations.getHintsState()) {			
			if (!applicationConfigurations.isSearchFilterShownInThisSession()) {
				applicationConfigurations.setIsSearchFilterShownInThisSession(true);
				showSearchFilterHint();
			} else {
				filterHint.setVisibility(View.GONE);
			}
		} else {
			filterHint.setVisibility(View.GONE);
		}
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); 
		FlurryAgent.onPageView();
		FlurryAgent.logEvent("Search");
	}
	
	private void showSearchFilterHint() {
		Animation animationIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_right_enter);
		final Animation animationOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_left_exit_without_dec_interpolator);
		
		filterHint.setVisibility(View.VISIBLE);
		filterHint.startAnimation(animationIn);
		
		final CountDownTimer countDownTimer = new CountDownTimer(7000, 1000) {

		     public void onTick(long millisUntilFinished) {
		    	 
		     }

		     public void onFinish() {
		    	 cancel();
		    	 filterHint.startAnimation(animationOut);
		    	 filterHint.setVisibility(View.GONE);
		     }
		  }.start();
		
		  filterHint.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					filterHint.startAnimation(animationOut);
					countDownTimer.cancel();
					filterHint.setVisibility(View.GONE);
					
				}
			});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mSearchBar.setOnSearchBarStateChangedListener(this);
		mSearchBar.requestFocus();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mSearchBar.stopAnySearchListenning();
		mSearchBar.setOnSearchBarStateChangedListener(null);
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mDataManager.cancelGetSearchAutoSuggest();
		
		mInputMethodManager.hideSoftInputFromWindow(mSearchBar.getWindowToken(), 0);
		
		FlurryAgent.onEndSession(getActivity());
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
	}
	
	private final android.view.View.OnClickListener mPopularKeywordButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			// gets the search query from the button.
			String query = ((Button) view).getText().toString();
			String type;
			if (tvSearchCategory.getText().toString().equalsIgnoreCase("All:")) {
				type = "";
			} else {
				type = tvSearchCategory.getText().toString().replace("s:", "");
			}	
//			String type = tvSearchCategory.getText().toString().replace(":", "");
			
			mSearchBar.setFakeSearchQueryText(query);
			openSearchResults(query, type);
		}
	};
	
	
	// ======================================================
	// Operation Methods 
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS) {
			showLoadingDialog(R.string.application_dialog_loading_content);
			mHasLoaded = true;
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			showLoadingDialog(R.string.application_dialog_loading_content);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS) {
			
			// gets the given media item to check it's type to retrieve the correct details implementation
			List<String> list = (List<String>) responseObjects.get(SearchPopularKeywordOperation.RESULT_KEY_LIST_KEYWORDS);
			populateKeywords(list);			
			
			Bundle data = getArguments();
			if (data != null && data.containsKey(VideoActivity.ARGUMENT_SEARCH_VIDEO)) {
				openSearchVideo(data.getString(VideoActivity.ARGUMENT_SEARCH_VIDEO));
			}
			
			hideLoadingDialog();
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.SEARCH_AUTO_SUGGEST) {
			
			List<String> suggestions = (List<String>) responseObjects.get(SearchAutoSuggestOperation.RESULT_KEY_LIST_SUGGESTED_KEYWORDS);
			if (suggestions.size() > MAXIMUM_SUGGESTIONS_TO_PRESENT) {
				suggestions = suggestions.subList(0, MAXIMUM_SUGGESTIONS_TO_PRESENT);
			}			
			populateAutoSuggestedKeywords(suggestions);
			
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			
			MediaItem mediaItem = (MediaItem) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);
			if (mediaItem.getMediaType() == MediaType.ALBUM || mediaItem.getMediaType() == MediaType.PLAYLIST) {
				
				MediaSetDetails setDetails = (MediaSetDetails) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
				PlayerOption playerOptions = (PlayerOption) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_PLAYER_OPTION);
				
				List<Track> tracks = setDetails.getTracks();
				if (playerOptions == PlayerOption.OPTION_PLAY_NOW) {
					mPlayerBarFragment.playNow(tracks);
				} else if (playerOptions == PlayerOption.OPTION_PLAY_NEXT) {
					mPlayerBarFragment.playNext(tracks);
				} else if (playerOptions == PlayerOption.OPTION_ADD_TO_QUEUE) {
					mPlayerBarFragment.addToQueue(tracks);
				}
			}
			
			hideLoadingDialog();
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS) {
			Logger.i(TAG, "Failed loading media details");		
			hideLoadingDialog();
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.SEARCH_AUTO_SUGGEST) {
			Logger.i(TAG, "Failed loading auto suggest keywords");
			closeSearchSuggestionsPanel();
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			hideLoadingDialog();
		}
	}	
	
	
	// ======================================================
	// Helper Methods 
	// ======================================================

	private void openSearchVideo(String videoQuery) {
		mSearchBar.setFakeSearchQueryText(videoQuery);
		onStartSearchKeyboard(videoQuery);
	}
	
	
	// ======================================================
	// Populate Methods 
	// ======================================================
	
	public void populateAutoSuggestedKeywords(List<String> mList) {
		
		closeCategoriesPanel();		
		
		// Create ListAdapter using the mList list.
		AutoSuggestKeywordsAdapter listAdapter = new AutoSuggestKeywordsAdapter(mList);  
	    mSearchSuggestionsList.setAdapter(listAdapter);
	    openSearchSuggestionsPanel();
	}
	
	public void populateKeywords(List<String> keywords) {
	    if (keywords != null) {
	    	if (getActivity() != null) {
		    	Resources resources = getResources();
		    	
		    	// creates the properties for constructing the buttons and their rows.
//		    	final int rawPadding = resources.getDimensionPixelSize(R.dimen.search_popular_keyword_raw_padding);
		    	final int keywordMargin = resources.getDimensionPixelSize(R.dimen.search_popular_keyword_margin);
		    	final int keywordHeight = resources.getDimensionPixelSize(R.dimen.search_popular_keyword_height);
		    	final float keywordTextSize = (float) resources.getDimension(R.dimen.search_popular_keyword_text_size);
		    			
		    	// creates properties for layouting the buttons within the rows.
		    	final int rowWidth = mFirstRowLayout.getWidth();
		    	int currentWidth = 0;
			    int keywordButtonWidth = 0;
			    
			    Context context = getActivity(); 
			    Button keywordButton = null;
		    	
				for (String keyword : keywords) {
					
					// constructs the button.
					keywordButton = new Button(context);
					keywordButton.setText(keyword);
					keywordButton.setTextSize(keywordTextSize);
					keywordButton.setTextColor(resources.getColorStateList(R.color.main_search_popular_keyword_text));
					keywordButton.setBackgroundResource(R.drawable.background_search_keywords);
					keywordButton.setSingleLine(true);
					keywordButton.setOnClickListener(mPopularKeywordButtonListener);
					
					// set margins for the button
					LayoutParams paramsButton = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					paramsButton.setMargins(0, 0, keywordMargin, 0);
					keywordButton.setLayoutParams(paramsButton);
					
					// measures it to the sum width of the row.
					keywordButton.measure(0, 0);
					keywordButtonWidth = keywordButton.getMeasuredWidth() + keywordMargin;
					
					if (rowWidth - currentWidth >= keywordButtonWidth) {
						mFirstRowLayout.addView(keywordButton);
						currentWidth += keywordButtonWidth;
					} else {
						LinearLayout llNewRow = new LinearLayout(getActivity());	
						llNewRow.setPadding(0, 0, 0, rawPadding);
						llNewRow.setLayoutParams(mRowParams);					
						searchLayout.addView(llNewRow);
						llNewRow.addView(keywordButton);
						currentWidth = keywordButtonWidth;
						mFirstRowLayout = llNewRow;
					}
				}
	    	}
	    }
	    
	}
	
	
	// ======================================================
	// Panels methods. 
	// ======================================================
	
	public void toggleCategoriesPanel() {
		if (mCategoriesPanel.getVisibility() == View.GONE) {
			openCategoriesPanel();
			
		} else {
			closeCategoriesPanel();
		}
	}
	
	public void toggleSuggestionsPanel() {
		if (mCategoriesPanel.getVisibility() == View.GONE) {
			openSearchSuggestionsPanel();
			
		} else {
			closeSearchSuggestionsPanel();
		}
	}
	
	public void openCategoriesPanel() {
		if (mCategoriesPanel.getVisibility() != View.VISIBLE) {
			mFadeInAnimation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					mCategoriesPanel.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					
				}
			});
			mCategoriesPanel.startAnimation(mFadeInAnimation);
		}
	}
	
	public void closeCategoriesPanel() {
		if (mCategoriesPanel.getVisibility() != View.GONE) {
			mFadeOutAnimation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					mCategoriesPanel.setVisibility(View.GONE);
				}
			});
			mCategoriesPanel.startAnimation(mFadeOutAnimation);
		}
	}
	
	public void openSearchSuggestionsPanel() {
		mFadeInAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				mSearchSuggestionsList.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				
			}
		});
		mSearchSuggestionsList.startAnimation(mFadeInAnimation);
	}
	
	public void closeSearchSuggestionsPanel() {
		mFadeOutAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mSearchSuggestionsList.setVisibility(View.GONE);
			}
		});
		mSearchSuggestionsList.startAnimation(mFadeOutAnimation);
	}
	
	public void openSearchResults(String query, String type) {
		
		FragmentManager fragmentManager = getChildFragmentManager();
		MainSearchResultsFragment fragment = 
				(MainSearchResultsFragment) fragmentManager.findFragmentByTag(MainSearchResultsFragment.TAG);
		
		if (fragment != null) {
			// the fragment has been already initialized and shown on the screen.
			fragment.searchForQueury(query, type);
			
		} else {
			
			// the are no results presented on the screen, creates them.
			Bundle arguments = new Bundle();
			arguments.putString(MainSearchResultsFragment.FRAGMENT_ARGUMENT_QUERY, query);
			arguments.putString(MainSearchResultsFragment.FRAGMENT_ARGUMENT_TYPE, type);
			
			fragment = new MainSearchResultsFragment();
			fragment.setArguments(arguments);
			fragment.setOnSearchResultsOptionSelectedListener(this);
			
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit);
			fragmentTransaction.add(R.id.main_search_results_container, fragment, MainSearchResultsFragment.TAG);
			fragmentTransaction.commit();
		}
	}
	
	public void closeSearchResults() {
		
		// gets the fragment and removes it.
		FragmentManager fragmentManager = getChildFragmentManager();
		MainSearchResultsFragment fragment = 
				(MainSearchResultsFragment) fragmentManager.findFragmentByTag(MainSearchResultsFragment.TAG);
		
		if (fragment != null && fragment.isAdded() && fragment.isVisible()) {
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_and_show_bottom_enter, R.anim.slide_and_show_bottom_exit);
			fragmentTransaction.remove(fragment);
			fragmentTransaction.commit();
		}
	}

	// ======================================================
	// OnSearchBarStateChangedListener Methods 
	// ======================================================
	
	@Override
	public void onStartTypingSearchQuery() {}

	@Override
	public void onStartSearch(String query) {
		// cancels any running progress.
		mDataManager.cancelGetSearchAutoSuggest();
		// Querying like a bous!
		mDataManager.getSearchAutoSuggest(query, String.valueOf(query.length()) , this);	
	}
	
	@Override
	public void onStartSearchKeyboard(String query) {
		
		// cancels any existing work.
		mDataManager.cancelGetSearchAutoSuggest();
		
		closeSearchSuggestionsPanel();
		
		mInputMethodManager.hideSoftInputFromWindow(mSearchBar.getWindowToken(), 0);
		
		String type;
		if (tvSearchCategory.getText().toString().equalsIgnoreCase("All:")) {
			type = "";
		} else {
			type = tvSearchCategory.getText().toString().replace("s:", "");
		}		
		openSearchResults(query, type);
	}

	@Override
	public void onCancelSearch() {
		// cancels any running search query.
		mDataManager.cancelGetSearchAutoSuggest();
		
		// hides all panels.
		closeCategoriesPanel();
		closeSearchResults();
		closeSearchSuggestionsPanel();
	}
	
	// ======================================================
	// Adapters 
	// ======================================================
	
	private static class ViewHolder {
		TextView suggestedKeyword;
	}
	
	private class AutoSuggestKeywordsAdapter extends BaseAdapter {
		
		private List<String> suggestedKeywords;
		private LayoutInflater mInflater;
		
		public AutoSuggestKeywordsAdapter(List<String> list) {
			suggestedKeywords = list;
			mInflater = (LayoutInflater) getActivity().
					getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}			
		
		@Override
		public int getCount() {
			return (Utils.isListEmpty(suggestedKeywords) ? 0 : suggestedKeywords.size());
		}

		@Override
		public Object getItem(int position) {
			return suggestedKeywords.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
		
			ViewHolder viewHolder;
			// create view if not exist.
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_search_auto_suggest_line, parent, false);
				
				viewHolder = new ViewHolder();								
				viewHolder.suggestedKeyword = (TextView) convertView.findViewById(R.id.search_auto_suggest_name);
				convertView.setTag(R.id.view_tag_view_holder, viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag(R.id.view_tag_view_holder);
			}
			
			// populate the view from the keywords's list.
			String keyword = suggestedKeywords.get(position);
			// stores the object in the view.
			convertView.setTag(R.id.view_tag_object, keyword);
			
			viewHolder.suggestedKeyword.setText(keyword);
			viewHolder.suggestedKeyword.setOnClickListener(new OnClickListener() {						
				@Override
				public void onClick(View view) {
					
					final String keyword = (String) view.getTag(R.id.view_tag_object);
					// Set the keyword in the search bar
					mSearchBar.setFakeSearchQueryText(keyword);
					onStartSearchKeyboard(keyword);
					
					FlurryAgent.logEvent("Auto-complete tap - " + keyword);
				}
			});			
			
			return convertView;
		}
		
	}
	
	@Override
	public void onPlayNowSelected(MediaItem mediaItem) {
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
										mediaItem.getAlbumName(), mediaItem.getArtistName(), 
										mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				
				mPlayerBarFragment.playNow(tracks);
				
			} else {
				mDataManager.getMediaDetails(mediaItem, PlayerOption.OPTION_PLAY_NOW , MainSearchFragment.this);
			}
		}
	}

	@Override
	public void onAddToQueueSelected(MediaItem mediaItem) {
		if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), 
										mediaItem.getAlbumName(), mediaItem.getArtistName(), 
										mediaItem.getImageUrl(), mediaItem.getBigImageUrl());
				List<Track> tracks = new ArrayList<Track>();
				tracks.add(track);
				
				mPlayerBarFragment.addToQueue(tracks);
				
			} else {
				mDataManager.getMediaDetails(mediaItem, PlayerOption.OPTION_ADD_TO_QUEUE , MainSearchFragment.this);
			}
			
		} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {
			Intent intent = new Intent(getActivity(), RadioActivity.class);
			intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_MEDIA_ITEM, (Serializable) mediaItem);
			intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_CATEGORY_TYPE, (Serializable) MediaCategoryType.TOP_ARTISTS_RADIO);
			startActivity(intent);	
		}
	}

	@Override
	public void onShowDetails(MediaItem mediaItem) {
		Intent intent;
		if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			intent = new Intent(getActivity(), VideoActivity.class);
			intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO, (Serializable) mediaItem);
			
		} else if (mediaItem.getMediaContentType() == MediaContentType.RADIO) {
			intent = new Intent(getActivity(), RadioActivity.class);
			intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_MEDIA_ITEM, (Serializable) mediaItem);
			intent.putExtra(RadioActivity.EXTRA_SHOW_DETAILS_CATEGORY_TYPE, (Serializable) MediaCategoryType.TOP_ARTISTS_RADIO);
			
		} else {
			intent = new Intent(getActivity(), MediaDetailsActivity.class);
			intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM, (Serializable) mediaItem);
		}
		
		startActivity(intent);		
	}
	
	
}
