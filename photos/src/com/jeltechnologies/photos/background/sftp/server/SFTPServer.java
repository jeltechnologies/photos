package com.jeltechnologies.photos.background.sftp.server;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
    private final String serverName;
    private SshServer sshd;
    private final SFTPEventListener eventListener;
    private boolean started = false;
    private final SFTPPasswordAuthenticator passwordAuthenticator;
    private VirtualFileSystemFactory fileSystemFactory;
    
    public record User(String user, String password) {};
	
    public SFTPServer(int port, File homeFolder, List<User> users) throws IOException{
	this.port = port;
	this.passwordAuthenticator = new SFTPPasswordAuthenticator(users);
	this.fileSystemFactory = new VirtualFileSystemFactory();
	this.fileSystemFactory.setDefaultHomeDir(homeFolder.toPath());
	createFolderIfNotExists(homeFolder);
	for (User user : users) {
	    File userFolder = new File(homeFolder, user.user());
	    createFolderIfNotExists(userFolder);
	    this.fileSystemFactory.setUserHomeDir(user.user, userFolder.toPath());
	}
	this.serverName = "SFTP server on port " + port;
	this.eventListener = new SFTPEventListener(port);
	JMXUtils.getInstance().registerMBean(serverName, "SFTP", eventListener);
	start();
    }
    
    private void createFolderIfNotExists(File folder) {
	if (!folder.isDirectory()) {
	    boolean created = folder.mkdirs();
	    if (created) {
		LOGGER.info("Created folder " + folder);
	    } else {
		LOGGER.warn("Cannot create folder " + folder);
	    }
	}
    }

    public String getServerName() {
	return this.serverName;
    }
    
    public void start() throws IOException {
	sshd = SshServer.setUpDefaultServer();
	sshd.setPasswordAuthenticator(passwordAuthenticator);	
	sshd.setFileSystemFactory(fileSystemFactory);
	sshd.setPort(port);
	sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("host.ser")));
	SftpSubsystemFactory sftpSubsystemFactory = new SftpSubsystemFactory();
	sshd.setSubsystemFactories(Collections.singletonList(sftpSubsystemFactory));
	sftpSubsystemFactory.addSftpEventListener(eventListener);
	sshd.start();
	started = true;
	LOGGER.info(serverName + " started listening on port " + port);
    }

    public void stop() throws IOException {
	if (sshd != null) {
	    sshd.close(false);
	}
	LOGGER.info(serverName + " stopped");
    }
    
    public void addListener(FileChangeListener listener) {
	if (!started) {
	    throw new IllegalStateException("Server not yet started");
	}
	eventListener.addListener(listener);
    }

}
