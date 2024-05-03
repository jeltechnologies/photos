package com.jeltechnologies.photos.picures.map;

import java.io.Serializable;

public class Country implements Serializable {
    private static final long serialVersionUID = 8707769367646128402L;
    private String code;
    private String name;

    public String getCode() {
	return code;
    }

    public void setCode(String code) {
	this.code = code;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    @Override
    public String toString() {
	return "Country [code=" + code + ", name=" + name + "]";
    }
}
