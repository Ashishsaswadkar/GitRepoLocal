package com.hungama.myplay.activity.operations.catchmedia;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.widget.VideoView;

//import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.saranyu.SaranyuVideo;

public class MediaHandleOperation extends CMOperation {
	
	private static final String TAG = "MediaHandleOperation";
	
	public static final int MAX_BANDWIDTH = 271;
	
	public static final String RESPONSE_KEY_HANDLE = "handle";
	public static final String RESPONSE_KEY_DELIVERY_ID = "delivery_id";
	public static final String RESPONSE_KEY_DO_NOT_CACHE = "do_not_cache";
	public static final String RESPONSE_KEY_LIMIT_DURATION = "limit_duration";
	
	private static final String KEY_CONTENT = "content";
	private static final String KEY_BITRATE = "bitrate";
	private static final String KEY_PROTOCOL = "protocol";
	private static final String KEY_FORMATS = "formats";
	private static final String KEY_MEDIA_ID = "media_id";
	private static final String KEY_MEDIA_KIND = "media_kind";

	private static final String VALUE_DEFAULT_MEDIA_KIND = MediaType.TRACK.toString().toLowerCase();
	private static final String VALUE_CONTENT = "audio/mp3";
	private static final String VALUE_PROTOCOL = "HTTP";
	
	private static final String KEY_FORMATE_MP3 = ".mp3"; 
	
	public static final String RESPONSE_KEY_FILE_SIZE = "file_size";
	
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	
	
	private final long mMediaId;

	private final Context mContext;
	
	public int getNetworkBandwidth() {
		mDataManager = DataManager.getInstance(mContext);
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		String networkType = Utils.getNetworkType(mContext);
		if (!TextUtils.isEmpty(networkType)) {
			long bandwidth = mApplicationConfigurations.getBandwidth();
 			if (networkType.equalsIgnoreCase(Utils.NETWORK_WIFI)  || networkType.equalsIgnoreCase(Utils.NETWORK_3G)) {  				 				
 				if (bandwidth == 0) {
 					Logger.i(TAG, networkType + " - First Time - No bandwidth. Bitrate should be 64");
 					return -1; //-1 = bitrate 64 					 					
 				} else {
 					Logger.i(TAG, networkType + " - Bandwidth from previous = " + bandwidth);
 					return (int) bandwidth;
 				}
	 		} else if (networkType.equalsIgnoreCase(Utils.NETWORK_2G)) {
	 			return 0; // 0 - bitrate = 32
	 		}
 		}
 		Logger.i(TAG, "Not WIFI & Not Mobile - bitrate = 32");
 		return 0; // Not WIFI & Not Mobile - bitrate = 32 
 	}
	
	public MediaHandleOperation(Context context, long mediaId) {
		super(context);
		mContext = context;
		
		mMediaId = mediaId;
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.CREATE;
	}
	
	@Override
	protected Map<String, Object> getCredentials() {
		
		Map<String, Object> credentialsMap = new HashMap<String, Object>();
		 
		credentialsMap.put(ServerConfigurations.LC_ID, pServerConfigurations.getLcId());
		credentialsMap.put(ServerConfigurations.PARTNER_ID, pServerConfigurations.getPartnerId());
		credentialsMap.put(ServerConfigurations.WEB_SERVER_VERSION, pServerConfigurations.getWebServiceVersion());
		credentialsMap.put(ServerConfigurations.APPLICATION_VERSION, pServerConfigurations.getAppVersion());
		credentialsMap.put(ServerConfigurations.APPLICATION_CODE, pServerConfigurations.getAppCode());
		credentialsMap.put(DeviceConfigurations.TIMESTAMP, pDeviceConfigurations.getTimeStamp());
		credentialsMap.put(ApplicationConfigurations.SESSION_ID, pApplicationConfigurations.getSessionID());
		
		return credentialsMap;
	}

