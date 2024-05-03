package com.jeltechnologies.photos.utils;

public interface JSONUtils {
    public String toJSON(Object object) throws Exception;
    public Object fromJSON(String json, Class<?> clazz) throws Exception;
}
