package com.hungama.myplay.activity.operations.hungama;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.BadgesAndCoins;
import com.hungama.myplay.activity.ui.BadgesAndCoinsActivity;
import com.hungama.myplay.activity.ui.VideoActivity;
import com.hungama.myplay.activity.util.ToastExpander;
import com.hungama.myplay.activity.util.Utils;

/**
 * Decorator Operation for adding validation of the UnAuthorized error message <b>Not in uses</b>
 * Sending the device's type and it's resolution.
 */
public class HungamaWrapperOperation extends HungamaOperation {
	
	private final Context mContext;
	private final HungamaOperation mHungamaOperation;
	private final CommunicationOperationListener mListener;
	
	private UIHandler uiHandler;
	
	protected static final String KEY_POINTS_EARNED = "points_earned";
	protected static final String KEY_BADGE_EARNED = "badge_earned";
	protected static final String KEY_NEXT_DESCRIPTION = "next_description";
	protected static final String KEY_BADGE_DATA = "badge_data";
	protected static final String KEY_BADGE = "badge";
	protected static final String KEY_BADGE_URL = "badge_url";
	
	public HungamaWrapperOperation(CommunicationOperationListener listener, Context context, HungamaOperation hungamaOperation) {
		mContext = context;
		mHungamaOperation = hungamaOperation;
		mListener = listener;
	}

	@Override
	public int getOperationId() {
		return mHungamaOperation.getOperationId();
	}

	@Override
	public RequestMethod getRequestMethod() {
		return mHungamaOperation.getRequestMethod();
	}

	@Override
	public String getServiceUrl(final Context context) {
		if (getRequestMethod() == RequestMethod.GET) {
			// adds the device name "android" and its size (string density). 
			String serviceUrl = mHungamaOperation.getServiceUrl(context) 
							+ AMPERSAND + PARAMS_DEVICE + EQUALS + DataManager.DEVICE
							+ AMPERSAND + PARAMS_SIZE + EQUALS + DataManager.getDisplayDensityLabel();
			
			return serviceUrl;
		}

		return mHungamaOperation.getServiceUrl(context);
	}

	@Override
	public String getRequestBody() {
		return mHungamaOperation.getRequestBody();
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
					InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		parseResponseForBadges(response);
		
		return mHungamaOperation.parseResponse(response);
	}
	
