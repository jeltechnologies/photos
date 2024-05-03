package com.jeltechnologies.photos.manage.add;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.background.thumbs.SingleMediaProducer;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.pictures.MediaQueue;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.servlet.BaseServlet;
import com.jeltechnologies.photos.timeline.TimeLineTurboCache;
import com.jeltechnologies.photos.utils.FileUtils;
import com.jeltechnologies.photos.utils.JSONUtilsFactory;
import com.jeltechnologies.photos.utils.StringUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/add-photos")
public class AddNewPhotosServlet extends BaseServlet {
    private static final long serialVersionUID = -4090282823923953250L;

    private static final Environment ENV = Environment.INSTANCE;

    private static final String ROOT_ALBUM_NAME = Environment.INSTANCE.getRelativeRootAlbums();

    private static final Logger LOGGER = LoggerFactory.getLogger(AddNewPhotosServlet.class);

    private static final int SECONDS = 1000;
    private static final int TIME_OUT = 10 * SECONDS;
    private static final int SLEEP_TIME = 1 * SECONDS;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	User user = RoleModel.getUser(request);
	Database database = null;
	try {
	    database = new Database();
	    LocalDate now = LocalDate.now();
	    int year = StringUtils.toInt(request.getParameter("year"), now.getYear());
	    int month = StringUtils.toInt(request.getParameter("month"), now.getMonthValue());
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Getting photos not in albums for month : " + month + " in year " + year);
	    }
	    List<Photo> photos = database.getPhotosIdsNotInAlbums(user, month, year, OrderBy.DATE_TAKEN_NEWEST);
	    NewPhotosResponse payload = new NewPhotosResponse(month, year, photos);
	    respondJson(response, payload);
	} catch (SQLException e) {
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	    LOGGER.error("Cannot fetch timeline", e);
	} finally {
	    if (database != null) {
		database.close();
	    }
	}
    }

    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	User user = RoleModel.getUser(request);
	List<String> selectedIDs = null;
	try {
	    String requestJson = getBody(request);
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("doPost " + requestJson);
	    }
	    selectedIDs = (List<String>) JSONUtilsFactory.getInstance().fromJSON(requestJson, List.class);
	} catch (Exception e) {
	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    LOGGER.warn(e.getMessage());
	}
	if (selectedIDs != null && !selectedIDs.isEmpty()) {
	    List<Photo> selectedPhotos = new ArrayList<Photo>();
	    Database db = null;
	    try {
		db = new Database();
		String idNotFound = null;
		for (int i = 0; i < selectedIDs.size() && idNotFound == null; i++) {
		    String id = selectedIDs.get(i);
		    Photo p = db.getFirstPhotoById(user, id);
		    if (p != null) {
			selectedPhotos.add(p);
		    } else {
			idNotFound = id;
			LOGGER.error("Cannot found photo for id " + id);
		    }
		}
		if (idNotFound != null) {
		    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		    response.getWriter().println(idNotFound + " was not found");
		} else {
		    addPhotos(selectedPhotos);
		}

		db.commit();

	    } catch (Exception e) {
		LOGGER.error("Error adding selectedIds", e);
		db.rollback();
		throw new ServletException(e.getMessage(), e);
	    } finally {
		if (db != null) {
		    db.close();
		}
	    }
	}
    }

    private void addPhotos(List<Photo> photos) throws SQLException, IOException, InterruptedException {
	for (Photo p : photos) {
	    LocalDateTime photoDate = p.getDateTaken();
	    int year = photoDate.getYear();
	    int month = photoDate.getMonthValue();
	    String monthString = String.valueOf(month);
	    if (monthString.length() == 1) {
		monthString = "0" + monthString;
	    }
	    String defaultFolder = ROOT_ALBUM_NAME + "/" + year + "/" + year + "-" + monthString;
	    addPhoto(p, defaultFolder);
	}
	if (photos.size() > 0) {
	    waitForQueuesToBeEmpty();
	}
    }

    private void waitForQueuesToBeEmpty() throws InterruptedException {
	BackgroundServices backgroundServices = BackgroundServices.getInstance();
	MediaQueue thumbs = backgroundServices.getThumbsQueue();
	MediaQueue video = backgroundServices.getVideoQueue();
	long startTime = System.currentTimeMillis();
	long timeOut = startTime + TIME_OUT;
	boolean allEmpty = false;
	while (startTime < timeOut && !allEmpty) {
	    allEmpty = (thumbs.getSize() == 0) && (video.getSize() == 0);
	    if (!allEmpty) {
		Thread.sleep(SLEEP_TIME);
	    }
	    startTime = System.currentTimeMillis();
	}
	TimeLineTurboCache.getInstance().setCacheMustBeRefreshed();
    }

    private boolean addPhoto(Photo photo, String selectedAlbum) throws IOException {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Adding " + photo.getRelativeFileName() + " to " + selectedAlbum);
	}
	File fromFile = ENV.getFile(photo.getRelativeFileName());

	String localName = fromFile.getName();
	String destinationRelativeFileName = selectedAlbum + "/" + localName;

	File toFile = ENV.getFile(destinationRelativeFileName);
	File toFolder = toFile.getParentFile();
	toFolder.mkdirs();

	boolean ok = false;
	if (fromFile.isFile() && !toFile.exists()) {
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("... copying file from " + fromFile + " to " + toFile);
	    }
	    FileUtils.copyFile(fromFile.getAbsolutePath(), toFile.getAbsolutePath(), true);
	    new SingleMediaProducer(RoleModel.ROLE_USER).addToQueue(toFile);
	    ok = true;
	}
	return ok;
    }

}
