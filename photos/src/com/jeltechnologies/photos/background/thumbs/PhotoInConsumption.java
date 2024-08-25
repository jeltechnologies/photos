package com.jeltechnologies.photos.background.thumbs;

import java.time.LocalDateTime;
import java.util.Objects;

import com.jeltechnologies.geoservices.datamodel.Address;
import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.geoservices.datamodel.Distance;
import com.jeltechnologies.photos.exiftool.MetaData;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;

public class PhotoInConsumption {
    private Photo photo;
    private boolean added;
    private boolean photoChanged;
    private boolean fileChanged = false;
    
    public PhotoInConsumption () {
    }

    public PhotoInConsumption (Photo photo) {
	setPhoto(photo);
    }

    public void setPhoto(Photo photo) {
	photoChanged = false;
	this.photo = photo;
    }

    public boolean isAdded() {
	return added;
    }

    public void setAdded(boolean added) {
	this.added = added;
    }

    public boolean isPhotoChanged() {
	return photoChanged;
    }
    
    public boolean isFileChanged() {
        return fileChanged;
    }

    public void setFileChanged(boolean fileChanged) {
        this.fileChanged = fileChanged;
    }

    public Photo getPhoto() {
	return photo;
    }

    public void setId(String id) {
	if (!Objects.equals(id, photo.getId())) {
	    photoChanged = true;
	    photo.setId(id);
	}
    }

    public void setFileName(String fileName) {
	if (!Objects.equals(fileName, photo.getFileName())) {
	    fileChanged = true;
	    photo.setFileName(fileName);
	}
    }

    public void setRelativeFileName(String relativeFileName) {
	if (!Objects.equals(relativeFileName, photo.getRelativeFileName())) {
	    fileChanged = true;
	    photo.setRelativeFileName(relativeFileName);
	}
    }

    public void setRelativeFolderName(String relativeFolderName) {
	if (!Objects.equals(relativeFolderName, photo.getRelativeFolderName())) {
	    fileChanged = true;
	    photo.setRelativeFolderName(relativeFolderName);
	}
    }

    public void setLabel(String label) {
	if (!Objects.equals(label, photo.getLabel())) {
	    photoChanged = true;
	    photo.setLabel(label);
	}
    }

    public void setDateTaken(LocalDateTime dateTaken) {
	if (!Objects.equals(dateTaken, photo.getDateTaken())) {
	    photoChanged = true;
	    photo.setDateTaken(dateTaken);
	}
    }

    public void setThumbWidth(int thumbWidth) {
	if (thumbWidth != photo.getThumbWidth()) {
	    photoChanged = true;
	    photo.setThumbWidth(thumbWidth);
	}
    }

    public void setThumbHeight(int thumbHeight) {
	if (thumbHeight != photo.getThumbHeight()) {
	    photoChanged = true;
	    photo.setThumbHeight(thumbHeight);
	}
    }

    public void setDuration(int duration) {
	if (duration != photo.getDuration()) {
	    photoChanged = true;
	    photo.setDuration(duration);
	}
    }

    public void setOrientation(int orientation) {
	if (orientation != photo.getOrientation()) {
	    photoChanged = true;
	    photo.setOrientation(orientation);
	}
    }

    public void setSource(String source) {
	if (!Objects.equals(source, photo.getSource())) {
	    photoChanged = true;
	    photo.setSource(source);
	}
    }

    public void setHidden(boolean hidden) {
	if (hidden != photo.isHidden()) {
	    photoChanged = true;
	    photo.setHidden(hidden);
	}
    }

    public void setLivePhoto(boolean livePhoto) {
	if (livePhoto != photo.isLivePhoto()) {
	    photoChanged = true;
	    photo.setLivePhoto(livePhoto);
	}
    }

    public void setCoordinates(Coordinates coordinates) {
	if (!Objects.equals(coordinates, photo.getCoordinates())) {
	    photoChanged = true;
	    photo.setCoordinates(coordinates);
	}
    }

    public void setAddress(Address address) {
	if (!Objects.equals(address, photo.getAddress())) {
	    photoChanged = true;
	    photo.setAddress(address);
	}
    }

    public void setDistanceFromAddress(Distance distanceFromAddress) {
	if (!Objects.equals(distanceFromAddress, photo.getDistanceFromAddress())) {
	    photoChanged = true;
	    photo.setDistanceFromAddress(distanceFromAddress);
	}
    }

    public void setMetaData(MetaData metaData) {
	if (!Objects.equals(metaData, photo.getMetaData())) {
	    photoChanged = true;
	    photo.setMetaData(metaData);
	}
    }

    public String getFileName() {
	return photo.getFileName();
    }

    public String getRelativeFileName() {
	return photo.getRelativeFileName();
    }

    public int hashCode() {
	return photo.hashCode();
    }

    public String getRelativeFolderName() {
	return photo.getRelativeFolderName();
    }

    public String getLabel() {
	return photo.getLabel();
    }

    public LocalDateTime getDateTaken() {
	return photo.getDateTaken();
    }

    public int getThumbWidth() {
	return photo.getThumbWidth();
    }

    public int getThumbHeight() {
	return photo.getThumbHeight();
    }

    public int getDuration() {
	return photo.getDuration();
    }

    public int getOrientation() {
	return photo.getOrientation();
    }

    public String getSource() {
	return photo.getSource();
    }

    public boolean isHidden() {
	return photo.isHidden();
    }

    public boolean isLivePhoto() {
	return photo.isLivePhoto();
    }

    public Coordinates getCoordinates() {
	return photo.getCoordinates();
    }

    public Address getAddress() {
	return photo.getAddress();
    }

    public Distance getDistanceFromAddress() {
	return photo.getDistanceFromAddress();
    }

    public String getId() {
	return photo.getId();
    }

    public MediaType getType() {
	return photo.getType();
    }

    public MetaData getMetaData() {
	return photo.getMetaData();
    }

    public boolean hasSameMetaData(Object obj) {
	return photo.hasSameMetaData(obj);
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("PhotoInphoto [photo=[");
	builder.append(photo.getId()).append("file=").append(photo.getRelativeFileName()).append("]");
	builder.append(", added=");
	builder.append(added);
	builder.append(", photoChanged=");
	builder.append(photoChanged);
	builder.append(", fileChanged=");
	builder.append(fileChanged);
	builder.append("]");
	return builder.toString();
    }
    
    

}
