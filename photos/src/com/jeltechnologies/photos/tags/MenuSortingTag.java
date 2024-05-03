package com.jeltechnologies.photos.tags;

import com.jeltechnologies.photos.datatypes.NamedValueCollection;

public class MenuSortingTag extends MenuTag {
    private static final long serialVersionUID = 1223249364187693172L;

    @Override
    public NamedValueCollection<String> getActions() {
	NamedValueCollection<String> actions = new NamedValueCollection<String>();
	
	actions.add("Random", "viewClicked('day')");
	actions.add("Newst first", "viewClicked('week')");
	actions.add("Oldest first", "viewClicked('month')");
	return actions;
    }

}
