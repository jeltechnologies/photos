package com.jeltechnologies.photos.background.thumbs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.background.sftp.client.SyncThread;

public class GetLatestPicturesThread implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(GetLatestPicturesThread.class);

    @Override
    public void run() {
	String threadName = getClass().getName();
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("Started " + threadName);
	}

	try {
	    startZinkers();
	    startProducers();
	} catch (Exception e) {
	    LOGGER.error("Error getting last pictures: " + e.getMessage(), e);
	}

	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("Ended " + threadName);
	}
    }

    private void startZinkers() throws Exception {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("startZinkers start");
	}
	List<SyncThread> zinkers = BackgroundServices.getInstance().getSFTPClientThreads();
	for (Runnable zinker : zinkers) {
	    zinker.run();
	}
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("startZinkers ended");
	}
    }

    private void startProducers() throws Exception {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("startProducers start");
	}
	BackgroundServices.getInstance().startProducer();
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("startProducers ended");
	}
    }

}
