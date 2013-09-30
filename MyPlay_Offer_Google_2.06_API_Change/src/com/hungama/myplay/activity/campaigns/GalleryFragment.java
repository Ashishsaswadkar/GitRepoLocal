package com.hungama.myplay.activity.campaigns;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.campaigns.util.Util;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.EventManager;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.data.events.CampaignPlayEvent;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.hungama.myplay.activity.util.images.Utils;
import com.hungama.myplay.activity.util.images.ImageCache.ImageCacheParams;
import com.squareup.picasso.Picasso;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class GalleryFragment extends Fragment implements OnItemClickListener{

	public static final String IMAGES_URLS = "images_urls";
	public static final String CLICKED_IMAGE_POSITION = "clicked_image_position";
	
	private GridView gridView;
	private Context context;
	
	private List<Node> listNode;
	
	private CampaignGalleryAdapter campaignGalleryAdapter;
	
	private View thisFragmentView;
	
	private static int densityDpi;
	
	// BitmapFun
	private static final String IMAGE_CACHE_DIR = "campaigns";
    private int mImageThumbSize;
//    private ImageFetcher mImageFetcher;
	
    private DataManager mDataManager;
    
	public static GalleryFragment newInstance(List<Node> listNode) {
		GalleryFragment fragment = new GalleryFragment();
      
        Bundle args = new Bundle();
        args.putSerializable("List_Node", (Serializable) listNode);
        
        fragment.setArguments(args);
		
        return fragment;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		        
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);

//        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

        // Set memory cache to 25% of mem class
//        cacheParams.setMemCacheSizePercent(getActivity(), 0.25f);

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
//        mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
//        mImageFetcher.setLoadingImage(R.drawable.campaign_default_image);
//        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
        
        mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		thisFragmentView = inflater.inflate(R.layout.fragment_gallery, container, false);
		
		gridView = (GridView) thisFragmentView.findViewById(R.id.myGrid);
		
		gridView.setOnItemClickListener(this);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
//                    mImageFetcher.setPauseWork(true);
                } else {
//                    mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
            }
        });
		
		return thisFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		context = getActivity().getApplicationContext();
		
		densityDpi = Util.getDeviceDensity(context);
		
		listNode =  (List<Node>) getArguments().getSerializable("List_Node");
		
		campaignGalleryAdapter = new CampaignGalleryAdapter(context, (ArrayList<Node>) listNode);
		gridView.setAdapter(campaignGalleryAdapter);
		
	}
		
	public class CampaignGalleryAdapter extends BaseAdapter {
		
		private ArrayList<Node> nodes;
		private Context mContext;
		
        public CampaignGalleryAdapter(Context c, ArrayList<Node> nodeList) {
            mContext = c;
            nodes = new ArrayList<Node>(nodeList);
        }

        public int getCount() {
            return nodes.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
        	
            View galleryViewItem = convertView;

            Node node = nodes.get(position);
            
            //Inflate the layout
            LayoutInflater li = getActivity().getLayoutInflater();
            galleryViewItem = li.inflate(R.layout.gallery_item, null);
        	
            // Add texts
            TextView text1 = (TextView) galleryViewItem.findViewById(R.id.textView1);
            TextView text2 = (TextView) galleryViewItem.findViewById(R.id.textView2);
            TextView text3 = (TextView) galleryViewItem.findViewById(R.id.textView3);
            
            String txt = node.getText1();
            if(txt.equalsIgnoreCase("n/a") || txt.equalsIgnoreCase("") || txt.length() == 0){
            	text1.setVisibility(View.GONE);
            }else{
            	//text1.setVisibility(View.VISIBLE);
                text1.setText(node.getText1());
                text1.setTextColor(Color.parseColor(node.getText1Color()));
            }

            txt = node.getText2();
            if(txt.equalsIgnoreCase("n/a") || txt.equalsIgnoreCase("") || txt.length() == 0){
            	text2.setVisibility(View.GONE);
            }else{
            	//text2.setVisibility(View.VISIBLE);
                text2.setText(node.getText2());
                text2.setTextColor(Color.parseColor(node.getText2Color()));
            }

            txt = node.getText3();
            if(txt.equalsIgnoreCase("n/a") || txt.equalsIgnoreCase("") || txt.length() == 0){
            	//text3.setVisibility(View.GONE);
            }else{
            	text3.setVisibility(View.VISIBLE);
                text3.setText(node.getText3());
                text3.setTextColor(Color.parseColor(node.getText3Color()));
            }
            
            // Add the image
            final ImageView image = (ImageView) galleryViewItem.findViewById(R.id.gallery_item_image);
                
//            mImageFetcher.loadImage(node.getThumbSmall(), image);
            Picasso.with(mContext).cancelRequest(image);
            if (mContext != null && node.getThumbSmall() != null && !TextUtils.isEmpty(node.getThumbSmall())) {    	
            	Picasso.with(mContext).load(node.getThumbSmall()).placeholder(R.drawable.campaign_default_image).into(image); 
            }
            return galleryViewItem;
        }
        
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		
		Node clickedNode = listNode.get(position);
		
		// Add Event
		int consumerId = mDataManager.getApplicationConfigurations().getConsumerID();
		String deviceId = mDataManager.getApplicationConfigurations().getDeviceID();
		String timeStamp = mDataManager.getDeviceConfigurations().getTimeStamp();
		
		CampaignPlayEvent campaignPlayEvent = 
				new CampaignPlayEvent(consumerId, 
									  deviceId, 
									  false,
									  0,timeStamp,0,0, 
									  clickedNode.getTrackingID(), 
									  Long.parseLong(clickedNode.getCampaignID()), 
									  EventManager.CLICK);
		
		mDataManager.addEvent(campaignPlayEvent);
		
        final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
		i.putStringArrayListExtra(IMAGES_URLS, (ArrayList<String>)getUrlImagesListFromNodeList(listNode));
		i.putExtra(CLICKED_IMAGE_POSITION, position);
        
        if (Utils.hasJellyBean()) {
            // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
            // show plus the thumbnail image in GridView is cropped. so using
            // makeScaleUpAnimation() instead.
            ActivityOptions options =
                    ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
            getActivity().startActivity(i, options.toBundle());
        } else {
            startActivity(i);
        }
	}
	
	private List<String> getUrlImagesListFromNodeList(List<Node> list){
		
		List<String> imagesURLs = new ArrayList<String>();
		
		for(Node node : list){
			
			if(densityDpi == DisplayMetrics.DENSITY_LOW){
					imagesURLs.add(node.getThumbSmall());
			} else if(densityDpi == DisplayMetrics.DENSITY_MEDIUM){
					imagesURLs.add(node.getThumbSmall());
			} else if(densityDpi == DisplayMetrics.DENSITY_HIGH){
					imagesURLs.add(node.getMediaUrl());
			} else{
				imagesURLs.add(node.getMediaUrl());
			}
		}
		return imagesURLs;
	}
	
    @Override
    public void onResume() {
        super.onResume();
//        mImageFetcher.setExitTasksEarly(false);
        campaignGalleryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
//        mImageFetcher.setExitTasksEarly(true);
//        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mImageFetcher.closeCache();
    }
}
