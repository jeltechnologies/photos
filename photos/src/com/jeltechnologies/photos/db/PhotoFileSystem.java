package com.jeltechnologies.photos.db;

import java.io.File;
import java.io.FileFilter;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.pictures.Folder;

public class PhotoFileSystem {

    public static Folder getFolder(String relativeFileName) {
	Folder result = null;
	Environment env = Environment.INSTANCE;
	File file = env.getFile(relativeFileName);
	if (file.isDirectory()) {
	    result = new Folder();
	    result.setName(file.getName());
	    File[] foldersInFolder = file.listFiles(new FileFilter() {
		@Override
		public boolean accept(File pathname) {
		    return pathname.isDirectory();
		}
	    });
	    for (File folderInFolder : foldersInFolder) {
		result.addFolder(env.getRelativePhotoFileName(folderInFolder));
	    }
	}
	if (result != null) {
	    result.sort();
	}
	return result;
    }

}
