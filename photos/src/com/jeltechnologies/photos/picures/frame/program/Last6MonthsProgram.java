package com.jeltechnologies.photos.picures.frame.program;

import java.time.LocalDate;

import com.jeltechnologies.photos.pictures.Photo;

public class Last6MonthsProgram extends BaseFrameProgram {
    private LocalDate tomorrow = LocalDate.now().plusDays(1);
    private LocalDate past = LocalDate.now().minusMonths(6).minusDays(1);

    public Last6MonthsProgram() {
	super("last6months", "Last 6 months");
    }

    @Override
    protected boolean isInProgram(Photo photo) {
	LocalDate date = getDate(photo);
	boolean in = (date != null) && date.isAfter(past) && date.isBefore(tomorrow);
	return in;
    }
}
