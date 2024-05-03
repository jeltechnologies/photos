package com.jeltechnologies.photos.tags;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.jsp.JspException;

public class MainMenuTag extends BaseTag {

    private static final long serialVersionUID = 5894817743018548586L;

    private String selected;

    private static final List<MenuItem> MENU_ITEMS;

    private record MenuItem(String displayName, String script) {
    };

    static {
	MENU_ITEMS = new ArrayList<MenuItem>();
	MENU_ITEMS.add(new MenuItem("Timeline", "javascript:goto('timeline.jsp');"));
	MENU_ITEMS.add(new MenuItem("Albums", "javascript:goto('albums.jsp');"));
	MENU_ITEMS.add(new MenuItem("Map", "javascript:goto('map.jsp');"));
	MENU_ITEMS.add(new MenuItem("Frame", "javascript:goto('frame.jsp');"));
    }

    public void setSelected(String selected) {
	this.selected = selected;
    }

    @Override
    public void addHTML() throws Exception {
	boolean itemWasSelected = false;
	for (MenuItem item : MENU_ITEMS) {
	    boolean selected = addMenuItem(item);
	    if (selected) {
		itemWasSelected = true;
	    }
	}
	if (selected != null && !selected.isBlank() && !itemWasSelected) {
	    addMenuItem(new MenuItem(selected, "javascript:return false;"));
	}
    }

    private boolean addMenuItem(MenuItem item) throws JspException {
	StringBuilder b = new StringBuilder();
	boolean itemIsSelected = item.displayName.equalsIgnoreCase(selected);
	b.append("<span class=\"");
	if (itemIsSelected) {
	    b.append("view-selected\"><a href=\"").append(item.script()).append("\">");
	} else {
	    b.append("view\"><a href=\"").append(item.script()).append("\">");
	}
	b.append(item.displayName());
	b.append("</a>");
	b.append("</span>");
	addLine(b.toString());
	return itemIsSelected;
    }
}
