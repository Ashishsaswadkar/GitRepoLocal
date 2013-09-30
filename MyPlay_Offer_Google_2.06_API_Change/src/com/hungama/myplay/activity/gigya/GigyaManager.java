package com.hungama.myplay.activity.gigya;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.simple.JSONObject;
import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts.Data;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenSource;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.TokenCachingStrategy;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.facebook.widget.WebDialog.RequestsDialogBuilder;
import com.gigya.socialize.GSArray;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.gigya.socialize.GSResponseListener;
import com.gigya.socialize.android.GSAPI;
import com.gigya.socialize.android.GSSession;
import com.gigya.socialize.android.event.GSEventListener;
import com.google.gson.Gson;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.SignOption;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupField;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;

/**
 * @author DavidSvilem
 */
public class GigyaManager implements GSEventListener{

	// GSObject keys
	public static final String PROVIDER = "provider";
	public static final String FACEBOOK_EXTRA_PERMISSIONS = "facebookExtraPermissions";
	public static final String FORCE_AUTHENTICATION = "forceAuthentication";
	public static final String ENABLED_PROVIDERS = "enabledProviders";
	public static final String RECIPIENTS = "recipients";
	public static final String BODY = "body";
	public static final String GRAPH_PATH = "graphPath";
	public static final String FEED = "feed";
	public static final String POST = "post";
	public static final String GRAPH_PARAMS = "graphParams";
	public static final String METHOD = "method";
	public static final String NAME = "name";
	public static final String LINK = "link";
	public static final String MESSAGE = "message";
	public static final String DESCRIPTION = "description";
	public static final String SUBJECT = "subject";
	public static final String FACEBOOK_APP_ID = "facebookAppId";	
	
	// Methods
	public static final String REMOVE_CONNECTION = "removeConnection";
	public static final String LOGIN = "login";
	public static final String ADD_CONNECTION  = "addConnection";
	
	// Requests
	public static final String SOCIALIZE_GET_USER_INFO = "socialize.getUserInfo";
	public static final String SOCIALIZE_GET_FRIENDS_INFO = "socialize.getFriendsInfo";
	public static final String SOCIALIZE_GET_CONTACTS = "socialize.getContacts";
	public static final String SOCIALIZE_SEND_NOTIFICATION = "socialize.sendNotification";
	public static final String SOCIALIZE_FACEBOOK_GRAPH_OPERATION = "socialize.facebookGraphOperation";
	public static final String SOCIALIZE_LOGOUT = "socialize.logout";
	public static final String SOCIALIZE_GET_SESSION_INFO = "socialize.getSessionInfo";

	
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	
	private Activity mActivity;
	
	private GSAPI gsAPI;
	
	private SignOption gigyaSignOption;
	
	public static SocialNetwork provider = SocialNetwork.NONE;
	private SocialNetwork currentProvider;
		
	private Map<String, Object> fieldMap;
	private Map<String, Object> signupFields;
	
	private static boolean isFBConnected;
	private static boolean isGoogleConnected;
	private static boolean isTwitterConnected;
	
	private String currenMethod = "";

	private String providerAuthToken = "EMPTY";
	
	private String facebookPermissions;
	
