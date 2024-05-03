package com.jeltechnologies.photos.background;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.MovieQuality;
import com.jeltechnologies.photos.datatypes.MovieQuality.Type;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.db.TimePeriod;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.pictures.ThumbnailUtilsFactory;

@SuppressWarnings("unused")
public class MaintenanceJob implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceJob.class);

    private Database db;

    private static final Environment ENV = Environment.INSTANCE;

    private static final String THREAD_NAME = MaintenanceJob.class.getSimpleName();

    private User user = RoleModel.getSystemAdmin();

    @Override
    public void run() {
	Thread.currentThread().setName(THREAD_NAME);
	LOGGER.info(THREAD_NAME + " started");
	try {
	    db = new Database();
	    removeH265Thumbs();
	} catch (Exception e) {
	    LOGGER.error(e.getMessage(), e);
	} finally {
	    db.rollback();
	}
	LOGGER.info(THREAD_NAME + " ended");
    }

    private void removeH265Thumbs() throws SQLException {
	TimePeriod period = new TimePeriod();
	LocalDateTime from = LocalDate.of(2024, 1, 1).atStartOfDay();
	LocalDateTime to = LocalDate.of(2025, 1, 1).atStartOfDay();
	List<Photo> photos = db.getAllPhotos();
	for (Photo photo : photos) {
	    LocalDateTime date = photo.getDateTaken();
	    if (photo.getType() == MediaType.VIDEO && date.isAfter(from) && date.isBefore(to)) {
		File hqMovie = ThumbnailUtilsFactory.getUtils().getConvertedMovie(new MovieQuality(Type.HIGH), photo);
		boolean exists = hqMovie.isFile();
		LOGGER.info(DateTimeFormatter.ISO_LOCAL_DATE.format(photo.getDateTaken()) + " => " + hqMovie.getAbsolutePath() + " = >" + exists);
		if (exists) {
		    hqMovie.delete();
		}
	    }
	}

    }

}
