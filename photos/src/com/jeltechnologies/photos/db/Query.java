package com.jeltechnologies.photos.db;

import java.util.Objects;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.usermodel.User;
import com.jeltechnologies.photos.pictures.MediaType;

public class Query {

    public enum InAlbum {
	ALL_PHOTOS, IN_ALBUM_WITH_DUPLICATES, IN_ALBUM_NO_DUPLICATES
    };

    private static final int MAX_LIVE_PHOTOS_LENGTH = 4;
    private String checksum = null;
    private final User user;
    private String relativeFolderName = null;
    private TimePeriod timePeriod = null;
    private boolean includeSubFolders = true;
    private OrderBy orderBy;
    private MediaType mediaType = null;
    private int minimumDuration = -1;
    private int maximumDuration = -1;
    private boolean onlyReturnChecksums = false;
    private static boolean LIVE_PHOTOS = Environment.INSTANCE.getConfig().isShowLivePhotos();
    private boolean includeHidden = false;
    private MapBounds mapBounds = null;
    private InAlbum inAlbums = InAlbum.ALL_PHOTOS;

    public Query(User user) {
	this.user = user;
	setDefaultDurations();
    }

    private void setDefaultDurations() {
	if (!LIVE_PHOTOS) {
	    excludeLivePhotos();
	}
    }

    public String getId() {
	return checksum;
    }

    public void setChecksum(String checksum) {
	this.checksum = checksum;
    }

    public MapBounds getMapBounds() {
	return mapBounds;
    }

    public void setMapBounds(MapBounds mapBounds) {
	this.mapBounds = mapBounds;
    }

    public int getMinimumDuration() {
	return minimumDuration;
    }

    public void setMinimumDuration(int minimumDuration) {
	this.minimumDuration = minimumDuration;
    }

    public void excludeLivePhotos() {
	this.minimumDuration = MAX_LIVE_PHOTOS_LENGTH;
    }

    public MediaType getMediaType() {
	return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
	this.mediaType = mediaType;
    }

    public OrderBy getOrderBy() {
	return orderBy;
    }

    public void setOrderBy(OrderBy orderBy) {
	this.orderBy = orderBy;
    }

    public boolean isIncludeSubFolders() {
	return includeSubFolders;
    }

    public void setIncludeSubFolders(boolean includeSubFolders) {
	this.includeSubFolders = includeSubFolders;
    }

    public String getRelativeFolderName() {
	return relativeFolderName;
    }

    public void setRelativeFolderName(String relativeFolderName) {
	this.relativeFolderName = relativeFolderName;
    }

    public TimePeriod getTimePeriod() {
	return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
	this.timePeriod = timePeriod;
    }

    public User getUser() {
	return user;
    }

    public int getMaximumDuration() {
	return maximumDuration;
    }

    public void setMaximumDuration(int maximumDuration) {
	this.maximumDuration = maximumDuration;
    }

    public boolean isOnlyReturnChecksums() {
	return onlyReturnChecksums;
    }

    public void setOnlyReturnChecksums(boolean onlyReturnFilenames) {
	this.onlyReturnChecksums = onlyReturnFilenames;
    }

    public boolean isIncludeHidden() {
	return includeHidden;
    }

    public void setIncludeHidden(boolean includeHidden) {
	this.includeHidden = includeHidden;
    }

    public InAlbum getInAlbums() {
	return inAlbums;
    }

    public void setInAlbums(InAlbum inAlbums) {
	this.inAlbums = inAlbums;
    }

    // For hashcode, take user roles not the complee user

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("Query [checksum=");
	builder.append(checksum);
	builder.append(", user=");
	builder.append(user);
	builder.append(", relativeFolderName=");
	builder.append(relativeFolderName);
	builder.append(", timePeriod=");
	builder.append(timePeriod);
	builder.append(", includeSubFolders=");
	builder.append(includeSubFolders);
	builder.append(", orderBy=");
	builder.append(orderBy);
	builder.append(", mediaType=");
	builder.append(mediaType);
	builder.append(", minimumDuration=");
	builder.append(minimumDuration);
	builder.append(", maximumDuration=");
	builder.append(maximumDuration);
	builder.append(", onlyReturnChecksums=");
	builder.append(onlyReturnChecksums);
	builder.append(", includeHidden=");
	builder.append(includeHidden);
	builder.append(", mapBounds=");
	builder.append(mapBounds);
	builder.append(", inAlbums=");
	builder.append(inAlbums);
	builder.append("]");
	return builder.toString();
    }

    @Override
    public int hashCode() {
	return Objects.hash(checksum, includeHidden, includeSubFolders, mapBounds, maximumDuration, mediaType, minimumDuration, onlyReturnChecksums, orderBy,
		relativeFolderName, timePeriod, inAlbums, user.roles());
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Query other = (Query) obj;
	return checksum == other.checksum && includeHidden == other.includeHidden && includeSubFolders == other.includeSubFolders
		&& Objects.equals(mapBounds, other.mapBounds)
		&& maximumDuration == other.maximumDuration && mediaType == other.mediaType && minimumDuration == other.minimumDuration
		&& onlyReturnChecksums == other.onlyReturnChecksums && orderBy == other.orderBy && Objects.equals(relativeFolderName, other.relativeFolderName)
		&& Objects.equals(timePeriod, other.timePeriod)
		&& Objects.equals(inAlbums, other.inAlbums)
		&& Objects.equals(user.roles(), other.user.roles());
    }

}
