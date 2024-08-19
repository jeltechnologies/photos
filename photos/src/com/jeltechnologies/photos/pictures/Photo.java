package com.jeltechnologies.photos.pictures;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import com.jeltechnologies.geoservices.datamodel.Address;
import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.geoservices.datamodel.Distance;
import com.jeltechnologies.photos.exiftool.MetaData;

public class Photo implements Serializable {
    private static final long serialVersionUID = -6797925421264802006L;
    private String id;
    private MediaType type;
    private String fileName;
    private String relativeFileName;
    private String relativeFolderName;
    private String label;
    private LocalDateTime dateTaken;
    private int thumbWidth;
    private int thumbHeight;
    private int duration;
    private int orientation;
    private String source;
    private boolean hidden;
    private boolean livePhoto;
    private Coordinates coordinates;
    private Address address;
    private Distance distanceFromAddress;
    private MetaData metaData;

    public Photo() {
    }

    public Photo(String id, MediaType type) {
	this.id = id;
	this.type = type;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getFileName() {
	return fileName;
    }

    public void setFileName(String fileName) {
	this.fileName = fileName;
    }

    public String getRelativeFileName() {
	return relativeFileName;
    }

    public void setRelativeFileName(String relativeFileName) {
	this.relativeFileName = relativeFileName;
    }

    public String getRelativeFolderName() {
	return relativeFolderName;
    }

    public void setRelativeFolderName(String relativeFolderName) {
	this.relativeFolderName = relativeFolderName;
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public LocalDateTime getDateTaken() {
	return dateTaken;
    }

    public void setDateTaken(LocalDateTime dateTaken) {
	this.dateTaken = dateTaken;
    }

    public int getThumbWidth() {
	return thumbWidth;
    }

    public void setThumbWidth(int thumbWidth) {
	this.thumbWidth = thumbWidth;
    }

    public int getThumbHeight() {
	return thumbHeight;
    }

    public void setThumbHeight(int thumbHeight) {
	this.thumbHeight = thumbHeight;
    }

    public int getDuration() {
	return duration;
    }

    public void setDuration(int duration) {
	this.duration = duration;
    }

    public int getOrientation() {
	return orientation;
    }

    public void setOrientation(int orientation) {
	this.orientation = orientation;
    }

    public String getSource() {
	return source;
    }

    public void setSource(String source) {
	this.source = source;
    }

    public boolean isHidden() {
	return hidden;
    }

    public void setHidden(boolean hidden) {
	this.hidden = hidden;
    }

    public boolean isLivePhoto() {
	return livePhoto;
    }

    public void setLivePhoto(boolean livePhoto) {
	this.livePhoto = livePhoto;
    }

    public Coordinates getCoordinates() {
	return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
	this.coordinates = coordinates;
    }

    public Address getAddress() {
	return address;
    }

    public void setAddress(Address address) {
	this.address = address;
    }

    public Distance getDistanceFromAddress() {
	return distanceFromAddress;
    }

    public void setDistanceFromAddress(Distance distanceFromAddress) {
	this.distanceFromAddress = distanceFromAddress;
    }

    public String getId() {
	return id;
    }

    public MediaType getType() {
	return type;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public String toString() {
	return "Photo [id=" + id + ", type=" + type + ", fileName=" + fileName + ", relativeFileName=" + relativeFileName + ", relativeFolderName="
		+ relativeFolderName + ", label=" + label + ", dateTaken=" + dateTaken + ", thumbWidth=" + thumbWidth + ", thumbHeight=" + thumbHeight
		+ ", duration=" + duration + ", orientation=" + orientation + ", source=" + source + ", hidden=" + hidden + ", livePhoto=" + livePhoto
		+ ", coordinates=" + coordinates + ", address=" + address + ", distanceFromAddress=" + distanceFromAddress + "]";
    }

    @Override
    public int hashCode() {
	return Objects.hash(address, coordinates, dateTaken, distanceFromAddress, duration, fileName, hidden, id, label, livePhoto, orientation,
		relativeFileName, relativeFolderName, source, thumbHeight, thumbWidth, type);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Photo other = (Photo) obj;
	return Objects.equals(address, other.address) && Objects.equals(coordinates, other.coordinates) && Objects.equals(dateTaken, other.dateTaken)
		&& Objects.equals(distanceFromAddress, other.distanceFromAddress) && duration == other.duration && Objects.equals(fileName, other.fileName)
		&& hidden == other.hidden && Objects.equals(id, other.id) && Objects.equals(label, other.label) && livePhoto == other.livePhoto
		&& orientation == other.orientation && Objects.equals(relativeFileName, other.relativeFileName)
		&& Objects.equals(relativeFolderName, other.relativeFolderName) && Objects.equals(source, other.source) && thumbHeight == other.thumbHeight
		&& thumbWidth == other.thumbWidth && type == other.type && Objects.equals(metaData, other.metaData);
    }

    /**
     * Compare two photos to check if any metadata has changed. This ignores the file and folder information.
     * 
     * @param obj
     * @return
     */
    public boolean hasSameMetaData(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Photo other = (Photo) obj;
	return Objects.equals(address, other.address)
		&& Objects.equals(coordinates, other.coordinates)
		&& Objects.equals(dateTaken, other.dateTaken)
		&& Objects.equals(distanceFromAddress, other.distanceFromAddress)
		&& Objects.equals(metaData, other.metaData)
		&& duration == other.duration
		&& hidden == other.hidden
		&& Objects.equals(id, other.id)
		&& Objects.equals(label, other.label)
		&& livePhoto == other.livePhoto
		&& orientation == other.orientation
		&& Objects.equals(source, other.source)
		&& thumbHeight == other.thumbHeight
		&& thumbWidth == other.thumbWidth && type == other.type;
    }
}
