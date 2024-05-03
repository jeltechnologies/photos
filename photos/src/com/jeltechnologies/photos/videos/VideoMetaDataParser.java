package com.jeltechnologies.photos.videos;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.utils.StringUtils;

public class VideoMetaDataParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoMetaDataParser.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    
    private final File input;
    
    public VideoMetaDataParser(File input) {
	this.input = input;
    }
    
    public LocalDateTime parseDate(String line) {
	// com.apple.quicktime.creationdate: 2021-06-17T11:41:26+0200
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("parseDate " + line);
	}
	LocalDateTime ldt = null;
	try {
	    ldt = parseAppleDate(line); 
	} catch (Exception e) {
	    LOGGER.warn("Cannot parse creation date " + line + " in " + input.getAbsolutePath(), e);
	}
	return ldt;
    }

    private LocalDateTime parseAppleDate(String line) {
	LocalDateTime dateTime;
	StringBuilder b = new StringBuilder();
	if (line.length() >= 24) {
	    String workingPart = line.substring(0, 19);
	    String offSet = line.substring(19);
	    String offsetHour = offSet.substring(0, 3);
	    String minute = offSet.substring(3);
	    b.append(workingPart).append(offsetHour).append(":").append(minute);
	    dateTime = LocalDateTime.parse(b.toString(), DATE_FORMAT);
	} else {
	    dateTime = null;
	    LOGGER.warn("Not expected data for com.apple.quicktime.creationdate: " + line);
	}
	return dateTime;
    }

    private List<String> splitCoordinates(String line) {
	List<String> parts = new ArrayList<String>();
	StringBuilder b = new StringBuilder();
	for (int i = 0; i < line.length(); i++) {
	    char c = line.charAt(i);
	    if (c != '/') {
		if (c == '+' || c == '-') {
		    if (b.length() > 0) {
			parts.add(b.toString());
			b = new StringBuilder();
		    }
		}
		b.append(c);
	    }
	}
	parts.add(b.toString());
	return parts;
    }

    public BigDecimal parseLatidude(String line) {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("parseLatidude " + line);
	}
	BigDecimal result = null;
	List<String> parts = splitCoordinates(line);
	if (parts.size() > 0) {
	    String la = parts.get(0);
	    try {
		result = new BigDecimal(la);
	    } catch (Exception e) {
		LOGGER.warn("Cannot parse coordinate [la:" + la + "] from " + line + " in " + input.getAbsoluteFile());
	    }
	}
	return result;
    }
    
    public BigDecimal parseLongitude(String line) {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("parseLongitude " + line);
	}
	BigDecimal result = null;
	List<String> parts = splitCoordinates(line);
	if (parts.size() > 0) {
	    String lo = parts.get(1);
	    try {
		result = new BigDecimal(lo);
	    } catch (Exception e) {
		LOGGER.warn("Cannot parse coordinate [lo:" + lo + "] from " + line + " in " + input.getAbsoluteFile());
	    }
	}
	return result;
    }
   

    public int parseDuration(String duration) {
	int seconds = parseDurationToSeconds(duration);
	return seconds;
    }

    private int parseDurationToSeconds(String duration) {
	// 00:00:02.60
	int totalSeconds = 0;

	boolean parseOK = false;
	try {
	    List<String> parts = StringUtils.split(duration, ':');
	    if (parts.size() == 3) {
		int hours = Integer.parseInt(parts.get(0));
		int minutes = Integer.parseInt(parts.get(1));
		List<String> secondParts = StringUtils.split(parts.get(2), '.');
		if (secondParts.size() == 2) {
		    int seconds = Integer.parseInt(secondParts.get(0));
		    int milliseconds = Integer.parseInt(secondParts.get(1));
		    if (milliseconds >= 50) {
			seconds++;
		    }
		    parseOK = true;
		    totalSeconds = (hours * 3600) + (minutes * 60) + seconds;
		}
	    }
	} catch (NumberFormatException nfe) {
	    parseOK = false;
	}

	if (!parseOK) {
	    throw new IllegalArgumentException("Cannot parse duration: " + duration);
	}

	return totalSeconds;
    }

}
