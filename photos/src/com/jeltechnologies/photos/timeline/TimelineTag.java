package com.jeltechnologies.photos.timeline;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.db.TimePeriod;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.tags.AlbumListTag;
import com.jeltechnologies.photos.tags.BaseTag;
import com.jeltechnologies.photos.utils.StringUtils;

import jakarta.servlet.jsp.JspException;

public class TimelineTag extends BaseTag {
    private static final long serialVersionUID = -7833804399361485187L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AlbumListTag.class);

    private static final Environment ENV = Environment.INSTANCE;

    private Random random = new Random();

    public static final String ALBUM_PARAMETER = "album";

    private final static String[] MONTHS = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November",
	    "December" };

    private enum Operation {
	Uncategorized, Albums
    }

    private static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private Grouping grouping;

    private Sorting sorting;

    private Operation operation = Operation.Albums;

    public TimelineTag() {
    }

    public void setOperation(String operationString) {
	if (operationString.equalsIgnoreCase("Albums")) {
	    operation = Operation.Albums;
	} else {
	    if (operationString.equalsIgnoreCase("Uncategorized")) {
		operation = Operation.Uncategorized;
	    } else {
		throw new IllegalArgumentException("Unsupported operation");
	    }
	}
    }

    @Override
    public void addHTML() throws Exception {
	String groupedParameter = getParameter("grouped");
	String sortParamter = getParameter("sort");
	add("<div");
	if (id != null) {
	    add(" id=\"" + id + "\"");
	}
	if (cssClass != null) {
	    add(" class=\"" + cssClass + "\"");
	}
	add(">");

	addLine("");

	addLine("<p class=\"albumlist\">");
	generateFromAlbums(groupedParameter, sortParamter);

	add("</p>");
    }

    private void generateFromAlbums(String groupedParameter, String sortedParameter) throws Exception {
	if (groupedParameter == null) {
	    grouping = Grouping.MONTH;
	} else {
	    if (groupedParameter.equalsIgnoreCase("week")) {
		grouping = Grouping.WEEK;
	    } else {
		if (groupedParameter.equalsIgnoreCase("year")) {
		    grouping = Grouping.YEAR;
		} else {
		    grouping = Grouping.MONTH;
		}
	    }
	}

	if (sortedParameter == null) {
	    sorting = Sorting.NEWESTFIRST;
	} else {
	    if (sortedParameter.equalsIgnoreCase("newestfirst")) {
		sorting = Sorting.NEWESTFIRST;
	    } else {
		if (sortedParameter.equalsIgnoreCase("oldestfirst")) {
		    sorting = Sorting.OLDESTFIRST;
		} else {
		    sorting = Sorting.NEWESTFIRST;
		}
	    }
	}

	generateTimeline();
    }

    private void generateTimeline() throws Exception {
	LocalDate now = LocalDate.now();
	LocalDate endDate = null;
	if (operation == Operation.Uncategorized) {
	    endDate = LocalDate.now().minusYears(1);
	}
	Database db = null;
	try {

	    db = new Database();
	    
	    LocalDate oldestPictureTaken = db.getEarliestDayTaken(user);
	    
	    LocalDate currentStart;
	    LocalDate currentEnd;

	    if (sorting == Sorting.NEWESTFIRST) {

		switch (grouping) {
		    case WEEK:
			currentStart = now.plusDays(1);
			currentEnd = currentStart.plusWeeks(1);
			break;
		    case MONTH:
			currentStart = now.withDayOfMonth(1);
			currentEnd = currentStart.plusMonths(1);
			break;
		    case YEAR:
			currentStart = LocalDate.of(now.getYear(), 1, 1);
			currentEnd = currentStart.plusYears(1);
			break;
		    default:
			throw new IllegalArgumentException(grouping.toString());
		}

	    } else {
		currentStart = oldestPictureTaken;
		switch (grouping) {
		    case WEEK:
			currentEnd = now.plusWeeks(1).withDayOfMonth(1);
		    case MONTH:
			currentEnd = now.plusMonths(1).withDayOfMonth(1);
			break;
		    case YEAR:
			currentEnd = now.plusYears(1).withDayOfMonth(1);
			break;
		    default:
			throw new IllegalArgumentException(grouping.toString());
		}
	    }

	    boolean hasMorePhotos = true;

	    while (hasMorePhotos) {
		User user = RoleModel.getUser(pageContext);
		Query query = new Query(user);

		if (sorting == Sorting.NEWESTFIRST) {
		    query.setOrderBy(OrderBy.DATE_TAKEN_NEWEST);
		} else {
		    query.setOrderBy(OrderBy.DATE_TAKEN_OLDEST);
		}

		TimePeriod period = new TimePeriod();
		period.setFrom(currentStart);
		period.setTo(currentEnd);
		query.setTimePeriod(period);
		query.setOnlyReturnChecksums(true);
		List<Photo> photosWithinPeriod = db.query(query);

		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug(query.toString() + " => " + photosWithinPeriod.size());
		}

		if (!photosWithinPeriod.isEmpty()) {
		    if (operation == Operation.Albums) {
			photosWithinPeriod = filterAlbums(photosWithinPeriod);
		    }
		    if (!photosWithinPeriod.isEmpty()) {
			generateThumbsForPeriod(period, photosWithinPeriod);
		    }
		}

		if (sorting == Sorting.NEWESTFIRST) {
		    switch (grouping) {
			case WEEK:
			    currentStart = currentStart.minusWeeks(1);
			    currentEnd = currentStart.plusWeeks(1);
			    break;
			case MONTH:
			    currentStart = currentStart.minusMonths(1);
			    currentEnd = currentStart.plusMonths(1);
			    break;
			case YEAR:
			    currentStart = currentStart.minusYears(1);
			    currentEnd = currentStart.plusYears(1);
			    break;
			default:
			    throw new IllegalArgumentException(grouping.toString());
		    }
		    hasMorePhotos = currentStart.isAfter(oldestPictureTaken);
		    if (hasMorePhotos && endDate != null) {
			LOGGER.debug(currentStart + " => " + endDate);
			hasMorePhotos = currentStart.isAfter(endDate);
		    }

		} else {

		    switch (grouping) {
			case WEEK:
			    currentStart = currentStart.plusWeeks(1);
			    currentEnd = currentStart.plusWeeks(1);
			    break;
			case MONTH:
			    currentStart = currentStart.plusMonths(1);
			    currentEnd = currentStart.plusMonths(1);
			    break;
			case YEAR:
			    currentStart = currentStart.plusYears(1);
			    currentEnd = currentStart.plusYears(1);
			    break;
			default:
			    throw new IllegalArgumentException(grouping.toString());
		    }

		    hasMorePhotos = currentEnd.isBefore(now);
		}
	    }
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

    private List<Photo> filterAlbums(List<Photo> photosWithinPeriod) {
	List<Photo> result = new ArrayList<Photo>(photosWithinPeriod.size());
	String albumRoot = ENV.getRelativeRootAlbums();
	for (Photo photo : photosWithinPeriod) {
	    if (photo.getRelativeFileName().startsWith(albumRoot)) {
		result.add(photo);
	    }
	}
	return result;
    }

    private void generateThumbsForPeriod(TimePeriod period, List<Photo> photos) throws JspException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("generateThumbsForPeriod" + period);
	}
	LocalDate from = period.getFrom();
	int year = from.getYear();
	int week = from.get(ChronoField.ALIGNED_WEEK_OF_YEAR);

	Photo albumPhoto;
	int monthIndex = period.getFrom().getMonth().getValue();
	String month = MONTHS[monthIndex - 1];
	String title;

	switch (grouping) {
	    case WEEK:
		title = "Week " + week + ", " + year;
		break;
	    case MONTH:
		title = month + " " + year;
		break;
	    case YEAR:
		title = String.valueOf(year);
		break;
	    default:
		throw new IllegalArgumentException(grouping.toString());
	}

	StringBuilder link = new StringBuilder();
	switch (operation) {
	    case Albums: {
		int randomIndex = random.nextInt(photos.size());
		albumPhoto = photos.get(randomIndex);
		link.append("<a href=\"photo.jsp?photo=");
		link.append(StringUtils.encodeURL(albumPhoto.getRelativeFileName()));
		link.append("&from=");
		String dateFrom = DATE_FORMATTER.format(period.getFrom());
		String dateTo = DATE_FORMATTER.format(period.getTo());
		link.append(StringUtils.encodeURL(dateFrom));
		link.append("&to=").append(StringUtils.encodeURL(dateTo));
		link.append("\">");
		break;
	    }
	    case Uncategorized: {
		link.append("<a href=\"add-new-month.jsp?year=" + year + "&month=" + monthIndex);
		link.append("\">");
		albumPhoto = photos.get(0);
		break;
	    }
	    default: {
		throw new IllegalStateException("Unsupported operation " + operation);
	    }
	}

	StringBuilder ib = new StringBuilder();
	ib.append("<img class=\"thumbnail\" src=\"img?id=").append(albumPhoto.getId()).append("&size=medium").append("\"");
	ib.append(">");

	addLine("");
	add("         <span class=\"image-in-album\">");
	add(link);
	add(ib);
	add("<span class=\"phototitle\">" + title + "</span>");
	add("</a>");
	add("</span>");

    }

    @SuppressWarnings("unused")
    private void generateTimelineAll() throws Exception {
	Database db = null;
	try {

	    db = new Database();
	    LocalDate startDate = LocalDate.now();

	    LocalDate endDate = startDate.minus(Period.ofMonths(12));
	    LocalDate currentDate = startDate;

	    while (currentDate.isAfter(endDate)) {
		LocalDate previousDay = currentDate.minus(Period.ofDays(1));

		User user = RoleModel.getUser(pageContext);
		Query query = new Query(user);
		query.setOrderBy(OrderBy.DATE_TAKEN_NEWEST);
		TimePeriod period = new TimePeriod();
		period.setFrom(previousDay);
		period.setTo(currentDate);
		query.setTimePeriod(period);

		List<Photo> photosAtThatDay = db.query(query);

		if (!photosAtThatDay.isEmpty()) {
		    generatePhotosForDay(currentDate, photosAtThatDay);
		}

		currentDate = currentDate.minus(Period.ofDays(1));
	    }
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

    private void generatePhotosForDay(LocalDate date, List<Photo> photos) throws JspException {
	// addTitle(date.toString());
	add("<ul>");
	// generateHtmlListItems(photos);
	add("</ul>");
    }

}
