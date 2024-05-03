package com.jeltechnologies.photos.picures.frame.program;

import java.time.LocalDate;

import com.jeltechnologies.photos.pictures.Photo;

public class Last24HoursProgram extends BaseFrameProgram {
    private LocalDate tomorrow = LocalDate.now().plusDays(1);
    private LocalDate past = LocalDate.now().minusDays(2);

    public Last24HoursProgram() {
	super("last24hours", "Last 24 hours");
    }

    @Override
    protected boolean isInProgram(Photo photo) {
	LocalDate date = getDate(photo);
	boolean in = (date != null) && date.isAfter(past) && date.isBefore(tomorrow);
	return in;
    }

}
