package com.jeltechnologies.photos.background.sftp.server;

import java.io.File;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.DirectoryHandle;
import org.apache.sshd.server.subsystem.sftp.FileHandle;
import org.apache.sshd.server.subsystem.sftp.Handle;
import org.apache.sshd.server.subsystem.sftp.SftpEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFTPEventListener implements SftpEventListener, SFTPEventListenerMBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(SFTPEventListener.class);

    private List<FileChangeListener> listeners = new ArrayList<FileChangeListener>();

    private final int port;

    private int filesChanged = 0;

    private String lastEvent;

    public SFTPEventListener(int port) {
	this.port = port;
	if (LOGGER.isTraceEnabled()) {
	    lastEvent = "Instantiated";
	}
    }

    public void addListener(FileChangeListener listener) {
	this.listeners.add(listener);
    }

    @Override
    public String getLastEvent() {
	return lastEvent;
    }

    @Override
    public int getPort() {
	return port;
    }

    @Override
    public int getFilesChanged() {
	return filesChanged;
    }

    private void fileWasChanged(Path path) {
	filesChanged++;
	for (FileChangeListener listener : listeners) {
	    listener.fileChanged(path.toFile());
	}
    }

    @Override
    public void initialized(ServerSession serverSession, int version) {
	lastEvent = "Initialized " + serverSession.getUsername();
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

    @Override
    public void destroying(ServerSession serverSession) {
	lastEvent = "Destroying";
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

    @Override
    public void open(ServerSession serverSession, String remoteHandle, Handle localHandle) {
	lastEvent = "Open";
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

    @Override
    public void read(ServerSession serverSession, String remoteHandle, DirectoryHandle localHandle, Map<String, Path> entries) {
	lastEvent = "Read";
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

    @Override
    public void blocking(ServerSession serverSession, String remoteHandle, FileHandle localHandle, long offset, long length, int mask) {
	lastEvent = "Blocking";
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

    @Override
    public void blocked(ServerSession serverSession, String remoteHandle, FileHandle localHandle, long offset, long length, int mask, Throwable thrown) {
	lastEvent = "Blocked";
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

    @Override
    public void unblocking(ServerSession serverSession, String remoteHandle, FileHandle localHandle, long offset, long length) {
	lastEvent = "Unblocking";
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

    @Override
    public void close(ServerSession serverSession, String remoteHandle, Handle localHandle) {
	lastEvent = "Close";
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
	File closedFile = localHandle.getFile().toFile();
	if (closedFile.exists() && closedFile.isFile()) {
	    for (FileChangeListener listener : listeners) {
		listener.fileChanged(closedFile);
	    }
	}
    }

    @Override
    public void creating(ServerSession serverSession, Path path, Map<String, ?> attrs) throws UnsupportedOperationException {
	lastEvent = "Creating folder: " + path;
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

    @Override
    public void created(ServerSession serverSession, Path path, Map<String, ?> attrs, Throwable thrown) {
	lastEvent = "Created folder: " + path;
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
	String username = serverSession.getUsername();
	LOGGER.info(String.format("User %s created: \"%s\"", username, path.toString()));
    }

    @Override
    public void moving(ServerSession serverSession, Path path, Path path1, Collection<CopyOption> collection) {
	lastEvent = "Moving: " + path;
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

    @Override
    public void moved(ServerSession serverSession, Path source, Path destination, Collection<CopyOption> collection, Throwable throwable) {
	lastEvent = "Moved: " + source;
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
	fileWasChanged(destination);
    }

    @Override
    public void removing(ServerSession serverSession, Path path) {
	lastEvent = "Removing: " + path;
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

    @Override
    public void removed(ServerSession serverSession, Path path, Throwable thrown) {
	lastEvent = "Removed: " + path;
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
	fileWasChanged(path);
    }

    @Override
    public void linking(ServerSession serverSession, Path source, Path target, boolean symLink) throws UnsupportedOperationException {
	lastEvent = "Linking: " + source;
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
	LOGGER.warn(String.format("Blocked user %s attempt to create a link to \"%s\" at \"%s\"", serverSession.getUsername(), target.toString(),
		source.toString()));
	throw new UnsupportedOperationException("Creating links is not permitted");
    }

    @Override
    public void linked(ServerSession serverSession, Path source, Path target, boolean symLink, Throwable thrown) {
	lastEvent = "Linked: " + source;
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

    @Override
    public void modifyingAttributes(ServerSession serverSession, Path path, Map<String, ?> attrs) {
	lastEvent = "modifyingAttributes: " + path;
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

    @Override
    public void modifiedAttributes(ServerSession serverSession, Path path, Map<String, ?> attrs, Throwable thrown) {
	lastEvent = "modifiedAttributes: " + path;
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace(lastEvent);
	}
    }

}
