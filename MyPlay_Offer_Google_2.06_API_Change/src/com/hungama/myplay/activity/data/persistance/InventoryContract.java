package com.hungama.myplay.activity.data.persistance;

import java.util.Arrays;
import java.util.List;

import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;

/**
 * Contract class for interacting with {@link Itemable} and {@link InventoryDatabaseHelper}, </br>
 * with the definition of the tables and columns names. 
 */
public class InventoryContract {
	
	private static final String[] TableList = {
		Tables.TRACKS,
		Tables.PLAYLISTS
	};
	
	public static List<String> getTableList() {
		return Arrays.asList(TableList);
	}
	
	/**
	 * Retrieves Itemable object by the name of the given DataBase table.</br>
	 * The object will be empty from data, call {@code Itemable.getInitializedObject(Map map) or</br>
	 * {@code Itemable.getInitializedObject(Cursor cursor)} for creating new object from it.
	 * 
	 * @param tableName from the database to retrieve the relevant Itemable
	 * @return empty Itemable implementation object for instance creation, null if no table has been found. 
	 * @throws IllegalArgumentException when the table name is null or empty.
	 */
	public static Itemable getItemableByTableName(String tableName) throws IllegalArgumentException {
		if (tableName == null || tableName.length() == 0){
			throw new IllegalArgumentException("Argument is empty or null");
		}
				
		return null;
	}
	
	public interface Tables {		
		// data:
		String TRACKS = "tracks";
    	String PLAYLISTS = "playlists";
    }

	public interface Tracks {
		String ID = "id";
		String NAME = "name";
		String ALBUM_ID = "album_id";
		String ARTIST_ID = "artist_id";
		String TRACK_NUMBER = "track_number";
		String IS_CACHED = "is_cached";
		String DELIVERY_ID = "delivery_id";
		String DO_NOT_CACHE = "do_not_cache";
		String LIMIT_DURATION = "limit_duration";
	}
	
	public interface Playlists {
		String ID = "id";
		String NAME = "name";
		String TRACK_LIST = "tracklist";
	}
	
}
