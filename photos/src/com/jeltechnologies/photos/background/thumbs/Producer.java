package com.jeltechnologies.photos.background.thumbs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.datatypes.usermodel.Role;
import com.jeltechnologies.photos.pictures.MediaQueue;
import com.jeltechnologies.photos.utils.FileUtils;

public class Producer implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    protected final List<ProduceFolder> folders = new ArrayList<ProduceFolder>();
    protected String threadName;
    private volatile boolean running;
    
    public enum Type {
	ONLY_ADD_NEW_PHOTOS, COMPLETE_REFRESH_AT_STARTUP, REFRESH_ALL_METADATA
    }
    
    private final Type type;

    public Producer(Type type) {
	this.threadName = null;
	this.type = type;
    }

    public Producer(String threadName, Type type) {
	this.threadName = threadName;
	this.type = type;
    }

    public void add(ProduceFolder folder) {
	folders.add(folder);
    }
    
    public void run() {
	if (threadName == null) {
	    threadName = Thread.currentThread().getName();
	} else {
	    Thread.currentThread().setName(threadName);
	}
	running = true;
	LOGGER.info(threadName + " started");
	try {
	    for (ProduceFolder folder : folders) {
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("Producing for " + folder.getFolder());
		}
		List<File> files = new ArrayList<>();
		FileUtils.getFilesIterative(folder.getFolder(), files, folder.getFilenameFilter());
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("Files found " + files.size());
		}
		Role role = folder.getRole();
		MediaQueue queue = folder.getQueue();
		for (File file : files) {
		    if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Adding file to queue: " + file);
		    }
		    queue.add(role, file, type);
		}
	    }
	} catch (InterruptedException interruptedException) {
	    LOGGER.info(threadName + "interrupted");
	}
	running = false;
	LOGGER.info(threadName + " ended");
    }
    
    public boolean isRunning() {
	return running;
    }
    
    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("Producer [folders=");
	builder.append(folders);
	builder.append(", threadName=");
	builder.append(threadName);
	builder.append(", running=");
	builder.append(running);
	builder.append("]");
	return builder.toString();
    }

}
