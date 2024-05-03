package com.jeltechnologies.photos.config.yaml;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NetworkConfiguration implements Serializable {
    private static final long serialVersionUID = 3433469391765266163L;
    
    @JsonProperty(value = "public-url")
    private String publicUrl;
    @JsonProperty(value = "local-nas-port")
    private int localNASPort = -1;

    public String getPublicUrl() {
	return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
	this.publicUrl = publicUrl;
    }

    public int getLocalNASPort() {
	return localNASPort;
    }

    public void setLocalNASPort(int localNASPort) {
	this.localNASPort = localNASPort;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("NetworkConfiguration [publicUrl=");
	builder.append(publicUrl);
	builder.append(", localNASPort=");
	builder.append(localNASPort);
	builder.append("]");
	return builder.toString();
    }
}
