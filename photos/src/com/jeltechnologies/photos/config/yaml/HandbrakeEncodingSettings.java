package com.jeltechnologies.photos.config.yaml;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HandbrakeEncodingSettings implements Serializable {
    private static final long serialVersionUID = 6186245722211149051L;

    private String preset;

    @JsonProperty(value = "video-encoder")
    private String videoEncoder = "x264";

    public HandbrakeEncodingSettings() {
    }

    public HandbrakeEncodingSettings(String preset, String videoEncoder) {
	this.preset = preset;
	this.videoEncoder = videoEncoder;
    }

    public String getPreset() {
	return preset;
    }

    public void setPreset(String preset) {
	this.preset = preset;
    }

    public String getVideoEncoder() {
	return videoEncoder;
    }

    public void setVideoEncoder(String videoEncoder) {
	this.videoEncoder = videoEncoder;
    }

    @Override
    public String toString() {
	return "HandbrakeEncodingSettings [preset=" + preset + ", videoEncoder=" + videoEncoder + "]";
    }
}
