package com.jeltechnologies.photos.background.thumbs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.utils.FileUtils;
import com.jeltechnologies.photos.utils.StringUtils;

public class NotWorkingFiles implements Iterable<NotWorkingFile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotWorkingFiles.class);
    private static final File STORAGE = Environment.INSTANCE.getConfig().getNotWorkingFilesStorage();
    private List<NotWorkingFile> files;
    private static final String SEPERATOR = ",";
    private final static File FAILED_FOLDER = Environment.INSTANCE.getConfig().getFailedFolder();

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
		    if (parts.length > 1) {
			String fileName = parts[0];
			String error = parts[1];
			files.add(new NotWorkingFile(new File(fileName), error));
		    }
		}
		LOGGER.debug("Loaded " + files.size() + " not working files");
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
	    }
	    FileUtils.writeTextFile(STORAGE.getAbsolutePath(), lines, Charset.forName("UTF-8"));
	} catch (IOException e) {
	    LOGGER.error(e.getMessage() + " while saving NotWorkingFiles to " + STORAGE, e);
	}
    }

    public boolean contains(File file) {
	return get(file) != null;
    }

    private NotWorkingFile get(File file) {
	NotWorkingFile found = null;
	Iterator<NotWorkingFile> i = files.iterator();
	while (found == null && i.hasNext()) {
	    NotWorkingFile current = i.next();
	    if (current.file().equals(file)) {
		found = current;
	    }
	}
	return found;
    }

    public void moveAllNotWorkingFiles() {
	int size = files.size();
	if (size == 0) {
	    LOGGER.debug("No not working original files need moving");
	} else {
	    if (size > 0) {
		LOGGER.debug("Moving " + size + " originals files that were not working");
	    }
	}
	List<File> ioFiles = new ArrayList<File>();
	for (NotWorkingFile nwf : files) {
	    ioFiles.add(nwf.file());
	}
	for (File file : ioFiles) {
	    moveAndRemoveFromList(file);
	}
    }

    public void moveAndRemoveFromList(File original) {
	NotWorkingFile notWorkingFile = get(original);
	if (notWorkingFile == null) {
	    throw new IllegalStateException("Notworkingfile not found");
	} else {
	    try {
		String relativeFileName = Environment.INSTANCE.getRelativePhotoFileName(original);
		String destinationPath = FAILED_FOLDER + relativeFileName;
		File destination = new File(destinationPath);
		File destinationFolder = destination.getParentFile();
		if (!destinationFolder.isDirectory()) {
		    boolean ok = destinationFolder.mkdirs();
		    if (!ok) {
			throw new IOException("Cannot create folder: " + destinationPath);
		    }
		}
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("  -> Moving " + original.getAbsolutePath() + " to " + destination.getAbsolutePath());
		}
		Files.move(original.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		LOGGER.debug("Moved failed file " + original.getName() + " to " + destinationFolder);
		this.files.remove(notWorkingFile);
		this.save();
	    } catch (IOException e) {
		LOGGER.debug(e.getMessage() + " for file " + original.getName() + " => " + e.getMessage());
	    }
	}

    }

    public void add(File file, Throwable throwable) {
	this.files.add(new NotWorkingFile(file, throwable.getMessage()));
	save();
    }

    public void add(NotWorkingFile file) {
	this.files.add(file);
	save();
    }

    public int size() {
	return files.size();
    }

    @Override
    public Iterator<NotWorkingFile> iterator() {
	return this.files.iterator();
    }
}
