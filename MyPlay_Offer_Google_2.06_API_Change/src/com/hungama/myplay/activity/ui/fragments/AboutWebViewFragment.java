package com.hungama.myplay.activity.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Shows a web content with webview by an argumented URL.
 */
public class AboutWebViewFragment extends Fragment {

	private static final String TAG = "AboutWebViewFragment";
	
	public static final String FRAGMENT_WEBVIEW = "fragment_webview";
	
	public static final String FRAGMENT_ARGUMENT_URL = "fragment_argument_url";
	
	private String mUrl;
	private WebView mWebview;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle arguments = getArguments();
		if (arguments != null && arguments.containsKey(FRAGMENT_ARGUMENT_URL)) {
			mUrl = arguments.getString(FRAGMENT_ARGUMENT_URL);
		} else {
			throw new IllegalArgumentException("Fragment argument must contain FRAGMENT_ARGUMENT_URL String url value!");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mWebview = new WebView(getActivity());
		
		// sets the web views features.
		WebSettings settings = mWebview.getSettings();
		settings.setJavaScriptEnabled(true);
		
		mWebview.setWebViewClient(new AboutWebViewClient());
		mWebview.loadUrl(mUrl);
		
		return mWebview;
	}
	
	
	public boolean onBackPressed() {
		if (mWebview.canGoBack()) {
			mWebview.goBack();
			
			return true;
		}
		return false;
	}
	
	
	private class AboutWebViewClient extends WebViewClient {
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
	}
	
}
