package com.hungama.myplay.activity.util;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.hungama.myplay.activity.util.images.AsyncTask;

public class DownloadFile extends AsyncTask<String, Integer, String>{

	private String downloadFromUrlHere;
	
	public DownloadFile(String downloadFromUrl) {
		downloadFromUrlHere = downloadFromUrl;
	}
	
	@Override
	protected String doInBackground(String... url) {
	    int count;
	    try {
	        URL myUrl = new URL(downloadFromUrlHere);
	        URLConnection conexion = myUrl.openConnection();
	        conexion.connect();
	        // this will be useful so that you can show a tipical 0-100% progress bar
	        int lenghtOfFile = conexion.getContentLength();
	
	        // downlod the file
	        InputStream input = new BufferedInputStream(myUrl.openStream());
	        OutputStream output = new FileOutputStream("/sdcard/somewhere/nameofthefile.mp3");
	
	        byte data[] = new byte[1024];
	
	        long total = 0;
	
	        while ((count = input.read(data)) != -1) {
	            total += count;
	            // publishing the progress....
	            publishProgress((int)(total*100/lenghtOfFile));
	            output.write(data, 0, count);
	        }
	
	        output.flush();
	        output.close();
	        input.close();
	    } catch (Exception e) {}
	    return null;
	}
}