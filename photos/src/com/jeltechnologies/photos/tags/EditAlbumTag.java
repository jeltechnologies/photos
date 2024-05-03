package com.jeltechnologies.photos.tags;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.pictures.Album;

import jakarta.servlet.jsp.JspException;

public class EditAlbumTag extends BaseTag {
    private static final long serialVersionUID = 8279840894400700104L;

    private static final Logger LOGGER = LoggerFactory.getLogger(EditAlbumTag.class);

    private Database db;

    private Album album;

    @Override
    public void addHTML() throws Exception {
	String relativeFolderName = getParameter("album");
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(relativeFolderName);
	}
	try {
	    db = new Database();
	    album = db.getAlbum(relativeFolderName);
	    if (album != null) {
		add();
	    }
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

    private void addTree() throws Exception {
	List<Album> tree = db.getAlbumAndItsParents(album.getRelativeFolderName());
	StringBuilder b = new StringBuilder();
	for (int i = 0; i < tree.size(); i++) {
	    if (i > 0) {
		b.append(" / ");
	    }
	    b.append(tree.get(i).getName());
	}
	add("<p>");
	add(b);
	add("</p>");
    }

    private void add() throws Exception {
	StringBuilder b = new StringBuilder();

	addTree();
	addCoverFile(b);

	String input = addInputText("txtName", "", album.getName(), false, false);
	addLine(input);
    }

    private void addCoverFile(StringBuilder b) throws SQLException, JspException {
	String coverPhotoId = album.getCoverPhoto().getId();
	LOGGER.info("addCoverFile => " + coverPhotoId);
	if (coverPhotoId != null) {
	    b.append("<img src=\"img?id=").append(coverPhotoId).append("&size=small\">");
	    addLine(b);
	}
    }

}