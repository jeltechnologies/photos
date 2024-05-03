package com.jeltechnologies.photos.db;

public class DBException extends Exception {
    private static final long serialVersionUID = 2510120037529380263L;

    public DBException(String message) {
	super(message);
    }
    
    public DBException(String message, Throwable t) {
	super(message, t);
    }

}
