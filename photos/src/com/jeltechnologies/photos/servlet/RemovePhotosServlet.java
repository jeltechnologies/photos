package com.jeltechnologies.photos.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.background.BackgroundServices;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.pictures.PhotoActionHandler;
import com.jeltechnologies.photos.pictures.PhotoRemoveFromAlbumHandler;
import com.jeltechnologies.photos.utils.StringUtils;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/remove-photos")
public class RemovePhotosServlet extends HttpServlet {
    private static final long serialVersionUID = -2824783858618046048L;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RemovePhotosServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String selectedPhotosParam = request.getParameter("selectedPhotos");
	User user = RoleModel.getUser(request);
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(selectedPhotosParam);
	}
	Database db = null;
	try {
	    List<String> selectedPhotos = null;
	    List<Photo> remove = new ArrayList<Photo>();
	    if (selectedPhotosParam != null && !selectedPhotosParam.equals("")) {
		selectedPhotos = StringUtils.split(selectedPhotosParam, ':');
		db = new Database();
		for (String fileName : selectedPhotos) {
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("FILENAME: " + fileName);
		    }
		    Photo p = db.getPhotoByFileName(user, fileName);
		    if (p != null) {
			remove.add(p);
		    }
		}
		removePhotos(user, remove);
	    } 
	    RequestDispatcher requestDispatcher = request.getRequestDispatcher("albums.jsp");
	    requestDispatcher.forward(request, response);

	} catch (Exception e) {
	    throw new ServletException("Cannot get selectedPhotos", e);
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }
    
    private void removePhotos(User user, List<Photo> photos) throws Exception {
	for (Photo photo : photos) {
	    PhotoAction action = new PhotoAction();
	    action.setUser(user);
	    action.setId(photo.getRelativeFileName());
	    PhotoActionHandler handler = new PhotoRemoveFromAlbumHandler(action);
	    handler.handle();
	}
	BackgroundServices.getInstance().refreshCacheAfter(5, TimeUnit.MINUTES);
    }

    
    

}
