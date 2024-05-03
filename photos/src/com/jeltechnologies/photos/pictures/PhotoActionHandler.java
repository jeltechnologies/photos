package com.jeltechnologies.photos.pictures;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.servlet.PhotoAction;

public abstract class PhotoActionHandler {

    protected Database database;
    
    protected final PhotoAction action;
        
    protected static final Environment ENV = Environment.INSTANCE;
    
    public abstract void handleDetails() throws Exception;
    
    public PhotoActionHandler(PhotoAction action) {
	this.action = action;
    }

    public void handle() throws Exception {
	database = null;
	try {
	    database = new Database();
	    handleDetails();
	} 
	catch (Exception e) {
	    database.rollback();
	    throw e;
	}
	finally {
	    if (database != null) {
		database.close();
	    }
	}
    }

}
