package com.jeltechnologies.photos.picures.frame;

import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.picures.frame.MoreLikeRequest.MoreLikeProgram;
import com.jeltechnologies.photos.servlet.BaseServlet;
import com.jeltechnologies.photos.utils.StringUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/frame/more-like-this")
public class MoreLikeThisServlet extends BaseServlet {
    private static final long serialVersionUID = -9112595247743204974L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MoreLikeThisServlet.class);

    private static final int DEFAULT_AMOUNT = FrameServlet.DEFAULT_AMOUNT;
    private static final int DEFAULT_DISTANCE_METERS = 2000;
    private static final int DEFAULT_DAYS_BEFORE = 5;
    private static final int DEFAULT_DAYS_AFTER = 5;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String id = request.getParameter("id");
	String moreLike = request.getParameter("morelike");
	String error = null;
	if (id == null || id.isBlank()) {
	    error = "No id found";
	}

	MoreLikeProgram program = null;
	if (error == null) {
	    if (moreLike != null && !moreLike.isBlank()) {
		if (moreLike.equalsIgnoreCase("date-and-place")) {
		    program = MoreLikeProgram.DATE_AND_PLACE;
		} else {
		    if (moreLike.equalsIgnoreCase("date")) {
			program = MoreLikeProgram.DATE;
		    } else {
			if (moreLike.equals("place")) {
			    program = MoreLikeProgram.PLACE;
			}
		    }
		}
	    }
	    if (moreLike == null) {
		error = "No morelike program found";
	    }
	}

	if (error != null) {
	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    LOGGER.error("Error: " + error);
	} else {
	    int amount = StringUtils.toInt(request.getParameter("amount"), DEFAULT_AMOUNT);
	    int distanceKilometers = StringUtils.toInt(request.getParameter("distancekm"), DEFAULT_DISTANCE_METERS);
	    int daysBefore = StringUtils.toInt(request.getParameter("before"), DEFAULT_DAYS_BEFORE);
	    int daysAfter = StringUtils.toInt(request.getParameter("after"), DEFAULT_DAYS_AFTER);
	    int index = StringUtils.toInt(request.getParameter("index"), 0);
	    MoreLikeRequest moreLikeRequest = new MoreLikeRequest();
	    moreLikeRequest.setId(id);
	    moreLikeRequest.setMoreLike(program);
	    moreLikeRequest.setRequestedAmount(amount);
	    moreLikeRequest.setDistanceKilometers(distanceKilometers);
	    moreLikeRequest.setDaysAfter(daysAfter);
	    moreLikeRequest.setDaysBefore(daysBefore);
	    moreLikeRequest.setIndex(index);
	    Database db = null;
	    try {
		db = new Database();
		User user = RoleModel.getUser(request);
		MoreLikeRequestHandler handler = new MoreLikeRequestHandler(db, user, moreLikeRequest);
		handler.getPhotos();
		respondJson(response, moreLikeRequest);
	    } catch (SQLException e) {
		throw new ServletException(e);
	    } finally {
		if (db != null) {
		    db.close();
		}
	    }
	}
    }

}
