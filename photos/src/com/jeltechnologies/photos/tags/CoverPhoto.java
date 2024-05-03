package com.jeltechnologies.photos.tags;

import com.jeltechnologies.photos.utils.StringUtils;

public class CoverPhoto extends BaseTag {
    private static final long serialVersionUID = 3019513028418601407L;
    private int height = 300;
    private int width = 400;
    
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public void addHTML() throws Exception {
	String photo = getParameter("photo");
	String photoEncoded = StringUtils.encodeURL(photo);
	StringBuilder b = new StringBuilder();
	b.append("<span class=\"image-in-album\">");
	b.append("<a href=\"photo.jsp?photo=").append(photoEncoded);
	b.append("\">");
	b.append("<img src=\"img").append(photo);
	b.append("?height=").append(height).append("&width=").append(width).append("\">");
	b.append("</a>");
	b.append("</span>");
	add(b.toString());
    }
}
