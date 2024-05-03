package com.jeltechnologies.photos.pictures;

import com.jeltechnologies.photos.servlet.PhotoAction;

public class ShowOrHidePhotoHandler extends PhotoActionHandler {
    private final boolean hide;
    
    public ShowOrHidePhotoHandler(PhotoAction action, boolean hide) {
	super(action);
	this.hide = hide;
    }

    @Override
    public void handleDetails() throws Exception {
	String id = action.getId();
	database.updateHiddenPhoto(id, hide);
	database.commit();
    }

}
