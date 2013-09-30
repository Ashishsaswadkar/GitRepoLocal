package com.hungama.myplay.activity.data.persistance;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class for creating and upgrading the application's Inventory data base. 
 */
public class InventoryDatabaseHelper extends SQLiteOpenHelper {
	
	private static InventoryDatabaseHelper mInstance = null;
	
	private static final String DATABASE_NAME = "inventory.db";
	/*
	 * Change this to commit call to onUpgrade when the new value equals newVersion argument. 
	 */
    private static final int DATABASE_VERSION = 1; //  
    
    /**
	 * Retrieves an instance of the class.  
	 */
	public static InventoryDatabaseHelper getInstance(Context context){
		//TODO: Thread safe this.
		if(mInstance == null){
			mInstance = new InventoryDatabaseHelper(context);
		}
		return mInstance;
	}
	
    private InventoryDatabaseHelper(Context context){
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// creates playlists table:
		db.execSQL("CREATE TABLE " + InventoryContract.Tables.PLAYLISTS + " ("
				+ InventoryContract.Playlists.ID + " INTEGER," 
				+ InventoryContract.Playlists.NAME + " TEXT,"
				+ InventoryContract.Playlists.TRACK_LIST + " TEXT"
				+ ");"
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Method is called during an upgrade of the database,
		// e.g. if you increase the database version. 
	}

}
