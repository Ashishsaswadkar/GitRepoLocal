package com.hungama.myplay.activity.data.configurations;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class DeviceConfigurations {
	
	private Context mContext;
	
	public DeviceConfigurations(Context context) {
		mContext = context;
	}

	public static final String TIMESTAMP = "timestamp";
	public static final String DEVICE_MODEL_NAME = "device_model_name";
	public static final String HARDWARE_ID = "hardware_id";
	public static final String HARDWARE_ID_TYPE = "hardware_id_type";
	public static final String DEVICE_OS = "device_os";
	public static final String DEVICE_OS_DESCRIPTION = "device_os_description";
	
	public final String ANDROID = "Android";  
	
	
	public enum DeviceHardwareIdType {
		 IMEI("imei"),
		 MAC_ADDRESS("mac addr");
		 
		 private final String name;
		 DeviceHardwareIdType(String name) {
			 this.name = name;
		 }
		 
		 public String getName() {
			 return this.name;
		 }
   }
	
	private static final SimpleDateFormat sUTCTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ENGLISH);//Changes by Hungama
	static {
		sUTCTimeFormat.setTimeZone(TimeZone.getTimeZone("utc"));
	}
	
	private String mDeviceModelName = null;
	private String mHardwareId = null;
	private DeviceHardwareIdType mDeviceHardwareIdType = null;
	
//	private static final String EMPTY = "empty"; 
//	private String mDevicePhoneNumber = EMPTY;
	
	/**
	 * Retrieves the current device time stamp formatted as UTC time zone:</br>
	 * yyyy-MM-dd'T'HH:mm:ss'Z'
	 */
	public String getTimeStamp() {
		return sUTCTimeFormat.format(System.currentTimeMillis());
	}
	
	public String getDeviceModelName(){
		if (mDeviceModelName == null) {
			mDeviceModelName = Build.MANUFACTURER.toString() + " " +
					 Build.MODEL.toString() + " " +
					 "("+Build.PRODUCT.toString()+")";
		}
		
		return mDeviceModelName;
	}
	
	public String getHardwareId() {

		if (mHardwareId == null) {
            // First we try to get the device telephony id, assuming that it has a gsm module.
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            /*
             * If the device has a telephony device id, that will be enough for identification.
             */
            if (telephonyManager != null && !TextUtils.isEmpty(telephonyManager.getDeviceId())) {
                mHardwareId = telephonyManager.getDeviceId();
                
            } else {
                /*
                 *  The device doesn't have any telephony device id, it means that it will be the mac address
                 */
                WifiManager wimanager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                String macAddress = wimanager.getConnectionInfo().getMacAddress();
                mHardwareId = macAddress;
            }
        }
        
        return mHardwareId;
    }
	
    public String getHardwareIdType() {
        
        if (mDeviceHardwareIdType == null) {
            // First we try to get the device telephony id, assuming that it has a gsm module.
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            /*
             * If the device has a telephony device id, that will be enough for identification.
             */
            if (telephonyManager != null && !TextUtils.isEmpty(telephonyManager.getDeviceId())) {
                mDeviceHardwareIdType = DeviceHardwareIdType.IMEI;
            } else {
                /*
                 *  The device doesn't have any telephony device id, it means that it will be the mac address
                 */
                mDeviceHardwareIdType = DeviceHardwareIdType.MAC_ADDRESS;
            }
        }
        
        return mDeviceHardwareIdType.getName();
    }
    
    public String getDeviceOS(){
		return ANDROID; //Build.VERSION.RELEASE;
	}
    
    public String getDeviceOSDescription(){
		return Build.VERSION.RELEASE;
	}
    
    /**
     * If no phone number available, retrieves null;
     * Removes the "+" prefix if exist.
     */
    public String getDevicePhoneNumber() {
    	TelephonyManager tMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    	
    	String phoneNumber = tMgr.getLine1Number();
    	
    	if (!TextUtils.isEmpty(phoneNumber)) {
    		phoneNumber = phoneNumber.replace("+", "");
    	}
    	
    	return phoneNumber;

    	// for testing on ugly devices.
//    	return "4774839560";
    }
    
}