	private void parseResponseForBadges(String response) {
		JSONParser jsonParser = new JSONParser();
		BadgesAndCoins objFromOperation = new BadgesAndCoins();	
		Map<String, Object> responseMap;
		try {
			responseMap = (Map<String, Object>) jsonParser.parse(response);
			Map<String, Object> badgesCoinsMap = null;
			if (responseMap.containsKey(KEY_RESPONSE)) {
				badgesCoinsMap = (Map<String, Object>) responseMap.get(KEY_RESPONSE);
			} 
//			else if (responseMap.containsKey(KEY_CATALOG)) {
//				badgesCoinsMap = (Map<String, Object>) responseMap.get(KEY_CATALOG);
//			}
			if (badgesCoinsMap != null) {
				if (badgesCoinsMap.containsKey(KEY_DISPLAY)) {							
					Object displayObj = ((Object) badgesCoinsMap.get(KEY_DISPLAY));
					String displayStr = String.valueOf(displayObj);
					int display;
					if (displayStr.equalsIgnoreCase("false")) {
						display = 0;
					} else if (displayStr.equalsIgnoreCase("true")) {
						display = 1;
					} else {
						display = Integer.valueOf(displayStr);
					}					
//					int display = ((Long) badgesCoinsMap.get(KEY_DISPLAY)).intValue();
					if (display == 1) {
						//Toast					
						String message = (String) badgesCoinsMap.get(KEY_MESSAGE);
						if (message != null && message != Utils.TEXT_EMPTY) {
							HandlerThread uiThread = new HandlerThread("UIHandler");
						    uiThread.start();
						    uiHandler = new UIHandler(uiThread.getLooper(), mContext.getApplicationContext());
						    handleUIRequestToast(message);
	//						Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
						}
					} else if (display == 0) {
						if (badgesCoinsMap.containsKey(KEY_POINTS_EARNED) && badgesCoinsMap.containsKey(KEY_BADGE_EARNED)) {
							int pointsEarned = ((Long) badgesCoinsMap.get(KEY_POINTS_EARNED)).intValue();
							int badgesEarned = ((Long) badgesCoinsMap.get(KEY_BADGE_EARNED)).intValue();
							if (pointsEarned > 0 ) {
								objFromOperation.setPointsEarned(pointsEarned);
								if (badgesEarned == 0) {
									String nextDescription = (String) badgesCoinsMap.get(KEY_NEXT_DESCRIPTION);
									if (nextDescription == Utils.TEXT_EMPTY) {
	//										boolean isNextDescriptionEmpty = true;
										objFromOperation.setDisplayCase(BadgesAndCoins.CASE_COINS_2_LINES);	
										String message = (String) badgesCoinsMap.get(KEY_MESSAGE);
										objFromOperation.setMessage(message);
										if (mListener instanceof VideoActivity && mHungamaOperation instanceof AddToFavoriteOperation) {
											DataManager dataManager = DataManager.getInstance(mContext);
											ApplicationConfigurations mApplicationConfigurations = dataManager.getApplicationConfigurations();
											mApplicationConfigurations.setBadgesAndCoinsForVideoActivity(objFromOperation);
										} else {
											startBadgesAndCoinsActivity(objFromOperation);
										}
										
									} else {
										objFromOperation.setDisplayCase(BadgesAndCoins.CASE_COINS_3_LINES);
										objFromOperation.setNextDescription(nextDescription);
										String message = (String) badgesCoinsMap.get(KEY_MESSAGE);
										objFromOperation.setMessage(message);
										if (mListener instanceof VideoActivity && mHungamaOperation instanceof AddToFavoriteOperation) {
											DataManager dataManager = DataManager.getInstance(mContext);
											ApplicationConfigurations mApplicationConfigurations = dataManager.getApplicationConfigurations();
											mApplicationConfigurations.setBadgesAndCoinsForVideoActivity(objFromOperation);
										} else {
											startBadgesAndCoinsActivity(objFromOperation);
										}										
									}
								} else if (badgesEarned > 0) {
									String nextDescription = (String) badgesCoinsMap.get(KEY_NEXT_DESCRIPTION);
									if (nextDescription == Utils.TEXT_EMPTY) {
										objFromOperation.setDisplayCase(BadgesAndCoins.CASE_COINS_2_LINES_AND_BADGE);
										String message = (String) badgesCoinsMap.get(KEY_MESSAGE);
										objFromOperation.setMessage(message);
	//									startBadgesAndCoinsActivity(objFromOperation);
									} else {
										objFromOperation.setDisplayCase(BadgesAndCoins.CASE_COINS_3_LINES_AND_BADGE);
										objFromOperation.setNextDescription(nextDescription);
										String message = (String) badgesCoinsMap.get(KEY_MESSAGE);
										objFromOperation.setMessage(message);
	//									startBadgesAndCoinsActivity(objFromOperation);
									}
									if (badgesCoinsMap.containsKey(KEY_BADGE_DATA)) {
										Map<String, Object> badgesDataMap = (Map<String, Object>) badgesCoinsMap.get(KEY_BADGE_DATA);									
										if (badgesDataMap.containsKey(KEY_BADGE) && badgesDataMap.containsKey(KEY_BADGE_URL)) {
											String badge = (String) badgesDataMap.get(KEY_BADGE);
											String badgeUrl = (String) badgesDataMap.get(KEY_BADGE_URL);
											objFromOperation.setBadgeName(badge);
											objFromOperation.setBadgeUrl(badgeUrl);
											if (mListener instanceof VideoActivity && mHungamaOperation instanceof AddToFavoriteOperation) {
												DataManager dataManager = DataManager.getInstance(mContext);
												ApplicationConfigurations mApplicationConfigurations = dataManager.getApplicationConfigurations();
												mApplicationConfigurations.setBadgesAndCoinsForVideoActivity(objFromOperation);
											} else {
												startBadgesAndCoinsActivity(objFromOperation);
											}
										}
									}
								}
							}
						}
						
					}								
				}			
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void startBadgesAndCoinsActivity(BadgesAndCoins badgesAndCoins) {
		Intent intent = new Intent(mContext, BadgesAndCoinsActivity.class);
		intent.putExtra(BadgesAndCoinsActivity.ARGUMENT_OBJECT, (Serializable) badgesAndCoins);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}
	
	protected void handleUIRequestToast(String message) {
	    Message msg = uiHandler.obtainMessage(UIHandler.DISPLAY_UI_TOAST);
	    msg.obj = message;
	    uiHandler.sendMessage(msg);
	}
	
	protected void handleUIRequestDialog(String message) {
	    Message msg = uiHandler.obtainMessage(UIHandler.DISPLAY_UI_DIALOG);
	    msg.obj = message;
	    uiHandler.sendMessage(msg);
	}

	private static class UIHandler extends Handler {
	    
		public static final int DISPLAY_UI_TOAST = 0;
	    public static final int DISPLAY_UI_DIALOG = 1;
	    
	    private final WeakReference<Context> applicationContextReference;
	    
	    public UIHandler(Looper looper, Context context) {
	        super(looper);
	        applicationContextReference = new WeakReference<Context>(context);
	    }

	    @Override
	    public void handleMessage(Message msg) {
	        switch(msg.what) {
	        case UIHandler.DISPLAY_UI_TOAST: {
	        	// get the real reference to the context.
	        	Context applicationContext = applicationContextReference.get();
	        	
	        	if (applicationContext != null) {
	        		Toast t = Toast.makeText(applicationContext, (String) msg.obj, Toast.LENGTH_LONG);
		            t.setGravity(Gravity.CENTER, 0, 0);
		            ToastExpander.showFor(t, 4000);
	        	}
	            
	        } default:
	            break;
	        }
	    }
	}
	
}
