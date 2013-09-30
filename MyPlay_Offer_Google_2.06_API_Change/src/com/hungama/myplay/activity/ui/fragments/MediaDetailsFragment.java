package com.hungama.myplay.activity.ui.fragments;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.ActionDefinition;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.Video;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.RelatedVideoOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.VideoStreamingOperation;
import com.hungama.myplay.activity.ui.MediaDetailsActivity;
import com.hungama.myplay.activity.util.Logger;
import com.squareup.picasso.Picasso;

public class MediaDetailsFragment extends MainFragment implements
		CommunicationOperationListener {

	private static final String TAG = "MediaDetailsFragment";

	public static final String ARGUMENT_MEDIAITEM = "argument_mediaitem";
	public static final String ARGUMENT_MEDIA_ITEMS_VIDEOS = "argument_media_items_videos";

	public static final int FAVORITE_SUCCESS = 1;

	private DataManager mDataManager;
	private MediaItem mMediaItem;

	private MediaSetDetails mMediaSetDetails;
	private MediaTrackDetails mMediaTrackDetails;
	private Video video;
	
	private ImageView mMediaImage;

	// Favorites
	private Drawable whiteHeart;
	private Drawable blueHeart;
	private String mediaType;
	private Button favButton;
	private Context mContext;
	private boolean mHasLoaded = false;

	// Comments
	private ImageView commentsButton;
	
	
	private LocalBroadcastManager mLocalBroadcastManager; 
	private MediaItemFavoriteStateReceiver mMediaItemFavoriteStateReceiver;
	
//	private ImageFetcher mImageFetcher = null;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// initialize components.
		mContext = getActivity().getApplicationContext();
		mDataManager = DataManager.getInstance(mContext);
		
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
		mMediaItemFavoriteStateReceiver = new MediaItemFavoriteStateReceiver(this);
		
		// gets the media item from parent.
		Bundle data = getArguments();
		mMediaItem = (MediaItem) data.getSerializable(ARGUMENT_MEDIAITEM);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView;
		if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC) {
			rootView = inflater.inflate(R.layout.fragment_media_details, container, false);
		} else {
			rootView = inflater.inflate(R.layout.fragment_media_details_video, container, false);
		}
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		// For Favorites
		whiteHeart = getResources().getDrawable(R.drawable.icon_media_details_fav_white);
		blueHeart = getResources().getDrawable(R.drawable.icon_media_details_fav_blue);

		if (!mHasLoaded) {
			if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				mDataManager.getVideoDetails(mMediaItem, this);
				mediaType = MediaContentType.VIDEO.toString();
			} else {
				mDataManager.getMediaDetails(mMediaItem, null, this);
				if (mMediaItem.getMediaType() == MediaType.TRACK) {
					mediaType = "song";
				} else {
					mediaType = mMediaItem.getMediaType().toString();
				}
			}
		} else {
			if (mMediaItem.getMediaType() == MediaType.VIDEO) {
				// For Favorites
				mediaType = MediaType.VIDEO.toString();
				// get details for video (video streaming).
				populateUserControls(video);
			} else if (mMediaItem.getMediaType() == MediaType.ALBUM
					|| mMediaItem.getMediaType() == MediaType.PLAYLIST) {
				// For favorites
				mediaType = mMediaItem.getMediaType().toString();
				// get details for albums / playlists.
				if (mMediaSetDetails != null) {
					populateUserControls(mMediaSetDetails);
				}
				setActionButtons();
			} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
				// For Favorites
				mediaType = "song";
				// get details for track (song).
				populateUserControls(mMediaTrackDetails);
				setActionButtons();
			}
		}

		if (mMediaItem.getMediaType() == MediaType.TRACK) {
			FlurryAgent.logEvent("Song detail");
		} else if (mMediaItem.getMediaType() == MediaType.ALBUM) {
			FlurryAgent.logEvent("Album detail");
		} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			FlurryAgent.logEvent("Plalist detail");
		}

		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key));
		FlurryAgent.onPageView();
		
		IntentFilter filter = new IntentFilter(ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
		mLocalBroadcastManager.registerReceiver(mMediaItemFavoriteStateReceiver, filter);
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
		
		mLocalBroadcastManager.unregisterReceiver(mMediaItemFavoriteStateReceiver);
		
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
	// Communication callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			showLoadingDialogWithoutVisibleCheck(R.string.application_dialog_loading_content);
			mHasLoaded = true;
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING) {
			showLoadingDialog(R.string.application_dialog_loading_content);
			mHasLoaded = true;
		} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
			showLoadingDialog(R.string.application_dialog_loading_content);

		} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
//			showLoadingDialog(R.string.application_dialog_loading_content);

		} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
