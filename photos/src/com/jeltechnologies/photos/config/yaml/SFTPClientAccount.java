package com.jeltechnologies.photos.config.yaml;

import java.io.Serializable;

public class SFTPClientAccount implements Serializable {
    private static final long serialVersionUID = -4808396806416501799L;
    private String host;
    private int port;
    private String rootfolder;
    private String user;
    private String password;

    public String getHost() {
	return host;
    }

    public void setHost(String host) {
	this.host = host;
    }

    public int getPort() {
	return port;
    }

    public void setPort(int port) {
	this.port = port;
    }

    public String getRootfolder() {
	return rootfolder;
    }

    public void setRootfolder(String rootfolder) {
	this.rootfolder = rootfolder;
    }

    public String getUser() {
	return user;
    }

    public void setUser(String user) {
	this.user = user;
    }

    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public static long getSerialversionuid() {
	return serialVersionUID;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("SFTPClientData [host=");
	builder.append(host);
	builder.append(", port=");
	builder.append(port);
	builder.append(", rootfolder=");
	builder.append(rootfolder);
	builder.append(", user=");
	builder.append(user);
	builder.append(", password=");
	builder.append("********");
	builder.append("]");
	return builder.toString();
    }

}
