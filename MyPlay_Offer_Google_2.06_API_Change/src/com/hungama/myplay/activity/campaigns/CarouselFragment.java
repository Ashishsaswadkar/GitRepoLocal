package com.hungama.myplay.activity.campaigns;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.campaigns.util.CacheImageLoader;
import com.hungama.myplay.activity.campaigns.util.Util;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.EventManager;
import com.hungama.myplay.activity.data.dao.campaigns.Action;
import com.hungama.myplay.activity.data.dao.campaigns.CampaignStrings;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.data.events.CampaignPlayEvent;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.hungama.myplay.activity.util.images.ImageCache.ImageCacheParams;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AbsListView;
import android.widget.Gallery;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CarouselFragment extends Fragment implements OnItemSelectedListener{

	private List<Node> listNode;
	private Node node;
	
	private Gallery gallery;
	private ImageView imagePreview;
	private RelativeLayout textsRL;
	private RelativeLayout previewImageRL;
	
	private TextView previewText1;
	private TextView previewText2;
	private TextView previewText3;
	private Node selectedNode;
	private Context context;
	
	private ProgressBar videoLoadPB;
	private ProgressBar actionButtonPB;
	private Button videoPlayBth;
	private Button actionBth;
	private Boolean showPlayButton;
	
	private CampaignCarouselAdapter campaignCarouselAdapter;
	
	private List<Action> actions;
	
	// BitmapFun
	private static final String IMAGE_CACHE_DIR = "campaigns";
    private int mImageThumbSize;
    private ImageFetcher mImageFetcher;
	
    private DataManager mDataManager;
    
	public static CarouselFragment newInstance(List<Node> listNode, Node node) {
		CarouselFragment fragment = new CarouselFragment();
      
        Bundle args = new Bundle();
        args.putSerializable("List_Node", (Serializable) listNode);
        args.putSerializable("Node", (Serializable) node);
        
        fragment.setArguments(args);
        
        return fragment;
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// First call
		super.onCreate(savedInstanceState);
		showPlayButton = false;
		
		node = (Node) getArguments().getSerializable("Node");
		listNode =  (List<Node>) getArguments().getSerializable("List_Node");
		
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);

        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

        // Set memory cache to 25% of mem class
        cacheParams.setMemCacheSizePercent(getActivity(), 0.25f);

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.campaign_default_image);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
        
        mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Second call
		View thisFragmentView = inflater.inflate(R.layout.carousel_fragment, container, false);
		
		gallery = (Gallery)thisFragmentView.findViewById(R.id.gallery);
		
		imagePreview = (ImageView)thisFragmentView.findViewById(R.id.imagePreview); 
		imagePreview.setOnClickListener(onImagePreviewClickListener);
		
		previewText1 = (TextView)thisFragmentView.findViewById(R.id.previewText1);
		previewText2 = (TextView)thisFragmentView.findViewById(R.id.previewText2);
		previewText3 = (TextView)thisFragmentView.findViewById(R.id.previewText3);
				
		videoLoadPB = (ProgressBar)thisFragmentView.findViewById(R.id.videoLoadPB);
		videoLoadPB.setVisibility(View.INVISIBLE);
		
		actionButtonPB = (ProgressBar)thisFragmentView.findViewById(R.id.progressBar);
		actionButtonPB.setVisibility(View.INVISIBLE);
		
		videoPlayBth = (Button)thisFragmentView.findViewById(R.id.videoPlayBtn); 
		videoPlayBth.setOnClickListener(onPlayVideoBtnClickListener);
		
		actionBth = (Button) thisFragmentView.findViewById(R.id.actionButton);
		actionBth.setOnClickListener(onActionButtonClickListener);
		
		textsRL = (RelativeLayout) thisFragmentView.findViewById(R.id.textRelativeLayout);
		previewImageRL = (RelativeLayout) thisFragmentView.findViewById(R.id.previewImageRelativeLayout);

		if(!node.isShowChildMedia()){
			previewImageRL.setVisibility(View.GONE);
			imagePreview.setVisibility(View.GONE);
		}
		if(!node.isShowChildText()){
			textsRL.setVisibility(View.GONE);
		}
		
		return thisFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// Third call
		super.onActivityCreated(savedInstanceState);

		context = getActivity().getApplicationContext();
		
		campaignCarouselAdapter = new CampaignCarouselAdapter(context, (ArrayList<Node>) listNode, mImageFetcher);
		gallery.setAdapter(campaignCarouselAdapter);
		
		gallery.setOnItemSelectedListener(this);
	}
		
    final Handler handler = new Handler() { 
        @Override
        public void handleMessage(Message message) { 
        	
        	if(node.isShowChildMedia()){
        		showChildMeida();
        	}
        	if(node.isShowChildText()){
        		showChildText();
        	}
        } 
    }; 
	
	public void showChildText(){
		
		// Show the selected child node texts 1,2,3

		previewText1.setText(selectedNode.getText1());
		previewText1.setTextColor(Color.parseColor(selectedNode.getText1Color()));
		
		previewText2.setText(selectedNode.getText2());
		previewText2.setTextColor(Color.parseColor(selectedNode.getText2Color()));
		
		previewText3.setText(selectedNode.getText3());
		previewText3.setTextColor(Color.parseColor(selectedNode.getText3Color()));
		
	}
	
	public void showChildMeida(){
		String mediaUrl = null;
		
		String mediaKind = selectedNode.getMediaKind();
		if(mediaKind != null){
			if(selectedNode.getMediaKind().equals(CampaignStrings.MEDIA_KIND_VIDEO)){
				videoPlayBth.setVisibility(View.VISIBLE);
				showPlayButton = true;
				
				mediaUrl = selectedNode.getThumbLarge();
			}
			
			if(selectedNode.getMediaKind().equals(CampaignStrings.MEDIA_KIND_IMAGE)){
				
				actions = selectedNode.getActionsList();
				if(actions != null){
					actionBth.setVisibility(View.VISIBLE);
					String actionText = (String) actions.get(0).action_text;
					actionBth.setText(actionText);
				}
				
				mediaUrl = selectedNode.getMediaUrl();
			}
		}
		
		CacheImageLoader cacheImageLoader = new CacheImageLoader(context);
		
		Drawable d = cacheImageLoader.loadDrawable(mediaUrl, new CacheImageLoader.ImageCallback() {
			
			@Override
			public void imageLoaded(Drawable imageDrawable, String imageUrl) {
				if(imageDrawable != null){
					imagePreview.setLayoutParams(Util.getLayoutParams(context, imageDrawable));
					imagePreview.setImageDrawable(imageDrawable);
				}
			}
		});
		
		if(d != null){
			
			imagePreview.setLayoutParams(Util.getLayoutParams(context, d));
			imagePreview.setImageDrawable(d);
		}
	}
	
	public OnClickListener onPlayVideoBtnClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// Check the media info of the selected child node
			String mediaKind = selectedNode.getMediaKind();
			if(mediaKind != null){
				if(selectedNode.getMediaKind().equalsIgnoreCase(CampaignStrings.MEDIA_KIND_VIDEO)){
					
					videoLoadPB.setVisibility(View.VISIBLE);
					videoPlayBth.setVisibility(View.INVISIBLE);
					
					Intent intent = new Intent(context, VideoViewActivity.class);
					
					intent.putExtra(VideoViewActivity.VIDEO_URL, selectedNode.getMediaUrl());
					intent.putExtra(VideoViewActivity.CAMPAIGN_ID, selectedNode.getCampaignID());
					intent.putExtra(VideoViewActivity.CAMPAIGN_MEDIA_ID, selectedNode.getTrackingID());
					
					List<Action> actions = selectedNode.getActionsList();
					if(actions != null){
						intent.putExtra(VideoViewActivity.ACTION_TEXT, (String) actions.get(0).action_text);
						intent.putExtra(VideoViewActivity.ACTION_URI, (String) actions.get(0).action_uri);
					}
					
					startActivityForResult(intent, 0);
				}
			}
		}
	};

	public OnClickListener onActionButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			
			actionButtonPB.setVisibility(View.VISIBLE);
			actionBth.setVisibility(View.INVISIBLE);
			
			// Add Event
			int consumerId = mDataManager.getApplicationConfigurations().getConsumerID();
			String deviceId = mDataManager.getApplicationConfigurations().getDeviceID();
			String timeStamp = mDataManager.getDeviceConfigurations().getTimeStamp();
			
			CampaignPlayEvent campaignPlayEvent = 
					new CampaignPlayEvent(consumerId, 
										  deviceId, 
										  true,
										  0,timeStamp,0,0, 
										  selectedNode.getTrackingID(), 
										  Long.parseLong(selectedNode.getCampaignID()), 
										  EventManager.PLAY);
			
			mDataManager.addEvent(campaignPlayEvent);
			
			
			// Go to Web Site
			String actionUri =  (String) actions.get(0).action_uri;
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(actionUri));
			startActivity(i);
		}
	};
	
	public OnClickListener onImagePreviewClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// Check the media info of the selected child node
			if(selectedNode.getMediaKind().equalsIgnoreCase(CampaignStrings.MEDIA_KIND_IMAGE)){
				
				List<Action> actions = selectedNode.getActionsList();
				if(actions != null){
					String actionText = (String) actions.get(0).action_text;
					String actionUri =  (String) actions.get(0).action_uri;
				}
			}
		}
	};
	
	private Thread mWorker;

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		
		selectedNode = listNode.get(position);
						
		mWorker = new Thread() { 
            @Override
            public void run() { 
            	
            	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
                Message message = handler.obtainMessage(0,selectedNode); 
                handler.sendMessage(message); 
            } 
        };
        
        mWorker.start();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}

	@Override
	public void onDestroy() {
		videoLoadPB.setVisibility(View.INVISIBLE);
		mImageFetcher.closeCache();
		super.onDestroy();
	}

	@Override
	public void onResume() {
		videoLoadPB.setVisibility(View.INVISIBLE);

		if(showPlayButton){
			videoPlayBth.setVisibility(View.VISIBLE);	
		}
		if(actions != null){
			actionBth.setVisibility(View.VISIBLE);	
		}

		mImageFetcher.setExitTasksEarly(false);
		campaignCarouselAdapter.notifyDataSetChanged();

		super.onResume();
	}

	@Override
	public void onPause() {

		actionButtonPB.setVisibility(View.INVISIBLE);

		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
		super.onPause();
	}
}





