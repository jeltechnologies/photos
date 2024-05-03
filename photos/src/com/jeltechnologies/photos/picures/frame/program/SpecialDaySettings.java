package com.jeltechnologies.photos.picures.frame.program;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.config.yaml.SpecialDayConfiguration;
import com.jeltechnologies.photos.config.yaml.SpecialDaysProgramGroupConfiguration;
import com.jeltechnologies.photos.config.yaml.SpecialDaysProgramsConfiguration;
import com.jeltechnologies.photos.datatypes.NamedValue;
import com.jeltechnologies.photos.datatypes.NamedValueCollection;

public class SpecialDaySettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpecialDaySettings.class);

    private NamedValueCollection<SpecialDay> specialDays = new NamedValueCollection<SpecialDay>();
    private NamedValueCollection<SpecialDayGroup> specialDayGroups = new NamedValueCollection<SpecialDayGroup>();

    public SpecialDaySettings() {
    }

    public void addConfiguration(SpecialDaysProgramsConfiguration configuration) {
	for (SpecialDayConfiguration day : configuration.getDays()) {
	    try {
		LocalDate date = LocalDate.of(day.getYear(), day.getMonth(), day.getDay());
		String name = day.getName();
		if (specialDays.containsName(name)) {
		    LOGGER.warn("Skipped " + day + " because the name is not unique");
		} else {
		    specialDays.add(name, new SpecialDay(name, day.getLabel(), date));
		}
	    } catch (DateTimeException e) {
		LOGGER.warn("Skipped " + day + " because the date is not valid");
	    }
	}
	for (SpecialDaysProgramGroupConfiguration groupConfig : configuration.getGroups()) {
	    String name = groupConfig.getName();
	    SpecialDayGroup group = specialDayGroups.getFirst(name);
	    if (group != null) {
		LOGGER.warn("Skipped group " + groupConfig + " because the name is not unque");
	    } else {
		group = new SpecialDayGroup(name, groupConfig.getLabel());
		specialDayGroups.add(name, group);
		String[] dayNames = groupConfig.getDays().split(",");
		for (String dayName : dayNames) {
		    String trimmedDay = dayName.trim();
		    SpecialDay day = specialDays.getFirst(trimmedDay);
		    if (day == null) {
			LOGGER.warn("Cannot find day named " + trimmedDay + " in " + groupConfig);
		    } else {
			group.add(day);
		    }
		}
	    }
	}
    }

    public List<BaseFrameProgram> getPrograms() {
	List<BaseFrameProgram> programs = new ArrayList<BaseFrameProgram>();
	for (NamedValue<SpecialDayGroup> groupNV : specialDayGroups) {
	    SpecialDayGroup group = groupNV.getValue();
	    SpecialDaysProgram program = new SpecialDaysProgram(group.getName(), group.getLabel());
	    for (SpecialDay day : group) {
		program.addDate(day.getDate());
	    }
	    programs.add(program);
	}
	for (NamedValue<SpecialDay> dayNV : specialDays) {
	    SpecialDay day = dayNV.getValue();
	    SpecialDaysProgram program = new SpecialDaysProgram(day.getName(), day.getLabel());
	    program.addDate(day.getDate());
	    programs.add(program);
	}
	return programs;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("SpecialDaySettings [specialDays=");
	builder.append(specialDays);
	builder.append(", specialDayGroups=");
	builder.append(specialDayGroups);
	builder.append("]");
	return builder.toString();
    }
}
