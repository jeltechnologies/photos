package com.jeltechnologies.photos.picures.frame.program;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpecialDayGroup implements  Serializable, Iterable<SpecialDay> {
    private static final long serialVersionUID = 7864170169006715964L;
    private final String name;
    private final String label;
    private final List<SpecialDay> days = new ArrayList<SpecialDay>();
    
    public SpecialDayGroup(String name, String label) {
	super();
	this.name = name;
	this.label = label;
    }
    
    public void add(SpecialDay birthDay) {
	days.add(birthDay);
    }

    public String getName() {
        return name;
    }
    
    public String getLabel() {
        return label;
    }

    @Override
    public Iterator<SpecialDay> iterator() {
	return days.iterator();
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("SpecialDayGroup [name=");
	builder.append(name);
	builder.append(", label=");
	builder.append(label);
	builder.append(", days=");
	builder.append(days);
	builder.append("]");
	return builder.toString();
    }
}
