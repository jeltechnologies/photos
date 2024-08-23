package com.jeltechnologies.photos.timeline;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.db.QuerySupport;
import com.jeltechnologies.photos.db.TimePeriod;
import com.jeltechnologies.photos.db.Query.InAlbum;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.utils.JMXUtils;

public class TimeLineTurboCache implements QuerySupport, TimeLineTurboCacheMBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLineTurboCache.class);

    private static final TimeLineTurboCache INSTANCE = new TimeLineTurboCache();

    private AtomicBoolean cacheMustBeFreshed = new AtomicBoolean(true);

    private List<Photo> filesNewFirst;

    private List<Photo> photosNewFirst;

    private List<Photo> videosNewFirst;

    private Map<Query, ListIndex> periods;

    private LocalDate earliestDayTaken;

    private LocalDateTime lastTimeRefreshed = LocalDateTime.now();

    private TimeLineTurboCache() {
	LOGGER.debug("Instantiated");
	JMXUtils.getInstance().registerMBean("Cache", "Queries", this);
    }

    public static TimeLineTurboCache getInstance() {
	return INSTANCE;
    }

    public void setCacheMustBeRefreshed() {
	LOGGER.debug("setCacheMustBeRefreshed true");
	cacheMustBeFreshed.set(true);
	lastTimeRefreshed = LocalDateTime.now();
    }

    public int getQueruesInCache() {
	if (periods != null) {
	    return periods.size();
	} else {
	    return -1;
	}
    }

    private synchronized void ensureFreshCache() {
	boolean mustBeRefreshed = cacheMustBeFreshed.get();
	if (mustBeRefreshed) {
	    synchronized(this) {
		empty();
		cacheMustBeFreshed.set(false);
	    }
	}
    }

    private void empty() {
	if (LOGGER.isInfoEnabled()) {
	    LOGGER.info("Emptying cache");
	}
	this.periods = new HashMap<Query, ListIndex>();
	filesNewFirst = null;
	earliestDayTaken = null;
	filesNewFirst = getAllFiles();

	if (filesNewFirst != null) {
	    videosNewFirst = new ArrayList<Photo>();
	    photosNewFirst = new ArrayList<Photo>();
	    if (photosNewFirst.size() > 0) {
		Photo last = filesNewFirst.get(filesNewFirst.size() - 1);
		earliestDayTaken = last.getDateTaken().toLocalDate();
	    }
	    for (Photo p : filesNewFirst) {
		switch (p.getType()) {
		    case PHOTO:
			photosNewFirst.add(p);
			break;
		    case VIDEO:
			videosNewFirst.add(p);
			break;
		    default:
			break;
		    case ALL:
			break;
		}
	    }
	}
	this.lastTimeRefreshed = LocalDateTime.now();
	if (LOGGER.isInfoEnabled()) {
	    LOGGER.info("Caching completed");
	}
    }

    private List<Photo> reverse(List<Photo> list) {
	if (list == null) {
	    return null;
	} else {
	    List<Photo> result = new ArrayList<Photo>(list.size());
	    int last = list.size() - 1;
	    for (int i = last; i >= 0; i--) {
		result.add(list.get(i));
	    }
	    return result;
	}
    }

    private List<Photo> getAllFiles() {
	Database db = null;
	try {
	    db = new Database();
	    User user = RoleModel.getSystemUser();
	    Query query = new Query(user);
	    query.setInAlbums(InAlbum.IN_ALBUM_NO_DUPLICATES);
	    query.setIncludeHidden(false);
	    query.setOrderBy(OrderBy.DATE_TAKEN_NEWEST);
	    return db.query(query);
	} catch (SQLException e) {
	    LOGGER.error("Cannot initialize cache because of " + e.getMessage());
	    return null;
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

    @Override
    public synchronized List<Photo> query(Query query) throws SQLException {
	ensureFreshCache();
	List<Photo> photos;
	ListIndex index = this.periods.get(query);
	if (index == null) {
	    if (filesNewFirst != null) {
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("Getting lists to built query: " + query);
		}
		photos = queryFromLists(query);
	    } else {
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("Getting database to built query: " + query);
		}
		photos = queryFromDatabase(query);
	    }
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Period: " + query.getTimePeriod() + ": " + photos.size() + " photos");
	    }
	    ListIndex newIndex = new ListIndex(query, photos);
	    periods.put(query, newIndex);
	} else {
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("Using cache for query: " + query);
	    }
	    photos = index.getPhotos();
	}
	if (query.getOrderBy() == OrderBy.DATE_TAKEN_OLDEST) {
	    photos = reverse(photos);
	}
	return photos;
    }

    private List<Photo> queryFromDatabase(Query query) throws SQLException {
	Database db = null;
	try {
	    db = new Database();
	    List<Photo> photos = db.query(query);
	    ListIndex newIndex = new ListIndex(query, photos);
	    periods.put(query, newIndex);
	    return photos;
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

    private List<Photo> queryFromLists(Query query) {
	List<Photo> result = new ArrayList<Photo>();
	List<Photo> source;
	MediaType type = query.getMediaType();
	if (type == null) {
	    type = MediaType.ALL;
	}
	switch (type) {
	    case PHOTO:
		source = photosNewFirst;
		break;
	    case VIDEO:
		source = videosNewFirst;
		break;
	    default:
	    case ALL: {
		source = filesNewFirst;
		break;
	    }
	}
	TimePeriod period = query.getTimePeriod();
	LocalDate searchFrom = period.getFrom().minusDays(1);
	LocalDate searchTo = period.getTo().plusDays(1);

	for (Photo p : source) {
	    boolean exclude = false;
	    if (!exclude) {
		if (p.getType() != type) {
		    if (p.getType() == null) {
			exclude = true;
		    }
		}
	    }
	    if (period != null) {
		LocalDate taken = p.getDateTaken().toLocalDate();
		boolean inRange = taken.isAfter(searchFrom) && taken.isBefore(searchTo);
		if (!inRange) {
		    exclude = true;
		}
	    }
	    if (!exclude) {
		result.add(p);
	    }
	}
	return result;
    }

    @Override
    public LocalDate getEarliestDayTaken(User user) throws SQLException {
	if (earliestDayTaken == null) {
	    Database db = null;
	    try {
		db = new Database();
		earliestDayTaken = db.getEarliestDayTaken(user);
	    } finally {
		if (db != null) {
		    db.close();
		}
	    }
	}
	return earliestDayTaken;
    }

    public void close() {
    }

    public LocalDateTime getRefresh() {
	return lastTimeRefreshed;
    }

    public List<Photo> getCopy() {
	ensureFreshCache();
	List<Photo> copy = new ArrayList<Photo>(this.photosNewFirst.size());
	for (Photo photo : this.photosNewFirst) {
	    copy.add(photo);
	}
	return copy;
    }

}
