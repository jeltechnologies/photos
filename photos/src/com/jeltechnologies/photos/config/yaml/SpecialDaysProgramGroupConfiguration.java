package com.jeltechnologies.photos.config.yaml;

import java.io.Serializable;

public class SpecialDaysProgramGroupConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String label;
    private String days;

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

    public String getDays() {
	return days;
    }

    public void setDays(String days) {
	this.days = days;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("SpecialDaysProgramGroupConfiguration [name=");
	builder.append(name);
	builder.append(", label=");
	builder.append(label);
	builder.append(", days=");
	builder.append(days);
	builder.append("]");
	return builder.toString();
    }
}
