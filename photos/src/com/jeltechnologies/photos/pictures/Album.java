package com.jeltechnologies.photos.pictures;

import java.io.Serializable;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.usermodel.Role;

public class Album implements Serializable {
    private static final long serialVersionUID = -6977198071012697887L;
    private String relativeFolderName;
    private Role role;
    private String name;
    private Photo coverPhoto;
    private static final String FOLDER_ALBUMS = Environment.INSTANCE.getRelativeRootAlbums();
    private static final String FOLDER_UNCATEGORIZED = Environment.INSTANCE.getRelativeRootUncategorized();

    public String getRelativeFolderName() {
	return relativeFolderName;
    }

    public void setRelativeFolderName(String relativeFolderName) {
	this.relativeFolderName = relativeFolderName;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public Photo getCoverPhoto() {
	return coverPhoto;
    }

    public void setCoverPhoto(Photo photo) {
	this.coverPhoto = photo;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    
    public boolean hasParent() {
	return !isAlbumsRoot() && !isUncategorizedRoot();
    }
    
    
    public boolean isAlbumsRoot() {
	return FOLDER_ALBUMS.equals(relativeFolderName);
    }
    
    public boolean isUncategorizedRoot() {
  	return FOLDER_UNCATEGORIZED.equals(relativeFolderName);
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("Album [relativeFolderName=");
	builder.append(relativeFolderName);
	builder.append(", role=");
	builder.append(role);
	builder.append(", name=");
	builder.append(name);
	builder.append(", coverPhoto=");
	builder.append(coverPhoto);
	builder.append("]");
	return builder.toString();
    }
}
