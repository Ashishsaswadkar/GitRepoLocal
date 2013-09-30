package com.hungama.myplay.activity.ui.widgets;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Compound widget that behaves as a regular search bar.
 */
public class SearchBar extends RelativeLayout implements OnClickListener, OnEditorActionListener {
	
	private static final String TAG = "SearchBar";
	
	/**
	 * Default quantity of characters to start querying.
	 */
	public static final int SEARCH_BAR_MINMUM_CHARACTERS_TO_ACTION = 1;
	public static final int SEARCH_BAR_MINMUM_CHARACTERS_TO_ACTION_AUTO_COMPLETE = 3;
	
	/*
	 * Default time in milliseconds for pending any action when the text is changes.
	 */
	private static final int SEARCH_BAR_PENDING_TIME_TO_ACTION_MILLISECONDS = 500;
	
	/*
	 * Default height of the action bar.
	 */
	private static final int SEARCH_BAR_HEIGHT_DP = 47;
	
	private static final int VIEW_ID_SEARCH_QUERY_EDITTEXT = 100001;
	private static final int VIEW_ID_CANCEL_BUTTON = 100003;
	private InputMethodManager mInputMethodManager;
	
	private EditText mTextSearchBar;
	private SearchQueryTextWatcher mSearchQueryTextWatcher;
	
	private ImageButton mButtonCancelSearch;
	
	private int mMinCharactersToAction = SEARCH_BAR_MINMUM_CHARACTERS_TO_ACTION;
	private int mMinCharactersToActionAutoComplete = SEARCH_BAR_MINMUM_CHARACTERS_TO_ACTION_AUTO_COMPLETE;
	
	private OnSearchBarStateChangedListener mOnSearchBarStateChangedListener;
	
	public SearchBar(Context context) {
		super(context);
		
		initialize();
	}
	
