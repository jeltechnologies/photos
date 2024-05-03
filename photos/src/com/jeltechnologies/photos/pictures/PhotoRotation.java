package com.jeltechnologies.photos.pictures;

import java.io.Serializable;

public enum PhotoRotation implements Serializable {
    HORIZONTAL_NORMAL(1, "Top, left side (Horizontal / normal)"), MIRROR_HORIZONTAL(2, "Top, right side (Mirror horizontal)"),
    ROTATE_180(3, "Bottom, right side (Rotate 180)"), MIRROR_VERTICAL(4, "Bottom, left side (Mirror vertical)"),
    MIRROR_HORIZONTAL_AND_ROTATE_270_CW(5, "Mirror horizontal and rotate 270 CW"), ROTATE_90_CW(6, "Right side, top (Rotate 90 CW)"),
    MIRROR_HORIZONTAL_AND_ROTATE_90_CW(7, "Right side, bottom (Mirror horizontal and rotate 90 CW)"), ROTATE_270_CW(8, "Left side, bottom (Rotate 270 CW)");

    String description;
    int orientation;

    PhotoRotation(int orientation, String description) {
	this.orientation = orientation;
	this.description = description;
    }

    public static PhotoRotation getRotation(int orientation) {
	PhotoRotation r = null;
	switch (orientation) {
	    case 0: {
		// do not do anything
		r = PhotoRotation.HORIZONTAL_NORMAL;
		break;
	    }
	    case 1:
		// desc = "Top, left side (Horizontal / normal)";
		r = PhotoRotation.HORIZONTAL_NORMAL;
		break;
	    case 2:
		// desc = "Top, right side (Mirror horizontal)";
		r = PhotoRotation.MIRROR_HORIZONTAL;
		break;
	    case 3:
		// desc = "Bottom, right side (Rotate 180)";
		r = PhotoRotation.ROTATE_180;
		break;
	    case 4:
		// desc = "Bottom, left side (Mirror vertical)";
		r = PhotoRotation.MIRROR_VERTICAL;
		break;
	    case 5:
		// desc = "Left side, top (Mirror horizontal and rotate 270 CW)";
		r = PhotoRotation.MIRROR_HORIZONTAL_AND_ROTATE_270_CW;
		break;
	    case 6:
		// desc = "Right side, top (Rotate 90 CW)";
		r = PhotoRotation.ROTATE_90_CW;
		break;
	    case 7:
		r = PhotoRotation.MIRROR_HORIZONTAL_AND_ROTATE_90_CW;
		// desc = "Right side, bottom (Mirror horizontal and rotate 90 CW)";
		break;
	    case 8:
		r = PhotoRotation.ROTATE_270_CW;
		// desc = "Left side, bottom (Rotate 270 CW)";
		break;
	}
	return r;
    }
    
    public String getDescription() {
	return description;
    }

}
