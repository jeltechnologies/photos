package com.jeltechnologies.photos.tags;

import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.timeline.Grouping;

public class RequestParameterParser {

    private RequestParameterParser() {
    }

    public static OrderBy getOrderByFromParameter(String sortedParameter) {
	OrderBy orderBy;
	if (sortedParameter == null) {
	    orderBy = OrderBy.DATE_TAKEN_OLDEST;
	} else {
	    if (sortedParameter.equalsIgnoreCase("newestfirst")) {
		orderBy = OrderBy.DATE_TAKEN_NEWEST;
	    } else {
		if (sortedParameter.equalsIgnoreCase("oldestfirst")) {
		    orderBy = OrderBy.DATE_TAKEN_OLDEST;
		} else {
		    orderBy = OrderBy.DATE_TAKEN_OLDEST;
		}
	    }
	}
	return orderBy;
    }

    public static Grouping getGrouping(String groupedParameter) {
	Grouping grouping;
	if (groupedParameter == null || groupedParameter.equalsIgnoreCase("month")) {
	    grouping = Grouping.MONTH;
	} else {
	    if (groupedParameter.equalsIgnoreCase("week")) {
		grouping = Grouping.WEEK;
	    } else {
		if (groupedParameter.equalsIgnoreCase("year")) {
		    grouping = Grouping.YEAR;
		} else {
		    grouping = Grouping.MONTH;
		}
	    }
	}
	return grouping;
    }

    public static MediaType getMediaTypeFromParameter(String mediaTypeParameter) {
	MediaType mediaType;
	if (mediaTypeParameter == null) {
	    mediaType = MediaType.ALL;
	} else {
	    if (mediaTypeParameter.equalsIgnoreCase("p")) {
		mediaType = MediaType.PHOTO;
	    } else {
		if (mediaTypeParameter.equalsIgnoreCase("m")) {
		    mediaType = MediaType.VIDEO;
		} else {
		    mediaType = MediaType.ALL;
		}
	    }
	}
	return mediaType;
    }

    public static String getMediaTypeParameter(MediaType mediaType) {
	String result;
	switch (mediaType) {
	    case ALL:
		result = "all";
		break;
	    case VIDEO:
		result = "m";
		break;
	    case PHOTO:
		result = "p";
		break;
	    default:
		result = "all";
		break;
	}
	return result;
    }

}
