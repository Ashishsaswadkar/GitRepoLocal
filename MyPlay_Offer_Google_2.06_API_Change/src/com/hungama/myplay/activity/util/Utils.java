package com.hungama.myplay.activity.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Patterns;

import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;

public class Utils {
		
	public static final String TEXT_EMPTY = "";
	public static final String NETWORK_WIFI = "wifi";
	public static final String NETWORK_3G = "3g";
	public static final String NETWORK_2G = "2g";

	/**
	 * Encrypts the given string with MD5 algorithm.</br>
	 * If it doesn't success, an empty String will be returned.
	 */
	public static final String toMD5(String stringToConvert) {
		
		try {
	    	
	        MessageDigest md5 = MessageDigest.getInstance("MD5");
	        byte result[] = md5.digest(stringToConvert.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < result.length; i++) {
	            String s = Integer.toHexString(result[i]);
	            int length = s.length();
	            if (length >= 2) {
	                sb.append(s.substring(length - 2, length));
	            } else {
	                sb.append("0");
	                sb.append(s);
	            }
	        }
	        return sb.toString();
	        
	    } catch (NoSuchAlgorithmException e) {
	        return "";
	    }
	}
	
	public static final String secondsToString(int seconds) {
		return String.format("%02d:%02d", ((seconds % 3600) / 60), (seconds % 60));
	}
	
	public static boolean isListEmpty(List<?> list) {
		
		if (list == null || list.isEmpty()) {
			
			return true;
		}
		
		return false;
	}
	
	public static int convertDPtoPX (Context mContext, int sizeInDP) {
	    final float scale = mContext.getResources().getDisplayMetrics().density;
	    int sizeInPX = (int) (sizeInDP * scale + 0.5f);	
	    return sizeInPX;
	}
	
	/**
	 * Function to convert milliseconds time to
	 * Timer Format
	 * Hours:Minutes:Seconds
	 * */
	public static String milliSecondsToTimer(long milliseconds){
		String finalTimerString = "";
		String secondsString = "";
		
		// Convert total duration into time
		   int hours = (int)( milliseconds / (1000*60*60));
		   int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
		   int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
		   // Add hours if there
		   if(hours > 0){
			   finalTimerString = hours + ":";
		   }
		   
		   // Prepending 0 to seconds if it is one digit
		   if(seconds < 10){ 
			   secondsString = "0" + seconds;
		   }else{
			   secondsString = "" + seconds;}
		   
		   finalTimerString = finalTimerString + minutes + ":" + secondsString;
		
		// return timer string
		return finalTimerString;
	}
	
	/**
	 * Function to get Progress percentage
	 * @param currentDuration
	 * @param totalDuration
	 * */
	public static int getProgressPercentage(long currentDuration, long totalDuration) {
		
		Double percentage = (double) 0;
		
		long currentSeconds = (int) (currentDuration / 1000);
		long totalSeconds = (int) (totalDuration / 1000);
		
		// Calculating percentage
		percentage = (((double) currentSeconds) / totalSeconds) * 100;
		
		// Return percentage
		return percentage.intValue();
	}
	
	/**
	 * Function to change progress to timer
	 * @param progress - 
	 * @param totalDuration
	 * returns current duration in milliseconds
	 * */
	public static int progressToTimer(int progress, int totalDuration) {
		int currentDuration = 0;
		totalDuration = (int) (totalDuration / 1000);
		currentDuration = (int) ((((double)progress) / 100) * totalDuration);
		
		// return current duration in milliseconds
		return currentDuration * 1000;
	}
	
