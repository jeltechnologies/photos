package com.jeltechnologies.photos.config.yaml;

import com.fasterxml.jackson.annotation.JsonProperty;

public class YamlConfiguration {
    private DimensioningConfiguration dimensioning = new DimensioningConfiguration();
    
    private NetworkConfiguration network = new NetworkConfiguration();
    
    private StorageConfiguration storage = new StorageConfiguration();
    
    private ExternalProgramsConfiguration converters = new ExternalProgramsConfiguration();
    
    private FeedsConfiguration feeds = new FeedsConfiguration();

    private SpecialDaysProgramsConfiguration specialdaysprograms = new SpecialDaysProgramsConfiguration();
    
    private RefreshConfiguration refresh = new RefreshConfiguration();
    
    private String mapkey;
    
    @JsonProperty(value = "geoservices-url")
    private String geoServicesURL = "http://localhost:8080/geoservices/address";
    
    @JsonProperty(value = "database-jdni")
    private String databaseJndi = "java:/comp/env/jdbc/photos";
    
    @JsonProperty(value = "show-live-photos")
    private boolean showLivePhotos = true;

    public YamlConfiguration() {
    }
    
    public String getDatabaseJndi() {
        return databaseJndi;
    }

    public void setDatabaseJndi(String databaseJndi) {
        this.databaseJndi = databaseJndi;
    }

    public boolean isShow_live_photos() {
        return isShowLivePhotos();
    }

    public boolean isShowLivePhotos() {
	return showLivePhotos;
    }

    public void setShow_live_photos(boolean show_live_photos) {
        setShowLivePhotos(show_live_photos);
    }

    public void setShowLivePhotos(boolean show_live_photos) {
	this.showLivePhotos = show_live_photos;
    }

    public StorageConfiguration getStorage() {
	return storage;
    }

    public ExternalProgramsConfiguration getConverters() {
	return converters;
    }

    public String getMapkey() {
	return mapkey;
    }

    public FeedsConfiguration getFeeds() {
	return feeds;
    }

    public NetworkConfiguration getNetwork() {
	return network;
    }

    public void setNetwork(NetworkConfiguration network) {
	this.network = network;
    }

    public DimensioningConfiguration getDimensioning() {
	return dimensioning;
    }

    public void setDimensioning(DimensioningConfiguration dimensioning) {
	this.dimensioning = dimensioning;
    }

    public void setStorage(StorageConfiguration storage) {
	this.storage = storage;
    }

    public void setConverters(ExternalProgramsConfiguration converters) {
	this.converters = converters;
    }

    public void setFeeds(FeedsConfiguration feeds) {
	this.feeds = feeds;
    }
    
    public SpecialDaysProgramsConfiguration getSpecialdaysprograms() {
        return specialdaysprograms;
    }

    public void setSpecialdaysprograms(SpecialDaysProgramsConfiguration specialdaysprograms) {
        this.specialdaysprograms = specialdaysprograms;
    }

    public void setMapkey(String mapkey) {
	this.mapkey = mapkey;
    }
    
    public String getGeoServicesURL() {
        return geoServicesURL;
    }

    public void setGeoServicesURL(String geoServicesURL) {
        this.geoServicesURL = geoServicesURL;
    }
    
    public RefreshConfiguration getRefresh() {
        return refresh;
    }

    public void setRefresh(RefreshConfiguration refresh) {
        this.refresh = refresh;
    }

    @Override
    public String toString() {
	return "YamlConfiguration [dimensioning=" + dimensioning + ", network=" + network + ", storage=" + storage + ", converters=" + converters + ", feeds="
		+ feeds + ", specialdaysprograms=" + specialdaysprograms + ", refresh=" + refresh + ", mapkey=" + mapkey + ", geoServicesURL=" + geoServicesURL
		+ ", databaseJndi=" + databaseJndi + ", showLivePhotos=" + showLivePhotos + "]";
    }

}
