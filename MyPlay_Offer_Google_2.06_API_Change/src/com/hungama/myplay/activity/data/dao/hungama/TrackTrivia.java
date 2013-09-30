package com.hungama.myplay.activity.data.dao.hungama;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Trivia of a given {@link Track} as a response of {@link TrackTriviaOperation}. 
 */
public class TrackTrivia {

	public static final String KEY_CONTENT_ID = "content_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_TRIVIA = "trivia";
	
	@SerializedName(KEY_CONTENT_ID)
	public final long id;
	@SerializedName(KEY_TITLE)
	public final String title;
	@SerializedName(KEY_TRIVIA)
	public final List<String> trivia;
	
	public TrackTrivia(long id, String title, List<String> trivia) {
		this.id = id;
		this.title = title;
		this.trivia = trivia;
	}
}
