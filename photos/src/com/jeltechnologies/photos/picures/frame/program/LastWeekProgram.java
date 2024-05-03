package com.jeltechnologies.photos.picures.frame.program;

import java.time.LocalDate;

import com.jeltechnologies.photos.pictures.Photo;

public class LastWeekProgram extends BaseFrameProgram {
    private LocalDate tomorrow = LocalDate.now().plusDays(1);
    private LocalDate past = LocalDate.now().minusWeeks(1).minusDays(1);

    public LastWeekProgram() {
	super("lastweek", "Last week");
    }

    @Override
    protected boolean isInProgram(Photo photo) {
	LocalDate date = getDate(photo);
	boolean in = (date != null) && date.isAfter(past) && date.isBefore(tomorrow);
	return in;
    }
}
