package com.jeltechnologies.photos.picures.frame;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.QuerySupport;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.picures.frame.FilterOption.Contents;
import com.jeltechnologies.photos.picures.frame.program.AllPhotosProgram;
import com.jeltechnologies.photos.picures.frame.program.BaseFrameProgram;
import com.jeltechnologies.photos.picures.frame.program.Group;
import com.jeltechnologies.photos.picures.frame.program.QueryStringAtTime;
import com.jeltechnologies.photos.servlet.BaseServlet;
import com.jeltechnologies.photos.utils.StringUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/frame/random")
public class FrameServlet extends BaseServlet {
    private static final long serialVersionUID = 7326200087772567907L;
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameServlet.class);
    protected static final int DEFAULT_AMOUNT = 120;
    private static final String PREVIOUS_QUERY_SESSION_KEY = QueryStringAtTime.class.getName();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	if (LOGGER.isTraceEnabled()) {
	    Map<String, String[]> parameterMap = request.getParameterMap();
	    for (String key : parameterMap.keySet()) {
		String[] values = parameterMap.get(key);
		for (String value : values) {
		    LOGGER.trace("Parameter: " + key + "=" + value);
		}
	    }
	}

	QuerySupport querySupport = null;
	try {
	    long startTime = System.currentTimeMillis();
	    querySupport = new Database();
	    FilterOption filterOption = getFilterOption(request);
	    HttpSession session = request.getSession();

	    String currentQuery = request.getQueryString();
	    
	    QueryStringAtTime previousQueryString = (QueryStringAtTime) session.getAttribute(PREVIOUS_QUERY_SESSION_KEY);
	    boolean found;
	    boolean same;
	    if (previousQueryString == null) {
		found = false;
		same = false;
	    } else {
		found = true;
		String oldQuery = previousQueryString.getQuery();
		same = currentQuery.equals(oldQuery);
	    }

	    if (!found || !same) {
		previousQueryString = new QueryStringAtTime(currentQuery);
		session.setAttribute(PREVIOUS_QUERY_SESSION_KEY, previousQueryString);
	    }

	    LocalDateTime now = LocalDateTime.now();
	    LocalDateTime filterStart = previousQueryString.getStartedAt();
	    int sinceHours = (int) ChronoUnit.HOURS.between(filterStart, now);

	    List<Photo> photos = filterOption.program().getPhotos(filterOption);
	    String mapKey = Environment.INSTANCE.getConfig().getMapBoxAccessToken();
	    FrameResponse frameResponse = new FrameResponse(filterOption.program().getName(), filterOption.program().getDescription(), mapKey, photos, sinceHours);
	    respondJson(response, frameResponse);
	    if (LOGGER.isTraceEnabled()) {
		long endTime = System.currentTimeMillis();
		LOGGER.trace("Got " + (photos.size()) + " photos for gallery in " + (endTime - startTime) + " milliseconds");
	    }

	} catch (IllegalArgumentException e) {
	    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
	} catch (Exception e) {
	    throw new ServletException("Could not get photos", e);
	} finally {
	    if (querySupport != null) {
		querySupport.close();
	    }
	}
    }

    private FilterOption getFilterOption(HttpServletRequest request) {
	String programParam = request.getParameter("program");
	String programPercentageParam = request.getParameter("program-percentage");
	String amountParam = request.getParameter("amount");
	String photoInSlideShow = request.getParameter("photo-in-slideshow");

	int amount = StringUtils.toInt(amountParam, DEFAULT_AMOUNT);
	int programPercentage = StringUtils.toInt(programPercentageParam, 100);
	if (programPercentage < 0 || programPercentage > 100) {
	    throw new IllegalArgumentException("Invalid program-percentage: " + programPercentageParam);
	}

	List<Group> groups = Environment.INSTANCE.getFrameProgramGroups();
	BaseFrameProgram program;
	program = getProgram(programParam, groups);
	if (program == null || programPercentage == 0) {
	    program = new AllPhotosProgram();
	    programPercentage = 100;
	}

	FilterOption.Contents contents;
	if (programPercentage == 100) {
	    contents = Contents.PROGRAM_ONLY;
	} else {
	    contents = Contents.PROGRAM_WITH_RANDOM;
	}

	FilterOption filter = new FilterOption(RoleModel.getUser(request), program, photoInSlideShow, amount, contents, programPercentage);
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug(filter.toString());
	}
	return filter;
    }

    private BaseFrameProgram getProgram(String programParam, List<Group> programGroups) {
	BaseFrameProgram found = null;
	Iterator<Group> groupIterator = programGroups.iterator();
	while (found == null && groupIterator.hasNext()) {
	    Iterator<BaseFrameProgram> i = groupIterator.next().programs().iterator();
	    while (i.hasNext() && found == null) {
		BaseFrameProgram p = i.next();
		if (p.getName().equals(programParam)) {
		    found = p;
		}
	    }
	}
	return found;
    }
}
