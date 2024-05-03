package com.jeltechnologies.photos.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.datatypes.MovieQuality;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.pictures.ThumbnailUtilsFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/video")
public class VideoServlet extends BaseServlet {
    private static final long serialVersionUID = 4385488343386370179L;
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoServlet.class);

    public VideoServlet() {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	User user = RoleModel.getUser(request);
	String checksum = request.getParameter("id");
	String quality = request.getParameter("quality");
	try {
	    Photo movie = getMovie(user, checksum);
	    if (movie == null) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    } else {
		File file;
		MovieQuality movieQuality = new MovieQuality(quality);
		file = ThumbnailUtilsFactory.getUtils().getConvertedMovie(movieQuality, movie);
		if (LOGGER.isTraceEnabled()) {
		    LOGGER.trace("doGet quality: " + quality + ", fileName: " + checksum + " pointing to " + file.getAbsolutePath() + " eixts: " + file.exists());
		}
		if (!file.exists()) {
		    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
		    String fileName = movie.getId() + ".mp4";
		    serveVideo(request, response, file, fileName);
		}
	    }
	} 
	catch (IOException e) {
	    if (!e.getMessage().equals("An established connection was aborted by the software in your host machine")) {
		throw e;
	    }
	}
	catch (Exception e) {
	    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    LOGGER.error(e.getMessage(), e);
	}
    }

    private void serveVideo(HttpServletRequest request, HttpServletResponse response, File file, String fileName) throws FileNotFoundException, IOException {
	OutputStream output = response.getOutputStream();
	if (request.getHeader("range") != null) {

	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("Streaming, because range was requested");
	    }
	    response.setStatus(206);
	    String rangeValue = request.getHeader("range").trim().substring("bytes=".length());
	    long fileLength = file.length();
	    long start, end;
	    if (rangeValue.startsWith("-")) {
		end = fileLength - 1;
		start = fileLength - 1 - Long.parseLong(rangeValue.substring("-".length()));
	    } else {
		String[] range = rangeValue.split("-");
		start = Long.parseLong(range[0]);
		end = range.length > 1 ? Long.parseLong(range[1]) : fileLength - 1;
	    }
	    if (end > fileLength - 1) {
		end = fileLength - 1;
	    }
	    if (start <= end) {
		long contentLength = end - start + 1;
		response.setHeader("Content-Length", contentLength + "");
		response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
		response.setHeader("Content-Source", "video/mp4");
		response.setHeader("Content-Disposition", "inline; filename=" + fileName);

		response.setHeader("Accept-Ranges", "bytes");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "no-cache, no-store");
		response.setHeader("Connection", "keep-alive");
		response.setHeader("Content-Transfer-Encoding", "binary");

		RandomAccessFile raf = null;

		try {
		    raf = new RandomAccessFile(file, "r");
		    raf.seek(start);
		    output = response.getOutputStream();
		    byte[] buffer = new byte[2096];
		    int bytesRead = 0;
		    int totalRead = 0;
		    while (totalRead < contentLength) {
			bytesRead = raf.read(buffer);
			totalRead += bytesRead;
			output.write(buffer, 0, bytesRead);
		    }
		} finally {
		    if (raf != null) {
			raf.close();
		    }
		}
	    }
	} else {
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("Directly returning file");
	    }
	    try (InputStream input = new FileInputStream(file.getPath())) {
		response.setContentType("video/mp4");
		response.setHeader("Content-Disposition", "inline; filename=" + fileName);
		response.setStatus(200);
		output = response.getOutputStream();
		byte[] buffer = new byte[2096];
		int read;
		while ((read = input.read(buffer)) != -1) {
		    output.write(buffer, 0, read);
		}
		output.flush();
		output.close();
	    }
	}
    }

    private Photo getMovie(User user, String checksum) throws SQLException {
	Database db = null;
	try {
	    db = new Database();
	    Photo p = db.getFirstPhotoById(user, checksum);
	    if (p != null && p.getType() != MediaType.VIDEO) {
		LOGGER.warn("Tried to playa photo as video: " + p);
		p = null;
	    }
	    return p;
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

}