package com.jeltechnologies.photos.servlet;

import java.io.Serializable;

import com.jeltechnologies.geoservices.datamodel.Coordinates;

public class PhotoPayload implements Serializable {
    private static final long serialVersionUID = -7555778099659745112L;
    private String type;
    private String name;
    private String relativeFileName;
    private String albumName;
    private String date;
    private String source;
    private Coordinates coordinates;
    private int orientation;
    private String rotation;
    private int durationSeconds = -1;

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

    public String getType() {
	return type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getRelativeFileName() {
	return relativeFileName;
    }

    public void setRelativeFileName(String relativeFileName) {
	this.relativeFileName = relativeFileName;
    }

    public String getAlbumName() {
	return albumName;
    }

    public void setAlbumName(String albumName) {
	this.albumName = albumName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Coordinates getCoordinate() {
	return coordinates;
    }

    public void setCoordinate(Coordinates coordinates) {
	this.coordinates = coordinates;
    }

    public int getDurationSeconds() {
	return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
	this.durationSeconds = durationSeconds;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("PhotoPayload [type=");
	builder.append(type);
	builder.append(", name=");
	builder.append(name);
	builder.append(", relativeFileName=");
	builder.append(relativeFileName);
	builder.append(", albumName=");
	builder.append(albumName);
	builder.append(", date=");
	builder.append(date);
	builder.append(", coordinate=");
	builder.append(coordinates);
	builder.append(", orientation=");
	builder.append(orientation);
	builder.append(", rotation=");
	builder.append(rotation);
	builder.append(", durationSeconds=");
	builder.append(durationSeconds);
	builder.append("]");
	return builder.toString();
    }
}
