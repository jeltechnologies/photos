package com.jeltechnologies.photos.pictures;

import java.io.Serializable;

import com.jeltechnologies.photos.datatypes.Dimension;

public class Thumbnail implements Serializable {
    private static final long serialVersionUID = -4989118826531118485L;
    private Dimension dimension;
    private String relativeFileName;

    public Thumbnail() {
    }
    
    public Thumbnail(Dimension dimension, String relativeFileName) { 
	this.dimension = dimension;
	this.relativeFileName = relativeFileName;
    }

    public Dimension getDimension() {
	return dimension;
    }

    public void setDimension(Dimension dimension) {
	this.dimension = dimension;
    }

    public String getRelativeFileName() {
	return relativeFileName;
    }

    public void setRelativeFileName(String relativeFileName) {
	this.relativeFileName = relativeFileName;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("Thumbnail [dimension=");
	builder.append(dimension);
	builder.append(", relativeFileName=");
	builder.append(relativeFileName);
	builder.append("]");
	return builder.toString();
    }
    
    

}
