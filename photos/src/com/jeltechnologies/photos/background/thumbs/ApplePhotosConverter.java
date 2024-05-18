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

    // private final boolean USES_IMAGEMICK_EXE = ENV.getConfig().getImageMagickExecutable().toString().toLowerCase().endsWith(".exe");

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
	if (exe.getName().endsWith("magick.exe")) {
	    OperatingSystemCommand command = new OperatingSystemCommand(exe);
	    command.addArgument("mogrify");
	    command.addArgument("-format");
	    command.addArgument("jpg");
	    command.addArgument(heicFile.getAbsolutePath());
	    command.execute();
	} else {
	    if (exe.getName().endsWith("heif-convert")) { 
		// Mint Linux 
		// https://7.dev/converting-heic-to-jpg-using-the-command-line/
		OperatingSystemCommand command = new OperatingSystemCommand(exe);
		command.addArgument(heicFile.getAbsolutePath());
		command.addArgument(convertedFile.getAbsolutePath());
		command.execute();
	    }
	}
	
    }
}
