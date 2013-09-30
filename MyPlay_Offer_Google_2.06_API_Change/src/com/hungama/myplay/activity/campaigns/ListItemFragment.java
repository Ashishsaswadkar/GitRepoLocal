package com.hungama.myplay.activity.campaigns;

import java.util.ArrayList;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.campaigns.util.CacheImageLoader;
import com.hungama.myplay.activity.campaigns.util.Util;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.hungama.myplay.activity.util.images.ImageCache.ImageCacheParams;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ListItemFragment extends Fragment implements OnClickListener{

	private Node node;
	
	private View thisFragmentView;
	
	private ImageView campaignBGImage;
	private ImageView campaignThumbImage;
	private TextView campaignText1;
	private TextView campaignText2;
	private TextView campaignText3;
		
	private Context context;
	
	private Activity activity;
	
	private CacheImageLoader cacheImageLoader;
	
	private ForYouActivity forYouActivity;
	
	public static ListItemFragment newInstance(Node node) {
		ListItemFragment fragment = new ListItemFragment();
      
        Bundle args = new Bundle();
        args.putSerializable("Node", node);
        
        fragment.setArguments(args);
		
        return fragment;
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// First call
		super.onCreate(savedInstanceState);
		
		cacheImageLoader = new CacheImageLoader(getActivity().getApplicationContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Second call
		
		thisFragmentView = inflater.inflate(R.layout.listitem_fragment, container, false);
		
		thisFragmentView.setOnClickListener(this);
		campaignBGImage = (ImageView)thisFragmentView.findViewById(R.id.background_image);
		campaignThumbImage = (ImageView)thisFragmentView.findViewById(R.id.thumbnail);
		campaignText1 = (TextView)thisFragmentView.findViewById(R.id.title);
		campaignText2 = (TextView)thisFragmentView.findViewById(R.id.sub_title);
		campaignText3 = (TextView)thisFragmentView.findViewById(R.id.description);
		
//        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
//
//        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
//
//        // Set memory cache to 25% of mem class
//        cacheParams.setMemCacheSizePercent(getActivity(), 0.25f);
//
//        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
//        mImageFetcher = new ImageFetcher(getActivity(), 0);
//        mImageFetcher.setLoadingImage(R.drawable.campaign_default_image);
//        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
		
		return thisFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// Third call
		super.onActivityCreated(savedInstanceState);
		
		activity = getActivity();
		context = activity.getApplicationContext();
		
		node = (Node) getArguments().getSerializable("Node");
		
		if(node.isShowThumb() != null && node.isShowThumb() == true){
			
			String ThumbUrlSmall = node.getThumbSmall();
			//String ThumbUrlLarge = node.getThumbLarge();
			
			float scale = context.getResources().getDisplayMetrics().density;
			
			// Set the thumbnail size 73.33dp
			int thumbWidth = (int) (73.33*scale);
			int thumbHeight = (int) (73.33*scale);
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(thumbWidth, thumbHeight); 
			campaignThumbImage.setLayoutParams(params);
			
	        forYouActivity = (ForYouActivity) getActivity();
//	        forYouActivity.getImageThumbFetcher().loadImage(ThumbUrlSmall, campaignThumbImage);
	        Picasso.with(forYouActivity).cancelRequest(campaignThumbImage);
	        if (forYouActivity != null && ThumbUrlSmall != null && !TextUtils.isEmpty(ThumbUrlSmall)) {
	        	Picasso.with(forYouActivity).load(ThumbUrlSmall).placeholder(R.drawable.background_home_tile_album_default).into(campaignThumbImage);
	        }
		}else{
			// There is no thumbnail so we do not need its background
			campaignThumbImage.setVisibility(View.INVISIBLE);
		}

		if(node.isShowBGImage() != null && node.isShowBGImage() == true){
			
			String BGImageurlSmall = node.getBgImageSmall();
			//String BGImageurlMedium = node.getBgImageMedium();
			//String BGImageurlLarge = node.getBgImageLarge();
			
			RelativeLayout.LayoutParams params = new 
					RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT); 
			campaignBGImage.setLayoutParams(params);

			campaignBGImage.setScaleType(ScaleType.FIT_XY);
			
	        forYouActivity = (ForYouActivity) getActivity();
//	        forYouActivity.getImageBackgroundFetcher().loadImage(BGImageurlSmall, campaignBGImage);
	        Picasso.with(forYouActivity).cancelRequest(campaignBGImage);
	        if (forYouActivity != null && BGImageurlSmall != null && !TextUtils.isEmpty(BGImageurlSmall)) {
	        	Picasso.with(forYouActivity).load(BGImageurlSmall).into(campaignBGImage);
	        }
			
		}else{
			// Show background color (cause show_bg_img = false)
			thisFragmentView.setBackgroundColor(Color.parseColor(node.getBgColor()));
		}

		if(node.isShowText() != null && node.isShowText() == true){
			
			campaignText1.setText(node.getText1());
			campaignText1.setTextColor(Color.parseColor(node.getText1Color()));
			
			campaignText2.setText(node.getText2());
			campaignText2.setTextColor(Color.parseColor(node.getText2Color()));
			
			campaignText3.setText(node.getText3());
			campaignText3.setTextColor(Color.parseColor(node.getText3Color()));
						
		}else{
			campaignText1.setVisibility(View.INVISIBLE);
			campaignText2.setVisibility(View.INVISIBLE);
			campaignText3.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	public void onClick(View v) {
		
		Node clickedNode = node;
		
		Intent intent = new Intent(activity, ForYouActivity.class);
		
		// Save the campaign's text_1 for the header title
		intent.putExtra(ForYouActivity.CAMPAIGN_TITLE, clickedNode.getCampaignTitle());
		intent.putExtra(ForYouActivity.CAMPAIGN_ID, clickedNode.getCampaignID());
		
		if(clickedNode.getChildNodes() != null && clickedNode.getChildNodes().size() > 0){
			//Node has children
			intent.putExtra(ForYouActivity.CLICKED_NODES_CHILDS, (ArrayList<Node>)clickedNode.getChildNodes());
			startActivityForResult(intent, 0);
			
		}else{
			// Node has no children
			intent.putExtra(ForYouActivity.CLICKED_NODE, clickedNode);
			
			if(clickedNode.getAction() != null){
				startActivityForResult(intent, 0);
				
			}else{
				//Do nothing for now
			}
		}
	}
	
	@Override
	public void onResume() {
//		mImageFetcher.setExitTasksEarly(false);
		super.onResume();
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