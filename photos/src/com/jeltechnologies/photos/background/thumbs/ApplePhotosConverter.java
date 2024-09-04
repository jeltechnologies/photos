package com.jeltechnologies.photos.background.thumbs;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.utils.StringUtils;
import com.jeltechnologies.util.OperatingSystemCommand;

public class ApplePhotosConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplePhotosConverter.class);
    private final static Environment ENV = Environment.INSTANCE;
    private final File heicFile;
    private final File convertedFile;

    public ApplePhotosConverter(File heic) {
	this.heicFile = heic;
	String nameWithoutExtension = StringUtils.stripAfterLast(heicFile.getAbsolutePath(), ".");
	this.convertedFile = new File(nameWithoutExtension + ".jpg");
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("Apple converter " + heicFile);
	}
    }

    public File getConvertedFile() throws IOException, InterruptedException {
	convert();
	if (!convertedFile.isFile()) {
	    LOGGER.warn("Converted file not found for " + heicFile);
	}
	return convertedFile;
    }

    private void convert() throws IOException, InterruptedException {
	File exe = ENV.getConfig().getImageMagickExecutable();
	OperatingSystemCommand command = new OperatingSystemCommand(exe);
	String exeName = exe.getName();
	if (exeName.indexOf("magick.exe") > -1) {
	    command.addArgument("mogrify");
	    command.addArgument("-quality");
	    command.addArgument("95%");
	    command.addArgument("-format");
	    command.addArgument("jpg");
	    command.addArgument(heicFile.getAbsolutePath());
	} else {
	    if (exeName.indexOf("heif-convert") > -1) {
		command.addArgument("-q");
		command.addArgument("95");
		command.addArgument(heicFile.getAbsolutePath());
		command.addArgument(convertedFile.getAbsolutePath());
	    } else {
		throw new IllegalStateException("Heic converter not properly installed");
	    }
	}
	command.execute();
	//LOGGER.info(command.getDescription());
    }
}
