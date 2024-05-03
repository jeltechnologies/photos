package com.jeltechnologies.photos.servlet;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.datatypes.MovieQuality;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.pictures.ThumbnailUtils;
import com.jeltechnologies.photos.pictures.ThumbnailUtilsFactory;
import com.jeltechnologies.photos.utils.StringUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/download/*")
public class DownloadServlet extends BaseServlet {
    private static final String SIZE = "-size-";

    private static final long serialVersionUID = -5786621203180698473L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadServlet.class);

    public static final Environment ENV = Environment.INSTANCE;

    private Dimension dimension;

    private Source source;

    public enum Source {
	ORIGINAL, CACHE
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String requestURI = request.getRequestURI();
	String downloadArtifact = StringUtils.findAfterIfNotFoundReturnIn(requestURI, "/download/");
	downloadArtifact = StringUtils.decodeURL(downloadArtifact);
	String sourceParam = request.getParameter("source");
	LOGGER.trace("Source: " + sourceParam);
	if (sourceParam != null && sourceParam.toLowerCase().equals("original")) {
	    source = Source.ORIGINAL;
	} else {
	    source = Source.CACHE;
	}

	dimension = null;
	String fileExtension = StringUtils.findAfterLast(downloadArtifact, ".");
	String dimensionInFileName = StringUtils.findBetween(downloadArtifact, SIZE, ".");
	LOGGER.trace("dimensionInFileName: " + dimensionInFileName);
	if (dimensionInFileName != null && !dimensionInFileName.equals("")) {
	    downloadArtifact = StringUtils.stripAfter(downloadArtifact, SIZE) + "." + fileExtension;
	}
	dimension = ImageServlet.getDimension(dimensionInFileName);
	if (dimension == null) {
	    dimension = ImageServlet.getDimension(request);
	}

	boolean isAlbum = downloadArtifact.toLowerCase().endsWith(".zip");
	if (isAlbum) {
	    String relativeFolderName = StringUtils.stripAfterLast(downloadArtifact, ".");
	    String fileName = StringUtils.replaceAll(relativeFolderName, '/', '_') + ".zip";
	    respondAlbumAsZipFile(request, response, "/" + relativeFolderName, fileName);
	} else {
	    String fileWithQuality = StringUtils.stripAfterLast(downloadArtifact, ".");
	    String fileName = StringUtils.stripAfter(fileWithQuality, "-");
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("Artifact: " + downloadArtifact + ", extension: " + fileExtension + ", fileName: " + fileName + ", isAlbum: " + isAlbum);
	    }
	    respondSingleFile(request, response, fileName);
	}
    }

    private void respondSingleFile(HttpServletRequest request, HttpServletResponse response, String fileName) throws ServletException {
	Database database = null;
	try {
	    database = new Database();
	    Query query = new Query(RoleModel.getUser(request));
	    String id = fileName;
	    query.setChecksum(id);
	    Photo photo = null;
	    List<Photo> photos = database.query(query);
	    if (photos.size() > 0) {
		photo = photos.get(0);
	    }
	    ThumbnailUtils utils = ThumbnailUtilsFactory.getUtils();
	    File photoFile;
	    switch (photo.getType()) {
		case PHOTO:
		    switch (source) {
			case CACHE:
			    photoFile = utils.getThumbFile(dimension, photo);
			    break;
			case ORIGINAL:
			    photoFile = ENV.getFile(photo.getRelativeFileName());
			    break;
			default:
			    throw new IllegalStateException("Uknown source: " + source);
		    }
		    break;
		case VIDEO:
		    switch (source) {
			case CACHE:
			    MovieQuality quality;
			    switch (dimension.getType()) {
				case MEDIUM:
				    quality = new MovieQuality(MovieQuality.Type.LOW);
				    break;
				case ORIGINAL:
				    quality = new MovieQuality(MovieQuality.Type.HIGH);
				    break;
				case SMALL:
				    quality = new MovieQuality(MovieQuality.Type.LOW);
				    break;
				default:
				    throw new IllegalStateException("Unknown dimension: " + dimension.getType());
			    }
			    photoFile = utils.getConvertedMovie(quality, photo);
			    break;
			case ORIGINAL:
			    photoFile = ENV.getFile(photo.getRelativeFileName());
			    break;
			default:
			    throw new IllegalStateException("Unknown source: " + source);
		    }
		    break;
		default:
		    throw new IllegalStateException("Unknown type " + photo.getType());
	    }

	    if (photoFile != null && photoFile.isFile()) {
		int status = respondBinaryFile(response, request.getServletContext(), photoFile, true);
		response.setStatus(status);

	    } else {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    }
	    LOGGER.debug("Download: " + photoFile + " => " + response.getStatus());

	} catch (Exception e) {
	    LOGGER.error("Error in downloadServlet: " + e.getMessage(), e);
	    throw new ServletException("Error in downloadServlet: " + e.getMessage());
	} finally {
	    if (database != null) {
		database.close();
	    }
	}
    }

    private void respondAlbumAsZipFile(HttpServletRequest request, HttpServletResponse response, String relativeFolderName, String fileName)
	    throws ServletException, IOException {
	LOGGER.debug(fileName + " => " + relativeFolderName);
	Database database = null;
	try {
	    Query query = new Query(RoleModel.getUser(request));
	    query.setRelativeFolderName(relativeFolderName);
	    database = new Database();
	    List<Photo> photos = database.query(query);
	    new DownloadZipCreater(request, response).respondZippedFolder(photos, fileName, source);
	} catch (SQLException e) {
	    throw new ServletException(e.getMessage(), e);
	} finally {
	    if (database != null) {
		database.close();
	    }
	}
    }

}
