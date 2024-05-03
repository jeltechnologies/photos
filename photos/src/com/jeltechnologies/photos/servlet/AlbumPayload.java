package com.jeltechnologies.photos.servlet;

import java.io.Serializable;

public class AlbumPayload implements Serializable {
    private static final long serialVersionUID = -8760118453570561442L;
    private String relativeFileName;
    private String name;
    private String coverPhotoId;

    public String getRelativeFileName() {
        return relativeFileName;
    }

    public void setRelativeFileName(String relativeFileName) {
        this.relativeFileName = relativeFileName;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getCoverPhotoId() {
        return coverPhotoId;
    }

    public void setCoverPhotoId(String coverPhotoId) {
        this.coverPhotoId = coverPhotoId;
    }

    @Override
    public String toString() {
	return "AlbumPayload [relativeFileName=" + relativeFileName + ", name=" + name + ", coverPhotoId=" + coverPhotoId + "]";
    }

}