	@Override
	public Map<String, Object> getDescriptor() {
		
		Map<String, Object> formatsMap = new HashMap<String, Object>();
		formatsMap.put(KEY_CONTENT, VALUE_CONTENT);
		formatsMap.put(KEY_PROTOCOL, VALUE_PROTOCOL);
		// sets the application configuration for the bitrate.
		// new
		
		
		//old
		int bitrateState = pApplicationConfigurations.getBitRateState();
		int bitrate = 0;
		if (bitrateState == pApplicationConfigurations.BITRATE_AUTO) {
//			SaranyuVideo s = new SaranyuVideo();		
//			int networkSpeed = s.getCurrentBandwidth();
			int networkSpeed = getNetworkBandwidth();
//			32 KBPS	 Less than 80 k 	"protocol":"RTSP", "content":"audio\/mp3","bitrate":"32"	
//			64 KBPS	 81 – 140 k 	"protocol":"RTSP", "content":"audio\/mp3","bitrate":"64"	
//			128 KBPS	 141 – 270 k 	"protocol":"RTSP", "content":"audio\/mp3","bitrate":"128"	
//			256 KBPS	270k & Above	"protocol":"RTSP", "content":"audio\/mp3","bitrate":"256"
			if (networkSpeed == -1) {
				bitrate = 64;
			} else if (networkSpeed == 0) {
				bitrate = 32;
			} else if (networkSpeed <= 80) {
				bitrate = 32;
			} else if (networkSpeed > 80 && networkSpeed <= 140) {
				bitrate = 64;
			} else if (networkSpeed > 140 && networkSpeed <= 270) {
				bitrate = 128;
			} else if (networkSpeed > 270) {
				bitrate = 256;
			}
		} else {
			bitrate = bitrateState;
		}
		
		
		formatsMap.put(KEY_BITRATE, Integer.toString(bitrate));

		Map<String, Object> descriptorMap = new HashMap<String, Object>();
		descriptorMap.put(KEY_FORMATS, formatsMap);
		descriptorMap.put(KEY_MEDIA_ID, mMediaId);
		descriptorMap.put(KEY_MEDIA_KIND, VALUE_DEFAULT_MEDIA_KIND);
		descriptorMap.put(ApplicationConfigurations.KEY_MEDIA_ID_NS, 
							ApplicationConfigurations.VALUE_MEDIA_ID_NS);
            
        return descriptorMap;	
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.MEDIA_HANDLE_CRERATE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.MEDIA_HANDLE_CRERATE;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
									InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		JSONParser jsonParser = new JSONParser();
		
		Map<String, Object> handleMap = new HashMap<String, Object>();
		
		try {
			Map<String, Object> reponseMap = (Map<String, Object>) jsonParser.parse(response);
			
			/*
			 * Sometimes the CM servers are unable to reach their partner servers to retrieve
			 * the media handle (playing URL) and retrieves some junky URL (base URL with "unable+to+connect.. " string) 
			 * Validates if the Handle URL is not empty or doesn't contain the valid playing format for the application, if soo,
			 * throws server error.  
			 */
			String mediaHandle = (String) reponseMap.get(RESPONSE_KEY_HANDLE);
			if (TextUtils.isEmpty(mediaHandle) || !mediaHandle.contains(KEY_FORMATE_MP3)) {
				throw new InvalidResponseDataException("Media Handle is not valide: " + mediaHandle);
			}
			
			handleMap.put(RESPONSE_KEY_HANDLE, mediaHandle);			
			handleMap.put(RESPONSE_KEY_DELIVERY_ID, (Long) reponseMap.get(RESPONSE_KEY_DELIVERY_ID));
			boolean doNotCache = Boolean.parseBoolean((String) reponseMap.get(RESPONSE_KEY_DO_NOT_CACHE));
			handleMap.put(RESPONSE_KEY_DO_NOT_CACHE, doNotCache);
			
			if (reponseMap.containsKey(RESPONSE_KEY_LIMIT_DURATION)) {
				int limitDuration = Integer.parseInt((String) reponseMap.get(RESPONSE_KEY_LIMIT_DURATION));
				handleMap.put(RESPONSE_KEY_LIMIT_DURATION, limitDuration);
			} else {
				handleMap.put(RESPONSE_KEY_LIMIT_DURATION, 0);
			}
			
			URL url;
			try {
				url = new URL(mediaHandle);
				URLConnection urlConnection = url.openConnection();
				urlConnection.connect();
				long file_size = urlConnection.getContentLength();	
				Logger.i("MediaHandleOperation", "File Size = " + file_size);
				handleMap.put(RESPONSE_KEY_FILE_SIZE, file_size);
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			return handleMap;
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("MediaHandle map parsing error.");
		}
	}

}
