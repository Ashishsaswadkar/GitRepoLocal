package com.hungama.myplay.activity.ui.fragments;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.social.StreamItem;
import com.hungama.myplay.activity.data.dao.hungama.social.StreamItemCategory;
import com.hungama.myplay.activity.gigya.InviteFriendsActivity;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SocialMyStreamOperation;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.StreamMediaItemView;
import com.hungama.myplay.activity.ui.widgets.StreamMediaItemView.SocialMyStreamMediaItemListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

public class SocialMyStreamFragment extends Fragment implements OnClickListener, 
													CommunicationOperationListener {
	
	private static final String TAG = "SocialMyStreamFragment";
	
	private Context mContext;
	private DataManager mDataManager;
	
	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;
	
	public void setOnMediaItemOptionSelectedListener(OnMediaItemOptionSelectedListener listener) {
		mOnMediaItemOptionSelectedListener = listener;
	}
	
	private LinearLayout mContainerNoContent;
	private LinearLayout mContainerConnectionError;
	private ListView mListContent;
	
	private LinearLayout mTitleBar;
	private Button mButtonOpen;
	private Button mButtonClose;
	private Button mButtonEveryone;
	private Button mButtonFriends;
	private Button mButtonMe;
	private Button mButtonInvite;
	private Button mButtonContentInvite;
	
	private StreamItemsAdapter mStreamItemsAdapter;
	private List<StreamItem> mStreamItems = new ArrayList<StreamItem>();
	
//	private ImageFetcher mImageFetcher = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = getActivity().getApplicationContext();
		mDataManager = DataManager.getInstance(mContext);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_social_mystream, container, false);
		
		initializeUserControls(rootView);
		
		onFriendsButtonClicked();
		
		return rootView;
	}
	
	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		
		if (viewId == R.id.social_mystream_title_bar_button_close) {
			closeTitleBar();
			
		} else if (viewId == R.id.social_mystream_title_bar_button_open) {
			openTitleBar();
			
		} else if (viewId == R.id.social_mystream_title_bar_button_everyone) {
			onEveryoneButtonClicked();
			closeTitleBar();
			
		} else if (viewId == R.id.social_mystream_title_bar_button_friends) {
			onFriendsButtonClicked();
			closeTitleBar();
			
		} else if (viewId == R.id.social_mystream_title_bar_button_me) {
			onMeButtonClicked();
			closeTitleBar();
			
		} else if (viewId == R.id.social_mystream_title_bar_button_invite || 
				   viewId == R.id.social_mystream_container_no_content_button_invite) {
			
			closeTitleBar();
			// Invite Friends Section
			Intent intent = new Intent(getActivity(), InviteFriendsActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
//		if (mImageFetcher != null)
//			mImageFetcher.setExitTasksEarly(false);
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
		mDataManager.cancelGetMyStreamItems();
		
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
	// Communication Operations callbacks.
	// ======================================================
	
	@Override
	public void onStart(int operationId) {}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_MY_STREAM) {
			if (responseObjects.containsKey(SocialMyStreamOperation.RESULT_KEY_STREAM_ITEMS)) {
				mStreamItems = (List<StreamItem>) responseObjects.get(SocialMyStreamOperation.RESULT_KEY_STREAM_ITEMS);
				
				if(mStreamItems != null) {
					
					if (!mStreamItems.isEmpty()){				
						mListContent.setVisibility(View.VISIBLE);
						mContainerNoContent.setVisibility(View.GONE);
					} else {
						mListContent.setVisibility(View.GONE);
						mContainerNoContent.setVisibility(View.VISIBLE);
					}
				}
				
				mContainerConnectionError.setVisibility(View.GONE);
				
				mStreamItemsAdapter.notifyDataSetChanged();
				
			} else {
				mListContent.setVisibility(View.GONE);
				mContainerNoContent.setVisibility(View.VISIBLE);
				
				mStreamItems.clear();
				mStreamItemsAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_MY_STREAM) {
			
			if (mListContent.getVisibility() == View.VISIBLE)
				mListContent.setVisibility(View.GONE);
			
			if (mContainerNoContent.getVisibility() != View.VISIBLE)
				mContainerNoContent.setVisibility(View.VISIBLE);
			
			mStreamItems.clear();
			mStreamItemsAdapter.notifyDataSetChanged();
			
			if (errorType != ErrorType.OPERATION_CANCELLED) {
				Logger.e(TAG, "asjdfnksdjfnskdjfnsdkjfnskdjfnskdjfnk Error Error");
				mContainerConnectionError.setVisibility(View.VISIBLE);
				mContainerNoContent.setVisibility(View.GONE);
			}
		}	
	}
	

	// ======================================================
	// Helper methods.
	// ======================================================
	
	private void initializeUserControls(View rootView) {
		
		mContainerNoContent = (LinearLayout) rootView.findViewById(R.id.social_mystream_container_no_content);
		mListContent = (ListView) rootView.findViewById(R.id.social_mystream_content_list);
		
		mTitleBar = (LinearLayout) rootView.findViewById(R.id.social_mystream_title_bar);
		
		mButtonOpen = (Button) rootView.findViewById(R.id.social_mystream_title_bar_button_open);
		mButtonClose = (Button) rootView.findViewById(R.id.social_mystream_title_bar_button_close);
		mButtonEveryone = (Button) rootView.findViewById(R.id.social_mystream_title_bar_button_everyone);
		mButtonFriends = (Button) rootView.findViewById(R.id.social_mystream_title_bar_button_friends);
		mButtonMe = (Button) rootView.findViewById(R.id.social_mystream_title_bar_button_me);
		mButtonInvite = (Button) rootView.findViewById(R.id.social_mystream_title_bar_button_invite);
		mButtonContentInvite = (Button) rootView.findViewById(R.id.social_mystream_container_no_content_button_invite);
		
		mButtonOpen.setOnClickListener(this);
		mButtonClose.setOnClickListener(this);
		mButtonEveryone.setOnClickListener(this);
		mButtonFriends.setOnClickListener(this);
		mButtonMe.setOnClickListener(this);
		mButtonInvite.setOnClickListener(this);
		mButtonContentInvite.setOnClickListener(this);
		
		mStreamItemsAdapter = new StreamItemsAdapter();
		mListContent.setAdapter(mStreamItemsAdapter);

		
		// sets the connection error message.
		mContainerConnectionError = (LinearLayout) 
				rootView.findViewById(R.id.social_mystream_container_connection_error);
		
		Button retryButton = (Button) 
				mContainerConnectionError.findViewById(R.id.connection_error_empty_view_button_retry);
		
		retryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				
				StreamItemCategory streamItemCategory = null;
				if (mButtonEveryone.isSelected()) {
					streamItemCategory = StreamItemCategory.EVERYONE;
					
				} else if (mButtonFriends.isSelected()) {
					streamItemCategory = StreamItemCategory.FRIENDS;
					
				} else {
					streamItemCategory = StreamItemCategory.ME;
				}

				mDataManager.cancelGetMyStreamItems();
				mDataManager.getMyStreamItems(streamItemCategory, SocialMyStreamFragment.this);
			}
		});
	}
	
	private void openTitleBar() {
		Animation slideInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_left_enter);
		slideInAnimation.setAnimationListener(new AnimationListener() {

			@Override public void onAnimationRepeat(Animation animation) {}
			
			@Override public void onAnimationEnd(Animation animation) {}
			
			@Override
			public void onAnimationStart(Animation animation) {
				mTitleBar.setVisibility(View.VISIBLE);
			}
			
		});
		mButtonOpen.setVisibility(View.GONE);
		mTitleBar.startAnimation(slideInAnimation);
	}
		
	private void closeTitleBar() {
		Animation slideOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_right_exit);
		slideOutAnimation.setAnimationListener(new AnimationListener() {

			@Override public void onAnimationRepeat(Animation animation) {}
			
			@Override public void onAnimationStart(Animation animation) {}
			
			@Override 
			public void onAnimationEnd(Animation animation) {
				mTitleBar.setVisibility(View.INVISIBLE);
			}
		});
		
		mTitleBar.startAnimation(slideOutAnimation);
		mButtonOpen.setVisibility(View.VISIBLE);
	}
	
	private void onEveryoneButtonClicked() {
		mButtonEveryone.setSelected(true);
		mButtonFriends.setSelected(false);
		mButtonMe.setSelected(false);
		
		mDataManager.cancelGetMyStreamItems();
		mDataManager.getMyStreamItems(StreamItemCategory.EVERYONE, this);
		
		FlurryAgent.logEvent("My Stream - Everyone");
	}
	
	private void onFriendsButtonClicked() {
		mButtonEveryone.setSelected(false);
		mButtonFriends.setSelected(true);
		mButtonMe.setSelected(false);
		
		mDataManager.cancelGetMyStreamItems();
		mDataManager.getMyStreamItems(StreamItemCategory.FRIENDS, this);
		
		FlurryAgent.logEvent("My Stream - Friends");
	}

	private void onMeButtonClicked() {
		mButtonEveryone.setSelected(false);
		mButtonFriends.setSelected(false);
		mButtonMe.setSelected(true);
		
		mDataManager.cancelGetMyStreamItems();
		mDataManager.getMyStreamItems(StreamItemCategory.ME, this);
		
		FlurryAgent.logEvent("My Stream - Me");
	}

	
	// ======================================================
	// Adapter.
	// ======================================================
	
	private static final String STREAM_DATE_FORMATE = "dd.MM.yyyy";
	
	private static final class ViewHolder {
		
		View convertView;
		ImageView friendThumbnail;
		TextView title;
		TextView dateLabel;
		StreamMediaItemView streamMediaItem1;
		StreamMediaItemView streamMediaItem2;
		StreamMediaItemView streamMediaItem3;
		StreamMediaItemView streamMediaItem4;
	}
	
	private class StreamItemsAdapter extends BaseAdapter implements SocialMyStreamMediaItemListener, OnClickListener {
		
		private Resources resources;
		private LayoutInflater mLayoutInflater;
		
		private int evenPositionRowColorResource;
		private int oddPositionRowColorResource;
		
		private int streamMediaItemViewSize;
		private int mediaItemViewMargin;
		
		private DateTime dateTimeNow = new DateTime(new Date());
//		private Calendar nowCalendar = Calendar.getInstance();
//		private Calendar itemCalendar = Calendar.getInstance();
		
		public StreamItemsAdapter() {
			mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			resources = getResources();
			
			// initializes the image loader.
			mediaItemViewMargin = resources.getDimensionPixelSize(R.dimen.social_mystream_item_padding);
			
	        oddPositionRowColorResource = resources.getColor(R.color.social_mystream_item_background_odd);
	        evenPositionRowColorResource = resources.getColor(R.color.social_mystream_item_background_even);
	        
	        Display display = getActivity().getWindowManager().getDefaultDisplay();
	        int screenWidth = 0;
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
	        	screenWidth = display.getWidth();
	        } else {
	        	Point displaySize = new Point();
	        	display.getSize(displaySize);
	        	screenWidth = displaySize.x;
	        }
	        
	        // calculates the mediaItemView's size, considers the item's mergins.
	        streamMediaItemViewSize = (screenWidth - (5 * mediaItemViewMargin)) / 4;
	        
	        // creates the cache.
//			ImageCache.ImageCacheParams cacheParams =
//	                new ImageCache.ImageCacheParams(getActivity(), DataManager.FOLDER_THUMBNAILS_CACHE);
//			cacheParams.compressFormat = CompressFormat.PNG;
//	        cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);
//			
//			mImageFetcher = new ImageFetcher(getActivity(), streamMediaItemViewSize);
//			mImageFetcher.addImageCache(getChildFragmentManager(), cacheParams);
//	        mImageFetcher.setImageFadeIn(true);
		}

		@Override
		public int getCount() {
			return mStreamItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mStreamItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mStreamItems.get(position).conentId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder viewHolder;
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.list_item_social_mystream_item, parent, false);
				
				viewHolder = new ViewHolder();
				viewHolder.convertView = convertView;
				viewHolder.friendThumbnail = (ImageView) convertView.findViewById(R.id.social_mystream_item_friend_thumbnail);
				viewHolder.title = (TextView) convertView.findViewById(R.id.social_mystream_item_title);
				viewHolder.dateLabel = (TextView) convertView.findViewById(R.id.social_mystream_item_date_from_label);
				viewHolder.streamMediaItem1 = (StreamMediaItemView) convertView.findViewById(R.id.social_mystream_item_streammediaitem1);
				viewHolder.streamMediaItem2 = (StreamMediaItemView) convertView.findViewById(R.id.social_mystream_item_streammediaitem2);
				viewHolder.streamMediaItem3 = (StreamMediaItemView) convertView.findViewById(R.id.social_mystream_item_streammediaitem3);
				viewHolder.streamMediaItem4 = (StreamMediaItemView) convertView.findViewById(R.id.social_mystream_item_streammediaitem4);
				
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			StreamItem streamItem = mStreamItems.get(position);
			
			// colors the background by pattern.
			boolean isRowEven = position % 2 == 0;
			if (isRowEven) {
				convertView.setBackgroundColor(evenPositionRowColorResource);
			} else {
				convertView.setBackgroundColor(oddPositionRowColorResource);
			}
			
			// the friend thumbnail.
