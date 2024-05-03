package com.jeltechnologies.photos.utils;

public class JSONUtilsFactory {
    
    public static JSONUtils getInstance() {
	return new JSONUtilsJackson();
    }

}
