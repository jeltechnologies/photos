package com.jeltechnologies.photos.exiftool;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.utils.StringUtils;
import com.jeltechnologies.util.OperatingSystemCommand;

public class ExifToolGpsGrabber {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExifToolGpsGrabber.class);

    private final static Environment ENV = Environment.INSTANCE;

    private final File file;

    private final File exe;

    public ExifToolGpsGrabber(File file) {
	this.file = file;
	this.exe = ENV.getConfig().getExifToolExecutable();
    }

    public ExifToolGpsGrabber(File file, File exe) {
	this.file = file;
	this.exe = exe;
    }

    public Coordinates getCoordinates() throws IOException, InterruptedException {
	OperatingSystemCommand command = new OperatingSystemCommand(exe);
	command.addArgument(file.getAbsolutePath());
	command.addArgument("-gpsposition");
	command.addArgument("-n");
	command.execute();
	List<String> output = command.getOutput();
	Coordinates coordinates = null;
	if (output.size() > 0) {
	    String line = output.get(0);
	    int firstDoublePoint = line.indexOf(":");
	    if (firstDoublePoint < 0) {
		if (LOGGER.isTraceEnabled()) {
		    LOGGER.trace("Skipping line in metadata for line " + line + " in file: " + file.toString());
		}
	    } else {
		String gpsPosition = line.substring(firstDoublePoint + 1).trim();
		List<String> parts = StringUtils.split(gpsPosition, ' ');
		if (parts.size() == 2) {
		    String latituide = parts.get(0);
		    String longitude = parts.get(1);
		    coordinates = new Coordinates(latituide, longitude);
		} else {
		    if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Skipping line " + line + " in file " + file);
		    }
		}
	    }
	}
	return coordinates;
    }

}
