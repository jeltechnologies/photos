package com.jeltechnologies.photos.servlet;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Settings;
import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.pictures.Album;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.pictures.PhotoRemoveFromAlbumHandler;
import com.jeltechnologies.photos.pictures.PhotoRotateActionHandler;
import com.jeltechnologies.photos.pictures.PhotoRotation;
import com.jeltechnologies.photos.pictures.ShowOrHidePhotoHandler;
import com.jeltechnologies.photos.utils.JSONUtilsFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/photo")
public class PhotoServlet extends BaseServlet {
    private static final long serialVersionUID = -828525890136127616L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("doGet");
	}
	String relativeFileName = request.getParameter("photo");
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug(relativeFileName);
	}
	Database db = null;
	try {
	    User user = RoleModel.getUser(request);
	    db = new Database();
	    Photo p = db.getPhotoByFileName(user, relativeFileName);
	    if (p == null) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    } else {
		Album a = db.getAlbum(p.getRelativeFolderName());
		Settings settings = Settings.get(request.getSession());
		PhotoPayload info = getPhotoPayload(p, a, settings);
		respondJson(response, info);
	    }
	} catch (Exception e) {
	    LOGGER.error(e.getMessage(), e);
	    throw new ServletException(e.getMessage());
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	User user = RoleModel.getUser(request);
	String body = getBody(request);

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug(body);
	}
	try {
	    PhotoAction action = (PhotoAction) JSONUtilsFactory.getInstance().fromJSON(body, PhotoAction.class);
	    action.setUser(user);
	    switch (action.getAction()) {
		case REMOVE_FROM_ALBUM: {
		    new PhotoRemoveFromAlbumHandler(action).handle();
		    BackgroundServices.getInstance().refreshCacheAfter(5, TimeUnit.MINUTES);
		    break;
		}
		case SHOW: {
		    new ShowOrHidePhotoHandler(action, false).handle();
		    break;
		}
		case HIDE: {
		    new ShowOrHidePhotoHandler(action, true).handle();
		    break;
		}
		case ROTATE_LEFT:
		case ROTATE_RIGHT:
		case FLIP_VERTICAL:
		case FLIP_HORIZONTAL: {
		    new PhotoRotateActionHandler(action).handle();
		    break;
		}
		case NOT_SUPPORTED: {
		    throw new IllegalArgumentException("Uknown action: " + action.getAction());
		}
		default: {
		    throw new IllegalArgumentException("Uknown action: " + action.getAction());
		}
	    }
	} catch (Exception e) {
	    LOGGER.error(e.getMessage(), e);
	    throw new ServletException(e.getMessage());
	}
    }


    private PhotoPayload getPhotoPayload(Photo p, Album a, Settings settings) {
	PhotoPayload info;
	if (p == null) {
	    info = null;
	} else {
	    info = new PhotoPayload();
	    info.setType(p.getType().toString());
	    if (a != null) {
		info.setAlbumName(a.getName());
	    }
	    info.setRelativeFileName(p.getRelativeFileName());
	    info.setName(p.getFileName());
	    info.setDurationSeconds(p.getDuration());

	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(settings.getDateFormat());
	    info.setDate(formatter.format(p.getDateTaken()));
	    info.setCoordinate(p.getCoordinates());
	    int orientation = p.getOrientation();
	    PhotoRotation rotation = PhotoRotation.getRotation(orientation);
	    p.setOrientation(orientation);
	    info.setRotation(rotation.toString());
	    info.setSource(p.getSource());
	    info.setName(p.getFileName());
	}
	return info;
    }

}
