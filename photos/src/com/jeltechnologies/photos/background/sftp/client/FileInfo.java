package com.jeltechnologies.photos.background.sftp.client;

import java.util.Date;

import com.jeltechnologies.photos.pictures.MediaType;

public class FileInfo {
    private String name;
    private String absolutePath;
    private String relativePath;
    private Date lastModifiedDate;
    private long size;
    private MediaType type;
    
    public MediaType getType() {
        return type;
    }

    public void setType(MediaType type) {
        this.type = type;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getAbsolutePath() {
	return absolutePath;
    }

    public void setAbsolutePath(String path) {
	this.absolutePath = path;
    }

    public Date getLastModifiedDate() {
	return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
	this.lastModifiedDate = lastModifiedDate;
    }

    public long getSize() {
	return size;
    }

    public void setSize(long size) {
	this.size = size;
    }
    
    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("FileInfo [name=");
	builder.append(name);
	builder.append(", absolutePath=");
	builder.append(absolutePath);
	builder.append(", relativePath=");
	builder.append(relativePath);
	builder.append(", lastModifiedDate=");
	builder.append(lastModifiedDate);
	builder.append(", size=");
	builder.append(size);
	builder.append(", type=");
	builder.append(type);
	builder.append("]");
	return builder.toString();
    }

}
