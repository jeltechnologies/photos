package com.jeltechnologies.util;

import java.util.ArrayList;
import java.util.List;

import com.jeltechnologies.photos.utils.StringUtils;

public class TimeLogger {
    private List<TimeLog> tasks = new ArrayList<TimeLog>();
    private final long startTime;
    private final String description;

    public TimeLogger(String description) {
	this.description = description;
	this.startTime = System.currentTimeMillis();
    }

    public void add(String task) {
	tasks.add(new TimeLog(task));
    }
    
    public String getDescription() {
        return description;
    }

    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("TimeLogger:\r\n");
	builder.append("Tasks:\r\n");
	builder.append("================================================");
	for (int i = 0; i < tasks.size(); i++) {
	    long previousTime;
	    if (i == 0) {
		previousTime = startTime;
	    } else {
		previousTime = tasks.get(i - 1).getTime();
		
	    }
	    TimeLog log = tasks.get(i);
	    long timeSpent = log.getTime() - previousTime;
	    builder.append("\r\n").append(log.getTask()).append(" => ").append(StringUtils.formatNumber(timeSpent)).append(" ms");
	}
	return builder.toString();
    }
}
