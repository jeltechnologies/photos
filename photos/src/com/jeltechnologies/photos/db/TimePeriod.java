package com.jeltechnologies.photos.db;

import java.io.Serializable;
import java.time.LocalDate;

public class TimePeriod implements Serializable {
    private static final long serialVersionUID = -6784158345852575047L;
    private LocalDate from;
    private LocalDate to;
    
    public TimePeriod() {
    }
    
    public TimePeriod(LocalDate from, LocalDate to) {
	this.from = from;
	this.to = to;
    }

    public LocalDate getFrom() {
	return from;
    }

    public void setFrom(LocalDate from) {
	this.from = from;
    }

    public LocalDate getTo() {
	return to;
    }

    public void setTo(LocalDate to) {
	this.to = to;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((from == null) ? 0 : from.hashCode());
	result = prime * result + ((to == null) ? 0 : to.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	TimePeriod other = (TimePeriod) obj;
	if (from == null) {
	    if (other.from != null)
		return false;
	} else if (!from.equals(other.from))
	    return false;
	if (to == null) {
	    if (other.to != null)
		return false;
	} else if (!to.equals(other.to))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("TimePeriod [from=");
	builder.append(from);
	builder.append(", to=");
	builder.append(to);
	builder.append("]");
	return builder.toString();
    }

}
