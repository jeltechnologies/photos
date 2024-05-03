package com.jeltechnologies.photos.videos;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.config.yaml.HandbrakeEncodingSettings;
import com.jeltechnologies.util.OperatingSystemCommand;

public class HandbrakeEncoder {
    private final static Logger LOGGER = LoggerFactory.getLogger(HandbrakeEncoder.class);
    private static final File HANDBRAKE_EXE = Environment.INSTANCE.getConfig().getHandbrakeExecutable();

    private final File input;
    private final File output;
    private final HandbrakeEncodingSettings encodingSettings;

    public HandbrakeEncoder(File input, File output, HandbrakeEncodingSettings encodingSettings) {
	this.input = input;
	this.output = output;
	this.encodingSettings = encodingSettings;
    }

    public void convert(boolean removeHDR) throws VideoConvertException, InterruptedException {
	OperatingSystemCommand command = new OperatingSystemCommand(HANDBRAKE_EXE);
	command.addArgument("-Z");
	command.addArgument(encodingSettings.getPreset());
	if (removeHDR) {
	    command.addArgument("--colorspace");
	    command.addArgument("bt709");
	}
	
	command.addArgument("--encoder");
	command.addArgument(encodingSettings.getVideoEncoder());
	
	command.addArgument("-i");
	command.addArgument(input.getAbsolutePath());
	command.addArgument("-o");
	command.addArgument(output.getAbsolutePath());

	try {
	    command.execute();
	} catch (IOException e) {
	    if (LOGGER.isDebugEnabled()) {
		if (command.getOutput() != null) {
		    for (String line : command.getOutput()) {
			LOGGER.debug(line);
		    }
		}
	    }
	    throw new VideoConvertException("Error converting movie with Handbrake: " + e.getMessage());
	}
    }

}
