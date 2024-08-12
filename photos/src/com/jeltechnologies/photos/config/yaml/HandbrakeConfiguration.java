package com.jeltechnologies.photos.config.yaml;

import java.io.File;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HandbrakeConfiguration implements Serializable {
    private static final long serialVersionUID = 2107314943889436745L;
    
    private File executable;
    
    @JsonProperty(value = "quality-high")
    private HandbrakeEncodingSettings qualityHigh = new HandbrakeEncodingSettings("Creator 2160p60 4K", "x264");

    @JsonProperty(value = "quality-low")
    private HandbrakeEncodingSettings qualityLow = new HandbrakeEncodingSettings("Email 25 MB 5 Minutes 480p30", "x264");

    public HandbrakeEncodingSettings getQualityHigh() {
	return qualityHigh;
    }

    public void setQualityHigh(HandbrakeEncodingSettings qualityHigh) {
	this.qualityHigh = qualityHigh;
    }

    public HandbrakeEncodingSettings getQualityLow() {
	return qualityLow;
    }

    public void setQualityLow(HandbrakeEncodingSettings qualityLow) {
	this.qualityLow = qualityLow;
    }

    public File getExecutable() {
        return executable;
    }

    public void setExecutable(File executable) {
        this.executable = executable;
    }

    @Override
    public String toString() {
	return "HandbrakeConfiguration [executable=" + executable + ", qualityHigh=" + qualityHigh + ", qualityLow=" + qualityLow + "]";
    }


}
