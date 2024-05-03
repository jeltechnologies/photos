package com.jeltechnologies.photos.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.db.Database;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextListener.class);
    
    private static final String CONFIG_ENVIRONMENT_VARIABLE = "PHOTOS_CONFIG";

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
	LOGGER.info("===============================================================================================================");
	LOGGER.info("Photos starting");

	Database database = null;
	try {
	    String environmentName = CONFIG_ENVIRONMENT_VARIABLE;
	    String value = System.getProperty(environmentName);
	    if (value == null || value.isEmpty()) {
		value = System.getenv(environmentName);
	    }
	    if (value != null) {
		Environment.INSTANCE.init(value);
		database = new Database();
		database.createDatabaseTablesNotExists();
		database.commit();
		BackgroundServices.getInstance().start();
		LOGGER.info("Photos context initialized");
	    } else {
		LOGGER.error("Cannot load configuration. Set environment variable " + CONFIG_ENVIRONMENT_VARIABLE + " to the yaml file with configuration");
	    }
	} catch (Throwable e) {
	    LOGGER.error("Error starting photos web application: " + e.getMessage(), e);
	} finally {
	    if (database != null) {
		database.close();
	    }
	}
	LOGGER.info("===============================================================================================================");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
	System.out.println("Will shut down the photo application");
	BackgroundServices.getInstance().shutdown();
	Environment.deleteTempFiles();
	if (LOGGER.isInfoEnabled()) {
	    System.out.println("Photos context destroyed");
	}
    }
}
