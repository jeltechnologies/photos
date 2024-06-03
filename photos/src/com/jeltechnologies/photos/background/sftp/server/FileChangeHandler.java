package com.jeltechnologies.photos.background.sftp.server;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.background.thumbs.SftpProducerQueue;

public class FileChangeHandler implements FileChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileChangeHandler.class);

    private SftpProducerQueue queue = new SftpProducerQueue();

    public FileChangeHandler() {
	startListeningThread();
    }

    private void startListeningThread() {
	ChangedFileProducer producer = new ChangedFileProducer(queue);
	BackgroundServices.getInstance().startTask(producer);
    }

    @Override
    public void fileChanged(File file) {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("File changed " + file);
	}
	boolean add = true;
	if (file == null) {
	    add = false;
	}
	if (add && !file.isFile()) {
	    add = false;
	}
	if (isPartialUpload(file)) {
	    boolean delete = file.delete();
	    LOGGER.debug(file.getName() + " deleted: " + delete);
	} else {
	    if (add) {
		queue.add(file);
	    }
	}
    }

    private boolean isPartialUpload(File file) {
	boolean partial = false;
	String name = file.getName();
	// Partial file for Photosync on IPhone
	if (name.startsWith("_") || name.startsWith(".")) {
	    partial = true;
	}
	// Partial file for WS/FTP
	if (!partial && name.endsWith(".filepart")) {
	    partial = true;
	}
	LOGGER.debug(file.getName() + " => partial: " + partial);
	return partial;
    }

}
