package com.hungama.myplay.activity.util;

import java.io.File;
import java.io.IOException;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class FileUtils {

	private static final String TAG = "FileUtils";

	private Context mContext;
	private boolean mExternalStorageAvailable = false;
	private boolean mExternalStorageWriteable = false;
	private boolean mCreateDirectorySuccess = false;
	private boolean mCreateFileSuccess = false;

	public FileUtils(Context context) {

		mContext = context;
	}

	public boolean isExternalStoragePresent() {

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {

			// We can read and write the media.
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = true;

		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {

			// We can only read the media.
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;

		} else {

			// Something else is wrong. It may be one of many other states, but
			// all we need
			// To know is we can neither read nor write.
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		// if (!((mExternalStorageAvailable) && (mExternalStorageWriteable))) {
		//
		// Toast.makeText(mContext, "SD card not present",
		// Toast.LENGTH_LONG).show();
		// }

		return (mExternalStorageAvailable) && (mExternalStorageWriteable);
	}
	
	/*
	 * Method to create a directory
	 * @param dirName
	 * @return the directory which was created or null if create failed
	 */
//	public File createDirectory(File path) {
//
//		try {
//		
//			if (!isExternalStoragePresent()) {
//	
//				Toast.makeText(mContext, "SD card not present", Toast.LENGTH_LONG).show();
//				return null;
//	
//			} else {
//	
////				File directory = new File(Environment.getExternalStorageDirectory()
////						.getPath() + "/" + dirName);
//	
//				if (!path.exists()) {
//	
//					mCreateDirectorySuccess = directory.mkdir();
//	
//					if (!mCreateDirectorySuccess) {
//	
//						Toast.makeText(mContext, "Fail to create directory", Toast.LENGTH_LONG).show();
//						return null;
//	
//					} else {
//	
//						Toast.makeText(mContext, "Directory created successfully", Toast.LENGTH_LONG).show();
//						return directory;
//					}
//				} else {
////					Toast.makeText(mContext, "Directory already exists", Toast.LENGTH_LONG).show();
//					return directory;
//				}
//			}
//		
//		} catch (Exception e) {
//			
//			Logger.e(TAG, e.getMessage());
//			return null;
//		}
//	}
	

	public boolean createFile(String dirName, String fileName) {

		try {

			if (!isExternalStoragePresent()) {

				Toast.makeText(mContext, "SD card not present", Toast.LENGTH_LONG).show();
				return false;

			} else {

				File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + dirName + "/" + fileName);

				if (!file.exists()) {

					mCreateFileSuccess = file.createNewFile();

					if (!mCreateFileSuccess) {

						Toast.makeText(mContext, "Fail to create file", Toast.LENGTH_LONG).show();
						return false;

					} else {

						Toast.makeText(mContext, "File created successfully", Toast.LENGTH_LONG).show();
						return true;
					}
				} else {
					Toast.makeText(mContext, "File already exists", Toast.LENGTH_LONG).show();
					return false;
				}

			}

		} catch (IOException e) {

			Logger.e(TAG, e.getMessage());
			return false;
		}
	}		

	public void deleteDirectoryRecursively(File dir) {

		try {
		
			if (dir.isDirectory()) {
	
				String[] children = dir.list();
	
				for (int i = 0; i < children.length; i++) {
	
					File child = new File(dir, children[i]);
	
					if (child.isDirectory()) {
	
						deleteDirectoryRecursively(child);
	
					} else {
	
						boolean isDeleted = child.delete();
	
						if (isDeleted == false) {
	
							Toast.makeText(mContext, "Child deleted fail.", Toast.LENGTH_LONG).show();
						}
					}
				}
	
				dir.delete();
			}
		
		} catch (Exception e) {

			Logger.e(TAG, e.getMessage());
		}
	}
	
	public void deleteFile(File dir, File file) {
		
		try {
			
			boolean isDeleted = file.delete();
			
			if (isDeleted == false) {
				
				Toast.makeText(mContext, "Child deleted fail.", Toast.LENGTH_LONG).show();
			}
			
		} catch (Exception e) {
			
			Logger.e(TAG, e.getMessage());
		}
	}
	
	/*
	 * Method to check if a file exists in a specific directory
	 * @param directory
	 * @param fileName
	 */
	public boolean isFileInDirectory(File dir, String fileName) {
		if (dir.exists() && dir.isDirectory()) {
			for (File f : dir.listFiles()) {
			    if (f.isFile()) {
			        String name = f.getName();
				    if (name.equalsIgnoreCase(fileName)) {
				    	return true;						
					}
			    }
			}
		}
		return false;
	}
	
	public File getStoragePath (MediaContentType mediaType) {
		if (!isExternalStoragePresent()) {

			Toast.makeText(mContext, "SD card not present", Toast.LENGTH_LONG).show();
			return null;
		}
		
		File path = null;
		if (!(mediaType == MediaContentType.VIDEO)) {
			path = Environment.getExternalStoragePublicDirectory(
		            Environment.DIRECTORY_MUSIC);
		}
		
		if (path == null || !path.exists()) {
			 path = Environment.getExternalStoragePublicDirectory(
					 	Environment.DIRECTORY_DOWNLOADS);
		}
		return path;
	}
	
//	String externalStorageDirectory = Environment.getExternalStorageDirectory().toString();
//    File folder = new File(externalStorageDirectory, "Varonis_PDF");	        	        	        
//    folder.mkdir();
//    file = new File(folder, fileMetadata.getData().getFileName());
//    file.createNewFile();
}
