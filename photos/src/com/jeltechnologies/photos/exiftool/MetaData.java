package com.jeltechnologies.photos.exiftool;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public record MetaData(List<MetaTag> tags) {
    
    public String getValue(String name)  {
	MetaTag found = null;
	Iterator<MetaTag> iterator = tags.iterator();
	while (found == null && iterator.hasNext()) {
	    MetaTag current = iterator.next();
	    if (current.name().equals(name)) {
		found = current;
	    }
	}
	String result = null;
	if (found != null) {
	    result = found.value();
	}
	return result;
    }

    @Override
    public int hashCode() {
	return Objects.hash(tags);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	MetaData other = (MetaData) obj;
	boolean same = true;
	Iterator<MetaTag> iterator = tags.iterator();
	while (iterator.hasNext() && same) {
	    same = other.tags.contains(iterator.next());
	}
	Iterator<MetaTag> otherIterator = other.tags.iterator();
	while (otherIterator.hasNext() && same) {
	    same = other.tags.contains(otherIterator.next());
	}
	return same;
    }
    
    
    
    
}
