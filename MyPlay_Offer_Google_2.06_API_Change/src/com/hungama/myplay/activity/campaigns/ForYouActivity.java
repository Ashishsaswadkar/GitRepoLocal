package com.hungama.myplay.activity.campaigns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.EventManager;
import com.hungama.myplay.activity.data.dao.campaigns.Action;
import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.data.dao.campaigns.CampaignStrings;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.data.events.CampaignPlayEvent;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CampaignCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.CampaignListCreateOperation;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

public class ForYouActivity extends SherlockFragmentActivity implements CommunicationOperationListener{
	
	public static final String CLICKED_NODES_CHILDS = "Clicked_Node's_Childs";
	public static final String CLICKED_NODE = "Clicked_Node";
	public static final String CAMPAIGN_TITLE = "Campaign_Title";
	public static final String CAMPAIGN_ID = "Campaign_Id";
	public static final String ACTION_TYPE_CAMPAIGN = "campaign";
	public static final String PLACEMENT_TYPE_RADIO = "radio";
	public static final String PLACEMENT_TYPE_SPLASH = "splash";
	public static final String NODE = "node";

	
	private TextView campaignTitle;
	private List<Node> nodes;
	
	private boolean isNodeLevel = false;
	
	private boolean showWhiteDivider = false;
	
	private DataManager mDataManager;
	
	private Node leafNode = null;
	
	private ProgressDialog mProgressDialog;
	
	private ImageFetcher mImageThumbFetcher = null;
	private ImageFetcher mImageBackgroundFetcher = null;
	
	private String campaignId;
	
