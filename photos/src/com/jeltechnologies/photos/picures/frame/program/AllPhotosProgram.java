package com.jeltechnologies.photos.picures.frame.program;

import com.jeltechnologies.photos.pictures.Photo;

public class AllPhotosProgram extends BaseFrameProgram {

    public AllPhotosProgram() {
	super("ALL", "All photos and videos");
    }

    @Override
    protected boolean isInProgram(Photo photo) {
	return true;
    }
}
