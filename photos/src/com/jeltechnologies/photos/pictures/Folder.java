package com.jeltechnologies.photos.pictures;

import java.io.File;
import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Folder implements Serializable {
    private static final long serialVersionUID = -5069197719817631150L;
    private String name;
    private String description;
    private File file;
    private String relativeName;
    private List<String> folderNames = new ArrayList<String>();

    public void addFolder(String relativeFolderName) {
	this.folderNames.add(relativeFolderName);
    }

    public List<String> getFolderNames() {
        return folderNames;
    }

    public void setFolderNames(List<String> folderNames) {
        this.folderNames = folderNames;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public File getFile() {
	return file;
    }

    public void setFile(File file) {
	this.file = file;
    }

    public String getRelativeName() {
	return relativeName;
    }

    public void setRelativeName(String relativeName) {
	this.relativeName = relativeName;
    }
    
    public void sort() {
	Collections.sort(folderNames, String.CASE_INSENSITIVE_ORDER);
	folderNames.sort(Collator.getInstance().reversed());
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("Folder [relativeName=");
	builder.append(relativeName);
	builder.append("]");
	return builder.toString();
    }

}
