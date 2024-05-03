package com.jeltechnologies.photos.pictures;

import java.io.File;
import java.io.FileFilter;

import com.jeltechnologies.photos.utils.StringUtils;

public class PhotosFileNameFilter implements FileFilter {

    private final String[] extensions;

    public PhotosFileNameFilter(String[] extensions) {
	this.extensions = extensions;
    }

    @Override
    public boolean accept(File file) {
	boolean result;
	if (file.isDirectory()) {
	    result = true;
	} else {
	    String fileExtension = StringUtils.findAfterLast(file.getName(), ".").toLowerCase();
	    boolean found = false;
	    for (int i = 0; !found && i < extensions.length; i++) {
		found = extensions[i].equals(fileExtension);
	    }
	    result = found;
	}
	return result;
    }

}
