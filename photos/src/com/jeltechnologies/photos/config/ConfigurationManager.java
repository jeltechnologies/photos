package com.jeltechnologies.photos.config;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jeltechnologies.photos.config.yaml.HandbrakeConfiguration;
import com.jeltechnologies.photos.config.yaml.RefreshConfiguration;
import com.jeltechnologies.photos.config.yaml.SFTPClientAccount;
import com.jeltechnologies.photos.config.yaml.SFTPServerConfig;
import com.jeltechnologies.photos.config.yaml.SpecialDaysProgramsConfiguration;
import com.jeltechnologies.photos.config.yaml.YamlConfiguration;

public class ConfigurationManager implements Serializable {
    private static final long serialVersionUID = 3285511252620610327L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);
    private YamlConfiguration config;
    
    private File albumsFolder;

    private File uncategorizedFolder;

    private File removedPhotosFolder;
    
    private File failedPhotosFolder;
    
    private File removedCacheFolder;

    private File notWorkingFilesStorage;

    public ConfigurationManager(File file) throws Exception {
	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	mapper.findAndRegisterModules();
	try {
	    LOGGER.info("Reading configuration from " + file);
	    config = (YamlConfiguration) mapper.readValue(file, YamlConfiguration.class);
	    if (LOGGER.isInfoEnabled()) {
		LOGGER.info("Using configuration: " + config);
	    }
	    String error = validateLoad();
	    if (error != null) {
		throw new IllegalStateException(error);
	    }
	    setupFolders();
	    setupConverters();
	} catch (Exception e) {
	    throw new IllegalStateException("Could not load config " + file.getName() + " because " + e.getMessage());
	}
    }

    private String validateLoad() {
	String error = null;
	File originals = config.getStorage().getOriginals();
	if (originals == null || !originals.isDirectory()) {
	    error = "Storage originals is not a directory";
	}
	if (error == null) {
	    File cache = config.getStorage().getCache();
	    if (cache == null || !cache.isDirectory()) {
		error = "Storage cache is not a directory";
	    }
	}
	return error;
    }

    private void setupFolders() {
	File originalsFolder = config.getStorage().getOriginals();
	albumsFolder = createFolderIfNeeded(originalsFolder, "Albums");
	uncategorizedFolder = createFolderIfNeeded(originalsFolder, "Uncategorized");
	removedPhotosFolder = createFolderIfNeeded(originalsFolder, "_Recycle_Bin_");
	failedPhotosFolder = createFolderIfNeeded(originalsFolder, "_Failed_");
	File cache = config.getStorage().getCache();
	removedCacheFolder = createFolderIfNeeded(cache, "_Recycle_Bin_");
	notWorkingFilesStorage = new File(failedPhotosFolder.getAbsolutePath() + "/not-working-files.csv");
    }

    private File createFolderIfNeeded(File inFolder, String childFolderName) {
	File child = new File(inFolder.getAbsolutePath() + "/" + childFolderName);
	if (!child.isDirectory()) {
	    LOGGER.info("Creating folder " + child);
	    boolean madeFolders = child.mkdirs();
	    if (!madeFolders || child.isFile()) {
		throw new IllegalStateException("Cannot create folder " + child.getAbsolutePath());
	    }
	} else {
	    LOGGER.info("Using folder " + child);
	}
	return child;
    }

    private void setupConverters() {
	if (!isCanConvertVideo()) {
	    LOGGER.warn("Cannot convert videos because handbrake configured is not properly");
	} else {
	    LOGGER.info("Handbrake found");
	}
	if (!getFfmpegExecutable().isFile()) {
	    LOGGER.warn("Cannot handle videos because ffmpeg is not configured properly");
	} else {
	    LOGGER.info("ffmpeg found");
	}
	if (!isCanConvertApplePhotos()) {
	    LOGGER.warn("Cannot convert Apple's HEIC pictures, because imagemick is not configured properly");
	} else {
	    LOGGER.info("Imagemick found");
	}
    }
    
    public boolean isCanConvertVideo() {
	return config.getConverters().getHandbrake().getExecutable().isFile();
    }

    public String getMapBoxAccessToken() {
	return config.getMapkey();
    }

    public File getRootOriginalFolder() {
	return config.getStorage().getOriginals();
    }
    
    public File getRemovedCacheFolder() {
	return removedCacheFolder;
    }
    
    public File getCacheFolder() {
	return config.getStorage().getCache();
    }
    
    public File getNotWorkingFilesStorage() {
	return notWorkingFilesStorage;
    }

    public File getAlbumsFolder() {
	return albumsFolder;
    }

    public File getUncategorizedFolder() {
	return uncategorizedFolder;
    }

    public File getRemovedPhotosFolder() {
	return removedPhotosFolder;
    }
    
    public File getFailedFolder() {
	return failedPhotosFolder;
    }

    public boolean isShowLivePhotos() {
	return config.isShowLivePhotos();
    }

    public int getLocalNetworkListeningPort() {
	return config.getNetwork().getLocalNASPort();
    }

    public File getImageMagickExecutable() {
	return config.getConverters().getImagemagick().getExecutable();
    }

    public File getHandbrakeExecutable() {
	return config.getConverters().getHandbrake().getExecutable();
    }

    public File getFfmpegExecutable() {
	return config.getConverters().getFfmpeg().getExecutable();
    }

    public HandbrakeConfiguration getHandbrakeConfiguration() {
	return config.getConverters().getHandbrake();
    }

    public boolean isCanConvertApplePhotos() {
	return config.getConverters().getImagemagick().executableExists();
    }
    
    public boolean isCanUseExifTool() {
	return config.getConverters().getExiftool().executableExists();
    }

    public int getNrOfThumbnailsConsumers() {
	return config.getDimensioning().getPhotoThreads();
    }

    public int getNrOfVideoConsumers() {
	return config.getDimensioning().getVideoThreads();
    }

    public int getNrOfConsumers() {
	return getNrOfThumbnailsConsumers() + getNrOfVideoConsumers();
    }
    
    public List<SFTPClientAccount> getSFTPClients() {
	return config.getFeeds().getSftpclients();
    }
    
    public SFTPServerConfig getSFTPServer() {
	return config.getFeeds().getSftpserver();
    }

    public String getPublicUrl() {
	return config.getNetwork().getPublicUrl();
    }

    public int getLocalNASPort() {
	return config.getNetwork().getLocalNASPort();
    }

    public boolean isStartBackgroundTasksAtStartup() {
	return config.getDimensioning().isStartBackgroundTasksAtStartup();
    }

    public boolean isCaching() {
	return config.getDimensioning().isCaching();
    }

    public SpecialDaysProgramsConfiguration getSpecialDaysConfiguration() {
	return config.getSpecialdaysprograms();
    }
    
    public String getDatabaseJndi() {
	return config.getDatabaseJndi();
    }
    
    public boolean isGeoServicesConfigured() {
	String url = config.getGeoServicesURL();
	return url != null && !url.isBlank();
    }
    
    public String getGeoServicesURL() {
	return config.getGeoServicesURL();
    }
    
    public RefreshConfiguration getRefreshConfiguration() {
	return config.getRefresh();
    }
    
    public File getExifToolExecutable() {
	return config.getConverters().getExiftool().getExecutable();
    }

}
