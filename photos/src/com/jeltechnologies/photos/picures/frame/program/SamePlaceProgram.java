package com.jeltechnologies.photos.picures.frame.program;

import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.photos.pictures.Photo;

public class SamePlaceProgram extends BaseFrameProgram {
    private final int maxDistanceKilometres;
    
    public SamePlaceProgram(String name, String description, int maxDistanceKilometres) {
	super(name, description, false);
	this.maxDistanceKilometres = maxDistanceKilometres;
    }

    @Override
    protected boolean isInProgram(Photo photo, Photo photoInSlideShow) {
	return isInProgram(photo, photoInSlideShow, maxDistanceKilometres);
    }

    protected static boolean isInProgram(Photo photo, Photo photoInSlideShow, int maxDistanceKilometres) {
	boolean result;
	if (photoInSlideShow == null) {
	    result = false;
	} else {
	    Coordinates coordinatesInSlideShow = photoInSlideShow.getCoordinates();
	    if (coordinatesInSlideShow == null) {
		result = false;
	    } else {
		Coordinates coordinates = photo.getCoordinates();
		if (coordinates == null) {
		    return false;
		} else {
		    double distance = coordinates.getDistanceFrom(coordinatesInSlideShow);
		    result = distance <= maxDistanceKilometres;
		}
	    }
	}
	return result;
    }
}
