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

    private final boolean USES_IMAGEMICK_EXE = ENV.getConfig().getImageMagickExecutable().toString().toLowerCase().endsWith(".exe");

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
	// In Windows we execute magick.exe and we need to add mogrify as argument.
	// In Linux we use mogrify as executable and do not use mogrify as argument.
	if (USES_IMAGEMICK_EXE) {
	    command.addArgument("mogrify");
	}
	command.addArgument("-format");
	command.addArgument("jpg");
	command.addArgument(heicFile.getAbsolutePath());
	command.execute();
    }
}
