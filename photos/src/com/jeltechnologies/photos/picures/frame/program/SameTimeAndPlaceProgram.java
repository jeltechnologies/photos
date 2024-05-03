package com.jeltechnologies.photos.picures.frame.program;

import com.jeltechnologies.photos.pictures.Photo;

public class SameTimeAndPlaceProgram extends SameTimePhotoProgram {
    private final int maxDistanceKilometres;
    
    public SameTimeAndPlaceProgram(String name, String description, int maxDistanceKilometres) {
	super(name, description);
	this.maxDistanceKilometres = maxDistanceKilometres;
    }

    @Override
    protected boolean isInProgram(Photo photo, Photo photoInSlideShow) {
	boolean sameTime = super.isInProgram(photo, photoInSlideShow);
	boolean samePlace = false;
	if (sameTime) {
	    samePlace = SamePlaceProgram.isInProgram(photo, photoInSlideShow, maxDistanceKilometres);
	}
	boolean sameSame = sameTime && samePlace;
	return sameSame;
    }
}
