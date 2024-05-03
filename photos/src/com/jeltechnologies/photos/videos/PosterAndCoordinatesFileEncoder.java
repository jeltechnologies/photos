package com.jeltechnologies.photos.videos;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.utils.StringUtils;
import com.jeltechnologies.util.OperatingSystemCommand;

import javaxt.io.Image;

public class PosterAndCoordinatesFileEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(PosterAndCoordinatesFileEncoder.class);

    private final File EXECUTABLE = Environment.INSTANCE.getConfig().getFfmpegExecutable();

    private final static Environment ENV = Environment.INSTANCE;

    private final Photo video;

    public PosterAndCoordinatesFileEncoder(Photo video) {
	this.video = video;
    }

    public void parseMetaData() throws VideoConvertException, InterruptedException {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("parseMetaData " + video.getId() + " => " + video.getRelativeFileName());
	}
	File input = ENV.getFile(video.getRelativeFileName());
	File tempFile = null;
	try {
	    tempFile = File.createTempFile("photos-video-", ".jpg");
	    List<String> logLines = convertOneFrameToImage(input, tempFile, 0);

	    boolean foundMake = false;
	    boolean foundModel = false;
	    String make = "";
	    String model = "";

	    VideoMetaDataParser parser = new VideoMetaDataParser(input);

	    for (String line : logLines) {
//		    com.apple.quicktime.creationdate: 2021-06-17T11:41:26+0200
//		    com.apple.quicktime.location.accuracy.horizontal: 14.267767
//		    com.apple.quicktime.live-video.auto: 1
//		    com.apple.quicktime.content.identifier: 9331787B-40EC-48E5-B1E2-DE9DC9676C35
//		    com.apple.quicktime.live-video.vitality-score: 1.000000
//		    com.apple.quicktime.live-video.vitality-scoring-version: 0
//		    com.apple.quicktime.location.ISO6709: +59.3467+018.0719+038.790/
//		    com.apple.quicktime.make: Apple
//		    com.apple.quicktime.model: iPhone 12
//		    com.apple.quicktime.software: 14.6

		String creationDate = StringUtils.findAfter(line, "com.apple.quicktime.creationdate:").trim();
		if (creationDate != null && !creationDate.equals("")) {
		    LocalDateTime ldt = parser.parseDate(creationDate);
		    video.setDateTaken(ldt);
		}
		String location = StringUtils.findAfter(line, "com.apple.quicktime.location.ISO6709:").trim();
		if (location != null && !location.equals("")) {
		    BigDecimal lat = parser.parseLatidude(location);
		    BigDecimal lon = parser.parseLongitude(location);
		    video.setCoordinates(new Coordinates(lat, lon));
		}

		// Duration: 00:00:02.60, start: 0.000000, bitrate: 11369 kb/s
		String duration = StringUtils.findBetween(line, "Duration: ", ",");
		if (duration != null && !duration.equals("")) {
		    video.setDuration(parser.parseDuration(duration));
		}

		// com.apple.quicktime.make: Apple
		if (!foundMake) {
		    make = StringUtils.findAfter(line, "com.apple.quicktime.make:").trim();
		    foundMake = !make.isEmpty();
		}

		// com.apple.quicktime.model: iPhone 13
		if (!foundModel) {
		    model = StringUtils.findAfter(line, "com.apple.quicktime.model:").trim();
		    foundModel = !model.isEmpty();
		}

		// com.apple.quicktime.live-video.auto: 1
		String liveVideoValue = StringUtils.findAfter(line, "com.apple.quicktime.live-video.auto:").trim();
		if (!liveVideoValue.isBlank()) {
		    video.setLivePhoto(true);
		}
		
		// com.apple.quicktime.live-photo.auto:
		String livePhotoValue = StringUtils.findAfter(line, "com.apple.quicktime.live-photo.auto:").trim();
		if (!livePhotoValue.isBlank()) {
		    video.setLivePhoto(true);
		}

		if (LOGGER.isTraceEnabled()) {
		    LOGGER.trace(line);
		}
	    }

	    if (foundMake || foundModel) {
		String source = (make + " " + model).trim();
		if (!source.equals(video.getSource())) {
		    video.setSource(source);
		}
	    }
	    if (video.getDateTaken() == null) {
		LocalDateTime date;
		try {
		    BasicFileAttributes attr = Files.readAttributes(input.toPath(), BasicFileAttributes.class);
		    date = attr.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Read date from filesystem: " + date);
		    }
		} catch (IOException e) {
		    LOGGER.error("Error when trying to get date", e);
		    date = LocalDateTime.now();
		}
		video.setDateTaken(date);
	    }

	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Converting " + input.getName() + " completed successfully");
	    }
	} catch (IOException e) {
	    throw new VideoConvertException("Error converting video file [" + input + "], because " + e.getMessage(), e);
	} finally {
	    deleteTempFile(tempFile);
	}
    }

    private void deleteTempFile(File tempFile) {
	if (tempFile != null) {
	    boolean ok = tempFile.delete();
	    if (!ok) {
		LOGGER.warn("Cannot delete temporary file " + tempFile);
	    }
	}
    }

    public boolean createPosterAndDimensions(File input, Dimension dimension, File output) throws InterruptedException, VideoConvertException {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("createPosterAndDimensions " + video.getId() + " => " + video.getRelativeFileName() + " => " + dimension + " => " + output);
	}
	File tempFile = null;
	//File input = ENV.getFile(video.getRelativeFileName());
	boolean updatedDimension = false;
	try {
	    tempFile = File.createTempFile("photos-video-", ".jpg");
	    int halfDuration = video.getDuration() / 2;
	    convertOneFrameToImage(input, tempFile, halfDuration);

	    Image image;
	    image = new Image(tempFile);
	    int currentWidth = video.getThumbWidth();
	    int currentHeight = video.getThumbHeight();
	    if (currentWidth != image.getWidth()) {
		video.setThumbWidth(image.getWidth());
		updatedDimension = true;
	    }

	    if (currentHeight != image.getHeight()) {
		video.setThumbHeight(image.getHeight());
		updatedDimension = true;
	    }

	    int thumbHeight = dimension.getHeight();
	    image.setHeight(thumbHeight);
	    image.setOutputQuality(Environment.JPG_QUALITY);
	    image.saveAs(output);
	} catch (IOException e) {
	    throw new VideoConvertException("Error converting video file [" + input + "], because " + e.getMessage(), e);
	} finally {
	    deleteTempFile(tempFile);
	}
	return updatedDimension;
    }

    private List<String> convertOneFrameToImage(File input, File output, int atSecond) throws InterruptedException, VideoConvertException {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("createPoster at second " + atSecond + " for " + video.getRelativeFileName() + " stored in " + output);
	}
	
	OperatingSystemCommand command = new OperatingSystemCommand(EXECUTABLE);

	String inPath = input.getAbsolutePath();
	String outPath = output.getAbsolutePath();

	command.addArgument("-y");
	command.addArgument("-i");
	command.addArgument(inPath);
	String duration = toDuration(atSecond);
	command.addArgument("-ss");
	command.addArgument(duration);

	command.addArgument("-frames:v");
	command.addArgument("1");
	command.addArgument(outPath);

	try {
	    command.execute();
	} catch (IOException e) {
//	    LOGGER.warn(command.getDescription());
//	    for (String line : command.getOutput()) {
//		LOGGER.warn(line);
//	    }
	    throw new VideoConvertException("Cannot convert video in " + inPath + ". Error: " + command.getOutput());
	}
	return command.getOutput();
    }

    public String toDuration(int totalSecs) {
	int hours = totalSecs / 3600;
	int minutes = (totalSecs % 3600) / 60;
	int seconds = totalSecs % 60;
	String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
	return timeString;
    }

}
