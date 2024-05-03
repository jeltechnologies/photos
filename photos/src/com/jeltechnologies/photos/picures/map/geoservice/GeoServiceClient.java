package com.jeltechnologies.photos.picures.map.geoservice;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;

import com.jeltechnologies.geoservices.datamodel.AddressRequest;
import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.utils.JSONUtilsFactory;

public class GeoServiceClient implements Callable<AddressRequest> {
    private static final String REST_SERVICE = Environment.INSTANCE.getConfig().getGeoServicesURL();

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private final Coordinates coordinates;

    public GeoServiceClient(Coordinates coordinates) {
	this.coordinates = coordinates;
    }

    public AddressRequest call() throws InterruptedException, IOException {
	StringBuilder url = new StringBuilder(REST_SERVICE);
	url.append("?latitude=").append(String.valueOf(coordinates.latitude())).append("&longitude=").append(coordinates.longitude());
	HttpRequest request = HttpRequest.newBuilder()
		.uri(URI.create(url.toString()))
		.GET() 
		.build();

	HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
	int responseCode = response.statusCode();
	if (responseCode != 200) {
	    throw new IOException("Server responded with response code " + responseCode);
	} else {
	    String json;
	    AddressRequest result = null;
	    json = response.body();
	    if (json != null && !json.isBlank()) {
		try {
		    result = (AddressRequest) JSONUtilsFactory.getInstance().fromJSON(json, AddressRequest.class);
		} catch (Exception e) {
		    throw new IOException("Error parsing JSON " + e.getMessage() + " for " + json);
		}
	    }
	    return result;
	}
    }

}
