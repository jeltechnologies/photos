package com.jeltechnologies.photos.picures.frame;

import java.util.List;

import com.jeltechnologies.photos.pictures.Photo;

public class MoreLikeRequest {
    public enum MoreLikeProgram {
	DATE_AND_PLACE, DATE, PLACE
    };
    private String id;
    private int requestedAmount;
    private MoreLikeProgram moreLike;
    private int daysBefore;
    private int daysAfter;
    private int distanceKilometers;
    private int index;
    private List<Photo> photos;

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getDaysBefore() {
        return daysBefore;
    }

    public void setDaysBefore(int daysBefore) {
        this.daysBefore = daysBefore;
    }

    public int getDaysAfter() {
        return daysAfter;
    }

    public void setDaysAfter(int daysAfter) {
        this.daysAfter = daysAfter;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public int getRequestedAmount() {
	return requestedAmount;
    }

    public void setRequestedAmount(int requestedAmount) {
	this.requestedAmount = requestedAmount;
    }

    public MoreLikeProgram getMoreLike() {
        return moreLike;
    }

    public void setMoreLike(MoreLikeProgram moreLike) {
        this.moreLike = moreLike;
    }

    public int getDistanceKilometers() {
        return distanceKilometers;
    }

    public void setDistanceKilometers(int maxDistanceeKilometers) {
        this.distanceKilometers = maxDistanceeKilometers;
    }

    @Override
    public String toString() {
	return "MoreLikeRequest [id=" + id + ", requestedAmount=" + requestedAmount + ", moreLike=" + moreLike + ", daysBefore=" + daysBefore + ", daysAfter="
		+ daysAfter + ", distanceKilometers=" + distanceKilometers + ", index=" + index + "]";
    }
}
