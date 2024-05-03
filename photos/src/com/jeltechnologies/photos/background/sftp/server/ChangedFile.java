package com.jeltechnologies.photos.background.sftp.server;

import java.io.File;
import java.time.LocalDateTime;

public class ChangedFile {
    private LocalDateTime time;
    private File file;
    
    public LocalDateTime getTime() {
        return time;
    }
    public void setTime(LocalDateTime time) {
        this.time = time;
    }
    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("ChangedFile [time=");
	builder.append(time);
	builder.append(", file=");
	builder.append(file);
	builder.append("]");
	return builder.toString();
    }
}
