package com.jeltechnologies.photos.config.yaml;

import java.io.Serializable;

public class SpecialDayConfiguration implements Serializable {
    private static final long serialVersionUID = -8060368647550758020L;
    private String name;
    private String label;
    private int day;
    private int month;
    private int year;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public int getDay() {
	return day;
    }

    public void setDay(int day) {
	this.day = day;
    }

    public int getMonth() {
	return month;
    }

    public void setMonth(int month) {
	this.month = month;
    }

    public int getYear() {
	return year;
    }

    public void setYear(int year) {
	this.year = year;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("SpecialDayConfiguration [name=");
	builder.append(name);
	builder.append(", label=");
	builder.append(label);
	builder.append(", day=");
	builder.append(day);
	builder.append(", month=");
	builder.append(month);
	builder.append(", year=");
	builder.append(year);
	builder.append("]");
	return builder.toString();
    }
}
