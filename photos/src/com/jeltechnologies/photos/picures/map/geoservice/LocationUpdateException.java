package com.jeltechnologies.photos.picures.map.geoservice;

public class LocationUpdateException extends Exception {
    
    private final String message;
    
    public LocationUpdateException(Throwable cause) {
	super(cause);
	this.message = cause.getMessage();
    }
    
    public String getMessage() {
	return message;
    }

    private static final long serialVersionUID = 7697523005052534711L;

}
