package com.jeltechnologies.photos.config.yaml;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DimensioningConfiguration implements Serializable {
    private static final long serialVersionUID = 4450554603580490007L;

    @JsonProperty(value = "photo-threads")
    private int photoThreads = 10;

    @JsonProperty(value = "video-threads")
    private int videoThreads = 3;

    private boolean caching = false;
    
    @JsonProperty(value = "start-background-tasks-at-startup")
    private boolean startBackgroundTasksAtStartup = true;

    public int getPhotoThreads() {
	return photoThreads;
    }

    public void setPhotoThreads(int photoThreads) {
	this.photoThreads = photoThreads;
    }

    public int getVideoThreads() {
	return videoThreads;
    }

    public void setVideoThreads(int movieThreads) {
	this.videoThreads = movieThreads;
    }

    public boolean isCaching() {
	return caching;
    }

    public void setCaching(boolean caching) {
	this.caching = caching;
    }
    
    public boolean isStartBackgroundTasksAtStartup() {
        return startBackgroundTasksAtStartup;
    }

    public void setStartBackgroundTasksAtStartup(boolean startBackgroundTasksAtStartup) {
        this.startBackgroundTasksAtStartup = startBackgroundTasksAtStartup;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("DimensioningConfiguration [photoThreads=");
	builder.append(photoThreads);
	builder.append(", videoThreads=");
	builder.append(videoThreads);
	builder.append(", caching=");
	builder.append(caching);
	builder.append(", startBackgroundTasksAtStartup=");
	builder.append(startBackgroundTasksAtStartup);
	builder.append("]");
	return builder.toString();
    }

}
