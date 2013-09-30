package com.hungama.myplay.activity.campaigns;

import java.util.ArrayList;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.campaigns.util.CacheImageLoader;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class CampaignCarouselAdapter extends BaseAdapter {

    int mGalleryItemBackground;
    private Context context;
    
    private ArrayList<Node> nodes;
    
    private CacheImageLoader cacheImageLoader;
    
    public LayoutInflater inflater;
    
    private Drawable draw;
    
    private ImageFetcher mImageFetcher;
    
    public CampaignCarouselAdapter(Context c, ArrayList<Node> nodeList, ImageFetcher mImageFetcher) {
    	
        context = c;
        nodes = new ArrayList<Node>(nodeList);
        
        cacheImageLoader = new CacheImageLoader(context);
        
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        draw = c.getResources().getDrawable(R.drawable.default_image);
        
//        this.mImageFetcher = mImageFetcher;
    }

    public int getCount() {
        return nodes.size();
    }
    
    public int addItem(Node item) {
    	nodes.add(item);
    	return nodes.indexOf(item);
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
    	    	    	
    	final ViewHolder viewHolder;
    	
    	if(convertView == null){
    		
    		viewHolder = new ViewHolder();
    		
    		convertView  = (RelativeLayout) inflater.inflate(R.layout.carousel_row, parent, false);
    		viewHolder.image = (ImageView)convertView.findViewById(R.id.carouselImage); 
    		viewHolder.image.setImageDrawable(draw);
    		
    		convertView.setTag(viewHolder);
    		
    	}else{
    		viewHolder = (ViewHolder)convertView.getTag();
    	}
    	    	
//    	mImageFetcher.loadImage(nodes.get(position).getThumbSmall(), viewHolder.image);
    	Picasso.with(context).cancelRequest(viewHolder.image);
    	if (context != null && nodes.get(position).getThumbSmall() != null && !TextUtils.isEmpty(nodes.get(position).getThumbSmall())) {    	
    		Picasso.with(context).load(nodes.get(position).getThumbSmall()).into(viewHolder.image);	    	
    	}
		return convertView;
    }
    
	public static class ViewHolder{
		ImageView image;
	}
}
