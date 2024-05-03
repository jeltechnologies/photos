package com.jeltechnologies.photos.picures.frame.program;

import java.io.Serializable;
import java.time.LocalDate;

public class SpecialDay implements Serializable {
    private static final long serialVersionUID = 5913263521206850466L;
    private final String name;
    private final String label;
    private final LocalDate date;

    public SpecialDay(String name, String label, LocalDate date) {
	super();
	this.name = name;
	this.label = label;
	this.date = date;
    }

    public String getName() {
	return name;
    }

    public LocalDate getDate() {
	return date;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("SpecialDay [name=");
	builder.append(name);
	builder.append(", label=");
	builder.append(label);
	builder.append(", date=");
	builder.append(date);
	builder.append("]");
	return builder.toString();
    }
}
