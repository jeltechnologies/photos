package com.jeltechnologies.photos.picures.map.geoservice;

import java.io.IOException;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.geoservices.datamodel.AddressRequest;
import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.background.thumbs.PhotoInConsumption;

public class PhotoAddressUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoAddressUpdater.class);

    private static final int TIME_OUT_AFTERS_SECONDS = 10;
    
    private static final int RETRIES = 5;
    
    private static final int WAIT_BEFORE_RETRY_MAX_TIME = 5;
    
    private final ExecutorService executorService;

    public PhotoAddressUpdater() {
	this.executorService = BackgroundServices.getInstance().getThreadService().getExecutor();
    }

    public void updateAddress(PhotoInConsumption p) throws LocationUpdateException, InterruptedException, IOException {
	Coordinates c = p.getCoordinates();
	if (c != null) {
	    boolean success = false;
	    for (int attempts = 1; !success && attempts < RETRIES; attempts++) {
		try {
		    if (Thread.interrupted()) {
			throw new InterruptedException();
		    }
		    Future<AddressRequest> geoClient = executorService.submit(new GeoServiceClient(c));
		    AddressRequest addressRequest = geoClient.get(TIME_OUT_AFTERS_SECONDS, TimeUnit.SECONDS);
		    if (addressRequest != null) { 
			if (LOGGER.isDebugEnabled()) {
			    LOGGER.debug("Received address: " + addressRequest.toString());
			}
			p.setAddress(addressRequest.answer().getAddress());
			p.setDistanceFromAddress(addressRequest.answer().getDistanceFromQuery());
			success = true;
		    }
		} catch (InterruptedException e) {
		    LOGGER.trace("Interrupted.");
		} catch (ExecutionException e) {
		    Throwable cause = e.getCause();
		    if (cause instanceof IOException) {
			throw new LocationUpdateException(cause);
		    } else {
			throw new IOException("Caannot get address for picture: " + cause.getMessage(), cause);
		    }
		} catch (TimeoutException toe) {
		    int randomWait = new Random().nextInt(WAIT_BEFORE_RETRY_MAX_TIME);
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Timeout in attempt " + attempts + ". Waiting for " + randomWait + "seconds");
		    }
		    Thread.sleep(Duration.ofSeconds(randomWait));
		}
	    }
	}
    }

}