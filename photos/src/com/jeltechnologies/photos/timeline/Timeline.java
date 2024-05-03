package com.jeltechnologies.photos.timeline;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Timeline implements Serializable {
    private static final long serialVersionUID = 6616419434024916586L;
    private Grouping grouping;
    private LocalDateTime fetched = LocalDateTime.now();
    private LocalDateTime cacheVersion;
    
    private List<TimelinePhoto> payload = new ArrayList<TimelinePhoto>();
    
    public Timeline(TimelineView request) {
	grouping = request.getGrouping();
    }
    
    public void add(TimelinePhoto picture) {
	this.payload.add(picture);
    }

    public Grouping getGrouping() {
	return grouping;
    }

    public void setGrouping(Grouping grouping) {
	this.grouping = grouping;
    }

    public List<TimelinePhoto> getPayload() {
	return payload;
    }
    
    public int size() {
	return payload.size();
    }

    public LocalDateTime getFetched() {
        return fetched;
    }

    public void setFetched(LocalDateTime fetched) {
        this.fetched = fetched;
    }

    public LocalDateTime getCacheVersion() {
        return cacheVersion;
    }

    public void setCacheVersion(LocalDateTime cacheVersion) {
        this.cacheVersion = cacheVersion;
    }

    public void setPayload(List<TimelinePhoto> payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("Timeline [grouping=");
	builder.append(grouping);
	builder.append("]");
	return builder.toString();
    }

}
