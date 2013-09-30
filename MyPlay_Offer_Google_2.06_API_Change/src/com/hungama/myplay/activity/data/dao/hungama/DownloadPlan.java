package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.billing.SkuDetails;

/**
 * Media item represent any form of media that can be presented in the application.
 */
public class DownloadPlan implements Serializable {
	
	public static String ACCOUNT_TYPE_GOOGLE = "google"; 
	
	public static final String KEY_PLAN_ID = "plan_id";
	public static final String KEY_PLAN_NAME = "plan_name";
	public static final String KEY_PLAN_CURRENCY = "plan_currency";
	public static final String KEY_PLAN_PRICE = "plan_price";
	public static final String KEY_MSISDN = "msisdn";
	public static final String KEY_STATUS = "status";
	public static final String KEY_CREDIT_BALANCE = "credit_balance";
	public static final String KEY_REDEEM = "redeem";
	public static final String KEY_TYPE = "type";
	public static final String KEY_IS_INDIA = "isIndia";	

	
	
	@Expose
	@SerializedName(KEY_PLAN_ID)
	private int planId;
	@Expose
	@SerializedName(KEY_PLAN_NAME)
	private final String planName;
	@Expose
	@SerializedName(KEY_PLAN_CURRENCY)
	private final String planCurrency;
	@Expose
	@SerializedName(KEY_PLAN_PRICE)
	private final String planPrice;
	@Expose
	@SerializedName(KEY_MSISDN)
	private final String msisdn;
	@Expose
	@SerializedName(KEY_STATUS)
	private final String status;
	@Expose
	@SerializedName(KEY_CREDIT_BALANCE)
	private final int creditBalance;
	@Expose
	@SerializedName(KEY_REDEEM)
	private final int redeem;
	@Expose
	@SerializedName(KEY_TYPE)
	protected String type;
	@Expose
	@SerializedName(KEY_IS_INDIA)
	private final int isIndia;
	
	private PlanType planType;
	
	private SkuDetails skudetails;
	
	private boolean isBuyButtonClickable = true;
	
	public DownloadPlan(int planId, String planName, String planCurrency, String planPrice, String msisdn, String status, int creditBalance, int redeem, String type, int isIndia) {
		this.planId = planId;
		this.planName = planName;
		this.planCurrency = planCurrency;
		this.planPrice = planPrice;
		this.msisdn = msisdn;
		this.status = status;
		this.creditBalance = creditBalance;
		this.redeem = redeem;
		this.type = type;
		this.isIndia = isIndia;
		
		
	}


	public void setPlanId(int planId) {
		this.planId = planId;
	}


	public int getPlanId() {
		return planId;
	}



	public String getPlanName() {
		return planName;
	}



	public String getPlanCurrency() {
		return planCurrency;
	}



	public String getPlanPrice() {
		return planPrice;
	}



	public String getMsisdn() {
		return msisdn;
	}



	public String getStatus() {
		return status;
	}



	public int getCreditBalance() {
		return creditBalance;
	}


	public int getRedeem() {
		return redeem;
	}
	
	public void setType(String type) {
		this.type = type;
	}


	public String getType() {
		return type;
	}
	
	public void setPlanType(PlanType planType) {
		this.planType = planType;
	}
	
	public PlanType getPlanType() {
		if (planType == null)
			planType = PlanType.getPlanByName(type);
		
		return planType;
	}


	public SkuDetails getSkudetails() {
		return skudetails;
	}


	public void setSkudetails(SkuDetails skudetails) {
		this.skudetails = skudetails;
	}


	public boolean isBuyButtonClickable() {
		return isBuyButtonClickable;
	}


	public void setBuyButtonClickable(boolean isBuyButtonClickable) {
		this.isBuyButtonClickable = isBuyButtonClickable;
	}
	
	public int getIsIndia() {
		return isIndia;
	}


	public boolean isIndia() {
		if (this.isIndia == 0) {
			return false;
		}
		return true;
	}
		
}
