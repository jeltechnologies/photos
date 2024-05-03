package com.jeltechnologies.photos.picures.frame;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.db.TimePeriod;
import com.jeltechnologies.photos.db.Query.InAlbum;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.timeline.TimeLineTurboCache;

public class MoreLikeRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoreLikeRequestHandler.class);
    private final MoreLikeRequest request;
    private final Database db;
    private Photo photo;
    private final User user;

    public MoreLikeRequestHandler(Database db, User user, MoreLikeRequest request) {
	this.request = request;
	this.db = db;
	this.user = user;
    }

    public void getPhotos() throws SQLException {
	photo = db.getPhotoById(request.getId());
	List<Photo> photos;
	if (photo == null) {
	    photos = new ArrayList<Photo>();
	} else {
	    List<Photo> filteredPhotos;
	    switch (request.getMoreLike()) {
		case DATE:
		    filteredPhotos = filterOnDate();
		    break;
		case DATE_AND_PLACE:
		    filteredPhotos = filterOnDate();
//		    if (LOGGER.isTraceEnabled()) {
//			trace(filteredPhotos);
//		    }
		    filteredPhotos = filterOnPlace(filteredPhotos);
//		    if (LOGGER.isTraceEnabled()) {
//			trace(filteredPhotos);
//		    }
		    break;
		case PLACE:
		    List<Photo> allPhotos = TimeLineTurboCache.getInstance().getCopy();
		    filteredPhotos = photos = filterOnPlace(allPhotos);
		    break;
		default:
		    throw new IllegalArgumentException("Unknown program " + request.getMoreLike());
	    }
	    Collections.sort(filteredPhotos, (a, b) -> {
		return a.getDateTaken().compareTo(b.getDateTaken());
	    });
	    
	    int requestPhotoIndex = getPhotoInRequest(filteredPhotos);
	    if (requestPhotoIndex == -1) {
		LOGGER.warn("Expected requestPhotoIndex found");
		requestPhotoIndex = 0;
	    }
	    
	    int amount = request.getRequestedAmount();
	    int start = requestPhotoIndex - (amount / 2);
	    if (start < 0) {
		start = 0;
	    }
	    int end = start + amount;
	    int lastIndex = filteredPhotos.size() - 1;
	    if (end > lastIndex) {
		end = lastIndex;
	    }
	    
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Returning photo from filterdPhotos " + start + " to " + end + " with requestedPhotoIndex " + requestPhotoIndex); 
	    }
	    
	    photos = new ArrayList<Photo>(request.getRequestedAmount());
	    for (int i = start; i < end && i < filteredPhotos.size(); i++) {
		Photo p = filteredPhotos.get(i);
		photos.add(p);
	    }
	}
	
	trace(photos);
	
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Returning " + photos.size() + " photos"); 
	}
	request.setPhotos(photos);
    }

    private int getPhotoInRequest(List<Photo> photos) {
	int found = -1;
	for (int i = 0; i < photos.size() && found == -1; i++) {
	    Photo current = photos.get(i);
	    if (current.getId().equals(request.getId())) {
		found = i;
		LOGGER.trace("found: " + found);
	    }
	}
	return found;
    }

    private void trace(List<Photo> photos) {
	LOGGER.trace("List of " + photos.size() + " photos");
	for (Photo p : photos) {
	    LOGGER.trace("  " + p.getId() + " => " + p.getDateTaken());
	}
    }

    private List<Photo> filterOnDate() throws SQLException {
	List<Photo> photos;
	LocalDate photoDate = photo.getDateTaken().toLocalDate();
	if (photoDate != null) {
	    LocalDate from = photoDate.minusDays(request.getDaysBefore());
	    LocalDate to = photoDate.plusDays(request.getDaysAfter());
	    Query query = new Query(user);
	    query.setOrderBy(OrderBy.DATE_TAKEN_OLDEST);
	    query.setTimePeriod(new TimePeriod(from, to));
	    query.setInAlbums(InAlbum.IN_ALBUM_NO_DUPLICATES);
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Query: " + query);
	    }
	    photos = db.query(query);
	} else {
	    photos = new ArrayList<Photo>();
	}
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("filterOnDate() => " + photos.size() + " photos");
	}
	return photos;
    }

    private List<Photo> filterOnPlace(List<Photo> photos) {
	Coordinates requestedPhotoCoordinates = photo.getCoordinates();
	int requestDistance = request.getDistanceKilometers();
	List<Photo> filtered = new ArrayList<Photo>();
	if (requestedPhotoCoordinates != null) {
	    for (Photo photoIn : photos) {
		Coordinates coordinates = photoIn.getCoordinates();
		if (coordinates != null) {
		    double distance = requestedPhotoCoordinates.getDistanceFrom(coordinates);
		    boolean mustAdd = distance <= requestDistance;
		    if (mustAdd) {
			filtered.add(photoIn);
		    }
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Distance: " + distance + ". Added: " + mustAdd);
		    }
		}
	    }
	}
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("filterOnPlace(" + photos.size() + " photos)  => " + filtered.size() + " photos");
	}
	return filtered;
    }

}
