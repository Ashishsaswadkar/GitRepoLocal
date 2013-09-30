package com.hungama.myplay.activity.data.persistance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Manages Inventory Database actions.
 */
public final class DatabaseManager {
	
	private static final String INVENTORY_ITEM_ACTION_DELETE = "Del";
	private static final String INVENTORY_ITEM_ACTION_MOD = "Mod";
	private static final String INVENTORY_ITEM_ACTION_ADD = "Add";
	private static final String INVENTORY_ITEM_ACTION = "action";
	
	public static final String TAG = "DatabaseManager";
	private static DatabaseManager mInstance = null;
	
	private static InventoryDatabaseHelper mDatabaseHelper;
	private static SQLiteDatabase mDatabase;
	
	
	private DatabaseManager(Context context) throws SQLException {
		mDatabaseHelper = InventoryDatabaseHelper.getInstance(context);
		mDatabase = mDatabaseHelper.getWritableDatabase();
	}
	
	public static DatabaseManager getInstance(Context context) throws SQLException {
		if (mInstance == null){
			synchronized (DatabaseManager.class) {
				if (mInstance == null){
					mInstance = new DatabaseManager(context);
				}
			}
			
		} else{
			
			// TODO: check this comment out, + sync this?
			/*
			 * Every class that uses this manager, must close the connection to the database.
			 * This validates if a new class requests an instance, it will get it with open connection.  
			 */
			if (!mDatabase.isOpen()){
				mDatabase = mDatabaseHelper.getWritableDatabase();
			}
		}
		
		return mInstance;
	}
	
	/**
	 * Close the connection to the Database. 
	 */
	public void close() {
		if (mDatabase != null){
			mDatabase.close();
		}
	}
	
	/**
	 * Updates old inventory database with the given one. 
	 */
	public void updateInventory(Map<String, List<Map>> inventoryMap) {
		
		// get database tables name list.
		List<String> tables = InventoryContract.getTableList();
		
		// *****************************************
		
		// check if "data" map has any media categories.
		if (inventoryMap != null && !inventoryMap.isEmpty()) {
		
			String mediaCategoryName = null;
			List<Map> mediaItems = null;
			
			mDatabase.beginTransaction();
			try {
			
				// iterate through all media categories.
				for (Map.Entry<String, List<Map>> mediaCategory : inventoryMap.entrySet()) {
			    
					// check existence of mediaCategoryName in database tables list and if it has any data.
					mediaCategoryName = mediaCategory.getKey();
					mediaItems = mediaCategory.getValue();
					
					if (tables.contains(mediaCategoryName) && mediaItems != null && !mediaItems.isEmpty()) {
					
	 					String action = null;
	 					
	 					// media category and the database tables must be the same.
	 					Itemable inventoryItem = InventoryContract.getItemableByTableName(mediaCategoryName);
						
						// iterate through mediaCategory :
						for (Map mediaItem : mediaItems) {
							// contains action ?
							action = (String)mediaItem.get(INVENTORY_ITEM_ACTION);
							if (action != null && action.length() != 0) {
										
								if (action.equals(INVENTORY_ITEM_ACTION_DELETE)) {
									// Del - delete item.
									
									long itemId = ((Long) mediaItem.get("id")).longValue();
									deleteItemHelper(inventoryItem.getTableName(), inventoryItem.getIdColumnName(), itemId);
									Log.i("Inventory", "Delete " + inventoryItem.getTableName() + " " + inventoryItem.getId());
									
								} else if (action.equals(INVENTORY_ITEM_ACTION_MOD)) { 
									// Mod - update item.
									// load inventoryItem with data (from mediaItem map).
									inventoryItem = inventoryItem.getInitializedObject(mediaItem);
									// validate if exist
									if (isExist(inventoryItem)) {
										update(inventoryItem);
										Log.i("Inventory", "Update " + inventoryItem.getTableName() + " " + inventoryItem.getId());
									} else {
										insert(inventoryItem);
										Log.i("Inventory", "Insert " + inventoryItem.getTableName() + " " + inventoryItem.getId());
									}
									
								} else if (action.equals(INVENTORY_ITEM_ACTION_ADD)) {
									// Add - insert item.
									// load inventoryItem with data (from mediaItem map).
									inventoryItem = inventoryItem.getInitializedObject(mediaItem);
									
									if(isExist(inventoryItem)){
										update(inventoryItem);
										Log.i("Inventory", "Update " + inventoryItem.getTableName() + " " + inventoryItem.getId());
									}else{
										insert(inventoryItem);
										Log.i("Inventory", "Insert " + inventoryItem.getTableName() + " " + inventoryItem.getId());
									}
								}
								
							} else {
								// does not contain action:
								// Log.d("Inventory", "Test Album " + (Long) mediaItem.get(InventoryContract.Albums.ID));
								inventoryItem = inventoryItem.getInitializedObject(mediaItem);
								if (isExist(inventoryItem)) {
									update(inventoryItem);
									Log.i("Inventory", "Update " + inventoryItem.getTableName() + " " + inventoryItem.getId());
								}else {
									insert(inventoryItem);
									Log.i("Inventory", "Insert " + inventoryItem.getTableName() + " " + inventoryItem.getId());
								}
							}
							
						}
					}
				}
				
				mDatabase.setTransactionSuccessful();
				
			} catch(Exception error) {
				
				if (error != null) {
					error.printStackTrace();
				}
				
				mDatabase.endTransaction();
			}
			
			mDatabase.endTransaction();
		}
		
		
	}
	
