package com.jeltechnologies.photos.picures.frame.program;

import java.time.LocalDate;

import com.jeltechnologies.photos.pictures.Photo;

public class Last3MonthsProgram extends BaseFrameProgram {
    private LocalDate tomorrow = LocalDate.now().plusDays(1);
    private LocalDate past = LocalDate.now().minusMonths(3).minusDays(1);

    public Last3MonthsProgram() {
	super("last3months", "Last 3 months");
    }

    @Override
    protected boolean isInProgram(Photo photo) {
	LocalDate date = getDate(photo);
	boolean in = (date != null) && date.isAfter(past) && date.isBefore(tomorrow);
	return in;
    }
}
