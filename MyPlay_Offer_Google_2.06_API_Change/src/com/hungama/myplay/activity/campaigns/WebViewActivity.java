package com.hungama.myplay.activity.campaigns;

import com.hungama.myplay.activity.ui.VideoActivity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {

	private static String actionUri; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        actionUri = getIntent().getStringExtra(VideoViewActivity.ACTION_URI);
        
		WebView webview = new WebView(this);
		webview.setBackgroundColor(Color.TRANSPARENT);
		setContentView(webview);
		webview.loadUrl(actionUri);
		
		webview.getSettings().setJavaScriptEnabled(true);
	  
	    webview.setWebViewClient(new HelloWebViewClient());   
	   
    }
    
    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
    
//  @Override
//  public boolean onKeyDown(int keyCode, KeyEvent event) {
//      if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
//      	webview.goBack();
//          return true;
//      }
//      return super.onKeyDown(keyCode, event);
//  }
}
