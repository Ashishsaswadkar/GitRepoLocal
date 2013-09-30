package com.hungama.myplay.activity.util.images;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.graphics.drawable.Drawable;

public class SingleImageLoader extends AsyncTask<String, Void, Drawable> {
	
	private OnImageLoaderStateChangedListener mOnImageLoaderStateChangedListener;
	
	public interface OnImageLoaderStateChangedListener {
		
		public void onStartLoadingImage();
		
		public void onDoneLoadingImage(Drawable drawable);
		
		public void onFailLoadingImage();
	}
	
	public void setOnImageLoaderStateChangedListener(OnImageLoaderStateChangedListener listener) {
		mOnImageLoaderStateChangedListener = listener;
	}
	
	@Override
	protected void onPreExecute() {
		if (mOnImageLoaderStateChangedListener != null) {
			mOnImageLoaderStateChangedListener.onStartLoadingImage();
		}
		super.onPreExecute();
	}
	
	@Override
	protected Drawable doInBackground(String... params) {
		// gets the URL.
		String url = params[0];
		
		// Downloads the image from the given URL.
		InputStream inputStream = null;
        try { 
            inputStream = new URL(url).openStream(); 
            return Drawable.createFromStream(inputStream, "src");           
            
        } catch (Exception e) { 
        	return null; 
        } finally {
        	if (inputStream != null) {
        		try {
					inputStream.close();
					inputStream = null;
				} catch (IOException e) {}
        	}
        }
	}
	
	@Override
	protected void onPostExecute(Drawable result) {
		if (result != null && !isCancelled()) {
			if (mOnImageLoaderStateChangedListener != null) {
				mOnImageLoaderStateChangedListener.onDoneLoadingImage(result);
			}
		} else {
			if (mOnImageLoaderStateChangedListener != null) {
				mOnImageLoaderStateChangedListener.onFailLoadingImage();
			}
		}
		super.onPostExecute(result);
	}
	
}