	public GigyaManager(Activity parent){
		this.mActivity = parent;
		
		mDataManager = DataManager.getInstance(parent.getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		
		gigyaSignOption = mApplicationConfigurations.getGigyaSignup();
		
		// You should create only one GSAPI object and retain it for the lifetime of your application.
		gsAPI = new GSAPI(parent.getResources().getString(R.string.gigya_api_key), mActivity);
		
		gsAPI.setEventListener(this);
	}
	
	public void setSession(String token, String secret){
		GSSession mGSSession = new GSSession(token, secret);
		gsAPI.setSession(mGSSession);
	}
	
	public void facebookLogin(){
		
		GSObject params = new GSObject();
		
		if (gigyaSignOption != null && gigyaSignOption.getFacebookPermissions() != null) {
			facebookPermissions = gigyaSignOption.getFacebookPermissions();
		} else {
			facebookPermissions = "email,publish_actions,publish_stream";
		}
		
		provider = SocialNetwork.FACEBOOK;
		
		params.put(PROVIDER, provider.toString().toLowerCase());
		params.put(FACEBOOK_EXTRA_PERMISSIONS, facebookPermissions);
		params.put(FORCE_AUTHENTICATION, false);
		params.put(FACEBOOK_APP_ID, mActivity.getResources().getString(R.string.gigya_facebook_application_id));

		try {
						
			if(isFBConnected){
				// Do Nothing
			}else{
				
				GSSession gsSession = gsAPI.getSession();
				
				if(gsSession != null && gsSession.isValid()){
					gsAPI.addConnection(params, new GigyaResponseListener(), mActivity);
				}else{
					gsAPI.login(params, new GigyaResponseListener(), null);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void twitterLogin(){
		
		provider = SocialNetwork.TWITTER;
		
		GSObject params = new GSObject();
		
		params.put(PROVIDER, SocialNetwork.TWITTER.toString().toLowerCase());
		
		try {
			
			if(isTwitterConnected){
				// Do Nothing
			}else{
				
				GSSession gsSession = gsAPI.getSession();
				
				if(gsSession != null && gsSession.isValid()){
					gsAPI.addConnection(params, new GigyaResponseListener(), mActivity);
				}else{
					gsAPI.login(params, new GigyaResponseListener(), mActivity);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	public void googleLogin(){
		
		provider = SocialNetwork.GOOGLE;
		
		GSObject params = new GSObject();
		
		params.put(PROVIDER, SocialNetwork.GOOGLE.toString().toLowerCase());
		
		try {
			
			if(isGoogleConnected){
				// Do Nothing
			}else{
				
				GSSession gsSession = gsAPI.getSession();
				
				if(gsSession != null && gsSession.isValid()){
					gsAPI.addConnection(params, new GigyaResponseListener(), mActivity);
				}else{
					gsAPI.login(params, new GigyaResponseListener(), mActivity);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public class GigyaResponseListener implements GSResponseListener{

		@Override
		public void onGSResponse(String method, GSResponse response, Object context) {
			
			if(response.getErrorCode() == 0){ // SUCCESS! response status = OK
				
				if(method.equalsIgnoreCase(REMOVE_CONNECTION)){
					
					// Do Nothing
					
				}else if(method.equalsIgnoreCase(LOGIN) || method.equalsIgnoreCase(ADD_CONNECTION)){
					
					// Get the values from Gigya response
					GSArray gsArray = response.getArray("identities", null);
					checkForSocialNetworkConnected(gsArray);
					
					// Fill up the fields 
					signupFields = new HashMap<String, Object>();	
					
					SignupField gigyaEmail = null;
					SignupField firstName = null;
					SignupField lastName = null;
					SignupField loginProvider = null;
					SignupField loginProviderUid = null;
					SignupField isSiteUid = null;
					SignupField uid = null;
					
					// For any provider
					for(SignupField field : gigyaSignOption.getSignupFields()){
						
						String name = field.getName();
						
						if(name.equalsIgnoreCase("gigya_email")){
							gigyaEmail = field;
						}else if(name.equalsIgnoreCase("first_name")){
							firstName = field;
						}else if(name.equalsIgnoreCase("last_name")){
							lastName = field;
						}else if(name.equalsIgnoreCase("login_provider")){
							loginProvider = field;
						}else if(name.equalsIgnoreCase("login_provider_uid")){
							loginProviderUid = field;
						}else if(name.equalsIgnoreCase("is_site_uid")){
							isSiteUid = field;
						}else if(name.equalsIgnoreCase("uid")){
							uid = field;
						}else if(name.equalsIgnoreCase("email")){
							
						}else if(name.equalsIgnoreCase("password")){
							
						}else if(name.equalsIgnoreCase("hardware_id")){
							signupFields.put("hardware_id", "");
						}else if(name.equalsIgnoreCase("partner_user_id")){
							signupFields.put("partner_user_id", "");
						}
					}
										
					for(int idx = 0; idx < gsArray.length(); idx++){
						
						GSObject gsObj = gsArray.getObject(idx);
						
						String currProvider = gsObj.getString("provider", null);
						
						String email = null;
						String fn = null;
						String ln = null;
						
						// Set the thunmbnailURL in shared preferences
						String url = gsObj.getString("thumbnailURL", null);
						
						// Save the email to SharedPref
						email = gsObj.getString("email", null);
						
						// Save First Name to SharedPref
						fn = gsObj.getString("firstName","");
						
						// Save Last Name to SharedPref
						ln = gsObj.getString("lastName", "");
						
						if(currProvider.equalsIgnoreCase(SocialNetwork.TWITTER.toString().toLowerCase())){
							
							// Thumbail URL
							mApplicationConfigurations.setGiGyaTwitterThumbUrl(url);
							
							// First Name
							mApplicationConfigurations.setGigyaTwitterFirstName(fn);
							
							//Last Name
							mApplicationConfigurations.setGigyaTwitterLastName(ln);
							
							// Email
							mApplicationConfigurations.setGigyaTwitterEmail(email);
							
						}else if(currProvider.equalsIgnoreCase(SocialNetwork.FACEBOOK.toString().toLowerCase())){
							
							// Thumbail URL
							mApplicationConfigurations.setGiGyaFBThumbUrl(url);
							
							// First Name
							mApplicationConfigurations.setGigyaFBFirstName(fn);
							
							//Last Name
							mApplicationConfigurations.setGigyaFBLastName(ln);
							
							// Email
							mApplicationConfigurations.setGigyaFBEmail(email);
							
						}else if(currProvider.equalsIgnoreCase(SocialNetwork.GOOGLE.toString().toLowerCase())){
							
							// First Name
							mApplicationConfigurations.setGigyaGoogleFirstName(fn);
							
							//Last Name
							mApplicationConfigurations.setGigyaGoogleLastName(ln);
							
							// Email
							mApplicationConfigurations.setGigyaGoogleEmail(email);
						}
						
						
						if(currProvider.equalsIgnoreCase(provider.toString().toLowerCase())){
							
							fieldMap = new HashMap<String, Object>();// inner
							fieldMap.put("value", email);
							signupFields.put(gigyaEmail.getName(), fieldMap);
							
							fieldMap = new HashMap<String, Object>();// inner
							fieldMap.put("value", fn);
							signupFields.put(firstName.getName(), fieldMap);
							
							fieldMap = new HashMap<String, Object>();// inner
							fieldMap.put("value", ln);
							signupFields.put(lastName.getName(), fieldMap);
							
							fieldMap = new HashMap<String, Object>();// inner
							fieldMap.put("value", gsObj.getString("provider", null));
							signupFields.put(loginProvider.getName(), fieldMap);
							
							fieldMap = new HashMap<String, Object>();// inner
							fieldMap.put("value", gsObj.getString("providerUID", null));
							signupFields.put(loginProviderUid.getName(), fieldMap);
							
							fieldMap = new HashMap<String, Object>();// outer 
							fieldMap.put("value", response.getBool("isSiteUID", false));
							signupFields.put(isSiteUid.getName(), fieldMap);
							
							fieldMap = new HashMap<String, Object>();// outer
							fieldMap.put("value", response.getString("UID", null));
							signupFields.put(uid.getName(), fieldMap);
						}
						
					}
					
					// If the provider is Twitter then the user first needs to filled Email and Password (optional)
					// and then make the PCP call to Hungama.
					if (provider == SocialNetwork.TWITTER){
						// Provider: Twitter
						if (mListener != null) {
							mListener.onGigyaLoginListener(provider, signupFields, gigyaSignOption.getSetID());
						}
						
					} else { 
						// Providers: FaceBook, Google
						if (mListener != null) {
							mListener.onGigyaLoginListener(provider, signupFields, gigyaSignOption.getSetID());
						}
					}
					
					if(currenMethod.equalsIgnoreCase(SOCIALIZE_GET_FRIENDS_INFO) ||
				       currenMethod.equalsIgnoreCase(SOCIALIZE_GET_CONTACTS)){
						
						socializeGetFriendsInfo(currentProvider);
					}
					
				}else if(method.equalsIgnoreCase(SOCIALIZE_GET_USER_INFO)){
					
					// Get the values from Gigya response
					GSArray gsArray = response.getArray("identities", null);
					
					checkForSocialNetworkConnected(gsArray);
				
					if (mListener != null) {
						mListener.onSocializeGetUserInfoListener();
					}
					
					if(currenMethod.equalsIgnoreCase(SOCIALIZE_GET_FRIENDS_INFO) ||
					   currenMethod.equalsIgnoreCase(SOCIALIZE_GET_CONTACTS)){
						
						socializeGetFriendsInfo(currentProvider);
						
					}else{
						
					}
					
				}else if(method.equalsIgnoreCase(SOCIALIZE_GET_FRIENDS_INFO) ||
						 method.equalsIgnoreCase(SOCIALIZE_GET_CONTACTS)){
					
					if(currentProvider == SocialNetwork.FACEBOOK){
						
						String json = response.getResponseText();
						Gson gson = new Gson();
						Friend mFriend = gson.fromJson(json, Friend.class);
						
						if (mListener != null) {
							mListener.onSocializeGetFriendsInfoListener(mFriend.friends);
						}
						
					}else if(currentProvider == SocialNetwork.TWITTER){
						
						String json = response.getResponseText();
						Gson gson = new Gson();
						Friend mFriend = gson.fromJson(json, Friend.class);
						
						if (mListener != null) {
							mListener.onSocializeGetFriendsInfoListener(mFriend.friends);
						}
						
					}else if(currentProvider == SocialNetwork.GOOGLE){
						
						String json = response.getResponseText();
						Gson gson = new Gson();
						Contact mContact = gson.fromJson(json, Contact.class);
						
						if (mListener != null) {
							mListener.onSocializeGetContactsListener(mContact.contacts);
						}
					}
					
				}else if(method.equalsIgnoreCase(SOCIALIZE_FACEBOOK_GRAPH_OPERATION)){
					// FaceBook Invitation
					mListener.onFacebookInvite();
					
				}else if(method.equalsIgnoreCase(SOCIALIZE_SEND_NOTIFICATION)){
					// Twitter Invitation
					mListener.onTwitterInvite();
					
				}else if(method.equalsIgnoreCase(SOCIALIZE_GET_SESSION_INFO)){
					String json = response.getResponseText();
					
					Gson gson = new Gson();
					AuthToken authToken = gson.fromJson(json, AuthToken.class);
					
					providerAuthToken = authToken.authToken;
				}
				
			}else{ // Error
				
				if(response.getErrorCode() == 100001){
					
					if(method.equalsIgnoreCase(SOCIALIZE_GET_FRIENDS_INFO)||
					   method.equalsIgnoreCase(SOCIALIZE_GET_CONTACTS)){
						
						socializeGetFriendsInfo(currentProvider);
					}
				}
				
				if(method.equalsIgnoreCase(SOCIALIZE_GET_USER_INFO)){
					
					if(currenMethod == SOCIALIZE_GET_USER_INFO){
						isFBConnected = false;
						isTwitterConnected = false;
						isGoogleConnected = false;	
						
					}else if(currenMethod == SOCIALIZE_GET_FRIENDS_INFO ||
				       currenMethod	== SOCIALIZE_GET_CONTACTS){
						
						if(currentProvider == SocialNetwork.FACEBOOK){
							facebookLogin();
							
						}else if(currentProvider == SocialNetwork.TWITTER){
							twitterLogin();
							
						}else if(currentProvider == SocialNetwork.GOOGLE){
							googleLogin();
						}
						
					}else{
						
						if(response.getErrorCode() != 500026){
							// 500026 = no Internet connection
							// If it's not an Internet connection problem then set all networks as log out
							isFBConnected = false;
							isTwitterConnected = false;
							isGoogleConnected = false;
						}
						
					}
					
					mListener.onSocializeGetUserInfoListener();	
				}
			}
		}
	}
		
	// Get the status of social network connection
	public boolean isFBConnected(){
		return GigyaManager.isFBConnected;
	}
	
	public void setIsFBConnected(boolean value) {
		GigyaManager.isFBConnected = value;
	}
	
	public boolean isTwitterConnected(){
		return GigyaManager.isTwitterConnected;
	}
	
	public void setIsTwitterConnected(boolean value) {
		GigyaManager.isTwitterConnected = value;
	}
	
	public boolean isGoogleConnected(){
		return GigyaManager.isGoogleConnected;
	}
	
	public void setIsGoogleConnected(boolean value) {
		GigyaManager.isGoogleConnected = value;
	}
	
	// Remove Connection
	public void removeConnetion(SocialNetwork provider){
		
		GSObject params = new GSObject();
		params.put(PROVIDER, provider.toString().toLowerCase());
		gsAPI.removeConnetion(params, new GigyaResponseListener(), mActivity);
	}
		
	// Get User Info
	public void socializeGetUserInfo(){
			
		GSObject dummy = new GSObject();
		gsAPI.sendRequest(SOCIALIZE_GET_USER_INFO, dummy, new GigyaResponseListener(), mActivity);
	}
	
	// Get Friends Info
	public void getSocialNetworkFriends(SocialNetwork provider){
		
		if(provider == SocialNetwork.FACEBOOK){
			currenMethod = SOCIALIZE_GET_FRIENDS_INFO;
		}else if(provider == SocialNetwork.TWITTER){
			currenMethod = SOCIALIZE_GET_FRIENDS_INFO;
		}else if(provider == SocialNetwork.GOOGLE){
			currenMethod = SOCIALIZE_GET_CONTACTS;
		}
	
		currentProvider = provider;
		
		socializeGetUserInfo();
	}
	
	
	
	// Get social network friends list from Gigya
	private void socializeGetFriendsInfo(SocialNetwork provider){
		
		GSObject params = new GSObject();
		params.put(ENABLED_PROVIDERS, provider.toString().toLowerCase());
				
		if(provider == SocialNetwork.FACEBOOK){
			
			if(!isFBConnected){
				
				currenMethod = SOCIALIZE_GET_FRIENDS_INFO;
				currentProvider = SocialNetwork.FACEBOOK;
				
				facebookLogin();
			}else{
				gsAPI.sendRequest(SOCIALIZE_GET_FRIENDS_INFO, params, new GigyaResponseListener(), mActivity);				
			}
			
		}else if(provider == SocialNetwork.TWITTER){
			
			if(!isTwitterConnected){
				
				currenMethod = SOCIALIZE_GET_FRIENDS_INFO;
				currentProvider = SocialNetwork.TWITTER;
				
				twitterLogin();
				
			}else{
				gsAPI.sendRequest(SOCIALIZE_GET_FRIENDS_INFO, params, new GigyaResponseListener(), mActivity);				
			}
			
		}else if(provider == SocialNetwork.GOOGLE){
			
			if(!isGoogleConnected){
				
				currenMethod = SOCIALIZE_GET_CONTACTS;
				currentProvider = SocialNetwork.GOOGLE;
				
				googleLogin();
				
			}else{
				gsAPI.sendRequest(SOCIALIZE_GET_CONTACTS, params, new GigyaResponseListener(), mActivity);				
			}
		}
	}
	
	private void checkForSocialNetworkConnected(GSArray gsArray){
		if (gsArray == null) {
			isFBConnected = false;
			isTwitterConnected = false;
			isGoogleConnected = false;
			return;
		}
		for(int idx = 0; idx < gsArray.length(); idx++){
			
			GSObject gsObj = gsArray.getObject(idx);
			
			String currProvider = gsObj.getString("provider", null);
			
			if(currProvider.equalsIgnoreCase(SocialNetwork.FACEBOOK.toString())){
				isFBConnected = true;
			}else if(currProvider.equalsIgnoreCase(SocialNetwork.TWITTER.toString())){
				isTwitterConnected = true;
			}else if(currProvider.equalsIgnoreCase(SocialNetwork.GOOGLE.toString())){
				isGoogleConnected = true;
			}else{
			}
		}
	}
	
	// 
	// Invite Friends
	//
	public void socializeSendNotification(List<FBFriend> friends){
		// Twitter
		
		if(friends != null && !friends.isEmpty()){
			GSObject params = new GSObject();
			
			StringBuilder contactsUIDs = new StringBuilder();
			
			// A comma separated list of UIDs representing recipients that will receive this notification
			for(FBFriend friend : friends){
				contactsUIDs.append(friend.UID).append(",");
			}
			
			// Delete the last comma
			if (contactsUIDs.length() > 0) {
				contactsUIDs.deleteCharAt(contactsUIDs.length()-1);
			}
			
			params.put(RECIPIENTS, contactsUIDs.toString());
			params.put(BODY, mActivity.getResources().getString(R.string.twitter_body_text));
			params.put(SUBJECT, R.string.facebook_twitter_body_text);
			
			// Send the request
			gsAPI.sendRequest(SOCIALIZE_SEND_NOTIFICATION, params, new GigyaResponseListener(), mActivity);
		}
	}
	
	public void socializeFacebookGraphOperation(List<FBFriend> friends){
		// FaceBook
		
		if(friends != null && !friends.isEmpty()){
			GSObject params;
			
			StringBuilder inviteStr = null;
			
			for(FBFriend friend : friends){
				
				inviteStr = new StringBuilder();
				
				inviteStr.append("/")
						 .append(friend.identities.get(0).providerUID)
						 .append("/")
						 .append(FEED);
				
				params = new GSObject();
				
				params.put(GRAPH_PATH, inviteStr.toString());
				
				params.put(METHOD, POST);
				
				JSONObject jsonObject = new JSONObject();
				
				jsonObject.put(NAME, "Hungama, the ultimate music app");
				jsonObject.put(LINK, "http://www.hungama.com");
				jsonObject.put(MESSAGE, mActivity.getString(R.string.facebook_twitter_body_text));
				jsonObject.put(DESCRIPTION, mActivity.getString(R.string.facebook_message_text));
				
				params.put(GRAPH_PARAMS, jsonObject.toJSONString());
				
				// Send the request
				gsAPI.sendRequest(SOCIALIZE_FACEBOOK_GRAPH_OPERATION,
								  params, 
								  new GigyaResponseListener(),
									
								  mActivity);
				
			}
		}
	}
	
	//
	// Interface
	//
	OnGigyaResponseListener mListener;
	
	public interface OnGigyaResponseListener{
		
		public void onGigyaLoginListener(SocialNetwork provider,Map<String, Object> signupFields, long setId);
		
		public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList);
		
		public void onSocializeGetContactsListener(List<GoogleFriend> googleFriendsList);
		
		public void onSocializeGetUserInfoListener();
		
		public void onGigyaLogoutListener();
		
		public void onFacebookInvite();
		
		public void onTwitterInvite();
	}
	
	public void setOnGigyaResponseListener(OnGigyaResponseListener l){
		this.mListener = l;
	}
	
	public void logout(){
		if(gsAPI != null){
			gsAPI.logout();
		}
	}

	// GSEventListener
	@Override
	public void onLogin(String provider, GSObject user, Object context) {
		System.out.println("");
	}

	@Override
	public void onLogout(Object context) {
		mListener.onGigyaLogoutListener();
	}

	@Override
	public void onConnectionAdded(String provider, GSObject user, Object context) {}

	@Override
	public void onConnectionRemoved(String provider, Object context) {}
	
	public void cancelGigyaProviderLogin () {
		if (GigyaManager.provider != SocialNetwork.NONE) {
			if (GigyaManager.provider == SocialNetwork.FACEBOOK) {
				setIsFBConnected(false);
				removeConnetion(SocialNetwork.FACEBOOK);
			} else if (GigyaManager.provider == SocialNetwork.TWITTER) {
				setIsTwitterConnected(false);
				removeConnetion(SocialNetwork.TWITTER);
			} else if (GigyaManager.provider == SocialNetwork.GOOGLE) {
				setIsGoogleConnected(false);
				removeConnetion(SocialNetwork.GOOGLE);
			}
			
			if (!mApplicationConfigurations.isRealUser()){
				logout();
			}
		}
	}
	
	public void inviteFacebookFriends(final String friendsProviderUIDs, final Activity activity){
		
		socializeGetSessionInfo(SocialNetwork.FACEBOOK, new GSResponseListener() {
			
			@Override
			public void onGSResponse(String method, GSResponse response, Object context) {
				
				String json = response.getResponseText();
				
				Gson gson = new Gson();
				AuthToken mAuthToken = gson.fromJson(json, AuthToken.class);
				
		        String authToken = mAuthToken.authToken;
				String tokenExpiration = mAuthToken.tokenExpiration;
				
		    	String dateFormatUTC = "yyyy-MM-dd HH:mm:ss";
				SimpleDateFormat sdfUTC = new SimpleDateFormat(dateFormatUTC,Locale.ENGLISH);//Changes by Hungama

				Date date = null;
				try {
					date = sdfUTC.parse(tokenExpiration);
				} catch (ParseException e) {
					e.printStackTrace();
				} 
				
				String permissions = gigyaSignOption.getFacebookPermissions();
				
				AccessToken accessToken = AccessToken.createFromExistingAccessToken(
						authToken, 
						date, 
						null, 
						AccessTokenSource.FACEBOOK_APPLICATION_NATIVE, 
						Arrays.asList(permissions.split(",")));
		        
		        Session.openActiveSessionWithAccessToken(
		        		activity,
		        		accessToken , 
		        		new Session.StatusCallback() {

		            @Override
		            public void call(Session session, SessionState state, Exception exception) {
		                if(session != null && session.isOpened()) {
		                    Session.setActiveSession(session);
		                }
		            }
		        });
		        
				Bundle parameters = new Bundle();
				parameters.putString("to", friendsProviderUIDs);
				parameters.putString("message", activity.getString(R.string.invite_facebook_message));
				parameters.putString("title",  activity.getString(R.string.invite_facebook_title));

				WebDialog requestsDialog = (
						new WebDialog.RequestsDialogBuilder(
								activity,
								Session.getActiveSession(),
								parameters))
								.setOnCompleteListener(new OnCompleteListener() {

									@Override
									public void onComplete(Bundle values,
											FacebookException error) {
										if (error != null) {
											if (error instanceof FacebookOperationCanceledException) {
												Toast.makeText(activity.getApplicationContext(), 
														"Request cancelled", 
														Toast.LENGTH_SHORT).show();
											} else {
												Toast.makeText(activity.getApplicationContext(), 
														"Network Error", 
														Toast.LENGTH_SHORT).show();
											}
										} else {
											final String requestId = values.getString("request");
											if (requestId != null) {
												Toast.makeText(activity.getApplicationContext(), 
														"Request sent",  
														Toast.LENGTH_SHORT).show();
												
												// Inform the Activity/Fragment that the invite to FB is finished
												mListener.onFacebookInvite();
												
											} else {
												Toast.makeText(activity.getApplicationContext(), 
														"Request cancelled", 
														Toast.LENGTH_SHORT).show();
											}
										}   
									}
								})
								.build();

				requestsDialog.show();
			}
		});
		
	}
	
	private void socializeGetSessionInfo(SocialNetwork socialNetworkProvider, GSResponseListener listener){
		
		// Delete the last providerAuthToken
		providerAuthToken = "EMPTY";
		
		GSObject params = new GSObject();
		
		params.put(PROVIDER, socialNetworkProvider.toString().toLowerCase());
		
		// Send the request
		gsAPI.sendRequest(SOCIALIZE_GET_SESSION_INFO,
						  params, 
						  listener,
						  null);
	}
}
