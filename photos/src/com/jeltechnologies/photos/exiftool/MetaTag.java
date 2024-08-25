package com.jeltechnologies.photos.exiftool;

public record MetaTag (String name, String value) implements Comparable<MetaTag> {
    @Override
    public int compareTo(MetaTag o) {
	return name.compareTo(o.name);
    }
}
