package com.jeltechnologies.photos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.config.ConfigurationManager;
import com.jeltechnologies.photos.config.CountryMap;
import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.picures.frame.program.AllPhotosProgram;
import com.jeltechnologies.photos.picures.frame.program.BaseFrameProgram;
import com.jeltechnologies.photos.picures.frame.program.FamilyVideosProgram;
import com.jeltechnologies.photos.picures.frame.program.Group;
import com.jeltechnologies.photos.picures.frame.program.GroupTitle;
import com.jeltechnologies.photos.picures.frame.program.Last12MonthsProgram;
import com.jeltechnologies.photos.picures.frame.program.Last3MonthsProgram;
import com.jeltechnologies.photos.picures.frame.program.Last6MonthsProgram;
import com.jeltechnologies.photos.picures.frame.program.LastMonthProgram;
import com.jeltechnologies.photos.picures.frame.program.LastWeekProgram;
import com.jeltechnologies.photos.picures.frame.program.SamePlaceProgram;
import com.jeltechnologies.photos.picures.frame.program.SameTimeAndPlaceProgram;
import com.jeltechnologies.photos.picures.frame.program.SameTimePhotoProgram;
import com.jeltechnologies.photos.picures.frame.program.SpecialDaySettings;
import com.jeltechnologies.photos.picures.frame.program.TodayLastYears;
import com.jeltechnologies.photos.utils.FileUtils;
import com.jeltechnologies.photos.utils.StringUtils;

public class Environment {

    private ConfigurationManager config;

    private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);

    public static final Environment INSTANCE = new Environment();

    public static final String THUMB_FILE_FORMAT = ".jpg";

    public static final int JPG_QUALITY = 99;

    public static final String DEFAULT_PRIVILEGE = "photos-user";

    public static final String[] VIDEO_EXTENSIONS = { "mp4", "mov", "m2ts", "avi", "wmv", "3gp", "mkv", "vob", "m4v" };

    public static final String[] PHOTO_EXTENSIONS = { "jpg", "jpeg", "heic", "png", "bmp", "gif" };

    private static final int SAME_PLACE_DISTANCE_WITHIN_KILOMETRES = 5;

    private String absolutePhotoFolderName;

    private SpecialDaySettings specialDays;

    private CountryMap countryMap;

    private Environment() {
    }

    public static MediaType getMediaType(String fileName) {
	String extension = StringUtils.findAfterLast(fileName, ".").toLowerCase();
	MediaType type = null;
	for (int i = 0; type == null && i < PHOTO_EXTENSIONS.length; i++) {
	    if (PHOTO_EXTENSIONS[i].equals(extension)) {
		type = MediaType.PHOTO;
	    }
	}
	for (int i = 0; type == null && i < VIDEO_EXTENSIONS.length; i++) {
	    if (VIDEO_EXTENSIONS[i].equals(extension)) {
		type = MediaType.VIDEO;
	    }
	}
	return type;
    }

    public void init(String yamlFileName) throws Exception {
	config = new ConfigurationManager(new File(yamlFileName));
	absolutePhotoFolderName = toUnixFileName(config.getRootOriginalFolder());
	countryMap = new CountryMap(getClass().getResourceAsStream("/countrycodes.json"));
	specialDays = new SpecialDaySettings();
	specialDays.addConfiguration(config.getSpecialDaysConfiguration());
    }

    public CountryMap getCountryMap() {
	return countryMap;
    }

    public ConfigurationManager getConfig() {
	return config;
    }

    public static void deleteTempFiles() {
    }

    private String toUnixFileName(File file) {
	return toUnixFileName(file.getAbsolutePath());
    }

    private String toUnixFileName(String fileName) {
	String slashed = StringUtils.replaceAll(fileName, '\\', '/');
	String noDoubleSlash = StringUtils.replaceAll(slashed, "//", "/");
	return noDoubleSlash;
    }

    public File getRootOriginalFolder() {
	return config.getRootOriginalFolder();
    }

    public String getRelativePhotoFileName(File file) {
	String result;
	String absoluteFileName = toUnixFileName(FileUtils.getNormalizedPath(file));
	if (absoluteFileName.equals(absolutePhotoFolderName)) {
	    result = "";
	} else {
	    if (absoluteFileName.startsWith(absolutePhotoFolderName)) {
		result = StringUtils.stripBefore(absoluteFileName, absolutePhotoFolderName);
	    } else {
		result = null;
	    }
	}
	if (result != null && !result.startsWith("/")) {
	    result = "/" + result;
	}
	return result;
    }

    public File getFile(String relativeFileName) {
	String absoluteFileName = config.getRootOriginalFolder().getAbsolutePath() + "/" + relativeFileName;
	File file = new File(absoluteFileName);
	if (LOGGER.isDebugEnabled()) {
	    if (!file.isFile() && !file.isDirectory()) {
		LOGGER.debug("File with relativeName " + relativeFileName + " does not exist as " + file.getAbsolutePath());
	    }
	}
	return file;
    }

    public List<Dimension> getAllThumbnailDimensions() {
	List<Dimension> dimensions = new ArrayList<Dimension>();
	dimensions.add(getDimensionOriginal());
	dimensions.add(getDimensionThumbs());
	dimensions.add(getDimensionFullscreen());
	return dimensions;
    }

    public Dimension getDimensionOriginal() {
	return new Dimension(0, 0, Dimension.Type.ORIGINAL);
    }

    public Dimension getDimensionThumbs() {
	return new Dimension(400, 300, Dimension.Type.SMALL);
    }

    public Dimension getDimensionFullscreen() {
	return new Dimension(1920, 1080, Dimension.Type.MEDIUM); // ipad retina
    }

    public String getRelativeRootAlbums() {
	return getRelativePhotoFileName(config.getAlbumsFolder());
    }

    public String getRelativeRootUncategorized() {
	return getRelativePhotoFileName(config.getUncategorizedFolder());
    }

    @Deprecated
    public SpecialDaySettings getSpecialDays() {
	return specialDays;
    }

    public List<Group> getFrameProgramGroups() {
	List<Group> groups = new ArrayList<Group>();

	List<BaseFrameProgram> all = new ArrayList<BaseFrameProgram>();
	all.add(new AllPhotosProgram());
	all.add(new FamilyVideosProgram());
	all.add(new TodayLastYears("todaylastyears", "Today years ago"));
	groups.add(new Group(new GroupTitle("all", "All photos and videos"), all));

	List<BaseFrameProgram> programs = new ArrayList<BaseFrameProgram>();
	programs.add(new LastWeekProgram());
	programs.add(new LastMonthProgram());
	programs.add(new Last3MonthsProgram());
	programs.add(new Last6MonthsProgram());
	programs.add(new Last12MonthsProgram());

	groups.add(new Group(new GroupTitle("programs", "Time"), programs));
	groups.add(new Group(new GroupTitle("special-days", "Special days"), specialDays.getPrograms()));

	List<BaseFrameProgram> moreLikePrograms = new ArrayList<BaseFrameProgram>();
	moreLikePrograms.add(new SameTimePhotoProgram("same-time", "Same time"));
	moreLikePrograms.add(new SameTimeAndPlaceProgram("same-time-and-place", "Same time and place", SAME_PLACE_DISTANCE_WITHIN_KILOMETRES));
	moreLikePrograms.add(new SamePlaceProgram("same-place", "Same place", SAME_PLACE_DISTANCE_WITHIN_KILOMETRES));
	groups.add(new Group(new GroupTitle("more-like", "More like"), moreLikePrograms));

	return groups;
    }

}
