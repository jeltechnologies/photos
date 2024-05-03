package com.jeltechnologies.photos.servlet;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.datatypes.usermodel.Preferences;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.utils.JSONUtilsFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/preferences")
public class PreferencesServlet extends BaseServlet {
    private static final long serialVersionUID = 6532024734436026113L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	User user = RoleModel.getUser(request);
	Database db = null;
	try {
	    db = new Database();
	    Preferences preferences = db.getPreferences(user.name());
	    if (preferences == null) {
		preferences = new Preferences();
	    }
	    boolean logging = LOGGER.isDebugEnabled();
	    respondJson(response, preferences, logging);
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
	    LOGGER.debug("Post: " + body);
	}
	Database db = null;
	try {
	    Preferences preferences = (Preferences) JSONUtilsFactory.getInstance().fromJSON(body, Preferences.class);
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug(preferences.toString());
	    }
	    db = new Database();
	    db.setPreferences(user.name(), preferences);
	    db.commit();
	} catch (Exception e) {
	    if (db != null) {
		db.rollback();
	    }
	    LOGGER.error(e.getMessage(), e);
	    throw new ServletException(e.getMessage());
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

}
