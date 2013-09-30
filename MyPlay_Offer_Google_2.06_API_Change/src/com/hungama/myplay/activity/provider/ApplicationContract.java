package com.hungama.myplay.activity.provider;

import android.net.Uri;

/**
 * Contract's the application base segmented data.
 */
public class ApplicationContract {

	public static final String CONTENT_AUTHORITY = "com.hungama.myplay.activity";
	
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
}
