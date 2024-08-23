package com.jeltechnologies.photos.videos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.background.thumbs.AbstractConsumer;
import com.jeltechnologies.photos.config.yaml.HandbrakeConfiguration;
import com.jeltechnologies.photos.config.yaml.HandbrakeEncodingSettings;
import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.datatypes.MovieQuality;
import com.jeltechnologies.photos.pictures.MediaQueue;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.utils.StringUtils;

import javaxt.io.Image;

public class VideoConsumer extends AbstractConsumer implements VideoConsumerMBean {
    private final static Logger LOGGER = LoggerFactory.getLogger(VideoConsumer.class);
    private final boolean enforceEncoding = false;
    private final boolean enforceGetMetaData = false;
    private final boolean enforceCreatetPosterAndThumb = false;
    private static final Dimension FULL_SCREEN = Environment.INSTANCE.getDimensionFullscreen();
    private static final Dimension THUMB = Environment.INSTANCE.getDimensionThumbs();
    private File videoFile;
    private List<File> createdFiles = null;

    public VideoConsumer(MediaQueue queue, String threadName, boolean moveFailedFiles) {
	super(queue, threadName, moveFailedFiles);
    }

    protected void handleFile() throws Exception {
	videoFile = queuedMediaFile.getFile();
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("handleFile() " + videoFile);
	}
	createdFiles = new ArrayList<File>();
	try {
	    PosterAndCoordinatesFileEncoder ffmpeg = new PosterAndCoordinatesFileEncoder(consumption);
	    if (consumption.isAdded() || enforceGetMetaData) {
		ffmpeg.parseMetaData();
	    }

	    File hq = handbrakeConvertToMP4(new MovieQuality(MovieQuality.Type.HIGH));
	    handbrakeConvertToMP4(new MovieQuality(MovieQuality.Type.LOW));

	    File posterFile = thumbUtils.getThumbFile(FULL_SCREEN, consumption.getPhoto());
	    if (!posterFile.isFile() || enforceCreatetPosterAndThumb || consumption.getThumbHeight() == 0 || consumption.getThumbWidth() == 0) {
		ffmpeg.createPosterAndDimensions(hq, FULL_SCREEN, posterFile);
		createdFiles.add(posterFile);
	    }
	    File thumbFile = thumbUtils.getThumbFile(THUMB, consumption.getPhoto());
	    if (!thumbFile.isFile() || enforceCreatetPosterAndThumb) {
		createThumbnail(posterFile, thumbFile);
		createdFiles.add(thumbFile);
	    }
	} catch (Exception e) {
	    for (File file : createdFiles) {
		cleanNotFinished(file);
	    }
	    throw e;
	}
    }

    private void createThumbnail(File input, File output) {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("createThumbnail " + input + " => " + output);
	}
	if (enforceEncoding || !output.isFile()) {
	    if (!input.isFile()) {
		LOGGER.warn("PosterFile does not exist " + input);
	    } else {
		Image image = new Image(input);
		image.setHeight(THUMB.getHeight());
		image.setOutputQuality(Environment.JPG_QUALITY);
		if (output.exists()) {
		    boolean ok = output.delete();
		    if (!ok) {
			LOGGER.warn("Could not overwrite poster thumbFile with new image " + output);
		    }
		}
		image.saveAs(output);
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("Created poster thumb " + output.getAbsolutePath());
		}
	    }
	}
    }

    @Override
    protected MediaType getMediaType() {
	return MediaType.VIDEO;
    }

    private File handbrakeConvertToMP4(MovieQuality quality) throws VideoConvertException, InterruptedException {
	File destination = thumbUtils.getConvertedMovie(quality, consumption.getPhoto());

	boolean mustConvert = enforceEncoding || !destination.isFile();
	
//	if (!mustConvert) {
//	    boolean highResulution = consumption.getThumbHeight() > 1080 || consumption.getThumbWidth() > 1920;
//	    mustConvert = quality.getType() == Type.HIGH && highResulution;
//	}
	
//	if (consumption.getId().equals("35f3481dfab2f5ca5c5c93fad6ba187a")) {
//	    mustConvert = true;
//	}
//	
	if (mustConvert) {
	    HandbrakeConfiguration handBrakeConfig = Environment.INSTANCE.getConfig().getHandbrakeConfiguration();
	    HandbrakeEncodingSettings encodingSettings = null;
	    switch (quality.getType()) {
		case HIGH: {
		    encodingSettings = handBrakeConfig.getQualityHigh();
		    setStatus("Converting to MP4 with high quality:" + videoFile, true);
		    break;
		}
		case LOW: {
		    encodingSettings = handBrakeConfig.getQualityLow();
		    setStatus("Converting to MP4 with low quality:" + videoFile, true);
		    break;
		}
		default: {
		    throw new IllegalArgumentException("Unsupported type: " + quality.getType());
		}
	    }
	    createdFiles.add(destination);
	    boolean removeHDR = checkHDRMustBeRemoved();
	    new HandbrakeEncoder(videoFile, destination, encodingSettings).convert(removeHDR);
	    LOGGER.debug("Succesfully encoded with handbrake: " + videoFile.getName() + " to file " + destination);
	}
	return destination;
    }
    
    private boolean checkHDRMustBeRemoved() {
	boolean result = false;
	if (!consumption.isLivePhoto()) {
	    String iPhone = StringUtils.findAfter(consumption.getSource(), "Apple iPhone ").trim();
	    if (!iPhone.isBlank()) { 
		try {
		    int model = StringUtils.stripToInteger(iPhone);
		    result = model >= 12;
		}
		catch (Exception e) {
		    // ignore numberformatexception
		}
	    }
	}
	if (result == true && !consumption.isLivePhoto()) {
	    result = true;
	} else {
	    result = false;
	}
	return result;
	
    }

    private void cleanNotFinished(File file) {
	if (file != null) {
	    if (file.exists()) {
		boolean ok = file.delete();
		if (!ok) {
		    LOGGER.warn("Cannot clean err");
		}
	    }
	}
    }

}
