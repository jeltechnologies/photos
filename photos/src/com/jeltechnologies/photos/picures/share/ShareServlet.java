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
import com.jeltechnologies.photos.utils.StringUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/share/*")
public class ShareServlet extends BaseServlet {
    public record PostResponseBody(String url, String fileName, String mimeType) {
    };

    private static final long serialVersionUID = -7057970436310179978L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ShareServlet.class);
    private static Environment env = Environment.INSTANCE;
    protected static final String SESSION_KEY = SharedFile.class.getName();
    private static final int TOKEN_VALIDATY_SECONDS = 10;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("doGet: " + request.getRequestURI());
	}
	String uuid = StringUtils.findAfterLast(request.getRequestURI(), "/");
	Database db = null;
	try {
	    db = new Database();
	    SharedFile shared = db.getSharedFile(uuid);
	    LOGGER.trace("Found shared from database: " + shared);
	    if (shared == null) {
		LOGGER.trace("Shared not found in database");
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    } else {
		LocalDateTime now = LocalDateTime.now();
		if (now.isAfter(shared.expirationDate())) {
		    LOGGER.trace("Shared expired. " + shared.expirationDate() + ", while it is now: " + now );
		    response.setStatus(HttpServletResponse.SC_GONE);
		} else {
		    Photo photo = db.getFirstPhotoById(RoleModel.getSystemAdmin(), shared.photoId());
		    if (photo == null) {
			LOGGER.trace("Photo not found in database");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		    } else {
			boolean originalQuality = true;
			LOGGER.trace("Responding photo");
			respondSharedPhoto(request, response, photo, originalQuality);
		    }
		}
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
    
    private void respondSharedPhoto(HttpServletRequest request, HttpServletResponse response, Photo photo, boolean originalQuality) throws Exception {
	switch (photo.getType()) {
	    case PHOTO: {
		Dimension dimension = env.getDimensionFullscreen();
		File imageFile = ThumbnailUtilsFactory.getUtils().getThumbFile(dimension, photo);
		if (imageFile != null && imageFile.exists()) {
		    response.setHeader("Access-Control-Allow-Origin", "*");
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	LOGGER.trace("doPost");
	SharedFileRequest sharedFileRequested = null;
	try {
	    sharedFileRequested = (SharedFileRequest) getJsonFromBody(request, SharedFileRequest.class);
	    LOGGER.trace("sharedFileRequest: " + sharedFileRequested);
	    Database database = null;
	    try {
		database = new Database();
		User user = RoleModel.getUser(request);
		Photo photo = database.getFirstPhotoById(user, sharedFileRequested.id());
		if (photo == null) {
		    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
		    String uuid = UUID.randomUUID().toString();
		    LocalDateTime created = LocalDateTime.now();
		    LocalDateTime expired = created.plusSeconds(TOKEN_VALIDATY_SECONDS);
		    String publicUrl = Environment.INSTANCE.getConfig().getPublicUrl();
		    if (!publicUrl.endsWith("/")) {
			publicUrl += "/";
		    }
		    publicUrl += "share/" + uuid;
		    SharedFile shared = new SharedFile(uuid, photo.getId(), created, expired, user.name());
		    database.addSharedFile(shared);
		    database.commit();
		    response.setHeader("Location", publicUrl);
		    response.setStatus(HttpServletResponse.SC_CREATED);
		    String mimeType;
		    String fileName = photo.getId();
		    switch (photo.getType()) {
			case PHOTO: {
			    fileName += ".jpg";
			    mimeType = "image/jpeg";
			    break;
			}
			case VIDEO: {
			    fileName += ".mp4";
			    mimeType = "video/mp4";
			    break;
			}
			default: {
			    throw new IllegalStateException("Unsupported type: " + photo.getType());
			}
		    }
		    respondJson(response, new PostResponseBody(publicUrl, fileName, mimeType));
		}
	    } catch (Exception e) {
		LOGGER.error("Error posting shared file: " + sharedFileRequested, e);
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    } finally {
		if (database != null) {
		    database.close();
		}
	    }
	} catch (Exception e) {
	    LOGGER.warn("Cannot parse JSON from body because " + e.getMessage(), e);
	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}
    }

}
