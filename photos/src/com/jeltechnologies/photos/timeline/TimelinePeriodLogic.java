package com.jeltechnologies.photos.timeline;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.db.TimePeriod;
import com.jeltechnologies.photos.gallery.GalleryLogic;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;

public class TimelinePeriodLogic {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimelinePeriodLogic.class);
    private OrderBy orderBy;
    private MediaType mediaType;
    private TimePeriod timePeriod;
    private final User user;

    public TimelinePeriodLogic(User user) {
	this.user = user;
    }

    public List<Photo> getPhotos() throws Exception {
	Database db = null;
	try {
	    db = new Database();
	    List<Photo> photos = getPhotos(db);
	    photos = GalleryLogic.removeDuplicated(photos);
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("getPhotos: " + photos.size() + " photos");
	    }
	    return photos;
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

    private List<Photo> getPhotos(Database db) throws SQLException {
	Query query = new Query(user);
	query.setOrderBy(orderBy);
	query.setIncludeSubFolders(true);
	query.setRelativeFolderName(Environment.INSTANCE.getRelativeRootAlbums());
	query.setTimePeriod(timePeriod);
	if (mediaType != null) {
	    query.setMediaType(mediaType);
	}
	if (user.isAdmin()) {
	    query.setIncludeHidden(true);
	} else {
	    query.setIncludeHidden(false);
	}

	return db.query(query);
    }

    public void setOrderBy(OrderBy orderBy) {
        this.orderBy = orderBy;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }
}
