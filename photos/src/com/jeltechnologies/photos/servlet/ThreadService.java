package com.jeltechnologies.photos.servlet;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;

public class ThreadService implements Serializable {
    private static final long serialVersionUID = -7466494504073151203L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadService.class);
    private static final int THREAD_POOL_SIZE_SCHEDULE = 100;
    private static final int THREAD_POOL_SIZE_SESSIONS = THREAD_POOL_SIZE_SCHEDULE + Environment.INSTANCE.getConfig().getNrOfConsumers();

    private ScheduledExecutorService scheduler = null;
    private ExecutorService executor = null;

    public ThreadService() {
	scheduler = Executors.newScheduledThreadPool(THREAD_POOL_SIZE_SCHEDULE);
	executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE_SESSIONS);
    }
    
    public ExecutorService getExecutor() {
   	return executor;
    }

    public void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
	scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public void scheduleOnce(Runnable callable, long delay, TimeUnit unit) {
	scheduler.schedule(callable, delay, unit);
    }

    public void scheduleDailyAt(Runnable command, int hour, int minute) {
	ZonedDateTime now = ZonedDateTime.now();
	ZonedDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(0);
	if (now.compareTo(nextRun) > 0) {
	    nextRun = nextRun.plusDays(1);
	}
	Duration duration = Duration.between(now, nextRun);
	long initalDelay = duration.getSeconds();
	scheduler.scheduleAtFixedRate(command, initalDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    }

    public void execute(Runnable command) {
	executor.execute(command);
    }

    @SuppressWarnings("rawtypes")
    public Future submit(Runnable command) {
	return executor.submit(command);
    }
    
    public void shutdown() {
	LOGGER.info(this.getClass().getSimpleName() + " will initiate shutdown.....");
	if (executor != null) {
	    executor.shutdownNow();
	}
	if (scheduler != null) {
	    scheduler.shutdownNow();
	}
	LOGGER.info(this.getClass().getSimpleName() + " ..... shutdown completed");
    }
}
