package com.hungama.myplay.activity.operations.catchmedia;

/**
 * Enumeration definition of the Json-RPC2 common methods.
 */
public enum JsonRPC2Methods {
	
	CREATE		("Create"),
	READ		("Read"),
	READ_ALL	("ReadAll"),
	UPDATE		("Update"),
	DELETE		("Delete");
	
	private final String tag;
	
	JsonRPC2Methods(String tag) {
		this.tag = tag;
	}
	
	@Override
	public String toString() {
		return this.tag;
	}
}
