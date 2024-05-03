package com.jeltechnologies.photos.background.thumbs;

import java.io.File;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.datatypes.usermodel.Role;
import com.jeltechnologies.photos.pictures.MediaQueue;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.utils.StringUtils;

public class SingleMediaProducer {
    
    private final Role role;
    
    public SingleMediaProducer(Role role) {
	this.role = role;
    }

    public void addToQueue(File file) {
	MediaQueue queue;
	MediaType type = getType(file);
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
	queue.add(role, file, Producer.Type.COMPLETE_REFRESH);
    }

    private MediaType getType(File file) {
	MediaType type = null;
	String extension = StringUtils.findAfterLast(file.getName(), ".");
	if (findInArray(extension, Environment.PHOTO_EXTENSIONS)) {
	    type = MediaType.PHOTO;
	} else {
	    if (findInArray(extension, Environment.VIDEO_EXTENSIONS)) {
		type = MediaType.VIDEO;
	    }
	}
	return type;
    }

    private boolean findInArray(String extension, String[] extensions) {
	boolean found = false;
	for (int i = 0; !found && i < extensions.length; i++) {
	    if (extensions[i].equalsIgnoreCase(extension)) {
		found = true;
	    }
	}
	return found;
    }

}