	public SearchBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize();
	}

	public SearchBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initialize();
	}
	
	/**
	 * Constructs the widgets and initialize its actions.
	 */
	private void initialize() {
		
		Resources resources = getResources();
		
		// initialize the control on the keyboard.
		mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		
		// the search bar.
		int searchBarHeight = resources.getDimensionPixelSize(R.dimen.search_searchbar_container_height);
		RelativeLayout.LayoutParams searchBarLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, searchBarHeight);
		int sidesPadding = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, resources.getDisplayMetrics()));
		this.setPadding(sidesPadding, 0, sidesPadding, 0);
		this.setLayoutParams(searchBarLayoutParams);
		this.setGravity(Gravity.CENTER_VERTICAL);
		
		// Text of the search bar.
		float textSize = (float) resources.getDimension(R.dimen.search_searchbar_text_size);;
		int paddingSides = Utils.convertDPtoPX(getContext(), 5);
		
		RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, Utils.convertDPtoPX(getContext(), 40));// Utils.convertDPtoPX(getContext(), 200)
		textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
		
		mTextSearchBar = new EditText(getContext());
		mTextSearchBar.setId(VIEW_ID_SEARCH_QUERY_EDITTEXT);
		mTextSearchBar.setPadding(0, 0, paddingSides, 0);
		mTextSearchBar.setBackgroundResource(0);
		mTextSearchBar.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		mTextSearchBar.setSingleLine(true);
		mTextSearchBar.setGravity(Gravity.CENTER_VERTICAL);
		mTextSearchBar.setTextColor(resources.getColor(R.color.black));		
		mTextSearchBar.setHintTextColor(resources.getColor(R.color.black));
		mTextSearchBar.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT);
		mTextSearchBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		mTextSearchBar.setOnClickListener(this);
		mTextSearchBar.setTextAppearance(getContext(), R.style.cursorStyle);
		
		mSearchQueryTextWatcher = new SearchQueryTextWatcher();
		mTextSearchBar.addTextChangedListener(mSearchQueryTextWatcher);
		mTextSearchBar.setOnEditorActionListener(this);
		
		
		// cancel button.
		int size = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, resources.getDisplayMetrics()));
		int cancelButtonPadding = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, resources.getDisplayMetrics()));
		RelativeLayout.LayoutParams cancelButtonParams = new RelativeLayout.LayoutParams(size, size);
		cancelButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		cancelButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
		
		mButtonCancelSearch = new ImageButton(getContext());
		mButtonCancelSearch.setId(VIEW_ID_CANCEL_BUTTON);
		mButtonCancelSearch.setBackgroundResource(R.drawable.icon_main_search_close);
		mButtonCancelSearch.setPadding(cancelButtonPadding, cancelButtonPadding, cancelButtonPadding, cancelButtonPadding);
		mButtonCancelSearch.setVisibility(INVISIBLE);
		mButtonCancelSearch.setOnClickListener(this);
		
		textParams.addRule(RelativeLayout.LEFT_OF, mButtonCancelSearch.getId());
		this.addView(mTextSearchBar, textParams);
		this.addView(mButtonCancelSearch, cancelButtonParams);
		
		invalidate();
	}
	
	private boolean mTextChangedByCancelButton = false;
	
	@Override
	public void onClick(View view) {
		
		if (view.getId() == VIEW_ID_CANCEL_BUTTON) {
			
			Logger.i(TAG, "cancel was clicked.");
			
			mTextChangedByCancelButton = true;
			
			setFakeSearchQueryText("");
			
			mButtonCancelSearch.setVisibility(View.INVISIBLE);
			
			mTextChangedByCancelButton = false;
			
			if (mOnSearchBarStateChangedListener != null) {
				mOnSearchBarStateChangedListener.onCancelSearch();
			}
			
		} else if (view.getId() == VIEW_ID_SEARCH_QUERY_EDITTEXT) {
			
			Editable queryText = mTextSearchBar.getText();
			if (queryText == null || TextUtils.isEmpty(queryText.toString())) {
				if (mOnSearchBarStateChangedListener != null) {
					mOnSearchBarStateChangedListener.onStartTypingSearchQuery();
				}
			}
		}
		
	}
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			
			Logger.i(TAG, "initiating Search by keyboard.");

			Editable queryText = mTextSearchBar.getText();
			if (queryText != null && queryText.length() >= mMinCharactersToAction) {
				
				cancelAction();
				mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
				
				if (mOnSearchBarStateChangedListener != null) {
					mOnSearchBarStateChangedListener.onStartSearchKeyboard(queryText.toString());
					return true;
				}
			}
        }
		
		return false;
	}
	
	
	// ======================================================
	// PUBLIC METHODS. 
	// ======================================================
	
	/**
	 * Sets default hint text when the search bar is empty.
	 */
	public void setHint(String hint) {
		mTextSearchBar.setHint(hint);
	}
	
	/**
	 * Sets the minimum number of characters of the query length to perform search action.
	 * 
	 * @param numberOfCharacters - to start performing the action. if it's smaller or equal to 0, the default will been set {@code SEARCH_BAR_MINMUM_CHARACTERS_TO_ACTION}.
	 */
	public void setMiniumumCharactersToSearch(int numberOfCharacters) {
		if (numberOfCharacters > 0) {
			mMinCharactersToAction = numberOfCharacters;
			mMinCharactersToActionAutoComplete = numberOfCharacters;
		} else {
			mMinCharactersToAction = SEARCH_BAR_MINMUM_CHARACTERS_TO_ACTION;
			mMinCharactersToActionAutoComplete = SEARCH_BAR_MINMUM_CHARACTERS_TO_ACTION_AUTO_COMPLETE;
		}
	}
	
	public void setOnSearchBarStateChangedListener(OnSearchBarStateChangedListener listener) {
		mOnSearchBarStateChangedListener = listener;
	}
	
	/**
	 *	Interface definition to be invoked when there are changes in the search bar searching text.
	 */
	public interface OnSearchBarStateChangedListener {
		
		/**
		 * Invoked when the search query is empty and the user clicked to get focus on it.
		 */
		public void onStartTypingSearchQuery();
		
		/**
		 * Invoked when the search bar query is ready to being search.
		 */
		public void onStartSearch(String query);
		
		/**
		 * Invoked when the search button on keyboard is pressed.
		 */
		public void onStartSearchKeyboard(String query);
		
		/**
		 * Invoked when the search bar query is cancelled.
		 */
		public void onCancelSearch();
	}
	
	/**
	 * Stops any pending searching timers.
	 */
	public void stopAnySearchListenning() {
		
		cancelAnyAction();
	}
	
	@Override
	public IBinder getWindowToken() {
		return mTextSearchBar.getWindowToken();
	}
	
	/**
	 * Sets the search bar text field without invoking any listener's methods.
	 * @param searchQuery
	 */
	public void setFakeSearchQueryText(String searchQuery) {
		Logger.d(TAG, "setFakeSearchQueryText: " + searchQuery);
		
		mSearchQueryTextWatcher.setEnabled(false);
		mTextSearchBar.removeTextChangedListener(mSearchQueryTextWatcher);
		mTextSearchBar.setOnEditorActionListener(null);
		
		mTextSearchBar.setText(searchQuery);
		
		mTextSearchBar.addTextChangedListener(mSearchQueryTextWatcher);
		mTextSearchBar.setOnEditorActionListener(this);
		mSearchQueryTextWatcher.setEnabled(true);
		
		mButtonCancelSearch.setVisibility(View.VISIBLE);
		mButtonCancelSearch.requestFocus();
	}
	
	/**
	 * Retrieves the text inside the search bar field.
	 */
	public String getSearchQueryText() {
		Editable text = mTextSearchBar.getText();
		if (text != null && text.length() > 0) {
			return text.toString();
		} else {
			return "";
		}
	}
	
	
	// ======================================================
	// KEY CHANGES COUNTER LISTENER. 
	// ======================================================
	
	/**
	 * Listener for changes in the query string inside the vehicles search bar. 
	 */
 	private class SearchQueryTextWatcher implements TextWatcher {
 		
 		private boolean isEnabled = true;
		// flag that avoids calling multiple time to cancel state.
		private boolean canBeCancelled = false;
		
		public void setEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
		}
		
		public boolean isEnabled() {
			return isEnabled;
		}
		
		@Override
		public void afterTextChanged(Editable editable) {
			Logger.i(TAG, "afterTextChanged " + editable.toString());
			if (isEnabled) {
				if (editable.length() > 0) {
					if (mButtonCancelSearch.getVisibility() != View.VISIBLE) {
						mButtonCancelSearch.setVisibility(View.VISIBLE);
						mButtonCancelSearch.requestFocus();
					}
				} else {
					if (mButtonCancelSearch.getVisibility() == View.VISIBLE) {
						mButtonCancelSearch.setVisibility(View.INVISIBLE);
					}
				}
				
				if (editable.length() >= mMinCharactersToActionAutoComplete) {
					performAction();
					
					canBeCancelled = true;
					
				} else {
					cancelAction();
					
					if (canBeCancelled && !mTextChangedByCancelButton) {
						canBeCancelled = false;
						mTextChangedByCancelButton = false;
						if (mOnSearchBarStateChangedListener != null) {
							mOnSearchBarStateChangedListener.onCancelSearch();
						}
					}
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence string, int start, int before, int count) {
			Logger.i(TAG, "onTextChanged: " + "start: " + Integer.toString(start) + " before: " + 
					Integer.toString(before) + " count: " + Integer.toString(count));
			
			if (isEnabled) {
				// verifies if the changes are for canceling the search.
				if (before < mMinCharactersToActionAutoComplete && before > count) {
					
					if (mButtonCancelSearch.getVisibility() == View.VISIBLE) {
						mButtonCancelSearch.setVisibility(View.INVISIBLE);
					}
					
					canBeCancelled = false;
					
					if (!mTextChangedByCancelButton) {
						mTextChangedByCancelButton = false;
						if (mOnSearchBarStateChangedListener != null) {
							mOnSearchBarStateChangedListener.onCancelSearch();
						}
					}
					
				}
			}
		}
		
	}
	
 	
	// ======================================================
	// SEARCH BAR TEXT CHANGE TIMER. 
	// ======================================================
	
	// handler to post execution to OnActionCounterPerform implementor.
	private static final int CLIENT_MESSAGE_PERFORM_ACTION = 1;

	private class ClientHandler extends Handler {
		
		public void handleMessage(Message message) {
			if (message.what == CLIENT_MESSAGE_PERFORM_ACTION) {
				
				Logger.i(TAG, "Perform searching action requested.");
				if (mOnSearchBarStateChangedListener != null) {

					Editable text = mTextSearchBar.getText();
					if (text != null && text.toString().length() >= mMinCharactersToActionAutoComplete) {
						mOnSearchBarStateChangedListener.onStartSearch(text.toString());
					}
					
				}
			}
		}
	}
	
	private ClientHandler mClientHandler;
	private Handler mInternalHandler;	// handler to post execution in the interval.
	private Runnable mNotification;		// executor task after post.
	
	/**
	 * Starts to count and perform any action, any recent actions will be cancelled. 
	 */
	private void performAction() {
		
		if (mClientHandler == null) {
			mClientHandler = new ClientHandler(); 
		}
		
		// cancel any pending action.
		if (mInternalHandler != null && mNotification != null) {
			mInternalHandler.removeCallbacks(mNotification);
			mInternalHandler = null;
			mNotification = null;
		}
		
		mInternalHandler = new Handler();
		mNotification = new Runnable() {
			
			@Override
			public void run() {
				if (mClientHandler != null) {
					mClientHandler.sendEmptyMessage(CLIENT_MESSAGE_PERFORM_ACTION);
				}
			}
		};
		
		mInternalHandler.postDelayed(mNotification, SEARCH_BAR_PENDING_TIME_TO_ACTION_MILLISECONDS);
	}
	
	/**
	 * Cancel to count and perform any action last pending action. 
	 */
	private void cancelAction() {
		// resets any post actions.
		if (mInternalHandler != null && mNotification != null) {
			
			mInternalHandler.removeCallbacks(mNotification);
			
			mInternalHandler = null;
			mNotification = null;
		}
	}
	
	/**
	 * Stops any action, after calling this function you must create new {@link ActionCounter}.
	 */
	private void cancelAnyAction() {
		// resets any post actions.
		if (mInternalHandler != null && mNotification != null) {
			
			mInternalHandler.removeCallbacks(mNotification);
			mInternalHandler = null;
			mNotification = null;
		}
		
		if (mClientHandler != null) {
			mClientHandler.removeCallbacks(null);
			mClientHandler = null;
		}
	}
}
