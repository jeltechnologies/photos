package com.jeltechnologies.photos.utils;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javaxt.io.Image;

/**
 * Get relevant JPEG tag like location, dimension, date, camera etc.
 * 
 * @See https://www.javaxt.com/javaxt-core/io/Image
 */
public class JPEGExifParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JPEGExifParser.class);

    private final Image image;

    private final File file;

    private final HashMap<Integer, Object> exif;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    public JPEGExifParser(File file) {
	this.file = file;
	image = new Image(file);
	exif = image.getExifTags();
    }

    private LocalDateTime exifDate(Object key) {
	Object value = exif.get(key);
	LocalDateTime date;
	if (value == null) {
	    date = null;
	} else {
	    try {
		date = LocalDateTime.parse(StringUtils.stripNonAscii(value.toString()), dateFormatter);
	    } catch (Exception e) {
		LOGGER.warn("Cannot parse date: " + value + " from file " + file);
		date = null;
	    }
	}
	return date;
    }

    private String exifString(Object key) {
	Object value = exif.get(key);
	if (value == null) {
	    return null;
	} else {
	    return value.toString();
	}
    }

    private Integer exifInt(Object key) { 
	Object value = exif.get(key);
	if (value == null) {
	    return null;
	} else {
	    try {
		return (Integer) value;
	    } catch (Exception e) {
		LOGGER.warn("Cannot convert " + value + " to int from file " + file);
		return null;
	    }
	}
    }

    public LocalDateTime getDateTaken() {
	LocalDateTime taken = exifDate(0x9003);
	if (taken == null) {
	    taken = exifDate(0x0132);
	}
	LocalDateTime result;
	if (taken == null) {
	    result = null;
	}
	else {
	    result = taken;
	}
	return result;
    }

    public String getManufacturer() {
	return exifString(0x010F);
    }

    public String getCamera() {
	return exifString(0x0110);
    }

    public String getOrientationDescription() {
	// Print Image Orientation
	String desc = null;
	int orientation = exifInt(0x0112);
	switch (orientation) {
	    case 1:
		desc = "Top, left side (Horizontal / normal)";
		break;
	    case 2:
		desc = "Top, right side (Mirror horizontal)";
		break;
	    case 3:
		desc = "Bottom, right side (Rotate 180)";
		break;
	    case 4:
		desc = "Bottom, left side (Mirror vertical)";
		break;
	    case 5:
		desc = "Left side, top (Mirror horizontal and rotate 270 CW)";
		break;
	    case 6:
		desc = "Right side, top (Rotate 90 CW)";
		break;
	    case 7:
		desc = "Right side, bottom (Mirror horizontal and rotate 90 CW)";
		break;
	    case 8:
		desc = "Left side, bottom (Rotate 270 CW)";
		break;
	}
	return desc;
    }

    public int getOrientation() {
	Integer orientation = exifInt(0x0112);
	if (orientation == null) {
	    return 0;
	} else {
	    return orientation;
	}
    }

    public BigDecimal getLatitude() {
	BigDecimal bd = null;
	double[] coord = image.getGPSCoordinate();
	if (coord != null && coord.length > 1) {
	    bd = new BigDecimal(coord[1]);
	}
	return bd;
    }

    public BigDecimal getLongitude() {
	BigDecimal bd = null;
	double[] coord = image.getGPSCoordinate();
	if (coord != null && coord.length > 1) {
	    bd = new BigDecimal(coord[0]);
	}
	return bd;
    }

    public int getWidth() {
	return image.getWidth();
    }

    public int getHeight() {
	return image.getHeight();
    }

    public void demo() {
	// https://www.javaxt.com/javaxt-core/io/Image

	// Open Image and Get EXIF Metadata
	java.util.HashMap<Integer, Object> exif = image.getExifTags();

	// Print Camera Info
	System.out.println("EXIF Fields: " + exif.size());
	System.out.println("-----------------------------");
	System.out.println("Date: " + exif.get(0x0132)); // 0x9003
	System.out.println("Date2: " + exif.get(0x9003)); // 0x9003
	System.out.println("Camera: " + exif.get(0x0110));
	System.out.println("Manufacturer: " + exif.get(0x010F));
	System.out.println("Focal Length: " + exif.get(0x920A));
	System.out.println("F-Stop: " + exif.get(0x829D));
	System.out.println("Exposure Time (1 / Shutter Speed): " + exif.get(0x829A));
	System.out.println("ISO Speed Ratings: " + exif.get(0x8827));
	System.out.println("Shutter Speed Value (APEX): " + exif.get(0x9201));
	System.out.println("Shutter Speed (Exposure Time): " + exif.get(0x9201));
	System.out.println("Aperture Value (APEX): " + exif.get(0x9202));

	Object orientationObject = exif.get(0x0112);
	if (orientationObject != null) {
	    // Print Image Orientation
	    int orientation = -1;
	    try {
		orientation = (Integer) orientationObject;
		String desc = "";
		switch (orientation) {
		    case 1:
			desc = "Top, left side (Horizontal / normal)";
			break;
		    case 2:
			desc = "Top, right side (Mirror horizontal)";
			break;
		    case 3:
			desc = "Bottom, right side (Rotate 180)";
			break;
		    case 4:
			desc = "Bottom, left side (Mirror vertical)";
			break;
		    case 5:
			desc = "Left side, top (Mirror horizontal and rotate 270 CW)";
			break;
		    case 6:
			desc = "Right side, top (Rotate 90 CW)";
			break;
		    case 7:
			desc = "Right side, bottom (Mirror horizontal and rotate 90 CW)";
			break;
		    case 8:
			desc = "Left side, bottom (Rotate 270 CW)";
			break;
		}
		System.out.println("Orientation: " + orientation + " -- " + desc);
	    } catch (Exception e) {
		LOGGER.warn("Unsupoprted orientation " + orientation + " on photo ");
	    }
	}

	// Print GPS Information
	double[] coord = image.getGPSCoordinate();
	if (coord != null) {
	    System.out.println("GPS Coordinate: " + coord[0] + ", " + coord[1]);
	    System.out.println("GPS Datum: " + image.getGPSDatum());
	}

	// Open Image and Get IPTC Metadata
	java.util.HashMap<Integer, Object> iptc = image.getIptcTags();

	// Print Selected Fields
	System.out.println("IPTC Fields: " + iptc.size());
	System.out.println("-----------------------------");
	System.out.println("Date: " + iptc.get(0x0237));
	System.out.println("Caption: " + iptc.get(0x0278));
	System.out.println("Copyright: " + iptc.get(0x0274));

    }

//    public static void main(String[] args) throws Exception {
//	File file2 = new File("F:\\photos\\Albums\\2022\\Familiedag 6 juni 2022\\fam-dag 2022 1090.JPG");
//	
//	String test2 = "F:\\photos\\Albums\\2022\\2022-05\\IMG_1606.jpg";
//	String test = "F:\\tmp\\doke.JPG";
//	File file = new File(test);
//	
//	
//	
//	JPEGExifParser parser = new JPEGExifParser(file);
//	parser.demo();
//	System.out.println(parser.getDateTaken());
//    }

}
