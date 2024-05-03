package com.jeltechnologies.photos.db;

import java.io.Serializable;
import java.util.Objects;

import com.jeltechnologies.photos.picures.map.Coordinate;

public class MapBounds implements Serializable {
    private static final long serialVersionUID = -7393812119545501236L;
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
	builder.append("MapBounds [northEast=");
	builder.append(northEast);
	builder.append(", southWest=");
	builder.append(southWest);
	builder.append("]");
	return builder.toString();
    }

    @Override
    public int hashCode() {
	return Objects.hash(northEast, southWest);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	MapBounds other = (MapBounds) obj;
	return Objects.equals(northEast, other.northEast) && Objects.equals(southWest, other.southWest);
    }
    
    
}
