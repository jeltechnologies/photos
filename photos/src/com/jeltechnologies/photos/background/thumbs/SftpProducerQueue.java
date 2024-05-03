package com.jeltechnologies.photos.background.thumbs;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.background.sftp.server.ChangedFile;

public class SftpProducerQueue implements Iterable<String> {
    private ChangedFile lastRelativFileName;

    private Map<String, ChangedFile> queue = new HashMap<String, ChangedFile>();

    public ChangedFile get(String relativeFileName) {
	return queue.get(relativeFileName);
    }

    public int size() {
	synchronized (queue) {
	    return queue.size();
	}
    }

    public void add(File file) {
	String relativeFileName = Environment.INSTANCE.getRelativePhotoFileName(file);
	LocalDateTime now = LocalDateTime.now();
	ChangedFile changedFile = new ChangedFile();
	changedFile.setFile(file);
	changedFile.setTime(now);
	synchronized (queue) {
	    if (!queue.containsKey(relativeFileName)) {
		queue.put(relativeFileName, changedFile);
	    }
	    lastRelativFileName = changedFile;
	}
    }

    public void empty() {
	synchronized (queue) {
	    List<String> keys = new ArrayList<String>(queue.size());
	    for (String key :queue.keySet()) {
		keys.add(key);
	    }
	    for (String relativeFileName : keys) {
		queue.remove(relativeFileName);
	    }
	    lastRelativFileName = null;
	}
    }

    public ChangedFile getLastRelativFileName() {
	synchronized (queue) {
	    return lastRelativFileName;
	}
    }

    @Override
    public Iterator<String> iterator() {
	return queue.keySet().iterator();
    }
}
