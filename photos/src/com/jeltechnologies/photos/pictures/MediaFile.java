package com.jeltechnologies.photos.pictures;

import com.jeltechnologies.photos.datatypes.usermodel.Role;

public class MediaFile {
    private String id;
    private String relativeFileName;
    private String relativeFolderName;
    private String fileName;
    private long fileLastModified;
    private long size;
    private Role role;
    private boolean inAlbums;    

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getRelativeFileName() {
	return relativeFileName;
    }

    public void setRelativeFileName(String relativeFileName) {
	this.relativeFileName = relativeFileName;
    }

    public long getFileLastModified() {
        return fileLastModified;
    }

    public void setFileLastModified(long fileLastModified) {
        this.fileLastModified = fileLastModified;
    }

    public long getSize() {
	return size;
    }

    public void setSize(long size) {
	this.size = size;
    }

    public String getRelativeFolderName() {
        return relativeFolderName;
    }

    public void setRelativeFolderName(String relativeFolderName) {
        this.relativeFolderName = relativeFolderName;
    }
    
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public boolean isInAlbums() {
        return inAlbums;
    }

    public void setInAlbums(boolean inAlbums) {
        this.inAlbums = inAlbums;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("MediaFile [id=");
	builder.append(id);
	builder.append(", relativeFileName=");
	builder.append(relativeFileName);
	builder.append(", relativeFolderName=");
	builder.append(relativeFolderName);
	builder.append(", fileName=");
	builder.append(fileName);
	builder.append(", fileLastModified=");
	builder.append(fileLastModified);
	builder.append(", size=");
	builder.append(size);
	builder.append(", role=");
	builder.append(role);
	builder.append(", inAlbums=");
	builder.append(inAlbums);
	builder.append("]");
	return builder.toString();
    }
}
