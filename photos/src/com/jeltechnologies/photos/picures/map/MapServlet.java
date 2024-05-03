package com.jeltechnologies.photos.picures.map;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.Settings;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.MapBounds;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.db.TimePeriod;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.servlet.BaseServlet;
import com.jeltechnologies.photos.utils.StringUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/map")
public class MapServlet extends BaseServlet {
    private static final long serialVersionUID = 7893300338179099641L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MapServlet.class);
    private static final Environment ENV = Environment.INSTANCE;
    public static final int DEFAULT_ZOOM = 5;

    private enum Mode {
	TIMELINE, ALBUM, FREE
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("doGet");
	}

	User user = RoleModel.getUser(request);
	Database db = null;
	try {
	    db = new Database();
	    Mode mode = getMode(request);

	    MapBounds bounds = getBounds(request);
	    MapView view = getMapCenter(request);

	    Query query = new Query(user);
	    query.setRelativeFolderName(ENV.getRelativeRootAlbums());
	    query.setIncludeSubFolders(true);
	    query.setMapBounds(bounds);
	    query.excludeLivePhotos();

	    switch (mode) {
		case ALBUM: {
		    query.setRelativeFolderName(request.getParameter("album"));
		    break;
		}
		case TIMELINE: {
		    TimePeriod timePeriod = getTimeline(request);
		    query.setTimePeriod(timePeriod);
		    break;
		}
		case FREE: {
		    TimePeriod timePeriod = getTimePeriod(request);
		    query.setTimePeriod(timePeriod);
		    break;
		}
	    }

	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace(bounds.toString());
		LOGGER.trace("MapView: " + view);
	    }

	    query.setOrderBy(OrderBy.DATE_TAKEN_NEWEST);

	    List<Photo> photos = db.query(query);

	    LOGGER.trace("Photos in view: " + photos.size());

	    respondJson(response, photos);
	} catch (Exception e) {
	    LOGGER.error(e.getMessage(), e);
	    throw new ServletException(e.getMessage(), e);
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

    public static List<PhotoMapInfo> createMapInfo(HttpServletRequest request, List<Photo> photos) {
	List<PhotoMapInfo> coordinates = new ArrayList<PhotoMapInfo>(photos.size());
	Settings settings = Settings.get(request.getSession());
	DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(settings.getDateFormat());
	for (Photo photo : photos) {
	    coordinates.add(new PhotoMapInfo(photo, dateFormat));
	}
	return coordinates;
    }

    private MapBounds getBounds(HttpServletRequest request) {
	String northEastLat = request.getParameter("northEastLat");
	String northEastLng = request.getParameter("northEastLng");
	String southWestLat = request.getParameter("southWestLat");
	String southWestLng = request.getParameter("southWestLng");

	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("NE " + northEastLat + ", " + northEastLng);
	    LOGGER.trace("SE " + southWestLat + ", " + southWestLng);
	}

	Coordinate northEast = new Coordinate(northEastLat, northEastLng);
	Coordinate southWest = new Coordinate(southWestLat, southWestLng);
	MapBounds bounds = new MapBounds();
	bounds.setNorthEast(northEast);
	bounds.setSouthWest(southWest);
	return bounds;
    }

    private MapView getMapCenter(HttpServletRequest request) {
	String lat = request.getParameter("mapCenterLat");
	String lng = request.getParameter("mapCenterLng");
	String zoomString = request.getParameter("zoom");

	int zoom;

	try {
	    zoom = Integer.parseInt(zoomString);
	} catch (NumberFormatException nfe) {
	    zoom = 12;
	    LOGGER.warn("Error in zoom " + zoomString);
	}
	Coordinate c = new Coordinate(lat, lng);

	MapView view = MapView.getMapView(request.getSession());
	view.setMapCenter(c);
	view.setZoom(zoom);

	return view;
    }

    private TimePeriod getTimeline(HttpServletRequest request) {
	String fromParam = request.getParameter("from");
	String toParam = request.getParameter("to");
	LocalDate from = LocalDate.parse(fromParam, DateTimeFormatter.ISO_DATE);
	LocalDate to = LocalDate.parse(toParam, DateTimeFormatter.ISO_DATE);
	TimePeriod period = new TimePeriod();
	period.setFrom(from);
	period.setTo(to);
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug(period.toString());
	}
	return period;
    }

    private TimePeriod getTimePeriod(HttpServletRequest request) {
	String period = request.getParameter("period");
	TimePeriod timePeriod = null;
	if ("all".equals(period)) {
	    period = null;
	} else {
	    if ("custom".equals(period)) {
		int fromMonth = StringUtils.toInt(request.getParameter("fromMonth"), -1);
		int fromYear = StringUtils.toInt(request.getParameter("fromYear"), -1);
		int toMonth = StringUtils.toInt(request.getParameter("toMonth"), -1);
		int toYear = StringUtils.toInt(request.getParameter("toYear"), -1);

		timePeriod = new TimePeriod();
		LocalDate from = LocalDate.of(fromYear, fromMonth, 1);
		LocalDate to = LocalDate.of(toYear, toMonth, 1).plusMonths(1);
		timePeriod.setFrom(from);
		timePeriod.setTo(to);
	    } else {
		LocalDate today = LocalDate.now();
		if ("thismonth".equals(period)) {
		    timePeriod = new TimePeriod();
		    LocalDate from = today.minusMonths(1);
		    timePeriod.setFrom(from);
		    timePeriod.setTo(today);
		} else {
		    if ("last3months".equals(period)) {
			timePeriod = new TimePeriod();
			LocalDate from = today.minusMonths(3);
			timePeriod.setFrom(from);
			timePeriod.setTo(today);
		    } else {
			if ("last12months".equals(period)) {
			    timePeriod = new TimePeriod();
			    LocalDate from = today.minusMonths(12);
			    timePeriod.setFrom(from);
			    timePeriod.setTo(today);
			} else {
			    if ("last24months".equals(period)) {
				timePeriod = new TimePeriod();
				LocalDate from = today.minusMonths(24);
				timePeriod.setFrom(from);
				timePeriod.setTo(today);
			    } else {
				if ("last36months".equals(period)) {
				    timePeriod = new TimePeriod();
				    LocalDate from = today.minusMonths(36);
				    timePeriod.setFrom(from);
				    timePeriod.setTo(today);
				} else {
				    LOGGER.warn("Unsupported period: " + period);
				}
			    }
			}
		    }
		}
	    }
	}
	return timePeriod;
    }

    private Mode getMode(HttpServletRequest request) {
	Mode m;
	String s = request.getParameter("mode");
	if (s.equalsIgnoreCase("album")) {
	    m = Mode.ALBUM;
	} else {
	    if (s.equalsIgnoreCase("timeline")) {
		m = Mode.TIMELINE;
	    } else {
		if (s.equalsIgnoreCase("free")) {
		    m = Mode.FREE;
		} else {
		    m = Mode.FREE;
		    LOGGER.warn("Unsupported mode: " + s);
		}
	    }
	}
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Mode: " + m);
	}
	return m;
    }

}