//			showLoadingDialog(R.string.application_dialog_loading_content);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			// gets the given media item to check it's type to retrieve the
			// correct details implementation
			MediaItem mediaItem = (MediaItem) responseObjects
					.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);
			if (mediaItem.getMediaType() == MediaType.ALBUM
					|| mediaItem.getMediaType() == MediaType.PLAYLIST) {
				// get details for albums / playlists.
				mMediaSetDetails = (MediaSetDetails) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
				if (mMediaSetDetails != null) {
					populateUserControls(mMediaSetDetails);
				}
				setActionButtons();
			} else if (mediaItem.getMediaType() == MediaType.TRACK) {
				// get details for track (song).
				mMediaTrackDetails = (MediaTrackDetails) responseObjects
						.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
				populateUserControls(mMediaTrackDetails);
				setActionButtons();

				mDataManager.updateTracks(mMediaTrackDetails);

			}
			
			hideLoadingDialog();

		} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING) {
			video = (Video) responseObjects.get(VideoStreamingOperation.RESPONSE_KEY_VIDEO_STREAMING);
			populateUserControls(video);
			hideLoadingDialog();
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
			List<MediaItem> mediaItems = (List<MediaItem>) responseObjects.get(RelatedVideoOperation.RESPONSE_KEY_RELATED_VIDEO);
																((MediaDetailsActivity) getActivity()).openVideoPageTrack(mediaItems);

		} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
			BaseHungamaResponse addToFavoriteResponse = (BaseHungamaResponse) responseObjects
															.get(AddToFavoriteOperation.RESULT_KEY_ADD_TO_FAVORITE);
			
			// has the item been added from favorites.
			if (addToFavoriteResponse.getCode() == FAVORITE_SUCCESS) {
				
				int favorites = 0;
				if (mMediaItem.getMediaType() == MediaType.ALBUM || 
						mMediaItem.getMediaType() == MediaType.PLAYLIST) {
					favorites = mMediaSetDetails.getNumOfFav() + 1;
					
				} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
					favorites = mMediaTrackDetails.getNumOfFav() + 1;
				}
				
				// packs an added media item intent action.
				Intent intent = new Intent(ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
				Bundle extras = new Bundle();
				extras.putSerializable(ActionDefinition.EXTRA_MEDIA_ITEM, (Serializable) mMediaItem);
				extras.putBoolean(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE, true);
				extras.putInt(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT, favorites);
				intent.putExtras(extras);
				
				mLocalBroadcastManager.sendBroadcast(intent);
				
			} else {
				Toast.makeText(mContext, getResources().getString(R.string.favorite_error_saving, 
															mMediaItem.getTitle()), Toast.LENGTH_LONG).show();
			}

		} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
			BaseHungamaResponse removeFromFavoriteResponse = (BaseHungamaResponse) 
									responseObjects.get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);
			
			// has the item been removed from favorites.
			if (removeFromFavoriteResponse.getCode() == FAVORITE_SUCCESS) {
				Toast.makeText(mContext,getResources().getString(R.string.favorite_removed, 
															mMediaItem.getTitle()), Toast.LENGTH_LONG).show();
				
				int favorites = 0;
				if (mMediaItem.getMediaType() == MediaType.ALBUM || 
						mMediaItem.getMediaType() == MediaType.PLAYLIST) {
					favorites = mMediaSetDetails.getNumOfFav() - 1;
					
				} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
					favorites = mMediaTrackDetails.getNumOfFav() - 1;
				}
				
				// packs an added media item intent action.
				Intent intent = new Intent(ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
				Bundle extras = new Bundle();
				extras.putSerializable(ActionDefinition.EXTRA_MEDIA_ITEM, mMediaItem);
				extras.putBoolean(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE, false);
				extras.putInt(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT, favorites);
				intent.putExtras(extras);
				
				mLocalBroadcastManager.sendBroadcast(intent);
				
			} else {
				Toast.makeText(mContext, getResources().getString(R.string.favorite_error_removing, 
															mMediaItem.getTitle()), Toast.LENGTH_LONG).show();
			}
		}
		hideLoadingDialog();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
			Logger.i(TAG, "Failed loading media details");
			
			if(mContext != null && errorMessage != null){
				Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();				
			}
			
			// I do not why there is a finish action here, but i'm adding null validation so it wont crash
			FragmentActivity activity = getActivity();
			if(activity != null){
				activity.finish();
			}
			
		} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_STREAMING) {
			Logger.i(TAG, "Failed loading video streaming");
		} else if (operationId == OperationDefinition.Hungama.OperationId.VIDEO_RELATED) {
			Logger.i(TAG, "Failed loading video related");
		} else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
			Logger.i(TAG, "Failed add to favorite");

		} else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
			Logger.i(TAG, "Failed remove from favorite");
		}
		
		hideLoadingDialog();
	}

	// ======================================================
	// Private helper methods.
	// ======================================================

	private void initializeUserControls(final View rootView) {
		mMediaImage = (ImageView) rootView.findViewById(R.id.imageView_media_details);
		
		// initializes the image loader.
		int imageWidth = getResources().getDimensionPixelSize(R.dimen.media_details_left_panel_image_width);
		int imageHeight = getResources().getDimensionPixelSize(R.dimen.media_details_left_panel_image_height);
		
		// creates the cache.
//		ImageCache.ImageCacheParams cacheParams =
//                new ImageCache.ImageCacheParams(getActivity(), DataManager.FOLDER_THUMBNAILS_CACHE);
//        cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);
//		
//		mImageFetcher = new ImageFetcher(getActivity(), imageWidth, imageHeight);
//		mImageFetcher.setLoadingImage(R.drawable.background_media_details_playlist_inside_thumb);
//		mImageFetcher.addImageCache(getChildFragmentManager(), cacheParams);
//        mImageFetcher.setImageFadeIn(true);
		Picasso.with(mContext).cancelRequest(mMediaImage);
		if (mContext != null && mMediaItem != null && mMediaItem.getBigImageUrl() != null && !TextUtils.isEmpty(mMediaItem.getBigImageUrl())) {	
//			mImageFetcher.loadImage(mMediaItem.getBigImageUrl(), mMediaImage);
			Picasso.with(mContext)
					.load(mMediaItem.getBigImageUrl())
					.placeholder(R.drawable.background_media_details_playlist_inside_thumb)
					.into(mMediaImage);
		} else {
//			mImageFetcher.loadImage(mMediaTrackDetails.getBigImageUrl(), mMediaImage);
			if (mContext != null && mMediaTrackDetails != null && mMediaTrackDetails.getBigImageUrl() != null && !TextUtils.isEmpty(mMediaTrackDetails.getBigImageUrl())) {
				Picasso.with(mContext)
				.load(mMediaTrackDetails.getBigImageUrl())
				.placeholder(R.drawable.background_media_details_playlist_inside_thumb)
				.into(mMediaImage);
			}
		}

		hideAndShowPanels();
	}

	private void hideAndShowPanels() {
		RelativeLayout mAlbumPanel = (RelativeLayout) getView().findViewById(
				R.id.Relativelayout_media_details_album);
		RelativeLayout mAlbumAndPlaylistPanel = (RelativeLayout) getView()
				.findViewById(R.id.relativelayout_panel_for_album_and_playlist);
		RelativeLayout mTrackPanel = (RelativeLayout) getView().findViewById(
				R.id.media_details_mid_right_song_details);
		LinearLayout mNumPlaysPanel = (LinearLayout) getView().findViewById(
				R.id.media_details_mid_right_internal_mid);

		if (mMediaItem.getMediaType() == MediaType.ALBUM) {
			mTrackPanel.setVisibility(View.GONE);
			mAlbumPanel.setVisibility(View.VISIBLE);
			mAlbumAndPlaylistPanel.setVisibility(View.VISIBLE);
		} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			mTrackPanel.setVisibility(View.GONE);
			mAlbumPanel.setVisibility(View.GONE);
			mAlbumAndPlaylistPanel.setVisibility(View.VISIBLE);
			mNumPlaysPanel.getLayoutParams().height = (int) getResources()
					.getDimension(
							R.dimen.media_details_right_panel_num_plays_height);
			mNumPlaysPanel.requestLayout();
			mNumPlaysPanel.setGravity(Gravity.TOP);
		} else {
			mTrackPanel.setVisibility(View.VISIBLE);
			mAlbumPanel.setVisibility(View.GONE);
			mAlbumAndPlaylistPanel.setVisibility(View.GONE);
		}
	}

	private void populateUserControls(final MediaSetDetails details) {

		// initialize Texts for ALBUM page
		TextView mAlbumYear = (TextView) getView().findViewById(R.id.text_view_media_details_album_details_year);
		TextView mAlbumGenre = (TextView) getView().findViewById(R.id.text_view_media_details_album_details_genre);
		TextView mAlbumMusicBy = (TextView) getView().findViewById(R.id.text_view_media_details_album_details_music_by);
		TextView mListTitle = (TextView) getView().findViewById(R.id.text_view_media_details_list_title);
		ListView mList = (ListView) getView().findViewById(R.id.text_view_media_details_list);
		TextView mTrackNumOfPlays = (TextView) getView().findViewById(R.id.text_view_media_details_num_plays);

		if (mTrackNumOfPlays != null && details.getNumOfPlays() >= 0) {
			mTrackNumOfPlays.setText(String.valueOf(details.getNumOfPlays()));
		}
		if (mMediaItem.getMediaType() == MediaType.ALBUM) {
			if (details.getReleaseYear() != null) {
				mAlbumYear.setText(details.getReleaseYear());
			}
			
			if (details.getLanguage() != null) {
				mAlbumGenre.setText(details.getLanguage());
			}
			
			if (details.getDirector() != null) {
				mAlbumMusicBy.setText(details.getDirector());
			}
			
			if (details.getNumberOfTracks() >= 0) {
				mListTitle.setText(getResources().getString(R.string.media_details_list_title_album,
																details.getNumberOfTracks()));
			}
			
		} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			if (details.getNumberOfTracks() >= 0) {
				mListTitle.setText(getResources().getString(
						R.string.media_details_list_title_playlist,
						details.getNumberOfTracks()));
			}
		}

		TracksAdapter tracksAdapter = new TracksAdapter(details);
		mList.setAdapter(tracksAdapter);

		// Set video action button
		Button mActionButtonVideo = (Button) getView().findViewById(
				R.id.button_media_details_videos);
		if (details.isHasVideo()) {
			mActionButtonVideo.setVisibility(View.VISIBLE);
		} else {
			mActionButtonVideo.setVisibility(View.GONE);
		}

		// Set Favorite Button
		favButton = (Button) getView().findViewById(R.id.button_media_details_heart);
		favButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.isSelected()) {
					favButton.setCompoundDrawablesWithIntrinsicBounds(whiteHeart, null, null, null);
					favButton.setText(String.valueOf(details.getNumOfFav() - 1));
					mDataManager.removeFromFavorites(
							String.valueOf(mMediaItem.getId()), mediaType,
							MediaDetailsFragment.this);
				} else {
					favButton.setCompoundDrawablesWithIntrinsicBounds(blueHeart, null, null, null);
					favButton.setText(String.valueOf(details.getNumOfFav() + 1));
					mDataManager.addToFavorites(
							String.valueOf(mMediaItem.getId()), mediaType,
							MediaDetailsFragment.this);
				}

			}
		});
		if (details.getNumOfFav() >= 0) {
			favButton.setText(String.valueOf(details.getNumOfFav()));
		}
		if (details.IsFavorite()) {
			favButton.setCompoundDrawablesWithIntrinsicBounds(blueHeart, null, null, null);
			favButton.setSelected(true);
		} else {
			favButton.setCompoundDrawablesWithIntrinsicBounds(whiteHeart, null, null, null);
			favButton.setSelected(false);
		}

		// Set Comments Button
		commentsButton = (ImageView) getView().findViewById(R.id.button_media_details_comment);
		commentsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (details.getNumOfComments() >= 0) {
					((MediaDetailsActivity) getActivity()).openCommentsPage(
							mMediaItem, details.getNumOfComments());
				}
			}
		});

		initializeUserControls(getView());
	}

	private void populateUserControls(final MediaTrackDetails details) {

		// initialize Texts for TRACK page
		TextView mTrackAlbumName = (TextView) getView().findViewById(
				R.id.text_view_media_details_song_details_album_name);
		TextView mTrackYear = (TextView) getView().findViewById(
				R.id.text_view_media_details_song_details_year);
		TextView mTrackLanguage = (TextView) getView().findViewById(
				R.id.text_view_media_details_song_details_language);
		TextView mTrackMusicBy = (TextView) getView().findViewById(
				R.id.text_view_media_details_song_details_music_by);
		TextView mTrackSingerName = (TextView) getView().findViewById(
				R.id.text_view_media_details_song_details_singer_name);
		TextView mTrackLyricistName = (TextView) getView().findViewById(
				R.id.text_view_media_details_song_details_lyricist_name);
		TextView mTrackNumOfPlays = (TextView) getView().findViewById(
				R.id.text_view_media_details_num_plays);
		
		TextView mTrackMusic =  (TextView) getView().findViewById(
				R.id.text_view_media_details_song_details_music);
		
		TextView mTrackSinger = (TextView) getView().findViewById(
				R.id.text_view_media_details_song_details_singer);
		
		TextView mTrackLyricist = (TextView) getView().findViewById(
				R.id.text_view_media_details_song_details_lyricist);

		// populate TRACK page fields
		mTrackAlbumName.setText(details.getAlbumName());
		mTrackYear.setText(details.getReleaseYear());
		mTrackLanguage.setText(details.getLanguage());

		String music = details.getMusicDirector();
		if(!TextUtils.isEmpty(music)){
			mTrackMusicBy.setText(music);
		}else{
			mTrackMusicBy.setVisibility(View.GONE);
			mTrackMusic.setVisibility(View.GONE);
		}
		
		String singer = details.getSingers();
		if(!TextUtils.isEmpty(singer)){
			mTrackSingerName.setText(singer);
		}else{
			mTrackSingerName.setVisibility(View.GONE);
			mTrackSinger.setVisibility(View.GONE);
		}
		
		String lyricist = details.getLyricist();
		if(!TextUtils.isEmpty(lyricist)){
			mTrackLyricistName.setText(lyricist);
		}else{
			mTrackLyricistName.setVisibility(View.GONE);
			mTrackLyricist.setVisibility(View.GONE);
		}
		
		mTrackNumOfPlays.setText(String.valueOf(details.getNumOfPlays()));
		// Set video action button
		Button mActionButtonVideo = (Button) getView().findViewById(R.id.button_media_details_videos);
		if (details.hasVideo()) {
			mActionButtonVideo.setVisibility(View.VISIBLE);
		} else {
			mActionButtonVideo.setVisibility(View.GONE);
		}

		// Set Favorite Button
		favButton = (Button) getView().findViewById(R.id.button_media_details_heart);
		favButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.isSelected()) {
					favButton.setCompoundDrawablesWithIntrinsicBounds(whiteHeart, null, null, null);
					favButton.setText(String.valueOf(details.getNumOfFav() - 1));
					mDataManager.removeFromFavorites(String.valueOf(mMediaItem.getId()), mediaType, MediaDetailsFragment.this);
				} else {
					favButton.setCompoundDrawablesWithIntrinsicBounds(blueHeart, null, null, null);
					favButton.setText(String.valueOf(details.getNumOfFav() + 1));
					mDataManager.addToFavorites(String.valueOf(mMediaItem.getId()), mediaType, MediaDetailsFragment.this);
				}

			}
		});
		favButton.setText(String.valueOf(details.getNumOfFav()));
		if (details.IsFavorite()) {
			favButton.setCompoundDrawablesWithIntrinsicBounds(blueHeart, null,
					null, null);
			favButton.setSelected(true);
		} else {
			favButton.setCompoundDrawablesWithIntrinsicBounds(whiteHeart, null,
					null, null);
			favButton.setSelected(false);
		}

		// Set Comments Button
		commentsButton = (ImageView) getView().findViewById(
				R.id.button_media_details_comment);
		commentsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MediaDetailsActivity) getActivity()).openCommentsPage(mMediaItem, details.getNumOfComments());
			}
		});
		
		initializeUserControls(getView());
	}

	private void populateUserControls(final Video video) {

		TextView mTitleBarTextVideo = (TextView) getView().findViewById(R.id.textview_video_title);
		mTitleBarTextVideo.setText(mMediaItem.getTitle());

		TextView mTitleBarTextVideoAlbum = (TextView) getView().findViewById(R.id.textview_video_album);
		mTitleBarTextVideoAlbum.setText(mMediaItem.getAlbumName());

		// Set Favorite Button
		favButton = (Button) getView().findViewById(R.id.button_media_details_heart);
		favButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.isSelected()) {
					favButton.setCompoundDrawablesWithIntrinsicBounds(whiteHeart, null, null, null);
					favButton.setText(String.valueOf(video.getNumOfFav() - 1));
					mDataManager.removeFromFavorites(String.valueOf(mMediaItem.getId()), mediaType, MediaDetailsFragment.this);
				} else {
					favButton.setCompoundDrawablesWithIntrinsicBounds(blueHeart, null, null, null);
					favButton.setText(String.valueOf(video.getNumOfFav() + 1));
					mDataManager.addToFavorites(String.valueOf(mMediaItem.getId()), mediaType, MediaDetailsFragment.this);
				}
			}
		});

		favButton.setText(String.valueOf(video.getNumOfFav()));
		if (video.IsFavorite()) {
			favButton.setCompoundDrawablesWithIntrinsicBounds(null, blueHeart, null, null);
			favButton.setSelected(true);
		} else {
			favButton.setCompoundDrawablesWithIntrinsicBounds(null, whiteHeart, null, null);
			favButton.setSelected(false);
		}

		VideoView videoView = (VideoView) getView().findViewById(R.id.videoview_video_details);
		videoView.setVideoURI(Uri.parse(video.getVideoUrl()));
		videoView.start();
	}

	private void showOptionDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
 
			// set title
			alertDialogBuilder.setTitle("Play " + mMediaItem.getTitle());
 
			// set dialog message
			alertDialogBuilder