//			mImageFetcher.loadImage(streamItem.photoUrl, viewHolder.friendThumbnail);
			
			viewHolder.friendThumbnail.setTag(R.id.view_tag_object, streamItem.userId);
			viewHolder.friendThumbnail.setImageResource(R.drawable.background_home_tile_album_default);
			Picasso.with(mContext).cancelRequest(viewHolder.friendThumbnail);
			if (mContext != null && streamItem != null && streamItem.photoUrl != null && !TextUtils.isEmpty(streamItem.photoUrl)) {
				Picasso.with(mContext)
						.load(streamItem.photoUrl)
						.into(viewHolder.friendThumbnail);
			}
			viewHolder.friendThumbnail.setOnClickListener(this);
			
			// sets the title.
			String titleString = streamItem.userName + " "
								+ streamItem.action + " "
								+ streamItem.title + " "
								+  streamItem.moreSongs;
			viewHolder.title.setText(titleString);
			
			// sets the date label.
			DateTime streamItemDate = new DateTime(streamItem.getDate());
			int daysInterval = Days.daysBetween(dateTimeNow, streamItemDate).getDays();
			String dateLabelString;
			if (daysInterval == 0) {
				// shows it as hours.
				int hours = Math.abs(Hours.hoursBetween(dateTimeNow, streamItemDate).getHours());
				if (hours > 0) {
					dateLabelString = Integer.toString(hours) + " " + resources.getString(R.string.social_mystream_date_label_hours);
				} else {
					dateLabelString = resources.getString(R.string.social_mystream_date_label_now);
				}
				
			} else if (daysInterval > 0 && daysInterval < 7) {
				if (daysInterval == 1) {
					// shows the "a day" label.
					dateLabelString = resources.getString(R.string.social_mystream_date_label_a_day);
				} else {
					// shows the "X days" label.
					dateLabelString = Integer.toString(daysInterval) + " " + 
								resources.getString(R.string.social_mystream_date_label_days);
				}

			} else if (daysInterval == 7) {
				// shows the "week" label.
				dateLabelString = resources.getString(R.string.social_mystream_date_label_week);
			} else {
				// sets the current date.
				dateLabelString = (String) DateFormat.format(STREAM_DATE_FORMATE ,streamItem.getDate());
			}
			viewHolder.dateLabel.setText(dateLabelString);
			
			
			/*
			 * sets the mediaItems.
			 */
			Stack<StreamMediaItemView> socialstreamMediaItemsStack = new Stack<StreamMediaItemView>();
			socialstreamMediaItemsStack.add(viewHolder.streamMediaItem4);
			socialstreamMediaItemsStack.add(viewHolder.streamMediaItem3);
			socialstreamMediaItemsStack.add(viewHolder.streamMediaItem2);
			
			viewHolder.streamMediaItem4.setVisibility(View.INVISIBLE);
			viewHolder.streamMediaItem3.setVisibility(View.INVISIBLE);
			viewHolder.streamMediaItem2.setVisibility(View.INVISIBLE);
			
			// resets the media items views with an updated size.
			LinearLayout.LayoutParams mediaItemParams = null;
			
			// the first visible one.
			mediaItemParams = new LinearLayout.LayoutParams(streamMediaItemViewSize, streamMediaItemViewSize, 1);
			mediaItemParams.rightMargin = mediaItemViewMargin;
			viewHolder.streamMediaItem1.setLayoutParams(mediaItemParams);
			
			for (StreamMediaItemView streamMediaItemView : socialstreamMediaItemsStack) {
				mediaItemParams = new LinearLayout.LayoutParams(streamMediaItemViewSize, streamMediaItemViewSize, 1);
				mediaItemParams.rightMargin = mediaItemViewMargin;
				streamMediaItemView.setLayoutParams(mediaItemParams);
			}
			
			// populates the first visible one.
			MediaItem firstItem = createMediaItemFromStreamItem(streamItem);
			viewHolder.streamMediaItem1.setVisibility(View.VISIBLE);
			viewHolder.streamMediaItem1.setTag(R.id.view_tag_object, firstItem);
			viewHolder.streamMediaItem1.setSocialMyStreamMediaItemListener(this);
			
			if (StreamItem.TYPE_BADGE.equalsIgnoreCase(streamItem.type)) {
				viewHolder.streamMediaItem1.setPlayButtonVisibilty(false);
				viewHolder.streamMediaItem1.setClickable(false);
				if (isRowEven) {
					viewHolder.streamMediaItem1.setBackgroundColor(evenPositionRowColorResource);
				} else {
					viewHolder.streamMediaItem1.setBackgroundColor(oddPositionRowColorResource);
				}
				
			} else {
				viewHolder.streamMediaItem1.setPlayButtonVisibilty(true);
				viewHolder.streamMediaItem1.setClickable(true);
				viewHolder.streamMediaItem1.setImageResource(R.drawable.background_home_tile_album_default);
			}
			
