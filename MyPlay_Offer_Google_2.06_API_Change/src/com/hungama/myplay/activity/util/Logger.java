package com.hungama.myplay.activity.util;

import com.hungama.myplay.activity.BuildConfig;

import android.util.Log;


/**
 * Util class for controlling the SDK Log the {@code BuildConfig.DEBUG} flag.
 */
public class Logger {
	
	/**
	 * Set an info log message.
	 * @param Tag for the log message.
	 * @param message Log to output to the console.
	 */
	public static void i(String tag, String message) {
		//if (BuildConfig.DEBUG)
			Log.i(tag , message);
	}

	/**
	 * Set an error log message.
	 * @param Tag for the log message.
	 * @param message Log to output to the console.
	 */
	public static void e(String tag, String message) {
		//if (BuildConfig.DEBUG)
			Log.e(tag , message);
	}

	/**
	 * Set a warning log message.
	 * @param Tag for the log message.
	 * @param message Log to output to the console.
	 */
	public static void w(String tag , String message) {
		//if (BuildConfig.DEBUG)
			Log.w(tag, message);
	}

	/**
	 * Set a debug log message.
	 * @param Tag for the log message.
	 * @param message Log to output to the console.
	 */
	public static void d(String tag , String message) {
		//if (BuildConfig.DEBUG)
			Log.d(tag, message);
	}

	/**
	 * Set a verbose log message.
	 * @param Tag for the log message.
	 * @param message Log to output to the console.
	 */
	public static void v(String tag , String message) {
		//if (BuildConfig.DEBUG)
			Log.v(tag, message);
	}


}
