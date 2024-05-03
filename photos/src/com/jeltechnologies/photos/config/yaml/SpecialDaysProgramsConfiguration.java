package com.jeltechnologies.photos.config.yaml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SpecialDaysProgramsConfiguration implements Serializable {
    private static final long serialVersionUID = 6786426290460606490L;
    private List<SpecialDayConfiguration> days = new ArrayList<SpecialDayConfiguration>();
    private List<SpecialDaysProgramGroupConfiguration> groups = new ArrayList<SpecialDaysProgramGroupConfiguration>();

    public List<SpecialDayConfiguration> getDays() {
	return days;
    }

    public void setDays(List<SpecialDayConfiguration> days) {
	this.days = days;
    }

    public List<SpecialDaysProgramGroupConfiguration> getGroups() {
	return groups;
    }

    public void setGroups(List<SpecialDaysProgramGroupConfiguration> groups) {
	this.groups = groups;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("SpecialDaysProgramsConfiguration [days=");
	builder.append(days);
	builder.append(", groups=");
	builder.append(groups);
	builder.append("]");
	return builder.toString();
    }
}
