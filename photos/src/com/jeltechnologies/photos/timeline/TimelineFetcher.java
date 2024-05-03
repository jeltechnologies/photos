package com.jeltechnologies.photos.timeline;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.db.QuerySupport;
import com.jeltechnologies.photos.db.TimePeriod;
import com.jeltechnologies.photos.db.Query.InAlbum;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;

public class TimelineFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimelineFetcher.class);

    private final Random random = new Random();

    private final QuerySupport database;

    private final TimelineView view;

    public TimelineFetcher(QuerySupport database, TimelineView view) {
	this.database = database;
	this.view = view;
    }

    public Timeline fetch()
	    throws SQLException {
	Grouping grouping = view.getGrouping();
	MediaType mediaType = view.getMediaType();
	boolean randomize = view.isRandomCover();
	User user = view.getUser();

	Timeline timeline = new Timeline(view);

	LocalDate today = LocalDate.now();
	LocalDate oldestPictureTaken = database.getEarliestDayTaken(user);
	if (oldestPictureTaken == null) {
	    oldestPictureTaken = LocalDate.now();
	}
	LocalDate currentStart = today;
	TimeLineTemporalAdjuster adjusterOldest = new TimeLineTemporalAdjuster(grouping, Sorting.OLDESTFIRST);
	currentStart = adjusterOldest.getFirstDayOfGroup(currentStart);
	LocalDate currentEnd = getEnd(currentStart);

	boolean hasMorePeriods = true;
	while (hasMorePeriods) {
	    TimePeriod period = new TimePeriod();
	    period.setFrom(currentStart);
	    period.setTo(currentEnd);
	    Query query = new Query(user);
	    query.setTimePeriod(period);
	    query.setInAlbums(InAlbum.IN_ALBUM_NO_DUPLICATES);
	    query.setMediaType(mediaType);
	    query.setOnlyReturnChecksums(true);
	    query.setOrderBy(OrderBy.DATE_TAKEN_NEWEST);
	    List<Photo> photosWithinPeriod = database.query(query);
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("Period: " + currentStart + " to " + currentEnd + " photos: " + photosWithinPeriod.size());
	    }
	    if (!photosWithinPeriod.isEmpty()) {
		TimelinePhoto picture = generateThumbForPeriod(period, photosWithinPeriod, randomize);
		timeline.add(picture);
	    }
	    currentStart = getPreviousPeriod(currentStart);
	    currentEnd = getEnd(currentStart);
	    hasMorePeriods = currentEnd.isAfter(oldestPictureTaken);
	}
	return timeline;
    }

    private LocalDate getEnd(LocalDate currentStart) {
	LocalDate currentEnd;
	switch (view.getGrouping()) {
	    case MONTH:
		currentEnd = currentStart.plusMonths(1);
		break;
	    case WEEK:
		currentEnd = currentStart.plusWeeks(1);
		break;
	    case YEAR:
		currentEnd = currentStart.plusYears(1);
		break;
	    default:
		throw new IllegalStateException("Unsupported grouping: " + view.getGrouping());
	}
	currentEnd = currentEnd.minusDays(1);
	return currentEnd;
    }

    private LocalDate getPreviousPeriod(LocalDate currentStart) {
	LocalDate previousPeriod;
	switch (view.getGrouping()) {
	    case MONTH:
		previousPeriod = currentStart.minusMonths(1);
		break;
	    case WEEK:
		previousPeriod = currentStart.minusWeeks(1);
		break;
	    case YEAR:
		previousPeriod = currentStart.minusYears(1);
		break;
	    default:
		throw new IllegalStateException("Unsupported grouping: " + view.getGrouping());
	}
	return previousPeriod;
    }

    private TimelinePhoto generateThumbForPeriod(TimePeriod period, List<Photo> photos, boolean randomize) {
	int coverIndex;
	if (randomize) {
	    coverIndex = random.nextInt(photos.size());
	} else {
	    coverIndex = 0;
	}
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("Randomize: " + randomize + ", photos size: " + photos.size() + ", coverIndex: " + coverIndex);
	}
	String id = photos.get(coverIndex).getId();
	Database db = null;
	try {
	    db = new Database();
	    Photo cover = db.getPhotoById(id);
	    return new TimelinePhoto(cover, period.getFrom(), period.getTo());
	} catch (SQLException e) {
	    throw new IllegalStateException("Cannot find cover photo with id: " + id);
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

}
