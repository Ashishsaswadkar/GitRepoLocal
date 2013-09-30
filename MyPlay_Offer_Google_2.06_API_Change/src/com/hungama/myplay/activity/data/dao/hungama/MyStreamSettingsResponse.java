package com.hungama.myplay.activity.data.dao.hungama;

public class MyStreamSettingsResponse {	
		
	public int code;
	public String message;
	
	public Data data;
	
	public MyStreamSettingsResponse(){}

	public class Data {
		
		public int musiclisten;
		public int likes;
		public int downloads;
		public int comments;
		public int videowatched;
		public int shares;
		public int badges;
		
		public Data(){}
	}
}
