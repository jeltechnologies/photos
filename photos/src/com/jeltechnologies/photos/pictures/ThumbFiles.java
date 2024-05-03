package com.jeltechnologies.photos.pictures;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbFiles {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(ThumbFiles.class);
    
    private final Map<String, File> files = new HashMap<String, File>();
    
    private void add(File file) {
	boolean existing = files.containsKey(file.getAbsolutePath());
	if (existing) {
	    LOGGER.warn("Tried to add thumb with same name twice: " + file.getAbsolutePath());
	}
	files.put(file.getAbsolutePath(), file);
    }

    public void add(Photo photo) {
	List<File> generatedFiles = ThumbnailUtilsFactory.getUtils().getAllGeneratedThumbnailFiles(photo);
	for (File file : generatedFiles) {
	    add(file);
	}
    }
    
    public boolean contains(File file) {
	return files.containsKey(file.getAbsolutePath());
    }
}
