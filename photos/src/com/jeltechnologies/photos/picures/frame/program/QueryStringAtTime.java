package com.jeltechnologies.photos.picures.frame.program;

import java.io.Serializable;
import java.time.LocalDateTime;

public class QueryStringAtTime implements Serializable {
    private static final long serialVersionUID = 3386704238464660936L;

    private String query;
    
    private final LocalDateTime startedAt;
    
    public QueryStringAtTime(String request) {
	this.query = request;
	this.startedAt = LocalDateTime.now();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String request) {
        this.query = request;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    @Override
    public String toString() {
	return "QueryStringAtTime [query=" + query + ", startedAt=" + startedAt + "]";
    }

   
}
