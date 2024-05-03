package com.jeltechnologies.photos.datatypes.usermodel;

import java.util.Objects;

import com.jeltechnologies.photos.Settings;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.timeline.Grouping;
import com.jeltechnologies.photos.timeline.Sorting;

public class TimelinePreferences {
    private Grouping grouping;
    private Sorting sorting;
    private int items;
    private boolean randomCover;
    private MediaType mediaType;

    public TimelinePreferences() {
	setDefaults();
    }

    public void setDefaults() {
	items = Settings.timelineDefaultItems;
	grouping = Settings.timelineDefaultGrouping;
	sorting = Settings.timelineDefaultSorting;
	randomCover = Settings.timelineDefaultRandomCover;
	mediaType = Settings.timeLineDefaultMediaType;
    }

    public Grouping getGrouping() {
	return grouping;
    }

    public void setGrouping(Grouping timelineGrouping) {
	this.grouping = timelineGrouping;
    }

    public Sorting getSorting() {
	return sorting;
    }

    public void setSorting(Sorting timelineSorting) {
	this.sorting = timelineSorting;
    }

    public int getItems() {
	return items;
    }

    public void setItems(int tiemLineitems) {
	this.items = tiemLineitems;
    }

    public boolean isRandomCover() {
	return randomCover;
    }

    public void setRandomCover(boolean timelineRandomCover) {
	this.randomCover = timelineRandomCover;
    }
    
    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("TimelinePreferences [grouping=");
	builder.append(grouping);
	builder.append(", sorting=");
	builder.append(sorting);
	builder.append(", items=");
	builder.append(items);
	builder.append(", randomCover=");
	builder.append(randomCover);
	builder.append(", mediaType=");
	builder.append(mediaType);
	builder.append("]");
	return builder.toString();
    }

    @Override
    public int hashCode() {
	return Objects.hash(mediaType, grouping, items, randomCover, sorting);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	TimelinePreferences other = (TimelinePreferences) obj;
	return mediaType == other.mediaType && grouping == other.grouping && items == other.items
		&& randomCover == other.randomCover && sorting == other.sorting;
    }
}
