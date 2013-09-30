package com.hungama.myplay.activity.data.dao.catchmedia;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * An option of signing up for the application.
 */
public class SignOption {
	
	public static final String ACTION_SIGNUP = "signup";
	public static final String ACTION_LOGIN = "login";
	public static final String ACTION_AUTO_SIGNUP = "auto_signup";
	
	// general.
	@SerializedName("action")
	private final String action;
	@SerializedName("set_description")
	private final String setDescription;
	@SerializedName("set_id")
	private final long setID;
	@SerializedName("signup_fields")
	private final List<SignupField> signupFields;
	
	// CM sign up.
	@SerializedName("consumer_portal_url")
	private final String consumerPortalUrl;
	@SerializedName("consumer_signup_url")
	private final String consumerSignupUrl;
	@SerializedName("consumer_subscribe_url")
	private final String consumerSubscribeUrl;
	@SerializedName("partner_full_name")
	private final String partnerFullName;
	@SerializedName("partner_name")
	private final String partnerName;
	@SerializedName("product_name")
	private final String productName;
	@SerializedName("terms_and_conditions_url")
	private final String termsAndConditionsUrl;
	
	// facebook.
	@SerializedName("facebook_permissions")
	private final String facebookPermissions;

	public SignOption(String action, String setDescription, long setID,
			List<SignupField> signupFields, String consumerPortalUrl,
			String consumerSignupUrl, String consumerSubscribeUrl,
			String partnerFullName, String partnerName, String productName,
			String termsAndConditionsUrl, String facebookPermissions) {
		
		this.action = action;
		this.setDescription = setDescription;
		this.setID = setID;
		this.signupFields = signupFields;
		this.consumerPortalUrl = consumerPortalUrl;
		this.consumerSignupUrl = consumerSignupUrl;
		this.consumerSubscribeUrl = consumerSubscribeUrl;
		this.partnerFullName = partnerFullName;
		this.partnerName = partnerName;
		this.productName = productName;
		this.termsAndConditionsUrl = termsAndConditionsUrl;
		this.facebookPermissions = facebookPermissions;
	}

	public String getAction() {
		return action;
	}

	public String getSetDescription() {
		return setDescription;
	}

	public long getSetID() {
		return setID;
	}

	public List<SignupField> getSignupFields() {
		return signupFields;
	}

	public String getConsumerPortalUrl() {
		return consumerPortalUrl;
	}

	public String getConsumerSignupUrl() {
		return consumerSignupUrl;
	}

	public String getConsumerSubscribeUrl() {
		return consumerSubscribeUrl;
	}

	public String getPartnerFullName() {
		return partnerFullName;
	}

	public String getPartnerName() {
		return partnerName;
	}

	public String getProductName() {
		return productName;
	}

	public String getTermsAndConditionsUrl() {
		return termsAndConditionsUrl;
	}

	public String getFacebookPermissions() {
		return facebookPermissions;
	}
	
}
