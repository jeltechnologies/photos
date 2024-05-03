package com.jeltechnologies.photos.utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JSONUtilsJackson implements JSONUtils {
    private static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static Logger LOGGER = LoggerFactory.getLogger(JSONUtils.class);

    public String toJSON(Object object) throws JsonProcessingException {
	ObjectMapper mapper = new ObjectMapper();
	mapper.registerModule(new JavaTimeModule());
	mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
	    private static final long serialVersionUID = -3754772346136842990L;
	    @Override
	    public boolean hasIgnoreMarker(final AnnotatedMember m) {
		List<String> exclusions = Arrays.asList("role");
		return exclusions.contains(m.getName()) || super.hasIgnoreMarker(m);
	    }
	});
	mapper.setDateFormat(new SimpleDateFormat(ISO_8601));
	mapper.setSerializationInclusion(Include.NON_NULL);
	String json;
	json = mapper.writeValueAsString(object);
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(json);
	}
	return json;
    }

    public Object fromJSON(String json, Class<?> clazz) throws Exception {
	ObjectMapper mapper = new ObjectMapper();
	mapper.registerModule(new JavaTimeModule());
	mapper.setDateFormat(new SimpleDateFormat(ISO_8601));
	return mapper.readValue(json, clazz);
    }

}
