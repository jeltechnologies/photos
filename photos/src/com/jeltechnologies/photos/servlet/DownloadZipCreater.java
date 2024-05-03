package com.jeltechnologies.photos.servlet;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.datatypes.MovieQuality;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.pictures.ThumbnailUtils;
import com.jeltechnologies.photos.pictures.ThumbnailUtilsFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DownloadZipCreater {

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private final HttpServletResponse response;

    public DownloadZipCreater(HttpServletRequest request, HttpServletResponse response) {
	this.response = response;
    }

    public void respondZippedFolder(List<Photo> photos, String fileName, DownloadServlet.Source source) throws ServletException, IOException, SQLException {
	ServletOutputStream sos = null;
	try {
	    // https://kodejava.org/how-do-i-create-zip-file-in-servlet-for-download/
	    // Checks to see if the directory contains some files.
	    // Call the zipFiles method for creating a zip stream.
	    byte[] zip = zipPhotos(photos, source);

	    /*
	     * Sends the response back to the user / browser. The content for zip file type is "application/zip". We also set the
	     * content disposition as attachment for the browser to show a dialog that will let user choose what action will he do
	     * to the content.
	     */
	    sos = response.getOutputStream();
	    response.setContentType("application/zip");
	    response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
	    sos.write(zip);
	    sos.flush();
	} finally {
	    if (sos != null) {
		sos.flush();
	    }
	}
    }

    private byte[] zipPhotos(List<Photo> photos, DownloadServlet.Source source) throws IOException {
	Environment ENV = Environment.INSTANCE;
	Dimension dimension = ENV.getDimensionOriginal();
	MovieQuality movieQuality = new MovieQuality(MovieQuality.Type.HIGH);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	ZipOutputStream zos = null;
	try {
	    zos = new ZipOutputStream(baos);
	    zos.setLevel(ZipOutputStream.STORED);
	    byte[] bytes = new byte[2048];
	    ThumbnailUtils utils = ThumbnailUtilsFactory.getUtils();
	    for (Photo photo : photos) {
		File file;
		switch (source) {
		    case ORIGINAL: {
			file = ENV.getFile(photo.getRelativeFileName());
			break;
		    }
		    case CACHE: {
			switch (photo.getType()) {
			    case PHOTO: {
				file = utils.getThumbFile(dimension, photo);
				break;
			    } case VIDEO: {
				file = utils.getConvertedMovie(movieQuality, photo);
				break;
			    }
			    default: {
				throw new IllegalStateException("Unsupported phototype: " + photo.getType());
			    }
			}
			break;
		    }
		    default: {
			throw new IllegalStateException("Unsupported type: " + source);
		    }
		}
		if (file.isFile()) {
		    try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {
			zos.putNextEntry(new ZipEntry(file.getName()));
			int bytesRead;
			while ((bytesRead = bis.read(bytes)) != -1) {
			    zos.write(bytes, 0, bytesRead);
			}
			zos.closeEntry();
		    }
		}
	    }
	} finally {
	    if (zos != null) {
		zos.close();
	    }
	}
	return baos.toByteArray();

    }
}
