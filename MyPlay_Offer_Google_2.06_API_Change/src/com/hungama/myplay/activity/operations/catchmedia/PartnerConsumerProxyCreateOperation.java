package com.hungama.myplay.activity.operations.catchmedia;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupField;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.services.GCMRegistrationService;

public class PartnerConsumerProxyCreateOperation extends CMOperation {

	private static final String TAG = "PartnerConsumerProxyCreateOperation";

	public static final String RESPONSE_KEY_OBJECT_SIGNUP_FIELDS = "response_key_object_signup_fields"; 

	private static final String KEY_SIGNUP_FIELDS = "signup_fields";
	private static final String KEY_SET_ID = "set_id";
	
	private final Map<String, Object> mSignupFields;
	private final long mSetID;

	private boolean isSkipSelected;
	private Context mContext;
	private ApplicationConfigurations mApplicationConfigurations;
	
	public PartnerConsumerProxyCreateOperation(Context context, Map<String, Object> signupFields, long setId, boolean value) {
		super(context);
		
		mSignupFields = signupFields;
		mSetID = setId;
		isSkipSelected = value;
		mContext = context;
		mApplicationConfigurations = new ApplicationConfigurations(mContext);
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.CREATE;
	}

	@Override
	public Map<String, Object> getDescriptor() {
		
		Map<String, Object> descriptorMap = new HashMap<String, Object>();
		descriptorMap.put(ServerConfigurations.PARTNER_ID, pServerConfigurations.getPartnerId());
		descriptorMap.put(KEY_SIGNUP_FIELDS, mSignupFields);
		descriptorMap.put(KEY_SET_ID, mSetID);
		descriptorMap.put(ServerConfigurations.APPLICATION_CODE, pServerConfigurations.getAppCode());
		descriptorMap.put(DeviceConfigurations.DEVICE_MODEL_NAME, pDeviceConfigurations.getDeviceModelName());

		return descriptorMap;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.PARTNER_CONSUMER_PROXY_CREATE;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
	InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {

		JSONParser parser = new JSONParser();
		try {
			Map<String, Object> activationMap = (Map<String, Object>) parser.parse(response);
			Map<String, Object> responseMap =  new HashMap<String, Object>();

			if (!activationMap.containsKey(ApplicationConfigurations.ACTIVATION_CODE) || 
					!activationMap.containsKey(ApplicationConfigurations.PARTNER_USER_ID)) {
				throw new InvalidResponseDataException();
			}

			String activationCode = (String) activationMap.get(ApplicationConfigurations.ACTIVATION_CODE);
			String partnerUserId = (String) activationMap.get(ApplicationConfigurations.PARTNER_USER_ID);
			boolean isRealUser = Boolean.parseBoolean((String) activationMap.get(ApplicationConfigurations.IS_REAL_USER));

			// Save the gigya login token and secret
			String gigyaLoginSessionToken = 
					(String) activationMap.get(ApplicationConfigurations.GIGYA_LOGIN_SESSION_TOKEN);
			String gigyaLoginSessionSecret = 
					(String) activationMap.get(ApplicationConfigurations.GIGYA_LOGIN_SESSION_SECRET);

			if(!TextUtils.isEmpty(gigyaLoginSessionToken)&& 
			   !TextUtils.isEmpty(gigyaLoginSessionSecret)){
				pApplicationConfigurations.setGigyaSessionToken(gigyaLoginSessionToken);
				pApplicationConfigurations.setGigyaSessionSecret(gigyaLoginSessionSecret);
			}

			if(isSkipSelected){
				String skippedPartnerUserId = 
						(String) activationMap.get(ApplicationConfigurations.PARTNER_USER_ID);
				if(!TextUtils.isEmpty(skippedPartnerUserId)){
					pApplicationConfigurations.setSkippedPartnerUserId(skippedPartnerUserId);					
				}
			}
			
			if (mSignupFields != null && mSignupFields.containsKey("phone_number")) {
				Map<String, Object> fieldMap = (Map<String, Object>) mSignupFields.get("phone_number");
				String value = "";
				if (fieldMap != null) {
				value = (String) fieldMap.get(SignupField.VALUE);
				}
				pApplicationConfigurations.setUserLoginPhoneNumber(value);
			}
			
			pApplicationConfigurations.setPartnerUserId(partnerUserId);
			
			boolean realUser = pApplicationConfigurations.isRealUser();
			
			if(!realUser && isRealUser){
				// 
				pApplicationConfigurations.setConsumerRevision(0);
				pApplicationConfigurations.setHouseholdRevision(0);
				
				// Delete all locale playlists on device
				DataManager mDataManager = DataManager.getInstance(getContext());
				Map<Long, Playlist> empty = new HashMap<Long, Playlist>();
				mDataManager.storePlaylists(empty);
			}
			
			pApplicationConfigurations.setIsRealUser(isRealUser);
			
			responseMap.put(ApplicationConfigurations.ACTIVATION_CODE, activationCode);
			responseMap.put(ApplicationConfigurations.PARTNER_USER_ID, partnerUserId);
			responseMap.put(ApplicationConfigurations.IS_REAL_USER, isRealUser);
			responseMap.put(RESPONSE_KEY_OBJECT_SIGNUP_FIELDS, mSignupFields);
			
			// Grab the GCM registration id and send to Hungama.
			Intent gcmRegistrationServiceIntent = new Intent(mContext, GCMRegistrationService.class);
			gcmRegistrationServiceIntent.putExtra(GCMRegistrationService.EXTRA_REGISTRATION_ID, mApplicationConfigurations.getRegistrationId());			
			mContext.startService(gcmRegistrationServiceIntent);
			
			return responseMap;

		} catch (ParseException exception) {
			exception.printStackTrace();
		}

		return null;
	}

}
