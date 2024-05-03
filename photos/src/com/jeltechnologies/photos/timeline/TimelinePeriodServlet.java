package com.jeltechnologies.photos.timeline;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.TimePeriod;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.servlet.BaseServlet;
import com.jeltechnologies.photos.tags.RequestParameterParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/timeline")
public class TimelinePeriodServlet extends BaseServlet {
    private static final long serialVersionUID = -2281194623786689252L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String fromString = request.getParameter("from");
	String toString = request.getParameter("to");
	String sortedParameter = request.getParameter("sort");
	String mediaTypeString = request.getParameter("mediatype");

	if (fromString == null && toString == null && sortedParameter == null && mediaTypeString == null) {
	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	} else {
	    LocalDate from = LocalDate.parse(fromString, DateTimeFormatter.ISO_LOCAL_DATE);
	    LocalDate to = LocalDate.parse(toString, DateTimeFormatter.ISO_LOCAL_DATE);
	    TimePeriod timePeriod = new TimePeriod();
	    timePeriod.setFrom(from);
	    timePeriod.setTo(to);

	    OrderBy orderBy = RequestParameterParser.getOrderByFromParameter(sortedParameter);
	    MediaType mediaType = RequestParameterParser.getMediaTypeFromParameter(mediaTypeString);

	    User user = RoleModel.getUser(request);
	    TimelinePeriodLogic logic = new TimelinePeriodLogic(user);
	    logic.setMediaType(mediaType);
	    logic.setOrderBy(orderBy);
	    logic.setTimePeriod(timePeriod);

	    try {
		List<Photo> photos = logic.getPhotos();
		respondJson(response, photos);
	    } catch (Exception e) {
		new ServletException("Could not get photos", e);
	    }
	}
    }

}
