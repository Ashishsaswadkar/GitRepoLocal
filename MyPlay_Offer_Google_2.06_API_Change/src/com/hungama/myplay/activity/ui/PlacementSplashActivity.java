package com.hungama.myplay.activity.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.campaigns.ForYouActivity;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.campaigns.Action;
import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

/**
 * @author DavidSvilem
 *
 */
public class PlacementSplashActivity extends FragmentActivity implements OnClickListener{
	
	// Managers
	private DataManager mDataManager;
	
	// Views
	private ImageView splashImage;
	private LinearLayout buttonsLayout;
	private ProgressBar splashImageProgress;
	
	private Button button;
	
	// Data Members
	private List<Placement> splashPlacements;
	private Placement splashPlacement;
	
	// 
//	private ImageFetcher mImageFetcher = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_placement_splash);
		
		initViews();
		
		mDataManager = DataManager.getInstance(this);
		splashPlacements = mDataManager.getStoredSplashPlacement();
		
		if(splashPlacements != null && !splashPlacements.isEmpty()){
			
			// Get randomly one of the splash placement from the list 
        	Random myRandom = new Random();
        	int listSize = splashPlacements.size();
        	      	
        	int randomSplashPlacement = (Math.abs(myRandom.nextInt()) % (listSize));
        	splashPlacement = splashPlacements.get(randomSplashPlacement);
        	
    		/*
    		 * Loads the image for the campaigns.
    		 */
//    		ImageCache.ImageCacheParams cacheParams =
//                    new ImageCache.ImageCacheParams(this, DataManager.FOLDER_CAMPAIGNS_CACHE);
//            cacheParams.setMemCacheSizePercent(this, 0.10f);
//            
//    		mImageFetcher = new ImageFetcher(this, 0);
//    		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
//    		mImageFetcher.setLoadingImage(R.drawable.background_home_tile_album_default);
//            mImageFetcher.setImageFadeIn(false);
//            
//            mImageFetcher.loadImage(splashPlacement.getBgImageSmall(), splashImage);
        	Picasso.with(this).cancelRequest(splashImage);
        	if (this != null && splashPlacement.getBgImageSmall() != null && !TextUtils.isEmpty(splashPlacement.getBgImageSmall())) {
				Picasso.with(this)
						.load(splashPlacement.getBgImageSmall())
						.placeholder(R.drawable.background_home_tile_album_default)
						.into(splashImage);
        	}
            createButtonsLayout(splashPlacement);
		}
	}
	
	private void initViews(){
		splashImage = (ImageView) findViewById(R.id.splash_image);
		buttonsLayout = (LinearLayout) findViewById(R.id.buttons_layout);
		splashImageProgress = (ProgressBar) findViewById(R.id.splash_image_progress);
		splashImageProgress.setVisibility(View.INVISIBLE);
	}

	private void createButtonsLayout(Placement placementSplash) {
		
	 	List<Action> actions = splashPlacement.getActions();
	 
        int id = actions.size();
        
        int heigh = (int) getResources().getDimension(R.dimen.splash_campaigns_buttons_layout_heigh);
        float keywordTextSize = 
    			(float) getResources().getDimension(R.dimen.splash_campaigns_buttons_layout_text);
        
        for(Action action : actions){
        	
        	button = new Button(this);
        	button.setId(id--);
        	button.setText(action.action_text);
        	button.setTag(action.action_uri);
        	
        	button.setTextColor(getResources().getColorStateList(R.color.main_search_popular_keyword_text));
        	button.setBackgroundResource(R.drawable.background_search_keywords);
        	
        	button.setTextSize(keywordTextSize);
        	
        	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,heigh);
        	params.setMargins(0, 0, (int) getResources().getDimension(R.dimen.splash_campaigns_buttons_layout_padding_right), 0);
        	button.setLayoutParams(params);
        	
        	button.setContentDescription(action.tracking_id);
        	
        	button.setOnClickListener(this);
        	buttonsLayout.addView(button);
        }
	}

	@Override
	public void onClick(View v) {
		
		Button button = (Button)v;
		
		String action = (String)button.getTag(); 
		
		if(action != null){
		
			URI uri = URI.create(action);
			
			String scheme = uri.getScheme();
			String authotiry = uri.getAuthority();
			String part = uri.getPath();
			
			if(scheme == null){
				// Go to Web Site
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"+uri.toString()));
				startActivity(i);
				
			}else if(scheme.equalsIgnoreCase("http")){
				
				returnToCallingActivity();
				
				// Go to Web Site
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()));
				startActivity(i);
				
			}else if(scheme.equalsIgnoreCase("app")){
				
				if(authotiry.equalsIgnoreCase("skip")){
					// Skip Campaign
					returnToCallingActivity();
				
				}else if(authotiry.equalsIgnoreCase("campaign")){
					
					// Go To Campaign
					List<Campaign> campaigns = mDataManager.getStoredCampaign();
					Node clickedNode = CampaignsManager.findCampaignRootNodeByID(campaigns, splashPlacement.getCampaignID());
					
					Intent intent = new Intent(this, ForYouActivity.class);
					
					// Save the campaign's text_1 for the header title
					intent.putExtra(ForYouActivity.CAMPAIGN_TITLE, clickedNode.getCampaignTitle());
					
					if(clickedNode.getChildNodes() != null && clickedNode.getChildNodes().size() > 0){
						
						returnToCallingActivity();
						
						//Node has children
						intent.putExtra(ForYouActivity.CLICKED_NODES_CHILDS, (ArrayList<Node>)clickedNode.getChildNodes());
						startActivity(intent);
						
					}else{
						// Node has no children
						intent.putExtra(ForYouActivity.CLICKED_NODE, clickedNode);
						
						if(clickedNode.getAction() != null){
							
							returnToCallingActivity();
							
							Toast.makeText(this, "Action: "+clickedNode.getAction(), Toast.LENGTH_LONG).show();
							startActivity(intent);
							
						}else{
							// Do nothing for now
						}
					}
				}
			}
		}
	}
	
	
	@Override
	public void onBackPressed() {
		// This Activity does not need Back button
	}
	
	/**
	 * return's to the calling activity with RESULT_OK flag.
	 */
	private void returnToCallingActivity() {
		setResult(RESULT_OK);
		finish();
	}
}
