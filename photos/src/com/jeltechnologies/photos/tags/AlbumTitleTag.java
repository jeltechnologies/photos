package com.jeltechnologies.photos.tags;

import java.util.List;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.pictures.Album;

public class AlbumTitleTag extends BaseTag {
    private static final long serialVersionUID = -3967656138719685087L;

    private Database database;

    private boolean canBeRenamed;

    @Override
    public void addHTML() throws Exception {
	try {
	    database = new Database();
	    String relativeFolderName = getParameter("album");
	    if (relativeFolderName == null || relativeFolderName.equals("")) {
		relativeFolderName = Environment.INSTANCE.getRelativeRootAlbums();
	    }
	    List<Album> tree = database.getAlbumAndItsParents(relativeFolderName);
	    int size = tree.size();
	    if (size > 0) {
		String name = tree.get(size - 1).getName();
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < tree.size() - 1; i++) {
		    if (i > 0) {
			b.append(" / ");
		    }
		    b.append(tree.get(i).getName());
		}
		String subName = b.toString();
		renderHTML(name, subName);
	    }
	} finally {
	    if (database != null) {
		database.close();
	    }
	}
    }

    private void renderHTML(String name, String subName) throws Exception {
	String mainName = name;
	addLine(BACK_BUTTON);

	if (!subName.isEmpty()) {
	    addLine("<span id=\"album-name-sub\">" + subName + " /&nbsp;");
	}

	if (canBeRenamed) {
	    add("<div id=\"album-name-edit\">");
	    add("<input type=\"text\" id=\"album-name-edit-input\" name=\"album-name-edit-input\">");
	    add("<button onclick=\"handleRenameAlbumOK()\"><span>Ok</span></button>");
	    add("<button onclick=\"handleRenameAlbumCancel()\"><span>Cancel</span></button>");
	    addLine("</div>");
	}

	addLine("<div id=\"album-name\" class=\"album-title\" >" + mainName + "</div>");
	addLine("</span>");
    }

}
