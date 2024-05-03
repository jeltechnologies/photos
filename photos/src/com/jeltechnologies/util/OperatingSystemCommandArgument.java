package com.jeltechnologies.util;

public class OperatingSystemCommandArgument {
    public enum Quotes {
	NONE, SINGLE, DOUBLE
    };

    private final String argument;
    private final Quotes quotes;

    public OperatingSystemCommandArgument(String argument) {
	this.argument = argument;
	this.quotes = Quotes.NONE;
    }

    public OperatingSystemCommandArgument(String argument, Quotes quotes) {
	this.argument = argument;
	this.quotes = quotes;
    }

    public String getArgument() {
	return argument;
    }

    public Quotes getQuotes() {
	return quotes;
    }

    public String toString() {
	String result;
	switch (quotes) {
	    case DOUBLE: {
		result = "\"" + argument + "\"";
		break;
	    }
	    case SINGLE: {
		result = "'" + argument + "'";
		break;
	    }
	    default: {
		result = argument;
		break;
	    }
	}
	return result;
    }

}
