package com.jeltechnologies.photos.timeline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TimelinePayload implements Serializable {
    private static final long serialVersionUID = -411705830377415667L;

    private List<TimelinePhoto> payload = new ArrayList<TimelinePhoto>();
    
    private String key;
    
    private int page;
    
    private int totalPages;
    
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void add(TimelinePhoto picture) {
        this.payload.add(picture);
    }

    public List<TimelinePhoto> getPayload() {
	return payload;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("TimelinePayload [payload=");
	builder.append(payload.size());
	builder.append(", key=");
	builder.append(key);
	builder.append(", page=");
	builder.append(page);
	builder.append(", totalPages=");
	builder.append(totalPages);
	builder.append("]");
	return builder.toString();
    }
    
}
