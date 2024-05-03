package com.jeltechnologies.photos.background.sftp.client;

public class SFTPClientConnectionData {
    private String rootFolder;
    private String user;
    private String password;
    private String host;
    private int port;

    public SFTPClientConnectionData() {
    }
    
    public SFTPClientConnectionData(String host, int port, String rootFolder, String user, String password) {
	this.host = host;
	this.port = port;
	this.user = user;
	this.password = password;
	this.rootFolder = rootFolder;
    }

    public String getRootFolder() {
	return rootFolder;
    }

    public void setRootFolder(String rootFolder) {
	this.rootFolder = rootFolder;
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
}