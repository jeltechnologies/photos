package com.jeltechnologies.photos.tags;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.utils.StringUtils;

public class ManagePhotosTag extends BaseTag {
    private static final long serialVersionUID = 4728342329513942900L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagePhotosTag.class);

    private Database db;

    private int year;

    private int month;

    public ManagePhotosTag() {
	LOGGER.info("Instantiated");
    }

    @Override
    public void addHTML() throws Exception {
	String relativeFolderName = getParameter("album");
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(relativeFolderName);
	}
	try {
	    db = new Database();
	    year = StringUtils.stripToInteger(getParameter("year"));
	    month = StringUtils.stripToInteger(getParameter("month"));
	    List<Photo> photoIds = db.getPhotosIdsNotInAlbums(user, month, year, OrderBy.DATE_TAKEN_NEWEST);
	    generateHTML(photoIds);
	} finally {
	    if (db != null) { 
		db.close();
	    }
	}
    }

    private void generateHTML(List<Photo> photos) throws Exception {
	addLine("");
	addLine("<div class=\"new-photo-list\">");
	addLine("  <ul>");
	String onClick = "onclick=\"$(this).toggleClass('selected-photo');$(this).toggleClass('unselected-photo');return false;\"";
	for (Photo photo : photos) {
	    StringBuilder s = new StringBuilder();
	    s.append("    <li id=\"").append(photo.getId()).append("\"").append(" class=\"unselected-photo\" ").append(onClick).append(">");
	    addLine(s.toString());

	    switch (photo.getType()) {
		case PHOTO: {
		    StringBuilder b = new StringBuilder();
		    b.append("<img src=\"img?id=").append(photo.getId()).append("&size=medium\"");
		    b.append(" photo=\"").append(photo.getId()).append("\" ");
		    b.append(" class=\"image-in-album\"").append(">");
		    addLine("      " + b.toString());
		    break;
		}
		case VIDEO: {
		    String posterImage = "img?id=" + photo.getId() + "&size=medium";
		    StringBuilder b = new StringBuilder();
		    b.append("<video class=\"gallery-video\"");
		    b.append(" class=\"image-in-album\"");
		    b.append(" controls preload=\"none\"");
		    b.append(" poster=\"").append(posterImage).append("\"");
		    b.append(" photo=\"").append(photo.getId()).append("\" ");
		    b.append(">");
		    b.append("<source src=\"").append("video?id=").append(photo.getId()).append("&quality=high\">");
		    b.append("</video>");
		    addLine("      " + b.toString());
		}
		default: {
		}
	    }
	    addLine("    </li>");
	}
	addLine("  </ul>");
	addLine("</div>");
    }

}
