package com.jeltechnologies.photos.picures.map;

import java.io.Serializable;

import jakarta.servlet.http.HttpSession;

public class MapView implements Serializable {
    private static final long serialVersionUID = 363543187017022465L;
    private static final String SESSION_KEY = MapView.class.getName();
    
    private int zoom;
    private Coordinate mapCenter;
    
    public static final Coordinate PAGANINILAAN = new Coordinate("53.00994629456384", "6.5629816958232565");
    
    // 59.37839970213399, 18.067625420869895
    public static final Coordinate STOCKHOLM = new Coordinate("59.4597451", "18.0524787");
    
    public static MapView getMapView(HttpSession session) {
	MapView view = (MapView) session.getAttribute(SESSION_KEY);
	if (view == null) {
	    view = new MapView();
	    session.setAttribute(SESSION_KEY, view);
	}
	return view;
    }
    
    private MapView() {
	setDefaults();
    }
    
    // 59.83839219818664, longitude=20.04175357974049
    private void setDefaults() {
	mapCenter = STOCKHOLM;
	zoom = MapServlet.DEFAULT_ZOOM;
    }

    public int getZoom() {
	return zoom;
    }

    public void setZoom(int zoom) {
	this.zoom = zoom;
    }

    public Coordinate getMapCenter() {
	return mapCenter;
    }

    public void setMapCenter(Coordinate coordinate) {
	this.mapCenter = coordinate;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("MapView [zoom=");
	builder.append(zoom);
	builder.append(", coordinate=");
	builder.append(mapCenter);
	builder.append("]");
	return builder.toString();
    }

}
