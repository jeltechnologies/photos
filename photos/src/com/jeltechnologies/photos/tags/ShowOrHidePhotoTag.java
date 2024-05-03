package com.jeltechnologies.photos.tags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.pictures.Photo;

public class ShowOrHidePhotoTag extends BaseTag {
    private static final long serialVersionUID = 2207050727258396063L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ShowOrHidePhotoTag.class);
    
    @Override
    public void addHTML() throws Exception {
	String relativeFileName = getParameter("photo");
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(relativeFileName);
	}
	Database db = null;
	try {
	    db = new Database();
	    Photo photo = db.getPhotoByFileName(user, relativeFileName);
	    if (photo != null) {
		addHTML(photo);
	    }
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }
    
    private void addHTML(Photo p) throws Exception {
	StringBuilder b = new StringBuilder();
    
	b.append("<img src=\"img?id=" + p.getId() + "?size=small\">");
	addLine(b);

	b = new StringBuilder();
	b.append("<input type=\"hidden\" id=\"photo\" name=\"photo\" value=\"");
	b.append(p.getRelativeFileName()).append("\">");
	addLine(b);
	
	b = new StringBuilder();
	b.append("<div><input type=\"checkbox\" id=\"hide-photo\" name=\"hide-photo\"");
	if (p.isHidden()) {
	    b.append(" checked");
	}
	b.append("><label for=\"hide-photo\">Hide from albums and frame</label></div>");
	addLine(b.toString());
    }

}
