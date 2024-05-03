package com.jeltechnologies.photos.background.sftp.server;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.background.thumbs.SftpProducerQueue;
import com.jeltechnologies.photos.background.thumbs.SingleMediaProducer;
import com.jeltechnologies.photos.datatypes.usermodel.Role;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;

public class ChangedFileProducer implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangedFileProducer.class);

    private final SftpProducerQueue queue;

    private static final int POLL_TIME_SECONDS = 10;

    private static final int POLL_TIME_MILLISECONDS = POLL_TIME_SECONDS * 1000;

    private static final int PRODUCE_WHEN_LAST_FILE_IS_OLD_SECONDS = 20;

    private final static Role ROLE = RoleModel.ROLE_ADMIN;

    private static final String THREAD_NAME = ChangedFileProducer.class.getName();

    public ChangedFileProducer(SftpProducerQueue queue) {
	this.queue = queue;
	Thread.currentThread().setName(THREAD_NAME);
    }

    @Override
    public void run() {
	LOGGER.info(THREAD_NAME + " started");
	boolean interrupted = false;
	while (!interrupted) {
	    try {
		ChangedFile lastChangedFile = queue.getLastRelativFileName();
		if (lastChangedFile != null) {
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Last file: " + lastChangedFile);
		    }
		    LocalDateTime lastTime = lastChangedFile.getTime();
		    LocalDateTime now = LocalDateTime.now();
		    long diff = ChronoUnit.SECONDS.between(lastTime, now);
		    if (diff >= PRODUCE_WHEN_LAST_FILE_IS_OLD_SECONDS) {
			sendFilesToMediaProducer();
		    }
		} else {
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("No files in queue");
		    }
		}
		Thread.sleep(POLL_TIME_MILLISECONDS);

	    } catch (InterruptedException e) {
		LOGGER.debug("Interrupted");
		interrupted = true;
	    }
	}
	LOGGER.info(THREAD_NAME + " ended");

    }

    private void sendFilesToMediaProducer() {
	List<File> files = new ArrayList<File>();
	for (String relativeFileName : queue) {
	    ChangedFile changedFile = queue.get(relativeFileName);
	    files.add(changedFile.getFile());
	}
	queue.empty();
	if (!files.isEmpty()) {
	    if (LOGGER.isInfoEnabled()) {
		LOGGER.debug("Sending " + files.size() + " file(s) in queue to Media Producer");
		for (File file : files) {
		    LOGGER.debug("  " + file);
		}
	    }
	    SingleMediaProducer producer = new SingleMediaProducer(ROLE);
	    for (File file : files) {
		producer.addToQueue(file);
	    }
	}
    }

}
