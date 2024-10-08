package com.jeltechnologies.photos.background.thumbs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.pictures.MediaQueue;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.PhotoRotation;
import com.jeltechnologies.photos.utils.FileUtils;
import com.jeltechnologies.photos.utils.ImageUtils;
import com.jeltechnologies.photos.utils.StringUtils;

import javaxt.io.Image;

public class ThumbnailsConsumer extends AbstractConsumer implements ThumbnailsConsumerMBean {
    private final static Logger LOGGER = LoggerFactory.getLogger(ThumbnailsConsumer.class);

    private final static boolean APPLE_HEIC_CONVERTER_AVAILABLE = ENV.getConfig().isCanConvertApplePhotos();

    private final static List<Dimension> DIMENSIONS = Environment.INSTANCE.getAllThumbnailDimensions();
    
    private final static Dimension ORIGINAL_DIMENSION = Environment.INSTANCE.getDimensionOriginal();

    private static final boolean ROTATE_HEIC_THUMBNAILS = false;

    public ThumbnailsConsumer(MediaQueue queue, String threadName, boolean moveFailedFiles) {
	super(queue, threadName, moveFailedFiles);
    }

    protected MediaType getMediaType() {
	return MediaType.PHOTO;
    }

    protected void handleFile() throws Exception {
	makeThumbnailsIfNeeded();
    }

    private void makeThumbnailsIfNeeded() throws IOException, InterruptedException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("makeThumbnailsIfNeeded: " + consumption);
	}
	for (Dimension dimension : DIMENSIONS) {
	    File thumbFile = thumbUtils.getThumbFile(dimension, consumption.getPhoto());
	    if (!thumbFile.isFile()) { //   || consumption.getRelativeFolderName().endsWith("2024-07")) { // for testing
		makeThumb(thumbFile, dimension);
	    }
	}
	if (consumption.getThumbHeight() == 0 || consumption.getThumbWidth() == 0) {
	    File thumbFile = thumbUtils.getThumbFile(ORIGINAL_DIMENSION, consumption.getPhoto());
	    updateJPGSize(thumbFile);
	}
    }

    private void makeThumb(File thumbFile, Dimension dimension) throws IOException, InterruptedException {
	if (Thread.interrupted()) {
	    throw new InterruptedException();
	}
	File imageFile = queuedMediaFile.getFile();
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Creating thumbFile for ORIGINAL " + imageFile + " to " + thumbFile + "  " + ", with orientation " + consumption.getOrientation());
	}

	switch (dimension.getType()) {
	    case ORIGINAL: {
		if (queuedMediaFile.isApple()) {
		    if (APPLE_HEIC_CONVERTER_AVAILABLE) {
			String nameWithoutExtension = StringUtils.stripAfterLast(thumbFile.getAbsolutePath(), ".");
			File tempHeicFile = new File(nameWithoutExtension + ".HEIC");
			FileUtils.copyFile(imageFile, tempHeicFile, true);
			File thumbJpg = new ApplePhotosConverter(tempHeicFile).getConvertedFile();
			updateJPGSize(thumbJpg);
			tempHeicFile.delete();
		    } else {
			LOGGER.debug("Apple HEIC converter not available, cannot convert " + consumption.getRelativeFileName());
		    }
		} else {
		    checkJpgLoadsOK(imageFile);
		    FileUtils.copyFile(imageFile, thumbFile, true);
		    if (queuedMediaFile.isJpg()) {
			updateJPGSize(thumbFile);
		    }
		}
		break;
	    }
	    default: {
		File jpg;
		boolean mustRotate;
		if (queuedMediaFile.isApple()) {
		    jpg = thumbUtils.getThumbFile(ENV.getDimensionOriginal(), consumption.getPhoto());
		    mustRotate = ROTATE_HEIC_THUMBNAILS;
		} else {
		    jpg = imageFile;
		    mustRotate = true;
		}
		Image image;
		if (mustRotate) {
		    PhotoRotation rotation = PhotoRotation.getRotation(consumption.getOrientation());
		    image = ImageUtils.fixOrientation(jpg, rotation);
		} else {
		    image = new Image(jpg);
		}

		if (image == null || image.getBufferedImage() == null) {
		    String errorMessage = "Image could not be loaded: " + consumption.getRelativeFileName();
		    throw new IllegalArgumentException(errorMessage);
		} else {
		    image.setHeight(dimension.getHeight());
		    File folder = thumbFile.getParentFile();
		    if (!folder.isDirectory()) {
			boolean ok = folder.mkdirs();
			if (ok) {
			    LOGGER.debug("Created thumbnail directory " + folder.getAbsolutePath());
			} else {
			    LOGGER.error("Could not create folder " + folder.getAbsolutePath());
			}
		    }
		    image.setOutputQuality(Environment.JPG_QUALITY);
		    if (thumbFile.exists()) {
			boolean ok = thumbFile.delete();
			if (!ok) {
			    LOGGER.warn("Could not overwrite thumbFile with new image " + thumbFile);
			}
		    }
		    image.saveAs(thumbFile);
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Created thumb " + thumbFile.getAbsolutePath());
		    }
		}
	    }
	}
    }

    private void checkJpgLoadsOK(File imageFile) throws IOException {
	Image image = new Image(imageFile);
	if (image.getBufferedImage() == null) {
	    throw new IOException("Cannot open file " + imageFile.getAbsolutePath());
	}
    }

    private void updateJPGSize(File jpgFile) {
	Image image = new Image(jpgFile);
	if (image != null) {
	    consumption.setThumbWidth(image.getWidth());
	    consumption.setThumbHeight(image.getHeight());
	}
    }
}
