package com.jeltechnologies.photos.pictures;

import java.io.File;

import com.jeltechnologies.photos.background.thumbs.Producer;
import com.jeltechnologies.photos.datatypes.usermodel.Role;
import com.jeltechnologies.photos.utils.StringUtils;

public class QueuedMediaFile {
    private final File file;
    private final Role role;
    private final String extension;
    private final boolean jpg;
    private final boolean apple;
    private final Producer.Type type;
    
    public QueuedMediaFile(File file, Role role, Producer.Type type) {
	this.file = file;
	this.role = role;
	this.type = type;
	this.extension = StringUtils.findAfterLast(file.getName(), ".").toLowerCase();
	this.jpg = this.extension.equals("jpg") || this.extension.equals("jpeg");
	this.apple = this.extension.equals("heic");
    }

    public File getFile() {
	return file;
    }

    public Role getRole() {
	return role;
    }

    public boolean isJpg() {
        return jpg;
    }

    public boolean isApple() {
        return apple;
    }
    
    public String getExtension() {
        return extension;
    }

    public Producer.Type getType() {
        return type;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("QueuedMediaFile [file=");
	builder.append(file);
	builder.append(", role=");
	builder.append(role);
	builder.append("]");
	return builder.toString();
    }

}
