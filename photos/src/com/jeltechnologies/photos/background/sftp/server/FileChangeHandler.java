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
	queue.add(file);
    }

}
