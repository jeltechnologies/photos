package com.jeltechnologies.photos.tags;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.pictures.FolderFilter;
import com.jeltechnologies.photos.utils.StringUtils;

public class AlbumsTreeTag extends BaseTag {
    private static final long serialVersionUID = -7512671213037647534L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AlbumsTreeTag.class);

    private static final File ROOT_ALBUM = Environment.INSTANCE.getConfig().getAlbumsFolder();

    private final static Environment ENV = Environment.INSTANCE;

    private List<String> ids;

    private String defaultNodeID;

    private String defaultfolder = "";

    private String defaultFoldersRoot;

    public String getDefaultfolder() {
	return defaultfolder;
    }

    public void setDefaultfolder(String defaultFolder) {
	this.defaultfolder = (String) this.pageContext.getRequest().getAttribute("defaultFolder");
	this.defaultFoldersRoot = StringUtils.stripAfterLast(defaultFolder, "/");
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("defaultFolder: " + defaultFolder);
	    LOGGER.trace("defaultFoldersRoot: " + defaultFoldersRoot);
	}
    }

    @Override
    public void addHTML() throws Exception {
	String defaultFromRequest = (String) this.pageContext.getRequest().getAttribute("defaultFolder");
	LOGGER.info("Default from request: " + defaultFromRequest);

	if (defaultFromRequest != null) {
	    setDefaultfolder(defaultFromRequest);
	}
	
	File folder = ENV.getFile(defaultFromRequest);
	if (!folder.exists()) {
	    boolean ok = folder.mkdirs();
	    if (ok) {
		LOGGER.info("Created folder " + folder);
	    } else {
		LOGGER.warn("Could not create golder " + folder);
	    }
	}
 
	defaultNodeID = null;
	ids = new ArrayList<String>();

	addLine("<div id=\"" + id + "\">");
	File root = ROOT_ALBUM;
	addFolder(root);
	addLine("</div>");

	

	addLine("<script>");
	StringBuilder b = new StringBuilder();
	b.append("var albumTreeIds = [ ");
	for (int i = 0; i < ids.size(); i++) {
	    String id = ids.get(i);
	    if (i > 0) {
		b.append(", ");
	    }
	    b.append("\"").append(id).append("\"");
	}

	b.append(" ];");
	addLine(b.toString());

	StringBuilder openNode = new StringBuilder();
	openNode.append("var albumTreeOpenNodeID = \"").append(defaultNodeID).append("\";");
	addLine(openNode.toString());

	addLine("</script>");
    }

    private void addFolder(File folder) throws Exception {
	if (folder.isDirectory()) {

	    String relativeFolder = ENV.getRelativePhotoFileName(folder);
	    boolean defaultExpected = relativeFolder.equals(defaultFoldersRoot);

	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace(relativeFolder + " defaultExpected: " + defaultExpected);
	    }

	    int idCounter = ids.size();
	    String nodeID = "node-" + idCounter;

	    if (relativeFolder.equals(this.defaultfolder)) {
		defaultNodeID = nodeID;
	    }

	    ids.add(relativeFolder);

	    addLine("<ul>");
	    add("<li id=\"" + nodeID + "\">");
	    add(folder.getName());
	    File[] children = folder.listFiles(new FolderFilter());
	    for (File child : children) {
		addFolder(child);
	    }

	    if (defaultExpected && defaultNodeID == null) {
		idCounter = ids.size();
		nodeID = "node-" + idCounter;
		ids.add(defaultfolder);
		defaultNodeID = nodeID;
		add("<ul><li id=\"" + nodeID + "\">");
		String name = StringUtils.stripBeforeLast(defaultfolder, "/");
		add(name);
		add("</li></ul>");
	    }

	    addLine("</li>");
	    addLine("</ul>");
	}
    }

}
