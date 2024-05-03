package com.jeltechnologies.photos.pictures;

import java.io.File;
import java.util.List;

import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.datatypes.MovieQuality;

public interface ThumbnailUtils {
    List<File> getAllGeneratedThumbnailFiles(Photo photo);
    
    File getThumbFile(Dimension dimension, Photo photo);

    File getConvertedMovie(MovieQuality quality, Photo movie);
    
    File getCacheFolder(Photo photo);
}