package com.hungama.myplay.activity.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.util.Utils;


public class AppTourFragment extends Fragment {
	
	private static final String TAG = "AppTourFragment";
	
	private Drawable mPreviewImage;
	private String mTitle;
	private String mBody;
	
	private ImageView mImagePreview;
	private TextView mTextTitle;
	private TextView mTextBody;

    /**
     * Create a new instance of TestDetailsFragment, providing "num" as an argument.
     */
	public static AppTourFragment newInstance(Drawable previewImage, String title, String body) {
		
		AppTourFragment appTourFragment = new AppTourFragment();
		// sets argument to the fragment
		appTourFragment.setPreviewImage(previewImage);
		appTourFragment.setTitle(title);
		appTourFragment.setBody(body);

        return appTourFragment;
    }
	
	private void setPreviewImage(Drawable previewImage) {
		mPreviewImage = previewImage;
	}
	
	private void setTitle(String title) {
		mTitle = title;
	}

	private void setBody(String body) {
		mBody = body;
	}
	
	
	// ======================================================
	// Life cycle callbacks.
	// ======================================================
	
    /**
     * The Fragment's UI is just a simple text view showing its instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	View view;
    	
    	view = inflater.inflate(R.layout.fragment_app_tour, container, false);        
    	mImagePreview = (ImageView) view.findViewById(R.id.image_view_app_tour);
    	int width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
    	int margins = Utils.convertDPtoPX(getActivity(), 20);
    	int widthMinusMargin = width - margins ;
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(widthMinusMargin, widthMinusMargin);
    	mImagePreview.setLayoutParams(params);
    	
    	mTextTitle = (TextView) view.findViewById(R.id.app_tour_text_title);
    	mTextBody = (TextView) view.findViewById(R.id.app_tour_text_body);
            
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
    		mImagePreview.setBackground(mPreviewImage);
    	} else {
    		mImagePreview.setBackgroundDrawable(mPreviewImage);
    	}
            
    	mTextTitle.setText(mTitle);
    	mTextBody.setText(mBody);            	
        
        return view;
    }
    
}
