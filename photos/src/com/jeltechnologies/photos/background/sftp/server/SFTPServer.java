package com.jeltechnologies.photos.background.sftp.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.photos.utils.JMXUtils;

public class SFTPServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SFTPServer.class);
    private final int port;
    private final String user;
    private final String password;
    private final Path homeFolder;
    private final String serverName;
    private SshServer sshd;
    private final SFTPEventListener eventListener;
    private boolean started = false;

    public SFTPServer(int port, String user, String password, File homeFolder) throws IOException{
	this.port = port;
	this.user = user;
	this.password = password;
	if (!homeFolder.isDirectory()) {
	    boolean created = homeFolder.mkdirs();
	    if (created) {
		LOGGER.info("Created folder " + homeFolder);
	    } else {
		LOGGER.warn("Cannot create folder " + homeFolder);
	    }
	}
	this.homeFolder = homeFolder.toPath();
	this.serverName = "SFTP server on port " + port + " for user " + user;
	this.eventListener = new SFTPEventListener(user, port);
	JMXUtils.getInstance().registerMBean(serverName, "SFTP", eventListener);
	start();
    }

    public void addListener(FileChangeListener listener) {
	if (!started) {
	    throw new IllegalStateException("Server not yet started");
	}
	eventListener.addListener(listener);
    }

    public String getServerName() {
	return this.serverName;
    }

    private void start() throws IOException {
	sshd = SshServer.setUpDefaultServer();
	VirtualFileSystemFactory fileSystemFactory = new VirtualFileSystemFactory();
	fileSystemFactory.setDefaultHomeDir(homeFolder);
	sshd.setFileSystemFactory(fileSystemFactory);
	sshd.setPort(port);
	sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("host.ser")));
	SftpSubsystemFactory sftpSubsystemFactory = new SftpSubsystemFactory();
	sshd.setSubsystemFactories(Collections.singletonList(sftpSubsystemFactory));
	sshd.setPasswordAuthenticator((username, password, session) -> username.equals(this.user) && password.equals(this.password));
	
	sftpSubsystemFactory.addSftpEventListener(eventListener);
	sshd.start();
	started = true;
	LOGGER.info(serverName + " started");
    }

    public void stop() throws IOException {
	if (sshd != null) {
	    sshd.close(false);
	}
	LOGGER.info(serverName + " stopped");
    }

}
