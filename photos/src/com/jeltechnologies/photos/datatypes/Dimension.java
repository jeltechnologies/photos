package com.jeltechnologies.photos.datatypes;

import java.io.Serializable;

public class Dimension implements Serializable {
    private static final long serialVersionUID = -5979124128559255523L;
    private final int height;
    private final int width;
    private final Type type;

    public enum Type {
	SMALL, MEDIUM, ORIGINAL;
    }

    public Dimension(int width, int height, Type type) {
	this.height = height;
	this.width = width;
	this.type = type;
    }

    public Dimension(int width, int height) {
	this.height = height;
	this.width = width;
	this.type = null;
    }

    public int getHeight() {
	return height;
    }

    public int getWidth() {
	return width;
    }

    public Type getType() {
	return type;
    }
    
    public String getFileAddendum() {
	return type.toString().toLowerCase();
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("Dimension [width=");
	builder.append(width);
	builder.append(", height=");
	builder.append(height);
	builder.append(", type=");
	builder.append(type);
	builder.append("]");
	return builder.toString();
    }

}