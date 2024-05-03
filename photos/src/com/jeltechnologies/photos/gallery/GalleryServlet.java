package com.jeltechnologies.photos.gallery;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Settings;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.TimePeriod;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.servlet.BaseServlet;
import com.jeltechnologies.photos.tags.RequestParameterParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/gallery")
public class GalleryServlet extends BaseServlet {
    private static final long serialVersionUID = 7613701765517587762L;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GalleryServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String albumUrl = request.getParameter("album");
	String sort = request.getParameter("sort");
	String from = request.getParameter("from");
	String to = request.getParameter("to");
	String mediaTypeString = request.getParameter("mediatype");
	String photo = request.getParameter("photo");
	
	TimePeriod period = null;
	if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {
	    LocalDate fromDate = LocalDate.parse(from);
	    LocalDate toDate = LocalDate.parse(to);
	    period = new TimePeriod();
	    period.setFrom(fromDate);
	    period.setTo(toDate);
	}
	OrderBy orderBy = RequestParameterParser.getOrderByFromParameter(sort);
	MediaType mediaType = RequestParameterParser.getMediaTypeFromParameter(mediaTypeString);

	long startTime = System.currentTimeMillis();
	Database db = null;
	try {
	    db = new Database();
	    User user = RoleModel.getUser(request);
	    GalleryLogic logic = new GalleryLogic(user, db);
	    logic.setAlbumUrl(albumUrl);
	    logic.setMediaType(mediaType);
	    logic.setPeriod(period);
	    logic.setOrderBy(orderBy);
	    logic.setPhoto(photo);
	    Settings settings = Settings.get(request.getSession());
	    logic.setMaximumAmount(settings.getMaximumAmountInGallary());
	    List<Photo> photos = logic.getPhotos();
	    if (LOGGER.isDebugEnabled()) {
		long endTime = System.currentTimeMillis();
		LOGGER.debug("Got " + (photos.size()) + " photos for gallery in " + (endTime - startTime) + " milliseconds");
	    }
	    respondJson(response, photos);
	} catch (Exception e) {
	    throw new ServletException("Could not get photos", e);
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }
}
