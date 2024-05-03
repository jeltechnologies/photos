package com.jeltechnologies.photos.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.pictures.PhotoRotation;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

import javaxt.io.Image;

public class ImageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);

    public static BufferedImage createThumb(BufferedImage image, int picWidth, int picHeight) throws IOException {
	int imageWidth = image.getWidth(null);
	int imageHeight = image.getHeight(null);

	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("Max width is: " + picWidth + ", height is: " + picHeight + " # Original width is: " + imageWidth + ", height: " + imageHeight);
	}

	double thumbRatio = (double) picWidth / (double) picHeight;
	double imageRatio = (double) imageWidth / (double) imageHeight;

	if (thumbRatio < imageRatio) {
	    picHeight = (int) (picWidth / imageRatio);
	} else {
	    picWidth = (int) (picHeight * imageRatio);
	}

	if ((imageWidth < picWidth) && (imageHeight < picHeight)) {
	    picWidth = imageWidth;
	    picHeight = imageHeight;
	} else if (imageWidth < picWidth) {
	    picWidth = imageWidth;
	} else if (imageHeight < picHeight) {
	    picHeight = imageHeight;
	}

	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(".. Resized width is: " + picWidth + ", height is: " + picHeight);
	}

	ResampleOp resampleOp = new ResampleOp(picWidth, picHeight);
	resampleOp.setFilter(ResampleFilters.getLanczos3Filter());
	BufferedImage thumb = resampleOp.filter(image, null);
	return thumb;
    }

    public static BufferedImage createMaximizedThumb(BufferedImage source, int minWidth, int minHeight) throws IOException {
	int originalWidth = source.getWidth();
	int originalHeight = source.getHeight();

	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("Min width is: " + minWidth + ", height is: " + minHeight + " # Original width is: " + originalWidth + ", height: " + originalHeight);
	}

	int thumbWidth;
	int thumbHeight;
	float percentage;
	if (originalWidth > originalHeight) {
	    thumbWidth = minWidth;
	    percentage = ((float) minWidth / originalWidth);
	    thumbHeight = (int) (originalHeight * percentage);
	} else {
	    thumbHeight = minHeight;
	    percentage = ((float) minHeight / originalHeight);
	    thumbWidth = (int) (originalWidth * percentage);
	}
	if (thumbHeight > minHeight) {
	    thumbHeight = minHeight;
	    percentage = ((float) minHeight / originalHeight);
	    thumbWidth = (int) (originalWidth * percentage);
	}

	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("Resized thumb width is: " + thumbWidth + ", height: " + thumbHeight + ", percentage: " + percentage);
	}

	ResampleOp resampleOp = new ResampleOp(thumbWidth, thumbHeight);
	resampleOp.setFilter(ResampleFilters.getLanczos3Filter());
	BufferedImage thumb = resampleOp.filter(source, null);
	return thumb;
    }

    public static BufferedImage concatImagesHorizontally(List<BufferedImage> images) throws IOException {
	BufferedImage resultImage = null;
	if (!images.isEmpty()) {
	    int resultWidth = 0;
	    int height = images.get(0).getHeight();
	    for (BufferedImage image : images) {
		resultWidth = resultWidth + image.getWidth();
	    }
	    resultImage = new BufferedImage(resultWidth, height, BufferedImage.TYPE_INT_RGB);
	    int x = 0;
	    for (int index = 0; index < images.size(); index++) {
		BufferedImage currentImage = images.get(index);
		resultImage.createGraphics().drawImage(currentImage, x, 0, null);
		x = x + currentImage.getWidth();
	    }
	}
	return resultImage;
    }

    public static BufferedImage getWhiteImage(int width, int height) throws IOException {
	BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	Graphics2D graphics = img.createGraphics();
	graphics.setBackground(Color.WHITE);
	graphics.clearRect(0, 0, width, height);
	return img;
    }

    public static BufferedImage sharpen(BufferedImage image) {
	Kernel kernel = new Kernel(3, 3, new float[] { -1, -1, -1, -1, 9, -1, -1, -1, -1 });
	BufferedImageOp op = new ConvolveOp(kernel);
	return op.filter(image, null);
    }

    public static BufferedImage blur(BufferedImage image) {
	Kernel kernel = new Kernel(3, 3, new float[] { 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f });
	BufferedImageOp op = new ConvolveOp(kernel);
	return op.filter(image, null);
    }

    public static Image mirrorHorizontally(Image image) {
	BufferedImage bufferedImage = horizontalFlip(image.getBufferedImage());
	Image result = new Image(bufferedImage);
	return result;
    }

    public static Image mirrorVertically(Image image) {
	BufferedImage bufferedImage = verticalFlip(image.getBufferedImage());
	Image result = new Image(bufferedImage);
	return result;
    }

    /**
     * This method flips the image horizontally
     * 
     * @param img --> BufferedImage Object to be flipped horizontally
     * @return
     */
    private static BufferedImage horizontalFlip(BufferedImage img) {
	int w = img.getWidth();
	int h = img.getHeight();
	BufferedImage dimg = new BufferedImage(w, h, img.getType());
	Graphics2D g = dimg.createGraphics();
	/*
	 * img - the specified image to be drawn. This method does nothing if img is null. dx1 - the x coordinate of the first
	 * corner of the destination rectangle. dy1 - the y coordinate of the first corner of the destination rectangle. dx2 -
	 * the x coordinate of the second corner of the destination rectangle. dy2 - the y coordinate of the second corner of
	 * the destination rectangle. sx1 - the x coordinate of the first corner of the source rectangle. sy1 - the y coordinate
	 * of the first corner of the source rectangle. sx2 - the x coordinate of the second corner of the source rectangle. sy2
	 * - the y coordinate of the second corner of the source rectangle. observer - object to be notified as more of the
	 * image is scaled and converted.
	 *
	 */
	g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);
	g.dispose();
	return dimg;
    }

    /**
     * This method flips the image vertically
     * 
     * @param img --> BufferedImage object to be flipped
     * @return
     */
    private static BufferedImage verticalFlip(BufferedImage img) {
	int w = img.getWidth();
	int h = img.getHeight();
	BufferedImage dimg = new BufferedImage(w, h, img.getColorModel().getTransparency());
	Graphics2D g = dimg.createGraphics();
	g.drawImage(img, 0, 0, w, h, 0, h, w, 0, null);
	g.dispose();
	return dimg;
    }

    public static Image fixOrientation(File file, PhotoRotation rotation) {
	Image image;
	image = new Image(file);
	if (image != null) {
	    switch (rotation) {
		case HORIZONTAL_NORMAL:
		    // Do nothing
		    break;
		case MIRROR_HORIZONTAL:
		    image = ImageUtils.mirrorHorizontally(image);
		    break;
		case MIRROR_HORIZONTAL_AND_ROTATE_270_CW:
		    image = ImageUtils.mirrorHorizontally(image);
		    image.rotate(270);
		    break;
		case MIRROR_HORIZONTAL_AND_ROTATE_90_CW:
		    image = ImageUtils.mirrorHorizontally(image);
		    image.rotate(270);
		    break;
		case MIRROR_VERTICAL:
		    image = ImageUtils.mirrorVertically(image);
		    break;
		case ROTATE_180:
		    image.rotate(180);
		    break;
		case ROTATE_270_CW:
		    image.rotate(270);
		    break;
		case ROTATE_90_CW:
		    image.rotate(90);
		    break;
		default:
		    break;
	    }
	}
	return image;
    }
}