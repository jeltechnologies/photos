package com.jeltechnologies.photos.pictures;

import java.io.File;
import java.io.FileFilter;

public class FolderFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
	return pathname.isDirectory();
    }
}
