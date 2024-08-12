package com.jeltechnologies.photos.picures.frame;

import java.io.IOException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.servlet.BaseServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/frame/log")
public class FrameLogServlet extends BaseServlet {
    private static final long serialVersionUID = 8251873566661463457L;
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameLogServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	try {
	    FrameLogLine line = (FrameLogLine) getJsonFromBody(request, FrameLogLine.class);
	    line.setTimestamp(LocalDateTime.now());
	    line.setSessionId(request.getSession().getId());
	    line.setUser(getUserName(request));
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug(line.toString());
	    }
	    Database db = null;
	    try {
		db = new Database();
		db.addLogLine(line);
		db.commit();
	    } catch (Exception e) {
		LOGGER.error("Could not add log line " + line, e);
	    } finally {
		if (db != null) {
		    db.rollback();
		    db.close();
		}
	    }
	} catch (Exception e) {
	    LOGGER.warn("Cannot parse frame log line", e);
	    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	}
    }
}
