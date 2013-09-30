package com.hungama.myplay.activity.ui;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.util.FileUtils;
import com.hungama.myplay.activity.util.Logger;

/**
 * Controller for presenting details of the given MediaItem.
 */
public class DownloadConnectingActivity extends MainActivity {
	
	private static final String TAG = "DownloadConnectingActivity";
	
	protected static final String FRAGMENT_TAG_UPGRADE = "fragment_tag_upgrade";
	
	public static final String PASSWORD_SMS_SENT = "1";
	public static final String MSISDN_ALREADY_EXIST_AND_VERIFIED = "3";
	public static final String EXTRA_MEDIA_ITEM = "extra_media_item";
	
	public static final String CONTENT_TYPE_AUDIO = "audio";
	public static final String CONTENT_TYPE_VIDEO = "video";
	
	private MediaItem mMediaItem; 
	private Dialog downloadDialog;
	
	private FileUtils fileUtils;
	private File hungamaCollectionDir;
	
	public static String mobileToSend;
	
	private Bundle data;
	
	private boolean backFromDownloadActivity = false;
	
	
	// ======================================================
	// Activity life-cycle callbacks. 
	// ======================================================
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		// validate calling intent.
		Intent intent = getIntent();
		if (intent == null) {
			Logger.e(TAG, "No intent for the given Activity.");
			return; 
		}		
		
		setContentView(R.layout.activity_download_connecting);
		
		data = intent.getExtras();
		
		if (data != null && data.containsKey(EXTRA_MEDIA_ITEM)) {
			// retrieves the given Media item for the activity.
			mMediaItem = (MediaItem) data.getSerializable(EXTRA_MEDIA_ITEM);
			if (mMediaItem != null) {
				// check if the file already exists in the downloaded files library - display dialog.
				fileUtils = new FileUtils(this);
				// create directory and return it or just return it if already exists 
//				String hungamaFolder = getResources().getString(R.string.download_media_folder);
				
				if (fileUtils.isExternalStoragePresent()) {
					File path = fileUtils.getStoragePath(mMediaItem.getMediaContentType());
//					hungamaCollectionDir = fileUtils.createDirectory(path);
					if (path != null) {
						String mediaFileName;
						if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
							mediaFileName = mMediaItem.getTitle() + "_" + String.valueOf(mMediaItem.getId()) + ".mp4";
						} else {
							mediaFileName = mMediaItem.getTitle() + "_" + String.valueOf(mMediaItem.getId()) + ".mp3";
						}
						
						String encodedMediaFileName = "";
						try {
							encodedMediaFileName = URLEncoder.encode(
									mediaFileName, 
									"UTF-8");
						} catch (UnsupportedEncodingException e) {
							Logger.i(TAG, e.getMessage());
							e.printStackTrace();
						}
//						File file = new File(path, mediaFileName);
					 
						if (fileUtils.isFileInDirectory(path, encodedMediaFileName)) {
							String title = getResources().getString(R.string.general_download);
							String body ="";
							if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
								body = getResources().getString(R.string.download_same_song_dialog_body_text_video);
							} else {
								body = getResources().getString(R.string.download_same_song_dialog_body_text);
							}
							
							hideLoadingDialog();
							showDownloadDialog(title, body, true, false);
							
						} else {
							Intent downloadActivityIntent = new Intent(this, DownloadActivity.class);
							downloadActivityIntent.putExtra(DownloadActivity.EXTRA_MEDIA_ITEM, (Serializable) mMediaItem);
							startActivity(downloadActivityIntent);
						}		
					}
				} else {
					finish();
				}
			} 													
		} else {
			Logger.e(TAG, "No MediaItem set for the given Activity.");
			return; 
		}
			
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(backFromDownloadActivity) {
			finish();
			Logger.i(TAG, "finished the connecting Activity");
		} else {
			backFromDownloadActivity = true;
			Logger.i(TAG, "backFromDownloadActivity changed to true");
		}
	}
	
	public void showDownloadDialog(String header, String body, boolean isLeftButtonVisible, boolean isRightButtonVisible) {
		//set up custom dialog
        downloadDialog = new Dialog(this);
        downloadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        downloadDialog.setContentView(R.layout.dialog_download_same_song);
        
        TextView title = (TextView) downloadDialog.findViewById(R.id.download_custom_dialog_title_text);
        title.setText(header);
        
        TextView text = (TextView) downloadDialog.findViewById(R.id.download_custom_dialog_text);
        text.setText(body);
                       
        Button goToMyCollectionButton = (Button) downloadDialog.findViewById(R.id.download_left_button);
        Button downloadAgainButton = (Button) downloadDialog.findViewById(R.id.download_right_button);
        
        LinearLayout ButtonsPanel = (LinearLayout) downloadDialog.findViewById(R.id.buttons_panel);
        if (!isLeftButtonVisible && !isRightButtonVisible) {
        	ButtonsPanel.setVisibility(View.GONE);
        } else {
        	ButtonsPanel.setVisibility(View.VISIBLE);
        	if (isLeftButtonVisible) {
        		goToMyCollectionButton.setVisibility(View.VISIBLE);
        		goToMyCollectionButton.setOnClickListener(new OnClickListener() {
        			
        			@Override
        			public void onClick(View v) {
        				Intent myCollectionActivityIntent = new Intent(getApplicationContext(), MyCollectionActivity.class);
        				startActivity(myCollectionActivityIntent);        				
        			}
        		});
        	} else {
        		goToMyCollectionButton.setVisibility(View.GONE);
        	}
        	
        	if (isRightButtonVisible) {
	        	downloadAgainButton.setVisibility(View.VISIBLE);
	        	downloadAgainButton.setOnClickListener(new OnClickListener() {
	    			
	    			@Override
	    			public void onClick(View v) {
	    				// TODO Auto-generated method stub
	    				
	    			}
	    		});
	        } else {
	        	downloadAgainButton.setVisibility(View.GONE);
	        }
        }
        
        ImageButton closeButton = (ImageButton) downloadDialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				downloadDialog.dismiss();
				onBackPressed();
			}
		});
        downloadDialog.setCancelable(true);
        downloadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				downloadDialog.dismiss();
				onBackPressed();
			}
		});
        downloadDialog.show();

	}
	
	@Override
	protected NavigationItem getNavigationItem() {
		// TODO: Do MediaItem must be valid ?
		if (mMediaItem != null) {
			if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				return NavigationItem.VIDEOS;
				
			} else if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC) {
				return NavigationItem.MUSIC;
			}
		}
		
		return NavigationItem.OTHER;
	}

	
}
