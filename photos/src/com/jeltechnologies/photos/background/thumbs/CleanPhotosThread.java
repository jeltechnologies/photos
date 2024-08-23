package com.jeltechnologies.photos.background.thumbs;

import java.io.File;
import java.io.FileFilter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.datatypes.MovieQuality;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.pictures.MediaFile;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.pictures.ThumbnailUtils;
import com.jeltechnologies.photos.pictures.ThumbnailUtilsFactory;
import com.jeltechnologies.photos.utils.FileUtils;
import com.jeltechnologies.photos.utils.StringUtils;

public class CleanPhotosThread implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(CleanPhotosThread.class);
    private static final String THREAD_NAME = CleanPhotosThread.class.getSimpleName();
    private static final Environment ENV = Environment.INSTANCE;
    private Database db;
    private final boolean moveUneccesaryFiles;

    public CleanPhotosThread(boolean moveUneccesaryFiles) {
	this.moveUneccesaryFiles = moveUneccesaryFiles;
    }

    @Override
    public void run() {
	LOGGER.info(THREAD_NAME + " started");
	
	List<Photo> allPhotosWithFiles = null;
	try {
	    if (moveUneccesaryFiles) {
		removeNotWorkingOriginals();
	    }
	    try {
		db = new Database();
		cleanMediaFilesNotFoundOnDisk();
		db.commit();
		allPhotosWithFiles = db.getAllPhotosWithFile();
	    } finally {
		if (db != null) {
		    db.close();
		}
	    }
	    if (moveUneccesaryFiles) {
		if (allPhotosWithFiles != null) {
		    handleUnnecessayCache(allPhotosWithFiles);
		}
	    }
	} catch (Exception e) {
	    LOGGER.error(THREAD_NAME + " error: " + e.getMessage(), e);
	}
	LOGGER.info(THREAD_NAME + " ended");
    }
    
    private void removeNotWorkingOriginals() throws Exception {
	NotWorkingFiles notWorkingFiles = new NotWorkingFiles();
	notWorkingFiles.moveAllNotWorkingFiles();
    }
    
    private void handleUnnecessayCache(List<Photo> allPhotosWithFiles) throws Exception {
	List<File> unnecessaryFilesInCache = findUnnecessaryFilesInCache(allPhotosWithFiles);
	if (!unnecessaryFilesInCache.isEmpty()) {
	    File removedFolder = Environment.INSTANCE.getConfig().getRemovedCacheFolder();
	    String cachedFolder = Environment.INSTANCE.getConfig().getCacheFolder().getAbsolutePath();
	    for (File file : unnecessaryFilesInCache) {
		if (moveUneccesaryFiles) {
		    String relativeFileName = StringUtils.stripBefore(file.getAbsolutePath(), cachedFolder);
		    File destination = new File(removedFolder.getAbsolutePath() + "/" + relativeFileName);
		    LOGGER.info("Moving " + file + " to " + destination);
		    File destinationFolder = destination.getParentFile();
		    boolean ok;
		    if (destinationFolder.isDirectory()) {
			ok = true;
		    } else { 
			ok = destinationFolder.mkdirs();
			if (!ok) {
			    LOGGER.warn("Cannot create destination folder: " + destinationFolder);
			}
		    }
		    if (ok) {
			FileUtils.moveFile(file, destination, true, true);
		    }
		} else {
		    LOGGER.info("Cache file not used: " + file);
		}
	    }
	}
    }

    private void cleanMediaFilesNotFoundOnDisk() throws SQLException {
	List<MediaFile> allMedia = db.getAllMediaFiles();
	List<MediaFile> mediaNotOnDisk = new ArrayList<>();
	for (MediaFile media : allMedia) {
	    File file = ENV.getFile(media.getRelativeFileName());
	    if (!file.isFile()) {
		mediaNotOnDisk.add(media);
	    }
	}
	if (mediaNotOnDisk.isEmpty()) {
	    LOGGER.info("All files in database were found on disk");
	} else {
	    LOGGER.info("Cleaning " + mediaNotOnDisk.size() + " photo(s)");
	    for (MediaFile media : mediaNotOnDisk) {
		db.deleteMediaFile(media);
	    }
	    //BackgroundServices.getInstance().refreshCacheAfter(1, TimeUnit.SECONDS);
	}
    }

    private List<File> findUnnecessaryFilesInCache(List<Photo> allPhotosWithFiles) throws Exception {
	List<Dimension> photoDimensions = Environment.INSTANCE.getAllThumbnailDimensions();
	Dimension videoFullScreenPosterDimension = Environment.INSTANCE.getDimensionFullscreen();
	Dimension videoThumbPosterDimension = Environment.INSTANCE.getDimensionThumbs();
	ThumbnailUtils thumbUtils = ThumbnailUtilsFactory.getUtils();
	List<File> neededFiles = new ArrayList<File>();
	Set<String> idsProcessed = new HashSet<String>();
	for (Photo photo : allPhotosWithFiles) {
	    String photoId = photo.getId();
	    if (!idsProcessed.contains(photoId)) {
		idsProcessed.add(photoId);
		switch (photo.getType()) {
		    case PHOTO: {
			for (Dimension dimension : photoDimensions) {
			    File thumbFile = thumbUtils.getThumbFile(dimension, photo);
			    neededFiles.add(thumbFile);
			}
			break;
		    }
		    case VIDEO: {
			File highQuality = thumbUtils.getConvertedMovie(new MovieQuality(MovieQuality.Type.HIGH), photo);
			File lowQuality = thumbUtils.getConvertedMovie(new MovieQuality(MovieQuality.Type.LOW), photo);
			File posterFullscreen = thumbUtils.getThumbFile(videoFullScreenPosterDimension, photo);
			File posterThumb = thumbUtils.getThumbFile(videoThumbPosterDimension, photo);
			neededFiles.add(highQuality);
			neededFiles.add(lowQuality);
			neededFiles.add(posterFullscreen);
			neededFiles.add(posterThumb);
			break;
		    }
		    default: {
			break;
		    }
		}
	    }
	}

	Map<String, File> cacheFiles = getAllFilesInCache();
	int cacheSize = cacheFiles.size();

	int missingCache = 0;
	for (File needed : neededFiles) {
	    File cachedFile = cacheFiles.remove(needed.getAbsolutePath());
	    if (cachedFile == null) {
		LOGGER.info("Cannot find the needed file in cache: " + needed);
		missingCache++;
	    }
	}

	if (LOGGER.isInfoEnabled()) {
	    LOGGER.info("Cache info                     : " + Environment.INSTANCE.getConfig().getCacheFolder());
	    LOGGER.info("Cache disk found               : " + StringUtils.formatNumber(cacheSize));
	    LOGGER.info("Cache disk expected            : " + StringUtils.formatNumber(neededFiles.size()));
	    LOGGER.info("Cache files needing cleaning   : " + StringUtils.formatNumber(cacheFiles.size()));
	    LOGGER.info("Missing cache files            : " + StringUtils.formatNumber(missingCache));
	}

	List<File> unnecessaryFilesInCache = new ArrayList<File>();
	for (String fileName : cacheFiles.keySet()) {
	    unnecessaryFilesInCache.add(cacheFiles.get(fileName));
	}
	return unnecessaryFilesInCache;
    }

    private Map<String, File> getAllFilesInCache() throws Exception {
	List<File> allFilesAndFolder = new ArrayList<File>();
	FileFilter filter = new FileFilter() {
	    @Override
	    public boolean accept(File pathname) {
		return true;
	    }
	};
	File cacheFolder = Environment.INSTANCE.getConfig().getCacheFolder();
	String cacheRecycledFolder = Environment.INSTANCE.getConfig().getRemovedCacheFolder().getAbsolutePath();
	FileUtils.getFilesIterative(cacheFolder, allFilesAndFolder, filter);
	Map<String, File> cacheFiles = new HashMap<String, File>();
	for (File file : allFilesAndFolder) {
	    if (file.isFile()) {
		if (!file.getAbsolutePath().startsWith(cacheRecycledFolder)) {
		    cacheFiles.put(file.getAbsolutePath(), file);
		}
	    }
	}
	return cacheFiles;
    }

}
