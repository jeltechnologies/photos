package com.jeltechnologies.photos.pictures;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.Dimension;
import com.jeltechnologies.photos.datatypes.MovieQuality;
import com.jeltechnologies.photos.datatypes.MovieQuality.Type;

public class ThumbnailUtilsImpl implements ThumbnailUtils {
    private final static Environment ENV = Environment.INSTANCE;
    private final static String CACHE_FOLDER_NAME = ENV.getConfig().getCacheFolder().getAbsolutePath();

    @Override
    public List<File> getAllGeneratedThumbnailFiles(Photo photo) {
	List<File> files = new ArrayList<File>();
	for (Dimension dimension :ENV.getAllThumbnailDimensions()) {
	    files.add(getThumbFile(dimension, photo));
	} 
	if (photo.getType() == MediaType.VIDEO) {
	    files.add(getConvertedMovie(new MovieQuality(Type.HIGH) , photo));
	    files.add(getConvertedMovie(new MovieQuality(Type.LOW) , photo));
	}
	return files;
    }

    @Override
    public File getThumbFile(Dimension inDimension, Photo photo) {
	Dimension dimension;
	if (photo.getType() == MediaType.VIDEO && inDimension.getType().equals(Dimension.Type.ORIGINAL)) {
	    dimension = ENV.getDimensionFullscreen(); 
	} else {
	    dimension = inDimension;
	}
	StringBuilder b = getCachePrefix(photo);
	b.append("-").append(dimension.getFileAddendum()).append(".jpg");
	return new File(b.toString());
    }
    
    private File getConvertedMovieFile(Photo movie, String quality) {
	StringBuilder b = getCachePrefix(movie);
	b.append("-").append(quality).append(".mp4");
	return new File(b.toString());
    }
    
    @Override
    public File getCacheFolder(Photo photo) {
	StringBuilder folderName = getCacheFolderPath(photo);
	return new File(folderName.toString());
    }

    private StringBuilder getCachePrefix(Photo photo) {
	return getCacheFolderPath(photo).append("/").append(photo.getId());
    }
    
    private StringBuilder getCacheFolderPath(Photo photo) {
	return new StringBuilder(CACHE_FOLDER_NAME).append("/").append(photo.getId());
    }
    
    @Override
    public File getConvertedMovie(MovieQuality quality, Photo movie) {
	File file;
	switch (quality.getType()) {
	    case HIGH: {
		file = getConvertedMovieFile(movie, "high");
		break;
	    }
	    case LOW: {
		file = getConvertedMovieFile(movie, "low");
		break;
	    }
	    case ORIGINAL: {
		file = Environment.INSTANCE.getFile(movie.getRelativeFileName());
		break;
	    }
	    default: {
		throw new IllegalArgumentException("Not supported quality: " + quality.getType());
	    }
	}
	return file;
    }

}
