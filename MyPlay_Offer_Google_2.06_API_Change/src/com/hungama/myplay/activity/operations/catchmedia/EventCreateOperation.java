package com.hungama.myplay.activity.operations.catchmedia;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.events.CampaignPlayEvent;
import com.hungama.myplay.activity.data.events.Event;
import com.hungama.myplay.activity.data.events.PlayEvent;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Posts user events such as playing music and campaigns events.
 */
public class EventCreateOperation extends CMOperation {
	
	private static final String TAG = "EventCreateOperation";
	
	public static final String RESULT_KEY_OBJECT = "result_key_object";
	public static final String RESULT_KEY_OBJECT_OK = "RESULT_KEY_OBJECT_OK";
	public static final String RESULT_KEY_OBJECT_FAIL = "RESULT_KEY_OBJECT_FAIL";
	
	private static final String RESPONSE_OK = "[]"; 
	
	private final Event mEvent;

	public EventCreateOperation(Context context, Event event) {
		super(context);
		mEvent = event;
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.CREATE;
	}
	
	@Override
	protected Map<String, Object> getCredentials() {
		Map<String, Object> params = new HashMap<String, Object>();
		 
		params.put(ServerConfigurations.LC_ID, pServerConfigurations.getLcId());
	    params.put(ServerConfigurations.PARTNER_ID, pServerConfigurations.getPartnerId());     
	    params.put(ServerConfigurations.WEB_SERVER_VERSION, pServerConfigurations.getWebServiceVersion());
	    params.put(ServerConfigurations.APPLICATION_VERSION, pServerConfigurations.getAppVersion());
	    params.put(ServerConfigurations.APPLICATION_CODE, pServerConfigurations.getAppCode());
	    params.put(ApplicationConfigurations.SESSION_ID, pApplicationConfigurations.getSessionID());
	    params.put(DeviceConfigurations.TIMESTAMP, pDeviceConfigurations.getTimeStamp());
	        	
	     return params;
	}

	@Override
	public Map<String, Object> getDescriptor() {
		Map<String, Object> params = new HashMap<String, Object>();
		if (mEvent instanceof PlayEvent) {
			PlayEvent event = (PlayEvent) mEvent;
			params.put("consumer_id", event.getConsumerId());
			params.put("device_id", event.getDeviceId());
			params.put("media_id", event.getMediaId());
			params.put("media_kind", event.getMediaKind());
			params.put("timestamp", event.getTimestamp(getContext()));
			params.put("playing_source_type", event.getPlayingSourceType().toString().toLowerCase());
			params.put("complete_play", Boolean.toString(event.isCompletePlay()));
			params.put("duration", event.getDuration());
			params.put("start_position", event.getStartPosition());
			params.put("stop_position", event.getStopPosition());
			params.put("delivery_id", event.getDeliveryId());
			params.put(ApplicationConfigurations.KEY_MEDIA_ID_NS, 
					ApplicationConfigurations.VALUE_MEDIA_ID_NS);
			
		} else {
			CampaignPlayEvent event = (CampaignPlayEvent) mEvent;
			params.put("consumer_id", event.getConsumerId());
			params.put("device_id", event.getDeviceId());
			params.put("campaign_media_id", event.getCampaignMediaId());
			params.put("campaign_id", event.getCampaignId());
			params.put("timestamp", event.getTimestamp(getContext()));
			params.put("complete_play", event.isCompletePlay());
			params.put("duration", event.getDuration());
			params.put("Latitude", event.getLatitude());
			params.put("Longitude", event.getLongitude());
			params.put("play_type", event.getPlayType());
		}
		
		return params;
	}

	@Override
	public int getOperationId() {
		if (mEvent instanceof PlayEvent) {
			return OperationDefinition.CatchMedia.OperationId.PLAY_EVENT_CREATE;
		} else {
			return OperationDefinition.CatchMedia.OperationId.CAMPAIGN_PLAY_EVENT_CREATE;
		}
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		if (mEvent instanceof PlayEvent) {
			return OperationDefinition.CatchMedia.ServiceName.PLAY_EVENT_CRERATE;
		} else {
			return OperationDefinition.CatchMedia.ServiceName.CAMPAIGN_PLAY_EVENT_CRERATE;
		}
	}

	@Override
	public String getRequestBody() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException,
							InvalidRequestParametersException, InvalidRequestTokenException,
							OperationCancelledException {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (response.equalsIgnoreCase(RESPONSE_OK)) {
			resultMap.put(RESULT_KEY_OBJECT, RESULT_KEY_OBJECT_OK);
		} else {
			resultMap.put(RESULT_KEY_OBJECT, RESULT_KEY_OBJECT_FAIL);
		}
		
		return resultMap;
	}

}