//				.setMessage(getResources().getString(R.string.exit_dialog_text))
				.setCancelable(true)
				.setPositiveButton(R.string.media_details_custom_dialog_long_click_add_to_queue, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, add song to queue
						Track track = new Track(mMediaItem.getId(), mMediaItem
								.getTitle(), mMediaItem.getAlbumName(), mMediaItem
								.getArtistName(), mMediaItem.getImageUrl(),
								mMediaItem.getBigImageUrl());
						List<Track> tracks = new ArrayList<Track>();
						tracks.add(track);
						if ((MediaDetailsActivity) getActivity() != null && track != null) {
							((MediaDetailsActivity) getActivity())
									.addToQueueButtonClickActivity(tracks);
						}
						
						dialog.dismiss();
					}
				  })
				.setNegativeButton(R.string.media_details_custom_dialog_long_click_play_now ,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, play song now
						Track track = new Track(mMediaItem.getId(), mMediaItem
								.getTitle(), mMediaItem.getAlbumName(), mMediaItem
								.getArtistName(), mMediaItem.getImageUrl(),
								mMediaItem.getBigImageUrl());
						List<Track> tracks = new ArrayList<Track>();
						tracks.add(track);
						if ((MediaDetailsActivity) getActivity() != null && track != null) {
							((MediaDetailsActivity) getActivity())
									.playButtonClickActivity(tracks);
						}
						dialog.dismiss();
						
					}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
	}
	
	private void setActionButtons() {
		Button mActionButtonPlay = (Button) getView().findViewById(R.id.button_media_details_play_all);
		Button mActionButtonAddToPlaylist = (Button) getView().findViewById(R.id.button_media_details_playlist);
		Button mActionButtonShare = (Button) getView().findViewById(R.id.button_media_details_share);
		Button mActionButtonVideo = (Button) getView().findViewById(R.id.button_media_details_videos);

		if (mMediaItem.getMediaType() == MediaType.ALBUM) {

			 mActionButtonPlay.setText(R.string.media_details_play_all);
			// mActionButtonShare.setVisibility(View.VISIBLE);
			// mActionButtonAddToPlaylist.setVisibility(View.VISIBLE);
			// mActionButtonAddToPlaylist.setText(R.string.media_details_add_to_playlist);

			// sets other icon to button.
			// iconDrawable =
			// getResources().getDrawable(R.drawable.icon_media_details_add_to_playlist_grey);
			// iconDrawable.setBounds(0, 0,
			// mActionButtonAddToPlaylist.getMeasuredWidth(),
			// mActionButtonAddToPlaylist.getMeasuredHeight());
			// mActionButtonAddToPlaylist.setCompoundDrawablesWithIntrinsicBounds(null,
			// iconDrawable, null, null);

		} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			 mActionButtonPlay.setText(R.string.media_details_play_all);
			// mActionButtonAddToPlaylist.setText(R.string.media_details_share);
			// iconDrawable =
			// getResources().getDrawable(R.drawable.icon_media_details_share_grey);
			// iconDrawable.setBounds(0, 0,
			// mActionButtonAddToPlaylist.getMeasuredWidth(),
			// mActionButtonAddToPlaylist.getMeasuredHeight());
			// mActionButtonAddToPlaylist.setCompoundDrawablesWithIntrinsicBounds(null,
			// iconDrawable, null, null);
			// mActionButtonShare.setVisibility(View.GONE);

		} else {
			 mActionButtonPlay.setText(R.string.media_details_play);
			// mActionButtonShare.setVisibility(View.VISIBLE);
			// mActionButtonAddToPlaylist.setVisibility(View.VISIBLE);
			// mActionButtonAddToPlaylist.setText(R.string.media_details_add_to_playlist);
			// iconDrawable =
			// getResources().getDrawable(R.drawable.icon_media_details_add_to_playlist_grey);
			// iconDrawable.setBounds(0, 0,
			// mActionButtonAddToPlaylist.getMeasuredWidth(),
			// mActionButtonAddToPlaylist.getMeasuredHeight());
			// mActionButtonAddToPlaylist.setCompoundDrawablesWithIntrinsicBounds(null,
			// iconDrawable, null, null);
		}

		mActionButtonPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mMediaItem.getMediaType() == MediaType.ALBUM
						|| mMediaItem.getMediaType() == MediaType.PLAYLIST) {
					if ((MediaDetailsActivity) getActivity() != null && mMediaSetDetails != null && mMediaSetDetails.getTracks() != null) {
						((MediaDetailsActivity) getActivity())
								.playButtonClickActivity(mMediaSetDetails
										.getTracks());
						}
				} else {
					showOptionDialog();
//					Track track = new Track(mMediaItem.getId(), mMediaItem
//							.getTitle(), mMediaItem.getAlbumName(), mMediaItem
//							.getArtistName(), mMediaItem.getImageUrl(),
//							mMediaItem.getBigImageUrl());
//					List<Track> tracks = new ArrayList<Track>();
//					tracks.add(track);
//					if ((MediaDetailsActivity) getActivity() != null && track != null) {
//						((MediaDetailsActivity) getActivity())
//								.playButtonClickActivity(tracks);
//					}
				}
			}
		});

		mActionButtonShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Prepare data for ShareDialogFragmnet
				Map<String, Object> shareData = new HashMap<String, Object>();
				shareData.put(ShareDialogFragment.TITLE_DATA, mMediaItem.getTitle());
				shareData.put(ShareDialogFragment.SUB_TITLE_DATA, mMediaItem.getAlbumName());
				shareData.put(ShareDialogFragment.THUMB_URL_DATA, mMediaItem.getBigImageUrl());
				shareData.put(ShareDialogFragment.MEDIA_TYPE_DATA, mMediaItem.getMediaType());
				shareData.put(ShareDialogFragment.TRACK_NUMBER_DATA, mMediaItem.getMusicTrackCount());
				shareData.put(ShareDialogFragment.CONTENT_ID_DATA, mMediaItem.getId());

				FragmentManager mFragmentManager = getFragmentManager();
				ShareDialogFragment shareDialogFragment = ShareDialogFragment
						.newInstance(shareData);
				shareDialogFragment.show(mFragmentManager,
						ShareDialogFragment.FRAGMENT_TAG);

			}
		});

		// mActionButtonPlayNext.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// if(mMediaItem.getMediaType() == MediaType.ALBUM ||
		// mMediaItem.getMediaType() == MediaType.PLAYLIST) {
		// ((MediaDetailsActivity)
		// getActivity()).playNextButtonClickActivity(mMediaSetDetails.getTracks());
		// } else {
		// Track track = new Track(mMediaItem.getId(), mMediaItem.getTitle(),
		// mMediaItem.getAlbumName(), mMediaItem.getArtistName(),
		// mMediaItem.getImageUrl(), mMediaItem.getBigImageUrl());
		// List<Track> tracks = new ArrayList<Track>();
		// tracks.add(track);
		// ((MediaDetailsActivity)
		// getActivity()).playNextButtonClickActivity(tracks);
		// }
		//
		// }
		// });

		// mActionButtonAddToQueue.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// if(mMediaItem.getMediaType() == MediaType.ALBUM ||
		// mMediaItem.getMediaType() == MediaType.PLAYLIST) {
		// ((MediaDetailsActivity)
		// getActivity()).addToQueueButtonClickActivity(mMediaSetDetails.getTracks());
		// } else {
		// Track track = new Track(mMediaItem.getId(), mMediaItem.getTitle(),
		// mMediaItem.getAlbumName(), mMediaItem.getArtistName(),
		// mMediaItem.getImageUrl(), mMediaItem.getBigImageUrl());
		// List<Track> tracks = new ArrayList<Track>();
		// tracks.add(track);
		// ((MediaDetailsActivity)
		// getActivity()).addToQueueButtonClickActivity(tracks);
		// }
		// }
		// });

		mActionButtonVideo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mMediaItem.getMediaType() == MediaType.ALBUM
						|| mMediaItem.getMediaType() == MediaType.PLAYLIST) {
					((MediaDetailsActivity) getActivity())
							.openVideoPage(mMediaSetDetails);
				} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
					mDataManager.getRelatedVideo(mMediaTrackDetails,
							mMediaItem, MediaDetailsFragment.this);
				}
			}
		});

		mActionButtonAddToPlaylist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mMediaItem.getMediaType() == MediaType.ALBUM
						|| mMediaItem.getMediaType() == MediaType.PLAYLIST) {
					((MediaDetailsActivity) getActivity())
							.addToPlaylistButtonClickActivity(mMediaSetDetails
									.getTracks());

					// Added by David Svilem 20/11/2012
					showPlaylistDialog(mMediaSetDetails.getTracks());
					//

				} else {
					Track track = new Track(mMediaItem.getId(), mMediaItem
							.getTitle(), mMediaItem.getAlbumName(), mMediaItem
							.getArtistName(), mMediaItem.getImageUrl(),
							mMediaItem.getBigImageUrl());
					List<Track> tracks = new ArrayList<Track>();
					tracks.add(track);
					((MediaDetailsActivity) getActivity())
							.addToPlaylistButtonClickActivity(tracks);

					// Added by David Svilem 20/11/2012
					showPlaylistDialog(tracks);
					//
				}
				
				FlurryAgent.logEvent("Add to playlist - triggered");
			}
		});

	}

	private void showPlaylistDialog(List<Track> tracks) {
		FragmentManager fm = getActivity().getSupportFragmentManager();
		boolean isFromLoadMenu = false;
		PlaylistDialogFragment editNameDialog = PlaylistDialogFragment
				.newInstance(tracks, isFromLoadMenu);
		editNameDialog.show(fm, "PlaylistDialogFragment");
	}

	private static class ViewHolder {
		LinearLayout layout;
		TextView textTrackName;
		ImageButton buttonPlay;
	}

	private class TracksAdapter extends BaseAdapter {

		private List<Track> mTracks;
		private LayoutInflater mInflater;
		MediaSetDetails mediaSetDetailsTrack;

		public TracksAdapter(MediaSetDetails mediaSetDetails) {
			mediaSetDetailsTrack = mediaSetDetails;
			mTracks = mediaSetDetails.getTracks();
			mInflater = (LayoutInflater) getActivity().getApplicationContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return mTracks.size();
		}

		@Override
		public Object getItem(int position) {
			return mTracks.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mTracks.get(position).getId();
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder viewHolder;
			// create view if not exist.
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.list_item_media_details_track, parent, false);

				viewHolder = new ViewHolder();
				viewHolder.layout = (LinearLayout) convertView
						.findViewById(R.id.media_details_track);

				viewHolder.textTrackName = (TextView) convertView
						.findViewById(R.id.media_details_track_name);
				viewHolder.buttonPlay = (ImageButton) convertView
						.findViewById(R.id.media_details_track_button_play);

				convertView.setTag(R.id.view_tag_view_holder, viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView
						.getTag(R.id.view_tag_view_holder);
			}

			// populate the view from the Track's list.
			final Track track = mTracks.get(position);
			// stores the object in the view.
			convertView.setTag(R.id.view_tag_object, track);

			viewHolder.textTrackName.setText(track.getTitle());
			viewHolder.layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {

					Track track = (Track) view.getTag(R.id.view_tag_object);
					List<Track> tracks = new ArrayList<Track>();
					tracks.add(track);
					((MediaDetailsActivity) getActivity())
							.addToQueueButtonClickActivity(tracks);

					// MediaItem trackMediaItem = new MediaItem(track.getId(),
					// track.getTitle(), track.getAlbumName(),
					// track.getArtistName(), null, null,
					// MediaType.TRACK.toString(), 0);
					//
					// trackMediaItem.setMediaContentType(MediaContentType.MUSIC);
					// trackMediaItem.setMediaType(MediaType.TRACK);
					//
					// ((MediaDetailsActivity)
					// getActivity()).openTrackPage(trackMediaItem);

				}
			});

			viewHolder.layout.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View view) {
					((MediaDetailsActivity) getActivity())
							.showLongClickDialog(track);
					return true;
				}
			});
			viewHolder.buttonPlay.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {
					Logger.v(TAG, "Play button was clicked");
					View rowView = (View) view.getParent();
					Track track = (Track) rowView.getTag(R.id.view_tag_object);
					List<Track> tracks = new ArrayList<Track>();
					tracks.add(track);
					((MediaDetailsActivity) getActivity())
							.addToQueueButtonClickActivity(tracks);

				}
			});

			viewHolder.buttonPlay
					.setOnLongClickListener(new OnLongClickListener() {

						@Override
						public boolean onLongClick(View view) {
							((MediaDetailsActivity) getActivity())
									.showLongClickDialog(track);
							return true;
						}
					});
			return convertView;
		}

	}

	
	/**
	 * Handles changes in the favorite state of Media Items, marks the button accordingly.
	 */
	private static final class MediaItemFavoriteStateReceiver extends BroadcastReceiver {
		
		private final WeakReference<MediaDetailsFragment> mediaDetailsFragmentReference;
		
		MediaItemFavoriteStateReceiver(MediaDetailsFragment mediaDetailsFragment) {
			this.mediaDetailsFragmentReference = new WeakReference<MediaDetailsFragment>(mediaDetailsFragment);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED.equalsIgnoreCase(intent.getAction())) {
				
				Bundle extras = intent.getExtras();
				MediaItem mediaItem = (MediaItem) extras.getSerializable(ActionDefinition.EXTRA_MEDIA_ITEM);
				boolean isFavorite = extras.getBoolean(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE);
				int count = extras.getInt(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT);

				MediaDetailsFragment mediaDetailsFragment = mediaDetailsFragmentReference.get();
				if (mediaDetailsFragment == null) {
					return;
				}
				
				if (mediaItem.getId() == mediaDetailsFragment.mMediaItem.getId() && 
						mediaItem.getMediaType() == mediaDetailsFragment.mMediaItem.getMediaType()) {
					
					if (mediaItem.getMediaType() == MediaType.ALBUM || 
							mediaDetailsFragment.mMediaItem.getMediaType() == MediaType.PLAYLIST) {
						if (mediaDetailsFragment.mMediaSetDetails != null) {
							mediaDetailsFragment.mMediaSetDetails.setNumOfFav(count);
						}
						
					} else if (mediaDetailsFragment.mMediaItem.getMediaType() == MediaType.TRACK) {
						mediaDetailsFragment.mMediaTrackDetails.setNumOfFav(count);
					}
					
					if (isFavorite) {
						mediaDetailsFragment.favButton.setCompoundDrawablesWithIntrinsicBounds(mediaDetailsFragment.blueHeart, null, null, null);
						mediaDetailsFragment.favButton.setSelected(true);
						mediaDetailsFragment.favButton.setText(Integer.toString(count));
						
					} else {
						mediaDetailsFragment.favButton.setCompoundDrawablesWithIntrinsicBounds(mediaDetailsFragment.whiteHeart, null, null, null);
						mediaDetailsFragment.favButton.setSelected(false);
						mediaDetailsFragment.favButton.setText(Integer.toString(count));
					}
				}
			}
		}
		
	}

	
}
