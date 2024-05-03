package com.jeltechnologies.photos.datatypes;

public class MovieQuality {
    private final Type type;

    public enum Type {
	LOW, HIGH, ORIGINAL;
    }

    public MovieQuality(Type type) {
	super();
	this.type = type;
    }

    public MovieQuality(String typeName) {
	if (typeName == null) {
	    type = Type.ORIGINAL;
	} else {
	    if (typeName.equalsIgnoreCase(Type.LOW.toString())) {
		type = Type.LOW;
	    } else {
		if (typeName.equalsIgnoreCase(Type.HIGH.toString())) {
		    type = Type.HIGH;
		} else {
		    type = Type.ORIGINAL;
		}
	    }
	}
    }

    public Type getType() {
	return type;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("MovieQuality [type=");
	builder.append(type);
	builder.append("]");
	return builder.toString();
    }
}
