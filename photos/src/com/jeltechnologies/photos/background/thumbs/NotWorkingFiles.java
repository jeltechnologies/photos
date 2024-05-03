package com.jeltechnologies.photos.background.thumbs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.utils.FileUtils;
import com.jeltechnologies.photos.utils.StringUtils;

public class NotWorkingFiles {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotWorkingFiles.class);
    private static final File STORAGE = Environment.INSTANCE.getConfig().getNotWorkingFilesStorage();
    private List<NotWorkingFile> files;
    private static final String SEPERATOR = ";";

    public NotWorkingFiles() {
	load();
    }

    private void load() {
	files = new CopyOnWriteArrayList<NotWorkingFile>();
	if (STORAGE.isFile()) {
	    List<String> names;
	    try {
		names = FileUtils.readTextFileLines(STORAGE.getAbsolutePath(), false, "UTF-8");
		for (String name : names) {  
		    String[] parts = name.split(SEPERATOR);
		    if (parts.length > 2) {
			String fileName = parts[0];
			String error = StringUtils.findAfter(name, SEPERATOR);
			files.add(new NotWorkingFile(new File(fileName), error));
		    }
		}
		LOGGER.info("Loaded " + files.size() + " not working files");
	    } catch (IOException e) {
		LOGGER.error(e.getMessage() + " while loading NotWorkingFiles from " + STORAGE, e);
	    }
	}
	save();
    }

    private void save() {
	try {
	    List<String> lines = new ArrayList<String>(files.size());
	    for (NotWorkingFile nwf : files) {
		String error = StringUtils.stripControlChars(nwf.errorMessage());
		error = StringUtils.replaceAll(error, SEPERATOR, " ");
		String line = nwf.file().getAbsolutePath() + SEPERATOR + error;
		lines.add(line);
		//LOGGER.info(line);
	    }
	    FileUtils.writeTextFile(STORAGE.getAbsolutePath(), lines, Charset.forName("UTF-8"));
	} catch (IOException e) {
	    LOGGER.error(e.getMessage() + " while saving NotWorkingFiles to " + STORAGE, e);
	}
    }

    public boolean contains(File file) {
	boolean found = false;
	Iterator<NotWorkingFile> i = files.iterator();
	while (!found && i.hasNext()) {
	    NotWorkingFile current = i.next();
	    if (current.file().equals(file)) {
		found = true;
	    }
	}
	return found;
    }

    public void add(File file, Throwable throwable) {
	this.files.add(new NotWorkingFile(file, throwable.getMessage()));
	save();
    }
}
