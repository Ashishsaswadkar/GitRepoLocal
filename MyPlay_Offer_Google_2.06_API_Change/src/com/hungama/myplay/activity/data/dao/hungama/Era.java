package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Era implements Serializable {
	
	private static final String CENTURIES = "s";

	private final int from;
	private final int to;
	
	private static int sDefaultToYear = -1;
	
	public Era(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public int getFrom() {
		return from;
	}
	
	public int getTo() {
		return to;
	}
	
	public static int getDefaultFrom() {
		return 1950;
	}
	
	public static int getDefaultMiddle() {
		return 2000;
	}
	
	public static int getDefaultTo() {
		if (sDefaultToYear < 0) {
			Calendar calendar = new GregorianCalendar();
			sDefaultToYear =  calendar.get(Calendar.YEAR);
		}
		return sDefaultToYear;
	}
	
	public static String getTime(int year) {
		if (year >= getDefaultMiddle()){
			return Integer.toString(year);
		} else {
			return Integer.toString(year).substring(2) + CENTURIES;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (! (o instanceof Era))
			return false;
		
		Era era = (Era) o;
		if (this.getFrom() != era.getFrom())
			return false;
		
		if (this.getTo() != era.getTo())
			return false;
		
		return true;
	}
	
}
