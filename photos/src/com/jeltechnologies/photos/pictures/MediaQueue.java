package com.jeltechnologies.photos.pictures;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.jeltechnologies.photos.background.thumbs.Producer;
import com.jeltechnologies.photos.datatypes.usermodel.Role;

public class MediaQueue implements MediaQueueMBean {
    private BlockingQueue<QueuedMediaFile> queue = new LinkedBlockingQueue<QueuedMediaFile>();
    
    public MediaQueue() {
    }
    
    public void add(Role role, File file, Producer.Type type) {
	QueuedMediaFile mediaFile = new QueuedMediaFile(file, role, type);
	queue.add(mediaFile);
    }
    
    public QueuedMediaFile poll() throws InterruptedException {
	return queue.poll(60, TimeUnit.MINUTES);
    }
    
    public int getSize() {
	return queue.size();
    }
}
 