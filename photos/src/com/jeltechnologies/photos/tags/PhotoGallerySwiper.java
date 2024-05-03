package com.jeltechnologies.photos.tags;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Settings;
import com.jeltechnologies.photos.db.Database;
import com.jeltechnologies.photos.db.OrderBy;
import com.jeltechnologies.photos.db.TimePeriod;
import com.jeltechnologies.photos.gallery.GalleryLogic;
import com.jeltechnologies.photos.pictures.MediaType;
import com.jeltechnologies.photos.pictures.Photo;
import com.jeltechnologies.photos.utils.JSONUtilsFactory;
import com.jeltechnologies.photos.utils.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

public class PhotoGallerySwiper extends BaseTag {
    private static final long serialVersionUID = 6374150373820377055L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoGallerySwiper.class);
    private static final String LEADING_SPACES = StringUtils.dup(10, ' ');
    public static final String MEDIA_SLIDE_ID_PREFIX_MOVIE = "slide-video-";
    public static final String MEDIA_SLIDE_ID_PREFIX_PHOTO = "slide-photo-";
    private final static String videoQuality = "high";

    @Override
    public void addHTML() throws Exception {
	Database database;
	database = new Database();
	try {
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("doStartTag");
	    }

	    HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
	    String photoUrl = request.getParameter("photo");
	    String albumUrl = request.getParameter("album");
	    String from = request.getParameter("from");
	    String to = request.getParameter("to");
	    
	    TimePeriod period = null;
	    if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {
		LocalDate fromDate = LocalDate.parse(from);
		LocalDate toDate = LocalDate.parse(to);
		period = new TimePeriod();
		period.setFrom(fromDate);
		period.setTo(toDate);
	    }

	    String sort = request.getParameter("sort");
	    OrderBy orderBy = RequestParameterParser.getOrderByFromParameter(sort);
	    String mediaTypeString = request.getParameter("mediatype");
	    MediaType mediaType = RequestParameterParser.getMediaTypeFromParameter(mediaTypeString);

	    if (photoUrl == null || photoUrl.isEmpty()) {
		throw new IllegalArgumentException("PHOTO URL expected");
	    }

	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("PHOTO URL: " + photoUrl);
	    }

	    GalleryLogic galleryLogic = new GalleryLogic(user, database);
	    
	    galleryLogic.setAlbumUrl(albumUrl);
	    galleryLogic.setMediaType(mediaType);
	    galleryLogic.setOrderBy(orderBy);
	    galleryLogic.setPeriod(period);
	    galleryLogic.setPhoto(photoUrl);
	    
	    galleryLogic.setMaximumAmount(Settings.get(request.getSession()).getMaximumAmountInGallary());

	    List<Photo> photos = galleryLogic.getPhotos();
	    createGallery(photos);
	    setPhotosJsonAsAttribute(photos);

	} finally {
	    if (database != null) {
		database.close();
	    }
	    database = null;
	}
    }

    protected void setPhotosJsonAsAttribute(List<Photo> photos) {
	String json;
	try {
	    json = JSONUtilsFactory.getInstance().toJSON(photos);
	    pageContext.setAttribute("photos-in-swiper-javascript", json);
	} catch (Exception e) {
	    LOGGER.warn("Cannot set photos-in-swiper-javascript pageAttribute to json", e);
	}
    }

    private void createGallery(List<Photo> photos) throws Exception {
	addLine("");
	final boolean IN_SLIDE_SHOW = false;

	int slideIndex = 1;
	for (Photo photo : photos) {
	    StringBuilder lb = new StringBuilder();
	    lb.append(LEADING_SPACES).append("<div class=\"swiper-slide\">");
	    addLine(lb);
	    
	    add(LEADING_SPACES);
	    addLine("  <div class=\"swiper-zoom-container\">");

	    switch (photo.getType()) {
		case PHOTO: {
		    StringBuilder b = new StringBuilder();
		    b = new StringBuilder();
		    b.append(LEADING_SPACES).append("  <img ");
		    b.append("id=\"").append(MEDIA_SLIDE_ID_PREFIX_PHOTO).append(slideIndex).append("\" ");
		    b.append("src=\"");
		    b.append("img?id=").append(photo.getId()).append("&size=small").append("\" loading=\"lazy\"/>");
		    b.append("<span class=\"title\"><span class=\"title-text\" id=\"title-").append(slideIndex).append("\"></span></span>");
		    addLine(b);
		    break;
		}
		case VIDEO: {
		    StringBuilder b = new StringBuilder();
		    String tag = generateVideoTagHTML5(photo, IN_SLIDE_SHOW, -1, -1, slideIndex);
		    b.append(LEADING_SPACES).append(" ").append(tag);
		    addLine(b);
		    break;

		}
		default:
		    break;
	    }
	    
	    add(LEADING_SPACES);
	    addLine("</div>");
	    lb = new StringBuilder();
	    lb.append(LEADING_SPACES).append("</div>");
	    addLine(lb);
	    slideIndex++;
	}
    }

    protected String generateVideoTagHTML5(Photo video, boolean inSlideShow, int startAtSeconds, int endAtSecond, int slideIndex) {
	boolean preload;
	boolean muted;
	boolean loop;
	boolean controls;
	boolean autoplay;
	boolean poster;

	if (inSlideShow) {
	    preload = false;
	    muted = true;
	    loop = true;
	    controls = false;
	    autoplay = false;
	    poster = false;
	} else {
	    preload = false;
	    muted = false;
	    loop = false;
	    controls = true;
	    autoplay = false;
	    poster = true;
	}

	String posterImage = "img?id=" + video.getId();
	StringBuilder b = new StringBuilder();

	b.append("<video");
	b.append(" id=\"").append(MEDIA_SLIDE_ID_PREFIX_MOVIE).append(slideIndex).append("\"");

	if (controls) {
	    b.append(" controls");
	}
	if (autoplay) {
	    b.append(" autoplay");
	}
	if (!preload) {
	    b.append(" preload=\"none\"");
	} else {
	    b.append(" preload=\"auto\"");
	}
	if (muted) {
	    b.append(" muted");
	}

	if (loop) {
	    b.append(" loop");
	}
	if (poster) {
	    b.append(" poster=\"").append(posterImage).append("\"");
	}

	b.append(">");

	StringBuilder ub = new StringBuilder();
	ub.append("video?id=").append(video.getId()).append("&quality=").append(videoQuality);
	if (startAtSeconds > -1 && endAtSecond > -1) {
	    ub.append("#t=").append(startAtSeconds).append(",").append(endAtSecond);
	}

	b.append("<source src=\"").append(ub.toString()).append("\" type=\"video/mp4\">");
	b.append("</video>");
	return b.toString();
    }

}
