package com.jeltechnologies.photos.picures.map;

import java.io.Serializable;

public class MapBoundsRequest implements Serializable {
    private static final long serialVersionUID = -8302271523586333360L;
    private Coordinate northEast;
    private Coordinate southWest;

    public Coordinate getNorthEast() {
	return northEast;
    }

    public void setNorthEast(Coordinate northEast) {
	this.northEast = northEast;
    }

    public Coordinate getSouthWest() {
	return southWest;
    }

    public void setSouthWest(Coordinate southWest) {
	this.southWest = southWest;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("MapBoundsRequest [northEast=");
	builder.append(northEast);
	builder.append(", southWest=");
	builder.append(southWest);
	builder.append("]");
	return builder.toString();
    }

}
