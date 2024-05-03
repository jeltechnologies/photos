package com.jeltechnologies.photos.picures.frame.program;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.jeltechnologies.photos.pictures.Photo;

public class SameTimePhotoProgram extends BaseFrameProgram {
    private LocalDate after = null;
    private LocalDate before = null;

    public SameTimePhotoProgram(String name, String description) {
	super(name, description, false);
    }

    @Override
    protected boolean isInProgram(Photo photo, Photo photoShownInSlideShow) {
	boolean result;
	if (photoShownInSlideShow == null) {
	    result = false;
	} else {
	    LocalDateTime slideShowDateTime = photoShownInSlideShow.getDateTaken();
	    if (slideShowDateTime == null) {
		result = false;
	    } else {
		after = slideShowDateTime.minusWeeks(1).toLocalDate();
		before = slideShowDateTime.plusWeeks(1).toLocalDate();
		LocalDate date = getDate(photo);
		result = (date != null) && date.isAfter(after) && date.isBefore(before);
	    }
	}
	return result;
    }

}
