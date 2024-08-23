package com.jeltechnologies.photos.config.yaml;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalProgramsConfiguration implements Serializable {
    private static final long serialVersionUID = 5381487654649872159L;
    
    private HandbrakeConfiguration handbrake = new HandbrakeConfiguration();
    
    private ExternalProgramConfiguration ffmpeg = new ExternalProgramConfiguration();
    
    @JsonProperty(value = "apple-heic-to-jpg")
    private ExternalProgramConfiguration appleHeicToJpg = new ExternalProgramConfiguration();
    
    private ExternalProgramConfiguration exiftool = new ExternalProgramConfiguration();

    public HandbrakeConfiguration getHandbrake() {
        return handbrake;
    }

    public void setHandbrake(HandbrakeConfiguration handbrake) {
        this.handbrake = handbrake;
    }

    public ExternalProgramConfiguration getFfmpeg() {
        return ffmpeg;
    }

    public void setFfmpeg(ExternalProgramConfiguration ffmpeg) {
        this.ffmpeg = ffmpeg;
    }

    public ExternalProgramConfiguration getAppleHeicToJpg() {
        return appleHeicToJpg;
    }

    public void setAppleHeicToJpg(ExternalProgramConfiguration appleHeicToJpg) {
        this.appleHeicToJpg = appleHeicToJpg;
    }
    
    public ExternalProgramConfiguration getExiftool() {
        return exiftool;
    }

    public void setExiftool(ExternalProgramConfiguration exiftool) {
        this.exiftool = exiftool;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("ExternalProgramsConfiguration [handbrake=");
	builder.append(handbrake);
	builder.append(", ffmpeg=");
	builder.append(ffmpeg);
	builder.append(", appleHeicToJpg=");
	builder.append(appleHeicToJpg);
	builder.append(", exiftool=");
	builder.append(exiftool);
	builder.append("]");
	return builder.toString();
    }


}
