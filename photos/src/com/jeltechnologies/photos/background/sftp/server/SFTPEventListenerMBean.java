package com.jeltechnologies.photos.background.sftp.server;

public interface SFTPEventListenerMBean {
    String getLastEvent();
    int getPort();
    int getFilesChanged();
}
