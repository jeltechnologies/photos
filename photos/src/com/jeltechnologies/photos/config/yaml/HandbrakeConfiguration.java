package com.jeltechnologies.photos.config.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HandbrakeConfiguration extends ExternalProgramConfiguration {
    private static final long serialVersionUID = 2107314943889436745L;

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

    @Override
    public String toString() {
	return "HandbrakeConfiguration [qualityHigh=" + qualityHigh + ", qualityLow=" + qualityLow + ", getExecutable()=" + getExecutable() + "]";
    }
}
