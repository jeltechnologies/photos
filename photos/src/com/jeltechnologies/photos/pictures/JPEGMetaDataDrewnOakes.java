package com.jeltechnologies.photos.pictures;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.jeltechnologies.geoservices.datamodel.Coordinates;

public class JPEGMetaDataDrewnOakes implements PhotoJPEGDataUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(JPEGMetaDataDrewnOakes.class);
    private final File file;

    private List<DirectoryInfo> directories = new ArrayList<DirectoryInfo>();

    private Metadata metadata;

    public JPEGMetaDataDrewnOakes(File file) throws IOException {
	this.file = file;
	try {
	    this.metadata = ImageMetadataReader.readMetadata(file);
	    parseAllMetaData();
	} catch (ImageProcessingException e) {
	    throw new IOException("Error parsing jpg meta info: " + e.getMessage(), e);
	}
    }

    public LocalDateTime getDateTaken() {
	LocalDateTime dateTaken = null;
	try {
	    ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
	    if (directory != null) {
		Date utilDate = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
		if (utilDate != null) {
		    dateTaken = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		}
	    }
	} catch (Exception e) {
	    LOGGER.warn("Cannot get DateTaken from file " + file.getAbsolutePath());
	}
	return dateTaken;
    }

    @Override
    public void addMetaData(Photo photo) {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Parsing photo information of " + photo.getRelativeFileName());
	}

	try {
	    ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
	    Date dateTaken = null;
	    if (directory != null) {
		dateTaken = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
		if (dateTaken != null) {
		    LocalDateTime localDateTime = dateTaken.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		    photo.setDateTaken(localDateTime);
		}
	    }

	    GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
	    if (gpsDirectory != null) {
		GeoLocation location = gpsDirectory.getGeoLocation();
		if (location != null) {
		    Coordinates coordinates = new Coordinates(location.getLatitude(), location.getLongitude());
		    photo.setCoordinates(coordinates);
		}
	    }

	    JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);

	    // HIER ZIT DE FOUT
	    int width = jpegDirectory.getImageWidth();
	    int height = jpegDirectory.getImageHeight();

	    // LOGGER.info("Width: " + width + ", Height: " + height);

	    photo.setThumbHeight(height);
	    photo.setThumbWidth(width);

	    photo.setOrientation(getOrientation());

	} catch (Exception ex) {
	    LOGGER.error("Crawler get EXIF for photo " + file.getAbsolutePath(), ex);
	}
    }

    // https://drewnoakes.com/code/exif/
    private void parseAllMetaData() throws ImageProcessingException, IOException {
	Metadata metadata = ImageMetadataReader.readMetadata(file);

	for (Directory directory : metadata.getDirectories()) {
	    DirectoryInfo info = new DirectoryInfo(directory.getName());
	    directories.add(info);

	    for (Tag tag : directory.getTags()) {
		info.addTag(tag.getTagName(), tag.getDescription());
	    }
	    if (directory.hasErrors()) {
		StringBuilder b = new StringBuilder();
		for (String error : directory.getErrors()) {
		    if (b.length() > 0) {
			b.append(", ");
		    }
		    b.append(error);
		}
		LOGGER.warn("Errors parsing jpg meta info for " + file.getName() + " : " + b.toString());
	    }
	}
    }

    public String getTag(String name) {
	String found = null;
	Iterator<DirectoryInfo> iterator = this.directories.iterator();
	while (iterator.hasNext() && found == null) {
	    DirectoryInfo info = iterator.next();
	    found = info.getTag(name);
	}
	return found;
    }

    public String getOrientationDescription() {
	return getTag("Orientation");
    }

    public int getOrientation() {
	// "Right side, top (Rotate 90 CW)"
	// "Bottom, right side (Rotate 180)"
	// "Top, left side (Horizontal / normal)"
	// "Left side, bottom (Rotate 270 CW)"
	// Unknown (0)
	String descr = getOrientationDescription();
	int o = 0;
	if (descr != null) {
	    if (descr.equalsIgnoreCase("Right side, top (Rotate 90 CW)")) {
		o = 90;
	    } else {
		if (descr.equalsIgnoreCase("Bottom, right side (Rotate 180)")) {
		    o = 180;
		} else {
		    if (descr.equalsIgnoreCase("Top, left side (Horizontal / normal)")) {
			o = 0;
		    } else {
			if (descr.equalsIgnoreCase("Left side, bottom (Rotate 270 CW)")) {
			    o = 270;
			} else {
			    if (descr.equalsIgnoreCase("Unknown (0)")) {
				o = 0;
			    } else {
				LOGGER.warn(file.getAbsolutePath() + " has unknown orientation: " + descr);
			    }
			}
		    }
		}
	    }

	}
	return o;
    }

    private class DirectoryInfo {
	private final String name;
	private Map<String, String> tags = new HashMap<String, String>();

	public DirectoryInfo(String name) {
	    this.name = name;
	}

	@SuppressWarnings("unused")
	public String getName() {
	    return name;
	}

	public void addTag(String name, String value) {
	    tags.put(name.toLowerCase(), value);
	}

	public String getTag(String name) {
	    return tags.get(name.toLowerCase());
	}

	@Override
	public String toString() {
	    StringBuilder builder = new StringBuilder();
	    builder.append("DirectoryInfo [name=");
	    builder.append(name);
	    builder.append(", tags=");
	    builder.append(tags);
	    builder.append("]");
	    return builder.toString();
	}
    }

}