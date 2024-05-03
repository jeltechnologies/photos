package com.jeltechnologies.photos.config.yaml;

import java.io.Serializable;

public class ExternalProgramsConfiguration implements Serializable {
    private static final long serialVersionUID = 5381487654649872159L;
    private HandbrakeConfiguration handbrake = new HandbrakeConfiguration();
    private ExternalProgramConfiguration ffmpeg = new ExternalProgramConfiguration();
    private ExternalProgramConfiguration imagemagick = new ExternalProgramConfiguration();

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

    public ExternalProgramConfiguration getImagemagick() {
        return imagemagick;
    }

    public void setImagemagick(ExternalProgramConfiguration imagemagick) {
        this.imagemagick = imagemagick;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("ExternalProgramsConfiguration [handbrake=");
	builder.append(handbrake);
	builder.append(", ffmpeg=");
	builder.append(ffmpeg);
	builder.append(", imagemagick=");
	builder.append(imagemagick);
	builder.append("]");
	return builder.toString();
    }
}
