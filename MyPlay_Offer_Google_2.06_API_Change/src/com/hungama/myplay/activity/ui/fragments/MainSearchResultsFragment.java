package com.hungama.myplay.activity.ui.fragments;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.SearchResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SearchKeyboardOperation;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

public class MainSearchResultsFragment extends Fragment implements CommunicationOperationListener {
	
	public static final String TAG = "MainSearchResultsFragment";
	
	public static final String FRAGMENT_ARGUMENT_QUERY = "fragment_argument_query";
	public static final String FRAGMENT_ARGUMENT_TYPE = "fragment_argument_type";
	
	private static final int RESULT_MINIMUM_INDEX = 0;
	private static final int RESULT_TO_PRESENT = 20;
	
	private DataManager mDataManager;
	private LayoutInflater mInflater;
	
	private OnSearchResultsOptionSelectedListener mOnSearchResultsOptionSelectedListener;
	
	private SearchResponse mSearchResponse = null;
	private List<MediaItem> mMediaItems;
	
	private TextView mTitleResultCount;
	private TextView mTitleResultLabel;
	private TextView mTitleSearchQuery;
	private ListView mListResults;
	
	private View mLoadingBar;
	
	private SearchResultsAdapter mSearchResultsAdapter;
//	private ImageFetcher mImageFetcher = null;
	
	private String mLoadingContent;
	private boolean mIsThrottling = false;
	
	private Context mContext;
	
	// ======================================================
	// Life Cycle.
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();
		mDataManager = DataManager.getInstance(mContext);
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mLoadingContent = getResources().getString(R.string.search_results_loading_indicator_loading_more);
		
