package com.jeltechnologies.photos.background.thumbs;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.exiftool.ExifToolDateUtils;
import com.jeltechnologies.photos.exiftool.ExifToolGpsGrabber;
import com.jeltechnologies.photos.exiftool.ExifToolGrabber;
import com.jeltechnologies.photos.exiftool.MetaData;
import com.jeltechnologies.photos.pictures.MediaFile;
import com.jeltechnologies.photos.pictures.MediaQueue;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.pictures.QueuedMediaFile;
import com.jeltechnologies.photos.pictures.ThumbnailUtils;
import com.jeltechnologies.photos.pictures.ThumbnailUtilsFactory;
import com.jeltechnologies.photos.picures.map.geoservice.LocationUpdateException;
import com.jeltechnologies.photos.picures.map.geoservice.PhotoAddressUpdater;
import com.jeltechnologies.photos.timeline.TimeLineTurboCache;
import com.jeltechnologies.photos.utils.FileUtils;
import com.jeltechnologies.photos.utils.StringUtils;

public abstract class AbstractConsumer implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractConsumer.class);

    private boolean moveFailedFiles = false;

    protected static final Environment ENV = Environment.INSTANCE;

    private final static LocalDateTime EARLIEST_POSSIBLE = LocalDateTime.of(1900, 1, 1, 0, 0);

    private final static boolean FORCE_LOCATION_UPDATE = false;

    private final static boolean FORCE_UPDATE_EXIF = false;

    private final static boolean LOCATION_SERVICE_CONFIGURED = ENV.getConfig().isGeoServicesConfigured();

    private final static boolean EXIFTOOL_CONFIGURED = ENV.getConfig().isCanUseExifTool();

    private final MediaQueue queue;

    private final String threadName;

    private final NotWorkingFiles notWorkingFiles;

    private Database database;

    protected QueuedMediaFile queuedMediaFile;

    protected PhotoInConsumption consumption;

    private String status;

    private AtomicBoolean locationServiceAvailable = new AtomicBoolean(true);

    protected final ThumbnailUtils thumbUtils = ThumbnailUtilsFactory.getUtils();

    protected abstract void handleFile() throws Exception;

    protected abstract MediaType getMediaType();

    public AbstractConsumer(MediaQueue queue, String threadName, boolean moveFailedFails) {
	this.threadName = threadName;
	this.queue = queue;
	this.notWorkingFiles = new NotWorkingFiles();
	this.moveFailedFiles = moveFailedFails;
    }

    protected Database getDatabase() {
	if (database == null) {
	    database = new Database();
	}
	return database;
    }

    protected void setStatus(String status) {
	this.status = status;
    }

    protected void setStatus(String status, boolean log) {
	if (log && LOGGER.isDebugEnabled()) {
	    LOGGER.debug("setStatus: " + status);
	}
	setStatus(status);
    }

    public String getStatus() {
	return status;
    }

    @Override
    public void run() {
	Thread.currentThread().setName(threadName);
	if (LOGGER.isInfoEnabled()) {
	    LOGGER.info(threadName + " started");
	}
	boolean interrupted = false;
	String currentFilePath = null;
	while (!interrupted) {
	    try {
		consumption = null;
		setStatus("Waiting (polling) queue");
		queuedMediaFile = queue.poll();
		if (queuedMediaFile != null) {
		    File photoFile = queuedMediaFile.getFile();
		    currentFilePath = FileUtils.getNormalizedPath(photoFile);
		    setStatus("Processing " + currentFilePath);
		    if (!notWorkingFiles.contains(photoFile)) {
			if (photoFile.isFile()) {
			    consumeFile(photoFile);
			} else {
			    LOGGER.warn(photoFile + " is not a file");
			}
		    }
		} else {
		    currentFilePath = null;
		}
		if (database != null) {
		    database.commit();
		}
	    } catch (InterruptedException ie) {
		interrupted = true;
	    } catch (LocationUpdateException lue) {
		LOGGER.warn("Location service is not available, will not try again for one hour (" + lue.getMessage() + ")");
		locationServiceAvailable.set(false);
		scheduleEnablingLocationService(1, TimeUnit.HOURS);
	    } catch (Throwable t) {
		String errorMessage = queuedMediaFile.getFile().getName() + " error " + t.getMessage();
		if (LOGGER.isInfoEnabled()) {
		    LOGGER.info(errorMessage);
		} else {
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.warn(errorMessage, t);
		    }
		}
		if (database != null) {
		    database.rollback();
		}
		NotWorkingFile notWorkingFile = new NotWorkingFile(queuedMediaFile.getFile(), t.getMessage());
		notWorkingFiles.add(notWorkingFile);
		if (moveFailedFiles) {
		    scheduleMoveNotWorkingFile(notWorkingFile, 30, TimeUnit.SECONDS);
		}
	    }

	    if (database != null && queue.getSize() == 0) {
		database.close();
		database = null;
	    }
	}
	if (LOGGER.isInfoEnabled()) {
	    LOGGER.info(threadName + " ended");
	}
    }

    private void consumeFile(File photoFile) throws Exception, LocationUpdateException, SQLException, IOException, InterruptedException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("consumeFile " + photoFile);
	}
	consumption = new PhotoInConsumption();
	File diskFile = queuedMediaFile.getFile();

	String relativeFileName = ENV.getRelativePhotoFileName(diskFile);
	MediaFile dbFile = getDatabase().getMediaFile(relativeFileName);
	if (dbFile == null) {
	    consumption.setFileChanged(true);
	} else {
	    boolean fileChanged = dbFile.getSize() != diskFile.length() || dbFile.getFileLastModified() != diskFile.lastModified();
	    consumption.setFileChanged(fileChanged);
	}
	String photoId;
	if (consumption.isFileChanged()) {
	    photoId = FileUtils.createMD5Checksum(diskFile);
	} else {
	    photoId = dbFile.getId();
	}

	Photo photoFromDatabase = getDatabase().getPhotoById(photoId);
	if (photoFromDatabase == null) {
	    Photo newPhoto = new Photo(photoId, getMediaType());
	    consumption = new PhotoInConsumption(newPhoto);
	    consumption.setAdded(true);
	} else {
	    consumption = new PhotoInConsumption(photoFromDatabase);
	    consumption.setAdded(false);
	}

	boolean refreshMetaDataNeeded= queuedMediaFile.getType() == Producer.Type.REFRESH_ALL_METADATA || FORCE_UPDATE_EXIF;
	if (consumption.isAdded() || refreshMetaDataNeeded) {
	    updateFileInPhoto();
	    if (cacheFolderOK()) {
		database.getMetaData(consumption.getPhoto());
		if (consumption.isAdded() || consumption.getMetaData() == null || refreshMetaDataNeeded) {
		    if (EXIFTOOL_CONFIGURED) {
			updateExifMetaData(photoFile);
		    }
		}
		handleFile();
		updateLocation();
		if (consumption.isAdded()) {
		    getDatabase().createPhoto(consumption.getPhoto());
		} else {
		    if (consumption.isPhotoChanged()) {
			getDatabase().updatePhoto(consumption.getPhoto());
		    }
		}
	    } else {
		LOGGER.warn("Cache folder does not exist for " + consumption.getId());
	    }
	}
	if (consumption.isFileChanged()) {
	    addFileToDatabase();
	}
	if (consumption.isPhotoChanged() || consumption.isAdded()) {
	    boolean inAlbums = User.inUserAlbum(consumption.getPhoto());
	    if (inAlbums) {
		if (LOGGER.isInfoEnabled()) {
		    LOGGER.info("Channged: " + consumption.toString());
		}
		TimeLineTurboCache.getInstance().setCacheMustBeRefreshed();
	    }
	}
    }

    private void updateExifMetaData(File photoFile) throws IOException, InterruptedException {
	ExifToolGpsGrabber gpsGrabber = new ExifToolGpsGrabber(photoFile);
	Coordinates coordinates = gpsGrabber.getCoordinates();
	consumption.setCoordinates(coordinates);
	ExifToolGrabber exifGrabber = new ExifToolGrabber(photoFile);
	MetaData meta = exifGrabber.getMetaData();
	consumption.setMetaData(meta);

	String make = meta.getValue("Make");
	String model = meta.getValue("Camera Model Name");
	String source = null;
	if (make != null && model != null) {
	    source = make + " " + model;
	} else {
	    if (model != null) {
		source = model;
	    }
	}
	consumption.setSource(source);
	String orientationString = meta.getValue("Orientation");
	int orientation = 0;
	if (orientationString != null) {
	    orientation = Integer.parseInt(orientationString);
	}
	consumption.setOrientation(orientation);

	int duration = 0;
	String durationString = meta.getValue("Duration");
	try {
	    if (durationString != null) {
		int sPos = durationString.indexOf('s');
		if (sPos > 0) {
		    // 29.03 s
		    String secondsString = durationString.substring(0, sPos);
		    Double secondsDouble = Double.parseDouble(secondsString);
		    long secondsLong = Math.round(secondsDouble);
		    duration = (int) secondsLong;
		} else {
		    // 0:01:24
		    int doublePointPos = durationString.indexOf(":");
		    if (doublePointPos > 0) {
			List<String> parts = StringUtils.split(durationString, ':');
			int hourPart = Integer.parseInt(parts.get(0));
			int minutePart = Integer.parseInt(parts.get(1));
			int secondsPart = Integer.parseInt(parts.get(2));
			duration = (hourPart * 3600) + (minutePart * 60) + secondsPart;
		    } else {
			LOGGER.warn("Cannot parse duration: " + durationString + " in " + photoFile);
		    }
		}
	    }
	} catch (Exception e) {
	    LOGGER.warn("Parse error for duration: " + durationString + " in " + photoFile, e);
	}
	consumption.setDuration(duration);

	ZonedDateTime dateTaken = null;
	try {
	    String gpsDateTime = meta.getValue("GPS Date/Time");
	    if (gpsDateTime != null) {
		dateTaken = ExifToolDateUtils.parseDateTime(gpsDateTime);
	    } else {
		String dateTimeOriginal = meta.getValue("Date/Time Original");
		if (dateTimeOriginal != null) {
		    dateTaken = ExifToolDateUtils.parseDateTime(dateTimeOriginal);
		} else {
		    String modifyDate = meta.getValue("Modify Date");
		    if (modifyDate != null) {
			dateTaken = ExifToolDateUtils.parseDateTime(modifyDate);
		    }
		}
	    }
	} catch (Exception e) {
	    LOGGER.warn("Error parsing datatime from file " + photoFile);
	}
	long lastModified = photoFile.lastModified();
	ZonedDateTime fallBackdateLastModified = Instant.ofEpochMilli(lastModified).atZone(ZoneId.systemDefault());
	if (dateTaken == null) {
	    dateTaken = fallBackdateLastModified;
	}

	// TODO may be also store time zone in PostgresSQL in future
	ZonedDateTime dateTakenLocalZoned = dateTaken.withZoneSameInstant(ZoneId.systemDefault());
	LocalDateTime dateTakenDateTime = LocalDateTime.from(dateTakenLocalZoned);
	if (impossibleDate(dateTakenDateTime)) {
	    dateTakenDateTime = LocalDateTime.from(fallBackdateLastModified);
	}
	consumption.setDateTaken(dateTakenDateTime);
    }

    private boolean impossibleDate(LocalDateTime dateTaken) {
	LocalDateTime latestPossible = LocalDateTime.now().plusMonths(1);
	return dateTaken == null || dateTaken.isBefore(EARLIEST_POSSIBLE) || dateTaken.isAfter(latestPossible);
    }

    private boolean cacheFolderOK() {
	File cache = thumbUtils.getCacheFolder(consumption.getPhoto());
	if (!cache.exists()) {
	    cache.mkdirs();
	}
	return cache.isDirectory();
    }

    private void addFileToDatabase() throws Exception {
	File disk = queuedMediaFile.getFile();
	File folder = disk.getParentFile();
	String relativeFileName = ENV.getRelativePhotoFileName(disk);
	String relativeFolderName = ENV.getRelativePhotoFileName(folder);
	MediaFile newMediaFile = new MediaFile();
	newMediaFile.setRelativeFileName(relativeFileName);
	newMediaFile.setRelativeFolderName(relativeFolderName);
	newMediaFile.setId(consumption.getId());
	newMediaFile.setFileLastModified(disk.lastModified());
	newMediaFile.setSize(disk.length());
	newMediaFile.setFileName(disk.getName());
	newMediaFile.setRole(queuedMediaFile.getRole());
	boolean inAlbums = relativeFolderName.startsWith(ENV.getRelativeRootAlbums());
	newMediaFile.setInAlbums(inAlbums);
	getDatabase().setMediaFile(newMediaFile);
    }

    private void updateFileInPhoto() {
	File file = queuedMediaFile.getFile();
	String relativeFolderName = ENV.getRelativePhotoFileName(file.getParentFile());
	String relativeFileName = ENV.getRelativePhotoFileName(file);
	consumption.setId(consumption.getId());
	consumption.setRelativeFileName(relativeFileName);
	consumption.setFileName(file.getName());
	consumption.setRelativeFolderName(relativeFolderName);
    }

    private void updateLocation() throws LocationUpdateException, IOException, InterruptedException {
	if (LOCATION_SERVICE_CONFIGURED && locationServiceAvailable.get() == true) {
	    boolean missingAddress = consumption.getCoordinates() != null && consumption.getAddress() == null;
	    if (FORCE_LOCATION_UPDATE || missingAddress) {
		setStatus("Getting address from geoservices");
		new PhotoAddressUpdater().updateAddress(consumption);
	    }
	}
    }

    private void scheduleMoveNotWorkingFile(NotWorkingFile file, int time, TimeUnit timeUnit) {
	BackgroundServices.getInstance().getThreadService().scheduleOnce(new Runnable() {
	    @Override
	    public void run() {
		notWorkingFiles.moveAndRemoveFromList(file.file());
	    }
	}, time, timeUnit);
    }

    private void scheduleEnablingLocationService(int time, TimeUnit timeUnit) {
	BackgroundServices.getInstance().getThreadService().scheduleOnce(new Runnable() {
	    @Override
	    public void run() {
		locationServiceAvailable.set(true);
		LOGGER.debug("Will retry using location service again");
	    }
	}, time, timeUnit);
    }

}