	/**
	 * Query for the given {@link Itemable} table, retrieve items as result. 
	 * @param Itemable for getting the table name and it's columns.
	 * @param whereClause filter declaring which rows to return, formatted as an SQL WHERE clause.
	 * @return {@code ArrayList<Itemable>} with {@link Itemable} objects to return, if there is no result to the query, </br>
	 * {@code null} will return. 
	 */
	public List<Itemable> query(Itemable itemable, String whereClause, String sortBy) {
		ArrayList<Itemable> results;
		Cursor resultCursor = mDatabase.query(itemable.getTableName(), itemable.getTableColumns(), 
				whereClause, null, null, null, sortBy);
		
		if(resultCursor != null && resultCursor.getCount() > 0){
			
			results = new ArrayList<Itemable>();
			resultCursor.moveToFirst();
			
			do{
				itemable = itemable.getInitializedObject(resultCursor);
				results.add(itemable);
			} while (resultCursor.moveToNext());
			
			resultCursor.close();
			resultCursor = null;
			
			return results;
		} else if (resultCursor != null && resultCursor.getCount() == 0) {
			
			resultCursor.close();
			resultCursor = null;
		}
		
		return null;
	} 
	
	/**
	 * Checks if the object is stored in the database
	 * @param itemable object.
	 */
	public synchronized boolean isExist(Itemable itemable) {
		Cursor resultCursor = mDatabase.query(itemable.getTableName(), itemable.getTableColumns(), 
				itemable.getIdColumnName() + " = " + itemable.getId(), null, null, null, null);
		
		boolean isExist = (resultCursor != null && resultCursor.getCount() > 0);
		
		resultCursor.close();
		resultCursor = null;
		
		return isExist;
	}
	
	// insert
	
	/**
	 * Inserts the item in the database.
	 * @param itemable object to insert.
	 * @return the row ID of the newly inserted row, or -1 if an error occurred, see {@code SQLiteDatabase.insert()}</br>
	 * for more details.
	 */
	public long insert(Itemable itemable) {
		long rowId = mDatabase.insert(itemable.getTableName(), null, itemable.getObjectFieldValues());
		return rowId;
	}
	
	/**
	 * Inserts the list of items in the database.</br>
	 * Calls {@link insert} to batch insert to list.
	 * @param itemables list to insert.
	 */
	public void insert(List<Itemable> itemables) {
		// validates fields.
		if (itemables == null || itemables.isEmpty()) {
			throw new IllegalArgumentException("Argumented list is null or empty.");
		}
		
		for(Itemable itemable : itemables) {
			insert(itemable);
		}
	}
	
	
	// update
	
	/**
	 * Updates the item in the database.
	 * @param itemable object to update.
	 * @return number of rows effected for error monitoring, see {@code SQLiteDatabase.update()}</br>
	 * for more details.
	 */
	public int update(Itemable itemable) { 
		int result = mDatabase.update(itemable.getTableName(), itemable.getObjectFieldValues(), itemable.getIdColumnName() + " = " + itemable.getId(), null);
		return result;
	}
	
	/**
	 * Updates the list of items in the database.</br>
	 * Calls {@link update} to batch update to list.
	 * @param itemables list to update.
	 * @return number of rows effected.
	 */
	public int update(List<Itemable> itemables) {
		// validates fields.
		if (itemables == null || itemables.isEmpty()) {
			throw new IllegalArgumentException("Argumented list is null or empty.");
		}
		
		int updatedRows = 0;
		int x;
		
		for(Itemable itemable : itemables) {
			x = update(itemable);
			updatedRows += x;
		}
		
		return updatedRows;
	}
	
