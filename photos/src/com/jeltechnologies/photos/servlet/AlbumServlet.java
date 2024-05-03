package com.jeltechnologies.photos.servlet;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.pictures.Album;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.utils.JSONUtilsFactory;
import com.jeltechnologies.photos.utils.StringUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/album")
public class AlbumServlet extends BaseServlet {
    private static final long serialVersionUID = 778183772328636599L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AlbumServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	User user = RoleModel.getUser(request);
	String body = getBody(request);
	AlbumPayload payload;
	try {
	    payload = (AlbumPayload) JSONUtilsFactory.getInstance().fromJSON(body, AlbumPayload.class);
	    Database db = null;
	    try {
		db = new Database();

		String relativeFileName = payload.getRelativeFileName();

		if (!StringUtils.containsValue(relativeFileName)) {
		    throw new IllegalArgumentException("Missing attribute: relativeFileName");
		}

		if (LOGGER.isDebugEnabled()) {
		    Album albumBeforeUpdate = db.getAlbum(relativeFileName);
		    LOGGER.debug("Before update database: " + albumBeforeUpdate);
		}

		String coverPhotoId = payload.getCoverPhotoId();
		String name = payload.getName();

		Album album = new Album();
		album.setRelativeFolderName(relativeFileName);
		
		Photo coverPhoto = new Photo();
		coverPhoto.setId(coverPhotoId);
		album.setCoverPhoto(coverPhoto);
		album.setName(name);
		album.setRole(null);

		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("Add or update " + album);
		}

		db.addOrUpdateAlbum(user, album);

		db.commit();

		if (LOGGER.isDebugEnabled()) {
		    Album albumAfterUpdate = db.getAlbum(relativeFileName);
		    LOGGER.debug("After update database: " + albumAfterUpdate);
		}

	    } catch (Exception e) {
		db.rollback();
		throw new ServletException("Could not update album", e);
	    } finally {
		if (db != null) {
		    db.close();
		}
	    }
	} catch (Exception e1) {
	    LOGGER.warn("Cannot parse body", e1);
	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}
    }

}
