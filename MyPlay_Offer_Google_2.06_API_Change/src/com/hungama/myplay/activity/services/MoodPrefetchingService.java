package com.hungama.myplay.activity.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.operations.hungama.DiscoverOptionsOperation;
import com.hungama.myplay.activity.operations.hungama.HungamaWrapperOperation;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.images.DiskLruCache;

/**
 * Background service for prefetching moods and thier images in the application's internal storage for late use.
 */
public class MoodPrefetchingService extends IntentService {
	
	private static final String TAG = "MoodPrefetchingService";
	
	public static final String INTENT_ACTION_PREFETCHING_MOODS_SYNC_EVENT = "com.hungama.myplay.activity.intents.INTENT_ACTION_PREFETCHING_MOODS_SYNC_EVENT";
	public static final String EXTRA_PREFETCHING_MOODS_SYNC_SUCCESS_FLAG = "com.hungama.myplay.activity.intents.EXTRA_PREFETCHING_MOODS_SYNC_SUCCESS_FLAG";
	public static final String EXTRA_PREFETCHING_MOODS_SYNC_STATE_RUNNING = "com.hungama.myplay.activity.intents.EXTRA_PREFETCHING_MOODS_SYNC_STATE_RUNNING";
	
	private DataManager mDataManager;
	
	private DiskLruCache mDiskLruCache;
	private File mImagesFile;
	
	private static final int IO_BUFFER_SIZE = 8 * 1024;
	
	private String mServiceUrl;
	private String mAuthKey;
	
	public MoodPrefetchingService() {
		super(TAG);
	}

	public MoodPrefetchingService(String name) {
		super(TAG);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mDataManager = DataManager.getInstance(getApplicationContext());
		ServerConfigurations serverConfigurations = mDataManager.getServerConfigurations();
		
		mImagesFile = getDir(DataManager.FOLDER_MOODS_IMAGES, Context.MODE_PRIVATE);
		
		mServiceUrl = serverConfigurations.getHungamaServerUrl();
		mAuthKey = serverConfigurations.getHungamaAuthKey();
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		// removes any existing state of the intent.
		Intent prefetchingIntentSyncEvent = new Intent(INTENT_ACTION_PREFETCHING_MOODS_SYNC_EVENT);
		removeStickyBroadcast(prefetchingIntentSyncEvent);
		
		// resets the intent and broadcasts it again.
		prefetchingIntentSyncEvent.putExtra(EXTRA_PREFETCHING_MOODS_SYNC_SUCCESS_FLAG, false);
		prefetchingIntentSyncEvent.putExtra(EXTRA_PREFETCHING_MOODS_SYNC_STATE_RUNNING, true);
		
		sendStickyBroadcast(prefetchingIntentSyncEvent);
		
		
		boolean hasSuccess = false;
		
		Logger.d(TAG, "Starts prefetching moods.");
		// gets the moods from the servers.
		CommunicationManager communicationManager = new CommunicationManager();
		try {
			// stores the discover preferences. 
			DiscoverOptionsOperation discoverOptionsOperation = new DiscoverOptionsOperation(mServiceUrl, mAuthKey);
			Map<String, Object> resultMoodsMap = 
					communicationManager.performOperation(new HungamaWrapperOperation(null, getApplicationContext(), discoverOptionsOperation),getApplicationContext());
			List<Mood> moods = (List<Mood>) resultMoodsMap.get(DiscoverOptionsOperation.RESULT_KEY_OBJECT_MOODS);
			// stores the objects in an internal file dir.
			mDataManager.storeMoods(moods);
			
			// deletes any existing images
			mDiskLruCache = DiskLruCache.open(mImagesFile, 1, 1, DataManager.CACHE_SIZE_MOODS_IMAGES);
			mDiskLruCache.delete();
			
			// for each mood.
			for (Mood mood : moods) {
				// downloads the images.
				// stores in the internal dir.
				if (!TextUtils.isEmpty(mood.getBigImageUrl())) {
					downloadBitmapToInternalStorage(mood.getBigImageUrl());
				}
				if (!TextUtils.isEmpty(mood.getBigImageUrl())) {
					downloadBitmapToInternalStorage(mood.getSmallImageUrl());
				}
			}
			
			Logger.d(TAG, "Done prefetching moods.");
			hasSuccess = true;
			
			// updates the preferences.
			mDataManager.getApplicationConfigurations().setHasSuccessedPrefetchingMoods(hasSuccess);
			
		} catch (InvalidRequestException e) { e.printStackTrace(); Logger.e(TAG, "Failed to prefetch moods.");
		} catch (InvalidResponseDataException e) { e.printStackTrace(); Logger.e(TAG, "Failed to prefetch moods.");
		} catch (OperationCancelledException e) { e.printStackTrace(); Logger.e(TAG, "Failed to prefetch moods.");
		} catch (NoConnectivityException e) { e.printStackTrace(); Logger.e(TAG, "Failed to prefetch moods."); 
		} catch (IOException e) { e.printStackTrace(); Logger.e(TAG, "Failed to create / delete cache."); }
		
		// indicates for the success / failure.
		prefetchingIntentSyncEvent.putExtra(EXTRA_PREFETCHING_MOODS_SYNC_SUCCESS_FLAG, hasSuccess);
		// indicates it's not running anymore.
		prefetchingIntentSyncEvent.putExtra(EXTRA_PREFETCHING_MOODS_SYNC_STATE_RUNNING, false);
		// bang!
		sendStickyBroadcast(prefetchingIntentSyncEvent);
	}
	
	/**
	 * Downloads the images to their directory.
	 */
	private void downloadBitmapToInternalStorage(String urlString) throws IOException {
		
		final File cacheFile = new File(mDiskLruCache.createFilePath(urlString));

        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            final InputStream in =
                    new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(new FileOutputStream(cacheFile), IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
        } catch (final IOException e) {
            Log.e(TAG, "Error in downloadBitmap - " + e);
            throw new IOException("Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error in downloadBitmap - " + e);
                }
            }
        }
    }

	
}
