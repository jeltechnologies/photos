package com.jeltechnologies.photos.pictures;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.servlet.PhotoAction;
import com.jeltechnologies.photos.utils.FileUtils;
import com.jeltechnologies.photos.utils.StringUtils;

public class PhotoRemoveFromAlbumHandler extends PhotoActionHandler {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PhotoRemoveFromAlbumHandler.class);

    private static final File REMOVED_FOLDERS_FOLDER = Environment.INSTANCE.getConfig().getRemovedPhotosFolder();

    public PhotoRemoveFromAlbumHandler(PhotoAction action) {
	super(action);
    }

    @Override
    public void handleDetails() throws Exception {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("Remove from album: " + action.getId());
	}
	User user = action.getUser();
	List<Photo> photos = database.getPhotosById(user, action.getId());
	List<Photo> photosinAlbums = new ArrayList<Photo>();
	Iterator<Photo> iterator = photos.iterator();
	while (iterator.hasNext()) {
	    Photo current = iterator.next();
	    if (User.inUserAlbum(current)) {
		photosinAlbums.add(current);
	    }
	}
	if (photosinAlbums.isEmpty()) {
	    LOGGER.warn("No photos found in albums for photo " + action.getId());
	} else {
	    for (Photo photo : photosinAlbums) {
		moveFilesToDeletedFolder(photo);
		deletePhotoFromDatabase(photo);
		database.commit();
	    }
	}
    }

    private void deletePhotoFromDatabase(Photo photo) throws SQLException {
	database.deletePhoto(action.getUser(), photo);
    }

    private void moveFilesToDeletedFolder(Photo photo) throws IOException {

	String path = REMOVED_FOLDERS_FOLDER + photo.getRelativeFileName();
	File removeFolder = new File(path);
	if (!removeFolder.isDirectory()) {
	    removeFolder.mkdirs();
	}
	File availableFile = null;
	for (int i = 0; i < 100 && availableFile == null; i++) {
	    StringBuilder b = new StringBuilder();
	    b.append(removeFolder.getAbsolutePath()).append("/").append(photo.getFileName());
	    if (i > 0) {
		b.append("_").append(i);
	    }
	    File distinationFile = new File(b.toString());
	    if (!distinationFile.isFile()) {
		availableFile = distinationFile;
	    }
	}
	if (availableFile == null) {
	    throw new IllegalStateException("Cannot move to distinationfolder, file already exists");
	}

	File file = ENV.getFile(photo.getRelativeFileName());
	if (LOGGER.isInfoEnabled()) {
	    LOGGER.info("Moving " + file.getAbsolutePath() + " to " + availableFile.getAbsolutePath());
	}
	FileUtils.moveFile(file.getAbsolutePath(), availableFile.getAbsolutePath(), false);
	removeAppleHeicForJPG(file);
    }

    private void removeAppleHeicForJPG(File jpgFile) throws IOException {
	String extension = StringUtils.findAfterLast(jpgFile.getName(), ".");
	LOGGER.info("removeAppleHeicForJPG [" + jpgFile + "] extension: " + extension);
	if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
	    String nameWithoutExtension = StringUtils.stripAfterLast(jpgFile.getName(), ".");
	    String heicFileName = nameWithoutExtension + ".HEIC";
	    String jpgFolder = jpgFile.getParentFile().getAbsolutePath();
	    File heicFile = new File(jpgFolder + "/" + heicFileName);
	    boolean heicExists = heicFile.isFile();
	    if (heicExists) {
		heicFile.delete();
		if (LOGGER.isInfoEnabled()) {
		    LOGGER.info("Deleted HEIC file " + heicFile.getAbsolutePath());
		}
	    } else {
		LOGGER.info("No Heic file found to delete in " + heicFile.getAbsolutePath());
	    }
	}
    }

}
