package com.hungama.myplay.activity.data.persistance;

import java.io.Serializable;
import java.util.Map;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * Represents media item in the applications inventory's database.
 */
public interface Itemable {
		
	/**
	 * Tells all delegators to operate Database actions by this unique identifier.  
	 * @return id of the implementor object (the "item").
	 */
	public long getId();
	
	public String getName();
	
	/**
	 * Retrieves object's id column name in the database for query builders.
	 */
	public String getIdColumnName();
	
	/**
	 * Retrieves object's table name in the database.
	 */
	public String getTableName();
	
	/**
	 * Retrieves object's column names in the database.
	 */
	public String[] getTableColumns();
	
	/**
	 * Retrieves new instance of the object with the current cursor position data. 	
	 * @param cursor from the query.
	 * @return new implementation populated with the current position of the cursor.
	 */
	public Itemable getInitializedObject(Cursor cursor);
	
	/**
	 * Retrieves new instance of the object with the given map. 	
	 * @param map with data to initialize the new object.
	 * @return new implementation populated with the current position of the cursor.
	 */
	public Itemable getInitializedObject(Map map);
	
	/**
	 * Retrieves set of the object's values, for {@link DatabaseManager} operations.
	 */
	public ContentValues getObjectFieldValues();
		
}
