package com.jeltechnologies.photos.picures.frame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.picures.frame.program.AllPhotosProgram;
import com.jeltechnologies.photos.picures.frame.program.BaseFrameProgram;
import com.jeltechnologies.photos.picures.frame.program.FamilyVideosProgram;
import com.jeltechnologies.photos.picures.frame.program.Group;
import com.jeltechnologies.photos.picures.frame.program.Last12MonthsProgram;
import com.jeltechnologies.photos.picures.frame.program.Last3MonthsProgram;
import com.jeltechnologies.photos.picures.frame.program.Last6MonthsProgram;
import com.jeltechnologies.photos.picures.frame.program.LastMonthProgram;
import com.jeltechnologies.photos.picures.frame.program.LastWeekProgram;
import com.jeltechnologies.photos.tags.BaseTag;
import com.jeltechnologies.photos.utils.StringUtils;

public class FrameProgramTag extends BaseTag {
    private static final long serialVersionUID = 6748406685839839582L;

    private static final String LEADING_SPACES = StringUtils.dup(25, ' ');

    public static final String INPUT_NAME = "program";

    public static final String PROGRAMS = "programs";

    public static final String SPECIALDAYS = "specialdays";

    private String type;

    public void setType(String type) {
	this.type = type;
    }

    @Override
    public void addHTML() throws Exception {
	addLine(getOptionsHtml());
    }

    public String getOptionsHtml() {
	StringBuilder result = new StringBuilder();
	List<Group> programGroups = Environment.INSTANCE.getFrameProgramGroups(); 
	Group found = null;
	Iterator<Group> iterator = programGroups.iterator();
	while (found == null && iterator.hasNext()) {
	    Group current = iterator.next();
	    if (current.title().name().equals(type)) {
		found = current;
	    }
	}
	if (found == null) {
	    throw new IllegalArgumentException("Unkown program type: " + type);
	}
	List<BaseFrameProgram> programs = found.programs();
	boolean inPrograms = type.equals(PROGRAMS);
	
	for (int i = 0; i < programs.size(); i++) {
	    boolean checked = inPrograms &&  (i == 0);
	    String s = getRadioButton(programs.get(i), checked);
	    result.append(s);
	}
	return result.toString();
    }

    private String getRadioButton(BaseFrameProgram program, boolean checked) {
	StringBuilder s = new StringBuilder(LEADING_SPACES);
	String name = program.getName();
	String description = program.getDescription();
	s.append("<div");
	if (cssClass != null && !cssClass.isBlank()) {
	    s.append(" class=\"");
	    s.append(cssClass);
	    s.append("\"");
	}
	s.append(">");
	StringBuilder b = new StringBuilder();
	String id = "radio-" + name;
	b.append("<input type=\"radio\" id=\"").append(id).append("\" name=\"");
	b.append(INPUT_NAME).append("\" value=\"").append(name).append("\"");
	b.append(" onChange=\"updateMix();\"");
	if (checked) {
	    b.append(" checked");
	}
	b.append("><label id=\"").append("label-").append(name).append("\" for=\"").append(id).append("\">");
	b.append(description);
	b.append("</label>");
	s.append(b);
	s.append("</div>").append("\r\n");
	return s.toString();
    }

    public static List<BaseFrameProgram> getFramePrograms() {
	List<BaseFrameProgram> programs = new ArrayList<BaseFrameProgram>();
	programs.add(new AllPhotosProgram());
	programs.add(new LastWeekProgram());
	programs.add(new LastMonthProgram());
	programs.add(new Last3MonthsProgram());
	programs.add(new Last6MonthsProgram());
	programs.add(new Last12MonthsProgram());
	programs.add(new FamilyVideosProgram());
	return programs;
    }

}
