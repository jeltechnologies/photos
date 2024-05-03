package com.jeltechnologies.photos.tags;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.NamedValueCollection;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.pictures.Album;
import com.jeltechnologies.photos.pictures.Photo;

public class MenuAlbumTag extends MenuTag {
    private static final long serialVersionUID = -8491775971472237708L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MenuAlbumTag.class);

    protected Database db;

    protected Album album;

    private boolean albumCanBeRenamed() {
	boolean result = false;
	if (album != null && user.isAdmin()) {
	    if (!album.isAlbumsRoot()) {
		result = true;
	    }
	}
	return result;
    }

    private boolean albumContainsPhotos() {
	boolean hasPhotos;
	if (album == null) {
	    hasPhotos = false;
	} else {
	    try {
		Query query = new Query(user);
		query.setRelativeFolderName(album.getRelativeFolderName());
		query.setIncludeSubFolders(false);
		query.setOrderBy(OrderBy.DATE_TAKEN_NEWEST);
		List<Photo> photosInAlbum = db.query(query);
		hasPhotos = photosInAlbum.size() > 0;
	    } catch (SQLException e) {
		LOGGER.warn("Cannot get photos for album " + album.getRelativeFolderName(), e);
		hasPhotos = false;
	    }
	}
	return hasPhotos;
    }

    @Override
    public NamedValueCollection<String> getActions() throws Exception {
	NamedValueCollection<String> actions = new NamedValueCollection<String>();
	String relativeFolderName = getParameter("album");
	if (relativeFolderName == null || relativeFolderName.equals("")) {
	    relativeFolderName = Environment.INSTANCE.getRelativeRootAlbums();
	}
	try {
	    db = new Database();
	    album = db.getAlbum(relativeFolderName);
	    boolean containsPhotos = albumContainsPhotos();

	    if (user.isAdmin()) {
		actions.add("Add new", "addNewClicked()");
	    }
	    if (containsPhotos && user.isAdmin()) {
		actions.add("Remove pictures", "removePicturesClicked()");
	    }
	    if (albumCanBeRenamed()) {
		actions.add("Rename album", "renameAlbumClicked()");
	    }
	    if (containsPhotos) {
		actions.add("Download album (original)", "downloadAlbumClicked('original')");
	    }
	    if (containsPhotos) {
		actions.add("Download album (converted)", "downloadAlbumClicked('converted')");
	    }
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
	return actions;
    }

}