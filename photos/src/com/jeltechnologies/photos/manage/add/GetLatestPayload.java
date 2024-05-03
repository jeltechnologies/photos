package com.jeltechnologies.photos.manage.add;

import java.io.Serializable;

public class GetLatestPayload implements Serializable {
    private static final long serialVersionUID = -1647181561857388035L;
    private String status;
    
    private int thumbsQueue;
    
    private int videosQueue;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getThumbsQueue() {
        return thumbsQueue;
    }

    public void setThumbsQueue(int thumbsQueue) {
        this.thumbsQueue = thumbsQueue;
    }

    public int getVideosQueue() {
        return videosQueue;
    }

    public void setVideosQueue(int videosQueue) {
        this.videosQueue = videosQueue;
    }

}
