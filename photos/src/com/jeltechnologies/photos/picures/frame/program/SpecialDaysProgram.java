package com.jeltechnologies.photos.picures.frame.program;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jeltechnologies.photos.pictures.Photo;

public class SpecialDaysProgram extends BaseFrameProgram {
    private final List<LocalDate> dates = new ArrayList<LocalDate>();

    public SpecialDaysProgram(String name, String description) {
	super(name, description);
    }

    public void addDate(LocalDate date) {
	dates.add(date);
    }

    @Override
    protected boolean isInProgram(Photo photo) {
	boolean in = false;
	LocalDate date = getDate(photo);
	int month = date.getMonthValue();
	int day = date.getDayOfMonth();
	int year = date.getYear();
	Iterator<LocalDate> i = dates.iterator();
	while (!in && i.hasNext()) {
	    LocalDate current = i.next();
	    in = current.getDayOfMonth() == day && current.getMonthValue() == month && current.getYear() <= year;
	}
	return in;
    }

}
