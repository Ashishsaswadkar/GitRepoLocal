package com.hungama.myplay.activity.campaigns;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.operations.catchmedia.MediaHandleOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.ui.DownloadConnectingActivity;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PurchaseFragment extends Fragment implements
		CommunicationOperationListener, OnCompletionListener, OnErrorListener,
		OnPreparedListener, OnAudioFocusChangeListener {

	private ListView mListView;
	private ImageView mMediaArt;
	private TextView mTextViewAlbumName;
	private TextView mTextViewArtistName;

	private Node node;
	private DataManager mDataManager;
	private Context mContext;
	private ProgressDialog mProgressDialog;

	private String mType;
	private String mId;
	private String mCMServerUrl;

	private PurchaseTracksAdapter adapter;
	private List<Track> tracksItemsList;

	// Image Fetcher
//	private ImageFetcher mImageFetcher = null;

	//
	private MediaPlayer mMediaPlayer;
	private AudioManager mAudioManager;
	private boolean isPaused;
	private int playingPosition;

	public static PurchaseFragment newInstance(Node node) {
		PurchaseFragment fragment = new PurchaseFragment();

		Bundle args = new Bundle();
		args.putSerializable(ForYouActivity.NODE, node);

		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity().getApplicationContext();
		
		mDataManager = DataManager.getInstance(mContext);

		mCMServerUrl = DataManager.getInstance(getActivity())
				.getServerConfigurations().getServerUrl();

		node = (Node) getArguments().getSerializable(ForYouActivity.NODE);

		String action = node.getAction();

		URI uri = URI.create(action);

		String part = uri.getPath();
		String query = uri.getQuery();

		String[] actionProperties = part.split("/");

		mType = actionProperties[1];
		mId = actionProperties[2];

		// initializes the image loader.
		int imageSize = getResources().getDimensionPixelSize(
				R.dimen.search_result_line_image_size);

		// creates the cache.
//		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
//				getActivity(), DataManager.FOLDER_CAMPAIGNS_CACHE);
//		cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);

//		mImageFetcher = new ImageFetcher(getActivity(), imageSize);
//		mImageFetcher
//				.setLoadingImage(R.drawable.background_home_tile_album_default);
//		mImageFetcher.addImageCache(getChildFragmentManager(), cacheParams);
//		mImageFetcher.setImageFadeIn(false);

		// For AudioFocus
		mAudioManager = (AudioManager) getActivity().getSystemService(
				Context.AUDIO_SERVICE);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_purchase, container,
				false);

		// Init views
		mListView = (ListView) view
				.findViewById(R.id.campaigns_for_you_perview_album_listview);
		mMediaArt = (ImageView) view
				.findViewById(R.id.campaigns_for_you_perview_album_albumart);
		mTextViewAlbumName = (TextView) view
				.findViewById(R.id.campaigns_for_you_perview_album_album_name);
		mTextViewArtistName = (TextView) view
				.findViewById(R.id.campaigns_for_you_perview_album_artist_name);

		// Set data's views
		mTextViewAlbumName.setText(node.getText2());
//		mImageFetcher.loadImage(node.getThumbSmall(), mMediaArt);
		Picasso.with(mContext).cancelRequest(mMediaArt);
		if (mContext != null && node.getThumbSmall() != null && !TextUtils.isEmpty(node.getThumbSmall())) {
			Picasso.with(mContext)
					.load(node.getThumbSmall())
					.placeholder(R.drawable.background_home_tile_album_default)
					.into(mMediaArt);
		}
		
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (mId != null && !mId.isEmpty()) {

			long mIdLong = Long.valueOf(mId.trim());
			MediaItem mediaItem = new MediaItem(mIdLong, "", "", "", "", "",
					"", 0);
			mediaItem.setMediaType(MediaType.ALBUM);
			mediaItem.setMediaContentType(MediaContentType.MUSIC);
			mDataManager.getMediaDetails(mediaItem, null, this);

		}
	}

	@Override
	public void onStart(int operationId) {
		showLoadingDialog(getActivity().getResources().getString(
				R.string.processing));
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {

		switch (operationId) {

		case (OperationDefinition.Hungama.OperationId.MEDIA_DETAILS):

			MediaItem mediaItem = (MediaItem) responseObjects
					.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);

			if (mediaItem.getMediaType() == MediaType.ALBUM) {

				MediaSetDetails setDetails = (MediaSetDetails) responseObjects
						.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);

				tracksItemsList = setDetails.getTracks();

				adapter = new PurchaseTracksAdapter(getActivity(),
						tracksItemsList);
				mListView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}

			hideLoadingDialog();

			break;

		case (OperationDefinition.CatchMedia.OperationId.MEDIA_HANDLE_CRERATE):

			String mediaHandleUrl = (String) responseObjects
					.get(MediaHandleOperation.RESPONSE_KEY_HANDLE);

			mMediaPlayer = new MediaPlayer();
			try {
				mMediaPlayer.setDataSource(mediaHandleUrl);
				mMediaPlayer.setOnCompletionListener(this);
				mMediaPlayer.setOnErrorListener(this);
				mMediaPlayer.setOnPreparedListener(this);

				mMediaPlayer.prepare();

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			hideLoadingDialog();

			break;

		default:
			break;
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {

		switch (operationId) {
		case (OperationDefinition.Hungama.OperationId.MEDIA_DETAILS):

			if (adapter == null || adapter.isEmpty()) {
				TextView tv = new TextView(getActivity());
				tv.setText("Empty list");
				mListView.setEmptyView(tv);
			}

			hideLoadingDialog();

			break;

		case (OperationDefinition.CatchMedia.OperationId.MEDIA_HANDLE_CRERATE):

			hideLoadingDialog();

			break;

		default:
			break;
		}

	}

	// Dialog help methods
	public void showLoadingDialog(String message) {
		if (!getActivity().isFinishing()) {
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(getActivity());
				mProgressDialog = ProgressDialog.show(getActivity(), "",
						message, true, true);
			}
		}
	}

	public void hideLoadingDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	public class PurchaseTracksAdapter extends ArrayAdapter<Track> {

		private ArrayList<Track> mList;
		private Context mContext;

		private Track item;

		private int currentPos = -1;
		
		public PurchaseTracksAdapter(Context c, List<Track> list) {
			super(c, R.layout.fragment_campaigns_for_you_list_item_song);

			mContext = c;
			mList = new ArrayList<Track>(list);
		}

		public int getCount() {
			return mList.size();
		}

		public long getItemId(int position) {
			return position;
		}

		private class ViewHolder {
			TextView  songName;
			CheckedTextView playButton;
			Button getButton;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {

			final ViewHolder viewHolder;
			
			final int pos = position;
			
			if(convertView == null){
				
				//Inflate the layout
				LayoutInflater li = getActivity().getLayoutInflater();
				convertView = li.inflate(R.layout.fragment_campaigns_for_you_list_item_song, null);
				
				viewHolder = new ViewHolder();	
				
				viewHolder.getButton = (Button) convertView.findViewById(R.id.campaigns_for_you_perview_album_list_item_price);
				viewHolder.songName = (TextView) convertView.findViewById(R.id.campaigns_for_you_perview_album_list_item_song_name);
				viewHolder.playButton = (CheckedTextView) convertView.findViewById(R.id.campaigns_for_you_perview_album_list_item_play);
				
				viewHolder.playButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
					
						viewHolder.playButton.toggle();
						mListView.setItemChecked(pos, viewHolder.playButton.isChecked());
						
						handlePlayButtonClicked(viewHolder.playButton.isChecked(),pos);
						
						currentPos = pos;
						
					}
				});
				
				viewHolder.getButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
					
						Track track = mList.get(pos);
						
						// lunches the activity manages the track downloading.
						MediaItem trackMediaItem = new MediaItem(track.getId(),
								track.getTitle(), track.getAlbumName(),
								track.getArtistName(), track.getImageUrl(),
								track.getBigImageUrl(), MediaType.TRACK.toString(), 0);
						Intent intent = new Intent(mContext,
								DownloadConnectingActivity.class);
						intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
								(Serializable) trackMediaItem);
						startActivity(intent);
						
					}
				});
				
				convertView.setTag(viewHolder);
				
			}else{
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			item = mList.get(position);

			viewHolder.songName.setText(item.getTitle());
			
			if(currentPos != -1){
				if(currentPos == position){
					if(!viewHolder.playButton.isChecked()){
						viewHolder.playButton.setChecked(false);	
					}else{
						viewHolder.playButton.setChecked(true);
					}
				}else{
					viewHolder.playButton.setChecked(false);
				}
			}
			
			return convertView;
		}
		
		private void handlePlayButtonClicked(boolean value, int trackSelected){
			
			if(value){
				
				CommunicationManager communicationManager = new
						CommunicationManager();

				communicationManager.performOperationAsync(
						new CMDecoratorOperation(mCMServerUrl,
								new MediaHandleOperation(getActivity(), mList.get(trackSelected).getId())),
								PurchaseFragment.this,mContext);
				
			}else{
				
				
				
			}
			
			if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {

				mMediaPlayer.stop();
				mMediaPlayer.reset();
			}
			
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {

		switch (what) {

		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			Toast.makeText(getActivity(), "MEDIA ERROR UNKNOWN ",
					Toast.LENGTH_LONG).show();
			break;

		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			Toast.makeText(getActivity(), "MEDIA ERROR SERVER DIED",
					Toast.LENGTH_LONG).show();
			break;

		case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
			Toast.makeText(getActivity(),
					"MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK",
					Toast.LENGTH_LONG).show();
			break;

		default:
			break;
		}

		mAudioManager.abandonAudioFocus(this);

		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Toast.makeText(getActivity(), "Finish ", Toast.LENGTH_LONG).show();
		mAudioManager.abandonAudioFocus(this);
	}

	@Override
	public void onPrepared(MediaPlayer mp) {

		// Request focus
		mAudioManager.requestAudioFocus(PurchaseFragment.this,
		// Use the music stream.
				AudioManager.STREAM_MUSIC,
				// Request permanent focus.
				AudioManager.AUDIOFOCUS_GAIN);

		mMediaPlayer.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mMediaPlayer != null) {

			mAudioManager.abandonAudioFocus(PurchaseFragment.this);

			mMediaPlayer.stop();
			mMediaPlayer.reset();
			mMediaPlayer.release();
		}
	}

	@Override
	public void onAudioFocusChange(int focusChange) {

		if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {

			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				isPaused = true;
			}

		} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

			if (isPaused) {
				mMediaPlayer.start();
			}

		} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {

			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				isPaused = true;
			}

			mAudioManager.abandonAudioFocus(this);
		}
	}

}
