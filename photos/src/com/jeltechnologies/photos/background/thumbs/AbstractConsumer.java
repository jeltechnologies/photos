package com.jeltechnologies.photos.background.thumbs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

    private boolean moveFailedFails = false;

    protected static final Environment ENV = Environment.INSTANCE;

    private final static File FAILED_FOLDER = ENV.getConfig().getFailedFolder();

    private final static LocalDateTime EARLIEST_POSSIBLE = LocalDateTime.of(1900, 1, 1, 0, 0);

    private final static boolean FORCE_LOCATION_UPDATE = false;

    private final static boolean FORCE_UPDATE_EXIF = false;

    private final static boolean LOCATION_SERVICE_CONFIGURED = ENV.getConfig().isGeoServicesConfigured();

    private final static boolean EXIFTOOL_CONFIGURED = ENV.getConfig().isCanUseExifTool();

    private final MediaQueue queue;

    private final String threadName;

    protected final NotWorkingFiles notWorkingFiles;

    private Database database;

    protected QueuedMediaFile queuedMediaFile;

    protected Photo photo;

    private String status;

    private File failedFileToMove = null;

    private AtomicBoolean locationServiceAvailable = new AtomicBoolean(true);

    protected final ThumbnailUtils thumbUtils = ThumbnailUtilsFactory.getUtils();

    protected abstract void handleFile(boolean generateMetaData) throws Exception;

    protected abstract MediaType getMediaType();

    public AbstractConsumer(MediaQueue queue, String threadName, NotWorkingFiles notWorkingFiles, boolean moveFailedFails) {
	this.threadName = threadName;
	this.queue = queue;
	this.notWorkingFiles = notWorkingFiles;
	this.moveFailedFails = moveFailedFails;
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
		if (moveFailedFails && failedFileToMove != null) {
		    moveFailedFileFromLastRound();
		}
		photo = null;
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
		    LOGGER.info(errorMessage, t);
		} else {
		    LOGGER.warn(errorMessage, t);
		}
		if (database != null) {
		    database.rollback();
		}
		this.failedFileToMove = queuedMediaFile.getFile();
		notWorkingFiles.add(queuedMediaFile.getFile(), t);
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

    private void moveFailedFileFromLastRound() {
	try {
	    File original = this.failedFileToMove;
	    String relativeFileName = ENV.getRelativePhotoFileName(original);
	    String destinationPath = FAILED_FOLDER + relativeFileName;
	    File destination = new File(destinationPath);
	    File destinationFolder = destination.getParentFile();
	    if (!destinationFolder.isDirectory()) {
		boolean ok = destinationFolder.mkdirs();
		if (!ok) {
		    throw new IOException("Cannot create folder: " + destinationPath);
		}
	    }
	    if (LOGGER.isInfoEnabled()) {
		LOGGER.info("  -> Moving " + original.getAbsolutePath() + " to " + destination.getAbsolutePath());
	    }
	    Files.move(original.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
	    this.failedFileToMove = null;
	} catch (IOException e) {
	    LOGGER.warn(e.getMessage() + " for file " + queuedMediaFile.getFile());
	}
    }

    private void consumeFile(File photoFile) throws Exception, LocationUpdateException, SQLException, IOException, InterruptedException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("consumeFile " + photoFile);
	}
	boolean cacheMustBeCleared = false;
	File diskFile = queuedMediaFile.getFile();

	boolean fileChanged;
	String relativeFileName = ENV.getRelativePhotoFileName(diskFile);
	MediaFile dbFile = getDatabase().getMediaFile(relativeFileName);
	if (dbFile == null) {
	    fileChanged = true;
	} else {
	    fileChanged = dbFile.getSize() != diskFile.length() || dbFile.getFileLastModified() != diskFile.lastModified();
	}
	String photoId;
	if (fileChanged) {
	    photoId = FileUtils.createMD5Checksum(diskFile);
	} else {
	    photoId = dbFile.getId();
	}

	photo = getDatabase().getPhotoById(photoId);
	boolean newPhoto = (photo == null);
	if (newPhoto || queuedMediaFile.getType() == Producer.Type.COMPLETE_REFRESH) {
	    Photo beforeChange;
	    if (newPhoto) {
		photo = new Photo(photoId, getMediaType());
		beforeChange = null;
	    } else {
		beforeChange = getDatabase().getPhotoById(photoId);
	    }

	    updateFileInPhoto();
	    if (cacheFolderOK()) {
		database.getMetaData(photo);
		if (newPhoto || photo.getMetaData() == null || FORCE_UPDATE_EXIF) {
		    if (EXIFTOOL_CONFIGURED) {
			updateExifMetaData(photoFile);
		    }
		}
		handleFile(newPhoto);
		updateLocation();
		if (newPhoto) {
		    getDatabase().createPhoto(photo);
		    cacheMustBeCleared = true;
		} else {
		    if (beforeChange == null || !beforeChange.hasSameMetaData(photo)) {
			if (LOGGER.isDebugEnabled()) {
			    LOGGER.debug("Before: " + beforeChange.toString());
			    LOGGER.debug("After : " + photo.toString());
			    LOGGER.debug("Updating changed photo in database " + photo.getRelativeFileName());
			}

			getDatabase().updatePhoto(photo);
			cacheMustBeCleared = true;
		    } else {
			if (LOGGER.isTraceEnabled()) {
			    LOGGER.trace("Photo not changed: " + photo.getRelativeFileName());
			}
		    }
		}
	    } else {
		LOGGER.warn("Cache folder does not exist for " + photo.getId());
	    }
	}
	if (fileChanged) {
	    cacheMustBeCleared = true;
	    addFileToDatabase();
	}
	if (cacheMustBeCleared) {
	    boolean inAlbums = User.inUserAlbum(photo);
	    if (inAlbums) {
		TimeLineTurboCache.getInstance().setCacheMustBeRefreshed();
	    }
	}
    }

    private void updateExifMetaData(File photoFile) throws IOException, InterruptedException {
	ExifToolGpsGrabber gpsGrabber = new ExifToolGpsGrabber(photoFile);
	Coordinates coordinates = gpsGrabber.getCoordinates();
	photo.setCoordinates(coordinates);
	ExifToolGrabber exifGrabber = new ExifToolGrabber(photoFile);
	MetaData meta = exifGrabber.getMetaData();
	photo.setMetaData(meta);

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
	photo.setSource(source);
	String orientationString = meta.getValue("Orientation");
	int orientation = 0;
	if (orientationString != null) {
	    orientation = Integer.parseInt(orientationString);
	}
	photo.setOrientation(orientation);

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
	photo.setDuration(duration);

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
	photo.setDateTaken(dateTakenDateTime);
    }

    private boolean impossibleDate(LocalDateTime dateTaken) {
	LocalDateTime latestPossible = LocalDateTime.now().plusMonths(1);
	return dateTaken == null || dateTaken.isBefore(EARLIEST_POSSIBLE) || dateTaken.isAfter(latestPossible);
    }

    private boolean cacheFolderOK() {
	File cache = thumbUtils.getCacheFolder(photo);
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
	newMediaFile.setId(photo.getId());
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
	photo.setId(photo.getId());
	photo.setRelativeFileName(relativeFileName);
	photo.setFileName(file.getName());
	photo.setRelativeFolderName(relativeFolderName);
    }

    private void updateLocation() throws LocationUpdateException, IOException, InterruptedException {
	if (LOCATION_SERVICE_CONFIGURED && locationServiceAvailable.get() == true) {
	    boolean missingAddress = photo.getCoordinates() != null && photo.getAddress() == null;
	    if (FORCE_LOCATION_UPDATE || missingAddress) {
		setStatus("Getting address from geoservices");
		new PhotoAddressUpdater().updateAddress(photo);
	    }
	}
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
