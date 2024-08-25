package com.jeltechnologies.photos.exiftool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.util.OperatingSystemCommand;

public class ExifToolGrabber {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExifToolGrabber.class);

    private final static ExifToolTagFilter FILTER = new ExifToolTagFilter();

    private final static Environment ENV = Environment.INSTANCE;

    private final File file;

    private final File exe;

    public ExifToolGrabber(File file) {
	this.file = file;
	this.exe = ENV.getConfig().getExifToolExecutable();
    }

    public ExifToolGrabber(File file, File exe) {
	this.file = file;
	this.exe = exe;
    }

    public MetaData getMetaData() throws IOException, InterruptedException {
	OperatingSystemCommand command = new OperatingSystemCommand(exe);
	command.addArgument(file.getAbsolutePath());
	command.addArgument("-orientation#");
	command.addArgument("-all");
	command.execute();
	List<String> output = command.getOutput();
	MetaData meta = parse(output);
	return meta;
    }

    private MetaData parse(List<String> output) {
	Map<String, String> tags = new HashMap<String, String>();
	for (String line : output) {
	    int firstDoublePoint = line.indexOf(":");
	    if (firstDoublePoint < 0) {
		if (LOGGER.isTraceEnabled()) {
		    LOGGER.trace("Skipping line in metadata for line " + line + " in file: " + file.toString());
		}
	    } else {
		String name = line.substring(0, firstDoublePoint).trim();
		if (FILTER.isInFilter(name)) {
		    String value = line.substring(firstDoublePoint + 1).trim();
		    boolean valid = true;
		    if (name.equals("Orientation")) {
			try {
			    Integer.parseInt(value);
			} catch(NumberFormatException nfe) {
			    name = "Orientation Description";
			}
		    }
		    if (valid) {
			tags.put(name, value);
		    }
		}
	    }
	}
	List<MetaTag> tagsList = new ArrayList<MetaTag>(tags.size());
	for (String name : tags.keySet()) {
	    String value = tags.get(name);
	    tagsList.add(new MetaTag(name, value));
	}
	Collections.sort(tagsList);
	MetaData meta = new MetaData(tagsList);
	return meta;
    }

    public static void main(String[] args) throws Exception {
	File f = new File("C:\\tmp\\4925ae0e76a0e5ac45e0f7fa3c175353-original.HEIC");
	File exe = new File("E:\\Coding\\exiftool-12.92_64\\exiftool.exe");
	MetaData metaData = new ExifToolGrabber(f, exe).getMetaData();
	System.out.println(metaData);
    }
}
