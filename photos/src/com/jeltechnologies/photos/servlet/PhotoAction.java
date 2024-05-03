package com.jeltechnologies.photos.servlet;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.datatypes.usermodel.User;

public class PhotoAction implements Serializable {
    private static final long serialVersionUID = -306039276398294657L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoAction.class);

    private Action action;
    private String id;
    private User user;

    public enum Action {
	REMOVE_FROM_ALBUM, SHOW, HIDE, ROTATE_RIGHT, ROTATE_LEFT, FLIP_HORIZONTAL, FLIP_VERTICAL, NOT_SUPPORTED;
    }

    public PhotoAction() {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("Instantiated");
	}
    }

    public Action getAction() {
	return action;
    }

    public void setAction(String actionName) {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("setAction (" + actionName + ")");
	}
	Action a = Action.NOT_SUPPORTED;
	if (actionName.equals("remove-from-album")) {
	    a = Action.REMOVE_FROM_ALBUM;
	} else {
	    if (actionName.equals("rotate-right")) {
		a = Action.ROTATE_RIGHT;
	    } else {
		if (actionName.equals("rotate-left")) {
		    a = Action.ROTATE_LEFT;
		} else {
		    if (actionName.equals("flip-horizontal")) {
			a = Action.FLIP_HORIZONTAL;
		    } else {
			if (actionName.equals("flip-vertical")) {
			    a = Action.FLIP_VERTICAL;
			} else {
			    if (actionName.equals("show")) {
				a = Action.SHOW;
			    } else {
				if (actionName.equals("hide")) {
				    a = Action.HIDE;
				}
			    }
			}
		    }
		}
	    }
	}
	this.action = a;
    }

    public String getId() {
	return id;
    }

    public void setId(String payload) {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("setId(" + payload + ")");
	}
	this.id = payload;
    }

    public User getUser() {
	return user;
    }

    public void setUser(User user) {
	this.user = user;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("PhotoAction [action=");
	builder.append(action);
	builder.append(", id=");
	builder.append(id);
	builder.append(", user=");
	builder.append(user);
	builder.append("]");
	return builder.toString();
    }

}
