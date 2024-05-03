package com.jeltechnologies.photos.background.sftp.server;

public interface SFTPEventListenerMBean {
    String getLastEvent();
    String getUser();
    int getPort();
    int getFilesChanged();
}
