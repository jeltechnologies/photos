package com.jeltechnologies.photos.exiftool;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.jeltechnologies.photos.utils.StringUtils;

public class ExifToolDateUtils {

    public static void main(String[] args) throws Exception {
	System.out.println(parseGPSDateTime("2023:11:28 07:54:59.19Z"));
	// 2010:07:25 15:51:10+08:00
	// 2022:07:28 19:48:34.589-04:00
	System.out.println(parseWithTimeZone("2010:07:25 15:51:10+08:00"));
	System.out.println(parseWithTimeZone("2022:07:28 19:48:34.589-04:00"));
	System.out.println(parseWithoutTimeZone("2010:09:08 08:44:49"));
	System.out.println(parseDateTime("2023:11:28 07:54:59.19Z"));
	System.out.println(parseDateTime("2010:07:25 15:51:10+08:00"));
	System.out.println(parseDateTime("2022:07:28 19:48:34.589-04:00"));
	System.out.println(parseDateTime("2010:09:08 08:44:49"));
    }

    public static ZonedDateTime parseDateTime(String exifDataTime) {
	// 01234567890123456789012345667
	// 2010:09:08 08:44:49
	// 010:07:25 15:51:10+08:00
	// 2022:07:28 19:48:34.589-04:00
	// 2023:11:28 07:54:59.19Z
	ZonedDateTime result;
	boolean isGpsTime = exifDataTime.indexOf('Z') > 0;
	if (isGpsTime) {
	    result = parseGPSDateTime(exifDataTime);
	} else {
	    boolean containsTimeZone = exifDataTime.indexOf('+') > 0 || exifDataTime.indexOf('-') > 0;
	    if (containsTimeZone) {
		result = parseWithTimeZone(exifDataTime);
	    } else {
		result = parseWithoutTimeZone(exifDataTime);
	    }
	}
	return result;
    }

    private static ZonedDateTime parseWithoutTimeZone(String exifDateTime) {
	try {
	    // 0 1 2
	    // 0123456789012345678901234
	    // 2010:07:25 15:51:10+08:00
	    // 2022:07:28 19:48:34.589-04:00
	    String yearString = exifDateTime.substring(0, 4);
	    String monthString = exifDateTime.substring(5, 7);
	    String dayString = exifDateTime.substring(8, 10);
	    String hourString = exifDateTime.substring(11, 13);
	    String minuteString = exifDateTime.substring(14, 16);
	    String secondString = exifDateTime.substring(17, 19);
	    int year = StringUtils.toInt(yearString, -1);
	    int month = StringUtils.toInt(monthString, -1);
	    int day = StringUtils.toInt(dayString, -1);
	    int hour = StringUtils.toInt(hourString, -1);
	    int minute = StringUtils.toInt(minuteString, -1);
	    int second = StringUtils.toInt(secondString, -1);
	    int nano = 0;
	    ZoneId zoneId = ZoneId.systemDefault();
	    ZonedDateTime zonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, nano, zoneId);
	    return zonedDateTime;
	} catch (Exception e) {
	    throw new IllegalArgumentException("Cannot parse " + exifDateTime, e);
	}
    }

    private static ZonedDateTime parseWithTimeZone(String exifDateTime) {
	try {
	    // 0 1 2
	    // 0123456789012345678901234
	    // 2010:07:25 15:51:10+08:00
	    // 2022:07:28 19:48:34.589-04:00
	    String yearString = exifDateTime.substring(0, 4);
	    String monthString = exifDateTime.substring(5, 7);
	    String dayString = exifDateTime.substring(8, 10);
	    String hourString = exifDateTime.substring(11, 13);
	    String minuteString = exifDateTime.substring(14, 16);
	    String secondString = exifDateTime.substring(17, 19);
	    int year = StringUtils.toInt(yearString, -1);
	    int month = StringUtils.toInt(monthString, -1);
	    int day = StringUtils.toInt(dayString, -1);
	    int hour = StringUtils.toInt(hourString, -1);
	    int minute = StringUtils.toInt(minuteString, -1);
	    int second = StringUtils.toInt(secondString, -1);
	    int nano = 0;
	    ZoneId zoneId = null;
	    int plus = exifDateTime.indexOf('+');
	    int minus = exifDateTime.indexOf('-');
	    if (plus > -1) {
		String timeZone = exifDateTime.substring(plus);
		zoneId = ZoneId.of(timeZone);
	    } else {
		if (minus > -1) {
		    String timeZone = exifDateTime.substring(minus);
		    zoneId = ZoneId.of(timeZone);
		}
	    }
	    if (zoneId == null) {
		throw new IllegalArgumentException("No ZoneId found in " + exifDateTime);
	    }
	    ZonedDateTime zonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, nano, zoneId);
	    return zonedDateTime;
	} catch (Exception e) {
	    throw new IllegalArgumentException("Cannot parse " + exifDateTime, e);
	}

    }

    private static ZonedDateTime parseGPSDateTime(String gpsDateTime) {
	try {
	    // 01234567890123456789012
	    // 2023:11:28 07:54:59.19Z
	    String yearString = gpsDateTime.substring(0, 4);
	    String monthString = gpsDateTime.substring(5, 7);
	    String dayString = gpsDateTime.substring(8, 10);
	    String hourString = gpsDateTime.substring(11, 13);
	    String minuteString = gpsDateTime.substring(14, 16);
	    String secondString = gpsDateTime.substring(17, 19);
	    int year = StringUtils.toInt(yearString, -1);
	    int month = StringUtils.toInt(monthString, -1);
	    int day = StringUtils.toInt(dayString, -1);
	    int hour = StringUtils.toInt(hourString, -1);
	    int minute = StringUtils.toInt(minuteString, -1);
	    int second = StringUtils.toInt(secondString, -1);
	    int nano = 0;
	    ZoneId zoneId = ZoneId.of("Z");
	    ZonedDateTime zonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, nano, zoneId);
	    return zonedDateTime;
	} catch (Exception e) {
	    throw new IllegalArgumentException("Cannot parse " + gpsDateTime, e);
	}
    }

}
