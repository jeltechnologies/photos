package com.jeltechnologies.photos.pictures;

import java.io.File;
import java.io.FilenameFilter;

import javaxt.io.Image;

public class ScanSplitter {
    public static void main(String[] args) {
	new ScanSplitter().start();
    }
    
    public void start() {
	File inputFolder = new File("F:\\tmp\\pdfscans\\70");
	String outputFolderName = "F:\\tmp\\pdfscans\\export\\";
	File[] imageFiles = inputFolder.listFiles(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return name.endsWith(".jpg");
	    }
	});
	
	
	for (File imageFile : imageFiles) {
	    System.out.println(imageFile.getName());
	    javaxt.io.Image image = new javaxt.io.Image(imageFile);
	    
	    
	    image.rotate(-90);
	    
	    //image.saveAs(outputFolderName + "test.jpg");
	    
	    int width = image.getWidth();
	    int half = width / 2;
	    int height = image.getHeight();
	    
	    Image left = image.copyRect(0, 0, half, height);

	    Image right = image.copyRect(half, 0, width, height);
	    right.crop(0, 0, half, height);
	    
	    String leftFileName = outputFolderName + imageFile.getName() + "-1.jpg";
	    String rightFileName = outputFolderName + imageFile.getName() + "-2.jpg";
	    
	    
	    left.saveAs(leftFileName);
	    right.saveAs(rightFileName);
	    
	    
	}
	
	
	
    }

}
