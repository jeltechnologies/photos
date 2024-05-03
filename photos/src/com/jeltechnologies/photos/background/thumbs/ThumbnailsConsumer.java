package com.jeltechnologies.photos.background.thumbs;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.pictures.JPEGMetaDataDrewnOakes;
import com.jeltechnologies.photos.pictures.JPEGMetaDataJavaXT;
import com.jeltechnologies.photos.pictures.MediaQueue;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.pictures.PhotoJPEGDataUpdater;
import com.jeltechnologies.photos.pictures.PhotoRotation;
import com.jeltechnologies.photos.utils.FileUtils;
import com.jeltechnologies.photos.utils.ImageUtils;
import com.jeltechnologies.photos.utils.StringUtils;

import javaxt.io.Image;

public class ThumbnailsConsumer extends AbstractConsumer implements ThumbnailsConsumerMBean {
    private final static Logger LOGGER = LoggerFactory.getLogger(ThumbnailsConsumer.class);

    private final static boolean APPLE_HEIC_CONVERTER_AVAILABLE = ENV.getConfig().isCanConvertApplePhotos();

    private final List<Dimension> dimensions = Environment.INSTANCE.getAllThumbnailDimensions();

    public ThumbnailsConsumer(MediaQueue queue, String threadName, NotWorkingFiles notWorkingFiles, boolean moveFailedFiles) {
	super(queue, threadName, notWorkingFiles, moveFailedFiles);
    }

    protected MediaType getMediaType() {
	return MediaType.PHOTO;
    }

    protected void handleFile(boolean newPhoto) throws Exception {
	makeThumbnailsIfNeeded();
	
	//enforceUpdatingCoordinates();
	
	if (newPhoto) {
	    makeThumbnailsIfNeeded();
	    updateThumbDimensions();
	} else {
	    makeThumbnailsIfNeeded();
	}
    }
    
    @SuppressWarnings("unused")
    private void enforceUpdatingCoordinates() {
	File thumbFile = thumbUtils.getThumbFile(Environment.INSTANCE.getDimensionOriginal(), photo);
	getJPGMetadata(thumbFile);
    }

    private void makeThumbnailsIfNeeded() throws IOException, InterruptedException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("makeThumbnailsIfNeeded: " + photo);
	}
	for (Dimension dimension : dimensions) {
	    File thumbFile = thumbUtils.getThumbFile(dimension, photo);
	    if (!thumbFile.isFile()) {
		makeThumb(thumbFile, dimension, photo);
	    }
	}
    }

    private void makeThumb(File thumbFile, Dimension dimension, Photo photo) throws IOException, InterruptedException {
	if (Thread.interrupted()) {
	    throw new InterruptedException();
	}

	File imageFile = queuedMediaFile.getFile();
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Creating thumbFile for ORIGINAL " + imageFile + " to " + thumbFile + "  " + ", with orientation " + photo.getOrientation());
	}

	switch (dimension.getType()) {
	    case ORIGINAL: {
		if (queuedMediaFile.isApple()) {
		    if (APPLE_HEIC_CONVERTER_AVAILABLE) {
			String nameWithoutExtension = StringUtils.stripAfterLast(thumbFile.getAbsolutePath(), ".");
			File tempHeicFile = new File(nameWithoutExtension + ".HEIC");
			FileUtils.copyFile(imageFile, tempHeicFile, true);
			File thumbJpg = new ApplePhotosConverter(tempHeicFile).getConvertedFile();
			getJPGMetadata(thumbJpg);
			tempHeicFile.delete();
		    } else {
			LOGGER.debug("Apple HEIC converter not available, cannot convert " + photo.getRelativeFileName());
		    }
		} else {
		    checkJpgLoadsOK(imageFile);
		    FileUtils.copyFile(imageFile, thumbFile, true);
		    if (queuedMediaFile.isJpg()) {
			getJPGMetadata(thumbFile);
		    }
		}
		break;
	    }
	    default: {
		File jpg;
		if (queuedMediaFile.isApple()) {
		    jpg = thumbUtils.getThumbFile(ENV.getDimensionOriginal(), photo);
		} else {
		    jpg = imageFile;
		}
		PhotoRotation rotation = PhotoRotation.getRotation(photo.getOrientation());
		Image image = ImageUtils.fixOrientation(jpg, rotation);

		if (image == null || image.getBufferedImage() == null) {
		    String errorMessage = "Image could not be loaded: " + photo.getRelativeFileName();
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

    private void getJPGMetadata(File jpgFile) {
	PhotoJPEGDataUpdater metaData = new JPEGMetaDataJavaXT(jpgFile);
	metaData.addMetaData(photo);

	LocalDateTime dateTaken = photo.getDateTaken();
	if (impossibleDate(dateTaken)) {
	    // Fallback on second option
	    JPEGMetaDataDrewnOakes drewnOakes;
	    try {
		drewnOakes = new JPEGMetaDataDrewnOakes(jpgFile);
		dateTaken = drewnOakes.getDateTaken();
		if (impossibleDate(dateTaken)) {
		    long lastModified = jpgFile.lastModified();
		    LocalDateTime dateLastModified = Instant.ofEpochMilli(lastModified).atZone(ZoneId.systemDefault()).toLocalDateTime();
		    photo.setDateTaken(dateLastModified);
		} else {
		    photo.setDateTaken(dateTaken);
		}

	    } catch (IOException e) {
		LOGGER.warn("Cannot get the dateTaken EXIF from " + jpgFile);
	    }
	}
    }

    private void updateThumbDimensions() {
	File thumbFile = thumbUtils.getThumbFile(Environment.INSTANCE.getDimensionFullscreen(), photo);
	if (thumbFile.isFile()) {
	    Image image = new Image(thumbFile);
	    if (image != null) {
		photo.setThumbWidth(image.getWidth());
		photo.setThumbHeight(image.getHeight());
	    }
	} else {
	    LOGGER.warn("Cannot find dimensions because cannot find thumbfile " + thumbFile);
	}
    }

}
