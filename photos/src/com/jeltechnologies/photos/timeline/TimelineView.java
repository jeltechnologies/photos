package com.jeltechnologies.photos.timeline;

import java.io.Serializable;

import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.pictures.MediaType;

public class TimelineView implements Serializable, Cloneable {
    private static final long serialVersionUID = -8859615964452925401L;
    private User user;
    private Grouping grouping;
    private MediaType mediaType;
    private boolean randomCover;
    
    public TimelineView() {
    }
    
    public TimelineView(User user, Grouping grouping, boolean randomCover, MediaType mediaType) {
	super();
	this.user = user;
	this.grouping = grouping;
	this.randomCover = randomCover;
	this.mediaType = mediaType;
    }
    
    public User getUser() {
	return user;
    }

    public void setUser(User user) {
	this.user = user;
    }

    public Grouping getGrouping() {
	return grouping;
    }

    public void setGrouping(Grouping grouping) {
	this.grouping = grouping;
    }

    public boolean isRandomCover() {
        return randomCover;
    }

    public void setRandomCover(boolean randomCover) {
        this.randomCover = randomCover;
    }
    
    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("TimelineView [user=");
	builder.append(user);
	builder.append(", grouping=");
	builder.append(grouping);
	builder.append(", mediaType=");
	builder.append(mediaType);
	builder.append(", randomCover=");
	builder.append(randomCover);
	builder.append("]");
	return builder.toString();
    }

   
    
}
