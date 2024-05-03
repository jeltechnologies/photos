package com.jeltechnologies.photos.pictures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.servlet.PhotoAction;

public class PhotoRotateActionHandler extends PhotoActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoRotateActionHandler.class);

    public PhotoRotateActionHandler(PhotoAction action) {
	super(action);
    }

    @Override
    public void handleDetails() throws Exception {
	Photo photo = database.getPhotoByFileName(action.getUser(), action.getId());
	LOGGER.info(action.getId() + " orientation " + photo.getOrientation());
    }

}
