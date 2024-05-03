package com.jeltechnologies.photos.timeline;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.db.QuerySupport;
import com.jeltechnologies.photos.pictures.MediaType;

public class TimelineCacheUpdateThread implements Runnable {
    private static final boolean USE_CACHE = Environment.INSTANCE.getConfig().isCaching();

    private static final Logger LOGGER = LoggerFactory.getLogger(TimelineCacheUpdateThread.class);

    public void run() {
	String threadName = this.getClass().getSimpleName();
	if (!USE_CACHE) {
	    LOGGER.info(threadName + " skipped, because Timeline is not using cache");
	} else {
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug(threadName + " started");
	    }

	    QuerySupport querySupport = null;

	    try {
		TimeLineTurboCache.getInstance().setCacheMustBeRefreshed();
		querySupport = TimeLineTurboCache.getInstance();

		List<User> users = new ArrayList<User>();
		users.add(RoleModel.getSystemAdmin());

		List<Grouping> groups = new ArrayList<Grouping>();
		groups.add(Grouping.YEAR);
		groups.add(Grouping.MONTH);
		groups.add(Grouping.WEEK);

		List<MediaType> mediaTypes = new ArrayList<MediaType>();
		mediaTypes.add(MediaType.ALL);
		mediaTypes.add(MediaType.PHOTO);
		mediaTypes.add(MediaType.VIDEO);

		for (User user : users) {

		    Query query = new Query(user);
		    querySupport.query(query);

		    for (MediaType mediaType : mediaTypes) {
			for (Grouping grouping : groups) {
			    if (Thread.interrupted()) {
				throw new InterruptedException();
			    }
			    long startTime = System.currentTimeMillis();
			    TimelineView request = new TimelineView(user, grouping, false, mediaType);
			    new TimelineFetcher(querySupport, request).fetch();
			    if (LOGGER.isDebugEnabled()) {
				long endTime = System.currentTimeMillis();
				long timeSpent = endTime - startTime;
				long minutes = (timeSpent / 1000) / 60;
				long seconds = (timeSpent / 1000) % 60;
				StringBuilder log = new StringBuilder("Timeline cached for ");
				log.append("type: ").append(mediaType);
				log.append(", grouping: ").append(grouping);
				log.append(" => in ");
				if (minutes > 0) {
				    log.append(minutes).append(" minutes and ");
				}
				log.append(seconds + " seconds.");
				LOGGER.debug(log.toString());
			    }
			}
		    }
		}

	    } catch (SQLException e) {
		LOGGER.error("Error updatin cache: " + e.getMessage(), e);
	    } catch (InterruptedException e) {
		LOGGER.info(threadName + " interrupted");
	    } finally {
		if (querySupport != null) {
		    querySupport.close();
		}
	    }
	    if (LOGGER.isInfoEnabled()) {
		LOGGER.debug(threadName + " ended");
	    }
	}
    }

}
