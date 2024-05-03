package com.jeltechnologies.photos.config.yaml;

import java.io.Serializable;

public class SFTPServerAccount implements Serializable {
    private static final long serialVersionUID = 854796427088153793L;
    private int port;
    private String user;
    private String password;

    public void setPort(int port) {
	this.port = port;
    }

    public void setUser(String user) {
	this.user = user;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public int getPort() {
	return port;
    }

    public String getUser() {
	return user;
    }

    public String getPassword() {
	return password;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("SFTPServerData [port=");
	builder.append(port);
	builder.append(", user=");
	builder.append(user);
	builder.append(", password=");
	builder.append("****");
	builder.append("]");
	return builder.toString();
    }

}
