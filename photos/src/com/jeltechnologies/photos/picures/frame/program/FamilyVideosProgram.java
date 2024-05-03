package com.jeltechnologies.photos.picures.frame.program;

import com.jeltechnologies.photos.pictures.Photo;

public class FamilyVideosProgram extends BaseFrameProgram {

    public FamilyVideosProgram() {
	super("familyvideos", "Family Videos");
    }

    @Override
    protected boolean isInProgram(Photo photo) {
	return photo.getRelativeFolderName() != null && photo.getRelativeFolderName().startsWith("/Albums/Family videos/");
    }
}
