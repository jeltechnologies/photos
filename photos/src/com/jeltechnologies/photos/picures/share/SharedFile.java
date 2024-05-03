package com.jeltechnologies.photos.picures.share;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SharedFile implements Serializable {
    private static final long serialVersionUID = -5060871418624172277L;
    private String uuid;
    private String relativeFileName;
    private LocalDateTime creationDate;
    private LocalDateTime expirationDate;
    private String username;
    private String publicUrl;

    public SharedFile() {
    }

    public String getUuid() {
	return uuid;
    }

    public void setUuid(String uuid) {
	this.uuid = uuid;
    }

    public String getRelativeFileName() {
	return relativeFileName;
    }

    public void setRelativeFileName(String relativeFileName) {
	this.relativeFileName = relativeFileName;
    }

    public LocalDateTime getCreationDate() {
	return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
	this.creationDate = creationDate;
    }

    public LocalDateTime getExpirationDate() {
	return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
	this.expirationDate = expirationDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("SharedFile [uuid=");
	builder.append(uuid);
	builder.append(", relativeFileName=");
	builder.append(relativeFileName);
	builder.append(", creationDate=");
	builder.append(creationDate);
	builder.append(", expirationDate=");
	builder.append(expirationDate);
	builder.append(", username=");
	builder.append(username);
	builder.append(", publicUrl=");
	builder.append(publicUrl);
	builder.append("]");
	return builder.toString();
    }
}