	/**
	 * Function to convert String to Date(Hungama format)
	 * @param validityDateString - 
	 * returns validityDate in type Date. 
	 * */
	public static Date convertStringToDate(String validityDateString) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",Locale.ENGLISH);//Changes by Hungama
		Date validityDate = null;
		if (!validityDateString.equalsIgnoreCase(""))
		try {
			validityDate = dateFormat.parse(validityDateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return validityDate;
	}
	
    public static Date convertTimeStampToDate(String timeStamp){
    	
    	String dateFormatUTC = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdfUTC = new SimpleDateFormat(dateFormatUTC,Locale.ENGLISH);//Changes by Hungama

		Date date = null;
		try {
			date = sdfUTC.parse(timeStamp);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		
		return date;
    }
	
	public static boolean validateEmailAddress(String email) {
		Pattern pattern = Patterns.EMAIL_ADDRESS;
	    return pattern.matcher(email).matches();
	}
	
	public static boolean validateName(String name) {
		String pattern = "[A-Za-z]+";
	    return name.matches(pattern);
	}
	
	public static boolean isAlphaNumeric(String s){
	    String pattern= "^[a-zA-Z0-9]*$";
	        if(s.matches(pattern)){
	            return true;
	        }
	        return false;   
	}
	public static void invokeSMSApp(Context context,String smsBody){
		
		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		
		smsIntent.putExtra("sms_body", smsBody);
		smsIntent.setType("vnd.android-dir/mms-sms");
		
		context.startActivity(smsIntent);
	}
	
	public static void invokeEmailApp(Fragment fragment, List<String> emailBccTo, String subject ,String extraText){
		
		// Send mail to all checked friends
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO,Uri.fromParts("mailto","", null));
		//Intent emailIntent = new Intent(Intent.ACTION_SEND);
		
		//emailIntent.setType("plain/text");
		//emailIntent.setType("text/html");

		if(subject != null){
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);	
		}

		if(extraText != null){
			emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(extraText));
		}
		
		if(emailBccTo != null){
			// Convert List<String> to String[]
			String[] emailBccToArr = emailBccTo.toArray(new String[emailBccTo.size()]);

			emailIntent.putExtra(Intent.EXTRA_BCC, emailBccToArr);
		}
		
		fragment.startActivityForResult(emailIntent, 100);
	}
	
	public static void invokeEmailApp(Activity activity, List<String> emailBccTo, String subject ,String extraText){
		
		// Send mail to all checked friends
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO,Uri.fromParts("mailto","", null));
		//Intent emailIntent = new Intent(Intent.ACTION_SEND);
		
		//emailIntent.setType("plain/text");
		//emailIntent.setType("text/html");

		if(subject != null){
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);	
		}

		if(extraText != null){
			emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(extraText));
		}
		
		if(emailBccTo != null){
			// Convert List<String> to String[]
			String[] emailBccToArr = emailBccTo.toArray(new String[emailBccTo.size()]);

			emailIntent.putExtra(Intent.EXTRA_BCC, emailBccToArr);
		}
		
		activity.startActivityForResult(emailIntent, 100);
	}
	
	
	public static String getTimestampAfterDelta(String eventTimestamp, Context context) {
		String timestamp = null;
		
		ApplicationConfigurations applicationConfigurations = 
				DataManager.getInstance(context.getApplicationContext()).getApplicationConfigurations();
		
		long updatedTimestamp;
		Date eventDate = null;
		try {
			eventDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ENGLISH).parse(eventTimestamp);//Changes by Hungama
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (eventDate != null) {
			long eventTimestampMillis = eventDate.getTime();
			
			updatedTimestamp =  eventTimestampMillis + applicationConfigurations.getTimeReadDelta(); 
			
			SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ENGLISH);//Changes by Hungama
		    sSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("utc"));
			timestamp = sSimpleDateFormat.format(new Date(updatedTimestamp));
		}
		
		return timestamp;
	}

	public static String getNetworkType(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
 		if (networkInfo != null && networkInfo.isConnected()) {
 			String networkType = networkInfo.getTypeName();
 			if (networkType.equalsIgnoreCase(NETWORK_WIFI) ) { 
 				return networkType; 				
 			} else if (networkType.equalsIgnoreCase("MOBILE")) {
 				int subType = networkInfo.getSubtype();
 				switch(subType){
					case TelephonyManager.NETWORK_TYPE_EDGE:
					case TelephonyManager.NETWORK_TYPE_GPRS:
						return networkType = NETWORK_2G;
						
					case TelephonyManager.NETWORK_TYPE_EVDO_0:
					case TelephonyManager.NETWORK_TYPE_EVDO_A:
					case TelephonyManager.NETWORK_TYPE_EVDO_B:
					case TelephonyManager.NETWORK_TYPE_HSDPA:
					case TelephonyManager.NETWORK_TYPE_HSPA :
					case TelephonyManager.NETWORK_TYPE_HSPAP:
					case TelephonyManager.NETWORK_TYPE_HSUPA:
					case TelephonyManager.NETWORK_TYPE_LTE:
					case TelephonyManager.NETWORK_TYPE_UMTS:
						return networkType = NETWORK_3G;
						
					case TelephonyManager.NETWORK_TYPE_UNKNOWN:
						return networkType = TEXT_EMPTY;					
 				} 				
 			}
 		}
 		return TEXT_EMPTY;
	}
}
