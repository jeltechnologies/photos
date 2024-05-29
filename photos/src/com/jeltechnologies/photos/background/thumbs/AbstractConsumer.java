package com.jeltechnologies.photos.background.thumbs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
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

public abstract class AbstractConsumer implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractConsumer.class);

    private boolean moveFailedFails = false;

    protected static final Environment ENV = Environment.INSTANCE;

    private final static File FAILED_FOLDER = ENV.getConfig().getFailedFolder();

    private final static LocalDateTime EARLIEST_POSSIBLE = LocalDateTime.of(1900, 1, 1, 0, 0);

    private final static boolean FORCE_LOCATION_UPDATE = false;

    private final static boolean LOCATION_SERVICE_CONFIGURED = ENV.getConfig().isGeoServicesConfigured();

    private final MediaQueue queue;

    private final String threadName;

    protected String id;

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
		    LOGGER.info(errorMessage);
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
	boolean fileWasChanged = getFileIdAndCheckChanged();
	photo = getDatabase().getPhotoById(id);
	boolean newPhoto = (photo == null);
	if (newPhoto || queuedMediaFile.getType() == Producer.Type.COMPLETE_REFRESH) {
	    Photo beforeChange;
	    if (newPhoto) {
		photo = new Photo(id, getMediaType());
		beforeChange = null;
	    } else {
		beforeChange = getDatabase().getPhotoById(id);
	    }
	    updateFileInPhoto();
	    if (cacheFolderOK()) {
		handleFile(newPhoto);
		fixDateTaken();
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
	if (fileWasChanged) {
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

    private boolean cacheFolderOK() {
	File cache = thumbUtils.getCacheFolder(photo);
	if (!cache.exists()) {
	    cache.mkdirs();
	}
	return cache.isDirectory();
    }

    private void fixDateTaken() throws IOException {
	if (photo.getDateTaken() == null) {
	    File file = queuedMediaFile.getFile();
	    LocalDateTime modified = FileUtils.getLastModifiedTime(file);
	    photo.setDateTaken(modified);
	}
    }

    protected boolean impossibleDate(LocalDateTime dateTaken) {
	LocalDateTime latestPossible = LocalDateTime.now().plusMonths(1);
	return dateTaken == null || dateTaken.isBefore(EARLIEST_POSSIBLE) || dateTaken.isAfter(latestPossible);
    }

    private boolean getFileIdAndCheckChanged() throws Exception {
	boolean same = false;
	File disk = queuedMediaFile.getFile();
	String relativeFileName = ENV.getRelativePhotoFileName(disk);
	MediaFile db = getDatabase().getMediaFile(relativeFileName);
	same = db != null && db.getSize() == disk.length() && db.getFileLastModified() == disk.lastModified();
	if (!same) {
	    id = FileUtils.createMD5Checksum(disk);
	} else {
	    id = db.getId();
	}
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("Checksum: " + id);
	}
	return !same;
    }

    private void addFileToDatabase() throws Exception {
	File disk = queuedMediaFile.getFile();
	File folder = disk.getParentFile();
	String relativeFileName = ENV.getRelativePhotoFileName(disk);
	String relativeFolderName = ENV.getRelativePhotoFileName(folder);
	MediaFile newMediaFile = new MediaFile();
	newMediaFile.setRelativeFileName(relativeFileName);
	newMediaFile.setRelativeFolderName(relativeFolderName);
	newMediaFile.setId(id);
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
	photo.setId(id);
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
