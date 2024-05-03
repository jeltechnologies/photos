package com.jeltechnologies.photos.picures.map;

import java.io.Serializable;
import java.util.List;

import com.jeltechnologies.photos.pictures.Photo;

public class MapBoundsResponse implements Serializable {
    private static final long serialVersionUID = -1220819386144086214L;
    
    private List<Photo> photos;

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }
}
