package com.hungama.myplay.activity.data.dao.hungama;

/**
 * Holds properties of the indexing from the response thru the paging process. 
 */
public class DiscoverSearchResultIndexer {
	
	public static final int DEFAULT_START_INDEX = 0;
	public static final int DEFAULT_LENGTH = 20;

	private final int startIndex;
	private final int length;
	private final int max;
	
	public DiscoverSearchResultIndexer(int startIndex, int length, int max) {
		this.startIndex = startIndex;
		this.length = length;
		this.max = max;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public int getLength() {
		return length;
	}

	public int getMax() {
		return max;
	}
}
