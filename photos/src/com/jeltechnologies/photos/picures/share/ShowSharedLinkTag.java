package com.jeltechnologies.photos.picures.share;

import com.jeltechnologies.photos.tags.BaseTag;

public class ShowSharedLinkTag extends BaseTag {
    private static final long serialVersionUID = 1808465237871831420L;

    @Override
    public void addHTML() throws Exception {
	SharedFile shared = (SharedFile) pageContext.getSession().getAttribute(ShareServlet.SESSION_KEY);
	if (shared == null) {
	    add("Error, cannot find shared file from session");
	} else {
	    String html = addUrl(shared.getPublicUrl());
	    addLine(html);
	}
    }

    private String addUrl(String value) {
	StringBuilder b = new StringBuilder();
	String name = "shared-url";
	String inputId = name;
	
	b.append("<label for=\"").append(inputId).append("\">");
	
	b.append("<div class=\"copy-shared-link\">");
	b.append("Share link").append("</label>");
	b.append("<input type=\"text\" id=\"");
	b.append(inputId).append("\" name=\"");
	b.append(inputId).append("\" maxlength=\"255\" size=\"70\" readonly value=\"");
	b.append(value).append("\">");
	
	b.append("<button class=\"copy-shared-link-button\" onclick=\"updateClipboard('");
	b.append(value).append("')\">");
	b.append("Copy");
	b.append("</button>");
	
	b.append("</div>");
	
	return b.toString();
    }

}
