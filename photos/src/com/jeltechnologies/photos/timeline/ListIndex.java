package com.jeltechnologies.photos.timeline;

import java.util.List;
import java.util.Objects;

import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.pictures.Photo;

public class ListIndex {
    private final Query query;
    private final List<Photo> photos;

    ListIndex(Query query, List<Photo> photos) {
	this.query = query;
	this.photos = photos;
    }

    public Query getQuery() {
	return query;
    }

    public List<Photo> getPhotos() {
	return photos;
    }

    @Override
    public int hashCode() {
	return Objects.hash(photos, query);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	ListIndex other = (ListIndex) obj;
	return Objects.equals(photos, other.photos) && Objects.equals(query, other.query);
    }
}
