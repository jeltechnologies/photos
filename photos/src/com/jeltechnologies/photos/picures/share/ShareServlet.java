package com.jeltechnologies.photos.picures.share;

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
import com.jeltechnologies.photos.servlet.BaseServlet;

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
	    LOGGER.debug("sharedFileRequest: " + sharedFileRequested);
	    Database database = null;
	    try {
		database = new Database();
		User user = RoleModel.getUser(request);
		Photo photo = database.getFirstPhotoById(user, sharedFileRequested.id());
		if (photo == null) {
		    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
		    String mimeType;
		    String fileName = photo.getId();
		    String size;
		    String url;
		    switch (photo.getType()) {
			case PHOTO: {
			    fileName += ".jpg";
			    mimeType = "image/jpeg";
			    size="medium";
			    url = "img?id=" + sharedFileRequested.id() + "&size=" + size;
			    break;
			}
			case VIDEO: {
			    fileName += ".mp4";
			    mimeType = "video/mp4";
			    size = "small";
			    url = "download/" + sharedFileRequested.id() + "-size-medium.mp4";
			    break;
			}
			default: {
			    throw new IllegalStateException("Unsupported type: " + photo.getType());
			}
		    }
		    
		    PostResponseBody responseBody = new PostResponseBody(url, fileName, mimeType);
		    LOGGER.debug(responseBody.toString());
		    respondJson(response, responseBody);
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
