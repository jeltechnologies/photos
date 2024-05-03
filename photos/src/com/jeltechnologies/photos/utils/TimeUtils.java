package com.jeltechnologies.photos.utils;

import java.util.GregorianCalendar;

public class TimeUtils {
    public static boolean nowIsNight() {
	GregorianCalendar cal = new GregorianCalendar();
	int hour = cal.get(GregorianCalendar.HOUR_OF_DAY);
	return hour < 6 || hour > 22;
    }
}
