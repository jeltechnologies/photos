package com.jeltechnologies.util;

public class TimeLog {
    private final long time;
    private final String task;
    
    public TimeLog(String task) {
	this.time = System.currentTimeMillis();
	this.task = task;
    }

    public long getTime() {
        return time;
    }

    public String getTask() {
        return task;
    }

    @Override
    public String toString() {
	return "TimeLog [time=" + time + ", task=" + task + "]";
    }
}
