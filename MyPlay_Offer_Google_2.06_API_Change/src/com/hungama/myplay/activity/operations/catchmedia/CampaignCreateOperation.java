package com.hungama.myplay.activity.operations.catchmedia;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.data.dao.campaigns.Node;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Utils;

/**
 * Retrieves device ID from CM servers, based on the device's properties.
 */
public class CampaignCreateOperation extends CMOperation {
	
	private static final String TAG = "CampaignCreateOperation";
	
	public static final String RESPONSE_KEY_OBJECT_CAMPAIGN = "response_key_campaign";
	
	private List<String> campaignIDs;
	
	public CampaignCreateOperation(Context context, List<String> campaignIDs) {
		super(context);
		
		this.campaignIDs = new ArrayList<String>(campaignIDs);
		
	}

	@Override
	public JsonRPC2Methods getMethod() {
		return JsonRPC2Methods.READ;
	}
	
	@Override
	public Map<String, Object> getDescriptor() {
		
		Map<String, Object> descriptor = new HashMap<String, Object>();
		
		descriptor.put(ApplicationConfigurations.CAMPAIGN_IDS, campaignIDs);
		
		return descriptor;
	}
	
	@Override
	public int getOperationId() {
		return OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		return OperationDefinition.CatchMedia.ServiceName.CAMPAIGN_READ;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(String response) throws InvalidResponseDataException, 
				InvalidRequestParametersException, InvalidRequestTokenException, OperationCancelledException {
		
		JSONParser jsonParser = new JSONParser();
		try {
			
			Map<String, Object> campainMap = (Map<String, Object>) jsonParser.parse(response);
			
			Type listType = new TypeToken<ArrayList<Campaign>>() {}.getType();
			Gson gsonParser = new Gson();
			
			List<Campaign> campaigns = gsonParser.fromJson(campainMap.get("campaigns").toString(), listType);

			// sets the internal nodes with their parent campaigns.
			if (!Utils.isListEmpty(campaigns)) {
				List<Node> nodesList = new ArrayList<Node>();
				for(Campaign c : campaigns){
					CampaignsManager.setCampaignIdForNode(c.getNode(), c.getID());
					nodesList.add(c.getNode());
				}
			}
			
			Map<String, Object> responseMap = new HashMap<String, Object>();
			responseMap.put(RESPONSE_KEY_OBJECT_CAMPAIGN, campaigns);
			
			return responseMap;
			
		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("Device map parsing error.");
		}
		
	}

}