	private ActionBar mActionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_foryou);
		
		mActionBar = getSupportActionBar();
		mActionBar.setIcon(R.drawable.icon_actionbar_logo);
		mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_actionbar));
		
		mDataManager = DataManager.getInstance(getApplicationContext());
		
		// Get the list of Node 
		Bundle bundle = getIntent().getExtras();
		
		if(bundle == null){
						
			mActionBar.setTitle(getString(R.string.for_you_title));
			
			isNodeLevel = false;
			
			showWhiteDivider = true;
			
			nodes = new ArrayList<Node>();
			
			List<Campaign> campaigns = mDataManager.getStoredCampaign();
			
			if(campaigns != null && !campaigns.isEmpty()){
				
				buildNodesListFromCampaigns(campaigns);
				
				showCampaigns();
				
			}else{
				mDataManager.getCampignsList(this);				
			}
			
		}else{
			
		    mActionBar.setTitle(bundle.getString(CAMPAIGN_TITLE));
			
			campaignId = bundle.getString(CAMPAIGN_ID);
			
			nodes = (List<Node>) bundle.getSerializable(CLICKED_NODES_CHILDS);
			
			leafNode = (Node) bundle.getSerializable(CLICKED_NODE); 
			
			isNodeLevel = true;
			
			showWhiteDivider = false;
			
			showCampaigns();
		}
		
		// Create ImageFetcher for all ListItemFragment's thumbnails
		ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, DataManager.FOLDER_CAMPAIGNS_CACHE);
        cacheParams.setMemCacheSizePercent(this, 0.10f);
        
        mImageThumbFetcher = new ImageFetcher(this, 0);
        mImageThumbFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
        mImageThumbFetcher.setLoadingImage(R.drawable.background_home_tile_album_default);
        mImageThumbFetcher.setImageFadeIn(false);	
        
		mImageBackgroundFetcher = new ImageFetcher(this, 0);
		mImageBackgroundFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		mImageBackgroundFetcher.setImageFadeIn(false);
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		FlurryAgent.onPageView();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		FlurryAgent.onEndSession(this);
	}
	
	public ImageFetcher getImageThumbFetcher(){
		return mImageThumbFetcher;
	}
	
	public ImageFetcher getImageBackgroundFetcher(){
		return mImageBackgroundFetcher;
	}
	
	public void showCampaigns(){
		
		if(nodes != null && nodes.size() > 0){
			// List of Nodes
			showNodesList(nodes);
			
		}else if(leafNode != null){
			// Check if media_kink is: Video / Image / Track
			String mediaKind = leafNode.getMediaKind();
			
			if(mediaKind != null){
				if(mediaKind.equalsIgnoreCase(CampaignStrings.MEDIA_KIND_VIDEO)){
					
					// Video
					Intent intent = new Intent(this, VideoViewActivity.class);
					
					intent.putExtra("Video_Url", leafNode.getMediaUrl());
					intent.putExtra("Campaign_Id", leafNode.getCampaignID());
					
					List<Action> actions = leafNode.getActionsList();
					
					if(actions != null){
						intent.putExtra("Action_Text", (String) actions.get(0).action_text);
						intent.putExtra("Action_Uri", (String) actions.get(0).action_uri);
					}
					
					// Add Event
					int consumerId = mDataManager.getApplicationConfigurations().getConsumerID();
					String deviceId = mDataManager.getApplicationConfigurations().getDeviceID();
					String timeStamp = mDataManager.getDeviceConfigurations().getTimeStamp();
					
					CampaignPlayEvent campaignPlayEvent = 
							new CampaignPlayEvent(consumerId, 
												  deviceId, 
												  false,
												  0,timeStamp,0,0, 
												  leafNode.getTrackingID(), 
												  Long.parseLong(leafNode.getCampaignID()), 
												  EventManager.CLICK);
					
					mDataManager.addEvent(campaignPlayEvent);
					
					intent.putExtra("Campaign_Media_Id", leafNode.getTrackingID());
					startActivityForResult(intent, 0);
					finish();
					
				}else if(mediaKind.equalsIgnoreCase(CampaignStrings.MEDIA_KIND_IMAGE)){
					
					// Image

					// Add Event
					int consumerId = mDataManager.getApplicationConfigurations().getConsumerID();
					String deviceId = mDataManager.getApplicationConfigurations().getDeviceID();
					String timeStamp = mDataManager.getDeviceConfigurations().getTimeStamp();
					
					CampaignPlayEvent campaignPlayEvent = 
							new CampaignPlayEvent(consumerId, 
												  deviceId, 
												  false,
												  0,timeStamp,0,0, 
												  leafNode.getTrackingID(), 
												  Long.parseLong(leafNode.getCampaignID()), 
												  EventManager.CLICK);
					
					mDataManager.addEvent(campaignPlayEvent);
					
					List<String> url = new ArrayList<String>();
					url.add(leafNode.getMediaUrl());
					
			        final Intent i = new Intent(this, ImageDetailActivity.class);
					i.putStringArrayListExtra(GalleryFragment.IMAGES_URLS,(ArrayList<String>) url);
					i.putExtra(GalleryFragment.CLICKED_IMAGE_POSITION, 0);
					
					startActivityForResult(i, 0);
					finish();
					
				}else if(mediaKind.equalsIgnoreCase(CampaignStrings.MEDIA_KIND_TRACK)){
					
					// Add Event
					int consumerId = mDataManager.getApplicationConfigurations().getConsumerID();
					String deviceId = mDataManager.getApplicationConfigurations().getDeviceID();
					String timeStamp = mDataManager.getDeviceConfigurations().getTimeStamp();
					
					CampaignPlayEvent campaignPlayEvent = 
							new CampaignPlayEvent(consumerId, 
												  deviceId, 
												  false,
												  0,timeStamp,0,0, 
												  leafNode.getTrackingID(), 
												  Long.parseLong(leafNode.getCampaignID()), 
												  EventManager.CLICK);
					
					mDataManager.addEvent(campaignPlayEvent);
					
				}
			}
			
			String action = leafNode.getAction();
			if(action != null && 
					(action.contains(CampaignStrings.ACTION_HTTP_URL) || 
					 action.contains(CampaignStrings.ACTION_HTTPS_URL))) {
				
				// Add Event
				int consumerId = mDataManager.getApplicationConfigurations().getConsumerID();
				String deviceId = mDataManager.getApplicationConfigurations().getDeviceID();
				String timeStamp = mDataManager.getDeviceConfigurations().getTimeStamp();
				
				CampaignPlayEvent campaignPlayEvent = 
						new CampaignPlayEvent(consumerId, 
											  deviceId, 
											  false,
											  0,timeStamp,0,0, 
											  leafNode.getTrackingID(), 
											  Long.parseLong(leafNode.getCampaignID()), 
											  EventManager.CLICK);
				
				mDataManager.addEvent(campaignPlayEvent);
				
				// Go to Web Site
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(action));
				startActivity(i);
				finish();
				
			}else if(action != null && action.contains(CampaignStrings.ACTION_PURCHASE)){
		     
				PurchaseFragment purchaseFragment = PurchaseFragment.newInstance(leafNode);
				FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		        
		        fragmentTransaction.add(R.id.static_header, purchaseFragment);
		        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		        fragmentTransaction.addToBackStack(null);
		        fragmentTransaction.commit();
				
			}
		}
	}
	
	public void showNodesList(List<Node> nodes){
		
		for(Node node : nodes){
			
			if(node.getWidgetType() != null && node.getWidgetType().equalsIgnoreCase(CampaignStrings.WIDGET_TYPE_LIST_ITEM)){
				
				// Add Event
				int consumerId = mDataManager.getApplicationConfigurations().getConsumerID();
				String deviceId = mDataManager.getApplicationConfigurations().getDeviceID();
				String timeStamp = mDataManager.getDeviceConfigurations().getTimeStamp();
				
				CampaignPlayEvent campaignPlayEvent = 
						new CampaignPlayEvent(consumerId, 
											  deviceId, 
											  false,
											  0,timeStamp,0,0, 
											  node.getTrackingID(), 
											  Long.parseLong(node.getCampaignID()), 
											  EventManager.VIEW);
				
				mDataManager.addEvent(campaignPlayEvent);
				
				Fragment listitemFragment = ListItemFragment.newInstance(node);
				FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
				
				DividerFragment df = new DividerFragment();
				
				// If the Node is static then it should be out from the Scroll View.
				if(node.isStatic()){
					trans.add(R.id.static_header, listitemFragment);
					
					if(showWhiteDivider){
						trans.add(R.id.static_header, df);	
					}
					
				}else{
					trans.add(R.id.header, listitemFragment);
					
					if(showWhiteDivider){
					    trans.add(R.id.header, df);						
					}
				}
		        
		        trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		        trans.addToBackStack(null);
		        
		        trans.commit();
		        
			}else if(node.getWidgetType() != null && node.getWidgetType().equalsIgnoreCase(CampaignStrings.WIDGET_TYPE_CAROUSEL)){
								
				// Add Event
				int consumerId = mDataManager.getApplicationConfigurations().getConsumerID();
				String deviceId = mDataManager.getApplicationConfigurations().getDeviceID();
				String timeStamp = mDataManager.getDeviceConfigurations().getTimeStamp();
				
				CampaignPlayEvent campaignPlayEvent = 
						new CampaignPlayEvent(consumerId, 
											  deviceId, 
											  false,
											  0,timeStamp,0,0, 
											  node.getTrackingID(), 
											  Long.parseLong(node.getCampaignID()), 
											  EventManager.VIEW);
				
				mDataManager.addEvent(campaignPlayEvent);
				
				CarouselFragment carouselFragment = CarouselFragment.newInstance(node.getChildNodes(), node);
				FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		        
		        fragmentTransaction.add(R.id.header, carouselFragment);
		        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		        fragmentTransaction.addToBackStack(null);
		        fragmentTransaction.commit();
				
			}else if(node.getWidgetType() != null && node.getWidgetType().equalsIgnoreCase(CampaignStrings.WIDGET_TYPE_GALLERY)){
							
				// Add Event
				int consumerId = mDataManager.getApplicationConfigurations().getConsumerID();
				String deviceId = mDataManager.getApplicationConfigurations().getDeviceID();
				String timeStamp = mDataManager.getDeviceConfigurations().getTimeStamp();
				
				CampaignPlayEvent campaignPlayEvent = 
						new CampaignPlayEvent(consumerId, 
											  deviceId, 
											  false,
											  0,timeStamp,0,0, 
											  node.getTrackingID(), 
											  Long.parseLong(node.getCampaignID()), 
											  EventManager.VIEW);
				
				mDataManager.addEvent(campaignPlayEvent);
				
				GalleryFragment galleryFragment = GalleryFragment.newInstance(node.getChildNodes());
				FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		        
		        fragmentTransaction.add(R.id.static_header, galleryFragment);
		        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		        fragmentTransaction.addToBackStack(null);
		        fragmentTransaction.commit();
				
			}else{
				
			}
		}
	}
		
	public boolean isNodeLevel(){
		// Call this function in order to know if to display Share (Campaign) option
		return this.isNodeLevel;
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		
		switch (operationId) {
		case (OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ):

			List<String> campaignList = (List<String>) responseObjects.get(
					CampaignListCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN_LIST);
		
			mDataManager.getCampigns(this, campaignList);
			
			break;
			
		case (OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ):
			
			List<Campaign> campaigns = (List<Campaign>) responseObjects.get(
					CampaignCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN);
					
			buildNodesListFromCampaigns(campaigns);
			
			showCampaigns();
			
			hideLoadingDialog();
			
			break;

		default:
			break;
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		hideLoadingDialog();
	}
	
	@Override
	public void onStart(int operationId) {
		showLoadingDialog("Loading Campaigns");
	}

	private void buildNodesListFromCampaigns(List<Campaign> list){
		
		if(list != null && !list.isEmpty()){
			for(Campaign c : list){
				nodes.add(c.getNode());
			}
		}
		
	}
	
	@Override
	public void onBackPressed() {
	
		FragmentManager fm = getSupportFragmentManager();
		for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {    
		    fm.popBackStack();
		}
		super.onBackPressed();
	}
	
	public void showLoadingDialog(String message) {
		if (!isFinishing()) {
    		if (mProgressDialog == null) {
    			mProgressDialog = new ProgressDialog(this);
    			mProgressDialog = ProgressDialog.show(this, "", message, true, true);
    		}
    	}
	}
	
	public void hideLoadingDialog() {
		if (mProgressDialog != null) {
    		mProgressDialog.dismiss();
    		mProgressDialog = null;
		}
	}
}










