package com.jeltechnologies.photos.picures.frame.program;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.Query;
import com.jeltechnologies.photos.db.Query.InAlbum;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.picures.frame.FilterOption;
import com.jeltechnologies.photos.utils.StringUtils;

public abstract class BaseFrameProgram {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseFrameProgram.class);
    protected static final String ROOT_ALBUMS = Environment.INSTANCE.getRelativeRootAlbums();
    private final String name;
    private final String description;
    private Random random = new Random();
    private final boolean shuffleResults;

    public BaseFrameProgram(String name, String description) {
	this.name = StringUtils.stripSpaces(name);
	this.description = description;
	this.shuffleResults = true;
    }

    public BaseFrameProgram(String name, String description, boolean shuffleResult) {
	this.name = StringUtils.stripSpaces(name);
	this.description = description;
	this.shuffleResults = shuffleResult;
    }

    public final String getName() {
	return name;
    }

    public final String getDescription() {
	return description;
    }

    public final List<Photo> getPhotos(FilterOption options) throws IOException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(this.getClass().getSimpleName() + " getPhotos " + options);
	}
	Database db = null;
	List<Photo> allPhotos = null;
	List<Photo> allPhotosInProgram = null;
	try {
	    db = new Database();
	    Photo photoInSlideShow = null;
	    String photoId = options.photoIdInSlideShow();
	    if (photoId != null && !photoId.isBlank()) {
		photoInSlideShow = db.getFirstPhotoById(options.user(), photoId);
	    }
	    Query allPhotosQuery = new Query(options.user());
	    allPhotosQuery.setRelativeFolderName(ROOT_ALBUMS);
	    allPhotosQuery.setIncludeSubFolders(true);
	    allPhotosQuery.setIncludeHidden(false);
	    allPhotosQuery.setOrderBy(OrderBy.DATE_TAKEN_OLDEST);
	    allPhotosQuery.setInAlbums(InAlbum.IN_ALBUM_NO_DUPLICATES);
	    allPhotos = db.query(allPhotosQuery);
	    allPhotosInProgram = applyFilter(allPhotos, photoInSlideShow);
	} catch (SQLException e) {
	    throw new IOException("Cannot get photos because " + e.getMessage(), e);
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
	List<Photo> result;
	switch (options.contents()) {
	    case PROGRAM_ONLY:
		result = selectRandomPhotosFromList(options.amount(), allPhotosInProgram);
		break;
	    case PROGRAM_WITH_RANDOM:
		result = selectProgramWithRandom(options, allPhotosInProgram, allPhotos);
		break;
	    case RANDOM_WITH_PROGRAM:
		result = selectRandomPhotosAddWithinPhotosFromProgram(options, allPhotosInProgram, allPhotos);
		break;
	    default:
		throw new IllegalStateException("Unkown contents: " + options.contents());
	}
	return result;
    }

    private List<Photo> selectRandomPhotosFromList(int amount, List<Photo> photos) {
	List<Photo> result = new ArrayList<Photo>(photos.size());
	int maxRandomAttempts = amount * 10;
	for (int i = 0; i < amount && i < photos.size(); i++) {
	    Photo found = null;
	    for (int attempt = 0; found == null && attempt < maxRandomAttempts; attempt++) {
		int nextRandomIndex = random.nextInt(photos.size());
		Photo candidate = photos.get(nextRandomIndex);
		if (!result.contains(candidate)) {
		    found = candidate;
		}
	    }
	    if (found == null) {
		LOGGER.warn("Cannot find candidate after " + maxRandomAttempts + " attempts");
	    } else {
		result.add(found);
	    }
	}
	return result;
    }

    private List<Photo> selectProgramWithRandom(FilterOption options, List<Photo> allPhotosInProgram, List<Photo> allPhotos) {
	List<Photo> result;
	int wantedTotal = options.amount();
	int percentage = options.programPercentage();
	List<Photo> randomFromProgram = selectRandomPhotosFromList(wantedTotal, allPhotosInProgram);

	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("wantedTotal        : " + wantedTotal);
	    LOGGER.trace("percentage         : " + percentage + "%");
	    LOGGER.trace("randomFromProgram  : " + randomFromProgram.size() + " photos");
	}

	List<Photo> randomFromAll;
	if (randomFromProgram.size() >= wantedTotal) {
	    int wantedInProgram = new BigDecimal(wantedTotal * percentage).divide(new BigDecimal(100), RoundingMode.HALF_UP).intValueExact();
	    int wantedFromAllPhotos = wantedTotal - wantedInProgram;
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("wantedInProgram    : " + wantedInProgram);
		LOGGER.trace("wantedFromAllPhotos: " + wantedFromAllPhotos);
	    }
	    randomFromAll = selectRandomPhotosFromList(wantedFromAllPhotos, allPhotos);
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("randomFromAll      : " + randomFromAll.size() + " photos");
	    }
	} else {
	    MathContext mc = new MathContext(0, RoundingMode.HALF_UP);
	    BigDecimal hundred = new BigDecimal(100, mc);
	    BigDecimal percentageBd = new BigDecimal(percentage, mc);
	    BigDecimal randomFromProgramSize = new BigDecimal(randomFromProgram.size(), mc);
	    randomFromProgramSize.setScale(0);
	    BigDecimal percentageDevidedBy100 = percentageBd.divide(hundred).setScale(2);
	    BigDecimal totalNeeded = randomFromProgramSize.divide(percentageDevidedBy100, RoundingMode.HALF_UP);
	    int total = totalNeeded.round(new MathContext(0, RoundingMode.HALF_UP)).intValueExact();
	    int randomFromAllNeeded = total - randomFromProgram.size();
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("totalNeeded        : " + total);
		LOGGER.trace("randomFromAllNeeded: " + randomFromAllNeeded);
	    }
	    randomFromAll = selectRandomPhotosFromList(randomFromAllNeeded, allPhotos);
	}

	List<Photo> preResult = new ArrayList<Photo>(wantedTotal);
	if (randomFromProgram.size() == 0) {
	    preResult.addAll(randomFromProgram);
	    preResult.addAll(randomFromAll);
	    if (shuffleResults) {
		Collections.shuffle(preResult);
	    }
	} else {
	    preResult.add(randomFromProgram.get(0));
	    List<Photo> remaining = new ArrayList<>(options.amount());
	    for (int i = 1; i < randomFromProgram.size(); i++) {
		remaining.add(randomFromProgram.get(i));
	    }
	    remaining.addAll(randomFromAll);
	    if (shuffleResults) {
		Collections.shuffle(remaining);
	    }
	    preResult.addAll(remaining);
	}

	result = new ArrayList<Photo>(wantedTotal);
	for (int i = 0; i < wantedTotal && i < preResult.size(); i++) {
	    result.add(preResult.get(i));
	}

	return result;
    }

    private List<Photo> selectRandomPhotosAddWithinPhotosFromProgram(FilterOption options, List<Photo> allPhotosInProgram, List<Photo> allPhotos) {
	List<Photo> randomPhotosInProgram = selectRandomPhotosFromList(options.amount(), allPhotosInProgram);
	int randomProgramPhotos = randomPhotosInProgram.size();

	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("randomProgramPhotos: " + randomProgramPhotos);
	}

	int percentage = options.programPercentage();
	BigDecimal oneProcent;
	if (percentage > 0) {
	    oneProcent = new BigDecimal(randomProgramPhotos).divide(new BigDecimal(percentage), 2, RoundingMode.HALF_UP);
	} else {
	    oneProcent = new BigDecimal(0);
	}
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("onePercent: " + oneProcent);
	}

	BigDecimal hundredPercentBD = oneProcent.multiply(new BigDecimal(100));

	int hundredPercent = hundredPercentBD.setScale(0, RoundingMode.HALF_UP).intValueExact();
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("hundredPercent: " + hundredPercent);
	}

	// int wantedRandomAmount = hundredPercent - randomProgramPhotos;
	int wantedRandomAmount = options.amount() - randomProgramPhotos;
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("wantedRandomAmount : " + wantedRandomAmount);
	}

	List<Photo> randomFillerPhotos = selectRandomPhotosFromList(wantedRandomAmount, allPhotos);
	List<Photo> result = new ArrayList<Photo>(options.amount());

	if (randomPhotosInProgram.size() == 0) {
	    result.addAll(randomPhotosInProgram);
	    result.addAll(randomFillerPhotos);
	    Collections.shuffle(result);
	} else {
	    result.add(randomPhotosInProgram.get(0));
	    List<Photo> remaining = new ArrayList<>(options.amount());
	    for (int i = 1; i < randomPhotosInProgram.size(); i++) {
		remaining.add(randomPhotosInProgram.get(i));
	    }
	    remaining.addAll(randomFillerPhotos);
	    Collections.shuffle(remaining);
	    result.addAll(remaining);
	}

	return result;

    }

    private List<Photo> applyFilter(List<Photo> photos, Photo photoInSlideShow) {
	List<Photo> filtered = new ArrayList<Photo>(photos.size());
	for (Photo p : photos) {
	    if (isInProgram(p, photoInSlideShow)) {
		filtered.add(p);
	    }
	}
	return filtered;
    }

    protected final LocalDate getDate(Photo photo) {
	return photo.getDateTaken().toLocalDate();
    }

    /**
     * Override to interact with photos in the slide show, for example to find simular photos
     * 
     * @param photo
     * @param photoInSlideShow
     * @return
     */
    protected boolean isInProgram(Photo photo, Photo photoInSlideShow) {
	return isInProgram(photo);
    }

    /**
     * Override in case interacting with photo in slideshow is not needed
     * 
     * @param photo
     * @param photoInSlideShow
     * @return
     */
    protected boolean isInProgram(Photo photo) {
	return false;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("BaseFrameProgram [name=");
	builder.append(name);
	builder.append("]");
	return builder.toString();
    }

}