		Logger.i(TAG, "onCreate");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_search_results, container, false);
		
		initializeUserControls(rootView);
		
		return rootView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if(mSearchResultsAdapter == null || mSearchResultsAdapter.getCount() == 0){
			if (!isResumed()) {
				Bundle data = getArguments();
				
				String query = data.getString(FRAGMENT_ARGUMENT_QUERY);
				String type = data.getString(FRAGMENT_ARGUMENT_TYPE);
				searchForQueury(query, type);
				
			}
		}
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key));
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
//			mImageFetcher.setExitTasksEarly(true);
//			mImageFetcher.flushCache();
//		}
	}
	
	@Override
	public void onStop() {
		// cancels any running operation.
		mDataManager.cancelGetSearch();
		super.onStop();
		
		FlurryAgent.onEndSession(getActivity());
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
//		if (mImageFetcher != null) {
//			mImageFetcher.closeCache();
//			mImageFetcher = null;
//		}
	}
	
	
	// ======================================================
	// Communication.
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.SEARCH) {
			
			if (mIsThrottling) {
				Toast.makeText(getActivity(), mLoadingContent, Toast.LENGTH_SHORT).show();
				
			} else {
				if (!Utils.isListEmpty(mMediaItems)) {
					mMediaItems.clear();
					mSearchResultsAdapter.notifyDataSetChanged();
				}
				
				hideTitle();
				
				mLoadingBar.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.SEARCH) {
			
			mSearchResponse = (SearchResponse) responseObjects.get(SearchKeyboardOperation.RESPONSE_KEY_SEARCH);
			
			String query = (String) responseObjects.get(SearchKeyboardOperation.RESPONSE_KEY_QUERY);
			String type = (String) responseObjects.get(SearchKeyboardOperation.RESPONSE_KEY_TYPE);
			
			mSearchResponse.setQuery(query);
			mSearchResponse.setType(type);
			
			if (mIsThrottling) {
				
				mIsThrottling = false;
				
				List<MediaItem> newMediaItems = mSearchResponse.getContent();
				
				mMediaItems.addAll(newMediaItems);
				
				refreshAdapter();
				
			} else {
				// it's a new search results.
				mMediaItems = mSearchResponse.getContent();
				
				if (!Utils.isListEmpty(mMediaItems)) {
					// updates the list.
					refreshAdapter();
					
					// sets the title.
					mTitleResultCount.setText(Integer.toString(mSearchResponse.getTotalCount()));
					if (mMediaItems.size() == 1) {
						mTitleResultLabel.setText(R.string.search_results_layout_top_text_results_for_single);
					} else {
						mTitleResultLabel.setText(R.string.search_results_layout_top_text_results_for);
					}
					
					mTitleSearchQuery.setText(query);
					showTitle();
					
				} else {
					// sets the title.
					mTitleResultCount.setText("No");
					mTitleResultLabel.setText(R.string.search_results_layout_top_text_results_for);
					mTitleSearchQuery.setText(query);
					showTitle();
				}
			}
			
			mLoadingBar.setVisibility(View.GONE);
			
			FlurryAgent.logEvent("Search results");
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.SEARCH) {
			
			if (mIsThrottling) {
				
				mIsThrottling = false;
				
			} else {
				if (!Utils.isListEmpty(mMediaItems)) {
					mMediaItems.clear();
					refreshAdapter();
				}
				
				hideTitle();
				
				mLoadingBar.setVisibility(View.GONE);
			}
			
			if (errorType != ErrorType.OPERATION_CANCELLED && !TextUtils.isEmpty(errorMessage) && getActivity() != null) {
				Toast.makeText(getActivity().getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	// ======================================================
	// Public.
	// ======================================================
	
	public interface OnSearchResultsOptionSelectedListener {
		
		public void onPlayNowSelected(MediaItem mediaItem);
		
		public void onAddToQueueSelected(MediaItem mediaItem);
		
		public void onShowDetails(MediaItem mediaItem);
	}
	
	public void setOnSearchResultsOptionSelectedListener(OnSearchResultsOptionSelectedListener listener) {
		mOnSearchResultsOptionSelectedListener = listener;
	}
	
	public void searchForQueury(String query, String type) {
		// cancels any running operation.
		mDataManager.cancelGetSearch();
		
		// sets the flag to indicate any new response is as brand new. 
		mIsThrottling = false;
		
		// Querying like a bous!
		try {
			query = URLEncoder.encode(query, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			// something is wrong with the query - void the request.
			return;
		}
		
		mDataManager.getSearchKeyboard(query, type, String.valueOf(RESULT_MINIMUM_INDEX), 
													String.valueOf(RESULT_TO_PRESENT), this);
	}
	
	
	// ======================================================
	// Helper methods.
	// ======================================================

	private void initializeUserControls(View rootView) {
		mTitleResultCount = (TextView) rootView.findViewById(R.id.main_search_results_title_text_count);
		mTitleResultLabel = (TextView) rootView.findViewById(R.id.main_search_results_title_label_result_for);
		mTitleSearchQuery = (TextView) rootView.findViewById(R.id.main_search_results_title_text_search_query);
		mListResults = (ListView)  rootView.findViewById(R.id.main_search_results_list);
		
		mListResults.setOnScrollListener(new ScrollToBottomListener());
		
		mLoadingBar = (View) rootView.findViewById(R.id.main_search_results_loading_indicator);
		
		// initializes the Adapter and default empty result list. 
		mMediaItems = new ArrayList<MediaItem>();
		
		mSearchResultsAdapter = new SearchResultsAdapter();
		mListResults.setAdapter(mSearchResultsAdapter);
		
		// creates a footer for the throttle loading.
//		mThrottleFooterBar = mInflater.inflate(R.layout.layout_main_search_results_loading_more_bar, null, false);
	}
	
	private void showTitle() {
		mTitleResultCount.setVisibility(View.VISIBLE);
		mTitleResultLabel.setVisibility(View.VISIBLE);
		mTitleSearchQuery.setVisibility(View.VISIBLE);
	}
	
	private void hideTitle() {
		mTitleResultCount.setVisibility(View.GONE);
		mTitleResultLabel.setVisibility(View.GONE);
		mTitleSearchQuery.setVisibility(View.GONE);
	}
	
	private void throttleForNextPage() {
		
		int currentPagingIndex = mMediaItems.size();
		
		mIsThrottling = true;
		
		// Querying like a bous!
		mDataManager.getSearchKeyboard(mSearchResponse.getQuery(), mSearchResponse.getType(), Integer.toString(currentPagingIndex), 
				Integer.toString(RESULT_TO_PRESENT), this);
	}
	
	private static class ViewHolder {
		ImageView searchResultImage ;
		TextView searchResultTopText;
		ImageView searchResultImageType;
		TextView searchResultTypeAndName;
		RelativeLayout searchResultRow;
		ImageButton searchResultButtonPlay;
		
	}
	
	private class SearchResultsAdapter extends BaseAdapter implements OnClickListener, OnLongClickListener {
		
		private final String trackPrefix;
		private final String albumPrefix;
		private final String playlistPrefix;
		private final String artistPrefix;
		private final String videoPrefix;
		private final String playlistAlbumSuffix;
		
		
		public SearchResultsAdapter() {
			
			// creates the prefixes.
			trackPrefix = getResources().getString(R.string.search_results_layout_bottom_text_for_track);
			albumPrefix = getResources().getString(R.string.search_results_layout_bottom_text_for_album);
			playlistPrefix = getResources().getString(R.string.search_results_layout_bottom_text_for_playlist);
			artistPrefix = getResources().getString(R.string.search_result_line_type_and_name_artist);
			videoPrefix = getResources().getString(R.string.search_results_layout_bottom_text_for_video);
			playlistAlbumSuffix = getResources().getString(R.string.search_results_layout_bottom_text_album_playlist);
			
			// initializes the image loader.
			int imageSize = getResources().getDimensionPixelSize(R.dimen.search_result_line_image_size);
			
			// creates the cache.
//			ImageCache.ImageCacheParams cacheParams =
//	                new ImageCache.ImageCacheParams(getActivity(), DataManager.FOLDER_THUMBNAILS_CACHE);
//	        cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);
			
//			mImageFetcher = new ImageFetcher(getActivity(), imageSize);
//			mImageFetcher.setLoadingImage(R.drawable.background_home_tile_album_default);
//			mImageFetcher.addImageCache(getChildFragmentManager(), cacheParams);
//	        mImageFetcher.setImageFadeIn(true);
		}			
		
		@Override
		public int getCount() {
			return (Utils.isListEmpty(mMediaItems) ? 0 : mMediaItems.size());
		}

		@Override
		public Object getItem(int position) {
			return mMediaItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
		
			final ViewHolder viewHolder;
			// create view if not exist.
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_media_search_result_line, parent, false);
				
				viewHolder = new ViewHolder();	
				viewHolder.searchResultRow = (RelativeLayout) convertView.findViewById(R.id.linearlayout_search_result_line);
				viewHolder.searchResultButtonPlay = (ImageButton) convertView.findViewById(R.id.search_result_line_button_play);
				
				viewHolder.searchResultTopText = (TextView) convertView.findViewById(R.id.search_result_line_top_text);
				viewHolder.searchResultImageType = (ImageView) convertView.findViewById(R.id.search_result_media_image_type);
				viewHolder.searchResultTypeAndName = (TextView) convertView.findViewById(R.id.search_result_text_media_type_and_name);				
				
				convertView.setTag(R.id.view_tag_view_holder, viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag(R.id.view_tag_view_holder);
			}
			
			// populate the view from the keywords's list.
			MediaItem mediaItem = mMediaItems.get(position);
			
			// stores the object in the view.
			convertView.setTag(R.id.view_tag_object, mediaItem);
			

			// gets the image and its size.
	        viewHolder.searchResultImage = (ImageView) convertView.findViewById(R.id.search_result_media_image);
			
	        Picasso.with(mContext).cancelRequest(viewHolder.searchResultImage);
	        if (mContext != null && !TextUtils.isEmpty(mediaItem.getImageUrl())) {
//	        	mImageFetcher.loadImage(mediaItem.getImageUrl(), viewHolder.searchResultImage);
	        	
	        	Picasso.with(mContext)
	        			.load(mediaItem.getImageUrl())
	        			.placeholder(R.drawable.background_home_tile_album_default)
	        			.into(viewHolder.searchResultImage);	        		        	
	        	
	        } else {
	        	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
	        		viewHolder.searchResultImage.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_media_details_playlist_inside_thumb));
	        	} else {
	        		viewHolder.searchResultImage.setImageResource(R.drawable.background_media_details_playlist_inside_thumb);
	        	}
	        }
		        
			// Set title 			
			viewHolder.searchResultTopText.setText(mediaItem.getTitle());
			
			// Set Image Type and Text Below title By Type
			if (mediaItem.getMediaType() == MediaType.TRACK) {
				viewHolder.searchResultImageType.setImageResource(R.drawable.icon_main_settings_music);
				viewHolder.searchResultTypeAndName.setText(trackPrefix + " - " + mediaItem.getAlbumName());
				
			} else if (mediaItem.getMediaType() == MediaType.ALBUM) {
				viewHolder.searchResultImageType.setImageResource(R.drawable.icon_main_search_album);
				viewHolder.searchResultTypeAndName.setText(albumPrefix + " - " 
									+ String.valueOf(mediaItem.getMusicTrackCount() 
									+ " " + playlistAlbumSuffix));								
				
			} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
				viewHolder.searchResultImageType.setImageResource(R.drawable.icon_home_music_tile_playlist);
				viewHolder.searchResultTypeAndName.setText(playlistPrefix   + " - " 
										+ String.valueOf(mediaItem.getMusicTrackCount() 
										+ " " + playlistAlbumSuffix));
				
			} else if (mediaItem.getMediaType() == MediaType.ARTIST) {
				viewHolder.searchResultImageType.setImageResource(R.drawable.icon_main_settings_live_radio);			
				viewHolder.searchResultTypeAndName.setText(artistPrefix);
				
			} else if (mediaItem.getMediaType() == MediaType.VIDEO){
				viewHolder.searchResultImageType.setImageResource(R.drawable.icon_main_settings_videos);
				viewHolder.searchResultTypeAndName.setText(videoPrefix + " - " + String.valueOf(mediaItem.getAlbumName()));
			}
			
			viewHolder.searchResultRow.setOnClickListener(this);
			viewHolder.searchResultRow.setOnLongClickListener(this);
			
			viewHolder.searchResultButtonPlay.setOnClickListener(this);
			viewHolder.searchResultButtonPlay.setOnLongClickListener(this);
					
			
			return convertView;
		}
		
		@Override
		public void onClick(View view) {
			final int viewId = view.getId();
			if (viewId == R.id.linearlayout_search_result_line) {
				// gets the media item from the row.
				MediaItem mediaItem = (MediaItem) view.getTag(R.id.view_tag_object);
				
				if (mOnSearchResultsOptionSelectedListener != null) {
					mOnSearchResultsOptionSelectedListener.onShowDetails(mediaItem);
				}
				
			} else if (viewId == R.id.search_result_line_button_play) {
				// gets the media item from the row.
				View parent = (View) view.getParent();
				MediaItem mediaItem = (MediaItem) parent.getTag(R.id.view_tag_object);
				
				if (mOnSearchResultsOptionSelectedListener != null) {
					mOnSearchResultsOptionSelectedListener.onPlayNowSelected(mediaItem);
				}
				
			}
		}
		
		@Override
		public boolean onLongClick(View view) {
			final int viewId = view.getId();
			if (viewId == R.id.linearlayout_search_result_line) {
				// gets the media item from the row.
				MediaItem mediaItem = (MediaItem) view.getTag(R.id.view_tag_object);
				
				// radio items does not support long clicks.
				if (mediaItem.getMediaContentType() != MediaContentType.RADIO && mediaItem.getMediaContentType() != MediaContentType.VIDEO) {
					showMediaItemOptionsDialog(mediaItem);
				}
				
				return true;
				
			} else if (viewId == R.id.search_result_line_button_play) {
				// gets the media item from the row.
				View parent = (View) view.getParent();
				MediaItem mediaItem = (MediaItem) parent.getTag(R.id.view_tag_object);
				
				// radio items does not support long clicks.
				if (mediaItem.getMediaContentType() != MediaContentType.RADIO && mediaItem.getMediaContentType() != MediaContentType.VIDEO) {
					showMediaItemOptionsDialog(mediaItem);
				}
				
				return true;
			}
			
			return false;
		}
		
		private void showMediaItemOptionsDialog(final MediaItem mediaItem) {
			//set up custom dialog
			final Dialog mediaItemOptionsDialog = new Dialog(getActivity());
			mediaItemOptionsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mediaItemOptionsDialog.setContentView(R.layout.dialog_media_playing_options);
			mediaItemOptionsDialog.setCancelable(true);
	        mediaItemOptionsDialog.show();
	        
			// sets the title.
	        TextView title = (TextView) mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_title_text);
	        title.setText(mediaItem.getTitle());
	        
	        // sets the cancel button.
	        ImageButton closeButton = (ImageButton) mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_title_image);
	        closeButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mediaItemOptionsDialog.dismiss();				
				}
			});
	        
	        // sets the options buttons.
	        LinearLayout llPlayNow = (LinearLayout) mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_play_now_row);
	        LinearLayout llAddtoQueue = (LinearLayout) mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_add_to_queue_row);
	        LinearLayout llDetails = (LinearLayout) mediaItemOptionsDialog.findViewById(R.id.long_click_custom_dialog_details_row);

	        // play now.
	        llPlayNow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					
					if (mOnSearchResultsOptionSelectedListener != null) {
						mOnSearchResultsOptionSelectedListener.onPlayNowSelected(mediaItem);
					}
					
					mediaItemOptionsDialog.dismiss();
				}
			});        
	        
	        // add to queue.
	        llAddtoQueue.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mOnSearchResultsOptionSelectedListener != null) {
						mOnSearchResultsOptionSelectedListener.onAddToQueueSelected(mediaItem);
					}
					
					mediaItemOptionsDialog.dismiss();			
				}
			});
	        
	        // show details.
	        llDetails.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View view) {
					if (mOnSearchResultsOptionSelectedListener != null) {
						mOnSearchResultsOptionSelectedListener.onShowDetails(mediaItem);
					}
					
					mediaItemOptionsDialog.dismiss(); 
				}
			});
		}

	}
	
	private class ScrollToBottomListener implements OnScrollListener {
		
		private int totalItemCount;
		private int currentFirstVisibleItem;
		private int currentVisibleItemCount;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			this.currentFirstVisibleItem = firstVisibleItem;
		    this.currentVisibleItemCount = visibleItemCount;
		    this.totalItemCount = totalItemCount;
		}
	
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (mIsThrottling)
				return;
			
			if (scrollState == SCROLL_STATE_IDLE && currentVisibleItemCount > 0) {
				Logger.v(TAG, "totalItemCount " + totalItemCount 
							+ " currentFirstVisibleItem " + currentFirstVisibleItem 
							+ " currentVisibleItemCount " + currentVisibleItemCount);
				
				boolean lastItemVisible = (currentFirstVisibleItem + currentVisibleItemCount) == totalItemCount;
				boolean needMorePages = !Utils.isListEmpty(mMediaItems) && mMediaItems.size() < mSearchResponse.getTotalCount();
				
				if (lastItemVisible && needMorePages) {
					Logger.v(TAG, "More Items are requested - throttling !!!");
					throttleForNextPage();
				}
			}
		}
	
	}
	
	private void refreshAdapter(){
		if(isVisible()){
			mSearchResultsAdapter.notifyDataSetChanged();
		}
	}

}
