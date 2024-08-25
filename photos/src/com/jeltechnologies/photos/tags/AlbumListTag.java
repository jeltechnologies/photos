package com.jeltechnologies.photos.tags;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.icons.IconTag;
import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.PhotoFileSystem;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.pictures.Album;
import com.jeltechnologies.photos.pictures.Folder;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.utils.JSONUtilsFactory;
import com.jeltechnologies.photos.utils.StringUtils;
import com.jeltechnologies.util.TimeLogger;

import jakarta.servlet.jsp.JspException;

public class AlbumListTag extends BaseTag {
    private static final long serialVersionUID = -7833804399361485187L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AlbumListTag.class);

    private static final String ROOT_ALBUM_NAME = Environment.INSTANCE.getConfig().getAlbumsFolder().getName();

    public static final String ALBUM_PARAMETER = "album";

    private String albumRelativeFolderName;

    private List<Album> albums;

    private Folder folder;

    private User user;

    private Database db;

    private static final OrderBy SORTING = OrderBy.DATE_TAKEN_OLDEST;

    private static final String ALBUM_WITHOUT_COVER = "images/album-without-cover-1440-1080.jpg";

    private static final String PLAY_ICON = new IconTag("play-circle").toString();

    private static final int ALBUM_WITHOUT_COVER_WIDTH = 1440;

    private static final int ALBUM_WITHOUT_COVER_HEIGHT = 1080;

    private static final BigDecimal MAX_HEIGHT = new BigDecimal(200);

    private static final int LOAD_LAZY_AFTER_IMAGE = 10;

    public AlbumListTag() {
	LOGGER.trace("AlbumTag instantiated");
    }

    private String getSizeHtml(int thumbWidth, int thumbHeight) {
	int width;
	if (thumbHeight > 0 && thumbWidth > 0) {
	    BigDecimal bdWidth = new BigDecimal(thumbWidth).setScale(10).divide(new BigDecimal(thumbHeight), RoundingMode.HALF_UP).multiply(MAX_HEIGHT);
	    width = bdWidth.intValue();
	} else {
	    width = 0;
	}
	String sizeHtml = " width='" + width + "' height='" + MAX_HEIGHT + "'";
	return sizeHtml;
    }

    @Override
    public void addHTML() throws Exception {
	db = null;
	try {
	    user = RoleModel.getUser(pageContext);
	    albumRelativeFolderName = getParameter("album");
	    if (albumRelativeFolderName == null || albumRelativeFolderName.isEmpty()) {
		albumRelativeFolderName = "/" + ROOT_ALBUM_NAME;
	    }
	    folder = PhotoFileSystem.getFolder(albumRelativeFolderName);
	    add("<div");
	    if (id != null) {
		add(" id=\"" + id + "\"");
	    }
	    if (cssClass != null) {
		add(" class=\"" + cssClass + "\"");
	    }
	    addLine(">");
	    if (folder == null) {
		add("<p>Error, canot find album " + albumRelativeFolderName + "</p>");
	    } else {
		db = new Database();
		generateAlbum();
	    }
	    add("</div>");
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

    private void generateAlbum() throws Exception {
	TimeLogger timeLogger = new TimeLogger("generateAlbum");
	albums = db.getAllAlbums(user);
	timeLogger.add("getAllAlbums");

	String albumTitle = null;
	if (folder == null) {
	    albumTitle = ("Unkown album: " + albumRelativeFolderName);
	} else {
	    albumTitle = getAlbumName(albumRelativeFolderName);
	}
	if (albumTitle == null || albumTitle.equals("")) {
	    albumTitle = "Album";
	}
	String relativeFolderName = getParameter("album");
	if (relativeFolderName == null || relativeFolderName.equals("")) {
	    relativeFolderName = Environment.INSTANCE.getRelativeRootAlbums();
	}

	List<Album> albumHierarchy = db.getAlbumAndItsParents(relativeFolderName);
	timeLogger.add("albumHierarchy");

	Query query = new Query(user);
	query.setRelativeFolderName(this.albumRelativeFolderName);
	query.setIncludeSubFolders(false);
	query.setOrderBy(SORTING);
	if (user.isAdmin()) {
	    query.setIncludeHidden(true);
	} else {
	    query.setIncludeHidden(false);
	}
	List<Photo> photos = db.query(query);

	timeLogger.add("dbQuery photos");

	String json = JSONUtilsFactory.getInstance().toJSON(photos);
	pageContext.setAttribute("photos-on-page", json);
	pageContext.setAttribute("album-title", albumTitle);

	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("Album title: " + albumTitle);
	    LOGGER.trace("Photos on page: " + json);
	}

	Photo cover = null;
	Album album = db.getAlbum(albumRelativeFolderName);
	if (albumRelativeFolderName != null) {
	    cover = album.getCoverPhoto();
	}

	if (cover != null) {
	    generateCoverPhoto(cover, SORTING, MediaType.ALL);
	    add("<br style=\"clear:both\" />");
	}
	addAlbumTitle(albumHierarchy);
	timeLogger.add("addAlbumTitle");

	List<String> childFoldersFileNames = folder.getFolderNames();
	timeLogger.add("childFoldersFileNames");

	List<Album> childAlbums = new ArrayList<Album>();
	for (String childFolderName : childFoldersFileNames) {
	    Album foundAlbum = null;
	    Iterator<Album> iterator = albums.iterator();
	    while (iterator.hasNext() && foundAlbum == null) {
		Album current = iterator.next();
		if (current.getRelativeFolderName().equals(childFolderName)) {
		    foundAlbum = current;
		}
	    }
	    if (foundAlbum != null) {
		childAlbums.add(foundAlbum);
	    } else {
		LOGGER.warn("Cannot find album for relativeFolder: " + childFolderName);
	    }
	}

	generateChildFolders(childAlbums);
	timeLogger.add("generateChildFolders");

	generateHtmlListItems(photos, albumRelativeFolderName, OrderBy.DATE_TAKEN_OLDEST);
	timeLogger.add("generateHtmlListItems");

	addLine("");
	timeLogger.add("generateAlbum complete");
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(timeLogger.toString());
	}
    }

    // error with http://localhost:8080/photos/albums.jsp?album=/Albums/2013

    private void generateChildFolders(List<Album> childAlbums) throws JspException, Exception {
	if (!childAlbums.isEmpty()) {
	    addComment("Child albums start");
	    addLine("<ul>");
	}
	for (int i = 0; i < childAlbums.size(); i++) {
	    Album album = childAlbums.get(i);
	    Photo cover = album.getCoverPhoto();
	    String image;
	    String sizeHtml;
	    if (cover != null) {
		image = "img?id=" + album.getCoverPhoto().getId();
		sizeHtml = getSizeHtml(cover.getThumbWidth(), cover.getThumbHeight());
	    } else {
		image = ALBUM_WITHOUT_COVER;
		sizeHtml = getSizeHtml(ALBUM_WITHOUT_COVER_WIDTH, ALBUM_WITHOUT_COVER_HEIGHT);
	    }

	    String link = "albums.jsp?" + ALBUM_PARAMETER + "=" + album.getRelativeFolderName();
	    addLine("  <li>");
	    addLine("    <a href=\"" + link + "\">");
	    StringBuilder b = new StringBuilder();
	    b.append("      <img class=\"image-in-album\" src=\"").append(image);
	    if (cover != null) {
		b.append("&size=small\"");
	    } else {
		b.append("\"");
	    }
	    String loading;
	    if (i > LOAD_LAZY_AFTER_IMAGE) {
		loading = " loading='lazy'";
	    } else {
		loading = " loading='eager'";
	    }
	    b.append(sizeHtml).append(loading).append(">");
	    addLine(b.toString());
	    String subFolderName = album.getName();
	    addLine("      <div class=\"photo-title-container\"><span class=\"photo-title\">" + subFolderName + "</span></div>");
	    addLine("    </a>");
	    addLine("  </li>");
	}
	if (!childAlbums.isEmpty()) {
	    addLine("  <li></li>");
	    addLine("</ul>");
	    addComment("Child albums end");
	}
    }

    private String getAlbumName(String relativeFolderName) {
	Iterator<Album> iterator = albums.iterator();
	Album found = null;
	while (iterator.hasNext() && found == null) {
	    Album current = iterator.next();
	    if (current.getRelativeFolderName().equals(relativeFolderName)) {
		found = current;
	    }
	}
	String albumName;
	if (found != null) {
	    albumName = found.getName();
	} else {
	    albumName = StringUtils.findAfterLast(relativeFolderName, "/");
	}
	return albumName;
    }

    private void addAlbumTitle(List<Album> albums) throws Exception {
	add("<p>");
	addLine(BACK_BUTTON);
	int size = albums.size();
	for (int i = 0; i < size; i++) {
	    boolean last = i == (size - 1);
	    Album album = albums.get(i);
	    add("<span class=\"album-title\">");
	    StringBuilder link = new StringBuilder();
	    if (!last) {
		String folderName = StringUtils.encodeURL(album.getRelativeFolderName());
		link.append("<a href=\"albums.jsp?album=").append(folderName).append("\">");
		add(link);
	    }
	    add(album.getName());
	    if (!last) {
		add("</a>");
		add("&nbsp;/&nbsp;");
	    }
	    addLine("</span>");
	}
	addLine("</p>");
    }

    private void generateCoverPhoto(Photo cover, OrderBy orderBy, MediaType mediaType) throws Exception {
	StringBuilder link = new StringBuilder();

	link.append("<a href=\"photo.jsp");
	if (cover != null) {
	    link.append("?photo=").append(cover.getId()).append("&");
	} else {
	    link.append("?");
	}
	link.append("sort=").append(orderBy).append("&mediatype=").append(mediaType).append("\">");
	addLine(link.toString());
	StringBuilder img = new StringBuilder();
	img.append("<img class=\"image-cover-album\" src=\"");
	if (cover != null) {
	    img.append("img?id=").append(cover.getId()).append("&size=small\">");
	} else {
	    img.append(ALBUM_WITHOUT_COVER).append(">");
	}
	addLine(img.toString());
	addLine("   </a>");
	addLine("   <div id=\"map-album\"></div>");
    }

    private String getDurationLabel(Photo photo) {
	int totalSecs = photo.getDuration();
	int hours = totalSecs / 3600;
	int minutes = (totalSecs % 3600) / 60;
	int seconds = totalSecs % 60;
	return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void generateHtmlListItems(List<Photo> photos, String album, OrderBy orderBy)
	    throws JspException {
	addComment("generateHtmlListItems");
	addLine("  <ul>");
	for (int i = 0; i < photos.size(); i++) {
	    Photo photo = photos.get(i);
	    StringBuilder link = new StringBuilder();
	    link.append("<a href=\"photo.jsp?photo=").append(photo.getId());
	    link.append("&album=").append(StringUtils.encodeURL(album));
	    if (orderBy != null) {
		link.append("&sort=");
		if (orderBy == OrderBy.DATE_TAKEN_NEWEST) {
		    link.append("newestfirst");
		} else {
		    link.append("oldestfirst");
		}
	    }
	    link.append("\">");
	    addLine("  <li>");
	    addLine("    " + link.toString());
	    String thumbImage;
	    switch (photo.getType()) {
		case PHOTO: {
		    thumbImage = photo.getId();
		    break;
		}
		case VIDEO: {
		    thumbImage = photo.getId();
		    break;
		}
		default: {
		    throw new JspException("Unsupported media type " + photo.getType());
		}
	    }
	    StringBuilder img = new StringBuilder("<img class=\"image-in-album\" src=\"img?id=");
	    img.append(thumbImage).append("&size=small\"");
	    img.append(getSizeHtml(photo.getThumbWidth(), photo.getThumbHeight()));
	    if (i > LOAD_LAZY_AFTER_IMAGE) {
		img.append(" loading='lazy'");
	    } else {
		img.append(" loading='eager'");
	    }
	    img.append(">");
	    addLine(img);
	    if (photo.getType() == MediaType.VIDEO) {
		String icon = PLAY_ICON;
		addLine("        <span class=\"video-icon\">" + icon + "</span>");
		addLine("        <span class=\"video-time-label\">" + getDurationLabel(photo) + "</span>");
	    }
	    addLine("      </a>");
	    addLine("  </li>");
	}
	addLine("    <li></li>");
	addLine("  </ul>");
    }

}
