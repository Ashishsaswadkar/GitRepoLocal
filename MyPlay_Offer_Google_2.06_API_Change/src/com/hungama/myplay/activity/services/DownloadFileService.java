package com.hungama.myplay.activity.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.util.FileUtils;
import com.hungama.myplay.activity.util.ToastExpander;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Service that takes place as track downloading queue, only one client can follow the process.
 */
public class DownloadFileService extends IntentService  {
	
	private static final String TAG = "DownloadFileService";
	
	public static final String DOWNLOAD_URL = "download_url";	
	public static final String TRACK_KEY = "track_key";

	private MediaItem mCurrentMediaItem;
	private String responseDownloadUrl;
	
	private static long mediaId = 0;
	private static String mediaTitle;
	
	private Handler mHandler;
	
	private FileUtils fileUtils;
	private File mediaFolder;
	
 	public DownloadFileService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler();
		Log.i(TAG, "Start");
		fileUtils = new FileUtils(getApplicationContext());
        
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
				
		mCurrentMediaItem = null;
		
		responseDownloadUrl = intent.getStringExtra(DOWNLOAD_URL);
		mCurrentMediaItem = (MediaItem) intent.getSerializableExtra(TRACK_KEY);
		mediaFolder = fileUtils.getStoragePath(mCurrentMediaItem.getMediaContentType());		
		String hungamaFolder = mediaFolder.getName();
//		.getResources().getString(R.string.download_media_folder);
		
		if (handleTrackDownload(mCurrentMediaItem)) {
			String successMsg = getResources().getString(R.string.download_media_succeded_toast, hungamaFolder);
			mHandler.post(new DisplayToast(successMsg));
//			Toast.makeText(getApplicationContext(), getResources().getString(R.string.download_media_succeded_toast), Toast.LENGTH_LONG).show();
		} else {
			mHandler.post(new DisplayToast(getResources().getString(R.string.download_media_unsucceded_toast)));
//			Toast.makeText(getApplicationContext(), getResources().getString(R.string.download_media_unsucceded_toast), Toast.LENGTH_LONG).show();
		}
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Stop");
	}
	
	
	private boolean handleTrackDownload(MediaItem mediaItem){
		if (mediaItem != null) {
			if (mediaItem.getId() > -1) {
				mediaId = mediaItem.getId();
			}			 
			if (mediaItem.getTitle() != null) {
				mediaTitle = mediaItem.getTitle();
			}			
		}
		boolean success = false;
		
		while(!(success = downloadTrackToCache(mediaItem))){
			
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
		
		return success;
	}
	
	private boolean downloadTrackToCache(MediaItem mediaItem) {
	    
		boolean result = false;
		
		String downloadUrl = responseDownloadUrl;
		
		Log.i(TAG, "downloadUrl for caching: " + downloadUrl);
		
		File outputFile = null;
		FileOutputStream fos = null;
		InputStream is = null;
		
	    if (downloadUrl == null) {
	    	return result;
	    }
	   
	    HttpURLConnection connection  = null;
	    
	    try {
		
		    URL ul = new URL(downloadUrl);
	        connection = (HttpURLConnection) ul.openConnection();
	        
	        connection.setConnectTimeout(5000);
	        connection.setReadTimeout(5000);
	        
	        connection.setRequestMethod("GET");
	        connection.connect();
	
//	        FileUtils fileUtils = new FileUtils(getApplicationContext());
////	        String hungamaFolder = getResources().getString(R.string.download_media_folder);
////	        File mediaFolder = fileUtils.createDirectory(hungamaFolder);
//	        File mediaFolder = fileUtils.getStoragePath();
	        if (mediaFolder != null) {
	        	String fileName;
	        	if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
	        		 fileName = mediaTitle + "_" + String.valueOf(mediaId) + ".mp4";
	        	} else {
	        		 fileName = mediaTitle + "_" + String.valueOf(mediaId) + ".mp3";
	        	}
	        	String encodedFileName = URLEncoder.encode(
	        			fileName, 
						"UTF-8");
			    outputFile = new File(mediaFolder, encodedFileName);
			    fos = new FileOutputStream(outputFile);
			    is = connection.getInputStream();
			    byte[] buffer = new byte[1024*100];
			    
			    int bytesRead = 0;		 		    		 		    
			    int totalBytesRead = 0;
			    int totalBytes = connection.getContentLength();
			    
			    while ((bytesRead = is.read(buffer)) != -1) {		    			    
			    	fos.write(buffer, 0, bytesRead);
			    	totalBytesRead += bytesRead;
			    	//Log.i(TAG, "bytesRead " + totalBytesRead + " out of " + totalBytes);
			    }		  
			    Log.i(TAG, "bytesRead " + totalBytesRead + " out of " + totalBytes);
			    fos.close();
			    is.close();
			   	
			    if(totalBytesRead < totalBytes){
			    	throw new IOException("Track " + mediaTitle + " " + mediaId + " is not completely downloaded");
			    }else{
				    result = true;				    			
				    
				    return result;
			    }
	        }
		    
        } catch (IOException e) {
        	Log.i(TAG, "IOException: " + e.getMessage());
        	e.printStackTrace();
        	        	
	    	if(connection != null){
	    		connection.disconnect();
	    	}
        	
        	if(outputFile != null){
        		outputFile.delete();
        	}
        	
        	try {
        		if(fos != null){
        			fos.close();
        		}
        		if(is != null){
        			is.close();
        		}
        		
			} catch (IOException ex) {
				Log.i(TAG, "IOException: " + ex.getMessage());
			}
        } 
	    
	    return result;
	}
	
	
	private class DisplayToast implements Runnable{
		
		String mText;
		
		public DisplayToast(String text){
			this.mText = text;
		}
		
		@Override
		public void run() {
			
			Toast toast = new Toast(getApplicationContext());
			toast = Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_LONG);
			
			ToastExpander.showFor(
					toast,
					6000);
		}
	}
}
