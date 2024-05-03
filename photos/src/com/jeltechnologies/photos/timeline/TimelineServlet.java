package com.jeltechnologies.photos.timeline;

import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.QuerySupport;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.servlet.BaseServlet;
import com.jeltechnologies.photos.tags.RequestParameterParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/timeline-page")
public class TimelineServlet extends BaseServlet {
    private static final long serialVersionUID = -4706494009517002227L;
    private static final Logger LOGGER = LoggerFactory.getLogger(TimelineServlet.class);
    private static final boolean USE_CACHE = Environment.INSTANCE.getConfig().isCaching();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String groupingParameter = request.getParameter("grouping");
	String randomizeParameter = request.getParameter("randomize");
	String mediaTypeParameter = request.getParameter("mediatype");

	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("doGet");
	    LOGGER.trace("  grouping: " + groupingParameter);
	    LOGGER.trace("  randomize: " + randomizeParameter);
	    LOGGER.trace("  mediaType: " + mediaTypeParameter);
	}

	boolean randomize = Boolean.parseBoolean(randomizeParameter);
	Grouping grouping = RequestParameterParser.getGrouping(groupingParameter);
	MediaType mediaType = RequestParameterParser.getMediaTypeFromParameter(mediaTypeParameter);
	User user = RoleModel.getUser(request);

	QuerySupport database = null;
	try {
	    if (USE_CACHE) {
		database = TimeLineTurboCache.getInstance();
	    } else {
		database = new Database();
	    }
	    TimelineView view = new TimelineView(user, grouping, randomize, mediaType);
	    Timeline timeline = new TimelineFetcher(database, view).fetch();
	    respondJson(response, timeline);
	} 
	catch (SQLException e) {
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	    LOGGER.error("Cannot fetch timeline", e);
	}
	finally {
	    if (database != null) {
		database.close();
	    }
	}
    }

}
