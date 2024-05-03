package com.jeltechnologies.photos.tags;

import com.jeltechnologies.photos.datatypes.NamedValueCollection;

public class MenuNewTag extends MenuTag {

    private static final long serialVersionUID = -8520865165162671475L;

    @Override
    public NamedValueCollection<String> getActions() {
	NamedValueCollection<String> actions = new NamedValueCollection<String>();
	if (user.isAdmin()) {
	    actions.add("Get latest...", "userClickedGetLatest();");
	}
	return actions;
    }

}