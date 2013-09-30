package com.hungama.myplay.activity.campaigns.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

public class CacheImageLoader { 
    
    //private CacheManager cacheManager;
    
    public CacheImageLoader(Context context) { 
    	//cacheManager = GlobalApplicationData.getCacheManager();
    } 
      
    public Drawable loadDrawable(final String imageUrl, final ImageCallback imageCallback) { 
    	
    	// TODO: remove the comment
//    	// Check if image is cached ?
//    	String fileName = getImageExtentionFromUrl(imageUrl);
//    	
//    	Boolean isExist = cacheManager.isImageExist(fileName, CacheManager.CAMPAIGNS_FOLDER_NAME);
//    	
//    	if(isExist){
//    		Bitmap b = cacheManager.getImage(fileName, CacheManager.CAMPAIGNS_FOLDER_NAME);
//    		
//    		try{
//    			if(b != null){
//        			return new BitmapDrawable(b);
//        		}
//    		}catch(OutOfMemoryError e){
//    			e.printStackTrace();
//    			return null;
//    		}
//    	}
    	        
        final Handler handler = new Handler() { 
            @Override
            public void handleMessage(Message message) { 
                imageCallback.imageLoaded((Drawable) message.obj, imageUrl); 
            } 
        }; 
        
        new Thread() { 
            @Override
            public void run() { 
                Drawable drawable = loadImageFromUrl(imageUrl);
                
                if (drawable != null)
                {
                	String fileName = getImageExtentionFromUrl(imageUrl);
                
                	Bitmap b = ((BitmapDrawable)drawable).getBitmap();
                	
                	// TODO: remove the comment
//                	try {
//						cacheManager.insertImage(fileName, CacheManager.CAMPAIGNS_FOLDER_NAME,b);
//					} catch (FileNotFoundException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
                }
                
                Message message = handler.obtainMessage(0, drawable); 
                handler.sendMessage(message); 
            } 
        }.start(); 
        
        return null; 
    } 
  
    public interface ImageCallback { 
        public void imageLoaded(Drawable imageDrawable, String imageUrl); 
    } 
    
    public static Drawable loadImageFromUrl(String url) {
    	
    	try {
			Bitmap image = getImageFromUrl(url);
			
			if (image != null) {
				return new BitmapDrawable(image);
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return null;
    	
//        InputStream inputStream; 
//        try { 
//            inputStream = new URL(url).openStream(); 
//
//            return Drawable.createFromStream(inputStream, "src");
//            
////            TypedValue typedValue = new TypedValue();
////            //density none divides by the density, density default keeps the original size
////            typedValue.density = TypedValue.DENSITY_DEFAULT;
////            
////            Drawable d = Drawable.createFromResourceStream(null, typedValue, inputStream, "src");
////            
////            return d;
//            
//        } catch (Exception e) { 
//        	
//        } 
//
//        return null;
    } 
    
    public String getImageExtentionFromUrl(String imageUrl){
    	
    	int lastIndexOfSlash = imageUrl.lastIndexOf("/");
    	String fileName = imageUrl.substring(lastIndexOfSlash + 1);
    	
    	return fileName;
    }
    
    public static Bitmap getImageFromUrl(String urlPath) throws MalformedURLException, IOException, OutOfMemoryError {
    	Bitmap image = null; 
    	HttpURLConnection connection = null;
    	InputStream input = null;

    	try {
    		connection = (HttpURLConnection)new URL(urlPath).openConnection();
    		connection.connect();
    		input = connection.getInputStream();

    		PatchInputStream patchInputStream = new PatchInputStream(input);
    		BitmapFactory.Options options = new BitmapFactory.Options();

    		image = BitmapFactory.decodeStream(patchInputStream, null, options);
    		connection.disconnect();

    	} catch (MalformedURLException e) {
    		throw new MalformedURLException();
    	} catch (OutOfMemoryError e){
    		throw new OutOfMemoryError();
    	} catch (IOException e) {
    		throw new IOException();
    	} 
    	finally {
    		if(input != null){
    			input.close();
    			input = null;
    		}

    		if (connection != null) {
    			connection.disconnect();
    			connection = null;
    		}
    	}

    	return image;
    }

    public static class PatchInputStream extends FilterInputStream {
    	public PatchInputStream(InputStream in) {
    		super(in);
    	}
    	public long skip(long n) throws IOException {
    		long m = 0L;
    		while (m < n) {
    			long _m = in.skip(n-m);
    			if (_m == 0L) break;
    			m += _m;
    		}
    		return m;
    	}
    }
}