	// delete
	
	/**
	 * Deletes the object from the database.</br>
	 * this method relays on {@link Itemable.getId()} return value, it removes the object by it's id only. 
	 * @param itemable to delete from the database.
	 * @return 1 if the object has been deleted, 0 otherwise.
	 */
	public int delete(Itemable itemable) {
		int result = mDatabase.delete(itemable.getTableName(), itemable.getIdColumnName() + " = " + itemable.getId(), null);
		return result;
	}
		
	/**
	 * Deletes the list of items in the database.
	 * Calls {@link delete} to batch delete to list.
	 * @param itemables list to delete from the database.
	 * @return the number of rows deleted.
	 */
	public int delete(List<Itemable> itemables) {
		// validates fields.
		if (itemables == null || itemables.isEmpty()) {
			throw new IllegalArgumentException("Argumented list is null or empty.");
		}
		
		int deletedRows = 0;
		int x;
		
		for(Itemable itemable : itemables) {
			x = delete(itemable);
			deletedRows += x;
		}
		
		return deletedRows;
	}
	
	
	// names
	
	public String getItemNameById(String query, String selectionArgs[]) {
		Cursor resultCursor = mDatabase.rawQuery(query, selectionArgs);
		// extract the name from the cursor (only retrieved value)
		String artistName = "";
		
		if (resultCursor != null && resultCursor.getCount() > 0) {
			resultCursor.moveToFirst();
			artistName = (String) resultCursor.getString(0);
		}
		
		resultCursor.close();
		resultCursor = null;
		
		return artistName;
	}
	
	// counts
	
	/**
	 * Returns count value of all the objects from the same kind of {@link Itemable}.
	 * @param itemable 
	 * @return count of existing items in the database from the same kind.
	 */
	public long getNumberOfItemable(Itemable itemable) {
		// build the query.
		String queryString = "SELECT COUNT(" + itemable.getIdColumnName() + ") FROM " + itemable.getTableName();
		// run the query.
		Cursor resultCursor = mDatabase.rawQuery(queryString, null);
		long result = 0;
		
		// get the result
		if (resultCursor != null && resultCursor.getCount() > 0) {
			resultCursor.moveToFirst();
			result = (Long) resultCursor.getLong(0);
		}
		
		resultCursor.close();
		resultCursor = null;
		
		return result;
	}
	
	/**
	 * Generic method to get number of items in the Database by given query and arguments.
	 * 
	 * @param itemable.
	 * @param where clause.
	 * @return number of items in the database based on the query.
	 */
	public long getNumberOfItemable(Itemable itemable, String where) {
		
		if (where != null) {
			where = " WHERE " + where;
		}
		
		// build the query.
		String queryString = "SELECT COUNT(" + itemable.getIdColumnName() + ") FROM " + itemable.getTableName() + where;
		// run the query.
		Cursor resultCursor = mDatabase.rawQuery(queryString, null);
		long result = 0;
		
		// get the result
		if (resultCursor != null && resultCursor.getCount() > 0) {
			resultCursor.moveToFirst();
			result = (Long) resultCursor.getLong(0);
		}
		
		resultCursor.close();
		resultCursor = null;
		
		return result;
	}
	
	/**
	 * Generic method to get number of items in the Database by given query and arguments.
	 * @param query for receiving number result of the desired item.
	 * @param selectionArgs parameters for the WHERE clause.
	 * @return number of items in the database based on the query.
	 */
	public long getNumberOfItemableBy(String query, String selectionArgs[]) {
		// run the query.
		Cursor resultCursor = mDatabase.rawQuery(query, selectionArgs);
		long result = 0;
		
		// get the result
		if (resultCursor != null && resultCursor.getCount() > 0) {
			resultCursor.moveToFirst();
			result = (Long) resultCursor.getLong(0);
		}
		
		resultCursor.close();
		resultCursor = null;
				
		return result;
	}
	
	
	// helper methods
	
	/**
	 * Helper method for deleting item from the update inventory update process. 
	 * @param tableName - name of the table from whom to delete the item.
	 * @param itemId - id of the item.
	 * 
	 * @return number of items deleted.
	 */
	private int deleteItemHelper(String tableName, String idColumnName,long itemId) {
		int result = mDatabase.delete(tableName, idColumnName + " = " + itemId, null);
		
		return result;
	}
}
 