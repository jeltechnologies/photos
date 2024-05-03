package com.jeltechnologies.photos.gallery;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.db.TimePeriod;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;

public class GalleryLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(GalleryLogic.class);

    private String albumUrl;
    private TimePeriod period;
    private OrderBy orderBy;
    private MediaType mediaType;
    private int maximumAmount;
    private final User user;
    private final Database database;
    private String photo;

    public GalleryLogic(User user, Database database) {
	this.user = user;
	this.database = database;
    }

    public void setAlbumUrl(String albumUrl) {
	this.albumUrl = albumUrl;
    }

    public void setPeriod(TimePeriod period) {
	this.period = period;
    }

    public void setOrderBy(OrderBy orderBy) {
	this.orderBy = orderBy;
    }

    public void setMediaType(MediaType mediaType) {
	this.mediaType = mediaType;
    }

    public void setPhoto(String photo) {
	this.photo = photo;
    }

    public int getMaximumAmount() {
	return maximumAmount;
    }

    public void setMaximumAmount(int maximumAmount) {
	this.maximumAmount = maximumAmount;
    }

    public List<Photo> getPhotos() throws SQLException {
	List<Photo> photos = new ArrayList<>();
	if (albumUrl != null && !albumUrl.isEmpty() && !albumUrl.equals("null")) {
	    Query query = new Query(user);
	    query.setRelativeFolderName(albumUrl);
	    query.setIncludeSubFolders(false);
	    query.setOrderBy(OrderBy.DATE_TAKEN_OLDEST);
	    photos = database.query(query);
	} else {
	    if (period != null) {
		Query query = new Query(user);
		query.setTimePeriod(period);
		query.setOrderBy(orderBy);
		query.setRelativeFolderName(Environment.INSTANCE.getRelativeRootAlbums());
		if (mediaType != null) {
		    query.setMediaType(mediaType);
		}
		if (user.isAdmin()) {
		    query.setIncludeHidden(true);
		} else {
		    query.setIncludeHidden(false);
		}
		photos = database.query(query);
	    } else {
		Photo selectedPhoto = database.getFirstPhotoById(user, photo);
		photos.add(selectedPhoto);
	    }
	}
	photos = trimToMaximumAmount(photos);
	return photos;
    }

    private List<Photo> trimToMaximumAmount(List<Photo> photos) {
	List<Photo> trimmed = new ArrayList<>(maximumAmount);
	int size = photos.size();
	int selected = -1;
	for (int i = 0; i < size && selected == -1; i++) {
	    if (photos.get(i).getId().equals(this.photo)) {
		selected = i;
	    }
	}
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Found selected photo at index " + selected);
	}

	int halfAmount = maximumAmount / 2;
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Half amount " + halfAmount);
	}

	int startIndex;
	if (selected == -1) {
	    startIndex = 0;

	} else {
	    startIndex = selected - halfAmount;
	    if (startIndex < 0) {
		startIndex = 0;
	    }
	}

	int endIndex = startIndex + maximumAmount;

	if (endIndex > size) {
	    endIndex = size;
	}

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("startIndex: " + startIndex);
	    LOGGER.debug("endIndex: " + endIndex);
	}

	for (int i = startIndex; i < endIndex; i++) {
	    trimmed.add(photos.get(i));
	}

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Trimmed size: " + trimmed.size());
	}

	return trimmed;
    }

    public static List<Photo> removeDuplicated(List<Photo> photos) {
	Set<String> ids = new HashSet<String>();
	List<Photo> results = new ArrayList<>(photos.size());
	for (Photo p : photos) {
	    String id = p.getId();
	    if (!ids.contains(id)) {
		ids.add(id);
		results.add(p);
	    }
	}
	return results;
    }
}
