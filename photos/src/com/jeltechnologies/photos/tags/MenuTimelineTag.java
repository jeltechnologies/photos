package com.jeltechnologies.photos.tags;

import com.jeltechnologies.photos.datatypes.NamedValueCollection;

public class MenuTimelineTag extends MenuTag {
    private static final long serialVersionUID = 7372474259141450094L;

    @Override
    public NamedValueCollection<String> getActions() {
	NamedValueCollection<String> actions = new NamedValueCollection<String>();
	if (user.isAdmin()) {
	    actions.add("Add new", "addNewClicked()");
	}
	return actions;
    }

}
