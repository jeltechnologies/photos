package com.jeltechnologies.photos.picures.share;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.pictures.ThumbnailUtilsFactory;
import com.jeltechnologies.photos.servlet.BaseServlet;
import com.jeltechnologies.photos.utils.JSONUtilsFactory;
import com.jeltechnologies.photos.utils.StringUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/share/*")
public class ShareServlet extends BaseServlet {
    private static final long serialVersionUID = -7057970436310179978L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ShareServlet.class);
    private static Environment env = Environment.INSTANCE;
    protected static final String SESSION_KEY = SharedFile.class.getName();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String uuid = null;
	Database db = null;
	try {
	    uuid = getUUID(request.getRequestURI());
	    db = new Database();
	    SharedFile shared = db.getSharedFile(uuid);
	    Photo photo = null;
	    if (shared != null) {
		photo = db.getPhotoByFileName(RoleModel.getSystemUser(), shared.getRelativeFileName());
	    }
	    if (shared == null || photo == null) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    } else {
		boolean originalQuality = true;
		respondSharedPhoto(request, response, photo, originalQuality);
	    }
	} catch (Exception e) {
	    LOGGER.error("Cannot get shared file " + uuid, e);
	    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	User user = RoleModel.getUser(request);
	Database database = null;
	SharedFile shared = null;
	try {
	    database = new Database();
	    String body = getBody(request);
	    shared = (SharedFile) JSONUtilsFactory.getInstance().fromJSON(body, SharedFile.class);
	    Photo photo = database.getPhotoByFileName(user, shared.getRelativeFileName());
	    if (photo == null) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		LOGGER.error("No photo " + shared.getRelativeFileName() + " found in database for user " + user);
	    } else {
		String uuid = UUID.randomUUID().toString();
		shared.setUuid(uuid);
		LocalDateTime created = LocalDateTime.now();
		shared.setCreationDate(created);
		LocalDateTime expired = created.plusMonths(1);
		shared.setExpirationDate(expired);
		shared.setUsername(user.name());
		shared.setPublicUrl(createSharedUrl(photo, uuid));
		database.addSharedFile(shared);
		database.commit();
		request.getSession().setAttribute(SESSION_KEY, shared);
		respondJson(response, shared);
	    }

	} catch (Exception e) {
	    LOGGER.error("Error posting shared file: " + shared);
	    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	} finally {
	    if (database != null) {
		database.close();
	    }
	}

    }

    private String createSharedUrl(Photo photo, String uuid) {
	StringBuilder b = new StringBuilder();
	b.append(env.getConfig().getPublicUrl());
	b.append("share/").append(uuid);
	switch (photo.getType()) {
	    case PHOTO: {
		b.append(".jpg");
		break;
	    } case VIDEO: {
		b.append(".mp4");
		break;
	    }
	    default:
		break;
	}
	return b.toString();
    }

    private void respondSharedPhoto(HttpServletRequest request, HttpServletResponse response, Photo photo, boolean originalQuality) throws Exception {
	switch (photo.getType()) {
	    case PHOTO: {
		Dimension dimension = env.getDimensionFullscreen();
		File imageFile = ThumbnailUtilsFactory.getUtils().getThumbFile(dimension, photo);
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
		break;
	    }
	    case VIDEO: {

		break;
	    }
	    default: {
		throw new IllegalArgumentException("Source not supported");
	    }
	}
    }

    protected static String getUUID(String requestURI) {
	String fileName = StringUtils.findAfterIfNotFoundReturnIn(requestURI, "share/");
	String uuid = StringUtils.stripAfterLast(fileName, ".");
	return uuid;
    }

   

}
