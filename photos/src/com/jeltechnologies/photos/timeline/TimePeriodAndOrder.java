package com.jeltechnologies.photos.timeline;

import java.io.Serializable;
import java.util.Objects;

import com.jeltechnologies.photos.db.Query;

public class TimePeriodAndOrder implements Serializable{
    private static final long serialVersionUID = 8441339880504023776L;
    private final Query query;

    public TimePeriodAndOrder(Query query) {
	super();
	this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public int hashCode() {
	return Objects.hash(query);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	TimePeriodAndOrder other = (TimePeriodAndOrder) obj;
	return Objects.equals(query, other.query);
    }

}
    

