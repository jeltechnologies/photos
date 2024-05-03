package com.jeltechnologies.photos.timeline;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

public class TimeLineTemporalAdjuster implements TemporalAdjuster {

    private final Grouping grouping;
    private final Sorting sorting;
    
    public TimeLineTemporalAdjuster(Grouping grouping, Sorting sorting) {
	this.grouping = grouping;
	this.sorting = sorting;
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
	Temporal result;
	int direction;
	switch (sorting) {
	    case NEWESTFIRST:
		direction = -1;
		break;
	    case OLDESTFIRST:
		direction = 1;
		break;
	    default:
		throw new IllegalArgumentException(sorting.toString());
	}
	switch (grouping) {
	    case MONTH:
		result = temporal.plus(direction, ChronoUnit.MONTHS);
		break;
	    case WEEK:
		result = temporal.plus(direction, ChronoUnit.WEEKS);
		break;
	    case YEAR:
		result = temporal.plus(direction, ChronoUnit.YEARS);
		break;
	    default:
		throw new IllegalArgumentException(grouping.toString());
	}
	return result;
    }
    
    public LocalDate getFirstDayOfGroup(LocalDate date) {
	LocalDate result;
	switch (grouping) {
	    case MONTH:
		result = date.withDayOfMonth(1);
		break;
	    case WEEK:
		result = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		break;
	    case YEAR:
		result = date.withDayOfYear(1);
		break;
	    default:
		throw new IllegalArgumentException(grouping.toString());
	}
	return result;
    }
}
