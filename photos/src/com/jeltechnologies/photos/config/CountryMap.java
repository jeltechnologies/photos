package com.jeltechnologies.photos.config;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.jeltechnologies.geoservices.datamodel.Country;
import com.jeltechnologies.photos.utils.JSONUtilsFactory;

public class CountryMap {
    private Map<String, Country> countries = new HashMap<String, Country>();

    public CountryMap(InputStream stream) throws IOException {
	Country[] countryArray = parseJson(stream);
	for (Country country : countryArray) {
	    countries.put(country.code(), country);
	}
    }

    public Country getCountry(String code) {
	return countries.get(code);
    }

    private Country[] parseJson(InputStream in) throws IOException {
	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	String line;
	StringBuilder content = new StringBuilder();
	while ((line = br.readLine()) != null) {
	    content.append(line);
	}
	try {
	    return (Country[]) JSONUtilsFactory.getInstance().fromJSON(content.toString(), Country[].class);
	} catch (Exception e) {
	    throw new IOException("Cannot parse JSON from country list", e);
	}
    }
}
