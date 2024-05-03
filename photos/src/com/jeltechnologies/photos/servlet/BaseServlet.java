package com.jeltechnologies.photos.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.utils.JSONUtilsFactory;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BaseServlet extends HttpServlet {
    private static final long serialVersionUID = 6038480827616267060L;
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServlet.class);
    private static final int LOCAL_NAS_PORT = Environment.INSTANCE.getConfig().getLocalNASPort();

    public String getUserName(HttpServletRequest request) {
	return RoleModel.getUser(request).name();
    }
    
    protected static boolean isLocalNetworkRequest(HttpServletRequest request) {
	int port = request.getServerPort();
	return port == LOCAL_NAS_PORT;
    }

    protected void respondJson(HttpServletResponse response, Object info) throws IOException {
	respondJson(response, info, false);
    }

    protected void respondJson(HttpServletResponse response, Object info, boolean log) throws IOException {
	try {
	    String json = JSONUtilsFactory.getInstance().toJSON(info);
	    if (log) {
		LOGGER.info(json);
	    }
	    if (json != null) {
		response.setHeader("Content-Type", "application/json");
		respondText(response, json);
	    } else {
		throw new IOException("Cannot convert to JSON: " + info);
	    }
	} catch (Exception e) {
	    throw new IOException("Cannot create JSON from command", e);
	}
    }

    protected Object getJsonFromBody(HttpServletRequest request, @SuppressWarnings("rawtypes") Class clazz) throws Exception {
	String body = getBody(request);
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("JSON body: " + body);
	}
	Object result = JSONUtilsFactory.getInstance().fromJSON(body, Object.class);
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("JSON object: " + result);
	}
	return result;
    }

    protected static int respondBinaryFile(HttpServletResponse response, ServletContext context, File file) throws IOException {
	return respondBinaryFile(response, context, file, false);
    }

    protected static int respondBinaryFile(HttpServletResponse response, ServletContext context, File file, boolean attachment) throws IOException {
	int responseCode;
	if (file.exists() && file.isFile()) {
	    response.setHeader("Content-Source", context.getMimeType(file.getCanonicalPath()));
	    response.setHeader("Content-Length", String.valueOf(file.length()));
	    if (attachment) {
		response.setHeader("Content-disposition", "attachment; filename=\"" + file.getName() + "\"");
	    } else {
		response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
	    }
	    Files.copy(file.toPath(), response.getOutputStream());
	    responseCode = HttpServletResponse.SC_OK;
	    response.setStatus(responseCode);
	} else {
	    responseCode = HttpServletResponse.SC_NOT_FOUND;
	    response.sendError(responseCode);
	}
	return responseCode;
    }

    protected void respondBinary(HttpServletResponse response, ServletContext context, byte[] imagePayload) throws IOException {
	if (imagePayload != null && imagePayload.length > 0) {
	    ServletOutputStream out = null;
	    try {
		out = response.getOutputStream();
		out.write(imagePayload, 0, imagePayload.length);
		response.setStatus(HttpServletResponse.SC_OK);
	    } finally {
		if (out != null) {
		    out.close();
		}
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    }
	} else {
	    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}
    }

    protected void respondText(HttpServletResponse httpServletResponse, String responseBody) throws IOException {
	if (responseBody == null) {
	    httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
	} else {
	    httpServletResponse.setCharacterEncoding("UTF-8");
	    httpServletResponse.getWriter().append(responseBody);
	    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
	}
    }

    protected String getBody(HttpServletRequest request) throws IOException {
	StringBuilder builder = new StringBuilder();
	String line = null;
	BufferedReader reader = null;
	try {
	    reader = request.getReader();
	    while ((line = reader.readLine()) != null) {
		builder.append(line);
	    }
	} finally {
	    if (reader != null) {
		reader.close();
	    }
	}
	return builder.toString();
    }
}
