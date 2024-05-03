package com.jeltechnologies.photos.config.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshConfiguration {
    
    @JsonProperty(value = "all-at-startup")
    private boolean allAtStarup = true;
    
    @JsonProperty(value = "all-at-hour")
    private int atHour = -1;
    
    @JsonProperty(value = "all-at-minute")
    private int atMinute = -1;

    public boolean isAllAtStarup() {
	return allAtStarup;
    }
    
    public boolean isScheduled() {
	return atHour > -1 && atMinute > -1;
    }

    public void setAllAtStarup(boolean allAtStarup) {
	this.allAtStarup = allAtStarup;
    }

    public int getAtHour() {
	return atHour;
    }

    public void setAtHour(int atHour) {
	this.atHour = atHour;
    }

    public int getAtMinute() {
	return atMinute;
    }

    public void setAtMinute(int atMinute) {
	this.atMinute = atMinute;
    }

    @Override
    public String toString() {
	return "RefreshConfiguration [allAtStarup=" + allAtStarup + ", atHour=" + atHour + ", atMinute=" + atMinute + "]";
    }
}
