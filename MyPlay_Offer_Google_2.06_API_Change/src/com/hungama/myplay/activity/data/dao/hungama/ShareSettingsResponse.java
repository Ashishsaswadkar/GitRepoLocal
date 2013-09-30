package com.hungama.myplay.activity.data.dao.hungama;

public class ShareSettingsResponse {	
		
	public int code;
	public String message;
	
	public Data data;
	
	public ShareSettingsResponse(){}

	public class Data {
		
		public int songs_listen;
		public int my_favorites;
		public int songs_download;
		public int my_comments;
		public int my_badges;
		public int videos_watched;
		public int videos_download;
		
		public Data(){}
	}
}
