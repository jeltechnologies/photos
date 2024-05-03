package com.jeltechnologies.photos;

import java.io.Serializable;

import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.timeline.Grouping;
import com.jeltechnologies.photos.timeline.Sorting;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.PageContext;


public class Settings implements Serializable {
    private static final long serialVersionUID = 5049448431452892711L;
    private static final String SESSION_KEY = "photos-settings";
    private int albumThumbWidth = 200;
    private int albumThumbHeight = 200;
    private String dateFormat = "d MMMM yyyy, H:mm";
    private int maximumAmountInGallary = 120;
    public static final Grouping timelineDefaultGrouping = Grouping.MONTH;
    public static final Sorting timelineDefaultSorting = Sorting.NEWESTFIRST;
    public static final int timelineDefaultItems = 50;
    public static final boolean timelineDefaultRandomCover = true;
    public static final MediaType timeLineDefaultMediaType = MediaType.ALL;
    
    private Settings() {
    }
    
    public int getMaximumAmountInGallary() {
        return maximumAmountInGallary;
    }

    public void setMaximumAmountInGallary(int maximumAmountInGallary) {
        this.maximumAmountInGallary = maximumAmountInGallary;
    }

    public static Settings get(PageContext pageContext) {
	return get(pageContext.getSession());
    }
    
    public static Settings get(HttpSession session) {
	Settings settings = (Settings) session.getAttribute(SESSION_KEY);
	if (settings == null) {
	    settings = new Settings();
	    session.setAttribute(SESSION_KEY, settings);
	}
	return settings;
    }
    

    public int getAlbumThumbWidth() {
        return albumThumbWidth;
    }

    public void setAlbumThumbWidth(int thumbWidth) {
        this.albumThumbWidth = thumbWidth;
    }

    public int getAlbumThumbHeight() {
        return albumThumbHeight;
    }

    public void setAlbumThumbHeight(int thumbHeight) {
        this.albumThumbHeight = thumbHeight;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
    
}