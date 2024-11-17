package com.jeltechnologies.photos.background.thumbs;

import java.io.File;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.datatypes.usermodel.Role;
import com.jeltechnologies.photos.pictures.MediaQueue;
import com.jeltechnologies.photos.pictures.MediaType;

public class SingleMediaProducer {
    
    private final Role role;
    
    public SingleMediaProducer(Role role) {
	this.role = role;
    }

    public void addToQueue(File file) {
	MediaQueue queue;
	MediaType type = Environment.getMediaType(file.getName());
	if (type == null) {
	    throw new IllegalArgumentException("Unsupported file " + file);
	}
	switch (type) {
	    case PHOTO: {
		queue = BackgroundServices.getInstance().getThumbsQueue();
		break;
	    }
	    case VIDEO: {
		queue = BackgroundServices.getInstance().getVideoQueue();
		break;
	    }
	    default: {
		throw new IllegalArgumentException("Unsupported media type: " + file);
	    }
	}
	queue.add(role, file, Producer.Type.REFRESH_ALL_METADATA);
    }
}
