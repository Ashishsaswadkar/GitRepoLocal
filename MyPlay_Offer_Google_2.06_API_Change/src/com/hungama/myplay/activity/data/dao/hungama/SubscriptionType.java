package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

/**
 * Enumeration definition of different Subscription Operation.
 */
public enum SubscriptionType implements Serializable  {
	PLAN,
	CHARGE,
	UNSUBSCRIBE,
	CHECK_SUBSCRIPTION;
}
