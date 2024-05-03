package com.jeltechnologies.photos.tags;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.pictures.Album;
import com.jeltechnologies.photos.pictures.Photo;

public class SetAlbumCoverTag extends BaseTag {
    private static final long serialVersionUID = 6177495097621010744L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SetAlbumCoverTag.class);

    private Database db;

    @Override
    public void addHTML() throws Exception {
	String id = getParameter("photo");
	String album = getParameter("album");
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(id);
	}
	try {
	    db = new Database();
	    Photo photo = db.getFirstPhotoById(user, id);
	    if (photo != null) {
		addHTML(photo, album);
	    }
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

    private void addHTML(Photo p, String relativeFolderName) throws Exception {
	StringBuilder b = new StringBuilder();
	String image = p.getId();

	b.append("<img src=\"img?id=").append(image).append("&size=small\">");
	addLine(b);

	b = new StringBuilder();
	b.append("<input type=\"hidden\" id=\"photo\" name=\"photo\" value=\"");
	b.append(p.getId()).append("\">");
	addLine(b);

	List<Album> albumTree = db.getAlbumAndItsParents(relativeFolderName);

	addLine("<p>Select an album</p>");
	
	boolean first = true;
	for (int i = albumTree.size() - 1; i > 0; i--) {
	    Album album = albumTree.get(i);
	    String id = album.getRelativeFolderName();
	    String label = album.getName();

	    StringBuilder r = new StringBuilder();
	    r.append("<input type=\"radio\" id=\"");
	    r.append(id).append("\" name=\"selected-album\" value=\"");
	    r.append(id).append("\"");
	    if (first) {
		r.append(" checked=\"checked\"");
		first = false;
	    }
	    r.append(">");
	    r.append("<label for=\"").append(id).append("\">");
	    r.append(label);
	    r.append("</label><br>");
	    addLine(r);
	}
    }

}
