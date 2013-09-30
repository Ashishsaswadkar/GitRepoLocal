package com.hungama.myplay.activity.data.dao.catchmedia;

import android.text.InputType;

/**
 * Enumeration definition of the different fields types that a {@link SignupField} can be.
 */
public enum SignupFieldType {
	
	STRING		(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL),
	EMAIL		(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS),
	PASSWORD	(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD),
	NUMBER		(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER),
	HIDDEN		(0);
	
	private final int inputType;
	
	SignupFieldType(int inputType) {
		this.inputType = inputType;
	}
	
	public int getInputType() {
		return this.inputType;
	}
	
	public static final SignupFieldType getSignupFieldTypeByName(String name) {
		
		if (name.equalsIgnoreCase(STRING.toString())) {
			return STRING;
		}
		
		if (name.equalsIgnoreCase(EMAIL.toString())) {
			return EMAIL;
		}
		
		if (name.equalsIgnoreCase(PASSWORD.toString())) {
			return PASSWORD;
		}
		
		if (name.equalsIgnoreCase(NUMBER.toString())) {
			return NUMBER;
		}
		
		if (name.equalsIgnoreCase(HIDDEN.toString())) {
			return HIDDEN;
		}
		
		return STRING;
	}
}
