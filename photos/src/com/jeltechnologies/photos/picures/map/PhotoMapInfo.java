package com.jeltechnologies.photos.picures.map;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.utils.StringUtils;

public class PhotoMapInfo implements Serializable {
    private static final long serialVersionUID = 1021018471619621130L;
    private BigDecimal lat;
    private BigDecimal lng;
    private String date;
    private String html;

    private static final Dimension THUMB_SIZE = Environment.INSTANCE.getDimensionThumbs();

    private static final String SIZE = "?width=" + THUMB_SIZE.getWidth() + "&height=" + THUMB_SIZE.getHeight();

    public PhotoMapInfo() {
    }

    public PhotoMapInfo(Photo photo, DateTimeFormatter format) {
	super();
	String url = photo.getRelativeFileName();
	
	Coordinates coordinates = photo.getCoordinates();
	if (coordinates != null) {
	    this.lat = new BigDecimal(coordinates.latitude());
	    this.lng = new BigDecimal(coordinates.longitude());
	}
	
	this.date = format.format(photo.getDateTaken());
	
	StringBuilder b = new StringBuilder();
	
	b.append("<a href=\"photo.jsp?photo=");
	b.append(StringUtils.encodeURL(photo.getRelativeFileName()));
	b.append("&album=");
	b.append(StringUtils.encodeURL(photo.getRelativeFolderName()));
	b.append("\">");
	b.append("<img src=\"img").append(url).append(SIZE).append("\">");
	b.append("</a>");
	b.append("<p>").append(date).append("</p>");
	html = b.toString();
    }

    public BigDecimal getLat() {
	return lat;
    }

    public void setLat(BigDecimal lat) {
	this.lat = lat;
    }

    public BigDecimal getLng() {
	return lng;
    }

    public String getDate() {
	return date;
    }

    public String getHtml() {
	return html;
    }

}