//			mImageFetcher.loadImage(firstItem.getImageUrl(), viewHolder.streamMediaItem1.getBackgroundImage());
			Picasso.with(mContext).cancelRequest(viewHolder.streamMediaItem1.getBackgroundImage());
			if (mContext != null && firstItem != null && firstItem.getImageUrl() != null && !TextUtils.isEmpty(firstItem.getImageUrl())) {
				Picasso.with(mContext)
				.load(firstItem.getImageUrl())
				.into(viewHolder.streamMediaItem1.getBackgroundImage());
			} else {
				viewHolder.streamMediaItem1.setVisibility(View.INVISIBLE);
			}
			
			// populates the other.
			if (!Utils.isListEmpty(streamItem.moreSongsItems)) {
				List<MediaItem> mediaItems = streamItem.moreSongsItems;
				StreamMediaItemView mediaItemView = null;
				
				for (MediaItem mediaItem : mediaItems) {
					
					if (socialstreamMediaItemsStack.isEmpty()) {
						break;
					}
					
					mediaItemView = socialstreamMediaItemsStack.pop();
					mediaItemView.setVisibility(View.VISIBLE);
					mediaItemView.setTag(R.id.view_tag_object, mediaItem);
					mediaItemView.setSocialMyStreamMediaItemListener(this);
					
//					mImageFetcher.loadImage(mediaItem.getImageUrl(), mediaItemView.getBackgroundImage());
					Picasso.with(mContext).cancelRequest(mediaItemView.getBackgroundImage());
					if (mContext != null && mediaItem != null && mediaItem.getImageUrl() != null && !TextUtils.isEmpty(mediaItem.getImageUrl())) {
						Picasso.with(mContext)
						.load(mediaItem.getImageUrl())
						.into(mediaItemView.getBackgroundImage());
					}
				}
			}
			
			return convertView;
		}
		
		@Override
		public void onPlayButtonClicked(View mediaItemView) {
			MediaItem mediaItem = (MediaItem) mediaItemView.getTag(R.id.view_tag_object);
			
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				if (mOnMediaItemOptionSelectedListener != null) {
					mOnMediaItemOptionSelectedListener.onMediaItemOptionShowDetailsSelected(mediaItem, 0);
				}
			} else if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				if (mOnMediaItemOptionSelectedListener != null) {
					mOnMediaItemOptionSelectedListener.onMediaItemOptionShowDetailsSelected(mediaItem, 0);
				}
			}
			
		}

		@Override
		public void onItemClicked(View mediaItemView) {
			MediaItem mediaItem = (MediaItem) mediaItemView.getTag(R.id.view_tag_object);
			
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				if (mOnMediaItemOptionSelectedListener != null) {
					mOnMediaItemOptionSelectedListener.onMediaItemOptionShowDetailsSelected(mediaItem, 0);
				}
			} else if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				if (mOnMediaItemOptionSelectedListener != null) {
					mOnMediaItemOptionSelectedListener.onMediaItemOptionShowDetailsSelected(mediaItem, 0);
				}
			}
		}

		@Override
		public void onPlayButtonLongClicked(View mediaItemView) {
			MediaItem mediaItem = (MediaItem) mediaItemView.getTag(R.id.view_tag_object);
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				showMediaItemOptionsDialog(mediaItem, 0);
			}
		}

		@Override
		public void onItemLongClicked(View mediaItemView) {
			MediaItem mediaItem = (MediaItem) mediaItemView.getTag(R.id.view_tag_object);
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				showMediaItemOptionsDialog(mediaItem, 0);
			}
		}
		
		
		private void showMediaItemOptionsDialog(final MediaItem mediaItem, final int position) {
			//set up custom dialog
			final Dialog dialog = new Dialog(getActivity());
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.dialog_media_playing_options);
			dialog.setCancelable(true);
			dialog.show();
	        
			// sets the title.
	        TextView title = (TextView) dialog.findViewById(R.id.long_click_custom_dialog_title_text);
	        title.setText(mediaItem.getTitle());
	        
	        // sets the cancel button.
	        ImageButton closeButton = (ImageButton) dialog.findViewById(R.id.long_click_custom_dialog_title_image);
	        closeButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();				
				}
			});
	        
	        // sets the options buttons.
	        LinearLayout llPlayNow = (LinearLayout) dialog.findViewById(R.id.long_click_custom_dialog_play_now_row);
	        LinearLayout llAddtoQueue = (LinearLayout) dialog.findViewById(R.id.long_click_custom_dialog_add_to_queue_row);
	        LinearLayout llDetails = (LinearLayout) dialog.findViewById(R.id.long_click_custom_dialog_details_row);
	        
	        // play now.
	        llPlayNow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mOnMediaItemOptionSelectedListener != null){
						mOnMediaItemOptionSelectedListener.onMediaItemOptionPlayNowSelected(mediaItem, position);
					}
					dialog.dismiss();
				}
			});        
	                
	        // add to queue.
	        llAddtoQueue.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mOnMediaItemOptionSelectedListener != null){
						mOnMediaItemOptionSelectedListener.onMediaItemOptionAddToQueueSelected(mediaItem, position);
					}
					dialog.dismiss();			
				}
			});
	        
	        // show details.
	        llDetails.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View view) {
					if (mOnMediaItemOptionSelectedListener != null){
						mOnMediaItemOptionSelectedListener.onMediaItemOptionShowDetailsSelected(mediaItem, position);
					}
					dialog.dismiss();
				}
			});
		}
		
		private MediaItem createMediaItemFromStreamItem(StreamItem streamItem) {
			MediaItem mediaItem = new MediaItem(streamItem.conentId, streamItem.title, streamItem.albumName, 
												streamItem.albumName, streamItem.imageUrl, streamItem.bigImageUrl, 
												streamItem.type, streamItem.songsCount);
			
			if (MediaType.VIDEO.name().equalsIgnoreCase(streamItem.type)) {
				mediaItem.setMediaContentType(MediaContentType.VIDEO);
				
			} else if (MediaType.BADGE.name().equalsIgnoreCase(streamItem.type)) {
				mediaItem.setMediaContentType(MediaContentType.BADGE);
				
			} else {
				mediaItem.setMediaContentType(MediaContentType.MUSIC);
			}
			
			return mediaItem;
		}

		@Override
		public void onClick(View view) {
			long friendId = (Long) view.getTag(R.id.view_tag_object);			
			Bundle arguments = new Bundle();
			arguments.putString(ProfileActivity.DATA_EXTRA_USER_ID,  String.valueOf(friendId));
			Intent profileActivity = new Intent(getActivity().getApplicationContext(), ProfileActivity.class);
			profileActivity.putExtras(arguments);
			startActivity(profileActivity);
		}
		
	}


}
