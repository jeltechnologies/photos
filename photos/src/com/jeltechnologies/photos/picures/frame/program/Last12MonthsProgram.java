package com.jeltechnologies.photos.picures.frame.program;

import java.time.LocalDate;

import com.jeltechnologies.photos.pictures.Photo;

public class Last12MonthsProgram extends BaseFrameProgram {
    private LocalDate tomorrow = LocalDate.now().plusDays(1);
    private LocalDate past = tomorrow.minusMonths(12);

    public Last12MonthsProgram() {
	super("last12months", "Last 12 months");
    }

    @Override
    protected boolean isInProgram(Photo photo) {
	LocalDate date = getDate(photo);
	boolean in = (date != null) && date.isAfter(past) && date.isBefore(tomorrow);
	return in;
    }
}
