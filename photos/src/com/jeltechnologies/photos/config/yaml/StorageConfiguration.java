package com.jeltechnologies.photos.config.yaml;

import java.io.File;
import java.io.Serializable;

public class StorageConfiguration implements Serializable {
    private static final long serialVersionUID = 2221424706526469138L;
    private File originals;
    private File cache;

    public File getOriginals() {
	return originals;
    }

    public void setOriginals(File originals) {
	this.originals = originals;
    }

    public File getCache() {
        return cache;
    }

    public void setCache(File cache) {
        this.cache = cache;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("StorageConfiguration [originals=");
	builder.append(originals);
	builder.append(", cache=");
	builder.append(cache);
	builder.append("]");
	return builder.toString();
    }
}
