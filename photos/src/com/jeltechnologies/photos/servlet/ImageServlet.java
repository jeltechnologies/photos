package com.jeltechnologies.photos.servlet;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.pictures.ThumbnailUtilsFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/img")
public class ImageServlet extends BaseServlet {
    private static final long serialVersionUID = 7360746348801696929L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageServlet.class);
    private static final Environment ENVIRONMENT = Environment.INSTANCE;

    private static final Dimension DIMENSION_SMALL = ENVIRONMENT.getDimensionThumbs();
    private static final Dimension DIMENSION_MEDIUM = ENVIRONMENT.getDimensionFullscreen();
    private static final Dimension DIMENSION_ORIGINAL = ENVIRONMENT.getDimensionOriginal();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	User user = RoleModel.getUser(request);
	String id = request.getParameter("id");
	if (LOGGER.isTraceEnabled()) {
	    String requestQueryString = request.getQueryString();
	    LOGGER.trace("doGet " + id + " query: " + requestQueryString);
	}

	Dimension dimension = getDimension(request);
	if (dimension == null) {
	    boolean useOriginal = isLocalNetworkRequest(request);
	    if (useOriginal) {
		dimension = DIMENSION_ORIGINAL;
	    } else {
		dimension = DIMENSION_MEDIUM;
	    }
	}

	Database database = null;
	try {
	    database = new Database();
	    Photo photo = database.getFirstPhotoById(user, id);
	    if (photo == null) {
		LOGGER.warn("No photo on id: " + id);
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    } else {
		serveImage(request, response, photo, dimension);
	    }
	} catch (IOException e) {
	    if (!e.getMessage().equals("An established connection was aborted by the software in your host machine")) {
		throw e;
	    }
	}

	catch (Exception e) {
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace(e.getMessage() + " while serving image at " + request.getRequestURI());
	    }
	    throw new ServletException("Error in imageServlet: " + e.getMessage(), e);
	} finally {
	    if (database != null) {
		database.close();
	    }
	}
    }

    public static Dimension getDimension(HttpServletRequest request) {
	String sizeString = request.getParameter("size");
	return getDimension(sizeString);
    }
    
    public static Dimension getDimension(String sizeString) {
	Dimension dimension = null;
	if (sizeString != null) {
	    if (sizeString.equalsIgnoreCase("small")) {
		dimension = DIMENSION_SMALL;
	    } else {
		if (sizeString.equalsIgnoreCase("medium")) {
		    dimension = DIMENSION_MEDIUM;
		} else {
		    if (sizeString.equalsIgnoreCase("original")) {
			dimension = DIMENSION_ORIGINAL;
		    }
		}
	    }
	}
	return dimension;
    }

    private void serveImage(HttpServletRequest request, HttpServletResponse response, Photo photo, Dimension dimension) throws IOException {
	File imageFile;
	imageFile = ThumbnailUtilsFactory.getUtils().getThumbFile(dimension, photo);
	if (imageFile != null && imageFile.exists()) {
	    respondBinaryFile(response, request.getServletContext(), imageFile);
	} else {
	    if (LOGGER.isTraceEnabled()) {
		boolean exists = false;
		if (imageFile != null) {
		    exists = imageFile.exists();
		}
		LOGGER.trace("imageFile: " + imageFile + ", exists " + exists);
	    }
	}
    }

}
