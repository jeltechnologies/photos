package com.jeltechnologies.photos.pictures;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.photos.utils.JPEGExifParser;
import com.jeltechnologies.photos.utils.StringUtils;

public class JPEGMetaDataJavaXT implements PhotoJPEGDataUpdater {
    
    private final File file;
    
    public JPEGMetaDataJavaXT(File file) {
	this.file = file;
    }

    @Override
    public void addMetaData(Photo photo) {
	JPEGExifParser parser = new JPEGExifParser(file);
	photo.setThumbWidth(parser.getWidth());
	photo.setThumbHeight(parser.getHeight());
	
	BigDecimal lat = parser.getLatitude();
	BigDecimal lng = parser.getLongitude();
	
	if (lat != null && lng != null) {
	    Coordinates coordinates = new Coordinates(lat, lng);
	    photo.setCoordinates(coordinates);
	}
	photo.setOrientation(parser.getOrientation());
	LocalDateTime dateTaken = parser.getDateTaken();
	
	photo.setDateTaken(dateTaken);
	
	StringBuilder b = new StringBuilder();
	String manufacturer = StringUtils.fixFirstCharUpperCase(parser.getManufacturer());
	if (manufacturer != null) {
	    b.append(manufacturer.trim()).append(" ");
	}
	String camera = parser.getCamera();
	if (camera != null) {
	    b.append(camera);
	}
	photo.setSource(b.toString().trim());
    }
}
