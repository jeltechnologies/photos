package com.jeltechnologies.photos.pictures;

public class ThumbnailUtilsFactory {
    public static ThumbnailUtils getUtils() {
	return new ThumbnailUtilsImpl();
    }
}
