package com.jeltechnologies.photos.picures.map;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class Coordinate implements Serializable {
    private static final long serialVersionUID = 2757112224828629897L;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public Coordinate() {
    }

    public Coordinate(BigDecimal lat, BigDecimal lng) {
	this.latitude = lat;
	this.longitude = lng;
    }

    public Coordinate(String lat, String lng) {
	this.latitude = new BigDecimal(lat);
	this.longitude = new BigDecimal(lng);
    }

    public BigDecimal getLatitude() {
	return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
	this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
	return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
	this.longitude = longitude;
    }

    public BigDecimal getDistanceFrom(Coordinate other) {
	double result = calculateDistance(latitude.doubleValue(), longitude.doubleValue(), other.getLatitude().doubleValue(), other.getLongitude().doubleValue());
	return new BigDecimal(result);
    }
    
    private double haversine(double val) {
	return Math.pow(Math.sin(val / 2), 2);
    }

    private double calculateDistance(double startLat, double startLong, double endLat, double endLong) {
	double dLat = Math.toRadians((endLat - startLat));
	double dLong = Math.toRadians((endLong - startLong));
	startLat = Math.toRadians(startLat);
	endLat = Math.toRadians(endLat);
	double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
	double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	final int EARTH_RADIUS = 6371;
	return EARTH_RADIUS * c;
    }

    @Override
    public int hashCode() {
	return Objects.hash(latitude, longitude);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Coordinate other = (Coordinate) obj;
	return Objects.equals(latitude, other.latitude) && Objects.equals(longitude, other.longitude);
    }
    
    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("[ ").append(latitude);
	builder.append(", ");
	builder.append(longitude).append(" ]");
	return builder.toString();
    }
}
