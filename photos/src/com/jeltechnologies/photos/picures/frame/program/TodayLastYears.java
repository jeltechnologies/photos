package com.jeltechnologies.photos.picures.frame.program;

import java.time.LocalDate;

import com.jeltechnologies.photos.pictures.Photo;

public class TodayLastYears extends BaseFrameProgram {
    private int dayInMonth;
    private int month;

    public TodayLastYears(String name, String description) {
	super(name, description);
	LocalDate now = LocalDate.now();
	dayInMonth = now.getDayOfMonth();
	month = now.getMonthValue();
    }

    @Override
    protected boolean isInProgram(Photo photo) {
	LocalDate date = getDate(photo);
	return date.getDayOfMonth() == dayInMonth && date.getMonthValue() == month;
    }

}
