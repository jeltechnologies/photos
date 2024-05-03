package com.jeltechnologies.photos.datatypes.usermodel;

import java.io.Serializable;
import java.util.Objects;

public class Preferences implements Serializable {
    private static final long serialVersionUID = 4911795781663916554L;
    private TimelinePreferences timeline = new TimelinePreferences();

    public Preferences() {
    }

    public TimelinePreferences getTimeline() {
	if (timeline == null) {
	    timeline = new TimelinePreferences();
	}
        return timeline;
    }

    public void setTimeline(TimelinePreferences timeline) {
        this.timeline = timeline;
    }

    @Override
    public int hashCode() {
	return Objects.hash(timeline);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Preferences other = (Preferences) obj;
	return Objects.equals(timeline, other.timeline);
    }

    @Override
    public String toString() {
	return "Preferences [timeline=" + timeline + "]";
    }

}
