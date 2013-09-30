package com.hungama.myplay.activity.data.dao.catchmedia;

import com.google.gson.annotations.SerializedName;

/**
 * Signup field to fill for performing signing to the application.
 */
public class SignupField {
	
	public static final String KEY = "key";
	public static final String VALUE = "value";
	
	public static final String KEY_FIRST_NAME = "first_name";
	public static final String KEY_LAST_NAME = "last_name";
	public static final String KEY_EMAIL = "email";

	// general fields.
	@SerializedName("type")
	private final String type;
	@SerializedName("name")
	private final String name;
	@SerializedName("display")
	private final String display;

	// restrictions, for password types.
	@SerializedName("min_length")
	private final long minimumLength;
	@SerializedName("max_length")
	private final long maximumLength;
	
	// GIGYA additional field.
	@SerializedName("default_value")
	private final String defaultValue;
	@SerializedName("alternate_for")
	private final String alternateFor;
	
	// auto signing up.
	@SerializedName("optional")
	private final String optional;
	
	private String value;

	public SignupField(String type, String name, String display, long minimumLength, 
						long maximumLength, String defaultValue, String alternateFor, String optional) {
		
		this.type = type;
		this.name = name;
		this.display = display;
		this.minimumLength = minimumLength;
		this.maximumLength = maximumLength;
		this.defaultValue = defaultValue;
		this.alternateFor = alternateFor;
		this.optional = optional;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getDisplay() {
		return display;
	}

	public long getMinimumLength() {
		return minimumLength;
	}

	public long getMaximumLength() {
		return maximumLength;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getAlternateFor() {
		return alternateFor;
	}

	public String getOptional() {
		return optional;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
