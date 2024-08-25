package com.jeltechnologies.photos.exiftool;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.utils.FileUtils;

public class ExifToolTagFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExifToolTagFilter.class);
    
    private final Set<String> tags = new HashSet<String>();
    
    public ExifToolTagFilter() {
	try {
	    init();
	} catch (IOException e) {
	    LOGGER.warn("Cannot read ExifToolTagFilter because " + e.getMessage(), e);
	}
    }
    
    private void init() throws IOException {
	List<String> lines = FileUtils.readTextFileLines("exiftool-tags.txt");
	for (String line : lines) {
	    String trimmed = line.trim().toLowerCase();
	    if (!trimmed.isBlank() && !trimmed.isEmpty() && !trimmed.startsWith("#")) {
		tags.add(trimmed);
	    }
	}
    }
    
    public boolean isInFilter(String tag) {
	return tags.contains(tag.toLowerCase());
    }

}
