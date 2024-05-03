package com.jeltechnologies.photos.background.thumbs;

import java.io.File;
import java.io.FileFilter;

import com.jeltechnologies.photos.datatypes.usermodel.Role;
import com.jeltechnologies.photos.pictures.MediaQueue;

public class ProduceFolder {
    private File folder;
    private Role role;
    private FileFilter filenameFilter;
    private MediaQueue queue;

    public File getFolder() {
	return folder;
    }

    public void setFolder(File folder) {
	this.folder = folder;
    }

    public Role getRole() {
	return role;
    }

    public void setRole(Role role) {
	this.role = role;
    }

    public FileFilter getFilenameFilter() {
	return filenameFilter;
    }

    public void setFilenameFilter(FileFilter filenameFilter) {
	this.filenameFilter = filenameFilter;
    }

    public MediaQueue getQueue() {
	return queue;
    }

    public void setQueue(MediaQueue queue) {
	this.queue = queue;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("ProduceFolder [folder=");
	builder.append(folder);
	builder.append(", role=");
	builder.append(role);
	builder.append("]");
	return builder.toString();
    }
}
