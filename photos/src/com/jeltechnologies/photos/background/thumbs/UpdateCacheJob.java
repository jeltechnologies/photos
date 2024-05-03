package com.jeltechnologies.photos.background.thumbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.timeline.TimelineCacheUpdateThread;

public class UpdateCacheJob implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCacheJob.class);

    private final BackgroundServices backgroundServices;
    
    public UpdateCacheJob(BackgroundServices backgroundServices) {
	this.backgroundServices = backgroundServices;
    }
    
    @Override
    public void run() {
	String name = this.getClass().getSimpleName();
	LOGGER.info(name + " started");
	int thumbQueue = -1;
	int videoQueue = -1;
	try {
	    while (thumbQueue != 0 && videoQueue != 0) {
		Thread.sleep(500);
		thumbQueue = backgroundServices.getThumbsQueueSize();
		videoQueue = backgroundServices.getVideoQueueSize();
	    }
	    Runnable task = new TimelineCacheUpdateThread();
	    backgroundServices.startTask(task);
	    LOGGER.info(name + " ended");
	} catch (InterruptedException e) {
	    LOGGER.info(name + " interrupted");
	}
	
    }

}
